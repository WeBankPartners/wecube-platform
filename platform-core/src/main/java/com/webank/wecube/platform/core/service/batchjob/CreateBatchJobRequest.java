package com.webank.wecube.platform.core.service.batchjob;
import java.util.List;
public class CreateBatchJobRequest {
    private String  creator;
    private List <String> hosts;
    private String scriptContent;

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getHosts() {
        return this.hosts;
    }
    public void setHosts(List <String> hosts) {
        this.hosts = hosts;
    }

    public String getScriptContent() {
        return this.scriptContent;
    }
    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }
}
