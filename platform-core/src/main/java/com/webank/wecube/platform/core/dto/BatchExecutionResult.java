package com.webank.wecube.platform.core.dto;

import java.util.Map;

public class BatchExecutionResult {

    private ItsDangerConfirmResultDto itsDangerConfirmResultDto;

    private Map<String, ExecutionJobResponseDto> result;

    public ItsDangerConfirmResultDto getItsDangerConfirmResultDto() {
        return itsDangerConfirmResultDto;
    }

    public void setItsDangerConfirmResultDto(ItsDangerConfirmResultDto itsDangerConfirmResultDto) {
        this.itsDangerConfirmResultDto = itsDangerConfirmResultDto;
    }

    public Map<String, ExecutionJobResponseDto> getResult() {
        return result;
    }

    public void setResult(Map<String, ExecutionJobResponseDto> result) {
        this.result = result;
    }

}
