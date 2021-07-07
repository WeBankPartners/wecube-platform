ALTER TABLE `core_ru_proc_exec_binding` ADD COLUMN `confirm_token` VARCHAR(45) NULL;
ALTER TABLE `plugin_object_meta` ADD COLUMN `map_expr` VARCHAR(300) NULL;
ALTER TABLE `plugin_config_interface_parameters` ADD COLUMN `mapping_val` VARCHAR(500) NULL;