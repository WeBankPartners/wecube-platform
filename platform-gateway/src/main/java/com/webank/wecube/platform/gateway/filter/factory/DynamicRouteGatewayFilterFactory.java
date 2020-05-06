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
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.route.DynamicRouteContext;
import com.webank.wecube.platform.gateway.route.DynamicRouteItemInfoHolder;
import com.webank.wecube.platform.gateway.route.HttpDestination;
import com.webank.wecube.platform.gateway.route.MvcHttpMethodAndPathConfig;

import reactor.core.publisher.Mono;

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
        if (log.isDebugEnabled()) {
            log.debug("Filter-{} applied", DynamicRouteGatewayFilterFactory.class.getSimpleName());
        }
        return ((exchange, chain) -> {
            log.debug("Filter-IN-{}, uri:{}", DynamicRouteGatewayFilterFactory.class.getSimpleName(),
                    exchange.getRequest().getURI().toString());

            boolean enabled = config.isEnabled();

            if (!enabled) {
                return chain.filter(exchange);
            }

            Route originalRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            if (originalRoute == null) {
                log.debug("There is none route found for filter:{}",
                        DynamicRouteGatewayFilterFactory.class.getSimpleName());
                return chain.filter(exchange);
            }

            tryPrepareDynamicRoute(exchange, originalRoute);

            try {
                return chain.filter(exchange);
            } catch (Exception e) {
                log.debug("errors while exchanging", e);
                return Mono.justOrEmpty(null);
            }
        });
    }

    protected void trySetGatewayRouteAttribute(ServerWebExchange exchange, Route route, String baseUrl) {
        if (log.isDebugEnabled()) {
            log.debug("base url:{}", baseUrl);
        }

        URI newUri = UriComponentsBuilder.fromHttpUrl(baseUrl).build().toUri();
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, exchange.getRequest().getURI());

        Route newRoute = Route.async().asyncPredicate(route.getPredicate()).filters(route.getFilters())
                .id(route.getId()).order(route.getOrder()).uri(newUri).build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
    }

    protected void tryPrepareDynamicRoute(ServerWebExchange exchange, Route originalRoute) {
        String path = exchange.getRequest().getURI().getPath();
        String componentPath = calculateComponentPath(path);
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        MvcHttpMethodAndPathConfig methodAndPathConfig = DynamicRouteItemInfoHolder.findRouteConfig(componentPath, path,
                httpMethod);

        List<HttpDestination> httpDestinations = null;
        if (methodAndPathConfig == null) {
            httpDestinations = DynamicRouteItemInfoHolder.findDefaultRouteConfig(componentPath, path, httpMethod);
        } else {
            httpDestinations = methodAndPathConfig.getHttpDestinations();
        }

        if (httpDestinations.isEmpty()) {
            log.debug("cannot find http destination for {}", path);
            return;
        }

        DynamicRouteContext routeContext = DynamicRouteContext.newInstance().addHttpDestinations(httpDestinations)
                .sortByWeight();

        exchange.getAttributes().put(DynamicRouteContext.DYNAMIC_ROUTE_CONTEXT_KEY, routeContext);

        if (routeContext.hasNext()) {
            HttpDestination httpDest = routeContext.next();
            String baseUrl = String.format("%s://%s:%s", httpDest.getScheme(), httpDest.getHost(), httpDest.getPort());
            trySetGatewayRouteAttribute(exchange, originalRoute, baseUrl);
        }

        return;
    }

    protected String calculateComponentPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
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
}
