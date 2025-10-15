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
import { getBaseMigrationExportList, getBaseMigrationExportQuery } from '@/api/server'
import { updateTimeBasedOnDateType } from '@/const/util'
export default {
  data() {
    return {
      MODALHEIGHT: 0,
      searchOptions: [
        // 创建时间
        {
          key: 'time',
          label: this.$t('table_created_date'),
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
        // 目标客户
        {
          key: 'customerIds',
          placeholder: this.$t('pi_target_custom'),
          multiple: true,
          component: 'select',
          list: []
        },
        // 导出产品
        {
          key: 'business',
          placeholder: this.$t('pe_export_product'),
          multiple: true,
          component: 'select',
          list: []
        },
        // 导出状态
        {
          key: 'status',
          placeholder: this.$t('pe_export_status'),
          component: 'tag-select',
          multiple: true,
          list: [
            {
              label: this.$t('be_status_draft'),
              value: 'start',
              color: '#808695'
            },
            {
              label: this.$t('fe_inProgress'),
              value: 'doing',
              color: '#5384ff'
            },
            {
              label: this.$t('be_success'),
              value: 'success',
              color: '#00cb91'
            },
            {
              label: this.$t('be_error'),
              value: 'fail',
              color: '#ff4d4f'
            }
          ]
        },
        // 记录ID
        {
          key: 'id',
          placeholder: this.$t('pe_record_id'),
          component: 'input'
        },
        // 创建人
        {
          key: 'operators',
          placeholder: this.$t('createdBy'),
          multiple: true,
          component: 'select',
          list: []
        }
      ],
      searchParams: {
        id: '',
        time: [dayjs(new Date()).subtract(3, 'month')
          .format('YYYY-MM-DD'), dayjs(new Date()).format('YYYY-MM-DD')],
        execTimeStart: '',
        execTimeEnd: '',
        customerIds: [],
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
        // 目标客户
        {
          title: this.$t('pi_target_custom'),
          key: 'customerName',
          minWidth: 200,
          render: (h, params) => <span>{params.row.customerName || '-'}</span>
        },
        // 导出产品
        {
          title: this.$t('pe_export_product'),
          key: 'businessName',
          minWidth: 200,
          render: (h, params) => {
            const productList = params.row.businessName.split(',')
            return <BaseScrollTag list={productList} />
          }
        },
        // 导出状态
        {
          title: this.$t('pe_export_status'),
          key: 'status',
          minWidth: 120,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('be_status_draft'),
                value: 'start',
                color: '#808695'
              },
              {
                label: this.$t('fe_inProgress'),
                value: 'doing',
                color: '#5384ff'
              },
              {
                label: this.$t('be_success'),
                value: 'success',
                color: '#00cb91'
              },
              {
                label: this.$t('be_error'),
                value: 'fail',
                color: '#ff4d4f'
              }
            ]
            const findObj = list.find(item => item.value === params.row.status) || {}
            return <Tag color={findObj.color}>{findObj.label}</Tag>
          }
        },
        {
          title: this.$t('pe_record_id'),
          minWidth: 160,
          key: 'id'
        },
        {
          title: this.$t('createdBy'),
          key: 'createdUser',
          minWidth: 120
        },
        {
          title: this.$t('table_created_date'),
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
          width: 110,
          align: 'center',
          fixed: 'right',
          render: (h, params) => (
            <div style="display:flex;justify-content:center;">
              {params.row.status !== 'start' && (
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
              )}
              {['success', 'fail'].includes(params.row.status) && (
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
              {params.row.status === 'start' && (
                <Tooltip content={this.$t('edit')} placement="top">
                  <Button
                    size="small"
                    type="primary"
                    onClick={() => {
                      this.handleEdit(params.row) // 编辑
                    }}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-create" size="16"></Icon>
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
      if (from.path === '/admin/base-migration/export' && Object.keys(from.query).length > 0) {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('platform_export_baseMigration') || ''
        if (storage) {
          const { searchParams, searchOptions, pageable } = JSON.parse(storage)
          vm.searchParams = searchParams
          vm.searchOptions = searchOptions
          // 确保时间是最新的
          updateTimeBasedOnDateType(vm.searchOptions, vm.searchParams, 'time')
          // 多选下拉框有默认值自动触发onSearch事件，导致页数被重置，采用延时方法解决这个问题
          setTimeout(() => {
            vm.pageable = pageable
            vm.initData()
          }, 500)
        } else {
          vm.initData()
        }
      } else {
        // 列表刷新不能放在mounted, mounted会先执行，导致拿不到缓存参数
        vm.initData()
      }
    })
  },
  beforeDestroy() {
    // 缓存列表搜索条件
    const storage = {
      searchParams: this.searchParams,
      searchOptions: this.searchOptions,
      pageable: this.pageable
    }
    window.sessionStorage.setItem('platform_export_baseMigration', JSON.stringify(storage))
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
      const { status, data } = await getBaseMigrationExportQuery()
      if (status === 'OK') {
        this.searchOptions.forEach(item => {
          if (item.key === 'business') {
            item.list = (data.businessList
                && data.businessList.map(item => ({
                  label: item.businessName,
                  value: item.businessId
                })))
              || []
          } else if (item.key === 'operators') {
            item.list = (data.operators
                && data.operators.map(item => ({
                  label: item,
                  value: item
                })))
              || []
          } else if (item.key === 'customerIds') {
            item.list = (data.customers
                && data.customers.map(item => ({
                  label: item.name,
                  value: item.id
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
        customerIds: this.searchParams.customerIds,
        startIndex: (this.pageable.current - 1) * this.pageable.pageSize,
        pageSize: this.pageable.pageSize,
        execTimeStart: this.searchParams.time[0] ? this.searchParams.time[0] + ' 00:00:00' : undefined,
        execTimeEnd: this.searchParams.time[1] ? this.searchParams.time[1] + ' 23:59:59' : undefined
      }
      this.loading = true
      const { status, data } = await getBaseMigrationExportList(params)
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
        path: '/admin/base-migration/export',
        query: {
          type: 'detail',
          id: row.id
        }
      })
    },
    handleEdit(row) {
      this.$router.push({
        path: '/admin/base-migration/export',
        query: {
          type: 'edit',
          id: row.id
        }
      })
    },
    // 重新发起
    handleRepub(row) {
      this.$router.push({
        path: '/admin/base-migration/export',
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
