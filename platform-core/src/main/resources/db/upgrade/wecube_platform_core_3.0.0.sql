ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `ref_package` VARCHAR(45) NULL,
ADD COLUMN `ref_entity` VARCHAR(45) NULL,
ADD COLUMN `ref_attr` VARCHAR(45) NULL;

ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `mandatory` TINYINT NULL DEFAULT 0;

ALTER TABLE `core_re_task_node_def_info` 
ADD COLUMN `dynamic_bind` VARCHAR(45) NULL DEFAULT 'N';

