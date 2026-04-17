
ALTER TABLE resource_server MODIFY COLUMN login_password varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '连接密码';

CREATE TABLE `plugin_package_runtime_resources_volume` (
  `id` varchar(64) NOT NULL,
  `plugin_package_id` varchar(64) NOT NULL,
  `name` varchar(64) DEFAULT NULL COMMENT '卷名称',
  `size` varchar(16) NOT NULL COMMENT '卷大小，如10Gi',
  `mount_path` varchar(255) NOT NULL COMMENT '卷路径',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

alter table proc_data_preview modify column entity_data_name varchar(1024) default null;
alter table proc_ins_graph_node modify column display_name varchar(1024) default null;
ALTER TABLE proc_ins MODIFY COLUMN entity_data_name VARCHAR(1024) DEFAULT NULL;
ALTER TABLE proc_data_binding MODIFY COLUMN entity_data_name VARCHAR(1024) DEFAULT NULL;
ALTER TABLE proc_data_cache MODIFY COLUMN entity_data_name VARCHAR(1024) DEFAULT NULL;
ALTER TABLE proc_data_preview MODIFY COLUMN entity_data_name VARCHAR(1024) DEFAULT NULL;



-- v4.6.0.1 支持插件资源限制
ALTER TABLE plugin_package_runtime_resources_docker ADD cpu varchar(32) NULL COMMENT '插件建议CPU，如500m/0.5/1/3等';
ALTER TABLE plugin_package_runtime_resources_docker ADD memory varchar(32) NULL COMMENT '插件建议内存，如512Mi/0.5Gi/1Gi/3Gi';

ALTER TABLE plugin_instances ADD cpu varchar(32) NULL COMMENT '插件实际运行CPU，如500m/0.5/1/3等';
ALTER TABLE plugin_instances ADD memory varchar(32) NULL COMMENT '插件实际运行内存，如512Mi/0.5Gi/1Gi/3Gi';
ALTER TABLE plugin_instances ADD replicas INT DEFAULT 1 NULL COMMENT '实例副本数量(仅k8s)';

