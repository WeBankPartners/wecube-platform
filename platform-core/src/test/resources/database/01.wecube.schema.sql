
drop table if exists plugin_packages;
CREATE TABLE `plugin_packages` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL,
	`version` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) NOT NULL default 'UNREGISTERED',
    `upload_timestamp` timestamp default current_timestamp,
    `ui_package_included` TINYINT(1) default 0,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `name` (`name`, `version`)
);


drop table if exists plugin_package_dependencies;
create table plugin_package_dependencies (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  dependency_package_name VARCHAR(50) not null,
  dependency_package_version varchar(20) not null,
  PRIMARY KEY (`id`)
);

drop table if exists plugin_package_menus;
create table plugin_package_menus (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  code varchar(64) not null,
  category varchar(64) not null,
  display_name VARCHAR(256) not null,
  path VARCHAR(256) not null,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS plugin_package_data_model;
CREATE TABLE plugin_package_data_model
(
    id                  INT AUTO_INCREMENT PRIMARY KEY          NOT NULL,
    version             INTEGER                        NOT NULL DEFAULT 1,
    plugin_package_id   INTEGER                                 NOT NULL,
    package_name        VARCHAR(50)                             NOT NULL,
    package_version     VARCHAR(20)                             NOT NULL,
    is_dynamic          TINYINT(1)  default 0,
    update_path         VARCHAR(256),
    update_method       VARCHAR(10),
    update_timestamp    timestamp   default current_timestamp   NOT NULL,
    CONSTRAINT fk_plugin_package_id FOREIGN KEY (plugin_package_id) REFERENCES plugin_package(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_plugin_package_data_model(plugin_package_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS plugin_package_entities;
CREATE TABLE plugin_package_entities
(
    id                 INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    plugin_package_data_model_id  INTEGER                        NOT NULL,
    name               VARCHAR(100)                   NOT NULL,
    display_name       VARCHAR(100)                   NOT NULL,
    description        VARCHAR(256)                   NOT NULL,
    CONSTRAINT fk_package_data_model_id FOREIGN KEY (plugin_package_data_model_id) REFERENCES plugin_package_data_model(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_package_entity(plugin_package_data_model_id, name)

);

DROP TABLE IF EXISTS plugin_package_attributes;
CREATE TABLE plugin_package_attributes
(
    id           INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    entity_id    INTEGER                        NOT NULL,
    reference_id INTEGER,
    name         VARCHAR(100)                   NOT NULL,
    description  VARCHAR(256)                   NOT NULL,
    data_type    VARCHAR(20)                    NOT NULL,
    CONSTRAINT fk_entity_id FOREIGN KEY (entity_id) REFERENCES plugin_package_entities (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reference_id FOREIGN KEY (reference_id) REFERENCES plugin_package_attributes (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_entity_attribute (entity_id, name)
);

drop table if exists system_variables;
create table system_variables (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  name varchar(255) not null,
  value varchar(2000),
  default_value varchar(2000),
  scope_type varchar(50) not null default 'global',
  scope_value varchar(500),
  seq_no int not null default 0,
  status varchar(50) not null default 'active',
  PRIMARY KEY (`id`),
  index idx_prop_scope_val (plugin_package_id)
);

drop table if exists plugin_package_authorities;
create table plugin_package_authorities (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  role_name varchar(64) not null,
  menu_code varchar(64) not null,
  PRIMARY KEY (`id`)
);


drop table if exists plugin_package_runtime_resources_docker;
create table plugin_package_runtime_resources_docker (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  image_name varchar(256) not null, 
  container_name varchar(128) not null,
  port_bindings varchar(64) not null, 
  volume_bindings varchar(1024) not null,
  env_variables varchar(2000),
  PRIMARY KEY (`id`)
);

drop table if exists plugin_package_runtime_resources_mysql;
create table plugin_package_runtime_resources_mysql (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  schema_name varchar(128) not null,
  init_file_name varchar(256),
  upgrade_file_name varchar(256),
  PRIMARY KEY (`id`)
);

drop table if exists plugin_package_runtime_resources_s3;
create table plugin_package_runtime_resources_s3 (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  bucket_name varchar(63) not null,
  PRIMARY KEY (`id`)
);


drop table if exists plugin_configs;
CREATE TABLE `plugin_configs` (
  id int auto_increment primary key,
  `plugin_package_id` INT(11) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `entity_id` INT(11) NULL DEFAULT NULL,
  `entity_name` VARCHAR(100) NOT NULL,
  `status` VARCHAR(20) NOT NULL default 'DISABLED',
  PRIMARY KEY (`id`)
);

drop table if exists plugin_config_interfaces;
create table plugin_config_interfaces (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`plugin_config_id` INT(11) NOT NULL,
	`action` VARCHAR(100) NOT NULL,
	`service_name` VARCHAR(500) NOT NULL, 
	`service_display_name` VARCHAR(500) NOT NULL,
	`path` VARCHAR(500) NOT NULL, 
	`http_method` VARCHAR(10) NOT NULL,
    PRIMARY KEY (`id`)
);

drop table if exists plugin_config_interface_parameters;
CREATE TABLE `plugin_config_interface_parameters` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`plugin_config_interface_id` INT(11) NOT NULL,
	`type` VARCHAR(50) NOT NULL,
	`name` VARCHAR(255) NOT NULL,
	`data_type` VARCHAR(50) NOT NULL,
	`mapping_type` VARCHAR(50) NULL DEFAULT NULL,
	`mapping_entity_expression` varchar(1024) NULL DEFAULT NULL,
	`mapping_system_variable_id` VARCHAR(500) NULL DEFAULT NULL,
	`required` varchar(5),
	PRIMARY KEY (`id`)
);

drop table if exists plugin_instances;
create table plugin_instances (
  id int auto_increment primary key,
  instance_container_id varchar(64) not null,
  package_id int,
  host varchar(50) ,
  port int ,
  status varchar(50) not null,
  unique key (host,port)
);

drop table if exists menu_items;
create table menu_items
(
    id          int auto_increment primary key,
    parent_id   int,
    code        varchar(50) not null,
    description varchar(200),
    unique key uk_code (code)
);

drop table if exists role_menu;
create table role_menu
(
    id      int auto_increment primary key,
    role_id int not null,
    menu_id int not null,
    unique key uk_roleid_menuid (role_id, menu_id)
);

drop table if exists plugin_package_resource_files;
create table plugin_package_resource_files
(
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  package_name varchar(50) not null,
  package_version varchar(20) not null,
  source varchar(64) not null,
  related_path varchar(1024) not null,
  PRIMARY KEY (`id`)
);

drop table if exists plugin_datamodel_table;
create table plugin_datamodel_table
(
    `id`      int auto_increment primary key,
	`name`    VARCHAR(50) NOT NULL,
	`value`   double,
	`date`    date,
	`time`    timestamp,
	PRIMARY KEY (`id`)
);