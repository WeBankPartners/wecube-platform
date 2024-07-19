<!--批量执行-模板管理-->
<template>
  <div class="batch-execution-template-list">
    <div class="search">
      <Search :options="searchOptions" v-model="form" @search="handleSearch"></Search>
    </div>
    <div class="template-card">
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
              <Table size="small" :columns="tableColumns" :data="i.data" @on-row-click="handleChooseTemplate" />
            </div>
          </Card>
        </template>
        <div v-else class="no-data">{{ $t('noData') }}</div>
        <Spin fix v-if="spinShow">
          <Icon type="ios-loading" size="44"></Icon>
        </Spin>
      </Card>
    </div>
  </div>
</template>

<script>
import Search from '@/pages/components/base-search.vue'
import { getBatchExecuteTemplateList, collectBatchTemplate, uncollectBatchTemplate } from '@/api/server'
import { debounce } from '@/const/util'
export default {
  components: {
    Search
  },
  props: {
    from: {
      type: String,
      default: 'template'
    }
  },
  data () {
    return {
      form: {
        name: '',
        pluginService: '',
        operateObject: '',
        isShowCollectTemplate: false, // 仅展示收藏模板
        permissionType: 'USE' // 权限类型
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
          render: (h, params) => {
            return (
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
          }
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
          render: (h, params) => {
            return params.row.operateObject && <Tag color="default">{params.row.operateObject}</Tag>
          }
        },
        status: {
          title: this.$t('be_use_status'),
          key: 'status',
          minWidth: 90,
          render: (h, params) => {
            const list = [
              { label: this.$t('be_status_use'), value: 'available', color: '#19be6b' },
              { label: this.$t('be_status_draft'), value: 'draft', color: '#c5c8ce' },
              { label: this.$t('be_status_role'), value: 'unauthorized', color: '#ed4014' }
            ]
            const item = list.find(i => i.value === params.row.status)
            return item && <Tag color={item.color}>{item.label}</Tag>
          }
        },
        createdTime: {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 120
        }
      }
    }
  },
  mounted () {
    // 新建执行页面
    this.tableColumns = [
      this.baseColumns.name,
      this.baseColumns.id,
      this.baseColumns.pluginService,
      this.baseColumns.operateObject,
      {
        title: this.$t('be_createby_role'),
        key: 'createdBy',
        minWidth: 90,
        render: (h, params) => {
          return (
            <div style="display:flex;flex-direction:column">
              <span>{params.row.createdBy}</span>
              <span>
                {params.row.permissionToRole.MGMTDisplayName && params.row.permissionToRole.MGMTDisplayName[0]}
              </span>
            </div>
          )
        }
      },
      this.baseColumns.status,
      this.baseColumns.createdTime
    ]
    this.getTemplateList()
  },
  methods: {
    // 选择模板新建执行
    handleChooseTemplate (row) {
      if (row.status === 'unauthorized') {
        return this.$Notice.warning({
          title: this.$t('warning'),
          desc: this.$t('be_template_role_tips')
        })
      }
      this.$router.push({
        path: '/implementation/batch-execution/create-execution',
        query: {
          id: row.id,
          from: 'template'
        }
      })
    },
    handleSearch () {
      this.getTemplateList()
    },
    async getTemplateList () {
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
        let useGroup = []
        data.contents.forEach(item => {
          if (item.permissionToRole.USEDisplayName && item.permissionToRole.USEDisplayName.length > 0) {
            item.permissionToRole.USEDisplayName.forEach(role => {
              useGroup.push(role)
            })
          }
        })
        useGroup = Array.from(new Set(useGroup))
        this.cardList = useGroup.map(role => {
          const group = {
            expand: true,
            data: [],
            role: role
          }
          data.contents.forEach(item => {
            if (item.permissionToRole.USEDisplayName && item.permissionToRole.USEDisplayName.includes(role)) {
              group.data.push(item)
            }
          })
          return group
        })
      }
    },
    // 展开收缩卡片
    handleExpand (item) {
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
    }, 300)
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
