package com.webank.wecube.platform.gateway.route;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

@Service
public class DynamicRouteConfigurationService implements ApplicationEventPublisherAware {

    private Logger log = LoggerFactory.getLogger(DynamicRouteConfigurationService.class);

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    @PostConstruct
    public void afterPropertiesSet() {
        if (log.isInfoEnabled()) {
            log.info("{} applied", DynamicRouteConfigurationService.class.getSimpleName());
        }

        loadRoutes();
    }

    protected void loadRoutes() {
        log.info("start to load routes...");

        RouteDefinition definition = new RouteDefinition();
        definition.setId("dynamic-1");
        URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:9000").build().toUri();
        definition.setUri(uri);

        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");

        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", "/wecube-cds/**");
        predicate.setArgs(predicateParams);

        FilterDefinition filter = new FilterDefinition();
        filter.setName("StripPrefix");
        Map<String, String> filterParams = new HashMap<>(8);
        filterParams.put("parts", "1");
        filter.setArgs(filterParams);

        FilterDefinition filter1 = new FilterDefinition();
        filter1.setName("JwtSsoToken");
        Map<String, String> filterParams1 = new HashMap<>(8);
        filterParams1.put("authenticated", "true");
        filter1.setArgs(filterParams1);

        definition.setFilters(Arrays.asList(filter, filter1));
        definition.setPredicates(Arrays.asList(predicate));

        this.add(definition);
    }

    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public String add(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        notifyChanged();
        return "success";
    }

    public String update(RouteDefinition definition) {
        try {
            this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
        } catch (Exception e) {
            return "update fail,not find route  routeId: " + definition.getId();
        }
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            notifyChanged();
            return "success";
        } catch (Exception e) {
            return "update route  fail";
        }

    }

    public String delete(String id) {
        try {
            this.routeDefinitionWriter.delete(Mono.just(id));

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

}
