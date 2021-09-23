<template>
  <div class=" ">
    <div class="report-container">
      <div class="item">
        {{ $t('datetime_range') }}:
        <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm:ss" @on-change="getDate"></DatePicker>
      </div>
      <div class="item">
        {{ $t('set_up_person') }}:
        <Input v-model="searchConfig.params.owner" style="width: 80%"></Input>
      </div>
      <div class="item">
        {{ $t('timing_type') }}:
        <Select v-model="searchConfig.params.scheduleMode" clearable style="width: 70%">
          <Option v-for="item in searchConfig.timingTypeOptions" :value="item.value" :key="item.value">{{
            item.label
          }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary" @click="getUserScheduledTasks"> {{ $t('query') }}</Button>
      </div>
    </div>
    <Table size="small" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
    <Modal v-model="showModal" :fullscreen="fullscreen" width="1000" footer-hide>
      <p slot="header">
        <span>{{ $t('details') }}</span>
        <Icon
          v-if="!fullscreen"
          @click="fullscreen = true"
          style="float: right;margin: 3px 40px 0 0 !important;"
          type="ios-expand"
        />
        <Icon
          v-else
          @click="fullscreen = false"
          style="float: right;margin: 3px 40px 0 0 !important;"
          type="ios-contract"
        />
      </p>
      <Table :columns="detailTableColums" size="small" :max-height="MODALHEIGHT" :data="detailTableData"></Table>
    </Modal>
  </div>
</template>

<script>
import {
  getUserScheduledTasks,
  deleteUserScheduledTasks,
  resumeUserScheduledTasks,
  getScheduledTasksByStatus,
  stopUserScheduledTasks
} from '@/api/server'
export default {
  name: '',
  data () {
    return {
      showModal: false,
      fullscreen: false,
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
      ],
      detailTableData: [],

      MODALHEIGHT: 0,
      searchConfig: {
        params: {
          startTime: '',
          endTime: '',
          owner: '',
          scheduleMode: ''
        },
        timingTypeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ]
      },
      tableData: [],
      tableColumns: [
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
          title: this.$t('target_object'),
          key: 'entityDataName'
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime'
        },
        {
          title: this.$t('set_up_person'),
          key: 'owner'
        },
        {
          title: this.$t('timing_type'),
          key: 'scheduleMode'
        },
        {
          title: this.$t('schedule_expr'),
          key: 'scheduleExpr'
        },
        {
          title: this.$t('status'),
          key: 'status'
        },
        {
          title: this.$t('failure_count'),
          key: 'totalFaultedInstances',
          width: 100,
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
                    onClick={() => this.getDetails(params.row, 'F')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('success_count'),
          key: 'totalCompletedInstances',
          width: 100,
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
                    onClick={() => this.getDetails(params.row, 'S')}
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
          width: 250,
          align: 'center',
          render: (h, params) => {
            return (
              <div>
                {params.row.status === 'Ready' && (
                  <Button onClick={() => this.pause(params.row)} type="warning" size="small" style="margin-right: 5px">
                    {this.$t('pause')}
                  </Button>
                )}
                {params.row.status === 'Stopped' && (
                  <Button onClick={() => this.resume(params.row)} type="info" size="small" style="margin-right: 5px">
                    {this.$t('start_up')}
                  </Button>
                )}
                <Button onClick={() => this.remove(params.row)} type="error" size="small" style="margin-right: 5px">
                  {this.$t('delete')}
                </Button>
                <Button
                  onClick={() => this.getDetails(params.row, '')}
                  type="primary"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('list')}
                </Button>
              </div>
            )
          }
        }
      ]
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 300
    this.getUserScheduledTasks()
  },
  methods: {
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
      let params = JSON.parse(JSON.stringify(this.searchConfig.params))
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
  },
  components: {}
}
</script>

<style scoped lang="scss">
.report-container {
  display: flex;
  flex-wrap: wrap;
  // margin-bottom: 16px;
}
.item {
  width: 21%;
  margin: 8px;
}
</style>
