delete from menu_items;
insert into menu_items (id,parent_id,code,description) values
(1,null,'JOBS','')
,(2,null,'DESIGNING','')
,(3,null,'IMPLEMENTATION','')
,(4,null,'MONITORING','')
,(5,null,'ADJUSTMENT','')
,(6,null,'INTELLIGENCE_OPS','')
,(7,null,'COLLABORATION','')
,(8,null,'ADMIN','')
,(305,3,'IMPLEMENTATION_WORKFLOW_EXECUTION','')
,(701,7,'COLLABORATION_PLUGIN_MANAGEMENT','')
,(702,7,'COLLABORATION_WORKFLOW_ORCHESTRATION','')
,(801,8,'ADMIN_SYSTEM_PARAMS','')
,(802,8,'ADMIN_RESOURCES_MANAGEMENT','');

INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'CORE_ADDR', NULL, 'http://127.0.0.1:19090', 'global', NULL, 0, 'active');
INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', NULL, 0, 'active');
INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'CMDB_URL', NULL, 'http://127.0.0.1:19090', 'global', NULL, 0, 'active');
