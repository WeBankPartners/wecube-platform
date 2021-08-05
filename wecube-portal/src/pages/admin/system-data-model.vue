<template>
  <div>
    <div class="model-container" id="data-model"></div>
    <Button class="model-reset-button" @click="ResetModel">ResetZoom</Button>
    <Drawer width="450" :title="nodeName" :closable="false" v-model="drawerVisible">
      <div class="attr-container">
        <div v-for="attr in currentAttrs" :key="attr.name">
          <Divider orientation="left">{{ attr.name }}</Divider>
          <Form :label-width="120">
            <!-- <FormItem prop="propertyName" label="name">
              <span>{{attr.name}}</span>
            </FormItem> -->
            <FormItem prop="propertyName" label="dataType">
              <span>{{ attr.dataType }}</span>
            </FormItem>
            <FormItem prop="propertyName" label="description">
              <span>{{ attr.description }}</span>
            </FormItem>
            <FormItem prop="propertyName" label="mandatory">
              <span>{{ attr.mandatory }}</span>
            </FormItem>
            <FormItem prop="propertyName" label="multiple">
              <span>{{ attr.multiple }}</span>
            </FormItem>
            <FormItem v-if="attr.dataType === 'ref'" prop="propertyName" label="refEntityName">
              <span>{{ `[${attr.refPackageName}]${attr.refEntityName}.${attr.refAttributeName}` }}</span>
            </FormItem>
          </Form>
        </div>
      </div>
    </Drawer>
  </div>
