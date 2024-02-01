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
        accept=".json"
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
    <div>
      <Input
        v-model="searchParams.procDefId"
        placeholder="ID"
        class="search-item"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Input
        v-model="searchParams.procDefName"
        :placeholder="$t('flow_name')"
        class="search-item"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Select
        v-model="searchParams.plugins"
        filterable
        multiple
        class="search-item"
        placeholder="授权插件"
        :max-tag-count="1"
        @on-change="getFlowList"
      >
        <Option v-for="item in authPluginList" :value="item" :key="item">{{ item }} </Option>
      </Select>
      <Input
        v-model="searchParams.scene"
        placeholder="分组"
        class="search-item"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Input
        v-model="searchParams.createdBy"
        placeholder="创建人"
        class="search-item"
        clearable
        @on-change="getFlowList"
      ></Input>
      <Input
        v-model="searchParams.updatedBy"
        placeholder="更新人"
        class="search-item"
        clearable
        @on-change="getFlowList"
      ></Input>
      <div style="display: inline; width: 100%" class="search-item">
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
      </div>
      <span style="margin-top: 8px; float: right">
        <Button @click="getFlowList" type="primary">{{ $t('search') }}</Button>
        <Button @click="handleReset" style="margin-left: 5px">{{ $t('reset') }}</Button>
      </span>
    </div>
    <div>
      <Tabs :value="searchParams.status" @on-click="changeTab">
        <TabPane :label="$t('deployed')" name="deployed"></TabPane>
        <TabPane :label="$t('draft')" name="draft"></TabPane>
        <TabPane :label="$t('disabled')" name="disabled"></TabPane>
      </Tabs>
      <div class="table-zone">
        <Spin v-if="spinShow" size="large">
          <Icon type="ios-loading" size="36"></Icon>
        </Spin>
        <template v-else>
          <template v-if="data.length > 0">
            <div v-for="(roleData, roleDataIndex) in data" :key="roleDataIndex">
              <Card>
                <div class="w-header" slot="title">
                  <Icon size="28" type="ios-people" />
                  <div class="title">
                    {{ roleData.manageRole }}
                    <span class="underline"></span>
                  </div>
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
                    :columns="tableColumn"
                    :data="roleData.dataList"
                    @on-select-all="onSelectAll"
                    @on-select="onSelect"
                    @on-select-cancel="cancelSelect"
                    @on-selection-change="onSelectionChange"
                    width="100%"
                  ></Table>
                </div>
              </Card>
            </div>
          </template>
          <template v-else>
            <div style="text-align: center; margin-top: 16px">
              {{ $t('noData') }}
            </div>
          </template>
        </template>
      </div>
    </div>
    <FlowAuth ref="flowAuthRef" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import axios from 'axios'
