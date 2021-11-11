ALTER TABLE `resource_server` ADD COLUMN `login_mode` VARCHAR(45) NOT NULL DEFAULT 'PASSWD';
ALTER TABLE `resource_server` ADD COLUMN `ssh_key` TEXT NULL;