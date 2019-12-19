insert into menu_items (id,parent_code,code,source,menu_order,description) values
('MOCK_MENU1',null,'MOCK_MENU1','SYSTEM', 1, ''),
('MOCK_MENU2',null,'MOCK_MENU2','SYSTEM', 2, '');

INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES ('service-mt__v0.6', 'service-mt', 'v0.6', 'REGISTERED', '2019-11-05 03:25:25', '1');

INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, source, menu_order, `display_name`, `path`) VALUES ('JOBS__JOBS_SERVICE_CATALOG_MANAGEMENT', '25', 'JOBS_SERVICE_CATALOG_MANAGEMENT', 'JOBS', 'PLUGIN', 1, 'Servive Catalog Management', '/service-catalog');
INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, source, menu_order, `display_name`, `path`) VALUES ('JOBS__JOBS_TASK_MANAGEMENT', '25', 'JOBS_TASK_MANAGEMENT', 'JOBS', 'PLUGIN', 2, 'Task Management', '/task-management');

INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES ('service-mgmt__v1.0', 'service-mgmt', 'v1.0', 'UNREGISTERED', '2019-11-25 20:31:48', '1');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES (1, NULL, 'CORE_ADDR', NULL, 'http://127.0.0.1:19090', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES (2, NULL, 'BASE_MOUNT_PATH', NULL, '/data', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES (3, 'service-mgmt', 'ALLOCATE_PORT', NULL, '20000', 'service-mgmt', 'service-mgmt__v1.0', 'active');
INSERT INTO `system_variables` (`id`, `package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES (4, 'service-mgmt', 'ALLOCATE_HOST', NULL, NULL, 'service-mgmt', 'service-mgmt__v1.0', 'active');

INSERT INTO `plugin_instances` (`id`, `host`, `container_name`, `port`, `container_status`, `package_id`, `docker_instance_resource_id`, `instance_name`, `plugin_mysql_instance_resource_id`, `s3bucket_resource_id`) VALUES ('service-mgmt__v1.0__service-mgmt__10.0.2.12__20003', '10.0.2.12', 'service-mgmt', 20003, 'RUNNING', 'service-mgmt__v1.0', NULL, 'wecmdb', NULL, NULL);
