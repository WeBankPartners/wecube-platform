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
,('IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM', 9, '')
,('COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM', 10, '')
,('COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM', 11, '')
,('ADMIN__ADMIN_SYSTEM_PARAMS','ADMIN','ADMIN_SYSTEM_PARAMS','SYSTEM', 12, '')
,('ADMIN__ADMIN_RESOURCES_MANAGEMENT','ADMIN','ADMIN_RESOURCES_MANAGEMENT','SYSTEM', 13, '')
,('ADMIN__ADMIN_USER_ROLE_MANAGEMENT', 'ADMIN', 'ADMIN_USER_ROLE_MANAGEMENT', 'SYSTEM', 14, '')
,('IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION', 'IMPLEMENTATION', 'IMPLEMENTATION_BATCH_EXECUTION', 'SYSTEM', 15, '');



INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CORE_ADDR', NULL, 'CORE_ADDR', NULL, 'http://127.0.0.1:19090', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__BASE_MOUNT_PATH', NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CMDB_URL', NULL, 'CMDB_URL', NULL, 'http://127.0.0.1:19090', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CALLBACK_URL', NULL, 'CALLBACK_URL', NULL, '/v1/process/instances/callback', 'global', 'system', 'active');