import { getCookie } from '@/pages/util/cookie'
import { flowMgmt, getPluginList, flowList, flowBatchAuth, flowBatchChangeStatus, flowCopy } from '@/api/server.js'
import FlowAuth from '@/pages/components/auth.vue'
import dayjs from 'dayjs'
export default {
  components: {
    FlowAuth
  },
  data () {
    return {
      spinShow: true,
      expand: true,
      searchParams: {
        procDefId: '',
        procDefName: '',
        plugins: [],
        updatedTimeStart: '',
        updatedTimeEnd: '',
        createdBy: '',
        updatedBy: '',
        scene: '', // 分组
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
          title: 'ID',
          width: 80,
          ellipsis: true,
          key: 'id'
        },
        {
          title: this.$t('flow_name'),
          key: 'name',
          render: (h, params) => {
            return (
              <span>
                {params.row.name}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
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
          title: this.$t('instance_type'),
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
          title: this.$t('use_role'),
          key: 'userRoles',
          render: (h, params) => {
            if (params.row.userRoles.length > 0) {
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
          title: this.$t('conflict_test'),
          key: 'conflictCheck',
          width: 90,
          render: (h, params) => {
            const res = params.row.conflictCheck ? '是' : '否'
            return <span>{res}</span>
          }
        },
        {
          title: '分组',
          key: 'scene',
          width: 90,
          render: (h, params) => {
            if (params.row.scene !== '') {
              return <div>{params.row.scene}</div>
            } else {
              return <span>-</span>
            }
          }
        },
        {
          title: this.$t('createdBy'),
          key: 'createdBy',
          width: 90
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          width: 90
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          width: 130
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 120,
          align: 'left',
          fixed: 'right',
          render: (h, params) => {
            const status = params.row.status
            return (
              <div style="text-align: left; cursor: pointer;display: inline-flex;">
                <Tooltip content={this.$t('view')} placement="top">
                  <Button
                    size="small"
                    type="primary"
                    onClick={() => this.viewAction(params.row)}
                    style="margin-right:5px;"
                  >
                    <Icon type="md-eye" size="16"></Icon>
                  </Button>
                </Tooltip>

                {['deployed'].includes(status) && (
                  <Tooltip content={this.$t('copy')} placement="top">
                    <Button
                      size="small"
                      type="info"
                      onClick={() => this.copyAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-copy" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['draft'].includes(status) && (
                  <Tooltip content={this.$t('edit')} placement="top">
                    <Button
                      size="small"
                      type="success"
                      onClick={() => this.editAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="ios-create-outline" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['deployed'].includes(status) && (
                  <Tooltip
                    content={
                      params.row.enableCreated
                        ? this.$t('edit')
                        : '当前编排已有一条[未发布]草稿数据,请直接在[未发布]中编排'
                    }
                    placement="left"
                    max-width="200"
                  >
                    <Button
                      size="small"
                      type="success"
                      disabled={!params.row.enableCreated}
                      onClick={() => this.copyToEditAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="ios-create-outline" size="16"></Icon>
                    </Button>
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
    if (this.$route.query.flowListTab) {
      this.searchParams.status = this.$route.query.flowListTab
    }

    this.setHeaders()
    this.handleDateTypeChange(1)
    this.getFlowList()
    this.pluginList()
  },
  methods: {
    setHeaders () {
      const lang = localStorage.getItem('lang') || 'zh-CN'
      const accessToken = getCookie('accessToken')
      this.headers = {
        Authorization: 'Bearer ' + accessToken,
        'Accept-Language': lang === 'zh-CN' ? 'zh-CN,zh;q=0.9,en;q=0.8' : 'en-US,en;q=0.9,zh;q=0.8'
      }
    },
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
        updatedTimeEnd: '',
        createdBy: '',
        updatedBy: '',
        scene: '', // 分组
        status: 'deployed'
      }
      this.hideRoles = []
      this.dateType = 1
      this.getFlowList()
    },
    // 获取编排列表
    async getFlowList () {
      this.spinShow = true
      let { data, status } = await flowList(this.searchParams)
      this.spinShow = false
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
          name: `${this.$t('workflow_report_aspect')}_${dayjs().format('YYMMDDHHmmss')}`,
          version: 'v1',
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
          this.$router.push({ path: '/collaboration/workflow-mgmt', query: { flowId: data.id, flowListTab: 'draft' } })
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
            this.$nextTick(() => {
              if (state === 'disabled') {
                this.searchParams.status = 'disabled'
              } else if (state === 'enabled') {
                this.searchParams.status = 'deployed'
              }
              this.getFlowList()
              this.selectedParams.ids = []
              this.selectedParams.names = []
            })
          }
        },
        onCancel: () => {}
      })
    },
    // 普通编辑直接跳转
    editAction (row) {
      const status = row.status
      if (status === 'draft') {
        this.$router.push({ path: '/collaboration/workflow-mgmt', query: { flowId: row.id, flowListTab: 'draft' } })
      }
      if (status === 'deployed') {
      }
    },
    // deployed且 enableCreated为true，先调用接口成功后再跳转
    async copyToEditAction (row) {
      let { status, data } = await flowCopy(row.id, 'y')
      if (status === 'OK') {
        this.$router.push({ path: '/collaboration/workflow-mgmt', query: { flowId: data, flowListTab: 'draft' } })
      }
    },
    async copyAction (row) {
      let { status, message, data } = await flowCopy(row.id, 'n')
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message + data
        })
        this.$router.push({ path: '/collaboration/workflow-mgmt', query: { flowId: data, flowListTab: 'draft' } })
      }
    },
    viewAction (row) {
      this.$router.push({
        path: '/collaboration/workflow-mgmt',
        query: { flowId: row.id, editFlow: 'false', flowListTab: this.searchParams.status }
      })
    },

    // #endregion
    // 自定义时间控件转化时间格式值
    handleDateTypeChange (dateType) {
      this.dateType = dateType
      const cur = dayjs().format('YYYY-MM-DD')
      if (dateType === 1) {
        const pre = dayjs().subtract(3, 'month').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.updatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 2) {
        const pre = dayjs().subtract(6, 'month').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.updatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 3) {
        const pre = dayjs().subtract(1, 'year').format('YYYY-MM-DD')
        this.searchParams.updatedTimeStart = pre + ' 00:00:00'
        this.searchParams.updatedTimeEnd = cur + ' 23:59:59'
      } else if (dateType === 4) {
        this.searchParams.updatedTimeStart = ''
        this.searchParams.updatedTimeEnd = ''
      }
      this.getFlowList()
    },
    handleDateRange (dateArr) {
      this.searchParams.updatedTimeStart = dateArr[0]
      this.searchParams.updatedTimeEnd = dateArr[1]
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
        let finalResult = []
        res.data.resultList.forEach(r => {
          finalResult.push(`${r.procDefName}(${r.ProcDefVersion}): ${r.message}`)
        })
        this.$Notice.info({
          duration: 0,
          title: this.$t('import_flow'),
          render: h => {
            return (
              <div>
                {finalResult.length > 0 &&
                  finalResult.map(i => {
                    return <div>{i}</div>
                  })}
              </div>
            )
          }
        })
        if (res.data.resultList.length === 1 && res.data.resultList[0].code === 0) {
          this.$router.push({
            path: '/collaboration/workflow-mgmt',
            query: { flowId: res.data.resultList[0].procDefId, flowListTab: 'draft' }
          })
        }
        this.getFlowList()
      }
    },
    async exportFlow () {
      this.setHeaders()
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
            const fileNameArr = response.headers['content-disposition'].split('filename=')
            let fileName = `export_${dayjs().format('YYMMDDHHmmss')}.json`
            if (fileNameArr.length === 2) {
              fileName = fileNameArr[1]
            }
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
.search-item {
  width: 200px;
  margin-right: 6px;
  margin: 8px 6px 8px 0;
}
.btn-right {
  margin-right: 10px;
}
.btn-img {
  width: 16px;
  vertical-align: middle;
}

.table-zone {
  overflow: auto;
  height: calc(100vh - 270px);
}
.w-header {
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
</style>
