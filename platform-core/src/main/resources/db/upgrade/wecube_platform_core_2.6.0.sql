CREATE TABLE  IF NOT EXISTS `plugin_config_roles` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `perm_type` varchar(255) DEFAULT NULL,
  `plugin_cfg_id` varchar(255) DEFAULT NULL,
  `role_id` varchar(255) DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


delete from plugin_config_roles where created_by = 'bootScript';

insert into plugin_config_roles
(id,created_by,created_time,is_active,perm_type,plugin_cfg_id,role_id,role_name)
select uuid() as id,
'bootScript' as created_by,
sysdate(),
1 as is_actvie,
'MGMT' as perm_type,
t.id as plugin_cfg_id,
'2c9280827019695c017019ac974f001c' as role_id,
'SUPER_ADMIN' as role_name
from plugin_configs t
where t.register_name is not null;