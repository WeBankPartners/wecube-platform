<!--编排普通执行-模板选择-->
<template>
  <div class="normal-execution-template">
    <div class="search">
      <Search :options="searchOptions" v-model="searchParams" @search="handleSearch"></Search>
    </div>
    <div class="template-card">
      <Card :bordered="false" dis-hover :padding="0">
        <template v-if="cardList.length">
          <Card v-for="(i, index) in cardList" :key="index" style="width: 100%; margin-bottom: 20px">
            <div class="custom-header" slot="title">
              <Icon size="28" type="ios-people" />
              <div class="title">
                {{ i.manageRoleDisplay }}
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
              <Table size="small" :columns="tableColumns" :data="i.dataList" />
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
import { debounce, deepClone } from '@/const/util'
import { flowList, collectFlow, unCollectFlow } from '@/api/server'
import dayjs from 'dayjs'
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
      searchParams: {
        procDefId: '',
        procDefName: '',
        plugins: [],
        updatedTime: [dayjs().subtract(3, 'month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
        updatedTimeStart: '',
        updatedTimeEnd: '',
        createdBy: '',
        scene: '', // 分组
        subProc: 'main',
        onlyCollect: false
      },
      cardList: [], // 模板数据
      spinShow: false,
      searchOptions: [
        {
          key: 'onlyCollect',
          label: this.$t('be_only_show_collect'),
          component: 'switch',
          initValue: false
        },
        {
          key: 'subProc',
          component: 'radio-group',
          list: [
            { label: this.$t('main_workflow'), value: 'main' },
            { label: this.$t('child_workflow'), value: 'sub' }
          ],
          initValue: 'main'
        },
        {
          key: 'updatedTime',
          label: this.$t('table_updated_date'),
          initDateType: 1,
          dateRange: [
            { label: '近3个月', type: 'month', value: 3, dateType: 1 },
            { label: '近半年', type: 'month', value: 6, dateType: 2 },
            { label: '近一年', type: 'year', value: 1, dateType: 3 },
            { label: this.$t('be_auto'), dateType: 4 } // 自定义
          ],
          labelWidth: 110,
          component: 'custom-time'
        },
        {
          key: 'procDefName',
          placeholder: this.$t('flow_name'),
          component: 'input'
        },
        {
          key: 'procDefId',
          placeholder: this.$t('workflow_id'),
          component: 'input'
        },
        {
          key: 'createdBy',
          placeholder: this.$t('createdBy'),
          component: 'input'
        }
      ],
      tableColumns: [
        {
          title: this.$t('flow_name'),
          key: 'name',
          minWidth: 220,
          render: (h, params) => {
            return (
              <div>
                {
                  /* 收藏 */
                  !params.row.collected && (
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
                  params.row.collected && (
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
                <Icon
                  type="ios-funnel-outline"
                  size="12"
                  style="cursor: pointer;margin-right:4px"
                  onClick={() => this.copyNameToSearch(params.row.name)}
                />
                <span
                  onClick={() => {
                    this.handleChooseTemplate(params.row)
                  }}
                >
                  {params.row.name}
                  <Tag style="margin-left:2px">{params.row.version}</Tag>
                </span>
              </div>
            )
          }
        },
        {
          title: this.$t('workflow_id'),
          minWidth: 180,
          ellipsis: true,
          key: 'id',
          render: (h, params) => {
            return (
              <div>
                <Tooltip content={params.row.id} placement="top">
                  <span>{params.row.id}</span>
                </Tooltip>
              </div>
            )
          }
        },
        {
          title: this.$t('be_instance_type'),
          key: 'rootEntity',
          minWidth: 200,
          render: (h, params) => {
            if (params.row.rootEntity !== '') {
              return <Tag color="default">{params.row.rootEntity}</Tag>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('be_createby_role'),
          key: 'createdBy',
          minWidth: 180,
          render: (h, params) => {
            return (
              <div style="display:flex;flex-direction:column">
                <span>{params.row.createdBy}</span>
                <span>{params.row.mgmtRolesDisplay && params.row.mgmtRolesDisplay[0]}</span>
              </div>
            )
          }
        },
        {
          title: this.$t('be_use_status'),
          key: 'status',
          minWidth: 120,
          render: (h, params) => {
            const list = [
              { label: this.$t('deployed'), value: 'deployed', color: '#19be6b' },
              { label: this.$t('draft'), value: 'draft', color: '#c5c8ce' },
              { label: this.$t('disabled'), value: 'disabled', color: '#ed4014' }
            ]
            const item = list.find(i => i.value === params.row.status)
            return item && <Tag color={item.color}>{item.label}</Tag>
          }
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 90
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        }
      ]
    }
  },
  mounted () {
    this.getTemplateList()
  },
  methods: {
    // 选择模板新建执行
    handleChooseTemplate (row) {
      this.$router.push({
        path: '/implementation/workflow-execution/normal-create',
        query: {
          templateId: row.id,
          type: this.searchParams.subProc
        }
      })
    },
    handleSearch () {
      this.getTemplateList()
    },
    // 选择编排名称后过滤
    copyNameToSearch (procDefName) {
      this.searchParams.procDefName = procDefName
      this.getTemplateList()
    },
    async getTemplateList () {
      const params = deepClone(this.searchParams)
      params.updatedTimeStart = params.updatedTime[0] ? params.updatedTime[0] + ' 00:00:00' : ''
      params.updatedTimeEnd = params.updatedTime[1] ? params.updatedTime[1] + ' 23:59:59' : ''
      params.permissionType = 'USE'
      params.status = 'deployed'
      delete params.updatedTime
      this.spinShow = true
      let { data, status } = await flowList(params)
      this.spinShow = false
      if (status === 'OK') {
        this.cardList = data.map(item => {
          return { ...item, expand: true }
        })
      }
    },
    // 展开收缩卡片
    handleExpand (item) {
      item.expand = !item.expand
    },
    // 收藏or取消收藏
    handleStar: debounce(async function ({ id, collected }) {
      const method = collected ? unCollectFlow : collectFlow
      const params = {
        procDefId: id
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
.normal-execution-template {
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
.normal-execution-template {
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
