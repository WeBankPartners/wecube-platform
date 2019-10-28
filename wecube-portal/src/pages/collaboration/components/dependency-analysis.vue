<template>
  <div class="graph-container" id="dependency-analysis-graph"></div>
</template>
<script>
import { getPluginPkgDependcy } from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
export default {
  name: "dependency-analysis",
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
    formatData(data) {
      let dependency = [data];
      dependency.forEach(_ => {
        let firstLevelTos = _.dependencies.map(i => {
          if (i.dependencies) {
            this.data.push({
              ...i,
              id: i.packageName,
              tos: i.dependencies.map(j => {
                return { ...j, id: j.packageName };
              }),
              bys: []
            });
            i.dependencies.forEach(d => {
              this.data.push({
                ...d,
                id: d.packageName,
                tos: [],
                bys: []
              });
            });
          }
          return { ...i, id: i.packageName };
        });

        this.data.push({
          ..._,
          id: _.packageName,
          tos: firstLevelTos,
          bys: []
        });
      });
    },
    async getData() {
      let { status, data, message } = await getPluginPkgDependcy(this.pkgId);
      if (status === "OK") {
        this.formatData(data);
        this.initGraph();
      }
    },

    genDOT() {
      var dots = [
        "digraph  {",
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="none",width="0.8", height="0.8", color="#273c75" ,fontsize=10];',
        'Edge [fontname=Arial, minlen="1", color="#000", fontsize=10];'
      ];
      let drawConnection = (from, to) => {
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.id}"];`;
      };
      let addNodeAttr = node => {
        const color = node.status === "active" ? "#19be6b" : "#c5c8ce";
        return `"${node.id}" [id="${node.id}" label="${node.id +
          "_v" +
          node.version}" shape="ellipse" color="${color}" style="filled"];`;
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
        graph = d3.select(`#dependency-analysis-graph`);
        graph.on("dblclick.zoom", null);
        this.graph.graphviz = graph.graphviz().zoom(false);
      };

      initEvent();
      this.renderGraph();
    }
  }
};
</script>
