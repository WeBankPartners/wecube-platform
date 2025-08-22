ALTER TABLE trans_export ADD COLUMN selected_tree_json TEXT COMMENT '前端选中的tree结构json';
ALTER TABLE trans_import ADD COLUMN selected_tree_json TEXT COMMENT '前端选中的tree结构json';
ALTER TABLE trans_export modify column business text DEFAULT NULL COMMENT '业务';
ALTER TABLE trans_export modify column business_name text DEFAULT NULL COMMENT '业务名称';
ALTER TABLE trans_import modify column business text DEFAULT NULL COMMENT '业务';
ALTER TABLE trans_import modify column business_name text DEFAULT NULL COMMENT '业务名称';

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_INIT_DB_PWD', '', 'PLATFORM_IMPORT_INIT_DB_PWD', '', '', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_INIT_APP_PWD', '', 'PLATFORM_IMPORT_INIT_APP_PWD', '', '', 'global','system', 'active');
