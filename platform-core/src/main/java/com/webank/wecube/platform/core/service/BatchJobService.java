package com.webank.wecube.platform.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.BatchJob;
import com.webank.wecube.platform.core.domain.BatchJobHost;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.jpa.BatchJobHostRepository;
import com.webank.wecube.platform.core.jpa.BatchJobRepository;
import com.webank.wecube.platform.core.service.batchjob.CreateBatchJobRequest;
import com.webank.wecube.platform.core.service.batchjob.CreateBatchJobResponse;
import com.webank.wecube.platform.core.service.batchjob.GetContextRequest;
import com.webank.wecube.platform.core.service.batchjob.SearchTextProcessorRequest;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRunScriptOutput;
import com.webank.wecube.platform.core.support.S3Client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Setter
@Slf4j
public class BatchJobService {
    private static final String CONSTRANT_ENDPOINT = "endpoint";
    private static final String CONSTRANT_ACCESS_KEY = "accessKey";
    private static final String CONSTRANT_SECRET_KEY = "secretKey";
    private static final String S3_BUCKET_NAME_FOR_SCRIPTS = "scripts";
    private static final String S3_BUCKET_NAME_FOR_SCRIPT_OUTPUT = "scripts-output";
    private static final String SCRIPT_RUN_RESULT_SUCCESS = "success";
    private static final String SCRIPT_RUN_RESULT_FAILED = "failed";

    @Autowired
    private ApplicationProperties.S3Properties s3properties;

    @Autowired
    BatchJobRepository batchJobRepository;

    @Autowired
    private PluginInstanceService pluginInstanceService;

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    BatchJobHostRepository batchJobHostRepository;

    @Autowired
    private PluginProperties pluginProperties;
    @Autowired
    private S3Client s3Client;

    public CreateBatchJobResponse createBatchJob(CreateBatchJobRequest request) throws IOException {
        String content = request.getScriptContent();
        List<String> hosts = request.getHosts();
        byte[] byteContent;
        BatchJob batchJob = new BatchJob();

        if (null == content || content.isEmpty()) {
            throw new WecubeCoreException("scriptContent is empty");
        }
        if (null == hosts || hosts.isEmpty()) {
            throw new WecubeCoreException("hosts is empty");
        }

        if (null != request.getCreator()) {
            batchJob.setCreator(request.getCreator());
        }
        batchJob = batchJobRepository.save(batchJob);

        String s3KeyName = String.format(new String("%d.sh"), batchJob.getId());
        String s3Url = s3Client.uploadBinaryToS3(S3_BUCKET_NAME_FOR_SCRIPTS, s3KeyName, content.getBytes());
        batchJob.setScriptUrl(s3Url);
        batchJobRepository.save(batchJob);

        for (int i = 0; i < hosts.size(); i++) {
            BatchJobHost batchJobHost = new BatchJobHost();
            batchJobHost.setBatchJobId(batchJob.getId());
            batchJobHost.setHostIp(hosts.get(i));
            batchJobHost.setStatus(BatchJobHost.BATCH_JOB_STATUS_CREATED);
            batchJobHostRepository.save(batchJobHost);
        }

        CreateBatchJobResponse resp = new CreateBatchJobResponse();
        resp.setBatchJobId(batchJob.getId());
        return resp;
    }

    public void executeBatchJob(Integer batchId) throws IOException {
        PluginRequest.DefaultPluginRequest request = new PluginRequest.DefaultPluginRequest();
        List<Map<String, Object>> inputs = new ArrayList<Map<String, Object>>();
        String s3KeyName, s3Url;
        List<PluginInstance> instances = pluginInstanceService
                .getRunningPluginInstances(pluginProperties.getPluginPackageNameOfDeploy());

        Optional<BatchJob> batchJobs = batchJobRepository.findById(batchId);
        if (!batchJobs.isPresent()) {
            throw new WecubeCoreException("invalid batchId ");
        }
        BatchJob batchJob = batchJobs.get();

        List<BatchJobHost> hosts = batchJobHostRepository.findBatchJobByBatchId(batchId);
        for (int i = 0; i < hosts.size(); i++) {
            inputs.add(ImmutableMap.<String, Object>builder().put("target", hosts.get(i).getHostIp())
                    .put(CONSTRANT_ENDPOINT, batchJob.getScriptUrl())
                    .put(CONSTRANT_ACCESS_KEY, s3properties.getAccessKey())
                    .put(CONSTRANT_SECRET_KEY, s3properties.getSecretKey()).build());
            hosts.get(i).setStatus(BatchJobHost.BATCH_JOB_STATUS_DOING);
            batchJobHostRepository.save(hosts.get(i));
        }
        request.setInputs(inputs);
        PluginResponse.ResultData<PluginRunScriptOutput> response = pluginServiceStub
                .callPluginRunScript(pluginInstanceService.getInstanceAddress(instances.get(0)), request);
        List<PluginRunScriptOutput> outputs = response.getOutputs();

        if (hosts.size() != outputs.size()) {
            throw new WecubeCoreException("sizeof(host) do not match sizeof(ouput)");
        }

        for (int i = 0; i < hosts.size(); i++) {
            s3KeyName = String.format(new String("batchId_%d_%s.txt"), batchId, hosts.get(i).getHostIp());
            s3Url = s3Client.uploadBinaryToS3(S3_BUCKET_NAME_FOR_SCRIPT_OUTPUT, s3KeyName,
                    outputs.get(i).getDetail().getBytes());
            hosts.get(i).setOutputUrl(s3Url);
            hosts.get(i).setStatus(BatchJobHost.BATCH_JOB_STATUS_DONE);

            if (outputs.get(i).getRetCode() == 0) {
                hosts.get(i).setResult(SCRIPT_RUN_RESULT_SUCCESS);
            } else {
                hosts.get(i).setResult(SCRIPT_RUN_RESULT_FAILED);
            }

            batchJobHostRepository.save(hosts.get(i));
        }
    }

