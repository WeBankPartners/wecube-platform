ALTER TABLE trans_export ADD COLUMN selected_tree_json TEXT COMMENT '前端选中的tree结构json';
ALTER TABLE trans_import ADD COLUMN selected_tree_json TEXT COMMENT '前端选中的tree结构json';