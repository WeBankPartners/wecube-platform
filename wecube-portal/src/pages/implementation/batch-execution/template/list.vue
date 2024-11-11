<!--批量执行-模板管理-->
<template>
  <div class="batch-execution-template-list">
    <div class="search">
      <BaseSearch :options="searchOptions" v-model="form" @search="handleSearch"></BaseSearch>
      <!--新建模板-->
      <Button type="success" class="create-template" @click="handleCreateTemplate">{{ $t('be_new_template') }}</Button>
    </div>
    <div class="template-card">
      <Tabs v-model="publishStatus" @on-click="handleSearch">
        <!--已发布-->
        <TabPane :label="$t('deployed')" name="published"></TabPane>
        <!--我的草稿-->
        <TabPane :label="$t('draft')" name="draft"></TabPane>
      </Tabs>
      <Card :bordered="false" dis-hover :padding="0">
        <template v-if="cardList.length">
          <Card v-for="(i, index) in cardList" :key="index" style="width: 100%; margin-bottom: 20px">
            <div class="custom-header" slot="title">
              <Icon size="28" type="ios-people" />
              <div class="title">
                {{ i.role }}
                <span class="underline"></span>
              </div>
              <Icon
                v-if="i.expand"
                size="28"
                type="md-arrow-dropdown"
                style="cursor: pointer"
                @click="handleExpand(i)"
              />
              <Icon v-else size="28" type="md-arrow-dropright" style="cursor: pointer" @click="handleExpand(i)" />
            </div>
            <div v-show="i.expand">
              <Table size="small" :columns="tableColumns" :data="i.data" />
            </div>
          </Card>
        </template>
        <div v-else class="no-data">{{ $t('noData') }}</div>
        <Spin fix v-if="spinShow">
          <Icon type="ios-loading" size="44"></Icon>
        </Spin>
      </Card>
    </div>
    <!--权限弹窗-->
    <AuthDialog ref="authDialog" :useRolesRequired="true" @sendAuth="handleUpdateRole" />
  </div>
</template>

