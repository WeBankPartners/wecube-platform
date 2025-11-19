alter table trans_export add column source_export varchar(64) DEFAULT NULL COMMENT '源导出纪录id';
alter table trans_export ADD COLUMN incremental_description varchar(1024) COMMENT '增量导出描述';
alter table trans_export add column diff_data text DEFAULT NULL COMMENT '新增的业务产品,部署区域等数据';
alter table trans_export_analyze_data add column diff_data text DEFAULT NULL COMMENT '差异数据';
alter table trans_import add column diff_data text DEFAULT NULL COMMENT '新增的业务产品,部署区域等数据';
alter table trans_import add column source_export varchar(64) DEFAULT NULL COMMENT '源导出纪录id';
