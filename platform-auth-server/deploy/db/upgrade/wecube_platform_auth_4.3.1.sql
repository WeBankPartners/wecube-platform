SET FOREIGN_KEY_CHECKS = 0;
alter table auth_sys_user_role add index user_role_index(user_id);
SET FOREIGN_KEY_CHECKS = 1;