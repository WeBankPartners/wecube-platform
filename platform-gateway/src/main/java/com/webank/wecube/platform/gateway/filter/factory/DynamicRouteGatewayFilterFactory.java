package com.webank.wecube.platform.gateway.filter.factory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.dto.GenericResponseDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;

public class DynamicRouteGatewayFilterFactory
        extends AbstractGatewayFilterFactory<DynamicRouteGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteGatewayFilterFactory.class);

    public static final String ENABLED_KEY = "enabled";

    private DynamicRouteProperties dynamicRouteProperties;

    public DynamicRouteGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        if (log.isInfoEnabled()) {
            log.info("Filter-{} applied", DynamicRouteGatewayFilterFactory.class.getSimpleName());
        }
        return ((exchange, chain) -> {
            ServerHttpRequest req = exchange.getRequest();
            log.info("Filter-{}, uri:{}", DynamicRouteGatewayFilterFactory.class.getSimpleName(),
                    req.getURI().toString());

            boolean enabled = config.isEnabled();

            if (!enabled) {
                return chain.filter(exchange);
            }

            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            if (route == null) {
                log.warn("There is none route found for filter:{}",
                        DynamicRouteGatewayFilterFactory.class.getSimpleName());
                return chain.filter(exchange);
            }

            if (log.isDebugEnabled()) {
                log.debug("route:{} {}", route.getId(), route.getUri().toString());
            }

            String newPath = req.getURI().getRawPath();
            String baseUrl = null;
            try {
                baseUrl = determineBaseUrl(newPath);
            } catch (Exception e) {
                log.error("errors while determining base url", e);
                return chain.filter(exchange);
            }

            if (log.isDebugEnabled()) {
                log.debug("base url:{}", baseUrl);
            }

            URI newUri = UriComponentsBuilder.fromHttpUrl(baseUrl + newPath).build().toUri();

            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
            ServerHttpRequest request = req.mutate().uri(newUri).build();

            Route newRoute = Route.async().asyncPredicate(route.getPredicate()).filters(route.getFilters())
                    .id(route.getId()).order(route.getOrder()).uri(newUri).build();

            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    protected String determineBaseUrl(String path) {
        String componentPath = calculateComponentPath(path);
        RestTemplate client = new RestTemplate();

        String url = dynamicRouteProperties.getRouteConfigServer() + dynamicRouteProperties.getRouteConfigUri() + "/"
                + componentPath;

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

        String baseUrl = "";
        if (routeItemInfoDtos != null && !routeItemInfoDtos.isEmpty()) {
            RouteItemInfoDto item = routeItemInfoDtos.get(0);
            baseUrl = String.format("%s://%s:%s", item.getSchema(), item.getHost(), item.getPort());
        }
        return baseUrl;
    }

    protected String calculateComponentPath(String path) {
        if (path.startsWith("/api/")) {
            path = path.substring(5);
        }

        if (path.indexOf("/") >= 0) {
            path = path.substring(0, path.indexOf("/"));
        }

        return path;
    }

    public DynamicRouteProperties getDynamicRouteProperties() {
        return dynamicRouteProperties;
    }

    public void setDynamicRouteProperties(DynamicRouteProperties dynamicRouteProperties) {
        this.dynamicRouteProperties = dynamicRouteProperties;
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList(ENABLED_KEY);
    }

    public static class Config {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    private static class RouteConfigInfoResponseDto extends GenericResponseDto<List<RouteItemInfoDto>> {

    }

}
