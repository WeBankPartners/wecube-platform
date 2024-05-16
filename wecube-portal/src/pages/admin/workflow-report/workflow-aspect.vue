<template>
  <div>
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
          :max-tag-count="2"
          multiple
          filterable
          :placeholder="$t('flow_name')"
          @on-open-change="getProcess"
          style="width: 200px"
        >
          <Option v-for="item in searchConfig.processOptions" :value="item.procDefId" :key="item.procDefId">{{
            item.procDefName
          }}</Option>
        </Select>
      </div>
      <div class="item">
        <Select
          v-model="searchConfig.params.pageable.pageSize"
          filterable
          :placeholder="$t('display_number')"
          style="width: 200px"
        >
          <Option v-for="item in searchConfig.displayNumberOptions" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary" :disabled="!disableBtn()" @click="getFlowExecuteOverviews"> {{ $t('query') }}</Button>
      </div>
    </div>
    <Table size="small" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
  </div>
</template>

<script>
import { getFlowExecuteOverviews, getProcessList } from '@/api/server.js'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
      tableData: [],
      searchConfig: {
        params: {
          startDate: '',
          endDate: '',
          procDefIds: [],
          pageable: {
            pageSize: 100,
            startIndex: 0
          }
        },
        processOptions: [],
        displayNumberOptions: [100, 300, 500, 1000]
      },
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
          title: this.$t('success_count'),
          key: 'totalCompletedInstances',
          render: (h, params) => {
            return (
              <div>
                <span style="color:#2d8cf0">{params.row.totalCompletedInstances}</span>
              </div>
            )
          }
        },
        {
          title: this.$t('failure_count'),
          key: 'totalFaultedInstances',
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalFaultedInstances}</span>
              </div>
            )
          }
        },
        {
          title: this.$t('in_progress_count'),
          key: 'totalInProgressInstances',
          render: (h, params) => {
            return (
              <div>
                <span style="color:#19be6b">{params.row.totalInProgressInstances}</span>
              </div>
            )
          }
        },
        {
          title: this.$t('count'),
          key: 'totalInstances'
        }
      ]
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 300
  },
  methods: {
    async getFlowExecuteOverviews () {
      const { status, data, message } = await getFlowExecuteOverviews(this.searchConfig.params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.tableData = data
      }
    },
    async getProcess () {
      const { status, data } = await getProcessList()
      if (status === 'OK') {
        this.searchConfig.processOptions = data
      }
    },
    getDate (dateRange) {
      this.searchConfig.params.startDate = dateRange[0]
      this.searchConfig.params.endDate = dateRange[1]
    },
    disableBtn () {
      return (
        this.searchConfig.params.startDate !== '' &&
        this.searchConfig.params.endDate !== '' &&
        this.searchConfig.params.procDefIds.length !== 0
      )
    }
  },
  components: {}
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
