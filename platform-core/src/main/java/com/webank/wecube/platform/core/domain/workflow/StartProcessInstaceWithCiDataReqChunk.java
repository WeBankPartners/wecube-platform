package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartProcessInstaceWithCiDataReqChunk {

    private List<StartProcessInstaceWithCiDataReq> requests = new ArrayList<StartProcessInstaceWithCiDataReq>();

    private AttachVO attach;

    public List<StartProcessInstaceWithCiDataReq> getRequests() {
        return requests;
    }

    public void setRequests(List<StartProcessInstaceWithCiDataReq> requests) {
        this.requests = requests;
    }

    public AttachVO getAttach() {
        return attach;
    }

    public void setAttach(AttachVO attach) {
        this.attach = attach;
    }
}
