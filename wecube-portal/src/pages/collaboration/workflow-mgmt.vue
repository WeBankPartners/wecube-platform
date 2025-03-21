<template>
  <div class="root">
    <FlowHeader
      @openCanvasPanel="openCanvasPanel"
      @updateAuth="updateAuth"
      @updateFlowData="updateFlowData"
      ref="headerInfoRef"
    ></FlowHeader>
    <!-- v-show="isShowGraph" -->
    <div class="canvas-zone">
      <!-- 左侧按钮 -->
      <item-panel ref="itemPanelRef" />
      <div class="floating-button">
        <Button size="small" @click="resetCanvas">Reset</Button>
      </div>
      <!-- 挂载节点 -->
      <div id="canvasPanel" ref="canvasPanel" @dragover.prevent></div>
      <!-- 信息配置 -->
      <ItemInfoCanvas
        v-show="itemInfoType === 'canvas'"
        ref="itemInfoCanvasRef"
        @sendItemInfo="setCanvasInfo"
        @hideItemInfo="hideItemInfo"
      ></ItemInfoCanvas>
      <ItemInfoNode
        v-if="itemInfoType === 'node'"
        ref="itemInfoNodeRef"
        @sendItemInfo="setNodeInfo"
        @hideItemInfo="hideItemInfo"
        @hideReleaseBtn="hideReleaseBtn"
      >
      </ItemInfoNode>
      <ItemInfoEdge
        v-show="itemInfoType === 'edge'"
        ref="itemInfoEdgeRef"
        @sendItemInfo="setEdgeInfo"
        @hideItemInfo="hideItemInfo"
      >
      </ItemInfoEdge>
    </div>
  </div>
</template>

<script>
import G6 from '@antv/g6'
import FlowHeader from '@/pages/collaboration/flow/flow-header.vue'
import registerFactory from './flow/graph/graph'
import ItemPanel from '@/pages/collaboration/flow/item-panel.vue'
import ItemInfoCanvas from '@/pages/collaboration/flow/item-info-canvas.vue'
import ItemInfoEdge from '@/pages/collaboration/flow/item-info-edge.vue'
import ItemInfoNode from '@/pages/collaboration/flow/item-info-node.vue'
import { nodeDefaultAttr } from './flow/node-default-attr.js'
import {
  getFlowById, flowMgmt, flowNodeMgmt, flowEdgeMgmt, flowNodeDelete, flowEdgeDelete
} from '@/api/server.js'

