<template>
  <div class=" ">
    <div class="report-container">
      <div class="item" style="width: 400px">
        {{ $t('datetime_range') }}:
        <DatePicker
          type="datetimerange"
          format="yyyy-MM-dd HH:mm:ss"
          v-model="time"
          @on-change="getDate"
          style="width: 320px"
        ></DatePicker>
      </div>
      <div class="item">
        ID:
        <Input v-model="searchConfig.params.id" style="width: 60%" clearable></Input>
      </div>
      <div class="item">
        {{ $t('flow_name') }}:
        <Select
          label
          v-model="searchConfig.params.procInstName"
          @on-open-change="getFlows()"
          filterable
          clearable
          style="width: 60%"
        >
          <Option v-for="item in allFlows" :value="item.procDefName" :key="item.procDefId">{{
            item.procDefName
          }}</Option>
        </Select>
      </div>
      <div class="item">
        {{ $t('target_object') }}:
        <Input v-model="searchConfig.params.entityDisplayName" style="width: 60%" clearable></Input>
      </div>
      <div class="item">
        {{ $t('executor') }}:
        <Select v-model="searchConfig.params.operator" filterable clearable style="width: 60%">
          <Option v-for="item in users" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        {{ $t('status') }}:
        <Select v-model="searchConfig.params.status" clearable style="width: 60%">
          <Option v-for="item in statusOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>

      <div class="item">
        <Button type="primary" @click="getProcessInstances"> {{ $t('query') }}</Button>
      </div>
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
import { instancesWithPaging, getUserList, getAllFlow, createWorkflowInstanceTerminationRequest } from '@/api/server'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
      statusOptions: ['NotStarted', 'InProgress', 'Completed', 'Faulted', 'Timeouted', 'InternallyTerminated'],
      time: ['', ''],
      pageable: {
        pageSize: 10,
        startIndex: 1,
        current: 1,
        total: 0
      },
      searchConfig: {
        params: {
          id: '',
          startTime: '',
          endTime: '',
          procInstName: '',
          entityDisplayName: '',
          operator: '',
          status: ''
        },
        timingTypeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ]
      },
      allFlows: [],
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: 'ID',
          width: 60,
          key: 'id'
        },
        {
          title: this.$t('flow_name'),
          key: 'procInstName'
        },
        {
          title: this.$t('target_object'),
          key: 'entityDisplayName'
        },
        {
          title: this.$t('executor'),
          key: 'operator'
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime'
        },
        {
          title: this.$t('flow_status'),
          key: 'status'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 250,
          align: 'center',
          render: (h, params) => {
            return (
              <div>
                <Button
                  onClick={() => this.jumpToHistory(params.row)}
                  type="info"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('details')}
                </Button>
                {params.row.status === 'InProgress' && (
                  <Button
                    onClick={() => this.stopTask(params.row)}
                    type="warning"
                    size="small"
                    style="margin-right: 5px"
                  >
                    {this.$t('终止')}
                  </Button>
                )}
              </div>
            )
          }
        }
      ],
      users: []
    }
  },
  async mounted () {
    const cacheParams = localStorage.getItem('history-execution-search-params')
    if (cacheParams) {
      await this.getFlows()
      const tmp = JSON.parse(cacheParams)
      this.time = [tmp.startTime || '', tmp.endTime || '']
      this.searchConfig.params.id = tmp.id || ''
      this.searchConfig.params.startTime = tmp.startTime || ''
      this.searchConfig.params.endTime = tmp.endTime || ''
      this.searchConfig.params.procInstName = tmp.procInstName || ''
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
    // 终止任务
    stopTask (row) {
      this.$Modal.confirm({
        title: this.$t('bc_confirm') + ' ' + this.$t('stop_orch'),
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
      }
    },
    async getAllUsers () {
      let { status, data } = await getUserList()
      if (status === 'OK') {
        this.users = data.map(_ => _.username)
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
        id: this.searchConfig.params.id !== '' ? this.searchConfig.params.id : undefined,
        procInstName: this.searchConfig.params.procInstName !== '' ? this.searchConfig.params.procInstName : undefined,
        entityDisplayName:
          this.searchConfig.params.entityDisplayName !== '' ? this.searchConfig.params.entityDisplayName : undefined,
        operator: this.searchConfig.params.operator !== '' ? this.searchConfig.params.operator : undefined,
        status: this.searchConfig.params.status !== '' ? this.searchConfig.params.status : undefined,
        startTime: this.searchConfig.params.startTime !== '' ? this.searchConfig.params.startTime : undefined,
        endTime: this.searchConfig.params.endTime !== '' ? this.searchConfig.params.endTime : undefined,
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
          startIndex: 1,
          pageSize: 500
        }
      }
      let { status, data } = await instancesWithPaging(params)
      if (status === 'OK') {
        const detail = Array.isArray(data.contents) && data.contents[0]
        // 能获取到历史记录，跳转，否则给出提示
        if (detail && detail.id) {
          return this.$emit('jumpToHistory', row.id)
        }
      }
      this.$Notice.warning({
        title: '',
        desc: this.$t('no_detail_warning')
      })
    },
    getDate (dateRange, type) {
      if (type === 'date' && dateRange[1].slice(-8) === '00:00:00') {
        // type类型判断等于date,是为了防止用户手动选时间为 00:00:00 时触发，变成 '23:59:59'
        dateRange[1] = dateRange[1].slice(0, -8) + '23:59:59'
      }
      this.time = dateRange
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
.ivu-form-item {
  margin-bottom: 8px;
}
.item {
  width: 260px;
  margin: 8px;
}
</style>
