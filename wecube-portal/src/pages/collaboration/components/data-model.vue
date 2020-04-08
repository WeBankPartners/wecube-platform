<template>
  <div>
    <!-- <div>111</div> -->
    <div id="dataModelContainer">
      <!-- <div class="loading" v-if="isLoading"> -->
      <Spin v-if="isLoading" fix size="large">
        <Icon type="ios-loading" size="54" class="demo-spin-icon-load"></Icon>
        <!-- <div>Loading</div> -->
      </Spin>
      <!-- </div> -->
      <div v-show="dataModel.dynamic" style="padding-left:3px;margin-bottom: 10px">
        <Button size="small" shape="circle" type="primary" icon="md-sync" @click="getData(true)">{{
          $t('get_dynamic_model')
        }}</Button>
        <Button
          :disabled="isApplyBtnDisabled"
          size="small"
          shape="circle"
          type="primary"
          icon="md-hammer"
          @click="applyNewDataModel"
          >{{ $t('apply_data_model') }}</Button
        >
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
        this.data = data.pluginPackageEntities.map(_ => {
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
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.id}"];`
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
            let found = this.data.find(_ => by.id === _.id)
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
    renderGraph () {
      let nodesString = this.genDOT()
      this.graph.graphviz.renderDot(nodesString)
    },
    initGraph () {
      const graphEl = document.getElementById('data-model-graph')
      console.log(graphEl.offsetHeight)
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
</style>
