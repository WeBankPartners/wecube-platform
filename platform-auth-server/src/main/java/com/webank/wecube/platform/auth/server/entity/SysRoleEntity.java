package com.webank.wecube.platform.auth.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

@Entity
@Table(name = "AUTH_SYS_ROLE")
public class SysRoleEntity extends AbstractTraceableEntity {

    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME", unique = true)
    private String name;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "EMAIL")
    private String email;

    public SysRoleEntity() {
    }

    public SysRoleEntity(String name, String displayName, String email) {
        this.setName(name);
        this.setDisplayName(displayName);
        this.setEmail(email);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
