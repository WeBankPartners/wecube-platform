<!--批量执行-模板管理-->
<template>
  <div class="batch-execution-template">
    <div class="search">
      <!--搜索条件-->
      <BaseSearch :options="searchOptions" v-model="form" @search="handleSearch" :showExpand="false"></BaseSearch>
    </div>
    <div class="switch">
      <span>仅展示收藏模板</span>
      <i-Switch v-model="form.isShowCollectTemplate" @on-change="getTemplateList()"></i-Switch>
    </div>
    <div class="table">
      <template v-if="cardList.length">
        <Card v-for="(i, index) in cardList" :key="index" style="width: 100%; margin-bottom: 20px">
          <div class="table-header" slot="title">
            <Icon size="28" type="ios-people" />
            <div class="title">
              {{ i.mgmtRole }}
              <span class="underline"></span>
            </div>
            <Icon v-if="i.expand" size="28" type="md-arrow-dropdown" style="cursor: pointer" @click="handleExpand(i)" />
            <Icon v-else size="28" type="md-arrow-dropright" style="cursor: pointer" @click="handleExpand(i)" />
          </div>
          <div v-show="i.expand">
            <Table size="small" :columns="tableColumns" :data="i.data" style="margin: 10px 0 20px 0"> </Table>
          </div>
        </Card>
      </template>
    </div>
  </div>
</template>

<script>
import BaseSearch from '@/pages/components/base-search.vue'
import { getBatchExecuteTemplateList } from '@/api/server'
export default {
  components: {
    BaseSearch
  },
  data () {
    return {
      searchOptions: [
        {
          key: 'name',
          placeholder: '模板名/ID',
          component: 'input'
        },
        {
          key: 'pluginService',
          placeholder: '插件服务',
          component: 'input'
        },
        {
          key: 'operateObject',
          placeholder: '操作对象类型',
          component: 'input'
        }
      ],
      form: {
        name: '',
        pluginService: '',
        operateObject: '',
        isShowCollectTemplate: true // 仅展示收藏模板
        // permissionType: '' // 权限类型
      },
      // 模板数据
      cardList: [],
      tableColumns: [
        {
          title: '模板ID',
          key: 'id',
          minWidth: 150
        },
        {
          title: '模板名称',
          key: 'name',
          minWidth: 150
        },
        {
          title: '插件服务',
          key: 'pluginService',
          minWidth: 150
        },
        {
          title: '操作对象类型',
          key: 'operateObject',
          minWidth: 120
        },
        {
          title: '使用角色',
          key: 'useRole',
          minWidth: 80,
          render: (h, params) => {
            return (
              <div>
                {params.row.permissionToRole.USE &&
                  params.row.permissionToRole.USE.map(item => {
                    return <span>{item}</span>
                  })}
              </div>
            )
          }
        },
        {
          title: '创建时间',
          key: 'createdTime',
          minWidth: 120
        },
        {
          title: '操作',
          key: 'action',
          width: 180,
          render: (h, params) => {
            return (
              <div style="display:flex;">
                <Tooltip content={'查看'} placement="top">
                  <Icon
                    type="md-eye"
                    size="20"
                    class="icon-btn"
                    onClick={() => {
                      this.handleView()
                    }}
                  ></Icon>
                </Tooltip>
                <Tooltip content={'复制'} placement="top">
                  <Icon
                    type="md-copy"
                    color="#19be6b"
                    size="20"
                    class="icon-btn"
                    onClick={() => {
                      this.handleCopy()
                    }}
                  ></Icon>
                </Tooltip>
                <Tooltip content={'权限'} placement="top">
                  <Icon
                    type="md-person"
                    color="#bfbf3d"
                    size="20"
                    class="icon-btn"
                    onClick={() => {
                      this.handleRole()
                    }}
                  ></Icon>
                </Tooltip>
                <Tooltip content={'删除'} placement="top">
                  <Icon
                    type="md-trash"
                    color="#ed4014"
                    size="20"
                    class="icon-btn"
                    onClick={() => {
                      this.handleDelete()
                    }}
                  ></Icon>
                </Tooltip>
              </div>
            )
          }
        }
      ]
    }
  },
  mounted () {
    this.getTemplateList()
  },
  methods: {
    handleSearch () {
      this.getTemplateList()
    },
    async getTemplateList () {
      const params = {
        filters: [],
        paging: true,
        pageable: {
          startIndex: 0,
          pageSize: 1000
        },
        sorting: [
          {
            asc: false,
            field: 'updatedTime'
          }
        ]
      }
      Object.keys(this.form).forEach(key => {
        params.filters.push({
          name: key,
          operator: key === 'isShowCollectTemplate' ? 'eq' : 'contains',
          value: this.form[key]
        })
      })
      const { status, data } = await getBatchExecuteTemplateList(params)
      if (status === 'OK') {
        let mgmtGroup = []
        data.contents.forEach(item => {
          // 属主角色
          if (item.permissionToRole.MGMT && item.permissionToRole.MGMT.length > 0) {
            item.permissionToRole.MGMT.forEach(role => {
              mgmtGroup.push(role)
            })
          }
        })
        mgmtGroup = Array.from(new Set(mgmtGroup))
        this.cardList = mgmtGroup.map(role => {
          const group = {
            expand: true,
            data: [],
            mgmtRole: role
          }
          data.contents.forEach(item => {
            if (item.permissionToRole.MGMT && item.permissionToRole.MGMT.includes(role)) {
              group.data.push(item)
            }
          })
          return group
        })
      }
    },
    // 展开收缩卡片
    handleExpand (item) {
      item.expand = !item.expand
    },
    // 查看
    handleView () {
      this.$eventBusP.$emit('change-menu', 'templateCreate')
    },
    // 复制
    handleCopy () {},
    // 权限
    handleRole () {},
    // 删除
    handleDelete () {}
  }
}
</script>

<style lang="scss" scoped>
.batch-execution-template {
  width: 100%;
  .search {
    display: flex;
    justify-content: space-between;
  }
  .switch {
    display: flex;
    align-items: center;
    color: #515a6e;
    span {
      margin-right: 10px;
    }
  }
  .table {
    margin-top: 20px;
    .table-header {
      display: flex;
      align-items: center;
      .title {
        font-size: 16px;
        font-weight: bold;
        margin: 0 10px;
        .underline {
          display: block;
          margin-top: -10px;
          margin-left: -6px;
          width: 100%;
          padding: 0 6px;
          height: 12px;
          border-radius: 12px;
          background-color: #c6eafe;
          box-sizing: content-box;
        }
      }
    }
  }
}
</style>
<style lang="scss">
.batch-execution-template {
  .ivu-card-head {
    border-bottom: 1px solid #e8eaec;
    padding: 5px 10px;
    line-height: 1;
  }
  .content .ivu-table-row {
    cursor: pointer;
  }
  .ivu-form-item {
    margin-bottom: 10px !important;
    display: inline-block !important;
  }
  .icon-btn {
    cursor: pointer;
  }
}
</style>
