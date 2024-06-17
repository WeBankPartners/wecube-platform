alter table plugin_package_data_model add column `updated_time` datetime DEFAULT NULL COMMENT '更新时间-新';
alter table plugin_package_attributes add column `is_array` bit(1) DEFAULT b'0' COMMENT '是否数组-新';

CREATE TABLE `proc_def` (
        `id` varchar(64) NOT NULL COMMENT '唯一标识',
        `key` varchar(64) NOT NULL COMMENT '编排key',
        `name` varchar(255) NOT NULL COMMENT '编排名称',
        `version` varchar(64) NOT NULL COMMENT '版本',
        `root_entity` varchar(255) DEFAULT NULL COMMENT '根节点',
        `status` varchar(32) NOT NULL COMMENT '状态->draft(草稿) | deployed(发布) |  disabled(禁用)',
        `tags` varchar(255) DEFAULT NULL COMMENT '标签',
        `for_plugin` varchar(255) DEFAULT NULL COMMENT '授权插件',
        `scene` varchar(255) DEFAULT NULL COMMENT '使用场景',
        `conflict_check` bit(1) DEFAULT 0 COMMENT '冲突检测',
        `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
        `created_time` datetime DEFAULT NULL COMMENT '创建时间',
        `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
        `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_def_node` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `proc_def_id` varchar(64) NOT NULL COMMENT '编排id',
     `node_id` varchar(64) default null COMMENT '前端节点id',
     `name` varchar(64) NOT NULL COMMENT '节点名称',
     `description` varchar(255) DEFAULT NULL COMMENT '节点描述',
     `status` varchar(32) NOT NULL COMMENT '状态',
     `node_type` varchar(64) NOT NULL COMMENT '节点类型',
     `service_name` varchar(255) DEFAULT NULL COMMENT '插件服务名',
     `dynamic_bind` bit(1) DEFAULT 0 COMMENT '是否动态绑定',
     `bind_node_id` varchar(64) DEFAULT NULL COMMENT '动态绑定节点',
     `risk_check` bit(1) DEFAULT 0 COMMENT '是否高危检测',
     `routine_expression` varchar(1024) DEFAULT NULL COMMENT '定位规则',
     `context_param_nodes` varchar(1024) DEFAULT NULL COMMENT '上下文参数节点',
     `timeout` int(11) DEFAULT 30 COMMENT '超时时间分钟',
     `ordered_no` int(11) DEFAULT 0 COMMENT '节点顺序',
     `time_config` varchar(1024) DEFAULT NULL COMMENT '时间节点配置',
     `ui_style` text DEFAULT NULL COMMENT '前端样式',
     `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     CONSTRAINT `fk_proc_def_node_def` FOREIGN KEY (`proc_def_id`) REFERENCES `proc_def` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_def_node_param` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `proc_def_node_id` varchar(64) NOT NULL COMMENT '编排节点id',
       `param_id` varchar(64) DEFAULT NULL COMMENT '关联参数配置id',
       `name` varchar(255) NOT NULL COMMENT '参数名',
       `bind_type` varchar(16) DEFAULT NULL COMMENT '参数类型->context(上下文) | constant(静态值)',
       `value` varchar(1024) DEFAULT NULL COMMENT '参数值',
       `ctx_bind_node` varchar(64) DEFAULT NULL COMMENT '上下文节点',
       `ctx_bind_type` varchar(16) DEFAULT NULL COMMENT '上下文出入参->input(入参) | output(出参)',
       `ctx_bind_name` varchar(255) DEFAULT NULL COMMENT '上下文参数名',
       `required` varchar(16) DEFAULT 'N' COMMENT '是否必填',
       PRIMARY KEY (`id`),
       CONSTRAINT `fk_proc_def_param_node` FOREIGN KEY (`proc_def_node_id`) REFERENCES `proc_def_node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_def_node_link` (
      `id` varchar(128) NOT NULL COMMENT '唯一标识(source__target)',
      `proc_def_id` varchar(64) NOT NULL COMMENT '编排id',
      `link_id` varchar(64) default null COMMENT '前端线id',
      `source` varchar(64) NOT NULL COMMENT '源节点',
      `target` varchar(64) NOT NULL COMMENT '目标节点',
      `name` varchar(64) DEFAULT NULL COMMENT '连接名称',
      `ui_style` text DEFAULT NULL COMMENT '前端样式',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_def_permission` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `proc_def_id` varchar(64) NOT NULL COMMENT '编排id',
       `role_id` varchar(64) NOT NULL COMMENT '角色id',
       `role_name` varchar(64) NOT NULL COMMENT '角色名称',
       `permission` varchar(32) NOT NULL COMMENT '权限->MGMT(管理) | USE(使用)',
       PRIMARY KEY (`id`),
       CONSTRAINT `fk_proc_def_perm_def` FOREIGN KEY (`proc_def_id`) REFERENCES `proc_def` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_def_collect` (
    `id` varchar(64) NOT NULL COMMENT '唯一标识',
    `proc_def_id` varchar(64) NOT NULL COMMENT '编排id',
    `role_id` varchar(64) NOT NULL COMMENT '角色id',
    `user_id` varchar(64) NOT NULL COMMENT '用户id',
    `created_time` datetime DEFAULT NULL COMMENT '创建时间',
    `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_proc_def_collect_def` FOREIGN KEY (`proc_def_id`) REFERENCES `proc_def` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `batch_execution` (
   `id` varchar(64) NOT NULL COMMENT '唯一标识',
   `name` varchar(128) NOT NULL COMMENT '名称',
   `batch_execution_template_id` varchar(64) DEFAULT NULL COMMENT '模板id',
   `batch_execution_template_name` varchar(128) DEFAULT NULL COMMENT '模板名称',
   `error_code` varchar(1) NULL COMMENT '错误码, 0:成功, 1:失败, 2:执行中',
   `error_message` text NULL COMMENT '错误信息',
   `config_data` mediumtext NULL COMMENT '配置数据',
   `source_data` mediumtext NULL COMMENT '回显数据',
   `created_by` varchar(64) NOT NULL COMMENT '创建者',
   `updated_by` varchar(64) NULL COMMENT '更新者',
   `created_time` datetime NOT NULL COMMENT '创建时间',
   `updated_time` datetime NULL COMMENT '更新时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX batch_exec_name_IDX USING BTREE ON batch_execution (`name`);
CREATE INDEX batch_exec_tmpl_name_IDX USING BTREE ON batch_execution (batch_execution_template_name);
CREATE INDEX batch_exec_error_code_IDX USING BTREE ON batch_execution (error_code);
CREATE INDEX batch_exec_created_time_IDX USING BTREE ON batch_execution (created_time);

CREATE TABLE `batch_exec_jobs` (
   `id` varchar(64) NOT NULL COMMENT '唯一标识',
   `batch_execution_id` varchar(64) NOT NULL COMMENT '批量执行任务id',
   `package_name` varchar(64) NOT NULL COMMENT '包名',
   `entity_name` varchar(100) NOT NULL COMMENT '实体名',
   `business_key` varchar(255) NOT NULL COMMENT '业务key',
   `root_entity_id` varchar(64) NOT NULL COMMENT '根实体id',
   `execute_time` datetime NOT NULL COMMENT '执行时间',
   `complete_time` datetime NULL COMMENT '完成时间',
   `error_code` varchar(1) NULL COMMENT '错误码, 0:成功, 1:失败, 2:执行中',
   `error_message` text NULL COMMENT '错误信息',
   `input_json` longtext NULL COMMENT '输入json',
   `return_json` longtext NULL COMMENT '输出json',
   `plugin_config_interface_id` varchar(64) NULL COMMENT '插件配置接口id',
   PRIMARY KEY (`id`),
   UNIQUE KEY `job_id_and_root_entity_id` (`batch_execution_id`, `root_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `batch_execution_template` (
            `id` varchar(64) NOT NULL COMMENT '唯一标识',
            `name` varchar(128) NOT NULL COMMENT '名称',
            `status` varchar(64)  NOT NULL COMMENT '使用状态: 可使用、权限被移出',
            `operate_object` varchar(190) NULL COMMENT '操作对象',
            `plugin_service` varchar(190) NULL COMMENT '插件服务',
            `is_dangerous_block` tinyint(1) DEFAULT NULL COMMENT '是否高危拦截',
            `config_data` mediumtext NULL COMMENT '配置数据',
            `source_data` mediumtext NULL COMMENT '回显数据',
            `created_by` varchar(64) NULL COMMENT '创建者',
            `created_time` datetime NULL COMMENT '创建时间',
            `updated_by` varchar(64) NULL COMMENT '更新者',
            `updated_time` datetime NULL COMMENT '更新时间',
            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX batch_exec_tmpl_name_IDX USING BTREE ON batch_execution_template (`name`);
CREATE INDEX batch_exec_tmpl_operate_object_IDX USING BTREE ON batch_execution_template (operate_object);
CREATE INDEX batch_exec_tmpl_plugin_service_IDX USING BTREE ON batch_execution_template (plugin_service);
CREATE INDEX batch_exec_tmpl_updated_time_IDX USING BTREE ON batch_execution_template (updated_time);
alter table batch_execution_template add publish_status varchar(64) default 'published' not null comment '发布状态：published, draft' after status;
alter table batch_execution_template add constraint name_unique unique (`name`);

CREATE TABLE `batch_execution_template_role` (
                 `id` varchar(64) NOT NULL COMMENT '唯一标识',
                 `batch_execution_template_id` varchar(64) NOT NULL COMMENT '批量执行模板id',
                 `permission` varchar(64) NOT NULL COMMENT '权限类型->MGMT(管理) | USE(使用)',
                 `role_id` varchar(64) NOT NULL COMMENT '角色id',
                 `role_name` varchar(64) NOT NULL COMMENT '角色名',
                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX batch_exec_tmpl_role_tmplId_IDX USING BTREE ON batch_execution_template_role (batch_execution_template_id);
CREATE INDEX batch_exec_tmpl_role_name_IDX USING BTREE ON batch_execution_template_role (role_name);
CREATE INDEX batch_exec_tmpl_role_permission_IDX USING BTREE ON batch_execution_template_role (permission);

CREATE TABLE `batch_execution_template_collect` (
                    `id` varchar(64) NOT NULL COMMENT '唯一标识',
                    `batch_execution_template_id` varchar(64) NOT NULL COMMENT '批量执行模板id',
                    `user_id` varchar(64) NOT NULL COMMENT '用户id',
                    `created_time` datetime DEFAULT NULL COMMENT '创建时间',
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `user_id_batch_execution_template_id` (`user_id`, `batch_execution_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX batch_exec_tmpl_collect_userId_IDX USING BTREE ON batch_execution_template_collect (user_id);
CREATE INDEX batch_exec_tmpl_id_IDX USING BTREE ON batch_execution_template_collect (batch_execution_template_id);

CREATE TABLE `proc_data_preview` (
         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
         `proc_def_id` varchar(64) NOT NULL COMMENT '编排定义id',
         `proc_session_id` varchar(64) NOT NULL COMMENT '试算任务id',
         `proc_def_node_id` varchar(64) DEFAULT NULL COMMENT '编排节点id',
         `entity_data_id` varchar(64) DEFAULT NULL COMMENT '数据id',
         `entity_data_name` varchar(255) DEFAULT NULL COMMENT '数据名称',
         `entity_type_id` varchar(64) DEFAULT NULL COMMENT '数据entity',
         `ordered_no` varchar(32) DEFAULT NULL COMMENT '节点排序',
         `bind_type` varchar(32) DEFAULT NULL COMMENT '编排(taskNode)还是节点(process)',
         `full_data_id` varchar(1024) DEFAULT NULL COMMENT '数据全路径',
         `is_bound` bit(1) DEFAULT 1 COMMENT '是否绑定',
         `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
         `created_time` datetime DEFAULT NULL COMMENT '创建时间',
         `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
         `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_ins_graph_node` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
   `proc_session_id` varchar(64) NOT NULL COMMENT '试算任务id',
   `proc_ins_id` varchar(64) DEFAULT NULL COMMENT '编排实例id',
   `data_id` varchar(64) DEFAULT NULL COMMENT '数据id',
   `display_name` varchar(255) DEFAULT NULL COMMENT '数据显示名',
   `entity_name` varchar(255) DEFAULT NULL COMMENT 'entity显示名',
   `graph_node_id` varchar(255) DEFAULT NULL COMMENT '图形节点id',
   `pkg_name` varchar(64) DEFAULT NULL COMMENT '数据所属包',
   `prev_ids` text DEFAULT NULL COMMENT '上游图形节点id列表',
   `succ_ids` text DEFAULT NULL COMMENT '下游图形节点id列表',
   `full_data_id` varchar(1024) DEFAULT NULL COMMENT '数据全路径',
   `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
   `created_time` datetime DEFAULT NULL COMMENT '创建时间',
   `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
   `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_data_binding` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `proc_def_id` varchar(64) NOT NULL COMMENT '编排定义id',
     `proc_ins_id` varchar(64) NOT NULL COMMENT '编排实例id',
     `proc_def_node_id` varchar(64) DEFAULT NULL COMMENT '编排节点id',
     `proc_ins_node_id` varchar(64) DEFAULT NULL COMMENT '编排实例节点id',
     `entity_id` varchar(64) DEFAULT NULL COMMENT '编排数据id',
     `entity_data_id` varchar(64) DEFAULT NULL COMMENT '数据id',
     `entity_data_name` varchar(255) DEFAULT NULL COMMENT '数据名称',
     `entity_type_id` varchar(64) DEFAULT NULL COMMENT '数据entity',
     `bind_flag` bit(1) DEFAULT 1 COMMENT '是否绑定',
     `bind_type` varchar(32) DEFAULT NULL COMMENT '编排(taskNode)还是节点(process)',
     `full_data_id` varchar(1024) DEFAULT NULL COMMENT '数据全路径',
     `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_data_cache` (
       `id` varchar(64) NOT NULL COMMENT '唯一标识',
       `proc_ins_id` varchar(64) NOT NULL COMMENT '编排实例id',
       `entity_id` varchar(64) DEFAULT NULL COMMENT '编排数据id',
       `entity_data_id` varchar(64) DEFAULT NULL COMMENT '数据id',
       `entity_data_name` varchar(255) DEFAULT NULL COMMENT '数据名称',
       `entity_type_id` varchar(64) DEFAULT NULL COMMENT '数据entity',
       `full_data_id` varchar(1024) DEFAULT NULL COMMENT '数据全路径',
       `data_value` text DEFAULT NULL COMMENT '数据值',
       `prev_ids` text DEFAULT NULL COMMENT '上游节点id列表',
       `succ_ids` text DEFAULT NULL COMMENT '下游节点id列表',
       `created_time` datetime DEFAULT NULL COMMENT '创建时间',
       `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_ins` (
    `id` varchar(64) NOT NULL COMMENT '唯一标识',
    `proc_def_id` varchar(64) NOT NULL COMMENT '编排定义id',
    `proc_def_key` varchar(64) NOT NULL COMMENT '编排定义key',
    `proc_def_name` varchar(255) NOT NULL COMMENT '编排定义名称',
    `status` varchar(32) NOT NULL COMMENT '状态->NotStarted(初始化) | InProgress(运行中) | Faulted(失败) | Completed(成功) | InternallyTerminated(终止)',
    `entity_data_id` varchar(64) DEFAULT NULL COMMENT '根数据id',
    `entity_type_id` varchar(64) DEFAULT NULL COMMENT '根数据类型',
    `entity_data_name` varchar(64) DEFAULT NULL COMMENT '根数据名称',
    `proc_session_id` varchar(64) DEFAULT NULL COMMENT '试算session',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT NULL COMMENT '创建时间',
    `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_ins_node` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `proc_ins_id` varchar(64) NOT NULL COMMENT '编排实例id',
     `proc_def_node_id` varchar(64) NOT NULL COMMENT '编排节点定义id',
     `name` varchar(64) DEFAULT NULL COMMENT '编排定义名称',
     `node_type` varchar(64) NOT NULL COMMENT '任务类型->start(开始) | auto(自动) | data(数据写入) | human(人工) | agg(聚合) | time(定时) | date(定期) | decision(判断) | end(结束) | break(异常结束)',
     `status` varchar(32) NOT NULL COMMENT '状态->NotStarted(初始化) | InProgress(运行中) | wait(等待or聚合) | Faulted(失败) | Completed(成功) | Timeout(超时)',
     `risk_check_result` text DEFAULT NULL COMMENT '高危检测结果',
     `error_msg` text DEFAULT NULL COMMENT '报错信息',
     `ordered_no` int(11) DEFAULT 0 COMMENT '节点排序',
     `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_ins_node_req` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `proc_ins_node_id` varchar(64) NOT NULL COMMENT '编排实例节点id',
     `req_url` varchar(255) NOT NULL COMMENT '请求url',
     `is_completed` bit(1) DEFAULT 0 COMMENT '是否完成',
     `error_code` varchar(64) DEFAULT NULL COMMENT '错误码',
     `error_msg` text DEFAULT NULL COMMENT '错误信息',
     `with_context_data` bit(1) DEFAULT 0 COMMENT '是否有上下文数据',
     `req_data_amount` int(11) DEFAULT NULL COMMENT '有多少组数据',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_ins_node_req_param` (
       `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
       `req_id` varchar(64) NOT NULL COMMENT '请求id',
       `data_index` int(11) DEFAULT 0 COMMENT '第几组数据',
       `from_type` varchar(16) DEFAULT 'input' COMMENT 'input | output',
       `name` varchar(255) DEFAULT NULL COMMENT '参数名',
       `data_type` varchar(64) DEFAULT NULL COMMENT '参数数据类型',
       `data_value` text DEFAULT NULL COMMENT '参数数据值',
       `entity_data_id` varchar(64) DEFAULT NULL COMMENT '数据id',
       `entity_type_id` varchar(255) DEFAULT NULL COMMENT '数据entity',
       `is_sensitive` bit(1) DEFAULT 0 COMMENT '是否敏感',
       `full_data_id` varchar(1024) DEFAULT NULL COMMENT '数据全路径',
       `multiple` bit(1) DEFAULT 0 COMMENT '是否数组',
       `param_def_id` varchar(64) DEFAULT NULL COMMENT '插件服务参数id',
       `mapping_type` varchar(64) DEFAULT NULL COMMENT '数据来源',
       `callback_id` varchar(64) DEFAULT NULL COMMENT '回调id',
       `created_time` datetime DEFAULT NULL COMMENT '创建时间',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `proc_run_workflow` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `proc_ins_id` varchar(64) NOT NULL COMMENT '编排实例id',
     `name` varchar(64) NOT NULL COMMENT '名称',
     `status` varchar(32) NOT NULL COMMENT '状态->NotStarted(初始化) | InProgress(运行中) | Faulted(失败) | Completed(成功) | InternallyTerminated(终止)',
     `error_message` text DEFAULT NULL COMMENT '错误信息',
     `sleep` bit(1) DEFAULT 0 COMMENT '休眠->problem超10min或running中当前节点wait超10min,防止不是终态的工作流一直占用资源',
     `stop` bit(1) DEFAULT 0 COMMENT '暂停->人为停止',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     `host` varchar(64) DEFAULT NULL COMMENT '当前运行主机',
     `last_alive_time` datetime DEFAULT NULL COMMENT '定期打卡时间->每隔10s更新',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `proc_run_node` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `workflow_id` varchar(64) NOT NULL COMMENT '工作流id',
     `proc_ins_node_id` varchar(64) NOT NULL COMMENT '编排节点id',
     `name` varchar(64) DEFAULT NULL COMMENT '名称',
     `job_type` varchar(64) NOT NULL COMMENT '任务类型->start(开始) | auto(自动) | data(数据写入) | human(人工) | agg(聚合) | time(定时) | date(定期) | decision(判断) | end(结束) | break(异常结束)',
     `status` varchar(32) NOT NULL COMMENT '状态->NotStarted(初始化) | InProgress(运行中) | wait(等待or聚合) | Faulted(失败) | Completed(成功) | Timeout(超时)',
     `input` text DEFAULT NULL COMMENT '输入',
     `output` text DEFAULT NULL COMMENT '输出',
     `tmp_data` text DEFAULT NULL COMMENT '临时数据',
     `error_message` text DEFAULT NULL COMMENT '错误信息',
     `timeout` int(11) DEFAULT 0 COMMENT '超时时间',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     `start_time` datetime DEFAULT NULL COMMENT '开始时间',
     `end_time` datetime DEFAULT NULL COMMENT '结束时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `proc_run_link` (
     `id` varchar(64) NOT NULL COMMENT '唯一标识',
     `workflow_id` varchar(64) NOT NULL COMMENT '工作流id',
     `proc_def_link_id` varchar(64) NOT NULL COMMENT '关联定义id',
     `name` varchar(64) DEFAULT NULL COMMENT '名称',
     `source` varchar(64) NOT NULL COMMENT '源',
     `target` varchar(64) NOT NULL COMMENT '目标',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `proc_run_work_record` (
        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
        `workflow_id` varchar(64) NOT NULL COMMENT '工作流id',
        `host` varchar(64) DEFAULT NULL COMMENT '主机',
        `action` varchar(32) DEFAULT NULL COMMENT '状态->NotStarted(初始化) | InProgress(运行中) | Faulted(失败) | Completed(成功) | kill(终止) | sleep(休眠) | takeOver(接管) | stop(暂停) | recover(恢复)',
        `message` text DEFAULT NULL COMMENT '详细信息,终止原因等',
        `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
        `created_time` datetime DEFAULT NULL COMMENT '创建时间',
        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `proc_run_operation` (
      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
      `workflow_id` varchar(64) NOT NULL COMMENT '工作流id',
      `node_id` varchar(64) DEFAULT NULL COMMENT '节点id',
      `operation` varchar(64) NOT NULL COMMENT '操作->kill(终止工作流) | retry(重试节点) | continue(跳过节点) | approve(人工审批) | date(定期触发)',
      `status` varchar(32) DEFAULT NULL COMMENT '状态->wait(待处理) | done(已处理)',
      `message` text DEFAULT NULL COMMENT '详细信息->审批结果,终止原因等',
      `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
      `created_time` datetime DEFAULT NULL COMMENT '创建时间',
      `handle_by` varchar(64) DEFAULT NULL COMMENT '处理的主机',
      `start_time` datetime DEFAULT NULL COMMENT '处理开始时间',
      `end_time` datetime DEFAULT NULL COMMENT '处理结束时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `proc_schedule_config` (
    `id` varchar(64) NOT NULL COMMENT '唯一标识',
    `proc_def_id` varchar(64) NOT NULL COMMENT '编排定义id',
    `proc_def_key` varchar(64) DEFAULT NULL COMMENT '编排定义key',
    `proc_def_name` varchar(255) NOT NULL COMMENT '编排定义名称',
    `status` varchar(32) DEFAULT NULL COMMENT '状态->Ready(正在运行) | Stopped(暂停) | Deleted(删除)',
    `entity_data_id` varchar(64) DEFAULT NULL COMMENT '根数据id',
    `entity_type_id` varchar(64) DEFAULT NULL COMMENT '根数据类型',
    `entity_data_name` varchar(255) DEFAULT NULL COMMENT '根数据名称',
    `schedule_mode` varchar(64) DEFAULT NULL COMMENT '定时模式->Monthly(每月) | Weekly(每周) | Daily(每天) | Hourly(每小时)',
    `schedule_expr` varchar(64) DEFAULT NULL COMMENT '时间表达式',
    `cron_expr` varchar(64) DEFAULT NULL COMMENT 'cron表达式',
    `exec_times` int(11) DEFAULT 0 COMMENT '执行次数',
    `role` varchar(64) DEFAULT NULL COMMENT '管理角色',
    `mail_mode` varchar(64) DEFAULT NULL COMMENT '邮件发送模式->role(角色邮箱) | user(用户邮箱) | none(不发送)',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT NULL COMMENT '创建时间',
    `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX idx_proc_schedule_config_mode USING BTREE ON proc_schedule_config (schedule_mode);
CREATE INDEX idx_proc_schedule_config_status USING BTREE ON proc_schedule_config (status);
CREATE INDEX idx_proc_schedule_config_owner USING BTREE ON proc_schedule_config (created_by);

CREATE TABLE `proc_schedule_job` (
     `id` varchar(96) NOT NULL COMMENT '定时配置id加时间戳',
     `schedule_config_id` varchar(64) NOT NULL COMMENT '定时配置id',
     `proc_ins_id` varchar(64) DEFAULT NULL COMMENT '编排实例id',
     `status` varchar(32) DEFAULT NULL COMMENT '状态->ready(准备启动) | fail(报错) | done(已完成)',
     `handle_by` varchar(64) DEFAULT NULL COMMENT '处理的主机',
     `error_msg` text DEFAULT NULL COMMENT '错误信息',
     `mail_status` varchar(32) DEFAULT NULL COMMENT '邮件状态->none(不发邮件) | wait(等待发) | sending(正在发) | fail(发送失败) | done(已发送)',
     `mail_msg` text DEFAULT NULL COMMENT '邮件通知信息',
     `created_time` datetime DEFAULT NULL COMMENT '创建时间',
     `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE INDEX idx_proc_schedule_job_config USING BTREE ON proc_schedule_job (schedule_config_id);
CREATE INDEX idx_proc_schedule_job_status USING BTREE ON proc_schedule_job (status);
CREATE INDEX idx_proc_schedule_job_time USING BTREE ON proc_schedule_job (created_time);

CREATE INDEX idx_sys_var_name USING BTREE ON system_variables (`name`);
CREATE INDEX idx_sys_var_scope USING BTREE ON system_variables (`scope`);
CREATE INDEX idx_sys_var_source USING BTREE ON system_variables (`source`);
CREATE INDEX idx_sys_var_status USING BTREE ON system_variables (`status`);

CREATE TABLE `proc_ins_event` (
      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
      `event_seq_no` varchar(64) NOT NULL COMMENT '事件序列号',
      `event_type` varchar(64) DEFAULT NULL COMMENT '事件类型',
      `operation_data` varchar(255) DEFAULT NULL COMMENT '根数据',
      `operation_key` varchar(255) DEFAULT NULL COMMENT '编排key',
      `operation_user` varchar(255) DEFAULT NULL COMMENT '发起者',
      `proc_def_id` varchar(64) DEFAULT NULL COMMENT '编排定义id',
      `proc_ins_id` varchar(64) DEFAULT NULL COMMENT '编排实例id',
      `source_plugin` varchar(64) DEFAULT NULL COMMENT '来源',
      `status` varchar(64) DEFAULT NULL COMMENT '状态->created(初始化) | pending(处理中) | done(处理完成功运行编排) | fail(处理失败)',
      `created_time` datetime DEFAULT NULL COMMENT '创建时间',
      `host` varchar(64) DEFAULT NULL COMMENT '处理主机',
      `error_message` text DEFAULT NULL COMMENT '错误信息',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;