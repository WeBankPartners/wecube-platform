alter table proc_def_node add column allow_continue bit(1) default 0 comment '允许跳过';
alter table proc_def_node modify column dynamic_bind tinyint(1) default 0 comment '是否动态绑定';
alter table proc_def_node modify column dynamic_bind tinyint(4) default 0 comment '动态绑定 -> 0(启动时绑定)|1->(绑定节点)|2->(运行时)';