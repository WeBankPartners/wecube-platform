<template>
  <div style="text-align: end;">
    <Button type="primary" style="margin-bottom:16px;" @click="getFlowExecuteOverviews"> {{ $t('query') }}</Button>
    <Table size="small" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
  </div>
</template>

<script>
import { getFlowExecuteOverviews } from '@/api/server.js'
export default {
  name: '',
  data () {
    return {
      MODALHEIGHT: 0,
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
    this.getFlowExecuteOverviews()
  },
  methods: {
    async getFlowExecuteOverviews () {
      const { status, data } = await getFlowExecuteOverviews()
      if (status === 'OK') {
        this.tableData = data
      }
    }
  },
  components: {}
}
</script>

<style scoped lang="scss"></style>
