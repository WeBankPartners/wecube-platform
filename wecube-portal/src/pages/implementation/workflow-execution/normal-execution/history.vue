<template>
  <div class="normal-execution-history">
    <div class="search">
      <Search :options="searchOptions" v-model="searchConfig.params" @search="handleQuery"></Search>
    </div>
    <Table size="small" ref="table" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
    <Page
      style="float: right; margin-top: 16px"
      :total="pageable.total"
      @on-change="changPage"
      show-sizer
      :current="pageable.current"
      :page-size="pageable.pageSize"
      @on-page-size-change="changePageSize"
      show-total
    />
  </div>
</template>

<script>
import Search from '@/pages/components/base-search.vue'
import {
  instancesWithPaging,
  getUserList,
  getAllFlow,
  createWorkflowInstanceTerminationRequest,
  pauseAndContinueFlow
} from '@/api/server'
import dayjs from 'dayjs'
export default {
  components: {
    Search
  },
  data () {
    return {
      MODALHEIGHT: 0,
      searchOptions: [
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
          key: 'time',
          label: '执行时间',
          dateType: 1,
          initValue: [dayjs().subtract(3, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          labelWidth: 110,
          component: 'custom-time'
        },
        {
          key: 'procDefId',
          placeholder: '编排名称',
          component: 'select',
          list: []
        },
        {
          key: 'id',
          placeholder: this.$t('workflow_id'),
          component: 'input'
        },
        {
          key: 'status',
          placeholder: '状态',
          component: 'select',
          list: [
            { label: 'NotStarted', value: 'NotStarted' },
            { label: 'InProgress', value: 'InProgress' },
            { label: 'Completed', value: 'Completed' },
            { label: 'Faulted', value: 'Faulted' },
            { label: 'Timeouted', value: 'Timeouted' },
            { label: 'InternallyTerminated', value: 'InternallyTerminated' },
            { label: 'Stop', value: 'Stop' }
          ]
        },
        {
          key: 'entityDisplayName',
          placeholder: '目标对象',
          component: 'input'
        },
        {
          key: 'operator',
          placeholder: '执行人',
          component: 'select',
          list: []
        }
      ],
      searchConfig: {
        params: {
          subProc: 'main',
          id: '',
          time: [dayjs().subtract(3, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          startTime: '',
          endTime: '',
          procDefId: '',
          entityDisplayName: '',
          operator: '',
          status: ''
        }
      },
      pageable: {
        pageSize: 10,
        startIndex: 1,
        current: 1,
        total: 0
      },
      allFlows: [],
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 50,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          minWidth: 200,
          key: 'procInstName',
          render: (h, params) => {
            return (
              <div>
                <span>
                  {params.row.procInstName}
                  <Tag style="margin-left:2px">{params.row.version}</Tag>
                </span>
              </div>
            )
          }
        },
        {
          title: this.$t('workflow_id'),
          minWidth: 200,
          key: 'id'
        },
        {
          title: this.$t('flow_status'),
          key: 'status',
          minWidth: 120
        },
        {
          title: this.$t('be_instance_type'),
          key: 'entityDisplayName',
          minWidth: 160,
          render: (h, params) => {
            if (params.row.entityDisplayName !== '') {
              return <Tag color="default">{params.row.entityDisplayName}</Tag>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('executor'),
          key: 'operator',
          minWidth: 120
        },
        {
          title: this.$t('execute_date'),
          key: 'createdTime',
          minWidth: 150
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 130,
          align: 'center',
          fixed: 'right',
          render: (h, params) => {
            return (
              <div style="display:flex;justify-content:center;">
                <Tooltip content={this.$t('be_details')} placement="top">
                  <Button
                    size="small"
                    type="info"
                    onClick={() => {
                      this.jumpToHistory(params.row) // 查看
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-eye" size="16"></Icon>
                  </Button>
                </Tooltip>
                {params.row.status === 'InProgress' && (
                  <Tooltip content={this.$t('pause')} placement="top">
                    <Button
                      size="small"
                      type="warning"
                      onClick={() => {
                        this.flowControlHandler('stop', params.row) // 暂停
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-pause" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {params.row.status === 'InProgress' && (
                  <Tooltip content={this.$t('stop_orch')} placement="top">
                    <Button
                      size="small"
                      type="error"
                      onClick={() => {
                        this.stopTask(params.row) // 终止
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-power" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {params.row.status === 'Stop' && (
                  <Tooltip content={this.$t('be_continue')} placement="top">
                    <Button
                      size="small"
                      type="success"
                      onClick={() => {
                        this.flowControlHandler('recover', params.row) // 继续
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-play" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
              </div>
            )
          }
        }
      ],
      users: []
    }
  },
  watch: {
    'searchConfig.params.subProc': {
      handler (val) {
        if (val === 'sub') {
          // 添加主编排列
          this.tableColumns.splice(3, 0, {
            title: this.$t('main_workflow'),
            width: 100,
            ellipsis: true,
            key: 'mainFlow',
            render: (h, params) => {
              return <span>{params.row.mainFlow || '-'}</span>
            }
          })
        } else if (val === 'main') {
          this.tableColumns = this.tableColumns.filter(i => i.key !== 'mainFlow')
        }
      },
      immediate: true
    }
  },
  async mounted () {
    const cacheParams = localStorage.getItem('history-execution-search-params')
    if (cacheParams) {
      await this.getFlows()
      const tmp = JSON.parse(cacheParams)
      // this.searchConfig.params.time = [tmp.startTime || '', tmp.endTime || '']
      this.searchConfig.params.id = tmp.id || ''
      // this.searchConfig.params.startTime = tmp.startTime || ''
      // this.searchConfig.params.endTime = tmp.endTime || ''
      this.searchConfig.params.procDefId = tmp.procDefId || ''
      this.searchConfig.params.entityDisplayName = tmp.entityDisplayName || ''
      this.searchConfig.params.operator = tmp.operator || ''
      this.searchConfig.params.status = tmp.status || ''
    }
    this.MODALHEIGHT = document.body.scrollHeight - 300
    this.getProcessInstances()
    this.getAllUsers()
  },
  beforeDestroy () {
    const selectParams = JSON.stringify(this.searchConfig.params)
    localStorage.setItem('history-execution-search-params', selectParams)
  },
  methods: {
    handleQuery () {
      this.getProcessInstances()
    },
    // #region 暂停、继续编排
    async flowControlHandler (operateType, row) {
      this.$Modal.confirm({
        title: this.$t('be_workflow_non_owner_title'),
        content: `${this.$t('be_workflow_non_owner_list_tip1')}[${row.operator}]${this.$t(
          'be_workflow_non_owner_list_tip2'
        )}`,
        'z-index': 1000000,
        onOk: async () => {
          let payload = {
            procInstId: row.id,
            act: operateType
          }
          const { status } = await pauseAndContinueFlow(payload)
          if (status === 'OK') {
            this.getProcessInstances()
            this.$Notice.success({
              title: 'Success',
              desc: 'Success'
            })
          }
        },
        onCancel: () => {}
      })
    },
    // 终止任务
    stopTask (row) {
      this.$Modal.confirm({
        title: this.$t('be_workflow_non_owner_title'),
        content: `${this.$t('be_workflow_non_owner_list_tip1')}[${row.operator}]${this.$t(
          'be_workflow_non_owner_list_tip2'
        )}`,
        'z-index': 1000000,
        onOk: async () => {
          const payload = {
            procInstId: row.id,
            procInstKey: row.procInstKey
          }
          const { status } = await createWorkflowInstanceTerminationRequest(payload)
          if (status === 'OK') {
            this.getProcessInstances()
            this.$Notice.success({
              title: 'Success',
              desc: 'Success'
            })
          }
        },
        onCancel: () => {}
      })
    },
    async getFlows () {
      let { status, data } = await getAllFlow(false)
      if (status === 'OK') {
        this.allFlows = data.sort((a, b) => {
          let s = a.createdTime.toLowerCase()
          let t = b.createdTime.toLowerCase()
          if (s > t) return -1
          if (s < t) return 1
        })
        this.allFlows = this.allFlows.map(item => {
          return {
            label: `${item.procDefName} [${item.procDefVersion}]`,
            value: item.procDefId
          }
        })
        this.searchOptions.forEach(item => {
          if (item.key === 'procDefId') {
            item.list = this.allFlows
          }
        })
      }
    },
    async getAllUsers () {
      let { status, data } = await getUserList()
      if (status === 'OK') {
        this.users = data.map(item => {
          return {
            label: item.username,
            value: item.username
          }
        })
        this.searchOptions.forEach(item => {
          if (item.key === 'operator') {
            item.list = this.users
          }
        })
      }
    },
    changePageSize (pageSize) {
      this.pageable.pageSize = pageSize
      this.getProcessInstances()
    },
    changPage (current) {
      this.pageable.current = current
      this.getProcessInstances()
    },
    async getProcessInstances () {
      const params = {
        subProc: this.searchConfig.params.subProc !== '' ? this.searchConfig.params.subProc : undefined,
        id: this.searchConfig.params.id !== '' ? this.searchConfig.params.id : undefined,
        procDefId: this.searchConfig.params.procDefId !== '' ? this.searchConfig.params.procDefId : undefined,
        entityDisplayName:
          this.searchConfig.params.entityDisplayName !== '' ? this.searchConfig.params.entityDisplayName : undefined,
        operator: this.searchConfig.params.operator !== '' ? this.searchConfig.params.operator : undefined,
        status: this.searchConfig.params.status !== '' ? this.searchConfig.params.status : undefined,
        startTime: this.searchConfig.params.time[0] ? this.searchConfig.params.time[0] + ' 00:00:00' : undefined,
        endTime: this.searchConfig.params.time[1] ? this.searchConfig.params.time[1] + '23:59:59' : undefined,
        pageable: {
          startIndex: (this.pageable.current - 1) * this.pageable.pageSize,
          pageSize: this.pageable.pageSize
        }
      }
      this.tableData = []
      let { status, data } = await instancesWithPaging(params)
      if (status === 'OK') {
        this.tableData = data.contents
        this.pageable.total = data.pageInfo.totalRows
        this.pageable.pageSize = data.pageInfo.pageSize
        this.pageable.startIndex = data.pageInfo.startIndex
      }
    },
    async jumpToHistory (row) {
      const params = {
        id: row.id,
        pageable: {
          startIndex: 0,
          pageSize: 5000
        }
      }
      let { status, data } = await instancesWithPaging(params)
      if (status === 'OK') {
        const detail = Array.isArray(data.contents) && data.contents[0]
        // 能获取到历史记录，跳转，否则给出提示
        if (detail && detail.id) {
          return this.$router.push({
            path: '/implementation/workflow-execution/view-execution',
            query: {
              id: row.id
            }
          })
        }
      }
      this.$Notice.warning({
        title: '',
        desc: this.$t('no_detail_warning')
      })
    }
    // getDate (dateRange, type) {
    //   if (type === 'date' && dateRange[1].slice(-8) === '00:00:00') {
    //     // type类型判断等于date,是为了防止用户手动选时间为 00:00:00 时触发，变成 '23:59:59'
    //     dateRange[1] = dateRange[1].slice(0, -8) + '23:59:59'
    //   }
    //   this.searchConfig.params.time = dateRange
    //   this.searchConfig.params.startTime = dateRange[0]
    //   this.searchConfig.params.endTime = dateRange[1]
    // }
  }
}
</script>

<style scoped lang="scss">
.normal-execution-history {
  .search {
    display: flex;
    justify-content: space-between;
  }
  .ivu-form-item {
    margin-bottom: 8px;
  }
  .item {
    width: 260px;
    margin: 8px;
  }
}
</style>
