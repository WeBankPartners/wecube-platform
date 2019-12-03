<template>
  <div>
    <Card dis-hover>
      <Row>
        <Col span="20">
          <Form label-position="left">
            <FormItem :label-width="150" :label="$t('orchs')">
              <Select
                v-model="selectedFlowInstance"
                style="width:70%"
                filterable
              >
                <Option
                  v-for="item in allFlowInstances"
                  :value="item.id"
                  :key="item.id"
                >
                  {{
                    item.procInstName +
                      " " +
                      (item.createdTime || "createdTime") +
                      " " +
                      (item.operator || "operator")
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
                  @on-open-change="getAllFlow"
                  filterable
                >
                  <Option
                    v-for="item in allFlows"
                    :value="item.procDefId"
                    :key="item.procDefId"
                    >{{ item.procDefName + " " + item.createdTime }}</Option
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
                  @on-open-change="getTargetOptions"
                  filterable
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
            <div style="text-align: center;margin-top: 60px;">
              <Button v-if="showExcution" type="info" @click="excutionFlow">{{
                $t("execute")
              }}</Button>
            </div>
          </Col>
          <Col
            span="18"
            style="text-align: center;margin-top: 60px;text-align: center"
          >
            <div class="graph-container" id="graph"></div>
          </Col>
        </Row>
      </Row>
    </Card>
    <Modal
      :title="$t('select_an_operation')"
      v-model="workflowActionModalVisible"
      :footer-hide="true"
      :mask-closable="false"
      :scrollable="true"
    >
      <div
        class="workflowActionModal-container"
        style="text-align: center;margin-top: 20px;"
      >
        <Button type="info" @click="workFlowActionHandler('retry')">
          {{ $t("retry") }}
        </Button>
        <Button
          type="info"
          @click="workFlowActionHandler('skip')"
          style="margin-left: 20px"
          >{{ $t("skip") }}</Button
        >
      </div>
    </Modal>
  </div>
</template>
<script>
import {
  getAllFlow,
  getFlowOutlineByID,
  getTargetOptions,
  getTreePreviewData,
  createFlowInstance,
  getProcessInstances,
  getProcessInstance,
  retryProcessInstance
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
      allFlowInstances: [],
      allFlows: [],
      allTarget: [],
      currentFlowNodeId: "",
      foundRefAry: [],
      selectedFlowInstance: "",
      selectedFlow: "",
      selectedTarget: "",
      showExcution: true,
      isShowBody: false,
      isEnqueryPage: false,
      workflowActionModalVisible: false,
      currentFailedNodeID: "",
      timer: null
    };
  },
  mounted() {
    this.getProcessInstances();
    this.getAllFlow();
  },
  methods: {
    async getProcessInstances(
      isAfterCreate = false,
      createResponse = undefined
    ) {
      let { status, data, message } = await getProcessInstances();
      if (status === "OK") {
        this.allFlowInstances = data.sort((a, b) => {
          return b.id - a.id;
        });
        if (isAfterCreate) {
          this.selectedFlowInstance = createResponse.id;
          this.processInstance();
        }
      }
    },
    async getAllFlow() {
      let { status, data, message } = await getAllFlow(false);
      if (status === "OK") {
        this.allFlows = data.sort((a, b) => {
          let s = a.createdTime.toLowerCase();
          let t = b.createdTime.toLowerCase();
          if (s > t) return -1;
          if (s < t) return 1;
        });
      }
    },

    orchestrationSelectHandler() {
      this.getFlowOutlineData(this.selectedFlow);
      if (this.selectedFlow && this.isEnqueryPage === false) {
        this.showExcution = true;
      }
    },
    async getTargetOptions() {
      if (!(this.flowData && this.flowData.rootEntity)) return;
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
      clearInterval(this.timer);
      this.timer = null;
      if (!this.selectedFlowInstance) return;
      this.isShowBody = true;
      this.isEnqueryPage = true;
      this.$nextTick(async () => {
        const found = this.allFlowInstances.find(
          _ => _.id === this.selectedFlowInstance
        );
        let { status, data, message } = await getProcessInstance(
          found && found.id
        );
        if (status === "OK") {
          this.flowData = {
            ...data,
            flowNodes: data.taskNodeInstances
          };
          this.initFlowGraph(true);
          removeEvent(".retry", "click", this.retryHandler);
          addEvent(".retry", "click", this.retryHandler);
          d3.selectAll(".retry").attr("cursor", "pointer");

          this.showExcution = false;
        }

        this.selectedFlow = found.procDefId;
        this.getTargetOptions();
        this.selectedTarget = found.entityDataId;
        this.getModelData();
      });
    },
    createHandler() {
      clearInterval(this.timer);
      this.timer = null;
      this.isShowBody = true;
      this.isEnqueryPage = false;
      this.selectedFlowInstance = "";
      this.selectedTarget = "";
      this.selectedFlow = "";
      this.modelData = [];
      this.flowData = {};
      this.showExcution = false;
      this.initModelGraph();
    },
    onTargetSelectHandler() {
      this.getModelData();
    },
    async getModelData() {
      let { status, data, message } = await getTreePreviewData(
        this.selectedFlow,
        this.selectedTarget
      );
      if (status === "OK") {
        this.modelData = data.map(_ => {
          return {
            ..._,
            refFlowNodeIds: []
          };
        });
        this.initModelGraph();
      }
    },
    async getFlowOutlineData(id) {
      let { status, data, message } = await getFlowOutlineByID(id);
      if (status === "OK") {
        this.flowData = data;
        this.initFlowGraph();
        this.getTargetOptions();
      }
    },
    renderModelGraph() {
      let nodes = this.modelData.map((_, index) => {
        const nodeId = _.packageName + "_" + _.entityName;
        let color = _.isHighlight ? "#5DB400" : "black";
        const isRecord = _.refFlowNodeIds.length > 0;
        const shape = isRecord ? "Mrecord" : "ellipse";
        const label =
          _.refFlowNodeIds.toString().replace(/,/g, "/") +
          (isRecord ? "|" : "") +
          _.packageName +
          "_" +
          _.entityName +
          "_" +
          _.dataId;
        return `${nodeId} [label="${
          isRecord ? label : _.packageName + "_" + _.entityName + "_" + _.dataId
        }" class="model" id="${nodeId}" color="${color}" shape="${shape}" width="5"]`;
      });
      let genEdge = () => {
        let pathAry = [];

        this.modelData.forEach(_ => {
          if (_.succeedingIds.length > 0) {
            const nodeId = _.packageName + "_" + _.entityName;
            let current = [];
            current = _.succeedingIds.map(to => {
              let tos = to.split(":");
              return nodeId + " -> " + (tos[0] + "_" + tos[1]);
            });
            pathAry.push(current);
          }
        });
        return pathAry
          .flat()
          .toString()
          .replace(/,/g, ";");
      };
      let nodesToString =
        Array.isArray(nodes) && nodes.length > 0
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

      this.graph.graphviz.renderDot(nodesString);
    },
    renderFlowGraph(excution) {
      const statusColor = {
        Completed: "#5DB400",
        deployed: "#7F8A96",
        InProgress: "#3C83F8",
        Faulted: "#FF6262",
        Timeouted: "#F7B500",
        NotStarted: "#7F8A96"
      };
      let nodes =
        this.flowData &&
        this.flowData.flowNodes &&
        this.flowData.flowNodes
          .filter(i => i.status != "predeploy")
          .map((_, index) => {
            if (_.nodeType === "startEvent" || _.nodeType === "endEvent") {
              return `${_.nodeId} [label="${_.nodeName ||
                "Null"}", fontsize="10", class="flow",style="${
                excution ? "filled" : "none"
              }" color="${
                excution ? statusColor[_.status] : "#7F8A96"
              }" shape="circle", id="${_.nodeId}"]`;
            } else {
              const className =
                _.status === "Faulted" || _.status === "Timeouted"
                  ? "retry"
                  : "";
              return `${_.nodeId} [fixedsize=false label="${_.orderedNo +
                "、" +
                _.nodeName}" class="flow ${className}" style="${
                excution ? "filled" : "none"
              }" color="${
                excution
                  ? statusColor[_.status]
                  : _.nodeId === this.currentFlowNodeId
                  ? "#5DB400"
                  : "#7F8A96"
              }"  shape="box" id="${_.nodeId}" ]`;
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

      this.flowGraph.graphviz.renderDot(nodesString);
      this.bindFlowEvent();
    },
    async excutionFlow() {
      // 区分已存在的flowInstance执行 和 新建的执行
      if (this.isEnqueryPage) {
        this.processInstance();
        this.showExcution = false;
      } else {
        const currentTarget = this.allTarget.find(
          _ => _.id === this.selectedTarget
        );
        let taskNodeBinds = [];
        this.modelData.forEach(_ => {
          let temp = [];
          _.refFlowNodeIds.forEach(i => {
            temp.push({
              ..._,
              flowOrderNo: i
            });
          });
          taskNodeBinds = taskNodeBinds.concat(temp);
        });

        let payload = {
          entityDataId: currentTarget.id,
          entityTypeId: this.flowData.rootEntity,
          procDefId: this.flowData.procDefId,
          taskNodeBinds: taskNodeBinds.map(_ => {
            const node = this.flowData.flowNodes.find(
              node => node.orderedNo === _.flowOrderNo
            );
            return {
              entityDataId: _.dataId,
              entityTypeId: this.flowData.rootEntity,
              nodeDefId: (node && node.nodeDefId) || "",
              orderedNo: _.flowOrderNo
            };
          })
        };
        let { status, data, message } = await createFlowInstance(payload);
        if (status === "OK") {
          this.getProcessInstances(true, data);
          this.showExcution = false;
          this.isEnqueryPage = true;
        }
      }
    },
    start() {
      if (this.timer === null) {
        this.getStatus();
      }
      if (this.timer != null) {
        clearInterval(this.timer);
        this.timer = null;
      }
      this.timer = setInterval(() => {
        this.getStatus();
      }, 5000);
    },
    stop() {
      clearInterval(this.timer);
      this.timer = null;
    },
    async getStatus() {
      const found = this.allFlowInstances.find(
        _ => _.id === this.selectedFlowInstance
      );
      let { status, data, message } = await getProcessInstance(
        found && found.id
      );
      if (status === "OK") {
        this.flowData = {
          ...data,
          flowNodes: data.taskNodeInstances
        };
        this.initFlowGraph(true);
        removeEvent(".retry", "click", this.retryHandler);
        addEvent(".retry", "click", this.retryHandler);
        d3.selectAll(".retry").attr("cursor", "pointer");
        if (data.status === "Completed") {
          this.stop();
        }
      }
    },
    processInstance() {
      this.timer = null;
      this.start();
    },
    retryHandler(e) {
      this.currentFailedNodeID = e.target.parentNode.getAttribute("id");
      this.workflowActionModalVisible = true;
    },
    async workFlowActionHandler(type) {
      const found = this.flowData.flowNodes.find(
        _ => _.nodeId === this.currentFailedNodeID
      );
      if (!found) {
        return;
      }
      const payload = {
        act: type,
        nodeInstId: found.id,
        procInstId: found.procInstId
      };
      const { data, message, status } = await retryProcessInstance(payload);
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc:
            (type === "retry" ? "Retry" : "Skip") +
            " action is proceed successfully"
        });
        this.workflowActionModalVisible = false;
        this.processInstance();
      }
    },
    bindFlowEvent() {
      if (this.isEnqueryPage !== true) {
        addEvent(".flow", "mouseover", e => {
          e.preventDefault();
          e.stopPropagation();
          d3.selectAll("g").attr("cursor", "pointer");
        });
        removeEvent(".flow", "click", this.flowNodesClickHandler);
        addEvent(".flow", "click", this.flowNodesClickHandler);
      }
    },
    flowNodesClickHandler(e) {
      e.preventDefault();
      e.stopPropagation();
      let g = e.currentTarget;
      this.highlightModel(g.id);
      this.currentFlowNodeId = g.id;
      this.renderFlowGraph();
    },
    highlightModel(nodeId) {
      this.foundRefAry = this.flowData.flowNodes
        .find(item => item.nodeId == nodeId)
        .routineExpression.split(/[~.>]/);
      this.modelData.forEach(item => {
        item["isHighlight"] = this.foundRefAry[
          this.foundRefAry.length - 1
        ].includes(item.entityName);
      });
      this.renderModelGraph();
      removeEvent(".model", "click", this.modelClickHandler);
      this.foundRefAry.forEach(_ => {
        // replace ':' by _'
        addEvent(`#${_.replace(/:/g, "_")}`, "click", this.modelClickHandler);
      });
    },
    modelClickHandler(e) {
      e.preventDefault();
      e.stopPropagation();
      let g = e.currentTarget;
      let foundModelNode = this.modelData.find(
        _ => _.packageName + "_" + _.entityName == g.id
      );
      const currentFlow = this.flowData.flowNodes.find(
        i => i.nodeId === this.currentFlowNodeId
      );
      const flowNodeIndex = foundModelNode.refFlowNodeIds.indexOf(
        currentFlow.orderedNo
      );
      if (flowNodeIndex > -1) {
        foundModelNode.refFlowNodeIds.splice(flowNodeIndex, 1);
      } else {
        foundModelNode.refFlowNodeIds.push(currentFlow.orderedNo);
      }
      document.getElementById("graph").innerHTML = "";
      this.renderModelGraph();
      this.foundRefAry.forEach(_ => {
        addEvent(`#${_.replace(/:/g, "_")}`, "click", this.modelClickHandler);
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
    initFlowGraph(excution = false) {
      const initEvent = () => {
        let graph;
        graph = d3.select(`#flow`);
        graph.on("dblclick.zoom", null);
        this.flowGraph.graphviz = graph.graphviz().zoom(false);
      };
      initEvent();
      this.renderFlowGraph(excution);
    }
  }
};
</script>
<style lang="scss" scoped>
body {
  color: #15a043;
}
</style>
