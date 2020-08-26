update menu_items set menu_order = 6 where id = 'ADJUSTMENT';
update menu_items set menu_order = 5 where id = 'INTELLIGENCE_OPS';

alter table core_re_proc_def_info modify proc_def_data  MEDIUMTEXT;