<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form label-position="left">
            <FormItem :label-width="150" :label="$t('orchs')">
              <Select v-model="selectedFlow" style="width:70%">
                <Option
                  v-for="item in allFlows"
                  :value="item.procDefId"
                  :key="item.procDefId"
                >
                  {{
                    item.procDefName +
                      " version_" +
                      item.procDefVersion +
                      " " +
                      (item.timestamp || "timestap") +
                      " " +
                      (item.createBy || "createBy")
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
                  v-model="selectedFlow"
                  :disabled="isEnqueryPage"
                  @on-change="orchestrationSelectHandler"
                >
                  <Option
                    v-for="item in allFlows"
                    :value="item.procDefId"
                    :key="item.procDefId"
                    >{{ item.procDefName }}</Option
                  >
                </Select>
              </FormItem>
            </Col>
            <Col span="8">
              <FormItem :label-width="100" :label="$t('target_object')">
                <Select
                  label
                  v-model="selectedTarget"
                  :disabled="isEnqueryPage"
                  @on-change="onTargetSelectHandler"
                >
                  <Option
                    v-for="item in allTarget"
                    :value="item.id"
                    :key="item.id"
                    >{{ item.key_name }}</Option
                  >
                </Select>
              </FormItem>
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
import {
  getAllFlow,
  getFlowOutlineByID,
  getTargetOptions,
  getTreePreviewData
} from "@/api/server";
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
import { addEvent, removeEvent } from "../util/event.js";
export default {
  data() {
    return {
      graph: {},
      flowGraph: {},
      modelData: [],
      flowData: {},
      allFlows: [],
      allTarget: [],
      currentFlowNodeId: "",
      foundRefAry: [],
      selectedFlow: "",
      selectedTarget: "",
      showExcution: true,
      isShowBody: false,
      isEnqueryPage: false
    };
  },
  mounted() {
    this.getAllFlow();
  },
  methods: {
    async getAllFlow() {
      let { status, data, message } = await getAllFlow(false);
      if (status === "OK") {
        this.allFlows = data;
      }
    },

    orchestrationSelectHandler() {
      this.getFlowOutlineData();
    },
    async getTargetOptions() {
      if (!this.flowData.rootEntity) return;
      const pkgName = this.flowData.rootEntity.split(":")[0];
      const entityName = this.flowData.rootEntity.split(":")[1];
      let { status, data, message } = await getTargetOptions(
        pkgName,
        entityName
      );
      if (status === "OK") {
        this.allTarget = data;
      }
    },
    queryHandler() {
      if (!this.selectedFlow) return;
      this.isShowBody = true;
      this.isEnqueryPage = true;
      this.$nextTick(() => {
        // this.getModelData();
        this.getFlowOutlineData();
      });
    },
    createHandler() {
      this.isShowBody = true;
      this.isEnqueryPage = false;
      this.selectedTarget = "";
      this.selectedFlow = "";
      this.modelData = [];
      this.flowData = {};
    },
    onTargetSelectHandler() {
      console.log("selectedTarget is: ", this.selectedTarget);
      this.getModelData();
    },
    async getModelData() {
      let { status, data, message } = await getTreePreviewData(
        this.selectedFlow,
        this.selectedTarget
      );
      if (status === "OK") {
        this.modelData = data;
        this.initModelGraph();
      }
    },
    async getFlowOutlineData() {
      let { status, data, message } = await getFlowOutlineByID(
        this.selectedFlow
      );
      if (status === "OK") {
        this.flowData = data;
        this.initFlowGraph();
        this.getTargetOptions();
      }
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
          if (_.succeedingNodeIds.length > 0) {
            let current = [];
            current = _.succeedingNodeIds.map(to => {
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
      let nodesToString = Array.isArray(nodes)
        ? nodes.toString().replace(/,/g, ";") + ";"
        : "";

      let nodesString =
        "digraph G { " +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, shape="ellipse", fixedsize="true", width="1.6", height=".8",fontsize=12];' +
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        "}";
      this.graph.graphviz.renderDot(nodesString).fit(true);
    },
    renderFlowGraph(excution) {
      const statusColor = {
        Completed: "#5DB400",
        deployed: "#7F8A96",
        InProgress: "#3C83F8",
        Faulted: "#FF6262",
        Timeouted: "#F7B500"
      };
      let nodes =
        this.flowData &&
        this.flowData.flowNodes &&
        this.flowData.flowNodes.map((_, index) => {
          if (_.nodeType === "startEvent" || _.nodeType === "endEvent") {
            return `${_.nodeId} [label="${
              _.nodeName
            }", fontsize="10", class="flow",style="${
              excution ? "filled" : "none"
            }" color="${
              excution ? statusColor[_.status] : "#7F8A96"
            }" shape="circle", id="${_.nodeId}"]`;
          } else {
            return `${_.nodeId} [label="${_.orderedNo +
              "、" +
              _.nodeName}" fontsize="10" class="flow" style="${
              excution ? "filled" : "none"
            }" color="${
              excution
                ? statusColor[_.status]
                : _.nodeId === this.currentFlowNodeId
                ? "#5DB400"
                : "#7F8A96"
            }"  shape="record" id="${_.nodeId}"] height=.2`;
          }
        });
      let genEdge = () => {
        let pathAry = [];
        this.flowData &&
          this.flowData.flowNodes &&
          this.flowData.flowNodes.forEach(_ => {
            if (_.succeedingNodeIds.length > 0) {
              let current = [];
              current = _.succeedingNodeIds.map(to => {
                return (
                  _.nodeId +
                  " -> " +
                  `${to} [color="${
                    excution ? statusColor[_.status] : "black"
                  }"]`
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
      let nodesToString = Array.isArray(nodes)
        ? nodes.toString().replace(/,/g, ";") + ";"
        : "";
      let nodesString =
        "digraph G {" +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, height=".3", fontsize=12];' +
        'Edge [fontname=Arial, color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        "}";
      this.flowGraph.graphviz.renderDot(nodesString).fit(true);
      this.bindFlowEvent();
    },
    excutionFlow() {
      if (!this.isEnqueryPage) {
        this.selectedFlow = 1;
      }
      this.showExcution = false;
      this.isEnqueryPage = true;
      this.flowData.flowNodes.forEach((_, index) => {
        setTimeout(() => {
          if (index > 0) {
            this.flowData.flowNodes[index - 1].status = "Completed";
          }
          _.status = "InProgress";
          this.renderFlowGraph(true);
        }, 3000 * index);
      });
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
    highlightModel(nodeId) {
      this.foundRefAry = this.flowData.flowNodes.find(
        item => item.nodeId == nodeId
      ).previousNodeIds;
      this.modelData.forEach(item => {
        item["isHighlight"] = this.foundRefAry.includes(item.nodeId);
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
