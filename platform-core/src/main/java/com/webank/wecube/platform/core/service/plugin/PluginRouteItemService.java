package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginRouteItemDto;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

@Service
public class PluginRouteItemService {
    private static final Logger log = LoggerFactory.getLogger(PluginRouteItemService.class);

    public static final String HTTP_SCHEME = "http";

    @Autowired
    private PluginInstanceRepository pluginInstanceRepository;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    @Autowired
    private EntityManager entityManager;

    public List<PluginRouteItemDto> getAllPluginRouteItems() {
    	
    	long startTime = System.currentTimeMillis();

        List<PluginRouteItemDto> resultList = new LinkedList<>();

        List<PluginInstanceEntity> pluginInstances = fetchRunningPluginInstanceInfo();

        //packageId -> PluginInstanceEntity
        Map<String,List<PluginInstanceEntity>> pluginInstanceMap = new HashMap<>();
        // 1 assemble default route for each context
        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getPackageName());

                resultList.add(d);

                String packageId = pi.getPackageId();
                List<PluginInstanceEntity> pluginInstanceList = null;
                if(pluginInstanceMap.containsKey(packageId)){
                    pluginInstanceList = pluginInstanceMap.get(packageId);
                }else{
                    pluginInstanceList = new ArrayList<>();
                    pluginInstanceMap.put(packageId,pluginInstanceList);
                }
                pluginInstanceList.add(pi);
            });
        }


        List<PluginPackageEntity> pkgs = fetchAllActivePluginPackageEntities();
        Map<String,PluginPackageEntity> latestActivePluginPackageMap = new HashMap<>();
        for (PluginPackageEntity pkg : pkgs) {
            if(!latestActivePluginPackageMap.containsKey(pkg.getName())) {
                latestActivePluginPackageMap.put(pkg.getName(),pkg);
            }
        }

        // 2 assemble routes for each interface
        tryCalculateInterfaceRoutes(resultList,pluginInstanceMap,latestActivePluginPackageMap);

        if (log.isInfoEnabled()) {
            log.info("total {} routes got to push.", resultList.size());
        }
        
        long endTime = System.currentTimeMillis();
        
        log.info("total {} seconds elapsed",(endTime - startTime)/1000);
        
        return resultList;
    }

    @SuppressWarnings("unchecked")
	private List<PluginPackageEntity> fetchAllActivePluginPackageEntities() {
        Query pluginPackageQuery = entityManager.createNativeQuery(
                "SELECT id,name FROM plugin_packages WHERE STATUS IN ('REGISTERED','RUNNING','STOPPED') ORDER BY NAME, upload_timestamp DESC", PluginPackageEntity.class);
        return (List<PluginPackageEntity>) pluginPackageQuery.getResultList();
    }

    @SuppressWarnings("unchecked")
	private List<PluginInstanceEntity> fetchRunningPluginInstanceInfo() {
        Query pliginInsQuery = entityManager.createNativeQuery("SELECT i.id,i.package_id,i.instance_name,i.container_name,i.host,i.port,i.container_status, " +
                "p.`name` AS package_name FROM plugin_instances i " +
                "JOIN plugin_packages p ON i.`package_id`=p.`id` WHERE i.`container_status` = 'RUNNING'", PluginInstanceEntity.class);
        return (List<PluginInstanceEntity>) pliginInsQuery.getResultList();
    }

    private void tryCalculateInterfaceRoutes(List<PluginRouteItemDto> resultList, Map<String,List<PluginInstanceEntity>> pluginInstanceMap,Map<String,PluginPackageEntity> latestActivePluginPackageMap) {

        List<PluginConfigInterfaceEntity> interfaces = fetchPluginConfigInterfaceInfo();

        if (interfaces == null || interfaces.isEmpty()) {
            return;
        }

        Object mapValue = new Object();
        Map<String, Object> calculatedServiceNames = new HashMap<String, Object>();

        for (PluginConfigInterfaceEntity intf : interfaces) {
            if (intf == null) {
                continue;
            }

            if (StringUtils.isBlank(intf.getServiceName())) {
                continue;
            }

            if (calculatedServiceNames.containsKey(intf.getServiceName())) {
                continue;
            }

            List<PluginRouteItemDto> routeItems = tryCalculatePluginRouteItem(intf, pluginInstanceMap, latestActivePluginPackageMap);
            if(!routeItems.isEmpty()){
                resultList.addAll(routeItems);
                calculatedServiceNames.put(intf.getServiceName(), mapValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
	private List<PluginConfigInterfaceEntity> fetchPluginConfigInterfaceInfo() {
        Query query = entityManager.createNativeQuery("SELECT i.id,i.service_name,i.path,i.http_method,pp.`name` as package_name " +
                "FROM plugin_config_interfaces i JOIN plugin_configs c ON i.`plugin_config_id` = c.`id` " +
                "JOIN plugin_configs pc ON i.`plugin_config_id`=pc.`id` " +
                "JOIN plugin_packages pp ON pc.`plugin_package_id`=pp.`id` " +
                "WHERE c.`status`='ENABLED'", PluginConfigInterfaceEntity.class);
        return (List<PluginConfigInterfaceEntity>) query.getResultList();
    }

    private List<PluginRouteItemDto> tryCalculatePluginRouteItem(PluginConfigInterfaceEntity intf, Map<String,List<PluginInstanceEntity>> packageId2PluginInstances,
                                                                 Map<String,PluginPackageEntity> latestActivePluginPackageMap) {
        String packageName = intf.getPackageName();

        List<PluginRouteItemDto> routeItems = new LinkedList<>();
        PluginPackageEntity latestActivePluginPackage = latestActivePluginPackageMap.get(packageName);
        if(latestActivePluginPackage == null){
            return routeItems;
        }

        List<PluginInstanceEntity> pluginInstances = packageId2PluginInstances.get(latestActivePluginPackage.getId());
        if (pluginInstances == null) {
            return routeItems;
        }

        if (StringUtils.isBlank(intf.getPath())) {
            return routeItems;
        }

        String httpMethod = intf.getHttpMethod();
        if (StringUtils.isBlank(httpMethod)) {
            httpMethod = HttpMethod.POST.name();
        }

        String path = intf.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        for (PluginInstanceEntity pluginInstance : pluginInstances) {
            
            if(!validateKeyRouteProperties(intf, pluginInstance)){
                log.info("such route is invalid,service name={}", intf.getServiceName());
            }
            
            PluginRouteItemDto routeItem = new PluginRouteItemDto();
            routeItem.setContext(packageName);
            routeItem.setHttpMethod(httpMethod);
            routeItem.setPath(path);
            routeItem.setHost(pluginInstance.getHost());
            routeItem.setHttpScheme(HTTP_SCHEME);
            routeItem.setPort(String.valueOf(pluginInstance.getPort()));

            routeItem.setWeight("0");
            
            routeItems.add(routeItem);
        }

        return routeItems;
    }

    private boolean validateKeyRouteProperties(PluginConfigInterfaceEntity intf,PluginInstanceEntity pluginInstance){
        if(StringUtils.isBlank(intf.getPath())){
            return false;
        }
        
        if(StringUtils.isBlank(pluginInstance.getHost())){
            return false;
        }
        
        if(pluginInstance.getPort() == null){
            return false;
        }
        
        return true;
    }

    public List<PluginInstance> getRunningPluginInstances(String pluginName) {
        List<PluginPackage> activePluginPackages = pluginPackageRepository.findLatestActiveVersionPluginPackagesByName(pluginName);
        if (activePluginPackages == null || activePluginPackages.isEmpty()) {
            log.info("Plugin package [{}] not found.", pluginName);
            return null;
        }
        List<PluginInstance> runningInstances = new ArrayList<PluginInstance>();
        for(PluginPackage pkg : activePluginPackages) {
        	List<PluginInstance> instances = pluginInstanceRepository
                    .findByContainerStatusAndPluginPackage_Id(PluginInstance.CONTAINER_STATUS_RUNNING, pkg.getId());
        	if(instances != null && (!instances.isEmpty())) {
        		runningInstances.addAll(instances);
        	}
        	
        	if(runningInstances.size() > 0) {
        		break;
        	}
        }
        if (runningInstances.isEmpty()) {
            log.info("No instance for plugin [{}] is available.", pluginName);
        }
        return runningInstances;
    }

    public List<PluginRouteItemDto> getPluginRouteItemsByName(String name) {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository
                .findAllByContainerStatusAndInstanceName(PluginInstance.CONTAINER_STATUS_RUNNING, name);

        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getInstanceName());

                dtos.add(d);
            });
        }

        return dtos;
    }

    @Entity
    static public class PluginInstanceEntity{
        @Id
        private String id;
        private String packageId;
        private String instanceName;
        private String containerName;
        private String host;
        private Integer port;
        private String containerStatus;
        private String packageName;

        public PluginInstanceEntity(){
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPackageId() {
            return packageId;
        }

        public void setPackageId(String packageId) {
            this.packageId = packageId;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getContainerStatus() {
            return containerStatus;
        }

        public void setContainerStatus(String containerStatus) {
            this.containerStatus = containerStatus;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    @Entity
    static public class PluginPackageEntity{
        @Id
        private String id;
        private String name;

        public PluginPackageEntity(){

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Entity
    static public class PluginConfigInterfaceEntity{
        @Id
        private String id;
        private String serviceName;
        private String path;
        private String httpMethod;
        private String packageName;

        public PluginConfigInterfaceEntity(){
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }
}
