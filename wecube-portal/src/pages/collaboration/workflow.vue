<template>
  <div class="root">
    <div>
      <Button type="success" class="btn-right" @click="create">
        <Icon type="ios-add-circle-outline" size="16"></Icon>
        {{ $t('create') }}
      </Button>
      <Upload
        action="platform/v1/process/definitions/import"
        :before-upload="handleUpload"
        :show-upload-list="false"
        with-credentials
        :headers="headers"
        :on-success="uploadSucess"
        :on-error="uploadFailed"
        style="display: inline-block"
      >
        <Button type="primary" class="btn-right">
          <img src="../../assets/icon/import.png" class="btn-img" alt="" />
          {{ $t('import_flow') }}
        </Button>
      </Upload>
      <Button
        type="info"
        class="btn-right"
        :disabled="!(['deployed'].includes(searchParams.status) && selectedParams.ids.length > 0)"
        @click="exportFlow"
      >
        <img src="../../assets/icon/export.png" class="btn-img" alt="" />
        {{ $t('export_flow') }}
      </Button>
      <Button
        type="warning"
        class="btn-right"
        @click="batchAuth"
        :disabled="!(['deployed', 'draft'].includes(searchParams.status) && selectedParams.ids.length > 0)"
      >
        <Icon type="ios-person-outline" size="16"></Icon>
        {{ $t('config_permission') }}
      </Button>
      <Button
        type="error"
        class="btn-right"
        @click="batchChangeStatus('deleted')"
        v-if="['draft'].includes(searchParams.status)"
        :disabled="!(['draft'].includes(searchParams.status) && selectedParams.ids.length > 0)"
      >
        <Icon type="ios-trash-outline" size="16"></Icon>
        {{ $t('delete') }}
      </Button>
      <Button
        type="error"
        v-if="['deployed'].includes(searchParams.status)"
        :disabled="!(['deployed'].includes(searchParams.status) && selectedParams.ids.length > 0)"
        @click="batchChangeStatus('disabled')"
      >
        <img src="../../assets/icon/disable.png" class="btn-img" />
        {{ $t('disable') }}
      </Button>
      <Button
        type="success"
        @click="batchChangeStatus('enabled')"
        v-if="['disabled'].includes(searchParams.status)"
        :disabled="!(['disabled'].includes(searchParams.status) && selectedParams.ids.length > 0)"
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
            <Table
              class="hide-select-all"
              size="small"
              :columns="mgmtColumns()"
              :data="roleData.dataList"
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
    <FlowAuth ref="flowAuthRef" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import axios from 'axios'
