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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class DynamicRouteGatewayFilterFactory
        extends AbstractGatewayFilterFactory<DynamicRouteGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteGatewayFilterFactory.class);

    public static final String ENABLED_KEY = "enabled";

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
            log.info("Filter-{}, uri:{}", DynamicRouteGatewayFilterFactory.class.getSimpleName(), req.getURI().toString());

            boolean enabled = config.isEnabled();

            if (!enabled) {
                return chain.filter(exchange);
            }

            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            if (route == null) {
                log.info("none route found.");
                return chain.filter(exchange);
            }
            
            log.info("route:{} {}", route.getId(), route.getUri().toString());

            
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());

            String newPath = req.getURI().getRawPath();
            log.info("raw path:{}", newPath);
            String baseUrl = determineBaseUrl(newPath);
            URI newUri = UriComponentsBuilder.fromHttpUrl(baseUrl + newPath).build().toUri();

            ServerHttpRequest request = req.mutate().uri(newUri).build();

            Route newRoute = Route.async().asyncPredicate(route.getPredicate()).filters(route.getFilters())
                    .id(route.getId()).order(route.getOrder()).uri(newUri).build();

            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    protected String determineBaseUrl(String path) {
        // TODO
        return "http://localhost:9999";
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