    public Object searchTextProcessor(SearchTextProcessorRequest req) {
        PluginRequest.DefaultPluginRequest request = new PluginRequest.DefaultPluginRequest();
        List<Map<String, Object>> inputs = new ArrayList<Map<String, Object>>();
        List<PluginInstance> instances = pluginInstanceService
                .getRunningPluginInstances(pluginProperties.getPluginPackageNameOfDeploy());

        String content = req.getPattern();
        List<String> hosts = req.getHosts();
        Integer batchId = req.getBatchId();

        if (null == content || content.isEmpty()) {
            throw new WecubeCoreException("Pattern is empty");
        }
        if (null == hosts || hosts.isEmpty()) {
            throw new WecubeCoreException("host is empty");
        }

        for (int i = 0; i < hosts.size(); i++) {
            List<BatchJobHost> dbHosts = batchJobHostRepository.findBatchJobByBatchIdAndHostIP(batchId, hosts.get(i));
            if (null == dbHosts || dbHosts.isEmpty()) {
                throw new WecubeCoreException("don't have batch job info");
            }

            if (dbHosts.size() > 1) {
                throw new WecubeCoreException("get more than one info");
            }

            inputs.add(ImmutableMap.<String, Object>builder().put("target", dbHosts.get(0).getHostIp())
                    .put(CONSTRANT_ENDPOINT, dbHosts.get(0).getOutputUrl()).put("pattern", req.getPattern())
                    .put(CONSTRANT_ACCESS_KEY, s3properties.getAccessKey())
                    .put(CONSTRANT_SECRET_KEY, s3properties.getSecretKey()).build());
        }
        request.setInputs(inputs);

        return pluginServiceStub.searchText(pluginInstanceService.getInstanceAddress(instances.get(0)), request);
    }

    public Object getContext(GetContextRequest req) {
        PluginRequest.DefaultPluginRequest request = new PluginRequest.DefaultPluginRequest();
        List<Map<String, Object>> inputs = new ArrayList<Map<String, Object>>();
        List<PluginInstance> instances = pluginInstanceService
                .getRunningPluginInstances(pluginProperties.getPluginPackageNameOfDeploy());

        String host = req.getHost();
        Integer batchId = req.getBatchId();
        Integer line = req.getLineNumber();

        if (null == line || line < 1) {
            throw new WecubeCoreException("line is empty");
        }
        if (null == batchId || batchId < 1) {
            throw new WecubeCoreException("batchId is empty");
        }
        if (null == host || host.isEmpty()) {
            throw new WecubeCoreException("host is empty");
        }

        List<BatchJobHost> dbHosts = batchJobHostRepository.findBatchJobByBatchIdAndHostIP(batchId, host);
        if (null == dbHosts || dbHosts.isEmpty() || (dbHosts.size() > 1)) {
            throw new WecubeCoreException("get batch job info error");
        }

        inputs.add(ImmutableMap.<String, Object>builder().put(CONSTRANT_ENDPOINT, dbHosts.get(0).getOutputUrl())
                .put("offset", req.getOffSet()).put("lineNum", line)
                .put(CONSTRANT_ACCESS_KEY, s3properties.getAccessKey())
                .put(CONSTRANT_SECRET_KEY, s3properties.getSecretKey()).build());

        request.setInputs(inputs);

        return pluginServiceStub.getTextContext(pluginInstanceService.getInstanceAddress(instances.get(0)), request);
    }
}