export default {
  components: {
    FlowHeader,
    ItemPanel,
    ItemInfoCanvas,
    ItemInfoNode,
    ItemInfoEdge
  },
  data() {
    return {
      flowListTab: '', // 记录跳转过来时列表的tab位置
      editFlow: true, // 在查看时隐藏按钮
      canRemovedId: '',
      demoFlowId: '',
      isAdd: this.$route.query.isAdd || 'false',
      isShowGraph: false,
      itemInfoType: '', // canvas、node、edge
      mode: 'drag-shadow-node',
      graph: {},
      highLight: {
        undo: false,
        redo: false
      },
      // 保存线条样式
      lineStyle: {
        type: 'line',
        width: 1
      },
      label: '',
      labelCfg: {
        fontSize: 12,
        style: {
          fill: '#fff'
        }
      },
      node: {
        fill: '',
        lineDash: 'none',
        borderColor: '',
        width: 160,
        height: 60,
        shape: 'rect-node'
      },
      headVisible: false,
      isMouseDown: false,
      config: '',
      tooltip: '',
      top: 0,
      left: 0,

      procDef: {
        // 仅列出两个基础信息
        id: '',
        label: '',
        permissionToRole: {} // 授权信息
      }, // 编排自身属性
      nodesAndDeges: {
        nodes: [],
        edges: []
      } // 节点和边信息
    }
  },
  async mounted() {
    if (this.$route.query.flowId) {
      this.flowListTab = this.$route.query.flowListTab
      this.demoFlowId = this.$route.query.flowId
      this.editFlow = this.$route.query.editFlow || true
      await this.getFlowInfo(this.demoFlowId)
      // 创建画布
      this.$nextTick(() => {
        this.$refs.itemPanelRef.setEditFlowStatus(this.editFlow)
        this.createGraphic()
        this.initGraphEvent()
        this.openCanvasPanel()
      })
    } else {
      this.$router.push({
        path: '/collaboration/workflow',
        query: { flowListTab: this.flowListTab }
      })
    }
  },
  methods: {
    // 更新编排信息
    updateFlowData() {
      this.getFlowInfo(this.demoFlowId)
    },
    // 画布属性展开
    openCanvasPanel() {
      this.itemInfoType = 'canvas'
      this.$refs.itemInfoCanvasRef.showItemInfo(this.procDef, this.editFlow)
    },
    async getFlowInfo(id) {
      const { status, data } = await getFlowById(id)
      if (status === 'OK') {
        this.permissionToRole = data.permissionToRole
        this.procDef = data.procDef
        this.procDef.label = data.procDef.name || ''
        this.procDef.permissionToRole = data.permissionToRole

        this.mgmtNodesAndEdges(data.taskNodeInfos)
        this.$refs.headerInfoRef.showItemInfo(this.procDef, this.editFlow, this.flowListTab)
      }
    },
    hideReleaseBtn() {
      this.$refs.headerInfoRef.hideReleaseBtn()
    },
    // 整理编排节点与边数据结构
    mgmtNodesAndEdges(info) {
      if (info.nodes && info.nodes.length > 0) {
        this.nodesAndDeges.nodes = info.nodes.map(n => {
          const customAttrs = n.customAttrs
          if (customAttrs.timeConfig) {
            customAttrs.timeConfig = JSON.parse(customAttrs.timeConfig)
          }
          const selfAttrs = JSON.parse(n.selfAttrs)
          // selfAttrs.logoIcon.img = ''
          return {
            ...selfAttrs,
            customAttrs
          }
        })
      }
      if (info.edges && info.edges.length > 0) {
        this.nodesAndDeges.edges = info.edges.map(n => ({
          ...JSON.parse(n.selfAttrs),
          customAttrs: n.customAttrs
        }))
      }
    },
    resetCanvas() {
      this.graph.zoomTo(1) // 缩放到原始大小
    },
    createGraphic() {
      const grid = new G6.Grid()
      const cfg = registerFactory(G6, {
        width: window.innerWidth - 70,
        height: window.innerHeight - 160,
        // renderer: 'svg',
        layout: {
          type: '' // 位置将固定
        },
        // 所有边的默认配置
        defaultEdge: {
          type: 'polyline-edge', // 扩展了内置边, 有边的事件
          style: {
            radius: 5,
            offset: 15,
            stroke: '#303030',
            lineWidth: 1,
            lineAppendWidth: 10, // 防止线太细没法点中
            endArrow: true
          }
        },
        // 覆盖全局样式
        nodeStateStyles: {
          'nodeState:default': {
            opacity: 1,
            stroke: '#303030',
            fill: 'white'
          },
          // 'nodeState:hover': {
          //   opacity: 0.8,
          //   stroke: '#303030'
          // },
          'nodeState:selected': {
            opacity: 0.9,
            stroke: '#303030',
            fill: '#1890FF'
          }
        },
        // 默认边不同状态下的样式集合
        edgeStateStyles: {
          'edgeState:default': {
            stroke: '#303030'
          },
          'edgeState:selected': {
            stroke: '#1890FF'
          },
          'edgeState:hover': {
            animate: true,
            animationType: 'dash',
            stroke: '#303030'
          }
        },
        modes: {
          // 支持的 behavior
          default: [
            'zoom-canvas',
            'drag-canvas',
            'drag-shadow-node',
            'canvas-event',
            'delete-item',
            'select-node',
            'hover-node',
            'active-edge'
          ],
          originDrag: [
            'drag-canvas',
            'drag-node',
            'canvas-event',
            'delete-item',
            'select-node',
            'hover-node',
            'active-edge'
          ]
        },
        // plugins: [menu, minimap, grid]
        plugins: [grid]
        // ... 其他G6原生入参
      })

      this.graph = new G6.Graph(cfg)
      this.graph.read(this.nodesAndDeges) // 读取数据
      // this.graph.paint() // 渲染到页面
      // this.graph.get('canvas').set('localRefresh', false) // 关闭局部渲染
      // this.graph.fitView()
    },
    removeItem() {
      this.$Modal.confirm({
        title: this.$t('delete'),
        content: this.$t('confirm_to_delete'),
        onOk: async () => {
          const method = this.canRemovedId.startsWith('pdef_node_') ? flowNodeDelete : flowEdgeDelete
          const { status } = await method(this.procDef.id, this.canRemovedId)
          if (status === 'OK') {
            const item = this.graph.findById(this.canRemovedId)
            this.graph.removeItem(item)
            this.itemInfoType = ''
            this.deleteRemoveNode()
          }
        },
        onCancel: () => {}
      })
    },
    // #region 流程数据校验
    alreadyHasStart() {
      const findNode = this.graph.save().nodes.findIndex(node => node.customAttrs.nodeType === 'start')
      if (findNode === -1) {
        return false
      }

      // this.$Message.warning('只能存在一个开始节点！')
      this.$Message.warning(`${this.$t('saveFailed')}${this.$t('canOnlyOneStartNode')}`)
      return true
    },
    alreadyHasEnd() {
      const findNode = this.graph.save().nodes.findIndex(node => node.customAttrs.nodeType === 'end')
      if (findNode === -1) {
        return false
      }

      // this.$Message.warning('只能存在一个结束节点！')
      this.$Message.warning(`${this.$t('saveFailed')}${this.$t('canOnlyOneEndNode')}`)
      return true
    },
    // #endregion
    // 初始化图事件
    initGraphEvent() {
      this.graph.on('drop', e => {
        const { originalEvent } = e
        if (originalEvent.dataTransfer) {
          const transferData = originalEvent.dataTransfer.getData('dragComponent')
          const { nodeType } = JSON.parse(transferData)
          if (nodeType === 'start' && this.alreadyHasStart(nodeType)) {
            return
          }
          if (nodeType === 'end' && this.alreadyHasEnd(nodeType)) {
            return
          }
          if (transferData) {
            this.addNode(transferData, e)
          }
        }
      })

      this.graph.on('node:drop', e => {
        e.item.getOutEdges().forEach(edge => {
          edge.clearStates('edgeState')
        })

        setTimeout(() => {
          const id = e.item.get('model').id
          const movedNode = this.graph.save().nodes.find(n => n.id === id)
          const tmp = JSON.parse(JSON.stringify(movedNode))
          const customAttrs = tmp.customAttrs
          customAttrs.id = tmp.id
          customAttrs.name = tmp.label
          delete tmp.customAttrs
          const selfAttrs = tmp
          const finalData = {
            selfAttrs,
            customAttrs
          }
          if (this.editFlow !== 'false') {
            this.setNodeInfo(finalData, true)
          }
        }, 100)
      })
      this.graph.on('node:dragstart', e => {
        this.hideItemInfo()
        if (e && e.item) {
          // this.hideItemInfo()
        }
      })
      this.graph.on('after-node-selected', e => {
        if (e && e.item) {
          const model = e.item.get('model')
          const nodeType = model.customAttrs.nodeType
          if (nodeType === 'delete') {
            this.removeItem()
            return
          }
          if (this.editFlow !== 'false' && this.isExecutionAllowed()) {
            return
          }
          this.itemInfoType = ''
          this.$nextTick(() => {
            if (e && e.item) {
              const model = e.item.get('model')
              this.itemInfoType = 'node'
              this.$nextTick(() => {
                this.$refs.itemInfoNodeRef
                  && this.$refs.itemInfoNodeRef.showItemInfo(
                    model,
                    false,
                    this.procDef.rootEntity.split('{')[0], // 禁止向节点传递过滤条件
                    this.editFlow,
                    this.permissionToRole
                  )
              })
              this.canRemovedId = model.id

              const point = {
                x: model.x + 40,
                y: model.y - 20
              }
              if (this.editFlow !== 'false') {
                this.addRemoveNode(point)
              }
            }
          })
        }
      })

      this.graph.on('on-node-mouseenter', e => {
        if (e && e.item) {
          e.item.getOutEdges().forEach(edge => {
            edge.clearStates('edgeState')
            edge.setState('edgeState', 'hover')
          })
        }
      })

      // 鼠标拖拽到画布外时特殊处理
      this.graph.on('mousedown', () => {
        this.isMouseDown = true
      })
      this.graph.on('mouseup', () => {
        this.isMouseDown = false
      })
      this.graph.on('canvas:mouseleave', () => {
        this.graph.getNodes().forEach(x => {
          const group = x.getContainer()

          group.clearAnchor()
          x.clearStates('anchorActived')
        })
      })

      this.graph.on('on-node-mousemove', e => {
        if (e && e.item) {
          this.tooltip = e.item.get('model').id
          this.left = e.clientX + 40
          this.top = e.clientY - 20
        }
      })

      this.graph.on('on-node-mouseleave', e => {
        if (e && e.item) {
          this.tooltip = ''
          if (e && e.item) {
            e.item.getOutEdges().forEach(edge => {
              edge.clearStates('edgeState')
            })
          }
        }
      })

      this.graph.on('before-node-removed', ({ callback }) => {
        setTimeout(() => {
          callback(true)
        }, 1000)
      })

      this.graph.on('after-node-dblclick', e => {
        if (e && e.item) {
          console.error(e.item)
        }
      })

      this.graph.on('after-edge-selected', e => {
        if (e && e.item) {
          const model = e.item.get('model')
          this.config = model.id

          this.graph.updateItem(e.item, {
            // shape: 'line-edge',
            style: {
              radius: 10,
              lineWidth: 1
            }
          })
          this.canRemovedId = model.id
        }
      })

      this.graph.on('on-edge-mousemove', e => {
        if (e && e.item) {
          this.tooltip = e.item.get('model').label
          this.left = e.clientX + 40
          this.top = e.clientY - 20
        }
      })

      this.graph.on('on-edge-mouseleave', e => {
        if (e && e.item) {
          this.tooltip = ''
        }
      })

      this.graph.on('before-edge-add', ({
        source, target, sourceAnchor, targetAnchor
      }) => {
        const sourceId = source.get('id')
        const targetId = target.get('id')

        const sourceNodeType = source.get('model').customAttrs.nodeType
        const targertNodeType = target.get('model').customAttrs.nodeType
        const sourceNodeName = source.get('model').customAttrs.name
        const targertNodeName = target.get('model').customAttrs.name
        // 结束节点不能连出
        if (sourceNodeType === 'end') {
          // this.$Message.warning('结束节点不能连出！')
          this.$Message.warning(`${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('noExit')}`)
          return
        }
        // 异常节点不能连出
        if (sourceNodeType === 'abnormal') {
          // this.$Message.warning('异常节点不能连出！')
          this.$Message.warning(`${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('noExit')}`)
          return
        }

        // 分流节点连出校验
        if (sourceNodeType === 'fork') {
          // 分流节点不能直连异常节点
          if (targertNodeType === 'abnormal') {
            // this.$Message.warning('分流节点不能直连异常节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
          // 分流节点不能直连结束节点
          if (targertNodeType === 'end') {
            // this.$Message.warning('分流节点不能直连结束节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
          // 分流节点不能直连汇聚节点
          if (targertNodeType === 'merge') {
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
        }

        // 汇聚节点不能直连分流节点
        if (sourceNodeType === 'merge') {
          if (targertNodeType === 'fork') {
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
        }

        // 判断开始不能直连判断结束节点
        // if (sourceNodeType === 'decision') {
        //   if (targertNodeType === 'decisionMerge') {
        //     this.$Message.warning(
        //       `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
        //     )
        //     return
        //   }
        // }

        // 判断结束不能直连判断开始节点
        // if (sourceNodeType === 'decisionMerge') {
        //   if (targertNodeType === 'decision') {
        //     this.$Message.warning(
        //       `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
        //     )
        //     return
        //   }
        // }

        if (sourceNodeType === 'start') {
          const outEdges = source.getOutEdges()
          // 开始节点只能连出一条
          if (outEdges.length > 0) {
            // this.$Message.warning('开始节点只能有一个出口！')
            this.$Message.warning(`${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('oneExit')}`)
            return
          }
          // 开始节点不能直连判断结束节点
          if (targertNodeType === 'decisionMerge') {
            // this.$Message.warning('开始节点不能直连判断结束节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
          // 开始节点不能直连汇聚节点
          if (targertNodeType === 'merge') {
            // this.$Message.warning('开始节点不能直连汇聚节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
          // 开始节点不能直连异常节点
          if (targertNodeType === 'abnormal') {
            // this.$Message.warning('开始节点不能直连异常节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
          // 开始节点不能直连结束节点
          if (targertNodeType === 'end') {
            // this.$Message.warning('开始节点不能直连结束节点！')
            this.$Message.warning(
              `${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('cannotBeDirectlyConnectedTo')}[${targertNodeName}]`
            )
            return
          }
        }
        // 结束节点只能连入
        if (targertNodeType === 'end') {
          const inEdges = target.getInEdges()
          if (inEdges.length > 0) {
            // this.$Message.warning('结束节点只能有一个入口！')
            this.$Message.warning(`${this.$t('saveFailed')}[${targertNodeName}]${this.$t('oneEntrance')}`)
            return
          }
        }
        // 开始节点不能连入
        if (targertNodeType === 'start') {
          // this.$Message.warning('开始节点不能连入！')
          this.$Message.warning(`${this.$t('saveFailed')}[${targertNodeName}]${this.$t('noEntry')}`)
          return
        }

        // 异常节点只能连入
        if (targertNodeType === 'abnormal') {
          const inEdges = target.getInEdges()
          if (inEdges.length > 0) {
            // this.$Message.warning('异常节点只能有一个入口！')
            this.$Message.warning(`${this.$t('saveFailed')}[${targertNodeName}]${this.$t('oneEntrance')}`)
            return
          }
        }

        // 异常节点只能连入
        // if (targertNodeType === 'decision') {
        //   if (!['human'].includes(sourceNodeType)) {
        //     this.$Message.warning(`${this.$t('decisionNodetip1')}`)
        //     return
        //   }
        // }

        // 只能有一个出口
        if (['data', 'human', 'automatic', 'subProc', 'date', 'timeInterval'].includes(sourceNodeType)) {
          const outEdges = source.getOutEdges()
          if (outEdges.length === 1) {
            // this.$Message.warning('该节点只能有一个出口！')
            this.$Message.warning(`${this.$t('saveFailed')}[${sourceNodeName}]${this.$t('oneExit')}`)
            return
          }
        }

        // 只能有一个入口
        if (['data', 'human', 'automatic', 'subProc', 'date', 'timeInterval'].includes(targertNodeType)) {
          const inEdges = target.getInEdges()
          if (inEdges.length === 1) {
            this.$Message.warning(`${this.$t('saveFailed')}[${targertNodeName}]${this.$t('oneEntrance')}`)
            return
          }
        }

        setTimeout(() => {
          const model = {
            id: `pdef_link_${this.uuid(16, 16)}`, // edge id
            label: '',
            source: sourceId,
            target: targetId,
            sourceAnchor,
            targetAnchor
          }
          this.graph.addItem('edge', model)

          this.itemInfoType = 'edge'
          const find = this.graph.save().nodes.find(node => node.id === sourceId)
          let isNameRequired = false
          if (find && ['decision', 'decisionMerge'].includes(find.customAttrs.nodeType)) {
            isNameRequired = true
          }
          this.$refs.itemInfoEdgeRef.showItemInfo(model, true, this.editFlow, isNameRequired)
        }, 100)
      })

      // 注册边点击事件
      this.graph.on('edge:click', e => {
        this.deleteRemoveNode()
        if (this.editFlow !== 'false' && this.isExecutionAllowed()) {
          return
        }
        const edge = e.item
        if (e && edge) {
          const model = edge.get('model')
          // 处理节点点击事件的逻辑
          const point = {
            x: e.x,
            y: e.y - 20
          }
          if (this.editFlow !== 'false') {
            this.addRemoveNode(point)
          }

          // const selected = edge.hasState('selected')
          // if (selected) {
          //   this.graph.setItemState(edge, 'selected', false)
          // } else {
          //   this.graph.setItemState(edge, 'selected', true)
          // }
          this.itemInfoType = 'edge'
          const find = this.graph.save().nodes.find(node => node.id === model.source)
          let isNameRequired = false
          if (find && ['decision', 'decisionMerge'].includes(find.customAttrs.nodeType)) {
            isNameRequired = true
          }
          this.$refs.itemInfoEdgeRef.showItemInfo(model, false, this.editFlow, isNameRequired)
        }
      })

      // 注册画布点击事件
      this.graph.on('canvas:click', () => {
        this.deleteRemoveNode()
        this.$nextTick(() => {
          if (this.editFlow !== 'false' && this.isExecutionAllowed()) {
            return
          }
          this.itemInfoType = 'canvas'
          this.$refs.itemInfoCanvasRef.showItemInfo(this.procDef, this.editFlow)
        })
      })
    },
    deleteNode(item) {
      this.graph.removeItem(item)
    },
    nameCheck(label) {
      const nodes = this.graph.save().nodes
      const findIndex = nodes.findIndex(node => node.label === label)
      if (findIndex > -1) {
        return this.nameCheck(label + '1')
      }

      return label
    },
    // 添加节点
    addNode(transferData, { x, y }) {
      if (this.editFlow !== 'false' && this.isExecutionAllowed()) {
        return
      }
      // eslint-disable-next-line
      let { label } = JSON.parse(transferData)
      const {
        shape, lineWidth, nodeType, stroke
      } = JSON.parse(transferData)
      const findStartNodeIndex = this.graph.save().nodes.findIndex(n => n.id.startsWith('id_start'))
      if (nodeType === 'start' && findStartNodeIndex > -1) {
        this.$Message.warning(this.$t('start_node_warning'))
        return
      }

      if (nodeType === 'start' && this.alreadyHasStart(nodeType)) {
        return
      }
      if (nodeType === 'end' && this.alreadyHasEnd(nodeType)) {
        return
      }

      label = this.nameCheck(label)
      const id = `pdef_node_${this.uuid(16, 16)}`
      const model = {
        id,
        label,
        // 形状
        type: shape,
        customAttrs: {
          id,
          name: label,
          procDefId: this.procDef.id, // 对应编排信息
          procDefKey: this.procDef.key, // 对应编排信息
          timeout: 30, // 超时时间
          description: null, // 描述说明
          dynamicBind: 0, // 动态绑定 0(启动时绑定)|1->(绑定节点)|2->(运行时)
          bindNodeId: null, // 动态绑定关联节点id
          nodeType, // 节点类型，对应节点原始类型（start、end……
          allowContinue: true, // 时间节点，是否允许继续
          routineExpression: this.procDef.rootEntity, // 对应节点中的定位规则
          routineRaw: null, // 还未知作用
          serviceName: null, // 选择的插件名称
          riskCheck: false, // 高危检测
          paramInfos: [], // 存在插件注册处需要填写的字段
          timeConfig: {
            duration: 0, // 时间间隔
            unit: 'sec', // 时间间隔单位
            date: '' // 固定时间
          }
        },
        style: {
          // fill: fill || '',
          fill: '#1890FF',
          stroke,
          lineWidth: lineWidth || 1,
          top: '100px'
        },
        logoIcon: nodeDefaultAttr[nodeType].logoIcon,
        // 坐标
        x,
        y,
        // 自定义锚点数量和位置
        anchorPoints: nodeDefaultAttr[nodeType].anchorPoints
      }
      this.graph.addItem('node', model)
      this.itemInfoType = 'node'
      this.$nextTick(() => {
        this.$refs.itemInfoNodeRef.showItemInfo(
          model,
          true,
          this.procDef.rootEntity.split('{')[0], // 禁止向节点传递过滤条件
          this.editFlow,
          this.permissionToRole
        )
        this.canRemovedId = id

        const point = {
          x: model.x + 40,
          y: model.y - 20
        }
        if (this.editFlow !== 'false') {
          this.addRemoveNode(point)
        }
      })
    },
    // 移除删除入口
    deleteRemoveNode() {
      const item = this.graph.findById('remove_node')
      this.graph.removeItem(item)
    },
    // 选中节点或线时，为其添加删除入口
    addRemoveNode({ x, y }) {
      const id = 'remove_node'
      const model = {
        id,
        label: '',
        // 形状
        type: 'rect-node',
        style: {
          fill: 'white',
          stroke: 'red',
          lineWidth: 1,
          width: 20,
          height: 24
        },
        logoIcon: {
          show: true,
          x: -6,
          y: -6,
          width: 12,
          height: 12,
          offset: 0
        },
        customAttrs: {
          nodeType: 'delete'
        },
        anchorPoints: [],
        // 坐标
        x,
        y
      }
      this.graph.addItem('node', model)
    },
    uuid(len, tempRadix) {
      const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('')
      const uuid = []
      const radix = tempRadix || chars.length

      if (len) {
        for (let i = 0; i < len; i++) {
          uuid[i] = chars[0 | (Math.random() * radix)]
        }
      } else {
        let r
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
        uuid[14] = '4'

        for (let i = 0; i < 36; i++) {
          if (!uuid[i]) {
            r = 0 | (Math.random() * 16)
            uuid[i] = chars[i === 19 ? (r & 0x3) | 0x8 : r]
          }
        }
      }
      return uuid.join('')
    },
    saveSvg() {
      const nodes = this.graph.save().nodes
      const edges = this.graph.save().edges

      console.error('Nodes:', nodes)
      console.error('Edges:', edges)
    },
    isExecutionAllowed() {
      const nodeToRef = {
        canvas: 'itemInfoCanvasRef',
        edge: 'itemInfoEdgeRef',
        node: 'itemInfoNodeRef'
      }
      // eslint-disable-next-line no-unused-expressions
      this.$refs[nodeToRef[this.itemInfoType]] && this.$refs[nodeToRef[this.itemInfoType]].hideItem()
      return this.$refs[nodeToRef[this.itemInfoType]] && this.$refs[nodeToRef[this.itemInfoType]].panalStatus()
    },
    // #region 数据保存集合
    // 保存编排整体信息
    async setCanvasInfo(info) {
      const { status } = await flowMgmt(info)
      if (status === 'OK') {
        this.itemInfoType = ''
        this.$Message.success(this.$t('save_successfully'))
        this.getFlowInfo(this.demoFlowId)
      }
    },
    async updateAuth(mgmt, use) {
      const finalData = JSON.parse(JSON.stringify(this.procDef))
      finalData.permissionToRole.MGMT = mgmt
      finalData.permissionToRole.USE = use

      const { status } = await flowMgmt(finalData)
      if (status === 'OK') {
        this.itemInfoType = ''
        this.$Message.success(this.$t('save_successfully'))
        this.getFlowInfo(this.demoFlowId)
      }
    },
    // 保存节点信息
    async setNodeInfo(info, needAddFirst) {
      info.selfAttrs.style.fill = 'white'
      const { status } = await flowNodeMgmt(info)
      if (status === 'OK') {
        if (!needAddFirst) {
          this.$Message.success(this.$t('save_successfully'))
          this.itemInfoType = ''
        }
        const item = this.graph.findById(info.customAttrs.id)
        const params = {
          ...info.selfAttrs,
          customAttrs: info.customAttrs
        }
        this.graph.updateItem(item, params)
        if (!needAddFirst) {
          this.deleteRemoveNode()
          // if (this.canRemovedId) {
          // const item = this.graph.findById(this.canRemovedId)
          // this.graph.clearItemStates(item)
          // }
        }
      }
    },
    // 保存边信息
    async setEdgeInfo(info, needAddFirst) {
      info.procDefId = this.procDef.id
      const { status } = await flowEdgeMgmt(info)
      if (status === 'OK') {
        if (!needAddFirst) {
          this.$Message.success(this.$t('save_successfully'))
          this.itemInfoType = ''
        }
        const item = this.graph.findById(info.customAttrs.id)
        const params = {
          ...info.selfAttrs,
          customAttrs: info.customAttrs
        }
        this.graph.updateItem(item, params)
        this.deleteRemoveNode()
        if (this.canRemovedId) {
          // const item = this.graph.findById(this.canRemovedId)
          // this.graph.clearItemStates(item)
        }
      }
    },
    hideItemInfo() {
      this.deleteRemoveNode()
      this.itemInfoType = ''
      if (this.canRemovedId) {
        this.graph.updateItem(this.canRemovedId, {
          style: {
            fill: '#ffffff00' // 更新后的节点背景色, 使用透明色#ffffff00
          }
        })
      }
      // this.graph.refresh()
    }
    // #endregion
  }
}
</script>

<style lang="scss">
/* 提示框的样式 */
.g6-tooltip {
  position: fixed;
  top: 0;
  left: 0;
  font-size: 12px;
  color: #545454;
  border-radius: 4px;
  border: 1px solid #e2e2e2;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: rgb(174, 174, 174) 0 0 10px;
  padding: 10px 8px;
}
.g6-minimap {
  position: absolute;
  right: 0;
  bottom: 0;
  background: #fff;
}

.floating-button {
  z-index: 10;
  position: fixed;
  left: 120px;
}

.v-enter-active,
.v-leave-active {
  transition: opacity 3.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}

.canvas-zone {
  border: 2px solid #f9f9f9;
  margin-top: 8px;
}
</style>
