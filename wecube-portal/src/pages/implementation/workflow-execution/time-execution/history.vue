<template>
  <div class="time-execution-history">
    <div class="search">
      <Search :options="searchOptions" v-model="searchConfig.params" @search="handleQuery"></Search>
      <Button :disabled="selectData.length === 0" type="error" class="btn-right" @click="batchStopTask">
        {{ $t('fe_batchStop') }}
      </Button>
    </div>
    <Table
      size="small"
      ref="table"
      :columns="tableColumns"
      :max-height="MODALHEIGHT"
      :data="tableData"
      :loading="loading"
      @on-selection-change="selectionChange"
    ></Table>
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
  getAllFlow,
  createWorkflowInstanceTerminationRequest,
  batchWorkflowInstanceTermination,
  pauseAndContinueFlow
} from '@/api/server'
import dayjs from 'dayjs'
export default {
  components: {
    Search
  },
  data() {
    return {
      MODALHEIGHT: 0,
      searchOptions: [
        // 执行时间
        {
          key: 'time',
          label: this.$t('execute_date'),
          initDateType: 1,
          dateRange: [
            {
              label: this.$t('be_threeDays_recent'),
              type: 'day',
              value: 3,
              dateType: 1
            },
            {
              label: this.$t('be_oneWeek_recent'),
              type: 'day',
              value: 7,
              dateType: 2
            },
            {
              label: this.$t('be_oneMonth_recent'),
              type: 'month',
              value: 1,
              dateType: 3
            },
            {
              label: this.$t('be_auto'),
              dateType: 4
            } // 自定义
          ],
          labelWidth: 110,
          component: 'custom-time'
        },
        // 任务名
        {
          key: 'name',
          placeholder: this.$t('fe_task_name'),
          component: 'input'
        },
        // 编排名称
        {
          key: 'procDefId',
          placeholder: this.$t('flow_name'),
          component: 'select',
          list: []
        },
        // 编排ID
        {
          key: 'id',
          placeholder: this.$t('fe_flowInstanceId'),
          component: 'input'
        },
        // 状态
        {
          key: 'status',
          placeholder: this.$t('flow_status'),
          component: 'tag-select',
          list: [
            {
              label: this.$t('fe_notStart'),
              value: 'NotStarted',
              color: '#808695'
            },
            {
              label: this.$t('fe_stop'),
              value: 'Stop',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgressFaulted'),
              value: 'InProgress(Faulted)',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgressTimeouted'),
              value: 'InProgress(Timeouted)',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgress'),
              value: 'InProgress',
              color: '#1990ff'
            },
            {
              label: this.$t('fe_completed'),
              value: 'Completed',
              color: '#7ac756'
            },
            {
              label: this.$t('fe_faulted'),
              value: 'Faulted',
              color: '#e29836'
            },
            {
              label: this.$t('fe_internallyTerminated'),
              value: 'InternallyTerminated',
              color: '#e29836'
            }
          ]
        },
        // 操作对象
        {
          key: 'entityDisplayName',
          placeholder: this.$t('bc_execution_instance'),
          component: 'input'
        }
      ],
      searchConfig: {
        params: {
          name: '',
          id: '',
          time: [dayjs().subtract(3, 'day')
            .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          startTime: '',
          endTime: '',
          procDefId: '',
          entityDisplayName: '',
          operator: 'systemCron',
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
      selectData: [],
      loading: false,
      tableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: this.$t('fe_task_name'),
          minWidth: 160,
          key: 'name',
          render: (h, params) => {
            if (params.row.scheduleJobName) {
              return (
                <span
                  style="cursor:pointer;color:#5cadff;"
                  onClick={() => {
                    this.jumpToHistory(params.row)
                  }}
                >
                  {params.row.scheduleJobName}
                </span>
              )
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('flow_name'),
          minWidth: 200,
          key: 'procInstName',
          render: (h, params) => (
            <div>
              <span>
                {params.row.procInstName}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
              </span>
            </div>
          )
        },
        {
          title: this.$t('fe_flowInstanceId'),
          minWidth: 200,
          key: 'id'
        },
        {
          title: this.$t('flow_status'),
          key: 'status',
          minWidth: 140,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('fe_notStart'),
                value: 'NotStarted',
                color: '#808695'
              },
              {
                label: this.$t('fe_inProgressFaulted'),
                value: 'InProgress(Faulted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgressTimeouted'),
                value: 'InProgress(Timeouted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_stop'),
                value: 'Stop',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgress'),
                value: 'InProgress',
                color: '#1990ff'
              },
              {
                label: this.$t('fe_completed'),
                value: 'Completed',
                color: '#7ac756'
              },
              {
                label: this.$t('fe_faulted'),
                value: 'Faulted',
                color: '#e29836'
              },
              {
                label: this.$t('fe_internallyTerminated'),
                value: 'InternallyTerminated',
                color: '#e29836'
              }
            ]
            const findObj = list.find(item => item.value === params.row.status) || {}
            return <Tag color={findObj.color}>{findObj.label}</Tag>
          }
        },
        // 操作对象
        {
          title: this.$t('bc_execution_instance'),
          key: 'entityDisplayName',
          minWidth: 160,
          render: (h, params) => {
            if (params.row.entityDisplayName !== '') {
              return <span>{params.row.entityDisplayName}</span>
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('fe_launcher'),
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
          render: (h, params) => (
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
              {['InProgress', 'InProgress(Faulted)', 'InProgress(Timeouted)'].includes(params.row.status) && (
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
              {['InProgress', 'InProgress(Faulted)', 'InProgress(Timeouted)', 'Stop'].includes(params.row.status) && (
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
            </div>
          )
        }
      ],
      users: []
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/implementation/workflow-execution/view-execution') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('search_timeExecution') || ''
        if (storage) {
          const { searchParams, searchOptions } = JSON.parse(storage)
          vm.searchConfig = searchParams
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
      searchParams: this.searchConfig,
      searchOptions: this.searchOptions
    }
    window.sessionStorage.setItem('search_timeExecution', JSON.stringify(storage))
  },
  methods: {
    initData() {
      this.MODALHEIGHT = document.body.scrollHeight - 220
      this.getFlows()
      this.getProcessInstances()
    },
    handleQuery() {
      this.pageable.current = 1
      this.getProcessInstances()
    },
    selectionChange(val) {
      this.selectData = val
    },
    // #region 暂停、继续编排
    async flowControlHandler(operateType, row) {
      this.$Modal.confirm({
        title:
          localStorage.getItem('username') !== row.operator
            ? this.$t('be_workflow_non_owner_title')
            : this.$t('bc_confirm') + ' ' + (operateType === 'stop' ? this.$t('pause') : this.$t('bc_continue')),
        content:
          localStorage.getItem('username') !== row.operator
            ? `${this.$t('be_workflow_non_owner_list_tip1')}[${row.operator}]${this.$t(
              'be_workflow_non_owner_list_tip2'
            )}`
            : '',
        'z-index': 1000000,
        onOk: async () => {
          const payload = {
            procInstId: row.id,
            act: operateType
          }
          this.loading = true
          const { status } = await pauseAndContinueFlow(payload)
          this.loading = false
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
    stopTask(row) {
      this.$Modal.confirm({
        title:
          localStorage.getItem('username') !== row.operator
            ? this.$t('be_workflow_non_owner_title')
            : this.$t('bc_confirm') + ' ' + this.$t('stop_orch'),
        content:
          localStorage.getItem('username') !== row.operator
            ? `${this.$t('be_workflow_non_owner_list_tip1')}[${row.operator}]${this.$t(
              'be_workflow_non_owner_list_tip2'
            )}`
            : '',
        'z-index': 1000000,
        onOk: async () => {
          const payload = {
            procInstId: row.id,
            procInstKey: row.procInstKey
          }
          this.loading = true
          const { status } = await createWorkflowInstanceTerminationRequest(payload)
          this.loading = false
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
    // 批量终止
    batchStopTask() {
      const list = this.selectData.filter(i => i.operator !== localStorage.getItem('username')) || []
      const operatorList = list.map(i => i.operator) || []
      const tips = Array.from(new Set(operatorList)).join('、')
      this.$Modal.confirm({
        title: tips ? this.$t('be_workflow_non_owner_title') : this.$t('bc_confirm') + ' ' + this.$t('stop_orch'),
        content: tips
          ? `${this.$t('be_workflow_non_owner_list_tip1')}[${tips}]${this.$t('be_workflow_non_owner_list_tip2')}`
          : '',
        'z-index': 1000000,
        onOk: async () => {
          const params = this.selectData.map(i => ({
            id: i.id
          }))
          this.loading = true
          const { status } = await batchWorkflowInstanceTermination(params)
          this.loading = false
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
    async getFlows() {
      const { status, data } = await getAllFlow(false)
      if (status === 'OK') {
        this.allFlows = data.sort((a, b) => {
          const s = a.createdTime.toLowerCase()
          const t = b.createdTime.toLowerCase()
          if (s > t) {
            return -1
          }
          if (s < t) {
            return 1
          }
        })
        this.allFlows = this.allFlows.map(item => ({
          label: `${item.procDefName} [${item.procDefVersion}]`,
          value: item.procDefId
        }))
        this.searchOptions.forEach(item => {
          if (item.key === 'procDefId') {
            item.list = this.allFlows
          }
        })
      }
    },
    changePageSize(pageSize) {
      this.pageable.current = 1
      this.pageable.pageSize = pageSize
      this.getProcessInstances()
    },
    changPage(current) {
      this.pageable.current = current
      this.getProcessInstances()
    },
    async getProcessInstances() {
      const params = {
        name: this.searchConfig.params.name !== '' ? this.searchConfig.params.name : undefined,
        id: this.searchConfig.params.id !== '' ? this.searchConfig.params.id : undefined,
        procDefId: this.searchConfig.params.procDefId !== '' ? this.searchConfig.params.procDefId : undefined,
        entityDisplayName:
          this.searchConfig.params.entityDisplayName !== '' ? this.searchConfig.params.entityDisplayName : undefined,
        operator: this.searchConfig.params.operator !== '' ? this.searchConfig.params.operator : undefined,
        status: this.searchConfig.params.status !== '' ? this.searchConfig.params.status : undefined,
        startTime: this.searchConfig.params.time[0] ? this.searchConfig.params.time[0] + ' 00:00:00' : undefined,
        endTime: this.searchConfig.params.time[1] ? this.searchConfig.params.time[1] + ' 23:59:59' : undefined,
        subProc: 'main', // 只查询主编排
        pageable: {
          startIndex: (this.pageable.current - 1) * this.pageable.pageSize,
          pageSize: this.pageable.pageSize
        }
      }
      this.tableData = []
      this.selectData = []
      this.loading = true
      const { status, data } = await instancesWithPaging(params)
      this.loading = false
      if (status === 'OK') {
        this.tableData = data.contents
        this.tableData.forEach(i => {
          // 禁用不能终止的表格复选框
          if (
            !(
              ['InProgress', 'InProgress(Faulted)', 'InProgress(Timeouted)', 'Stop'].includes(i.status)
              && !(i.parentProcIns && i.parentProcIns.procInsId)
            )
          ) {
            i._disabled = true
          }
        })
        this.pageable.total = data.pageInfo.totalRows
        this.pageable.pageSize = data.pageInfo.pageSize
        this.pageable.startIndex = data.pageInfo.startIndex
      }
    },
    async jumpToHistory(row) {
      const params = {
        id: row.id,
        operator: 'systemCron',
        pageable: {
          startIndex: 0,
          pageSize: 5000
        }
      }
      const { status, data } = await instancesWithPaging(params)
      if (status === 'OK') {
        const detail = Array.isArray(data.contents) && data.contents[0]
        // 能获取到历史记录，跳转，否则给出提示
        if (detail && detail.id) {
          return this.$router.push({
            path: '/implementation/workflow-execution/view-execution',
            query: {
              id: row.id,
              from: 'time'
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
.time-execution-history {
  .search {
    display: flex;
    justify-content: space-between;
    .btn-right {
      width: 90px;
      height: 28px;
      margin-left: 10px;
      padding: 0px;
    }
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
