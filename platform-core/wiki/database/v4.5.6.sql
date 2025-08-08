ALTER TABLE trans_export ADD COLUMN selected_tree_json TEXT COMMENT '前端选中的tree结构json';
ALTER TABLE trans_export modify column business text DEFAULT NULL COMMENT '业务';
ALTER TABLE trans_export modify column business_name text DEFAULT NULL COMMENT '业务名称';
ALTER TABLE trans_import modify column business text DEFAULT NULL COMMENT '业务';
ALTER TABLE trans_import modify column business_name text DEFAULT NULL COMMENT '业务名称';