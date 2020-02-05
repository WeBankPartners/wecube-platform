<template>
  <div>
    <Row style="margin-bottom: 10px">
      <Col span="7">
        <span style="margin-right: 10px">{{ $t('flow_name') }}</span>
        <Select clearable v-model="selectedFlow" style="width: 70%" @on-open-change="getAllFlows" filterable>
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
              <Button @click="setFlowPermission(item.procDefId)" icon="ios-build" type="primary" size="small"></Button>
            </span>
          </Option>
        </Select>
        <Button @click="createNewDiagram()" icon="md-add" type="success"></Button>
      </Col>
      <Col span="8" offset="1">
        <span style="margin-right: 10px">{{ $t('instance_type') }}</span>
        <Select @on-change="onEntitySelect" v-model="currentSelectedEntity" filterable clearable style="width: 70%">
          <OptionGroup :label="pluginPackage.packageName" v-for="(pluginPackage, index) in allEntityType" :key="index">
            <Option
              v-for="item in pluginPackage.pluginPackageEntities"
              :value="pluginPackage.packageName + ':' + item.name"
              :key="item.name"
              >{{ item.name }}</Option
            >
          </OptionGroup>
        </Select>
      </Col>
      <Button type="info" @click="saveDiagram(false)">
        {{ $t('save_flow') }}
      </Button>
      <Button type="info" @click="exportProcessDefinition(false)">
        {{ $t('export_flow') }}
      </Button>
      <Button type="info" @click="getHeaders">{{ $t('import_flow') }}</Button>
      <Upload
        ref="uploadButton"
        show-upload-list
        accept=".pds"
        name="uploadFile"
        :on-success="onImportProcessDefinitionSuccess"
        :on-error="onImportProcessDefinitionError"
        action="wecubeRequest/platform/v1/process/definitions/import"
        :headers="headers"
      >
        <Button style="display:none">{{ $t('import_flow') }}</Button>
      </Upload>
    </Row>
    <div v-show="showBpmn" class="containers" ref="content">
      <div style="height:100%;" class="canvas" ref="canvas"></div>
      <div id="right_click_menu">
        <a href="javascript:void(0);" @click="openPluginModal">
          {{ $t('config_plugin') }}
        </a>
        <br />
      </div>

      <div id="js-properties-panel" class="panel"></div>
      <ul class="buttons">
        <li>
          <Button @click="resetZoom">Reset Zoom</Button>
        </li>
      </ul>
    </div>
    <Modal v-model="pluginModalVisible" :title="$t('config_plugin')" width="40">
      <Form ref="pluginConfigForm" :model="pluginForm" label-position="right" :label-width="150">
        <FormItem :label="$t('locate_rules')" prop="routineExpression">
          <PathExp
            v-if="pluginModalVisible"
            :rootPkg="rootPkg"
            :rootEntity="rootEntity"
            :allDataModelsWithAttrs="allEntityType"
            v-model="pluginForm.routineExpression"
          ></PathExp>
        </FormItem>
        <FormItem :label="$t('plugin')" prop="serviceName">
          <Select
            filterable
            clearable
            v-model="pluginForm.serviceId"
            @on-open-change="getFilteredPluginInterfaceList(pluginForm.routineExpression)"
            @on-change="getPluginInterfaceList(false)"
          >
            <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
              item.serviceDisplayName
            }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('timeout')" prop="timeoutExpression">
          <Select clearable v-model="pluginForm.timeoutExpression">
            <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index">{{ item.label }} </Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('description')" prop="description">
          <Input v-model="pluginForm.description" />
        </FormItem>
        <hr style="margin-bottom: 20px" />
        <FormItem
          :label="item.paramName"
          :prop="item.paramName"
          v-for="(item, index) in pluginForm.paramInfos"
          :key="index"
        >
          <Select
            v-model="item.bindNodeId"
            style="width:30%"
            v-if="item.bindType === 'context'"
            @on-change="onParamsNodeChange(index)"
            @on-open-change="getFlowsNodes"
          >
            <Option v-for="(i, index) in currentflowsNodes" :value="i.nodeId" :key="index">{{ i.nodeName }}</Option>
          </Select>
          <Select
            v-model="item.bindParamType"
            v-if="item.bindType === 'context'"
            style="width:30%"
            @on-change="onParamsNodeChange(index)"
          >
            <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">{{ i.label }}</Option>
          </Select>
          <Select v-if="item.bindType === 'context'" v-model="item.bindParamName" style="width:30%">
            <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{ i.name }}</Option>
          </Select>
          <Input v-if="item.bindType === 'constant'" v-model="item.bindValue" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="savePluginConfig('pluginConfigForm')">{{ $t('confirm') }}</Button>
      </div>
    </Modal>
    <Modal
      v-model="flowRoleManageModal"
      width="700"
      :title="$t('edit_role')"
      @on-ok="confirmRole"
      @on-cancel="confirmRole"
    >
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
import axios from 'axios'

