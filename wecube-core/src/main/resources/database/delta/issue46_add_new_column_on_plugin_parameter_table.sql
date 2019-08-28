
call add_column('plugin_cfg_inf_parameters', 'mapping_type', 'varchar(50) ');
call add_column('plugin_cfg_inf_parameters', 'cmdb_enum_code', 'int');

update plugin_cfg_inf_parameters set mapping_type = 'CMDB_CI_TYPE' where mapping_type is NULL and type='INPUT';
update plugin_cfg_inf_parameters set mapping_type = 'N/A' where mapping_type is NULL and type='OUTPUT';
