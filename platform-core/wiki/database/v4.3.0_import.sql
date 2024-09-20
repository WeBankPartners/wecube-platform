CREATE TABLE `trans_export`
(
    `id`               varchar(64) COLLATE utf8_bin   NOT NULL,
    `business`         varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务',
    `business_name`    varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '业务名称',
    `environment`      varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境',
    `environment_name` varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '环境名称',
    `status`           varchar(32) COLLATE utf8_bin   NOT NULL COMMENT '状态: start开始,doing执行中,success成功,fail失败',
    `output_url`       varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '输出nexus地址',
    `created_user`     varchar(45) COLLATE utf8_bin   DEFAULT NULL COMMENT '创建人',
    `created_time`     datetime                       NOT NULL,
    `updated_user`     varchar(45) COLLATE utf8_bin   DEFAULT NULL,
    `updated_time`     datetime                       DEFAULT NULL,
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
