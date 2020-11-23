<template>
  <Row style="padding:20px">
    <Spin size="large" fix v-if="isLoading">
      <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
      <div>{{ $t('loading') }}</div>
    </Spin>
    <Col span="7">
      <Row>
        <Card dis-hover>
          <p slot="title">{{ $t('upload_plugin_pkg_title') }}</p>
          <div style="text-align: center">
            <Button type="info" icon="md-list-box" ghost :loading="loadingPlugin" @click="showUploadModalHandler">{{
              $t('origin_plugins')
            }}</Button>
            <Button type="info" style="margin-left:20px" ghost icon="ios-cloud-upload-outline" @click="getHeaders">{{
              $t('upload_plugin_btn')
            }}</Button>
            <div v-show="showUpload">
              <Upload
                ref="uploadButton"
                show-upload-list
                accept=".zip"
                name="zip-file"
                :on-success="onSuccess"
                :on-progress="onProgress"
                :on-error="onError"
                action="platform/v1/packages"
                :headers="headers"
              >
                <Button style="display:none" icon="ios-cloud-upload-outline">{{ $t('upload_plugin_btn') }}</Button>
              </Upload>
            </div>
            <span v-if="showSuccess" style="color:#2b85e4">{{ $t('plugin_analysis') }}</span>
          </div>
        </Card>
      </Row>
      <Row class="plugins-tree-container" style="margin-top: 20px">
        <Card dis-hover>
          <Row slot="title">
            <Col span="12">
              <span style="line-height:19px">
                {{ $t('plugins_list') }}
              </span>
            </Col>
            <Col style="float: right">
              <Checkbox style="width: max-content;" class="clear-default-css" v-model="isShowDecomissionedPackage">
                {{ $t('is_show_decomissioned_pkg') }}
              </Checkbox>
            </Col>
          </Row>
          <Spin size="large" v-if="isLoadingPluginList">
            <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
            <div>{{ $t('loading') }}</div>
          </Spin>
          <div v-if="!isLoadingPluginList" style="height: 70%; overflow: auto">
            <span v-if="plugins.length < 1">{{ $t('no_plugin_packages') }}</span>
            <div style="height: calc(100vh - 325px);overflow:auto" v-else>
              <Collapse accordion @on-change="pluginPackageChangeHandler">
                <Panel
                  :name="plugin.id + ''"
                  v-for="plugin in plugins"
                  v-if="plugin.status !== 'DECOMMISSIONED' || isShowDecomissionedPackage"
                  :key="plugin.id"
                >
                  <div style="float: right;width: calc(100% - 30px);">
                    <span
                      :class="plugin.status !== 'DECOMMISSIONED' ? 'plugin-title' : 'decomissionedPkgName plugin-title'"
                    >
                      {{ plugin.name + '_' + plugin.version }}
                    </span>
                    <span style="float: right; margin-right: 10px">
                      <Tooltip :content="$t('configuration_import')">
                        <Button
                          icon="ios-cloud-upload-outline"
                          v-if="plugin.status !== 'DECOMMISSIONED'"
                          size="small"
                          type="primary"
                          ghost
                          @click.stop.prevent="importBestPractices(plugin.id)"
                        ></Button>
                      </Tooltip>
                      <Tooltip :content="$t('configuration_export')">
                        <Button
                          v-if="plugin.status !== 'DECOMMISSIONED'"
                          @click.stop.prevent="exportBestPractices(plugin.id)"
                          size="small"
                          type="primary"
                          ghost
                          icon="md-download"
                        ></Button>
                      </Tooltip>
                      <Tooltip :content="$t('delete')">
                        <Button
                          v-if="plugin.status !== 'DECOMMISSIONED'"
                          @click.stop.prevent="deletePlugin(plugin.id)"
                          size="small"
                          type="error"
                          ghost
                          icon="ios-trash"
                        ></Button>
                      </Tooltip>
                    </span>
                  </div>
                  <p slot="content" class="button-group">
                    <Button @click="configPlugin(plugin.id)" size="small" type="info" ghost icon="ios-checkmark-circle">
                      {{ $t('plugin_config_check') }}
                    </Button>
                    <Button @click="manageService(plugin.id)" size="small" type="info" ghost icon="ios-construct">
                      {{ $t('service_regist') }}
                    </Button>
                    <Button @click="manageRuntimePlugin(plugin.id)" size="small" type="info" ghost icon="ios-settings">
                      {{ $t('runtime_manage') }}
                    </Button>
                  </p>
                </Panel>
              </Collapse>
            </div>
          </div>
        </Card>
      </Row>
    </Col>
    <Col span="17" style="padding-left: 20px" v-if="isShowConfigPanel">
      <Tabs type="card" :value="currentTab" @on-click="handleTabClick">
        <TabPane name="dependency" :label="$t('dependencies_analysis')">
          <DependencyAnalysis v-if="currentTab === 'dependency'" :pkgId="currentPlugin.id"></DependencyAnalysis>
        </TabPane>
        <TabPane name="menus" :label="$t('menu_injection')">
          <MenuInjection v-if="currentTab === 'menus'" :pkgId="currentPlugin.id"></MenuInjection>
        </TabPane>
        <TabPane name="models" :label="$t('data_model')">
          <DataModel v-if="currentTab === 'models'" :pkgId="currentPlugin.name"></DataModel>
        </TabPane>
        <TabPane name="systemParameters" :label="$t('system_params')">
          <SysParmas v-if="currentTab === 'systemParameters'" :pkgId="currentPlugin.id"></SysParmas>
        </TabPane>
        <TabPane name="authorities" :label="$t('auth_setting')">
          <AuthSettings v-if="currentTab === 'authorities'" :pkgId="currentPlugin.id"></AuthSettings>
        </TabPane>
        <TabPane name="runtimeResources" :label="$t('runtime_resource')">
          <RuntimesResources v-if="currentTab === 'runtimeResources'" :pkgId="currentPlugin.id"></RuntimesResources>
        </TabPane>
        <TabPane v-if="currentPlugin.status === 'UNREGISTERED'" name="confirm" :label="$t('confirm')">
          <Button type="info" :disabled="isRegisted" @click="registPackage()">{{
            $t('confirm_to_regist_plugin')
          }}</Button>
        </TabPane>
      </Tabs>
    </Col>
    <Col span="17" style="padding-left: 20px" v-if="isShowServicePanel">
      <Card dis-hover>
        <PluginRegister
          v-if="isShowServicePanel"
          :pkgId="currentPlugin.id"
          :pkgName="currentPlugin.name"
        ></PluginRegister>
      </Card>
    </Col>
    <Col span="17" style="padding-left: 20px" v-if="isShowRuntimeManagementPanel">
      <div v-if="Object.keys(currentPlugin).length > 0">
        <div>
          <Row class="instances-container">
            <Collapse value="1" @on-change="onRuntimeCollapseChange">
              <Panel name="1">
                <span style="font-size: 14px; font-weight: 600">{{ $t('runtime_container') }}</span>
                <p slot="content">
                  <Card dis-hover>
                    <Row>
                      <Select
                        @on-change="selectHost"
                        @on-open-change="hostSelectOpenHandler"
                        multiple
                        style="width: 40%"
                        :max-tag-count="4"
                        v-model="selectHosts"
                      >
                        <Option v-for="item in allAvailiableHosts" :value="item" :key="item">{{ item }}</Option>
                      </Select>
                      <Button size="small" type="success" @click="getAvailablePortByHostIp">
                        {{ $t('port_preview') }}
                      </Button>
                      <div v-if="availiableHostsWithPort.length > 0">
                        <p style="margin-top: 20px">{{ $t('avaliable_port') }}:</p>

                        <div v-for="item in availiableHostsWithPort" :key="item.ip + item.port">
                          <div class="instance-item-container" style="border-bottom: 1px solid gray; padding: 10px 0">
                            <div class="instance-item">
                              <Col span="3">{{ item.ip + ':' + item.port }}</Col>
                              <Button
                                size="small"
                                type="success"
                                @click="createPluginInstanceByPackageIdAndHostIp(item.ip, item.port)"
                                >{{ $t('create') }}</Button
                              >
                            </div>
                          </div>
                        </div>
                      </div>
                    </Row>
                    <Row>
                      <p style="margin-top: 20px">{{ $t('running_node') }}:</p>
                      <div v-if="allInstances.length === 0">{{ $t('no_avaliable_instances') }}</div>
                      <div v-else>
                        <div v-for="item in allInstances" :key="item.id">
                          <div class="instance-item-container">
                            <Col span="3">
                              <div class="instance-item">{{ item.displayLabel }}</div>
                            </Col>
                            <Col span="5" offset="0">
                              <Button size="small" type="error" @click="removePluginInstance(item.id)">
                                {{ $t('ternmiante') }}
                              </Button>
                            </Col>
                          </div>
                        </div>
                      </div>
                    </Row>
                  </Card>
                </p>
              </Panel>
              <Panel name="2">
                <span style="font-size: 14px; font-weight: 600">{{ $t('database') }}</span>
                <Row slot="content">
                  <Row>
                    <Col span="16">
                      <Input v-model="dbQueryCommandString" type="textarea" :placeholder="$t('only_select')" />
                    </Col>
                    <Col span="4" offset="1">
                      <Button @click="getDBTableData">{{ $t('execute') }}</Button>
                    </Col>
                  </Row>
                  <Row style="margin-top: 20px">
                    {{ $t('search_result') + ':' }}
                    <div style="width: 100%;overflow: auto">
                      <Table :columns="dbQueryColumns" :data="dbQueryData"></Table>
                    </div>
                    <Page
                      :total="dbTablePagination.total"
                      :current="dbTablePagination.currentPage"
                      :page-size="dbTablePagination.pageSize"
                      @on-change="onDBTablePageChange"
                      @on-page-size-change="onDBTablePageSizeChange"
                      show-elevator
                      show-sizer
                      show-total
                      style="float: right; margin: 10px 0;"
                    />
                  </Row>
                </Row>
              </Panel>
              <Panel name="3">
                <span style="font-size: 14px; font-weight: 600">{{ $t('storage_service') }}</span>
                <Row slot="content">
                  <Table :columns="storageServiceColumns" :data="storageServiceData"></Table>
                </Row>
              </Panel>
            </Collapse>
          </Row>
        </div>
      </div>
    </Col>
    <Modal footer-hide :title="$t('best_practices')" v-model="isShowImportXMLModal" @on-open-change="modalChangeHandle">
      <div>
        <Upload
          ref="importXML"
          type="drag"
          accept=".xml"
          name="xml-file"
          :on-success="onImportSuccess"
          :on-error="onError"
          :action="'platform/v1/plugins/packages/import/' + currentPluginId"
          :headers="headers"
        >
          <div style="padding: 20px 0">
            <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
            <p>Click or drag files here to upload</p>
          </div>
        </Upload>
      </div>
    </Modal>
    <Modal footer-hide :title="$t('origin_plugins')" v-model="showUploadModal" @on-open-change="modalChangeHandle">
      <div>
        <Input :placeholder="$t('search')" v-model="filterForPkg"></Input>
      </div>
      <div style="width:100%;height:350px;overflow: auto;margin-top: 10px">
        <RadioGroup v-model="selectedOriginPlugin" vertical>
          <template v-for="(item, index) in Object.keys(originPluginsGroupFilter)">
            <span :key="index" style="color: #999;line-height:30px">{{ item }}</span>
            <Radio v-for="plugin in originPluginsGroupFilter[item]" :key="plugin.keyName" :label="plugin.keyName">
              <span>{{ plugin.keyName }}</span>
            </Radio>
          </template>
        </RadioGroup>
      </div>
      <div style="height:30px;text-align: right">
        <Button style="margin-left:10px;float:right" type="info" @click="uploadHandler">{{ $t('bc_confirm') }}</Button>
        <Button style="margin-left:10px;float:right" @click="cancelHandler">{{ $t('cancel') }}</Button>
      </div>
    </Modal>
  </Row>
