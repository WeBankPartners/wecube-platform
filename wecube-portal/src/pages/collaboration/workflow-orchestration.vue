<template>
  <div>
    <Row style="margin-bottom: 10px">
      <Col span="6">
        <span style="margin-right: 10px">{{ $t("flow_name") }}</span>
        <Select
          filterable
          clearable
          @on-clear="createNewDiagram"
          v-model="selectedFlow"
          style="width: 70%"
        >
          <Option
            v-for="item in allFlows"
            :value="item.procDefId"
            :key="item.procDefId"
          >
            {{
              (item.procDefName || "null") +
                "-" +
                item.procDefId +
                (item.status === "draft" ? "*" : "")
            }}
          </Option>
        </Select>
      </Col>
      <Col span="6" ofset="1">
        <span style="margin-right: 10px">{{ $t("instance_type") }}</span>
        <Select
          @on-change="onCISelect"
          label-in-value
          v-model="selectedCI.value"
          style="width: 70%"
        >
          <Option
            v-for="item in allCITypes"
            :value="item.ciTypeId || ''"
            :key="item.ciTypeId"
            >{{ item.name || "-" }}</Option
          >
        </Select>
      </Col>
      <Button type="info" @click="saveDiagram(false)">
        {{ $t("save_flow") }}
      </Button>
      <!-- <Button type="success" @click="calcFlow">{{ $t("calc_form") }}</Button> -->
    </Row>
    <div class="containers" ref="content">
      <div class="canvas" ref="canvas"></div>
      <div id="right_click_menu">
        <a href="javascript:void(0);" @click="openPluginModal">{{
          $t("config_plugin")
        }}</a>
        <br />
      </div>

      <div id="js-properties-panel" class="panel"></div>
      <ul class="buttons">
        <li>
          <Button @click="resetZoom">Reset Zoom</Button>
        </li>
      </ul>
    </div>
    <Modal v-model="pluginModalVisible" :title="$t('config_plugin')" width="40">
      <Form
        ref="pluginConfigForm"
        :model="pluginForm"
        label-position="left"
        :label-width="100"
      >
        <FormItem :label="$t('locate_rules')" prop="routineExpression">
          <Input v-model="pluginForm.routineExpression" />
        </FormItem>
        <FormItem :label="$t('plugin')" prop="serviceName">
          <Select filterable clearable v-model="pluginForm.serviceId">
            <Option
              v-for="item in allPlugins"
              :value="item.serviceName"
              :key="item.serviceName"
              >{{ item.serviceDisplayName }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('timeout')" prop="timeoutExpression">
          <Select clearable v-model="pluginForm.timeoutExpression">
            <Option v-for="item in timeSelection" :value="item" :key="item"
              >{{ item }} {{ $t("mins") }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('description')" prop="description">
          <Input v-model="pluginForm.description" />
        </FormItem>
        <hr style="margin-bottom: 20px" />
        <FormItem
          :label="item.paramName"
          :prop="item.paramName"
          v-for="(item, index) in pluginForm.paramInfos"
          :key="index"
        >
          <Select
            v-model="item.bindNodeId"
            style="width:200px"
            @on-change="onParamsNodeChange(index)"
          >
            <Option
              v-for="i in currentflowsNodes"
              :value="i.value"
              :key="i.value"
            >
              {{ i.label }}
            </Option>
          </Select>
          <Select v-model="item.bindParamType" style="width:200px">
            <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">
              {{ i.label }}
            </Option>
          </Select>
          <Select v-model="item.bindParamName" style="width:200px">
            <Option
              v-for="i in item.currentParamNames"
              :value="i.value"
              :key="i.value"
              >{{ i.label }}</Option
            >
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="savePluginConfig('pluginConfigForm')">
          {{ $t("confirm") }}
        </Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import Vue from "vue";

import BpmnModeler from "bpmn-js/lib/Modeler";
import propertiesPanelModule from "bpmn-js-properties-panel";
import propertiesProviderModule from "bpmn-js-properties-panel/lib/provider/camunda";

import camundaModdleDescriptor from "camunda-bpmn-moddle/resources/camunda";
import customTranslate from "@/locale/flow-i18n/custom-translate";

/* Left side toolbar and node edit style */
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css";

/* Right side toobar style */
import "bpmn-js-properties-panel/dist/assets/bpmn-js-properties-panel.css";

import {
  getAllCITypesByLayerWithAttr,
  getAllFlow,
  saveFlow,
  saveFlowDraft,
  getFlowDetailByID,
  getLatestOnlinePluginInterfaces,
  getFlowNodes,
  getParamsInfosByFlowIdAndNodeId
} from "@/api/server.js";

import AttrInput from "../components/attr-input";

function setCTM(node, m) {
  var mstr =
    "matrix(" +
    m.a +
    "," +
    m.b +
    "," +
    m.c +
    "," +
    m.d +
    "," +
    m.e +
    "," +
    m.f +
    ")";
  node.setAttribute("transform", mstr);
}

export default {
  components: {
    AttrInput
  },
  data() {
    return {
      newFlowID: "",
      bpmnModeler: null,
      container: null,
      canvas: null,
      processName: "",
      currentNode: {
        id: "",
        name: ""
      },
      additionalModules: [propertiesProviderModule, propertiesPanelModule],
      allFlows: [],
      allCITypes: [],
      selectedFlow: "",
      selectedCI: {
        value: "",
        label: ""
      },
      pluginModalVisible: false,

      pluginForm: {},
      defaultPluginForm: {
        description: "",
        nodeDefId: "",
        nodeId: "",
        nodeName: "",
        nodeType: "",
        orderedNo: "",
        paramInfos: [],
        procDefId: "",
        procDefKey: "",
        routineExpression: "",
        routineRaw: "",
        serviceId: "",
        serviceName: "",
        status: "",
        timeoutExpression: "30"
      },
      serviceTaskBindInfos: [],
      allPlugins: [],
      timeSelection: ["5", "10", "20", "30", "60"],
      paramsTypes: [
        { value: "in", label: "入参" },
        { value: "out", label: "出参" }
      ],
      currentflowsNodes: [
        { value: "Node1", label: "Node1" },
        { value: "Node2", label: "Node2" }
      ]
    };
  },
  watch: {
    selectedFlow: {
      handler(val) {
        val && this.getFlowXml(val);
      }
      // immediate: true
    }
  },
  created() {
    this.init();
  },
  mounted() {
    this.initFlow();
  },
  methods: {
    init() {
      this.getAllCITypesByLayerWithAttr();
      this.getAllFlows();
      this.getAllPlugins();
    },
    async getAllCITypesByLayerWithAttr() {
      let ciTypes = {};
      let ciTypeAttrs = {};

      this.allCITypes = [
        {
          ciTypeId: "code1",
          name: "value1"
        },
        {
          ciTypeId: "code2",
          name: "value2"
        }
      ];
    },
    async getAllPlugins() {
      this.allPlugins = [
        {
          serviceName: "serviceName1",
          serviceDisplayName: "serviceDisplayName1"
        },
        {
          serviceName: "serviceName2",
          serviceDisplayName: "serviceDisplayName2"
        }
      ];
    },
    async getAllFlows() {
      const { data, message, status } = await getAllFlow();
      if (status === "OK") {
        this.allFlows = data || [];
      }
    },
    // onFlowSelect(v) {
    //   console.log(3, this.selectedFlow)
    //   v && this.getFlowXml(v);
    // },

    onCISelect(v) {
      this.selectedCI = v;
      if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = [];
      this.pluginForm = this.defaultPluginForm;
    },
    resetZoom() {
      var canvas = this.bpmnModeler.get("canvas");
      canvas._changeViewbox(function() {
        setCTM(canvas._viewport, {
          a: "1",
          b: "0",
          c: "0",
          d: "1",
          e: "0",
          f: "0"
        });
      });
    },
    createNewDiagram() {
      this.newFlowID = "wecube" + Date.now();
      const bpmnXmlStr =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn">\n' +
        '  <bpmn2:process id="' +
        this.newFlowID +
        '" isExecutable="true">\n' +
        "  </bpmn2:process>\n" +
        '  <bpmndi:BPMNDiagram id="BPMNDiagram_1">\n' +
        '    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="' +
        this.newFlowID +
        '">\n' +
        "    </bpmndi:BPMNPlane>\n" +
        "  </bpmndi:BPMNDiagram>\n" +
        "</bpmn2:definitions>";
      this.bpmnModeler.importXML(bpmnXmlStr, function(err) {
        if (err) {
          console.error(err);
        }
      });
    },
    saveDiagram(isDraft) {
      let _this = this;
      const okHandler = data => {
        this.getAllFlows();
        this.selectedFlow = data.data.procDefId;
      };
      this.bpmnModeler.saveXML({ format: true }, function(err, xml) {
        if (!xml) return;
        const xmlString = xml.replace(/[\r\n]/g, "");
        const processName = document.getElementById("camunda-name").innerText;

        const payload = {
          procDefData: xmlString,
          procDefId: "",
          procDefKey: _this.newFlowID,
          procDefName: processName,
          rootEntity: _this.selectedCI.value,
          status: "",
          taskNodeInfos: _this.serviceTaskBindInfos
        };

        isDraft
          ? saveFlowDraft(payload).then(data => {
              if (data && data.status === "OK") {
                _this.$Notice.success({
                  title: "Success",
                  desc: data.message
                });
                okHandler(data);
              }
            })
          : saveFlow(payload).then(data => {
              if (data && data.status === "OK") {
                _this.$Notice.success({
                  title: "Success",
                  desc: data.message
                });

                okHandler(data);
              }
            });
      });
    },
    savePluginConfig(ref) {
      let index = -1;
      this.serviceTaskBindInfos.forEach((_, i) => {
        if (this.currentNode.id === _.nodeId) {
          index = i;
        }
      });
      if (index > -1) {
        this.serviceTaskBindInfos.splice(index, 1);
      }

      let found = this.allPlugins.find(
        _ => _.serviceName === this.pluginForm.serviceId
      );

      let pluginFormCopy = JSON.parse(JSON.stringify(this.pluginForm));
      this.serviceTaskBindInfos.push({
        ...pluginFormCopy,
        nodeId: this.currentNode.id,
        nodeName: this.currentNode.name,
        serviceName: (found && found.serviceName) || "",
        routineRaw: pluginFormCopy.routineExpression
      });
      this.pluginModalVisible = false;
      this.saveDiagram(true);
    },
    async openPluginModal() {
      if (!this.selectedCI.value) {
        this.$Notice.warning({
          title: "Warning",
          desc: this.$t("select_ci_first")
        });
      } else {
        this.pluginModalVisible = true;
        this.pluginForm =
          this.currentFlow.taskNodeInfos.find(
            _ => _.nodeId === this.currentNode.id
          ) || this.defaultPluginForm;
        /* ************* */
        this.pluginForm.paramInfos.push({
          bindNodeId: "Node1",
          bindParamName: "paramsName1",
          bindParamType: "in",
          id: "empty",
          nodeId: "node id",
          paramName: "PARAMS A"
        });
        this.pluginForm.paramInfos.push({
          bindNodeId: "Node2",
          bindParamName: "paramsName2",
          bindParamType: "out",
          id: "empty",
          nodeId: "node id",
          paramName: "PARAMS B"
        });
        /* ************* */

        // get flow's params infos - nodes
        // this.getFlowsNodes()
        this.pluginForm.paramInfos.forEach((_, index) => {
          this.onParamsNodeChange(index);
        });
      }
    },
    onParamsNodeChange(index) {
      this.getParamsOptionsByNode(index);
    },
    async getFlowsNodes() {
      // let { status, data, message} = await getFlowNodes(this.currentFlow.procDefId)
      // if(status === "OK") {
      //   this.currentflowsNodes = data
      // }
    },
    async getParamsOptionsByNode(index) {
      // let { status, data, message } = await getParamsInfosByFlowIdAndNodeId(
      //   this.currentFlow.procDefId,
      //   this.pluginForm.paramInfos[index].bindNodeId
      // );
      // if (status === "OK") {
      this.pluginForm.paramInfos[index].currentParamNames = [
        { value: "paramsName1", label: "paramsName1" },
        { value: "paramsName2", label: "paramsName2" }
      ];
      // }
    },
    bindRightClick() {
      var menu = document.getElementById("right_click_menu");
      var elements = document.getElementsByClassName("djs-element djs-shape");
      const _this = this;
      for (var i = 0; i < elements.length; i++) {
        elements[i].oncontextmenu = function(e) {
          var e = e || window.event;
          var x = e.clientX;
          var y = e.clientY;
          menu.style.display = "block";
          menu.style.left = x - 25 + "px";
          menu.style.top = y - 130 + "px";
          _this.currentNode.id = e.target.parentNode.getAttribute(
            "data-element-id"
          );
          _this.currentNode.name =
            (e.target.previousSibling &&
              e.target.previousSibling.children[1] &&
              e.target.previousSibling.children[1].children[0] &&
              e.target.previousSibling.children[1].children[0].innerHTML) ||
            "";
          return false;
        };
      }

      document.onclick = function(e) {
        var e = e || window.event;
        menu.style.display = "none";
      };

      menu.onclick = function(e) {
        var e = e || window.event;
        e.stopPropagation();
      };
    },
    async getFlowXml(id) {
      const { status, message, data } = await getFlowDetailByID(id);
      if (status === "OK") {
        this.currentFlow = data;
        const _this = this;
        this.bpmnModeler.importXML(data.procDefData, function(err) {
          if (err) {
            console.error(err);
          }
          _this.bindRightClick();
          _this.serviceTaskBindInfos = data.taskNodeInfos;
          _this.selectedCI.value = data.rootEntity || "";
        });
      }
    },
    initFlow() {
      this.container = this.$refs.content;
      const canvas = this.$refs.canvas;
      canvas.onmouseup = () => {
        this.bindRightClick();
      };
      var customTranslateModule = {
        translate: ["value", customTranslate]
      };

      if (this.$lang === "zh-CN") {
        this.additionalModules.push(customTranslateModule);
      } else {
        if (this.additionalModules.length > 2) {
          this.additionalModules.pop();
        }
      }

      this.bpmnModeler = new BpmnModeler({
        container: canvas,
        propertiesPanel: {
          parent: "#js-properties-panel"
        },
        additionalModules: this.additionalModules,

        moddleExtensions: {
          camunda: camundaModdleDescriptor
        }
      });
      this.createNewDiagram();
    }
  }
};
</script>
<style lang="scss">
.containers {
  position: absolute;
  background-color: white;
  border: #999 1px solid;
  width: 97%;
  height: 84%;
}
.canvas {
  width: 100%;
  height: 100%;
}

#right_click_menu {
  display: none;
  width: 100px;
  border: 1px solid gray;
  position: absolute;
  background-color: white;
  padding: 5px 5px;
  box-shadow: 0 0 5px grey;
}
.panel {
  position: absolute;
  right: 0;
  top: 0;
  width: 400px;
  max-height: 100%;
  overflow-y: auto;

  .bpp-properties-panel .entry-label {
    border: black solid 1px;
    border-radius: 5px;
    background-color: white;
    padding: 2px 7px;
    font-style: normal;
  }
}
.buttons {
  position: absolute;
  left: 20px;
  bottom: 20px;
  & > li {
    display: inline-block;
    margin: 5px;
    & > a {
      color: #999;
      background: #eee;
      cursor: not-allowed;
      padding: 8px;
      border: 1px solid #ccc;
      &.active {
        color: #333;
        background: #fff;
        cursor: pointer;
      }
    }
  }
}
</style>
