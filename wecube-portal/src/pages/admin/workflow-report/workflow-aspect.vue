<template>
  <div>
    <div class="report-container">
      <Search :options="searchOptions" v-model="searchConfig.params" @search="getFlowExecuteOverviews"></Search>
      <!-- <div class="item">
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
      </div> -->
    </div>
    <Table size="small" :columns="tableColumns" :max-height="MODALHEIGHT" :loading="loading" :data="tableData"></Table>
  </div>
</template>

<script>
import Search from '@/pages/components/base-search.vue'
import dayjs from 'dayjs'
import { getFlowExecuteOverviews, getProcessList } from '@/api/server.js'
export default {
  components: {
    Search
  },
  data() {
    return {
      MODALHEIGHT: 0,
      tableData: [],
      loading: false,
      searchConfig: {
        params: {
          time: [dayjs().subtract(3, 'day')
            .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          startDate: '',
          endDate: '',
          procDefIds: [],
          pageSize: ''
        },
        processOptions: [],
        displayNumberOptions: [100, 300, 500, 1000]
      },
      searchOptions: [
        // 时间范围
        {
          key: 'time',
          label: this.$t('datetime_range'),
          initDateType: 1,
          dateRange: [
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
          ],
          labelWidth: 110,
          component: 'custom-time'
        },
        // 编排名称
        {
          key: 'procDefIds',
          placeholder: this.$t('flow_name'),
          component: 'select',
          multiple: true,
          list: []
        },
        // 编排名称
        {
          key: 'pageSize',
          placeholder: this.$t('display_number'),
          component: 'select',
          list: [
            {
              label: '100',
              value: 100
            },
            {
              label: '300',
              value: 300
            },
            {
              label: '500',
              value: 500
            },
            {
              label: '1000',
              value: 1000
            }
          ]
        }
      ],
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
          render: (h, params) => (
            <div>
              <span style="color:#2d8cf0">{params.row.totalCompletedInstances}</span>
            </div>
          )
        },
        {
          title: this.$t('failure_count'),
          key: 'totalFaultedInstances',
          render: (h, params) => (
            <div>
              <span style="color:red">{params.row.totalFaultedInstances}</span>
            </div>
          )
        },
        {
          title: this.$t('in_progress_count'),
          key: 'totalInProgressInstances',
          render: (h, params) => (
            <div>
              <span style="color:#19be6b">{params.row.totalInProgressInstances}</span>
            </div>
          )
        },
        {
          title: this.$t('count'),
          key: 'totalInstances'
        }
      ]
    }
  },
  mounted() {
    this.MODALHEIGHT = document.body.scrollHeight - 300
    this.getProcess()
    this.getFlowExecuteOverviews()
  },
  methods: {
    async getFlowExecuteOverviews() {
      const params = {
        procDefIds: this.searchConfig.params.procDefIds,
        startTime: this.searchConfig.params.time[0] ? this.searchConfig.params.time[0] + ' 00:00:00' : '',
        endTime: this.searchConfig.params.time[1] ? this.searchConfig.params.time[1] + ' 23:59:59' : '',
        pageable: {
          pageSize: this.searchConfig.params.pageSize || 100,
          startIndex: 0
        }
      }
      this.loading = true
      const { status, data, message } = await getFlowExecuteOverviews(params)
      this.loading = false
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.tableData = data
      }
    },
    async getProcess() {
      const { status, data } = await getProcessList()
      if (status === 'OK') {
        this.searchConfig.processOptions = data.map(i => ({
          label: i.procDefName,
          value: i.procDefId
        }))
        this.searchOptions.forEach(item => {
          if (item.key === 'procDefIds') {
            item.list = this.searchConfig.processOptions
          }
        })
      }
    }
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
