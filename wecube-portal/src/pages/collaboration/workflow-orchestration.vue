<template>
  <div>
    <Row style="margin-bottom:8px">
      <Col span="7" style="margin-right: 15px">
        <span style="margin-right: 10px">{{ $t('flow_name') }}</span>
        <Select clearable v-model="selectedFlow" style="width: 77%" @on-open-change="getAllFlows" filterable>
          <Option
            v-for="item in allFlows"
            :value="item.procDefId"
            :key="item.procDefId"
            :label="(item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')"
          >
            <span>{{
              (item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')
            }}</span>
            <span style="float:right">
              <Button
                @click="
                  showDeleteConfirm(
                    item.procDefId,
                    (item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')
                  )
                "
                icon="ios-trash"
                type="error"
                size="small"
              ></Button>
            </span>
            <span style="float:right;margin-right: 10px">
              <Button @click="setFlowPermission(item.procDefId)" icon="ios-person" type="primary" size="small"></Button>
            </span>
          </Option>
        </Select>
      </Col>
      <Col span="6" style="margin-right: 15px">
        <span style="margin-right: 10px">{{ $t('instance_type') }}</span>
        <div style="width:75%;display: inline-block;vertical-align: middle;">
          <FilterRules
            @change="onEntitySelect"
            v-model="currentSelectedEntity"
            :allDataModelsWithAttrs="allEntityType"
          ></FilterRules>
        </div>
      </Col>
      <Checkbox style="margin-right: 25px" border :disabled="!selectedFlow && !isAdd" v-model="excludeMode">{{
        $t('conflict_test')
      }}</Checkbox>
      <Button style="margin-top: -1px;" type="info" :disabled="isSaving || !selectedFlow" @click="saveDiagram(false)">
        {{ $t('release_flow') }}
      </Button>
      <Button
        @click="setFlowPermission(selectedFlow)"
        :disabled="!selectedFlow"
        style="margin-top: -1px;"
        type="primary"
      >
        {{ $t('permission_for_flow') }}
      </Button>
      <Button :disabled="!selectedFlow" style="margin-top: -1px;" type="info" @click="exportProcessDefinition(false)">
        {{ $t('export_flow') }}
      </Button>

      <Button style="float: right" @click="createNewDiagram()" type="success">
        {{ $t('create') }}
      </Button>
      <Button style="float: right;margin-right:4px" type="primary" @click="getHeaders">{{ $t('import_flow') }}</Button>

      <Upload
        v-show="isShowUploadList"
        ref="uploadButton"
        show-upload-list
        accept=".pds"
        name="uploadFile"
        :on-success="onImportProcessDefinitionSuccess"
        :on-error="onImportProcessDefinitionError"
        action="platform/v1/process/definitions/import"
        :headers="headers"
      >
        <Button style="display:none">{{ $t('import_flow') }}</Button>
      </Upload>
    </Row>
    <div v-show="showBpmn" class="split">
      <Split v-model="splitPanal" mode="vertical">
        <div slot="top" class="">
          <div class="containers" ref="content">
            <div class="canvas" ref="canvas"></div>

            <div id="js-properties-panel" class="panel"></div>
            <ul class="buttons">
              <li>
                <Button @click="resetZoom">Reset Zoom</Button>
              </li>
            </ul>
            <div class="node-operate-area">
              <Form
                v-if="show"
                ref="pluginConfigForm"
                :model="pluginForm"
                label-position="right"
                :label-width="120"
                style="margin-right:12px;padding-top: 16px;"
              >
                <template>
                  <Row>
                    <Col span="8">
                      <FormItem prop="serviceName">
                        <label slot="label"
                          >{{ $t('plugin_type') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <Select
                          filterable
                          v-model="pluginForm.taskCategory"
                          @on-change="
                            editFormdata()
                            pluginTypeChange()
                          "
                        >
                          <Option v-for="(item, index) in taskCategoryList" :value="item.value" :key="index">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="8">
                      <FormItem prop="timeoutExpression">
                        <label slot="label"
                          >{{ $t('timeout') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <Select v-model="pluginForm.timeoutExpression" @on-change="editFormdata">
                          <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index"
                            >{{ item.label }}
                          </Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="8">
                      <FormItem :label="$t('description')" prop="description">
                        <Input v-model="pluginForm.description" @on-change="editFormdata" />
                      </FormItem>
                    </Col>
                  </Row>
                  <Row>
                    <Col span="16">
                      <FormItem prop="routineExpression">
                        <label slot="label"
                          >{{ $t('locate_rules') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <!-- <FilterRules
                          :needAttr="true"
                          ref="filterRules"
                          v-model="pluginForm.routineExpression"
                          @change="filterRuleChanged"
                          :allDataModelsWithAttrs="allEntityType"
                        ></FilterRules> -->
                        <FilterRulesGroup
                          :isBatch="pluginForm.taskCategory === 'SDTN'"
                          ref="filterRulesGroup"
                          :rootEntity="currentSelectedEntity"
                          :routineExpression="pluginForm.routineExpression"
                          :allEntityType="allEntityType"
                        >
                        </FilterRulesGroup>
                      </FormItem>
                    </Col>
                    <Col span="8" v-if="pluginForm.taskCategory !== 'SDTN'">
                      <FormItem prop="serviceName">
                        <label slot="label"
                          >{{ $t('plugin') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <Select
                          filterable
                          clearable
                          v-model="pluginForm.serviceId"
                          @on-open-change="getPlugin"
                          @on-change="changePluginInterfaceList"
                        >
                          <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                            item.serviceDisplayName
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                  </Row>
                  <Row v-if="pluginForm.taskCategory !== 'SDTN'">
                    <Col span="8">
                      <FormItem prop="dynamicBind">
                        <label slot="label"
                          >{{ $t('dynamic_bind') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <Select v-model="pluginForm.dynamicBind" @on-change="editFormdata">
                          <Option v-for="item in yOn" :value="item" :key="item">{{ item }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="8">
                      <FormItem prop="preCheck">
                        <label slot="label"
                          >{{ $t('pre_check') }}
                          <span class="requires-tip">*</span>
                        </label>
                        <Select v-model="pluginForm.preCheck" @on-change="editFormdata">
                          <Option v-for="item in yOn" :value="item" :key="item">{{ item }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                  </Row>
                </template>
                <div
                  v-if="pluginForm.paramInfos.length && pluginForm.taskCategory !== 'SDTN'"
                  class="node-operate-plugin-config"
                >
                  <FormItem
                    :label="item.paramName"
                    :prop="item.paramName"
                    v-for="(item, index) in pluginForm.paramInfos"
                    :key="index"
                  >
                    <label slot="label" v-if="item.required === 'Y'"
                      >{{ item.paramName }}
                      <span class="requires-tip">*</span>
                    </label>
                    <Select
                      filterable
                      clearable
                      v-model="item.bindNodeId"
                      style="width:30%"
                      v-if="item.bindType === 'context'"
                      @on-change="onParamsNodeChange(index)"
                      @on-open-change="getFlowsNodes"
                    >
                      <Option v-for="(i, index) in currentflowsNodes" :value="i.nodeId" :key="index">{{
                        i.nodeName
                      }}</Option>
                    </Select>
                    <Select
                      v-model="item.bindParamType"
                      filterable
                      v-if="item.bindType === 'context'"
                      style="width:30%"
                      @on-change="onParamsNodeChange(index)"
                    >
                      <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">{{ i.label }}</Option>
                    </Select>
                    <Select
                      filterable
                      v-if="item.bindType === 'context'"
                      v-model="item.bindParamName"
                      style="width:30%"
                      @on-change="editFormdata"
                    >
                      <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{ i.name }}</Option>
                    </Select>
                    <label v-if="item.bindType === 'context' && item.required === 'Y'">
                      <span class="requires-tip">*</span>
                    </label>
                    <Input v-if="item.bindType === 'constant'" v-model="item.bindValue" @on-change="editFormdata" />
                  </FormItem>
                </div>
              </Form>
              <div class="node-operate-area-save-btn">
                <Button type="primary" style="float:right" @click="savePluginConfig('pluginConfigForm')">{{
                  $t('save')
                }}</Button>
              </div>
            </div>
          </div>
        </div>
      </Split>
    </div>
    <Modal v-model="flowRoleManageModal" width="700" :title="$t('edit_role')" :mask-closable="false">
      <div>
        <div class="role-transfer-title">{{ $t('mgmt_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRoles"
          :target-keys="mgmtRolesKeyToFlow"
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
          :target-keys="useRolesKeyToFlow"
          :render-format="renderRoleNameForTransfer"
          @on-change="handleUseRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div slot="footer">
        <Button type="primary" @click="confirmRole">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import BpmnModeler from 'bpmn-js/lib/Modeler'
import propertiesPanelModule from 'bpmn-js-properties-panel'
import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda'

import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda'
import customTranslate from '@/locale/flow-i18n/custom-translate'

/* Left side toolbar and node edit style */
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'

/* Right side toobar style */
import 'bpmn-js-properties-panel/dist/assets/bpmn-js-properties-panel.css'

import PathExp from '../components/path-exp.vue'
import FilterRules from '../components/filter-rules.vue'
import FilterRulesGroup from './components/filter-rules-group'
import axios from 'axios'
import { setCookie, getCookie } from '../util/cookie'
import CustomContextPad from '../util/CustomContextPad'
import xml2js from 'xml2js'
import {
  getAllFlow,
  saveFlow,
  saveFlowDraft,
  getFlowDetailByID,
  getFlowNodes,
  getParamsInfosByFlowIdAndNodeId,
  getAllDataModels,
  removeProcessDefinition,
  getPluginsByTargetEntityFilterRule,
  exportProcessDefinitionWithId,
  getRolesByCurrentUser,
  getRoleList,
  getPermissionByProcessId,
  updateFlowPermission
} from '@/api/server.js'

function setCTM (node, m) {
  var mstr = 'matrix(' + m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')'
  node.setAttribute('transform', mstr)
}

let contextPad = {
  __init__: ['customContextPad'],
  customContextPad: ['type', CustomContextPad]
}

export default {
  components: {
    PathExp,
    FilterRules,
    FilterRulesGroup
  },
  data () {
    return {
      excludeMode: false,
      yOn: ['Y', 'N'],
      splitPanal: 1,
      show: false,
      taskCategoryList: [
        { value: 'SSTN', label: this.$t('sstn') },
        { value: 'SUTN', label: this.$t('sutn') },
        { value: 'SDTN', label: this.$t('sdtn') }
      ],
      isSaving: false,
      headers: {},
      isShowUploadList: false,
      mgmtRolesKeyToFlow: [],
      useRolesKeyToFlow: [],
      currentUserRoles: [],
      allRolesBackUp: [],
      currentSettingFlow: '',
      flowRoleManageModal: false,
      isAdd: false,
      transferTitles: [this.$t('unselected_role'), this.$t('selected_role')],
      transferStyle: { width: '300px' },
      newFlowID: '',
      bpmnModeler: null,
      container: null,
      canvas: null,
      processName: '',
      currentNode: {
        id: '',
        name: '',
        nodeDefId: ''
      },
      additionalModules: [propertiesProviderModule, propertiesPanelModule, contextPad],
      allFlows: [],
      allEntityType: [],
      selectedFlow: null,
      selectedFlowData: '',
      temporaryFlow: null,
      currentSelectedEntity: '',
      rootPkg: '',
      rootEntity: '',
      isFormDataChange: false, // 是否编辑过node数据
      isFirstSelectNode: true,
      pluginForm: {},
      routineExpressionCache: '',
      defaultPluginForm: {
        description: '',
        dynamicBind: 'N',
        nodeDefId: '',
        nodeId: '',
        nodeName: '',
        nodeType: '',
        orderedNo: '',
        paramInfos: [],
        preCheck: 'N',
        procDefId: '',
        procDefKey: '',
        routineExpression: '',
        routineRaw: '',
        serviceId: '',
        serviceName: '',
        status: '',
        taskCategory: 'SSTN',
        timeoutExpression: '30'
      },
      serviceTaskBindInfos: [],
      // allPlugins: [],
      filteredPlugins: [],
      timeSelection: [
        {
          mins: '5',
          label: '5 ' + this.$t('mins')
        },
        {
          mins: '10',
          label: '10 ' + this.$t('mins')
        },
        {
          mins: '30',
          label: '30 ' + this.$t('mins')
        },
        {
          mins: '60',
          label: '1 ' + this.$t('hours')
        },
        {
          mins: '720',
          label: '12 ' + this.$t('hours')
        },
        {
          mins: '1440',
          label: '1 ' + this.$t('days')
        },
        {
          mins: '2880',
          label: '2 ' + this.$t('days')
        },
        {
          mins: '4320',
          label: '3 ' + this.$t('days')
        }
      ],
      paramsTypes: [
        { value: 'INPUT', label: this.$t('input') },
        { value: 'OUTPUT', label: this.$t('output') }
      ],
      currentflowsNodes: [],
      currentFlow: null,
      cacheFlowInfo: null // 缓存流程数据，供节点数据展示时使用
    }
  },
  watch: {
    selectedFlow: {
      handler (val, oldVal) {
        this.isFormDataChange = false
        this.currentSelectedEntity = ''
        this.show = false
        this.selectedFlowData = {}
        if (val) {
          this.selectedFlowData =
            this.allFlows.find(_ => {
              return _.procDefId === val
            }) || {}
          this.getFlowXml(val)
          this.getPermissionByProcess(val)
          this.pluginForm.paramInfos = []
          this.currentflowsNodes = []
        }
      }
    },
    temporaryFlow: {
      handler (val, oldVal) {
        if (val) {
          setTimeout(() => {
            this.selectedFlow = val
          }, 0)
        }
      }
    }
  },
  computed: {
    allRoles () {
      return this.isAdd ? this.currentUserRoles : this.allRolesBackUp
    },
    showBpmn () {
      if (this.selectedFlow || this.isAdd) {
        return true
      } else {
        return false
      }
    }
  },
  created () {
    this.init()
    this.getRoleList()
    this.getRolesByCurrentUser()
  },
  mounted () {
    this.initFlow()
    this.setCss('ivu-split-trigger-con', 'display: none;')
    this.setCss('bottom-pane', 'display: none;')
    this.setCss('top-pane', 'bottom: 0;')
  },
  methods: {
    pluginTypeChange () {
      this.pluginForm.routineExpression = this.currentSelectedEntity
      this.$refs.filterRulesGroup.changeRoutineExpressionItem(this.pluginForm.routineExpression)
    },
    // 节点定位规则变化检测
    filterRuleChanged () {
      this.isFormDataChange = true
    },
    editFormdata () {
      this.isFormDataChange = true
    },
    setCss (className, css) {
      document.getElementsByClassName(className)[0].style.cssText = css
    },
    renderRoleNameForTransfer (item) {
      return item.label
    },
    handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.mgmtRolesKeyToFlow = newTargetKeys
    },
    handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.useRolesKeyToFlow = newTargetKeys
    },
    async updateFlowPermission (proId, roleId, type) {
      const payload = {
        permission: type,
        roleId: roleId
      }
      const { status, message } = await updateFlowPermission(proId, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
      } else {
        this.$Notice.error({
          title: 'Fail',
          desc: message
        })
      }
    },
    setFlowPermission (id) {
      this.getPermissionByProcess(id)
      this.flowRoleManageModal = true
      this.currentSettingFlow = id
      this.isAdd = false
    },
    async getPermissionByProcess (id) {
      const { status, data } = await getPermissionByProcessId(id)
      if (status === 'OK') {
        this.mgmtRolesKeyToFlow = data.MGMT
        this.useRolesKeyToFlow = data.USE
      }
    },
    async confirmRole () {
      if (this.mgmtRolesKeyToFlow.length) {
        if (this.isAdd) {
          this.flowRoleManageModal = false
        } else {
          this.updatePermission(this.currentSettingFlow)
        }
      } else {
        this.$Message.warning(this.$t('mgmt_role_warning'))
        this.isAdd = false
      }
    },
    async updatePermission (id) {
      const payload = {
        permissionToRole: { MGMT: this.mgmtRolesKeyToFlow, USE: this.useRolesKeyToFlow }
      }
      const { status, message } = await updateFlowPermission(id, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.flowRoleManageModal = false
      } else {
        this.$Notice.error({
          title: 'Fail',
          desc: message
        })
      }
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
    init () {
      this.getAllDataModels()
      this.getAllFlows(true)
      // this.getPluginInterfaceList()
    },
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    async getPlugin (status) {
      if (status) {
        await this.getFilteredPluginInterfaceList(this.pluginForm.routineExpression)
      }
    },
    async getFilteredPluginInterfaceList (path) {
      // 相同定位规则使用缓存数据
      if (path === this.routineExpressionCache) {
        return
      }
      // const lastSelectType = this.$refs.filterRules && this.$refs.filterRules.lastSelectType
      // // 上下游节点不请求插件信息
      // if (lastSelectType === 'up' || lastSelectType === 'down') {
      //   this.$Notice.warning({
      //     title: 'Warning',
      //     desc: this.$t('obtain_plugin_warn')
      //   })
      //   this.pluginForm.serviceId = ''
      //   this.filteredPlugins = []
      //   return
      // }
      let pkg = ''
      let entity = ''
      let payload = {}
      this.filteredPlugins = []
      if (path) {
        // eslint-disable-next-line no-useless-escape
        const pathList = path.split(/[.~]+(?=[^\}]*(\{|$))/).filter(p => p.length > 1)
        const last = pathList[pathList.length - 1]
        const index = pathList[pathList.length - 1].indexOf('{')
        const isBy = last.indexOf(')')
        const current = last.split(':')
        const ruleIndex = current[1].indexOf('{')
        if (isBy > 0) {
          entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
          pkg = current[0].split(')')[1]
        } else {
          entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
          pkg = last.match(/[^>]+(?=:)/)[0]
        }
        payload = {
          pkgName: pkg,
          entityName: entity,
          targetEntityFilterRule: index > 0 ? pathList[pathList.length - 1].slice(index) : ''
        }
      } else {
        payload = {
          pkgName: '',
          entityName: '',
          targetEntityFilterRule: ''
        }
      }
      const { status, data } = await getPluginsByTargetEntityFilterRule(payload)
      if (status === 'OK') {
        this.filteredPlugins = data
      }
      this.routineExpressionCache = path
    },
    async changePluginInterfaceList (val) {
      this.editFormdata()
      let found = this.filteredPlugins.find(_ => _.serviceName === this.pluginForm.serviceId)
      this.pluginForm.paramInfos = {}
      if (found) {
        let needParams = found.inputParameters.filter(_ => _.mappingType === 'context' || _.mappingType === 'constant')
        this.pluginForm.paramInfos = needParams.map(_ => {
          return {
            paramName: _.name,
            bindNodeId: '',
            bindParamType: 'INPUT',
            bindParamName: '',
            bindType: _.mappingType,
            bindValue: '',
            required: _.required
          }
        })
      }
    },
    async getAllFlows (s) {
      if (s) {
        const { data, status } = await getAllFlow()
        if (status === 'OK') {
          let sortedResult = data.sort((a, b) => {
            let s = a.createdTime.toLowerCase()
            let t = b.createdTime.toLowerCase()
            if (s > t) return -1
            if (s < t) return 1
          })
          this.allFlows = sortedResult
        }
      }
    },
    showDeleteConfirm (id, name) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        content: name,
        onOk: () => {
          this.deleteFlow(id)
        },
        onCancel: () => {}
      })
    },
    async deleteFlow (id) {
      let { status, message } = await removeProcessDefinition(id)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getAllFlows(true)
      }
    },
    onEntitySelect (v) {
      this.currentSelectedEntity = v || ''
      if (this.currentSelectedEntity.split('{')[0] !== this.pluginForm.routineExpression.split('{')[0]) {
        if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = []
        this.pluginForm = {
          ...this.defaultPluginForm,
          routineExpression: v
        }
        this.resetNodePluginConfig()
      }
    },
    resetNodePluginConfig () {
      if (this.currentFlow && this.currentFlow.taskNodeInfos) {
        this.currentFlow.taskNodeInfos.forEach(_ => {
          if (_.nodeId.indexOf('Task') > -1) {
            Object.keys(_).forEach(key => {
              _[key] = this.defaultPluginForm[key]
            })
          }
        })
      }
    },
    resetZoom () {
      var canvas = this.bpmnModeler.get('canvas')
      canvas._changeViewbox(function () {
        setCTM(canvas._viewport, {
          a: '1',
          b: '0',
          c: '0',
          d: '1',
          e: '0',
          f: '0'
        })
      })
    },
    createNewDiagram () {
      this.newFlowID = 'wecube' + Date.now()
      this.isAdd = true
      this.mgmtRolesKeyToFlow = []
      this.useRolesKeyToFlow = []
      this.currentSelectedEntity = ''
      this.excludeMode = false
      this.pluginForm = { ...this.defaultPluginForm }
      this.currentFlow = {}
      const bpmnXmlStr =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn">\n' +
        '  <bpmn2:process id="' +
        this.newFlowID +
        '" isExecutable="true">\n' +
        '  </bpmn2:process>\n' +
        '  <bpmndi:BPMNDiagram id="BPMNDiagram_1">\n' +
        '    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="' +
        this.newFlowID +
        '">\n' +
        '    </bpmndi:BPMNPlane>\n' +
        '  </bpmndi:BPMNDiagram>\n' +
        '</bpmn2:definitions>'
      this.bpmnModeler.importXML(bpmnXmlStr, function (err) {
        if (err) {
          console.error(err)
        }
      })
      this.$nextTick(() => {
        this.selectedFlow = null
      })
      this.flowRoleManageModal = true
    },
    saveDiagram (isDraft) {
      let _this = this
      // eslint-disable-next-line handle-callback-err
      this.bpmnModeler.saveXML({ format: true }, function (err, xml) {
        let processName
        xml2js.parseString(xml, (errx, result) => {
          console.log(errx)
          processName = result['bpmn2:definitions']['bpmn2:process'][0]['$']['name']
        })
        if (!xml) return
        const xmlString = xml.replace(/[\r\n]/g, '')
        let payload = {
          permissionToRole: {
            MGMT: _this.mgmtRolesKeyToFlow,
            USE: _this.useRolesKeyToFlow
          },
          excludeMode: _this.excludeMode === true ? 'Y' : 'N',
          procDefData: xmlString,
          procDefId: (_this.currentFlow && _this.currentFlow.procDefId) || '',
          procDefKey: isDraft ? (_this.currentFlow && _this.currentFlow.procDefKey) || '' : _this.newFlowID,
          procDefName: processName,
          rootEntity: _this.currentSelectedEntity,
          status: isDraft ? (_this.currentFlow && _this.currentFlow.procDefKey) || '' : '',
          taskNodeInfos: [..._this.serviceTaskBindInfos]
        }
        if (isDraft) {
          const selectedFlowData = _this.allFlows.find(_ => {
            return _.procDefId === _this.selectedFlow
          })
          payload.procDefName = processName || (selectedFlowData && selectedFlowData.procDefName) || 'default'
          // payload.procDefName = _this.selectedFlowData.procDefName || 'default'
          saveFlowDraft(payload).then(data => {
            if (data && data.status === 'OK') {
              _this.$Notice.success({
                title: 'Success',
                desc: data.message
              })
              _this.getAllFlows(true)
              _this.selectedFlow = data.data.procDefId
              _this.getFlowXml(data.data.procDefId)
              _this.temporaryFlow = data.data.procDefId
            }
          })
        } else {
          _this.isSaving = true
          saveFlow(payload).then(data => {
            _this.isSaving = false
            if (data && data.status === 'OK') {
              _this.$Notice.success({
                title: 'Success',
                desc: data.message
              })
              _this.getAllFlows(true)
              _this.selectedFlow = data.data.procDefId
              _this.temporaryFlow = data.data.procDefId
            }
          })
        }
        _this.show = false
        _this.isFormDataChange = false
      })
    },
    savePluginConfig (ref) {
      let index = -1
      this.serviceTaskBindInfos.forEach((_, i) => {
        if (this.currentNode.id === _.nodeId) {
          index = i
        }
      })
      if (index > -1) {
        this.serviceTaskBindInfos.splice(index, 1)
      }

      let found = this.filteredPlugins.find(_ => _.serviceName === this.pluginForm.serviceId)
      const routineExpressionItem = this.$refs.filterRulesGroup.routineExpressionItem
      const isAllStartAsSelectedEntity = routineExpressionItem.every(item =>
        item.routineExpression.startsWith(this.currentSelectedEntity)
      )
      if (!isAllStartAsSelectedEntity) {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('locate_rules_warning')
        })
        return
      }
      this.pluginForm.routineExpression = routineExpressionItem.reduce((tmp, item, index) => {
        return tmp + item.routineExpression + (index === routineExpressionItem.length - 1 ? '' : '#DME#')
      }, '')

      let pluginFormCopy = JSON.parse(JSON.stringify(this.pluginForm))
      // 校验必填项，未选中节点跳过校验
      if (
        this.currentNode.id &&
        (this.currentNode.id.startsWith('SubProcess_') || this.currentNode.id.startsWith('Task_'))
      ) {
        const res = this.checkSaveParams(pluginFormCopy)
        if (!res) return
      }
      if (this.currentNode.id) {
        this.serviceTaskBindInfos.push({
          ...pluginFormCopy,
          nodeDefId: this.currentNode.nodeDefId,
          nodeId: this.currentNode.id,
          nodeName: this.currentNode.name,
          serviceName: (found && found.serviceName) || '',
          routineRaw: pluginFormCopy.routineExpression,
          taskCategory: pluginFormCopy.taskCategory
        })
      }
      this.saveDiagram(true)
    },
    checkSaveParams (pluginFormCopy) {
      if (!pluginFormCopy.routineExpression) {
        this.$Message.warning(this.$t('locate_rules') + ' ' + this.$t('required'))
        return false
      }
      if (!pluginFormCopy.serviceId) {
        this.$Message.warning(this.$t('plugin') + ' ' + this.$t('required'))
        return false
      }
      let hasvalue = true
      pluginFormCopy.paramInfos.forEach(item => {
        if (
          item.required === 'Y' &&
          ((item.bindType === 'constant' && item.bindValue === '') ||
            (item.bindType === 'context' && item.bindParamName === ''))
        ) {
          this.$Message.warning(item.paramName + ' ' + this.$t('required'))
          hasvalue = false
        }
      })
      if (!hasvalue) {
        return false
      }
      return true
    },
    async openPluginModal (e) {
      if (!this.currentSelectedEntity) {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('select_entity_first')
        })
      } else {
        this.pluginForm = JSON.parse(
          JSON.stringify(
            (this.cacheFlowInfo &&
              this.cacheFlowInfo.taskNodeInfos &&
              this.cacheFlowInfo.taskNodeInfos.find(_ => _.nodeId === this.currentNode.id)) ||
              this.prepareDefaultPluginForm()
          )
        )
        console.log(JSON.stringify(this.pluginForm))
        this.pluginForm.dynamicBind = this.pluginForm.dynamicBind || 'N'
        this.pluginForm.preCheck = this.pluginForm.preCheck || 'N'
        // 实体类型条件不带入节点中
        let rootEntity = this.currentSelectedEntity.split('{')[0]
        // TODO: 保存数据及获取到数据需处理
        this.pluginForm.routineExpression = this.pluginForm.routineExpression || rootEntity
        // eslint-disable-next-line no-useless-escape
        const pathList = this.pluginForm.routineExpression.split(/[.~]+(?=[^\}]*(\{|$))/).filter(p => p.length > 1)
        console.log(pathList)
        if (pathList[0].split('{')[0] !== rootEntity) {
          this.pluginForm.routineExpression = rootEntity
        }
        // this.getPluginInterfaceList()

        // get flow's params infos
        this.getFlowsNodes()
        // 数据写入节点无需获取插件
        if (this.pluginForm.taskCategory !== 'SDTN') {
          await this.getFilteredPluginInterfaceList(this.pluginForm.routineExpression)
        }
        const nodeOrigin = this.filteredPlugins.find(item => item.serviceName === this.pluginForm.serviceName)
        nodeOrigin &&
          nodeOrigin.inputParameters &&
          this.pluginForm.paramInfos.forEach(pItem => {
            nodeOrigin.inputParameters.forEach(oItem => {
              if (pItem.paramName === oItem.name) {
                pItem.required = oItem.required
              }
            })
          })
        this.$nextTick(() => {
          this.show = e.target.tagName === 'rect'
        })
      }
    },
    prepareDefaultPluginForm () {
      let temp = JSON.parse(JSON.stringify(this.defaultPluginForm))
      temp.routineExpression = this.currentSelectedEntity
      return { ...temp }
    },
    onParamsNodeChange (index) {
      // this.editFormdata()
      this.getParamsOptionsByNode(index)
    },
    async getFlowsNodes () {
      if (!this.currentFlow || !this.currentFlow.procDefId) {
        this.currentflowsNodes = []
        return
      }
      let { status, data } = await getFlowNodes(this.currentFlow.procDefId)
      if (status === 'OK') {
        this.currentflowsNodes = data.filter(_ => _.nodeId !== this.currentNode.id)
        const found = data.find(i => i.nodeId === this.currentNode.id)
        this.currentNode.nodeDefId = found ? found.nodeDefId : ''
        this.pluginForm.paramInfos.forEach((_, index) => {
          this.onParamsNodeChange(index)
        })
      }
    },
    async getParamsOptionsByNode (index) {
      const found = this.currentflowsNodes.find(_ => _.nodeId === this.pluginForm.paramInfos[index].bindNodeId)
      if (!this.currentFlow || !found) return
      let { status, data } = await getParamsInfosByFlowIdAndNodeId(this.currentFlow.procDefId, found.nodeDefId)
      if (status === 'OK') {
        let res = data.filter(_ => _.type === this.pluginForm.paramInfos[index].bindParamType)
        this.$set(this.pluginForm.paramInfos[index], 'currentParamNames', res)
      }
    },
    bindCurrentNode (e) {
      this.currentNode.id = e.target.parentNode.getAttribute('data-element-id')
      let nodeName = ''
      const previousSibling = e.target.previousSibling
      if (previousSibling && previousSibling.children[1] && previousSibling.children[1].children) {
        for (let i = 0; i < previousSibling.children[1].children.length; i++) {
          nodeName += previousSibling.children[1].children[i].innerHTML || ''
        }
      }
      this.currentNode.name = nodeName
    },
    async getFlowXml (id) {
      if (!id) return
      const { status, data } = await getFlowDetailByID(id)
      if (status === 'OK') {
        this.currentFlow = data
        this.cacheFlowInfo = JSON.parse(JSON.stringify(data))
        const _this = this
        this.bpmnModeler.importXML(data ? data.procDefData : '', function (err) {
          if (err) {
            console.error(err)
          }
          _this.serviceTaskBindInfos = data.taskNodeInfos
          _this.currentSelectedEntity = data.rootEntity || ''
          _this.excludeMode = data.excludeMode === 'Y'
          // _this.rootPkg = data.rootEntity.split(':')[0] || ''
          // _this.rootEntity = data.rootEntity.split(':')[1].split('{')[0] || ''
        })
      }
    },
    nodeClicked (e) {
      this.isFirstSelectNode = false
      this.show = false
      this.bindCurrentNode(e)
      this.isShowSaveBtnOnly = false
      if (
        this.currentNode.id &&
        (this.currentNode.id.startsWith('SubProcess_') || this.currentNode.id.startsWith('Task_'))
      ) {
        this.openPluginModal(e)
      }
    },
    initFlow () {
      this.isFirstSelectNode = true
      this.container = this.$refs.content
      const canvas = this.$refs.canvas
      canvas.onmouseup = e => {
        if (this.isFirstSelectNode) {
          this.nodeClicked(e)
        } else {
          if (this.isFormDataChange) {
            this.$Modal.confirm({
              title: this.$t('confirm_to_delete'),
              content: this.$t('node_params_edit_confirm'),
              okText: this.$t('abandon'),
              'z-index': 1000000,
              onOk: () => {
                this.isFormDataChange = false
                this.show = false
              },
              onCancel: () => {
                this.isFormDataChange = true
              }
            })
          } else {
            this.nodeClicked(e)
          }
        }
      }
      var customTranslateModule = {
        translate: ['value', customTranslate]
      }
      if (this.$lang === 'zh-CN') {
        this.additionalModules.push(customTranslateModule)
      } else {
        if (this.additionalModules.length > 3) {
          this.additionalModules.pop()
        }
      }

      this.bpmnModeler = new BpmnModeler({
        container: canvas,
        propertiesPanel: {
          parent: '#js-properties-panel'
        },
        additionalModules: this.additionalModules,

        moddleExtensions: {
          camunda: camundaModdleDescriptor
        }
      })
      document.getElementsByClassName('djs-palette')[0].classList.remove('two-column')
    },
    getHeaders () {
      this.isShowUploadList = true
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
    },
    exportProcessDefinition (isDraft) {
      let procDefId = this.selectedFlow
      if (procDefId == null || procDefId === 'undefined' || procDefId === '') {
        this.$Notice.error({
          title: 'Error',
          desc: 'Must select a process to export.'
        })
        return false
      }

      exportProcessDefinitionWithId(procDefId)
    },
    onImportProcessDefinitionSuccess (response, file, filelist) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })

        this.getAllFlows(true)
        this.selectedFlow = response.data.procDefId
        this.temporaryFlow = response.data.procDefId
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message || ''
        })
      }
    },
    onImportProcessDefinitionError (file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    }
  }
}
</script>

<style lang="scss">
.requires-tip {
  color: red;
  vertical-align: middle;
}
.containers {
  position: absolute;
  background-color: white;
  width: 100%;
  height: 100%;
}
.canvas {
  width: 100%;
  height: 100%;
}

#right_click_menu {
  display: none;
  width: 100px;
  border: 1px solid gray;
  position: absolute;
  background-color: white;
  padding: 5px 5px;
  box-shadow: 0 0 5px grey;
}
.panel {
  position: absolute;
  right: 0;
  top: 0;
  width: 300px;
  min-height: 100%;
  background: #f8f8f8;
  overflow-y: auto;

  .bpp-properties-panel .entry-label {
    border: black solid 1px;
    border-radius: 5px;
    background-color: white;
    padding: 2px 7px;
    font-style: normal;
  }
}
.buttons {
  position: absolute;
  left: 44px;
  bottom: 50px;
  & > li {
    display: inline-block;
    margin: 5px;
    & > a {
      color: #999;
      background: #eee;
      cursor: not-allowed;
      padding: 8px;
      border: 1px solid #ccc;
      &.active {
        color: #333;
        background: #fff;
        cursor: pointer;
      }
    }
  }
}
</style>
<style lang="scss">
.bjs-powered-by {
  bottom: 50px !important;
}
// hide toolbar
.bpmn-icon-data-object,
.bpmn-icon-data-store,
.bpmn-icon-subprocess-expanded,
.bpmn-icon-task,
.bpmn-icon-gateway-none,
.bpmn-icon-intermediate-event-none,
.bpmn-icon-participant {
  display: none;
}
// control toolbar position
.djs-palette {
  left: -1px;
  top: -1px;
}

