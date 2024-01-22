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
      <item-panel />
      <div class="floating-button">
        <Button size="small" type="primary" @click="resetCanvas" sytle="position: fixed">Reset Zoom</Button>
      </div>
      <!-- 挂载节点 -->
      <div id="canvasPanel" ref="canvasPanel" @dragover.prevent />
      <!-- 信息配置 -->
      <Transition appear>
        <ItemInfoCanvas
          v-show="itemInfoType === 'canvas'"
          ref="itemInfoCanvasRef"
          @sendItemInfo="setCanvasInfo"
          @hideItemInfo="() => (itemInfoType = '')"
        ></ItemInfoCanvas>
      </Transition>
      <Transition appear>
        <ItemInfoNode
          v-if="itemInfoType === 'node'"
          ref="itemInfoNodeRef"
          @sendItemInfo="setNodeInfo"
          @hideItemInfo="() => (itemInfoType = '')"
        >
        </ItemInfoNode>
      </Transition>
      <Transition appear>
        <ItemInfoEdge
          v-show="itemInfoType === 'edge'"
          ref="itemInfoEdgeRef"
          @sendItemInfo="setEdgeInfo"
          @hideItemInfo="() => (itemInfoType = '')"
        >
        </ItemInfoEdge>
      </Transition>
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
// import data from './flow/data.js'
import { nodeDefaultAttr } from './flow/node-default-attr.js'
import { getFlowById, flowMgmt, flowNodeMgmt, flowEdgeMgmt, flowNodeDelete, flowEdgeDelete } from '@/api/server.js'
// import { relativeTimeThreshold } from 'moment'
// const registerFactory = require('../../../library/welabx-g6').default

