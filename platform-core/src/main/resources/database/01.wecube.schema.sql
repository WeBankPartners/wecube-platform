
drop table if exists plugin_packages;
CREATE TABLE `plugin_packages` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL,
	`version` VARCHAR(20) NOT NULL,
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

DROP TABLE IF EXISTS plugin_package_entities;
CREATE TABLE plugin_package_entities
(
    id                INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    plugin_package_id INTEGER                        NOT NULL,
    name              VARCHAR(100)                   NOT NULL,
    display_name      VARCHAR(100)                   NOT NULL,
    description       VARCHAR(256)                   NOT NULL,
    CONSTRAINT fk_package_id FOREIGN KEY (plugin_package_id) REFERENCES plugin_packages (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_package_entity (plugin_package_id, name)

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
  schema varchar(128) not null,
  init_file_name varchar(256),
  upgrade_file_name varchar(256),
  PRIMARY KEY (`id`)
);

drop table if exists plugin_package_runtime_resources_s3;
create table plugin_package_runtime_resources_s3 (
  id int auto_increment primary key,
  plugin_package_id int(11) not null,
  bucket_name varchar(32) not null,
  PRIMARY KEY (`id`)
);


drop table if exists plugin_configs;
CREATE TABLE `plugin_configs` (
  id int auto_increment primary key,
  `plugin_package_id` INT(11) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `entity_id` INT(11) NULL DEFAULT NULL,
  `status` VARCHAR(50) NOT NULL,
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