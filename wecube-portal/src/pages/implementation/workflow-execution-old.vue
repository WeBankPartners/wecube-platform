<template>
  <div>
    <Tabs
      type="card"
      :value="currentTab"
      closable
      @on-tab-remove="handleTabRemove"
      @on-click="handleTabClick"
    >
      <TabPane :closable="false" name="CMDB" :label="$t('cmdb_model')">
        <div class="graph-container" id="graph"></div>
      </TabPane>
      <TabPane
        v-for="ci in tabList"
        :key="ci.id"
        :name="ci.id"
        :label="ci.name"
      >
        <WeTable
          :tableData="ci.tableData"
          :tableOuterActions="ci.outerActions"
          :tableInnerActions="ci.innerActions"
          :tableColumns="ci.tableColumns"
          :pagination="ci.pagination"
          :ascOptions="ci.ascOptions"
          :showCheckbox="false"
          @actionFun="actionFun"
          @handleSubmit="handleSubmit"
          @pageChange="pageChange"
          @pageSizeChange="pageSizeChange"
          tableHeight="650"
          :ref="'table' + ci.id"
        ></WeTable>
      </TabPane>
    </Tabs>
    <Modal
      :title="previewDefinitionName"
      v-model="previewVisibleSwap"
      :footer-hide="true"
      :mask-closable="false"
      @on-visible-change="hidePreModal"
      :scrollable="true"
    >
      <div
        class="graph-container"
        id="graph_preview"
        style="text-align: center;margin-top: 20px;"
      ></div>
      <div style="text-align: right;margin-top: 20px;">
        <Button type="info" @click="startProcessHandler">
          {{ $t("execute") }}
        </Button>
      </div>
    </Modal>
    <Modal
      :title="refreshDefinitionName"
      v-model="refreshVisibleSwap"
      :footer-hide="true"
      :mask-closable="false"
      @on-visible-change="hideRefreshModal"
      :scrollable="true"
    >
      <div
        class="graph-container"
        id="graph_refresh"
        style="text-align: center;margin-top: 20px;"
      ></div>
    </Modal>

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
        <p v-if="currentNodeStsatus === 'Completed'" style="margin: 25px 0px;">
          {{ $t("repeat_prompt") }}
        </p>

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
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
import { addEvent } from "../util/event.js";
import {
  getAllCITypesByLayerWithAttr,
  getAllLayers,
  queryCiData,
  getCiTypeAttributes,
  deleteCiDatas,
  createCiDatas,
  updateCiDatas,
  getEnumCodesByCategoryId,
  operateCiState,
  previewProcessDefinition,
  startProcessInstanceWithCiData,
  refreshProcessInstanceStatus,
  restartProcessInstance
} from "@/api/server";
import { setHeaders } from "@/api/base.js";
import { pagination, components } from "@/const/actions.js";
import { formatData } from "../util/format.js";
const defaultCiTypePNG = require("@/assets/ci-type-default.png");
const endEvent = require("../images/endEvent.png");
const errEndEvent = require("../images/errEndEvent.png");
const eventBasedGateway = require("../images/eventBasedGateway.png");
const exclusiveGateway = require("../images/exclusiveGateway.png");
const intermediateCatchEvent = require("../images/intermediateCatchEvent.png");
const startEvent = require("../images/startEvent.png");
const serviceTask = require("../images/serviceTask.png");