export default {
  components: {
    FlowHeader,
    ItemPanel,
    ItemInfoCanvas,
    ItemInfoNode,
    ItemInfoEdge
  },
  data () {
    return {
      demoFlowId: 'pdef_60f1e126f08c50d256735',
      // pdef_60f2fc1f5acfa58e852c8
      // pdef_60f1e126f08c50d256735
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
      nodeShapes: [
        {
          name: '矩形',
          shape: 'rect-node'
        },
        {
          name: '圆形',
          shape: 'circle-node'
        },
        {
          name: '椭圆',
          shape: 'ellipise-node'
        },
        {
          name: '菱形',
          shape: 'diamond-node'
        }
      ],
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
  async mounted () {
    this.demoFlowId = this.$route.query.flowId
    await this.getFlowInfo(this.demoFlowId)
    // 创建画布
    this.$nextTick(() => {
      this.createGraphic()
      this.initGraphEvent()
      this.openCanvasPanel()
    })
  },
  beforeDestroy () {
    this.graph.destroy()
  },
  methods: {
    // 更新编排信息
    updateFlowData () {
      this.getFlowInfo(this.demoFlowId)
    },
    // 画布属性展开
    openCanvasPanel () {
      this.itemInfoType = 'canvas'
      this.$refs.itemInfoCanvasRef.showItemInfo(this.procDef)
    },
    async getFlowInfo (id) {
      const { status, data } = await getFlowById(id)
      if (status === 'OK') {
        this.permissionToRole = data.permissionToRole
        this.procDef = data.procDef
        this.procDef.label = data.procDef.name || ''
        this.procDef.permissionToRole = data.permissionToRole

        this.mgmtNodesAndEdges(data.taskNodeInfos)
        this.$refs.headerInfoRef.showItemInfo(this.procDef)
      }
    },
    // 整理编排节点与边数据结构
    mgmtNodesAndEdges (info) {
      if (info.nodes && info.nodes.length > 0) {
        this.nodesAndDeges.nodes = info.nodes.map(n => {
          let customAttrs = n.customAttrs
          if (customAttrs.timeConfig) {
            customAttrs.timeConfig = JSON.parse(customAttrs.timeConfig)
          }
          let selfAttrs = JSON.parse(n.selfAttrs)
          // selfAttrs.logoIcon.img = ''
          return {
            ...selfAttrs,
            customAttrs: customAttrs
          }
        })
      }
      if (info.edges && info.edges.length > 0) {
        this.nodesAndDeges.edges = info.edges.map(n => {
          return {
            ...JSON.parse(n.selfAttrs),
            customAttrs: n.customAttrs
          }
        })
      }
    },
    resetCanvas () {
      this.graph.zoomTo(1) // 缩放到原始大小
    },
    createGraphic () {
      const vm = this
      const grid = new G6.Grid()
      const menu = new G6.Menu({
        offsetX: -20,
        offsetY: -50,
        itemTypes: ['node', 'edge'],
        getContent (e) {
          const outDiv = document.createElement('div')

          outDiv.style.width = '80px'
          outDiv.style.cursor = 'pointer'
          outDiv.innerHTML = '<p id="deleteNode">删除</p>'
          return outDiv
        },
        async handleMenuClick (target, item) {
          vm.$Modal.confirm({
            title: '删除',
            content: '确认删除该元素吗？',
            onOk: async () => {
              const method = item._cfg.id.startsWith('pdef_node_') ? flowNodeDelete : flowEdgeDelete
              const { status } = await method(vm.procDef.id, item._cfg.id)
              if (status === 'OK') {
                const { id } = target
                if (id) {
                  vm[id](item)
                }
                vm.$refs.itemInfoCanvasRef.showItemInfo(vm.procDef)
              }
            },
            onCancel: () => {}
          })
        }
      })
      // const minimap = new G6.Minimap({
      //   size: [200, 100],
      //   minimapViewCfg: {
      //     top: 10,
      //     right: 10
      //   }
      // })
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
            stroke: '#aab7c3',
            lineAppendWidth: 10, // 防止线太细没法点中
            endArrow: true
          }
        },
        // 覆盖全局样式
        nodeStateStyles: {
          'nodeState:default': {
            opacity: 1,
            stroke: '#aab7c3'
          },
          'nodeState:hover': {
            opacity: 0.8,
            stroke: '#1890FF'
          },
          'nodeState:selected': {
            opacity: 0.9,
            stroke: '#1890FF'
          }
        },
        // 默认边不同状态下的样式集合
        edgeStateStyles: {
          'edgeState:default': {
            stroke: '#aab7c3'
          },
          'edgeState:selected': {
            stroke: '#1890FF'
          },
          'edgeState:hover': {
            animate: true,
            animationType: 'dash',
            stroke: '#1890FF'
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
        plugins: [menu, grid]
        // ... 其他G6原生入参
      })

      this.graph = new G6.Graph(cfg)
      this.graph.read(this.nodesAndDeges) // 读取数据
      // this.graph.paint() // 渲染到页面
      // this.graph.get('canvas').set('localRefresh', false) // 关闭局部渲染
      // this.graph.fitView()
    },
    // 初始化图事件
    initGraphEvent () {
      this.graph.on('drop', e => {
        const { originalEvent } = e
        if (originalEvent.dataTransfer) {
          let transferData = originalEvent.dataTransfer.getData('dragComponent')
          console.log('准备新增节点：', transferData)
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
          let customAttrs = tmp.customAttrs
          customAttrs.id = tmp.id
          customAttrs.name = tmp.label
          delete tmp.customAttrs
          let selfAttrs = tmp
          let finalData = {
            selfAttrs: selfAttrs,
            customAttrs: customAttrs
          }
          this.setNodeInfo(finalData, true)
        }, 100)
      })

      this.graph.on('after-node-selected', e => {
        if (this.isExecutionAllowed()) return
        this.itemInfoType = ''
        this.$nextTick(() => {
          if (e && e.item) {
            const model = e.item.get('model')
            this.itemInfoType = 'node'
            this.$nextTick(() => {
              this.$refs.itemInfoNodeRef &&
                this.$refs.itemInfoNodeRef.showItemInfo(model, false, this.procDef.rootEntity)
            })
          }
        })
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
      this.graph.on('mousedown', e => {
        this.isMouseDown = true
      })
      this.graph.on('mouseup', e => {
        this.isMouseDown = false
      })
      this.graph.on('canvas:mouseleave', e => {
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

      this.graph.on('before-node-removed', ({ target, callback }) => {
        setTimeout(() => {
          // 确认提示
          // eslint-disable-next-line standard/no-callback-literal
          callback(true)
        }, 1000)
      })

      this.graph.on('after-node-dblclick', e => {
        if (e && e.item) {
          console.log(e.item)
        }
      })

      this.graph.on('after-edge-selected', e => {
        if (e && e.item) {
          this.config = e.item.get('model').id

          this.graph.updateItem(e.item, {
            // shape: 'line-edge',
            style: {
              radius: 10,
              lineWidth: 2
            }
          })
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

      this.graph.on('before-edge-add', ({ source, target, sourceAnchor, targetAnchor }) => {
        const sourceId = source.get('id')
        const targetId = target.get('id')
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
          this.$refs.itemInfoEdgeRef.showItemInfo(model, true)
        }, 100)
      })

      // 注册边点击事件
      this.graph.on('edge:click', e => {
        if (this.isExecutionAllowed()) return
        const edge = e.item
        if (e && edge) {
          const model = edge.get('model')
          // 处理节点点击事件的逻辑
          console.log('edge Clicked:', model)

          const selected = edge.hasState('selected')
          if (selected) {
            this.graph.setItemState(edge, 'selected', false)
          } else {
            this.graph.setItemState(edge, 'selected', true)
          }
          this.itemInfoType = 'edge'
          this.$refs.itemInfoEdgeRef.showItemInfo(model)
        }
      })

      // 注册画布点击事件
      this.graph.on('canvas:click', e => {
        this.$nextTick(() => {
          if (this.isExecutionAllowed()) return
          this.itemInfoType = 'canvas'
          this.$refs.itemInfoCanvasRef.showItemInfo(this.procDef)
        })
      })
    },
    deleteNode (item) {
      this.graph.removeItem(item)
    },
    // 添加节点
    addNode (transferData, { x, y }) {
      if (this.isExecutionAllowed()) return
      const { label, shape, fill, lineWidth, nodeType, stroke } = JSON.parse(transferData)
      const findStartNodeIndex = this.graph.save().nodes.findIndex(n => n.id.startsWith('id_start'))
      if (nodeType === 'start' && findStartNodeIndex > -1) {
        this.$Message.warning(this.$t('start_node_warning'))
        return
      }
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
          dynamicBind: false, // 动态绑定
          bindNodeId: null, // 动态绑定关联节点id
          nodeType, // 节点类型，对应节点原始类型（start、end……
          routineExpression: '', // 对应节点中的定位规则
          routineRaw: null, // 还未知作用
          serviceName: null, // 选择的插件名称
          riskCheck: true, // 高危检测
          paramInfos: [], // 存在插件注册处需要填写的字段
          timeConfig: {
            duration: 0, // 时间间隔
            unit: 'sec', // 时间间隔单位
            date: '' // 固定时间
          }
        },
        style: {
          fill: fill || '',
          stroke: stroke,
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
        this.$refs.itemInfoNodeRef.showItemInfo(model, true, this.procDef.rootEntity)
      })
    },
    uuid (len, radix) {
      let chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('')
      // eslint-disable-next-line one-var
      let uuid = [],
        i
      radix = radix || chars.length

      if (len) {
        for (i = 0; i < len; i++) uuid[i] = chars[0 | (Math.random() * radix)]
      } else {
        var r
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
        uuid[14] = '4'

        for (i = 0; i < 36; i++) {
          if (!uuid[i]) {
            r = 0 | (Math.random() * 16)
            uuid[i] = chars[i === 19 ? (r & 0x3) | 0x8 : r]
          }
        }
      }
      return uuid.join('')
    },
    saveSvg () {
      const nodes = this.graph.save().nodes
      const edges = this.graph.save().edges

      console.log('Nodes:', nodes)
      console.log('Edges:', edges)
    },
    isExecutionAllowed () {
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
    async setCanvasInfo (info) {
      const { status } = await flowMgmt(info)
      if (status === 'OK') {
        this.itemInfoType = ''
        this.$Message.success(this.$t('save_successfully'))
        this.getFlowInfo(this.demoFlowId)
      }
    },
    async updateAuth (mgmt, use) {
      let finalData = JSON.parse(JSON.stringify(this.procDef))
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
    async setNodeInfo (info, needAddFirst) {
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
      }
    },
    // 保存边信息
    async setEdgeInfo (info, needAddFirst) {
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
      }
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
  bottom: 30px;
  left: 28px;
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
