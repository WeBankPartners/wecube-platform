SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_role_apply add column expire_time datatime default null COMMENT '过期时间';
alter table auth_sys_user_role add column expire_time datatime default null COMMENT '过期时间';
alter table auth_sys_user_role add column notify_count tinyint default 0 COMMENT '快要过期通知';
alter table auth_sys_role_apply add index role_apply_expire_time(expire_time);
alter table auth_sys_role_apply add index role_apply_status(status);
SET FOREIGN_KEY_CHECKS = 1;