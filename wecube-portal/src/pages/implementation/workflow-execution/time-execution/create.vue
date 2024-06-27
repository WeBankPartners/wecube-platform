<template>
  <div class="time-execution-create">
    <div class="search">
      <Search
        :options="searchOptions"
        v-model="searchConfig.params"
        @search="getUserScheduledTasks"
        :showBtn="false"
      ></Search>
      <div class="button-group">
        <Button type="success" class="btn-right" @click="setTimedExecution">
          {{ $t('full_word_add') }}
        </Button>
        <Button type="info" @click="exportData">
          <img src="../../../../assets/icon/export.png" class="btn-img" alt="" />
          {{ $t('export_flow') }}
        </Button>
      </div>
    </div>
    <Table size="small" ref="table" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
    <Modal v-model="showModal" :fullscreen="fullscreen" width="1000" footer-hide>
      <p slot="header">
        <span>{{ $t('be_details') }}</span>
        <Icon
          v-if="!fullscreen"
          @click="fullscreen = true"
          style="float: right; margin: 3px 40px 0 0 !important"
          type="ios-expand"
        />
        <Icon
          v-else
          @click="fullscreen = false"
          style="float: right; margin: 3px 40px 0 0 !important"
          type="ios-contract"
        />
      </p>
      <Table :columns="detailTableColums" size="small" :max-height="MODALHEIGHT" :data="detailTableData"></Table>
    </Modal>
    <Modal v-model="timeConfig.isShow" width="650" :title="$t('timed_execution')">
      <Form :label-width="100" label-colon>
        <FormItem :label="$t('flow_name')">
          <Select v-model="timeConfig.params.selectedFlowInstance" filterable>
            <Option
              v-for="item in timeConfig.allFlowInstances"
              :value="item.id"
              :key="item.id"
              :label="
                item.procInstName +
                ' ' +
                item.entityDisplayName +
                ' ' +
                (item.createdTime || '0000-00-00 00:00:00') +
                ' ' +
                (item.operator || 'operator')
              "
            >
              <span>
                <span style="color: #2b85e4">{{ item.procInstName + ' ' }}</span>
                <span style="color: #515a6e">{{ item.entityDisplayName + ' ' }}</span>
                <span style="color: #ccc; padding-left: 8px; float: right">{{ item.status }}</span>
                <span style="color: #ccc; float: right">{{ (item.createdTime || '0000-00-00 00:00:00') + ' ' }}</span>
                <span style="float: right; color: #515a6e; margin-right: 20px">{{ item.operator || 'operator' }}</span>
              </span>
            </Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('timing_type')">
          <Select v-model="timeConfig.params.scheduleMode" @on-change="timeConfig.params.time = '00:00:00'">
            <Option v-for="item in timeConfig.scheduleModeOptions" :key="item.value" :value="item.value">{{
              item.label
            }}</Option>
          </Select>
        </FormItem>
        <FormItem
          v-if="['Monthly', 'Weekly'].includes(timeConfig.params.scheduleMode)"
          :label="timeConfig.params.scheduleMode === 'Monthly' ? $t('day') : $t('week')"
        >
          <Select v-model="timeConfig.params.cycle">
            <Option
              v-for="item in timeConfig.modeToValue[timeConfig.params.scheduleMode]"
              :key="item.value"
              :value="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('execute_date')">
          <TimePicker
            :value="timeConfig.params.time"
            @on-change="changeTimePicker"
            style="width: 100%"
            :disabled-hours="
              timeConfig.params.scheduleMode === 'Hourly'
                ? [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
                : []
            "
            :clearable="false"
            format="HH:mm:ss"
          ></TimePicker>
        </FormItem>
        <FormItem :label="$t('be_mgmt_role')">
          <Select v-model="timeConfig.params.role">
            <Option v-for="item in timeConfig.currentUserRoles" :key="item.name" :value="item.name">{{
              item.displayName
            }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('be_email_push')">
          <Select v-model="timeConfig.params.mailMode">
            <Option v-for="item in timeConfig.mailModeOptions" :key="item.value" :value="item.value">{{
              item.label
            }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="timeConfig.isShow = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="saveTime">{{ $t('save') }}</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import Search from '@/pages/components/base-search.vue'
import {
  getUserScheduledTasks,
  deleteUserScheduledTasks,
  resumeUserScheduledTasks,
  getScheduledTasksByStatus,
  getProcessInstances,
  setUserScheduledTasks,
  stopUserScheduledTasks,
  getCurrentUserRoles
} from '@/api/server'
import dayjs from 'dayjs'
export default {
  components: {
    Search
  },
  data () {
    return {
      showModal: false,
      fullscreen: false,
      MODALHEIGHT: 0,
      searchConfig: {
        params: {
          time: [dayjs().subtract(3, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          startTime: '',
          endTime: '',
          owner: '',
          scheduleMode: ''
        }
      },
      searchOptions: [
        {
          key: 'time',
          label: this.$t('datetime_range'),
          initDateType: 1,
          dateRange: [
            { label: this.$t('be_threeDays_recent'), type: 'day', value: 3, dateType: 1 },
            { label: this.$t('be_oneWeek_recent'), type: 'day', value: 7, dateType: 2 },
            { label: this.$t('be_oneMonth_recent'), type: 'month', value: 1, dateType: 3 },
            { label: this.$t('be_auto'), dateType: 4 } // 自定义
          ],
          labelWidth: 110,
          component: 'custom-time'
        },
        {
          key: 'owner',
          placeholder: '设置人',
          component: 'input'
        },
        {
          key: 'scheduleMode',
          placeholder: '定时类型',
          component: 'select',
          list: [
            { label: this.$t('Hourly'), value: 'Hourly' },
            { label: this.$t('Daily'), value: 'Daily' },
            { label: this.$t('Weekly'), value: 'Weekly' },
            { label: this.$t('Monthly'), value: 'Monthly' }
          ]
        }
      ],
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'procDefName',
          width: 200,
          render: (h, params) => {
            return (
              <div>
                <span>
                  {params.row.procDefName}
                  <Tag style="margin-left:2px">{params.row.version}</Tag>
                </span>
              </div>
            )
          }
        },
        {
          title: this.$t('target_object'),
          key: 'entityDataName',
          width: 200
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime',
          width: 200
        },
        {
          title: this.$t('set_up_person'),
          key: 'owner',
          width: 120
        },
        {
          title: this.$t('timing_type'),
          key: 'scheduleMode',
          width: 120,
          render: (h, params) => {
            const find = this.timeConfig.scheduleModeOptions.find(item => item.value === params.row.scheduleMode)
            return <div>{find.label}</div>
          }
        },
        {
          title: this.$t('schedule_expr'),
          key: 'scheduleExpr',
          width: 200
        },
        {
          title: this.$t('status'),
          key: 'status',
          width: 110,
          render: (h, params) => {
            const find = this.timeConfig.scheduleModeOptions.find(item => item.value === params.row.scheduleMode)
            return <div>{find.label}</div>
          }
        },
        {
          title: this.$t('role'),
          key: 'role',
          width: 100,
          render: (h, params) => {
            const role = params.row.role || ''
            const find = this.timeConfig.currentUserRoles.find(item => item.name === role)
            let res = ''
            if (find) {
              res = find.displayName
            }
            return <div>{res}</div>
          }
        },
        {
          title: this.$t('email'),
          key: 'mailMode',
          width: 120,
          render: (h, params) => {
            const mailMode = params.row.mailMode || ''
            const find = this.timeConfig.mailModeOptions.find(item => item.value === mailMode)
            let res = ''
            if (find) {
              res = find.label
            }
            return <div>{res}</div>
          }
        },
        {
          title: this.$t('success_count'),
          key: 'totalCompletedInstances',
          width: 90,
          render: (h, params) => {
            return (
              <div>
                <span style="color:#2d8cf0">{params.row.totalCompletedInstances}</span>
                {params.row.totalCompletedInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'Completed')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('in_progress_count'),
          key: 'totalInProgressInstances',
          width: 100,
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalInProgressInstances}</span>
                {params.row.totalInProgressInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'InProgress')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('terminate_count'),
          key: 'totalTerminateInstances',
          width: 100,
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalTerminateInstances}</span>
                {params.row.totalTerminateInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'InternallyTerminated')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('timeout_count'),
          key: 'totalTimeoutInstances',
          width: 90,
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalTimeoutInstances}</span>
                {params.row.totalTimeoutInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'Timeout')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('failure_count'),
          key: 'totalFaultedInstances',
          width: 90,
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalFaultedInstances}</span>
                {params.row.totalFaultedInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'Faulted')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 130,
          align: 'center',
          fixed: 'right',
          render: (h, params) => {
            return (
              <div style="display:flex;align-items:center;justify-content:center;">
                {params.row.status === 'Ready' && (
                  <Tooltip content={this.$t('pause')} placement="top">
                    <Button
                      size="small"
                      type="warning"
                      onClick={() => {
                        this.pause(params.row) // 暂停
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-pause" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {params.row.status === 'Stopped' && (
                  <Tooltip content={this.$t('start_up')} placement="top">
                    <Button
                      size="small"
                      type="success"
                      onClick={() => {
                        this.resume(params.row) // 继续
                      }}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-play" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                <Tooltip content={this.$t('delete')} placement="top">
                  <Button
                    type="error"
                    size="small"
                    onClick={() => {
                      this.remove(params.row) // 删除
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-trash" size="16"></Icon>
                  </Button>
                </Tooltip>
                <Tooltip content={this.$t('list')} placement="top">
                  <Button
                    size="small"
                    type="info"
                    onClick={() => {
                      this.getDetails(params.row, '') // 列表
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-eye" size="16"></Icon>
                  </Button>
                </Tooltip>
              </div>
            )
          }
        }
      ],
      timeConfig: {
        isShow: false,
        params: {
          selectedFlowInstance: '',
          scheduleMode: 'Monthly',
          time: '00:00:00',
          cycle: '',
          role: '',
          mailMode: 'node'
        },
        currentUserRoles: [],
        mailModeOptions: [
          { label: this.$t('be_role_email'), value: 'role' },
          { label: this.$t('be_user_email'), value: 'user' },
          { label: this.$t('be_not_send'), value: 'none' }
        ],
        scheduleModeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ],
        modeToValue: {
          Monthly: [
            { label: '1', value: 1 },
            { label: '2', value: 2 },
            { label: '3', value: 3 },
            { label: '4', value: 4 },
            { label: '5', value: 5 },
            { label: '6', value: 6 },
            { label: '7', value: 7 },
            { label: '8', value: 8 },
            { label: '9', value: 9 },
            { label: '10', value: 10 },
            { label: '11', value: 11 },
            { label: '12', value: 12 },
            { label: '13', value: 13 },
            { label: '14', value: 14 },
            { label: '15', value: 15 },
            { label: '16', value: 16 },
            { label: '17', value: 17 },
            { label: '18', value: 18 },
            { label: '19', value: 19 },
            { label: '20', value: 20 },
            { label: '21', value: 21 },
            { label: '22', value: 22 },
            { label: '23', value: 23 },
            { label: '24', value: 24 },
            { label: '25', value: 25 },
            { label: '26', value: 26 },
            { label: '27', value: 27 },
            { label: '28', value: 28 },
            { label: '29', value: 29 },
            { label: '30', value: 30 },
            { label: '31', value: 31 }
          ],
          Weekly: [
            { label: this.$t('Mon'), value: 1 },
            { label: this.$t('Tue'), value: 2 },
            { label: this.$t('Wed'), value: 3 },
            { label: this.$t('Thu'), value: 4 },
            { label: this.$t('Fri'), value: 5 },
            { label: this.$t('Sat'), value: 6 },
            { label: this.$t('Sun'), value: 7 }
          ]
        },
        allFlowInstances: []
      },
      detailTableData: [],
      detailTableColums: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'procDefName'
        },
        {
          title: this.$t('execute_date'),
          key: 'execTime'
        },
        {
          title: this.$t('status'),
          key: 'status'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 100,
          align: 'center',
          render: (h, params) => {
            return (
              <div>
                <Button
                  onClick={() => this.jumpToHistory(params.row)}
                  type="primary"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('bc_history_record')}
                </Button>
              </div>
            )
          }
        }
      ]
    }
  },
  mounted () {
    const catchParams = localStorage.getItem('timed-execution-search-params')
    if (catchParams) {
      const tmp = JSON.parse(catchParams)
      // this.time = [tmp.startTime || '', tmp.endTime || '']
      // this.searchConfig.params.startTime = tmp.startTime || ''
      // this.searchConfig.params.endTime = tmp.endTime || ''

      this.searchConfig.params.scheduleMode = tmp.scheduleMode || ''
      this.searchConfig.params.owner = tmp.owner || ''
    }
    this.MODALHEIGHT = document.body.scrollHeight - 220
    this.getUserScheduledTasks()
  },
  beforeDestroy () {
    const selectParams = JSON.stringify(this.searchConfig.params)
    localStorage.setItem('timed-execution-search-params', selectParams)
  },
  methods: {
    async getProcessInstances () {
      let { status, data } = await getProcessInstances()
      if (status === 'OK') {
        this.timeConfig.allFlowInstances = data.filter(item => item.status === 'Completed')
      }
    },
    changeTimePicker (time) {
      this.timeConfig.params.time = time
    },
    async saveTime () {
      const found = this.timeConfig.allFlowInstances.find(_ => _.id === this.timeConfig.params.selectedFlowInstance)
      if (!found) return
      let scheduleExpr = ''
      if (['Hourly', 'Daily'].includes(this.timeConfig.params.scheduleMode)) {
        scheduleExpr = this.timeConfig.params.time
        if (this.timeConfig.params.scheduleMode === 'Hourly') {
          scheduleExpr = this.timeConfig.params.time.substring(3)
        }
      } else {
        scheduleExpr = this.timeConfig.params.cycle + ' ' + this.timeConfig.params.time
      }
      let params = {
        scheduleMode: this.timeConfig.params.scheduleMode,
        scheduleExpr: scheduleExpr,
        procDefName: found.procInstName,
        procDefId: found.procDefId,
        entityDataName: found.entityDisplayName,
        entityDataId: found.entityDataId,
        mailMode: this.timeConfig.params.mailMode,
        role: this.timeConfig.params.role
      }
      const { status } = await setUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
        this.timeConfig.isShow = false
        this.getUserScheduledTasks()
      }
    },
    async setTimedExecution () {
      this.getProcessInstances()
      this.timeConfig.params.selectedFlowInstance = ''
      this.timeConfig.params.scheduleMode = 'Monthly'
      this.timeConfig.params.time = '00:00:00'
      this.timeConfig.params.cycle = ''
      this.timeConfig.params.role = ''
      this.timeConfig.params.mailMode = 'none'
      this.timeConfig.isShow = true
    },
    async getCurrentUserRoles () {
      const { status, data } = await getCurrentUserRoles()
      if (status === 'OK') {
        this.timeConfig.currentUserRoles = data
      }
    },
    exportData () {
      this.$refs.table.exportCsv({
        filename: 'timed_execution'
      })
    },
    jumpToHistory (row) {
      this.$emit('jumpToHistory', row.procInstId)
    },
    async getDetails (row, rowStatus) {
      const params = {
        userTaskId: row.id,
        procInstanceStatus: rowStatus
      }
      const { status, data } = await getScheduledTasksByStatus(params)
      if (status === 'OK') {
        this.showModal = true
        this.detailTableData = data
      }
    },
    async resume (row) {
      const params = [
        {
          id: row.id
        }
      ]
      const { status, message } = await resumeUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getUserScheduledTasks()
      }
    },
    async pause (row) {
      const params = [
        {
          id: row.id
        }
      ]
      const { status, message } = await stopUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getUserScheduledTasks()
      }
    },
    remove (row) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          const params = [
            {
              id: row.id
            }
          ]
          const { status, message } = await deleteUserScheduledTasks(params)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Delete Success',
              desc: message
            })
            this.getUserScheduledTasks()
          }
        },
        onCancel: () => {}
      })
    },
    async getUserScheduledTasks () {
      await this.getCurrentUserRoles()
      const { time, owner, scheduleMode } = this.searchConfig.params
      const params = {
        startTime: time[0] ? time[0] + ' 00:00:00' : '',
        endTime: time[1] ? time[1] + ' 23:59:59' : '',
        owner,
        scheduleMode
      }
      const keys = Object.keys(params)
      keys.forEach(key => {
        if (params[key] === '') {
          delete params[key]
        }
      })
      const { status, data } = await getUserScheduledTasks(params)
      if (status === 'OK') {
        this.tableData = data
      }
    },
    getDate (dateRange) {
      this.searchConfig.params.startTime = dateRange[0]
      this.searchConfig.params.endTime = dateRange[1]
    }
  }
}
</script>
<style lang="scss">
.time-execution-create {
  .ivu-table-cell {
    padding: 0 4px !important;
  }
}
</style>
<style scoped lang="scss">
.time-execution-create {
  .search {
    display: flex;
    .button-group {
      width: 180px;
      text-align: right;
    }
  }
  .ivu-form-item {
    margin-bottom: 8px;
  }
  .item {
    width: 21%;
    margin: 8px;
  }
}
</style>
