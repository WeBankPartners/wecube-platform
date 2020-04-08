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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.route.DynamicRouteContext;
import com.webank.wecube.platform.gateway.route.DynamicRouteItemInfo;
import com.webank.wecube.platform.gateway.route.DynamicRouteItemInfoHolder;

/**
 * 
 * @author gavin
 *
 */
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

            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            if (route == null) {
                log.warn("There is none route found for filter:{}",
                        DynamicRouteGatewayFilterFactory.class.getSimpleName());
                return chain.filter(exchange);
            }

            if (log.isDebugEnabled()) {
                log.debug("route:{} {}", route.getId(), route.getUri().toString());
            }

            String newPath = exchange.getRequest().getURI().getPath();
            String baseUrl = determineDynamicRoute(newPath, exchange);
            if (baseUrl != null) {
                tryPrepareRoute(exchange, route, baseUrl);
            }

            return chain.filter(exchange);
        });
    }
    
    protected void tryPrepareRoute(ServerWebExchange exchange, Route route, String baseUrl) {
        if (log.isDebugEnabled()) {
            log.debug("base url:{}", baseUrl);
        }

        URI newUri = UriComponentsBuilder.fromHttpUrl(baseUrl).build().toUri();
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, exchange.getRequest().getURI());

        Route newRoute = Route.async().asyncPredicate(route.getPredicate()).filters(route.getFilters())
                .id(route.getId()).order(route.getOrder()).uri(newUri).build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
    }

    protected String determineDynamicRoute(String path, ServerWebExchange exchange) {
        String componentPath = calculateComponentPath(path);
        List<DynamicRouteItemInfo> routeItemInfos = DynamicRouteItemInfoHolder.findByName(componentPath);
        if (routeItemInfos == null || routeItemInfos.isEmpty()) {
            return null;
        }
        
        DynamicRouteContext routeContext = new DynamicRouteContext().addDynamicRouteItemInfos(routeItemInfos);
        exchange.getAttributes().put(DynamicRouteContext.DYNAMIC_ROUTE_CONTEXT_KEY, routeContext);

        DynamicRouteItemInfo routeItemInfo = routeContext.next();
        
        if(routeItemInfo == null){
            return null;
        }

        String baseUrl = String.format("%s://%s:%s", routeItemInfo.getHttpSchema(), routeItemInfo.getHost(),
                routeItemInfo.getPort());
        return baseUrl;
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
