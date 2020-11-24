ALTER TABLE `plugin_package_attributes` 
ADD COLUMN `ref_package` VARCHAR(45) NULL,
ADD COLUMN `ref_entity` VARCHAR(45) NULL,
ADD COLUMN `ref_attr` VARCHAR(45) NULL;
