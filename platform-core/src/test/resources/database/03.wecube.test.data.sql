insert into menu_items (id,parent_id,code,description) values
(10001,null,'MOCK_MENU1',''),
(10002,null,'MOCK_MENU2','');

INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES (25, 'service-mt', 'v0.6', 'REGISTERED', '2019-11-05 03:25:25', '1');

INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, `display_name`, `path`) VALUES (50, 25, 'JOBS_SERVICE_CATALOG_MANAGEMENT', 'JOBS', 'Servive Catalog Management', '/service-catalog');
INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, `display_name`, `path`) VALUES (51, 25, 'JOBS_TASK_MANAGEMENT', 'JOBS', 'Task Management', '/task-management');
