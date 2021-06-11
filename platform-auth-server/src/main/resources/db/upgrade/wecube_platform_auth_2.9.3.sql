SET FOREIGN_KEY_CHECKS = 0;
delete from auth_sys_user_role where id = '2c93808376d095830176d15ceb6a000a';

INSERT INTO `auth_sys_user_role` (`id`,`created_by`,`created_time`,`updated_by`,`updated_time`,`is_active`,`is_deleted`,`role_id`,`role_name`,`user_id`,`username`) 
VALUES ('2c93808376d095830176d15ceb6a000a','SYSTEM','2021-01-05 15:04:56',NULL,NULL,'1','0','8ab86ba0723a78fe01723a790ceb0000','SUB_SYSTEM','2c9280827019695c017019a2d5ac001b','umadmin');
SET FOREIGN_KEY_CHECKS = 1;