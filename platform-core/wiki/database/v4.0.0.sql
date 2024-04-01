alter table plugin_package_data_model add column `updated_time` datetime DEFAULT NULL COMMENT '更新时间-新';
alter table plugin_package_attributes add column `is_array` bit(1) DEFAULT b'0' COMMENT '是否数组-新';

