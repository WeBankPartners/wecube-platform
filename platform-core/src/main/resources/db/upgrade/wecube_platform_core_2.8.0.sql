SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO menu_items (id, parent_code, code, source, menu_order , description, local_display_name) VALUES ('ADMIN__ADMIN_SYSTEM_DATA_MODEL', 'ADMIN', 'ADMIN_SYSTEM_DATA_MODEL', 'SYSTEM', 16 , '', '系统数据模型');
INSERT INTO role_menu (id, role_name, menu_code) VALUES ('SUPER_ADMIN__ADMIN_SYSTEM_DATA_MODEL', 'SUPER_ADMIN', 'ADMIN_SYSTEM_DATA_MODEL');

UPDATE system_variables set default_value = 'https://wecube-1259801214.cos.ap-guangzhou.myqcloud.com/plugins-v2/public-plugin-artifacts.release' where id = 'system__global__PLUGIN_ARTIFACTS_RELEASE_URL';
SET FOREIGN_KEY_CHECKS = 1;