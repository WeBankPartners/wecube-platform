SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE IF NOT EXISTS  `auth_sys_api` (
    `id` VARCHAR(255) COLLATE utf8_bin   NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `api_url` VARCHAR(2048) COLLATE utf8_bin   NULL DEFAULT NULL,
    `http_method` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `name` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `system_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_authority` (
    `id` VARCHAR(255) COLLATE utf8_bin   NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `code` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `description` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `display_name` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `scope` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8  COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_role` (
    `id` VARCHAR(255) COLLATE utf8_bin   NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `description` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `display_name` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `email_addr` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `name` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_role_authority` (
    `id` VARCHAR(255) COLLATE utf8_bin   NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin   NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `authority_code` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `authority_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `role_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `role_name` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_sub_system` (
    `id` VARCHAR(255) COLLATE utf8_bin  NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `api_key` VARCHAR(2048) COLLATE utf8_bin  NULL DEFAULT NULL,
    `is_blocked` BIT(1) NULL DEFAULT NULL,
    `description` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `name` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `pub_api_key` VARCHAR(2048) COLLATE utf8_bin  NULL DEFAULT NULL,
    `system_code` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_sub_system_authority` (
    `id` VARCHAR(255) COLLATE utf8_bin  NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `authority_code` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `authority_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `sub_system_code` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `sub_system_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_user` (
    `id` VARCHAR(255) COLLATE utf8_bin  NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_blocked` BIT(1) NULL DEFAULT NULL,
    `cell_phone_no` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `dept` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `email_addr` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `english_name` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `local_name` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `office_tel_no` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `password` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `title` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `username` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `auth_ctx` varchar(2048) COLLATE utf8_bin  DEFAULT NULL,
    `auth_src` varchar(255) COLLATE utf8_bin  DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

CREATE TABLE IF NOT EXISTS  `auth_sys_user_role` (
    `id` VARCHAR(255) COLLATE utf8_bin  NOT NULL,
    `created_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `role_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `role_name` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `user_id` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `username` VARCHAR(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
ENGINE=InnoDB
;

SET FOREIGN_KEY_CHECKS = 1;