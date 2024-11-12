<template>
  <div>
    <Spin fix v-if="isSpinShow" style="z-index: 1000000">
      <Icon type="ios-loading" :size="25" class="spin-icon-load"></Icon>
      <div style="font-size: 20px">{{ spinContent }}</div>
    </Spin>
    <div class="search-top">
      <div class="search-top-left">
        <RadioGroup
          v-model="searchForm.running"
          style="width: 140px"
          type="button"
          button-style="solid"
          @on-change="onFilterConditionChange"
        >
          <Radio v-for="(item, index) in searchRadioGroupOptions" :key="index" :label="item.value">{{
            item.label
          }}</Radio>
        </RadioGroup>
        <Select
          v-model="searchForm.name"
          style="width: 30%"
          class="mr-2"
          :placeholder="$t('p_enter_plugin_name')"
          filterable
          clearable
          @on-change="onFilterConditionChange"
        >
          <Option v-for="name in searchNameOptionList" :value="name" :key="name">{{ name }}</Option>
        </Select>
        <Select
          v-model="searchForm.updatedBy"
          style="width: 30%"
          class="mr-2"
          :placeholder="$t('p_enter_updater')"
          filterable
          clearable
          @on-change="onFilterConditionChange"
        >
          <Option v-for="name in updatedByOptionList" :value="name" :key="name">{{ name }}</Option>
        </Select>
      </div>
      <div class="search-top-right">
        <Button type="primary" class="mr-2" @click="showDeletedPlugin">{{ $t('p_deleted_plugin') }}</Button>
        <Button type="info" class="mr-2" @click="showOnlinePluginSelect" ghost icon="ios-cloud-upload-outline">{{
          $t('origin_plugins')
        }}</Button>
        <div>
          <Upload
            ref="uploadButton"
            show-upload-list
            accept=".zip"
            name="zip-file"
            :on-success="onSuccess"
            :on-progress="onProgress"
            :on-error="onError"
            action="platform/v1/packages"
            :headers="uploadHeaders"
          >
            <Button type="info" ghost icon="ios-cloud-upload-outline">{{ $t('upload_plugin_btn') }}</Button>
          </Upload>
          <span v-if="showSuccess" style="color: #2b85e4">{{ $t('plugin_analysis') }}</span>
        </div>
      </div>
    </div>
    <div class="card-content">
      <!-- <div v-if="!dataList || dataList.length === 0" class="no-card-tips">{{ $t('noData') }}</div> -->
      <Row class="all-card-item" :gutter="8">
        <Col v-for="(item, index) in dataList" :span="8" :key="index" class="panal-list">
          <Card class="panal-list-card">
            <div slot="title" class="panal-title">
              <div class="panal-title-text">
                <h6>{{ item.name + '_' + item.version }}</h6>
              </div>
              <div>
                <span v-if="item.menus && item.menus.length">
                  <Tag color="green" v-if="item.uiActive">{{ $t('p_menu_in_effect') }}</Tag>
                  <Tag v-else>{{ $t('p_menu_not_working') }}</Tag>
                </span>
                <Tag v-if="item.instances.length === 0">{{ $t('p_background_no_running') }}</Tag>
                <Tag v-else color="green">{{ $t('p_background_running') }}</Tag>
              </div>
            </div>
            <div class="card-item-content mb-2">
              <div class="card-content-list mb-1" v-for="(keyItem, index) in cardContentList" :key="index">
                <span style="min-width: 80px">{{ $t(keyItem.label) }}: </span>
                <Tooltip v-if="keyItem.key === 'menus'" max-width="450" placement="left">
                  <div slot="content" v-html="getMenuText(item).length ? getMenuText(item).join('</br>') : '-'"></div>
                  <div class="card-menu-content">
                    {{ getMenuText(item).length ? getMenuText(item).join(';') : '-' }}
                  </div>
                </Tooltip>
                <div v-else-if="keyItem.key === 'instances'">
                  <div v-if="item[keyItem.key].length">
                    <div v-for="(instanceItem, index) in item[keyItem.key]" :key="index" class="instance-list">
                      <span class="mr-2">{{ instanceItem.address }}</span>
                    </div>
                  </div>
                  <div v-else>-</div>
                </div>
                <div v-else>{{ item[keyItem.key] ? item[keyItem.key] : '-' }}</div>
                <Tooltip class="card-action-button" max-width="650" placement="bottom">
                  <template #content>
                    <p>{{ $t('regist_plugin_tip1') }}</p>
                    <p>{{ $t('regist_plugin_tip2') }}</p>
                  </template>
                  <Button
                    :disabled="!item.registerDone || item.menus.length === 0"
                    size="small"
                    v-if="keyItem.buttonText && keyItem.key === 'menus'"
                    :type="!item.registerDone || item.menus.length === 0 ? 'default' : keyItem.buttonType"
                    @click="registPlugin(item.id)"
                  >
                    {{
                      typeof keyItem.buttonText === 'string'
                        ? $t(keyItem.buttonText)
                        : $t(keyItem.buttonText(item.status))
                    }}
                  </Button>
                </Tooltip>
                <Button
                  :disabled="!item.registerDone"
                  size="small"
                  @click="onCreateInstanceButtonClick(item)"
                  v-if="keyItem.buttonText && keyItem.key !== 'menus'"
                  class="card-action-button"
                  :type="!item.registerDone ? 'default' : keyItem.buttonType"
                >
                  <span v-html="$t(keyItem.buttonText)"></span>
                </Button>
              </div>
            </div>
            <div class="card-divider mb-2"></div>
            <div class="card-content-footer">
              <Tooltip :content="$t('p_continue_installation')" placement="top">
                <Button @click="startInstallPlugin(item.id)" size="small" v-if="!item.registerDone" type="success">
                  <Icon type="ios-play" />
                </Button>
              </Tooltip>
              <Tooltip :content="$t('plugin_config_check')" placement="top">
                <Button
                  type="primary"
                  size="small"
                  :disabled="isButtonDisabled(item)"
                  @click="enterSettingPage(item.id, 1)"
                >
                  <Icon type="ios-settings" />
                </Button>
              </Tooltip>

              <Upload
                ref="importXML"
                :action="'platform/v1/plugins/packages/import/' + item.id"
                name="xml-file"
                with-credentials
                :headers="uploadHeaders"
                :on-success="onImportSuccess"
                :on-error="onError"
                accept=".xml"
              >
                <Tooltip :content="$t('p_import_service')" placement="top">
                  <Button type="info" size="small" :disabled="isButtonDisabled(item)">
                    <Icon type="md-cloud-upload" />
                  </Button>
                </Tooltip>
              </Upload>
              <Tooltip :content="$t('p_export_service')" placement="top">
                <Button
                  type="info"
                  size="small"
                  @click.stop="exportPluginFile(item.id)"
                  :disabled="isButtonDisabled(item)"
                >
                  <Icon type="md-cloud-download" />
                </Button>
              </Tooltip>
              <Tooltip :content="$t('p_services_list')" placement="top">
                <Button
                  type="warning"
                  size="small"
                  :disabled="isButtonDisabled(item)"
                  @click="enterSettingPage(item.id, 2)"
                >
                  <Icon type="md-cube" />
                </Button>
              </Tooltip>
              <Poptip confirm :title="$t('p_delConfirm_tip')" placement="left-end" @on-ok="onDeleteCardConfirm(item)">
                <Tooltip :content="$t('p_delete_plugin')" placement="top">
                  <Button type="error" :disabled="item.instances.length !== 0" size="small">
                    <Icon type="md-trash" />
                  </Button>
                </Tooltip>
              </Poptip>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
    <!-- 新增实例 -->
    <Modal
      v-model="isAddInstanceModalShow"
      :title="$t('p_add_instance')"
      cancel-text=""
      :ok-text="$t('p_finish')"
      @on-ok="addInstanceConfirm"
      @on-visible-change="onAddInstanceModalChange"
    >
      <div>
        <div class="instance-select">
          <span class="required-star">*</span>
          <span class="ml-1 mr-3">{{ $t('instance') }}</span>
          <Select
            v-model="selectedIp"
            clearable
            filterable
            multiple
            style="width: 60%"
            :placeholder="$t('select_an_instance')"
          >
            <Option v-for="item in availableHostList" :value="item" :key="item">{{ item }}</Option>
          </Select>
          <Button type="success" @click="getPortByHostIp" class="ml-3" size="small">{{ $t('port_preview') }}</Button>
        </div>
        <div class="allow-add-port">
          <span style="width: 70px">{{ $t('p_allow-create') }}</span>
          <div v-if="allowCreationIpPort.length">
            <div class="allow-add-port-item" v-for="(item, index) in allowCreationIpPort" :key="index">
              {{ item.ip + ':' + item.port }}
              <Button type="success" class="ml-3" @click="createInstanceByIpPort(item.ip, item.port)" size="small">{{
                $t('p_create')
              }}</Button>
            </div>
          </div>
          <div v-else>-</div>
        </div>
        <div class="allow-add-port">
          <span style="width: 70px">{{ $t('running_node') }}</span>
          <div v-if="allRunningInstances.length">
            <div class="allow-add-port-item" v-for="(item, index) in allRunningInstances" :key="index">
              {{ item.displayLabel }}
              <Poptip confirm :title="$t('p_destroy_tips')" placement="left-end" @on-ok="destroyInstance(item.id)">
                <Button size="small" type="error" class="destroy-instance-button">{{ $t('ternmiante') }}</Button>
              </Poptip>
            </div>
          </div>
          <div v-else>-</div>
        </div>
      </div>
    </Modal>
    <!-- 已删除插件列表 -->
    <Modal
      v-model="isDeletedPluginModalShow"
      :title="$t('p_deleted_plugin')"
      :width="50"
      cancel-text=""
      :ok-text="$t('p_finish')"
      @on-ok="onDeletePluginModalChange(false)"
      @on-visible-change="onDeletePluginModalChange"
    >
      <div class="delete-plugin-list">
        <Select
          v-model="deletedSearchForm.name"
          class="mb-3"
          style="width: 50%; height: 80%"
          :placeholder="$t('p_enter_plugin_name')"
          filterable
          clearable
          @on-change="onFilterConditionChange"
        >
          <Option v-for="name in searchNameOptionList" :value="name" :key="name">{{ name }}</Option>
        </Select>

        <Table :columns="deletedPluginTableColumns" :data="deletedPluginList" :max-height="500"> </Table>
      </div>
    </Modal>
    <!-- 选择在线插件 -->
    <Modal
      :title="$t('origin_plugins')"
      v-model="isOnlinePluginSelectShow"
      @on-ok="onOnlinePluginSelectModalConfirm"
      @on-cancel="resetOnlinePluginSelectModal"
      @on-visible-change="onOnlinePluginSelectModalChange"
    >
      <Input v-model="filterOnlinePluginKeyWord" :placeholder="$t('search')" @on-change="filterOnlinePlugin" />
      <div class="online-plugin-list">
        <RadioGroup v-model="selectedOnlinePlugin" vertical>
          <template v-for="(item, index) in Object.keys(originPluginsGroupFilter)">
            <span :key="index" style="color: #999; line-height: 30px">{{ item }}</span>
            <Radio v-for="plugin in originPluginsGroupFilter[item]" :key="plugin.keyName" :label="plugin.keyName">
              <span>{{ plugin.keyName }}</span>
            </Radio>
          </template>
        </RadioGroup>
      </div>
    </Modal>
    <!-- 批量提交 -->
    <batch-regist-modal
      :pluginId="currentPluginId"
      :isBatchModalShow="isBatchModalShow"
      @close="onBatchRegistModalClose"
    />
  </div>
