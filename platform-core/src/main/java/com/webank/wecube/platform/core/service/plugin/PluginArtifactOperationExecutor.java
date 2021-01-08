package com.webank.wecube.platform.core.service.plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.PluginArtifactPullReq;

@Service
public class PluginArtifactOperationExecutor {
    private static final Logger log = LoggerFactory.getLogger(PluginArtifactOperationExecutor.class);
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private PluginArtifactsMgmtService pluginPackageService;

    public void pullPluginArtifact(PluginArtifactPullContext ctx) {
        PluginArtifactOperationWorker worker = new PluginArtifactOperationWorker();
        worker.setPluginPackageService(pluginPackageService);
        worker.setPluginArtifactPullContext(ctx);
        executorService.submit(worker);
    }

    public static class PluginArtifactOperationWorker implements Callable<Void> {
        private PluginArtifactPullContext pluginArtifactPullContext;
        private PluginArtifactsMgmtService pluginPackageService;

        @Override
        public Void call() throws Exception {
            try {
                pluginPackageService.pullPluginArtifact(pluginArtifactPullContext);
            } catch (Exception e) {
                log.error("errors while executing pull plugin artifact", e);
                pluginPackageService.handlePullPluginArtifactFailure(pluginArtifactPullContext, e);
            }

            return null;
        }

        public void setPluginArtifactPullContext(PluginArtifactPullContext pluginArtifactPullContext) {
            this.pluginArtifactPullContext = pluginArtifactPullContext;
        }

        public void setPluginPackageService(PluginArtifactsMgmtService pluginPackageService) {
            this.pluginPackageService = pluginPackageService;
        }

    }

    public static class PluginArtifactPullContext {
        private String remoteEndpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;

        private String keyName;
        private String requestId;
        private PluginArtifactPullReq entity;

        public String getRemoteEndpoint() {
            return remoteEndpoint;
        }

        public void setRemoteEndpoint(String remoteEndpoint) {
            this.remoteEndpoint = remoteEndpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public PluginArtifactPullReq getEntity() {
            return entity;
        }

        public void setEntity(PluginArtifactPullReq entity) {
            this.entity = entity;
        }

    }
}
