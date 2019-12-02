delete from menu_items;
insert into menu_items (id,parent_code,code,source,menu_order,description) values
('JOBS',null,'JOBS','SYSTEM', 1, '')
,('DESIGNING',null,'DESIGNING','SYSTEM', 2, '')
,('IMPLEMENTATION',null,'IMPLEMENTATION','SYSTEM', 3, '')
,('MONITORING',null,'MONITORING','SYSTEM', 4, '')
,('ADJUSTMENT',null,'ADJUSTMENT','SYSTEM', 5, '')
,('INTELLIGENCE_OPS',null,'INTELLIGENCE_OPS','SYSTEM', 6, '')
,('COLLABORATION',null,'COLLABORATION','SYSTEM', 7, '')
,('ADMIN',null,'ADMIN','SYSTEM', 8, '')
,('IMPLEMENTATION:IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM', 9, '')
,('COLLABORATION:COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM', 10, '')
,('COLLABORATION:COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM', 11, '')
,('ADMIN:ADMIN_SYSTEM_PARAMS','ADMIN','ADMIN_SYSTEM_PARAMS','SYSTEM', 12, '')
,('ADMIN:ADMIN_RESOURCES_MANAGEMENT','ADMIN','ADMIN_RESOURCES_MANAGEMENT','SYSTEM', 13, '');


INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'CORE_ADDR', NULL, 'http://127.0.0.1:19090', 'global', NULL, 0, 'active');
INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', NULL, 0, 'active');
INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'CMDB_URL', NULL, 'http://127.0.0.1:19090', 'global', NULL, 0, 'active');
INSERT INTO `system_variables` (`plugin_package_id`, `name`, `value`, `default_value`, `scope_type`, `scope_value`, `seq_no`, `status`) VALUES (NULL, 'CALLBACK_URL', NULL, 'http://127.0.0.1:19090/workflow/callback', 'global', NULL, 0, 'active');

