<template>
  <div class="graph-container" id="data-model-graph"></div>
</template>
<script>
import { getPluginPkgDataModel } from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
export default {
  name: "data-model",
  data() {
    return {
      data: [],
      graph: {}
    };
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData();
      }
    }
  },
  props: {
    pkgId: {
      required: true,
      type: Number
    }
  },
  created() {
    this.getData();
  },
  methods: {
    async getData() {
      let { status, data, message } = await getPluginPkgDataModel(this.pkgId);
      if (status === "OK") {
        this.data = data.map(_ => {
          return {
            ..._,
            id: "[" + _.packageName + "]" + _.name,
            tos: _.referenceToEntityList.map(_ => {
              return { ..._, id: _.packageName + "_" + _.name };
            }),
            bys: _.referenceByEntityList.map(_ => {
              return { ..._, id: _.packageName + "_" + _.name };
            })
          };
        });
        this.initGraph();
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
          "_" +
          node.packageVersion}" shape="box" fontcolor="${color}"];`;
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
