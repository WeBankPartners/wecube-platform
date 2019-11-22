<template>
  <div>
    <div v-if="dataModel.dynamic" style="padding-left:3px;margin-bottom: 10px">
      <Button
        size="small"
        shape="circle"
        type="primary"
        icon="md-sync"
        @click="getData(true)"
        >{{ $t("get_dynamic_model") }}</Button
      >
      <Button
        size="small"
        shape="circle"
        type="primary"
        icon="md-hammer"
        @click="applyNewDataModel"
        >{{ $t("apply_data_model") }}</Button
      >
    </div>
    <div class="graph-container" id="data-model-graph"></div>
  </div>
</template>
<script>
import {
  getPluginPkgDataModel,
  pullDynamicDataModel,
  applyNewDataModel
} from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
export default {
  name: "data-model",
  data() {
    return {
      data: [],
      dataModel: {},
      graph: {}
    };
  },
  watch: {
    pkgId: {
      handler: () => {
        this.dataModel = {};
        this.getData(false);
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  created() {
    this.getData(false);
  },
  methods: {
    async getData(ispull) {
      let { status, data, message } = this.dataModel.dynamic
        ? await pullDynamicDataModel(this.pkgId)
        : await getPluginPkgDataModel(this.pkgId);
      if (status === "OK") {
        this.dataModel = data;
        this.data = data.pluginPackageEntities.map(_ => {
          return {
            ..._,
            id: "[" + _.packageName + "]" + _.name,
            tos: _.referenceToEntityList.map(to => {
              return { ...to, id: "[" + to.packageName + "]" + to.name };
            }),
            bys: _.referenceByEntityList.map(by => {
              return { ...by, id: "[" + by.packageName + "]" + by.name };
            })
          };
        });
        console.log(11, this.data);
        this.initGraph();
      }
    },
    async applyNewDataModel() {
      let { status, data, message } = await applyNewDataModel(this.dataModel);
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: "Data model apply successfully"
        });
      }
    },

    genDOT() {
      var dots = [
        "digraph  {",
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="none",width="0.7", height="0.8", color="#273c75" ,fontsize=10];',
        'Edge [fontname=Arial, minlen="1", color="#000", fontsize=10];'
      ];
      let drawConnection = (from, to) => {
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.id}"];`;
      };
      let addNodeAttr = node => {
        const color = "#273c75";
        return `"${node.id}" [id="${node.id}" label="${node.id +
          "_v" +
          node.dataModelVersion}" shape="box" fontcolor="${color}"];`;
      };
      const nodeMap = new Map();
      this.data.forEach(node => {
        dots.push(addNodeAttr(node));
        if (node.tos.length) {
          node.tos.forEach(to => {
            let found = this.data.find(_ => to.id === _.id);
            if (found) {
              const dot = drawConnection(node, found);
              if (!nodeMap.has(dot)) {
                dots.push(dot);
                nodeMap.set(dot, true);
              }
            }
          });
        }

        if (node.bys.length) {
          node.bys.forEach(by => {
            let found = this.data.find(_ => by.id === _.id);
            if (found) {
              const dot = drawConnection(found, node);
              if (!nodeMap.has(dot)) {
                dots.push(dot);
                nodeMap.set(dot, true);
              }
            }
          });
        }
      });

      dots.push("}");
      return dots.join("");
    },
    renderGraph() {
      let nodesString = this.genDOT();
      console.log(111111, nodesString);
      this.graph.graphviz.renderDot(nodesString);
    },
    initGraph() {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#data-model-graph`);
        graph.on("dblclick.zoom", null);
        this.graph.graphviz = graph
          .graphviz()
          .zoom(true)
          .scale(0.8);
      };

      initEvent();
      this.renderGraph();
    }
  }
};
</script>
