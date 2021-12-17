SET FOREIGN_KEY_CHECKS = 0;
delete from auth_sys_authority where id = '2c9380837b063455017b09e14e4d0000';

INSERT INTO `auth_sys_authority` (`id`,`created_by`,`created_time`,`updated_by`,`updated_time`,`is_active`,`is_deleted`,`code`,`description`,`display_name`,`scope`) 
VALUES ('2c9380837b063455017b09e14e4d0000','admin','2021-08-03 10:39:26',NULL,NULL,1,0,'ADMIN_CERTIFICATION',NULL,'ADMIN_CERTIFICATION','GLOBAL');

delete from auth_sys_role_authority where id = '2c9380837b063455017b09e14e530001';
INSERT INTO `auth_sys_role_authority` (`id`,`created_by`,`created_time`,`updated_by`,`updated_time`,`is_active`,`is_deleted`,`authority_code`,`authority_id`,`role_id`,`role_name`) 
VALUES ('2c9380837b063455017b09e14e530001','admin','2021-08-03 10:39:26',NULL,NULL,1,0,'ADMIN_CERTIFICATION','2c9380837b063455017b09e14e4d0000','2c9280827019695c017019ac974f001c','SUPER_ADMIN');
SET FOREIGN_KEY_CHECKS = 1;