<template>
  <div class="time-execution-create">
    <div class="button-group">
      <Button type="success" class="btn-right" @click="setTimedExecution">
        <Icon type="md-add" :size="18" />
        {{ $t('full_word_add') }}
      </Button>
      <Button class="btn-upload" @click="exportData">
        <img src="@/assets/icon/DownloadOutlined.svg" class="upload-icon" />
        {{ $t('export_flow') }}
      </Button>
    </div>
    <div class="search">
      <BaseSearch :options="searchOptions" v-model="searchConfig.params" @search="getUserScheduledTasks"></BaseSearch>
    </div>
    <Table
      size="small"
      ref="table"
      :columns="tableColumns"
      :max-height="MODALHEIGHT"
      :data="tableData"
      :loading="loading"
    ></Table>
    <!--查看详情-->
    <BaseDrawer :title="$t('be_details')" :visible.sync="showModal" realWidth="70%" :maskClosable="false">
      <template slot-scope="{maxHeight}" slot="content">
        <Table :columns="detailTableColums" size="small" :max-height="maxHeight" :data="detailTableData"></Table>
      </template>
    </BaseDrawer>
    <!--新增定时执行-->
    <BaseDrawer
      :title="$t('full_word_add') + $t('timed_execution')"
      :visible.sync="timeConfig.isShow"
      :realWidth="1000"
      :maskClosable="false"
    >
      <template slot="content">
        <Form :label-width="100" label-colon>
          <!--任务名-->
          <FormItem :label="$t('fe_task_name')" required>
            <Input
              v-model.trim="timeConfig.params.name"
              :maxlength="50"
              show-word-limit
              :placeholder="$t('fe_task_name')"
            />
          </FormItem>
          <FormItem :label="$t('be_mgmt_role')" required>
            <Select v-model="timeConfig.params.role" @on-change="handleRoleChange">
              <Option v-for="item in timeConfig.currentUserRoles" :key="item.name" :value="item.name">{{
                item.displayName
              }}</Option>
            </Select>
          </FormItem>
          <!--执行记录-->
          <FormItem v-if="timeConfig.params.role" :label="$t('execution_history')" required>
            <Select
              v-model="timeConfig.params.selectedFlowInstance"
              filterable
              :remote-method="() => {}"
              @on-query-change="remoteProcessInstances"
              :loading="remoteLoading"
              @on-open-change="remoteOpenChange"
              clearable
            >
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
                  getStatusStyleAndName(item.displayStatus, 'label')
                "
              >
                <div style="display: flex; justify-content: space-between">
                  <div>
                    <span style="color: #2b85e4">{{ item.procInstName + ' ' }}</span>
                    <span style="color: #2b85e4">{{ '[' + item.version + '] ' }}</span>
                    <!-- <Tag style="color: #515a6e">{{ item.entityDisplayName + ' ' }}</Tag> -->
                    <div
                      v-if="item.entityDisplayName"
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
          </FormItem>
          <FormItem :label="$t('timing_type')" required>
            <Select v-model="timeConfig.params.scheduleMode" @on-change="timeConfig.params.time = '00:00:00'">
              <Option v-for="item in timeConfig.scheduleModeOptions" :key="item.value" :value="item.value">{{
                item.label
              }}</Option>
            </Select>
          </FormItem>
          <FormItem
            v-if="['Monthly', 'Weekly'].includes(timeConfig.params.scheduleMode)"
            :label="timeConfig.params.scheduleMode === 'Monthly' ? $t('day') : $t('week')"
            required
          >
            <Select v-model="timeConfig.params.cycle">
              <Option
                v-for="item in timeConfig.modeToValue[timeConfig.params.scheduleMode]"
                :key="item.value"
                :value="item.value"
              >{{ item.label }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('execute_date')" required>
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
          <FormItem :label="$t('be_email_push')" required>
            <Select v-model="timeConfig.params.mailMode">
              <Option v-for="item in timeConfig.mailModeOptions" :key="item.value" :value="item.value">{{
                item.label
              }}</Option>
            </Select>
          </FormItem>
        </Form>
      </template>
      <template slot="footer">
        <Button type="default" @click="timeConfig.isShow = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="saveTime" style="margin-left: 10px">{{ $t('save') }}</Button>
      </template>
    </BaseDrawer>
  </div>
</template>

<script>
import {
  getUserScheduledTasks,
  deleteUserScheduledTasks,
  resumeUserScheduledTasks,
  getScheduledTasksByStatus,
  getProcessInstances,
  setUserScheduledTasks,
  stopUserScheduledTasks,
  getCurrentUserRoles,
  getAllFlow
} from '@/api/server'
import { debounce } from '@/const/util'
export default {
  data() {
    return {
      showModal: false,
      fullscreen: false,
      remoteLoading: false,
      MODALHEIGHT: 0,
      allFlows: [],
      searchConfig: {
        params: {
          name: '',
          procDefId: '',
          jobCreatedTime: [],
          jobCreatedStartTime: '',
          jobCreatedEndTime: '',
          time: [],
          startTime: '',
          endTime: '',
          owner: '',
          scheduleMode: ''
        }
      },
      searchOptions: [
        // 创建时间
        {
          key: 'jobCreatedTime',
          label: this.$t('table_created_date'),
          initDateType: 4,
          dateRange: [
            {
              label: this.$t('fe_recent3Months'),
              type: 'month',
              value: 3,
              dateType: 1
            },
            {
              label: this.$t('fe_recentHalfYear'),
              type: 'month',
              value: 6,
              dateType: 2
            },
            {
              label: this.$t('fe_recentOneYear'),
              type: 'year',
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
        // 创建人
        {
          key: 'owner',
          placeholder: this.$t('createdBy'),
          component: 'input'
        },
        // 定时类型
        {
          key: 'scheduleMode',
          placeholder: this.$t('timing_type'),
          component: 'select',
          list: [
            {
              label: this.$t('Hourly'),
              value: 'Hourly'
            },
            {
              label: this.$t('Daily'),
              value: 'Daily'
            },
            {
              label: this.$t('Weekly'),
              value: 'Weekly'
            },
            {
              label: this.$t('Monthly'),
              value: 'Monthly'
            }
          ]
        },
        // 执行时间
        {
          key: 'time',
          label: this.$t('execute_date'),
          initDateType: 4,
          dateRange: [
            {
              label: this.$t('fe_recent3Months'),
              type: 'month',
              value: 3,
              dateType: 1
            },
            {
              label: this.$t('fe_recentHalfYear'),
              type: 'month',
              value: 6,
              dateType: 2
            },
            {
              label: this.$t('fe_recentOneYear'),
              type: 'year',
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
        }
      ],
      tableData: [],
      loading: false,
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('fe_task_name'),
          key: 'name',
          width: 200,
          render: (h, params) => <span>{params.row.name || '-'}</span>
        },
        {
          title: this.$t('flow_name'),
          key: 'procDefName',
          width: 200,
          render: (h, params) => (
            <div>
              <span>
                {params.row.procDefName}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
              </span>
            </div>
          )
        },
        {
          title: this.$t('bc_execution_instance'),
          key: 'entityDataName',
          width: 200
        },
        // 定时类型
        {
          title: this.$t('timing_type'),
          key: 'scheduleMode',
          width: 120,
          render: (h, params) => {
            const weekMap = {
              1: this.$t('fe_monday'),
              2: this.$t('fe_tuesday'),
              3: this.$t('fe_wednesday'),
              4: this.$t('fe_thursday'),
              5: this.$t('fe_friday'),
              6: this.$t('fe_saturday'),
              7: this.$t('fe_sunday')
            }
            let schedule = ''
            if (['Weekly', 'Monthly'].includes(params.row.scheduleMode)) {
              schedule = params.row.scheduleExpr.split(' ')[0]
            }
            const find = this.timeConfig.scheduleModeOptions.find(item => item.value === params.row.scheduleMode)
            return (
              <div>{`${find.label}${params.row.scheduleMode === 'Weekly' ? weekMap[schedule] : schedule}${
                params.row.scheduleMode === 'Monthly' ? this.$t('fe_dayly') : ''
              }`}</div>
            )
          }
        },
        {
          title: this.$t('schedule_expr'),
          key: 'scheduleExpr',
          width: 150,
          render: (h, params) => {
            let scheduleExpr = params.row.scheduleExpr
            if (['Weekly', 'Monthly'].includes(params.row.scheduleMode)) {
              scheduleExpr = params.row.scheduleExpr.split(' ')[1]
            }
            return <span>{scheduleExpr || '-'}</span>
          }
        },
        // 管理角色
        {
          title: this.$t('be_mgmt_role'),
          key: 'role',
          width: 130,
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
        // 成功
        {
          title: this.$t('success_count'),
          key: 'totalCompletedInstances',
          width: 90,
          render: (h, params) => (
            <div>
              <span style="color:#5384ff">{params.row.totalCompletedInstances}</span>
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
        },
        // 执行中
        {
          title: this.$t('in_progress_count'),
          key: 'totalInProgressInstances',
          width: 100,
          render: (h, params) => (
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
        },
        // 手动终止
        {
          title: this.$t('fe_internallyTerminated'),
          key: 'totalTerminateInstances',
          width: 100,
          render: (h, params) => (
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
        },
        // 自动退出
        {
          title: this.$t('fe_faulted'),
          key: 'totalFaultedInstances',
          width: 90,
          render: (h, params) => (
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
        },
        {
          title: this.$t('createdBy'),
          key: 'owner',
          width: 120
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime',
          width: 200
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 140,
          align: 'center',
          fixed: 'right',
          render: (h, params) => (
            <div style="display:flex;align-items:center;justify-content:center;">
              {params.row.status === 'Ready' && (
                <Tooltip content={this.$t('disable')} placement="top">
                  <Button
                    size="small"
                    type="warning"
                    onClick={() => {
                      this.pause(params.row) // 禁用
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-lock" size="16"></Icon>
                  </Button>
                </Tooltip>
              )}
              {params.row.status === 'Stopped' && (
                <Tooltip content={this.$t('enable')} placement="top">
                  <Button
                    size="small"
                    type="success"
                    onClick={() => {
                      this.resume(params.row) // 启用
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-unlock" size="16"></Icon>
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
      ],
      timeConfig: {
        isShow: false,
        params: {
          name: '',
          selectedFlowInstance: '',
          scheduleMode: 'Monthly',
          time: '00:00:00',
          cycle: '',
          role: '',
          mailMode: 'node'
        },
        currentUserRoles: [],
        mailModeOptions: [
          {
            label: this.$t('be_role_email'),
            value: 'role'
          },
          {
            label: this.$t('be_user_email'),
            value: 'user'
          },
          {
            label: this.$t('be_not_send'),
            value: 'none'
          }
        ],
        scheduleModeOptions: [
          {
            label: this.$t('Hourly'),
            value: 'Hourly'
          },
          {
            label: this.$t('Daily'),
            value: 'Daily'
          },
          {
            label: this.$t('Weekly'),
            value: 'Weekly'
          },
          {
            label: this.$t('Monthly'),
            value: 'Monthly'
          }
        ],
        modeToValue: {
          Monthly: [
            {
              label: '1',
              value: 1
            },
            {
              label: '2',
              value: 2
            },
            {
              label: '3',
              value: 3
            },
            {
              label: '4',
              value: 4
            },
            {
              label: '5',
              value: 5
            },
            {
              label: '6',
              value: 6
            },
            {
              label: '7',
              value: 7
            },
            {
              label: '8',
              value: 8
            },
            {
              label: '9',
              value: 9
            },
            {
              label: '10',
              value: 10
            },
            {
              label: '11',
              value: 11
            },
            {
              label: '12',
              value: 12
            },
            {
              label: '13',
              value: 13
            },
            {
              label: '14',
              value: 14
            },
            {
              label: '15',
              value: 15
            },
            {
              label: '16',
              value: 16
            },
            {
              label: '17',
              value: 17
            },
            {
              label: '18',
              value: 18
            },
            {
              label: '19',
              value: 19
            },
            {
              label: '20',
              value: 20
            },
            {
              label: '21',
              value: 21
            },
            {
              label: '22',
              value: 22
            },
            {
              label: '23',
              value: 23
            },
            {
              label: '24',
              value: 24
            },
            {
              label: '25',
              value: 25
            },
            {
              label: '26',
              value: 26
            },
            {
              label: '27',
              value: 27
            },
            {
              label: '28',
              value: 28
            },
            {
              label: '29',
              value: 29
            },
            {
              label: '30',
              value: 30
            },
            {
              label: '31',
              value: 31
            }
          ],
          Weekly: [
            {
              label: this.$t('Mon'),
              value: 1
            },
            {
              label: this.$t('Tue'),
              value: 2
            },
            {
              label: this.$t('Wed'),
              value: 3
            },
            {
              label: this.$t('Thu'),
              value: 4
            },
            {
              label: this.$t('Fri'),
              value: 5
            },
            {
              label: this.$t('Sat'),
              value: 6
            },
            {
              label: this.$t('Sun'),
              value: 7
            }
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
          key: 'displayStatus',
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
                color: '#ff4d4f'
              },
              {
                label: this.$t('fe_inProgressTimeouted'),
                value: 'InProgress(Timeouted)',
                color: '#ff4d4f'
              },
              {
                label: this.$t('fe_stop'),
                value: 'Stop',
                color: '#ff4d4f'
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
            const findObj = list.find(item => item.value === params.row.displayStatus) || {}
            if (findObj.label) {
              return <Tag color={findObj.color}>{findObj.label}</Tag>
            }
            return <span>-</span>
          }
        },
        // 执行信息
        {
          title: this.$t('fe_executionInfo'),
          key: 'errorMsg',
          render: (h, params) => <span>{params.row.errorMsg || '-'}</span>
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 100,
          align: 'center',
          render: (h, params) => (
            <div>
              {params.row.status && (
                <Button
                  onClick={() => this.jumpToHistory(params.row)}
                  type="primary"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('bc_history_record')}
                </Button>
              )}
            </div>
          )
        }
      ]
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
            label: this.$t('fe_inProgressFaulted'),
            value: 'InProgress(Faulted)',
            color: '#ff4d4f'
          },
          {
            label: this.$t('fe_inProgressTimeouted'),
            value: 'InProgress(Timeouted)',
            color: '#ff4d4f'
          },
          {
            label: this.$t('fe_stop'),
            value: 'Stop',
            color: '#ff4d4f'
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
        const findObj = list.find(i => i.value === status)
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
  mounted() {
    this.MODALHEIGHT = document.body.scrollHeight - 220
    this.getFlows()
    this.getUserScheduledTasks()
  },
  methods: {
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
    handleRoleChange() {
      this.timeConfig.params.selectedFlowInstance = ''
      this.timeConfig.allFlowInstances = []
    },
    remoteOpenChange(flag) {
      if (flag) {
        this.timeConfig.allFlowInstances = []
        this.remoteProcessInstances(this.timeConfig.params.selectedFlowInstance)
      }
    },
    // 获取执行记录
    remoteProcessInstances: debounce(async function (query) {
      function containsDateTime(str) {
        const regex = /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/
        return regex.test(str)
      }
      if (containsDateTime(query)) {
        return
      } // 解决下拉框勾选时，用全部名称搜索，导致结果为空的问题
      const params = {
        params: {
          withCronIns: 'no', // no普通执行历史 yes定时执行历史
          withSubProc: 'no', // 过滤子编排记录
          search: query,
          mgmtRole: this.timeConfig.params.role,
          status: 'Completed'
        }
      }
      this.remoteLoading = true
      const { status, data } = await getProcessInstances(params)
      this.remoteLoading = false
      if (status === 'OK') {
        this.timeConfig.allFlowInstances = data.filter(item => item.status === 'Completed')
      }
    }, 500),
    changeTimePicker(time) {
      this.timeConfig.params.time = time
    },
    async saveTime() {
      if (!this.timeConfig.params.name) {
        this.$Message.warning(this.$t('fe_task_name') + this.$t('fe_can_not_be_empty'))
        return
      }
      if (!this.timeConfig.params.selectedFlowInstance) {
        this.$Message.warning(this.$t('execution_history') + this.$t('fe_can_not_be_empty'))
        return
      }
      if (!this.timeConfig.params.cycle && ['Monthly', 'Weekly'].includes(this.timeConfig.params.scheduleMode)) {
        this.$Message.warning(
          (this.timeConfig.params.scheduleMode === 'Monthly' ? this.$t('day') : this.$t('week'))
            + this.$t('fe_can_not_be_empty')
        )
        return
      }
      if (!this.timeConfig.params.role) {
        this.$Message.warning(this.$t('be_mgmt_role') + this.$t('fe_can_not_be_empty'))
        return
      }
      const found = this.timeConfig.allFlowInstances.find(_ => _.id === this.timeConfig.params.selectedFlowInstance)
      if (!found) {
        return
      }
      let scheduleExpr = ''
      if (['Hourly', 'Daily'].includes(this.timeConfig.params.scheduleMode)) {
        scheduleExpr = this.timeConfig.params.time
        if (this.timeConfig.params.scheduleMode === 'Hourly') {
          scheduleExpr = this.timeConfig.params.time.substring(3)
        }
      } else {
        scheduleExpr = this.timeConfig.params.cycle + ' ' + this.timeConfig.params.time
      }
      const params = {
        name: this.timeConfig.params.name,
        scheduleMode: this.timeConfig.params.scheduleMode,
        scheduleExpr,
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
    async setTimedExecution() {
      this.timeConfig.params.name = `${this.$t('fe_timeTask')}${new Date().getTime()}`
      this.timeConfig.params.selectedFlowInstance = ''
      this.timeConfig.params.scheduleMode = 'Monthly'
      this.timeConfig.params.time = '00:00:00'
      this.timeConfig.params.cycle = ''
      this.timeConfig.params.role = ''
      this.timeConfig.params.mailMode = 'none'
      this.timeConfig.isShow = true
    },
    async getCurrentUserRoles() {
      const { status, data } = await getCurrentUserRoles()
      if (status === 'OK') {
        this.timeConfig.currentUserRoles = data
      }
    },
    exportData() {
      this.$refs.table.exportCsv({
        filename: 'timed_execution'
      })
    },
    jumpToHistory(row) {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/implementation/workflow-execution/view-execution?id=${row.procInstId}&from=time`
      window.open(path, '_blank')
    },
    async getDetails(row, rowStatus) {
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
    async resume(row) {
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
    async pause(row) {
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
    remove(row) {
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
    async getUserScheduledTasks() {
      await this.getCurrentUserRoles()
      const {
        name, procDefId, jobCreatedTime, time, owner, scheduleMode
      } = this.searchConfig.params
      const params = {
        name,
        procDefId,
        jobCreatedStartTime: jobCreatedTime[0] ? jobCreatedTime[0] + ' 00:00:00' : '',
        jobCreatedEndTime: jobCreatedTime[1] ? jobCreatedTime[1] + ' 23:59:59' : '',
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
      this.loading = true
      const { status, data } = await getUserScheduledTasks(params)
      this.loading = false
      if (status === 'OK') {
        this.tableData = data
      }
    }
  }
}
</script>
<style lang="scss">
.time-execution-create {
  .ivu-table-cell {
    padding: 0 6px !important;
  }
}
</style>
<style scoped lang="scss">
.time-execution-create {
  .search {
    margin-top: 8px;
    display: flex;
    .button-group {
      width: 220px;
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
  .btn-right {
    margin-right: 10px;
  }
  .btn-img {
    width: 16px;
    vertical-align: middle;
  }
}
</style>
