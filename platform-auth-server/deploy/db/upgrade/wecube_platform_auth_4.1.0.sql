SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_role_apply add column expire_time datetime default null COMMENT '过期时间';
alter table auth_sys_user_role add column expire_time datetime default null COMMENT '过期时间';
alter table auth_sys_user_role add column notify_count tinyint default 0 COMMENT '快要过期通知';
alter table auth_sys_user_role add index role_apply_expire_time(expire_time);
alter table auth_sys_role_apply add index role_apply_status(status);
alter table auth_sys_user_role add column role_apply varchar(255) default null COMMENT '角色申请ID';
alter table auth_sys_user_role add CONSTRAINT `fore_auth_sys_user_role_apply` FOREIGN KEY (role_apply) REFERENCES auth_sys_role_apply(id);
SET FOREIGN_KEY_CHECKS = 1;