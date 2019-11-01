<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form>
            <FormItem :label-width="100" label="任务编排">
              <Select label v-model="selectedFlow" style="width:600px">
                <Option
                  v-for="item in allFlows"
                  :value="item.id"
                  :key="item.id"
                >
                  {{
                    item.orchestration.orchestrationName +
                      " " +
                      item.target.targetName +
                      " " +
                      item.timestamp +
                      " " +
                      item.createBy
                  }}
                </Option>
              </Select>
              <Button type="info">查询</Button>
              <Button type="info">新建</Button>
            </FormItem>
          </Form>
        </Col>
      </Row>
      <Row style="border:1px solid #ebe7e7;border-radius:3px; padding:20px">
        <Row>
          <Form>
            <Col span="6">
              <FormItem :label-width="100" label="选择编排">
                <Select label v-model="selectedFlow">
                  <Option
                    v-for="item in allFlows"
                    :value="item.id"
                    :key="item.id"
                  >
                    {{
                      item.orchestration.orchestrationName +
                        " " +
                        item.target.targetName +
                        " " +
                        item.timestamp +
                        " " +
                        item.createBy
                    }}
                  </Option>
                </Select>
              </FormItem>
            </Col>
            <Col span="8">
              <FormItem :label-width="100" label="目标对象">
                <Select label v-model="selectedFlow">
                  <Option
                    v-for="item in allFlows"
                    :value="item.id"
                    :key="item.id"
                  >
                    {{
                      item.orchestration.orchestrationName +
                        " " +
                        item.target.targetName +
                        " " +
                        item.timestamp +
                        " " +
                        item.createBy
                    }}
                  </Option>
                </Select>
              </FormItem>
            </Col>
            <Col span="2" offset="1">
              <Button type="info">构建任务</Button>
            </Col>
          </Form>
        </Row>
        <Row style="border:1px solid #d3cece;border-radius:3px; padding:20px">
          <Col span="6" style="border-right:1px solid #d3cece;">
            <div class="graph-container" id="flow"></div>
          </Col>
          <Col span="18">
            <div class="graph-container" id="graph"></div>
            <div style="text-align: center;margin-top: 60px">
              <Button type="info">执行</Button>
            </div>
          </Col>
        </Row>
      </Row>
    </Card>
  </div>
</template>
<script>
import {} from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
import { addEvent, removeEvent } from "../util/event.js";
import { modelData, flowData, allFlows } from "./mockData";
export default {
  data() {
    return {
      graph: {},
      flowGraph: {},
      modelData,
      flowData,
      allFlows,
      currentFlowNodeId: "",
      foundRefAry: [],
      selectedFlow: ""
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
        let color = _.isHighlight ? "#5DB400" : "black";
        const isRecord = _.refFlowNodeIds.length > 0;
        const shape = isRecord ? "Mrecord" : "ellipse";
        const label =
          _.refFlowNodeIds.toString().replace(/,/g, "\\n") +
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
        "digraph G { " +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, shape="ellipse", fixedsize="true", width="1.6", height=".8",fontsize=12];' +
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];' +
        nodes.toString().replace(/,/g, "; ") +
        ";" +
        genEdge() +
        "}";
      this.graph.graphviz.renderDot(nodesString).fit(true);
    },
    renderFlowGraph() {
      const statusColor = {
        Completed: "#5DB400",
        NotStarted: "#7F8A96",
        InProgress: "#3C83F8",
        Faulted: "#FF6262",
        Timeouted: "#F7B500"
      };
      let nodes = this.flowData.map((_, index) => {
        if (index === 0 || index === this.flowData.length - 1) {
          return `${_.id} [label="${(_.id > 1 && _.id < this.flowData.length
            ? _.id + "、"
            : "") +
            _.name}", fontsize="10", class="flow", shape="circle", id="${
            _.id
          }"]`;
        } else {
          return `${_.id} [label="${(_.id > 0 && _.id < this.flowData.length
            ? _.id + "、"
            : "") + _.name}" fontsize="10" class="flow" style="filled" color="${
            statusColor[_.status]
          }"  shape="record" id="${_.id}"] height=.2`;
        }
      });
      let genEdge = () => {
        let pathAry = [];
        this.flowData.forEach(_ => {
          if (_.toGraphNodeIds.length > 0) {
            let current = [];
            current = _.toGraphNodeIds.map(to => {
              return _.id + " -> " + `${to} [color="${statusColor[_.status]}"]`;
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
        'bgcolor="transparent";' +
        'Node [fontname=Arial, height=".3", fontsize=12];' +
        'Edge [fontname=Arial, color="#7f8fa6", fontsize=10];' +
        nodes.toString().replace(/,/g, ";") +
        ";" +
        genEdge() +
        "}";
      this.flowGraph.graphviz.renderDot(nodesString).fit(true);
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
<style lang="scss" scoped>
body {
  color: #15a043;
}
</style>
