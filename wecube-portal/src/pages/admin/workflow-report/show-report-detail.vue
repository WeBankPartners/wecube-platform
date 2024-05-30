<template>
  <div class=" ">
    <Modal v-model="showModal" :fullscreen="fullscreen" width="1000" footer-hide>
      <p slot="header">
        <span>{{ $t('be_details') }}</span>
        <Icon
          v-if="!fullscreen"
          @click="fullscreen = true"
          style="float: right; margin: 3px 40px 0 0 !important"
          type="ios-expand"
        />
        <Icon
          v-else
          @click="fullscreen = false"
          style="float: right; margin: 3px 40px 0 0 !important"
          type="ios-contract"
        />
      </p>
      <div style="text-align: end">
        <CheckboxGroup @on-change="changeParamsGroup" v-model="paramsGroup" style="display: initial">
          <Checkbox label="hideParams" value="hideSameParams">{{ $t('hide_same_parameters') }}</Checkbox>
          <Checkbox label="highlightParams" value="highlightDiffParams">{{
            $t('highlight_different_parameters')
          }}</Checkbox>
        </CheckboxGroup>
        <Poptip placement="bottom" width="200">
          <Button type="primary" size="small" icon="ios-funnel-outline" ghost></Button>
          <div class="api" slot="content" style="padding: 8px">
            <CheckboxGroup v-model="disabledGroup" @on-change="changeColumns">
              <Checkbox
                v-for="item in oriDetailTableColums"
                :label="item.key"
                style="display: block"
                :disabled="item.disabled"
                :key="item.key + item.title"
              >
                {{ item.title }}
              </Checkbox>
            </CheckboxGroup>
          </div>
        </Poptip>
        <Table
          id="detailTable"
          :columns="detailTableColums"
          size="small"
          :max-height="MODALHEIGHT"
          :data="detailTableData"
        ></Table>
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
      oriDetailTableData: [],
      detailTableData: [],
      disabledGroup: ['#', 'type', 'title'],
      paramsGroup: [],
      filterCol: []
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 200
    document.getElementById('detailTable').classList.add('wer')
  },
  methods: {
    changeParamsGroup () {
      this.detailTableData = JSON.parse(JSON.stringify(this.oriDetailTableData))
      let filterCol = []
      this.detailTableColums.forEach(col => {
        if (!['#', 'type', 'title'].includes(col.key) && col.isDisplay === true) {
          filterCol.push(col.key)
        }
      })
      this.paramsGroup.forEach(item => {
        if (item === 'hideParams') {
          this.hideSameData(filterCol)
        }
        if (item === 'highlightParams') {
          this.detailTableData.forEach(row => {
            row.cellClassName = {}
          })
          this.filterColumns(filterCol)
        }
      })
    },
    filterColumns (filterCol) {
      if (filterCol.length === 1) {
        return
      }
      let params = {}
      filterCol.forEach(col => {
        params[col] = 'remark'
      })
      this.detailTableData.forEach(row => {
        row.cellClassName = {}
        let set = new Set()
        filterCol.forEach(col => {
          set.add(row[col])
        })
        if (set.size === filterCol.length) {
          row.cellClassName = params
        }
      })
    },
    hideSameData (filterCol) {
      this.detailTableData = []
      if (filterCol.length === 1) {
        this.detailTableData = JSON.parse(JSON.stringify(this.oriDetailTableData))
      } else {
        this.oriDetailTableData.forEach(row => {
          let set = new Set()
          filterCol.forEach(col => {
            set.add(row[col])
          })
          if (set.size === filterCol.length) {
            this.detailTableData.push(row)
          }
        })
      }
      this.detailTableData.forEach(row => {
        row.cellClassName = {}
      })
    },
    changeColumns () {
      this.paramsGroup = []
      this.detailTableData.forEach(row => {
        row.cellClassName = {}
      })
      this.detailTableData = JSON.parse(JSON.stringify(this.oriDetailTableData))
      this.detailTableColums = this.oriDetailTableColums.filter(col => {
        return this.disabledGroup.includes(col.key)
      })
    },
    initData (data) {
      this.disabledGroup = ['#', 'type', 'title']
      this.paramsGroup = []
      this.oriDetailTableData = []
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
        this.disabledGroup.push('value' + index)
        this.filterCol.push('value' + index)
        this.oriDetailTableColums.push({
          title: d.execDate,
          tooltip: true,
          key: 'value' + index,
          width: 550,
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
                    {this.$t('execute_date')}:{params.column.other.execDate}
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
        if (this.oriDetailTableData.length === 0) {
          d.execParams.forEach(p => {
            let row = {}
            row.title = p.paramName
            row.type = p.paramType
            row['value' + index] = p.paramDataValue
            this.oriDetailTableData.push(row)
          })
        } else {
          d.execParams.forEach(p => {
            let find = this.oriDetailTableData.find(tp => tp.title === p.paramName && tp.type === p.paramType)
            find['value' + index] = p.paramDataValue
          })
        }
      })
      this.detailTableColums = JSON.parse(JSON.stringify(this.oriDetailTableColums))
      this.detailTableData = JSON.parse(JSON.stringify(this.oriDetailTableData))
      this.detailTableData.forEach(row => {
        row.cellClassName = {}
      })
      this.showModal = true
    }
  },
  components: {}
}
</script>

<style>
.remark {
  background-color: green !important;
  color: #fff;
}
</style>
