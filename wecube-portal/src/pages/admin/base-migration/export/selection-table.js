export default {
  data() {
    return {
      // 角色表
      roleTableLoading: false,
      roleTableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: '角色key',
          minWidth: 200,
          key: 'name',
          render: (h, params) => <span>{params.row.name || '-'}</span>
        },
        {
          title: '角色显示名',
          key: 'displayName',
          minWidth: 200,
          render: (h, params) => <span>{params.row.displayName || '-'}</span>
        },
        {
          title: '角色邮箱',
          key: 'email',
          minWidth: 200,
          render: (h, params) => <span>{params.row.email || '-'}</span>
        }
      ],
      roleTableData: [],
      roleSelectionList: [],
      // 编排表
      flowTableLoading: false,
      flowTableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'name',
          minWidth: 100,
          render: (h, params) => (
            <div>
              {params.row.name}
              <Tag style="margin-left:2px">{params.row.version}</Tag>
            </div>
          )
        },
        {
          title: 'ID',
          minWidth: 60,
          ellipsis: true,
          key: 'id',
          render: (h, params) => (
            <div>
              <Tooltip content={params.row.id} placement="top">
                <span>{params.row.id.slice(0, 7)}...</span>
              </Tooltip>
            </div>
          )
        },
        {
          title: this.$t('authPlugin'),
          key: 'authPlugins',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.authPlugins.length > 0) {
              return params.row.authPlugins && params.row.authPlugins.map(i => <Tag>{i}</Tag>)
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('instance_type'),
          key: 'rootEntity',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.rootEntity !== '') {
              return <div>{params.row.rootEntity}</div>
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('be_mgmt_role'),
          key: 'mgmtRoles',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.mgmtRolesDisplay.length > 0) {
              return <BaseScrollTag list={params.row.mgmtRolesDisplay} />
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('use_role'),
          key: 'userRoles',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.userRolesDisplay.length > 0) {
              return <BaseScrollTag list={params.row.userRolesDisplay} />
            }
            return <span>-</span>
          }
        }
      ],
      flowTableData: [],
      flowSelectionList: [],
      flowSearchOptions: [
        {
          key: 'name',
          placeholder: '编排名称',
          component: 'input'
        },
        {
          key: 'id',
          placeholder: '编排ID',
          component: 'input'
        }
      ],
      flowSearchParams: {
        name: '',
        id: ''
      },
      // 批量执行表
      batchTableLoading: false,
      batchTableColumns: [
        {
          title: this.$t('be_template_name'),
          key: 'name',
          minWidth: 140,
          render: (h, params) => (
            <div>
              {
                /* 收藏 */
                params.row.isCollected === false && (
                  <Tooltip content={this.$t('bc_save')} placement="top-start">
                    <Icon
                      style="cursor:pointer;margin-right:5px;"
                      size="18"
                      type="ios-star-outline"
                      onClick={e => {
                        e.stopPropagation()
                        this.handleStar(params.row)
                      }}
                    />
                  </Tooltip>
                )
              }
              {
                /* 取消收藏 */
                params.row.isCollected === true && (
                  <Tooltip content={this.$t('be_cancel_save')} placement="top-start">
                    <Icon
                      style="cursor:pointer;margin-right:5px;"
                      size="18"
                      type="ios-star"
                      color="#ebac42"
                      onClick={e => {
                        e.stopPropagation()
                        this.handleStar(params.row)
                      }}
                    />
                  </Tooltip>
                )
              }
              <span style="margin-right:2px">{params.row.name}</span>
            </div>
          )
        },
        {
          title: this.$t('be_template_id'),
          key: 'id',
          minWidth: 100
        },
        {
          title: this.$t('pluginService'),
          key: 'pluginService',
          minWidth: 140
        },
        {
          title: this.$t('be_instance_type'),
          key: 'operateObject',
          minWidth: 120,
          render: (h, params) => params.row.operateObject && <Tag color="default">{params.row.operateObject}</Tag>
        },
        {
          title: this.$t('use_role'),
          key: 'useRole',
          minWidth: 120,
          render: (h, params) => (
            <div>
              {params.row.permissionToRole.USEDisplayName
                && params.row.permissionToRole.USEDisplayName.map(item => <Tag color="default">{item}</Tag>)}
            </div>
          )
        },
        {
          title: this.$t('be_use_status'),
          key: 'status',
          minWidth: 90,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('be_status_use'),
                value: 'available',
                color: '#19be6b'
              },
              {
                label: this.$t('be_status_draft'),
                value: 'draft',
                color: '#c5c8ce'
              },
              {
                label: this.$t('be_status_role'),
                value: 'unauthorized',
                color: '#ed4014'
              }
            ]
            const item = list.find(i => i.value === params.row.status)
            return item && <Tag color={item.color}>{item.label}</Tag>
          }
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 120
        }
      ],
      batchTableData: [],
      batchSelectionList: [],
      batchSearchOptions: [
        {
          key: 'name',
          placeholder: '模板名称',
          component: 'input'
        },
        {
          key: 'id',
          placeholder: '模板ID',
          component: 'input'
        }
      ],
      batchSearchParams: {},
      // ITSM表
      itsmTableLoading: false,
      itsmTableColumns: [
        {
          title: this.$t('name'),
          resizable: true,
          width: 200,
          key: 'name',
          render: (h, params) => {
            return (
              <span>{params.row.name}</span>
            )
          }
        },
        {
          title: this.$t('version'),
          minWidth: 60,
          key: 'version',
          render: (h, params) => {
            if (params.row.version) {
              return <Tag>{params.row.version}</Tag>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('tw_useScene'),
          minWidth: 80,
          key: 'type',
          render: (h, params) => {
            const find = this.typeList.find(i => i.value === String(params.row.type)) || {}
            return (find.label && <Tag>{find.label}</Tag>) || <span>-</span>
          }
        },
        {
          title: this.$t('procDefId'),
          minWidth: 100,
          key: 'procDefName',
          render: (h, params) => {
            if (params.row.procDefName) {
              return (
                <span>
                  {`${params.row.procDefName}`}
                  {params.row.procDefVersion && (
                    <span style="border:1px solid #e8eaec;border-radius:3px;background:#f7f7f7;padding:1px 4px;">
                      {params.row.procDefVersion}
                    </span>
                  )}
                </span>
              )
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('tags'),
          minWidth: 130,
          key: 'tags',
          render: (h, params) => {
            if (params.row.tags) {
              return <Tag>{params.row.tags}</Tag>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('description'),
          resizable: true,
          minWidth: 120,
          key: 'description',
          render: (h, params) => {
            return <BaseEllipsis content={params.row.description}></BaseEllipsis>
          }
        },
        {
          title: this.$t('tw_template_owner_role'),
          minWidth: 120,
          key: 'mgmtRoles',
          render: (h, params) => {
            return params.row.mgmtRoles.map(item => {
              return <Tag>{item.displayName}</Tag>
            })
          }
        },
        {
          title: this.$t('useRoles'),
          minWidth: 120,
          key: 'mgmtRoles',
          render: (h, params) => {
            const list = params.row.useRoles.map(item => {
              return item.displayName
            })
            return <BaseScrollTag list={list} />
          }
        },
        {
          title: this.$t('updatedBy'),
          minWidth: 100,
          key: 'updatedBy'
        },
        {
          title: this.$t('tm_updated_time'),
          minWidth: 130,
          key: 'updatedTime'
        }
      ],
      itsmTableData: [],
      itsmSelectionList: [],
      itsmSearchOptions: [
        {
          key: 'name',
          placeholder: '流程名称',
          component: 'input'
        },
        {
          key: 'scene',
          placeholder: '场景',
          component: 'input'
        }
      ],
      itsmSearchParams: {},
    }
  }
}