</template>
<script>
import {
  getAllPluginPkgs,
  createPluginInstanceByPackageIdAndHostIp,
  removePluginInstance,
  queryLog,
  getAvailableContainerHosts,
  getAvailablePortByHostIp,
  deletePluginPkg,
  registPluginPackage,
  getAvailableInstancesByPackageId,
  queryDataBaseByPackageId,
  queryStorageFilesByPackageId,
  getPluginArtifacts,
  pullPluginArtifact,
  getPluginArtifactStatus,
  exportPluginXMLWithId
} from '@/api/server.js'

import DataModel from './components/data-model.vue'
import DependencyAnalysis from './components/dependency-analysis.vue'
import PluginRegister from './components/plugin-register.vue'
import MenuInjection from './components/menu-injection.vue'
import SysParmas from './components/system-params.vue'
import RuntimesResources from './components/runtime-resource.vue'
import AuthSettings from './components/auth-setting.vue'
import { setCookie, getCookie } from '../util/cookie'
import axios from 'axios'
const logTablePagination = {
  pageSize: 10,
  currentPage: 1,
  total: 0
}
const dbTablePagination = {
  pageSize: 10,
  currentPage: 1,
  total: 0
}
export default {
  components: {
    DataModel,
    DependencyAnalysis,
    PluginRegister,
    MenuInjection,
    SysParmas,
    RuntimesResources,
    AuthSettings
  },
  data () {
    return {
      showUpload: false,
      currentPluginId: '',
      filterForPkg: '',
      isRegisted: false,
      headers: {},
      showSuccess: false,
      showUploadModal: false,
      isLoading: false,
      plugins: [],
      isShowConfigPanel: false,
      isShowServicePanel: false,
      isShowRuntimeManagementPanel: false,
      currentTab: 'dependency',
      currentPlugin: {},
      logTableData: [],
      totalLogTableData: [],
      innerActions: [
        {
          label: this.$t('show_details'),
          props: {
            type: 'info',
            size: 'small'
          },
          actionType: 'showLogDetails'
        }
      ],
      logTableColumns: [
        {
          title: this.$t('instance'),
          key: 'instance',
          inputKey: 'instance',
          searchSeqNo: 1,
          displaySeqNo: 1,
          component: 'WeSelect',
          isMultiple: true,
          placeholder: this.$t('instance'),
          span: 5,
          width: '200px',
          options: []
        },
        {
          title: this.$t('file_name'),
          key: 'file_name',
          inputKey: 'file_name',
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: 'Input',
          isNotFilterable: true,
          placeholder: this.$t('file_name'),
          width: '200px'
        },
        {
          title: this.$t('line_number'),
          key: 'line_number',
          inputKey: 'line_number',
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: 'Input',
          isNotFilterable: true,
          placeholder: this.$t('line_number'),
          width: '150px'
        },
        {
          title: this.$t('match_text'),
          key: 'log',
          inputKey: 'log',
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: 'Input',
          placeholder: this.$t('match_text')
        }
      ],
      logTablePagination,
      dbTablePagination,
      allAvailiableHosts: [],
      allInstances: [],
      searchFilters: [],
      logDetailsModalVisible: false,
      logDetails: '',
      dbQueryCommandString: '',
      dbQueryColumns: [],
      dbQueryData: [],
      storageServiceColumns: [
        {
          title: this.$t('file_name'),
          key: 'file'
        },
        {
          title: this.$t('path'),
          key: 'path'
        },
        {
          title: 'Hash',
          key: 'hash'
        },
        {
          title: this.$t('upload_time'),
          key: 'uploadTime'
        }
      ],
      storageServiceData: [],
      defaultCreateParams: '',
      selectHosts: [],
      availiableHostsWithPort: [],
      isShowDecomissionedPackage: false,
      isLoadingPluginList: false,
      originPlugins: [],
      selectedOriginPlugin: '',
      originPluginsGroupFilter: {},
      pluginTimer: null,
      loadingPlugin: false,
      isShowImportXMLModal: false
    }
  },
  watch: {
    filterForPkg: {
      handler (v) {
        this.originPluginsGroupFilter = []
        this.originPlugins
          .filter(_ => _.keyName.indexOf(v) > -1)
          .forEach(item => {
            if (item.keyName.split('-v')[0] in this.originPluginsGroupFilter) {
              this.originPluginsGroupFilter[item.keyName.split('-v')[0]].push(item)
            } else {
              this.originPluginsGroupFilter[item.keyName.split('-v')[0]] = [item]
            }
          })
      }
    }
  },
  methods: {
    modalChangeHandle (v) {
      if (!v) {
        this.cancelHandler()
      }
    },
    cancelHandler () {
      this.showUploadModal = false
      this.selectedOriginPlugin = ''
      this.filterForPkg = ''
    },
    async uploadHandler () {
      const payload = {
        keyName: this.selectedOriginPlugin
      }
      this.isLoading = true
      const res = await pullPluginArtifact(payload) // getPluginArtifactStatus
      if (res.status === 'OK') {
        this.cancelHandler()
        this.$nextTick(() => {
          this.pluginTimer = setInterval(async () => {
            const { status, data } = await getPluginArtifactStatus(res.data.requestId)
            if (status !== 'OK' || data.state !== 'InProgress') {
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
              this.isLoading = false
            }
            if (status === 'OK' && data.state !== 'InProgress') {
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
              this.isLoading = false
              this.$Notice.info({
                title: 'Notification',
                desc: data.state
              })
              if (data.state === 'Completed') {
                this.getAllPluginPkgs()
              }
            }
          }, 5000)
        })
      }
    },
    async showUploadModalHandler () {
      this.loadingPlugin = true
      const { status, data } = await getPluginArtifacts()
      this.loadingPlugin = false
      if (status === 'OK') {
        this.originPlugins = data
        this.originPluginsGroupFilter = []
        data.forEach(item => {
          if (item.keyName.split('-v')[0] in this.originPluginsGroupFilter) {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]].push(item)
          } else {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]] = [item]
          }
        })
        this.showUploadModal = true
      }
    },
    onProgress (event, file, fileList) {
      if (event.percent === 100) {
        this.showSuccess = true
      }
    },
    async onImportSuccess (response, file, filelist) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })
        this.getAllPluginPkgs()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message || ''
        })
      }
      this.isShowImportXMLModal = false
      this.$refs.importXML.clearFiles()
    },
    async onSuccess (response, file, filelist) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })
        this.getAllPluginPkgs()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message || ''
        })
      }
      this.showSuccess = false
    },
    onError (file, filelist) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    },
    swapPanel (panel) {
      this.isShowServicePanel = panel === 'servicePanel'
      this.isShowConfigPanel = panel === 'pluginConfigPanel'
      this.isShowRuntimeManagementPanel = panel === 'runtimeManagePanel'
    },
    async createPluginInstanceByPackageIdAndHostIp (ip, port) {
      this.$Notice.info({
        title: 'Info',
        desc: 'Start Launching... It will take sometime.'
      })
      this.isLoading = true
      const { status } = await createPluginInstanceByPackageIdAndHostIp(this.currentPlugin.id, ip, port)
      this.isLoading = false
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Instance launched successfully'
        })
        const index = this.availiableHostsWithPort.findIndex(item => item.port === port)
        this.availiableHostsWithPort.splice(index, 1)
        this.getAvailableInstancesByPackageId(this.currentPlugin.id)
      }
    },

    async registPackage () {
      this.isRegisted = true
      let { status } = await registPluginPackage(this.currentPlugin.id)
      this.isRegisted = false
      if (status === 'OK') {
        this.$set(this.currentPlugin, 'status', 'REGISTERED')
        this.$Notice.success({
          title: 'Success',
          desc: this.$t('reload_to_get_ui')
        })
      }
    },
    importBestPractices (packageId) {
      let refreshRequest = null
      this.currentPluginId = packageId
      const currentTime = new Date().getTime()
      const accessToken = getCookie('accessToken')
      if (accessToken) {
        const expiration = getCookie('accessTokenExpirationTime') * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + getCookie('refreshToken')
            }
          })
          refreshRequest.then(
            res => {
              setCookie(res.data.data)
              this.setUploadActionHeader()
              // this.$refs.importXML.handleClick()
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
            }
          )
        } else {
          this.setUploadActionHeader()
        }
      } else {
        window.location.href = window.location.origin + window.location.pathname + '#/login'
      }
      this.isShowImportXMLModal = true
    },
    exportBestPractices (packageId) {
      exportPluginXMLWithId(packageId)
    },
    deletePlugin (packageId) {
      let pkgId = packageId
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          let { status } = await deletePluginPkg(pkgId)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: this.$t('reload_to_delete_ui')
            })
            this.getAllPluginPkgs()
            this.swapPanel('')
          }
        },
        onCancel: () => {}
      })
      document.querySelector('.ivu-modal-mask').click()
    },
    configPlugin (packageId) {
      this.swapPanel('pluginConfigPanel')
      this.currentPlugin = this.plugins.find(_ => _.id === packageId)
      this.selectedCiType = this.currentPlugin.cmdbCiTypeId || ''
    },
    manageService (packageId) {
      this.swapPanel('servicePanel')
      this.currentPlugin = this.plugins.find(_ => _.id === packageId)
      this.selectedCiType = this.currentPlugin.cmdbCiTypeId || ''
    },
    async manageRuntimePlugin (packageId) {
      this.swapPanel('runtimeManagePanel')
      let currentPlugin = this.plugins.find(_ => _.id === packageId)
      this.selectedCiType = currentPlugin.cmdbCiTypeId || ''
      this.currentPlugin = currentPlugin
      // if (currentPlugin.pluginConfigs) {
      this.selectHosts = []
      this.availiableHostsWithPort = []
      this.getAvailableInstancesByPackageId(this.currentPlugin.id)
      // }
      this.dbQueryData = []
      this.dbQueryColumns = []
      this.getAvailableContainerHosts()
      this.resetLogTable()
    },
    onRuntimeCollapseChange (key) {
      const found = !!key.find(i => i === '3')
      if (found) {
        this.getStorageTableData()
      }
    },
    async getStorageTableData () {
      let { status, data } = await queryStorageFilesByPackageId(this.currentPlugin.id)
      if (status === 'OK') {
        this.storageServiceData = data.map(_ => {
          return {
            file: _[0],
            path: _[1],
            hash: _[2],
            uploadTime: _[3]
          }
        })
      }
    },
    async getDBTableData () {
      let payload = {
        sqlQuery: this.dbQueryCommandString,
        pageable: {
          pageSize: this.dbTablePagination.pageSize,
          startIndex: this.dbTablePagination.pageSize * (this.dbTablePagination.currentPage - 1)
        }
      }
      let { status, data } = await queryDataBaseByPackageId(this.currentPlugin.id, payload)
      if (status === 'OK') {
        this.dbTablePagination.total = data.pageInfo.totalRows
        this.dbQueryColumns = data.headers.map(_ => {
          return {
            key: _,
            title: _,
            minWidth: 170
          }
        })
        this.dbQueryData = data.contents.map(_ => {
          let tempObj = {}
          _.forEach((i, index) => {
            tempObj[this.dbQueryColumns[index].key] = i
          })
          return tempObj
        })
      }
    },
    onDBTablePageChange (currentPage) {
      this.dbTablePagination.currentPage = currentPage
      this.getDBTableData()
    },
    onDBTablePageSizeChange (pageSize) {
      this.dbTablePagination.pageSize = pageSize
      this.getDBTableData()
    },
    pluginPackageChangeHandler (key) {
      this.swapPanel('')
      this.dbQueryCommandString = ''
      this.dbTablePagination.currentPage = 1
      this.dbTablePagination.pageSize = 10
      this.dbTablePagination.total = 0
    },
    async removePluginInstance (instanceId) {
      this.isLoading = true
      let { status, message } = await removePluginInstance(instanceId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
      }
      this.isLoading = false
      this.getAvailableInstancesByPackageId(this.currentPlugin.id)
    },
    async getAvailableInstancesByPackageId (id) {
      this.isLoading = true
      let { data, status } = await getAvailableInstancesByPackageId(id)
      if (status === 'OK') {
        this.allInstances = data.map(_ => {
          if (_.status !== 'REMOVED') {
            return {
              id: _.id,
              hostIp: _.host,
              port: _.port,
              displayLabel: _.host + ':' + _.port
            }
          }
        })
      }
      this.isLoading = false
    },
    hostSelectOpenHandler (flag) {
      if (flag) {
        this.getAvailableContainerHosts()
      }
    },
    async getAvailableContainerHosts () {
      const { data, status } = await getAvailableContainerHosts()
      if (status === 'OK') {
        this.allAvailiableHosts = data
      }
    },
    getAvailablePortByHostIp () {
      this.availiableHostsWithPort = []
      this.selectHosts.forEach(async _ => {
        const { data, status } = await getAvailablePortByHostIp(_)
        if (status === 'OK') {
          this.availiableHostsWithPort.push({
            ip: _,
            port: data,
            createParams: this.defaultCreateParams
          })
        }
      })
    },
    handleLogTableSubmit (data) {
      this.searchFilters = data
      this.getLogTableData()
    },
    async getLogTableData () {
      if (this.searchFilters.length < 2) return
      const payload = {
        instanceIds: this.searchFilters[0].value,
        pluginRequest: {
          inputs: [
            {
              key_word: this.searchFilters[1].value
            }
          ]
        }
      }
      let { status, data } = await queryLog(payload)
      if (status === 'OK') {
        for (let i in data) {
          let arr = []
          this.totalLogTableData = arr.concat(
            data[i].outputs.map(_ => {
              return {
                instance: this.allInstances.find(j => j.id === +i).displayLabel,
                instanceId: i,
                ..._
              }
            })
          )
        }
        this.handleLogTablePagination()
      }
    },
    onLogTableChange (current) {
      this.logTablePagination.currentPage = current
      this.handleLogTablePagination()
    },
    onLogTablePageSizeChange (size) {
      this.logTablePagination.pageSize = size
      this.handleLogTablePagination()
    },
    handleLogTablePagination () {
      this.logTablePagination.total = this.totalLogTableData.length
      let temp = Array.from(this.totalLogTableData)
      this.logTableData = temp.splice(
        (this.logTablePagination.currentPage - 1) * this.logTablePagination.pageSize,
        this.logTablePagination.pageSize
      )
    },
    actionFun (type, data) {
      if (type === 'showLogDetails') {
        this.getLogDetail(data)
      }
    },
    resetLogTable () {
      this.logTableData = []
      this.totalLogTableData = []
      this.$refs.table && this.$refs.table.reset()
    },
    selectHost (v) {
      this.selectHosts = v
    },
    handleTabClick (name) {
      this.currentTab = name
    },
    async getAllPluginPkgs () {
      this.isLoadingPluginList = true
      let { status, data } = await getAllPluginPkgs()
      this.isLoadingPluginList = false
      if (status === 'OK') {
        this.plugins = data.map(_ => {
          return {
            ..._,
            title: `${_.name}[${_.version}]`,
            id: _.id,
            expand: false,
            checked: false
            // children: _.pluginConfigs.map(i => {
            //   return {
            //     ...i,
            //     title: i.name,
            //     id: i.id,
            //     expand: true,
            //     checked: false
            //   }
            // })
          }
        })
      }
    },
    getHeaders () {
      this.showUpload = true
      let refreshRequest = null
      const currentTime = new Date().getTime()
      const accessToken = getCookie('accessToken')
      if (accessToken) {
        const expiration = getCookie('accessTokenExpirationTime') * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + getCookie('refreshToken')
            }
          })
          refreshRequest.then(
            res => {
              setCookie(res.data.data)
              this.setUploadActionHeader()
              this.$refs.uploadButton.handleClick()
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
            }
          )
        } else {
          this.setUploadActionHeader()
          this.$refs.uploadButton.handleClick()
        }
      } else {
        window.location.href = window.location.origin + window.location.pathname + '#/login'
      }
    },
    setUploadActionHeader () {
      this.headers = {
        Authorization: 'Bearer ' + getCookie('accessToken')
      }
    }
  },
  created () {
    this.getAllPluginPkgs()
  },
  computed: {}
}
</script>
<style lang="scss">
.decomissionedPkgName {
  font-style: italic;
  text-decoration: line-through;
}
.button-group {
  display: flex;
  justify-content: space-around;
}
.clear-default-css {
  margin-bottom: 0;
}
.plugin-title {
  width: calc(100% - 110px);
  display: block;
  float: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
