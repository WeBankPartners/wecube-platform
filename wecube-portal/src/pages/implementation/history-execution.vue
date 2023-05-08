<template>
  <div class=" ">
    <div class="report-container">
      <div class="item" style="width: 400px">
        {{ $t('datetime_range') }}:
        <DatePicker
          type="datetimerange"
          format="yyyy-MM-dd HH:mm:ss"
          @on-change="getDate"
          style="width: 300px"
        ></DatePicker>
      </div>
      <div class="item">
        {{ $t('flow_name') }}:
        <Input v-model="searchConfig.params.procInstName" style="width: 60%"></Input>
      </div>
      <div class="item">
        {{ $t('target_object') }}:
        <Input v-model="searchConfig.params.entityDisplayName" style="width: 60%"></Input>
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
import { instancesWithPaging, getUserList } from '@/api/server'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
      statusOptions: ['NotStarted', 'InProgress', 'Completed', 'Faulted', 'Timeouted', 'InternallyTerminated'],
      pageable: {
        pageSize: 10,
        startIndex: 1,
        current: 1,
        total: 0
      },
      searchConfig: {
        params: {
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
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
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
                  type="warning"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('details')}
                </Button>
              </div>
            )
          }
        }
      ],
      users: []
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 300
    this.getProcessInstances()
    this.getAllUsers()
  },
  methods: {
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
    jumpToHistory (row) {
      this.$emit('jumpToHistory', row.id)
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
.ivu-form-item {
  margin-bottom: 8px;
}
.item {
  width: 260px;
  margin: 8px;
}
</style>
