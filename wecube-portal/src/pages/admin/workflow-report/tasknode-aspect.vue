<template>
  <div class="">
    <div class="report-container">
      <div class="item">
        <DatePicker
          type="datetimerange"
          format="yyyy-MM-dd HH:mm:ss"
          :placeholder="$t('datetime_range')"
          style="width: 300px"
          split-panels
          @on-change="getDate"
        ></DatePicker>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.procDefIds"
          filterable
          :placeholder="$t('flow_name')"
          @on-open-change="getProcess"
          @on-change="changeProcess"
          style="width: 200px"
        >
          <Option v-for="item in searchConfig.processOptions" :value="item.procDefId" :key="item.procDefId">{{
            item.procDefName
          }}</Option>
        </Select>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.taskNodeIds"
          :max-tag-count="2"
          multiple
          filterable
          :placeholder="$t('task_node')"
          @on-open-change="getTasknodes"
          @on-change="changeTasknodes"
          :disabled="searchConfig.params.procDefIds === ''"
          style="width: 200px"
        >
          <Option
            v-for="(item, itenIndex) in searchConfig.tasknodeOptions"
            :value="item.nodeDefId"
            :key="item.nodeDefId + itenIndex"
            >{{ item.nodeName }}</Option
          >
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
          :disabled="searchConfig.params.taskNodeIds.length === 0"
          style="width: 200px"
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
        <Select
          v-model="searchConfig.params.pageable.pageSize"
          filterable
          :placeholder="$t('task_node_bindings')"
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
      size="small"
      @on-sort-change="sortTable"
      :columns="tableColumns"
      :max-height="MODALHEIGHT"
      :data="tableData"
    ></Table>
    <ReportDetail ref="reportDetail"></ReportDetail>
  </div>
</template>

<script>
import ReportDetail from './show-report-detail'
import {
  getProcessList,
  getTasknodesList,
  getTasknodesBindings,
  getTasknodesReport,
  getReportDetails
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
          serviceIds: [],
          taskNodeIds: [],
          procDefIds: '',
          pageable: {
            pageSize: 100,
            startIndex: 0
          },
          sorting: {}
        },
        processOptions: [],
        tasknodeOptions: [],
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
        // 编排名称
        {
          title: this.$t('flow_name'),
          key: 'procDefName'
        },
        // 编排节点
        {
          title: this.$t('workflow_task_node_aspect'),
          key: 'nodeDefName'
        },
        // 数据对象
        {
          title: this.$t('task_node_bindings'),
          // key: 'entityDataName',
          render: (h, params) => {
            return <span>{params.row.entityDataName || params.row.entityDataId}</span>
          }
        },
        {
          title: this.$t('failure_count'),
          sortable: 'custom',
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
                    onClick={() => this.getReportDetails(params.row, 'Faulted')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('success_count'),
          sortable: 'custom',
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
                    onClick={() => this.getReportDetails(params.row, 'Completed')}
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
    async sortTable (column) {
      this.searchConfig.params.sorting = { asc: column.order === 'asc', field: column.key }
      const { status, data } = await getTasknodesReport(this.searchConfig.params)
      if (status === 'OK') {
        this.tableData = data.contents
        this.totalRows = data.pageInfo.totalRows
      }
    },
    async getReportDetails (val, type) {
      const params = {
        endDate: this.searchConfig.params.endDate,
        startDate: this.searchConfig.params.startDate,
        status: type,
        serviceId: val.serviceId,
        procDefId: val.procDefId,
        nodeDefId: val.nodeDefId,
        entityDataId: val.entityDataId
      }
      const { status, data } = await getReportDetails(params)
      if (status === 'OK') {
        this.$refs.reportDetail.initData(data)
      }
    },
    async getReport () {
      let copyParams = JSON.parse(JSON.stringify(this.searchConfig.params))
      copyParams.procDefIds = [copyParams.procDefIds]
      const { status, data, message } = await getTasknodesReport(copyParams)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.tableData = data.contents
      }
    },
    disableBtn () {
      return (
        this.searchConfig.params.startDate !== '' &&
        this.searchConfig.params.endDate !== '' &&
        this.searchConfig.params.taskNodeIds.length !== 0 &&
        this.searchConfig.params.taskNodeIds.length !== 0 &&
        this.searchConfig.params.procDefIds !== ''
      )
    },
    getDate (dateRange) {
      this.searchConfig.params.startDate = dateRange[0]
      this.searchConfig.params.endDate = dateRange[1]
    },
    async getProcess () {
      const { status, data } = await getProcessList()
      if (status === 'OK') {
        this.searchConfig.processOptions = data
      }
    },
    changeProcess () {
      this.searchConfig.params.taskNodeIds = []
      this.searchConfig.params.entityDataIds = []
    },
    async getTasknodes () {
      const { status, data } = await getTasknodesList([this.searchConfig.params.procDefIds])
      if (status === 'OK') {
        this.searchConfig.tasknodeOptions = data
      }
    },
    changeTasknodes () {
      this.searchConfig.params.entityDataIds = []
    },
    async getTasknodesBindings () {
      const { status, data } = await getTasknodesBindings(this.searchConfig.params.taskNodeIds)
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
  // width: 290px;
  margin: 8px;
}
</style>