// hide panal tab
.bpp-properties-tabs-links .bpp-properties-tab-link {
  display: none;
}
.bpp-properties-tabs-links .bpp-active {
  display: inline-block;
}

// hide panal tab item
[data-entry='process-is-executable'],
[data-entry='initiator'],
[data-group='documentation'],
[data-group='historyConfiguration'],
[data-group='jobConfiguration'],
[data-group='externalTaskConfiguration'],
[data-group='candidateStarterConfiguration'],
[data-group='tasklist'],
[data-group='details'] .group-label,
[data-group='async'] {
  display: none;
}

// hide node toolbar
[data-id='replace-with-rule-task'],
[data-id='replace-with-send-task'],
[data-id='replace-with-receive-task'],
[data-id='replace-with-manual-task'],
[data-id='replace-with-script-task'],
[data-id='replace-with-user-task'],
[data-id='replace-with-transaction'] {
  display: none;
}

[data-action='append.append-task'] {
  display: none !important;
}
.ivu-transfer-list-body {
  margin-top: 10px;
}
.role-transfer-title {
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  background-color: rgb(226, 222, 222);
  margin-bottom: 5px;
}
.ivu-upload-select {
  display: none !important;
}
</style>
<style scoped lang="scss">
.split {
  height: calc(100vh - 155px);
  border: 1px solid #999;
  // border-bottom: none;
}
.split-bottom {
  position: relative;
  background: white;
  left: -1px;
  margin-right: -2px;
  border-right: 1px solid #999;
  border-left: 1px solid #999;
}
.ivu-form-item {
  margin-bottom: 0 !important;
}
.path-exp {
  margin-bottom: 8px;
  margin-top: 0 !important;
}
.btn-plugin-config {
  float: right;
  background: white;
}
</style>
<style lange="scss" scoped>
.node-operate-area {
  border: 1px solid #dcdee2;
  background: #fff;
  padding: 8px 32px;
  position: absolute;
  bottom: 0;
  width: 100%;
}
.node-operate-plugin-config {
  max-height: 200px;
  overflow-y: auto;
}
</style>
