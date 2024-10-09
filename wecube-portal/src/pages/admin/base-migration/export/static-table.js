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
                this.jumpToHistory(params.row)
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
                  this.handleDetai(params.row, 'cmdb')
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
                this.jumpToHistory(params.row)
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
                this.jumpToHistory(params.row)
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
                this.jumpToHistory(params.row)
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
                  this.handleDetai(params.row, 'artifacts')
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
              monitor_type: this.$t('p_general_type'),
              endpoint_group: this.$t('p_endpoint_group'),
              log_monitor_template: this.$t('p_log_monitor_template'),
              log_monitor_service_group: this.$t('p_log_monitor_config'),
              metric_list: this.$t('p_metric_list'),
              strategy_list: this.$t('p_warning_list'),
              logKeyword_service_group: this.$t('p_keyword_list'),
              dashboard: this.$t('p_dashboard'),
              endpoint: this.$t('p_endpoint'),
              service_group: this.$t('p_endpoint_level')
            }
            return (
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.jumpToHistory(params.row)
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
              monitor_type: ['全选'],
              endpoint: ['CMDB'],
              endpoint_group: ['对象'],
              service_group: ['CMDB'],
              log_monitor_template: ['指标-业务配置'],
              log_monitor_service_group: ['层级对象'],
              metric_list: ['层级对象', '对象组', '基础类型'],
              strategy_list: ['层级对象', '对象组'],
              logKeyword_service_group: ['层级对象'],
              dashboard: ['层级对象', '对象']
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
                  this.handleDetai(params.row, 'monitor')
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
                this.jumpToHistory(params.row)
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
                  this.handleDetai(params.row, 'plugin')
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
                  this.handleDetai(params.row, 'plugin')
                }}
              />
            </span>
          )
        }
      ]
    }
  }
}
