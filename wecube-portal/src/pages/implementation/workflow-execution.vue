<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form v-if="isEnqueryPage" label-position="left">
            <FormItem :label-width="150" :label="$t('orchs')">
              <Select v-model="selectedFlowInstance" style="width:70%" filterable clearable>
                <Option
                  v-for="item in allFlowInstances"
                  :value="item.id"
                  :key="item.id"
                  :label="
                    item.procInstName + ' ' + (item.createdTime || 'createdTime') + ' ' + (item.operator || 'operator')
                  "
                >
                  <span>
                    {{
                      item.procInstName +
                        ' ' +
                        (item.createdTime || 'createdTime') +
                        ' ' +
                        (item.operator || 'operator')
                    }}
                  </span>
                </Option>
              </Select>
              <Button type="info" @click="queryHandler">
                {{ $t('query_orch') }}
              </Button>
            </FormItem>
          </Form>
        </Col>
        <Col span="4" style="text-align: right;margin-bottom:8px;padding-right:40px;float:right;">
          <Button type="info" v-if="!isEnqueryPage" @click="queryHistory">
            {{ $t('enquery_new_workflow_job') }}
          </Button>
          <Button type="success" v-if="isEnqueryPage" @click="createHandler">
            {{ $t('create_new_workflow_job') }}
          </Button>
        </Col>
      </Row>
      <Row>
        <Row id="graphcontain">
          <Col span="6" style="border-right:1px solid #d3cece; text-align: center;height:100%">
            <div class="excution-serach">
              <Form>
                <FormItem :label-width="100" :label="$t('select_orch')">
                  <Select
                    label
                    v-model="selectedFlow"
                    :disabled="isEnqueryPage"
                    @on-change="orchestrationSelectHandler"
                    @on-open-change="getAllFlow"
                    filterable
                    clearable
                  >
                    <Option v-for="item in allFlows" :value="item.procDefId" :key="item.procDefId">{{
                      item.procDefName + ' ' + item.createdTime
                    }}</Option>
                  </Select>
                </FormItem>
              </Form>
            </div>

            <div class="graph-container" id="flow" style="height:90%"></div>
          </Col>
          <Col span="18" style="text-align: center;margin-top: 5px;text-align: center;height:100%;">
            <div>
              <Form>
                <FormItem :label-width="100" :label="$t('target_object')">
                  <Select
                    style="width:400px;float:left"
                    label
                    v-model="selectedTarget"
                    :disabled="isEnqueryPage"
                    @on-change="onTargetSelectHandler"
                    @on-open-change="getTargetOptions"
                    filterable
                    clearable
                  >
                    <Option v-for="item in allTarget" :value="item.id" :key="item.id">{{ item.key_name }}</Option>
                  </Select>
                </FormItem>
              </Form>
            </div>
            <div class="graph-container" id="graph" style="height:90%"></div>
            <Spin size="large" fix v-show="isLoading">
              <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
              <div>{{ $t('loading') }}</div>
            </Spin>
          </Col>
        </Row>
      </Row>
      <div style="text-align: right;margin-top: 6px;margin-right:40px">
        <Button v-if="showExcution" :disabled="isExecuteActive" style="width:120px" type="info" @click="excutionFlow">{{
          $t('execute')
        }}</Button>
      </div>
    </Card>
    <Modal
      :title="$t('select_an_operation')"
      v-model="workflowActionModalVisible"
      :footer-hide="true"
      :mask-closable="false"
      :scrollable="true"
    >
      <div class="workflowActionModal-container" style="text-align: center;margin-top: 20px;">
        <Button type="info" @click="workFlowActionHandler('retry')">{{ $t('retry') }}</Button>
        <Button type="info" @click="workFlowActionHandler('skip')" style="margin-left: 20px">{{ $t('skip') }}</Button>
      </div>
    </Modal>
    <Modal
      :title="currentNodeTitle"
      v-model="targetModalVisible"
      :mask-closable="false"
      :scrollable="true"
      :mask="false"
      class="model_target"
      width="50"
      @on-ok="targetModelConfirm"
    >
      <Table
        border
        ref="selection"
        max-height="300"
        @on-selection-change="targetModelSelectHandel"
        :columns="targetModelColums"
        :data="tartetModels"
      >
        <template slot-scope="{ row, index }" slot="action">
          <Tooltip placement="bottom" theme="light" @on-popper-show="getDetail(row)" :delay="500" max-width="400">
            <Button type="warning" size="small">View</Button>
            <div slot="content">
              <pre><span>{{rowContent}}</span></pre>
            </div>
          </Tooltip>
        </template>
      </Table>
    </Modal>
    <Modal v-model="showNodeDetail" :fullscreen="nodeDetailFullscreen" width="1000" :styles="{ top: '50px' }">
      <p slot="header">
        <span>{{ nodeTitle }}</span>
        <Icon v-if="!nodeDetailFullscreen" @click="zoomModal" class="header-icon" type="ios-expand" />
        <Icon v-else @click="nodeDetailFullscreen = false" class="header-icon" type="ios-contract" />
      </p>
      <div style="overflow:auto;">
        <h5>Data:</h5>
        <div style="margin: 0 6px 6px" v-html="nodeDetailResponseHeader"></div>
        <!-- <pre>{{ nodeDetail }}</pre> -->
        <h5>requestObjects:</h5>
        <Table :columns="nodeDetailColumns" :max-height="tableMaxHeight" tooltip="true" :data="nodeDetailIO"></Table>
      </div>
    </Modal>
    <div id="model_graph_detail">
      <highlight-code lang="json">{{ modelNodeDetail }}</highlight-code>
    </div>
    <div id="flow_graph_detail">
      <highlight-code lang="json">{{ flowNodeDetail }}</highlight-code>
    </div>
  </div>
