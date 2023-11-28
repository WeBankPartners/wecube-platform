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
