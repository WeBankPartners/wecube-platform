package com.webank.wecube.platform.core.model.workflow;

public class PluginInvocationResult extends PluginInvocationCommand {

    private String resultCode;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public PluginInvocationResult withResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public PluginInvocationResult parsePluginInvocationCommand(PluginInvocationCommand cmd) {
        if (cmd == null) {
            return this;
        }

        this.setExecutionId(cmd.getExecutionId());
        this.setNodeId(cmd.getNodeId());
        this.setNodeName(cmd.getNodeName());
        this.setProcDefId(cmd.getProcDefId());
        this.setProcDefKey(cmd.getProcDefKey());
        this.setProcDefVersion(cmd.getProcDefVersion());
        this.setProcInstId(cmd.getProcInstId());
        this.setProcInstKey(cmd.getProcInstKey());

        return this;
    }

    @Override
    public String toString() {
        return "resultCode=" + resultCode + ", toString()=" + super.toString() + "";
    }

}
