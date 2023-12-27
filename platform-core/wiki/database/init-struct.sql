CREATE TABLE `system_variables` (
    `id` varchar(64)  NOT NULL COMMENT '唯一标识',
    `package_name` varchar(64) DEFAULT NULL COMMENT '包名',
    `name` varchar(255) NOT NULL COMMENT '变量名',
    `value` text DEFAULT NULL COMMENT '变量值',
    `default_value` text DEFAULT NULL COMMENT '默认值',
    `scope` varchar(64) NOT NULL DEFAULT 'global' COMMENT '作用范围',
    `source` varchar(96) DEFAULT 'system' COMMENT '来源',
    `status` varchar(32) DEFAULT 'active' COMMENT '状态',
    PRIMARY KEY (`id`)
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
   `status` tinyint(1)  DEFAULT NULL COMMENT '状态,是否启用',
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
     `status` tinyint(4)  DEFAULT NULL COMMENT '状态',
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
      KEY (`menu_order`),
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `role_menu` (
     `id` varchar(128) NOT NULL COMMENT '唯一标识',
     `role_name` varchar(64) NOT NULL COMMENT '角色',
     `menu_code` varchar(64) NOT NULL COMMENT '菜单编码',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `plugin_packages` (
   `id` varchar(64) NOT NULL COMMENT '唯一标识',
   `name` varchar(64) NOT NULL COMMENT '显示名',
   `version` varchar(32) NOT NULL COMMENT '版本',
   `status` varchar(32) NOT NULL DEFAULT 'UNREGISTERED' COMMENT '状态->UNREGISTERED(已上传未注册态)|REGISTERED(注册态)|DECOMMISSIONED(注销态)',
   `upload_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
   `ui_package_included` bit(1) DEFAULT b'0' COMMENT '是否有ui->0(无)|1(有)',
   `edition` varchar(32) NOT NULL DEFAULT 'community' COMMENT '发行版本->community(社区版)|enterprise(企业版)',
   KEY `k_plugin_packages_status`(`status`),
   PRIMARY KEY (`id`)
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
      `plugin_package_id` varchar(64) DEFAULT NULL COMMENT '插件',
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
    KEY `plugin_package_menu_order` (`menu_order`),
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
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间-新',
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
     `is_array` bit(1) DEFAULT b'0' COMMENT '是否数组-新',
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
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;