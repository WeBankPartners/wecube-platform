ALTER TABLE `resource_server` ADD COLUMN `login_mode` VARCHAR(45) NOT NULL DEFAULT 'PASSWD';
ALTER TABLE `resource_server` CHANGE COLUMN `login_password` `login_password` TEXT CHARACTER SET 'utf8';

ALTER TABLE `plugin_package_runtime_resources_s3` ADD COLUMN `additional_properties` TEXT NULL;

ALTER TABLE `plugin_packages` ADD COLUMN `edition` VARCHAR(45) NOT NULL DEFAULT 'community';
ALTER TABLE `plugin_packages` DROP INDEX name;