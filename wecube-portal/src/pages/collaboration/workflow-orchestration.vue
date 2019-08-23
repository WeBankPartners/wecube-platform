<template>
  <div>
    <Row style="margin-bottom: 10px">
      <Col span="6">
        <span style="margin-right: 10px">编排名称</span>
        <Select
          filterable
          clearable
          @on-clear="createNewDiagram"
          @on-change="onFlowSelect"
          label-in-value
          style="width: 70%"
        >
          <Option
            v-for="item in allFlows"
            :value="item.definitionId"
            :key="item.definitionId"
            >{{ item.processName || "-" }}</Option
          >
        </Select>
      </Col>
      <Col span="6" ofset="1">
        <span style="margin-right: 10px">选择CI类型</span>
        <Select
          @on-change="onCISelect"
          label-in-value
          v-model="selectedCI.value"
          style="width: 70%"
        >
          <OptionGroup
            v-for="group in allCITypes"
            :label="group.value || '-'"
            :key="group.code"
          >
            <Option
              v-for="item in group.ciTypes"
              :value="item.ciTypeId || ''"
              :key="item.ciTypeId"
              >{{ item.name || "-" }}</Option
            >
          </OptionGroup>
        </Select>
      </Col>
      <Button type="info" @click="saveDiagram">保存编排</Button>
      <Button type="success" @click="calcFlow">表单计算</Button>
    </Row>
    <div class="containers" ref="content">
      <div class="canvas" ref="canvas"></div>
      <div id="right_click_menu">
        <a href="javascript:void(0);" @click="openPluginModal">配置插件</a>
        <br />
      </div>

      <div id="js-properties-panel" class="panel"></div>
      <ul class="buttons">
        <li>
          <Button @click="resetZoom">Reset Zoom</Button>
        </li>
      </ul>
    </div>
    <Modal v-model="pluginModalVisible" title="插件配置" width="40">
      <Form
        ref="pluginConfigForm"
        :model="pluginForm"
        label-position="left"
        :label-width="100"
      >
        <FormItem label="节点类型" prop="nodeType" style="display: none">
          <Select filterable clearable v-model="pluginForm.nodeType">
            <Option
              v-for="item in allNodeTypes"
              :value="item.value"
              :key="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>

        <FormItem label="插件选择" prop="serviceName">
          <Select filterable clearable v-model="pluginForm.serviceId">
            <Option
              v-for="item in allPlugins"
              :value="item.serviceName"
              :key="item.serviceName"
              >{{ item.serviceDisplayName }}</Option
            >
          </Select>
        </FormItem>

        <FormItem label="定位规则" prop="rules">
          <div style="width: 100%">
            <AttrInput
              :allCiTypes="allCITypes"
              :cmdbColumnSource="pluginForm.ciRoutineRaw"
              :rootCiType="selectedCI.value"
              v-model="pluginForm.rules"
              :ciTypesObj="this.ciTypesObj"
              :ciTypeAttributeObj="this.ciTypeAttributeObj"
              @change="setRootFilterRule"
            />
          </div>
        </FormItem>
        <FormItem label="描述说明" prop="description">
          <Input v-model="pluginForm.description" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="savePluginConfig('pluginConfigForm')"
          >Submit</Button
        >
      </div>
    </Modal>
    <Modal
      v-model="calcFlowModalVisible"
      width="60"
      title="预览"
      @on-ok="resetFlowCalcResult"
      @on-cancel="resetFlowCalcResult"
    >
      <Row class="attrs" v-for="item in calcFlowResult" :key="item.name">
        <h4>{{ item.name }}</h4>
        <Tag
          v-if="attr.isDisplayed"
          v-for="attr in item.attrs"
          type="dot"
          :color="attr.isHighlight ? 'success' : ''"
          :key="attr.ciTypeAttrId"
          >{{ attr.name }}</Tag
        >
      </Row>
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
  getAllCiTypesByCatalog,
  getAllFlow,
  saveFlow,
  getFlowDetailByID,
  getLatestOnlinePluginInterfaces,
  getFlowPreview
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
      ciTypesObj: {},
      ciTypeAttributeObj: {},
      bpmnModeler: null,
      container: null,
      canvas: null,
      processName: "",
      additionalModules: [propertiesProviderModule, propertiesPanelModule],
      rootFilterRule: {},
      allFlows: [],
      allCITypes: [],
      selectedFlow: {
        value: "",
        label: ""
      },
      selectedCI: {
        value: "",
        label: ""
      },
      selectNodeId: "",
      selectedNodeName: "",
      pluginModalVisible: false,
      pluginForm: {
        rules: {}
      },
      serviceTaskBindInfos: [],
      allNodeTypes: [
        { value: 1, label: "人工处理类" },
        { value: 2, label: "审批类" },
        { value: 3, label: "执行类" }
      ],
      allPlugins: [],
      calcFlowModalVisible: false,
      calcFlowResult: []
    };
  },
  watch: {},
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
      this.getAllCITypes();
      this.getAllPlugins();
    },
    setRootFilterRule(v) {
      this.rootFilterRule = v;
    },
    async getAllCITypesByLayerWithAttr() {
      let { status, data, message } = await getAllCITypesByLayerWithAttr([
        "notCreated",
        "created",
        "dirty",
        "decommissioned"
      ]);
      if (status === "OK") {
        let ciTypes = {};
        let ciTypeAttrs = {};
        data.forEach(layer => {
          if (layer.ciTypes instanceof Array) {
            layer.ciTypes.forEach(citype => {
              ciTypes[citype.ciTypeId] = citype;
              if (citype.attributes instanceof Array) {
                citype.attributes.forEach(citypeAttr => {
                  ciTypeAttrs[citypeAttr.ciTypeAttrId] = citypeAttr;
                });
              }
            });
          }
        });
        this.ciTypesObj = ciTypes;
        this.ciTypeAttributeObj = ciTypeAttrs;
      }
    },
    async getAllPlugins() {
      const { data, status, message } = await getLatestOnlinePluginInterfaces();
      if (status === "OK") {
        this.allPlugins = data;
      }
    },
    async getAllCITypes() {
      const { data, message, status } = await getAllCiTypesByCatalog();
      if (status === "OK") {
        data.forEach(_ => {
          _.ciTypes.filter(i => i.status !== "decommissioned");
        });
        this.allCITypes = data;
      }
    },
    async getAllFlows() {
      const { data, message, status } = await getAllFlow();
      if (status === "OK") {
        this.allFlows = data || [];
      }
    },
    onFlowSelect(v) {
      this.selectedFlow = v;
      v && this.getFlowXml(v.value);
    },

    onCISelect(v) {
      this.selectedCI = v;
      if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = [];
      this.pluginForm = {};
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
      const newFlowID = "wecube" + Date.now();
      const bpmnXmlStr =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn">\n' +
        '  <bpmn2:process id="' +
        newFlowID +
        '" isExecutable="true">\n' +
        "  </bpmn2:process>\n" +
        '  <bpmndi:BPMNDiagram id="BPMNDiagram_1">\n' +
        '    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="' +
        newFlowID +
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
    saveDiagram() {
      let _this = this;
      this.bpmnModeler.saveXML({ format: true }, function(err, xml) {
        if (!xml) return;
        const xmlString = xml.replace(/[\r\n]/g, "");
        const processName = document.getElementById("camunda-name").innerText;
        const payload = {
          processData: xmlString,
          processName: processName,
          rootCiTypeId: _this.selectedCI.value,
          serviceTaskBindInfos: _this.serviceTaskBindInfos
        };
        saveFlow(payload).then(data => {
          if (data && data.status === "OK") {
            _this.$Notice.success({
              title: "Success",
              desc: data.message
            });
            _this.getAllFlows();
          }
        });
      });
    },
    resetFlowCalcResult() {
      this.calcFlowModalVisible = false;
      this.calcFlowResult = [];
    },
    async calcFlow() {
      if (this.serviceTaskBindInfos.length < 1) {
        this.$Notice.warning({
          title: "Warning",
          desc: "无法预览"
        });
      } else {
        this.serviceTaskBindInfos.forEach(_ => {
          delete _.rules;
          /* ********START COMMENT ********/
          // TODO: Back end do not support nodeType for now 20190620
          delete _.nodeType;
          /* ********END COMMENT *********/
        });
        const { data, status, message } = await getFlowPreview(
          this.serviceTaskBindInfos
        );
        if (status === "OK") {
          this.calcFlowResult = data["ci-types"].map(_ => {
            return {
              name: _.name,
              attrs: _.attributes.map(i => {
                return {
                  ...i,
                  isHighlight: data["required-input-parameters"].includes(
                    i.ciTypeAttrId
                  )
                };
              })
            };
          });
          this.calcFlowModalVisible = true;
        }
      }
    },
    savePluginConfig(ref) {
      let index = -1;
      this.serviceTaskBindInfos.forEach((_, i) => {
        if (this.selectNodeId === _.nodeId) {
          index = i;
        }
      });
      if (index > -1) {
        this.serviceTaskBindInfos.splice(index, 1);
      }

      let found = this.allPlugins.find(
        _ => _.serviceName === this.pluginForm.serviceId
      );
      this.serviceTaskBindInfos.push({
        version: 0,
        ...this.pluginForm,
        nodeId: this.selectNodeId,
        nodeName: this.selectedNodeName,
        ciRoutineExp: JSON.stringify(
          this.pluginForm.rules.cmdbColumnCriteria.routine
        ),
        ciRoutineRaw: JSON.stringify(this.pluginForm.rules.cmdbColumnSource),
        serviceName: (found && found.serviceName) || ""
      });
      this.serviceTaskBindInfos.forEach(_ => {
        delete _.rules;
        /* ********START COMMENT ********/
        // TODO: Back end do not support nodeType for now 20190620
        delete _.nodeType;
        /* ********END COMMENT *********/
      });
      this.pluginModalVisible = false;
    },
    openPluginModal() {
      if (!this.selectedCI.value) {
        this.$Notice.warning({
          title: "Warning",
          desc: "请先选择CI类型"
        });
      } else {
        this.pluginModalVisible = true;
        this.pluginForm = this.serviceTaskBindInfos.find(
          _ => _.nodeId === this.selectNodeId
        ) || { rules: this.rootFilterRule };
        this.$nextTick(() => {
          document.querySelector(".attr-ul").style.width =
            document.querySelector(".input_in textarea").clientWidth + "px";
        });
      }
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
          _this.selectNodeId = e.target.parentNode.getAttribute(
            "data-element-id"
          );
          _this.selectedNodeName =
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
        const _this = this;
        this.bpmnModeler.importXML(data.definitionText, function(err) {
          if (err) {
            console.error(err);
          }
          _this.bindRightClick();
          _this.serviceTaskBindInfos = data.serviceTaskBindInfos;
          _this.selectedCI.value = data.rootCiTypeId || "";
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
