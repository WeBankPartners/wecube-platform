CREATE TABLE `trans_export`
(
    `id`                varchar(64) COLLATE utf8_bin   NOT NULL,
    `business`          varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务',
    `business_name`     varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务名称',
    `environment`       varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境',
    `environment_name`  varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境名称',
    `status`            varchar(32) COLLATE utf8_bin   NOT NULL COMMENT '状态: start开始,doing执行中,success成功,fail失败',
    `output_url`        varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '输出nexus地址',
    `created_user`      varchar(45) COLLATE utf8_bin   DEFAULT NULL COMMENT '创建人',
    `created_time`      datetime                       NOT NULL,
    `updated_user`      varchar(45) COLLATE utf8_bin   DEFAULT NULL,
    `updated_time`      datetime                       DEFAULT NULL,
    `last_confirm_time` datetime                       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导出记录表';

CREATE TABLE `trans_export_analyze_data`
(
    `id`             varchar(64) COLLATE utf8_bin NOT NULL,
    `trans_export`   varchar(64) COLLATE utf8_bin NOT NULL COMMENT '导出记录id',
    `source`         varchar(32) COLLATE utf8_bin NOT NULL COMMENT '数据来源类型:wecmdb,monitor,artifacts',
    `data_type`      varchar(64) COLLATE utf8_bin NOT NULL COMMENT '关联分析数据',
    `data_type_name` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '数据类型显示名',
    `data`           mediumtext COLLATE utf8_bin null COMMENT '输入',
    `data_len`       int(11) COLLATE utf8_bin default 0 COMMENT '数据总数',
    `error_msg`      text COLLATE utf8_bin         DEFAULT NULL COMMENT '导出报错信息',
    `start_time`     datetime                      default NULL COMMENT '开始时间',
    `end_time`       datetime                      default NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `trans_export_analyze_data_force_trans_export` FOREIGN KEY (`trans_export`) REFERENCES `trans_export` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导出记录表';


CREATE TABLE `trans_export_detail`
(
    `id`                  varchar(64) COLLATE utf8_bin NOT NULL,
    `trans_export`        varchar(64) COLLATE utf8_bin NOT NULL COMMENT '导出记录id',
    `name`                varchar(64) COLLATE utf8_bin NOT NULL COMMENT '名称',
    `analyze_data_source` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '关联分析数据',
    `step`                tinyint(2) COLLATE utf8_bin NOT NULL COMMENT '第几步',
    `status`              varchar(32) COLLATE utf8_bin NOT NULL COMMENT '导出状态: notStart未开始,success成功,fail失败',
    `input`               text COLLATE utf8_bin        DEFAULT NULL COMMENT '输入',
    `output`              longtext COLLATE utf8_bin    DEFAULT NULL COMMENT '输出',
    `error_msg`           text COLLATE utf8_bin        DEFAULT NULL COMMENT '导出报错信息',
    `start_time`          datetime                     default NULL COMMENT '开始时间',
    `end_time`            datetime                     default NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `trans_export_detail_force_trans_export` FOREIGN KEY (`trans_export`) REFERENCES `trans_export` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导出记录表';

INSERT INTO menu_items (id, parent_code, code, source, description, local_display_name)
VALUES ('ADMIN__ADMIN_BASE_MIGRATION', 'ADMIN', 'ADMIN_BASE_MIGRATION', 'SYSTEM', '', '底座迁移');

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_BUSINESS', '', 'PLATFORM_EXPORT_CI_BUSINESS', '', 'business_product',
        'global', 'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_ENV', '', 'PLATFORM_EXPORT_CI_ENV', '', 'deploy_environment', 'global',
        'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_NEXUS_URL', '', 'PLATFORM_EXPORT_NEXUS_URL', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_NEXUS_USER', '', 'PLATFORM_EXPORT_NEXUS_USER', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_NEXUS_PWD', '', 'PLATFORM_EXPORT_NEXUS_PWD', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_NEXUS_REPO', '', 'PLATFORM_EXPORT_NEXUS_REPO', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_ARTIFACT_INSTANCE', '', 'PLATFORM_EXPORT_CI_ARTIFACT_INSTANCE', '',
        'app_instance,rdb_instance', 'global', 'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_ARTIFACT_PACKAGE', '', 'PLATFORM_EXPORT_CI_ARTIFACT_PACKAGE', '',
        'deploy_package', 'global', 'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_SYSTEM', '', 'PLATFORM_EXPORT_CI_SYSTEM', 'system', '', 'global',
        'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_TECH_PRODUCT', '', 'PLATFORM_EXPORT_CI_TECH_PRODUCT', 'application_domain', '',
        'global', 'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_CI_ARTIFACT_UNIT_DESIGN', '', 'PLATFORM_EXPORT_CI_ARTIFACT_UNIT_DESIGN', '',
        'unit_design', 'global', 'system', 'active');

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_IMPORT_NEXUS_URL', '', 'PLATFORM_IMPORT_NEXUS_URL', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_IMPORT_NEXUS_USER', '', 'PLATFORM_IMPORT_NEXUS_USER', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_IMPORT_NEXUS_PWD', '', 'PLATFORM_IMPORT_NEXUS_PWD', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_IMPORT_NEXUS_REPO', '', 'PLATFORM_IMPORT_NEXUS_REPO', '', '', 'global', 'system',
        'active');


CREATE TABLE `trans_import`
(
    `id`                  varchar(64) COLLATE utf8_bin   NOT NULL,
    `input_url`           varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '输入nexus地址',
    `business`            varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务',
    `business_name`       varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务名称',
    `environment`         varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境',
    `environment_name`    varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境名称',
    `status`              varchar(32) COLLATE utf8_bin   NOT NULL COMMENT '状态: start开始,doing执行中,success成功,fail失败,exit 终止,suspend暂停',
    `association_system`  varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '关联系统',
    `association_product` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '关联产品',
    `created_user`        varchar(45) COLLATE utf8_bin   DEFAULT NULL COMMENT '创建人',
    `created_time`        datetime                       NOT NULL,
    `updated_user`        varchar(45) COLLATE utf8_bin   DEFAULT NULL,
    `updated_time`        datetime                       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导入记录表';

CREATE TABLE `trans_import_action`
(
    `id`                  varchar(64) COLLATE utf8_bin NOT NULL,
    `trans_import`        varchar(64) COLLATE utf8_bin NOT NULL COMMENT '导入记录表',
    `trans_import_detail` varchar(64) COLLATE utf8_bin NULL COMMENT '记录详情表',
    `action`              varchar(64) COLLATE utf8_bin NOT NULL COMMENT '执行操作: suspend暂停,restore 恢复,retry 重试,cancel取消',
    `error_msg`           text COLLATE utf8_bin NULL COMMENT '报错信息',
    `created_user`        varchar(45) COLLATE utf8_bin DEFAULT NULL,
    `updated_time`        datetime                     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导入操作记录表';

CREATE TABLE `trans_import_detail`
(
    `id`           varchar(64) COLLATE utf8_bin NOT NULL,
    `trans_import` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '导入记录id',
    `name`         varchar(64) COLLATE utf8_bin NOT NULL COMMENT '名称',
    `step`         tinyint(2) COLLATE utf8_bin NOT NULL COMMENT '第几步',
    `status`       varchar(32) COLLATE utf8_bin NOT NULL COMMENT '导出状态: notStart未开始,doing执行中,success成功,fail失败',
    `input`        longtext COLLATE utf8_bin DEFAULT NULL COMMENT '输入',
    `output`       longtext COLLATE utf8_bin DEFAULT NULL COMMENT '输出',
    `error_msg`    text COLLATE utf8_bin     DEFAULT NULL COMMENT '导出报错信息',
    `start_time`   datetime                  default NULL COMMENT '开始时间',
    `end_time`     datetime                  default NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `trans_import_detail_force_trans_import` FOREIGN KEY (`trans_import`) REFERENCES `trans_import` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移导入记录详情表';

CREATE TABLE `trans_import_proc_exec`
(
    `id`                  varchar(64) COLLATE utf8_bin  NOT NULL,
    `trans_import_detail` varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '记录详情表',
    `proc_ins`            varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '编排实例ID',
    `proc_def`            varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '编排定义ID',
    `proc_def_key`        varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '编排定义KEY',
    `proc_def_name`       varchar(255) COLLATE utf8_bin NOT NULL COMMENT '编排定义名称',
    `root_entity`         varchar(255) COLLATE utf8_bin NOT NULL COMMENT '根表达式',
    `entity_data_id`      varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '根数据ID',
    `entity_data_name`    varchar(255) COLLATE utf8_bin NOT NULL COMMENT '根数据名称',
    `exec_order`          tinyint(2) COLLATE utf8_bin DEFAULT 0 COMMENT '第几步',
    `status`              varchar(32) COLLATE utf8_bin  NOT NULL COMMENT '导出状态: notStart未开始,doing执行中,success成功,fail失败',
    `input`               text COLLATE utf8_bin        DEFAULT NULL COMMENT '输入',
    `output`              longtext COLLATE utf8_bin    DEFAULT NULL COMMENT '输出',
    `error_msg`           text COLLATE utf8_bin        DEFAULT NULL COMMENT '导出报错信息',
    `start_time`          datetime                     default NULL COMMENT '开始时间',
    `end_time`            datetime                     default NULL COMMENT '结束时间',
    `created_user`        varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '创建人',
    `created_time`        datetime                      NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `trans_import_proc_exec_force_detail` FOREIGN KEY (`trans_import_detail`) REFERENCES `trans_import_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '数据迁移执行记录详情表';

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_BUSINESS_EXPR', '', 'PLATFORM_EXPORT_BUSINESS_EXPR', '', '', 'global',
        'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_ENV_EXPR', '', 'PLATFORM_EXPORT_ENV_EXPR', '', '', 'global', 'system',
        'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_BACKWARD_ATTR_LIST', '', 'PLATFORM_EXPORT_BACKWARD_ATTR_LIST', '', '',
        'global', 'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_IGNORE_ATTR_LIST', '', 'PLATFORM_EXPORT_IGNORE_ATTR_LIST', '', '', 'global',
        'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_EMPTY_ATTR', '', 'PLATFORM_EXPORT_EMPTY_ATTR', '', 'asset_id', 'global',
        'system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status)
VALUES ('system__global__PLATFORM_EXPORT_EXEC_WORKFLOW', '', 'PLATFORM_EXPORT_EXEC_WORKFLOW', '', '', 'global',
        'system', 'active');

INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_NZ_CIDR', '', 'PLATFORM_IMPORT_NZ_CIDR', '', '', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_NSZ_CIDR', '', 'PLATFORM_IMPORT_NSZ_CIDR', '', '', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_RT_CODE', '', 'PLATFORM_IMPORT_RT_CODE', '', 'DEFAULT', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_BSG_NAME', '', 'PLATFORM_IMPORT_BSG_NAME', '', 'VPC_MGMT_APP', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_DC_REGION_NAME', '', 'PLATFORM_IMPORT_DC_REGION_NAME', '', 'GDCR', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_DC_AZ1_NAME', '', 'PLATFORM_IMPORT_DC_AZ1_NAME', '', 'RA1', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_DC_AZ2_NAME', '', 'PLATFORM_IMPORT_DC_AZ2_NAME', '', 'RA2', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_WECUBE_HOST_CODE', '', 'PLATFORM_IMPORT_WECUBE_HOST_CODE', '', 'wecube', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_CI_SYSTEM', '', 'PLATFORM_IMPORT_CI_SYSTEM', '', 'system', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_SYSTEM_DEPLOY_ATTR', '', 'PLATFORM_IMPORT_SYSTEM_DEPLOY_ATTR', '', 'deploy_batch_no', 'global','system', 'active');
INSERT INTO system_variables (id, package_name, name, value, default_value, `scope`, source, status) VALUES ('system__global__PLATFORM_IMPORT_CONFIRM_VIEW_LIST', '', 'PLATFORM_IMPORT_CONFIRM_VIEW_LIST', '', '', 'global','system', 'active');

