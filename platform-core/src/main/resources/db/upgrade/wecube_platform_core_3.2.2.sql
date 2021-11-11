ALTER TABLE `smoke2_wecube`.`resource_server` 
ADD COLUMN `login_mode` VARCHAR(45) NOT NULL DEFAULT 'PASSWD' AFTER `updated_date`,
ADD COLUMN `ssh_key` TEXT NULL AFTER `login_mode`;