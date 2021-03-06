SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `ref_package` VARCHAR(45) COLLATE utf8_bin NULL,
ADD COLUMN `ref_entity` VARCHAR(45) COLLATE utf8_bin NULL,
ADD COLUMN `ref_attr` VARCHAR(45) COLLATE utf8_bin NULL;

ALTER TABLE `core_ru_proc_exec_binding` 
ADD COLUMN `bind_flag` char(1) COLLATE utf8_bin DEFAULT 'Y';

ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `mandatory` BIT(1) NULL DEFAULT 0;

ALTER TABLE `core_re_task_node_def_info` 
ADD COLUMN `dynamic_bind` VARCHAR(45) COLLATE utf8_bin NULL DEFAULT 'N';

ALTER TABLE `core_re_proc_def_info` 
ADD COLUMN `exclude_mode` VARCHAR(10) COLLATE utf8_bin NULL DEFAULT 'N';

ALTER TABLE `core_re_task_node_def_info` 
ADD COLUMN `pre_check` VARCHAR(45) COLLATE utf8_bin NULL DEFAULT 'N';

ALTER TABLE `core_operation_event`
ADD COLUMN `rev` INT(11) DEFAULT 0;
ALTER TABLE `core_operation_event` 
ADD COLUMN `oper_mode` VARCHAR(45) COLLATE utf8_bin NULL DEFAULT 'defer';

