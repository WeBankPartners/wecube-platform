CREATE TABLE `core_proc_exec_context` (
  `id` VARCHAR(30) NOT NULL,
  `proc_def_id` VARCHAR(45) NULL,
  `proc_inst_id` INT NULL,
  `node_def_id` VARCHAR(45) NULL,
  `node_inst_id` INT NULL,
  `req_id` VARCHAR(45) NULL,
  `req_dir` VARCHAR(45) NULL,
  `ctx_type` VARCHAR(45) NULL,
  `ctx_data_format` VARCHAR(45) NULL,
  `ctx_data` MEDIUMTEXT NULL,
  `created_by` VARCHAR(45) NULL,
  `updated_by` VARCHAR(45) NULL,
  `created_time` DATETIME NULL,
  `updated_time` DATETIME NULL,
  `rev` int(11) NOT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;