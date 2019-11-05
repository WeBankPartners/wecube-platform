package com.webank.wecube.platform.core.model.workflow;

public class PluginInvocationResult extends PluginInvocationCommand {

    private int resultCode;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "PluginInvocationResult [resultCode=" + resultCode + ", toString()=" + super.toString() + "]";
    }

}
