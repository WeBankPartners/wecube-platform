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
      <div style="text-align: end">
        <Poptip placement="bottom" width="200">
          <Button type="primary" icon="ios-funnel-outline" ghost></Button>
          <div class="api" slot="content">
            <CheckboxGroup v-model="disabledGroup" @on-change="changeColumns">
              <Checkbox
                v-for="item in oriDetailTableColums"
                :label="item.title"
                style="display:block"
                :disabled="item.disabled"
                :key="item.key"
              >
                {{ item.title }}
              </Checkbox>
            </CheckboxGroup>
          </div>
        </Poptip>
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

      oriDetailTableColums: [],
      detailTableColums: [],
      detailTableData: [],
      disabledGroup: ['#', this.$t('params_type'), this.$t('params_name')]
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 200
  },
  methods: {
    changeColumns () {
      this.detailTableColums = this.oriDetailTableColums.filter(col => {
        return this.disabledGroup.includes(col.title)
      })
    },
    initData (data) {
      this.detailTableData = []
      this.oriDetailTableColums = [
        {
          key: '#',
          title: '#',
          type: 'index',
          width: 60,
          disabled: true,
          isDisplay: true,
          align: 'center'
        },
        {
          title: this.$t('params_type'),
          width: 100,
          disabled: true,
          isDisplay: true,
          key: 'type'
        },
        {
          title: this.$t('params_name'),
          width: 200,
          disabled: true,
          isDisplay: true,
          key: 'title'
        }
      ]
      data.forEach((d, index) => {
        this.disabledGroup.push(d.procExecDate)
        this.oriDetailTableColums.push({
          title: d.procExecDate,
          tooltip: true,
          key: 'value' + index,
          disabled: false,
          isDisplay: true,
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
      this.detailTableColums = JSON.parse(JSON.stringify(this.oriDetailTableColums))
      this.showModal = true
    }
  },
  components: {}
}
</script>

<style scoped lang="scss"></style>