export default {
  data() {
    return {
      graphsTimer: null,
      previewDefinitionName: "",
      previewVisibleSwap: false,
      graph_preview: [],
      refreshDefinitionName: "",
      refreshVisibleSwap: false,
      graph_refresh: [],
      tabList: [],
      currentTab: "CMDB",
      currentExeData: {},
      payload: {
        filters: [],
        pageable: {
          pageSize: 10,
          startIndex: 0
        },
        paging: true,
        sorting: {}
      },
      source: {},
      layers: [],
      graph: {},
      graphs: {
        graph_preview: {},
        graph_refresh: {}
      },
      g: {},
      currentNodeID: "",
      workflowActionModalVisible: false,
      currentNodeStsatus: "",
      innerActions: [
        {
          label: this.$t("preview_execution"),
          props: {
            type: "info",
            size: "small"
          },
          actionType: "exePreview",
          visible: {
            key: "biz_key",
            value: false
          }
        },
        {
          label: this.$t("query_execution"),
          props: {
            type: "info",
            size: "small"
          },
          actionType: "exeQuery",
          visible: {
            key: "biz_key",
            value: true
          }
        }
      ]
    };
  },
  computed: {
    tableRef() {
      return "table" + this.currentTab;
    }
  },
  methods: {
    async initGraph(filters = ["created", "dirty"]) {
      var origin;
      var edges = {};
      var levels = {};
      let graph;
      let graphviz;

      const initEvent = () => {
        graph = d3.select("#graph");
        graph
          .on("dblclick.zoom", null)
          .on("wheel.zoom", null)
          .on("mousewheel.zoom", null);

        this.graph.graphviz = graph
          .graphviz()
          .zoom(true)
          .scale(0.8)
          .width(window.innerWidth * 0.96)
          .attributer(function(d) {
            if (d.attributes.class === "edge") {
              var keys = d.key.split("->");
              var from = keys[0].trim();
              var to = keys[1].trim();
              d.attributes.from = from;
              d.attributes.to = to;
            }

            if (d.tag === "text") {
              var key = d.children[0].text;
              d3.select(this).attr("text-key", key);
            }
          });
      };

      let layerResponse = await getAllLayers();
      if (layerResponse.status === "OK") {
        let tempLayer = layerResponse.data
          .filter(i => i.status === "active")
          .map(_ => {
            return { name: _.value, layerId: _.codeId, ..._ };
          });
        this.layers = tempLayer.sort((a, b) => {
          return a.seqNo - b.seqNo;
        });
        let ciResponse = await getAllCITypesByLayerWithAttr(filters);
        if (ciResponse.status === "OK") {
          this.source = ciResponse.data;
          this.source.forEach(_ => {
            _.ciTypes &&
              _.ciTypes.forEach(async i => {
                let imgFileSource =
                  i.imageFileId === 0 || i.imageFileId === undefined
                    ? defaultCiTypePNG.substring(0, defaultCiTypePNG.length - 4)
                    : `/cmdb/files/${i.imageFileId}`;
                this.$set(i, "form", {
                  ...i,
                  imgSource: imgFileSource,
                  imgUploadURL: `/cmdb/ci-types/${i.ciTypeId}/icon`
                });
                i.attributes &&
                  i.attributes.forEach(j => {
                    this.$set(j, "form", {
                      ...j,
                      isAccessControlled: j.isAccessControlled ? "yes" : "no",
                      isNullable: j.isNullable ? "yes" : "no",
                      isSystem: j.isSystem ? "yes" : "no"
                    });
                  });
              });
          });
          let uploadToken = document.cookie
            .split(";")
            .find(i => i.indexOf("XSRF-TOKEN") !== -1);
          setHeaders({
            "X-XSRF-TOKEN": (uploadToken && uploadToken.split("=")[1]) || ""
          });
          initEvent();
          this.renderGraph(ciResponse.data);
        }
      }
    },
    genDOT(data) {
      let nodes = [];
      data.forEach(_ => {
        if (_.ciTypes) nodes = nodes.concat(_.ciTypes);
      });
      var dots = [
        "digraph  {",
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="ellipse", fixedsize="true", width="1.1", height="1.1", color="transparent" ,fontsize=11];',
        'Edge [fontname=Arial,minlen="1", color="#7f8fa6", fontsize=10];',
        'ranksep = 1.1; size = "11,11";rankdir=TB'
      ];
      let layerTag = `node [];`;

      // generate group
      let tempClusterObjForGraph = {};
      let tempClusterAryForGraph = [];
      this.layers.map((_, index) => {
        if (index !== this.layers.length - 1) {
          layerTag += '"' + _.name + '"' + "->";
        } else {
          layerTag += '"' + _.name + '"';
        }

        tempClusterObjForGraph[index] = [`{ rank=same; "${_.name}";`];
        nodes.forEach((node, nodeIndex) => {
          if (node.layerId === _.layerId) {
            tempClusterObjForGraph[index].push(
              '"' +
                node.name +
                '"[id=' +
                node.ciTypeId +
                ',image="' +
                node.form.imgSource +
                ".png" +
                '", labelloc="b"];'
            );
          }
          if (nodeIndex === nodes.length - 1) {
            tempClusterObjForGraph[index].push("} ");
          }
        });
        if (nodes.length === 0) {
          tempClusterObjForGraph[index].push("} ");
        }
        tempClusterAryForGraph.push(tempClusterObjForGraph[index].join(""));
      });

      dots.push(tempClusterAryForGraph.join(""));
      dots.push("{" + layerTag + "[style=invis]}");

      //generate edges
      nodes.forEach(node => {
        node.attributes &&
          node.attributes.forEach(attr => {
            if (attr.inputType === "ref" || attr.inputType === "multiRef") {
              var target = nodes.find(_ => _.ciTypeId === attr.referenceId);
              if (target) {
                dots.push(this.genEdge(nodes, node, attr));
              }
            }
          });
      });
      dots.push("}");
      return dots.join("");
    },
    genEdge(nodes, from, to) {
      const target = nodes.find(_ => _.ciTypeId === to.referenceId);
      let labels = to.referenceName ? to.referenceName.trim() : "";
      return (
        '"' +
        from.name +
        '"->' +
        '"' +
        target.name.trim() +
        '"[label="' +
        labels +
        '"];'
      );
    },

    loadImage(nodesString) {
      (nodesString.match(/image=[^,]*(files\/\d*|png)/g) || [])
        .filter((value, index, self) => {
          return self.indexOf(value) === index;
        })
        .map(keyvaluepaire => keyvaluepaire.substr(7))
        .forEach(image => {
          this.graph.graphviz.addImage(image, "48px", "48px");
        });
    },
    shadeAll() {
      d3.selectAll("g path")
        .attr("stroke", "#7f8fa6")
        .attr("stroke-opacity", ".2");
      d3.selectAll("g polygon")
        .attr("stroke", "#7f8fa6")
        .attr("stroke-opacity", ".2")
        .attr("fill", "#7f8fa6")
        .attr("fill-opacity", ".2");
      // d3.selectAll("text").attr("fill", "#000");
      d3.selectAll(".edge text").attr("fill", "#7f8fa6");
    },
    colorNode(nodeName) {
      d3.selectAll('g[from="' + nodeName + '"] path')
        .attr("stroke", "red")
        .attr("stroke-opacity", "1");
      d3.selectAll('g[from="' + nodeName + '"] text').attr("fill", "red");
      d3.selectAll('g[from="' + nodeName + '"] polygon')
        .attr("stroke", "red")
        .attr("fill", "red")
        .attr("fill-opacity", "1")
        .attr("stroke-opacity", "1");
      d3.selectAll('g[to="' + nodeName + '"] path')
        .attr("stroke", "green")
        .attr("stroke-opacity", "1");
      d3.selectAll('g[to="' + nodeName + '"] text').attr("fill", "green");
      d3.selectAll('g[to="' + nodeName + '"] polygon')
        .attr("stroke", "green")
        .attr("fill", "green")
        .attr("fill-opacity", "1")
        .attr("stroke-opacity", "1");
    },
    renderGraph(data) {
      let nodesString = this.genDOT(data);
      this.loadImage(nodesString);
      this.graph.graphviz.renderDot(nodesString);
      this.shadeAll();
      addEvent(".node", "mouseover", async e => {
        d3.selectAll("g").attr("cursor", "pointer");
        e.preventDefault();
        e.stopPropagation();
        this.g = e.currentTarget;
        var nodeName = this.g.children[0].innerHTML.trim();
        this.shadeAll();
        this.colorNode(nodeName);
      });

      addEvent("svg", "mouseover", e => {
        this.shadeAll();
        e.preventDefault();
        e.stopPropagation();
      });

      addEvent(".node", "click", async e => {
        e.preventDefault();
        e.stopPropagation();
        this.g = e.currentTarget;
        var nodeName = this.g.children[0].innerHTML.trim();
        this.queryCiAttrs(this.g.id, nodeName);
      });
    },
    handleTabRemove(name) {
      this.tabList.forEach((_, index) => {
        if (_.id === name) {
          this.tabList.splice(index, 1);
        }
      });
      this.currentTab = "CMDB";
    },
    handleTabClick(name) {
      this.payload.filters = [];
      this.currentTab = name;
    },
    handleSubmit(data) {
      this.payload.filters = data;
      this.queryCiData();
    },
    async queryCiAttrs(id, nodeName) {
      const found = this.tabList.find(_ => _.id === id);
      if (!found) {
        const ci = {
          name: nodeName,
          id: id,
          tableData: [],
          outerActions: JSON.parse(
            JSON.stringify([
              {
                label: "Export",
                props: {
                  type: "primary",
                  icon: "ios-download-outline"
                },
                actionType: "export"
              }
            ])
          ),
          innerActions: JSON.parse(JSON.stringify(this.innerActions)),
          tableColumns: [],
          pagination: JSON.parse(JSON.stringify(pagination)),
          ascOptions: {}
        };

        //   this.queryCiAttrs(g.id);
        const { status, message, data } = await getCiTypeAttributes(id);
        let columns = [];
        const disabledCol = [
          "created_date",
          "updated_date",
          "created_by",
          "updated_by",
          "key_name",
          "guid"
        ];
        if (status === "OK") {
          let columns = [];
          let isOrchestration = false;
          data.forEach(_ => {
            const disEditor = disabledCol.find(i => i === _.propertyName);
            let renderKey = _.propertyName;
            if (_.propertyName === "orchestration") {
              isOrchestration = true;
            }
            if (
              _.status !== "decommissioned" &&
              _.status !== "notCreated" &&
              _.isDisplayed &&
              _.isDisplayed !== 0
            ) {
              columns.push({
                ..._,
                title: _.name,
                key: renderKey,
                inputKey: _.propertyName,
                inputType: _.inputType,
                referenceId: _.referenceId,
                disEditor: !_.isEditable,
                disAdded: !_.isEditable,
                placeholder: _.name,
                component: "Input",
                ciType: { id: _.referenceId, name: _.name },
                type: "text",
                isMultiple: _.inputType === "multiSelect",
                ...components[_.inputType]
              });
            }
          });
          if (isOrchestration) {
            this.tabList.push(ci);
            this.currentTab = id;
            this.queryCiData();
            this.tabList.forEach(ci => {
              if (ci.id === this.currentTab) {
                ci.tableColumns = this.getSelectOptions(columns);
              }
            });
          } else {
            this.$Notice.warning({
              title: "Warning",
              desc: this.$t("no_orchestration_properties")
            });
          }
        }
        //   this.queryCiData();
      } else {
        this.currentTab = this.g.id;
      }
    },
    getSelectOptions(columns) {
      columns.forEach(async _ => {
        if (_.inputType === "select" || _.inputType === "multiSelect") {
          const { status, message, data } = await getEnumCodesByCategoryId(
            0,
            _.referenceId
          );
          _["options"] = data
            .filter(j => j.status === "active")
            .map(i => {
              return {
                label: i.value,
                value: i.codeId
              };
            });
        }
      });
      return columns;
    },
    async queryCiData() {
      this.payload.pageable.pageSize = 10;
      this.payload.pageable.startIndex = 0;
      this.tabList.forEach(ci => {
        if (ci.id === this.currentTab) {
          this.payload.pageable.pageSize = ci.pagination.pageSize;
          this.payload.pageable.startIndex =
            (ci.pagination.currentPage - 1) * ci.pagination.pageSize;
        }
      });
      const query = {
        id: this.currentTab,
        queryObject: this.payload
      };
      const { status, message, data } = await queryCiData(query);
      if (status === "OK") {
        this.tabList.forEach(ci => {
          if (ci.id === this.currentTab) {
            ci.tableData = data.contents.map(_ => {
              return {
                ..._.data,
                nextOperations: _.meta.nextOperations || [],
                citypeId: this.currentTab
              };
            });
            ci.pagination.total = data.pageInfo.totalRows;
          }
        });
      }
    },
    loadFlowImage(index, nodesString) {
      (nodesString.match(/image=[^,]*(img\/\d*|png)/g) || [])
        .filter((value, index, self) => {
          return self.indexOf(value) === index;
        })
        .map(keyvaluepaire => keyvaluepaire.substr(7))
        .forEach(image => {
          this.graphs[index].graphviz.addImage(image, "48px", "48px");
        });
    },
    genFlowDOT(raw) {
      const shapes = {
        startEvent,
        errEndEvent,
        eventBasedGateway,
        intermediateCatchEvent,
        exclusiveGateway,
        endEvent,
        serviceTask
      };
      const statusColor = {
        Completed: "#5DB400",
        NotStarted: "#7F8A96",
        InProgress: "#3C83F8",
        Faulted: "#FF6262",
        Timeouted: "#F7B500"
      };
      var dots = [
        "digraph  {",
        'bgcolor="transparent";',
        'Node [fontname=Arial,shape="none",width="0.8", height="0.8", color="#273c75" ,fontsize=10];',
        'Edge [fontname=Arial, minlen="1", color="#000", fontsize=10];'
      ];
      let drawConnection = (from, to) => {
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.name}" color="${
          statusColor[from.status]
        }"];`;
      };
      let addNodeAttr = node => {
        const color = "#273c75";
        let path = `${shapes[node.nodeTypeName] || shapes.startEvent}`;
        return `"${node.id}" [image="${path}" id="${node.id}" class="${
          node.nodeTypeName
        }" label="${node.name}" labelloc="b", shape="box" color="${
          statusColor[node.status]
        }" fontcolor="${color}"];`;
      };
      const nodeMap = new Map();
      raw.forEach(node => {
        dots.push(addNodeAttr(node));
        if (node.toNodeIds.length) {
          node.toNodeIds.forEach(toId => {
            let found = raw.find(_ => toId === _.id);
            if (found) {
              const dot = drawConnection(node, found);
              if (!nodeMap.has(dot)) {
                dots.push(dot);
                nodeMap.set(dot, true);
              }
            }
          });
        }

        if (node.fromNodeIds.length) {
          node.fromNodeIds.forEach(fromId => {
            let found = raw.find(_ => fromId === _.id);
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
    renderFlowGraph(data, name) {
      let nodesString = this.genFlowDOT(data.flowNodes || []);
      this.loadFlowImage(name, nodesString);
      this.graphs[name].graphviz.renderDot(nodesString);
    },
    initFlowGraph(name) {
      const initEvent = () => {
        let graphs;
        graphs = d3.select(`#${name}`);
        graphs.on("dblclick.zoom", null);
        this.graphs[name].graphviz = graphs.graphviz().zoom(false);
      };

      initEvent();
      const nodeData =
        name === "graph_refresh" ? this.graph_refresh : this.graph_preview;
      this.renderFlowGraph(nodeData, name);
    },
    async exportHandler() {
      const { status, message, data } = await queryCiData({
        id: this.currentTab,
        queryObject: this.payload
      });
      if (status === "OK") {
        this.$refs[this.tableRef][0].export({
          filename: "Ci Data",
          data: formatData(data.contents.map(_ => _.data))
        });
      }
    },
    hidePreModal(status) {
      if (status) return;
      this.previewVisibleSwap = false;
    },
    hideRefreshModal(status) {
      if (status) return;
      this.refreshVisibleSwap = false;
      clearInterval(this.graphsTimer);
    },
    async startProcessHandler() {
      const payload = {
        ciDataId: this.currentExeData.guid,
        ciTypeId: this.currentTab,
        processDefinitionKey: this.currentExeData.orchestration
      };
      const { status, data, message } = await startProcessInstanceWithCiData(
        payload
      );
      if (status === "OK") {
        this.$Notice.success({
          title: "success",
          desc: "success"
        });
      }
    },
    async exePreviewHandler(d) {
      this.currentExeData = d;
      const payload = {
        ciGuid: d.guid,
        ciTypeId: this.currentTab,
        definitionKey: d.orchestration
      };
      const { status, data, message } = await previewProcessDefinition(payload);
      if (status === "OK") {
        this.graph_preview = data;
        this.initFlowGraph("graph_preview");
        this.previewVisibleSwap = true;
      }
    },
    async getExeQuery(d) {
      const { status, data, message } = await refreshProcessInstanceStatus(
        d.biz_key
      );
      if (status === "OK") {
        this.graph_refresh = data;
        this.initFlowGraph("graph_refresh");
        this.refreshVisibleSwap = true;
        this.$nextTick(() => {
          this.bindClick();
        });
      } else {
        clearInterval(this.graphsTimer);
      }
    },
    async workFlowActionHandler(type) {
      const found = this.graph_refresh.flowNodes.find(
        _ => _.id === this.currentNodeID
      );
      if (!found) {
        return;
      }
      const payload = {
        act: type,
        activityId: found.id,
        processInstanceId: found.processInstanceId
      };
      const { data, message, status } = await restartProcessInstance(payload);
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: "Success"
        });
        this.workflowActionModalVisible = false;
      }
    },
    bindClick() {
      const _this = this;
      //add event to serviceTask
      addEvent("#graph_refresh .serviceTask image", "click", e => {
        e.preventDefault();
        e.stopPropagation();
        this.currentNodeID = e.target.parentNode.getAttribute("id");
        this.currentNodeStsatus = _this.graph_refresh.flowNodes.find(
          _ => _.id === this.currentNodeID
        ).status;
        this.workflowActionModalVisible = true;
      });
      addEvent("#graph_refresh .serviceTask image", "mouseover", e => {
        e.preventDefault();
        e.stopPropagation();
        d3.selectAll("#graph_refresh .serviceTask image").attr(
          "cursor",
          "pointer"
        );
      });
      //add event to sub process
      addEvent("#graph_refresh .subProcess image", "click", e => {
        e.preventDefault();
        e.stopPropagation();
        this.currentNodeID = e.target.parentNode.getAttribute("id");
        this.currentNodeStsatus = _this.graph_refresh.flowNodes.find(
          _ => _.id === this.currentNodeID
        ).status;

        this.workflowActionModalVisible = true;
      });
      addEvent("#graph_refresh .subProcess image", "mouseover", e => {
        e.preventDefault();
        e.stopPropagation();
        d3.selectAll("#graph_refresh .subProcess image").attr(
          "cursor",
          "pointer"
        );
      });
    },
    exeQueryHandler(d) {
      this.getExeQuery(d);
      this.graphsTimer = setInterval(() => {
        this.getExeQuery(d);
      }, 30000);
    },
    actionFun(type, data) {
      switch (type) {
        case "export":
          this.exportHandler();
          break;
        case "exePreview":
          this.exePreviewHandler(data);
          break;
        case "exeQuery":
          this.exeQueryHandler(data);
          break;
        default:
          break;
      }
    },
    pageChange(current) {
      this.tabList.forEach(ci => {
        if (ci.id === this.currentTab) {
          ci.pagination.currentPage = current;
        }
      });
      this.queryCiData();
    },
    pageSizeChange(size) {
      this.tabList.forEach(ci => {
        if (ci.id === this.currentTab) {
          ci.pagination.pageSize = size;
        }
      });
      this.queryCiData();
    }
  },
  mounted() {
    this.initGraph();
  }
};
</script>
<style lang="scss"></style>
