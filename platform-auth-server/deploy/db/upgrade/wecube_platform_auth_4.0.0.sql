SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_role add column administrator varchar(255) default "" COMMENT '角色管理员';
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `auth_sys_role_apply` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `created_by` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `email_addr` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `role_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;