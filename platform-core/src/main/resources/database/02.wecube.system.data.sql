delete from menu_items;
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('JOBS',null,'JOBS','SYSTEM', 1, '', '任务')
,('DESIGNING',null,'DESIGNING','SYSTEM', 2, '', '设计')
,('IMPLEMENTATION',null,'IMPLEMENTATION','SYSTEM', 3, '', '执行')
,('MONITORING',null,'MONITORING','SYSTEM', 4, '', '监测')
,('ADJUSTMENT',null,'ADJUSTMENT','SYSTEM', 5, '', '调整')
,('INTELLIGENCE_OPS',null,'INTELLIGENCE_OPS','SYSTEM', 6, '', '智慧')
,('COLLABORATION',null,'COLLABORATION','SYSTEM', 7, '', '协同')
,('ADMIN',null,'ADMIN','SYSTEM', 8, '', '系统')
,('IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM', 9, '', '任务编排执行')
,('COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM', 10, '', '插件注册')
,('COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM', 11, '', '任务编排')
,('ADMIN__ADMIN_SYSTEM_PARAMS','ADMIN','ADMIN_SYSTEM_PARAMS','SYSTEM', 12, '', '系统参数')
,('ADMIN__ADMIN_RESOURCES_MANAGEMENT','ADMIN','ADMIN_RESOURCES_MANAGEMENT','SYSTEM', 13, '', '资源管理')
,('ADMIN__ADMIN_USER_ROLE_MANAGEMENT', 'ADMIN', 'ADMIN_USER_ROLE_MANAGEMENT', 'SYSTEM', 14, '', '用户管理')
,('IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION', 'IMPLEMENTATION', 'IMPLEMENTATION_BATCH_EXECUTION', 'SYSTEM', 15, '', '批量执行');

delete from role_menu;
insert into role_menu (id, role_id, role_name, menu_code) values
('admin__JOBS','admin','admin','JOBS'),
('admin__DESIGNING','admin','admin','DESIGNING'),
('admin__IMPLEMENTATION','admin','admin','IMPLEMENTATION'),
('admin__MONITORING','admin','admin','MONITORING'),
('admin__ADJUSTMENT','admin','admin','ADJUSTMENT'),
('admin__INTELLIGENCE_OPS','admin','admin','INTELLIGENCE_OPS'),
('admin__COLLABORATION','admin','admin','COLLABORATION'),
('admin__ADMIN','admin','admin','ADMIN'),
('admin__IMPLEMENTATION_WORKFLOW_EXECUTION','admin','admin','IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION'),
('admin__COLLABORATION_PLUGIN_MANAGEMENT','admin','admin','COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT'),
('admin__COLLABORATION_WORKFLOW_ORCHESTRATION','admin','admin','COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION'),
('admin__ADMIN_SYSTEM_PARAMS','admin','admin','ADMIN__ADMIN_SYSTEM_PARAMS'),
('admin__ADMIN_RESOURCES_MANAGEMENT','admin','admin','ADMIN__ADMIN_RESOURCES_MANAGEMENT'),
('admin__ADMIN_USER_ROLE_MANAGEMENT','admin','admin','ADMIN__ADMIN_USER_ROLE_MANAGEMENT'),
('admin__IMPLEMENTATION_BATCH_EXECUTION','admin','admin','IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION');



INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CORE_ADDR', NULL, 'CORE_ADDR', NULL, 'http://127.0.0.1:19090', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__BASE_MOUNT_PATH', NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CMDB_URL', NULL, 'CMDB_URL', NULL, 'http://127.0.0.1:19090', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('global__CALLBACK_URL', NULL, 'CALLBACK_URL', NULL, '/v1/process/instances/callback', 'global', 'system', 'active');