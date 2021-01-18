SET FOREIGN_KEY_CHECKS = 0;

alter table auth_sys_api  convert to character set utf8 collate utf8_bin;
alter table auth_sys_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_role convert to character set utf8 collate utf8_bin;
alter table auth_sys_role_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_sub_system convert to character set utf8 collate utf8_bin;
alter table auth_sys_sub_system_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_user convert to character set utf8 collate utf8_bin;
alter table auth_sys_user_role collate convert to character set utf8 collate utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;