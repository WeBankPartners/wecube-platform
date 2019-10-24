<template>
  <div>
    <Row>
      <span style="margin-right: 10px">{{ $t("deployment_tasks") }}</span>
      <Select
        filterable
        @on-change="onDeployTaskSelect"
        label-in-name
        style="width: 500px;"
      >
        <Option v-for="item in allDeployTask" :value="item.id" :key="item.id">
          {{ item.name }}
        </Option>
      </Select>
    </Row>
    <hr style="margin: 10px 0" />
    <Row class="deploy_ment_tree" style="max-height: 500px">
      <Tree :data="deployTree" show-checkbox check-strictly></Tree>
    </Row>
    <Row>
      <div style="overflow-x: auto; display: -webkit-box; margin-top: 20px">
        <div
          v-for="(item, index) in graphSource"
          :key="index"
          style="width: 20%;"
        >
          <h3 style="text-align: center;">{{ item.definitionName }}</h3>
          <div
            class="graph-container"
            :id="`graph_${item.defintiionKey}_${index}`"
            style="text-align: center;margin-top: 20px;"
          ></div>
        </div>
      </div>
    </Row>
  </div>
</template>
<script>
import * as d3 from "d3-selection";
import * as d3Graphviz from "d3-graphviz";
const endEvent = require("../images/endEvent.png");
const errEndEvent = require("../images/errEndEvent.png");
const eventBasedGateway = require("../images/eventBasedGateway.png");
const exclusiveGateway = require("../images/exclusiveGateway.png");
const intermediateCatchEvent = require("../images/intermediateCatchEvent.png");
const startEvent = require("../images/startEvent.png");
const serviceTask = require("../images/serviceTask.png");
import {
  listProcessTransactions,
  refreshStatusesProcessTransactions,
  getAllDeployTreesFromDesignCi
} from "@/api/server.js";
export default {
  data() {
    return {
      allDeployTask: [],
      graphSource: [],
      graphs: {},
      currentTaskId: "",
      graphsTimer: null,
      deployTree: [],
      currentTaskFilters: {},
      treeCheckedNode: []
    };
  },
  mounted() {
    this.getAllDeployTask();
  },
  destroyed() {
    clearInterval(this.graphsTimer);
  },
  methods: {
    async getAllDeployTask() {
      const { data, status, message } = await listProcessTransactions();
      if (status === "OK") {
        this.allDeployTask = data;
      }
    },
    onDeployTaskSelect(id) {
      this.currentTaskId = id;
      this.treeCheckedNode = [];
      const found = this.allDeployTask.find(_ => _.id === id);
      found &&
        found.attach &&
        found.attach.attachItems &&
        found.attach.attachItems.forEach(i => {
          this.$set(this.currentTaskFilters, i.filterName, i.filterValue);
        });
      this.treeCheckedNode = found.tasks.map(t => t.rootCiDataId);
      this.queryDeployFlow();
      this.querySysTree();
      this.graphsTimer = null;
      this.graphsTimer = setInterval(() => {
        this.queryDeployFlow();
      }, 30000);
    },
    async queryDeployFlow() {
      const {
        data,
        status,
        message
      } = await refreshStatusesProcessTransactions(this.currentTaskId);
      if (status === "OK") {
        this.graphSource = data;
        data.forEach((_, index) => {
          this.$set(this.graphs, _.defintiionKey + "_" + index, {});
        });
        this.$nextTick(() => {
          this.initGraph();
        });
      }
    },
    loadImage(index, nodesString) {
      (nodesString.match(/image=[^,]*(img\/\d*|png)/g) || [])
        .filter((value, index, self) => {
          return self.indexOf(value) === index;
        })
        .map(keyvaluepaire => keyvaluepaire.substr(7))
        .forEach(image => {
          this.graphs[index].graphviz.addImage(image, "48px", "48px");
        });
    },
    genDOT(raw) {
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
        'Node [fontname=Arial, width="0.8", height="0.8", color="#273c75" ,fontsize=10];',
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];'
      ];
      let drawConnection = (from, to) => {
        return `"${from.id}" -> "${to.id}"[edgetooltip="${to.name}" color="${
          statusColor[from.status]
        }"];`;
      };
      let addNodeAttr = node => {
        const color = "#273c75";
        let path = `${shapes[node.nodeTypeName] || shapes.startEvent}`;
        return `"${node.id}" [image="${path}" label="${
          node.name
        }" labelloc="b", shape="box" color="${
          statusColor[node.status]
        }" fontcolor="#000"];`;
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
    renderGraph(data, index) {
      let nodesString = this.genDOT(data.flowNodes || []);
      this.loadImage(data.defintiionKey + "_" + index, nodesString);
      this.graphs[data.defintiionKey + "_" + index].graphviz.renderDot(
        nodesString
      );
    },
    initGraph() {
      const initEvent = () => {
        this.graphSource.forEach((item, index) => {
          let graph;
          graph = d3.select("#graph_" + item.defintiionKey + "_" + index);
          graph.on("dblclick.zoom", null);
          this.graphs[
            item.defintiionKey + "_" + index
          ].graphviz = graph.graphviz().zoom(false);
        });
      };

      initEvent();
      this.graphSource.forEach((_, index) => {
        this.renderGraph(_, index);
      });
    },
    async querySysTree() {
      const treeResponse = await getAllDeployTreesFromDesignCi(
        this.currentTaskFilters.systemDesignVersion,
        this.currentTaskFilters.env
      );
      if (treeResponse.status === "OK") {
        this.deployTree = this.formatTree(treeResponse.data);
      }
    },
    formatTree(data) {
      return data.map(_ => {
        if (_.children && _.children.length > 0) {
          return {
            ..._,

            title: _.data.key_name,
            id: _.guid,
            expand: true,
            disableCheckbox: true,
            children: this.formatTree(_.children),
            checked: !!this.treeCheckedNode.find(n => n === _.guid)
          };
        } else {
          return {
            ..._,
            title: _.data.key_name,
            id: _.guid,
            expand: true,
            disableCheckbox: true,
            checked: !!this.treeCheckedNode.find(n => n === _.guid)
          };
        }
      });
    }
  }
};
</script>
<style lang="scss">
.deploy_ment_tree {
  .ivu-checkbox-disabled.ivu-checkbox-checked .ivu-checkbox-inner {
    background-color: #2d8cf0;
  }
}
</style>
