alter table proc_def add column sub_proc bit(1) default 0 comment '是否子编排';
alter table proc_def_node add column sub_proc_def_id varchar(64) default null comment '子编排定义id';
alter table proc_data_preview add column sub_session_id varchar(64) default null comment '子试算id';
alter table proc_data_binding add column sub_proc_ins_id varchar(64) default null comment '子编排实例id';
alter table proc_ins add column parent_ins_node_id varchar(64) default null comment '父编排实例节点id';
alter table proc_run_workflow add column parent_run_node_id varchar(64) default null comment '父运行实例节点id';