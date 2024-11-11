<template>
  <div class="all-page">
    <Spin fix v-if="isSpinShow" style="z-index: 1000000">
      <Icon type="ios-loading" :size="25" class="spin-icon-load"></Icon>
      <div style="font-size: 20px">{{ spinContent }}</div>
    </Spin>
    <div class="register-content">
      <div class="content-header">
        <Icon size="22" class="arrow-back" type="md-arrow-back" @click="returnPreviousPage" />
        <h5>{{ pluginItemDetail.name + '_' + pluginItemDetail.version }}</h5>
      </div>
      <div class="content-detail">
        <div v-if="!isJustShowRightContent" class="left-content">
          <div class="use-underline-title left-title">
            {{ $t('p_installation_steps') }}
            <span class="underline"></span>
          </div>
          <div class="step-content">
            <Steps :current="currentStep" direction="vertical">
              <Step v-for="(item, index) in stepContent" :key="index" :title="item.title" :content="item.content">
              </Step>
            </Steps>
          </div>
        </div>

        <div class="right-content" :style="{width: isJustShowRightContent ? '98vw' : '67vw'}">
          <div class="use-underline-title right-title">
            {{ stepTitleMap[currentStep] }}
            <span class="underline"></span>
          </div>
          <Tabs v-if="[1, 3, 4].includes(currentStep)" type="card" :value="currentTabName" @on-click="handleTabClick">
            <TabPane name="0" :disabled="[3, 4].includes(currentStep)" :label="$t('dependencies_analysis')">
              <div class="single-tab-content">
                <DependencyAnalysis v-if="currentTabName === '0'" :pkgId="pluginId"></DependencyAnalysis>
              </div>
            </TabPane>
            <TabPane name="1" :disabled="[4].includes(currentStep)" :label="$t('menu_injection')">
              <div class="single-tab-content">
                <Alert style="margin: 5px 0">
                  <p>{{ $t('regist_plugin_tip1') }}</p>
                  <br />
                  <p>{{ $t('regist_plugin_tip2') }}</p>
                </Alert>
                <MenuInjection v-if="currentTabName === '1'" :pkgId="pluginId"></MenuInjection>
              </div>
            </TabPane>
            <TabPane name="2" :disabled="[3, 4].includes(currentStep)" :label="$t('data_model')">
              <div class="single-tab-content">
                <DataModel
                  v-if="currentTabName === '2'"
                  :pkgId="pluginId"
                  :pluginName="pluginItemDetail.name"
                ></DataModel>
              </div>
            </TabPane>
            <TabPane name="3" :disabled="[3, 4].includes(currentStep)" :label="$t('system_params')">
              <div class="single-tab-content">
                <SysParmas v-if="currentTabName === '3'" :pkgId="pluginId"></SysParmas>
              </div>
            </TabPane>
            <TabPane name="4" :disabled="[3, 4].includes(currentStep)" :label="$t('auth_setting')">
              <div class="single-tab-content">
                <AuthSettings v-if="currentTabName === '4'" :pkgId="pluginId"></AuthSettings>
              </div>
            </TabPane>
            <TabPane name="5" :disabled="[3, 4].includes(currentStep)" :label="$t('p_running_resource_declaration')">
              <div class="single-tab-content">
                <RuntimesResources v-if="currentTabName === '5'" :pkgId="pluginId"></RuntimesResources>
              </div>
            </TabPane>
            <TabPane
              name="6"
              :disabled="currentStep === 3 || (currentStep === 1 && !isJustShowRightContent)"
              :label="$t('p_running_resources_actual')"
            >
              <div class="single-tab-content">
                <Collapse value="1">
                  <Panel name="1">
                    <span style="font-size: 14px; font-weight: 600">{{ $t('runtime_container') }}</span>
                    <p slot="content">
                      <Card dis-hover>
                        <Row>
                          <Select v-model="selectedIp" multiple style="width: 40%" :max-tag-count="4">
                            <Option v-for="item in availableHostList" :value="item" :key="item">{{ item }}</Option>
                          </Select>
                          <Button size="small" type="success" @click="getPortByHostIp">
                            {{ $t('port_preview') }}
                          </Button>
                          <div v-if="allowCreationIpPort.length > 0">
                            <p style="margin-top: 20px">{{ $t('avaliable_port') }}:</p>
                            <div v-for="item in allowCreationIpPort" :key="item.ip + item.port">
                              <div class="instance-item-container">
                                <div class="instance-item">
                                  <Col span="4">{{ item.ip + ':' + item.port }}</Col>
                                  <Button
                                    size="small"
                                    type="success"
                                    @click="createInstanceByIpPort(item.ip, item.port)"
                                  >{{ $t('create') }}</Button>
                                </div>
                              </div>
                            </div>
                          </div>
                        </Row>
                        <Row>
                          <p style="margin-top: 20px">{{ $t('running_node') }}:</p>
                          <div v-if="allInstances.length === 0">{{ $t('no_avaliable_instances') }}</div>
                          <div v-else style="display: flex; flex-direction: column">
                            <div v-for="item in allInstances" :key="item.id" class="mt-2">
                              <div>
                                <Col span="4">
                                  <div class="instance-item">{{ item.displayLabel }}</div>
                                </Col>
                                <Col span="5" offset="0">
                                  <Poptip
                                    confirm
                                    :title="$t('p_destroy_tips')"
                                    placement="left-end"
                                    @on-ok="removePlugin(item.id)"
                                  >
                                    <Button size="small" type="error" class="destroy-instance-button">{{
                                      $t('ternmiante')
                                    }}</Button>
                                  </Poptip>
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
                        <div style="width: 100%; overflow: auto">
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
                          style="float: right; margin: 10px 0"
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
              </div>
            </TabPane>
          </Tabs>

          <div v-if="currentStep === 2">
            <div style="margin-left: 5px">
              {{ $t('p_use_version') }}
              <span style="color: #1e3fec; margin-left: 12px">{{
                $t('p_version_number') + ': ' + inheritedVersion
              }}</span>
            </div>
            <PluginRegister
              style="height: 40%"
              v-if="currentStep === 2"
              ref="pluginRegister"
              :pkgId="pluginId"
              :pkgName="pluginItemDetail.name"
              :batchRegistButtonShow="false"
              :modalTitleVersion="selectedVersion"
              @get-service-list="onServiceListGet"
              @success="onRegisteSuccess"
            ></PluginRegister>
          </div>
        </div>
      </div>
    </div>
    <div class="footer-button">
      <Dropdown v-if="currentStep === 2" placement="bottom-start" @on-click="onInheritedVersionSelected">
        <Button type="info" class="mr-3">
          {{ $t('p_inherited_version') }}
          <Icon type="ios-arrow-down"></Icon>
        </Button>
        <template #list>
          <DropdownMenu>
            <DropdownItem v-for="(item, index) in inheritedVersionOptionList" :name="JSON.stringify(item)" :key="index">
              {{ item.name + '_' + item.version }}
            </DropdownItem>
          </DropdownMenu>
        </template>
      </Dropdown>
      <Upload
        ref="importXML"
        :action="'platform/v1/plugins/packages/import/' + pluginId"
        name="xml-file"
        with-credentials
        :headers="uploadHeaders"
        :on-success="onImportSuccess"
        :on-error="onError"
        accept=".xml"
      >
        <Button v-if="currentStep === 2" class="mr-3" type="info">
          {{ $t('p_importing_configuration') }}
        </Button>
      </Upload>
      <Button v-if="currentStep === 2 && isJustShowRightContent" @click="() => batchRegist()" type="warning">
        {{ $t('batch_regist') }}
      </Button>
      <div v-else>
        <Poptip
          v-if="currentStep === 2 && !isServiceActionNotEmpty"
          confirm
          placement="left-end"
          word-wrap
          :ok-text="$t('p_skip')"
          :cancel-text="$t('return')"
          @on-ok="enterNextStep"
        >
          <div slot="title">
            <h6 class="mb-1">{{ $t('p_confirm_next_step') }}</h6>
            <div>{{ $t('p_no_use_warning') }}</div>
            <div>{{ $t('p_no_use_action') }}</div>
          </div>
          <Button type="primary">
            {{ $t('next_step') }}
          </Button>
        </Poptip>
        <div v-else>
          <div v-if="!isJustShowRightContent">
            <Button
              v-for="(item, index) in footerButtonMap[currentStep]"
              :key="index"
              :type="item.buttonType"
              :disabled="typeof item.disabled === 'function' ? item.disabled() : item.disabled"
              class="mr-3"
              @click="onFooterButtonClick(item.key)"
            >{{ item.label }}
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import hasIn from 'lodash/hasIn'
import isEmpty from 'lodash/isEmpty'
import cloneDeep from 'lodash/cloneDeep'
import find from 'lodash/find'
import DependencyAnalysis from './components/dependency-analysis.vue'
import MenuInjection from './components/menu-injection.vue'
import DataModel from './components/data-model.vue'
import SysParmas from './components/system-params.vue'
import AuthSettings from './components/auth-setting.vue'
import RuntimesResources from './components/runtime-resource.vue'
import PluginRegister from './components/plugin-register.vue'
import { getCookie } from '@/pages/util/cookie'
import req from '@/api/base'
import {
  getAvailableContainerHosts,
  getAvailablePortByHostIp,
  createPluginInstanceByPackageIdAndHostIp,
  removePluginInstance,
  queryDataBaseByPackageId,
  queryStorageFilesByPackageId,
  registPluginPackage,
  getAvailableInstancesByPackageId,
  getPluginConfigsByPackageId
} from '@/api/server.js'

