package com.webank.wecube.core.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resource_item")
public class ResourceItem {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "additional_properties")
    private String additionalProperties;

    @Column(name = "resource_server_id")
    private Integer resourceServerId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "resource_server_id", insertable = false, updatable = false)
    private ResourceServer resourceServer;

    @Column(name = "is_allocated")
    private Integer isAllocated;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Timestamp updatedDate;
}
