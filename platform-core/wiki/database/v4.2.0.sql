alter table proc_def add column sub_proc bit(1) default 0 comment '是否子编排';
alter table proc_def_node add column sub_proc_def_id varchar(64) default null comment '子编排定义id';
alter table proc_data_preview add column sub_session_id varchar(64) default null comment '子试算id';
alter table proc_data_binding add column sub_proc_ins_id varchar(64) default null comment '子编排实例id';
alter table proc_data_binding add column sub_session_id varchar(64) default null comment '子试算id';
alter table proc_ins add column parent_ins_node_id varchar(64) default null comment '父编排实例节点id';
alter table proc_run_workflow add column parent_run_node_id varchar(64) default null comment '父运行实例节点id';

CREATE TABLE `proc_run_node_sub_proc` (
      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
      `proc_run_node_id` varchar(64) NOT NULL COMMENT '任务节点id',
      `workflow_id` varchar(64) NOT NULL COMMENT '子工作流id',
      `entity_type_id` varchar(64) NOT NULL COMMENT '绑定数据entity',
      `entity_data_id` varchar(64) NOT NULL COMMENT '绑定数据id',
      `created_time` datetime DEFAULT NULL COMMENT '创建时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

alter table plugin_packages add column register_done tinyint(8) default 1 comment '是否完成注册';
alter table plugin_packages add column updated_by varchar(64) default null comment '更新人';
alter table plugin_packages add column updated_time datetime default null comment '更新时间';

alter table proc_def_collect modify column `role_id` varchar(64) DEFAULT NULL COMMENT '角色id';