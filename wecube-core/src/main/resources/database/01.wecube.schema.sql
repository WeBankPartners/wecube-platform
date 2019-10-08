drop table if exists blob_data;
create table blob_data (
  id int auto_increment primary key,
  type varchar(100) not null,
  name varchar(100)  not null,
  content longblob default null
);

drop table if exists menu_items;
create table menu_items (
  id int auto_increment primary key,
  parent_id int,
  code varchar(50) not null,
  description varchar(200) ,
  unique key uk_code (code)
);

drop table if exists role_menu;
create table role_menu (
  id int auto_increment primary key,
  role_id int not null,
  menu_id int not null,
  unique key uk_roleid_menuid (role_id, menu_id)
);

drop table if exists plugin_packages;
create table plugin_packages (
  id int auto_increment primary key,
  name varchar(100) not null,
  version varchar(100) not null,
  docker_image_file varchar(100),
  docker_image_repository varchar(100),
  docker_image_tag varchar(100),
  container_port varchar(10),
  container_config_directory varchar(100),
  container_log_directory varchar(100),
  container_data_directory varchar(100),
  container_start_param varchar(1024),
  unique key(name, version)
);

drop table if exists plugin_configs;
create table plugin_configs (
  id int auto_increment primary key,
  package_id int not null,
  name varchar(100) not null,
  cmdb_ci_type_id int,
  status varchar(50) not null
);

drop table if exists plugin_cfg_filter_rules;
create table plugin_cfg_filter_rules (
  id int auto_increment primary key,
  config_id int not null,
  cmdb_attribute_id int,
  cmdb_column_name varchar(500),
  filtering_values varchar(500)
);

drop table if exists plugin_cfg_interfaces;
create table plugin_cfg_interfaces (
  id int auto_increment primary key,
  config_id int not null,
  name varchar(100) not null,
  service_name varchar(500) not null,
  service_display_name varchar(500) not null,
  path varchar(500) not null,
  filter_status varchar(500),
  result_status varchar(500),
  cmdb_query_template_id int
);

