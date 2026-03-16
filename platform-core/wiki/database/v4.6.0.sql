
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

