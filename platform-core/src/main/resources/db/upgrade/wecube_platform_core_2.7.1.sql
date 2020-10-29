SET FOREIGN_KEY_CHECKS = 0;

create index idx_core_operation_event_seq_no_1 on core_operation_event(event_seq_no);
create index idx_core_ru_proc_exec_binding_inst_id_1 on core_ru_proc_exec_binding(proc_inst_id);

delete from system_variables where id = 'system__global__CORE_ADDR';

SET FOREIGN_KEY_CHECKS = 1;