export default {
  name: '',
  components: {
    DataModel,
    DependencyAnalysis,
    PluginRegister,
    MenuInjection,
    SysParmas,
    RuntimesResources,
    AuthSettings
  },
  data() {
    return {
      pluginId: '',
      pluginItemDetail: {
        name: '',
        version: ''
      },
      currentStep: 1,
      stepContent: [
        {
          title: this.$t('p_upload_success') + ':' + this.$t('p_plugin_packages'),
          content: ''
        },
        {
          title: this.$t('confirm') + this.$t('plugin_config_check'),
          content: this.$t('p_second_step_content')
        },
        {
          title: this.$t('p_third_step_title'),
          content: this.$t('p_third_step_content')
        },
        {
          title: this.$t('p_fourth_step_title'),
          content: this.$t('p_fourth_step_content')
        },
        {
          title: this.$t('p_fifth_step_title'),
          content: this.$t('p_fifth_step_content')
        },
        {
          title: this.$t('p_sixth_step_title'),
          content: this.$t('p_sixth_step_content')
        }
      ],
      currentTabName: '0',
      footerButtonMap: {
        1: [
          {
            label: this.$t('next_step'),
            buttonType: 'primary',
            key: 'oldRegist',
            disabled: false
          }
        ],
        2: [
          {
            label: this.$t('next_step'),
            buttonType: 'primary',
            key: 'next',
            disabled: false
          }
        ],
        3: [
          {
            label: this.$t('regist'),
            buttonType: 'success',
            key: 'regist',
            disabled: false
          }
        ],
        4: [
          {
            label: this.$t('p_sixth_step_title'),
            buttonType: 'primary',
            key: 'finish',
            disabled: () => this.allInstances.length === 0
          }
        ]
      },
      buttonFunctionMap: {
        oldRegist: 'oldRegistItem',
        next: 'enterNextStep',
        regist: 'registItem',
        finish: 'installFinish'
      },
      inheritedVersionOptionList: [],
      uploadHeaders: {
        Authorization: 'Bearer ' + getCookie('accessToken')
      },
      inheritedVersion: '-',
      selectedIp: [],
      availableHostList: [],
      allowCreationIpPort: [],
      allInstances: [],
      dbQueryCommandString: '',
      dbTablePagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
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
      stepTitleMap: {
        1: this.$t('plugin_config_check'),
        2: this.$t('p_services_list'),
        3: this.$t('plugin_config_check'),
        4: this.$t('p_fifth_step_title')
      },
      isJustShowRightContent: false,
      isServiceActionNotEmpty: false, // 整个注册数字中只要有一个item.pluginConfigDtoList不为空数组则为true
      isSpinShow: false,
      selectedVersion: '',
      spinContent: ''
    }
  },
  created() {
    this.pluginId = this.$route.query.pluginId || ''
    if (hasIn(this.$route.query, 'step') && this.$route.query.step) {
      const step = this.$route.query.step
      if ([1, 2].includes(step)) {
        this.isJustShowRightContent = true
        this.currentStep = Number(this.$route.query.step)
      } else {
        this.returnPreviousPage()
      }
    }
    this.getPluginItemDetail()
    this.getOptionList()
  },
  methods: {
    async getPluginItemDetail() {
      if (!this.pluginId) {
        this.$router.push({ path: '/collaboration/plugin-management' })
      }
      const api = '/platform/v1/packages'
      const params = {
        id: this.pluginId
      }
      const { data, status } = await req.get(api, { params })
      if (status === 'OK') {
        this.pluginItemDetail = data[0]
      } else {
        this.$Message.error(this.$t('p_request_fail'))
      }
      this.stepContent[0].title += this.pluginItemDetail.name + '_' + this.pluginItemDetail.version
    },
    async getOptionList() {
      const api = '/platform/v1/plugins/packages/version/get'
      const params = {
        id: this.pluginId
      }
      const res = await req.get(api, { params })
      if (res.status === 'OK') {
        this.inheritedVersionOptionList = res.data || []
      } else {
        this.inheritedVersionOptionList = []
        this.$Message.error(res.message)
      }
      this.availableHostList = (await getAvailableContainerHosts()).data || []
      const { status, data } = await queryStorageFilesByPackageId(this.pluginId)
      if (status === 'OK') {
        this.storageServiceData = data.map(_ => ({
          file: _[0],
          path: _[1],
          hash: _[2],
          uploadTime: _[3]
        }))
      }
      this.getAvailableInstances(this.pluginId)
    },
    returnPreviousPage() {
      this.$router.push({ path: '/collaboration/plugin-management' })
    },
    handleTabClick(name) {
      this.currentTabName = name
    },
    onFooterButtonClick(key) {
      this[this.buttonFunctionMap[key]]()
    },
    getAllServiceById() {
      return new Promise(resolve => {
        getPluginConfigsByPackageId(this.pluginId).then(res => {
          if (res.status === 'OK') {
            resolve(res.data)
          }
        })
      })
    },
    async enterNextStep() {
      if (this.currentStep === 1) {
        const data = await this.getAllServiceById()
        if (!data || isEmpty(data)) {
          this.currentStep += 1
          this.enterNextStep()
          return
        }
        if (!isEmpty(this.inheritedVersionOptionList)) {
          this.onInheritedVersionSelected(JSON.stringify(this.inheritedVersionOptionList[0]))
        }
      } else if (this.currentStep === 2) {
        if (!this.pluginItemDetail.menus || isEmpty(this.pluginItemDetail.menus)) {
          this.currentStep += 1
          this.enterNextStep()
          return
        }
        this.currentTabName = '1'
      } else if (this.currentStep === 3) {
        this.currentTabName = '6'
      }
      this.currentStep += 1
    },
    batchRegist() {
      this.$refs.pluginRegister.batchRegist()
    },
    async onInheritedVersionSelected(item) {
      if (item) {
        const versionObj = JSON.parse(item)
        const api = '/platform/v1/plugins/packages/version/inherit'
        const { status } = await req.post(api, {
          pluginPackageId: this.pluginId,
          inheritPackageId: versionObj.pluginPackageId
        })
        if (status === 'OK') {
          this.selectedVersion = versionObj.version
          this.$refs.pluginRegister.startRegister()
          this.batchRegist()
        } else {
          this.$Message.error(this.$t('p_execute_fail'))
        }
      }
    },
    onImportSuccess(response) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message
        })
        this.selectedVersion = this.$t('p_new_config')
        this.$refs.pluginRegister.startRegister()
        this.batchRegist()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message
        })
      }
      this.$refs.importXML.clearFiles()
    },
    onError(file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message
      })
    },
    async oldRegistItem() {
      const { status } = await registPluginPackage(this.pluginId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: this.$t('reload_to_get_ui')
        })
        this.enterNextStep()
      }
    },
    async registItem() {
      const api = '/platform/v1/packages/ui/register'
      const { status } = await req.post(api, {
        id: this.pluginId
      })
      if (status === 'OK') {
        this.$Message.success(this.$t('action_successful'))
        this.getAvailableInstances(this.pluginId)
        this.enterNextStep()
      } else {
        this.$Message.error(this.$t('p_execute_fail'))
      }
    },
    getPortByHostIp() {
      this.selectedIp.forEach(async ip => {
        const { data, status } = await getAvailablePortByHostIp(ip)
        if (status === 'OK') {
          this.allowCreationIpPort = []
          this.allowCreationIpPort.push({
            ip,
            port: data
          })
        }
      })
    },
    async createInstanceByIpPort(ip, port) {
      this.isSpinShow = true
      this.spinContent = this.$t('p_instance_creation')
      const timeId = setTimeout(() => {
        this.isSpinShow = false
        this.timeId = null
        this.$Message.error(this.$t('p_instance_creation_failed'))
      }, 180000)
      const { status } = await createPluginInstanceByPackageIdAndHostIp(this.pluginId, ip, port)
      if (status === 'OK') {
        this.isSpinShow = false
        clearTimeout(timeId)
        this.$Notice.success({
          title: 'Success',
          desc: 'Instance launched successfully'
        })
        const index = this.allowCreationIpPort.findIndex(item => item.port === port)
        this.allowCreationIpPort.splice(index, 1)
        this.getAvailableInstances(this.pluginId)
        this.selectedIp = []
      } else {
        this.isSpinShow = false
        clearTimeout(timeId)
      }
    },
    async getAvailableInstances(id) {
      const { data, status } = await getAvailableInstancesByPackageId(id)
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
        this.availableHostList = (await getAvailableContainerHosts()).data || []
        this.availableHostList = cloneDeep(this.availableHostList).filter(item => {
          const findItem = find(this.allInstances, {
            hostIp: item
          })
          return !findItem
        })
      }
    },
    async removePlugin(instanceId) {
      this.isSpinShow = true
      this.spinContent = this.$t('p_instance_destroy')
      const timeId = setTimeout(() => {
        this.isSpinShow = false
        this.timeId = null
        this.$Message.error(this.$t('p_instance_destroy_failed'))
      }, 180000)
      const { status, message } = await removePluginInstance(instanceId)
      this.isSpinShow = false
      clearTimeout(timeId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getAvailableInstances(this.pluginId)
        this.updateMenus()
      }
    },
    updateMenus() {
      this.$eventBusP.$emit('updateMenus')
    },
    async getDBTableData() {
      const payload = {
        sqlQuery: this.dbQueryCommandString,
        pageable: {
          pageSize: this.dbTablePagination.pageSize,
          startIndex: this.dbTablePagination.pageSize * (this.dbTablePagination.currentPage - 1)
        }
      }
      const { status, data } = await queryDataBaseByPackageId(this.pluginId, payload)
      if (status === 'OK') {
        this.dbTablePagination.total = data.pageInfo.totalRows
        this.dbQueryColumns = data.headers.map(_ => ({
          key: _,
          title: _,
          minWidth: 170
        }))
        this.dbQueryData = data.contents.map(_ => {
          const tempObj = {}
          _.forEach((i, index) => {
            tempObj[this.dbQueryColumns[index].key] = i
          })
          return tempObj
        })
      }
    },
    onDBTablePageChange(currentPage) {
      this.dbTablePagination.currentPage = currentPage
      this.getDBTableData()
    },
    onDBTablePageSizeChange(pageSize) {
      this.dbTablePagination.pageSize = pageSize
      this.getDBTableData()
    },
    async installFinish() {
      const api = '/platform/v1/packages/register-done'
      const { status } = await req.post(api, {
        id: this.pluginId
      })
      if (status === 'OK') {
        this.$Message.success(this.$t('p_sixth_step_title'))
        this.returnPreviousPage()
        this.reloadPage()
      }
    },
    reloadPage() {
      document.location.reload()
    },
    onServiceListGet(data = []) {
      if (!isEmpty(data)) {
        this.isServiceActionNotEmpty = false
        for (let i = 0; i < data.length; i++) {
          for (let j = 0; j < data[i].pluginConfigDtoList.length; j++) {
            if (data[i].pluginConfigDtoList[j].registerName) {
              this.isServiceActionNotEmpty = true
              return
            }
          }
        }
      } else {
        this.isServiceActionNotEmpty = false
      }
    },
    onRegisteSuccess() {
      this.inheritedVersion = this.selectedVersion
    }
  }
}
</script>

