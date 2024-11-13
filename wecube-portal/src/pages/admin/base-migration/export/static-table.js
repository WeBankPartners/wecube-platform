export default {
  data() {
    return {
      // cmdb CI
      cmdbCIColumns: [
        {
          title: this.$t('pe_ci_level'),
          key: 'group',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'cmdb-ci-level')
              }}
            >
              {params.row.group}
            </span>
          )
        },
        {
          title: this.$t('pe_ci_name'),
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.handleStaticTableLink(params.row, 'cmdb-ci-name')
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
                  this.handleOpenDetail(params.row, 'cmdb-ci')
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
      // 物料包
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
                  this.handleOpenDetail(params.row, 'artifacts')
                }}
              />
            </span>
          )
        }
      ],
      // 监控配置
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
            params.row.mapName = nameMap[params.row.name]
            return (
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.handleStaticTableLink(params.row, 'monitor')
                }}
              >
                {params.row.mapName || '-'}
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
                  this.handleOpenDetail(params.row, 'monitor')
                }}
              />
            </span>
          )
        }
      ],
      // 插件服务
      pluginColumns: [
        {
          title: this.$t('data_type'),
          key: 'name',
          render: (h, params) => <span>{params.row.name}</span>
        },
        {
          title: this.$t('pe_select_service'),
          key: 'total',
          width: 130,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.pluginInterfaceNum}</div>
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
            </span>
          )
        }
      ],
      // 导入监控基础配置
      importMonitorColumns: [
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
              custom_metric_monitor_type: this.$t('p_metric_monitor_type'),
              custom_metric_endpoint_group: this.$t('p_metric_endpoint_group'),
              custom_metric_service_group: this.$t('p_metric_service_group'),
              strategy_service_group: this.$t('p_strategy_service_group'),
              strategy_endpoint_group: this.$t('p_strategy_endpoint_group')
            }
            params.row.mapName = nameMap[params.row.name]
            return (
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.handleStaticTableLink(params.row, 'monitor')
                }}
              >
                {params.row.mapName || '-'}
              </span>
            )
          }
        },
        {
          title: this.$t('pe_monitor_query'),
          key: 'conditions',
          render: (h, params) => {
            const conditionsMap = {
              monitor_type: this.$t('p_monitor_type_des'),
              endpoint: this.$t('p_endpoint_des'),
              endpoint_group: this.$t('p_endpoint_group_des'),
              service_group: this.$t('p_service_group_des'),
              log_monitor_template: this.$t('p_log_monitor_template_des'),
              log_monitor_service_group: this.$t('p_log_monitor_service_group_des'),
              logKeyword_service_group: this.$t('p_logKeyword_service_group_des'),
              dashboard: this.$t('p_dashboard_des'),
              custom_metric_monitor_type: this.$t('p_custom_metric_monitor_type_des'),
              custom_metric_endpoint_group: this.$t('p_custom_metric_endpoint_group_des'),
              custom_metric_service_group: this.$t('p_custom_metric_service_group_des'),
              strategy_service_group: this.$t('p_strategy_service_group_des'),
              strategy_endpoint_group: this.$t('p_strategy_endpoint_group_des')
            }
            return <span>{conditionsMap[params.row.name] || '-'}</span>
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
                  this.handleOpenDetail(params.row, 'monitor')
                }}
              />
            </span>
          )
        }
      ],
      detailVisible: false,
      detailColumns: [],
      detailTableData: [],
      detailTitle: ''
    }
  },
  methods: {
    handleStaticTableLink(row, type) {
      let path = ''
      if (type === 'cmdb-ci-level') {
        path = `${window.location.origin}/#/wecmdb/data-mgmt-ci`
      } else if (type === 'cmdb-ci-name') {
        path = `${window.location.origin}/#/wecmdb/data-mgmt-ci?ciTypeId=${row.dataType}&name=${row.name}`
      } else if (type === 'cmdb-view') {
        path = `${window.location.origin}/#/wecmdb/data-mgmt-view?viewId=${row.dataType}`
      } else if (type === 'cmdb-report') {
        path = `${window.location.origin}/#/wecmdb/report-query?reportId=${row.dataType}`
      } else if (type === 'artifacts') {
        path = `${window.location.origin}/#/artifacts/implementation/artifact-management`
      } else if (row.name === 'monitor_type' && type === 'monitor') {
        path = `${window.location.origin}/#/adminConfig/typeConfig`
      } else if (row.name === 'endpoint_group' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/groupManagement`
      } else if (
        (row.name === 'metric_list'
          || ['custom_metric_service_group', 'custom_metric_endpoint_group', 'custom_metric_monitor_type'].includes(
            row.name
          ))
        && type === 'monitor'
      ) {
        path = `${window.location.origin}/#/monitorConfigIndex/metricConfig`
      } else if (row.name === 'log_monitor_service_group' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/businessMonitor`
      } else if (row.name === 'log_monitor_template' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/logTemplate`
      } else if (
        (row.name === 'strategy_list' || ['strategy_service_group', 'strategy_endpoint_group'].includes(row.name))
        && type === 'monitor'
      ) {
        path = `${window.location.origin}/#/monitorConfigIndex/thresholdManagement`
      } else if (row.name === 'logKeyword_service_group' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/logManagement`
      } else if (row.name === 'dashboard' && type === 'monitor') {
        path = `${window.location.origin}/#/viewConfigIndex/boardList`
      } else if (row.name === 'endpoint' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/endpointManagement`
      } else if (row.name === 'service_group' && type === 'monitor') {
        path = `${window.location.origin}/#/monitorConfigIndex/resourceLevel`
      }
      window.sessionStorage.currentPath = ''
      window.open(path, '_blank')
    },
    handleOpenDetail(row, type) {
      if (type === 'cmdb-ci') {
        this.detailTitle = row.name
        this.detailTableData = (row.data && Object.values(row.data)) || []
        this.detailVisible = true
        this.detailColumns = [
          {
            title: this.$t('name'),
            key: 'key_name'
          },
          {
            title: this.$t('createdBy'),
            key: 'create_user'
          },
          {
            title: this.$t('table_created_date'),
            key: 'create_time'
          }
        ]
      } else if (type === 'artifacts') {
        this.detailTitle = row.unitDesignName
        this.detailTableData = row.artifactRows || []
        this.detailVisible = true
        this.detailColumns = [
          {
            title: this.$t('package_name'),
            key: 'name'
          },
          {
            title: 'GUID',
            key: 'guid'
          },
          {
            title: this.$t('pi_package_type'),
            key: 'package_type',
            render: (h, params) => {
              const typeMap = {
                APP: this.$t('pi_app'),
                DB: this.$t('pi_db'),
                'APP&DB': this.$t('pi_app_db'),
                IMAGE: this.$t('pi_image')
              }
              return <span>{typeMap[params.row.package_type]}</span>
            }
          },
          {
            title: this.$t('status'),
            key: 'state',
            render: (h, params) => {
              const stateColor = {
                added_0: '#19be6b',
                added_1: '#19be6b',
                updated_0: '#5cadff',
                updated_1: '#5cadff',
                delete: '#ed4014',
                created: '#2b85e4',
                changed: 'purple',
                destroyed: '#ff9900'
              }
              const style = {
                color: stateColor[params.row.state] || '#2b85e4'
              }
              return <span style={style}>{params.row.state}</span>
            }
          },
          {
            title: this.$t('upload_by'),
            key: 'upload_user'
          }
        ]
      } else if (type === 'monitor') {
        this.detailTitle = row.mapName
        this.detailTableData = row.data || []
        this.detailVisible = true
        switch (row.name) {
          case 'monitor_type':
            this.detailColumns = [
              {
                title: '类型名',
                key: 'displayName'
              },
              {
                title: '对象数',
                key: 'objectCount'
              },
              {
                title: '创建人',
                key: 'createUser'
              },
              {
                title: '创建时间',
                key: 'createTime'
              }
            ]
            break
          case 'log_monitor_template':
            this.detailColumns = [
              {
                title: '模板名称',
                key: 'name'
              },
              {
                title: '模板类型',
                key: 'log_type'
              },
              {
                title: '更新人',
                key: 'update_time'
              },
              {
                title: '更新时间',
                key: 'update_user'
              }
            ]
            break
          case 'dashboard':
            this.detailColumns = [
              {
                title: '看板名',
                key: 'name'
              },
              {
                title: '看板id',
                key: 'id'
              },
              {
                title: '更新人',
                key: 'update_user'
              },
              {
                title: '更新时间',
                key: 'update_at_str'
              }
            ]
            break
          case 'service_group':
            this.detailColumns = [
              {
                title: '层级对象名',
                key: 'display_name'
              },
              {
                title: '层级对象类型',
                key: 'service_type'
              },
              {
                title: '更新人',
                key: 'update_user'
              },
              {
                title: '更新时间',
                key: 'update_time'
              }
            ]
            break
          default:
            this.detailTableData = row.data && row.data.map(i => ({ name: i }))
            this.detailColumns = [
              {
                title: '名称',
                key: 'name'
              }
            ]
        }
      }
    }
  }
}
