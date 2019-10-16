package com.webank.wecube.platform.core.domain;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class BatchJob {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String creator;

    @Column(name ="script_url")
    private String scriptUrl;

    public BatchJob() {
        this(null);
    }

    public BatchJob(Integer id) {
        this.setId(id);
    }
}
