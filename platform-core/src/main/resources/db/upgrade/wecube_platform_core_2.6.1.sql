update menu_items set menu_order = 6 where id = 'ADJUSTMENT';
update menu_items set menu_order = 5 where id = 'INTELLIGENCE_OPS';

alter table core_re_proc_def_info modify proc_def_data  MEDIUMTEXT;

delete from system_variables where id = 'system__global__HTTP_PROXY';
delete from system_variables where id = 'system__global__HTTPS_PROXY';

INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__HTTP_PROXY', NULL, 'HTTP_PROXY', NULL, '', 'global', 'system', 'active');
INSERT INTO `system_variables` (`id`,`package_name`, `name`, `value`, `default_value`, `scope`, `source`, `status`) VALUES ('system__global__HTTPS_PROXY', NULL, 'HTTPS_PROXY', NULL, '', 'global', 'system', 'active');
