package com.webank.wecube.platform.core.domain;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class BatchJobHost {
    public static final String BATCH_JOB_STATUS_CREATED= "created";
    public static final String BATCH_JOB_STATUS_DOING = "doing";
    public static final String BATCH_JOB_STATUS_DONE = "done";

    public static final String BATCH_JOB_RESULT_SUCCESS = "success";
    public static final String BATCH_JOB_RESULT_FAILED = "failed";

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name="batch_job_id")
    private Integer batchJobId;

    @Column(name="host_ip")
    private String hostIp;

    @Column(name="output_url")
    private String outputUrl;

    @Column
    private String status ;

    @Column
    private String result ;

    public BatchJobHost() {
        this(null);
    }

    public BatchJobHost(Integer id) {
        this.setId(id);
    }
}

