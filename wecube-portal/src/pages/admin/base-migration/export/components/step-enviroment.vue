<template>
  <div class="export-step-enviroment">
    <div class="item">
      <span class="title">选择环境：</span>
      <RadioGroup v-model="form.environment" type="button" button-style="solid">
        <Radio v-for="(j, idx) in environmentList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
      </RadioGroup>
    </div>
    <div class="item">
      <span class="title">选择产品<span class="number">{{ selectionList.length }}</span>：</span>
      <div>
        <BaseSearch
          :showBtn="false"
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
          @on-selection-change="handleSelectChange"
        >
        </Table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        environment: 0
      },
      environmentList: [
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
        name: '',
        id: ''
      },
      searchOptions: [
        {
          key: 'name',
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
              {params.row.name}
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
          minWidth: 140
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        }
      ],
      tableData: [],
      selectionList: [],
      loading: false
    }
  },
  methods: {
    handleSelectChange() {},
    getProductList() {},
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
