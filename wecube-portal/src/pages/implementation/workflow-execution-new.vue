<template>
  <div>
    <Row>
      Select
    </Row>
    <Row>
      <div class="graph-container" id="graph"></div>
    </Row>
  </div>
</template>
<script>
import {} from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
export default {
  data() {
    return {
      data: [],
      graph: {}
    };
  },
  mounted() {
    this.getData();
  },
  methods: {
    getData() {
      // let { status, data, message } = await xxxx();
      let data = [
        {
          id: "UNIT_APP",
          srcGraphNodeIds: [],
          toGraphNodeIds: ["INS1", "INS2", "PACK_V1"]
        },
        {
          id: "INS1",
          srcGraphNodeIds: ["UNIT_APP"],
          toGraphNodeIds: ["HOST1", "PACK_V1"]
        },
        {
          id: "HOST1",
          srcGraphNodeIds: ["INS1"],
          toGraphNodeIds: ["DISK1", "IP1"]
        },
        {
          id: "DISK1",
          srcGraphNodeIds: ["HOST1"],
          toGraphNodeIds: []
        },
        {
          id: "IP1",
          srcGraphNodeIds: ["HOST1"],
          toGraphNodeIds: []
        },
        {
          id: "PACK_V1",
          srcGraphNodeIds: ["INS1", "UNIT_APP", "INS2"],
          toGraphNodeIds: ["USER"]
        },
        {
          id: "USER",
          srcGraphNodeIds: ["PACK_V1"],
          toGraphNodeIds: []
        },
        {
          id: "INS2",
          srcGraphNodeIds: ["UNIT_APP"],
          toGraphNodeIds: ["HOST2", "PACK_V1"]
        },
        {
          id: "HOST2",
          srcGraphNodeIds: ["INS2"],
          toGraphNodeIds: ["IP2", "DISK2"]
        },
        {
          id: "IP2",
          srcGraphNodeIds: ["HOST2"],
          toGraphNodeIds: []
        },
        {
          id: "DISK2",
          srcGraphNodeIds: ["HOST2"],
          toGraphNodeIds: []
        }
      ];
      this.initGraph(data);
    },
    renderGraph(data) {
      let nodes = data.map(_ => _.id);
      let genEdge = data => {
        let pathAry = [];
        data.forEach(_ => {
          if (_.toGraphNodeIds.length > 0) {
            let current = [];
            current = _.toGraphNodeIds.map(to => {
              return _.id + " -> " + to;
            });
            pathAry.push(current);
          }
        });
        return pathAry
          .flat()
          .toString()
          .replace(/,/g, ";");
      };
      let nodesString =
        "digraph G {" + nodes.toString() + ";" + genEdge(data) + "}";
      this.graph.graphviz.renderDot(nodesString);
    },
    initGraph(data) {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#graph`);
        graph.on("dblclick.zoom", null);
        this.graph.graphviz = graph.graphviz().zoom(false);
      };
      initEvent();
      this.renderGraph(data);
    }
  }
};
</script>
