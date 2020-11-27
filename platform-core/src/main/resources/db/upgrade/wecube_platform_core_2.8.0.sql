SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO menu_items (id, parent_code, code, source, menu_order , description, local_display_name) VALUES ('ADMIN__ADMIN_SYSTEM_DATA_MODEL', 'ADMIN', 'ADMIN_SYSTEM_DATA_MODEL', 'SYSTEM', 16 , '', '系统数据模型');
INSERT INTO role_menu (id, role_name, menu_code) VALUES ('SUPER_ADMIN__ADMIN_SYSTEM_DATA_MODEL', 'SUPER_ADMIN', 'ADMIN_SYSTEM_DATA_MODEL');

SET FOREIGN_KEY_CHECKS = 1;