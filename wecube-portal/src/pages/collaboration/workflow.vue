<template>
  <div class="workflow-design">
    <div>
      <Button type="success" class="btn-right" @click="create">
        <Icon type="md-add" :size="18" />
        {{ $t('full_word_add') }}
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
        :disabled="!(['deployed'].includes(searchParams.status) && selectedParams.length > 0)"
        @click="exportFlow"
      >
        <img src="../../assets/icon/export.png" class="btn-img" alt="" />
        {{ $t('export_flow') }}
      </Button>
      <Button
        type="warning"
        class="btn-right"
        @click="batchAuth"
        :disabled="!(['deployed', 'draft'].includes(searchParams.status) && selectedParams.length > 0)"
      >
        <Icon type="ios-person-outline" size="16"></Icon>
        {{ $t('config_permission') }}
      </Button>
      <Button
        type="error"
        class="btn-right"
        @click="batchChangeStatus('deleted')"
        v-if="['draft'].includes(searchParams.status)"
        :disabled="!(['draft'].includes(searchParams.status) && selectedParams.length > 0)"
      >
        <Icon type="ios-trash-outline" size="16"></Icon>
        {{ $t('delete') }}
      </Button>
      <Button
        type="error"
        v-if="['deployed'].includes(searchParams.status) && searchParams.subProc !== 'sub'"
        :disabled="!(['deployed'].includes(searchParams.status) && selectedParams.length > 0)"
        @click="batchChangeStatus('disabled')"
      >
        <img src="../../assets/icon/disable.png" class="btn-img" />
        {{ $t('disable') }}
      </Button>
      <Button
        type="success"
        @click="batchChangeStatus('enabled')"
        v-if="['disabled'].includes(searchParams.status)"
        :disabled="!(['disabled'].includes(searchParams.status) && selectedParams.length > 0)"
      >
        <img src="../../assets/icon/enable.png" class="btn-img" alt="" />
        {{ $t('enable') }}
      </Button>
    </div>
    <div class="search">
      <BaseSearch
        ref="search"
        :options="searchOptions"
        v-model="searchParams"
        @search="getFlowList"
        style="margin-top: 10px"
      >
        <template slot="prepend">
          <RadioGroup
            v-model="searchParams.subProc"
            type="button"
            button-style="solid"
            @on-change="changeFlow"
            style="margin-right: 5px"
          >
            <Radio label="main">{{ $t('main_workflow') }}</Radio>
            <Radio label="sub">{{ $t('child_workflow') }}</Radio>
          </RadioGroup>
        </template>
      </BaseSearch>
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
                    {{ roleData.manageRoleDisplay }}
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
                    ref="table"
                    size="small"
                    :columns="tableColumn"
                    :data="roleData.dataList"
                    @on-select-all="selection => onSelectAll(selection, roleDataIndex)"
                    @on-select-all-cancel="selection => onSelectAllCancel(selection, roleDataIndex)"
                    @on-select="(selection, row) => onSelect(selection, row, roleDataIndex)"
                    @on-select-cancel="(selection, row) => cancelSelect(selection, row, roleDataIndex)"
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
    <FlowAuth ref="flowAuthRef" :useRolesRequired="true" @sendAuth="updateAuth"></FlowAuth>
    <!--关联主编排弹框-->
    <Modal
      v-model="mainFlowVisible"
      :title="$t('parent_flowTitle')"
      :width="800"
      footer-hide
      @on-cancel="mainFlowVisible = false"
    >
      <Table :border="false" size="small" :loading="mainFlowLoading" :columns="mainFlowColumn" :data="mainFlowData">
      </Table>
      <div style="text-align: right; margin-top: 10px">
        <Page
          :total="pagination.total"
          @on-change="changPage"
          show-sizer
          :current="pagination.currentPage"
          :page-size="pagination.pageSize"
          @on-page-size-change="changePageSize"
          show-total
        />
      </div>
    </Modal>
  </div>
</template>

