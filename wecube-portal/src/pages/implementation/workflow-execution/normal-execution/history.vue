<template>
  <div class="normal-execution-history">
    <div class="search">
      <BaseSearch :options="searchOptions" v-model="searchConfig.params" @search="handleQuery"></BaseSearch>
      <Button :disabled="selectData.length === 0" type="error" class="btn-right" @click="batchStopTask">
        {{ $t('fe_batchStop') }}
      </Button>
    </div>
    <!--子编排列表支持主编排搜索-->
    <div v-if="searchConfig.params.subProc === 'sub'" class="extra-search">
      <Select
        v-model="searchConfig.params.mainProcInsId"
        style="width: 600px; margin-bottom: 10px"
        filterable
        clearable
        :placeholder="$t('main_workflow')"
        @on-change="handleQuery"
        @on-open-change="getAllFlowInstances"
      >
        <Option
          v-for="item in allFlowInstances"
          :value="item.id"
          :key="item.id"
          :label="
            item.procInstName +
            '  ' +
            '[' +
            item.version +
            ']  ' +
            item.entityDisplayName +
            '  ' +
            (item.operator || 'operator') +
            '  ' +
            (item.createdTime || '0000-00-00 00:00:00') +
            '  ' +
            getStatusStyleAndName(item.displayStatus, 'label')
          "
        >
          <div style="display: flex; justify-content: space-between">
            <div>
              <span style="color: #2b85e4">{{ item.procInstName + ' ' }}</span>
              <span style="color: #2b85e4">{{ '[' + item.version + '] ' }}</span>
              <div
                :style="{
                  backgroundColor: '#c5c8ce',
                  padding: '4px 15px',
                  width: 'fit-content',
                  color: '#fff',
                  borderRadius: '4px',
                  display: 'inline-block',
                  marginLeft: '10px'
                }"
              >
                {{ item.entityDisplayName + ' ' }}
              </div>
            </div>
            <div style="display: flex; align-items: center">
              <span style="color: #515a6e; margin-right: 20px">{{ item.operator || 'operator' }}</span>
              <span style="color: #ccc">{{ (item.createdTime || '0000-00-00 00:00:00') + ' ' }}</span>
              <div style="width: 100px">
                <span :style="getStatusStyleAndName(item.displayStatus, 'style')">{{
                  getStatusStyleAndName(item.displayStatus, 'label')
                }}</span>
              </div>
            </div>
          </div>
        </Option>
      </Select>
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
import {
  instancesWithPaging,
  getUserList,
  getAllFlow,
  createWorkflowInstanceTerminationRequest,
  batchWorkflowInstanceTermination,
  pauseAndContinueFlow,
  getProcessInstances
} from '@/api/server'
import dayjs from 'dayjs'
export default {
  data() {
    return {
      MODALHEIGHT: 0,
      searchOptions: [
        // 是否子编排
        {
          key: 'subProc',
          component: 'radio-group',
          list: [
            {
              label: this.$t('main_workflow'),
              value: 'main'
            },
            {
              label: this.$t('child_workflow'),
              value: 'sub'
            }
          ],
          initValue: 'main'
        },
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
        },
        // 执行人
        {
          key: 'operator',
          placeholder: this.$t('executor'),
          component: 'select',
          list: []
        }
      ],
      searchConfig: {
        params: {
          subProc: this.$route.query.subProc || 'main',
          id: '',
          time: [dayjs().subtract(3, 'day')
            .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          startTime: '',
          endTime: '',
          procDefId: '',
          entityDisplayName: '',
          operator: '',
          status: '',
          rootEntityGuid: '',
          mainProcInsId: ''
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
          title: this.$t('flow_name'),
          minWidth: 150,
          key: 'procInstName',
          render: (h, params) => (
            <div>
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.jumpToHistory(params.row)
                }}
              >
                {params.row.procInstName}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
              </span>
            </div>
          )
        },
        {
          title: this.$t('fe_flowInstanceId'),
          minWidth: 120,
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
              {['InProgress', 'InProgress(Faulted)', 'InProgress(Timeouted)', 'Stop'].includes(params.row.status)
                && !(params.row.parentProcIns && params.row.parentProcIns.procInsId) && (
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
      users: [],
      allFlowInstances: []
    }
  },
  computed: {
    getStatusStyleAndName() {
      return function (status, type) {
        const list = [
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
        const findObj = list.find(i => i.value === status) || {}
        if (type === 'style') {
          return {
            display: 'inline-block',
            backgroundColor: findObj.color,
            padding: '4px 10px',
            width: 'fit-content',
            color: '#fff',
            borderRadius: '4px',
            float: 'right',
            fontSize: '12px',
            marginLeft: '5px'
          }
        }
        return findObj.label
      }
    }
  },
  watch: {
    'searchConfig.params.subProc': {
      handler(val) {
        this.searchConfig.params.mainProcInsId = ''
        if (val === 'sub') {
          // 添加主编排列
          const hasFlag = this.tableColumns.some(i => i.key === 'parentProcIns')
          if (!hasFlag) {
            this.tableColumns.splice(
              3,
              0,
              ...[
                {
                  title: this.$t('main_workflow'),
                  minWidth: 150,
                  key: 'parentProcIns',
                  render: (h, params) => {
                    if (params.row.parentProcIns && params.row.parentProcIns.procDefName) {
                      return (
                        <span
                          style="cursor:pointer;color:#5cadff;"
                          onClick={() => {
                            this.viewParentFlowGraph(params.row)
                          }}
                        >
                          {params.row.parentProcIns.procDefName}
                          <Tag style="margin-left:2px">{params.row.parentProcIns.version}</Tag>
                        </span>
                      )
                    }
                    return <span>-</span>
                  }
                },
                {
                  title: this.$t('main_workflow_id'),
                  minWidth: 120,
                  key: 'parentProcInsId',
                  render: (h, params) => <span>{(params.row.parentProcIns && params.row.parentProcIns.procInsId) || '-'}</span>
                }
              ]
            )
          }
        } else if (val === 'main') {
          this.tableColumns = this.tableColumns.filter(i => !['parentProcIns', 'parentProcInsId'].includes(i.key))
        }
      },
      immediate: true
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/implementation/workflow-execution/view-execution') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('search_normalExecution') || ''
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
    window.sessionStorage.setItem('search_normalExecution', JSON.stringify(storage))
  },
  mounted() {
    this.searchConfig.params.entityDisplayName = this.$route.query.entityDisplayName || ''
    this.searchConfig.params.rootEntityGuid = this.$route.query.rootEntityGuid || ''
  },
  methods: {
    initData() {
      this.MODALHEIGHT = document.body.scrollHeight - 220
      this.getFlows()
      this.getProcessInstances()
      this.getAllUsers()
    },
    // 查看主编排
    viewParentFlowGraph(row) {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/implementation/workflow-execution/view-execution?id=${row.parentProcIns.procInsId}&from=main&subProc=${this.searchConfig.params.subProc}`
      window.open(path, '_blank')
    },
    selectionChange(val) {
      this.selectData = val
    },
    handleQuery() {
      this.pageable.current = 1
      this.getProcessInstances()
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
    async getAllUsers() {
      const { status, data } = await getUserList()
      if (status === 'OK') {
        this.users = data.map(item => ({
          label: item.username,
          value: item.username
        }))
        this.searchOptions.forEach(item => {
          if (item.key === 'operator') {
            item.list = this.users
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
        subProc: this.searchConfig.params.subProc !== '' ? this.searchConfig.params.subProc : undefined,
        id: this.searchConfig.params.id !== '' ? this.searchConfig.params.id : undefined,
        procDefId: this.searchConfig.params.procDefId !== '' ? this.searchConfig.params.procDefId : undefined,
        entityDisplayName:
          this.searchConfig.params.entityDisplayName !== '' ? this.searchConfig.params.entityDisplayName : undefined,
        rootEntityGuid:
          this.searchConfig.params.rootEntityGuid !== '' ? this.searchConfig.params.rootEntityGuid : undefined,
        operator: this.searchConfig.params.operator !== '' ? this.searchConfig.params.operator : undefined,
        status: this.searchConfig.params.status !== '' ? this.searchConfig.params.status : undefined,
        startTime: this.searchConfig.params.time[0] ? this.searchConfig.params.time[0] + ' 00:00:00' : undefined,
        endTime: this.searchConfig.params.time[1] ? this.searchConfig.params.time[1] + ' 23:59:59' : undefined,
        mainProcInsId:
          this.searchConfig.params.mainProcInsId !== '' ? this.searchConfig.params.mainProcInsId : undefined,
        pageable: {
          startIndex: (this.pageable.current - 1) * this.pageable.pageSize,
          pageSize: this.pageable.pageSize
        }
      }
      if (params.subProc === 'main') {
        delete params.mainProcInsId
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
              subProc: this.searchConfig.params.subProc,
              from: 'normal'
            }
          })
        }
      }
      this.$Notice.warning({
        title: '',
        desc: this.$t('no_detail_warning')
      })
    },
    // getDate (dateRange, type) {
    //   if (type === 'date' && dateRange[1].slice(-8) === '00:00:00') {
    //     // type类型判断等于date,是为了防止用户手动选时间为 00:00:00 时触发，变成 '23:59:59'
    //     dateRange[1] = dateRange[1].slice(0, -8) + '23:59:59'
    //   }
    //   this.searchConfig.params.time = dateRange
    //   this.searchConfig.params.startTime = dateRange[0]
    //   this.searchConfig.params.endTime = dateRange[1]
    // }
    async getAllFlowInstances() {
      const params = {
        params: {
          withCronIns: 'no',
          search: '',
          withSubProc: '',
          mgmtRole: ''
        }
      }
      const { status, data } = await getProcessInstances(params)
      if (status === 'OK') {
        this.allFlowInstances = data || []
      }
    }
  }
}
</script>

<style scoped lang="scss">
.normal-execution-history {
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
