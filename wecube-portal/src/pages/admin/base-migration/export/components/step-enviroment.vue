<template>
  <div class="export-step-enviroment">
    <!--选择环境-->
    <div class="inline-item">
      <span class="title">{{ $t('pe_select_env') }}</span>
      <RadioGroup v-model="env" type="button" button-style="solid">
        <Radio v-for="(j, idx) in envList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
      </RadioGroup>
    </div>
    <div class="inline-item">
      <div style="display: flex; align-items: center">
        <span class="title">{{ $t('pi_data_confirmTime') }}</span>
        <DatePicker
          type="datetime"
          format="yyyy-MM-dd HH:mm:ss"
          :value="lastConfirmTime"
          @on-change="
            val => {
              lastConfirmTime = val
            }
          "
          :placeholder="$t('tw_please_select')"
          style="width: 250px"
          clearable
        ></DatePicker>
        <span class="sub-title">*{{ $t('pi_data_confirmTimeTips') }}</span>
      </div>
    </div>
    <!--选择产品-->
    <div class="item">
      <span class="title">{{ $t('pe_select_product') }}<span class="number">{{ selectionList.length }}</span></span>
      <div>
        <BaseSearch
          :onlyShowReset="true"
          :options="searchOptions"
          v-model="searchParams"
          @search="handleSearchTable"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="loading"
          :columns="tableColumns"
          :max-height="500"
          :data="tableData"
          @on-selection-change="onSelectionChange"
        >
        </Table>
      </div>
    </div>
  </div>
</template>

<script>
import { getExportBusinessList } from '@/api/server'
import { deepClone } from '@/const/util'
import dayjs from 'dayjs'
export default {
  props: {
    detailData: Object,
    from: String
  },
  data() {
    return {
      env: '', // 选择环境
      lastConfirmTime: dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'), // 数据确认时间
      envList: [],
      searchParams: {
        displayName: '',
        id: ''
      },
      searchOptions: [
        {
          key: 'displayName',
          placeholder: this.$t('pe_business_product'),
          component: 'input'
        },
        {
          key: 'id',
          placeholder: this.$t('pe_product_id'),
          component: 'input'
        }
      ],
      tableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: this.$t('pe_business_product'),
          minWidth: 180,
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {params.row.displayName}
            </span>
          )
        },
        {
          title: this.$t('pe_product_id'),
          minWidth: 180,
          key: 'id'
        },
        {
          title: this.$t('pe_product_des'),
          key: 'description',
          minWidth: 140,
          render: (h, params) => <span>{params.row.description || '-'}</span>
        },
        {
          title: this.$t('updatedBy'),
          key: 'update_user',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'update_time',
          minWidth: 150
        }
      ],
      tableData: [],
      originTableData: [],
      selectionList: [],
      loading: false
    }
  },
  watch: {
    detailData: {
      handler(val) {
        if (val && val.environment) {
          this.env = val.environment
        }
      },
      immediate: true,
      deep: true
    }
  },
  async mounted() {
    await this.getProductList()
    await this.getEnviromentList()
    if (this.detailData && this.detailData.environment) {
      this.env = this.detailData.environment
      const selectIds = this.detailData.business.split(',')
      this.tableData.forEach(i => {
        if (selectIds.includes(i.id)) {
          this.$set(i, '_checked', true)
        }
      })
      this.selectionList = this.tableData.filter(i => i._checked)
      this.lastConfirmTime = this.detailData.lastConfirmTime
    }
  },
  methods: {
    // 表格搜索
    handleSearchTable() {
      this.tableData = this.originTableData.filter(item => {
        const nameFlag = item.displayName.toLowerCase().indexOf(this.searchParams.displayName.toLowerCase()) > -1
        const idFlag = item.id.indexOf(this.searchParams.id) > -1
        if (nameFlag && idFlag) {
          return true
        }
      })
      const selectIds = this.selectionList.map(i => i.id)
      this.tableData.forEach(i => {
        if (selectIds.includes(i.id)) {
          this.$set(i, '_checked', true)
        }
      })
    },
    onSelectionChange(selection) {
      this.selectionList = selection
    },
    // 获取环境列表
    async getEnviromentList() {
      const params = {
        queryMode: 'env' // env代表查询环境，空代表查询产品
      }
      const { status, data } = await getExportBusinessList(params)
      if (status === 'OK') {
        this.envList = data
          && data.map(item => ({
            label: item.displayName,
            value: item.id
          }))
        if (!this.env) {
          this.env = this.envList[0].value
        }
      }
    },
    // 获取产品列表
    async getProductList() {
      const params = {
        id: this.searchParams.id,
        displayName: this.searchParams.displayName,
        queryMode: '' // env代表查询环境，空代表查询产品
      }
      this.loading = true
      const { status, data } = await getExportBusinessList(params)
      this.loading = false
      if (status === 'OK') {
        this.tableData = data || []
        this.originTableData = deepClone(this.tableData)
      }
    },
    jumpToHistory() {}
  }
}
</script>

<style lang="scss" scoped>
.export-step-enviroment {
  .inline-item {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-left: 12px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      width: 100px;
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
    }
    .sub-title {
      font-size: 14px;
      font-weight: normal;
      color: #ed4014;
      margin-left: 5px;
    }
  }
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
    padding-left: 12px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
    }
  }
}
</style>
<style lang="scss">
.export-step-enviroment {
  .common-base-search-button {
    width: fit-content;
  }
}
</style>
