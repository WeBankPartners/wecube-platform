CREATE TABLE `core_proc_exec_context` (
  `id` VARCHAR(30) NOT NULL,
  `proc_def_id` VARCHAR(45) NULL,
  `proc_inst_id` INT NULL,
  `node_def_id` VARCHAR(45) NULL,
  `node_inst_id` INT NULL,
  `req_id` VARCHAR(45) NULL,
  `req_dir` VARCHAR(45) NULL,
  `ctx_type` VARCHAR(45) NULL,
  `ctx_data_format` VARCHAR(45) NULL,
  `ctx_data` MEDIUMTEXT NULL,
  `created_by` VARCHAR(45) NULL,
  `updated_by` VARCHAR(45) NULL,
  `created_time` DATETIME NULL,
  `updated_time` DATETIME NULL,
  `rev` int(11) NOT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
  
ALTER TABLE `plugin_config_interfaces` ADD COLUMN `description` VARCHAR(200) NULL;

ALTER TABLE `plugin_config_interface_parameters` ADD COLUMN `description` VARCHAR(200) NULL;

drop table if exists `core_object_meta`;
drop table if exists `core_object_property_meta`;
drop table if exists `core_object_list_var`;
drop table if exists `core_object_var`;
drop table if exists `core_object_property_var`;

CREATE TABLE IF NOT EXISTS `plugin_object_meta` (
  `id` varchar(20) COLLATE utf8_bin  NOT NULL,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `package_name` varchar(45) COLLATE utf8_bin NOT NULL,
  `source` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `latest_source` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `plugin_object_property_meta` (
  `id` varchar(45) COLLATE utf8_bin NOT NULL,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `data_type` varchar(45) COLLATE utf8_bin NOT NULL,
  `ref_type` varchar(45) COLLATE utf8_bin DEFAULT NULL,
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
  `ref_name` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `plugin_object_var` (
  `id` varchar(45) COLLATE utf8_bin NOT NULL,
  `object_meta_id` varchar(45) COLLATE utf8_bin NOT NULL,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `package_name` varchar(45) COLLATE utf8_bin NOT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `parent_object_var_id` VARCHAR(45) COLLATE utf8_bin NULL,
  `parent_object_name` VARCHAR(45) COLLATE utf8_bin NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `plugin_object_property_var` (
  `id` varchar(45) COLLATE utf8_bin NOT NULL,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `data_type` varchar(45) COLLATE utf8_bin NOT NULL,
  `object_property_meta_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `object_meta_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `object_var_id` varchar(45) COLLATE utf8_bin NOT NULL,
  `data_value` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `data_type_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `data_id` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `data_name` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_sensitive` bit(1) DEFAULT NULL,
  `object_name` VARCHAR(45) COLLATE utf8_bin NULL,
  `package_name` VARCHAR(45) COLLATE utf8_bin NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `plugin_object_list_var` (
  `id` varchar(20) COLLATE utf8_bin NOT NULL,
  `data_type` varchar(45) COLLATE utf8_bin NOT NULL,
  `data_value` varchar(1000) COLLATE utf8_bin NOT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `is_sensitive` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `plugin_object_meta` ADD COLUMN `config_id` VARCHAR(45) COLLATE utf8_bin NULL;
ALTER TABLE `plugin_object_property_meta` ADD COLUMN `config_id` VARCHAR(45) COLLATE utf8_bin  NULL;

