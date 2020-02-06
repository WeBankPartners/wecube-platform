set names 'utf8';

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Records of auth_sys_authority
-- ----------------------------
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28b0f001e', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_WORKFLOW_EXECUTION', null, 'IMPLEMENTATION_WORKFLOW_EXECUTION', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28b130020', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_BATCH_EXECUTION', null, 'IMPLEMENTATION_BATCH_EXECUTION', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28b170022', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_ARTIFACT_MANAGEMENT', null, 'IMPLEMENTATION_ARTIFACT_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28f630024', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_MAIN_DASHBOARD', null, 'MONITOR_MAIN_DASHBOARD', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28f670026', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_METRIC_CONFIG', null, 'MONITOR_METRIC_CONFIG', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28f6a0028', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_CUSTOM_DASHBOARD', null, 'MONITOR_CUSTOM_DASHBOARD', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28f72002a', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_ALARM_CONFIG', null, 'MONITOR_ALARM_CONFIG', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c28f76002c', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_ALARM_MANAGEMENT', null, 'MONITOR_ALARM_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a086002e', 'umadmin', '2020-02-06 17:09:10', null, null, '', '\0', 'COLLABORATION_PLUGIN_MANAGEMENT', null, 'COLLABORATION_PLUGIN_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a08a0030', 'umadmin', '2020-02-06 17:09:10', null, null, '', '\0', 'COLLABORATION_WORKFLOW_ORCHESTRATION', null, 'COLLABORATION_WORKFLOW_ORCHESTRATION', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a8690032', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_SYSTEM_PARAMS', null, 'ADMIN_SYSTEM_PARAMS', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a86d0034', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_RESOURCES_MANAGEMENT', null, 'ADMIN_RESOURCES_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a8700036', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_USER_ROLE_MANAGEMENT', null, 'ADMIN_USER_ROLE_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a8740038', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_CMDB_MODEL_MANAGEMENT', null, 'ADMIN_CMDB_MODEL_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a877003a', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'CMDB_ADMIN_BASE_DATA_MANAGEMENT', null, 'CMDB_ADMIN_BASE_DATA_MANAGEMENT', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a87b003c', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_QUERY_LOG', null, 'ADMIN_QUERY_LOG', 'GLOBAL');
INSERT INTO `auth_sys_authority` VALUES ('2c9280827019695c017019c2a87f003e', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'MENU_ADMIN_PERMISSION_MANAGEMENT', null, 'MENU_ADMIN_PERMISSION_MANAGEMENT', 'GLOBAL');

-- ----------------------------
-- Records of auth_sys_role
-- ----------------------------
INSERT INTO `auth_sys_role` VALUES ('2c9280827019695c017019ac974f001c', 'system', '2019-12-09 17:19:22', null, null, '', '\0', '系统管理员', '系统管理员', null, 'admin');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794c3a270000', 'system', '2020-01-06 13:20:36', null, null, '', '\0', 'CMDB管理员', 'CMDB管理员', null, 'CMDB_ADMIN');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794cd6dd0001', 'system', '2020-01-06 13:21:16', null, null, '', '\0', '监控管理员', '监控管理员', null, 'MONITOR_ADMIN');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794d6bb50002', 'system', '2020-01-06 13:21:54', null, null, '', '\0', '生产运维', '生产运维', null, 'PRD_OPS');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794e0d3b0003', 'system', '2020-01-06 13:22:35', null, null, '', '\0', '测试运维', '测试运维', null, 'STG_OPS');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794e9b170004', 'system', '2020-01-06 13:23:12', null, null, '', '\0', '应用架构师', '应用架构师', null, 'APP_ARC');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794f20440005', 'system', '2020-01-06 13:23:46', null, null, '', '\0', '基础架构师', '基础架构师', null, 'IFA_ARC');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f794ff45e0006', 'system', '2020-01-06 13:24:40', null, null, '', '\0', '应用开发人员', '应用开发人员', null, 'APP_DEV');
INSERT INTO `auth_sys_role` VALUES ('2c9280836f78a84b016f795068870007', 'system', '2020-01-06 13:25:10', null, null, '', '\0', '基础架构运维人员', '基础架构运维人员', null, 'IFA_OPS');


-- ----------------------------
-- Records of auth_sys_role_authority
-- ----------------------------
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28b10001f', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_WORKFLOW_EXECUTION', '2c9280827019695c017019c28b0f001e', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28b140021', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_BATCH_EXECUTION', '2c9280827019695c017019c28b130020', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28b180023', 'umadmin', '2020-02-06 17:09:04', null, null, '', '\0', 'IMPLEMENTATION_ARTIFACT_MANAGEMENT', '2c9280827019695c017019c28b170022', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28f640025', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_MAIN_DASHBOARD', '2c9280827019695c017019c28f630024', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28f680027', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_METRIC_CONFIG', '2c9280827019695c017019c28f670026', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28f6b0029', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_CUSTOM_DASHBOARD', '2c9280827019695c017019c28f6a0028', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28f73002b', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_ALARM_CONFIG', '2c9280827019695c017019c28f72002a', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c28f77002d', 'umadmin', '2020-02-06 17:09:06', null, null, '', '\0', 'MONITOR_ALARM_MANAGEMENT', '2c9280827019695c017019c28f76002c', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a087002f', 'umadmin', '2020-02-06 17:09:10', null, null, '', '\0', 'COLLABORATION_PLUGIN_MANAGEMENT', '2c9280827019695c017019c2a086002e', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a08b0031', 'umadmin', '2020-02-06 17:09:10', null, null, '', '\0', 'COLLABORATION_WORKFLOW_ORCHESTRATION', '2c9280827019695c017019c2a08a0030', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a86a0033', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_SYSTEM_PARAMS', '2c9280827019695c017019c2a8690032', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a86e0035', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_RESOURCES_MANAGEMENT', '2c9280827019695c017019c2a86d0034', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a8710037', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_USER_ROLE_MANAGEMENT', '2c9280827019695c017019c2a8700036', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a8750039', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_CMDB_MODEL_MANAGEMENT', '2c9280827019695c017019c2a8740038', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a878003b', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'CMDB_ADMIN_BASE_DATA_MANAGEMENT', '2c9280827019695c017019c2a877003a', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a87c003d', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'ADMIN_QUERY_LOG', '2c9280827019695c017019c2a87b003c', '2c9280827019695c017019ac974f001c', 'admin');
INSERT INTO `auth_sys_role_authority` VALUES ('2c9280827019695c017019c2a880003f', 'umadmin', '2020-02-06 17:09:12', null, null, '', '\0', 'MENU_ADMIN_PERMISSION_MANAGEMENT', '2c9280827019695c017019c2a87f003e', '2c9280827019695c017019ac974f001c', 'admin');


