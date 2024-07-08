<template>
  <div>
    <div class="search-top">
      <div class="search-top-left">
        <RadioGroup
          v-model="searchForm.running"
          style="width: 20%"
          type="button"
          button-style="solid"
          @on-change="onFilterConditionChange"
        >
          <Radio v-for="(item, index) in searchRadioGroupOptions" :key="index" :label="item.value">{{
            item.label
          }}</Radio>
        </RadioGroup>
        <Input
          v-model="searchForm.name"
          style="width: 30%"
          class="mr-2"
          type="text"
          :placeholder="$t('p_enter_plugin_name')"
          clearable
          @on-change="onFilterConditionChange"
        >
        </Input>
        <Input
          v-model="searchForm.updatedBy"
          style="width: 30%"
          type="text"
          :placeholder="$t('p_enter_updater')"
          clearable
          @on-change="onFilterConditionChange"
        >
        </Input>
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
      <div v-if="!dataList || dataList.length === 0" class="no-card-tips">{{ $t('noData') }}</div>
      <Row class="all-card-item" :gutter="8" v-else>
        <Col v-for="(item, index) in dataList" :span="8" :key="index" class="panal-list">
          <Card class="panal-list-card">
            <div slot="title" class="panal-title">
              <div class="panal-title-text">{{ item.name + '_' + item.version }}</div>
              <Tag v-if="item.instances.length === 0">{{ $t('p_no_instance') }}</Tag>
              <Tag v-else color="green">{{ $t('p_running') }}</Tag>
            </div>
            <div class="card-item-content mb-2">
              <div class="card-content-list mb-1" v-for="(keyItem, index) in cardContentList" :key="index">
                <span style="min-width: 80px">{{ $t(keyItem.label) }}: </span>
                <div v-if="keyItem.key === 'menus'" class="card-menu-content">
                  {{ item[keyItem.key].join(';') }}
                </div>
                <div v-else-if="keyItem.key === 'instances'">
                  <div v-for="(instanceItem, index) in item[keyItem.key]" :key="index" class="instance-list">
                    <span class="mr-2">{{ instanceItem.address }}</span>
                    <!-- <Poptip
                        confirm
                        :title="$t('p_delConfirm_tip')"
                        placement="left-end"
                        @on-ok="destroyInstance(instanceItem.id)">
                        <Button size="small" type="info" class="destroy-instance-button">{{$t('ternmiante')}}</Button>
                      </Poptip> -->
                  </div>
                </div>
                <div v-else>{{ item[keyItem.key] }}</div>
                <Tooltip class="card-action-button" max-width="650" placement="left">
                  <template #content>
                    <p>{{ $t('regist_plugin_tip1') }}</p>
                    <p>{{ $t('regist_plugin_tip2') }}</p>
                  </template>
                  <Button
                    :disabled="!item.registerDone || isPluginRegistering"
                    v-if="keyItem.buttonText && keyItem.key === 'menus'"
                    :type="!item.registerDone ? 'default' : keyItem.buttonType"
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
                  @click="onCreateInstanceButtonClick(item)"
                  v-if="keyItem.buttonText && keyItem.key !== 'menus'"
                  class="card-action-button"
                  :type="!item.registerDone ? 'default' : keyItem.buttonType"
                >
                  {{ $t(keyItem.buttonText) }}
                </Button>
              </div>
            </div>
            <div class="card-divider mb-2"></div>
            <div class="card-content-footer">
              <Button @click="startInstallPlugin(item.id)" size="small" v-if="!item.registerDone" type="success">
                <Icon type="ios-play" />
              </Button>
              <Button type="primary" size="small" :disabled="!item.registerDone" @click="enterSettingPage(item.id, 1)">
                <Icon type="ios-settings" />
              </Button>
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
                <Button type="info" size="small" :disabled="!item.registerDone">
                  <Icon type="md-cloud-download" />
                </Button>
              </Upload>
              <Button type="info" size="small" @click.stop="exportPluginFile(item.id)" :disabled="!item.registerDone">
                <Icon type="md-cloud-upload" />
              </Button>
              <Button type="warning" size="small" :disabled="!item.registerDone" @click="enterSettingPage(item.id, 2)">
                <Icon type="md-cube" />
              </Button>
              <Poptip confirm :title="$t('p_delConfirm_tip')" placement="left-end" @on-ok="onDeleteCardConfirm(item)">
                <Button type="error" size="small">
                  <Icon type="md-trash" />
                </Button>
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
              <Poptip confirm :title="$t('p_delConfirm_tip')" placement="left-end" @on-ok="destroyInstance(item.id)">
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
      cancel-text=""
      :ok-text="$t('p_finish')"
      @on-ok="onDeletePluginModalChange(false)"
      @on-visible-change="onDeletePluginModalChange"
    >
      <div class="delete-plugin-list">
        <Input
          v-model="searchForm.deleteSearchName"
          style="width: 50%"
          type="text"
          :placeholder="$t('p_enter_plugin_name')"
          clearable
          @on-change="onFilterConditionChange"
        >
        </Input>
        <div class="plugin-text-all">
          <div v-for="(item, index) in deletedPluginList" class="delete-plugin-item" :key="index">
            {{ item.name + '_' + item.version + (item.edition === 'enterprise' ? ' [ enterprise ]' : '') }}
          </div>
        </div>
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
import debounce from 'lodash/debounce'
import cloneDeep from 'lodash/cloneDeep'
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
  running: 'all',
  name: '',
  updatedBy: '',
  withDelete: 'no', // 枚举值，yes是删除列表，no是未删除
  deleteSearchName: ''
}

