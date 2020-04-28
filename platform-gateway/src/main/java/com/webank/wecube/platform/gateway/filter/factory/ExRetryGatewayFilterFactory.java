package com.webank.wecube.platform.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_HEADER_NAMES;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ALREADY_ROUTED_ATTR;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.gateway.route.DynamicRouteContext;
import com.webank.wecube.platform.gateway.route.HttpDestination;

import reactor.core.publisher.Mono;
import reactor.retry.Repeat;
import reactor.retry.RepeatContext;
import reactor.retry.Retry;
import reactor.retry.RetryContext;

/**
 * 
 * @author gavin
 *
 */
public class ExRetryGatewayFilterFactory extends AbstractGatewayFilterFactory<ExRetryGatewayFilterFactory.RetryConfig> {

    public static final String EX_RETRY_ITERATION_KEY = "ex_retry_iteration";

    private static final Logger log = LoggerFactory.getLogger(ExRetryGatewayFilterFactory.class);

    public ExRetryGatewayFilterFactory() {
        super(RetryConfig.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> toList(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    @Override
    public GatewayFilter apply(RetryConfig retryConfig) {
        if (log.isDebugEnabled()) {
            log.debug("Config:{}", retryConfig);
        }
        retryConfig.validate();

        Repeat<ServerWebExchange> statusCodeRepeat = null;
        if (!retryConfig.getStatuses().isEmpty() || !retryConfig.getSeries().isEmpty()) {
            Predicate<RepeatContext<ServerWebExchange>> repeatPredicate = context -> {
                ServerWebExchange exchange = context.applicationContext();
                if (exceedsMaxIterations(exchange, retryConfig)) {
                    log.debug("exceeded max iterations.");
                    return false;
                }

                if (!isRouteContextAvailable(context.applicationContext())) {
                    log.debug("route context is NOT available now.");
                    return false;
                }

                HttpStatus statusCode = exchange.getResponse().getStatusCode();
                
                log.trace("statusCode:{}", statusCode.value());

                boolean retryableStatusCode = retryConfig.getStatuses().contains(statusCode);

                if (!retryableStatusCode && statusCode != null) {
                    // try the series
                    retryableStatusCode = retryConfig.getSeries().stream()
                            .anyMatch(series -> statusCode.series().equals(series));
                }

                trace("retryableStatusCode: %b, statusCode %s, configured statuses %s, configured series %s",
                        retryableStatusCode, statusCode, retryConfig.getStatuses(), retryConfig.getSeries());

                HttpMethod httpMethod = exchange.getRequest().getMethod();
                boolean retryableMethod = retryConfig.getMethods().contains(httpMethod);

                trace("retryableMethod: %b, httpMethod %s, configured methods %s", retryableMethod, httpMethod,
                        retryConfig.getMethods());
                return retryableMethod && retryableStatusCode;
            };

            statusCodeRepeat = Repeat.onlyIf(repeatPredicate)
                    .doOnRepeat(context -> reset(context.applicationContext()));
        }

        Retry<ServerWebExchange> exceptionRetry = null;
        if (!retryConfig.getExceptions().isEmpty()) {
            Predicate<RetryContext<ServerWebExchange>> retryContextPredicate = context -> {
                if (exceedsMaxIterations(context.applicationContext(), retryConfig)) {
                    log.debug("exceeded max iterations.");
                    return false;
                }

                if (!isRouteContextAvailable(context.applicationContext())) {
                    log.debug("route context is NOT available now.");
                    return false;
                }

                for (Class<? extends Throwable> clazz : retryConfig.getExceptions()) {
                    if (clazz.isInstance(context.exception())) {
                        trace("exception is retryable %s, configured exceptions",
                                context.exception().getClass().getName(), retryConfig.getExceptions());
                        return true;
                    }
                }
                trace("exception is not retryable %s, configured exceptions", context.exception().getClass().getName(),
                        retryConfig.getExceptions());
                return false;
            };
            exceptionRetry = Retry.onlyIf(retryContextPredicate)
                    .doOnRetry(context -> reset(context.applicationContext())).retryMax(retryConfig.getRetries());
        }

        return apply(retryConfig.getRouteId(), statusCodeRepeat, exceptionRetry);
    }

    public boolean exceedsMaxIterations(ServerWebExchange exchange, RetryConfig retryConfig) {
        Integer iteration = exchange.getAttribute(EX_RETRY_ITERATION_KEY);

        boolean exceeds = iteration != null && iteration >= retryConfig.getRetries();
        trace("exceedsMaxIterations %b, iteration %d, configured retries %d", exceeds, iteration,
                retryConfig.getRetries());
        return exceeds;
    }

    public void reset(ServerWebExchange exchange) {
        Set<String> addedHeaders = exchange.getAttributeOrDefault(CLIENT_RESPONSE_HEADER_NAMES, Collections.emptySet());
        addedHeaders.forEach(header -> exchange.getResponse().getHeaders().remove(header));
        exchange.getAttributes().remove(GATEWAY_ALREADY_ROUTED_ATTR);
    }

    public GatewayFilter apply(String routeId, Repeat<ServerWebExchange> repeat, Retry<ServerWebExchange> retry) {
        if (routeId != null && getPublisher() != null) {
            getPublisher().publishEvent(new EnableBodyCachingEvent(this, routeId));
        }
        return (exchange, chain) -> {
            trace("Entering retry-filter");

            Publisher<Void> publisher = chain.filter(exchange).doOnSuccessOrError((aVoid, throwable) -> {
                int iteration = exchange.getAttributeOrDefault(EX_RETRY_ITERATION_KEY, -1);
                int newIteration = iteration + 1;
                trace("setting new iteration in attr %d", newIteration);
                exchange.getAttributes().put(EX_RETRY_ITERATION_KEY, newIteration);
            });

            if (retry != null) {
                publisher = ((Mono<Void>) publisher).retryWhen(retry.withApplicationContext(exchange));
            }
            if (repeat != null) {
                publisher = ((Mono<Void>) publisher).repeatWhen(repeat.withApplicationContext(exchange));
            }

            return Mono.fromDirect(publisher);
        };
    }

    private void tryPrepareRoute(ServerWebExchange exchange) {
        DynamicRouteContext routeContext = exchange.getAttribute(DynamicRouteContext.DYNAMIC_ROUTE_CONTEXT_KEY);
        if (routeContext == null) {
            log.debug("routeContext is null for {}", exchange.getRequest().getURI().toString());
            return;
        }

        HttpDestination httpDest = routeContext.next();
        if (httpDest == null) {
            log.debug("dynamic route item info is null for {}", exchange.getRequest().getURI().toString());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("prepare dynamic route:{},last round:{}", httpDest, exchange.getAttribute(EX_RETRY_ITERATION_KEY));
        }

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String baseUrl = String.format("%s://%s:%s", httpDest.getScheme(), httpDest.getHost(), httpDest.getPort());
        URI newUri = UriComponentsBuilder.fromHttpUrl(baseUrl).build().toUri();
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, exchange.getRequest().getURI());
        Route newRoute = Route.async().asyncPredicate(route.getPredicate()).filters(route.getFilters())
                .id(route.getId()).order(route.getOrder()).uri(newUri).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
    }

    private boolean isRouteContextAvailable(ServerWebExchange exchange) {
        DynamicRouteContext routeContext = exchange.getAttribute(DynamicRouteContext.DYNAMIC_ROUTE_CONTEXT_KEY);
        if (routeContext == null) {
            log.debug("routeContext is null for {}", exchange.getRequest().getURI().toString());
            return false;
        }

       if(!routeContext.hasNext()){
           return false;
       }
       
       tryPrepareRoute(exchange);
       return true;
    }

    private void trace(String message, Object... args) {
        if (log.isTraceEnabled()) {
            log.trace(String.format(message, args));
        }
    }

    @SuppressWarnings("unchecked")
    public static class RetryConfig implements HasRouteId {

        private String routeId;

        private int retries = 3;

        private List<Series> series = toList(Series.SERVER_ERROR);

        private List<HttpStatus> statuses = new ArrayList<>();

        private List<HttpMethod> methods = toList(HttpMethod.GET);

        private List<Class<? extends Throwable>> exceptions = toList(IOException.class, TimeoutException.class);

        public RetryConfig allMethods() {
            return setMethods(HttpMethod.values());
        }

        public void validate() {
            Assert.isTrue(this.retries > 0, "retries must be greater than 0");
            Assert.isTrue(!this.series.isEmpty() || !this.statuses.isEmpty() || !this.exceptions.isEmpty(),
                    "series, status and exceptions may not all be empty");
            Assert.notEmpty(this.methods, "methods may not be empty");
        }

        @Override
        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        @Override
        public String getRouteId() {
            return this.routeId;
        }

        public int getRetries() {
            return retries;
        }

        public RetryConfig setRetries(int retries) {
            this.retries = retries;
            return this;
        }

        public List<Series> getSeries() {
            return series;
        }

        public RetryConfig setSeries(Series... series) {
            this.series = Arrays.asList(series);
            return this;
        }

        public List<HttpStatus> getStatuses() {
            return statuses;
        }

        public RetryConfig setStatuses(HttpStatus... statuses) {
            this.statuses = Arrays.asList(statuses);
            return this;
        }

        public List<HttpMethod> getMethods() {
            return methods;
        }

        public RetryConfig setMethods(HttpMethod... methods) {
            this.methods = Arrays.asList(methods);
            return this;
        }

        public List<Class<? extends Throwable>> getExceptions() {
            return exceptions;
        }

        public RetryConfig setExceptions(Class<? extends Throwable>... exceptions) {
            this.exceptions = Arrays.asList(exceptions);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RetryConfig [routeId=");
            builder.append(routeId);
            builder.append(", retries=");
            builder.append(retries);
            builder.append(", series=");
            builder.append(series);
            builder.append(", statuses=");
            builder.append(statuses);
            builder.append(", methods=");
            builder.append(methods);
            builder.append(", exceptions=");
            builder.append(exceptions);
            builder.append("]");
            return builder.toString();
        }

    }

}