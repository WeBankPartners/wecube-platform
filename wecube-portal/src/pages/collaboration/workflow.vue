<template>
  <div class="root">
    <div>
      <Button type="success" class="btn-right" @click="create">
        <Icon type="ios-add-circle-outline" size="16"></Icon>
        {{ $t('create') }}
      </Button>
      <Button type="info" class="btn-right">
        <img src="../../assets/icon/export.png" class="btn-img" alt="" />
        {{ $t('export_flow') }}
      </Button>

      <Button
        type="primary"
        class="btn-right"
        v-if="['deployed'].includes(searchParams.status) && selectedParams.ids.length > 0"
      >
        <img src="../../assets/icon/import.png" class="btn-img" alt="" />
        {{ $t('import_flow') }}
      </Button>
      <Button
        type="warning"
        class="btn-right"
        @click="batchAuth"
        v-if="['deployed', 'draft'].includes(searchParams.status) && selectedParams.ids.length > 0"
      >
        <Icon type="ios-person-outline" size="16"></Icon>
        {{ $t('config_permission') }}
      </Button>
      <Button
        type="error"
        class="btn-right"
        @click="batchChangeStatus('deleted')"
        v-if="['draft'].includes(searchParams.status) && selectedParams.ids.length > 0"
      >
        <Icon type="ios-trash-outline" size="16"></Icon>
        {{ $t('delete') }}
      </Button>
      <Button
        type="error"
        v-if="['deployed'].includes(searchParams.status) && selectedParams.ids.length > 0"
        @click="batchChangeStatus('disabled')"
      >
        <img src="../../assets/icon/disable.png" class="btn-img" />
        {{ $t('disable') }}
      </Button>
      <Button
        type="success"
        style="background-color: #5da782; border-color: #5da782"
        @click="batchChangeStatus('enabled')"
        v-if="['disabled'].includes(searchParams.status) && selectedParams.ids.length > 0"
      >
        <img src="../../assets/icon/enable.png" class="btn-img" alt="" />
        {{ $t('enable') }}
      </Button>
    </div>
    <div style="margin: 8px 0">
      <Input
        v-model="searchParams.procDefId"
        placeholder="编排ID"
        style="width: 200px"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Input
        v-model="searchParams.procDefName"
        placeholder="编排名称"
        style="width: 200px"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Select
        v-model="searchParams.plugins"
        filterable
        multiple
        style="width: 200px"
        placeholder="授权插件"
        :max-tag-count="1"
        @on-change="getFlowList"
      >
        <Option v-for="item in authPluginList" :value="item" :key="item">{{ item }} </Option>
      </Select>
      <span>{{ $t('table_updated_date') }}:</span>
      <RadioGroup
        v-if="dateType !== 4"
        v-model="dateType"
        type="button"
        size="small"
        @on-change="handleDateTypeChange(dateType)"
      >
        <Radio v-for="(j, idx) in dateTypeList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
      </RadioGroup>
      <template v-else>
        <DatePicker
          @on-change="
            val => {
              handleDateRange(val)
            }
          "
          type="daterange"
          placement="bottom-end"
          format="yyyy-MM-dd"
          placeholder=""
          style="width: 200px"
        />
        <Icon
          size="18"
          style="cursor: pointer"
          type="md-close-circle"
          @click="
            dateType = 1
            handleDateTypeChange(1)
          "
        />
      </template>
      <span style="float: right">
        <Button @click="getFlowList" type="primary">{{ $t('search') }}</Button>
        <Button @click="handleReset" style="margin-left: 5px">{{ $t('reset') }}</Button>
      </span>
    </div>
    <div>
      <Tabs :value="searchParams.status" @on-click="changeTab">
        <TabPane label="已发布" name="deployed"></TabPane>
        <TabPane label="未发布" name="draft"></TabPane>
        <TabPane label="已禁用" name="disabled"></TabPane>
      </Tabs>
      <div class="table-zone">
        <div v-for="(roleData, roleDataIndex) in data" :key="roleDataIndex">
          <div class="w-header">
            <Icon size="28" type="ios-people" />
            <div class="title">{{ roleData.manageRole }}<span class="underline"></span></div>
            <Icon
              v-if="!hideRoles.includes(roleDataIndex)"
              size="26"
              @click="changeRoleTableStatus(roleDataIndex, 'in')"
              type="md-arrow-dropdown"
              style="cursor: pointer"
            />
            <Icon
              v-else
              size="26"
              @click="changeRoleTableStatus(roleDataIndex, 'out')"
              type="md-arrow-dropright"
              style="cursor: pointer"
            />
          </div>
          <div v-show="!hideRoles.includes(roleDataIndex)">
            <div v-for="(tableData, tableDataIndex) in roleData.sceneData" :key="tableDataIndex" class="sub-header">
              <div style="margin: 6px 0">
                <Icon size="20" type="ios-folder" />
                <span class="title">{{ tableData.scene }}</span>
              </div>
              <div>
                <Table
                  class="hide-select-all"
                  size="small"
                  :columns="mgmtColumns()"
                  :data="tableData.dataList"
                  @on-select-all="onSelectAll"
                  @on-select="onSelect"
                  @on-select-cancel="cancelSelect"
                  @on-selection-change="onSelectionChange"
                  width="100%"
                ></Table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <FlowAuth ref="flowAuthRef" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import { flowMgmt, getPluginList, flowList, flowBatchAuth, flowBatchChangeStatus, flowCopy } from '@/api/server.js'
