package com.webank.wecube.platform.gateway.route;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.dto.GenericResponseDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;
import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteProperties;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiremock.org.apache.commons.lang3.StringUtils;

@Service
public class DynamicRouteConfigurationService implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteConfigurationService.class);

    @Resource
    private RouteDefinitionRepository routeDefinitionRepository;

    @Autowired
    private DynamicRouteProperties dynamicRouteProperties;

    private ApplicationEventPublisher publisher;

    private Map<String, List<RouteItemInfoDto>> routeItems = new ConcurrentHashMap<>();

    private volatile boolean isDynamicRouteLoaded = false;
    
    private ReentrantLock loadLock = new ReentrantLock();

    private ReentrantLock refreshLock = new ReentrantLock();

    private volatile Disposable loadDisposable = null;

    @PostConstruct
    public void afterPropertiesSet() {

        loadDisposable = Flux.interval(Duration.ofSeconds(dynamicRouteProperties.getRetryIntervalOfSeconds()))
                .subscribe(this::loadRoutes);
        Flux.interval(Duration.ofMinutes(dynamicRouteProperties.getRefreshIntervalOfMinutes()))
                .subscribe(this::refreshRoutes);

        if (log.isInfoEnabled()) {
            log.info("{} applied", DynamicRouteConfigurationService.class.getSimpleName());
        }
    }
    
    public List<RouteItemInfoDto> getAllLoadedRouteItemInfoDtos() {
        List<DynamicRouteItemInfo> itemInfos = DynamicRouteItemInfoHolder.findAll();
        List<RouteItemInfoDto> itemDtos = new ArrayList<>();
        itemInfos.forEach(info -> {
            RouteItemInfoDto dto = new RouteItemInfoDto();
            dto.setHost(info.getHost());
            dto.setName(info.getName());
            dto.setPort(info.getPort());
            dto.setSchema(info.getHttpSchema());

            dto.setAvailable(info.isAvailable());
            dto.setCreateTime(info.getCreateTime());
            dto.setLastModifyTime(info.getLastModifiedTime());
            dto.setItemId(info.getItemId());

            itemDtos.add(dto);
        });

        return itemDtos;
    }

    public void deleteRouteItem(String routeName) {
        if (log.isInfoEnabled()) {
            log.info("to delete route item:{}", routeName);
        }
        String routeId = String.format("%s#1", routeName);
        String result = delete(routeId);
        if (log.isInfoEnabled()) {
            log.info("delete result:{}", result);
        }
    }
    
    protected void refreshRoutes(Long time) {
        log.debug("refresh routes ---- {}", time);

        if (!isDynamicRouteLoaded) {
            return;
        }

        refreshLock.lock();

        try {
            doRefreshRoutes();
        } finally {
            refreshLock.unlock();
        }
    }

    
    protected void loadRoutes(Long time) {
        log.debug("load routes  ------  {}", time);
        if (!loadLock.tryLock()) {
            log.info("cannot acquire the lock.");
            return;
        }
        try {
            if (isDynamicRouteLoaded) {
                log.info("isDynamicRouteLoaded:{}, isDisposed:{}", isDynamicRouteLoaded, loadDisposable.isDisposed());

                if (!loadDisposable.isDisposed()) {
                    log.info("to dispose load tasks.");
                    loadDisposable.dispose();
                }

                return;
            }

            log.info("try to load dynamic routes --- {}", time);
            doLoadRoutes();
        } finally {
            loadLock.unlock();
        }
    }
    
    protected void doRefreshRoutes() {
        Mono<RouteConfigInfoResponseDto> mono = fetchAllRouteItemsWithWebClient();
        mono.doOnError(this::handleRefreshErrors).subscribe(this::handleRefreshRouteConfigInfoResponseDto);
    }
    
    private void handleRefreshRouteConfigInfoResponseDto(RouteConfigInfoResponseDto respDto) {
        List<RouteItemInfoDto> routeItemInfoDtos = respDto.getData();

        List<DynamicRouteItemInfo> routeItemInfos = new ArrayList<>();

        for (RouteItemInfoDto d : routeItemInfoDtos) {
            DynamicRouteItemInfo info = new DynamicRouteItemInfo();
            info.setHost(d.getHost());
            info.setHttpSchema(d.getSchema());
            info.setName(d.getName());
            info.setPort(d.getPort());

            routeItemInfos.add(info);
            
            tryAddRouteDefinition(d);
        }

        DynamicRouteItemInfoHolder.refreshDynamicRouteItemInfos(routeItemInfos);
    }
    
    private void tryAddRouteDefinition(RouteItemInfoDto routeItemInfoDto) {
    	if( routeItemInfoDto == null ) {
    		return;
    	}
    	
    	if( StringUtils.isBlank(routeItemInfoDto.getName()) ) {
    		return;
    	}
    	
    	if (this.routeItems.containsKey(routeItemInfoDto.getName())) {
    		return;
    	}
    	
    	this.buildRouteDefinition(routeItemInfoDto.getName(), routeItemInfoDto);
    	
    	
    	List<RouteItemInfoDto> items = new ArrayList<>();
    	items.add(routeItemInfoDto);
    	
    	this.routeItems.put(routeItemInfoDto.getName(), items);
    	
    	log.info("REFRESH loaded route definition:{}", routeItemInfoDto.getName());
    	
    	return;
    	
    }
    
    private void handleRefreshErrors(Throwable e) {
        log.info("errors while refreshing routes...", e);
    }

    protected void doLoadRoutes() {
        log.info("start to load routes...");

        loadLock.lock();

        try {

            Mono<RouteConfigInfoResponseDto> mono = fetchAllRouteItemsWithWebClient();
            mono.doOnError(this::handleLoadErrors).doOnSuccess(this::handleLoadSuccess)
                    .subscribe(this::handleRouteConfigInfoResponseDto);

        } finally {
            loadLock.unlock();
        }

    }
    
    private void handleLoadSuccess(RouteConfigInfoResponseDto dto) {
        log.info("on success...with dto {}", dto);

    }

    private void handleLoadErrors(Throwable e) {
        log.info("errors while loading routes...", e);
        isDynamicRouteLoaded = false;
    }

    protected void initRouteItems() {
        int count = 0;
        for (String name : routeItems.keySet()) {
            List<RouteItemInfoDto> dtos = routeItems.get(name);
            if (dtos == null || dtos.isEmpty()) {
                continue;
            }

            buildRouteDefinition(name, dtos.get(0));
            count++;
        }

        log.info("add {} route definitions", count);
    }
    
    
    
    private void handleRouteConfigInfoResponseDto(RouteConfigInfoResponseDto dto) {
        loadLock.lock();
        try {
            List<RouteItemInfoDto> routeItemInfoDtos = dto.getData();
            log.info("size:{}", routeItemInfoDtos.size());

            List<DynamicRouteItemInfo> routeItemInfos = new ArrayList<>();

            for (RouteItemInfoDto d : routeItemInfoDtos) {
                String name = d.getName();
                List<RouteItemInfoDto> itemList = routeItems.get(name);
                if (itemList == null) {
                    itemList = new ArrayList<>();
                    routeItems.put(name, itemList);
                }

                itemList.add(d);

                DynamicRouteItemInfo info = new DynamicRouteItemInfo();
                info.setHost(d.getHost());
                info.setHttpSchema(d.getSchema());
                info.setName(d.getName());
                info.setPort(d.getPort());

                routeItemInfos.add(info);
            }

            initRouteItems();

            DynamicRouteItemInfoHolder.refreshDynamicRouteItemInfos(routeItemInfos);
            isDynamicRouteLoaded = true;
        } finally {
            loadLock.unlock();
        }
    }

    protected void buildRouteDefinition(String name, RouteItemInfoDto dto) {
        RouteDefinition rd = new RouteDefinition();
        rd.setId(name + "#1");
        String urlStr = String.format("http://%s:%s", dto.getHost(), dto.getPort());
        URI uri = UriComponentsBuilder.fromHttpUrl(urlStr).build().toUri();
        rd.setUri(uri);

        PredicateDefinition pd = new PredicateDefinition();
        pd.setName("Path");
        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", String.format("/%s/**", name));
        pd.setArgs(predicateParams);
        rd.setPredicates(Arrays.asList(pd));

        List<FilterDefinition> filters = new ArrayList<>();

        FilterDefinition fdDynamicRoute = new FilterDefinition();
        fdDynamicRoute.setName("DynamicRoute");
        fdDynamicRoute.addArg("enabled", "true");

        filters.add(fdDynamicRoute);

        if (dynamicRouteProperties.isEnableRetry()) {

            FilterDefinition fdRetry = new FilterDefinition();
            fdRetry.setName("ExRetry");
            fdRetry.addArg("retries", "10");
            fdRetry.addArg("series", "SERVER_ERROR");
            fdRetry.addArg("methods", "GET,POST,PUT,DELETE");
            fdRetry.addArg("exceptions", "java.io.IOException,java.net.ConnectException");

            filters.add(fdRetry);
        }

        rd.setFilters(filters);

        add(rd);

        log.info("### route added:{} {} {}", dto.getName(), dto.getHost(), dto.getPort());
    }

    public List<RouteItemInfoDto> listAllRouteItems() {
        Flux<RouteDefinition> flux = routeDefinitionRepository.getRouteDefinitions();
        List<RouteItemInfoDto> items = new ArrayList<>();

        flux.subscribe((rd) -> {
            RouteItemInfoDto r = new RouteItemInfoDto();
            r.setName(rd.getId());

            String uri = rd.getUri().toString();
            String schema = uri.substring(0, uri.indexOf("://"));
            uri = uri.substring(uri.indexOf("://") + 3);
            String host = uri.substring(0, uri.indexOf(":"));
            String port = uri.substring(uri.indexOf(":") + 1);

            r.setHost(host);
            r.setPort(port);
            r.setSchema(schema);

            items.add(r);

        });

        return items;
    }
    
    protected Mono<RouteConfigInfoResponseDto> fetchAllRouteItemsWithWebClient() {
        String url = dynamicRouteProperties.getRouteConfigServer() + dynamicRouteProperties.getRouteConfigUri();
        Mono<RouteConfigInfoResponseDto> bodyMono = WebClient.create().get().uri(url)
                .header("Content-Type", "application/json").accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", dynamicRouteProperties.getRouteConfigAccessKey()))
                .retrieve().bodyToMono(RouteConfigInfoResponseDto.class);

        return bodyMono;
    }

    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public void pushRouteItem(String name, List<RouteItemInfoDto> newRouteItems) {
        if (StringUtils.isBlank(name)) {
            log.info("name is blank.");
            return;
        }

        if (newRouteItems == null || newRouteItems.isEmpty()) {
            log.info("route items is empty for name:{}", name);
            return;
        }

        if (this.routeItems.containsKey(name)) {
            log.info("route items already exist for name:{}", name);
            List<RouteItemInfoDto> storedRouteItems = this.routeItems.get(name);
            storedRouteItems.addAll(newRouteItems);
            
            List<DynamicRouteItemInfo> routeItemInfos = new ArrayList<>();

            for (RouteItemInfoDto d : newRouteItems) {
                DynamicRouteItemInfo info = new DynamicRouteItemInfo();
                info.setHost(d.getHost());
                info.setHttpSchema(d.getSchema());
                info.setName(d.getName());
                info.setPort(d.getPort());

                routeItemInfos.add(info);
            }
            
            DynamicRouteItemInfoHolder.addDynamicRouteItemInfos(routeItemInfos);
            
            return;
        }

        doPushRouteItem(name, newRouteItems);

    }

    protected void doPushRouteItem(String name, List<RouteItemInfoDto> newRouteItems) {
        log.info("about to add route items for name {} size {}", name, routeItems.size());
        List<RouteItemInfoDto> routeItemsToPush = new ArrayList<>();
        routeItemsToPush.addAll(newRouteItems);
        this.routeItems.put(name, routeItemsToPush);
        buildRouteDefinition(name, newRouteItems.get(0));
    }

    public String add(RouteDefinition definition) {
        routeDefinitionRepository.save(Mono.just(definition)).subscribe();
        notifyChanged();
        return "success";
    }

    public String update(RouteDefinition definition) {
        try {
            this.routeDefinitionRepository.delete(Mono.just(definition.getId()));
        } catch (Exception e) {
            return "update fail,not find route  routeId: " + definition.getId();
        }
        try {
            routeDefinitionRepository.save(Mono.just(definition)).subscribe();
            notifyChanged();
            return "success";
        } catch (Exception e) {
            return "update route  fail";
        }

    }

    public String delete(String id) {
        try {
            this.routeDefinitionRepository.delete(Mono.just(id)).subscribe();

            notifyChanged();
            return "delete success";
        } catch (Exception e) {
            e.printStackTrace();
            return "delete fail";
        }

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    private static class RouteConfigInfoResponseDto extends GenericResponseDto<List<RouteItemInfoDto>> {

    }

}
