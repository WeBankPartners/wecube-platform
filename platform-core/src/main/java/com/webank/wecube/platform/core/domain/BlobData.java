package com.webank.wecube.platform.core.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class BlobData {

    private static final long serialVersionUID = -2952735933715107253L;

    public static final String TYPE_CI_TYPE_TEMPLATE = "ci-type-template";
    public static final String TYPE_ICON = "icon";

    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String type;
    @Column
    private String name;
    @Lob
    @Column
    private byte[] content;

    public BlobData() {
        this(null);
    }
    public BlobData(Integer id) {
        this.setId(id);
    }

}