import { getCookie } from '@/pages/util/cookie'
import { flowMgmt, getPluginList, flowList, flowBatchAuth, flowBatchChangeStatus, flowCopy } from '@/api/server.js'
import FlowAuth from '@/pages/collaboration/flow/flow-auth.vue'
import dayjs from 'dayjs'
import eye from '@/assets/icon/eye.png'
import copy from '@/assets/icon/copy.png'
import edit from '@/assets/icon/edit-black.png'
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
          key: 'name',
          render: (h, params) => {
            return (
              <span>
                {params.row.name}({params.row.version})
              </span>
            )
          }
        },
        {
          title: '授权插件',
          key: 'authPlugins',
          render: (h, params) => {
            if (params.row.authPlugins.length > 0) {
              return (
                params.row.authPlugins &&
                params.row.authPlugins.map(i => {
                  return <Tag>{i}</Tag>
                })
              )
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: '操作对象类型',
          key: 'rootEntity',
          render: (h, params) => {
            if (params.row.rootEntity !== '') {
              return <div>{params.row.rootEntity}</div>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: '使用角色',
          key: 'rootEntity',
          render: (h, params) => {
            if (params.row.authPlugins.length > 0) {
              return (
                params.row.userRoles &&
                params.row.userRoles.map(i => {
                  return <Tag>{i}</Tag>
                })
              )
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: '冲突检测',
          key: 'conflictCheck',
          width: 90,
          render: (h, params) => {
            const res = params.row.conflictCheck ? '是' : '否'
            return <span>{res}</span>
          }
        },
        {
          title: '创建人',
          key: 'createdBy',
          width: 90
        },
        {
          title: '更新人',
          key: 'updatedBy',
          width: 90
        },
        {
          title: '更新时间',
          key: 'updatedTime',
          width: 130
        },
        {
          title: '操作',
          key: 'action',
          width: 100,
          align: 'left',
          render: (h, params) => {
            const status = params.row.status
            return (
              <div style="text-align: left; cursor: pointer;display: inline-flex;">
                <Tooltip placement="top" content={this.$t('view')}>
                  <img src={eye} style="width:18px;margin:0 4px;"></img>
                </Tooltip>
                {['deployed'].includes(status) && (
                  <Tooltip placement="top" content={this.$t('copy')}>
                    <img src={copy} style="width:18px;margin:0 4px;" onClick={() => this.copyAction(params.row)}>
                      复制
                    </img>
                  </Tooltip>
                )}
                {['draft'].includes(status) && (
                  <Tooltip placement="top" content={this.$t('edit')}>
                    <img src={edit} style="width:18px;margin:0 4px;" onClick={() => this.editAction(params.row)}>
                      编辑
                    </img>
                  </Tooltip>
                )}
                {['deployed'].includes(status) && params.row.enableCreated && (
                  <Tooltip placement="top" content={this.$t('edit')}>
                    <img src={edit} style="width:18px;margin:0 4px;" onClick={() => this.copyToEditAction(params.row)}>
                      复制
                    </img>
                  </Tooltip>
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
      },
      headers: {}
    }
  },
  mounted () {
    const accessToken = getCookie('accessToken')
    this.headers = {
      Authorization: 'Bearer ' + accessToken
    }
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
    },
    handleUpload (file) {
      if (!file.name.endsWith('.json')) {
        this.$Notice.warning({
          title: 'Warning',
          desc: 'Must be a json file'
        })
        return false
      }
      return true
    },
    uploadFailed (val, response) {
      this.$Notice.error({
        title: 'Error',
        desc: response.statusMessage
      })
    },
    async uploadSucess (res) {
      if (res.status === 'OK') {
        let finalResult = {
          0: [],
          1: [],
          2: []
        }
        res.data.resultList.forEach(r => {
          finalResult[r.code].push(`${r.procDefName}${r.ProcDefVersion}`)
        })
        this.$Notice.success({
          duration: 0,
          title: this.$t('import_flow'),
          desc: 'Successful',
          render: h => {
            const code0 = finalResult[0]
            const code1 = finalResult[1]
            const code2 = finalResult[2]
            return (
              <div>
                {code0.length > 0 && (
                  <div>
                    <span>0:</span>
                    <span>{JSON.stringify(code0)}</span>
                  </div>
                )}
                {code1.length > 0 && (
                  <div>
                    <span>1:</span>
                    <span>{JSON.stringify(code1)}</span>
                  </div>
                )}
                {code2.length > 0 && (
                  <div>
                    <span>2:</span>
                    <span>{JSON.stringify(code2)}</span>
                  </div>
                )}
              </div>
            )
          }
        })
        this.getFlowList()
      }
    },
    async exportFlow () {
      const accessToken = getCookie('accessToken')
      this.headers = {
        Authorization: 'Bearer ' + accessToken
      }
      axios({
        method: 'post',
        url: `platform/v1/process/definitions/export`,
        headers: this.headers,
        data: {
          procDefIds: this.selectedParams.ids
        },
        responseType: 'blob'
      })
        .then(response => {
          if (response.status < 400) {
            let fileName = `export_${dayjs().format('YYMMDDHHmmss')}.json`
            let blob = new Blob([response.data])
            if ('msSaveOrOpenBlob' in navigator) {
              window.navigator.msSaveOrOpenBlob(blob, fileName)
            } else {
              if ('download' in document.createElement('a')) {
                // 非IE下载
                let elink = document.createElement('a')
                elink.download = fileName
                elink.style.display = 'none'
                elink.href = URL.createObjectURL(blob)
                document.body.appendChild(elink)
                elink.click()
                URL.revokeObjectURL(elink.href) // 释放URL 对象
                document.body.removeChild(elink)
              } else {
                // IE10+下载
                navigator.msSaveOrOpenBlob(blob, fileName)
              }
            }
          }
        })
        .catch(() => {
          this.$Message.warning('Error')
        })
    }
  }
}
</script>

<style lang="scss">
// 屏蔽列表权限功能
th.ivu-table-column-center div.ivu-table-cell {
  display: none;
}
.ivu-table-cell {
  padding: 0 4px !important;
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
  height: calc(100vh - 250px);
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
