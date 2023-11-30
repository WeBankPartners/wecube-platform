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