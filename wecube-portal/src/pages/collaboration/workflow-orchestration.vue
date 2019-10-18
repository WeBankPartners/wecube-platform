<template>
  <div>
    <Row style="margin-bottom: 10px">
      <Col span="6">
        <span style="margin-right: 10px">{{ $t("flow_name") }}</span>
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
        <span style="margin-right: 10px">{{ $t("ci_type") }}</span>
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
      <Button type="info" @click="saveDiagram">{{ $t("save_flow") }}</Button>
      <Button type="success" @click="calcFlow">{{ $t("calc_form") }}</Button>
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
        <FormItem
          :label="$t('node_type')"
          prop="nodeType"
          style="display: none"
        >
          <Select filterable clearable v-model="pluginForm.nodeType">
            <Option
              v-for="item in allNodeTypes"
              :value="item.value"
              :key="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('locate_rules')" prop="rules">
          <div style="width: 100%">
            <AttrInput
              :allCiTypes="allCITypes"
              :cmdbColumnSource="pluginForm.ciRoutineRaw"
              :rootCiType="selectedCI.value"
              v-model="pluginForm.rules"
              :ciTypesObj="this.ciTypesObj"
              :ciTypeAttributeObj="this.ciTypeAttributeObj"
              @change="setRootFilterRule"
              :isEndWithCIType="true"
            />
          </div>
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
      :title="$t('preview')"
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
        rules: {},
        timeoutExpression: "30" // 默认超时时间30分钟
      },
      serviceTaskBindInfos: [],
      allNodeTypes: [
        { value: 1, label: "人工处理类" },
        { value: 2, label: "审批类" },
        { value: 3, label: "执行类" }
      ],
      allPlugins: [],
      calcFlowModalVisible: false,
      calcFlowResult: [],
      timeSelection: ["5", "10", "20", "30", "60"]
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
      this.getAllPlugins();
    },
    setRootFilterRule(v) {
      this.rootFilterRule = v;
      this.getAllPlugins();
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

        let tempCITypes = JSON.parse(JSON.stringify(data));
        tempCITypes.forEach(_ => {
          _.ciTypes && _.ciTypes.filter(i => i.status !== "decommissioned");
        });
        this.allCITypes = tempCITypes;

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
      let routine =
        this.pluginForm.rules.cmdbColumnCriteria &&
        this.pluginForm.rules.cmdbColumnCriteria.routine;
      let ciTypeId = routine && routine[routine.length - 1].ciTypeId;
      const { data, status, message } = !!routine
        ? await getLatestOnlinePluginInterfaces(ciTypeId)
        : await getLatestOnlinePluginInterfaces();
      if (status === "OK") {
        this.allPlugins = data;
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
      this.pluginForm = {
        rules: {},
        timeoutExpression: "30"
      };
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
          desc: this.$t("cannot_preview")
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

      let pluginFormCopy = JSON.parse(JSON.stringify(this.pluginForm));
      this.serviceTaskBindInfos.push({
        version: 0,
        ...pluginFormCopy,
        nodeId: this.selectNodeId,
        nodeName: this.selectedNodeName,
        ciRoutineExp: JSON.stringify(
          pluginFormCopy.rules.cmdbColumnCriteria.routine
        ),
        ciRoutineRaw: JSON.stringify(pluginFormCopy.rules.cmdbColumnSource),
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
          desc: this.$t("select_ci_first")
        });
      } else {
        this.pluginModalVisible = true;
        this.pluginForm = this.serviceTaskBindInfos.find(
          _ => _.nodeId === this.selectNodeId
        ) || { rules: this.rootFilterRule, timeoutExpression: "30" };
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
