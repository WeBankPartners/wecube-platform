ALTER TABLE `core_ru_proc_inst_info` ADD COLUMN `proc_batch_key` VARCHAR(45) NULL;

CREATE TABLE IF NOT EXISTS `core_user_scheduled_task` (
  `id` VARCHAR(20) NOT NULL,
  `created_by` VARCHAR(45) NULL,
  `created_time` DATETIME NULL,
  `updated_by` VARCHAR(45) NULL,
  `updated_time` DATETIME NULL,
  `owner` VARCHAR(45) NULL,
  `proc_def_id` VARCHAR(45) NULL,
  `proc_def_name` VARCHAR(100) NULL,
  `entity_data_id` VARCHAR(100) NULL,
  `entity_data_name` VARCHAR(100) NULL,
  `status` VARCHAR(45) NULL,
  `schedule_mode` VARCHAR(45) NULL,
  `schedule_expr` VARCHAR(100) NULL,
  `rev` INT NOT NULL DEFAULT 0,
  `exec_start_time` DATETIME NULL,
  `exec_end_time` DATETIME NULL,
  `exec_times` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
  
INSERT INTO `menu_items` (`id`,`parent_code`,`code`,`source`,`description`,`local_display_name`,`menu_order`) 
VALUES ('ADMIN_SYSTEM_WORKFLOW_REPORT','ADMIN','ADMIN_SYSTEM_WORKFLOW_REPORT','SYSTEM','','编排执行报表',18);

INSERT INTO `role_menu` (`id`,`role_name`,`menu_code`) 
VALUES ('sInz567m2BlS','SUPER_ADMIN','ADMIN_SYSTEM_WORKFLOW_REPORT');