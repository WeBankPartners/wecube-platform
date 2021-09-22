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
        <Select v-model="searchConfig.params.serviceIds" style="width: 70%">
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
  </div>
</template>

<script>
import {
  getUserScheduledTasks,
  deleteUserScheduledTasks,
  resumeUserScheduledTasks,
  stopUserScheduledTasks
} from '@/api/server'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
      searchConfig: {
        params: {
          startDate: '2021-09-22 00:00:00',
          endDate: '2021-09-01 00:00:00'
        },
        timingTypeOptions: [
          { label: 'Hourly', value: 'Hourly' },
          { label: 'Daily', value: 'Daily' },
          { label: 'Weekly', value: 'Weekly' },
          { label: 'Monthly', value: 'Monthly' }
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
                    // onClick={() => this.getPluginReportDetails(params.row, 'Faulted')}
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
                    // onClick={() => this.getPluginReportDetails(params.row, 'Completed')}
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
                <Button type="primary" size="small" style="margin-right: 5px">
                  {this.$t('list')}
                </Button>
                <Button onClick={() => this.remove(params.row)} type="error" size="small" style="margin-right: 5px">
                  {this.$t('delete')}
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
      const { status, data } = await getUserScheduledTasks(this.searchConfig.params)
      if (status === 'OK') {
        console.log(data)
        this.tableData = data
      }
    },
    getDate (dateRange) {
      this.searchConfig.params.startDate = dateRange[0]
      this.searchConfig.params.endDate = dateRange[1]
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
