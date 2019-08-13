package com.webank.wecube.core.domain.plugin;

import lombok.*;

import javax.persistence.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "plugin_instances")
@Setter
public class PluginInstance {
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_REMOVED = "REMOVED";

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private PluginPackage pluginPackage;

    @Column
    private String instanceContainerId;
    @Column
    private String host;
    @Column
    private Integer port;
    @Column
    private String status;
}
