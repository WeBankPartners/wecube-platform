ALTER TABLE trans_export_customer add column exec_workflow_ids varchar(500) DEFAULT NULL COMMENT '执行编排ID';

delete from system_variables where name='PLATFORM_EXPORT_NEXUS_PWD';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_REPO';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_URL';
delete from system_variables where name='PLATFORM_EXPORT_NEXUS_USER';
delete from system_variables where name='PLATFORM_EXPORT_EXEC_WORKFLOW';

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_EXPORT_IGNORE_ZONE_REPORT', '', 'PLATFORM_EXPORT_IGNORE_ZONE_REPORT', '', 'export_ignore_deploy_zone', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_EXPORT_DEPLOY_ZONE_GROUP', '', 'PLATFORM_EXPORT_DEPLOY_ZONE_GROUP', '', 'deploy_zone_group', 'global','system', 'active');

ALTER TABLE trans_export add column exclude_deploy_zone varchar(500) DEFAULT NULL COMMENT '排除的部署区域';