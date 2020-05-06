package com.webank.wecube.platform.gateway.route;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.dto.GenericResponseDto;
import com.webank.wecube.platform.gateway.dto.HttpDestinationDto;
import com.webank.wecube.platform.gateway.dto.MvcContextRouteConfigDto;
import com.webank.wecube.platform.gateway.dto.MvcHttpMethodAndPathConfigDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;
import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteProperties;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiremock.org.apache.commons.lang3.StringUtils;

@Service
public class DynamicRouteConfigurationService implements ApplicationEventPublisherAware {

	private static final Logger log = LoggerFactory.getLogger(DynamicRouteConfigurationService.class);
	private static final String ROUTE_ID_SUFFIX = "#1";

	@Resource
	private RouteDefinitionRepository routeDefinitionRepository;

	@Autowired
	private DynamicRouteProperties dynamicRouteProperties;

	private ApplicationEventPublisher publisher;

	private Map<String, Object> loadedContexts = new ConcurrentHashMap<>();

	private volatile boolean isDynamicRouteLoaded = false;

	private ReentrantLock loadLock = new ReentrantLock();

	private ReentrantLock refreshLock = new ReentrantLock();

	private volatile Disposable loadDisposable = null;

	private Object object = new Object();

	@PostConstruct
	public void afterPropertiesSet() {

		loadDisposable = Flux.interval(Duration.ofSeconds(dynamicRouteProperties.getRetryIntervalOfSeconds()))
				.subscribe(this::loadRoutes);
		Flux.interval(Duration.ofMinutes(dynamicRouteProperties.getRefreshIntervalOfMinutes()))
				.subscribe(this::refreshRoutes);

		if (log.isDebugEnabled()) {
			log.debug("{} applied", DynamicRouteConfigurationService.class.getSimpleName());
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

	protected void doRefreshRoutes() {
		Mono<RouteConfigInfoResponseDto> mono = fetchAllRouteItemsWithWebClient();
		mono.subscribe(this::handleRefreshRouteConfigInfoResponse, this::handleRefreshErrors);
	}

	private void handleRefreshRouteConfigInfoResponse(RouteConfigInfoResponseDto respDto) {
		List<DynamicRouteItemInfo> routeItemInfos = parseRouteConfigInfoResponse(respDto);

		DynamicRouteItemInfoHolder.refresh(routeItemInfos);

		initContextRouteConfigs();

		List<MvcContextRouteConfig> outdatedMvcContextRouteConfigs = DynamicRouteItemInfoHolder
				.outdatedMvcContextRouteConfigs();
		
		outdatedMvcContextRouteConfigs.forEach(c -> {
			String contextRouteId = c.getContext()+ROUTE_ID_SUFFIX;
			if(this.loadedContexts.containsKey(contextRouteId)) {
				delete(contextRouteId);
				log.debug("outdated context route:{}", contextRouteId);
				
				this.loadedContexts.remove(contextRouteId);
			}
		});

	}

	private void handleRefreshErrors(Throwable e) {
		log.info("errors while refreshing routes...", e);
	}

	protected void loadRoutes(Long time) {
		log.debug("load routes  ------  {}", time);
		if (!loadLock.tryLock()) {
			log.debug("cannot acquire the lock.");
			return;
		}
		try {
			if (isDynamicRouteLoaded) {
				log.debug("isDynamicRouteLoaded:{}, isDisposed:{}", isDynamicRouteLoaded, loadDisposable.isDisposed());

				if (!loadDisposable.isDisposed()) {
					log.debug("to dispose load tasks.");
					loadDisposable.dispose();
				}

				return;
			}

			log.debug("try to load dynamic routes --- {}", time);
			doLoadRoutes();
		} finally {
			loadLock.unlock();
		}

	}

	protected void doLoadRoutes() {
		log.debug("start to load routes...");

		loadLock.lock();

		try {

			Mono<RouteConfigInfoResponseDto> mono = fetchAllRouteItemsWithWebClient();
			mono.subscribe(this::handleLoadRouteConfigInfoResponseDto, this::handleLoadErrors);

		} finally {
			loadLock.unlock();
		}

	}

	private void handleLoadErrors(Throwable e) {
		log.info("errors while loading routes...", e);
		isDynamicRouteLoaded = false;
	}

	private List<DynamicRouteItemInfo> parseRouteConfigInfoResponse(RouteConfigInfoResponseDto respDto) {
		List<RouteItemInfoDto> routeItemInfoDtos = respDto.getData();

		List<DynamicRouteItemInfo> routeItemInfos = new LinkedList<>();

		for (RouteItemInfoDto dto : routeItemInfoDtos) {
			DynamicRouteItemInfo info = new DynamicRouteItemInfo();
			info.setHost(dto.getHost());
			info.setPath(dto.getPath());
			info.setHttpMethod(dto.getHttpMethod());
			info.setHttpScheme(dto.getHttpScheme());
			info.setContext(dto.getContext());
			info.setPort(StringUtils.isBlank(dto.getPort()) ? 0 : Integer.parseInt(dto.getPort()));
			info.setWeight(StringUtils.isBlank(dto.getWeight()) ? 0 : Integer.parseInt(dto.getWeight()));

			routeItemInfos.add(info);
		}

		return routeItemInfos;
	}

	private void handleLoadRouteConfigInfoResponseDto(RouteConfigInfoResponseDto respDto) {
		loadLock.lock();
		try {
			List<RouteItemInfoDto> routeItemInfoDtos = respDto.getData();
			log.debug("size:{}", routeItemInfoDtos.size());

			List<DynamicRouteItemInfo> routeItemInfos = parseRouteConfigInfoResponse(respDto);

			DynamicRouteItemInfoHolder.refresh(routeItemInfos);
			initContextRouteConfigs();

			isDynamicRouteLoaded = true;
		} finally {
			loadLock.unlock();
		}
	}

	protected void initContextRouteConfigs() {
		int count = 0;
		refreshAllLoadedContexts();
		Collection<MvcContextRouteConfig> contextRouteConfigs = DynamicRouteItemInfoHolder.routeConfigs();

		for (MvcContextRouteConfig contextRouteConfig : contextRouteConfigs) {
			if (loadedContexts.containsKey(contextRouteConfig.getContext() + ROUTE_ID_SUFFIX)) {
				log.debug("context route is already loaded, context={}", contextRouteConfig.getContext());
				continue;
			}

			if (initContextRouteConfig(contextRouteConfig)) {
				count++;
			}
		}

		log.debug("add {} route definitions", count);
	}

	private boolean initContextRouteConfig(MvcContextRouteConfig contextRouteConfig) {
		List<HttpDestination> defaultHttpDestinations = contextRouteConfig.getDefaultHttpDestinations();
		if (defaultHttpDestinations.isEmpty()) {
			log.warn("Cannot find default http destination for {}", contextRouteConfig.getContext());
			return false;
		}

		HttpDestination targetHttpDestination = defaultHttpDestinations.get(0);

		DynamicRouteItemInfo itemInfo = new DynamicRouteItemInfo();
		itemInfo.setContext(contextRouteConfig.getContext());
		itemInfo.setHost(targetHttpDestination.getHost());
		itemInfo.setPort(targetHttpDestination.getPort());
		itemInfo.setHttpScheme(targetHttpDestination.getScheme());

		buildRouteDefinition(contextRouteConfig.getContext(), itemInfo);

		return true;
	}

	protected void buildRouteDefinition(String context, DynamicRouteItemInfo itemInfo) {
		RouteDefinition rd = new RouteDefinition();
		rd.setId(context + ROUTE_ID_SUFFIX);
		String urlStr = String.format("%s://%s:%s", itemInfo.getHttpScheme(), itemInfo.getHost(), itemInfo.getPort());
		URI uri = UriComponentsBuilder.fromHttpUrl(urlStr).build().toUri();
		rd.setUri(uri);

		PredicateDefinition pd = new PredicateDefinition();
		pd.setName("Path");
		Map<String, String> predicateParams = new HashMap<>(8);
		predicateParams.put("pattern", String.format("/%s/**", context));
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
//			fdRetry.addArg("statuses", "NOT_FOUND");
			fdRetry.addArg("methods", "GET,POST,PUT,DELETE");
			fdRetry.addArg("exceptions", "java.io.IOException,java.net.ConnectException");

			filters.add(fdRetry);
		}

		rd.setFilters(filters);

		add(rd);

		log.debug("### route added:{} {} {}", itemInfo.getContext(), itemInfo.getHost(), itemInfo.getPort());
	}

	private void refreshAllLoadedContexts() {
		Flux<RouteDefinition> flux = routeDefinitionRepository.getRouteDefinitions();

		flux.subscribe((rd) -> {
			this.loadedContexts.put(rd.getId(), object);
		});
	}

	public List<MvcContextRouteConfigDto> getAllMvcContextRouteConfigs() {
		Collection<MvcContextRouteConfig> routeContextConfigs = DynamicRouteItemInfoHolder.routeConfigs();

		List<MvcContextRouteConfigDto> contextRouteConfigs = routeContextConfigs //
				.stream() //
				.map(c -> buildMvcContextRouteConfigDto(c)) //
				.collect(Collectors.toList()); //
		return contextRouteConfigs;
	}

	private MvcContextRouteConfigDto buildMvcContextRouteConfigDto(MvcContextRouteConfig routeConfig) {

		MvcContextRouteConfigDto dto = new MvcContextRouteConfigDto();
		dto.setContext(routeConfig.getContext());
		dto.setCreatedTime(routeConfig.getCreatedTime());
		dto.setDisabled(routeConfig.isDisabled());
		dto.setLastModifiedTime(routeConfig.getLastModifiedTime());
		dto.setVersion(routeConfig.getVersion());
		routeConfig.getDefaultHttpDestinations().forEach(d -> {
			dto.addDefaultHttpDestination(buildHttpDestinationDto(d));
		});

		routeConfig.getMvcPathRouteConfigs().values().forEach(c -> {
			dto.addMvcHttpMethodAndPathConfig(buildMvcHttpMethodAndPathConfigDto(c));
		});

		return dto;
	}

	private MvcHttpMethodAndPathConfigDto buildMvcHttpMethodAndPathConfigDto(MvcHttpMethodAndPathConfig c) {
		MvcHttpMethodAndPathConfigDto dto = new MvcHttpMethodAndPathConfigDto();
		dto.setCreatedTime(c.getCreatedTime());
		dto.setDisabled(c.isDisabled());
		dto.setHttpMethod(c.getMvcHttpMethodAndPath().getHttpMethod().name());
		dto.setPath(c.getMvcHttpMethodAndPath().getPath());
		dto.setVersion(c.getVersion());
		dto.setLastModifiedTime(c.getLastModifiedTime());

		c.getHttpDestinations().forEach(h -> {
			dto.addHttpDestinations(buildHttpDestinationDto(h));
		});

		return dto;
	}

	private HttpDestinationDto buildHttpDestinationDto(HttpDestination http) {
		HttpDestinationDto d = new HttpDestinationDto();
		d.setCreatedTime(http.getCreatedTime());
		d.setDisabled(http.isDisabled());
		d.setHost(http.getHost());
		d.setLastModifiedTime(http.getLastModifiedTime());
		d.setPort(http.getPort());
		d.setScheme(http.getScheme());
		d.setVersion(http.getVersion());
		d.setWeight(http.getWeight());

		return d;
	}

	public void deleteRouteItem(String routeContext) {
		if (log.isInfoEnabled()) {
			log.info("to delete route item:{}", routeContext);
		}
		String routeId = routeContext+ROUTE_ID_SUFFIX;
		
		if(!this.loadedContexts.containsKey(routeId)) {
			log.debug("such context route does not exist. context={}", routeId);
			return;
		}
		
		String result = delete(routeId);
		if (log.isInfoEnabled()) {
			log.info("delete result:{} {}", routeId, result);
		}
		
		this.loadedContexts.remove(routeId);
	}

	public void deleteRouteItems(List<RouteItemInfoDto> routeItems) {
		// Not implemented currently
	}

	public List<RouteItemInfoDto> listAllContextRouteItems() {
		Flux<RouteDefinition> flux = routeDefinitionRepository.getRouteDefinitions();
		List<RouteItemInfoDto> items = new ArrayList<>();

		flux.subscribe((rd) -> {
			RouteItemInfoDto r = new RouteItemInfoDto();
			r.setContext(rd.getId());

			String uri = rd.getUri().toString();
			String scheme = uri.substring(0, uri.indexOf("://"));
			uri = uri.substring(uri.indexOf("://") + 3);
			String host = uri.substring(0, uri.indexOf(":"));
			String port = uri.substring(uri.indexOf(":") + 1);

			r.setHost(host);
			r.setPort(port);
			r.setHttpScheme(scheme);

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
		if (log.isDebugEnabled()) {
			if (routeItemInfoDtos != null) {
				routeItemInfoDtos.forEach(ri -> {
					log.debug("Route Item:{}", ri);
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

	public void pushRouteItem(String context, List<RouteItemInfoDto> routeItems) {
		if (StringUtils.isBlank(context)) {
			log.debug("context is blank.");
			return;
		}

		if (routeItems == null || routeItems.isEmpty()) {
			log.debug("route items is empty for name:{}", context);
		}

		// considering retry here
		this.refreshRoutes(-100L);

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
			log.warn("delete failed {}" , id);
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
