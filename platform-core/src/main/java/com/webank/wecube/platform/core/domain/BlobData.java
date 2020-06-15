package com.webank.wecube.platform.core.domain;

import javax.persistence.*;

@Entity
public class BlobData {

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
