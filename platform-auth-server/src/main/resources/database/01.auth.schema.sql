SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS `auth_sys_api`;
CREATE TABLE `auth_sys_api` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `api_url` VARCHAR(255) NULL DEFAULT NULL,
    `http_method` VARCHAR(255) NULL DEFAULT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    `system_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_authority`;
CREATE TABLE `auth_sys_authority` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `code` VARCHAR(255) NULL DEFAULT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `display_name` VARCHAR(255) NULL DEFAULT NULL,
    `scope` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_role`;
CREATE TABLE `auth_sys_role` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `display_name` VARCHAR(255) NULL DEFAULT NULL,
    `email_addr` VARCHAR(255) NULL DEFAULT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_role_authority`;
CREATE TABLE `auth_sys_role_authority` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `authority_code` VARCHAR(255) NULL DEFAULT NULL,
    `authority_id` VARCHAR(255) NULL DEFAULT NULL,
    `role_id` VARCHAR(255) NULL DEFAULT NULL,
    `role_name` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_sub_system`;
CREATE TABLE `auth_sys_sub_system` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `api_key` VARCHAR(500) NULL DEFAULT NULL,
    `is_blocked` BIT(1) NULL DEFAULT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    `pub_api_key` VARCHAR(500) NULL DEFAULT NULL,
    `system_code` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_sub_system_authority`;
CREATE TABLE `auth_sys_sub_system_authority` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `authority_code` VARCHAR(255) NULL DEFAULT NULL,
    `authority_id` VARCHAR(255) NULL DEFAULT NULL,
    `sub_system_code` VARCHAR(255) NULL DEFAULT NULL,
    `sub_system_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_user`;
CREATE TABLE `auth_sys_user` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_blocked` BIT(1) NULL DEFAULT NULL,
    `cell_phone_no` VARCHAR(255) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `dept` VARCHAR(255) NULL DEFAULT NULL,
    `email_addr` VARCHAR(255) NULL DEFAULT NULL,
    `english_name` VARCHAR(255) NULL DEFAULT NULL,
    `local_name` VARCHAR(255) NULL DEFAULT NULL,
    `office_tel_no` VARCHAR(255) NULL DEFAULT NULL,
    `password` VARCHAR(255) NULL DEFAULT NULL,
    `title` VARCHAR(255) NULL DEFAULT NULL,
    `username` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
)
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

DROP TABLE IF EXISTS `auth_sys_user_role`;
CREATE TABLE `auth_sys_user_role` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT NULL,
    `role_id` VARCHAR(255) NULL DEFAULT NULL,
    `role_name` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    `username` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) 
 DEFAULT CHARSET=utf8 
ENGINE=InnoDB
;

SET FOREIGN_KEY_CHECKS=1;
