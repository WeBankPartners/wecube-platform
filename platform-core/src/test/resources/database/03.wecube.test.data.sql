insert into menu_items (id,parent_code,code,source,menu_order,description) values
('MOCK_MENU1',null,'MOCK_MENU1','SYSTEM', 1, ''),
('MOCK_MENU2',null,'MOCK_MENU2','SYSTEM', 2, '');

INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES ('service-mt:v0.6', 'service-mt', 'v0.6', 'REGISTERED', '2019-11-05 03:25:25', '1');

INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, source, menu_order, `display_name`, `path`) VALUES ('JOBS:JOBS_SERVICE_CATALOG_MANAGEMENT', '25', 'JOBS_SERVICE_CATALOG_MANAGEMENT', 'JOBS', 'PLUGIN', 1, 'Servive Catalog Management', '/service-catalog');
INSERT INTO `plugin_package_menus` (`id`, `plugin_package_id`, `code`, `category`, source, menu_order, `display_name`, `path`) VALUES ('JOBS:JOBS_TASK_MANAGEMENT', '25', 'JOBS_TASK_MANAGEMENT', 'JOBS', 'PLUGIN', 2, 'Task Management', '/task-management');
