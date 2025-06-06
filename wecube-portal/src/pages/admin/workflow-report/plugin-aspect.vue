<template>
  <div class="">
    <div class="report-container">
      <div class="item">
        <!-- <DatePicker
          type="datetimerange"
          format="yyyy-MM-dd HH:mm:ss"
          style="width: 300px"
          split-panels
          :placeholder="$t('datetime_range')"
          @on-change="getDate"
        ></DatePicker> -->
        <DateGroup :label="$t('datetime_range')" :typeList="dateTypeList" @change="getDate"></DateGroup>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.serviceIds"
          :max-tag-count="2"
          multiple
          filterable
          :placeholder="$t('workflow_plugin_aspect')"
          @on-open-change="getPlugin"
          @on-change="changePlugin"
          style="width: 200px"
        >
          <Option v-for="item in searchConfig.pluginOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.entityDataIds"
          :max-tag-count="2"
          multiple
          filterable
          :placeholder="$t('task_node_bindings')"
          @on-open-change="getTasknodesBindings"
          @on-change="changeTasknodesBindings"
          :disabled="searchConfig.params.serviceIds.length === 0"
          style="width: 200px"
        >
          <Option
            v-for="(item, itemIndex) in searchConfig.tasknodeBindingOptions"
            :value="item.entityDataId"
            :key="item.entityDataId + itemIndex"
          >{{ item.entityDisplayName || item.entityDataId }}</Option>
        </Select>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.pageable.pageSize"
          :placeholder="$t('display_number')"
          filterable
          style="width: 200px"
        >
          <Option v-for="item in searchConfig.displayNumberOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary" :disabled="!disableBtn()" @click="getReport"> {{ $t('query') }}</Button>
      </div>
    </div>
    <div style="text-align: right">{{ $t('total') }}{{ totalRows }} {{ $t('display') }}{{ tableData.length }}</div>
    <Table
      @on-sort-change="sortTable"
      size="small"
      :columns="tableColumns"
      :max-height="MODALHEIGHT"
      :data="tableData"
    ></Table>
    <ReportDetail ref="reportDetail"></ReportDetail>
  </div>
</template>

<script>
import ReportDetail from './show-report-detail'
import DateGroup from '@/pages/components/date-group'
import {
  getFlowExecutePluginList,
  getPluginTasknodesBindings,
  getPluginReport,
  getPluginReportDetails
} from '@/api/server.js'
export default {
  components: {
    ReportDetail,
    DateGroup
  },
  data() {
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
          },
          sorting: {}
        },
        pluginOptions: [],
        tasknodeBindingOptions: [],
        displayNumberOptions: [100, 300, 500, 1000]
      },
      totalRows: 0,
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        // 插件服务
        {
          title: this.$t('workflow_plugin_aspect'),
          key: 'serviceId'
        },
        // 数据对象
        {
          title: this.$t('task_node_bindings'),
          // key: 'entityDataName',
          render: (h, params) => <span>{params.row.entityDataName || params.row.entityDataId}</span>
        },
        {
          title: this.$t('failure_count'),
          sortable: 'custom',
          key: 'failureCount',
          render: (h, params) => (
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
        },
        {
          title: this.$t('success_count'),
          sortable: 'custom',
          key: 'successCount',
          render: (h, params) => (
            <div>
              <span style="color:#5384ff">{params.row.successCount}</span>
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
      ],
      dateTypeList: [
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
      ]
    }
  },
  mounted() {
    this.MODALHEIGHT = document.body.scrollHeight - 300
  },
  methods: {
    async sortTable(column) {
      this.searchConfig.params.sorting = {
        asc: column.order === 'asc',
        field: column.key
      }
      const { status, data } = await getPluginReport(this.searchConfig.params)
      if (status === 'OK') {
        this.tableData = data.contents
        this.totalRows = data.pageInfo.totalRows
      }
    },
    async getPluginReportDetails(val, type) {
      const params = {
        startDate: this.searchConfig.params.startDate,
        endDate: this.searchConfig.params.endDate,
        status: type,
        entityDataName: val.entityDataName,
        serviceId: val.serviceId,
        nodeDefId: val.nodeDefId,
        entityDataId: val.entityDataId
      }
      const { status, data } = await getPluginReportDetails(params)
      if (status === 'OK') {
        this.$refs.reportDetail.initData(data)
      }
    },
    async getReport() {
      this.searchConfig.params.sorting = {}
      const { status, data, message } = await getPluginReport(this.searchConfig.params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.tableData = data.contents
        this.totalRows = data.pageInfo.totalRows
      }
    },
    disableBtn() {
      return (
        this.searchConfig.params.startDate !== ''
        && this.searchConfig.params.endDate !== ''
        && this.searchConfig.params.serviceIds.length !== 0
      )
    },
    getDate(dateRange) {
      this.searchConfig.params.startDate = dateRange[0] ? dateRange[0] + ' 00:00:00' : ''
      this.searchConfig.params.endDate = dateRange[1] ? dateRange[1] + ' 23:59:59' : ''
    },
    async getPlugin() {
      const { status, data } = await getFlowExecutePluginList()
      if (status === 'OK') {
        this.searchConfig.pluginOptions = data
      }
    },
    changePlugin() {
      this.searchConfig.params.entityDataIds = []
    },
    async getTasknodesBindings() {
      const { status, data } = await getPluginTasknodesBindings(this.searchConfig.params.serviceIds)
      if (status === 'OK') {
        this.searchConfig.tasknodeBindingOptions = data
      }
    },
    changeTasknodesBindings() {}
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
  // width: 290px;
  margin: 8px;
}
</style>
