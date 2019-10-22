<template>
  <div class="graph-container" id="auth-setting-graph"></div>
</template>
<script>
import { getAuthSettings } from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
export default {
  name: "auth-setting",
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
      // let { status, data, message } = await getPluginPkgDataModel(this.pkgId);
      let { status, data, message } = await getAuthSettings(3);
      console.log(data);

      if (status === "OK") {
        this.data = data;
        this.initGraph();
      }
    },

    renderGraph() {
      let nodesString = `digraph G {
rankdir = LR;
  subgraph cluster_0 {
    rankdir = TB;
    style=filled;
    color=lightgrey;
    node [style=filled,color=white];
    a1  a2  a3;
    label = "roles";
  }

  subgraph cluster_1 {

    node [style=filled];
    b1  b2  b3;
    label = "menus";
    color=blue
  }

  a1 -> b1;
  a1 -> b2;
  a2 -> b1;

}`;
      this.graph.graphviz.renderDot(nodesString);
    },
    initGraph() {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#auth-setting-graph`);
        graph.on("dblclick.zoom", null);
        this.graph.graphviz = graph.graphviz().zoom(false);
      };

      initEvent();
      this.renderGraph();
    }
  }
};
</script>
