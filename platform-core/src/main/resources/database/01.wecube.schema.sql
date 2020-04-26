SET FOREIGN_KEY_CHECKS = 0;
drop table if exists plugin_packages;
CREATE TABLE `plugin_packages` (
    `id`                    VARCHAR(255) PRIMARY KEY,
    `name`                  VARCHAR(63) NOT NULL,
    `version`               VARCHAR(20) NOT NULL,
    `status`                VARCHAR(20) NOT NULL default 'UNREGISTERED',
    `upload_timestamp`      timestamp default current_timestamp,
    `ui_package_included`   BIT default 0,
    UNIQUE INDEX `name` (`name`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_dependencies;
create table plugin_package_dependencies (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  dependency_package_name VARCHAR(63) not null,
  dependency_package_version varchar(20) not null
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_menus;
create table plugin_package_menus (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  code varchar(64) not null,
  category varchar(64) not null,
  source VARCHAR(255) DEFAULT 'PLUGIN',
  display_name VARCHAR(256) not null,
  local_display_name VARCHAR(256) not null,
  menu_order INTEGER NOT NULL AUTO_INCREMENT,
  path VARCHAR(256) not null,
  active BIT default 0,
  KEY `plugin_package_menu_order` (`menu_order`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS plugin_package_data_model;
CREATE TABLE plugin_package_data_model
(
    id                  VARCHAR(255) PRIMARY KEY,
    version             INTEGER                        NOT NULL DEFAULT 1,
    package_name        VARCHAR(63)                             NOT NULL,
    is_dynamic          BIT  default 0,
    update_path         VARCHAR(256),
    update_method       VARCHAR(10),
    update_source       VARCHAR(32),
    update_time         BIGINT   default 0     NOT NULL,
    UNIQUE uk_plugin_package_data_model(package_name, version)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS plugin_package_entities;
CREATE TABLE plugin_package_entities
(
    id                 VARCHAR(255) PRIMARY KEY,
    data_model_id      VARCHAR(255)                        NOT NULL,
    data_model_version INTEGER                        NOT NULL,
    package_name        VARCHAR(63)                    NOT NULL,
    name               VARCHAR(100)                   NOT NULL,
    display_name       VARCHAR(100)                   NOT NULL,
    description        VARCHAR(256)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS plugin_package_attributes;
CREATE TABLE plugin_package_attributes
(
    id           VARCHAR(255) PRIMARY KEY,
    entity_id    VARCHAR(255)                        NOT NULL,
    reference_id VARCHAR(255),
    name         VARCHAR(100)                   NOT NULL,
    description  VARCHAR(256),
    data_type    VARCHAR(20)                    NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists system_variables;
create table system_variables (
  id VARCHAR(255) PRIMARY KEY,
  package_name VARCHAR(63) ,
  name varchar(255) not null,
  value varchar(2000),
  default_value varchar(2000) null,
  scope varchar(50) not null default 'global',
  source varchar(500) null default 'system',
  status varchar(50) null default 'active'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_authorities;
create table plugin_package_authorities (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  role_name varchar(64) not null,
  menu_code varchar(64) not null
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;


drop table if exists plugin_package_runtime_resources_docker;
create table plugin_package_runtime_resources_docker (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  image_name varchar(256) not null, 
  container_name varchar(128) not null,
  port_bindings varchar(256) not null, 
  volume_bindings varchar(1024) not null,
  env_variables varchar(2000)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_runtime_resources_mysql;
create table plugin_package_runtime_resources_mysql (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  schema_name varchar(128) not null,
  init_file_name varchar(256),
  upgrade_file_name varchar(256)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_runtime_resources_s3;
create table plugin_package_runtime_resources_s3 (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  bucket_name varchar(32) not null
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;


drop table if exists plugin_configs;
CREATE TABLE `plugin_configs` (
  id VARCHAR(255) PRIMARY KEY,
  `plugin_package_id` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `target_package` VARCHAR(63) NULL DEFAULT NULL,
  `target_entity` VARCHAR(100) NULL,
  `target_entity_filter_rule` VARCHAR(1024) NULL DEFAULT '',
  `register_name` VARCHAR(100) NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL default 'DISABLED'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_config_interfaces;
create table plugin_config_interfaces (
    `id` VARCHAR(255) PRIMARY KEY,
    `plugin_config_id` VARCHAR(255) NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `service_name` VARCHAR(500) NOT NULL, 
    `service_display_name` VARCHAR(500) NOT NULL,
    `path` VARCHAR(500) NOT NULL, 
    `http_method` VARCHAR(10) NOT NULL, 
    `is_async_processing` VARCHAR(1) DEFAULT 'N',
    `type` VARCHAR(16) DEFAULT 'EXECUTION',
    `filter_rule` VARCHAR(1024) NULL DEFAULT ''
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_config_interface_parameters;
CREATE TABLE `plugin_config_interface_parameters` (
    `id` VARCHAR(255) PRIMARY KEY,
    `plugin_config_interface_id` VARCHAR(255) NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `data_type` VARCHAR(50) NOT NULL,
    `mapping_type` VARCHAR(50) NULL DEFAULT NULL,
    `mapping_entity_expression` varchar(1024) NULL DEFAULT NULL,
    `mapping_system_variable_name` VARCHAR(500) NULL DEFAULT NULL,
    `required` varchar(5),
    `sensitive_data` varchar(5)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;


drop table if exists menu_items;
create table menu_items
(
  id VARCHAR(255) PRIMARY KEY,
    parent_code VARCHAR(64),
    code        VARCHAR(64) NOT NULL,
    source      VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    local_display_name VARCHAR(200),
    menu_order INTEGER NOT NULL AUTO_INCREMENT,
    UNIQUE KEY uk_code (code),
    KEY `menu_item_order` (`menu_order`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_package_resource_files;
create table plugin_package_resource_files
(
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  package_name varchar(63) not null,
  package_version varchar(20) not null,
  source varchar(64) not null,
  related_path varchar(1024) not null
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists resource_server;
CREATE TABLE `resource_server` (
    `id` VARCHAR(255) PRIMARY KEY,
    `created_by` VARCHAR(255) NULL DEFAULT NULL ,
    `created_date` DATETIME NULL DEFAULT NULL,
    `host` VARCHAR(255) NULL DEFAULT NULL ,
    `is_allocated` INT(11) NULL DEFAULT NULL,
    `login_password` VARCHAR(255) NULL DEFAULT NULL ,
    `login_username` VARCHAR(255) NULL DEFAULT NULL ,
    `name` VARCHAR(255) NULL DEFAULT NULL ,
    `port` VARCHAR(255) NULL DEFAULT NULL ,
    `purpose` VARCHAR(255) NULL DEFAULT NULL ,
    `status` VARCHAR(255) NULL DEFAULT NULL ,
    `type` VARCHAR(255) NULL DEFAULT NULL ,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL ,
    `updated_date` DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists resource_item;
CREATE TABLE `resource_item` (
    `id` VARCHAR(255) PRIMARY KEY,
    `additional_properties` VARCHAR(2048) NULL DEFAULT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_date` DATETIME NULL DEFAULT NULL,
    `is_allocated` INT(11) NULL DEFAULT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    `purpose` VARCHAR(255) NULL DEFAULT NULL,
    `resource_server_id` VARCHAR(64) DEFAULT NULL,
    `status` VARCHAR(255) NULL DEFAULT NULL,
    `type` VARCHAR(255) NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_date` DATETIME NULL DEFAULT NULL,
    INDEX `FK2g8cf9beg7msqry6cmqedvv9n` (`resource_server_id`),
    CONSTRAINT `FK2g8cf9beg7msqry6cmqedvv9n` FOREIGN KEY (`resource_server_id`) REFERENCES `resource_server` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

drop table if exists plugin_instances;
CREATE TABLE `plugin_instances` (
    `id` VARCHAR(255) PRIMARY KEY,
    `host` VARCHAR(255) NULL DEFAULT NULL,
    `container_name` VARCHAR(255) NULL DEFAULT NULL,
    `port` INT(11) NULL DEFAULT NULL,
    `container_status` VARCHAR(255) NULL DEFAULT NULL,
    `package_id` VARCHAR(255) DEFAULT NULL,
    `docker_instance_resource_id` VARCHAR(128) DEFAULT NULL,
    `instance_name` VARCHAR(255) NULL DEFAULT NULL,
    `plugin_mysql_instance_resource_id` VARCHAR(128) DEFAULT NULL,
    `s3bucket_resource_id` VARCHAR(128) DEFAULT NULL,
    INDEX `FKn8124r2uvtipsy1hfkjmd4jts` (`package_id`),
    INDEX `FKbqqlg3wrp1n0h926v5cojcjk7` (`s3bucket_resource_id`),
    CONSTRAINT `FKbqqlg3wrp1n0h926v5cojcjk7` FOREIGN KEY (`s3bucket_resource_id`) REFERENCES `resource_item` (`id`),
    CONSTRAINT `FKn8124r2uvtipsy1hfkjmd4jts` FOREIGN KEY (`package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;



drop table if exists plugin_mysql_instances;
CREATE TABLE `plugin_mysql_instances` (
    `id` VARCHAR(255) PRIMARY KEY,
    `password` VARCHAR(255) NULL DEFAULT NULL,
    `plugun_package_id` VARCHAR(255) DEFAULT NULL,
    `resource_item_id` VARCHAR(255) DEFAULT NULL,
    `schema_name` VARCHAR(255) NULL DEFAULT NULL,
    `status` VARCHAR(255) NULL DEFAULT NULL,
    `username` VARCHAR(255) NULL DEFAULT NULL,
    INDEX `FK6twufg10tr0fk81uyf9tdtxf1` (`plugun_package_id`),
    INDEX `FKn5plb1x3qnwxla4mixdhawo2o` (`resource_item_id`),
    CONSTRAINT `FK6twufg10tr0fk81uyf9tdtxf1` FOREIGN KEY (`plugun_package_id`) REFERENCES `plugin_packages` (`id`),
    CONSTRAINT `FKn5plb1x3qnwxla4mixdhawo2o` FOREIGN KEY (`resource_item_id`) REFERENCES `resource_item` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;

DROP TABLE if EXISTS role_menu;
CREATE TABLE `role_menu` (
    `id`      VARCHAR(255) PRIMARY KEY,
    `role_name` VARCHAR(64) NOT NULL,
    `menu_code` VARCHAR(255) NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

DROP TABLE if EXISTS core_ru_proc_role_binding;
CREATE TABLE `core_ru_proc_role_binding` (
    `id`      VARCHAR(255) PRIMARY KEY,
    `proc_id`      VARCHAR(255) NOT NULL,
    `role_id` VARCHAR(64)       NOT NULL,
    `role_name` VARCHAR(64)     NOT NULL,
    `permission` VARCHAR(255) NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

drop table if exists batch_execution_jobs;
CREATE TABLE `batch_execution_jobs` (
    `id` VARCHAR(255) NOT NULL,
    `create_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `complete_timestamp` TIMESTAMP NULL DEFAULT NULL,
    `creator` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

drop table if exists execution_jobs;
CREATE TABLE `execution_jobs` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `batch_execution_job_id` VARCHAR(255) NOT NULL,
    `package_name` VARCHAR(63) NOT NULL,
    `entity_name` VARCHAR(100) NOT NULL,
    `business_key` VARCHAR(255) NOT NULL,
    `root_entity_id` VARCHAR(255) NOT NULL,
    `execute_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `complete_time` TIMESTAMP NULL,
    `error_code` VARCHAR(1) NULL DEFAULT NULL,
    `error_message` TEXT NULL,
    `return_json` LONGTEXT NULL,
    `plugin_config_interface_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `job_id_and_root_entity_id` (`batch_execution_job_id`, `root_entity_id`),
    CONSTRAINT `FK534bth9hibanrjd5fqdel8u9c` FOREIGN KEY (`batch_execution_job_id`) REFERENCES `batch_execution_jobs` (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists execution_job_parameters;
CREATE TABLE `execution_job_parameters` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `execution_job_id` INT(11) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `data_type` VARCHAR(50) NOT NULL,
    `mapping_type` VARCHAR(50) NULL DEFAULT NULL,
    `mapping_entity_expression` VARCHAR(1024) NULL DEFAULT NULL,
    `mapping_system_variable_name` VARCHAR(500) NULL DEFAULT NULL,
    `required` VARCHAR(5) NULL DEFAULT NULL,
    `constant_value` VARCHAR(255) NULL DEFAULT NULL,
    `value` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK_execution_job_parameters_execution_jobs` (`execution_job_id`),
    CONSTRAINT `FK_execution_job_parameters_execution_jobs` FOREIGN KEY (`execution_job_id`) REFERENCES `execution_jobs` (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites` (
  `favorites_id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `collection_name` varchar(255) NOT NULL,
  `data` blob,
  PRIMARY KEY (`favorites_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `favorites_role`;
CREATE TABLE `favorites_role` (
  `id` varchar(255) NOT NULL,
  `favorites_id` varchar(255) DEFAULT NULL,
  `permission` varchar(255) DEFAULT NULL,
  `role_id` varchar(255) DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
