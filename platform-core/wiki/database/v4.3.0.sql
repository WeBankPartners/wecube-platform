CREATE TABLE `plugin_object_meta` (
  `id` varchar(64) COLLATE utf8_bin NOT NULL,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `package_name` varchar(45) COLLATE utf8_bin NOT NULL,
  `source` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `latest_source` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `config_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `map_expr` varchar(300) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `plugin_object_property_meta` (
           `id` varchar(45) COLLATE utf8_bin NOT NULL,
           `name` varchar(45) COLLATE utf8_bin NOT NULL,
           `data_type` varchar(45) COLLATE utf8_bin NOT NULL,
           `map_type` varchar(45) COLLATE utf8_bin NOT NULL,
           `map_expr` varchar(500) COLLATE utf8_bin DEFAULT NULL,
           `object_meta_id` varchar(45) COLLATE utf8_bin NOT NULL,
           `object_name` varchar(45) COLLATE utf8_bin NOT NULL,
           `package_name` varchar(45) COLLATE utf8_bin NOT NULL,
           `source` varchar(45) COLLATE utf8_bin DEFAULT NULL,
           `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
           `created_time` datetime NOT NULL,
           `updated_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
           `updated_time` datetime DEFAULT NULL,
           `is_sensitive` bit(1) DEFAULT NULL,
           `ref_object_name` varchar(45) COLLATE utf8_bin DEFAULT NULL,
           `config_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
           `multiple` varchar(10) COLLATE utf8_bin DEFAULT NULL,
           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

alter table plugin_object_meta modify id varchar(64) not null;

alter table proc_ins add column request_info text default null comment 'taskman请求信息';