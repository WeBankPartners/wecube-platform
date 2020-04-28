package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

public final class MvcContextRouteConfig {

    private String context;
    private Map<MvcHttpMethodAndPath, MvcHttpMethodAndPathConfig> mvcPathRouteConfigs = new ConcurrentHashMap<>();
    private List<HttpDestination> defaultHttpDestinations = new ArrayList<>();

    private long createdTime;
    private long lastModifiedTime;

    private boolean disabled;

    private long version = 0L;

    public MvcContextRouteConfig(String context) {
        super();
        this.context = context;
        this.createdTime = System.currentTimeMillis();
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public MvcHttpMethodAndPathConfig findByMvcHttpMethodAndPath(MvcHttpMethodAndPath httpMethodAndPath) {
        if (httpMethodAndPath == null) {
            return null;
        }
        return this.mvcPathRouteConfigs.get(httpMethodAndPath);
    }

    public MvcHttpMethodAndPathConfig findByMvcHttpMethodAndPath(HttpMethod httpMethod, String path) {
        if (httpMethod == null || StringUtils.isBlank(path)) {
            return null;
        }

        return this.mvcPathRouteConfigs.get(new MvcHttpMethodAndPath(httpMethod, path));
    }

    public void tryAddMvcHttpMethodAndPathConfig(String mvcPath, HttpDestination httpDestination) {
        if (StringUtils.isBlank(mvcPath) || httpDestination == null) {
            return;
        }

        for (HttpMethod httpMethod : HttpMethod.values()) {
            MvcHttpMethodAndPath mvcHttpMethodAndPath = new MvcHttpMethodAndPath(httpMethod, mvcPath);
            doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath, httpDestination);
        }
    }

    public void tryAddMvcHttpMethodAndPathConfig(String mvcPath, HttpMethod httpMethod,
            HttpDestination httpDestination) {
        if (StringUtils.isBlank(mvcPath) || httpDestination == null || httpMethod == null) {
            return;
        }

        MvcHttpMethodAndPath mvcHttpMethodAndPath = new MvcHttpMethodAndPath(httpMethod, mvcPath);
        doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath, httpDestination);
    }

    private void doAddMvcHttpMethodAndPathConfig(MvcHttpMethodAndPath mvcHttpMethodAndPath,
            HttpDestination httpDestination) {

        if (mvcHttpMethodAndPath == null || httpDestination == null) {
            return;
        }

        MvcHttpMethodAndPathConfig existConfig = mvcPathRouteConfigs.get(mvcHttpMethodAndPath);
        if (existConfig == null) {
            existConfig = new MvcHttpMethodAndPathConfig(mvcHttpMethodAndPath);

            mvcPathRouteConfigs.put(mvcHttpMethodAndPath, existConfig);

        }

        existConfig.version(this.version);
        existConfig.tryAddHttpDestination(httpDestination);

        lastModifiedTime();
    }

    MvcContextRouteConfig cleanOutdated() {
        for (MvcHttpMethodAndPathConfig config : mvcPathRouteConfigs.values()) {
            config.cleanOutdatedHttpDestination();
        }
        cleanOutdatedMvcPathRouteConfigs();

        cleanOutdatedHttpDestinations();

        return this;
    }

    private MvcContextRouteConfig cleanOutdatedMvcPathRouteConfigs() {

        List<MvcHttpMethodAndPathConfig> outDatedConfigs = mvcPathRouteConfigs //
        		.values() //
        		.stream() //
        		.filter(c -> c.getVersion() < version ) //
        		.collect(Collectors.toList()); //

        for (MvcHttpMethodAndPathConfig config : outDatedConfigs) {
            mvcPathRouteConfigs.remove(config.getMvcHttpMethodAndPath());
        }

        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig version(long version) {
        this.version = version;
        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig clearDefaultHttpDestinations() {
        this.defaultHttpDestinations.clear();
        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig removeDefaultHttpDestination(HttpDestination httpDestination) {
        if (httpDestination == null) {
            return this;
        }

        List<HttpDestination> toRemoves = new ArrayList<>();
        for (HttpDestination d : defaultHttpDestinations) {
            if (d.equals(httpDestination)) {
                toRemoves.add(d);
            }
        }

        this.defaultHttpDestinations.removeAll(toRemoves);

        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig cleanOutdatedHttpDestinations() {
        List<HttpDestination> toRemoves = this.defaultHttpDestinations //
        		.stream() //
        		.filter(c -> c.getVersion() < version ) //
        		.collect(Collectors.toList()); //

        this.defaultHttpDestinations.removeAll(toRemoves);

        lastModifiedTime();
        return this;
    }

    boolean tryAddDefaultHttpDestination(HttpDestination httpDestination) {
        if (httpDestination == null) {
            return false;
        }

        HttpDestination exist = findDefaultHttpDestination(httpDestination);
        if (exist == null) {
            exist = new HttpDestination(httpDestination.getScheme(), httpDestination.getHost(),
                    httpDestination.getPort());
            this.defaultHttpDestinations.add(exist);
        }

        exist.weight(httpDestination.getWeight());
        exist.version(this.version);

        lastModifiedTime();
        return true;

    }

    private HttpDestination findDefaultHttpDestination(HttpDestination criteria) {
        if (criteria == null) {
            return null;
        }

        for (HttpDestination h : this.defaultHttpDestinations) {
            if (h.equals(criteria)) {
                return h;
            }
        }

        return null;
    }

    MvcContextRouteConfig disable() {
        this.disabled = true;
        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig enable() {
        this.disabled = false;
        lastModifiedTime();
        return this;
    }

    MvcContextRouteConfig enableDefaultHttpDestination(HttpDestination criteria) {
        if (criteria == null) {
            return this;
        }
        
        HttpDestination existDest = this.findDefaultHttpDestination(criteria);
        if(existDest == null) {
        	return this;
        }
        
        existDest.disabled(false);

        lastModifiedTime();
        return this;
    }
    
    MvcContextRouteConfig disableDefaultHttpDestination(HttpDestination criteria) {
        if (criteria == null) {
            return this;
        }
        
        HttpDestination existDest = this.findDefaultHttpDestination(criteria);
        if(existDest == null) {
        	return this;
        }
        
        existDest.disabled(true);

        lastModifiedTime();
        return this;
    }

    public String getContext() {
        return context;
    }

    public Map<MvcHttpMethodAndPath, MvcHttpMethodAndPathConfig> getMvcPathRouteConfigs() {
        return Collections.unmodifiableMap(mvcPathRouteConfigs);
    }

    public List<HttpDestination> getDefaultHttpDestinations() {
        return Collections.unmodifiableList(this.defaultHttpDestinations);
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isDisabled() {
        return disabled;
    }

    private MvcContextRouteConfig lastModifiedTime() {
        this.lastModifiedTime = System.currentTimeMillis();
        return this;
    }

    public long getVersion() {
        return version;
    }

}