drop table if exists plugin_cfg_inf_parameters;
create table plugin_cfg_inf_parameters (
  id int auto_increment primary key,
  interface_id int not null,
  type varchar(50) not null,
  name varchar(255) not null,
  datatype varchar(50) default null,
  mapping_type varchar(50),
  cmdb_column_name varchar(500),
  cmdb_column_source varchar(2000),
  cmdb_citype_id int,
  cmdb_attribute_id int,
  cmdb_citype_path varchar(500),
  cmdb_enum_code int
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

drop table if exists batch_job_host;
drop table if exists batch_job;
create table batch_job(
 id int auto_increment primary key,
 script_url  VARCHAR(500) NULL DEFAULT NULL,
 create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
 creator VARCHAR(50) NULL DEFAULT NULL
);
create table batch_job_host(
 id int auto_increment primary key,
 batch_job_id int NOT NULL,
 host_ip  VARCHAR(20) NULL DEFAULT NULL,
 output_url   VARCHAR(500) NULL DEFAULT NULL,
 create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
 status  VARCHAR(50) NULL DEFAULT NULL,
 result  VARCHAR(10) NULL DEFAULT NULL,
 update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY (batch_job_id) REFERENCES batch_job(id)
);

DROP TABLE IF EXISTS `core_ru_task_node_exec_log`;
CREATE TABLE `core_ru_task_node_exec_log` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `INST_ID` varchar(60) DEFAULT NULL,
  `INST_KEY` varchar(60) DEFAULT NULL,
  `EXEC_ID` varchar(60) DEFAULT NULL,
  `NODE_ID` varchar(60) DEFAULT NULL,
  `SERV_NAME` varchar(255) DEFAULT NULL,
  `ROOT_CI_TYPE` int(11) DEFAULT NULL,
  `NODE_STATUS` varchar(30) DEFAULT NULL,
  `ERR_CODE` varchar(10) DEFAULT NULL,
  `ERR_MSG` varchar(255) DEFAULT NULL,
  `PRE_STATUS` varchar(60) DEFAULT NULL,
  `POST_STATUS` varchar(60) DEFAULT NULL,
  `CREATED_BY` varchar(60) DEFAULT NULL,
  `CREATED_TIME` datetime DEFAULT NULL,
  `UPDATED_BY` varchar(60) DEFAULT NULL,
  `UPDATED_TIME` datetime DEFAULT NULL,
  `REQ_DATA` text,
  `RESP_DATA` text,
  `REQ_URL` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

DROP TABLE IF EXISTS `core_ru_task_node_exec_var`;
CREATE TABLE `core_ru_task_node_exec_var` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CI_TYPE_ID` int(11) DEFAULT NULL,
  `CI_GUID` varchar(50) DEFAULT NULL,
  `CONFIRMED` char(1) DEFAULT NULL,
  `EXEC_LOG_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_EXEC_LOG_ID` (`EXEC_LOG_ID`),
  CONSTRAINT `FK_EXEC_LOG_ID` FOREIGN KEY (`EXEC_LOG_ID`) REFERENCES `core_ru_task_node_exec_log` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);


DROP TABLE IF EXISTS `core_re_proc_def`;
CREATE TABLE `core_re_proc_def` (
  `ID` varchar(30) NOT NULL,
  `PROC_DEF_KEY` varchar(255) DEFAULT NULL,
  `PROC_NAME` varchar(255) DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `BIND_CITYPE_ID` int(11) DEFAULT NULL,
  `ACTIVE` int(1) NOT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `CREATE_BY` varchar(255) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `UPDATE_BY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

DROP TABLE IF EXISTS `core_re_proc_task_service`;
CREATE TABLE `core_re_proc_task_service` (
  `ID` varchar(30) NOT NULL,
  `PROC_DEF_KEY` varchar(255) DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `PROC_DEF_ID` varchar(255) DEFAULT NULL,
  `TASK_NODE_ID` varchar(255) DEFAULT NULL,
  `TASK_NODE_NAME` varchar(255) DEFAULT NULL,
  `BIND_SERVICE_ID` varchar(255) DEFAULT NULL,
  `BIND_SERVICE_NAME` varchar(255) DEFAULT NULL,
  `BIND_CI_ROUTINE_EXP` varchar(800) DEFAULT NULL,
  `BIND_CI_ROUTINE_RAW` text,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ACTIVE` int(1) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `CREATE_BY` varchar(255) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `UPDATE_BY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

DROP TABLE IF EXISTS `core_ru_process_transaction`;
CREATE TABLE `core_ru_process_transaction` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) DEFAULT NULL,
  `ALIAS_NAME` varchar(255) DEFAULT NULL,
  `OPERATOR` varchar(255) DEFAULT NULL,
  `OPERATOR_GROUP` varchar(255) DEFAULT NULL,
  `STATUS` varchar(255) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `CREATE_BY` varchar(255) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `UPDATE_BY` varchar(255) DEFAULT NULL,
  `ATTACH` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

DROP TABLE IF EXISTS `core_ru_process_task`;
CREATE TABLE `core_ru_process_task` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `OPERATOR` varchar(255) DEFAULT NULL,
  `OPERATOR_GROUP` varchar(255) DEFAULT NULL,
  `DEF_ID` varchar(255) DEFAULT NULL,
  `DEF_KEY` varchar(255) DEFAULT NULL,
  `DEF_VER` int(11) DEFAULT NULL,
  `INST_ID` varchar(255) DEFAULT NULL,
  `INST_KEY` varchar(255) DEFAULT NULL,
  `CI_TYPE_ID` int(11) DEFAULT NULL,
  `CI_DATA_ID` varchar(255) DEFAULT NULL,
  `STATUS` varchar(255) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `CREATE_BY` varchar(255) DEFAULT NULL,
  `UPDATE_BY` varchar(255) DEFAULT NULL,
  `TRANSACTION_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_TRANS` (`TRANSACTION_ID`),
  CONSTRAINT `FK_TRANS` FOREIGN KEY (`TRANSACTION_ID`) REFERENCES `core_ru_process_transaction` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
 `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
 `operator` VARCHAR(64) NULL DEFAULT NULL,
 `operate_time` VARCHAR(64) NULL DEFAULT NULL,
 `category` VARCHAR(50) NULL DEFAULT NULL,
 `operation` VARCHAR(50) NULL DEFAULT NULL,
 `content` LONGTEXT NULL,
 `result` VARCHAR(20) NULL DEFAULT NULL,
 `result_message` LONGTEXT NULL,
 PRIMARY KEY (`id`),
 INDEX `idx_operator` (`operator`),
 INDEX `idx_operate_time` (`operate_time`),
 INDEX `idx_category` (`category`),
 INDEX `idx_operation` (`operation`),
 INDEX `idx_result` (`result`)
);

DROP TABLE IF EXISTS `resource_server`;
CREATE TABLE `resource_server` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`host` VARCHAR(50) NULL DEFAULT NULL,
	`port` VARCHAR(10) NULL DEFAULT NULL,
	`login_username` VARCHAR(50) NULL DEFAULT NULL,
	`login_password` VARCHAR(100) NULL DEFAULT NULL,
	`type` VARCHAR(20) NULL DEFAULT NULL,
	`is_allocated` INT(1) NULL DEFAULT 0,
	`purpose` VARCHAR(255) NULL DEFAULT NULL,
	`status` VARCHAR(20) NULL DEFAULT NULL,
	`created_by` VARCHAR(50) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`updated_by` VARCHAR(50) NULL DEFAULT NULL,
	`updated_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `uk_resource_server` (`name`)
);

