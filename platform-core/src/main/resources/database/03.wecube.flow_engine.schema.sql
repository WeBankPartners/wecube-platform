SET FOREIGN_KEY_CHECKS = 0;
/*Table structure for table `act_ge_bytearray` */

drop table if exists `act_ge_bytearray`;
CREATE TABLE `act_ge_bytearray` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BYTES_` longblob,
  `GENERATED_` tinyint(4) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` int(11) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_BYTEARR_DEPL` (`DEPLOYMENT_ID_`),
  KEY `ACT_IDX_BYTEARRAY_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_BYTEARRAY_RM_TIME` (`REMOVAL_TIME_`),
  KEY `ACT_IDX_BYTEARRAY_NAME` (`NAME_`),
  CONSTRAINT `ACT_FK_BYTEARR_DEPL` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `act_re_deployment` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ge_property` */

drop table if exists `act_ge_property`;
CREATE TABLE `act_ge_property` (
  `NAME_` varchar(64) COLLATE utf8_bin NOT NULL,
  `VALUE_` varchar(300) COLLATE utf8_bin DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  PRIMARY KEY (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ge_schema_log` */

drop table if exists `act_ge_schema_log`;
CREATE TABLE `act_ge_schema_log` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TIMESTAMP_` datetime(3) DEFAULT NULL,
  `VERSION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_actinst` */

drop table if exists `act_hi_actinst`;
CREATE TABLE `act_hi_actinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `PARENT_ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin NOT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CALL_CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `ASSIGNEE_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `ACT_INST_STATE_` int(11) DEFAULT NULL,
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ACTINST_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_START` (`START_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_PROCINST` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_COMP` (`EXECUTION_ID_`,`ACT_ID_`,`END_TIME_`,`ID_`),
  KEY `ACT_IDX_HI_ACT_INST_STATS` (`PROC_DEF_ID_`,`PROC_INST_ID_`,`ACT_ID_`,`END_TIME_`,`ACT_INST_STATE_`),
  KEY `ACT_IDX_HI_ACT_INST_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_AI_PDEFID_END_TIME` (`PROC_DEF_ID_`,`END_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_attachment` */

drop table if exists `act_hi_attachment`;
CREATE TABLE `act_hi_attachment` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `URL_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `CONTENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ATTACHMENT_CONTENT` (`CONTENT_ID_`),
  KEY `ACT_IDX_HI_ATTACHMENT_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_ATTACHMENT_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_ATTACHMENT_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_ATTACHMENT_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_batch` */

drop table if exists `act_hi_batch`;
CREATE TABLE `act_hi_batch` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TOTAL_JOBS_` int(11) DEFAULT NULL,
  `JOBS_PER_SEED_` int(11) DEFAULT NULL,
  `INVOCATIONS_PER_JOB_` int(11) DEFAULT NULL,
  `SEED_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `MONITOR_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BATCH_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_HI_BAT_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_caseactinst` */

drop table if exists `act_hi_caseactinst`;
CREATE TABLE `act_hi_caseactinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `PARENT_ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CASE_ACT_ID_` varchar(255) COLLATE utf8_bin NOT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CALL_CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_ACT_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_ACT_TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `STATE_` int(11) DEFAULT NULL,
  `REQUIRED_` tinyint(1) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_CAS_A_I_CREATE` (`CREATE_TIME_`),
  KEY `ACT_IDX_HI_CAS_A_I_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_CAS_A_I_COMP` (`CASE_ACT_ID_`,`END_TIME_`,`ID_`),
  KEY `ACT_IDX_HI_CAS_A_I_CASEINST` (`CASE_INST_ID_`,`CASE_ACT_ID_`),
  KEY `ACT_IDX_HI_CAS_A_I_TENANT_ID` (`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_caseinst` */

drop table if exists `act_hi_caseinst`;
CREATE TABLE `act_hi_caseinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `CLOSE_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `STATE_` int(11) DEFAULT NULL,
  `CREATE_USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_CASE_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `CASE_INST_ID_` (`CASE_INST_ID_`),
  KEY `ACT_IDX_HI_CAS_I_CLOSE` (`CLOSE_TIME_`),
  KEY `ACT_IDX_HI_CAS_I_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_HI_CAS_I_TENANT_ID` (`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_comment` */

drop table if exists `act_hi_comment`;
CREATE TABLE `act_hi_comment` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACTION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `MESSAGE_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `FULL_MSG_` longblob,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_COMMENT_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_COMMENT_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_COMMENT_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_COMMENT_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_dec_in` */

drop table if exists `act_hi_dec_in`;
CREATE TABLE `act_hi_dec_in` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `DEC_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CLAUSE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CLAUSE_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VAR_TYPE_` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DEC_IN_INST` (`DEC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_IN_CLAUSE` (`DEC_INST_ID_`,`CLAUSE_ID_`),
  KEY `ACT_IDX_HI_DEC_IN_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_IN_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_dec_out` */

drop table if exists `act_hi_dec_out`;
CREATE TABLE `act_hi_dec_out` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `DEC_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `CLAUSE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CLAUSE_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `RULE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `RULE_ORDER_` int(11) DEFAULT NULL,
  `VAR_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VAR_TYPE_` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DEC_OUT_INST` (`DEC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_OUT_RULE` (`RULE_ORDER_`,`CLAUSE_ID_`),
  KEY `ACT_IDX_HI_DEC_OUT_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_OUT_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_decinst` */

drop table if exists `act_hi_decinst`;
CREATE TABLE `act_hi_decinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `DEC_DEF_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `DEC_DEF_KEY_` varchar(255) COLLATE utf8_bin NOT NULL,
  `DEC_DEF_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `EVAL_TIME_` datetime(3) NOT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  `COLLECT_VALUE_` double DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_DEC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DEC_REQ_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DEC_REQ_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DEC_INST_ID` (`DEC_DEF_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_KEY` (`DEC_DEF_KEY_`),
  KEY `ACT_IDX_HI_DEC_INST_PI` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_CI` (`CASE_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_ACT` (`ACT_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_ACT_INST` (`ACT_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_TIME` (`EVAL_TIME_`),
  KEY `ACT_IDX_HI_DEC_INST_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_ROOT_ID` (`ROOT_DEC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_REQ_ID` (`DEC_REQ_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_REQ_KEY` (`DEC_REQ_KEY_`),
  KEY `ACT_IDX_HI_DEC_INST_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DEC_INST_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_detail` */

drop table if exists `act_hi_detail`;
CREATE TABLE `act_hi_detail` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `VAR_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VAR_TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `OPERATION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DETAIL_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_ACT_INST` (`ACT_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_CASE_INST` (`CASE_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_CASE_EXEC` (`CASE_EXECUTION_ID_`),
  KEY `ACT_IDX_HI_DETAIL_TIME` (`TIME_`),
  KEY `ACT_IDX_HI_DETAIL_NAME` (`NAME_`),
  KEY `ACT_IDX_HI_DETAIL_TASK_ID` (`TASK_ID_`),
  KEY `ACT_IDX_HI_DETAIL_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_DETAIL_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_DETAIL_BYTEAR` (`BYTEARRAY_ID_`),
  KEY `ACT_IDX_HI_DETAIL_RM_TIME` (`REMOVAL_TIME_`),
  KEY `ACT_IDX_HI_DETAIL_TASK_BYTEAR` (`BYTEARRAY_ID_`,`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_ext_task_log` */

drop table if exists `act_hi_ext_task_log`;
CREATE TABLE `act_hi_ext_task_log` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TIMESTAMP_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `EXT_TASK_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `TOPIC_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `WORKER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PRIORITY_` bigint(20) NOT NULL DEFAULT '0',
  `ERROR_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `ERROR_DETAILS_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `STATE_` int(11) DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_HI_EXT_TASK_LOG_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_HI_EXT_TASK_LOG_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_HI_EXT_TASK_LOG_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_HI_EXT_TASK_LOG_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_HI_EXT_TASK_LOG_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_EXTTASKLOG_ERRORDET` (`ERROR_DETAILS_ID_`),
  KEY `ACT_HI_EXT_TASK_LOG_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_identitylink` */

drop table if exists `act_hi_identitylink`;
CREATE TABLE `act_hi_identitylink` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TIMESTAMP_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `GROUP_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `OPERATION_TYPE_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ASSIGNER_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_GROUP` (`GROUP_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_IDENT_LINK_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_IDENT_LINK_RM_TIME` (`REMOVAL_TIME_`),
  KEY `ACT_IDX_HI_IDENT_LNK_TIMESTAMP` (`TIMESTAMP_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_incident` */

drop table if exists `act_hi_incident`;
CREATE TABLE `act_hi_incident` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `END_TIME_` timestamp(3) NULL DEFAULT NULL,
  `INCIDENT_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `INCIDENT_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `ACTIVITY_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CAUSE_INCIDENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_CAUSE_INCIDENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `INCIDENT_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_INCIDENT_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_INCIDENT_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_INCIDENT_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_INCIDENT_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_INCIDENT_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_job_log` */

drop table if exists `act_hi_job_log`;
CREATE TABLE `act_hi_job_log` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TIMESTAMP_` datetime(3) NOT NULL,
  `JOB_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `JOB_DUEDATE_` datetime(3) DEFAULT NULL,
  `JOB_RETRIES_` int(11) DEFAULT NULL,
  `JOB_PRIORITY_` bigint(20) NOT NULL DEFAULT '0',
  `JOB_EXCEPTION_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `JOB_EXCEPTION_STACK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_STATE_` int(11) DEFAULT NULL,
  `JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_DEF_TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `JOB_DEF_CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_PROCINST` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_PROCDEF` (`PROCESS_DEF_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_JOB_DEF_ID` (`JOB_DEF_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_PROC_DEF_KEY` (`PROCESS_DEF_KEY_`),
  KEY `ACT_IDX_HI_JOB_LOG_EX_STACK` (`JOB_EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_HI_JOB_LOG_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_op_log` */

drop table if exists `act_hi_op_log`;
CREATE TABLE `act_hi_op_log` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BATCH_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TIMESTAMP_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `OPERATION_TYPE_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `OPERATION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ENTITY_TYPE_` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `PROPERTY_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ORG_VALUE_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `NEW_VALUE_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  `CATEGORY_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXTERNAL_TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_OP_LOG_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_OP_LOG_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_OP_LOG_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_IDX_HI_OP_LOG_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_OP_LOG_RM_TIME` (`REMOVAL_TIME_`),
  KEY `ACT_IDX_HI_OP_LOG_TIMESTAMP` (`TIMESTAMP_`),
  KEY `ACT_IDX_HI_OP_LOG_USER_ID` (`USER_ID_`),
  KEY `ACT_IDX_HI_OP_LOG_OP_TYPE` (`OPERATION_TYPE_`),
  KEY `ACT_IDX_HI_OP_LOG_ENTITY_TYPE` (`ENTITY_TYPE_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_procinst` */

drop table if exists `act_hi_procinst`;
CREATE TABLE `act_hi_procinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `START_USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `START_ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `END_ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_CASE_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `STATE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `PROC_INST_ID_` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_PRO_I_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_HI_PRO_INST_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_PRO_INST_PROC_TIME` (`START_TIME_`,`END_TIME_`),
  KEY `ACT_IDX_HI_PI_PDEFID_END_TIME` (`PROC_DEF_ID_`,`END_TIME_`),
  KEY `ACT_IDX_HI_PRO_INST_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_taskinst` */

drop table if exists `act_hi_taskinst`;
CREATE TABLE `act_hi_taskinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TASK_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `OWNER_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ASSIGNEE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `PRIORITY_` int(11) DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `FOLLOW_UP_DATE_` datetime(3) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_TASKINST_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_TASK_INST_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_TASK_INST_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_TASKINST_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_TASKINSTID_PROCINST` (`ID_`,`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_TASK_INST_RM_TIME` (`REMOVAL_TIME_`),
  KEY `ACT_IDX_HI_TASK_INST_START` (`START_TIME_`),
  KEY `ACT_IDX_HI_TASK_INST_END` (`END_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_hi_varinst` */

drop table if exists `act_hi_varinst`;
CREATE TABLE `act_hi_varinst` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VAR_TYPE_` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `STATE_` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `REMOVAL_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_VARINST_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_NAME_TYPE` (`NAME_`,`VAR_TYPE_`),
  KEY `ACT_IDX_HI_CASEVAR_CASE_INST` (`CASE_INST_ID_`),
  KEY `ACT_IDX_HI_VAR_INST_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_HI_VAR_INST_PROC_DEF_KEY` (`PROC_DEF_KEY_`),
  KEY `ACT_IDX_HI_VARINST_BYTEAR` (`BYTEARRAY_ID_`),
  KEY `ACT_IDX_HI_VARINST_RM_TIME` (`REMOVAL_TIME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_group` */

drop table if exists `act_id_group`;
CREATE TABLE `act_id_group` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_info` */

drop table if exists `act_id_info`;
CREATE TABLE `act_id_info` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `USER_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VALUE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PASSWORD_` longblob,
  `PARENT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_membership` */

drop table if exists `act_id_membership`;
CREATE TABLE `act_id_membership` (
  `USER_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `GROUP_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`USER_ID_`,`GROUP_ID_`),
  KEY `ACT_FK_MEMB_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_MEMB_GROUP` FOREIGN KEY (`GROUP_ID_`) REFERENCES `act_id_group` (`ID_`),
  CONSTRAINT `ACT_FK_MEMB_USER` FOREIGN KEY (`USER_ID_`) REFERENCES `act_id_user` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_tenant` */

drop table if exists `act_id_tenant`;
CREATE TABLE `act_id_tenant` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_tenant_member` */

drop table if exists `act_id_tenant_member`;
CREATE TABLE `act_id_tenant_member` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `USER_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `GROUP_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_TENANT_MEMB_USER` (`TENANT_ID_`,`USER_ID_`),
  UNIQUE KEY `ACT_UNIQ_TENANT_MEMB_GROUP` (`TENANT_ID_`,`GROUP_ID_`),
  KEY `ACT_FK_TENANT_MEMB_USER` (`USER_ID_`),
  KEY `ACT_FK_TENANT_MEMB_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_TENANT_MEMB` FOREIGN KEY (`TENANT_ID_`) REFERENCES `act_id_tenant` (`ID_`),
  CONSTRAINT `ACT_FK_TENANT_MEMB_GROUP` FOREIGN KEY (`GROUP_ID_`) REFERENCES `act_id_group` (`ID_`),
  CONSTRAINT `ACT_FK_TENANT_MEMB_USER` FOREIGN KEY (`USER_ID_`) REFERENCES `act_id_user` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_id_user` */

drop table if exists `act_id_user`;
CREATE TABLE `act_id_user` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `FIRST_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `LAST_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `EMAIL_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PWD_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SALT_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `LOCK_EXP_TIME_` datetime(3) DEFAULT NULL,
  `ATTEMPTS_` int(11) DEFAULT NULL,
  `PICTURE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_re_case_def` */

drop table if exists `act_re_case_def`;
CREATE TABLE `act_re_case_def` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `KEY_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `HISTORY_TTL_` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_CASE_DEF_TENANT_ID` (`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_re_decision_def` */

drop table if exists `act_re_decision_def`;
CREATE TABLE `act_re_decision_def` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `KEY_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DEC_REQ_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DEC_REQ_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `HISTORY_TTL_` int(11) DEFAULT NULL,
  `VERSION_TAG_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_DEC_DEF_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_DEC_DEF_REQ_ID` (`DEC_REQ_ID_`),
  CONSTRAINT `ACT_FK_DEC_REQ` FOREIGN KEY (`DEC_REQ_ID_`) REFERENCES `act_re_decision_req_def` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_re_decision_req_def` */

drop table if exists `act_re_decision_req_def`;
CREATE TABLE `act_re_decision_req_def` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `KEY_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_DEC_REQ_DEF_TENANT_ID` (`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_re_deployment` */

drop table if exists `act_re_deployment`;
CREATE TABLE `act_re_deployment` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `DEPLOY_TIME_` datetime(3) DEFAULT NULL,
  `SOURCE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_DEPLOYMENT_NAME` (`NAME_`),
  KEY `ACT_IDX_DEPLOYMENT_TENANT_ID` (`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_re_procdef` */

drop table if exists `act_re_procdef`;
CREATE TABLE `act_re_procdef` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `KEY_` varchar(255) COLLATE utf8_bin NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `HAS_START_FORM_KEY_` tinyint(4) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `VERSION_TAG_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `HISTORY_TTL_` int(11) DEFAULT NULL,
  `STARTABLE_` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_PROCDEF_DEPLOYMENT_ID` (`DEPLOYMENT_ID_`),
  KEY `ACT_IDX_PROCDEF_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_PROCDEF_VER_TAG` (`VERSION_TAG_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_authorization` */

drop table if exists `act_ru_authorization`;
CREATE TABLE `act_ru_authorization` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) NOT NULL,
  `TYPE_` int(11) NOT NULL,
  `GROUP_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `RESOURCE_TYPE_` int(11) NOT NULL,
  `RESOURCE_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PERMS_` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_AUTH_USER` (`USER_ID_`,`TYPE_`,`RESOURCE_TYPE_`,`RESOURCE_ID_`),
  UNIQUE KEY `ACT_UNIQ_AUTH_GROUP` (`GROUP_ID_`,`TYPE_`,`RESOURCE_TYPE_`,`RESOURCE_ID_`),
  KEY `ACT_IDX_AUTH_GROUP_ID` (`GROUP_ID_`),
  KEY `ACT_IDX_AUTH_RESOURCE_ID` (`RESOURCE_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_batch` */

drop table if exists `act_ru_batch`;
CREATE TABLE `act_ru_batch` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TOTAL_JOBS_` int(11) DEFAULT NULL,
  `JOBS_CREATED_` int(11) DEFAULT NULL,
  `JOBS_PER_SEED_` int(11) DEFAULT NULL,
  `INVOCATIONS_PER_JOB_` int(11) DEFAULT NULL,
  `SEED_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BATCH_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `MONITOR_JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_BATCH_SEED_JOB_DEF` (`SEED_JOB_DEF_ID_`),
  KEY `ACT_IDX_BATCH_MONITOR_JOB_DEF` (`MONITOR_JOB_DEF_ID_`),
  KEY `ACT_IDX_BATCH_JOB_DEF` (`BATCH_JOB_DEF_ID_`),
  CONSTRAINT `ACT_FK_BATCH_JOB_DEF` FOREIGN KEY (`BATCH_JOB_DEF_ID_`) REFERENCES `act_ru_jobdef` (`ID_`),
  CONSTRAINT `ACT_FK_BATCH_MONITOR_JOB_DEF` FOREIGN KEY (`MONITOR_JOB_DEF_ID_`) REFERENCES `act_ru_jobdef` (`ID_`),
  CONSTRAINT `ACT_FK_BATCH_SEED_JOB_DEF` FOREIGN KEY (`SEED_JOB_DEF_ID_`) REFERENCES `act_ru_jobdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_case_execution` */

drop table if exists `act_ru_case_execution`;
CREATE TABLE `act_ru_case_execution` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_CASE_EXEC_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_EXEC_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PARENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PREV_STATE_` int(11) DEFAULT NULL,
  `CURRENT_STATE_` int(11) DEFAULT NULL,
  `REQUIRED_` tinyint(1) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_CASE_EXEC_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_CASE_EXE_CASE_INST` (`CASE_INST_ID_`),
  KEY `ACT_FK_CASE_EXE_PARENT` (`PARENT_ID_`),
  KEY `ACT_FK_CASE_EXE_CASE_DEF` (`CASE_DEF_ID_`),
  KEY `ACT_IDX_CASE_EXEC_TENANT_ID` (`TENANT_ID_`),
  CONSTRAINT `ACT_FK_CASE_EXE_CASE_DEF` FOREIGN KEY (`CASE_DEF_ID_`) REFERENCES `act_re_case_def` (`ID_`),
  CONSTRAINT `ACT_FK_CASE_EXE_CASE_INST` FOREIGN KEY (`CASE_INST_ID_`) REFERENCES `act_ru_case_execution` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_CASE_EXE_PARENT` FOREIGN KEY (`PARENT_ID_`) REFERENCES `act_ru_case_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_case_sentry_part` */

drop table if exists `act_ru_case_sentry_part`;
CREATE TABLE `act_ru_case_sentry_part` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXEC_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SENTRY_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SOURCE_CASE_EXEC_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `STANDARD_EVENT_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SOURCE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VARIABLE_EVENT_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VARIABLE_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SATISFIED_` tinyint(1) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_CASE_SENTRY_CASE_INST` (`CASE_INST_ID_`),
  KEY `ACT_FK_CASE_SENTRY_CASE_EXEC` (`CASE_EXEC_ID_`),
  CONSTRAINT `ACT_FK_CASE_SENTRY_CASE_EXEC` FOREIGN KEY (`CASE_EXEC_ID_`) REFERENCES `act_ru_case_execution` (`ID_`),
  CONSTRAINT `ACT_FK_CASE_SENTRY_CASE_INST` FOREIGN KEY (`CASE_INST_ID_`) REFERENCES `act_ru_case_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_event_subscr` */

drop table if exists `act_ru_event_subscr`;
CREATE TABLE `act_ru_event_subscr` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `EVENT_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `EVENT_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACTIVITY_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `CREATED_` datetime(3) NOT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_CONFIG_` (`CONFIGURATION_`),
  KEY `ACT_IDX_EVENT_SUBSCR_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_FK_EVENT_EXEC` (`EXECUTION_ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_EVT_NAME` (`EVENT_NAME_`),
  CONSTRAINT `ACT_FK_EVENT_EXEC` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_execution` */

drop table if exists `act_ru_execution`;
CREATE TABLE `act_ru_execution` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PARENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_EXEC_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUPER_CASE_EXEC_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `IS_ACTIVE_` tinyint(4) DEFAULT NULL,
  `IS_CONCURRENT_` tinyint(4) DEFAULT NULL,
  `IS_SCOPE_` tinyint(4) DEFAULT NULL,
  `IS_EVENT_SCOPE_` tinyint(4) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `CACHED_ENT_STATE_` int(11) DEFAULT NULL,
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXEC_ROOT_PI` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_EXEC_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_EXEC_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_FK_EXE_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_EXE_PARENT` (`PARENT_ID_`),
  KEY `ACT_FK_EXE_SUPER` (`SUPER_EXEC_`),
  KEY `ACT_FK_EXE_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_EXE_PARENT` FOREIGN KEY (`PARENT_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_EXE_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_EXE_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_EXE_SUPER` FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_ext_task` */

drop table if exists `act_ru_ext_task`;
CREATE TABLE `act_ru_ext_task` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) NOT NULL,
  `WORKER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TOPIC_NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `ERROR_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `ERROR_DETAILS_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `LOCK_EXP_TIME_` datetime(3) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PRIORITY_` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXT_TASK_TOPIC` (`TOPIC_NAME_`),
  KEY `ACT_IDX_EXT_TASK_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_EXT_TASK_PRIORITY` (`PRIORITY_`),
  KEY `ACT_IDX_EXT_TASK_ERR_DETAILS` (`ERROR_DETAILS_ID_`),
  KEY `ACT_IDX_EXT_TASK_EXEC` (`EXECUTION_ID_`),
  CONSTRAINT `ACT_FK_EXT_TASK_ERROR_DETAILS` FOREIGN KEY (`ERROR_DETAILS_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_EXT_TASK_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_filter` */

drop table if exists `act_ru_filter`;
CREATE TABLE `act_ru_filter` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) NOT NULL,
  `RESOURCE_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin NOT NULL,
  `OWNER_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `QUERY_` longtext COLLATE utf8_bin NOT NULL,
  `PROPERTIES_` longtext COLLATE utf8_bin,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_identitylink` */

drop table if exists `act_ru_identitylink`;
CREATE TABLE `act_ru_identitylink` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `GROUP_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_IDENT_LNK_GROUP` (`GROUP_ID_`),
  KEY `ACT_IDX_ATHRZ_PROCEDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_TSKASS_TASK` (`TASK_ID_`),
  CONSTRAINT `ACT_FK_ATHRZ_PROCEDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_TSKASS_TASK` FOREIGN KEY (`TASK_ID_`) REFERENCES `act_ru_task` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_incident` */

drop table if exists `act_ru_incident`;
CREATE TABLE `act_ru_incident` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) NOT NULL,
  `INCIDENT_TIMESTAMP_` datetime(3) NOT NULL,
  `INCIDENT_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `INCIDENT_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ACTIVITY_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CAUSE_INCIDENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ROOT_CAUSE_INCIDENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_INC_CONFIGURATION` (`CONFIGURATION_`),
  KEY `ACT_IDX_INC_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_INC_JOB_DEF` (`JOB_DEF_ID_`),
  KEY `ACT_IDX_INC_CAUSEINCID` (`CAUSE_INCIDENT_ID_`),
  KEY `ACT_IDX_INC_EXID` (`EXECUTION_ID_`),
  KEY `ACT_IDX_INC_PROCDEFID` (`PROC_DEF_ID_`),
  KEY `ACT_IDX_INC_PROCINSTID` (`PROC_INST_ID_`),
  KEY `ACT_IDX_INC_ROOTCAUSEINCID` (`ROOT_CAUSE_INCIDENT_ID_`),
  CONSTRAINT `ACT_FK_INC_CAUSE` FOREIGN KEY (`CAUSE_INCIDENT_ID_`) REFERENCES `act_ru_incident` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_INC_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_INC_JOB_DEF` FOREIGN KEY (`JOB_DEF_ID_`) REFERENCES `act_ru_jobdef` (`ID_`),
  CONSTRAINT `ACT_FK_INC_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_INC_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_INC_RCAUSE` FOREIGN KEY (`ROOT_CAUSE_INCIDENT_ID_`) REFERENCES `act_ru_incident` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_job` */

drop table if exists `act_ru_job`;
CREATE TABLE `act_ru_job` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `LOCK_EXP_TIME_` datetime(3) DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROCESS_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DUEDATE_` datetime(3) DEFAULT NULL,
  `REPEAT_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) NOT NULL DEFAULT '1',
  `JOB_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PRIORITY_` bigint(20) NOT NULL DEFAULT '0',
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_JOB_EXECUTION_ID` (`EXECUTION_ID_`),
  KEY `ACT_IDX_JOB_HANDLER` (`HANDLER_TYPE_`(100),`HANDLER_CFG_`(155)),
  KEY `ACT_IDX_JOB_PROCINST` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_IDX_JOB_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_JOB_JOB_DEF_ID` (`JOB_DEF_ID_`),
  KEY `ACT_FK_JOB_EXCEPTION` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_JOB_HANDLER_TYPE` (`HANDLER_TYPE_`),
  CONSTRAINT `ACT_FK_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_jobdef` */

drop table if exists `act_ru_jobdef`;
CREATE TABLE `act_ru_jobdef` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `JOB_TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `JOB_CONFIGURATION_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `JOB_PRIORITY_` bigint(20) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_JOBDEF_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_IDX_JOBDEF_PROC_DEF_ID` (`PROC_DEF_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_meter_log` */

drop table if exists `act_ru_meter_log`;
CREATE TABLE `act_ru_meter_log` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REPORTER_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `VALUE_` bigint(20) DEFAULT NULL,
  `TIMESTAMP_` datetime(3) DEFAULT NULL,
  `MILLISECONDS_` bigint(20) DEFAULT '0',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_METER_LOG_MS` (`MILLISECONDS_`),
  KEY `ACT_IDX_METER_LOG_NAME_MS` (`NAME_`,`MILLISECONDS_`),
  KEY `ACT_IDX_METER_LOG_REPORT` (`NAME_`,`REPORTER_`,`MILLISECONDS_`),
  KEY `ACT_IDX_METER_LOG_TIME` (`TIMESTAMP_`),
  KEY `ACT_IDX_METER_LOG` (`NAME_`,`TIMESTAMP_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_procinst_status` */

drop table if exists `act_ru_procinst_status`;
CREATE TABLE `act_ru_procinst_status` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `proc_def_id` varchar(255) DEFAULT NULL,
  `proc_def_key` varchar(255) DEFAULT NULL,
  `proc_def_name` varchar(255) DEFAULT NULL,
  `proc_inst_key` varchar(255) DEFAULT NULL,
  `proc_inst_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `act_ru_srvnode_status` */

drop table if exists `act_ru_srvnode_status`;
CREATE TABLE `act_ru_srvnode_status` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `node_id` varchar(255) DEFAULT NULL,
  `node_inst_id` varchar(255) DEFAULT NULL,
  `node_name` varchar(255) DEFAULT NULL,
  `node_type` varchar(255) DEFAULT NULL,
  `proc_inst_key` varchar(255) DEFAULT NULL,
  `proc_inst_id` varchar(255) DEFAULT NULL,
  `try_times` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `act_ru_task` */

drop table if exists `act_ru_task`;
CREATE TABLE `act_ru_task` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_DEF_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `OWNER_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ASSIGNEE_` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `DELEGATION_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PRIORITY_` int(11) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `FOLLOW_UP_DATE_` datetime(3) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_TASK_CREATE` (`CREATE_TIME_`),
  KEY `ACT_IDX_TASK_ASSIGNEE` (`ASSIGNEE_`),
  KEY `ACT_IDX_TASK_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_FK_TASK_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_TASK_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_TASK_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_TASK_CASE_EXE` (`CASE_EXECUTION_ID_`),
  KEY `ACT_FK_TASK_CASE_DEF` (`CASE_DEF_ID_`),
  CONSTRAINT `ACT_FK_TASK_CASE_DEF` FOREIGN KEY (`CASE_DEF_ID_`) REFERENCES `act_re_case_def` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_CASE_EXE` FOREIGN KEY (`CASE_EXECUTION_ID_`) REFERENCES `act_ru_case_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `act_ru_variable` */

drop table if exists `act_ru_variable`;
CREATE TABLE `act_ru_variable` (
  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(255) COLLATE utf8_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_EXECUTION_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `CASE_INST_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  `VAR_SCOPE_` varchar(64) COLLATE utf8_bin NOT NULL,
  `SEQUENCE_COUNTER_` bigint(20) DEFAULT NULL,
  `IS_CONCURRENT_LOCAL_` tinyint(4) DEFAULT NULL,
  `TENANT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_VARIABLE` (`VAR_SCOPE_`,`NAME_`),
  KEY `ACT_IDX_VARIABLE_TASK_ID` (`TASK_ID_`),
  KEY `ACT_IDX_VARIABLE_TENANT_ID` (`TENANT_ID_`),
  KEY `ACT_FK_VAR_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_VAR_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_VAR_BYTEARRAY` (`BYTEARRAY_ID_`),
  KEY `ACT_FK_VAR_CASE_EXE` (`CASE_EXECUTION_ID_`),
  KEY `ACT_FK_VAR_CASE_INST` (`CASE_INST_ID_`),
  CONSTRAINT `ACT_FK_VAR_BYTEARRAY` FOREIGN KEY (`BYTEARRAY_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_CASE_EXE` FOREIGN KEY (`CASE_EXECUTION_ID_`) REFERENCES `act_ru_case_execution` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_CASE_INST` FOREIGN KEY (`CASE_INST_ID_`) REFERENCES `act_ru_case_execution` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `batch_execution_jobs` */

drop table if exists `batch_execution_jobs`;
CREATE TABLE `batch_execution_jobs` (
  `id` varchar(255) NOT NULL,
  `complete_timestamp` datetime DEFAULT NULL,
  `create_timestamp` datetime DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `batch_job` */

drop table if exists `batch_job`;
CREATE TABLE `batch_job` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `script_url` varchar(500) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `batch_job_host` */

drop table if exists `batch_job_host`;
CREATE TABLE `batch_job_host` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `batch_job_id` int(11) NOT NULL,
  `host_ip` varchar(20) DEFAULT NULL,
  `output_url` varchar(500) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT NULL,
  `result` varchar(10) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `batch_job_id` (`batch_job_id`),
  CONSTRAINT `batch_job_host_ibfk_1` FOREIGN KEY (`batch_job_id`) REFERENCES `batch_job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `blob_data` */

drop table if exists `blob_data`;
CREATE TABLE `blob_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `content` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*Table structure for table `core_re_proc_def_info` */

drop table if exists `core_re_proc_def_info`;
CREATE TABLE `core_re_proc_def_info` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `rev` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `proc_def_data` text,
  `proc_def_data_fmt` varchar(255) DEFAULT NULL,
  `proc_def_kernel_id` varchar(255) DEFAULT NULL,
  `proc_def_key` varchar(255) DEFAULT NULL,
  `proc_def_name` varchar(255) DEFAULT NULL,
  `proc_def_ver` int(11) DEFAULT NULL,
  `root_entity` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_re_task_node_def_info` */

drop table if exists `core_re_task_node_def_info`;
CREATE TABLE `core_re_task_node_def_info` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `rev` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `node_id` varchar(255) DEFAULT NULL,
  `node_name` varchar(255) DEFAULT NULL,
  `node_type` varchar(255) DEFAULT NULL,
  `proc_def_id` varchar(255) DEFAULT NULL,
  `proc_def_kernel_id` varchar(255) DEFAULT NULL,
  `proc_def_key` varchar(255) DEFAULT NULL,
  `proc_def_ver` int(11) DEFAULT NULL,
  `routine_exp` varchar(255) DEFAULT NULL,
  `routine_raw` varchar(255) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `timeout_exp` varchar(255) DEFAULT NULL,
  `ordered_no` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_re_task_node_param` */

drop table if exists `core_re_task_node_param`;
CREATE TABLE `core_re_task_node_param` (
    `id` VARCHAR(255) NOT NULL,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `active` BIT(1) NULL DEFAULT NULL,
    `rev` INT(11) NULL DEFAULT NULL,
    `status` VARCHAR(255) NULL DEFAULT NULL,
    `bind_node_id` VARCHAR(255) NULL DEFAULT NULL,
    `bind_param_name` VARCHAR(255) NULL DEFAULT NULL,
    `bind_param_type` VARCHAR(255) NULL DEFAULT NULL,
    `node_id` VARCHAR(255) NULL DEFAULT NULL,
    `param_name` VARCHAR(255) NULL DEFAULT NULL,
    `proc_def_id` VARCHAR(255) NULL DEFAULT NULL,
    `task_node_def_id` VARCHAR(255) NULL DEFAULT NULL,
    `bind_type` VARCHAR(255) NULL DEFAULT NULL,
    `bind_val` VARCHAR(1024) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_ru_proc_exec_binding` */

drop table if exists `core_ru_proc_exec_binding`;
CREATE TABLE `core_ru_proc_exec_binding` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `bind_type` varchar(255) DEFAULT NULL,
  `node_def_id` varchar(255) DEFAULT NULL,
  `object_id` varchar(255) DEFAULT NULL,
  `proc_def_id` varchar(255) DEFAULT NULL,
  `proc_inst_id` int(11) DEFAULT NULL,
  `task_node_inst_id` int(11) DEFAULT NULL,
  `entity_data_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_ru_proc_inst_info` */

drop table if exists `core_ru_proc_inst_info`;
CREATE TABLE `core_ru_proc_inst_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `oper` varchar(255) DEFAULT NULL,
  `oper_grp` varchar(255) DEFAULT NULL,
  `rev` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `proc_def_id` varchar(255) DEFAULT NULL,
  `proc_def_key` varchar(255) DEFAULT NULL,
  `proc_def_name` varchar(255) DEFAULT NULL,
  `proc_inst_kernel_id` varchar(255) DEFAULT NULL,
  `proc_inst_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_ru_task_node_exec_param` */

drop table if exists `core_ru_task_node_exec_param`;
CREATE TABLE `core_ru_task_node_exec_param` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `data_type` varchar(255) DEFAULT NULL,
  `data_value` varchar(255) DEFAULT NULL,
  `object_id` varchar(255) DEFAULT NULL,
  `ordered_no` int(11) DEFAULT NULL,
  `param_name` varchar(255) DEFAULT NULL,
  `param_type` varchar(255) DEFAULT NULL,
  `proc_inst_id` int(11) DEFAULT NULL,
  `task_node_inst_id` int(11) DEFAULT NULL,
  `is_sensitive` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `core_ru_task_node_inst_info` */

drop table if exists `core_ru_task_node_inst_info`;
CREATE TABLE `core_ru_task_node_inst_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `oper` varchar(255) DEFAULT NULL,
  `oper_grp` varchar(255) DEFAULT NULL,
  `rev` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `err_url` varchar(255) DEFAULT NULL,
  `node_def_id` varchar(255) DEFAULT NULL,
  `node_id` varchar(255) DEFAULT NULL,
  `node_name` varchar(255) DEFAULT NULL,
  `proc_def_id` varchar(255) DEFAULT NULL,
  `proc_def_key` varchar(255) DEFAULT NULL,
  `proc_inst_id` int(11) DEFAULT NULL,
  `proc_inst_key` varchar(255) DEFAULT NULL,
  `req_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `core_ru_proc_exec_binding_tmp`;
CREATE TABLE `core_ru_proc_exec_binding_tmp` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255)  DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255)  DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `bind_type` varchar(255)  DEFAULT NULL,
  `is_bound` varchar(255)  DEFAULT NULL,
  `entity_data_id` varchar(255)  DEFAULT NULL,
  `entity_type_id` varchar(255) DEFAULT NULL,
  `node_def_id` varchar(255)  DEFAULT NULL,
  `ordered_no` varchar(255)  DEFAULT NULL,
  `proc_def_id` varchar(255)  DEFAULT NULL,
  `proc_session_id` varchar(255)  DEFAULT NULL,
  `entity_data_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `core_ru_graph_node`;
CREATE TABLE `core_ru_graph_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255)  DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255)  DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `data_id` varchar(255)  DEFAULT NULL,
  `display_name` varchar(255)  DEFAULT NULL,
  `entity_name` varchar(255)  DEFAULT NULL,
  `g_node_id` varchar(255)  DEFAULT NULL,
  `pkg_name` varchar(255)  DEFAULT NULL,
  `prev_ids` text,
  `proc_inst_id` int(11) DEFAULT NULL,
  `proc_sess_id` varchar(255)  DEFAULT NULL,
  `succ_ids` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `core_operation_event`;
CREATE TABLE `core_operation_event` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `created_by` VARCHAR(255) NULL DEFAULT NULL,
    `created_time` DATETIME NULL DEFAULT NULL,
    `updated_by` VARCHAR(255) NULL DEFAULT NULL,
    `updated_time` DATETIME NULL DEFAULT NULL,
    `event_seq_no` VARCHAR(255) NULL DEFAULT NULL,
    `event_type` VARCHAR(255) NULL DEFAULT NULL,
    `is_notified` BIT(1) NULL DEFAULT NULL,
    `notify_endpoint` VARCHAR(255) NULL DEFAULT NULL,
    `is_notify_required` BIT(1) NULL DEFAULT NULL,
    `oper_data` VARCHAR(255) NULL DEFAULT NULL,
    `oper_key` VARCHAR(255) NULL DEFAULT NULL,
    `oper_user` VARCHAR(255) NULL DEFAULT NULL,
    `proc_def_id` VARCHAR(255) NULL DEFAULT NULL,
    `proc_inst_id` VARCHAR(255) NULL DEFAULT NULL,
    `src_sub_system` VARCHAR(255) NULL DEFAULT NULL,
    `status` VARCHAR(255) NULL DEFAULT NULL,
    `end_time` DATETIME NULL DEFAULT NULL,
    `priority` INT(11) NULL DEFAULT NULL,
    `proc_inst_key` VARCHAR(255) NULL DEFAULT NULL,
    `start_time` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
