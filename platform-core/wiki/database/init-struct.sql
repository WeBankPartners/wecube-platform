CREATE TABLE `system_variables` (
    `id` varchar(64)  NOT NULL COMMENT '唯一标识',
    `package_name` varchar(64) DEFAULT NULL COMMENT '包名',
    `name` varchar(128) NOT NULL COMMENT '变量名',
    `value` text DEFAULT NULL COMMENT '变量值',
    `default_value` text DEFAULT NULL COMMENT '默认值',
    `scope` varchar(64) NOT NULL DEFAULT 'global' COMMENT '作用范围',
    `source` varchar(96) DEFAULT 'system' COMMENT '来源',
    `status` varchar(32) DEFAULT 'active' COMMENT '状态',
    PRIMARY KEY (`id`),
    INDEX idx_sv_source(`source`),
    INDEX idx_sv_name(`name`),
    INDEX idx_sv_status(`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `resource_server` (
   `id` varchar(64)  NOT NULL COMMENT '唯一标识',
   `created_by` varchar(64)  DEFAULT NULL COMMENT '创建人',
   `created_date` datetime DEFAULT NULL COMMENT '创建时间',
   `host` varchar(64)  DEFAULT NULL COMMENT '主机',
   `is_allocated` tinyint(1) DEFAULT NULL COMMENT '是否分配',
   `login_password` varchar(255) COMMENT '连接密码',
   `login_username` varchar(64) DEFAULT NULL COMMENT '连接用户名',
   `name` varchar(255)  DEFAULT NULL COMMENT '名称',
   `port` varchar(32)  DEFAULT NULL COMMENT '端口',
   `purpose` varchar(255)  DEFAULT NULL COMMENT '描述',
   `status` varchar(32)  DEFAULT 'inactive' COMMENT '状态,是否启用->inactive | active',
   `type` varchar(32)  DEFAULT NULL COMMENT '资源类型(docker,mysql,s3)',
   `updated_by` varchar(64)  DEFAULT NULL COMMENT '更新人',
   `updated_date` datetime DEFAULT NULL COMMENT '更新时间',
   `login_mode` varchar(32)  NOT NULL DEFAULT 'PASSWD' COMMENT '登录模式',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `resource_item` (
     `id` varchar(64)  NOT NULL COMMENT '唯一标识',
     `resource_server_id` varchar(64)  DEFAULT NULL COMMENT '关联资源',
     `additional_properties` text  COMMENT '连接参数',
     `created_by` varchar(64)  DEFAULT NULL COMMENT '创建人',
     `created_date` datetime DEFAULT NULL COMMENT '创建时间',
     `is_allocated` tinyint(1) DEFAULT NULL COMMENT '是否分配',
     `name` varchar(255)  DEFAULT NULL COMMENT '名称',
     `purpose` varchar(255)  DEFAULT NULL COMMENT '描述',
     `status` varchar(32)  DEFAULT NULL COMMENT '状态',
     `type` varchar(64)  DEFAULT NULL COMMENT '类型',
     `updated_by` varchar(64)  DEFAULT NULL COMMENT '更新人',
     `updated_date` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     CONSTRAINT `fk_resource_item_server` FOREIGN KEY (`resource_server_id`) REFERENCES `resource_server` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `menu_items` (
      `id` varchar(128) NOT NULL COMMENT '唯一标识',
      `parent_code` varchar(64) DEFAULT NULL COMMENT '所属菜单栏',
      `code` varchar(64) NOT NULL COMMENT '编码',
      `source` varchar(255) NOT NULL COMMENT '来源',
      `description` varchar(255) DEFAULT NULL COMMENT '描述',
      `local_display_name` varchar(255) DEFAULT NULL COMMENT '显示名',
      `menu_order` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单排序',
      INDEX (`menu_order`),
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `role_menu` (
     `id` varchar(128) NOT NULL COMMENT '唯一标识',
     `role_name` varchar(64) NOT NULL COMMENT '角色',
     `menu_code` varchar(64) NOT NULL COMMENT '菜单编码',
     PRIMARY KEY (`id`),
     INDEX idx_role_menu_role(`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `plugin_packages` (
   `id` varchar(64) NOT NULL COMMENT '唯一标识',
   `name` varchar(64) NOT NULL COMMENT '显示名',
   `version` varchar(32) NOT NULL COMMENT '版本',
   `status` varchar(32) NOT NULL DEFAULT 'UNREGISTERED' COMMENT '状态->UNREGISTERED(已上传未注册态)|REGISTERED(注册态)|DECOMMISSIONED(注销态)',
   `upload_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
   `ui_package_included` bit(1) DEFAULT b'0' COMMENT '是否有ui->0(无)|1(有)',
   `edition` varchar(32) NOT NULL DEFAULT 'community' COMMENT '发行版本->community(社区版)|enterprise(企业版)',
   PRIMARY KEY (`id`),
   INDEX `idx_plugin_packages_status`(`status`),
   INDEX `idx_plugin_packages_name`(`name`),
   INDEX `idx_plugin_packages_version`(`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_instances` (
    `id` varchar(64) NOT NULL COMMENT '唯一标识',
    `host` varchar(64) DEFAULT NULL COMMENT '主机ip',
    `container_name` varchar(128) DEFAULT NULL COMMENT '容器名',
    `port` int(11) DEFAULT NULL COMMENT '服务端口',
    `container_status` varchar(64) DEFAULT NULL COMMENT '容器状态',
    `package_id` varchar(64) DEFAULT NULL COMMENT '插件',
    `docker_instance_resource_id` varchar(64) DEFAULT NULL COMMENT '容器实例id',
    `instance_name` varchar(64) DEFAULT NULL COMMENT '容器实例名',
    `plugin_mysql_instance_resource_id` varchar(64) DEFAULT NULL COMMENT '数据库实例id',
    `s3bucket_resource_id` varchar(64) DEFAULT NULL COMMENT 's3资源id',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_plugin_instances_s3` FOREIGN KEY (`s3bucket_resource_id`) REFERENCES `resource_item` (`id`),
    CONSTRAINT `fk_plugin_instances_package` FOREIGN KEY (`package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_runtime_resources_docker` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
       `image_name` varchar(128) NOT NULL COMMENT '镜像名',
       `container_name` varchar(128) NOT NULL COMMENT '容器名',
       `port_bindings` varchar(255) NOT NULL COMMENT '端口信息',
       `volume_bindings` varchar(1024) NOT NULL COMMENT '目录映射',
       `env_variables` text DEFAULT NULL COMMENT '容器环境变量',
       PRIMARY KEY (`id`),
       CONSTRAINT `fk_plugin_rrd_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_runtime_resources_mysql` (
      `id` varchar(64) NOT NULL COMMENT '唯一标识',
      `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
      `schema_name` varchar(128) NOT NULL COMMENT '数据库名',
      `init_file_name` varchar(255) DEFAULT NULL COMMENT '初始化脚本',
      `upgrade_file_name` varchar(255) DEFAULT NULL COMMENT '升级脚本',
      PRIMARY KEY (`id`),
      CONSTRAINT `fk_plugin_rrm_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_runtime_resources_s3` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
       `bucket_name` varchar(255) NOT NULL COMMENT '桶名',
       `additional_properties` text DEFAULT NULL COMMENT '自动上传文件',
       PRIMARY KEY (`id`),
       CONSTRAINT `fk_plugin_rrs_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_mysql_instances` (
      `id` varchar(64) NOT NULL COMMENT '唯一标识',
      `password` varchar(255) DEFAULT NULL COMMENT '密码',
      `plugun_package_id` varchar(64) DEFAULT NULL COMMENT '插件',
      `plugin_package_id` varchar(64) DEFAULT NULL COMMENT '插件-新',
      `resource_item_id` varchar(64) DEFAULT NULL COMMENT '资源实例id',
      `schema_name` varchar(64) DEFAULT NULL COMMENT '数据库名',
      `status` varchar(32) DEFAULT 'inactive' COMMENT '状态->inactive(未启用)|active(启用)',
      `username` varchar(255) DEFAULT NULL COMMENT '用户名',
      `pre_version` varchar(64) DEFAULT NULL COMMENT '插件版本',
      `created_time` datetime DEFAULT NULL COMMENT '创建时间',
      `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
      PRIMARY KEY (`id`),
      CONSTRAINT `fk_plugin_mysql_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`),
      CONSTRAINT `fk_plugin_mysql_resource` FOREIGN KEY (`resource_item_id`) REFERENCES `resource_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_authorities` (
      `id` varchar(64) NOT NULL COMMENT '唯一标识',
      `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
      `role_name` varchar(64) NOT NULL COMMENT '角色',
      `menu_code` varchar(64) NOT NULL COMMENT '菜单编码',
      PRIMARY KEY (`id`),
      CONSTRAINT `fx_plugin_auth_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_dependencies` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
       `dependency_package_name` varchar(64) NOT NULL COMMENT '依赖包名',
       `dependency_package_version` varchar(32) NOT NULL COMMENT '依赖包版本',
       PRIMARY KEY (`id`),
       CONSTRAINT `fk_plugin_dependencies_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_resource_files` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
     `package_name` varchar(64) NOT NULL COMMENT '插件包名',
     `package_version` varchar(32) NOT NULL COMMENT '插件版本',
     `source` varchar(64) NOT NULL COMMENT '压缩文件',
     `related_path` varchar(1024) NOT NULL COMMENT '静态文件路径',
     PRIMARY KEY (`id`),
     CONSTRAINT `fk_plugin_resource_file_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_menus` (
    `id` varchar(64) NOT NULL COMMENT '唯一标识',
    `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
    `code` varchar(64) NOT NULL COMMENT '编码',
    `category` varchar(64) NOT NULL COMMENT '目录',
    `source` varchar(255) DEFAULT NULL COMMENT '来源',
    `display_name` varchar(255) NOT NULL COMMENT '英文显示名',
    `local_display_name` varchar(255) NOT NULL COMMENT '本地语言显示名',
    `menu_order` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单排序',
    `path` varchar(255) NOT NULL COMMENT '前端请求路径',
    `active` bit(1) DEFAULT b'0' COMMENT '是否启用->0(未启用)|1(启用)',
    PRIMARY KEY (`id`),
    INDEX `idx_plugin_package_menu_order` (`menu_order`),
    CONSTRAINT `fk_plugin_menus_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_configs` (
      `id` varchar(64) NOT NULL COMMENT '唯一标识',
      `plugin_package_id` varchar(64) NOT NULL COMMENT '插件',
      `name` varchar(255) NOT NULL COMMENT '服务类型名称',
      `target_package` varchar(64) DEFAULT NULL COMMENT '目标类型包',
      `target_entity` varchar(255) DEFAULT NULL COMMENT '目标类型项',
      `target_entity_filter_rule` varchar(2048) DEFAULT NULL COMMENT '目标类型过滤规则',
      `register_name` varchar(255) DEFAULT NULL COMMENT '服务注册名',
      `status` varchar(32) NOT NULL DEFAULT 'DISABLED' COMMENT '状态',
      PRIMARY KEY (`id`),
      CONSTRAINT `fk_plugin_config_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_config_interfaces` (
        `id` varchar(64) NOT NULL COMMENT '唯一标识',
        `plugin_config_id` varchar(64) NOT NULL COMMENT '插件服务',
        `action` varchar(255) NOT NULL COMMENT '接口',
        `service_name` varchar(255) NOT NULL COMMENT '服务名',
        `service_display_name` varchar(255) NOT NULL COMMENT '服务显示名',
        `path` varchar(255) NOT NULL COMMENT '插件接口uri',
        `http_method` varchar(32) NOT NULL COMMENT 'http请求方法',
        `is_async_processing` varchar(16) DEFAULT 'N' COMMENT '是否同步->Y(是) | N(否)',
        `type` varchar(32) DEFAULT 'EXECUTION' COMMENT '服务类型->APPROVAL(审批),EXECUTION(执行),DYNAMICFORM(动态表单)',
        `filter_rule` varchar(2048) DEFAULT NULL COMMENT '服务过滤规则',
        `description` varchar(255) DEFAULT NULL COMMENT '描述',
        PRIMARY KEY (`id`),
        CONSTRAINT `fk_plugin_interface_config` FOREIGN KEY (`plugin_config_id`) REFERENCES `plugin_configs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_config_interface_parameters` (
      `id` varchar(64) NOT NULL COMMENT '唯一标识',
      `plugin_config_interface_id` varchar(64) NOT NULL COMMENT '服务接口',
      `type` varchar(32) NOT NULL COMMENT '类型->INPUT(输入),OUTPUT(输出)',
      `name` varchar(255) NOT NULL COMMENT '接口属性名',
      `data_type` varchar(64) NOT NULL COMMENT '属性数据类型',
      `mapping_type` varchar(64) DEFAULT NULL COMMENT '数据来源',
      `mapping_entity_expression` varchar(2048) DEFAULT NULL COMMENT 'entity表达式',
      `mapping_system_variable_name` varchar(255) DEFAULT NULL COMMENT '系统参数',
      `required` varchar(16) DEFAULT 'N' COMMENT '是否必填->Y(是) | N(否)',
      `sensitive_data` varchar(16) DEFAULT 'N' COMMENT '是否敏感->Y(是) | N(否)',
      `description` varchar(255) DEFAULT NULL COMMENT '描述',
      `mapping_val` varchar(255) DEFAULT NULL COMMENT '静态值',
      `multiple` varchar(16) DEFAULT 'N' COMMENT '是否数组->Y(是) | N(否)',
      `ref_object_name` varchar(64) DEFAULT NULL COMMENT '关联对象名',
      PRIMARY KEY (`id`),
      CONSTRAINT `fk_plugin_param_interface` FOREIGN KEY (`plugin_config_interface_id`) REFERENCES `plugin_config_interfaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_data_model` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `version` int(11) NOT NULL DEFAULT 0 COMMENT '版本',
     `package_name` varchar(64) NOT NULL COMMENT '包名',
     `is_dynamic` bit(1) DEFAULT b'0' COMMENT '是否动态',
     `update_path` varchar(255) DEFAULT '/data-model' COMMENT '请求路径',
     `update_method` varchar(32) DEFAULT 'GET' COMMENT '请求方法',
     `update_source` varchar(32) DEFAULT 'PLUGIN_PACKAGE' COMMENT '来源',
     `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_plugin_package_data_model` (`package_name`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_entities` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `data_model_id` varchar(64) NOT NULL COMMENT '所属数据模型',
       `data_model_version` int(11) NOT NULL COMMENT '版本',
       `package_name` varchar(64) NOT NULL COMMENT '包名',
       `name` varchar(128) NOT NULL COMMENT '模型名',
       `display_name` varchar(255) NOT NULL COMMENT '显示名',
       `description` varchar(255) DEFAULT NULL COMMENT '描述',
       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_plugin_entity_model_name` (`data_model_id`,`name`),
       CONSTRAINT `fk_plugin_entity_model` FOREIGN KEY (`data_model_id`) REFERENCES `plugin_package_data_model` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_package_attributes` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `entity_id` varchar(64) NOT NULL COMMENT '所属数据模型CI项',
     `reference_id` varchar(64) DEFAULT NULL COMMENT '关联数据模型',
     `name` varchar(128) NOT NULL COMMENT '属性名',
     `description` varchar(255) DEFAULT NULL COMMENT '描述',
     `data_type` varchar(64) NOT NULL COMMENT '属性数据类型',
     `ref_package` varchar(64) DEFAULT NULL COMMENT '关联包',
     `ref_entity` varchar(64) DEFAULT NULL COMMENT '关联CI项',
     `ref_attr` varchar(64) DEFAULT NULL COMMENT '关联属性',
     `mandatory` bit(1) DEFAULT b'0' COMMENT '是否必填',
     `multiple` varchar(6) DEFAULT 'N' COMMENT '是否数组->Y(是) | N(否)',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `order_no` int(11) DEFAULT 0 COMMENT '排序',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_plugin_attr_entity_name` (`entity_id`,`name`),
     CONSTRAINT `fk_plugin_attr_entity` FOREIGN KEY (`entity_id`) REFERENCES `plugin_package_entities` (`id`),
     CONSTRAINT `fk_plugin_attr_ref` FOREIGN KEY (`reference_id`) REFERENCES `plugin_package_attributes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_config_roles` (
   `id` varchar(64) NOT NULL COMMENT '唯一标识,crc(perm_type,plugin_cfg_id,role_id)',
   `is_active` bit(1) DEFAULT NULL COMMENT '是否启用',
   `perm_type` varchar(32) DEFAULT NULL COMMENT '权限类型->USE(使用) | MGMT(管理)',
   `plugin_cfg_id` varchar(64) DEFAULT NULL COMMENT '服务配置id',
   `role_id` varchar(64) DEFAULT NULL COMMENT '角色id',
   `role_name` varchar(255) DEFAULT NULL COMMENT '角色名称',
   `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
   `created_time` datetime DEFAULT NULL COMMENT '创建时间',
   `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
   `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`),
   INDEX `idx_plugin_config_roles_cfg` (`plugin_cfg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `plugin_certification` (
        `id` VARCHAR(60) NOT NULL,
        `created_by` VARCHAR(45) NULL,
        `created_time` DATETIME NULL,
        `updated_by` VARCHAR(45) NULL,
        `updated_time` DATETIME NULL,
        `plugin` VARCHAR(45) NULL,
        `lpk` TEXT NULL DEFAULT NULL,
        `encrypt_data` TEXT NULL DEFAULT NULL,
        `signature` TEXT NULL DEFAULT NULL,
        `description` TEXT NULL DEFAULT NULL,
        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO menu_items (id,parent_code,code,source,description,local_display_name) VALUES
        ('JOBS',NULL,'JOBS','SYSTEM','','任务'),
        ('DESIGNING',NULL,'DESIGNING','SYSTEM','','设计'),
        ('IMPLEMENTATION',NULL,'IMPLEMENTATION','SYSTEM','','执行'),
        ('MONITORING',NULL,'MONITORING','SYSTEM','','监测'),
        ('INTELLIGENCE_OPS',NULL,'INTELLIGENCE_OPS','SYSTEM','','智慧'),
        ('ADJUSTMENT',NULL,'ADJUSTMENT','SYSTEM','','调整'),
        ('COLLABORATION',NULL,'COLLABORATION','SYSTEM','','协同'),
        ('ADMIN',NULL,'ADMIN','SYSTEM','','系统'),
        ('IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM','','任务编排执行'),
        ('COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM','','插件注册'),
        ('COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM','','任务编排'),
        ('ADMIN__ADMIN_SYSTEM_PARAMS','ADMIN','ADMIN_SYSTEM_PARAMS','SYSTEM','','系统参数'),
        ('ADMIN__ADMIN_RESOURCES_MANAGEMENT','ADMIN','ADMIN_RESOURCES_MANAGEMENT','SYSTEM','','资源管理'),
        ('ADMIN__ADMIN_USER_ROLE_MANAGEMENT','ADMIN','ADMIN_USER_ROLE_MANAGEMENT','SYSTEM','','用户管理'),
        ('IMPLEMENTATION__IMPLEMENTATION_BATCH_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_BATCH_EXECUTION','SYSTEM','','批量执行'),
        ('ADMIN__ADMIN_SYSTEM_DATA_MODEL','ADMIN','ADMIN_SYSTEM_DATA_MODEL','SYSTEM','','系统数据模型'),
        ('ADMIN_CERTIFICATION','ADMIN','ADMIN_CERTIFICATION','SYSTEM',NULL,'授权管理'),
        ('ADMIN_SYSTEM_WORKFLOW_REPORT','ADMIN','ADMIN_SYSTEM_WORKFLOW_REPORT','SYSTEM','','编排执行报表');

INSERT INTO role_menu (id,role_name,menu_code) VALUES
       ('6143aeb980f34e58ee7a5','SUPER_ADMIN','IMPLEMENTATION_BATCH_EXECUTION'),
       ('6143aeb983b85c9cb07fa','SUPER_ADMIN','IMPLEMENTATION_WORKFLOW_EXECUTION'),
       ('6143aec03f3c60133e6bb','SUPER_ADMIN','COLLABORATION_WORKFLOW_ORCHESTRATION'),
       ('6143aec040f392aeed8b7','SUPER_ADMIN','COLLABORATION_PLUGIN_MANAGEMENT'),
       ('6143aec181e9f6843e5ae','SUPER_ADMIN','ADMIN_SYSTEM_DATA_MODEL'),
       ('6143aec184a2eabe555f3','SUPER_ADMIN','ADMIN_SYSTEM_WORKFLOW_REPORT'),
       ('6143aec1906568877089d','SUPER_ADMIN','ADMIN_RESOURCES_MANAGEMENT'),
       ('6143aec191515f2094760','SUPER_ADMIN','ADMIN_USER_ROLE_MANAGEMENT'),
       ('6143aec19238d0d3647bb','SUPER_ADMIN','ADMIN_SYSTEM_PARAMS'),
       ('6143aec193f9004af05fc','SUPER_ADMIN','ADMIN_CERTIFICATION');

INSERT INTO system_variables (id,package_name,name,value,default_value,`scope`,source,status) VALUES
      ('system__global__PLATFORM_MAIL_SENDER','','PLATFORM_MAIL_SENDER','','','global','system','active'),
      ('system__global__PLATFORM_MAIL_SERVER','','PLATFORM_MAIL_SERVER','','','global','system','active'),
      ('system__global__PLATFORM_MAIL_PWD','','PLATFORM_MAIL_PWD','','','','system','active'),
      ('system__global__PLATFORM_MAIL_SSL','','PLATFORM_MAIL_SSL','','false','global','system','active'),
      ('system__global__BASE_MOUNT_PATH',NULL,'BASE_MOUNT_PATH',NULL,'/data','global','system','active'),
      ('system__global__CALLBACK_URL',NULL,'CALLBACK_URL',NULL,'/platform/v1/process/instances/callback','global','system','active'),
      ('system__global__ENCRYPT_SEED',NULL,'ENCRYPT_SEED','','seed-wecube2.1-2020','global','system','active'),
      ('system__global__GATEWAY_URL',NULL,'GATEWAY_URL',NULL,'http://127.0.0.1:8005','global','system','active'),
      ('system__global__HTTPS_PROXY',NULL,'HTTPS_PROXY',NULL,'','global','system','active'),
      ('system__global__HTTP_PROXY',NULL,'HTTP_PROXY',NULL,'','global','system','active'),
      ('system__global__PLUGIN_ARTIFACTS_RELEASE_URL',NULL,'PLUGIN_ARTIFACTS_RELEASE_URL',NULL,'','global','system','active'),
      ('system__global__S3_ACCESS_KEY',NULL,'S3_ACCESS_KEY','','access_key','global','system','active'),
      ('system__global__S3_SECRET_KEY',NULL,'S3_SECRET_KEY','','secret_key','global','system','active'),
      ('system__global__S3_SERVER_URL',NULL,'S3_SERVER_URL','','http://127.0.0.1:9000','global','system','active'),
      ('system__global__UM_AUTH_CONTEXT',NULL,'UM_AUTH_CONTEXT','','protocol=http;host=127.0.0.1;port=18080;appid=;appname=;','global','system','inactive');


delete from plugin_config_roles where created_by = 'bootScript';

insert into plugin_config_roles
(id,created_by,created_time,is_active,perm_type,plugin_cfg_id,role_id,role_name)
select uuid() as id, 'bootScript' as created_by, sysdate(), 1 as is_active, 'MGMT' as perm_type, t.id as plugin_cfg_id, '2c9280827019695c017019ac974f001c' as role_id, 'SUPER_ADMIN' as role_name
from plugin_configs t
where t.register_name is not null;

insert into plugin_config_roles
(id,created_by,created_time,is_active,perm_type,plugin_cfg_id,role_id,role_name)
select uuid() as id, 'bootScript' as created_by, sysdate(), 1 as is_active, 'USE' as perm_type, t.id as plugin_cfg_id, '2c9280827019695c017019ac974f001c' as role_id, 'SUPER_ADMIN' as role_name
from plugin_configs t
where t.register_name is not null;

CREATE TABLE `core_operation_event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `created_by` varchar(255) DEFAULT NULL,
    `created_time` datetime DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `updated_time` datetime DEFAULT NULL,
    `event_seq_no` varchar(128) DEFAULT NULL,
    `event_type` varchar(255) DEFAULT NULL,
    `is_notified` bit(1) DEFAULT NULL,
    `notify_endpoint` varchar(255) DEFAULT NULL,
    `is_notify_required` bit(1) DEFAULT NULL,
    `oper_data` varchar(255) DEFAULT NULL,
    `oper_key` varchar(255) DEFAULT NULL,
    `oper_user` varchar(255) DEFAULT NULL,
    `proc_def_id` varchar(255) DEFAULT NULL,
    `proc_inst_id` varchar(255) DEFAULT NULL,
    `src_sub_system` varchar(255) DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `priority` int(11) DEFAULT NULL,
    `proc_inst_key` varchar(255) DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `rev` int(11) DEFAULT '0',
    `oper_mode` varchar(45) DEFAULT 'defer',
    PRIMARY KEY (`id`),
    KEY `idx_core_operation_event_seq_no_1` (`event_seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;