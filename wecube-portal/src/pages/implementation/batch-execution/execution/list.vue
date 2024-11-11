<template>
  <div class="batch-execute-history">
    <div class="search">
      <BaseSearch :options="searchOptions" v-model="form" @search="handleQuery"></BaseSearch>
    </div>
    <Row :gutter="20">
      <Col v-show="!expand" :span="8">
        <Card :style="{minHeight: maxHeight + 'px', maxHeight: maxHeight + 'px'}">
          <!--执行记录列表-->
          <div class="title" slot="title">{{ $t('be_execute_record') }}</div>
          <Table
            class="hover"
            size="small"
            :loading="loading"
            :columns="tableColumns"
            :data="tableData"
            width="100%"
            :max-height="maxHeight - 100"
            :row-class-name="
              row => {
                return rowId === row.id ? 'ivu-table-row-highlight' : ''
              }
            "
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
      <Col :span="expand ? 24 : 16" style="position: relative">
        <!--批量执行结果-->
        <ExecuteResult ref="executeResult" @expand="expand = !expand"></ExecuteResult>
      </Col>
    </Row>
  </div>
</template>

<script>
import ExecuteResult from '../components/execute-result.vue'
import { getBatchExecuteList } from '@/api/server'
import dayjs from 'dayjs'
export default {
  components: {
    ExecuteResult
  },
  data() {
    return {
      searchOptions: [
        {
          key: 'name',
          placeholder: this.$t('be_execute_record_name'),
          component: 'input'
        },
        {
          key: 'errorCode',
          placeholder: this.$t('be_execute_status'),
          component: 'select',
          list: [
            {
              label: this.$t('be_success'),
              value: '0'
            },
            {
              label: this.$t('be_error'),
              value: '1'
            }
          ]
        },
        {
          key: 'createdTimeT',
          label: this.$t('execute_date'),
          initDateType: 1,
          dateRange: [
            {
              label: this.$t('be_threeDays_recent'),
              type: 'day',
              value: 3,
              dateType: 1
            },
            {
              label: this.$t('be_oneWeek_recent'),
              type: 'day',
              value: 7,
              dateType: 2
            },
            {
              label: this.$t('be_oneMonth_recent'),
              type: 'month',
              value: 1,
              dateType: 3
            },
            {
              label: this.$t('be_auto'),
              dateType: 4
            } // 自定义
          ],
          labelWidth: 110,
          component: 'custom-time'
        }
      ],
      form: {
        name: '',
        id: '',
        errorCode: '',
        createdTimeT: [dayjs().subtract(3, 'day')
          .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
      },
      tableData: [],
      tableColumns: [
        {
          title: this.$t('name'),
          minWidth: 100,
          key: 'name'
        },
        {
          title: this.$t('status'),
          minWidth: 80,
          key: 'errorCode',
          render: (h, params) => (
            <Tooltip max-width="300" content={params.row.errorMessage || '--'}>
              <Tag color={params.row.errorCode === '0' ? 'success' : 'error'}>
                {params.row.errorCode === '0' ? this.$t('be_success') : this.$t('be_error')}
              </Tag>
            </Tooltip>
          )
        },
        {
          title: this.$t('execute_date'),
          minWidth: 150,
          key: 'createdTime',
          render: (h, params) => <span>{params.row.createdTime || '--'}</span>
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 100,
          fixed: 'right',
          align: 'center',
          render: (h, params) => (
            <div style="display:flex;">
              {/* 执行详情 */}
              <Tooltip content={this.$t('be_execute_detail')} placement="top">
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
              {/* 重新发起 */}
              <Tooltip content={this.$t('be_republish')} placement="top">
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
      ],
      loading: false,
      historyList: [],
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      },
      maxHeight: 500,
      rowId: '',
      expand: false
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/implementation/batch-execution/create-execution') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('search_batchExecution') || ''
        if (storage) {
          const { searchParams, searchOptions } = JSON.parse(storage)
          vm.form = searchParams
          vm.searchOptions = searchOptions
        }
      }
      // 列表刷新不能放在mounted, mounted会先执行，导致拿不到缓存参数
      vm.initData()
    })
  },
  beforeDestroy() {
    // 缓存列表搜索条件
    const storage = {
      searchParams: this.form,
      searchOptions: this.searchOptions
    }
    window.sessionStorage.setItem('search_batchExecution', JSON.stringify(storage))
  },
  methods: {
    initData() {
      this.maxHeight = document.body.clientHeight - 150
      this.getList()
    },
    handleQuery() {
      this.pagination.currentPage = 1
      this.getList()
    },
    changPage(val) {
      this.pagination.currentPage = val
      this.getList()
    },
    changePageSize(val) {
      this.pagination.currentPage = 1
      this.pagination.pageSize = val
      this.getList()
    },
    async getList() {
      const params = {
        filters: [
          {
            name: 'errorCode',
            operator: 'neq',
            value: '3'
          }
        ],
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
          if (this.tableData.length > 0) {
            this.rowId = data.contents[0].id
            this.$refs.executeResult.getList(data.contents[0].id)
            this.$refs.executeResult.reset()
          } else {
            this.$refs.executeResult.handleReset()
          }
        })
        this.pagination.total = data.pageInfo.totalRows
      }
    },
    // 执行详情
    handleExecuteDetail(row) {
      this.$router.push({
        path: '/implementation/batch-execution/create-execution',
        query: {
          // 更新的参数
          id: row.id,
          type: 'view'
        }
      })
    },
    // 重新发起
    handleRelaunch(row) {
      this.$router.push({
        path: '/implementation/batch-execution/create-execution',
        query: {
          // 更新的参数
          id: row.id,
          type: 'copy'
        }
      })
    },
    // 执行历史
    async handleExecuteHistory(row) {
      this.rowId = row.id
      this.$refs.executeResult.getList(row.id)
      this.$refs.executeResult.reset()
    }
  }
}
</script>

<style lang="scss">
.batch-execute-history {
  width: 100%;
  .title {
    display: flex;
    justify-content: space-between;
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
  .highlight {
    background: red;
  }
}
</style>