DROP TABLE IF EXISTS `resource_item`;
CREATE TABLE `resource_item` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`type` VARCHAR(20) NULL DEFAULT NULL,
	`additional_properties` LONGTEXT NULL,
	`resource_server_id` INT(11) NULL DEFAULT NULL,
	`is_allocated` INT(1) NULL DEFAULT 0,
	`purpose` VARCHAR(255) NULL DEFAULT NULL,
	`status` VARCHAR(20) NULL DEFAULT NULL,
	`created_by` VARCHAR(50) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`updated_by` VARCHAR(50) NULL DEFAULT NULL,
	`updated_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `uk_name_resource_server_id` (`name`, `resource_server_id`),
	INDEX `fk_resource_server_id` (`resource_server_id`),
	CONSTRAINT `fk_resource_server_id` FOREIGN KEY (`resource_server_id`) REFERENCES `resource_server` (`id`)
);

DROP TABLE IF EXISTS plugin_model_entity;
CREATE TABLE plugin_model_entity
(
    id           INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    package_id   INTEGER                        NOT NULL,
    name         VARCHAR(100)                   NOT NULL,
    display_name VARCHAR(100)                   NOT NULL,
    description  VARCHAR(256)                   NOT NULL,
    state        VARCHAR(50) DEFAULT 'draft'    NOT NULL,
    CONSTRAINT fk_package_id FOREIGN KEY (package_id) REFERENCES plugin_packages (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_package_entity (package_id, name)

);

DROP TABLE IF EXISTS plugin_model_attribute;
CREATE TABLE plugin_model_attribute
(
    id           INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    entity_id    INTEGER                        NOT NULL,
    reference_id INTEGER,
    name         VARCHAR(100)                   NOT NULL,
    description  VARCHAR(256)                   NOT NULL,
    data_type    VARCHAR(20),
    state        VARCHAR(50)                    NOT NULL DEFAULT 'draft',
    CONSTRAINT fk_entity_id FOREIGN KEY (entity_id) REFERENCES plugin_model_entity (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reference_id FOREIGN KEY (reference_id) REFERENCES plugin_model_attribute (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE uk_entity_attribute (entity_id, name)
);