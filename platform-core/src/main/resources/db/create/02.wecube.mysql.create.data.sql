SET FOREIGN_KEY_CHECKS = 0;
delete from menu_items where id = 'JOBS';
delete from menu_items where id = 'DESIGNING';
delete from menu_items where id = 'IMPLEMENTATION';
delete from menu_items where id = 'MONITORING';
delete from menu_items where id = 'ADJUSTMENT';
delete from menu_items where id = 'INTELLIGENCE_OPS';
delete from menu_items where id = 'COLLABORATION';
delete from menu_items where id = 'ADMIN';
delete from menu_items where id = 'IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION';
delete from menu_items where id = 'COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT';
delete from menu_items where id = 'COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION';
delete from menu_items where id = 'ADMIN__ADMIN_SYSTEM_PARAMS';
delete from menu_items where id = 'ADMIN__ADMIN_RESOURCES_MANAGEMENT';
delete from menu_items where id = 'ADMIN__ADMIN_USER_ROLE_MANAGEMENT';
delete from menu_items where id = 'IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION';

insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('JOBS',null,'JOBS','SYSTEM', 1, '', '任务');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('DESIGNING',null,'DESIGNING','SYSTEM', 2, '', '设计');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('IMPLEMENTATION',null,'IMPLEMENTATION','SYSTEM', 3, '', '执行');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('MONITORING',null,'MONITORING','SYSTEM', 4, '', '监测');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('ADJUSTMENT',null,'ADJUSTMENT','SYSTEM', 6, '', '调整');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('INTELLIGENCE_OPS',null,'INTELLIGENCE_OPS','SYSTEM', 5, '', '智慧');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('COLLABORATION',null,'COLLABORATION','SYSTEM', 7, '', '协同');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('ADMIN',null,'ADMIN','SYSTEM', 8, '', '系统');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM', 9, '', '任务编排执行');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM', 10, '', '插件注册');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM', 11, '', '任务编排');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('ADMIN__ADMIN_SYSTEM_PARAMS','ADMIN','ADMIN_SYSTEM_PARAMS','SYSTEM', 12, '', '系统参数');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('ADMIN__ADMIN_RESOURCES_MANAGEMENT','ADMIN','ADMIN_RESOURCES_MANAGEMENT','SYSTEM', 13, '', '资源管理');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('ADMIN__ADMIN_USER_ROLE_MANAGEMENT', 'ADMIN', 'ADMIN_USER_ROLE_MANAGEMENT', 'SYSTEM', 14, '', '用户管理');
insert into menu_items (id,parent_code,code,source,menu_order,description,local_display_name) values
('IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION', 'IMPLEMENTATION', 'IMPLEMENTATION_BATCH_EXECUTION', 'SYSTEM', 15, '', '批量执行');

delete from role_menu where id = 'SUPER_ADMIN__IMPLEMENTATION_WORKFLOW_EXECUTION';
delete from role_menu where id = 'SUPER_ADMIN__COLLABORATION_PLUGIN_MANAGEMENT';
delete from role_menu where id = 'SUPER_ADMIN__COLLABORATION_WORKFLOW_ORCHESTRATION';
delete from role_menu where id = 'SUPER_ADMIN__ADMIN_SYSTEM_PARAMS';
delete from role_menu where id = 'SUPER_ADMIN__ADMIN_RESOURCES_MANAGEMENT';
delete from role_menu where id = 'SUPER_ADMIN__ADMIN_USER_ROLE_MANAGEMENT';
delete from role_menu where id = 'SUPER_ADMIN__IMPLEMENTATION_BATCH_EXECUTION';

insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__IMPLEMENTATION_WORKFLOW_EXECUTION','SUPER_ADMIN','IMPLEMENTATION_WORKFLOW_EXECUTION');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__COLLABORATION_PLUGIN_MANAGEMENT','SUPER_ADMIN','COLLABORATION_PLUGIN_MANAGEMENT');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__COLLABORATION_WORKFLOW_ORCHESTRATION','SUPER_ADMIN','COLLABORATION_WORKFLOW_ORCHESTRATION');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__ADMIN_SYSTEM_PARAMS','SUPER_ADMIN','ADMIN_SYSTEM_PARAMS');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__ADMIN_RESOURCES_MANAGEMENT','SUPER_ADMIN','ADMIN_RESOURCES_MANAGEMENT');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__ADMIN_USER_ROLE_MANAGEMENT','SUPER_ADMIN','ADMIN_USER_ROLE_MANAGEMENT');
insert into role_menu (id, role_name, menu_code) values
('SUPER_ADMIN__IMPLEMENTATION_BATCH_EXECUTION','SUPER_ADMIN','IMPLEMENTATION_BATCH_EXECUTION');


delete from system_variables where id = 'system__global__GATEWAY_URL';
delete from system_variables where id = 'system__global__BASE_MOUNT_PATH';
delete from system_variables where id = 'system__global__CALLBACK_URL';
delete from system_variables where id = 'system__global__S3_ACCESS_KEY';
delete from system_variables where id = 'system__global__S3_SECRET_KEY';
delete from system_variables where id = 'system__global__S3_SERVER_URL';
delete from system_variables where id = 'system__global__ENCRYPT_SEED';
delete from system_variables where id = 'system__global__PLUGIN_ARTIFACTS_RELEASE_URL';

delete from system_variables where id = 'system__global__HTTP_PROXY';
delete from system_variables where id = 'system__global__HTTPS_PROXY';

INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__GATEWAY_URL', NULL, 'GATEWAY_URL', NULL, 'http://127.0.0.1:19110', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__BASE_MOUNT_PATH', NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__CALLBACK_URL', NULL, 'CALLBACK_URL', NULL, '/platform/v1/process/instances/callback', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__S3_ACCESS_KEY', NULL, 'S3_ACCESS_KEY', '', 'access_key', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__S3_SECRET_KEY', NULL, 'S3_SECRET_KEY', '', 'secret_key', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__S3_SERVER_URL', NULL, 'S3_SERVER_URL', '', 'localhost:20000', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__ENCRYPT_SEED', NULL, 'ENCRYPT_SEED', NULL, 'seed-wecube2.1-2020', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__PLUGIN_ARTIFACTS_RELEASE_URL', NULL, 'PLUGIN_ARTIFACTS_RELEASE_URL', NULL, 'https://wecube-1259801214.cos.ap-guangzhou.myqcloud.com/plugins-v2/public-plugin-artifacts.release', 'global', 'system', 'active');

INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__HTTP_PROXY', NULL, 'HTTP_PROXY', NULL, '', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__HTTPS_PROXY', NULL, 'HTTPS_PROXY', NULL, '', 'global', 'system', 'active');

#workflow
delete from `act_ge_property` where NAME_ = 'deployment.lock';
delete from `act_ge_property` where NAME_ = 'history.cleanup.job.lock';
delete from `act_ge_property` where NAME_ = 'historyLevel';
delete from `act_ge_property` where NAME_ = 'next.dbid';
delete from `act_ge_property` where NAME_ = 'schema.history';
delete from `act_ge_property` where NAME_ = 'schema.version';
delete from `act_ge_property` where NAME_ = 'startup.lock';

INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('deployment.lock','0',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('history.cleanup.job.lock','0',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('historyLevel','3',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('next.dbid','1',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('schema.history','create(fox)',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('schema.version','fox',1);
INSERT INTO `act_ge_property` (`NAME_`,`VALUE_`,`REV_`) VALUES ('startup.lock','0',1);

SET FOREIGN_KEY_CHECKS = 1;