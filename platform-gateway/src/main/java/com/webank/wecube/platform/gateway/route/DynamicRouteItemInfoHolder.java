package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpMethod;

import com.github.jknack.handlebars.internal.lang3.StringUtils;

public class DynamicRouteItemInfoHolder {

    private Map<String, MvcContextRouteConfig> mvcContextRouteConfigs = new ConcurrentHashMap<String, MvcContextRouteConfig>();

    private static final DynamicRouteItemInfoHolder INSTANCE = new DynamicRouteItemInfoHolder();

    private volatile long lastVersion = 0L;
    private volatile long currentVersion = 0L;

    private List<HttpDestination> unreachableHttpDestinations = new ArrayList<>();

    private List<MvcContextRouteConfig> outdatedMvcContextRouteConfigs = new ArrayList<>();

    public static DynamicRouteItemInfoHolder instance() {
        return INSTANCE;
    }

    public static void refresh(List<DynamicRouteItemInfo> fullyDynamicRouteItemInfos) {
        INSTANCE.refreshRoutes(fullyDynamicRouteItemInfos);
    }

    public static Collection<MvcContextRouteConfig> routeConfigs() {
        return INSTANCE.getMvcContextRouteConfigs().values();
    }

    public static MvcContextRouteConfig routeConfig(String context){
        return INSTANCE.getMvcContextRouteConfig(context);
    }

    public static MvcHttpMethodAndPathConfig findRouteConfig(String context, String path, HttpMethod httpMethod) {
        return INSTANCE.findRoute(context, path, httpMethod);
    }

    public static List<HttpDestination> findDefaultRouteConfig(String context, String path, HttpMethod httpMethod) {
        return INSTANCE.findDefaultRoute(context, path, httpMethod);
    }
    
    public static List<MvcContextRouteConfig> outdatedMvcContextRouteConfigs(){
    	return INSTANCE.getOutdatedMvcContextRouteConfigs();
    }

    public void refreshRoutes(List<DynamicRouteItemInfo> fullyDynamicRouteItemInfos) {
        if (fullyDynamicRouteItemInfos == null) {
            return;
        }

        increaseVersion();

        for (DynamicRouteItemInfo item : fullyDynamicRouteItemInfos) {
            if (item == null) {
                continue;
            }

            if (StringUtils.isBlank(item.getContext())) {
                continue;
            }

            tryAddDynamicRouteItemInfo(item);
        }

        cleanOutdated();
    }

    private void tryAddDynamicRouteItemInfo(DynamicRouteItemInfo item) {
        
        MvcContextRouteConfig existConfig = mvcContextRouteConfigs.get(item.getContext());
        if (existConfig == null) {
            existConfig = new MvcContextRouteConfig(item.getContext());
            mvcContextRouteConfigs.put(item.getContext(), existConfig);
        }

        existConfig.version(currentVersion);

        if (StringUtils.isBlank(item.getHttpMethod()) && StringUtils.isBlank(item.getPath())) {
            existConfig.tryAddDefaultHttpDestination(
                    new HttpDestination(item.getHttpScheme(), item.getHost(), item.getPort(), item.getWeight()));

            return;
        }

        if (StringUtils.isBlank(item.getPath())) {
            return;
        }

        if (StringUtils.isBlank(item.getHttpMethod())) {
            existConfig.tryAddMvcHttpMethodAndPathConfig(item.getPath(),
                    new HttpDestination(item.getHttpScheme(), item.getHost(), item.getPort(), item.getWeight()));
            return;
        }

        HttpMethod httpMethod = resolveHttpMethod(item.getHttpMethod());
        if (httpMethod == null) {
            return;
        }

        existConfig.tryAddMvcHttpMethodAndPathConfig(item.getPath(), httpMethod,
                new HttpDestination(item.getHttpScheme(), item.getHost(), item.getPort(), item.getWeight()));

        return;
    }

