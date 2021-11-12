ALTER TABLE `resource_server` ADD COLUMN `login_mode` VARCHAR(45) NOT NULL DEFAULT 'PASSWD';
ALTER TABLE `resource_server` CHANGE COLUMN `login_password` `login_password` TEXT CHARACTER SET 'utf8';