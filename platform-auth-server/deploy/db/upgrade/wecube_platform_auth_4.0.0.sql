SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_user_role add column is_admin bit(1) default 0 COMMENT '是否角色管理员';
SET FOREIGN_KEY_CHECKS = 1;