<script>
import AuthDialog from '@/pages/components/auth.vue'
import {
  getBatchExecuteTemplateList,
  updateExecuteTemplateRole,
  deleteExecuteTemplate,
  collectBatchTemplate,
  uncollectBatchTemplate
} from '@/api/server'
import { debounce } from '@/const/util'
export default {
  components: {
    AuthDialog
  },
  data() {
    return {
      form: {
        name: '',
        pluginService: '',
        operateObject: '',
        isShowCollectTemplate: false, // 仅展示收藏模板
        permissionType: 'MGMT'
      },
      publishStatus: this.$route.query.status || 'published', // published已发布，draft草稿
      cardList: [], // 模板数据
      tableColumns: [],
      editRow: {},
      spinShow: false,
      searchOptions: [
        {
          key: 'isShowCollectTemplate',
          label: this.$t('be_only_show_collect'),
          component: 'switch',
          initValue: false
        },
        {
          key: 'name',
          placeholder: this.$t('be_template_name'),
          component: 'input'
        },
        {
          key: 'id',
          placeholder: this.$t('be_template_id'),
          component: 'input'
        },
        {
          key: 'pluginService',
          placeholder: this.$t('pluginService'),
          component: 'input'
        },
        {
          key: 'operateObject',
          placeholder: this.$t('be_instance_type'),
          component: 'input'
        }
      ],
      baseColumns: {
        name: {
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
        id: {
          title: this.$t('be_template_id'),
          key: 'id',
          minWidth: 100
        },
        pluginService: {
          title: this.$t('pluginService'),
          key: 'pluginService',
          minWidth: 140
        },
        operateObject: {
          title: this.$t('be_instance_type'),
          key: 'operateObject',
          minWidth: 120,
          render: (h, params) => params.row.operateObject && <Tag color="default">{params.row.operateObject}</Tag>
        },
        status: {
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
        createdTime: {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 120
        },
        action: {
          title: this.$t('table_action'),
          key: 'action',
          width: 165,
          align: 'center',
          fixed: 'right',
          render: (h, params) => (
            <div style="display:flex;justify-content:center;">
              {
                /* 查看 */
                this.publishStatus === 'published' && (
                  <Tooltip content={this.$t('view')} placement="top">
                    <Button
                      size="small"
                      type="info"
                      onClick={() => {
                        this.handleView(params.row)
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-eye" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )
              }
              {
                /* 复制 */
                this.publishStatus === 'published' && (
                  <Tooltip content={this.$t('copy')} placement="top">
                    <Button
                      size="small"
                      type="success"
                      onClick={() => {
                        this.handleCopy(params.row)
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-copy" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )
              }
              {
                /* 编辑 */
                this.publishStatus === 'draft' && (
                  <Tooltip content={this.$t('edit')} placement="top">
                    <Button
                      size="small"
                      type="primary"
                      onClick={() => {
                        this.handleEdit(params.row)
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-create" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )
              }
              {
                /* 权限 */
                this.publishStatus === 'published' && (
                  <Tooltip content={this.$t('config_permission')} placement="top">
                    <Button
                      size="small"
                      type="warning"
                      onClick={() => {
                        this.editRow = params.row
                        const mgmtRole = params.row.permissionToRole.MGMT || []
                        const useRole = params.row.permissionToRole.USE || []
                        this.$refs.authDialog.startAuth(mgmtRole, useRole)
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-person" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )
              }
              {
                /* 删除 */
                <Tooltip content={this.$t('delete')} placement="top">
                  <Button
                    size="small"
                    type="error"
                    onClick={() => {
                      this.handleDelete(params.row)
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-trash" size="16"></Icon>
                  </Button>
                </Tooltip>
              }
            </div>
          )
        }
      }
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/implementation/batch-execution/template-create') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('search_batchTemplate') || ''
        if (storage) {
          const { searchParams, searchOptions } = JSON.parse(storage)
          vm.form = searchParams
          vm.searchOptions = searchOptions
        }
      }
      // 列表刷新不能放在mounted, mounted会先执行，导致拿不到缓存参数
      vm.initData()
    })
  },
  beforeDestroy() {
    // 缓存列表搜索条件
    const storage = {
      searchParams: this.form,
      searchOptions: this.searchOptions
    }
    window.sessionStorage.setItem('search_batchTemplate', JSON.stringify(storage))
  },
  methods: {
    initData() {
      // 模板管理页面
      this.tableColumns = [
        this.baseColumns.name,
        this.baseColumns.id,
        this.baseColumns.pluginService,
        this.baseColumns.operateObject,
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
        this.baseColumns.status,
        this.baseColumns.createdTime,
        this.baseColumns.action
      ]
      this.getTemplateList()
    },
    handleCreateTemplate() {
      this.$router.push({
        path: '/implementation/batch-execution/template-create'
      })
    },
    handleSearch() {
      this.getTemplateList()
    },
    async getTemplateList() {
      const params = {
        filters: [],
        paging: true,
        pageable: {
          startIndex: 0,
          pageSize: 1000
        },
        sorting: [
          {
            asc: false,
            field: 'updatedTimeT'
          }
        ]
      }
      Object.keys(this.form).forEach(key => {
        if (this.form[key] || typeof this.form[key] === 'boolean') {
          params.filters.push({
            name: key,
            operator: ['isShowCollectTemplate', 'permissionType'].includes(key) ? 'eq' : 'contains',
            value: this.form[key]
          })
        }
      })
      params.filters.push({
        name: 'publishStatus',
        operator: 'eq',
        value: this.publishStatus
      })
      this.spinShow = true
      const { status, data } = await getBatchExecuteTemplateList(params)
      this.spinShow = false
      if (status === 'OK') {
        // 模板列表页按属主角色分组展示
        let mgmtGroup = []
        data.contents.forEach(item => {
          if (item.permissionToRole.MGMTDisplayName && item.permissionToRole.MGMTDisplayName.length > 0) {
            item.permissionToRole.MGMTDisplayName.forEach(role => {
              mgmtGroup.push(role)
            })
          }
        })
        mgmtGroup = Array.from(new Set(mgmtGroup))
        this.cardList = mgmtGroup.map(role => {
          const group = {
            expand: true,
            data: [],
            role
          }
          data.contents.forEach(item => {
            if (item.permissionToRole.MGMTDisplayName && item.permissionToRole.MGMTDisplayName.includes(role)) {
              group.data.push(item)
            }
          })
          return group
        })
      }
    },
    // 展开收缩卡片
    handleExpand(item) {
      item.expand = !item.expand
    },
    // 收藏or取消收藏
    handleStar: debounce(async function ({ id, isCollected }) {
      const method = isCollected ? uncollectBatchTemplate : collectBatchTemplate
      const params = {
        batchExecutionTemplateId: id
      }
      const { status } = await method(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.getTemplateList()
      }
    }, 300),
    // 查看
    handleView(row) {
      this.$router.push({
        path: '/implementation/batch-execution/template-create',
        query: {
          id: row.id,
          type: 'view'
        }
      })
    },
    // 复制
    handleCopy(row) {
      this.$router.push({
        path: '/implementation/batch-execution/template-create',
        query: {
          id: row.id,
          type: 'copy'
        }
      })
    },
    // 编辑草稿
    handleEdit(row) {
      this.$router.push({
        path: '/implementation/batch-execution/template-create',
        query: {
          id: row.id,
          type: 'edit'
        }
      })
    },
    // 更新权限
    async handleUpdateRole(mgmtRole, useRole) {
      const params = {
        id: this.editRow.id,
        permissionToRole: {
          MGMT: mgmtRole,
          USE: useRole
        }
      }
      const { status } = await updateExecuteTemplateRole(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.getTemplateList()
      }
    },
    // 删除
    async handleDelete(row) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        loading: true,
        onOk: async () => {
          this.$Modal.remove()
          const { status } = await deleteExecuteTemplate(row.id)
          if (status === 'OK') {
            this.$Notice.success({
              title: this.$t('successful'),
              desc: this.$t('successful')
            })
            this.getTemplateList()
          }
        },
        onCancel: () => {}
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execution-template-list {
  width: 100%;
  .search {
    display: flex;
    justify-content: space-between;
    margin-top: 15px;
  }
  .switch {
    display: flex;
    align-items: center;
    color: #515a6e;
    span {
      margin-right: 10px;
    }
  }
  .template-card {
    .custom-header {
      display: flex;
      align-items: center;
      .title {
        font-size: 16px;
        font-weight: bold;
        margin: 0 10px;
        .underline {
          display: block;
          margin-top: -10px;
          margin-left: -6px;
          width: 100%;
          padding: 0 6px;
          height: 12px;
          border-radius: 12px;
          background-color: #c6eafe;
          box-sizing: content-box;
        }
      }
    }
    .no-data {
      width: 100%;
      height: 400px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #515a6e;
    }
  }
}
</style>
<style lang="scss">
.batch-execution-template-list {
  .ivu-tooltip {
    width: auto !important;
  }
  .ivu-card-head {
    border-bottom: 1px solid #e8eaec;
    padding: 5px 10px;
    line-height: 1;
  }
  .template-card .ivu-table-row {
    cursor: pointer;
  }
  .ivu-form-item {
    margin-bottom: 10px !important;
    display: inline-block !important;
  }
  .ivu-tag {
    display: inline-block;
    line-height: 16px;
    height: auto;
    padding: 4px;
  }
  .icon-btn {
    cursor: pointer;
  }
}
</style>
