<template>
  <div class="base-migration-export-history">
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
        // 导出状态
        {
          key: 'status',
          placeholder: '导出状态',
          component: 'tag-select',
          list: [
            {
              label: this.$t('fe_notStart'),
              value: 'NotStarted',
              color: '#808695'
            },
            {
              label: this.$t('fe_stop'),
              value: 'Stop',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgressFaulted'),
              value: 'InProgress(Faulted)',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgressTimeouted'),
              value: 'InProgress(Timeouted)',
              color: '#ed4014'
            },
            {
              label: this.$t('fe_inProgress'),
              value: 'InProgress',
              color: '#1990ff'
            },
            {
              label: this.$t('fe_completed'),
              value: 'Completed',
              color: '#7ac756'
            },
            {
              label: this.$t('fe_faulted'),
              value: 'Faulted',
              color: '#e29836'
            },
            {
              label: this.$t('fe_internallyTerminated'),
              value: 'InternallyTerminated',
              color: '#e29836'
            }
          ]
        },
        // 导出产品
        {
          key: 'product',
          placeholder: '导出产品',
          component: 'input'
        },
        // 执行人
        {
          key: 'operator',
          placeholder: '执行人',
          component: 'input'
        }
      ],
      searchParams: {
        id: '',
        time: [dayjs().subtract(1, 'month')
          .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
        startTime: '',
        endTime: '',
        status: '',
        product: '',
        operator: ''
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
          title: '记录ID',
          minWidth: 160,
          key: 'id'
        },
        {
          title: '导出状态',
          key: 'status',
          minWidth: 140,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('fe_notStart'),
                value: 'NotStarted',
                color: '#808695'
              },
              {
                label: this.$t('fe_inProgressFaulted'),
                value: 'InProgress(Faulted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgressTimeouted'),
                value: 'InProgress(Timeouted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_stop'),
                value: 'Stop',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgress'),
                value: 'InProgress',
                color: '#1990ff'
              },
              {
                label: this.$t('fe_completed'),
                value: 'Completed',
                color: '#7ac756'
              },
              {
                label: this.$t('fe_faulted'),
                value: 'Faulted',
                color: '#e29836'
              },
              {
                label: this.$t('fe_internallyTerminated'),
                value: 'InternallyTerminated',
                color: '#e29836'
              }
            ]
            const findObj = list.find(item => item.value === params.row.status) || {}
            return <Tag color={findObj.color}>{findObj.label}</Tag>
          }
        },
        // 导入产品
        {
          title: '导入产品',
          key: 'product',
          minWidth: 160,
          render: (h, params) => {
            <BaseScrollTag list={params.row.product}></BaseScrollTag>
          }
        },
        {
          title: '执行人',
          key: 'operator',
          minWidth: 120
        },
        {
          title: this.$t('execute_date'),
          key: 'createdTime',
          minWidth: 150
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
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 80,
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
            </div>
          )
        }
      ],
      users: []
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/admin/base-migration/export') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('export_baseMigration') || ''
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
    window.sessionStorage.setItem('export_baseMigration', JSON.stringify(storage))
  },
  methods: {
    initData() {
      this.MODALHEIGHT = document.body.scrollHeight - 220
      this.getList()
    },
    handleQuery() {
      this.pageable.current = 1
      this.getList()
    },
    getList () {},
    changePageSize(pageSize) {
      this.pageable.current = 1
      this.pageable.pageSize = pageSize
      this.getList()
    },
    changPage(current) {
      this.pageable.current = current
      this.getList()
    },
    handleView() {}
  }
}
</script>

<style scoped lang="scss">
.base-migration-export-history {
  .ivu-form-item {
    margin-bottom: 8px;
  }
  .item {
    width: 260px;
    margin: 8px;
  }
}
</style>
