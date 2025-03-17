<template>
  <div class="batch-execution-entity-table">
    <div class="search">
      <RadioGroup v-model="activeTab">
        <Radio label="entity" border>
          <span>{{ $t('be_choose_pre') }}<span class="count">{{ selectData.length }}</span></span>
        </Radio>
      </RadioGroup>
      <Button type="primary" @click="handleClearData">{{ $t('pi_reset_data') }}</Button>
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
        ref="table"
        size="small"
        type="selection"
        :width="200 * columns.length - 122"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        @on-select="hanldeChooseOne"
        @on-select-cancel="handleCancelOne"
        @on-select-all="handleChooseAll"
        @on-select-all-cancel="handleCancelAll"
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
    },
    initSelectedRows: {
      type: Array,
      default: () => []
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
      },
      immediate: true,
      deep: true
    },
    initSelectedRows: {
      handler(val) {
        if (Array.isArray(val) && val.length > 0) {
          this.selectData = val
        }
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {},
  methods: {
    // 单选
    hanldeChooseOne(selection, row) {
      this.selectData.push(row)
      this.$emit('select', this.selectData)
    },
    // 单选取消
    handleCancelOne(selection, row) {
      const index = this.selectData.findIndex(item => item.id === row.id)
      this.selectData.splice(index, 1)
      this.$emit('select', this.selectData)
    },
    // 全选
    handleChooseAll(selection) {
      const ids = this.selectData.map(item => item.id)
      const pushArr = selection.filter(item => !ids.includes(item.id))
      this.selectData.push(...pushArr)
      this.$emit('select', this.selectData)
    },
    // 全部取消
    handleCancelAll() {
      this.tableData.forEach(i => {
        const index = this.selectData.findIndex(j => i.id === j.id)
        if (index > -1) {
          this.selectData.splice(index, 1)
        }
      })
      this.$emit('select', this.selectData)
    },
    handleClearData() {
      this.selectData = []
      this.$refs.table.selectAll(false)
      this.$emit('select', this.selectData)
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
    border-color: #5384ff;
    background: #5384ff;
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
