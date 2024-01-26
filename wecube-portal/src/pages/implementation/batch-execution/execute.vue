<template>
  <div class="batch-execute-history">
    <div class="search">
      <!--搜索条件-->
      <BaseSearch :options="searchOptions" v-model="form" @search="handleQuery" :showExpand="false"></BaseSearch>
    </div>
    <!--表格分页-->
    <Row :gutter="20">
      <Col :span="8">
        <Card :style="{ minHeight: maxHeight + 'px' }">
          <div class="title" slot="title">执行记录列表</div>
          <Table size="small" :loading="loading" :columns="tableColumns" :data="tableData" width="100%"></Table>
          <div class="pagination">
            <Page
              :total="pagination.total"
              @on-change="changPage"
              show-sizer
              :current="pagination.currentPage"
              :page-size="pagination.pageSize"
              @on-page-size-change="changePageSize"
              show-total
              size="small"
              style="margin-top: 10px"
            />
          </div>
        </Card>
      </Col>
      <Col :span="16">
        <!--批量执行结果-->
        <ExecuteResult></ExecuteResult>
      </Col>
    </Row>
  </div>
</template>

<script>
import BaseSearch from '@/pages/components/base-search.vue'
import ExecuteResult from './components/execute-result.vue'
import { getBatchExecuteList } from '@/api/server'
import dayjs from 'dayjs'
export default {
  components: {
    BaseSearch,
    ExecuteResult
  },
  data () {
    return {
      searchOptions: [
        {
          key: 'name',
          placeholder: '执行记录名称',
          component: 'input'
        },
        {
          key: 'id',
          placeholder: 'ID',
          component: 'input'
        },
        {
          key: 'status',
          placeholder: '执行状态',
          component: 'select',
          list: [
            { label: '成功', value: 1 },
            { label: '失败', value: 0 }
          ]
        },
        {
          key: 'createdTime',
          label: '执行时间',
          dateType: 1,
          initValue: [dayjs().subtract(3, 'month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          labelWidth: 110,
          component: 'custom-time'
        }
      ],
      form: {
        name: '',
        id: '',
        status: '',
        createdTime: [dayjs().subtract(3, 'month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
      },
      tableData: [],
      tableColumns: [
        {
          title: '名称',
          width: 200,
          key: 'name'
        },
        {
          title: '状态',
          minWidth: 100,
          key: 'errorCode',
          render: (h, params) => {
            return <Tag>{params.row.errorCode === '0' ? '成功' : '失败'}</Tag>
          }
        },
        {
          title: '操作',
          key: 'action',
          width: 80,
          fixed: 'right',
          align: 'center',
          render: (h, params) => {
            return (
              <Tooltip content={'执行详情'} placement="top">
                <Button
                  size="small"
                  type="info"
                  onClick={() => {
                    this.handleExecuteDetail(params.row)
                  }}
                  style="margin-right:5px;"
                >
                  <Icon type="md-eye" size="16"></Icon>
                </Button>
              </Tooltip>
            )
          }
        }
      ],
      loading: false,
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 10
      },
      maxHeight: 500
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 170
    this.getList()
  },
  methods: {
    handleQuery () {
      this.pagination.currentPage = 1
      this.getList()
    },
    changPage (val) {
      this.pagination.currentPage = val
      this.getList()
    },
    changePageSize (val) {
      this.pagination.currentPage = 1
      this.pagination.pageSize = val
      this.getList()
    },
    async getList () {
      const params = {
        filters: [],
        paging: true,
        pageable: {
          startIndex: (this.pagination.currentPage - 1) * this.pagination.pageSize,
          pageSize: this.pagination.pageSize
        },
        sorting: [
          {
            asc: false,
            field: 'updatedTime'
          }
        ]
      }
      Object.keys(this.form).forEach(key => {
        if (['name', 'id'].includes(key)) {
          params.filters.push({
            name: key,
            operator: 'contains',
            value: this.form[key]
          })
        } else if (key === 'status') {
          params.filters.push({
            name: key,
            operator: 'eq',
            value: this.form[key]
          })
        } else if (key === 'createdTime') {
          params.filters.push(
            ...[
              {
                name: key,
                operator: 'gte',
                value: this.form[key][0] + ' 00:00:00'
              },
              {
                name: key,
                operator: 'lte',
                value: this.form[key][1] + ' 23:59:59'
              }
            ]
          )
        }
      })
      this.loading = true
      const { status, data } = await getBatchExecuteList(params)
      this.loading = false
      if (status === 'OK') {
        this.tableData = data.contents || []
        this.pagination.total = data.pageInfo.totalRows
      }
    },
    // 执行详情
    handleExecuteDetail (row) {}
  }
}
</script>

<style lang="scss">
.batch-execute-history {
  width: 100%;
  .title {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    height: 50px;
    font-size: 14px;
    font-weight: bold;
  }
  .ivu-card-head {
    padding: 0 16px !important;
    border: none;
  }
  .ivu-card-body {
    padding: 0 16px 16px 16px !important;
  }
  .search {
    display: flex;
    justify-content: space-between;
  }
  .pagination {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
