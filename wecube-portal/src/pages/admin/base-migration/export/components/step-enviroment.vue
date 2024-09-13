<template>
  <div class="export-step-enviroment">
    <div class="item">
      <span class="title">选择环境：</span>
      <RadioGroup v-model="env" type="button" button-style="solid">
        <Radio v-for="(j, idx) in envList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
      </RadioGroup>
    </div>
    <div class="item">
      <span class="title">选择产品<span class="number">{{ selectionList.length }}</span></span>
      <div>
        <BaseSearch
          :onlyShowReset="true"
          :options="searchOptions"
          v-model="searchParams"
          @search="getProductList"
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
export default {
  data() {
    return {
      env: 0,
      envList: [
        {
          label: 'PRD_生产环境',
          value: 0
        },
        {
          label: 'DEV_开发环境',
          value: 1
        }
      ],
      searchParams: {
        displayName: '',
        id: ''
      },
      searchOptions: [
        {
          key: 'displayName',
          placeholder: '业务产品名',
          component: 'input'
        },
        {
          key: 'id',
          placeholder: '产品ID',
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
          title: '业务产品名',
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
          title: '产品ID',
          minWidth: 180,
          key: 'id'
        },
        {
          title: '产品描述',
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
      selectionList: [],
      loading: false
    }
  },
  mounted() {
    this.getProductList()
    this.getEnviromentList()
  },
  methods: {
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
        this.env = this.envList[0].value
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
      }
    },
    jumpToHistory() {}
  }
}
</script>

<style lang="scss" scoped>
.export-step-enviroment {
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
