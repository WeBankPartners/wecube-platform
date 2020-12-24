ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `ref_package` VARCHAR(45) NULL,
ADD COLUMN `ref_entity` VARCHAR(45) NULL,
ADD COLUMN `ref_attr` VARCHAR(45) NULL;

ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `mandatory` BIT(1) NULL DEFAULT 0;

ALTER TABLE `core_re_proc_def_info` 
ADD COLUMN `dynamic_bind` VARCHAR(45) NULL DEFAULT 'N';

ALTER TABLE `core_re_proc_def_info` 
ADD COLUMN `pre_check` VARCHAR(45) NULL DEFAULT 'N';

ALTER TABLE `core_re_task_node_def_info` 
ADD COLUMN `dynamic_bind` VARCHAR(45) NULL DEFAULT 'N';

ALTER TABLE `core_re_task_node_def_info` 
ADD COLUMN `pre_check` VARCHAR(45) NULL DEFAULT 'N';

ALTER TABLE `core_operation_event`
ADD COLUMN `rev` INT(11) DEFAULT '0';

CREATE TABLE `core_object_meta` (
  `id` varchar(20) NOT NULL,
  `name` varchar(45) NOT NULL,
  `package_name` varchar(45) NOT NULL,
  `source` varchar(45) DEFAULT NULL,
  `latest_source` varchar(45) DEFAULT NULL,
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `core_object_property_meta` (
  `id` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `data_type` varchar(45) NOT NULL,
  `ref_type` varchar(45) DEFAULT NULL,
  `map_type` varchar(45) NOT NULL,
  `map_expr` varchar(500) DEFAULT NULL,
  `object_meta_id` varchar(45) NOT NULL,
  `object_name` varchar(45) NOT NULL,
  `package_name` varchar(45) NOT NULL,
  `source` varchar(45) DEFAULT NULL,
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_sensitive` bit(1) DEFAULT NULL,
  `ref_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `core_object_var` (
  `id` varchar(45) NOT NULL,
  `object_meta_id` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `package_name` varchar(45) NOT NULL,
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `core_object_property_var` (
  `id` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `data_type` varchar(45) NOT NULL,
  `object_property_meta_id` varchar(45) DEFAULT NULL,
  `object_meta_id` varchar(45) DEFAULT NULL,
  `object_var_id` varchar(45) NOT NULL,
  `data_value` varchar(500) DEFAULT NULL,
  `data_type_id` varchar(45) DEFAULT NULL,
  `data_id` varchar(45) DEFAULT NULL,
  `data_name` varchar(45) DEFAULT NULL,
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `updated_by` varchar(45) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_sensitive` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `core_object_list_var` (
  `id` varchar(20) NOT NULL,
  `data_type` varchar(45) NOT NULL,
  `data_value` varchar(1000) NOT NULL,
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `is_sensitive` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
