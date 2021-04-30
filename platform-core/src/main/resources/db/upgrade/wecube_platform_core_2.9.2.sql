ALTER TABLE `core_ru_graph_node` ADD COLUMN `full_data_id` VARCHAR(300) NULL;

ALTER TABLE `core_ru_proc_exec_binding` ADD COLUMN `full_data_id` VARCHAR(300) NULL;

ALTER TABLE `core_ru_proc_exec_binding_tmp` ADD COLUMN `full_data_id` VARCHAR(300) NULL;

ALTER TABLE `core_ru_task_node_exec_param` ADD COLUMN `full_data_id` VARCHAR(300) NULL;