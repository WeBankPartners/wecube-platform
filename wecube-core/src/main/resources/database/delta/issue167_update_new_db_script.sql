
call add_column('core_re_proc_def', 'PROC_DATA', 'LONGTEXT');
call add_column('core_re_proc_def', 'PROC_STATUS', 'varchar(45)');

call add_column('core_re_proc_task_service', 'TIMEOUT_EXPR', 'varchar(45)');
call add_column('core_re_proc_task_service', 'TASK_NODE_TYPE', 'varchar(45)');
call add_column('core_re_proc_task_service', 'CORE_PROC_DEF_ID', 'varchar(45)');