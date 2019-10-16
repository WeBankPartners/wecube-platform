package com.webank.wecube.platform.core.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resource_server")
public class ResourceServer {
    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "host")
    private String host;

    @NotBlank
    @Column(name = "port")
    private String port;

    @NotBlank
    @Column(name = "login_username")
    private String loginUsername;

    @NotBlank
    @Column(name = "login_password")
    private String loginPassword;

    @NotBlank
    @Column(name = "type")
    private String type;

    @Column(name = "is_allocated")
    private Integer isAllocated;

    @NotBlank
    @Column(name = "purpose")
    private String purpose;

    @Column(name = "status")
    private String status;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "resourceServer", fetch = FetchType.EAGER)
    private List<ResourceItem> resourceItems = new ArrayList<>();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Timestamp updatedDate;
}
