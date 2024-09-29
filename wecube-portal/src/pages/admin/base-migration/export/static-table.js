export default {
  data() {
    return {
      // cmdb CI
      cmdbCIColumns: [
        {
          title: 'CI名称',
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
          title: '总数',
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
          title: '视图名',
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
          title: '创建人',
          key: 'creator',
          width: 90,
          render: (h, params) => <span>{params.row.creator || '-'}</span>
        }
      ],
      // cmdb报表
      cmdbReportColumns: [
        {
          title: '报表名',
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
          title: '创建人',
          key: 'creator',
          width: 90,
          render: (h, params) => <span>{params.row.creator || '-'}</span>
        }
      ],
      // 物料包数据
      artifactsColumns: [
        {
          title: '所属单元设计',
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
          title: '总包数',
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
          title: '数据类型',
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
          title: '监控配置查询条件',
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
          title: '已选',
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
          title: '数据类型',
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
          title: '已选服务',
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
          title: '已选系统参数',
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
