<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form v-if="isEnqueryPage" label-position="left">
            <FormItem :label-width="150" :label="$t('orchs')">
              <Select v-model="selectedFlowInstance" style="width:70%" filterable>
                <Option
                  v-for="item in allFlowInstances"
                  :value="item.id"
                  :key="item.id"
                  :label="
                    item.procInstName +
                      ' ' +
                      item.entityDisplayName +
                      ' ' +
                      (item.createdTime || 'createdTime') +
                      ' ' +
                      (item.operator || 'operator')
                  "
                >
                  <span>
                    <span style="color:#2b85e4">{{ item.procInstName + ' ' }}</span>
                    <span style="color:#515a6e">{{ item.entityDisplayName + ' ' }}</span>
                    <span style="color:#ccc;float:right">{{ (item.createdTime || 'createdTime') + ' ' }}</span>
                    <span style="float:right;color:#515a6e;margin-right:20px">{{ item.operator || 'operator' }}</span>
                  </span>
                </Option>
              </Select>
              <Button type="info" @click="queryHandler">{{ $t('query_orch') }}</Button>
            </FormItem>
          </Form>
        </Col>
        <Col span="4" style="text-align: right;margin-bottom:8px;padding-right:40px;float:right;">
          <Button type="info" v-if="!isEnqueryPage" @click="queryHistory">{{ $t('enquery_new_workflow_job') }}</Button>
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
        <Button type="info" @click="workFlowActionHandler('showlog')" style="margin-left: 20px">{{
          $t('show_log')
        }}</Button>
      </div>
    </Modal>
    <Modal
      :title="currentNodeTitle"
      v-model="targetModalVisible"
      :scrollable="true"
      :mask="false"
      :mask-closable="false"
      class="model_target"
      width="50"
      @on-ok="targetModelConfirm"
    >
      <Input v-model="tableFilterParam" placeholder="displayName filter" style="width: 300px;margin-bottom:8px;" />
      {{ catchNodeTableList.length }}
      <Table
        border
        ref="selection"
        max-height="300"
        @on-select="singleSelect"
        @on-select-cancel="singleCancle"
        @on-select-all-cancel="selectAllCancle"
        @on-select-all="selectAll"
        :columns="targetModelColums"
        :data="tartetModels"
      >
        <template slot-scope="{ row, index }" slot="action">
          <Tooltip
            placement="bottom"
            theme="light"
            trigger="click"
            @on-popper-show="getDetail(row)"
            :delay="500"
            max-width="500"
          >
            <Button type="warning" size="small">View</Button>
            <div slot="content">
              <pre style="max-height: 500px;"><span>{{rowContent}}</span></pre>
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
      <div v-if="!isTargetNodeDetail" :style="[{ overflow: 'auto' }, fullscreenModalContentStyle]">
        <h5>Data:</h5>
        <pre style="margin: 0 6px 6px" v-html="nodeDetailResponseHeader"></pre>
        <h5>requestObjects:</h5>
        <Table :columns="nodeDetailColumns" :max-height="tableMaxHeight" tooltip="true" :data="nodeDetailIO"></Table>
      </div>
      <div
        v-else
        :style="[{ overflow: 'auto', margin: '0 6px 6px' }, fullscreenModalContentStyle]"
        v-html="nodeDetail"
      ></div>
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
  // getTargetOptions,
  getTreePreviewData,
  createFlowInstance,
  getProcessInstances,
  getProcessInstance,
  retryProcessInstance,
  getModelNodeDetail,
  getNodeBindings,
  getNodeContext,
  getDataByNodeDefIdAndProcessSessionId,
  setDataByNodeDefIdAndProcessSessionId,
  getAllBindingsProcessSessionId,
  getTargetModelByProcessDefId,
  getPreviewEntitiesByInstancesId
} from '@/api/server'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
import { addEvent, removeEvent } from '../util/event.js'
export default {
  data () {
    return {
      currentModelNodeRefs: [],
      showNodeDetail: false,
      isTargetNodeDetail: false,
      nodeDetailFullscreen: false,
      fullscreenModalContentStyle: { 'max-height': '400px' },
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
      selectedFlowInstance: '',
      selectedFlow: '',
      selectedTarget: '',
      showExcution: true,
      isExecuteActive: false,
      isEnqueryPage: false,
      workflowActionModalVisible: false,
      targetModalVisible: false,
      tableFilterParam: null,
      tartetModels: [],
      catchTartetModels: [],
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
      catchNodeTableList: [],
      processSessionId: '',
      allBindingsList: []
    }
  },
  watch: {
    targetModalVisible: function (val) {
      this.tableFilterParam = null
      if (!val) {
        this.catchNodeTableList = []
      }
    },
    tableFilterParam: function (filter) {
      if (!filter) {
        this.tartetModels = this.catchTartetModels
      } else {
        this.tartetModels = this.catchTartetModels.filter(item => {
          return item.displayName.includes(filter)
        })
      }
      this.tartetModels.forEach(tm => {
        tm._checked = false
        this.catchNodeTableList.forEach(cn => {
          if (tm.id === cn.id) {
            tm._checked = true
          }
        })
      })
    },
    nodeDetailFullscreen: function (tag) {
      tag ? (this.fullscreenModalContentStyle = {}) : (this.fullscreenModalContentStyle['max-height'] = '400px')
    }
  },
  mounted () {
    this.getProcessInstances()
    this.getAllFlow()
    this.createHandler()
  },
  destroyed () {
    clearInterval(this.timer)
  },
  methods: {
    async getDetail (row) {
      if (!row.entityName || !row.dataId) return
      const { status, data } = await getModelNodeDetail(row.entityName, row.dataId)
      if (status === 'OK') {
        this.rowContent = data
      }
    },
    async targetModelConfirm (visible) {
      // TODO:
      this.targetModalVisible = visible
      if (!visible) {
        await this.updateNodeInfo()
        this.formatRefNodeIds()
        this.renderModelGraph()
      }
    },
    singleSelect (selection, row) {
      this.catchNodeTableList = this.catchNodeTableList.concat(row)
    },
    singleCancle (selection, row) {
      const index = this.catchNodeTableList.findIndex(cn => {
        return cn.id === row.id
      })
      this.catchNodeTableList.splice(index, 1)
    },
    selectAll (selection) {
      let temp = []
      this.catchNodeTableList.forEach(cntl => {
        temp.push(cntl.id)
      })
      selection.forEach(se => {
        if (!temp.includes(se.id)) {
          this.catchNodeTableList.push(se)
        }
      })
    },
    selectAllCancle () {
      let temp = []
      this.tartetModels.forEach(tm => {
        temp.push(tm.id)
      })
      if (this.tableFilterParam) {
        this.catchNodeTableList = this.catchNodeTableList.filter(item => {
          return !temp.includes(item.id)
        })
      } else {
        this.catchNodeTableList = []
      }
    },
    async updateNodeInfo () {
      const currentNode = this.flowData.flowNodes.find(_ => {
        return _.nodeId === this.currentFlowNodeId
      })
      const payload = this.catchNodeTableList.map(_ => {
        return { ..._, bound: 'Y' }
      })
      await setDataByNodeDefIdAndProcessSessionId(currentNode.nodeDefId, this.processSessionId, payload)
      const filter = this.allBindingsList.filter(_ => _.nodeDefId !== currentNode.nodeDefId)
      this.allBindingsList = filter.concat(payload)
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
        this.formatRefNodeIds()
        this.renderModelGraph()
      }
    },
    async getTargetOptions () {
      if (!(this.selectedFlow && this.selectedFlow.length > 0)) return
      const { status, data } = await getTargetModelByProcessDefId(this.selectedFlow)
      if (status === 'OK') {
        this.allTarget = data
      }
    },
    queryHandler () {
      this.stop()
      if (!this.selectedFlowInstance) return
      this.isEnqueryPage = true
      this.$nextTick(async () => {
        const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
        if (!(found && found.id)) return
        this.selectedFlow = found.procDefId
        this.selectedTarget = found.entityDataId
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
          removeEvent('.normal', 'click', this.normalHandler)
          addEvent('.normal', 'click', this.normalHandler)
          d3.selectAll('.retry').attr('cursor', 'pointer')

          this.showExcution = false
        }
        this.getModelData()
      })
    },
    queryHistory () {
      this.selectedTarget = null
      this.stop()
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
      this.stop()
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
    async getModelData () {
      this.modelData = []
      if ((!this.selectedFlow || !this.selectedTarget) && !this.isEnqueryPage) {
        this.renderModelGraph()
        return
      }
      this.isLoading = true
      let { status, data } = this.isEnqueryPage
        ? await getPreviewEntitiesByInstancesId(this.selectedFlowInstance)
        : await getTreePreviewData(this.selectedFlow, this.selectedTarget)
      this.isLoading = false
      if (!this.selectedTarget && !this.isEnqueryPage) return
      if (status === 'OK') {
        this.processSessionId = data.processSessionId
        const binds = await getAllBindingsProcessSessionId(data.processSessionId)
        this.allBindingsList = binds.data
        this.modelData = data.entityTreeNodes.map(_ => {
          return {
            ..._,
            refFlowNodeIds: []
          }
        })
        this.formatRefNodeIds()
      }
      this.renderModelGraph()
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
    formatRefNodeIds () {
      this.modelData.forEach(i => {
        i.refFlowNodeIds = []
        this.allBindingsList.forEach(j => {
          if (j.entityDataId === i.dataId) {
            i.refFlowNodeIds.push(j.orderedNo)
          }
        })
      })
    },
    renderModelGraph () {
      let nodes = this.modelData.map((_, index) => {
        const nodeId = _.packageName + '_' + _.entityName + '_' + _.dataId
        let color = _.isHighlight ? '#5DB400' : 'black'
        // const isRecord = _.refFlowNodeIds.length > 0
        // const shape = isRecord ? 'ellipse' : 'ellipse'
        const str = _.displayName || _.dataId
        const refStr = _.refFlowNodeIds.toString().replace(/,/g, '/')
        // const len = refStr.length - _.displayName.length > 0 ? refStr.length : _.displayName.length
        const firstLabel = str.length > 15 ? `${str.slice(0, 1)}...${str.slice(-14)}` : str
        // const fontSize = Math.min((58 / len) * 3, 16)
        const label = firstLabel + '\n' + refStr
        return `${nodeId} [label="${label}" class="model" id="${nodeId}" color="${color}" style="filled" fillcolor="white" shape="box"]`
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
        'Node [fontname=Arial, shape="ellipse"];' +
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        '}'
      this.graph.graphviz.transition().renderDot(nodesString)
      // .on('end', this.setFontSizeForText)
      removeEvent('.model text', 'mouseenter', this.modelGraphMouseenterHandler)
      removeEvent('.model text', 'mouseleave', this.modelGraphMouseleaveHandler)
      removeEvent('.model text', 'click', this.modelGraphClickHandler)
      removeEvent('#graph svg', 'click', this.resetcurrentModelNodeRefs)
      addEvent('.model text', 'click', this.modelGraphClickHandler)
      addEvent('#graph svg', 'click', this.resetcurrentModelNodeRefs)
      addEvent('.model text', 'mouseenter', this.modelGraphMouseenterHandler)
      addEvent('.model text', 'mouseleave', this.modelGraphMouseleaveHandler)
    },
    setFontSizeForText () {
      const nondes = d3.selectAll('#graph svg g .node')._groups[0]
      for (let i = 0; i < nondes.length; i++) {
        const len = nondes[i].children[2].innerHTML.replace(/&nbsp;/g, '').length
        const fontsize = Math.min((nondes[i].children[1].rx.baseVal.value / len) * 3, 16)
        for (let j = 2; j < nondes[i].children.length; j++) {
          nondes[i].children[j].setAttribute('font-size', fontsize)
        }
      }
    },
    resetcurrentModelNodeRefs () {
      if (!this.isEnqueryPage) {
        this.currentModelNodeRefs = []
        this.renderFlowGraph()
      }
    },
    modelGraphClickHandler (e) {
      e.preventDefault()
      e.stopPropagation()
      if (!this.isEnqueryPage) {
        const refEle = e.target.parentNode.children[3]
        if (refEle) {
          this.currentModelNodeRefs = refEle.innerHTML.trim().split('/')
          this.renderFlowGraph()
        }
      }
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
          this.nodeDetail = JSON.stringify(data)
            .split(',')
            .join(',<br/>')
        }
        this.isTargetNodeDetail = true
        this.nodeDetailFullscreen = false
        this.showNodeDetail = true
        this.nodeDetailFullscreen = false
        this.tableMaxHeight = 250
      }, 1300)
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
              const defaultLabel = _.nodeType === 'startEvent' ? 'start' : 'end'
              return `${_.nodeId} [label="${_.nodeName || defaultLabel}", fontsize="10", class="flow",style="${
                excution ? 'filled' : 'none'
              }" color="${excution ? statusColor[_.status] : '#7F8A96'}" shape="circle", id="${_.nodeId}"]`
            } else {
              const className = _.status === 'Faulted' || _.status === 'Timeouted' ? 'retry' : 'normal'
              const isModelClick = this.currentModelNodeRefs.indexOf(_.orderedNo) > -1
              return `${_.nodeId} [fixedsize=false label="${(_.orderedNo ? _.orderedNo + ' ' : '') +
                _.nodeName}" class="flow ${className}" style="${excution || isModelClick ? 'filled' : 'none'}" color="${
                excution
                  ? statusColor[_.status]
                  : isModelClick
                    ? '#ff9900'
                    : _.nodeId === this.currentFlowNodeId
                      ? '#5DB400'
                      : '#7F8A96'
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

      this.flowGraph.graphviz.transition().renderDot(nodesString)
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
          processSessionId: this.processSessionId,
          entityDisplayName: currentTarget.displayName,
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
    },
    stop () {
      clearInterval(this.timer)
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
        removeEvent('.normal', 'click', this.normalHandler)
        addEvent('.normal', 'click', this.normalHandler)
        d3.selectAll('.retry').attr('cursor', 'pointer')
        if (data.status === 'Completed') {
          this.stop()
        }
      }
    },
    processInstance () {
      this.start()
    },
    retryHandler (e) {
      this.currentFailedNodeID = e.target.parentNode.getAttribute('id')
      this.workflowActionModalVisible = true
      this.targetModalVisible = false
      this.showNodeDetail = false
    },
    normalHandler (e) {
      this.flowGraphMouseenterHandler(e.target.parentNode.getAttribute('id'))
    },
    async workFlowActionHandler (type) {
      const found = this.flowData.flowNodes.find(_ => _.nodeId === this.currentFailedNodeID)
      if (!found) {
        return
      }
      if (type === 'showlog') {
        this.flowGraphMouseenterHandler(this.currentFailedNodeID)
      } else {
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
        removeEvent('.flow', 'click', this.flowNodesClickHandler)
        // removeEvent('.flow text', 'mouseenter', this.flowGraphMouseenterHandler)
        removeEvent('.flow text', 'mouseleave', this.flowGraphLeaveHandler)
        // addEvent('.flow text', 'mouseenter', this.flowGraphMouseenterHandler)
        addEvent('.flow text', 'mouseleave', this.flowGraphLeaveHandler)
      }
    },
    flowGraphLeaveHandler (e) {
      clearTimeout(this.flowDetailTimer)
      this.flowDetailLeaveHandler()
    },
    flowGraphMouseenterHandler (id) {
      // Task_0f9a25l
      clearTimeout(this.flowDetailTimer)
      this.flowDetailTimer = setTimeout(async () => {
        const found = this.flowData.flowNodes.find(_ => _.nodeId === id)
        this.nodeTitle = (found.orderedNo ? found.orderedNo + '、' : '') + found.nodeName
        const { status, data } = await getNodeContext(found.procInstId, found.id)
        if (status === 'OK') {
          this.workflowActionModalVisible = false
          this.nodeDetailResponseHeader = JSON.parse(JSON.stringify(data))
          delete this.nodeDetailResponseHeader.requestObjects
          this.nodeDetailResponseHeader = JSON.stringify(this.replaceParams(this.nodeDetailResponseHeader))
            .split(',')
            .join(',<br/>')
          this.nodeDetailIO = data.requestObjects.map(ro => {
            ro['inputs'] = this.replaceParams(ro['inputs'])
            ro['outputs'] = this.replaceParams(ro['outputs'])
            return ro
          })
        }
        this.nodeDetailFullscreen = false
        this.isTargetNodeDetail = false
        this.showNodeDetail = true
        this.tableMaxHeight = 250
      }, 1000)
    },
    replaceParams (obj) {
      let placeholder = new Array(16).fill('&nbsp;')
      placeholder.unshift('<br/>')
      for (let key in obj) {
        if (obj[key] !== null && typeof obj[key] === 'string') {
          obj[key] = obj[key].replace('\r\n', placeholder.join(''))
        }
      }
      return obj
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
      this.currentFlowNodeId = g.id
      const currentNode = this.flowData.flowNodes.find(_ => {
        return _.nodeId === this.currentFlowNodeId
      })
      this.currentNodeTitle = `${currentNode.orderedNo}、${currentNode.nodeName}`
      this.highlightModel(g.id, currentNode.nodeDefId)
      this.renderFlowGraph()
    },
    async highlightModel (nodeId, nodeDefId) {
      console.log(this.processSessionId)
      if (nodeDefId && this.processSessionId) {
        let { status, data } = await getDataByNodeDefIdAndProcessSessionId(nodeDefId, this.processSessionId)
        if (status === 'OK') {
          this.tartetModels = data.map(_ => {
            return {
              ..._,
              ...this.modelData.find(j => j.dataId === _.entityDataId)
            }
          })
        } else {
          this.tartetModels = []
        }
      } else {
        return
      }

      this.catchTartetModels = []
      this.catchNodeTableList = []
      this.catchTartetModels = JSON.parse(JSON.stringify(this.tartetModels))
      this.targetModalVisible = true
      this.showNodeDetail = false
      this.$nextTick(() => {
        let objData = this.$refs.selection.objData
        Object.keys(objData).forEach(i => {
          this.allBindingsList.forEach(j => {
            if (j.nodeDefId === nodeDefId && j.entityDataId === objData[i].entityDataId) {
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
      this.formatRefNodeIds()
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
          .zoom(true)
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
  color: #e5f173; //#15a043;
}
.header-icon {
  margin: 3px 40px 0 0 !important;
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
