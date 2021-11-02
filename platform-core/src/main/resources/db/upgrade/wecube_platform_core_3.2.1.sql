ALTER TABLE `core_re_task_node_def_info` ADD COLUMN `ass_node_id` VARCHAR(45) NULL;

ALTER TABLE `plugin_package_attributes` ADD COLUMN `created_time` DATETIME NULL;
ALTER TABLE `plugin_package_attributes` ADD COLUMN `order_no` INT NULL DEFAULT 0;

update plugin_config_interfaces set type = 'EXECUTION'  where type is null or type = '';