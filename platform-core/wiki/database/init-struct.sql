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
      UNIQUE KEY `uk_code` (`code`),
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
   `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态->0(UNREGISTERED已上传未注册态)|1(REGISTERED注册态)|2(DECOMMISSIONED注销态)',
   `upload_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
   `ui_package_included` tinyint(1) DEFAULT 0 COMMENT '是否有ui->0(无)|1(有)',
   `edition` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发行版本->0(community社区版)|1(enterprise企业版)',
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
      `status` tinyint(1) DEFAULT NULL COMMENT '状态->0(inactive)|1(active)',
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
    `display_name` varchar(255) NOT NULL COMMENT '英文显示名',
    `local_display_name` varchar(255) NOT NULL COMMENT '本地语言显示名',
    `menu_order` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单排序',
    `path` varchar(255) NOT NULL COMMENT '前端请求路径',
    `active` tinyint(1) DEFAULT 0 COMMENT '是否启用->0(不启用)|1(启用)',
    PRIMARY KEY (`id`),
    KEY `plugin_package_menu_order` (`menu_order`),
    CONSTRAINT `fk_plugin_menus_package` FOREIGN KEY (`plugin_package_id`) REFERENCES `plugin_packages` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;