import FlowAuth from '@/pages/collaboration/flow/flow-auth.vue'
import dayjs from 'dayjs'
export default {
  components: {
    FlowAuth
  },
  data () {
    return {
      expand: true,
      searchParams: {
        procDefId: '',
        procDefName: '',
        plugins: [],
        updatedTimeStart: '',
        UpdatedTimeEnd: '',
        status: 'deployed'
      },
      dateType: 1, // 控制时间显示
      dateTypeList: [
        { label: this.$t('tw_recent_three_month'), value: 1 },
        { label: this.$t('tw_recent_half_year'), value: 2 },
        { label: this.$t('tw_recent_one_year'), value: 3 },
        { label: this.$t('tw_auto'), value: 4 }
      ],
      hideRoles: [], // 在此出现的角色index将被隐藏
      authPluginList: [], // 待授权插件列表
      data: [],
      tableColumn: [
        {
          type: 'selection',
          width: 30,
          align: 'center'
        },
        {
          title: '编排ID',
          key: 'id'
        },
        {
          title: '编排名称',
          key: 'name'
        },
        {
          title: '授权插件',
          key: 'authPlugins',
          render: (h, params) => {
            return (
              params.row.authPlugins &&
              params.row.authPlugins.map(i => {
                return <Tag>{i}</Tag>
              })
            )
          }
        },
        {
          title: '操作对象类型',
          key: 'rootEntity'
        },
        {
          title: '使用角色',
          key: 'rootEntity',
          render: (h, params) => {
            return (
              params.row.userRoles &&
              params.row.userRoles.map(i => {
                return <Tag>{i}</Tag>
              })
            )
          }
        },
        {
          title: '冲突检测',
          key: 'conflictCheck'
        },
        {
          title: '创建人',
          key: 'createdBy'
        },
        {
          title: '更新人',
          key: 'updatedBy'
        },
        {
          title: '更新时间',
          key: 'updatedTime'
        },
        {
          title: '操作',
          key: 'action',
          width: 150,
          align: 'left',
          render: (h, params) => {
            const status = params.row.status
            return (
              <div style="text-align: left">
                {['deployed'].includes(status) && (
                  <Button
                    onClick={() => this.copyAction(params.row)}
                    style="margin-left: 8px"
                    type="primary"
                    size="small"
                  >
                    {this.$t('copy')}
                  </Button>
                )}
                {['draft'].includes(status) && (
                  <Button
                    onClick={() => this.editAction(params.row)}
                    style="margin-left: 8px"
                    type="primary"
                    size="small"
                  >
                    {this.$t('edit')}
                  </Button>
                )}
                {['deployed'].includes(status) && params.row.enableCreated && (
                  <Button
                    onClick={() => this.copyToEditAction(params.row)}
                    style="margin-left: 8px"
                    type="primary"
                    size="small"
                  >
                    {this.$t('复制编辑')}
                  </Button>
                )}
              </div>
            )
          }
        }
      ],
      selectedParams: {
        ids: [],
        // status: [],
        names: []
      }
    }
  },
  mounted () {
    this.handleDateTypeChange(1)
    this.getFlowList()
    this.pluginList()
  },
  methods: {
    onSelectionChange (selection, b) {
      // console.log('onSelectionChange', selection, b)
    },
    onSelectAll (selection, b) {
      // console.log('onSelectAll', selection, b)
      selection.forEach(se => {
        if (!this.selectedParams.ids.includes(se.id)) {
          this.selectedParams.ids.push(se.id)
          this.selectedParams.names.push(se.name)
        }
      })
    },
    onSelect (selection, b) {
      // console.log('onSelect', selection, b)
      this.selectedParams.ids.push(b.id)
      this.selectedParams.names.push(b.name)
    },
    cancelSelect (selection, b) {
      // console.log('cancelSelect', selection, b)
      const findIndex = this.selectedParams.ids.findIndex(id => id === b.id)
      this.selectedParams.ids.splice(findIndex, 1)
      this.selectedParams.names.splice(findIndex, 1)
    },
    async create () {
      this.authTo = 'createFlow'
      this.$refs.flowAuthRef.startAuth([], [])
    },
    // 获取所有插件列表
    async pluginList () {
      let { data, status } = await getPluginList()
      if (status === 'OK') {
        this.authPluginList = data
      }
    },
    // 切换tab修改数据
    changeTab (name) {
      this.searchParams.status = name
      this.selectedParams.ids = []
      this.selectedParams.names = []
      this.hideRoles = []
      this.getFlowList()
    },
    // 重置参数
    handleReset () {
      this.searchParams = {
        procDefId: '',
        procDefName: '',
        plugins: [],
        updatedTimeStart: '',
        UpdatedTimeEnd: '',
        status: 'deployed'
      }
      this.hideRoles = []
      this.dateType = 1
      this.getFlowList()
    },
    // 获取编排列表
    async getFlowList () {
      let { data, status } = await flowList(this.searchParams)
      if (status === 'OK') {
        this.data = data
      }
    },
    // #region 按钮响应
    // 授权
    batchAuth () {
      this.authTo = 'batchAuth'
      this.$refs.flowAuthRef.startAuth([], [])
    },
    async updateAuth (mgmt, use) {
      if (this.authTo === 'batchAuth') {
        this.$Modal.confirm({
          title: this.$t('config_permission'),
          content: `保存权限,将批量覆盖编排 [${this.selectedParams.names}] 的权限.`,
          onOk: async () => {
            const data = {
              procDefIds: this.selectedParams.ids,
              permissionToRole: {
                MGMT: mgmt,
                USE: use
              }
            }
            let { status, message } = await flowBatchAuth(data)
            if (status === 'OK') {
              this.$Notice.success({
                title: 'Success',
                desc: message
              })
              this.getFlowList()
              this.selectedParams.ids = []
              this.selectedParams.names = []
            }
          },
          onCancel: () => {}
        })
      } else if (this.authTo === 'createFlow') {
        const params = {
          id: '',
          name: `编排_${dayjs().format('YYMMDDHHmmss')}`,
          version: '1',
          scene: '',
          authPlugins: [],
          tags: '',
          conflictCheck: false,
          rootEntity: '',
          permissionToRole: {
            MGMT: mgmt,
            USE: use
          }
        }
        const { data, status, message } = await flowMgmt(params)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Success',
            desc: message
          })
          this.$router.push({ path: '/collaboration/workflow-orchestration', query: { flowId: data.id } })
        }
      }
    },
    // 批量改状态
    batchChangeStatus (state) {
      const statusToTip = {
        disabled: {
          title: this.$t('disable'),
          content: `确认批量禁用编排: [${this.selectedParams.names}] 吗?禁用的模版会放入[编排列表-已禁用]中,可以重新启用`
        },
        deleted: {
          title: this.$t('delete'),
          content: `确认批量删除编排: [${this.selectedParams.names}] 吗?删除操作不可撤销.`
        },
        enabled: {
          title: this.$t('enable'),
          content: `确认批量启用编排: [${this.selectedParams.names}] 吗?`
        }
      }

      this.$Modal.confirm({
        title: statusToTip[state].title,
        content: statusToTip[state].content,
        onOk: async () => {
          const data = {
            procDefIds: this.selectedParams.ids,
            status: state
          }
          let { status, message } = await flowBatchChangeStatus(data)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            this.getFlowList()
            this.selectedParams.ids = []
            this.selectedParams.names = []
          }
        },
        onCancel: () => {}
      })
    },
    // 普通编辑直接跳转
    editAction (row) {
      const status = row.status
      console.log(11, status)
      if (status === 'draft') {
        console.log('转至详情')
        this.$router.push({ path: '/collaboration/workflow-orchestration', query: { flowId: row.id } })
      }
      if (status === 'deployed') {
      }
    },
    // deployed且 enableCreated为true，先调用接口成功后再跳转
    async copyToEditAction (row) {
      let { status, data } = await flowCopy(row.id, 'y')
      if (status === 'OK') {
        this.$router.push({ path: '/collaboration/workflow-orchestration', query: { flowId: data } })
      }
    },
    async copyAction (row) {
      let { status, message, data } = await flowCopy(row.id, 'n')
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message + data
        })
        this.getFlowList()
        this.selectedParams.ids = []
        this.selectedParams.names = []
      }
    },

    // #endregion
    // 自定义时间控件转化时间格式值
    handleDateTypeChange (dateType) {
      this.dateType = dateType
      const cur = dayjs().format('YYYY-MM-DD')
      if (dateType === 1) {
        const pre = dayjs().subtract(3, 'month').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.UpdatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 2) {
        const pre = dayjs().subtract(6, 'month').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.UpdatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 3) {
        const pre = dayjs().subtract(1, 'year').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.UpdatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 4) {
        this.searchParams.updatedTimeStart = ''
        this.searchParams.UpdatedTimeEnd = ''
      }
      this.getFlowList()
    },
    handleDateRange (dateArr) {
      this.searchParams.updatedTimeStart = dateArr[0]
      this.searchParams.UpdatedTimeEnd = dateArr[1]
      this.getFlowList()
    },
    // 控制角色下table的显示
    changeRoleTableStatus (index, type) {
      if (type === 'in') {
        this.hideRoles.push(index)
      } else if (type === 'out') {
        const findIndex = this.hideRoles.findIndex(rIndex => rIndex === index)
        this.hideRoles.splice(findIndex, 1)
      }
    },
    mgmtColumns () {
      if (this.searchParams.status !== 'disabled') {
        return this.tableColumn
      } else {
        let customCol = JSON.parse(JSON.stringify(this.tableColumn))
        customCol.pop()
        return customCol
      }
    }
  }
}
</script>

<style lang="scss">
// 屏蔽列表权限功能
th.ivu-table-column-center div.ivu-table-cell {
  display: none;
}
</style>
<style lang="scss" scoped>
.btn-right {
  margin-right: 10px;
}
.btn-img {
  width: 16px;
  vertical-align: middle;
}

.table-zone {
  overflow: auto;
  height: calc(100vh - 300px);
}

.w-header {
  display: flex;
  align-items: center;
  .title {
    font-size: 15px;
    font-weight: 500;
    color: #282e38;
    margin: 0 10px;
    .underline {
      display: block;
      margin-top: -10px;
      margin-left: -6px;
      width: 100%;
      padding: 0 6px;
      height: 10px;
      border-radius: 12px;
      background-color: #c6eafe;
      box-sizing: content-box;
    }
  }
  .sub-title {
    font-size: 15px;
  }
}
.sub-header {
  // display: flex;
  // align-items: center;
  margin-left: 12px;
  .title {
    font-size: 14px;
    font-weight: bold;
    margin-left: 5px;
  }
}
</style>