    private HttpMethod resolveHttpMethod(String sHttpMethod) {
        if (StringUtils.isBlank(sHttpMethod)) {
            return null;
        }

        return HttpMethod.resolve(sHttpMethod.toUpperCase());
    }

    private void cleanOutdated() {
        clearUnreachableHttpDestinations();
        cleanOutdatedMvcContextRouteConfigs();
        cleanMvcContextRouteConfigs();
    }

    private void cleanMvcContextRouteConfigs() {
        for (MvcContextRouteConfig config : mvcContextRouteConfigs.values()) {
            config.cleanOutdated();

            if (config.getVersion() < currentVersion) {
                this.outdatedMvcContextRouteConfigs.add(config);
            }
        }

        for (MvcContextRouteConfig config : this.outdatedMvcContextRouteConfigs) {
            mvcContextRouteConfigs.remove(config.getContext());
        }
    }

    private void cleanOutdatedMvcContextRouteConfigs() {
        outdatedMvcContextRouteConfigs.clear();
    }

    private void increaseVersion() {
        this.lastVersion = currentVersion;
        this.currentVersion = currentVersion + 1;
    }

    public MvcHttpMethodAndPathConfig findRoute(String context, String path, HttpMethod httpMethod) {

        MvcContextRouteConfig ctxConfig = findContextConfig(context, path, httpMethod);
        if (ctxConfig == null) {
            return null;
        }

        return ctxConfig.findByMvcHttpMethodAndPath(httpMethod, path);
    }

    private MvcContextRouteConfig findContextConfig(String context, String path, HttpMethod httpMethod) {
        if (StringUtils.isBlank(context) || StringUtils.isBlank(path) || httpMethod == null) {
            return null;
        }

        return this.mvcContextRouteConfigs.get(context);

    }

    public List<HttpDestination> findDefaultRoute(String context, String path, HttpMethod httpMethod) {
        MvcContextRouteConfig ctxConfig = findContextConfig(context, path, httpMethod);
        if (ctxConfig == null) {
            return Collections.emptyList();
        }

        return ctxConfig.getDefaultHttpDestinations();
    }

    public void addRoute(String context, String path, HttpDestination httpDest, HttpMethod httpMethod) {
        // NOT implemented currently
    }

    public void addRoute(String context, String path, HttpDestination httpDest) {
    	// NOT implemented currently
    }

    public void deleteRoute(String context, String path, HttpDestination httpDest, HttpMethod httpMethod) {
    	// NOT implemented currently
    }

    public void deleteRoute(String context, String path, HttpDestination httpDest) {
    	// NOT implemented currently
    }

    public void addDefaultRoute(String context, HttpDestination httpDest) {
    	// NOT implemented currently
    }

    public void removeDefaultRoute(String context, HttpDestination httpDest) {
    	// NOT implemented currently
    }

    void clearUnreachableHttpDestinations() {
        this.unreachableHttpDestinations.clear();
    }

    public void removeUnreachableHttpDestination(HttpDestination httpDestination) {
    	// NOT implemented currently
    }

    public void addUnreachableHttpDestination(HttpDestination httpDestination) {
    	// NOT implemented currently
    }

    public List<HttpDestination> getUnreachableHttpDestinations() {
        return Collections.unmodifiableList(this.unreachableHttpDestinations);
    }

    public Map<String, MvcContextRouteConfig> getMvcContextRouteConfigs() {
        return Collections.unmodifiableMap(this.mvcContextRouteConfigs);
    }

    public MvcContextRouteConfig getMvcContextRouteConfig(String context) {
        if (StringUtils.isBlank(context)) {
            return null;
        }
        return this.mvcContextRouteConfigs.get(context);
    }

    public List<MvcContextRouteConfig> getOutdatedMvcContextRouteConfigs() {
        return Collections.unmodifiableList(outdatedMvcContextRouteConfigs);
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

}