import {
  getAllFlow,
  saveFlow,
  saveFlowDraft,
  getFlowDetailByID,
  getFlowNodes,
  getParamsInfosByFlowIdAndNodeId,
  getAllDataModels,
  getPluginInterfaceList,
  removeProcessDefinition,
  getFilteredPluginInterfaceList,
  exportProcessDefinitionWithId,
  getRolesByCurrentUser,
  getRoleList,
  getPermissionByProcessId,
  updateFlowPermission,
  deleteFlowPermission
} from '@/api/server.js'

function setCTM (node, m) {
  var mstr = 'matrix(' + m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')'
  node.setAttribute('transform', mstr)
}

export default {
  components: {
    PathExp
  },
  data () {
    return {
      headers: {},
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
      additionalModules: [propertiesProviderModule, propertiesPanelModule],
      allFlows: [],
      allEntityType: [],
      selectedFlow: null,
      temporaryFlow: null,
      currentSelectedEntity: '',
      rootPkg: '',
      rootEntity: '',
      pluginModalVisible: false,
      pluginForm: {},
      defaultPluginForm: {
        description: '',
        nodeDefId: '',
        nodeId: '',
        nodeName: '',
        nodeType: '',
        orderedNo: '',
        paramInfos: [],
        procDefId: '',
        procDefKey: '',
        routineExpression: null,
        routineRaw: '',
        serviceId: '',
        serviceName: '',
        status: '',
        timeoutExpression: '30'
      },
      serviceTaskBindInfos: [],
      allPlugins: [],
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
      currentFlow: null
    }
  },
  watch: {
    selectedFlow: {
      handler (val, oldVal) {
        if (val) {
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
  },
  methods: {
    renderRoleNameForTransfer (item) {
      return item.label
    },
    handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      if (this.isAdd) {
        this.mgmtRolesKeyToFlow = newTargetKeys
      } else {
        if (direction === 'right') {
          this.updateFlowPermission(this.currentSettingFlow, moveKeys, 'mgmt')
        } else {
          this.deleteFlowPermission(this.currentSettingFlow, moveKeys, 'mgmt')
        }
        this.mgmtRolesKeyToFlow = newTargetKeys
      }
    },
    handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      if (this.isAdd) {
        this.useRolesKeyToFlow = newTargetKeys
      } else {
        if (direction === 'right') {
          this.updateFlowPermission(this.currentSettingFlow, moveKeys, 'use')
        } else {
          this.deleteFlowPermission(this.currentSettingFlow, moveKeys, 'use')
        }
        this.useRolesKeyToFlow = newTargetKeys
      }
    },
    async updateFlowPermission (proId, roleId, type) {
      const payload = {
        permission: type,
        roleId: roleId
      }
      await updateFlowPermission(proId, payload)
    },
    async deleteFlowPermission (proId, roleId, type) {
      const payload = {
        permission: type,
        roleId: roleId
      }
      await deleteFlowPermission(proId, payload)
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
    confirmRole () {
      this.flowRoleManageModal = false
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
    init () {
      this.getAllDataModels()
      this.getAllFlows(true)
      this.getPluginInterfaceList()
    },
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    async getFilteredPluginInterfaceList (path) {
      const pathList = path.split(/[~)>]/)
      const last = pathList[pathList.length - 1].split(':')
      const { status, data } = await getFilteredPluginInterfaceList(last[0], last[1])
      if (status === 'OK') {
        this.filteredPlugins = data
      }
    },
    async getPluginInterfaceList (isUseOriginParamsInfo = true) {
      let { status, data } = await getPluginInterfaceList()
      if (status === 'OK') {
        this.allPlugins = data

        let found = data.find(_ => _.serviceName === this.pluginForm.serviceId)
        if (found) {
          let needParams = found.inputParameters.filter(
            _ => _.mappingType === 'context' || _.mappingType === 'constant'
          )
          if (isUseOriginParamsInfo) return
          this.pluginForm.paramInfos = needParams.map(_ => {
            return {
              paramName: _.name,
              bindNodeId: '',
              bindParamType: 'INPUT',
              bindParamName: '',
              bindType: _.mappingType,
              bindValue: ''
            }
          })
        }
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
      this.rootPkg = this.currentSelectedEntity.split(':')[0]
      this.rootEntity = this.currentSelectedEntity.split(':')[1]

      if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = []
      this.pluginForm = {
        ...this.defaultPluginForm,
        routineExpression: v
      }
      this.resetNodePluginConfig()
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
      this.isAdd = true
      this.flowRoleManageModal = true
      this.mgmtRolesKeyToFlow = []
      this.useRolesKeyToFlow = []
      this.currentSelectedEntity = ''
      this.pluginForm = { ...this.defaultPluginForm }
      this.currentFlow = {}
      this.newFlowID = 'wecube' + Date.now()
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
    },
    saveDiagram (isDraft) {
      let _this = this
      // eslint-disable-next-line handle-callback-err
      this.bpmnModeler.saveXML({ format: true }, function (err, xml) {
        if (!xml) return
        const xmlString = xml.replace(/[\r\n]/g, '')
        const processName = document.getElementById('camunda-name').innerText
        const payload = {
          permissionToRole: {
            MGMT: _this.mgmtRolesKeyToFlow,
            USE: _this.useRolesKeyToFlow
          },
          procDefData: xmlString,
          procDefId: (_this.currentFlow && _this.currentFlow.procDefId) || '',
          procDefKey: isDraft ? (_this.currentFlow && _this.currentFlow.procDefKey) || '' : _this.newFlowID,
          procDefName: processName,
          rootEntity: _this.currentSelectedEntity,
          status: isDraft ? (_this.currentFlow && _this.currentFlow.procDefKey) || '' : '',
          taskNodeInfos: _this.serviceTaskBindInfos
        }

        isDraft
          ? saveFlowDraft(payload).then(data => {
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
          : saveFlow(payload).then(data => {
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

      let found = this.allPlugins.find(_ => _.serviceName === this.pluginForm.serviceId)

      let pluginFormCopy = JSON.parse(JSON.stringify(this.pluginForm))
      this.serviceTaskBindInfos.push({
        nodeDefId: this.currentNode.nodeDefId,
        ...pluginFormCopy,
        nodeId: this.currentNode.id,
        nodeName: this.currentNode.name,
        serviceName: (found && found.serviceName) || '',
        routineRaw: pluginFormCopy.routineExpression
      })
      this.pluginModalVisible = false
      this.saveDiagram(true)
    },
    async openPluginModal () {
      if (!this.currentSelectedEntity) {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('select_entity_first')
        })
      } else {
        this.pluginModalVisible = true
        this.pluginForm = (this.currentFlow &&
          this.currentFlow.taskNodeInfos &&
          this.currentFlow.taskNodeInfos.find(_ => _.nodeId === this.currentNode.id)) || { ...this.defaultPluginForm }
        this.getPluginInterfaceList()
        // get flow's params infos
        this.getFlowsNodes()
        this.pluginForm.routineExpression && this.getFilteredPluginInterfaceList(this.pluginForm.routineExpression)
      }
    },
    onParamsNodeChange (index) {
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
        this.currentNode.nodeDefId = data.find(i => i.nodeId === this.currentNode.id).nodeDefId
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
    bindRightClick () {
      var menu = document.getElementById('right_click_menu')
      var elements = document.getElementsByClassName('djs-element djs-shape')
      const _this = this
      for (var i = 0; i < elements.length; i++) {
        elements[i].oncontextmenu = function (e) {
          e = e || window.event
          var x = e.clientX
          var y = e.clientY
          menu.style.display = 'block'
          menu.style.left = x - 25 + 'px'
          menu.style.top = y - 130 + 'px'
          _this.currentNode.id = e.target.parentNode.getAttribute('data-element-id')
          _this.currentNode.name =
            (e.target.previousSibling &&
              e.target.previousSibling.children[1] &&
              e.target.previousSibling.children[1].children[0] &&
              e.target.previousSibling.children[1].children[0].innerHTML) ||
            ''
          return false
        }
      }

      document.onclick = function (e) {
        e = e || window.event
        menu.style.display = 'none'
      }

      menu.onclick = function (e) {
        e = e || window.event
        e.stopPropagation()
      }
    },
    async getFlowXml (id) {
      if (!id) return
      const { status, data } = await getFlowDetailByID(id)
      if (status === 'OK') {
        this.currentFlow = data
        const _this = this
        this.bpmnModeler.importXML(data ? data.procDefData : '', function (err) {
          if (err) {
            console.error(err)
          }
          _this.bindRightClick()
          _this.serviceTaskBindInfos = data.taskNodeInfos
          _this.currentSelectedEntity = data.rootEntity || ''
          _this.rootPkg = data.rootEntity.split(':')[0] || ''
          _this.rootEntity = data.rootEntity.split(':')[1] || ''
        })
      }
    },
    initFlow () {
      this.container = this.$refs.content
      const canvas = this.$refs.canvas
      canvas.onmouseup = () => {
        this.bindRightClick()
      }
      var customTranslateModule = {
        translate: ['value', customTranslate]
      }

      if (this.$lang === 'zh-CN') {
        this.additionalModules.push(customTranslateModule)
      } else {
        if (this.additionalModules.length > 2) {
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
    },
    getHeaders () {
      let refreshRequest = null
      const currentTime = new Date().getTime()
      let session = window.sessionStorage
      const token = JSON.parse(session.getItem('token'))
      if (token) {
        const accessToken = token.find(t => t.tokenType === 'accessToken')
        const expiration = accessToken.expiration * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/wecubeRequest/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + token.find(t => t.tokenType === 'refreshToken').token
            }
          })
          refreshRequest.then(
            res => {
              session.setItem('token', JSON.stringify(res.data.data))
              this.setUploadActionHeader()
              this.$refs.uploadButton.handleClick()
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + '/#/login'
            }
          )
        } else {
          this.setUploadActionHeader()
          this.$refs.uploadButton.handleClick()
        }
      } else {
        window.location.href = window.location.origin + '/#/login'
      }
    },
    setUploadActionHeader () {
      let session = window.sessionStorage
      const token = JSON.parse(session.getItem('token'))
      this.headers = {
        Authorization: 'Bearer ' + token.find(t => t.tokenType === 'accessToken').token
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
    async onImportProcessDefinitionSuccess (response, file, filelist) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })

        this.getAllFlows(true)
        this.selectedFlow = response.data.procDefId
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
.containers {
  position: absolute;
  background-color: white;
  border: #999 1px solid;
  width: 97%;
  height: 84%;
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
  width: 400px;
  max-height: 100%;
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
  left: 20px;
  bottom: 20px;
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
// hide toolbar
.bpmn-icon-data-object,
.bpmn-icon-data-store,
.bpmn-icon-participant {
  display: none;
}

// hide panal tab
.bpp-properties-tabs-links .bpp-properties-tab-link {
  display: none;
}
.bpp-properties-tabs-links .bpp-active {
  display: inline-block;
}

// hide panal tab item
[data-group='documentation'],
[data-group='historyConfiguration'],
[data-group='jobConfiguration'],
[data-group='externalTaskConfiguration'],
[data-group='candidateStarterConfiguration'],
[data-group='tasklist'],
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
</style>