-- ----------------------------
-- Records of auth_sys_sub_system
-- ----------------------------
INSERT INTO `auth_sys_sub_system` VALUES ('2c9280827019695c0170197b79470000', 'system', '2020-01-06 20:04:25', null, null, '', 'MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAwnTN7JDXFcSoikXuNOQDtAjic1Wu6oAtCQJquCJmXrBTqB7hwS2mK6TuT8P7Jx60BQcaRL12hPLi6cOiCawuVwIDAQABAkB9NORazDARjhzPW5OzbpWL2KSmiqcjywA0at/4S/4KPPM8vwRjzEMs7pV9nSJ2M+/YOqPMBDl8iBUSLpfKf/uxAiEA52UroIvo2URlmAycaJm7+e4QqqfhEnM9wlGCJwL2jTsCIQDXIh2zwN7KQEIypmOL+uXvlZUjmx0Tj29mWOwP/fBBlQIhAI9+VLSlror1eE73GxNeqoxNznYVz2RCpLzZEO4iT0S7AiARg0Z1tpKsVjTNWLwrzf3f1gZxApSIXhnMdBqrZpmjTQIhAJhgYctlaydmggTPCqWLGub9WqEyH2HrrcabRvpWdEcV', '\0', null, 'Wecube Platform Core', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMJ0zeyQ1xXEqIpF7jTkA7QI4nNVruqALQkCargiZl6wU6ge4cEtpiuk7k/D+ycetAUHGkS9doTy4unDogmsLlcCAwEAAQ==', 'SYS_PLATFORM');
INSERT INTO `auth_sys_sub_system` VALUES ('2c9280827019695c0170199c2375001a', 'system', '2020-01-06 20:04:44', null, null, '', 'MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEAhErKNhmx4o7apVfYxPEDOxaOkKe7lwk2uLzigW5NTLlhZRLJ4d7qXqAdBEFgUwj5KvzGtlp+v5c120X+JYFYUwIDAQABAkAFYSkx4/+Yz+hSOu1ErOxNtdAcT8XQEX7ZKk0nqD2adgw/fjUCdeVCde/bzEVyhdguT+cSAHVicyvRU8o4/r0xAiEA1Uv8EYtayyo0vMz5caR1uOhJDBoBgi1IsHF/+WMhPSsCIQCexxsXLl9DAD1tsJejfJiQEkef6kwsaw+TfHJkvnDNeQIhANDbh6bySuR3no5lM7hYrsFyCt0jtehvSSck7IgZzlljAiEAmgKFO4IGcwX7j7c4DyNfFHg2s13fj0I1tJiEmUXEQvkCIQC+nepLywSWr/XDIcRHnATReCfytK7+d3wDiy4d4YaVhQ==', '\0', null, 'WeCMDB Plugin', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIRKyjYZseKO2qVX2MTxAzsWjpCnu5cJNri84oFuTUy5YWUSyeHe6l6gHQRBYFMI+Sr8xrZafr+XNdtF/iWBWFMCAwEAAQ==', 'SYS_WECMDB');

-- ----------------------------
-- Records of auth_sys_user
-- ----------------------------
INSERT INTO `auth_sys_user` VALUES ('2c9280827019695c017019a2d5ac001b', 'system', '2020-02-06 12:05:03', null, null, '', '\0', '10000000000', '\0', 'OPT', null, 'UM ADMIN', 'UM管理员', '0755-12345678', '$2a$10$XH7kL/aIjCKwZZ2CXd5Nk.dFxyP4UubHa7vqekT1IYB1dX./0Hr8m', '运维岗', 'umadmin');
INSERT INTO `auth_sys_user` VALUES ('2c9280827019695c017019dac0ea0040', 'umadmin', '2020-02-06 17:35:31', null, null, '', '\0', null, '\0', null, null, null, null, null, '$2a$10$YOyZUonK23qiPS03MeZQL.T.4LHje8FRbp6dhV2wHBGeVWdm9hwtu', null, 'admin');

-- ----------------------------
-- Records of auth_sys_user_role
-- ----------------------------
INSERT INTO `auth_sys_user_role` VALUES ('2c9280827019695c017019aec87f001d', 'system', '2020-02-06 16:47:29', null, null, '', '\0', '2c9280827019695c017019ac974f001c', 'admin', '2c9280827019695c017019a2d5ac001b', 'umadmin');
INSERT INTO `auth_sys_user_role` VALUES ('2c9280827019695c017019daf64f0041', 'umadmin', '2020-02-06 17:35:45', null, null, '', '\0', '2c9280827019695c017019ac974f001c', 'admin', '2c9280827019695c017019dac0ea0040', 'admin');

SET FOREIGN_KEY_CHECKS=1;
