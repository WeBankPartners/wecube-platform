<template>
  <div class="plugin-register-page">
    <Row>
      <Col span="7" style="border-right: 1px solid #e8eaec;">
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
                  style="padding: 5px 10px 5px 16px;"
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
      <Col span="17" offset="0" style="padding-left: 10px">
        <Spin size="large" fix style="margin-top: 200px;" v-show="isLoading">
          <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
          <div>{{ $t('loading') }}</div>
        </Spin>
        <Form :model="form" v-if="hidePanal" label-position="left" :label-width="100">
          <Row style="border-bottom: 1px solid #bbb7b7; margin-top: 20px">
            <Col span="8" offset="0">
              <FormItem :label="$t('regist_name')">
                <Input v-model="registerName" ref="registerName" :disabled="currentPluginObj.status === 'ENABLED'" />
              </FormItem>
            </Col>
            <Col span="15" v-if="hidePanal" offset="1">
              <FormItem :label="$t('target_type')">
                <span @click="getAllDataModels">
                  <FilterRules
                    v-model="selectedEntityType"
                    :rootEntity="clearedEntityType"
                    :disabled="currentPluginObj.status === 'ENABLED'"
                    :allDataModelsWithAttrs="allEntityType"
                    @change="selectedEntityTypeChangeHandler"
                  ></FilterRules>
                </span>
              </FormItem>
            </Col>
          </Row>
          <div style="height: calc(100vh - 300px);overflow:auto;" id="paramsContainer">
            <div style="background:#f7f7f7">
              <div
                v-for="(inter, index) in currentPluginObj.interfaces"
                style="margin:4px;padding: 4px; border-bottom:1px solid #dcdee2;"
                :key="index + inter.action"
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
                <Tooltip :content="$t('copy')" placement="top">
                  <Button
                    size="small"
                    type="success"
                    :disabled="currentPluginObj.status === 'ENABLED'"
                    ghost
                    icon="md-copy"
                    @click.stop.prevent="copyInterface(inter)"
                  ></Button>
                </Tooltip>
                <Tooltip :content="$t('delete')" placement="top">
                  <Button
                    size="small"
                    type="error"
                    :disabled="currentPluginObj.status === 'ENABLED'"
                    ghost
                    icon="ios-trash-outline"
                    @click.stop.prevent="deleteInterface(index)"
                  ></Button>
                </Tooltip>
                <span style="float:right">
                  <Tooltip :content="$t('parameter_configuration')" placement="top-end">
                    <Button
                      size="small"
                      type="primary"
                      icon="ios-settings"
                      ghost
                      @click.stop.prevent="showParamsModal(inter, index, currentPluginObj.interfaces)"
                    ></Button>
                  </Tooltip>
                </span>
              </div>
            </div>
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
      v-model="paramsModalVisible"
      width="100"
      :title="currentServiceName"
      :mask-closable="false"
      @on-ok="confirmParamsHandler"
      @on-cancel="closeParamsModal"
    >
      <div v-if="paramsModalVisible" class="modal-paramsContainer">
        <Row style="border-bottom: 1px solid #e5dfdf;margin-bottom:5px">
          <Col span="2" offset="0">
            <strong style="font-size:15px;">{{ $t('params_type') }}</strong>
          </Col>
          <Col span="2" offset="0">
            <strong style="font-size:15px;">{{ $t('params_name') }}</strong>
          </Col>
          <Col span="2" offset="0" style="text-align: center;">
            <strong style="font-size:15px;">{{ $t('data_type') }}</strong>
          </Col>
          <Col span="1" style="margin-left:60px" offset="0">
            <strong style="font-size:15px;">{{ $t('core_multiple') }}</strong>
          </Col>
          <Col span="1" style="margin-left:45px" offset="0">
            <strong style="font-size:15px;">{{ $t('sensitive') }}</strong>
          </Col>
          <Col span="2" offset="1">
            <strong style="font-size:15px;">
              {{ $t('attribute_type') }}
            </strong>
          </Col>
          <Col span="10" style="margin-left:100px" offset="1">
            <strong style="font-size:15px;">{{ $t('attribute') }}</strong>
          </Col>
        </Row>
        <div class="modal-interfaceContainers">
          <Form>
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>{{ $t('input_params') }}</span>
                </FormItem>
              </Col>
              <Col span="22" offset="0">
                <Row v-for="(param, index) in currentInter['inputParameters']" :key="index">
                  <Col span="3">
                    <FormItem :label-width="0">
                      <span v-if="param.required === 'Y'" style="color:red;vertical-align: text-bottom;">*</span>
                      <Tooltip content="">
                        <span
                          style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                          >{{ param.name }}</span
                        >
                        <div slot="content" style="white-space: normal;">
                          <span>{{ param.description }}</span>
                        </div>
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="2" offset="0">
                    <FormItem :label-width="0">
                      <span>{{ param.dataType }}</span>
                    </FormItem>
                  </Col>
                  <Col span="1" offset="0">
                    <FormItem :label-width="0">
                      <Select
                        v-model="param.multiple"
                        filterable
                        style="width:50px"
                        :disabled="currentPluginObj.status === 'ENABLED'"
                      >
                        <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                          item.label
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="1" offset="1">
                    <FormItem :label-width="0">
                      <Select
                        v-model="param.sensitiveData"
                        filterable
                        style="width:50px"
                        :disabled="currentPluginObj.status === 'ENABLED'"
                      >
                        <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                          item.label
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="3" offset="1">
                    <FormItem :label-width="0">
                      <Select
                        filterable
                        :disabled="currentPluginObj.status === 'ENABLED'"
                        v-model="param.mappingType"
                        @on-change="mappingTypeChange($event, param)"
                      >
                        <Option v-for="item in mappingTypeOptions" :value="item.value" :key="item.key">{{
                          item.value
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="11" offset="1">
                    <FormItem :label-width="0">
                      <!-- <FilterRules
                        v-if="param.mappingType === 'entity'"
                        v-model="param.mappingEntityExpression"
                        :disabled="currentPluginObj.status === 'ENABLED'"
                        :allDataModelsWithAttrs="allEntityType"
                        :rootEntity="clearedEntityType"
                        :needNativeAttr="true"
                        :needAttr="true"
                        :rootEntityFirst="true"
                      ></FilterRules> -->
                      <!-- <FilterRulesRef
                        v-if="param.mappingType === 'entity'"
                        v-model="param.mappingEntityExpression"
                        :disabled="currentPluginObj.status === 'ENABLED'"
                        :allDataModelsWithAttrs="allEntityType"
                        :rootEntity="clearedEntityType"
                        :needNativeAttr="true"
                        :needAttr="true"
                        :rootEntityFirst="true"
                      ></FilterRulesRef> -->
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
                      <span v-if="param.mappingType === 'context'">N/A</span>
                      <span v-if="param.mappingType === 'constant'">
                        <Input
                          v-model="param.mappingValue"
                          placeholder=""
                          :disabled="currentPluginObj.status === 'ENABLED'"
                        />
                      </span>
                      <span v-if="param.mappingType === 'entity'">
                        <div style="width: 50%;display:inline-block;vertical-align: top;">
                          <FilterRulesRef
                            v-model="param.mappingEntityExpression"
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            :allDataModelsWithAttrs="allEntityType"
                            :rootEntity="clearedEntityType"
                            :needNativeAttr="true"
                            :needAttr="true"
                            :rootEntityFirst="true"
                          ></FilterRulesRef>
                        </div>
                        <Button
                          v-if="param.dataType === 'object'"
                          type="primary"
                          size="small"
                          @click="showObjectConfig(param)"
                          >{{ $t('configuration') }}</Button
                        >
                      </span>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
            <hr />
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>{{ $t('output_params') }}</span>
                  <Button @click="addOutputParams" size="small" icon="ios-add"></Button>
                </FormItem>
              </Col>
              <Col span="22" offset="0">
                <Row v-for="(outPut, index) in currentInter['outputParameters']" :key="index">
                  <template v-if="outPut.mappingType !== 'assign'">
                    <Col span="3">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color:red;vertical-align: text-bottom;">*</span>
                        <Tooltip content="">
                          <span
                            style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                            >{{ outPut.name }}</span
                          >
                          <div slot="content" style="white-space: normal;">
                            <span>{{ outPut.description }}</span>
                          </div>
                        </Tooltip>
                      </FormItem>
                    </Col>
                    <Col span="2" offset="0">
                      <FormItem :label-width="0">
                        <span>{{ outPut.dataType }}</span>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="0">
                      <FormItem :label-width="0">
                        <Select
                          v-model="outPut.multiple"
                          filterable
                          style="width:50px"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                        >
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="1">
                      <FormItem :label-width="0">
                        <Select
                          filterable
                          v-model="outPut.sensitiveData"
                          style="width:50px"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                        >
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="3" offset="1">
                      <FormItem :label-width="0">
                        <Select :disabled="currentPluginObj.status === 'ENABLED'" v-model="outPut.mappingType">
                          <Option value="context" key="context">context</Option>
                          <Option value="entity" key="entity">entity</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="11" offset="1">
                      <FormItem :label-width="0">
                        <FilterRulesRef
                          v-if="outPut.mappingType === 'entity'"
                          v-model="outPut.mappingEntityExpression"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                          :allDataModelsWithAttrs="allEntityType"
                          :rootEntity="clearedEntityType"
                          :needNativeAttr="true"
                          :needAttr="true"
                          :rootEntityFirst="true"
                        ></FilterRulesRef>
                        <span v-if="outPut.mappingType === 'context'">N/A</span>
                      </FormItem>
                    </Col>
                  </template>
                  <template v-else>
                    <Col span="5">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color:red;vertical-align: text-bottom;">*</span>
                        <!-- <Button @click="removeOutputParams(index)" size="small" icon="ios-trash"></Button> -->
                        <Input v-model="outPut.name" placeholder="key" :disabled="outPut.id !== ''" />
                      </FormItem>
                    </Col>
                    <Col span="4" offset="1">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color:red;vertical-align: text-bottom;">*</span>
                        <Input
                          v-model="outPut.mappingValue"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                          placeholder="value"
                        />
                      </FormItem>
                    </Col>
                    <Col span="12" offset="1">
                      <FormItem :label-width="0">
                        <FilterRulesRef
                          v-model="outPut.mappingEntityExpression"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                          :allDataModelsWithAttrs="allEntityType"
                          :rootEntity="clearedEntityType"
                          :needNativeAttr="true"
                          :needAttr="true"
                          :rootEntityFirst="true"
                        ></FilterRulesRef>
                        <span v-if="outPut.mappingType === 'context'">N/A</span>
                      </FormItem>
                    </Col>
                  </template>
                </Row>
              </Col>
            </Row>
          </Form>
        </div>
      </div>
    </Modal>
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
    <!-- @on-ok="ok"
  @on-cancel="cancel" -->
    <Modal
      v-model="objectModal.showObjectConfigModal"
      :title="objectModal.title"
      width="100%"
      @on-ok="okEdit"
      @on-cancel="cancelEdit"
    >
      <recursive
        ref="objectTree"
        increment="0"
        :treeData="objectModal.treeData"
        :clearedEntityType="objectRootEntity"
        :allEntityType="allEntityType"
        :status="currentPluginObj.status"
      ></recursive>
    </Modal>
  </div>
</template>
<script>
import FilterRules from '../../components/filter-rules.vue'
import FilterRulesRef from '../../components/filter-rules-ref.vue'
import InterfaceFilterRule from '../../components/interface-filter-rule.vue'
import recursive from './recursive'
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
  getRolesByCurrentUser,
  getConfigByPkgId,
  updateConfigStatus,
  getPluginRegisterObjectType,
  updatePluginRegisterObjectType
} from '@/api/server'

export default {
  data () {
    return {
      objectModal: {
        showObjectConfigModal: false,
        pluginConfigId: '',
        objectMetaId: '',
        treeData: {
          refObjectMeta: {
            propertyMetas: {}
          }
        }
      },
      currentServiceName: '',
      currentInter: {},
      currentInterIndex: 0,
      paramsModalVisible: false,
      configTreeManageModal: false,
      configTree: [],
      newPluginConfig: '', // 缓存新增及复制时数据
      isAddOrCopy: '',

      isAdd: false,
      currentPluginForPermission: {},
      isLoading: false,
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
      mappingTypeOptions: [
        { label: 'context', value: 'context' },
        { label: 'system_variable', value: 'system_variable' },
        { label: 'entity', value: 'entity' },
        { label: 'constant', value: 'constant' }
      ],
      sensitiveData: [
        {
          value: 'Y',
          label: 'Y'
        },
        {
          value: 'N',
          label: 'N'
        }
      ],
      clearedEntityType: '',
      objectRootEntity: '' // object类型外层表达式根
    }
  },
  components: {
    FilterRules,
    FilterRulesRef,
    InterfaceFilterRule,
    recursive
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
      handler (val) {
        if (val && val.length > 0) {
          this.clearedEntityType = val.split('{')[0]
        } else {
          this.clearedEntityType = ''
        }
      }
    }
  },
  methods: {
    cancelEdit () {
      this.objectModal = {
        showObjectConfigModal: false,
        type: '',
        indexNo: '',
        treeData: {
          refObjectMeta: {
            propertyMetas: {}
          }
        }
      }
    },
    async okEdit () {
      const { status } = await updatePluginRegisterObjectType(
        this.objectModal.pluginConfigId,
        this.objectModal.objectMetaId,
        this.objectModal.treeData.refObjectMeta
      )
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
      }
    },
    // 'inputParameters', param, index
    async showObjectConfig (originData) {
      let datax = JSON.parse(JSON.stringify(originData))
      if (!originData.refObjectMeta) {
        this.$Notice.error({
          title: 'Error',
          desc: 'Error'
        })
      }
      this.getObjectRoot(datax.mappingEntityExpression)
      const { status, data } = await getPluginRegisterObjectType(originData.refObjectMeta.id)
      if (status === 'OK') {
        datax.refObjectMeta = data
        this.objectModal.treeData = datax
      }
      this.objectModal.objectMetaId = originData.refObjectMeta.id
      this.objectModal.title = originData.name
      // this.objectModal.treeData = JSON.parse(JSON.stringify(originData))
      this.objectModal.showObjectConfigModal = true
    },
    getObjectRoot (expression) {
      // A --- '>' B -- '~'
      const lastIndexOfA = expression.lastIndexOf('>')
      const lastIndexOfB = expression.lastIndexOf('~')
      // 以 '>' 分割
      if (lastIndexOfA >= lastIndexOfB) {
        this.objectRootEntity = expression.split('>').pop()
      } else {
        // 以 '~' 分割
        this.objectRootEntity = expression
          .split('~')
          .pop()
          .split(')')
          .pop()
      }
    },
    managementExpression (mappingEntityExpression, rootEntity) {
      if (mappingEntityExpression && mappingEntityExpression.includes(rootEntity)) {
        return mappingEntityExpression
      } else {
        return rootEntity
      }
    },
    selectedEntityTypeChangeHandler (val) {
      const findIndex = val.indexOf('{')
      if (findIndex === -1) {
        this.currentPluginObj.filterRule = ''
      } else {
        const rule = val.substring(findIndex, val.length)
        this.currentPluginObj.filterRule = rule
      }
      const rootEntity = val.split('{')[0]
      this.currentPluginObj.interfaces.forEach(_ => {
        _.inputParameters.forEach(i => {
          if (i.mappingType === 'entity') {
            const tmp = this.managementExpression(i.mappingEntityExpression, rootEntity)
            i.mappingEntityExpression = tmp
          }
        })
        _.outputParameters.forEach(o => {
          if (o.mappingType === 'entity') {
            const tmp = this.managementExpression(o.mappingEntityExpression, rootEntity)
            o.mappingEntityExpression = tmp
          }
        })
      })
    },
    removeOutputParams (index) {
      this.currentInter.outputParameters.splice(index, 1)
    },
    addOutputParams () {
      const assginOutput = this.currentInter.outputParameters.filter(item => item.mappingType === 'assign')
      const res = assginOutput.every(item => item.name !== '' && item.mappingValue !== '')
      if (!res) {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('plugin_const_params_warning')
        })
        return
      }
      const outputParametersSingle = this.currentInter.outputParameters[0]
      this.currentInter.outputParameters.push({
        dataType: 'string',
        description: '',
        id: '',
        mappingEntityExpression: '',
        mappingValue: '',
        mappingType: 'assign',
        name: '',
        pluginConfigInterfaceId: outputParametersSingle.pluginConfigInterfaceId,
        refObjectMeta: null,
        required: 'N',
        sensitiveData: 'N',
        type: 'OUTPUT'
      })
    },
    managementObjectExpression (mappingEntityExpression, rootEntity) {
      if (mappingEntityExpression && mappingEntityExpression.includes(rootEntity)) {
        return mappingEntityExpression
      } else {
        return this.selectedEntityType
      }
    },
    // refObjectMeta。id pluginConfigId
    showParamsModal (val, index, currentPluginObj) {
      this.currentInter = val
      // this.currentInter.inputParameters.forEach(item => {
      //   item.mappingEntityExpression = this.managementObjectExpression(
      //     item.mappingEntityExpression,
      //     this.selectedEntityType
      //   )
      // })
      this.objectModal.pluginConfigId = val.pluginConfigId
      this.currentInterIndex = index
      this.currentServiceName = val.serviceName
      this.paramsModalVisible = true
    },
    closeParamsModal () {
      this.paramsModalVisible = false
    },
    confirmParamsHandler () {
      if (this.currentPluginObj.status !== 'ENABLED') {
        this.currentPluginObj.interfaces.splice(this.currentInterIndex, 1, this.currentInter)
        // this.pluginSave()
      }
      this.paramsModalVisible = false
    },
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
            key: _.name,
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
        const found = this.currentUserRoles.find(role => role.name === _)
        if (found) {
          hasPermission = true
        }
      })
      if (hasPermission) {
        this.configRoleManageModal = true
        this.currentPluginForPermission = config
        this.isAddOrCopy = 'new'
      } else {
        this.$Message.warning(this.$t('no_permission_to_mgmt'))
      }
    },
    async confirmRole () {
      if (this.mgmtRolesKey.length) {
        if (this.isAddOrCopy === 'copy') {
          // await this.updatePermission(this.newPluginConfig)
          await this.exectCopyPluginConfigDto()
          this.configRoleManageModal = false
        } else if (this.isAddOrCopy === 'add') {
          this.exectAddPluginConfigDto()
          this.configRoleManageModal = false
        } else if (this.isAddOrCopy === 'new') {
          await this.updatePermission(this.currentPluginForPermission.id)
        }
      } else {
        this.$Message.warning(this.$t('mgmt_role_warning'))
      }
    },
    async updatePermission (id) {
      const payload = {
        permissionToRole: { MGMT: this.mgmtRolesKey, USE: this.useRolesKey }
      }
      const { status } = await updatePluginConfigRoleBinding(id, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
        this.configRoleManageModal = false
      }
      this.getAllPluginByPkgId()
    },
    async getRoleList () {
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.allRolesBackUp = data.map(_ => {
          return {
            ..._,
            key: _.name,
            label: _.displayName
          }
        })
      }
    },
    handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.mgmtRolesKey = newTargetKeys
    },
    handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.useRolesKey = newTargetKeys
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
      const id = this.newPluginConfig.pluginConfigDtoList.find(_ => _.registerName === '' || _.registerName === null).id
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
        this.currentPluginObj = currentConfig
        this.selectedEntityType = currentConfig.targetEntityWithFilterRule
        this.registerName = this.currentPluginObj.registerName
      }
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
            entities: _.entities.sort(function (a, b) {
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
.modal-paramsContainer {
  height: calc(100vh - 300px);
  .modal-interfaceContainers {
    overflow: auto;
    height: calc(100vh - 320px);
  }
  .ivu-form-item {
    margin-bottom: 2px;
  }
}
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