export default {
  name: '',
  components: {
    BatchRegistModal
  },
  data () {
    return {
      searchForm: cloneDeep(initSearchForm),
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
          label: 'menus',
          buttonText: function (val) {
            return val === 'UNREGISTERED' ? 'regist' : 'p_register_again'
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
      isPluginRegistering: false,
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
      allRunningInstances: []
    }
  },
  computed: {},
  mounted () {
    this.getViewList()
  },
  methods: {
    onFilterConditionChange: debounce(function () {
      this.getViewList()
    }, 300),
    async getViewList () {
      const api = '/platform/v1/packages'
      const params = cloneDeep(this.searchForm)
      if (this.pluginListType === 'isDeleted') {
        params.withDelete = 'yes'
        params.name = params.deleteSearchName
      } else {
        params.withDelete = 'no'
      }
      delete params.deleteSearchName
      const { data } = await req.get(api, { params })
      if (this.pluginListType === 'isDeleted') {
        this.deletedPluginList = data || []
      } else {
        this.dataList = data || []
      }
    },
    showDeletedPlugin () {
      this.resetDeletePluginModal()
      this.pluginListType = 'isDeleted'
      this.getViewList()
      this.isDeletedPluginModalShow = true
    },
    async onSuccess (response) {
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
      document.querySelector('.ivu-upload-list').style.display = 'none'
      this.showSuccess = false
    },
    onProgress (event) {
      if (event.percent === 100) {
        this.showSuccess = true
      }
    },
    onError (file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    },
    async onDeleteCardConfirm (item) {
      let { status } = await deletePluginPkg(item.id)
      if (status === 'OK') {
        this.getViewList()
      }
    },
    async onCreateInstanceButtonClick (item) {
      this.availableHostList = []
      const res = await getAvailableContainerHosts()
      this.availableHostList = res.data ? res.data : []
      this.currentPluginId = item.id
      this.getAvailableInstancesByPackageId(this.currentPluginId)
      this.isAddInstanceModalShow = true
    },
    addInstanceConfirm () {
      this.resetAddInstanceForm()
    },
    resetAddInstanceForm () {
      this.isAddInstanceModalShow = false
      this.selectedIp = []
      this.allowCreationIpPort = []
      this.allRunningInstances = []
    },
    onAddInstanceModalChange (state) {
      if (!state) {
        this.getViewList()
        this.resetAddInstanceForm()
      }
    },
    async destroyInstance (instanceId) {
      let { status, message } = await removePluginInstance(instanceId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.reloadPage()
      }
    },
    getPortByHostIp () {
      this.allowCreationIpPort = []
      this.selectedIp.forEach(async ip => {
        const { data, status } = await getAvailablePortByHostIp(ip)
        if (status === 'OK') {
          this.allowCreationIpPort.push({
            ip,
            port: data
          })
        }
      })
    },
    async createInstanceByIpPort (ip, port) {
      this.$Notice.info({
        title: 'Info',
        desc: 'Start Launching... It will take sometime.'
      })
      const { status } = await createPluginInstanceByPackageIdAndHostIp(this.currentPluginId, ip, port)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Instance launched successfully'
        })
        const index = this.allowCreationIpPort.findIndex(item => item.port === port)
        this.allowCreationIpPort.splice(index, 1)
        this.getAvailableInstancesByPackageId(this.currentPluginId)
        this.reloadPage()
      }
    },
    reloadPage () {
      this.$Notice.info({
        title: this.$t('notify'),
        desc: this.$t('reload_notify')
      })
      setTimeout(() => {
        document.location.reload()
      }, 3000)
    },
    async registPlugin (pluginId) {
      this.isPluginRegistering = true
      const api = '/platform/v1/packages/ui/register'
      const { status } = await req.post(api, {
        id: pluginId
      })
      this.isPluginRegistering = false
      if (status === 'OK') {
        this.$Message.success(this.$t('action_successful'))
        this.getViewList()
      } else {
        this.$Message.error(this.$t('p_execute_fail'))
      }
    },
    resetDeletePluginModal () {
      this.isDeletedPluginModalShow = false
      this.deletedPluginList = []
      this.pluginListType = ''
      this.searchForm = cloneDeep(initSearchForm)
    },
    onDeletePluginModalChange (state) {
      if (!state) {
        this.resetDeletePluginModal()
        this.getViewList()
      }
    },
    async showOnlinePluginSelect () {
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
    filterOnlinePlugin (e) {
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
    async onOnlinePluginSelectModalConfirm () {
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
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
            }
            if (status === 'OK' && data.state !== 'InProgress') {
              clearInterval(this.pluginTimer)
              this.pluginTimer = null
              this.$Notice.info({
                title: 'Notification',
                desc: data.state
              })
              if (data.state === 'Completed') {
                this.resetOnlinePluginSelectModal()
                // this.getViewList();
                this.startInstallPlugin(res.data.requestId)
              }
            }
          }, 5000)
        })
      }
    },
    resetOnlinePluginSelectModal () {
      this.selectedOnlinePlugin = ''
      this.originPlugins = []
      this.originPluginsGroupFilter = {}
      this.filterOnlinePluginKeyWord = ''
      this.isOnlinePluginSelectShow = false
    },
    onOnlinePluginSelectModalChange (state) {
      if (!state) {
        this.resetOnlinePluginSelectModal()
      }
    },
    onImportSuccess (response) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message
        })
        this.getViewList()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message
        })
      }
      this.$refs.importXML.clearFiles()
    },
    exportPluginFile (pluginId) {
      debugger
      debugger
      this.currentPluginId = pluginId
      this.isBatchModalShow = true
    },
    startInstallPlugin (pluginId) {
      this.$router.push({ path: '/collaboration/registrationDetail', query: { pluginId } })
    },
    enterSettingPage (pluginId, step) {
      this.$router.push({
        path: '/collaboration/registrationDetail',
        query: {
          pluginId,
          step
        }
      })
    },
    onBatchRegistModalClose () {
      this.isBatchModalShow = false
    },
    async getAvailableInstancesByPackageId (id) {
      let { data, status } = await getAvailableInstancesByPackageId(id)
      if (status === 'OK') {
        this.allRunningInstances = data.map(_ => {
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
    }
  }
}
</script>

<style scoped lang="less">
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
  .no-card-tips {
    display: flex;
    justify-content: center;
    margin-top: 50px;
  }
  .all-card-item {
    .panal-list {
      margin-bottom: 10px;
      .panal-list-card {
        min-height: 275px;
        .panal-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          .panal-title-text {
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
              max-height: 63px;
              overflow: scroll;
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
          margin-right: -10px;
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

::-webkit-scrollbar {
  display: none;
}
</style>

<style lang="less">
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
</style>
