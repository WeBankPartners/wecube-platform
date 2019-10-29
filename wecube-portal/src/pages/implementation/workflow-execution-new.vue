<template>
  <div>
    <Row>Select</Row>
    <Row>
      <Col span="6">
        <div class="graph-container" id="flow"></div>
      </Col>
      <Col span="18">
        <div class="graph-container" id="graph"></div>
      </Col>
    </Row>
  </div>
</template>
<script>
import {} from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
import { addEvent, removeEvent } from "../util/event.js";
import { modelData, flowData } from "./mockData";
export default {
  data() {
    return {
      graph: {},
      flowGraph: {},
      modelData,
      flowData,
      currentFlowNodeId: "",
      foundRefAry: []
    };
  },
  mounted() {
    this.getData();
    this.getFlowData();
    this.bindEvents();
  },
  methods: {
    getData() {
      // let { status, data, message } = await xxxx();
      this.initModelGraph();
    },
    getFlowData() {
      // let { status, data, message } = await xxxx();
      this.initFlowGraph();
    },
    renderModelGraph() {
      let nodes = this.modelData.map((_, index) => {
        let color = _.isHighlight ? "green" : "black";
        const isRecord = _.refFlowNodeIds.length > 0;
        const shape = isRecord ? "Mrecord" : "ellipse";
        const label =
          _.refFlowNodeIds.toString().replace(/,/g, "|") +
          (isRecord ? "|" : "") +
          _.id;
        return `${_.id} [label="${label}" class="model" id="${
          _.id
        }" color="${color}" shape="${shape}" ]`;
      });
      let genEdge = () => {
        let pathAry = [];
        this.modelData.forEach(_ => {
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
        "digraph G {" +
        nodes.toString().replace(/,/g, "; ") +
        ";" +
        genEdge() +
        "}";
      this.graph.graphviz.renderDot(nodesString);
    },
    renderFlowGraph() {
      let nodes = this.flowData.map((_, index) => {
        if (index === 0 || index === this.flowData.length - 1) {
          return `${_.id} [label="${(_.id > 1 && _.id < 12
            ? _.id - 1 + "、"
            : "") +
            _.name}", fontsize="10", class="flow", shape="circle", id="${
            _.id
          }"]`;
        } else {
          return `${_.id} [label="${(_.id > 1 && _.id < 12
            ? _.id - 1 + "、"
            : "") + _.name}" fontsize="10" class="flow" color=${
            _.id === this.currentFlowNodeId * 1 ? "green" : "black"
          } shape="record" id="${_.id}"]`;
        }
      });
      let genEdge = () => {
        let pathAry = [];
        this.flowData.forEach(_ => {
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
        "digraph G {" +
        nodes.toString().replace(/,/g, ";") +
        ";" +
        genEdge() +
        "}";
      this.flowGraph.graphviz.renderDot(nodesString);
    },
    bindEvents() {
      addEvent(".flow", "mouseover", e => {
        e.preventDefault();
        e.stopPropagation();
        d3.selectAll("g").attr("cursor", "pointer");
      });
      addEvent(".flow", "click", e => {
        e.preventDefault();
        e.stopPropagation();
        let g = e.currentTarget;
        this.highlightModel(g.id);
        this.currentFlowNodeId = g.id;
        this.renderFlowGraph();
      });
    },
    highlightModel(id) {
      this.foundRefAry = this.flowData.find(
        item => item.id == id
      ).refGraphNodeIds;
      this.modelData.forEach(item => {
        item["isHighlight"] = this.foundRefAry.includes(item.id);
      });
      this.renderModelGraph();
      removeEvent(".model", "click", this.modelClickHandler);
      this.foundRefAry.forEach(_ => {
        addEvent(`#${_}`, "click", this.modelClickHandler);
      });
    },
    modelClickHandler(e) {
      e.preventDefault();
      e.stopPropagation();
      let g = e.currentTarget;
      let foundModelNode = this.modelData.find(_ => _.id == g.id);
      const flowNodeIndex = foundModelNode.refFlowNodeIds.indexOf(
        this.currentFlowNodeId
      );
      if (flowNodeIndex > -1) {
        foundModelNode.refFlowNodeIds.splice(flowNodeIndex, 1);
      } else {
        foundModelNode.refFlowNodeIds.push(this.currentFlowNodeId);
      }
      document.getElementById("graph").innerHTML = "";
      this.renderModelGraph();
      this.foundRefAry.forEach(_ => {
        addEvent(`#${_}`, "click", this.modelClickHandler);
      });
    },
    initModelGraph() {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#graph`);
        graph.on("dblclick.zoom", null);
        this.graph.graphviz = graph.graphviz().zoom(false);
      };
      initEvent();
      this.renderModelGraph();
    },
    initFlowGraph() {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#flow`);
        graph.on("dblclick.zoom", null);
        this.flowGraph.graphviz = graph.graphviz().zoom(false);
      };
      initEvent();
      this.renderFlowGraph();
    }
  }
};
</script>
