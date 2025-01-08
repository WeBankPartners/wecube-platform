CREATE TABLE `trans_export_customer`
(
    `id`            varchar(64) COLLATE utf8_bin  NOT NULL,
    `name`          varchar(200) COLLATE utf8_bin NOT NULL COMMENT '客户名称',
    `nexus_addr`    varchar(100) COLLATE utf8_bin NOT NULL COMMENT 'nexus地址',
    `nexus_account` varchar(50) COLLATE utf8_bin  NOT NULL COMMENT 'nexus账号',
    `nexus_pwd`     varchar(20) COLLATE utf8_bin  NOT NULL COMMENT 'nexus密码',
    `nexus_repo`    varchar(50) COLLATE utf8_bin  NOT NULL COMMENT 'nexus仓库名',
    `created_user`  varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '创建人',
    `created_time`  datetime                      NOT NULL,
    `updated_time`  datetime                      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '导出目标客户信息表';

alter table trans_export
    add column customer_name varchar(200) default null comment '客户名称';