<script>
import { getCookie } from '@/pages/util/cookie'
import axios from 'axios'
import {
  flowMgmt,
  getPluginList,
  flowList,
  flowBatchAuth,
  flowBatchChangeStatus,
  flowCopy,
  transferToMe,
  getParentFlowList
} from '@/api/server.js'
import FlowAuth from '@/pages/components/auth.vue'
import dayjs from 'dayjs'
export default {
  components: {
    FlowAuth
  },
  data() {
    return {
      spinShow: true,
      expand: true,
      searchParams: {
        procDefId: '',
        procDefName: '',
        plugins: [],
        createdTime: [dayjs().subtract(3, 'month')
          .format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
        createdTimeStart: '',
        createdTimeEnd: '',
        createdBy: '',
        updatedBy: '',
        scene: '', // 分组
        status: 'deployed',
        subProc: 'main'
      },
      searchOptions: [
        {
          key: 'createdTime',
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
        {
          key: 'procDefName',
          placeholder: this.$t('flow_name'),
          component: 'input'
        },
        {
          key: 'procDefId',
          placeholder: this.$t('workflow_id'),
          component: 'input'
        },
        {
          key: 'plugins',
          placeholder: this.$t('authPlugin'),
          component: 'select',
          multiple: true,
          list: []
        },
        {
          key: 'scene',
          placeholder: this.$t('group'),
          component: 'input'
        },
        {
          key: 'createdBy',
          placeholder: this.$t('createdBy'),
          component: 'input'
        },
        {
          key: 'updatedBy',
          placeholder: this.$t('updatedBy'),
          component: 'input'
        }
      ],
      dateType: 1, // 控制时间显示
      dateTypeList: [
        {
          label: this.$t('be_recent_three_month'),
          value: 1
        },
        {
          label: this.$t('be_recent_half_year'),
          value: 2
        },
        {
          label: this.$t('be_recent_one_year'),
          value: 3
        },
        {
          label: this.$t('be_auto'),
          value: 4
        }
      ],
      hideRoles: [], // 在此出现的角色index将被隐藏
      authPluginList: [], // 待授权插件列表
      data: [],
      tableColumn: [
        {
          type: 'selection',
          width: 40,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'name',
          minWidth: 100,
          render: (h, params) => (
            <div>
              <span>
                <Icon
                  type="ios-funnel-outline"
                  size="12"
                  style="cursor: pointer;margin-right:4px"
                  onClick={() => this.copyNameToSearch(params.row.name)}
                />
              </span>
              <span>
                {params.row.name}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
              </span>
            </div>
          )
        },
        {
          title: 'ID',
          minWidth: 60,
          ellipsis: true,
          key: 'id',
          render: (h, params) => (
            <div>
              <Tooltip content={params.row.id} placement="top">
                <span>{params.row.id.slice(0, 7)}...</span>
              </Tooltip>
            </div>
          )
        },
        {
          title: this.$t('authPlugin'),
          key: 'authPlugins',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.authPlugins.length > 0) {
              return params.row.authPlugins && params.row.authPlugins.map(i => <Tag>{i}</Tag>)
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('instance_type'),
          key: 'rootEntity',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.rootEntity !== '') {
              return <div>{params.row.rootEntity}</div>
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('use_role'),
          key: 'userRoles',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.userRolesDisplay.length > 0) {
              return <ScrollTag list={params.row.userRolesDisplay} />
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('conflict_test'),
          key: 'conflictCheck',
          minWidth: 60,
          render: (h, params) => {
            const res = params.row.conflictCheck ? this.$t('yes') : this.$t('no')
            return <span>{res}</span>
          }
        },
        {
          title: this.$t('group'),
          key: 'scene',
          minWidth: 60,
          render: (h, params) => {
            if (params.row.scene !== '') {
              return <div>{params.row.scene}</div>
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('createdBy'),
          key: 'createdBy',
          minWidth: 60
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime',
          minWidth: 60
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 60
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 60
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 170,
          align: 'center',
          fixed: 'right',
          render: (h, params) => {
            const status = params.row.status
            return (
              <div style="text-align: left; cursor: pointer;display: inline-flex;">
                <Tooltip content={this.$t('view')} placement="top">
                  <Button
                    size="small"
                    type="info"
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
                      type="success"
                      onClick={() => this.copyAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-copy" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['deployed'].includes(status) && (
                  <Tooltip
                    content={params.row.enableCreated ? this.$t('edit') : this.$t('hasDraftData')}
                    placement="left"
                    max-width="200"
                  >
                    <Button
                      size="small"
                      type="primary"
                      disabled={!params.row.enableCreated}
                      onClick={() => this.copyToEditAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-create" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['deployed'].includes(status) && (
                  <Tooltip content={this.$t('disable')} placement="left" max-width="200">
                    <Button
                      size="small"
                      type="error"
                      onClick={() => this.disabledSingleFlow(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-lock" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['disabled'].includes(status) && (
                  <Tooltip content={this.$t('enable')} placement="left" max-width="200">
                    <Button
                      size="small"
                      type="success"
                      onClick={() => this.enabledSingleFlow(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-unlock" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {['draft'].includes(status) && this.username === params.row.updatedBy && (
                  <Tooltip content={this.$t('edit')} placement="top">
                    <Button
                      size="small"
                      type="primary"
                      onClick={() => this.editAction(params.row)}
                      style="margin-right:5px;"
                    >
                      <Icon type="md-create" size="16"></Icon>
                    </Button>
                  </Tooltip>
                )}
                {
                  /* 转给我 */ ['draft'].includes(status) && this.username !== params.row.updatedBy && (
                    <Tooltip content={this.$t('take_over')} placement="left" max-width="200">
                      <Button
                        type="success"
                        size="small"
                        onClick={() => {
                          this.handleTransfer(params.row)
                        }}
                        style="margin-right:5px;"
                      >
                        <Icon type="ios-hand" size="16"></Icon>
                      </Button>
                    </Tooltip>
                  )
                }
              </div>
            )
          }
        }
      ],
      selectedParams: [], // id,name,rowDataIndex
      headers: {},
      username: window.localStorage.getItem('username'),
      mainFlowVisible: false,
      viewRow: {},
      mainFlowColumn: [
        {
          title: this.$t('flow_name'),
          key: 'name',
          minWidth: 250,
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.viewParentFlowGraph(params.row)
              }}
            >
              {params.row.name}
              <Tag style="margin-left:2px">{params.row.version}</Tag>
            </span>
          )
        },
        {
          title: this.$t('enum_status'),
          key: 'status',
          minWidth: 90,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('deployed'),
                value: 'deployed',
                color: '#19be6b'
              },
              {
                label: this.$t('draft'),
                value: 'draft',
                color: '#c5c8ce'
              },
              {
                label: this.$t('disabled'),
                value: 'disabled',
                color: '#ed4014'
              }
            ]
            const item = list.find(i => i.value === params.row.status)
            return item && <Tag color={item.color}>{item.label}</Tag>
          }
        }
      ],
      mainFlowData: [],
      mainFlowLoading: false,
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 10
      }
    }
  },
  watch: {
    'searchParams.subProc': {
      handler(val) {
        if (val === 'sub') {
          // 添加主编排列
          this.tableColumn.splice(3, 0, {
            title: this.$t('main_workflow'),
            minWidth: 60,
            ellipsis: true,
            key: 'mainFlow',
            render: (h, params) => (
              <Button
                type="info"
                size="small"
                onClick={() => {
                  this.viewMainFlow(params.row)
                }}
              >
                {this.$t('view')}
              </Button>
            )
          })
        }
        else if (val === 'main') {
          this.tableColumn = this.tableColumn.filter(i => i.key !== 'mainFlow')
        }
      },
      immediate: true
    },
    'searchParams.status': {
      handler(val) {
        if (val) {
          this.selectedParams = []
          this.hideRoles = []
        }
      },
      immediate: true
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (from.path === '/collaboration/workflow-mgmt') {
        // 读取列表搜索参数
        const storage = window.sessionStorage.getItem('search_workflow') || ''
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
    window.sessionStorage.setItem('search_workflow', JSON.stringify(storage))
  },
  methods: {
    initData() {
      if (this.$route.query.flowListTab) {
        this.searchParams.status = this.$route.query.flowListTab
      }
      if (this.$route.query.subProc) {
        this.searchParams.subProc = this.$route.query.subProc
      }
      this.setHeaders()
      this.getFlowList()
      this.pluginList()
    },
    setHeaders() {
      const lang = localStorage.getItem('lang') || 'zh-CN'
      const accessToken = getCookie('accessToken')
      this.headers = {
        Authorization: 'Bearer ' + accessToken,
        'Accept-Language': lang === 'zh-CN' ? 'zh-CN,zh;q=0.9,en;q=0.8' : 'en-US,en;q=0.9,zh;q=0.8'
      }
    },
    onSelectAll(selection, roleDataIndex) {
      selection.forEach(se => {
        const findIndex = this.selectedParams.findIndex(
          param => param.id === se.id && param.roleDataIndex === roleDataIndex
        )
        if (findIndex === -1) {
          this.selectedParams.push({
            id: se.id,
            name: se.name,
            roleDataIndex,
            mgmtRole: se.mgmtRoles || [],
            useRole: se.userRoles || []
          })
        }
      })
    },
    onSelectAllCancel(selection, roleDataIndex) {
      this.selectedParams = this.selectedParams.filter(param => param.roleDataIndex !== roleDataIndex)
    },
    onSelect(selection, row, roleDataIndex) {
      // if (this.searchParams.subProc === 'sub' && this.searchParams.status === 'deployed') {
      //   this.$nextTick(() => {
      //     // 实现单选效果，目前没找到更好的方法。。。
      //     this.$refs.table.forEach(tableEl => {
      //       for(let index in tableEl.objData) {
      //         if (row.id === tableEl.objData[index].id) {
      //           tableEl.objData[index]._isChecked = true
      //         } else {
      //           tableEl.objData[index]._isChecked = false
      //         }
      //       }
      //     })
      //   })
      //   this.selectedParams = []
      // }
      this.selectedParams.push({
        id: row.id,
        name: row.name,
        roleDataIndex,
        mgmtRole: row.mgmtRoles || [],
        useRole: row.userRoles || []
      })
    },
    cancelSelect(selection, row, roleDataIndex) {
      const findIndex = this.selectedParams.findIndex(
        param => param.id === row.id && param.roleDataIndex === roleDataIndex
      )
      this.selectedParams.splice(findIndex, 1)
    },
    async create() {
      this.authTo = 'createFlow'
      this.$refs.flowAuthRef.startAuth([], [])
    },
    // 获取所有插件列表
    async pluginList() {
      const { data, status } = await getPluginList()
      if (status === 'OK') {
        this.authPluginList = data
        this.searchOptions.forEach(i => {
          if (i.key === 'plugins') {
            i.list = this.authPluginList.map(i => ({
              label: i,
              value: i
            }))
          }
        })
      }
    },
    // 切换tab修改数据
    changeTab(name) {
      this.searchParams.status = name
      this.getFlowList()
    },
    // 切换主编排/子编排
    changeFlow() {
      this.selectedParams = []
      this.$refs.search.handleReset()
    },
    // 获取编排列表
    async getFlowList() {
      this.spinShow = true
      const params = JSON.parse(JSON.stringify(this.searchParams))
      params.createdTimeStart = params.createdTime[0] ? params.createdTime[0] + ' 00:00:00' : ''
      params.createdTimeEnd = params.createdTime[1] ? params.createdTime[1] + ' 23:59:59' : ''
      delete params.createdTime
      const { data, status } = await flowList(params)
      this.spinShow = false
      if (status === 'OK') {
        this.data = data
      }
    },
    // #region 按钮响应
    // 授权
    batchAuth() {
      this.authTo = 'batchAuth'
      if (this.selectedParams.length === 1) {
        this.$refs.flowAuthRef.startAuth(this.selectedParams[0].mgmtRole, this.selectedParams[0].useRole)
      }
      else {
        this.$refs.flowAuthRef.startAuth([], [])
      }
    },
    async updateAuth(mgmt, use) {
      if (this.authTo === 'batchAuth') {
        this.$Modal.confirm({
          title: this.$t('config_permission'),
          content: `${this.$t('authSaveTip1')}[${this.selectedParams.map(p => p.name)}] ${this.$t('authSaveTip2')}`,
          onOk: async () => {
            const data = {
              procDefIds: this.selectedParams.map(p => p.id),
              permissionToRole: {
                MGMT: mgmt,
                USE: use
              }
            }
            const { status, message } = await flowBatchAuth(data)
            if (status === 'OK') {
              this.$Notice.success({
                title: 'Success',
                desc: message
              })
              this.getFlowList()
              this.selectedParams = []
            }
          },
          onCancel: () => {}
        })
      }
      else if (this.authTo === 'createFlow') {
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
          this.$router.push({
            path: '/collaboration/workflow-mgmt',
            query: {
              flowId: data.id,
              flowListTab: 'draft',
              isAdd: 'true'
            }
          })
        }
      }
    },
    // 批量改状态
    batchChangeStatus(state) {
      const statusToTip = {
        disabled: {
          title: this.$t('disable'),
          content: `${this.$t('confirmBatchDisable')}[${this.selectedParams.map(p => p.name)}]?${this.$t(
            'confirmBatchDisableWarn'
          )}`
        },
        deleted: {
          title: this.$t('delete'),
          content: `${this.$t('confirmBatchDelete')}[${this.selectedParams.map(p => p.name)}]?${this.$t(
            'irreversible'
          )}`
        },
        enabled: {
          title: this.$t('enable'),
          content: `${this.$t('confirmBatchEnable')}[${this.selectedParams.map(p => p.name)}]?`
        }
      }

      this.$Modal.confirm({
        title: statusToTip[state].title,
        content: statusToTip[state].content,
        onOk: async () => {
          const data = {
            procDefIds: this.selectedParams.map(p => p.id),
            status: state
          }
          const { status, message } = await flowBatchChangeStatus(data)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            this.$nextTick(() => {
              if (state === 'disabled') {
                this.searchParams.status = 'disabled'
              }
              else if (state === 'enabled') {
                this.searchParams.status = 'deployed'
              }
              this.getFlowList()
              this.selectedParams = []
            })
          }
        },
        onCancel: () => {}
      })
    },
    // 普通编辑直接跳转
    editAction(row) {
      const status = row.status
      if (status === 'draft') {
        this.$router.push({
          path: '/collaboration/workflow-mgmt',
          query: {
            flowId: row.id,
            flowListTab: 'draft'
          }
        })
      }
    },
    // 选择编排名称后过滤
    copyNameToSearch(procDefName) {
      this.searchParams.procDefName = procDefName
      this.getFlowList()
    },
    // deployed且 enableCreated为true，先调用接口成功后再跳转
    async copyToEditAction(row) {
      const { status, data } = await flowCopy(row.id, 'y')
      if (status === 'OK') {
        this.$router.push({
          path: '/collaboration/workflow-mgmt',
          query: {
            flowId: data,
            flowListTab: 'draft'
          }
        })
      }
    },
    // 转给我
    async handleTransfer(row) {
      this.$Modal.confirm({
        title: this.$t('confirm') + this.$t('take_over'),
        'z-index': 1000000,
        loading: true,
        onOk: async () => {
          this.$Modal.remove()
          const params = {
            procDefId: row.id,
            latestUpdateTime: String(new Date(row.updatedTime).getTime()) || ''
          }
          const { status } = await transferToMe(params)
          if (status === 'OK') {
            this.$Notice.success({
              title: this.$t('successful'),
              desc: this.$t('successful')
            })
            this.getFlowList()
            if (row.status === 'draft') {
              this.$router.push({
                path: '/collaboration/workflow-mgmt',
                query: {
                  flowId: row.id,
                  flowListTab: 'draft'
                }
              })
            }
          }
        },
        onCancel: () => {}
      })
    },
    async copyAction(row) {
      const { status, message, data } = await flowCopy(row.id, 'n')
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message + data
        })
        this.$router.push({
          path: '/collaboration/workflow-mgmt',
          query: {
            flowId: data,
            flowListTab: 'draft',
            isAdd: 'true'
          }
        })
      }
    },
    viewAction(row) {
      this.$router.push({
        path: '/collaboration/workflow-mgmt',
        query: {
          flowId: row.id,
          editFlow: 'false',
          flowListTab: this.searchParams.status
        }
      })
    },
    // 控制角色下table的显示
    changeRoleTableStatus(index, type) {
      if (type === 'in') {
        this.hideRoles.push(index)
      }
      else if (type === 'out') {
        const findIndex = this.hideRoles.findIndex(rIndex => rIndex === index)
        this.hideRoles.splice(findIndex, 1)
      }
    },
    handleUpload(file) {
      if (!file.name.endsWith('.json')) {
        this.$Notice.warning({
          title: 'Warning',
          desc: 'Must be a json file'
        })
        return false
      }
      return true
    },
    uploadFailed(val, response) {
      this.$Notice.error({
        title: 'Error',
        desc: response.statusMessage
      })
    },
    async uploadSucess(res) {
      if (res.status === 'OK') {
        const finalResult = []
        res.data.resultList.forEach(r => {
          finalResult.push(`${r.procDefName}(${r.ProcDefVersion}): ${r.message}`)
        })
        this.$Notice.info({
          duration: 0,
          title: this.$t('import_flow'),
          render: () => <div>{finalResult.length > 0 && finalResult.map(i => <div>{i}</div>)}</div>
        })
        if (res.data.resultList.length === 1 && res.data.resultList[0].code === 0) {
          this.$router.push({
            path: '/collaboration/workflow-mgmt',
            query: {
              flowId: res.data.resultList[0].procDefId,
              flowListTab: 'draft'
            }
          })
        }
        this.getFlowList()
      }
    },
    async exportFlow() {
      this.setHeaders()
      axios({
        method: 'post',
        url: 'platform/v1/process/definitions/export',
        headers: this.headers,
        data: {
          procDefIds: this.selectedParams.map(p => p.id)
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
            const blob = new Blob([response.data])
            if ('msSaveOrOpenBlob' in navigator) {
              window.navigator.msSaveOrOpenBlob(blob, fileName)
            }
            else {
              if ('download' in document.createElement('a')) {
                // 非IE下载
                const elink = document.createElement('a')
                elink.download = fileName
                elink.style.display = 'none'
                elink.href = URL.createObjectURL(blob)
                document.body.appendChild(elink)
                elink.click()
                URL.revokeObjectURL(elink.href) // 释放URL 对象
                document.body.removeChild(elink)
              }
              else {
                // IE10+下载
                navigator.msSaveOrOpenBlob(blob, fileName)
              }
            }
          }
        })
        .catch(() => {
          this.$Message.warning('Error')
        })
    },
    // 查看主编排详情
    viewParentFlowGraph(row) {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/collaboration/workflow-mgmt?flowId=${row.id}&editFlow=false&flowListTab=deployed`
      window.open(path, '_blank')
    },
    // 查看主编排
    viewMainFlow(row) {
      this.mainFlowVisible = true
      this.viewRow = row
      this.getMainFlowList()
    },
    changPage(val) {
      this.pagination.currentPage = val
      this.getMainFlowList()
    },
    changePageSize(val) {
      this.pagination.currentPage = 1
      this.pagination.pageSize = val
      this.getMainFlowList()
    },
    async getMainFlowList() {
      const params = {
        startIndex: (this.pagination.currentPage - 1) * this.pagination.pageSize,
        pageSize: this.pagination.pageSize
      }
      this.mainFlowLoading = true
      const { status, data } = await getParentFlowList(this.viewRow.id, params)
      this.mainFlowLoading = false
      if (status === 'OK') {
        this.pagination.total = data.page.totalRows
        this.mainFlowData = data.content
      }
    },
    // 禁用单个编排
    async disabledSingleFlow(row) {
      let total = 0
      let nameStr = ''
      if (row.subProc === true) {
        const { status, data } = await getParentFlowList(row.id, {
          startIndex: 0,
          pageSize: 20
        })
        if (status === 'OK') {
          total = data.page.totalRows || 0
          const arr = data.content && data.content.map(i => i.name)
          nameStr = arr.join('，')
        }
      }
      this.$Modal.confirm({
        title: this.$t('disable'),
        'z-index': 1000000,
        width: 400,
        loading: true,
        render: () => {
          if (!total) {
            return <span>{`${this.$t('fe_confirmDisabledFlow')}${this.$t('confirmBatchDisableWarn')}`}</span>
          }
          return <span>{`禁用当前子编排会影响【${nameStr}】等${total}个主编排，确认禁用吗？`}</span>
        },
        onOk: async () => {
          this.$Modal.remove()
          const data = {
            procDefIds: [row.id],
            status: 'disabled'
          }
          const { status, message } = await flowBatchChangeStatus(data)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            this.$nextTick(() => {
              this.searchParams.status = 'disabled'
              this.getFlowList()
            })
          }
        },
        onCancel: () => {}
      })
    },
    // 启用单个编排
    enabledSingleFlow(row) {
      this.$Modal.confirm({
        title: this.$t('enable'),
        content: this.$t('fe_confirmEnableFlow'),
        onOk: async () => {
          const data = {
            procDefIds: [row.id],
            status: 'enabled'
          }
          const { status, message } = await flowBatchChangeStatus(data)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            this.$nextTick(() => {
              this.searchParams.status = 'deployed'
              this.getFlowList()
            })
          }
        },
        onCancel: () => {}
      })
    }
  }
}
</script>
<style lang="scss">
// 屏蔽列表权限功能
th.ivu-table-column-center div.ivu-table-cell-with-selection {
  padding: 0 4px !important;
}
.ivu-table-cell {
  padding: 0 4px !important;
}
.workflow-design .ivu-radio-wrapper-checked {
  background-color: #2d8cf0 !important;
  color: #fff !important;
}
.workflow-design .ivu-tag {
  display: inline-block;
  line-height: 16px;
  height: auto;
  padding: 5px 6px;
}
</style>
<style lang="scss" scoped>
.search {
  display: flex;
  align-items: flex-start;
  &-button {
    width: fit-content;
    margin-top: 8px;
  }
  &-form {
    flex: 1;
  }
}
.search-item {
  width: 195px;
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
