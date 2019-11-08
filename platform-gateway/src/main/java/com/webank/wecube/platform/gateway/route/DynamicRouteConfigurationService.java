package com.webank.wecube.platform.gateway.route;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.dto.GenericResponseDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;
import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteProperties;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiremock.org.apache.commons.lang3.StringUtils;

@Service
public class DynamicRouteConfigurationService implements ApplicationEventPublisherAware {

    private Logger log = LoggerFactory.getLogger(DynamicRouteConfigurationService.class);

    @Resource
    private RouteDefinitionRepository routeDefinitionRepository;

    @Autowired
    private DynamicRouteProperties dynamicRouteProperties;

    private ApplicationEventPublisher publisher;

    private Map<String, List<RouteItemInfoDto>> routeItems = new ConcurrentHashMap<>();

    @PostConstruct
    public void afterPropertiesSet() {
        if (log.isInfoEnabled()) {
            log.info("{} applied", DynamicRouteConfigurationService.class.getSimpleName());
        }

        try {
            loadRoutes();
        } catch (Exception e) {
            log.warn("#########################################");
            log.warn("failed to load default route items", e);

        }
    }

    protected void loadRoutes() {
        log.info("start to load routes...");

        List<RouteItemInfoDto> dtos = fetchAllRouteItems();

        for (RouteItemInfoDto d : dtos) {
            String name = d.getName();
            List<RouteItemInfoDto> itemList = routeItems.get(name);
            if (itemList == null) {
                itemList = new ArrayList<>();
                routeItems.put(name, itemList);
            }

            itemList.add(d);
        }

        initRouteItems();
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

    protected void buildRouteDefinition(String name, RouteItemInfoDto dto) {
        RouteDefinition rd = new RouteDefinition();
        rd.setId(name + "-1");
        String urlStr = String.format("http://%s:%s", dto.getHost(), dto.getPort());
        URI uri = UriComponentsBuilder.fromHttpUrl(urlStr).build().toUri();
        rd.setUri(uri);

        PredicateDefinition pd = new PredicateDefinition();
        pd.setName("Path");
        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", String.format("/%s/**", name));
        pd.setArgs(predicateParams);
        rd.setPredicates(Arrays.asList(pd));

        FilterDefinition fd = new FilterDefinition();
        fd.setName("DynamicRoute");
        fd.addArg("enabled", "true");

        rd.setFilters(Arrays.asList(fd));

        add(rd);

        log.info("### route added:{} {} {}", dto.getName(), dto.getHost(), dto.getPort());
    }
    
    public List<RouteItemInfoDto> listAllRouteItems(){
        Flux<RouteDefinition> flux =  routeDefinitionRepository.getRouteDefinitions();
        List<RouteItemInfoDto> items = new ArrayList<>();
        
        flux.subscribe((rd) -> {
            RouteItemInfoDto r = new RouteItemInfoDto();
            r.setName(rd.getId());
            r.setHost(rd.getUri().toString());
            r.setPort("");
            r.setSchema("http");
            
            items.add(r);
            
        });
        
        return items;
    }

    protected List<RouteItemInfoDto> fetchAllRouteItems() {
        RestTemplate client = new RestTemplate();

        String url = dynamicRouteProperties.getRouteConfigServer() + dynamicRouteProperties.getRouteConfigUri();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON_UTF8);
        header.add("Authorization", String.format("Bearer %s", dynamicRouteProperties.getRouteConfigAccessKey()));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(header);

        ResponseEntity<RouteConfigInfoResponseDto> responseEntity = client.exchange(url, HttpMethod.GET, httpEntity,
                RouteConfigInfoResponseDto.class);

        RouteConfigInfoResponseDto responseDto = responseEntity.getBody();

        List<RouteItemInfoDto> routeItemInfoDtos = responseDto.getData();
        if (log.isInfoEnabled()) {
            if (routeItemInfoDtos != null) {
                routeItemInfoDtos.forEach(ri -> {
                    log.info("Route Item:{}", ri);
                });
            }
        }

        if (routeItemInfoDtos == null) {
            routeItemInfoDtos = new ArrayList<>();
        }

        return routeItemInfoDtos;
    }

    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public void pushRouteItem(String name, List<RouteItemInfoDto> routeItems) {
        if (StringUtils.isBlank(name)) {
            log.error("name is blank.");
            return;
        }

        if (routeItems == null || routeItems.isEmpty()) {
            log.error("route items is empty for name:{}", name);
            return;
        }

        if (routeItems.contains(name)) {
            log.error("route items already exist for name:{}", name);
            return;
        }
        
        doPushRouteItem(name, routeItems);

    }

    protected void doPushRouteItem(String name, List<RouteItemInfoDto> routeItems) {
        log.info("about to add route items for name {} size {}", name, routeItems.size());
        buildRouteDefinition(name, routeItems.get(0));
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
            this.routeDefinitionRepository.delete(Mono.just(id));

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
