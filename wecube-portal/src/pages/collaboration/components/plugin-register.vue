<template>
  <div class="plugin-register-page">
    <Row>
      <Col span="6" style="border-right: 1px solid #e8eaec;">
        <div style="height: calc(100vh - 180px);overflow-y:auto;">
          <div v-if="plugins.length < 1">{{ $t('no_plugin') }}</div>
          <div style="">
            <Menu theme="light" :active-name="currentPlugin" @on-select="selectPlugin" style="width: 100%;z-index:10">
              <Submenu
                v-for="(plugin, index) in plugins"
                :name="plugin.pluginConfigName"
                style="padding: 0;"
                :key="index"
              >
                <template slot="title">
                  <Icon type="md-grid" />
                  <span style="font-size: 15px;">{{ plugin.pluginConfigName }}</span>
                  <div style="float:right;color: #2d8cf0;margin-right:30px">
                    <Tooltip :content="$t('add')" :delay="1000">
                      <Icon @click.stop.prevent="addPluginConfigDto(plugin)" style="" type="md-add" />
                    </Tooltip>
                  </div>
                </template>
                <MenuItem
                  v-for="(dto, index) in plugin.pluginConfigDtoList.filter(dto => dto.registerName)"
                  :name="dto.id"
                  :key="index"
                  style="padding: 5px 30px;"
                >
                  <span
                    style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;font-size: 15px; font-weight:400"
                    >{{ dto.registerName }}</span
                  >
                  <div style="vertical-align: top;display: inline-block;float: right;">
                    <Tooltip :content="$t('copy')" :delay="500">
                      <Icon
                        size="16"
                        style="color: #19be6b;"
                        @click.stop.prevent="copyPluginConfigDto(dto.id)"
                        type="md-copy"
                      />
                    </Tooltip>
                    <Tooltip :content="$t('config_permission')" :delay="500">
                      <Icon size="16" style="color: #2db7f5;" @click="permissionsHandler(dto)" type="md-contacts" />
                    </Tooltip>
                  </div>
                </MenuItem>
              </Submenu>
            </Menu>
          </div>
        </div>
        <div style="padding-right: 20px;margin-top: 10px;">
          <Button type="info" long ghost @click="batchRegist">{{ $t('batch_regist') }}</Button>
        </div>
      </Col>
      <Col span="18" offset="0" style="padding-left: 10px">
        <Spin size="large" fix style="margin-top: 200px;" v-show="isLoading">
          <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
          <div>{{ $t('loading') }}</div>
        </Spin>
        <Form :model="form" v-if="hidePanal">
          <Row style="border-bottom: 1px solid #bbb7b7; margin-top: 20px">
            <Col span="12" offset="0">
              <FormItem :label-width="100" :label="$t('regist_name')">
                <Input v-model="registerName" ref="registerName" :disabled="currentPluginObj.status === 'ENABLED'" />
              </FormItem>
            </Col>
            <Col span="12" v-if="hidePanal" offset="0">
              <FormItem :label-width="100" :label="$t('target_type')">
                <FilterRules
                  v-model="selectedEntityType"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  :allDataModelsWithAttrs="allEntityType"
                ></FilterRules>
              </FormItem>
            </Col>
          </Row>
          <div style="height: calc(100vh - 300px);overflow:auto" id="paramsContainer">
            <Collapse v-model="activePanel" accordion>
              <Panel
                v-for="(inter, index) in currentPluginObj.interfaces"
                :key="index + inter.action"
                :name="index + inter.action"
              >
                <Input
                  :value="inter.action"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  @on-blur="actionBlurHandler($event, inter)"
                  @click.stop.native="actionFocus($event)"
                  style="width:200px"
                  size="small"
                />
                <InterfaceFilterRule
                  v-model="inter.filterRule"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  :rootEntity="rootEntity"
                  :allDataModelsWithAttrs="allEntityType"
                ></InterfaceFilterRule>
                <Button
                  size="small"
                  type="success"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  ghost
                  icon="md-copy"
                  @click.stop.prevent="copyInterface(inter)"
                  >{{ $t('copy') }}</Button
                >
                <Tooltip :content="$t('completely_deleted')" placement="top">
                  <Button
                    size="small"
                    type="error"
                    :disabled="currentPluginObj.status === 'ENABLED'"
                    ghost
                    icon="ios-trash-outline"
                    @click.stop.prevent="deleteInterface(index)"
                    >{{ $t('remove') }}</Button
                  >
                </Tooltip>
                <div slot="content">
                  <Row style="border-bottom: 1px solid gray;margin-bottom:5px">
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('params_type') }}</strong>
                    </Col>
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('params_name') }}</strong>
                    </Col>
                    <Col span="3" offset="0" style="text-align: center">
                      <strong style="font-size:15px;">{{ $t('data_type') }}</strong>
                    </Col>
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('sensitive') }}</strong>
                    </Col>
                    <Col span="8" offset="0">
                      <strong style="font-size:15px;">{{ $t('attribute') }}</strong>
                    </Col>
                    <Col span="3" offset="1">
                      <strong style="font-size:15px;">
                        {{ $t('attribute_type') }}
                      </strong>
                    </Col>
                  </Row>
                  <div class="interfaceContainers">
                    <Row>
                      <Col span="3">
                        <FormItem :label-width="0">
                          <span>{{ $t('input_params') }}</span>
                        </FormItem>
                      </Col>
                      <Col span="21" offset="0">
                        <Row v-for="(param, index) in inter['inputParameters']" :key="index">
                          <Col span="5">
                            <FormItem :label-width="0">
                              <Tooltip :content="param.name" style="width: 100%">
                                <div>
                                  <span v-if="param.required === 'Y'" style="color:red">*</span>
                                  <span
                                    style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                                    >{{ param.name }}</span
                                  >
                                </div>
                              </Tooltip>
                            </FormItem>
                          </Col>
                          <Col span="2" offset="0">
                            <FormItem :label-width="0">
                              <span>{{ param.dataType }}</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="0">
                            <FormItem :label-width="0">
                              <Select
                                v-model="param.sensitiveData"
                                filterable
                                size="small"
                                style="width:50px"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                              >
                                <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                                  item.label
                                }}</Option>
                              </Select>
                            </FormItem>
                          </Col>
                          <Col span="10" offset="0">
                            <FormItem :label-width="0">
                              <FilterRules
                                v-if="param.mappingType === 'entity'"
                                v-model="param.mappingEntityExpression"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                :allDataModelsWithAttrs="allEntityType"
                                :rootEntity="clearedEntityType"
                                :needNativeAttr="true"
                                :needAttr="true"
                              ></FilterRules>
                              <Select
                                filterable
                                v-if="param.mappingType === 'system_variable'"
                                v-model="param.mappingSystemVariableName"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                @on-open-change="retrieveSystemVariables"
                              >
                                <Option
                                  v-for="(item, index) in allSystemVariables"
                                  v-if="item.status === 'active'"
                                  :value="item.name"
                                  :key="index"
                                  >{{ item.name }}</Option
                                >
                              </Select>
                              <span v-if="param.mappingType === 'context' || param.mappingType === 'constant'"
                                >N/A</span
                              >
                            </FormItem>
                          </Col>
                          <Col span="3" offset="1">
                            <FormItem :label-width="0">
                              <Select
                                size="small"
                                filterable
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                v-model="param.mappingType"
                                @on-change="mappingTypeChange($event, param)"
                              >
                                <Option value="context" key="context">context</Option>
                                <Option value="system_variable" key="system_variable">system_variable</Option>
                                <Option value="entity" key="entity">entity</Option>
                                <Option value="constant" key="constant">constant</Option>
                              </Select>
                            </FormItem>
                          </Col>
                        </Row>
                      </Col>
                    </Row>
                    <Row>
                      <Col span="3">
                        <FormItem :label-width="0">
                          <span>{{ $t('output_params') }}</span>
                        </FormItem>
                      </Col>
                      <Col span="21" offset="0">
                        <Row v-for="(outPut, index) in inter['outputParameters']" :key="index">
                          <Col span="5">
                            <FormItem :label-width="0">
                              <Tooltip :content="outPut.name" style="width: 100%">
                                <span v-if="outPut.required === 'Y'" style="color:red">*</span>
                                <span
                                  style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                                  >{{ outPut.name }}</span
                                >
                              </Tooltip>
                            </FormItem>
                          </Col>
                          <Col span="2" offset="0">
                            <FormItem :label-width="0">
                              <span>{{ outPut.dataType }}</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="0">
                            <FormItem :label-width="0">
                              <Select
                                filterable
                                v-model="outPut.sensitiveData"
                                size="small"
                                style="width:50px"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                              >
                                <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                                  item.label
                                }}</Option>
                              </Select>
                            </FormItem>
                          </Col>
                          <Col span="10" offset="0">
                            <FormItem :label-width="0">
                              <FilterRules
                                v-if="outPut.mappingType === 'entity'"
                                v-model="outPut.mappingEntityExpression"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                :allDataModelsWithAttrs="allEntityType"
                                :rootEntity="clearedEntityType"
                                :needNativeAttr="true"
                                :needAttr="true"
                              ></FilterRules>
                              <span v-if="outPut.mappingType === 'context'">N/A</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="1">
                            <FormItem :label-width="0">
                              <Select
                                size="small"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                v-model="outPut.mappingType"
                              >
                                <Option value="context" key="context">context</Option>
                                <Option value="entity" key="entity">entity</Option>
                              </Select>
                            </FormItem>
                          </Col>
                        </Row>
                      </Col>
                    </Row>
                  </div>
                </div>
              </Panel>
            </Collapse>
          </div>
          <Row v-if="currentPluginObjKeysLength > 1" style="margin:45px auto;margin-bottom:0;">
            <Col span="9" offset="8">
              <Button type="primary" ghost v-if="currentPluginObj.status === 'DISABLED'" @click="pluginSave">{{
                $t('save')
              }}</Button>
              <Button type="primary" ghost v-if="currentPluginObj.status === 'DISABLED'" @click="regist">{{
                $t('regist')
              }}</Button>
              <Button type="error" ghost v-if="currentPluginObj.status === 'DISABLED'" @click="deleteRegisterSource">{{
                $t('delete')
              }}</Button>
              <Button type="error" ghost v-if="currentPluginObj.status === 'ENABLED'" @click="removePlugin">{{
                $t('decommission')
              }}</Button>
            </Col>
          </Row>
        </Form>
      </Col>
    </Row>
    <Modal
      v-model="configTreeManageModal"
      width="700"
      :title="$t('batch_regist')"
      :mask-closable="false"
      @on-ok="setConfigTreeHandler"
      @on-cancel="closeTreeModal"
    >
      <div style="height:500px;overflow:auto">
        <Tree ref="configTree" :data="configTree" show-checkbox multiple></Tree>
      </div>
    </Modal>
    <Modal
      v-model="configRoleManageModal"
      width="700"
      :title="$t('edit_config_role')"
      :mask-closable="false"
      footer-hide
    >
      <div>
        <div class="role-transfer-title">{{ $t('mgmt_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRolesBackUp"
          :target-keys="mgmtRolesKey"
          :render-format="renderRoleNameForTransfer"
          @on-change="handleMgmtRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div style="margin-top: 30px">
        <div class="role-transfer-title">{{ $t('use_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRolesBackUp"
          :target-keys="useRolesKey"
          :render-format="renderRoleNameForTransfer"
          @on-change="handleUseRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div style="margin-top:20px;text-align:right">
        <Button type="primary" @click="confirmRole">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import FilterRules from '../../components/filter-rules.vue'
import InterfaceFilterRule from '../../components/interface-filter-rule.vue'
import {
  getAllPluginByPkgId,
  getAllDataModels,
  registerPlugin,
  deletePlugin,
  deleteRegisterSource,
  savePluginConfig,
  retrieveSystemVariables,
  getPluginConfigsByPackageId,
  getInterfacesByPluginConfigId,
  getRoleList,
  updatePluginConfigRoleBinding,
  deletePluginConfigRoleBinding,
  getRolesByCurrentUser,
  getConfigByPkgId,
  updateConfigStatus
} from '@/api/server'

export default {
  data () {
    return {
      configTreeManageModal: false,
      configTree: [],
      newPluginConfig: '', // 缓存新增及复制时数据
      isAddOrCopy: '',

      isAdd: false,
      currentPluginForPermission: {},
      isLoading: false,
      activePanel: null,
      allDataModelsWithAttrs: {},
      currentPlugin: '',
      plugins: [],
      configRoleManageModal: false,
      currentUserRoles: [],
      transferTitles: [this.$t('unselected_role'), this.$t('selected_role')],
      transferStyle: { width: '300px' },
      mgmtRolesKey: [],
      useRolesKey: [],
      allRolesBackUp: [],
      addRegistModal: false,
      currentPluginObj: {},
      sourceList: [],
      selectedSource: '',
      registerName: '',
      addRegisterName: '',
      hasNewSource: false,
      hidePanal: false,
      allEntityType: [],
      selectedEntityType: '',
      form: {},
      allSystemVariables: [],
      sensitiveData: [
        {
          value: 'Y',
          label: 'Y'
        },
        {
          value: 'N',
          label: 'N'
        }
      ]
    }
  },
  components: {
    FilterRules,
    InterfaceFilterRule
  },
  computed: {
    rootEntity () {
      if (this.selectedEntityType && this.selectedEntityType.length > 0) {
        return this.selectedEntityType.split('{')[0].split(':')[1]
      } else {
        return ''
      }
      // return ''
    },
    currentPluginObjKeysLength () {
      return Object.keys(this.currentPluginObj).length
    },
    allPluginConfigs () {
      return [].concat(...this.plugins.map(p => p.pluginConfigDtoList))
    },
    clearedEntityType () {
      return this.selectedEntityType.split('{')[0]
    }
  },
  props: {
    pkgId: {
      required: true
    },
    pkgName: {
      required: true
    }
  },
  watch: {
    selectedEntityType: {
      handler (val) {}
    }
  },
  methods: {
    async setConfigTreeHandler () {
      const payload = this.$refs.configTree.data.map(_ => {
        return {
          ..._,
          pluginConfigs: _.children.map(child => {
            return {
              ...child,
              status: child.checked ? 'ENABLED' : 'DISABLED'
            }
          })
        }
      })
      const { status } = await updateConfigStatus(this.pkgId, payload)
      if (status === 'OK') {
        await this.getAllPluginByPkgId()
        if (this.currentPlugin) {
          this.getInterfacesByPluginConfigId(this.currentPlugin)
        }
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
      }
    },
    closeTreeModal () {
      this.configTreeManageModal = false
    },
    batchRegist () {
      this.getConfigByPkgId()
      this.configTreeManageModal = true
    },
    async getConfigByPkgId () {
      const { status, data } = await getConfigByPkgId(this.pkgId)
      if (status === 'OK') {
        this.configTree = data.map(_ => {
          const hasPermission = _.pluginConfigs.find(i => i.hasMgmtPermission === true)
          return {
            ..._,
            title: _.name,
            expand: true,
            disabled: !hasPermission,
            children: _.pluginConfigs.map(config => {
              return {
                ...config,
                title: `${config.name}-(${config.registerName})`,
                expand: true,
                checked: config.status === 'ENABLED',
                disabled: !config.hasMgmtPermission
              }
            })
          }
        })
      }
    },
    renderRoleNameForTransfer (item) {
      return item.label
    },
    async getRolesByCurrentUser () {
      const { status, data } = await getRolesByCurrentUser()
      if (status === 'OK') {
        this.currentUserRoles = data.map(_ => {
          return {
            ..._,
            key: _.id,
            label: _.displayName
          }
        })
      }
    },
    permissionsHandler (config) {
      this.mgmtRolesKey = config.permissionToRole.MGMT || []
      this.useRolesKey = config.permissionToRole.USE || []
      let hasPermission = false
      this.mgmtRolesKey.forEach(_ => {
        const found = this.currentUserRoles.find(role => role.id === _)
        if (found) {
          hasPermission = true
        }
      })
      if (hasPermission) {
        this.configRoleManageModal = true
        this.currentPluginForPermission = config
        this.isAdd = false
      } else {
        this.$Message.warning(this.$t('no_permission_to_mgmt'))
      }
    },
    confirmRole () {
      if (this.mgmtRolesKey.length) {
        this.configRoleManageModal = false
        if (this.isAddOrCopy === 'copy') {
          this.exectCopyPluginConfigDto()
        } else if (this.isAddOrCopy === 'add') {
          this.exectAddPluginConfigDto()
        }
      } else {
        this.$Message.warning(this.$t('mgmt_role_warning'))
      }
    },
    async getRoleList () {
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.allRolesBackUp = data.map(_ => {
          return {
            ..._,
            key: _.id,
            label: _.displayName
          }
        })
      }
    },
    handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      if (this.hasNewSource) {
        this.mgmtRolesKey = newTargetKeys
      } else {
        if (direction === 'right') {
          this.updateConfigPermission(this.currentPluginForPermission.id, moveKeys, 'MGMT')
        } else {
          this.deleteConfigPermission(this.currentPluginForPermission.id, moveKeys, 'MGMT')
        }
        this.mgmtRolesKey = newTargetKeys
      }
    },
    handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      if (this.hasNewSource) {
        this.useRolesKey = newTargetKeys
      } else {
        if (direction === 'right') {
          this.updateConfigPermission(this.currentPluginForPermission.id, moveKeys, 'USE')
        } else {
          this.deleteConfigPermission(this.currentPluginForPermission.id, moveKeys, 'USE')
        }
        this.useRolesKey = newTargetKeys
      }
    },
    async updateConfigPermission (proId, roleId, type) {
      const payload = {
        permission: type,
        roleIds: roleId
      }
      const { status } = await updatePluginConfigRoleBinding(proId, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
      }
      this.getAllPluginByPkgId()
    },
    async deleteConfigPermission (proId, roleId, type) {
      const payload = {
        permission: type,
        roleIds: roleId
      }
      const { status } = await deletePluginConfigRoleBinding(proId, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
      }
      this.getAllPluginByPkgId()
    },
    async retrieveSystemVariables () {
      const { data, status } = await retrieveSystemVariables({
        filters: [],
        paging: false
      })
      if (status === 'OK') {
        this.allSystemVariables = data.contents
      }
    },
    async pluginSave () {
      if (this.registerName.length === 0) {
        this.$refs.registerName.focus()
        this.$Notice.warning({
          title: 'Warning',
          desc: '输入注册名称'
        })
        return
      }
      if (this.hasNewSource) {
        this.currentPluginObj.permissionToRole.MGMT = this.mgmtRolesKey
        this.currentPluginObj.permissionToRole.USE = this.useRolesKey
      }
      let currentPluginForSave = JSON.parse(JSON.stringify(this.currentPluginObj))
      currentPluginForSave.registerName = this.registerName
      currentPluginForSave.targetEntityWithFilterRule = this.selectedEntityType
      if (this.hasNewSource) {
        delete currentPluginForSave.id
        currentPluginForSave.interfaces.map(_ => {
          delete _.id
        })
      }
      const { data, status, message } = await savePluginConfig(currentPluginForSave)
      if (status === 'OK') {
        const id = data.id
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        if (this.hasNewSource) {
          this.hasNewSource = false
        }
        await this.getAllPluginByPkgId()
        this.getInterfacesByPluginConfigId(id)
      }
    },
    mappingTypeChange (v, param) {
      if (v === 'entity') {
        param.mappingEntityExpression = null
      }
    },
    async regist () {
      if (this.hasNewSource) {
        this.currentPluginObj.permissionToRole.MGMT = this.mgmtRolesKey
        this.currentPluginObj.permissionToRole.USE = this.useRolesKey
      }
      this.currentPluginObj.registerName = this.registerName
      let currentPluginForSave = JSON.parse(JSON.stringify(this.currentPluginObj))
      currentPluginForSave.targetEntityWithFilterRule = this.selectedEntityType
      if (this.hasNewSource) {
        delete currentPluginForSave.id
        currentPluginForSave.interfaces.map(_ => {
          delete _.id
        })
      }
      const saveRes = await savePluginConfig(currentPluginForSave)
      if (saveRes.status === 'OK') {
        if (this.hasNewSource) {
          this.hasNewSource = false
        }
        const { status, message } = await registerPlugin(saveRes.data.id)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Success',
            desc: message
          })
          await this.getAllPluginByPkgId()
          this.getInterfacesByPluginConfigId(saveRes.data.id)
        }
      }
    },
    deleteRegisterSource () {
      this.$Modal.confirm({
        title: 'Warning',
        content: `${this.$t('delete')} ${this.currentPluginObj.name}(${this.currentPluginObj.registerName}) ?`,
        onOk: async () => {
          const { status, message } = await deleteRegisterSource(this.currentPluginObj.id)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            const { data, status } = await getAllPluginByPkgId(this.pkgId)
            if (status === 'OK') {
              this.plugins = data
            }
            this.hidePanal = false
          }
        },
        onCancel: () => {}
      })
    },
    addRegistMsg () {
      this.addRegisterName = ''
      this.addRegistModal = true
      this.hasNewSource = false
    },
    ok () {
      this.registerName = this.addRegisterName
      this.currentPluginObj.id = new Date().getMilliseconds() + ''
      this.currentPluginObj.registerName = this.registerName
      this.sourceList.push(this.currentPluginObj)
      this.selectedSource = this.currentPluginObj.id
      this.hasNewSource = true
    },
    async removePlugin () {
      const { status, message } = await deletePlugin(this.currentPluginObj.id)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        await this.getAllPluginByPkgId()
        this.getInterfacesByPluginConfigId(this.currentPluginObj.id)
      }
    },
    actionFocus (e) {
      e.preventDefault()
      e.stopPropagation()
    },
    actionBlurHandler (e, i) {
      e.preventDefault()
      e.stopPropagation()
      i.action = e.target.value
    },
    copyInterface (interfaces) {
      const found = this.currentPluginObj.interfaces.find(_ => _.action === interfaces.action + '-(copy)')
      if (found) return
      let i = { ...interfaces }
      i.action = interfaces.action + '-(copy)'
      i.id = null
      i.inputParameters = i.inputParameters.map(_ => {
        return { ..._, id: null }
      })
      i.outputParameters = i.outputParameters.map(_ => {
        return { ..._, id: null }
      })
      this.currentPluginObj.interfaces.push(i)
    },
    deleteInterface (index) {
      this.currentPluginObj.interfaces.splice(index, 1)
    },
    async getAllPluginByPkgId () {
      const { data, status } = await getPluginConfigsByPackageId(this.pkgId)
      if (status === 'OK') {
        this.plugins = data
      }
    },
    async copyPluginConfigDto (id) {
      this.newPluginConfig = id
      this.isAddOrCopy = 'copy'

      this.currentPlugin = ''
      this.mgmtRolesKey = []
      this.useRolesKey = []
      this.configRoleManageModal = true
      this.hasNewSource = true
    },
    async exectCopyPluginConfigDto () {
      await this.getInterfacesByPluginConfigId(this.newPluginConfig)
      this.registerName = this.currentPluginObj.registerName + '-(copy)'
      this.currentPluginObj.status = 'DISABLED'
      this.$refs.registerName.focus()
    },
    async addPluginConfigDto (plugin) {
      this.newPluginConfig = plugin
      this.isAddOrCopy = 'add'

      this.mgmtRolesKey = []
      this.useRolesKey = []
      this.configRoleManageModal = true
      this.hasNewSource = true
    },
    async exectAddPluginConfigDto () {
      const id = this.newPluginConfig.pluginConfigDtoList.find(_ => _.registerName === null).id
      await this.getInterfacesByPluginConfigId(id)
      this.registerName = ''
      this.selectedEntityType = ''
      this.currentPluginObj.status = 'DISABLED'
      this.$refs.registerName.focus()
    },
    selectPlugin (val) {
      this.hasNewSource = false
      this.currentPlugin = val
      this.getInterfacesByPluginConfigId(val)
    },
    async getInterfacesByPluginConfigId (id) {
      this.hidePanal = false
      this.isLoading = true
      this.currentPluginObj = {}
      let currentConfig = this.allPluginConfigs.find(s => s.id === id)
      const { data, status } = await getInterfacesByPluginConfigId(id)
      if (status === 'OK') {
        currentConfig.interfaces = data.map(_ => {
          return {
            ..._,
            filterRule: _.filterRule ? _.filterRule : ''
          }
        })
      }
      this.currentPluginObj = currentConfig
      this.selectedEntityType = currentConfig.targetEntityWithFilterRule
      this.registerName = this.currentPluginObj.registerName
      this.hidePanal = true
      this.isLoading = false
    },
    copyRegistSource (v) {
      this.registSourceChange(v)
      this.currentPluginObj.status = 'DISABLED'
    },
    async getAllDataModels () {
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data.map(_ => {
          // handle result sort by name
          return {
            ..._,
            pluginPackageEntities: _.pluginPackageEntities.sort(function (a, b) {
              var s = a.name.toLowerCase()
              var t = b.name.toLowerCase()
              if (s < t) return -1
              if (s > t) return 1
            })
          }
        })
      }
    }
  },
  created () {
    this.getRoleList()
    this.getAllPluginByPkgId()
    this.getAllDataModels()
    this.retrieveSystemVariables()
    this.getRolesByCurrentUser()
  }
}
</script>
<style lang="scss">
.plugin-register-page {
  .interfaceContainers {
    overflow: auto;
    height: calc(100vh - 450px);
  }
  .ivu-menu-vertical .ivu-menu-submenu-title {
    padding: 8px 0px;
  }
  .ivu-menu-vertical .ivu-menu-submenu-title-icon {
    right: 0;
  }
  .ivu-menu-vertical .ivu-menu-opened > * > .ivu-menu-submenu-title-icon {
    color: #2d8cf0;
  }
  .ivu-menu-opened {
    .ivu-menu-submenu-title {
      background: rgb(224, 230, 231);
      border-radius: 5px;
    }
  }
  .role-transfer-title {
    text-align: center;
    font-size: 13px;
    font-weight: 700;
    background-color: rgb(226, 222, 222);
    margin-bottom: 5px;
  }
}
</style>
