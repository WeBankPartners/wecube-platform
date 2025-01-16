alter table resource_item add column `username` varchar(64) default null;
alter table resource_item add column `password` varchar(255) default null;

alter table proc_data_preview modify column `entity_type_id` varchar(255) DEFAULT NULL COMMENT '数据entity';
alter table proc_ins modify column `entity_type_id` varchar(255) DEFAULT NULL COMMENT '根数据类型';
alter table proc_schedule_config modify column `entity_type_id` varchar(255) DEFAULT NULL COMMENT '根数据类型';
alter table proc_data_binding modify column `entity_type_id` varchar(255) DEFAULT NULL COMMENT '数据entity';
alter table proc_data_cache modify column `entity_type_id` varchar(255) DEFAULT NULL COMMENT '数据entity';