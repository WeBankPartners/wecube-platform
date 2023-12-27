<template>
  <div class="root">
    <FlowHeader @canLoadGraph="startLoadGraph"></FlowHeader>
    <!-- v-show="isShowGraph" -->
    <div style="border: 2px solid #f9f9f9">
      <!-- 左侧按钮 -->
      <item-panel />
      <div class="floating-button">
        <Button size="small" type="primary" @click="resetCanvas" sytle="position: fixed">Reset Zoom</Button>
      </div>
      <!-- 挂载节点 -->
      <div id="canvasPanel" ref="canvasPanel" @dragover.prevent />
      <!-- 信息配置 -->
      <TtemInfoCanvas
        v-show="itemInfoType === 'canvas'"
        ref="itemInfoCanvasRef"
        @sendItemInfo="setItemInfo"
      ></TtemInfoCanvas>
      <TtemInfoNode v-show="itemInfoType === 'node'" ref="itemInfoNodeRef" @sendItemInfo="setItemInfo"></TtemInfoNode>
      <TtemInfoEdge v-show="itemInfoType === 'edge'" ref="itemInfoEdgeRef" @sendItemInfo="setItemInfo"></TtemInfoEdge>
    </div>
  </div>
</template>

<script>
import G6 from '@antv/g6'
import FlowHeader from '@/pages/collaboration/flow/flow-header.vue'
import registerFactory from './flow/graph/graph'
import ItemPanel from '@/pages/collaboration/flow/item-panel.vue'
import TtemInfoCanvas from '@/pages/collaboration/flow/item-info-canvas.vue'
import TtemInfoEdge from '@/pages/collaboration/flow/item-info-edge.vue'
import TtemInfoNode from '@/pages/collaboration/flow/item-info-node.vue'
import data from './flow/data.js'
import { nodeDefaultAttr } from './flow/node-default-attr.js'

// const registerFactory = require('../../../library/welabx-g6').default

export default {
  components: {
    FlowHeader,
    ItemPanel,
    TtemInfoCanvas,
    TtemInfoNode,
    TtemInfoEdge
  },
  data () {
    return {
      isShowGraph: false,
      itemInfoType: '', // canvas、node、edge
      flowInfo: {
        id: '123',
        label: 'ABC'
      },
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
      left: 0
    }
  },
  mounted () {
    // 创建画布
    this.$nextTick(() => {
      this.createGraphic()
      this.initGraphEvent()
      this.itemInfoType = 'canvas'
      this.$refs.itemInfoCanvasRef.showItemInfo(this.flowInfo)
    })
  },
  beforeDestroy () {
    this.graph.destroy()
  },
  methods: {
    // 开始加载流程图
    startLoadGraph () {
      this.isShowGraph = true
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
          outDiv.innerHTML = '<p id="deleteNode">删除节点</p>'
          return outDiv
        },
        handleMenuClick (target, item) {
          const { id } = target

          if (id) {
            vm[id](item)
          }
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
      this.graph.read(data) // 读取数据
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
      })

      this.graph.on('after-node-selected', e => {
        if (e && e.item) {
          const model = e.item.get('model')
          // this.config = model
          // this.label = model.label
          // this.labelCfg = {
          //   fontSize: model.labelCfg.fontSize,
          //   style: {
          //     fill: model.labelCfg.style.fill
          //   }
          // }
          // this.node = {
          //   fill: model.style.fill,
          //   borderColor: model.style.stroke,
          //   lineDash: model.style.lineDash || 'none',
          //   width: model.style.width,
          //   height: model.style.height,
          //   shape: model.type
          // }
          this.itemInfoType = 'node'
          this.$refs.itemInfoNodeRef.showItemInfo(model)
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
          this.graph.addItem('edge', {
            id: `id_edge_${Math.random().toString(36).substring(2, 8)}`, // edge id
            source: sourceId,
            target: targetId,
            sourceAnchor,
            targetAnchor
            // label:  'edge label',
          })
        }, 100)
      })

      // 注册边点击事件
      this.graph.on('edge:click', e => {
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
        const graph = e.item
        console.log(22, graph)
        // if (e && graph) {
        this.itemInfoType = 'canvas'
        this.$refs.itemInfoCanvasRef.showItemInfo(this.flowInfo)
        // }
      })

      this.graph.on('keydown', e => {
        // Check if the delete key is pressed
        if (e.code === 'Delete' || e.key === 'Delete' || e.key === 'Backspace') {
          const selectedNodes = this.graph.findAllByState('node', 'selected')
          const selectedEdges = this.graph.findAllByState('edge', 'selected')
          // Remove selected nodes
          selectedNodes.forEach(node => {
            this.graph.removeItem(node)
          })

          // Remove selected edges
          selectedEdges.forEach(edge => {
            this.graph.removeItem(edge)
          })

          // Clear selected states
          // this.graph.clearItemStates()
          this.itemInfoType = 'canvas'
          this.$refs.itemInfoCanvasRef.showItemInfo(this.flowInfo)
        }
      })
    },
    deleteNode (item) {
      this.graph.removeItem(item)
    },
    // 添加节点
    addNode (transferData, { x, y }) {
      const { label, shape, fill, lineWidth, nodeType, taskCategory } = JSON.parse(transferData)
      const findStartNodeIndex = this.graph.save().nodes.findIndex(n => n.id.startsWith('id_start'))
      if (nodeType === 'start' && findStartNodeIndex > -1) {
        this.$Message.warning(this.$t('start_node_warning'))
        return
      }
      const model = {
        id: `id_${nodeType}_${Math.random().toString(36).substring(2, 8)}`,
        label,
        // 形状
        type: shape,
        nodeType, // 标记节点类型，后端需要的字段
        taskCategory,
        style: {
          fill: fill || '',
          stroke: '#bbbbbb',
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
      console.log(44, model)
      // this.$nextTick(() => {
      this.itemInfoType = 'node'
      this.$refs.itemInfoNodeRef.showItemInfo(model)
      // })
    },
    setItemInfo (info) {
      // eslint-disable-next-line no-alert
      const item = this.graph.findById(info.id)
      console.log(11, item, info)
      this.graph.updateItem(item, info)
    },
    saveSvg () {
      const nodes = this.graph.save().nodes
      const edges = this.graph.save().edges

      console.log('Nodes:', nodes)
      console.log('Edges:', edges)
    }
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
  bottom: 30px; /* 调整按钮与底部的距离 */
  left: 30px; /* 将按钮水平居中 */
}
</style>
