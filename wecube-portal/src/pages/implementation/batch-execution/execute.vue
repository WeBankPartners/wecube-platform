<template>
  <div class="batch-execute-history">
    <div class="search">
      <!--搜索条件-->
      <BaseSearch :options="searchOptions" v-model="form" @search="handleQuery" :showExpand="false"></BaseSearch>
    </div>
    <!--表格分页-->
    <Row :gutter="20">
      <Col :span="8">
        <Card :style="{ minHeight: maxHeight + 'px', maxHeight: maxHeight + 'px' }">
          <div class="title" slot="title">执行记录列表</div>
          <Table
            class="hover"
            size="small"
            :loading="loading"
            :columns="tableColumns"
            :data="tableData"
            width="100%"
            :max-height="maxHeight - 100"
            @on-row-click="handleExecuteHistory"
          ></Table>
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
        <ExecuteResult ref="executeResult"></ExecuteResult>
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
          key: 'errorCode',
          placeholder: '执行状态',
          component: 'select',
          list: [
            { label: '成功', value: '0' },
            { label: '失败', value: '1' }
          ]
        },
        {
          key: 'createdTimeT',
          label: '执行时间',
          dateType: 1,
          initValue: [dayjs().subtract(3, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
          labelWidth: 110,
          component: 'custom-time'
        }
      ],
      form: {
        name: '',
        id: '',
        errorCode: '',
        createdTimeT: [dayjs().subtract(3, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
      },
      tableData: [],
      tableColumns: [
        {
          title: '名称',
          minWidth: 100,
          key: 'name'
        },
        {
          title: '状态',
          minWidth: 80,
          key: 'errorCode',
          render: (h, params) => {
            return (
              <Tooltip max-width="300" content={params.row.errorMessage || '--'}>
                <Tag color={params.row.errorCode === '0' ? 'success' : 'error'}>
                  {params.row.errorCode === '0' ? '成功' : '失败'}
                </Tag>
              </Tooltip>
            )
          }
        },
        {
          title: '执行时间',
          minWidth: 110,
          key: 'createdTime',
          render: (h, params) => {
            return <span>{params.row.createdTime || '--'}</span>
          }
        },
        {
          title: '操作',
          key: 'action',
          width: 100,
          fixed: 'right',
          align: 'center',
          render: (h, params) => {
            return (
              <div style="display:flex;">
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
                <Tooltip content={'重新发起'} placement="top">
                  <Button
                    size="small"
                    type="warning"
                    onClick={() => {
                      this.handleRelaunch(params.row)
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-add-circle" size="16"></Icon>
                  </Button>
                </Tooltip>
              </div>
            )
          }
        }
      ],
      loading: false,
      historyList: [],
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      },
      maxHeight: 500,
      rowId: ''
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 150
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
            field: 'updatedTimeT'
          }
        ]
      }
      Object.keys(this.form).forEach(key => {
        if (['name', 'id'].includes(key) && this.form[key]) {
          params.filters.push({
            name: key,
            operator: 'contains',
            value: this.form[key]
          })
        } else if (key === 'errorCode' && this.form[key]) {
          params.filters.push({
            name: key,
            operator: 'eq',
            value: this.form[key]
          })
        } else if (key === 'createdTimeT' && this.form[key] && this.form[key].length > 0) {
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
        this.$nextTick(() => {
          this.$refs.executeResult.getList(data.contents[0].id)
        })
        this.pagination.total = data.pageInfo.totalRows
      }
    },
    // 执行详情
    handleExecuteDetail (row) {
      this.$eventBusP.$emit('change-menu', 'executeCreate')
      this.$router.replace({
        name: this.$route.name,
        query: {
          ...this.$route.params,
          // 更新的参数
          id: row.id,
          type: 'view'
        }
      })
    },
    // 重新发起
    handleRelaunch (row) {
      this.$eventBusP.$emit('change-menu', 'executeCreate')
      this.$router.replace({
        name: this.$route.name,
        query: {
          ...this.$route.params,
          // 更新的参数
          id: row.id,
          type: 'add'
        }
      })
    },
    // 执行历史
    async handleExecuteHistory (row) {
      this.$refs.executeResult && this.$refs.executeResult.getList(row.id)
    }
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
  .hover .ivu-table-row {
    cursor: pointer;
  }
  .search {
    display: flex;
    justify-content: space-between;
  }
  .pagination {
    display: flex;
    justify-content: flex-end;
  }
  .word-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
  }
}
</style>