</template>
<script>
import {
  getAllFlow,
  getFlowOutlineByID,
  getTargetOptions,
  getTreePreviewData,
  createFlowInstance,
  getProcessInstances,
  getProcessInstance,
  retryProcessInstance,
  getModelNodeDetail,
  getNodeBindings,
  getNodeContext
} from '@/api/server'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
import { addEvent, removeEvent } from '../util/event.js'
export default {
  data () {
    return {
      showNodeDetail: false,
      nodeDetailFullscreen: false,
      tableMaxHeight: 250,
      nodeTitle: null,
      nodeDetail: null,
      graph: {},
      flowGraph: {},
      modelData: [],
      flowData: {},
      currentNodeTitle: null,
      rowContent: null,
      allFlowInstances: [],
      allFlows: [],
      allTarget: [],
      currentFlowNodeId: '',
      foundRefAry: [],
      selectedFlowInstance: '',
      selectedFlow: '',
      selectedTarget: '',
      showExcution: true,
      isExecuteActive: false,
      isEnqueryPage: false,
      workflowActionModalVisible: false,
      targetModalVisible: false,
      tartetModels: [],
      targetModelColums: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: 'PackageName',
          key: 'packageName'
        },
        {
          title: 'EntityName',
          key: 'entityName'
        },
        {
          title: 'DisplayName',
          key: 'displayName'
        },
        {
          title: 'Action',
          slot: 'action',
          width: 100,
          align: 'center'
        }
      ],
      nodeDetailColumns: [
        {
          title: 'inputs',
          key: 'inputs',
          render: (h, params) => {
            const strInput = JSON.stringify(params.row.inputs)
              .split(',')
              .join(',<br/>')
            return h(
              'div',
              {
                domProps: {
                  innerHTML: `<pre>${strInput}</pre>`
                }
              },
              []
            )
          }
        },
        {
          title: 'outputs',
          key: 'outputs',
          render: (h, params) => {
            const strOutput = JSON.stringify(params.row.outputs)
              .split(',')
              .join(',<br/>')
            return h(
              'div',
              {
                domProps: {
                  innerHTML: `<pre>${strOutput}</pre>`
                }
              },
              []
            )
          }
        }
      ],
      nodeDetailIO: [],
      nodeDetailResponseHeader: null,
      currentFailedNodeID: '',
      timer: null,
      modelNodeDetail: {},
      flowNodeDetail: {},
      modelDetailTimer: null,
      flowNodesBindings: [],
      flowDetailTimer: null,
      isLoading: false,
      catchNodeTableList: []
    }
  },
  watch: {
    targetModalVisible: function (val) {
      if (!val) {
        this.catchNodeTableList = []
      }
    }
  },
  mounted () {
    this.getProcessInstances()
    this.getAllFlow()
    this.createHandler()
  },
  destroyed () {
    console.log('destroyed', 'start clearInterval', this.timer)
    clearInterval(this.timer)
    console.log('destroyed', 'end clearInterval', this.timer)
  },
  methods: {
    async getDetail (row) {
      if (!row.entityName || !row.dataId) return
      const { status, data } = await getModelNodeDetail(row.entityName, row.dataId)
      if (status === 'OK') {
        this.rowContent = data
      }
    },
    targetModelConfirm (visible) {
      this.targetModalVisible = visible
      if (!visible) {
        this.updateNodeInfo()
        this.renderModelGraph()
      }
    },
    targetModelSelectHandel (selection) {
      this.catchNodeTableList = selection
    },
    updateNodeInfo () {
      const currentFlow = this.flowData.flowNodes.find(i => i.nodeId === this.currentFlowNodeId)
      this.modelData.forEach(i => {
        const flowNodeIndex = i.refFlowNodeIds.indexOf(currentFlow.orderedNo)
        if (flowNodeIndex > -1) {
          i.refFlowNodeIds.splice(flowNodeIndex, 1)
        }
        this.catchNodeTableList.forEach(_ => {
          if (i.id === _.id) {
            i.refFlowNodeIds.push(currentFlow.orderedNo)
          }
        })
      })
      this.catchNodeTableList = []
    },
    async getProcessInstances (isAfterCreate = false, createResponse = undefined) {
      let { status, data } = await getProcessInstances()
      if (status === 'OK') {
        this.allFlowInstances = data.sort((a, b) => {
          return b.id - a.id
        })
        if (isAfterCreate) {
          this.selectedFlowInstance = createResponse.id
          this.processInstance()
        }
      }
    },
    async getNodeBindings (id) {
      if (!id) return
      const { status, data } = await getNodeBindings(id)
      if (status === 'OK') {
        this.flowNodesBindings = data
      }
    },
    async getAllFlow () {
      let { status, data } = await getAllFlow(false)
      if (status === 'OK') {
        this.allFlows = data.sort((a, b) => {
          let s = a.createdTime.toLowerCase()
          let t = b.createdTime.toLowerCase()
          if (s > t) return -1
          if (s < t) return 1
        })
      }
    },

    orchestrationSelectHandler () {
      this.currentFlowNodeId = ''
      this.getFlowOutlineData(this.selectedFlow)
      if (this.selectedFlow && this.isEnqueryPage === false) {
        this.showExcution = true
        this.selectedTarget = ''
        this.modelData = []
        this.renderModelGraph()
      }
    },
    async getTargetOptions () {
      // if (!(this.flowData.rootEntity || !this.flowData.entityTypeId)) return
      if (!this.flowData.rootEntity && !this.flowData.entityTypeId) return
      let pkgName = ''
      let entityName = ''
      if (this.flowData.rootEntity) {
        pkgName = this.flowData.rootEntity.split(':')[0]
        entityName = this.flowData.rootEntity.split(':')[1]
      } else {
        pkgName = this.flowData.entityTypeId.split(':')[0]
        entityName = this.flowData.entityTypeId.split(':')[1]
      }
      let { status, data } = await getTargetOptions(pkgName, entityName)
      if (status === 'OK') {
        this.allTarget = data
      }
    },
    queryHandler () {
      console.log('queryHandler', 'start clearInterval', this.timer)
      this.stop()
      console.log('queryHandler', 'end clearInterval', this.timer)
      if (!this.selectedFlowInstance) return
      this.isEnqueryPage = true
      this.$nextTick(async () => {
        const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
        if (!(found && found.id)) return
        this.processInstance()
        this.getNodeBindings(found.id)
        let { status, data } = await getProcessInstance(found.id)
        if (status === 'OK') {
          this.flowData = {
            ...data,
            flowNodes: data.taskNodeInstances
          }
          this.getTargetOptions()

          this.initFlowGraph(true)
          removeEvent('.retry', 'click', this.retryHandler)
          addEvent('.retry', 'click', this.retryHandler)
          d3.selectAll('.retry').attr('cursor', 'pointer')

          this.showExcution = false
        }

        this.selectedFlow = found.procDefId
        this.selectedTarget = found.entityDataId
        this.getModelData()
      })
    },
    queryHistory () {
      this.selectedTarget = null
      console.log('queryHistory', 'start clearInterval', this.timer)
      this.stop()
      console.log('queryHistory', 'end clearInterval', this.timer)
      this.isEnqueryPage = true
      this.showExcution = false
      this.selectedFlow = ''
      this.modelData = []
      this.flowData = {}
      this.$nextTick(() => {
        this.initModelGraph()
        this.initFlowGraph()
      })
    },
    createHandler () {
      this.selectedTarget = null
      console.log('createHandler', 'start clearInterval', this.timer)
      this.stop()
      console.log('createHandler', 'end clearInterval', this.timer)
      this.isEnqueryPage = false
      this.selectedFlowInstance = ''
      this.selectedFlow = ''
      this.modelData = []
      this.flowData = {}
      this.showExcution = false
      this.$nextTick(() => {
        this.initModelGraph()
        this.initFlowGraph()
      })
    },
    onTargetSelectHandler () {
      this.getModelData()
    },
    formatNodesBindings () {
      this.modelData.forEach(item => {
        this.flowNodesBindings.forEach(d => {
          if (d.entityDataId === item.dataId) {
            item.refFlowNodeIds.push(d.orderedNo)
          }
        })
      })
    },
    async getModelData () {
      if (!this.selectedFlow || !this.selectedTarget) return
      this.isLoading = true
      let { status, data } = await getTreePreviewData(this.selectedFlow, this.selectedTarget)
      this.isLoading = false
      if (!this.selectedTarget) return
      if (status === 'OK') {
        this.modelData = data.map(_ => {
          return {
            ..._,
            refFlowNodeIds: []
          }
        })
        if (this.isEnqueryPage) {
          this.formatNodesBindings()
        }
        this.renderModelGraph()
      }
    },
    async getFlowOutlineData (id) {
      if (!id) return
      let { status, data } = await getFlowOutlineByID(id)
      if (status === 'OK') {
        this.flowData = data
        this.initFlowGraph()
        this.getTargetOptions()
      }
    },
    renderModelGraph () {
      let nodes = this.modelData.map((_, index) => {
        const nodeId = _.packageName + '_' + _.entityName + '_' + _.dataId
        let color = _.isHighlight ? '#5DB400' : 'black'
        const isRecord = _.refFlowNodeIds.length > 0
        const shape = isRecord ? 'ellipse' : 'ellipse'
        const fontSize = Math.abs(50 - _.displayName.length) * 0.25
        const label = (_.displayName || _.dataId) + '\n' + _.refFlowNodeIds.toString().replace(/,/g, '/')
        return `${nodeId} [label="${label}" class="model" id="${nodeId}" color="${color}" style="filled" fontsize="${fontSize}" fillcolor="white" shape="${shape}"]`
      })
      let genEdge = () => {
        let pathAry = []

        this.modelData.forEach(_ => {
          if (_.succeedingIds.length > 0) {
            const nodeId = _.packageName + '_' + _.entityName + '_' + _.dataId
            let current = []
            current = _.succeedingIds.map(to => {
              return nodeId + ' -> ' + to.replace(/:/g, '_')
            })
            pathAry.push(current)
          }
        })
        return pathAry
          .flat()
          .toString()
          .replace(/,/g, ';')
      }
      let nodesToString = Array.isArray(nodes) && nodes.length > 0 ? nodes.toString().replace(/,/g, ';') + ';' : ''

      let nodesString =
        'digraph G { ' +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, shape="ellipse", fixedsize="true", width="1.6", height=".8",fontsize=12];' +
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        '}'
      this.graph.graphviz.renderDot(nodesString)
      removeEvent('.model text', 'mouseenter', this.modelGraphMouseenterHandler)
      removeEvent('.model text', 'mouseleave', this.modelGraphMouseleaveHandler)
      addEvent('.model text', 'mouseenter', this.modelGraphMouseenterHandler)
      addEvent('.model text', 'mouseleave', this.modelGraphMouseleaveHandler)
    },
    modelGraphMouseenterHandler (e) {
      clearTimeout(this.modelDetailTimer)
      this.modelDetailTimer = setTimeout(async () => {
        const found = this.modelData.find(
          _ => _.packageName + '_' + _.entityName + '_' + _.dataId === e.target.parentNode.id
        )
        this.nodeTitle = `${found.displayName}`
        const { status, data } = await getModelNodeDetail(found.entityName, found.dataId)
        if (status === 'OK') {
          this.nodeDetail = data
        }
        this.nodeDetailFullscreen = false
        this.showNodeDetail = true
        this.nodeDetailFullscreen = false
        this.tableMaxHeight = 250
      }, 1000)
    },
    modelDetailEnterHandler (e) {
      let modelDetail = document.getElementById('model_graph_detail')
      modelDetail.style.display = 'block'
    },
    modelDetailLeaveHandler (e) {
      let modelDetail = document.getElementById('model_graph_detail')
      modelDetail.style.display = 'none'
    },
    modelGraphMouseleaveHandler (e) {
      clearTimeout(this.modelDetailTimer)
      this.modelDetailLeaveHandler(e)
    },
    renderFlowGraph (excution) {
      const statusColor = {
        Completed: '#5DB400',
        deployed: '#7F8A96',
        InProgress: '#3C83F8',
        Faulted: '#FF6262',
        Timeouted: '#F7B500',
        NotStarted: '#7F8A96'
      }
      let nodes =
        this.flowData &&
        this.flowData.flowNodes &&
        this.flowData.flowNodes
          .filter(i => i.status !== 'predeploy')
          .map((_, index) => {
            if (_.nodeType === 'startEvent' || _.nodeType === 'endEvent') {
              return `${_.nodeId} [label="${_.nodeName || _.nodeType}", fontsize="10", class="flow",style="${
                excution ? 'filled' : 'none'
              }" color="${excution ? statusColor[_.status] : '#7F8A96'}" shape="circle", id="${_.nodeId}"]`
            } else {
              const className = _.status === 'Faulted' || _.status === 'Timeouted' ? 'retry' : ''
              return `${_.nodeId} [fixedsize=false label="${(_.orderedNo ? _.orderedNo + '、' : '') +
                _.nodeName}" class="flow ${className}" style="${excution ? 'filled' : 'none'}" color="${
                excution ? statusColor[_.status] : _.nodeId === this.currentFlowNodeId ? '#5DB400' : '#7F8A96'
              }"  shape="box" id="${_.nodeId}" ]`
            }
          })
      let genEdge = () => {
        let pathAry = []
        this.flowData &&
          this.flowData.flowNodes &&
          this.flowData.flowNodes.forEach(_ => {
            if (_.succeedingNodeIds.length > 0) {
              let current = []
              current = _.succeedingNodeIds.map(to => {
                return _.nodeId + ' -> ' + `${to} [color="${excution ? statusColor[_.status] : 'black'}"]`
              })
              pathAry.push(current)
            }
          })
        return pathAry
          .flat()
          .toString()
          .replace(/,/g, ';')
      }
      let nodesToString = Array.isArray(nodes) ? nodes.toString().replace(/,/g, ';') + ';' : ''
      let nodesString =
        'digraph G {' +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, height=".3", fontsize=12];' +
        'Edge [fontname=Arial, color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        '}'

      this.flowGraph.graphviz.renderDot(nodesString)
      this.bindFlowEvent()
    },
    async excutionFlow () {
      // 区分已存在的flowInstance执行 和 新建的执行
      if (this.isEnqueryPage) {
        this.processInstance()
        this.showExcution = false
      } else {
        this.isExecuteActive = true
        const currentTarget = this.allTarget.find(_ => _.id === this.selectedTarget)
        let taskNodeBinds = []
        this.modelData.forEach(_ => {
          let temp = []
          _.refFlowNodeIds.forEach(i => {
            temp.push({
              ..._,
              flowOrderNo: i
            })
          })
          taskNodeBinds = taskNodeBinds.concat(temp)
        })

        let payload = {
          entityDataId: currentTarget.id,
          entityTypeId: this.flowData.rootEntity,
          procDefId: this.flowData.procDefId,
          taskNodeBinds: taskNodeBinds.map(_ => {
            const node = this.flowData.flowNodes.find(node => node.orderedNo === _.flowOrderNo)
            return {
              entityDataId: _.dataId,
              entityTypeId: this.flowData.rootEntity,
              nodeDefId: (node && node.nodeDefId) || '',
              orderedNo: _.flowOrderNo
            }
          })
        }
        let { status, data } = await createFlowInstance(payload)
        if (status === 'OK') {
          this.getProcessInstances(true, data)
          this.isExecuteActive = false
          this.showExcution = false
          this.isEnqueryPage = true
        }
      }
    },
    start () {
      if (this.timer === null) {
        this.getStatus()
      }
      if (this.timer !== null) {
        this.stop()
      }
      this.timer = setInterval(() => {
        this.getStatus()
      }, 5000)
      console.log('TIMER ', this.timer)
    },
    stop () {
      console.log('stop', 'start clearInterval', this.timer)
      clearInterval(this.timer)
      console.log('stop', 'end clearInterval', this.timer)
    },
    async getStatus () {
      const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
      if (!(found && found.id)) return
      let { status, data } = await getProcessInstance(found.id)
      if (status === 'OK') {
        this.flowData = {
          ...data,
          flowNodes: data.taskNodeInstances
        }
        this.initFlowGraph(true)
        removeEvent('.retry', 'click', this.retryHandler)
        addEvent('.retry', 'click', this.retryHandler)
        d3.selectAll('.retry').attr('cursor', 'pointer')
        if (data.status === 'Completed') {
          this.stop()
        }
      }
    },
    processInstance () {
      console.log('processInstance', 'start set timer', this.timer)
      this.start()
      console.log('processInstance', 'end set timer', this.timer)
    },
    retryHandler (e) {
      this.currentFailedNodeID = e.target.parentNode.getAttribute('id')
      this.workflowActionModalVisible = true
      this.targetModalVisible = false
      this.showNodeDetail = false
    },
    async workFlowActionHandler (type) {
      const found = this.flowData.flowNodes.find(_ => _.nodeId === this.currentFailedNodeID)
      if (!found) {
        return
      }
      const payload = {
        act: type,
        nodeInstId: found.id,
        procInstId: found.procInstId
      }
      const { status } = await retryProcessInstance(payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: (type === 'retry' ? 'Retry' : 'Skip') + ' action is proceed successfully'
        })
        this.workflowActionModalVisible = false
        this.processInstance()
      }
    },
    bindFlowEvent () {
      if (this.isEnqueryPage !== true) {
        addEvent('.flow', 'mouseover', e => {
          e.preventDefault()
          e.stopPropagation()
          d3.selectAll('g').attr('cursor', 'pointer')
        })
        removeEvent('.flow', 'click', this.flowNodesClickHandler)
        addEvent('.flow', 'click', this.flowNodesClickHandler)
      } else {
        removeEvent('.flow text', 'mouseenter', this.flowGraphMouseenterHandler)
        removeEvent('.flow text', 'mouseleave', this.flowGraphLeaveHandler)
        addEvent('.flow text', 'mouseenter', this.flowGraphMouseenterHandler)
        addEvent('.flow text', 'mouseleave', this.flowGraphLeaveHandler)
      }
    },
    flowGraphLeaveHandler (e) {
      clearTimeout(this.flowDetailTimer)
      this.flowDetailLeaveHandler()
    },
    flowGraphMouseenterHandler (e) {
      clearTimeout(this.flowDetailTimer)
      this.flowDetailTimer = setTimeout(async () => {
        const found = this.flowData.flowNodes.find(_ => _.nodeId === e.target.parentNode.id)
        this.nodeTitle = (found.orderedNo ? found.orderedNo + '、' : '') + found.nodeName
        const { status, data } = await getNodeContext(found.procInstId, found.id)
        if (status === 'OK') {
          this.nodeDetail = data
          this.nodeDetailResponseHeader = JSON.parse(JSON.stringify(data))
          delete this.nodeDetailResponseHeader.requestObjects
          this.nodeDetailResponseHeader = JSON.stringify(this.nodeDetailResponseHeader)
            .split(',')
            .join(',<br/>')
          this.nodeDetailIO = data.requestObjects
        }
        this.nodeDetailFullscreen = false
        this.showNodeDetail = true
        this.nodeDetailFullscreen = false
        this.tableMaxHeight = 250
      }, 1000)
    },
    flowDetailEnterHandler (e) {
      let modelDetail = document.getElementById('flow_graph_detail')
      modelDetail.style.display = 'block'
    },
    flowDetailLeaveHandler (e) {
      let modelDetail = document.getElementById('flow_graph_detail')
      modelDetail.style.display = 'none'
    },
    flowNodesClickHandler (e) {
      e.preventDefault()
      e.stopPropagation()
      let g = e.currentTarget
      this.highlightModel(g.id)
      this.currentFlowNodeId = g.id
      const currentNode = this.flowData.flowNodes.find(_ => {
        return _.nodeId === this.currentFlowNodeId
      })
      this.currentNodeTitle = `${currentNode.orderedNo}、${currentNode.nodeName}`
      this.renderFlowGraph()
    },
    highlightModel (nodeId) {
      const routineExpression = this.flowData.flowNodes.find(item => item.nodeId === nodeId).routineExpression
      if (routineExpression) {
        this.foundRefAry = routineExpression.split(/[~.>()]/).filter(i => i.length > 0)
      } else {
        this.$Message.info(this.$t('no_result'))
        this.targetModalVisible = false
        return
      }
      this.tartetModels = JSON.parse(
        JSON.stringify(
          this.modelData.filter(_ => this.foundRefAry[this.foundRefAry.length - 1].split(':')[1] === _.entityName)
        )
      )
      this.targetModalVisible = true
      this.showNodeDetail = false
      this.$nextTick(() => {
        let objData = this.$refs.selection.objData
        const currentFlow = this.flowData.flowNodes.find(i => i.nodeId === this.currentFlowNodeId)
        this.modelData.forEach(_ => {
          const flowNodeIndex = _.refFlowNodeIds.indexOf(currentFlow.orderedNo)
          Object.keys(objData).forEach(i => {
            if (_.id === objData[i].id && flowNodeIndex > -1) {
              objData[i]._isChecked = true
              this.catchNodeTableList.push(objData[i])
            }
          })
        })
      })
    },
    initModelGraph () {
      const graphEl = document.getElementById('graph')
      const initEvent = () => {
        let graph
        graph = d3.select(`#graph`)
        graph
          .on('dblclick.zoom', null)
          .on('wheel.zoom', null)
          .on('mousewheel.zoom', null)
        this.graph.graphviz = graph
          .graphviz()
          .fit(true)
          .zoom(true)
          .height(graphEl.offsetHeight - 10)
          .width(graphEl.offsetWidth - 10)
      }
      initEvent()
      this.renderModelGraph()
    },
    initFlowGraph (excution = false) {
      const graphEl = document.getElementById('flow')
      const initEvent = () => {
        let graph
        graph = d3.select(`#flow`)
        graph.on('dblclick.zoom', null)
        this.flowGraph.graphviz = graph
          .graphviz()
          .fit(true)
          .zoom(false)
          .height(graphEl.offsetHeight - 10)
          .width(graphEl.offsetWidth - 10)
      }
      initEvent()
      this.renderFlowGraph(excution)
    },
    zoomModal () {
      this.tableMaxHeight = document.body.scrollHeight - 410
      this.nodeDetailFullscreen = true
    }
  }
}
</script>
<style lang="scss" scoped>
body {
  color: #15a043;
}
#graphcontain {
  border: 1px solid #d3cece;
  border-radius: 3px;
  padding: 5px;
  height: calc(100vh - 210px);
}
.model_target .ivu-modal-content-drag {
  right: 40px;
}
.pages /deep/ .ivu-card-body {
  padding: 8px;
}
.ivu-form-item {
  margin-bottom: 0 !important;
}
.excution-serach {
  margin: 5px 6px 0 0;
}
.graph-container {
  overflow: auto;
}
#model_graph_detail {
  display: none;
  width: 600px;
  position: absolute;
  background-color: white;
  padding: 5px 5px;
  box-shadow: 0 0 5px grey;
  overflow: auto;
}
#flow_graph_detail {
  display: none;
  width: 600px;
  position: absolute;
  background-color: white;
  padding: 5px 5px;
  box-shadow: 0 0 5px grey;
  overflow: auto;
}
.header-icon {
  float: right;
  margin: 3px 20px 0 0;
}
</style>
