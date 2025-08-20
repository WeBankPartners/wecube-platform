ALTER TABLE trans_export_customer add column exec_workflow_ids varchar(500) DEFAULT NULL COMMENT '执行编排ID';

delete from system_variables where name='PLATFORM_EXPORT_NEXUS_PWD';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_REPO';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_URL';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_USER';
delete from system_variables where name='PLATFORM_EXPORT_EXEC_WORKFLOW';