<template>
  <div class=" ">
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
      <div>
        <Table :columns="detailTableColums" size="small" :max-height="MODALHEIGHT" :data="detailTableData"></Table>
      </div>
    </Modal>
  </div>
</template>

<script>
export default {
  name: '',
  data () {
    return {
      showModal: false,
      MODALHEIGHT: 200,
      fullscreen: false,

      detailTableColums: [],
      detailTableData: []
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 200
  },
  methods: {
    initData (data) {
      this.detailTableData = []
      this.detailTableColums = [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('params_type'),
          width: 100,
          key: 'type'
        },
        {
          title: this.$t('params_name'),
          width: 200,
          key: 'title'
        }
      ]
      let tmp = JSON.parse(JSON.stringify(data[0]))
      tmp.procExecDate = 'xxxx-xx-xx xx:xx'
      tmp.execParams[0].paramDataValue = '423564758'
      data.push(tmp)

      let tmp1 = JSON.parse(JSON.stringify(data[0]))
      tmp1.procExecDate = 'xxxx-xx-xx xx:xx'
      tmp1.execParams[0].paramDataValue = '423564758'
      data.push(tmp1)

      let tmp2 = JSON.parse(JSON.stringify(data[0]))
      tmp2.procExecDate = 'xxxx-xx-xx xx:xx'
      tmp2.execParams[0].paramDataValue = '423564758'
      data.push(tmp2)

      data.forEach((d, index) => {
        this.detailTableColums.push({
          title: d.procExecDate,
          tooltip: true,
          key: 'value' + index,
          other: d,
          renderHeader: (h, params) => {
            return (
              <Tooltip content="">
                <span>{params.column.title}</span>
                <div slot="content">
                  <p>
                    {this.$t('flow_name')}:{params.column.other.procDefName}
                  </p>
                  <p>
                    {this.$t('execute_date')}:{params.column.other.procExecDate}
                  </p>
                  <p>
                    {this.$t('executor')}:{params.column.other.procExecOper}
                  </p>
                  <p>
                    {this.$t('flow_status')}:{params.column.other.procStatus}
                  </p>
                  <p>
                    {this.$t('node_execute_date')}:{params.column.other.nodeExecDate}
                  </p>
                  <p>
                    {this.$t('node_status')}:{params.column.other.nodeStatus}
                  </p>
                </div>
              </Tooltip>
            )
          }
        })
        if (this.detailTableData.length === 0) {
          d.execParams.forEach(p => {
            let row = {}
            row.title = p.paramName
            row.type = p.paramType
            row['value' + index] = p.paramDataValue
            this.detailTableData.push(row)
          })
        } else {
          d.execParams.forEach(p => {
            let find = this.detailTableData.find(tp => tp.title === p.paramName && tp.type === p.paramType)
            find['value' + index] = p.paramDataValue
          })
        }
      })
      this.showModal = true
    }
  },
  components: {}
}
</script>

<style scoped lang="scss"></style>
