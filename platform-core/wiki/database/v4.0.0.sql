alter table plugin_package_data_model add column `updated_time` datetime DEFAULT NULL COMMENT '更新时间-新';
alter table plugin_package_attributes add column `is_array` bit(1) DEFAULT b'0' COMMENT '是否数组-新';

alter table plugin_config_roles modify plugin_cfg_id varchar(127) null;
CREATE INDEX plugin_config_roles_cfgId_IDX USING BTREE ON plugin_config_roles (plugin_cfg_id);
