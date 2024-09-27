<template>
  <div class="base-migration-import-history">
    <BaseSearch :options="searchOptions" v-model="searchParams" @search="handleQuery"></BaseSearch>
    <Table
      size="small"
      ref="table"
      :columns="tableColumns"
      :max-height="MODALHEIGHT"
      :data="tableData"
      :loading="loading"
    ></Table>
    <Page
      style="float: right; margin-top: 16px"
      :total="pageable.total"
      @on-change="changPage"
      show-sizer
      :current="pageable.current"
      :page-size="pageable.pageSize"
      @on-page-size-change="changePageSize"
      show-total
    />
  </div>
</template>

<script>
import dayjs from 'dayjs'
import { getBaseMigrationImportList, getBaseMigrationImportQuery } from '@/api/server'
export default {
  data() {
    return {
      MODALHEIGHT: 0,
      searchOptions: [
        // 执行时间
        {
          key: 'time',
          label: this.$t('execute_date'),
          initDateType: 1,
          dateRange: [
            {
              label: this.$t('fe_recent3Months'),
              type: 'month',
              value: 3,
              dateType: 1
            },
            {
              label: this.$t('fe_recentHalfYear'),
              type: 'month',
              value: 6,
              dateType: 2
            },
            {
              label: this.$t('fe_recentOneYear'),
              type: 'year',
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
        },
        // 记录ID
        {
          key: 'id',
          placeholder: '记录ID',
          component: 'input'
        },
        // 导入状态
        {
          key: 'status',
          placeholder: '导入状态',
          component: 'tag-select',
          multiple: true,
          list: [
            {
              label: '执行中',
              value: 'doing',
              color: '#2d8cf0'
            },
            {
              label: '成功',
              value: 'success',
              color: '#19be6b'
            },
            {
              label: '失败',
              value: 'fail',
              color: '#ed4014'
            },
            {
              label: '终止',
              value: 'cancel',
              color: '#ff9900'
            }
          ]
        },
        // 导入产品
        {
          key: 'business',
          placeholder: '导入产品',
          multiple: true,
          component: 'select',
          list: []
        },
        // 执行人
        {
          key: 'operators',
          placeholder: '执行人',
          multiple: true,
          component: 'select',
          list: []
        }
      ],
      searchParams: {
        id: '',
        time: [dayjs().subtract(3, 'month')
          .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
        execTimeStart: '',
        execTimeEnd: '',
        status: [],
        business: [],
        operators: []
      },
      pageable: {
        pageSize: 10,
        startIndex: 1,
        current: 1,
        total: 0
      },
      tableData: [],
      loading: false,
      tableColumns: [
        {
          title: '导入产品',
          key: 'businessName',
          minWidth: 200,
          render: (h, params) => <span>{params.row.businessName || '-'}</span>
        },
        {
          title: '导入状态',
          key: 'status',
          minWidth: 120,
          render: (h, params) => {
            const list = [
              {
                label: '执行中',
                value: 'doing',
                color: '#2d8cf0'
              },
              {
                label: '成功',
                value: 'success',
                color: '#19be6b'
              },
              {
                label: '失败',
                value: 'fail',
                color: '#ed4014'
              },
              {
                label: '终止',
                value: 'cancel',
                color: '#ff9900'
              }
            ]
            const findObj = list.find(item => item.value === params.row.status) || {}
            return <Tag color={findObj.color}>{findObj.label}</Tag>
          }
        },
        {
          title: '记录ID',
          minWidth: 160,
          key: 'id'
        },
        {
          title: '执行人',
          key: 'createdUser',
          minWidth: 120
        },
        {
          title: this.$t('execute_date'),
          key: 'createdTime',
          minWidth: 150
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedUser',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 100,
          align: 'center',
          fixed: 'right',
          render: (h, params) => (
            <div style="display:flex;justify-content:center;">
              <Tooltip content={this.$t('be_details')} placement="top">
                <Button
                  size="small"
                  type="info"
                  onClick={() => {
                    this.handleView(params.row) // 查看
                  }}
                >
                  <Icon type="md-eye" size="16"></Icon>
                </Button>
              </Tooltip>
              {['success', 'cancel'].includes(params.row.status) && (
                <Tooltip content={this.$t('be_republish')} placement="top">
                  <Button
                    type="success"
                    size="small"
                    onClick={() => {
                      this.handleRepub(params.row) // 重新发起
                    }}
                    style="margin-left:5px;"
                  >
                    <Icon type="ios-refresh" size="16"></Icon>
                  </Button>
                </Tooltip>
              )}
              {['fail', 'doing'].includes(params.row.status) && (
                <Tooltip content={this.$t('stop_orch')} placement="top">
                  <Button
                    size="small"
                    type="error"
                    onClick={() => {
                      this.handleStop(params.row) // 终止
                    }}
                    style="margin-left:5px;"
                  >
                    <Icon type="md-power" size="16"></Icon>
                  </Button>
                </Tooltip>
              )}
            </div>
          )
        }
      ],
      users: []
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/admin/base-migration/import') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('import_baseMigration') || ''
        if (storage) {
          const { searchParams, searchOptions } = JSON.parse(storage)
          vm.searchParams = searchParams
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
      searchParams: this.searchParams,
      searchOptions: this.searchOptions
    }
    window.sessionStorage.setItem('import_baseMigration', JSON.stringify(storage))
  },
  methods: {
    initData() {
      this.MODALHEIGHT = document.body.scrollHeight - 220
      this.getList()
      this.getSearchParams()
    },
    handleQuery() {
      this.pageable.current = 1
      this.getList()
    },
    async getSearchParams() {
      const { status, data } = await getBaseMigrationImportQuery()
      if (status === 'OK') {
        this.searchOptions.forEach(item => {
          if (item.key === 'business') {
            item.list = (data.businessList
                && data.businessList.map(item => ({
                  label: item.businessName,
                  value: item.businessId
                })))
              || []
          }
          if (item.key === 'operators') {
            item.list = (data.operators
                && data.operators.map(item => ({
                  label: item,
                  value: item
                })))
              || []
          }
        })
      }
    },
    async getList() {
      const params = {
        id: this.searchParams.id,
        status: this.searchParams.status,
        business: this.searchParams.business,
        operators: this.searchParams.operators,
        startIndex: (this.pageable.current - 1) * this.pageable.pageSize,
        pageSize: this.pageable.pageSize,
        execTimeStart: this.searchParams.time[0] ? this.searchParams.time[0] + ' 00:00:00' : undefined,
        execTimeEnd: this.searchParams.time[1] ? this.searchParams.time[1] + ' 23:59:59' : undefined
      }
      this.loading = true
      const { status, data } = await getBaseMigrationImportList(params)
      this.loading = false
      if (status === 'OK') {
        this.tableData = (data && data.contents) || []
        this.pageable.total = data.pageInfo.totalRows || 0
      }
    },
    changePageSize(pageSize) {
      this.pageable.current = 1
      this.pageable.pageSize = pageSize
      this.getList()
    },
    changPage(current) {
      this.pageable.current = current
      this.getList()
    },
    // 查看
    handleView(row) {
      this.$router.push({
        path: '/admin/base-migration/import',
        query: {
          type: 'detail',
          id: row.id
        }
      })
    },
    // 终止
    handleStop() {},
    // 重新发起
    handleRepub(row) {
      this.$router.push({
        path: '/admin/base-migration/import',
        query: {
          type: 'republish',
          id: row.id
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.base-migration-import-history {
  .ivu-form-item {
    margin-bottom: 8px;
  }
  .item {
    width: 260px;
    margin: 8px;
  }
}
</style>
