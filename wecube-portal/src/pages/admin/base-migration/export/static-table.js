export default {
  data() {
    return {
      // cmdb CI
      cmdbCIColumns: [
        {
          title: this.$t('pe_ci_name'),
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'cmdb-ci')
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: this.$t('count'),
          key: 'total',
          width: 90,
          render: (h, params) => (
            <span style="display:flex;align-items:center;justify-content:space-between;">
              <div style="width:25px">{params.row.count}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  // this.handleOpenDetail(params.row, 'cmdb-ci')
                }}
              />
            </span>
          )
        }
      ],
      // cmdb视图
      cmdbViewColumns: [
        {
          title: this.$t('pe_view_name'),
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'cmdb-view')
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: this.$t('createdBy'),
          key: 'creator',
          width: 90,
          render: (h, params) => <span>{params.row.creator || '-'}</span>
        }
      ],
      // cmdb报表
      cmdbReportColumns: [
        {
          title: this.$t('pe_report_name'),
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'cmdb-report')
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: this.$t('createdBy'),
          key: 'creator',
          width: 90,
          render: (h, params) => <span>{params.row.creator || '-'}</span>
        }
      ],
      // 物料包数据
      artifactsColumns: [
        {
          title: this.$t('pe_unit_design'),
          key: 'unitDesignName',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'artifacts')
              }}
            >
              {params.row.unitDesignName}
            </span>
          )
        },
        {
          title: this.$t('pe_total_package'),
          key: 'artifactLen',
          width: 120,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.artifactLen}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  // this.handleOpenDetail(params.row, 'artifacts')
                }}
              />
            </span>
          )
        }
      ],
      // 监控数据
      monitorColumns: [
        {
          title: this.$t('data_type'),
          render: (h, params) => {
            const nameMap = {
              monitor_type: this.$t('p_general_type'), // 基础类型
              endpoint_group: this.$t('p_endpoint_group'), // 对象组
              log_monitor_template: this.$t('p_log_monitor_template'), // 指标-业务日志模版
              log_monitor_service_group: this.$t('p_log_monitor_config'), // 指标-业务配置
              logKeyword_service_group: this.$t('p_keyword_list'), // 告警关键字
              dashboard: this.$t('p_dashboard'), // 自定义看板
              endpoint: this.$t('p_endpoint'), // 对象(仅分析)
              service_group: this.$t('p_endpoint_level'), // 层级对象(仅分析)
              metric_list: this.$t('p_metric_list'), // 指标-指标列表
              strategy_list: this.$t('p_warning_list') // 告警-指标阈值
            }
            return (
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.handleStaticTableLink(params.row, 'monitor')
                }}
              >
                {nameMap[params.row.name] || '-'}
              </span>
            )
          }
        },
        {
          title: this.$t('pe_monitor_query'),
          key: 'conditions',
          render: (h, params) => {
            const conditionsMap = {
              monitor_type: [this.$t('select_all')],
              endpoint: ['CMDB'],
              endpoint_group: [this.$t('object')],
              service_group: ['CMDB'],
              log_monitor_template: [this.$t('p_log_monitor_config')],
              log_monitor_service_group: [this.$t('p_level_object')],
              metric_list: [this.$t('p_level_object'), this.$t('p_endpoint_group'), this.$t('p_general_type')],
              strategy_list: [this.$t('p_level_object'), this.$t('p_endpoint_group')],
              logKeyword_service_group: [this.$t('p_level_object')],
              dashboard: [this.$t('p_level_object'), this.$t('object')]
            }
            return conditionsMap[params.row.name].map(item => <Tag>{item}</Tag>)
          }
        },
        {
          title: this.$t('pe_select'),
          key: 'total',
          width: 100,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.count}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  // this.handleOpenDetail(params.row, 'monitor')
                }}
              />
            </span>
          )
        }
      ],
      pluginColumns: [
        {
          title: this.$t('data_type'),
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'plugin')
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: this.$t('pe_select_service'),
          key: 'total',
          width: 130,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.pluginInterfaceNum}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  // this.handleOpenDetail(params.row, 'plugin')
                }}
              />
            </span>
          )
        },
        {
          title: this.$t('pe_select_system'),
          key: 'total',
          width: 130,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.systemVariableNum}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  // this.handleOpenDetail(params.row, 'plugin')
                }}
              />
            </span>
          )
        }
      ]
    }
  },
  methods: {
    handleStaticTableLink(type, row) {
      return type + row
    },
    handleOpenDetail(type, row) {
      return type + row
    }
  }
}
