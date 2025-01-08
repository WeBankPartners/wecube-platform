<template>
  <div class="batch-execution-entity-table">
    <div class="search">
      <RadioGroup v-model="activeTab">
        <Radio label="entity" border>
          <span
            >{{ $t('be_choose_pre') }}<span class="count">{{ selectData.length }}</span></span
          >
        </Radio>
      </RadioGroup>
      <Input
        v-model="keyword"
        @on-change="handleSearch"
        clearable
        :placeholder="$t('be_all_placeholder')"
        class="input"
      />
    </div>
    <template v-if="columns.length > 0">
      <Table
        size="small"
        type="selection"
        :width="200 * columns.length - 122"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        @on-selection-change="handleChooseData"
        :row-class-name="
          row => {
            return filterIdList.includes(row.id) ? 'ivu-table-row-hover' : ''
          }
        "
        style="margin-left: -100px; max-width: 100%"
      ></Table>
      <Page
        :total="pagination.total"
        @on-change="changPage"
        :current="pagination.currentPage"
        :page-size="pagination.pageSize"
        @on-page-size-change="changePageSize"
        show-total
        size="small"
        style="margin-top: 10px; margin-left: -100px"
      />
    </template>
    <div v-else class="no-data">{{ $t('noData') }}</div>
  </div>
</template>

<script>
import { deepClone, debounce } from '@/const/util'
export default {
  props: {
    data: {
      type: Array,
      default: () => []
    },
    columns: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    pagination: {
      type: Object,
      default: () => ({
        total: 0,
        currentPage: 1,
        pageSize: 50
      })
    }
  },
  data() {
    return {
      activeTab: 'entity',
      keyword: '',
      selectData: [],
      tableData: [],
      filterIdList: []
    }
  },
  watch: {
    data: {
      handler(val) {
        this.tableData = deepClone(val)
        this.selectData = this.tableData.filter(item => item._checked)
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {},
  methods: {
    handleChooseData(selection) {
      this.selectData = selection
      this.$emit('select', selection)
    },
    handleSearch: debounce(function () {
      // const filtersKeys = this.columns.map(item => item.key)
      // this.filterIdList = []
      // if (this.keyword) {
      //   this.data.forEach(item => {
      //     let tmp = []
      //     filtersKeys.forEach(key => {
      //       tmp += item[key] + '@#$'
      //     })
      //     if (tmp.includes(this.keyword)) {
      //       this.filterIdList.push(item.id)
      //     }
      //   })
      // }
      this.$emit('search', this.keyword)
    }, 300),
    changPage(val) {
      this.$emit('changePage', val)
    },
    changePageSize(val) {
      this.$emit('changePageSize', val)
    }
  }
}
</script>

<style lang="scss">
.batch-execution-entity-table {
  width: 100%;
  .ivu-radio {
    display: none;
  }
  .ivu-radio-wrapper {
    border-radius: 20px;
    font-size: 12px;
    color: #000;
    background: #fff;
  }
  .ivu-radio-wrapper-checked.ivu-radio-border {
    border-color: #2d8cf0;
    background: #2d8cf0;
    color: #fff;
  }
  .search {
    display: flex;
    margin-bottom: 10px;
    .input {
      width: 300px;
      margin-left: 10px;
    }
    .count {
      font-weight: bold;
      font-size: 14px;
      margin-left: 10px;
    }
  }
  .no-data {
    width: 800px;
    min-height: 100px;
    border: 1px dashed #d7dadc;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #515a6e;
  }
}
</style>
