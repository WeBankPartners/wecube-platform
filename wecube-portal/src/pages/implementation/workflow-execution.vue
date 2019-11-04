<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form>
            <FormItem :label-width="150" :label="$t('orchs')">
              <Select
                label
                v-model="selectedFlow"
                style="width:600px"
                clearable
              >
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
              <Button type="info" @click="queryHandler">{{
                $t("query_orch")
              }}</Button>
              <Button type="success" @click="createHandler">{{
                $t("create_orch")
              }}</Button>
            </FormItem>
          </Form>
        </Col>
      </Row>
      <Row
        v-if="isShowBody"
        style="border:1px solid #ebe7e7;border-radius:3px; padding:20px"
      >
        <Row>
          <Form>
            <Col span="6">
              <FormItem :label-width="100" :label="$t('select_orch')">
                <Select
                  label
                  v-model="selectedOrchestration"
                  :disabled="isEnqueryPage"
                >
                  <Option
                    v-for="item in allOrchestration"
                    :value="item.orchestrationId"
                    :key="item.orchestrationId"
                  >
                    {{ item.orchestrationName }}
                  </Option>
                </Select>
              </FormItem>
            </Col>
            <Col span="8">
              <FormItem :label-width="100" :label="$t('target_object')">
                <Select
                  label
                  v-model="selectedTarget"
                  :disabled="isEnqueryPage"
                >
                  <Option
                    v-for="item in allTarget"
                    :value="item.targetId"
                    :key="item.targetId"
                  >
                    {{ item.targetName }}
                  </Option>
                </Select>
              </FormItem>
            </Col>
            <Col span="2" offset="1">
              <Button
                v-if="!isEnqueryPage"
                type="create"
                @click="createFlowHandler"
                >{{ $t("create_job") }}</Button
              >
            </Col>
          </Form>
        </Row>
        <Row style="border:1px solid #d3cece;border-radius:3px; padding:20px">
          <Col
            span="6"
            style="border-right:1px solid #d3cece; text-align: center"
          >
            <div class="graph-container" id="flow"></div>
          </Col>
          <Col
            span="18"
            style="text-align: center;margin-top: 60px;text-align: center"
          >
            <div class="graph-container" id="graph"></div>
            <div style="text-align: center;margin-top: 60px;">
              <Button v-if="showExcution" type="info" @click="excutionFlow">{{
                $t("execute")
              }}</Button>
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
      allOrchestration: [],
      allTarget: [],
      currentFlowNodeId: "",
      foundRefAry: [],
      selectedFlow: "",
      selectedOrchestration: "",
      selectedTarget: "",
      showExcution: true,
      isShowBody: false,
      isEnqueryPage: false
    };
  },
  mounted() {
    this.getSelectionData();
  },
  methods: {
    getSelectionData() {
      this.allOrchestration = this.allFlows.map(_ => _.orchestration);
      this.allTarget = this.allFlows.map(_ => _.target);
    },
    queryHandler() {
      if (!this.selectedFlow) return;
      this.isShowBody = true;
      this.isEnqueryPage = true;
      this.$nextTick(() => {
        // set selection box value
        const found = this.allFlows.find(_ => _.id === this.selectedFlow);
        this.selectedOrchestration = found.orchestration.orchestrationId;
        this.selectedTarget = found.target.targetId;

        this.getModelData();
        this.getFlowData();
      });
    },
    createHandler() {
      this.isShowBody = true;
      this.isEnqueryPage = false;
      this.selectedOrchestration = "";
      this.selectedTarget = "";
      this.selectedFlow = "";
    },
    createFlowHandler() {
      this.getModelData();
      this.getFlowData();
    },
    getModelData() {
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
          _.refFlowNodeIds.toString().replace(/,/g, "/") +
          (isRecord ? "|" : "") +
          _.id;
        return `${_.id} [label="${label}" class="model" id="${_.id}" color="${color}" shape="${shape}" ]`;
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
    renderFlowGraph(excution) {
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
            : "") + _.name}", fontsize="10", class="flow",style="${
            excution ? "filled" : "none"
          }" color="${
            excution ? statusColor[_.status] : "#7F8A96"
          }" shape="circle", id="${_.id}"]`;
        } else {
          return `${_.id} [label="${(_.id > 0 && _.id < this.flowData.length
            ? _.id + "、"
            : "") + _.name}" fontsize="10" class="flow" style="${
            excution ? "filled" : "none"
          }" color="${
            excution
              ? statusColor[_.status]
              : _.id === this.currentFlowNodeId * 1
              ? "#5DB400"
              : "black"
          }"  shape="record" id="${_.id}"] height=.2`;
        }
      });
      let genEdge = () => {
        let pathAry = [];
        this.flowData.forEach(_ => {
          if (_.toGraphNodeIds.length > 0) {
            let current = [];
            current = _.toGraphNodeIds.map(to => {
              return (
                _.id +
                " -> " +
                `${to} [color="${excution ? statusColor[_.status] : "black"}"]`
              );
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
      this.bindFlowEvent();
    },
    excutionFlow() {
      this.showExcution = false;
      this.isEnqueryPage = true;
      this.flowData.forEach((_, index) => {
        setTimeout(() => {
          if (index > 0) {
            this.flowData[index - 1].status = "Completed";
          }
          _.status = "InProgress";
          this.renderFlowGraph(true);
        }, 3000 * index);
      });

      if (!this.isEnqueryPage) {
        this.selectedFlow = 1;
      }
    },
    bindFlowEvent() {
      if (this.isEnqueryPage !== true) {
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
      }
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