</template>
<script>
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
import { getAllDataModels } from '@/api/server'
import { addEvent } from '../util/event.js'
export default {
  data () {
    return {
      data: [],
      allEntityType: [],
      graph: {},
      drawerVisible: false,
      nodeName: '',
      isHandleNodeClick: false,
      currentAttrs: []
    }
  },
  mounted () {
    this.getAllDataModels()
  },
  methods: {
    ResetModel () {
      if (this.graph.graphviz) {
        this.graph.graphviz.resetZoom()
      }
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
        data.forEach(i => {
          i.entities.forEach(_ => {
            this.data.push({
              ..._,
              id: '[' + _.packageName + ']' + _.name,
              tos: _.referenceToEntityList.map(to => {
                return { ...to, id: '[' + to.packageName + ']' + to.name }
              }),
              bys: _.referenceByEntityList.map(by => {
                return { ...by, id: '[' + by.packageName + ']' + by.name }
              })
            })
          })
        })
        this.initGraph()
      }
    },
    genDOT () {
      var dots = [
        'digraph  {',
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="none",width="0.7", height="0.8", color="#273c75"];',
        'Edge [fontname=Arial, minlen="1", color="#000", fontsize=10];'
      ]
      let drawConnection = (from, to) => {
        return `"${from.id}"->"${to.id}"[edgetooltip="${to.id}" id="edge${from.id}" class="${to.id}" fontsize=8 taillabel="${from.refName}" labeldistance=6 minlen="2"];`
      }

      let addNodeAttr = node => {
        const color = '#273c75'
        return `"${node.id}" [fixedsize=false id="${node.id}" label="${node.name +
          '(v' +
          node.dataModelVersion +
          ')'}" shape="box" fontcolor="${color}"];`
      }
      const nodeMap = new Map()
      this.data.forEach(node => {
        dots.push(addNodeAttr(node))
        if (node.tos.length) {
          node.tos.forEach(to => {
            let found = this.data.find(_ => to.id === _.id)
            if (found) {
              const dot = drawConnection({ ...node, refName: to.relatedAttribute.name }, found)
              if (!nodeMap.has(dot)) {
                dots.push(dot)
                nodeMap.set(dot, true)
              }
            }
          })
        }

        if (node.bys.length) {
          node.bys.forEach(by => {
            let found = this.data.find(_ => by.id === _.id)
            if (found) {
              const dot = drawConnection({ ...found, refName: by.relatedAttribute.name }, node)
              if (!nodeMap.has(dot)) {
                dots.push(dot)
                nodeMap.set(dot, true)
              }
            }
          })
        }
      })

      dots.push('}')
      return dots.join('')
    },
    renderGraph () {
      let nodesString = this.genDOT()
      this.graph.graphviz.renderDot(nodesString)
      this.shadeAll()
      addEvent('svg', 'mouseover', e => {
        this.shadeAll()
        e.preventDefault()
        e.stopPropagation()
      })
      addEvent('.node', 'mouseover', this.handleNodeMouseover)
      addEvent('.node', 'click', this.handleNodeClick)
    },
    handleNodeClick (e) {
      this.currentAttrs = this.data.find(_ => _.id === this.nodeName).attributes
      this.drawerVisible = true
      if (this.isHandleNodeClick) return
      this.isHandleNodeClick = true
      setTimeout(() => {
        this.isHandleNodeClick = false
      }, 500)
    },
    handleNodeMouseover (e) {
      e.preventDefault()
      e.stopPropagation()
      d3.selectAll('g').attr('cursor', 'pointer')
      this.g = e.currentTarget
      this.nodeName = this.g.firstElementChild.textContent.trim()
      this.shadeAll()
      this.colorNode(this.nodeName)
    },
    shadeAll () {
      d3.selectAll('g path')
        .attr('stroke', '#7f8fa6')
        .attr('stroke-opacity', '.2')
      d3.selectAll('g polygon')
        .attr('stroke', '#7f8fa6')
        .attr('stroke-opacity', '.2')
        .attr('fill', '#7f8fa6')
        .attr('fill-opacity', '.2')
      d3.selectAll('.edge text').attr('fill', '#7f8fa6')
    },
    colorNode (nodeName) {
      let fromNodesIds = []
      const fromNodes = document.querySelectorAll('g[id="' + 'edge' + nodeName + '"]')
      for (let i = 0; i < fromNodes.length; i++) {
        fromNodesIds.push(fromNodes[i].attributes.class.nodeValue)
      }
      fromNodesIds.forEach(_ => {
        d3.selectAll('g[id="' + _.split(' ')[1] + '"] polygon')
          .attr('stroke', 'red')
          .attr('stroke-opacity', '1')
      })
      d3.selectAll('g[id="' + 'edge' + nodeName + '"] path')
        .attr('stroke', 'red')
        .attr('stroke-opacity', '1')
      d3.selectAll('g[id="' + 'edge' + nodeName + '"] text').attr('fill', 'red')
      d3.selectAll('g[id="' + 'edge' + nodeName + '"] polygon')
        .attr('stroke', 'red')
        .attr('fill', 'red')
        .attr('fill-opacity', '1')
        .attr('stroke-opacity', '1')
      let toNodesIds = []
      const toNodes = document.querySelectorAll('g[class="' + 'edge ' + nodeName + '"]')
      for (let i = 0; i < toNodes.length; i++) {
        toNodesIds.push(toNodes[i].attributes.id.nodeValue)
      }
      toNodesIds.forEach(i => {
        d3.selectAll('g[id="' + i.slice(4) + '"] polygon')
          .attr('stroke', 'green')
          .attr('stroke-opacity', '1')
      })
      d3.selectAll('g[class="' + 'edge ' + nodeName + '"] path')
        .attr('stroke', 'green')
        .attr('stroke-opacity', '1')
      d3.selectAll('g[class="' + 'edge ' + nodeName + '"] text').attr('fill', 'green')
      d3.selectAll('g[class="' + 'edge ' + nodeName + '"] polygon')
        .attr('stroke', 'green')
        .attr('fill', 'green')
        .attr('fill-opacity', '1')
        .attr('stroke-opacity', '1')
      d3.selectAll('g[id="' + nodeName + '"] polygon')
        .attr('stroke', 'yellow')
        .attr('stroke-opacity', '1')
    },
    initGraph () {
      const graphEl = document.getElementById('data-model')
      const height = graphEl.offsetHeight
      const width = graphEl.offsetWidth - 20
      const initEvent = () => {
        let graph
        graph = d3.select(`#data-model`)
        graph.on('dblclick.zoom', null)
        this.graph.graphviz = graph
          .graphviz()
          .zoom(true)
          .fit(true)
          .height(height)
          .width(width)
      }

      initEvent()
      this.renderGraph()
    }
  }
}
</script>
<style lang="scss">
#data-model {
  height: calc(100vh - 120px);
}
.attr-container {
  height: calc(100vh - 90px);
  width: 100%;
  overflow: auto;
  .ivu-form-item {
    margin-bottom: 0px;
  }
}
.model-reset-button {
  position: absolute;
  right: 30px;
  bottom: 30px;
}
</style>
