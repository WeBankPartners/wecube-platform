alter table proc_def_node add column allow_continue bit(1) default 0 comment '允许跳过';
alter table proc_def_node modify column dynamic_bind tinyint(1) default 0 comment '是否动态绑定';
alter table proc_def_node modify column dynamic_bind tinyint(4) default 0 comment '动态绑定 -> 0(启动时绑定)|1->(绑定节点)|2->(运行时)';
alter table proc_ins modify column `entity_data_name` varchar(255) DEFAULT NULL COMMENT '根数据名称';

CREATE INDEX idx_pd_status ON proc_def (status);
CREATE INDEX idx_pd_created_time ON proc_def (created_time);

CREATE INDEX idx_pi_proc_def_id ON proc_ins (proc_def_id);
CREATE INDEX idx_pi_created_time ON proc_ins (created_time);
CREATE INDEX idx_pi_status ON proc_ins (status);

CREATE INDEX idx_pdb_proc_ins_node_id ON proc_data_binding (proc_ins_node_id);
CREATE INDEX idx_pdb_entity_data_id ON proc_data_binding (entity_data_id);
CREATE INDEX idx_pdb_proc_def_id ON proc_data_binding (proc_def_id);

CREATE INDEX idx_pin_proc_def_node_id ON proc_ins_node (proc_def_node_id);
CREATE INDEX idx_pin_proc_ins_id ON proc_ins_node (proc_ins_id);

CREATE INDEX idx_pinrp_from_type ON proc_ins_node_req_param (from_type);
CREATE INDEX idx_pinrp_created_time ON proc_ins_node_req_param (created_time);
CREATE INDEX idx_pinrp_req_id ON proc_ins_node_req_param (req_id);
CREATE INDEX idx_pinrp_callback_id ON proc_ins_node_req_param (callback_id);

CREATE INDEX idx_pinr_proc_ins_node_id ON proc_ins_node_req (proc_ins_node_id);
CREATE INDEX idx_pinr_created_time ON proc_ins_node_req (created_time);