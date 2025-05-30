<template>
  <div class="graph-container" id="dependency-analysis-graph"></div>
</template>
<script>
import { getPluginPkgDependcy } from '@/api/server'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
export default {
  name: 'dependency-analysis',
  data() {
    return {
      data: [],
      graph: {}
    }
  },
  watch: {
    pkgId: {
      handler: val => {
        val && this.getData(val)
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  mounted() {
    this.getData(this.pkgId)
  },
  methods: {
    formatData(data) {
      const dependency = [data]
      dependency.forEach(_ => {
        const firstLevelTos = _.dependencies.map(i => {
          if (i.dependencies) {
            this.data.push({
              ...i,
              id: i.packageName,
              tos: i.dependencies.map(j => ({
                ...j,
                id: j.packageName
              })),
              bys: []
            })
            i.dependencies.forEach(d => {
              this.data.push({
                ...d,
                id: d.packageName,
                tos: [],
                bys: []
              })
            })
          }
          return {
            ...i,
            id: i.packageName
          }
        })

        this.data.push({
          ..._,
          id: _.packageName,
          tos: firstLevelTos,
          bys: []
        })
      })
    },
    async getData(id) {
      const { status, data } = await getPluginPkgDependcy(id)
      if (status === 'OK') {
        this.formatData(data)
        this.initGraph()
      }
    },

    genDOT() {
      const dots = [
        'digraph  {',
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="none",width="0.8", height="0.8", color="#273c75" ,fontsize=10];',
        'Edge [fontname=Arial, minlen="1", color="#000", fontsize=10];'
      ]
      const drawConnection = (from, to) => `"${from.id}" -> "${to.id}"[edgetooltip="${to.id}"];`
      const addNodeAttr = node => {
        const label = node.id + '_' + node.version
        const len = label.length
        const fontSize = Math.min((58 / len) * 3, 16)
        const color = node.status === 'active' ? '#00cb91' : '#c5c8ce'
        return `"${node.id}" [id="${node.id}" label="${label}" fontsize=${fontSize} shape="ellipse" color="${color}" style="filled"];`
      }
      const nodeMap = new Map()
      this.data.forEach(node => {
        dots.push(addNodeAttr(node))
        if (node.tos.length) {
          node.tos.forEach(to => {
            const found = this.data.find(_ => to.id === _.id)
            if (found) {
              const dot = drawConnection(node, found)
              if (!nodeMap.has(dot)) {
                dots.push(dot)
                nodeMap.set(dot, true)
              }
            }
          })
        }

        if (node.bys.length) {
          node.bys.forEach(by => {
            const found = this.data.find(_ => by.id === _.id)
            if (found) {
              const dot = drawConnection(found, node)
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
    renderGraph() {
      const nodesString = this.genDOT()
      this.graph.graphviz.renderDot(nodesString).on('end', this.setFontSizeForText)
    },
    setFontSizeForText() {
      const nondes = d3.selectAll('#dependency-analysis-graph svg g .node')._groups[0]
      for (let i = 0; i < nondes.length; i++) {
        const len = nondes[i].children[2].innerHTML.replace(/&nbsp;/g, '').length
        const fontsize = Math.min((nondes[i].children[1].rx.baseVal.value / len) * 3, 16)
        for (let j = 2; j < nondes[i].children.length; j++) {
          nondes[i].children[j].setAttribute('font-size', fontsize)
        }
      }
    },
    initGraph() {
      const initEvent = () => {
        const graph = d3.select('#dependency-analysis-graph')
        graph.on('dblclick.zoom', null)
        this.graph.graphviz = graph.graphviz().zoom(false)
      }

      initEvent()
      this.renderGraph()
    }
  }
}
</script>
