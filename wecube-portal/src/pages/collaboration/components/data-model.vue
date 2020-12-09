<template>
  <div>
    <div id="dataModelContainer">
      <Button class="data-model-reset-button" size="small" @click="ResetModel">ResetZoom</Button>
      <Spin v-if="isLoading" fix size="large">
        <Icon type="ios-loading" size="54" class="demo-spin-icon-load"></Icon>
      </Spin>
      <div style="padding-left:3px;margin-bottom: 10px">
        <Button size="small" shape="circle" type="primary" icon="md-sync" @click="getData(true)">{{
          $t('get_dynamic_model')
        }}</Button>
      </div>
      <div v-if="!dataModel.dynamic && dataModel.pluginPackageEntities && dataModel.pluginPackageEntities.length === 0">
        {{ $t('no_data_model_provided') }}
      </div>
      <div class="graph-container" id="data-model-graph"></div>
    </div>
  </div>
</template>
<script>
import { getPluginPkgDataModel, pullDynamicDataModel, applyNewDataModel } from '@/api/server'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
import { addEvent } from '../../util/event.js'
export default {
  name: 'data-model',
  data () {
    return {
      isLoading: false,
      data: [],
      dataModel: {},
      graph: {},
      isApplyBtnDisabled: true
    }
  },
  watch: {
    pkgId: {
      handler: () => {
        this.dataModel = {}
        this.getData(false)
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  created () {
    this.getData(false)
  },
  methods: {
    ResetModel () {
      if (this.graph.graphviz) {
        this.graph.graphviz.resetZoom()
      }
    },
    async getData (ispull) {
      this.isLoading = true
      let { status, data } = this.dataModel.dynamic
        ? await pullDynamicDataModel(this.pkgId)
        : await getPluginPkgDataModel(this.pkgId)
      this.isLoading = false
      if (status === 'OK') {
        if (this.dataModel.dynamic) {
          this.isApplyBtnDisabled = false
        }
        this.dataModel = data
        this.data = data.entities
          .sort(function (a, b) {
            let nameA = a.name.toUpperCase()
            let nameB = b.name.toUpperCase()
            if (nameA < nameB) {
              return -1
            }
            if (nameA > nameB) {
              return 1
            }
            return 0
          })
          .map(_ => {
            return {
              ..._,
              id: '[' + _.packageName + ']' + _.name,
              tos: _.referenceToEntityList.map(to => {
                return { ...to, id: '[' + to.packageName + ']' + to.name }
              }),
              bys: _.referenceByEntityList.map(by => {
                return { ...by, id: '[' + by.packageName + ']' + by.name }
              })
            }
          })
        console.log(this.data)
        this.initGraph()
      }
    },
    async applyNewDataModel () {
      this.isLoading = true
      let { status } = await applyNewDataModel(this.dataModel)
      this.isLoading = false
      if (status === 'OK') {
        if (this.dataModel.dynamic) {
          this.isApplyBtnDisabled = true
        }
        this.$Notice.success({
          title: 'Success',
          desc: 'Data model apply successfully'
        })
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
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.id}" id="edge${from.id}" class="${to.id}" fontsize=8 taillabel="${from.refName}" labeldistance=6 minlen="2"];`
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
      const graphEl = document.getElementById('data-model-graph')
      const height = graphEl.offsetHeight
      const width = graphEl.offsetWidth - 20
      const initEvent = () => {
        let graph
        graph = d3.select(`#data-model-graph`)
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
<style lang="scss" scoped>
#dataModelContainer {
  width: 100%;
  height: calc(100vh - 180px);
  overflow: auto;
  position: relative;
}
#data-model-graph {
  height: calc(100vh - 220px);
}
.demo-spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
.loading {
  position: absolute;
  top: 0;
  left: 0;
}
.data-model-reset-button {
  position: absolute;
  right: 20px;
  bottom: 20px;
}
</style>