</template>

<script>
import Vue from 'vue'
import debounce from 'lodash/debounce'
import find from 'lodash/find'
import cloneDeep from 'lodash/cloneDeep'
import isEmpty from 'lodash/isEmpty'
import { getCookie } from '@/pages/util/cookie'
import req from '@/api/base'
import {
  getAvailableContainerHosts,
  getAvailablePortByHostIp,
  createPluginInstanceByPackageIdAndHostIp,
  removePluginInstance,
  getPluginArtifacts,
  pullPluginArtifact,
  getPluginArtifactStatus,
  deletePluginPkg,
  getAvailableInstancesByPackageId
} from '@/api/server.js'
import BatchRegistModal from './components/batch-register-modal.vue'

const initSearchForm = {
  running: 'yes',
  name: '',
  updatedBy: '',
  withDelete: 'no' // 枚举值，yes是删除列表，no是未删除
}

export default {
  name: '',
  components: {
    BatchRegistModal
  },
  data() {
    return {
      searchForm: cloneDeep(initSearchForm),
      deletedSearchForm: {
        withDelete: 'yes', // 枚举值，yes是删除列表，no是未删除
        name: ''
      },
      searchRadioGroupOptions: [
        {
          label: this.$t('all'),
          value: 'all'
        },
        {
          label: this.$t('p_running'),
          value: 'yes'
        }
      ],
      showSuccess: false,
      dataList: [],
      cardContentList: [
        {
          key: 'menus',
          label: 'p_running_menus',
          buttonText() {
            // return val === 'UNREGISTERED' ? 'regist' : 'p_register_again'
            return 'regist'
          },
          buttonType: 'primary'
        },
        {
          key: 'instances',
          label: 'p_running_instance',
          buttonText: 'p_manage',
          buttonType: 'success'
        },
        {
          key: 'updatedBy',
          label: 'updatedBy'
        },
        {
          key: 'updatedTime',
          label: 'table_updated_date'
        }
      ],
      isAddInstanceModalShow: false,
      selectedIp: [],
      allowCreationIpPort: [],
      availableHostList: [],
      currentPluginId: '',
      isDeletedPluginModalShow: false,
      deletedPluginList: [],
      pluginListType: '',
      selectedOnlinePlugin: '',
      originPlugins: [],
      originPluginsGroupFilter: {},
      filterOnlinePluginKeyWord: '',
      isOnlinePluginSelectShow: false,
      pluginTimer: null,
      uploadHeaders: {
        Authorization: 'Bearer ' + getCookie('accessToken')
      },
      isBatchModalShow: false,
      allRunningInstances: [],
      searchNameOptionList: [],
      updatedByOptionList: [],
      deletedPluginTableColumns: [
        {
          title: this.$t('p_plugin_name'),
          width: 200,
          key: 'name'
        },
        {
          title: this.$t('version'),
          width: 130,
          key: 'version'
        },
        {
          title: this.$t('updatedBy'),
          width: 130,
          key: 'updatedBy'
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime'
        }
      ],
      isSpinShow: false,
      spinContent: ''
    }
  },
  async mounted() {
    if (window.pluginRegistrationListSearchForm) {
      this.searchForm = window.pluginRegistrationListSearchForm
    }
    await this.getViewList()
    this.getUpdatedByOptionList()
  },
  methods: {
    onFilterConditionChange: debounce(async function () {
      await this.getViewList()
    }, 300),
    async getViewList() {
      return new Promise(resolve => {
        const api = '/platform/v1/packages'
        let params = cloneDeep(this.searchForm)
        if (this.pluginListType === 'isDeleted') {
          params = cloneDeep(this.deletedSearchForm)
        }
        req.get(api, { params }).then(res => {
          this.processOptionList(res.data, this.searchNameOptionList, 'name')
          if (this.pluginListType === 'isDeleted') {
            this.deletedPluginList = res.data || []
          } else {
            this.dataList = res.data || []
          }
          resolve(res.data)
        })
      })
    },
    async getUpdatedByOptionList() {
      const api = '/platform/v1/users/retrieve'
      const { data } = await req.get(api)
      this.processOptionList(data, this.updatedByOptionList, 'username')
    },
    processOptionList(data = [], needFillArray = [], key = 'name') {
      if (!isEmpty(data)) {
        data.forEach(item => {
          if (!needFillArray.includes(item[key])) {
            needFillArray.push(item[key])
          }
        })
      } else {
        // eslint-disable-next-line
        needFillArray = []
      }
    },
    async showDeletedPlugin() {
      this.deletedSearchForm.name = ''
      this.pluginListType = 'isDeleted'
      await this.getViewList()
      this.isDeletedPluginModalShow = true
    },
    async onSuccess(response) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })
        this.startInstallPlugin(response.data.id)
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message || ''
        })
      }
      document.querySelector('.ivu-upload-list').style.display = 'none'
      this.showSuccess = false
    },
    onProgress(event) {
      if (event.percent === 100) {
        this.showSuccess = true
      }
    },
    onError(file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    },
    async onDeleteCardConfirm(item) {
      const { status } = await deletePluginPkg(item.id)
      if (status === 'OK') {
        await this.getViewList()
      }
    },
    async onCreateInstanceButtonClick(item) {
      this.availableHostList = []
      const res = await getAvailableContainerHosts()
      this.availableHostList = res.data ? res.data : []
      this.currentPluginId = item.id
      await this.getAvailableInstancesByPackageId(this.currentPluginId)
      this.availableHostList = cloneDeep(this.availableHostList).filter(item => {
        const findItem = find(this.allRunningInstances, {
          hostIp: item
        })
        return !findItem
      })
      this.isAddInstanceModalShow = true
    },
    addInstanceConfirm() {
      this.resetAddInstanceForm()
    },
    resetAddInstanceForm() {
      this.isAddInstanceModalShow = false
      this.selectedIp = []
      this.allowCreationIpPort = []
      this.allRunningInstances = []
    },
    async onAddInstanceModalChange(state) {
      if (!state) {
        await this.getViewList()
        this.resetAddInstanceForm()
      }
    },
    async destroyInstance(instanceId) {
      this.isSpinShow = true
      this.spinContent = this.$t('p_instance_destroy')
      let timeId = setTimeout(() => {
        this.isSpinShow = false
        timeId = null
        this.$Message.error(this.$t('p_instance_destroy_failed'))
      }, 180000)

      const { status, message } = await removePluginInstance(instanceId)
      this.allRunningInstances = cloneDeep(this.allRunningInstances).filter(item => item.id !== instanceId)
      this.isSpinShow = false
      clearTimeout(timeId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.reloadPage()
      }
    },
    async getPortByHostIp() {
      const ipMap = {}
      const promiseArray = []
      this.selectedIp.forEach(async ip => {
        promiseArray.push(getAvailablePortByHostIp(ip))
        ipMap[ip] = promiseArray.length - 1
      })
      const finallArray = await Promise.all(promiseArray)
      this.allowCreationIpPort = []
      for (const key in ipMap) {
        if (finallArray[ipMap[key]].data) {
          this.allowCreationIpPort.push({
            ip: key,
            port: finallArray[ipMap[key]].data
          })
        }
      }
    },
    async createInstanceByIpPort(ip, port) {
      this.isSpinShow = true
      this.spinContent = this.$t('p_instance_creation')
      let timeId = setTimeout(() => {
        this.isSpinShow = false
        timeId = null
        this.$Message.error(this.$t('p_instance_creation_failed'))
      }, 180000)
      const { status } = await createPluginInstanceByPackageIdAndHostIp(this.currentPluginId, ip, port)
      if (status === 'OK') {
        this.isSpinShow = false
        clearTimeout(timeId)
        this.$Notice.success({
          title: 'Success',
          desc: 'Instance launched successfully'
        })
        const index = this.allowCreationIpPort.findIndex(item => item.port === port)
        this.allowCreationIpPort.splice(index, 1)
        await this.getAvailableInstancesByPackageId(this.currentPluginId)
        this.reloadPage()
      } else {
        this.isSpinShow = false
        clearTimeout(timeId)
      }
    },
    reloadPage() {
      this.$Notice.info({
        title: this.$t('notify'),
        desc: this.$t('reload_notify')
      })
      setTimeout(() => {
        document.location.reload()
      }, 3000)
    },
    async registPlugin(pluginId) {
      this.isSpinShow = true
      this.spinContent = this.$t('plugin_registing')
      const api = '/platform/v1/packages/ui/register'
      const { status } = await req.post(api, {
        id: pluginId
      })
      this.isSpinShow = false
      if (status === 'OK') {
        this.$Message.success(this.$t('action_successful'))
        this.reloadPage()
      } else {
        this.$Message.error(this.$t('p_execute_fail'))
      }
    },
    resetDeletePluginModal() {
      this.isDeletedPluginModalShow = false
      this.deletedPluginList = []
      this.pluginListType = ''
    },
    async onDeletePluginModalChange(state) {
      if (!state) {
        this.resetDeletePluginModal()
        await this.getViewList()
      }
    },
    async showOnlinePluginSelect() {
      const { status, data } = await getPluginArtifacts()
      if (status === 'OK') {
        this.originPlugins = data
        this.originPluginsGroupFilter = {}
        data.forEach(item => {
          if (item.keyName.split('-v')[0] in this.originPluginsGroupFilter) {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]].push(item)
          } else {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]] = [item]
          }
        })
        this.isOnlinePluginSelectShow = true
      }
    },
    filterOnlinePlugin(e) {
      const filterKey = e.target.value
      this.originPluginsGroupFilter = {}
      this.originPlugins
        .filter(item => item.keyName.indexOf(filterKey) > -1)
        .forEach(item => {
          if (item.keyName.split('-v')[0] in this.originPluginsGroupFilter) {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]].push(item)
          } else {
            this.originPluginsGroupFilter[item.keyName.split('-v')[0]] = [item]
          }
        })
    },
    async onOnlinePluginSelectModalConfirm() {
      this.isSpinShow = true
      this.spinContent = this.$t('p_online_plugin_installation')
      let timeId = setTimeout(() => {
        this.isSpinShow = false
        timeId = null
        this.$Message.error(this.$t('p_instance_destroy_failed'))
      }, 180000)
      const payload = {
        keyName: this.selectedOnlinePlugin
      }
      const res = await pullPluginArtifact(payload) // getPluginArtifactStatus
      if (res.status === 'OK') {
        this.resetOnlinePluginSelectModal()
        this.$nextTick(() => {
          this.pluginTimer = setInterval(async () => {
            const { status, data } = await getPluginArtifactStatus(res.data.requestId)
            if (status !== 'OK' || data.state !== 'InProgress') {
              this.isSpinShow = false
              clearTimeout(timeId)
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
            }
            if (status === 'OK' && data.state !== 'InProgress') {
              this.isSpinShow = false
              clearTimeout(timeId)
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
              this.$Notice.info({
                title: 'Notification',
                desc: data.state
              })
              if (data.state === 'Completed') {
                this.resetOnlinePluginSelectModal()
                this.startInstallPlugin(data.pluginPackageId)
              }
            }
          }, 5000)
        })
      }
    },
    resetOnlinePluginSelectModal() {
      this.selectedOnlinePlugin = ''
      this.originPlugins = []
      this.originPluginsGroupFilter = {}
      this.filterOnlinePluginKeyWord = ''
      this.isOnlinePluginSelectShow = false
    },
    onOnlinePluginSelectModalChange(state) {
      if (!state) {
        this.resetOnlinePluginSelectModal()
      }
    },
    async onImportSuccess(response) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message
        })
        await this.getViewList()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message
        })
      }
    },
    exportPluginFile(pluginId) {
      this.currentPluginId = pluginId
      this.isBatchModalShow = true
    },
    saveSearchForm() {
      window.pluginRegistrationListSearchForm = this.searchForm
    },
    startInstallPlugin(pluginId) {
      this.saveSearchForm()
      this.$router.push({
        path: '/collaboration/registrationDetail',
        query: { pluginId }
      })
    },
    enterSettingPage(pluginId, step) {
      this.saveSearchForm()
      this.$router.push({
        path: '/collaboration/registrationDetail',
        query: {
          pluginId,
          step
        }
      })
    },
    onBatchRegistModalClose() {
      this.isBatchModalShow = false
    },
    async getAvailableInstancesByPackageId(id) {
      return new Promise(resolve => {
        getAvailableInstancesByPackageId(id).then(res => {
          if (res.status === 'OK') {
            this.allRunningInstances = res.data.map(_ => {
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
          resolve(this.allRunningInstances)
        })
      })
    },
    isButtonDisabled(item) {
      if (item.registerDone) {
        return false
      }
      return !(item.instances.length && item.uiActive && item.menus.length)
    },
    getMenuText(item) {
      if (Vue.config.lang === 'zh-CN') {
        return item.localMenus && item.localMenus.length ? item.localMenus : []
      }

      return item.menus && item.menus.length ? item.menus : []
    }
  }
}
</script>

<style scoped lang="scss">
.spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
@keyframes ani-demo-spin {
  from {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.search-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  .search-top-left {
    display: flex;
    width: 50%;
    flex-direction: row;
    .ivu-radio-wrapper-checked {
      background-color: #2d8cf0;
      color: #fff;
    }
  }

  .search-top-right {
    display: flex;
    flex-direction: row;
  }
}

.card-content {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
  width: 100%;
  .no-card-tips {
    display: flex;
    justify-content: center;
    margin-top: 50px;
  }
  .all-card-item {
    width: 100%;
    .panal-list {
      margin-bottom: 10px;
      .panal-list-card {
        min-height: 275px;
        .panal-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          .panal-title-text {
            font-weight: bold;
            padding: 8px 25px;
            background-color: #f7f7f7;
            border-color: #f7f7f7;
            color: #364253;
          }
        }
        .card-item-content {
          min-height: 145px;
          .card-content-list {
            display: flex;
            align-items: flex-start;
            .instance-list {
              display: flex;
              align-items: center;
              justify-content: space-between;
              margin-bottom: 5px;
            }
            .card-action-button {
              margin-left: auto;
            }
            .card-menu-content {
              max-height: 21px;
              overflow: hidden;
              max-width: calc(30vw - 150px);
              white-space: nowrap;
              text-overflow: ellipsis;
            }
          }
        }
        .card-item-content .card-content-list:nth-child(4) {
          margin-top: 10px;
        }
        .card-divider {
          height: 1px;
          width: 100%;
          background-color: #e8eaec;
        }
        .card-content-footer {
          margin-right: -5px;
          display: flex;
          flex-direction: row;
          justify-content: flex-end;
        }
        .card-content-footer button {
          margin-right: 5px;
        }
      }
    }
  }
}

.instance-select {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 10px;
  .required-star {
    color: #ed4014;
  }
}
.allow-add-port {
  display: flex;
  flex-direction: row;
  margin-top: 10px;
  .allow-add-port-item {
    width: 200px;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }
}

.delete-plugin-list {
  .plugin-text-all {
    height: 500px;
    overflow: scroll;
  }
  .delete-plugin-item {
    width: 50%;
    padding: 5px;
    border: 1px solid #dcdee2;
  }
}

.online-plugin-list {
  width: 100%;
  height: 350px;
  overflow: auto;
  margin-top: 10px;
}

.search-top {
  ::-webkit-scrollbar {
    display: none;
  }
}
</style>

<style lang="scss">
.search-top-right {
  .ivu-btn-primary {
    background-color: #a6adb2;
    border-color: #a6adb2;
  }
  .ivu-btn-primary:hover {
    background-color: #a6adb2;
    border-color: #a6adb2;
  }
}
.card-content-list {
  .card-action-button {
    .ivu-btn-primary {
      background-color: #b088f1;
      border-color: #b088f1;
    }
  }
  .destroy-instance-button.ivu-btn-info {
    background-color: #d06c4c;
    border-color: #d06c4c;
  }
}

.delete-plugin-list {
  .ivu-input-wrapper {
    margin-bottom: 10px;
  }
}

.card-content-footer {
  .ivu-upload-list {
    display: none;
  }
}

.panal-title {
  .ivu-tag-default {
    .ivu-tag-text {
      // color: #e8eaec
    }
  }
}
</style>
