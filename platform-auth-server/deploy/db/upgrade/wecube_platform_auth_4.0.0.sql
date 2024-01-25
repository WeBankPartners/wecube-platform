SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_role add column administrator varchar(255) default "" COMMENT '角色管理员';
SET FOREIGN_KEY_CHECKS = 1;