CREATE TABLE IF NOT EXISTS `core_object_meta` (
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

CREATE TABLE IF NOT EXISTS `core_object_property_meta` (
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

CREATE TABLE IF NOT EXISTS `core_object_var` (
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

CREATE TABLE IF NOT EXISTS `core_object_property_var` (
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

CREATE TABLE IF NOT EXISTS `core_object_list_var` (
  `id` varchar(20) COLLATE utf8_bin NOT NULL,
  `data_type` varchar(45) COLLATE utf8_bin NOT NULL,
  `data_value` varchar(1000) COLLATE utf8_bin NOT NULL,
  `created_by` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `is_sensitive` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `core_ru_task_node_inst_info` 
ADD COLUMN `pre_check_ret` VARCHAR(45) COLLATE utf8_bin NULL;

ALTER TABLE `core_ru_task_node_exec_req` 
CHANGE COLUMN `err_msg` `err_msg` MEDIUMTEXT COLLATE utf8_bin NULL DEFAULT NULL ;

alter table plugin_packages convert to character set utf8 collate utf8_bin;
alter table plugin_package_dependencies convert to character set utf8 collate utf8_bin;
alter table plugin_package_menus convert to character set utf8 collate utf8_bin;
alter table plugin_package_data_model convert to character set utf8 collate utf8_bin;
alter table plugin_package_entities convert to character set utf8 collate utf8_bin;
alter table plugin_package_attributes convert to character set utf8 collate utf8_bin;
alter table system_variables convert to character set utf8 collate utf8_bin;
alter table plugin_package_authorities convert to character set utf8 collate utf8_bin;
alter table plugin_package_runtime_resources_docker convert to character set utf8 collate utf8_bin;
alter table plugin_package_runtime_resources_mysql convert to character set utf8 collate utf8_bin;
alter table plugin_package_runtime_resources_s3 convert to character set utf8 collate utf8_bin;
alter table plugin_configs convert to character set utf8 collate utf8_bin;
alter table plugin_config_interfaces convert to character set utf8 collate utf8_bin;
alter table plugin_config_interface_parameters convert to character set utf8 collate utf8_bin;
alter table menu_items convert to character set utf8 collate utf8_bin;
alter table plugin_package_resource_files convert to character set utf8 collate utf8_bin;
alter table resource_server convert to character set utf8 collate utf8_bin;
alter table resource_item convert to character set utf8 collate utf8_bin;
alter table plugin_instances convert to character set utf8 collate utf8_bin;
alter table plugin_mysql_instances convert to character set utf8 collate utf8_bin;
alter table role_menu convert to character set utf8 collate utf8_bin;
alter table batch_execution_jobs convert to character set utf8 collate utf8_bin;
alter table execution_jobs convert to character set utf8 collate utf8_bin;
alter table execution_job_parameters convert to character set utf8 collate utf8_bin;
alter table favorites convert to character set utf8 collate utf8_bin;
alter table favorites_role convert to character set utf8 collate utf8_bin;
alter table plugin_artifact_pull_req convert to character set utf8 collate utf8_bin;
alter table act_ru_procinst_status convert to character set utf8 collate utf8_bin;
alter table act_ru_srvnode_status convert to character set utf8 collate utf8_bin;
alter table core_operation_event convert to character set utf8 collate utf8_bin;
alter table core_re_proc_def_info convert to character set utf8 collate utf8_bin;
alter table core_re_task_node_def_info convert to character set utf8 collate utf8_bin;
alter table core_re_task_node_param convert to character set utf8 collate utf8_bin;
alter table core_ru_graph_node convert to character set utf8 collate utf8_bin;
alter table core_ru_proc_exec_binding convert to character set utf8 collate utf8_bin;
alter table core_ru_proc_exec_binding_tmp convert to character set utf8 collate utf8_bin;
alter table core_ru_proc_inst_info convert to character set utf8 collate utf8_bin;
alter table core_ru_proc_role_binding convert to character set utf8 collate utf8_bin;
alter table core_ru_task_node_exec_param convert to character set utf8 collate utf8_bin;
alter table core_ru_task_node_exec_req convert to character set utf8 collate utf8_bin;
alter table core_ru_task_node_inst_info convert to character set utf8 collate utf8_bin;
alter table plugin_config_roles convert to character set utf8 collate utf8_bin;
alter table core_object_meta convert to character set utf8 collate utf8_bin;
alter table core_object_property_meta convert to character set utf8 collate utf8_bin;
alter table core_object_var convert to character set utf8 collate utf8_bin;
alter table core_object_property_var convert to character set utf8 collate utf8_bin;
alter table core_object_list_var convert to character set utf8 collate utf8_bin;

CREATE TABLE IF NOT EXISTS  `core_extra_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_seq_no` VARCHAR(60) COLLATE utf8_bin NOT NULL,
  `task_type` VARCHAR(45) COLLATE utf8_bin NOT NULL,
  `rev` int(11) NOT NULL,
  `status` VARCHAR(45) COLLATE utf8_bin  DEFAULT NULL,
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `created_by` VARCHAR(45) COLLATE utf8_bin DEFAULT NULL,
  `created_time` DATETIME DEFAULT NULL,
  `updated_by` VARCHAR(45) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` DATETIME DEFAULT NULL,
  `task_def` VARCHAR(2000) COLLATE utf8_bin DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE core_ru_task_node_inst_info 
ADD COLUMN bind_status VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL;

ALTER TABLE core_ru_task_node_exec_param 
ADD INDEX idx_core_t_n_e_param_comp (req_id, param_type, param_name);

ALTER TABLE core_ru_task_node_exec_req 
ADD INDEX idx_core_t_n_e_req_n_inst (node_inst_id);

ALTER TABLE act_ru_srvnode_status 
ADD INDEX idx_act_snode_status_insti_n (proc_inst_id,node_id);

ALTER TABLE act_ru_srvnode_status 
ADD INDEX idx_act_snode_status_instk_n (proc_inst_key,node_id);

ALTER TABLE act_ru_procinst_status 
ADD INDEX idx_act_procinst_status_inst_id (proc_inst_id);

ALTER TABLE core_ru_task_node_inst_info 
ADD INDEX idx_core_t_n_i_inst_node (proc_inst_id, node_id);

ALTER TABLE core_ru_proc_exec_binding 
ADD INDEX idx_core_p_e_binding_p_bt_n (proc_inst_id, bind_type, task_node_inst_id);

ALTER TABLE core_ru_proc_exec_binding 
ADD INDEX idx_core_p_e_binding_e_d_id (entity_data_id);

ALTER TABLE core_ru_proc_exec_binding_tmp 
ADD INDEX idx_core_p_e_binding_tmp_p_s_id (proc_session_id);

ALTER TABLE core_ru_graph_node 
ADD INDEX idx_core_g_node_p_s_id (proc_sess_id);

ALTER TABLE core_ru_graph_node 
ADD INDEX idx_core_g_node_p_i_id (proc_inst_id);

ALTER TABLE core_ru_proc_inst_info 
ADD INDEX idx_core_p_inst_p_kernel_id (proc_inst_kernel_id);

ALTER TABLE core_operation_event 
ADD INDEX idx_core_o_event_p_i_key (proc_inst_key);

ALTER TABLE plugin_config_interfaces 
ADD INDEX idx_core_plugin_c_intf_c_id (plugin_config_id);

ALTER TABLE plugin_config_interface_parameters 
ADD INDEX idx_core_plugin_c_intf_param_intf_id (plugin_config_interface_id);

ALTER TABLE plugin_package_attributes 
ADD INDEX idx_core_plugin_p_attr_e_id (entity_id);

SET FOREIGN_KEY_CHECKS = 1;