<style scoped lang="scss">
.all-page {
  .register-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 150px);
    .content-header {
      display: flex;
      .arrow-back {
        cursor: pointer;
        width: 28px;
        height: 24px;
        color: #fff;
        border-radius: 2px;
        background: #2d8cf0;
        margin-right: 12px;
        margin-bottom: 10px;
      }
    }
    .content-detail {
      display: flex;
      flex-direction: row;
      height: 75vh;
      .left-content {
        width: 30%;
        min-width: 30%;
        max-width: 30%;
        padding-right: 10px;
        margin-right: 10px;
        border-right: 1px solid #bbbbbb;
        .left-title {
          margin-left: 10px;
        }
      }
      .right-content {
        // flex: 1;
        .single-tab-content {
          height: 100%;
          max-height: calc(100vh - 280px);
          overflow-y: auto;
        }
        .right-title {
          margin-top: 5px;
          margin-left: 10px;
        }
        .service-list-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 5px 0px;
          width: 470px;
        }
        .instance-item-container {
          border-bottom: 1px solid gray;
          padding: 10px 0;
        }
      }
    }
    .use-underline-title {
      display: inline-block;
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 10px;
      .underline {
        display: block;
        margin-top: -10px;
        margin-left: -6px;
        width: 100%;
        padding: 0 6px;
        height: 12px;
        border-radius: 12px;
        background-color: #c6eafe;
        -webkit-box-sizing: content-box;
        box-sizing: content-box;
      }
    }
  }
  .footer-button {
    position: fixed;
    bottom: 10px;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid #e8eaec;
  }
}
</style>

<style lang="scss">
.footer-button {
  .ivu-btn-success {
    background-color: #b088f1;
    border-color: #b088f1;
  }
  .ivu-btn-success:hover {
    background-color: #b088f1;
    border-color: #b088f1;
  }
  .ivu-btn-warning {
    background-color: #70babc;
    border-color: #70babc;
  }
  .ivu-btn-warning:hover {
    background-color: #70babc;
    border-color: #70babc;
  }
}
.footer-button {
  .ivu-upload-list {
    display: none;
  }
}
</style>
