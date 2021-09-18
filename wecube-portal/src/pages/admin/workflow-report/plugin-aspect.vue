<template>
  <div class="">
    <div class="report-container">
      <div class="item">
        {{ $t('datetime_range') }}:
        <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm:ss" @on-change="getDate"></DatePicker>
      </div>
      <div class="item">
        {{ $t('plugin_regist') }}:
        <Select
          v-model="searchConfig.params.serviceIds"
          :max-tag-count="2"
          multiple
          filterable
          @on-open-change="getPlugin"
          @on-change="changePlugin"
          style="width:200px"
        >
          <Option v-for="item in searchConfig.pluginOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        {{ $t('task_node_bindings') }}:
        <Select
          v-model="searchConfig.params.entityDataIds"
          :max-tag-count="2"
          multiple
          filterable
          @on-open-change="getTasknodesBindings"
          @on-change="changeTasknodesBindings"
          :disabled="searchConfig.params.serviceIds.length === 0"
          style="width:200px"
        >
          <Option
            v-for="(item, itemIndex) in searchConfig.tasknodeBindingOptions"
            :value="item.entityDataId"
            :key="item.entityDataId + itemIndex"
            >{{ item.entityDisplayName || item.entityDataId }}</Option
          >
        </Select>
      </div>
      <div class="item">
        {{ $t('display_number') }}:
        <Select v-model="searchConfig.params.pageable.pageSize" filterable style="width:200px">
          <Option v-for="item in searchConfig.displayNumberOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary" :disabled="!disableBtn()" @click="getReport"> {{ $t('query') }}</Button>
      </div>
    </div>
    <Table size="small" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
    <ReportDetail ref="reportDetail"></ReportDetail>
  </div>
</template>

<script>
import ReportDetail from './show-report-detail'
import {
  getFlowExecutePluginList,
  getPluginTasknodesBindings,
  getPluginReport,
  getPluginReportDetails
} from '@/api/server.js'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
      searchParams: {},
      searchConfig: {
        params: {
          startDate: '',
          endDate: '',
          entityDataIds: [],
          serviceIds: [''],
          pageable: {
            pageSize: 100,
            startIndex: 0
          }
        },
        pluginOptions: [],
        tasknodeBindingOptions: [],
        displayNumberOptions: [100, 300, 500, 1000]
      },
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('plugin_regist'),
          key: 'serviceId'
        },
        {
          title: this.$t('task_node_bindings'),
          // key: 'entityDataName',
          render: (h, params) => {
            return <span>{params.row.entityDataName || params.row.entityDataId}</span>
          }
        },
        {
          title: this.$t('failure_count'),
          key: 'failureCount',
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.failureCount}</span>
                {params.row.failureCount > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getPluginReportDetails(params.row, 'Faulted')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('success_count'),
          key: 'successCount',
          render: (h, params) => {
            return (
              <div>
                <span style="color:#2d8cf0">{params.row.successCount}</span>
                {params.row.successCount > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getPluginReportDetails(params.row, 'Completed')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        }
      ]
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 300
  },
  methods: {
    async getPluginReportDetails (val, type) {
      const params = {
        endDate: this.searchConfig.params.endDate,
        startDate: this.searchConfig.params.startDate,
        status: type,
        serviceId: val.serviceId,
        nodeDefId: val.nodeDefId,
        entityDataId: val.entityDataId
      }
      const { status, data } = await getPluginReportDetails(params)
      if (status === 'OK') {
        this.$refs.reportDetail.initData(data)
      }
    },
    async getReport () {
      const { status, data } = await getPluginReport(this.searchConfig.params)
      if (status === 'OK') {
        this.tableData = data.contents
      }
    },
    disableBtn () {
      return (
        this.searchConfig.params.startDate !== '' &&
        this.searchConfig.params.endDate !== '' &&
        this.searchConfig.params.serviceIds.length !== 0
      )
    },
    getDate (dateRange) {
      this.searchConfig.params.startDate = dateRange[0]
      this.searchConfig.params.endDate = dateRange[1]
    },
    async getPlugin () {
      const { status, data } = await getFlowExecutePluginList()
      if (status === 'OK') {
        this.searchConfig.pluginOptions = data
      }
    },
    changePlugin () {
      this.searchConfig.params.entityDataIds = []
    },
    async getTasknodesBindings () {
      const { status, data } = await getPluginTasknodesBindings(this.searchConfig.params.serviceIds)
      if (status === 'OK') {
        this.searchConfig.tasknodeBindingOptions = data
      }
    },
    changeTasknodesBindings () {}
  },
  components: {
    ReportDetail
  }
}
</script>

<style scoped lang="scss">
.report-container {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.item {
  width: 290px;
  margin: 8px;
}
</style>
