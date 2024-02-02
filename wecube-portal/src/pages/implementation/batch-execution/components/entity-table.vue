<template>
  <div class="batch-execution-entity-table">
    <div class="search">
      <RadioGroup v-model="activeTab">
        <Radio label="entity" border>
          <span
            >已选择<span class="count">{{ selectData.length }}</span></span
          >
        </Radio>
      </RadioGroup>
      <Input v-model="keyword" clearable placeholder="所有字段模糊查询" class="input" />
      <Button type="primary" @click="handleSearch" style="margin-left: 20px">搜索</Button>
    </div>
    <Table
      v-if="columns.length > 0"
      size="small"
      type="selection"
      :width="200 * columns.length - 122"
      :columns="columns"
      :data="tableData"
      :loading="loading"
      @on-selection-change="handleChooseData"
      :max-height="400"
      style="margin-left: -100px; max-width: 100%"
    ></Table>
    <div v-else class="no-data">暂无数据</div>
  </div>
</template>

<script>
import { deepClone } from '@/const/util'
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
    }
  },
  data () {
    return {
      activeTab: 'entity',
      keyword: '',
      selectData: [],
      tableData: []
    }
  },
  watch: {
    data: {
      handler (val) {
        this.tableData = deepClone(val)
      },
      immediate: true,
      deep: true
    }
  },
  mounted () {},
  methods: {
    handleChooseData (selection) {
      this.selectData = selection
      this.$emit('select', selection)
    },
    handleSearch () {
      const filtersKeys = this.columns.map(item => item.key)
      this.tableData = []
      if (this.keyword) {
        this.data.forEach(item => {
          let tmp = []
          filtersKeys.forEach(key => {
            tmp += item[key] + '@#$'
          })
          if (tmp.includes(this.keyword)) {
            this.tableData.push(item)
          }
        })
      } else {
        this.tableData = this.data
      }
      const selectTag = this.selectData.map(item => item.id)
      this.tableData.forEach(item => {
        if (selectTag.includes(item.id)) {
          item._checked = true
        }
      })
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
