<template>
  <div class="">
    <section class="search">
      <Card v-if="displaySearchZone">
        <div class="search-zone">
          <Form :label-width="110">
            <FormItem label="查询操作对象：">
              <a @click="setSearchConditions">定义查询...</a>
            </FormItem>
            <FormItem label="查询路径：">
              <span v-if="dataModelExpression != ':'">{{
                dataModelExpression
              }}</span>
              <span v-else>(无)</span>
            </FormItem>
            <FormItem label="查询条件：">
              <div v-if="searchParameters.length">
                <Row>
                  <Col
                    span="6"
                    v-for="(sp, spIndex) in searchParameters"
                    :key="spIndex"
                    style="padding:0 8px"
                  >
                    <label for="">{{ sp.description }}:</label>
                    <Input v-model="sp.value" />
                  </Col>
                </Row>
              </div>
              <span v-else>(无)</span>
            </FormItem>
          </Form>
        </div>
        <div class="search-btn">
          <Button type="primary" @click="excuteSearch">执行查询</Button>
          <Button @click="clearParametes">清空条件</Button>
          <Button @click="resetParametes">重置查询</Button>
        </div>
      </Card>
      <div v-else>
        <a @click="reExcute('displaySearchZone')"
          >查询 资源实例 中满足以下条件的CI数据对象:</a
        >
        <ul>
          <li v-for="(sp, spIndex) in searchParameters">
            <span
              >{{ sp.packageName }}-{{ sp.entityName }}:[{{ sp.description }}:{{
                sp.value
              }}]</span
            >
          </li>
        </ul>
      </div>
    </section>
    <section
      v-if="!displaySearchZone"
      class="search-result-table"
      style="margin-top:20px;"
    >
      <Button
        type="primary"
        :disabled="!seletedRows.length"
        @click="batchAction"
        >批量操作</Button
      >
      <div class="we-table">
        <WeTable
          v-if="displayResultTableZone"
          :tableData="tableData"
          :tableOuterActions="[]"
          :tableInnerActions="null"
          :tableColumns="tableColumns"
          @getSelectedRows="onSelectedRowsChange"
          ref="table"
        />
        <a v-else @click="reExcute('displayResultTableZone')"
          >找到20个资源实例</a
        >
      </div>
    </section>
    <section
      v-if="!displaySearchZone && !displayResultTableZone"
      style="margin-top:60px;"
    >
      <Card>
        <Row>
          <Col span="6" class="excute-result excute-result-search">
            <Input v-model="businessKey" style="width:180px;" />
            <Button type="primary">搜索</Button>
            <ul style="margin: 8px 0">
              <li
                @click="activeResultKey = key"
                :class="[
                  activeResultKey === key ? 'active-key' : '',
                  'business-key'
                ]"
                v-for="(key, keyIndex) in filterBusinessKeySet"
              >
                <span>{{ key }}</span>
              </li>
            </ul>
          </Col>
          <Col span="17" class="excute-result excute-result-json">
            <Input v-model="resultFilterKey" style="width:300px;" />
            <div>
              <!-- <highlight-code lang="json"><pre>{{ businessKeyContent }}</pre></highlight-code> -->
              <pre> <span v-html="JSON.stringify(businessKeyContent, null, 2)"></span></pre>
              <!-- <p>{{ JSON.stringify(businessKeyContent, null, 2) }}</p> -->
            </div>
          </Col>
        </Row>
      </Card>
    </section>
    <Modal
      :width="700"
      v-model="isShowSearchConditions"
      title="定义操作对象的查询方式"
    >
      <Form :label-width="110">
        <FormItem label="路径起点：">
          <Select
            v-model="selectedEntityType"
            filterable
            @on-change="changeEntityType"
          >
            <OptionGroup
              :label="pluginPackage.packageName"
              v-for="(pluginPackage, index) in allEntityType"
              :key="index"
            >
              <Option
                v-for="item in pluginPackage.pluginPackageEntities"
                :value="item.name"
                :key="item.name"
                :label="item.name"
              ></Option>
            </OptionGroup>
          </Select>
        </FormItem>
        <FormItem label="查询路径：">
          <PathExp
            :rootEntity="selectedEntityType"
            :allDataModelsWithAttrs="allEntityType"
            v-model="dataModelExpression"
          ></PathExp>
        </FormItem>
        <FormItem label="目标类型：">
          <span>{{ currentPackageName }}:{{ currentEntityName }}</span>
        </FormItem>
        <FormItem label="业务主键：">
          <Select v-model="currentEntityAttr">
            <Option
              v-for="entityAttr in currentEntityAttrList"
              :value="entityAttr.id"
              :key="entityAttr.id"
              >{{ entityAttr.name }}</Option
            >
          </Select>
        </FormItem>
        <FormItem label="查询条件：" class="tree-style">
          <Row>
            <Col span="12">
              <Tree
                :data="allEntityAttr"
                @on-check-change="checkChange"
                show-checkbox
                multiple
              >
              </Tree>
            </Col>
            <Col span="12" class="tree-checked">
              <span>
                已选数据：
              </span>
              <ul>
                <li v-for="(tea, teaIndex) in targetEntityAttr" :key="teaIndex">
                  <span
                    >{{ tea.packageName }}-{{ tea.entityName }}:{{
                      tea.name
                    }}</span
                  >
                </li>
              </ul>
            </Col>
          </Row>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="saveSearchCondition">{{
          $t("save")
        }}</Button>
      </div>
    </Modal>

    <Modal v-model="batchActionModalVisible" title="批量操作" width="40">
      <Form label-position="right" :label-width="150">
        <FormItem :label="$t('plugin')">
          <Select filterable clearable v-model="pluginForm.serviceId">
            <Option
              v-for="(item, index) in filteredPlugins"
              :value="item.serviceName"
              :key="index"
              >{{ item.serviceDisplayName }}</Option
            >
          </Select>
        </FormItem>
        <template
          v-for="(item, index) in selectedPluginParams"
          v-if="item.mappingType === 'constant'"
        >
          <FormItem :label="item.name" :key="index">
            <Input v-model="item.bindValue" />
          </FormItem>
        </template>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="excuteBatchAction">{{
          $t("confirm")
        }}</Button>
      </div>
    </Modal>

    <Modal v-model="DelConfig.isDisplay" width="360">
      <p slot="header" style="color:#f60;text-align:center">
        <Icon type="ios-information-circle"></Icon>
        <span>删除确认</span>
      </p>
      <div style="text-align:center">
        <p>
          <span style="color:#2d8cf0"> 后续数据 </span>
          即将被删除
        </p>
      </div>
      <div slot="footer">
        <Button type="error" size="large" long @click="del">删除</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import PathExp from "@/pages/components/path-exp.vue";
import { innerActions } from "@/const/actions.js";
import {
  getAllDataModels,
  retrieveSystemVariables,
  dmeAllEntities,
  dmeIntegratedQuery,
  getFilteredPluginInterfaceList,
  batchExecution
} from "@/api/server.js";
import { formatData } from "../util/format.js";

export default {
  name: "",
  data() {
    return {
      displaySearchZone: true,
      displayResultTableZone: false,
      displayExcuteResultZone: false,

      DelConfig: {
        isDisplay: false,
        displayConfig: {
          name: ""
        },
        key: null
      },

      isShowSearchConditions: false,
      selectedEntityName: "",
      selectedEntityType: "",
      allEntityType: [],

      dataModelExpression: "wecmdb:data_center_design",
      currentEntityName: "",
      currentPackageName: "",
      currentEntityAttr: "",
      currentEntityAttrList: [],
      allEntityAttr: [],
      targetEntityAttr: [],

      searchParameters: [
        // {
        //   id: "wecmdb__2__data_center_design__key_name",
        //   pluginPackageAttribute: null,
        //   name: "key_name",
        //   description: "唯一名称",
        //   dataType: "str",
        //   key: "wecmdbdata_center_design0",
        //   index: 0,
        //   title: "key_name",
        //   entityName: "data_center_design",
        //   packageName: "wecmdb",
        //   nodeKey: 5,
        //   checked: true,
        //   indeterminate: false
        // },
        {
          id: "wecmdb__2__data_center_design__name",
          pluginPackageAttribute: null,
          name: "name",
          description: "名称",
          dataType: "str",
          key: "wecmdbdata_center_design0",
          index: 0,
          title: "name",
          entityName: "data_center_design",
          packageName: "wecmdb",
          nodeKey: 6,
          checked: true,
          indeterminate: false
        }
      ],

      tableData: [],
      innerActions,
      seletedRows: [],
      tableColumns: [
        {
          title: this.$t("table_id"),
          key: "id",
          displaySeqNo: 1
        },
        {
          title: this.$t("table_name"),
          key: "key_name",
          displaySeqNo: 2
        },
        {
          title: this.$t("table_description"),
          key: "description",
          displaySeqNo: 3
        }
      ],

      batchActionModalVisible: false,
      pluginForm: {},
      selectedPluginParams: [],
      allPlugins: [],
      filteredPlugins: [],

      excuteResult: {
        task1: {
          id: 29,
          serviceRequest: null,
          callbackUrl: "/v1/process/instances/callback",
          name: "batch-task-name",
          reporter: null,
          reportTime: "2019-12-26 06:23:39",
          operatorRole: "batch-role-name",
          operator: null,
          operateTime: null,
          inputParameters: null,
          description: "batch-task-name",
          result: null,
          resultMessage: null,
          status: "Pending",
          requestId: "RequestId-1577341419129",
          callbackParameter: null
        },
        task2: {
          id: 30,
          serviceRequest: null,
          callbackUrl: "/v1/process/instances/callback",
          name: "batch-task-name",
          reporter: null,
          reportTime: "2019-12-26 06:23:39",
          operatorRole: "batch-role-name",
          operator: null,
          operateTime: null,
          inputParameters: null,
          description: "batch-task-name",
          result: null,
          resultMessage: null,
          status: "Pending",
          requestId: "RequestId-1577341419168",
          callbackParameter: null
        }
      },
      excuteBusinessKeySet: ["task1", "task2"],
      filterBusinessKeySet: ["task1", "task2"],
      activeResultKey: "",
      businessKey: "",
      resultFilterKey: ""
    };
  },
  mounted() {},
  computed: {
    businessKeyContent: function() {
      // const textww = '2'
      // let res1 = JSON.stringify(this.excuteResult['key2']);
      // console.log(res1)
      // res1 = res1.replace(textww, `<span style=color:red>${textww}</span>`)
      //  console.log(JSON.parse(res1))
      // return JSON.parse(res1);
      console.log(this.activeResultKey);
      return this.excuteResult[this.activeResultKey];
    }
  },
  watch: {
    dataModelExpression: async function(val) {
      if (val === ":") {
        return;
      }
      const params = {
        dataModelExpression: val
      };
      const { data, status, message } = await dmeAllEntities(params);
      if (status === "OK") {
        this.currentEntityName = data.slice(-1)[0].entityName;
        this.currentPackageName = data.slice(-1)[0].packageName;
        this.currentEntityAttrList = data.slice(-1)[0].attributes;

        this.allEntityAttr = [];
        data.forEach((single, index) => {
          const childNode = single.attributes.map(attr => {
            attr.key = single.packageName + single.entityName + index;
            attr.index = index;
            attr.title = attr.name;
            attr.entityName = single.entityName;
            attr.packageName = single.packageName;
            return attr;
          });
          this.allEntityAttr.push({
            title: `${single.packageName}-${single.entityName}`,
            children: childNode
          });
        });
      }
    },
    "pluginForm.serviceId": function(val) {
      this.filteredPlugins.forEach(plugin => {
        if (plugin.serviceDisplayName === val) {
          this.selectedPluginParams = plugin.inputParameters;
        }
      });
      this.selectedPluginParams = this.selectedPluginParams.map(_ => {
        _.bindValue = "";
        return _;
      });
      console.log(this.selectedPluginParams);
    },
    businessKey: function(val) {
      this.filterBusinessKeySet = [];
      for (const key in this.excuteResult) {
        if (key.indexOf(this.businessKey) > -1) {
          this.filterBusinessKeySet.push(key);
        }
      }
    }
  },
  methods: {
    setSearchConditions() {
      this.getAllDataModels();
      this.isShowSearchConditions = true;
      if (document.querySelector(".wecube_attr-ul")) {
        document.querySelector(".wecube_attr-ul").style.width = "730px";
      }

      this.selectedEntityType = "";
      this.allEntityType = [];
      this.dataModelExpression = ":";
      this.currentEntityAttr = "";
      this.currentEntityAttrList = [];
      this.currentPackageName = "";
      this.currentEntityName = "";
      this.allEntityAttr = [];
      this.targetEntityAttr = [];
    },
    changeEntityType() {
      this.targetEntityAttr = [];
    },
    async getAllDataModels() {
      const { data, status, message } = await getAllDataModels();
      if (status === "OK") {
        this.allEntityType = data.map(_ => {
          // handle result sort by name
          return {
            ..._,
            pluginPackageEntities: _.pluginPackageEntities.sort(function(a, b) {
              var s = a.name.toLowerCase();
              var t = b.name.toLowerCase();
              if (s < t) return -1;
              if (s > t) return 1;
            })
          };
        });
      }
    },
    checkChange(totalChecked) {
      this.targetEntityAttr = totalChecked;
    },
    saveSearchCondition() {
      if (!this.currentEntityAttr) {
        this.$Message.warning("业务主键不能为空！");
        return;
      }
      if (this.targetEntityAttr == false) {
        this.$Message.warning("查询条件不能为空！");
        return;
      }
      this.isShowSearchConditions = false;
      this.searchParameters = this.targetEntityAttr;
      console.log(JSON.stringify(this.searchParameters));
    },

    async excuteSearch() {
      const requestParameter = {
        dataModelExpression: this.dataModelExpression,
        filters: []
      };
      let keySet = [];
      this.searchParameters.forEach(sParameter => {
        const index = keySet.indexOf(sParameter.key);
        if (index > -1) {
          const { name, value } = sParameter;
          console.log(value);
          requestParameter.filters[index].attributeFilters.push({
            name,
            value,
            operator: "eq"
          });
        } else {
          keySet.push(sParameter.key);
          const { index, packageName, entityName, name, value } = sParameter;
          console.log(value);
          requestParameter.filters.push({
            index,
            packageName,
            entityName,
            attributeFilters: [
              {
                name,
                value,
                operator: "eq"
              }
            ]
          });
        }
      });
      let { status, data, message } = await dmeIntegratedQuery(
        requestParameter
      );
      if (status === "OK") {
        if (data.length) {
          this.tableData = data;
          this.displaySearchZone = false;
          this.displayResultTableZone = true;
        } else {
          this.$Message.warning("空数据！");
        }
      }
    },
    clearParametes() {
      this.searchParameters.forEach(item => {
        item.value = "";
      });
    },
    resetParametes() {
      this.dataModelExpression = ":";
      this.searchParameters = [];
    },
    reExcute(key) {
      this.DelConfig.isDisplay = true;
      this.DelConfig.key = key;
    },
    del() {
      this.DelConfig.isDisplay = false;

      this.displaySearchZone = false;
      this.displayResultTableZone = false;
      this.displayExcuteResultZone = false;
      this[this.DelConfig.key] = true;
    },
    onSelectedRowsChange(rows, checkoutBoxdisable) {
      this.seletedRows = rows;
    },
    batchAction() {
      this.getFilteredPluginInterfaceList();
      this.batchActionModalVisible = true;
    },
    async getFilteredPluginInterfaceList() {
      const { status, message, data } = await getFilteredPluginInterfaceList(
        this.searchParameters.slice(-1)[0].packageName,
        this.searchParameters.slice(-1)[0].entityName
        // "wecmdb",
        // "resource_instance"
      );
      if (status === "OK") {
        this.filteredPlugins = data;
      }
    },
    async excuteBatchAction() {
      const plugin = this.filteredPlugins.find(_ => {
        return _.serviceName === this.pluginForm.serviceId;
      });
      const inputParameterDefinitions = plugin.inputParameters.map(p => {
        const inputParameterValue =
          p.mappingType === "constant"
            ? p.dataType === "number"
              ? Number(p.bindValue)
              : p.bindValue
            : null;
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        };
      });
      let currentEntity = this.currentEntityAttrList.find(_ => {
        return _.id === this.currentEntityAttr;
      });
      let requestBody = {
        packageName: this.currentPackageName,
        entityName: this.currentEntityName,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas: [
          {
            businessKeyValue: "task1",
            id: "1"
          },
          {
            businessKeyValue: "task2",
            id: "2"
          }
        ]
      };
      //   const requestBody = {
      // "packageName": "service-mgmt",
      // "entityName": "task",
      // "pluginConfigInterface": {
      //     "id": "service-mgmt__v1.8.7.1__task__create",
      //     "pluginConfigId": "service-mgmt__v1.8.7.1__task",
      //     "action": "create",
      //     "serviceName": "service-mgmt/task(task)/create",
      //     "serviceDisplayName": "service-mgmt/task(task)/create",
      //     "path": "/service-mgmt/v1/tasks",
      //     "httpMethod": "POST",
      //     "isAsyncProcessing": "Y",
      //     "inputParameters": [
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__callbackUrl",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "callbackUrl",
      //             "dataType": "string",
      //             "mappingType": "system_variable",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": "CALLBACK_URL",
      //             "required": "Y"
      //         },
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__roleName",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "roleName",
      //             "dataType": "string",
      //             "mappingType": "constant",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "Y"
      //         },
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__taskName",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "taskName",
      //             "dataType": "string",
      //             "mappingType": "constant",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "Y"
      //         }
      //     ],
      //     "outputParameters": [
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__OUTPUT__comment",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "OUTPUT",
      //             "name": "comment",
      //             "dataType": "string",
      //             "mappingType": "context",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "N"
      //         },
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__OUTPUT__errorCode",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "OUTPUT",
      //             "name": "errorCode",
      //             "dataType": "string",
      //             "mappingType": "context",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "N"
      //         },
      //         {
      //             "id": "service-mgmt__v1.8.7.1__task__create__OUTPUT__errorMessage",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "OUTPUT",
      //             "name": "errorMessage",
      //             "dataType": "string",
      //             "mappingType": "context",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "N"
      //         }
      //     ]
      // },
      // "inputParameterDefinitions": [
      //     {
      //         "inputParameter": {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__callbackUrl",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "callbackUrl",
      //             "dataType": "string",
      //             "mappingType": "system_variable",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": "CALLBACK_URL",
      //             "required": "Y"
      //         },
      //         "inputParameterValue": null
      //     },
      //     {
      //         "inputParameter": {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__roleName",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "roleName",
      //             "dataType": "string",
      //             "mappingType": "constant",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "Y"
      //         },
      //         "inputParameterValue": "batch-role-name"
      //     },
      //     {
      //         "inputParameter": {
      //             "id": "service-mgmt__v1.8.7.1__task__create__INPUT__taskName",
      //             "pluginConfigInterfaceId": "service-mgmt__v1.8.7.1__task__create",
      //             "type": "INPUT",
      //             "name": "taskName",
      //             "dataType": "string",
      //             "mappingType": "constant",
      //             "mappingEntityExpression": null,
      //             "mappingSystemVariableName": null,
      //             "required": "Y"
      //         },
      //         "inputParameterValue": "batch-task-name"
      //     }
      // ],
      // "businessKeyAttribute": {
      //     "id": "service-mgmt__1__task__name",
      //     "pluginPackageAttribute": null,
      //     "name": "name",
      //     "description": "任务名称",
      //     "dataType": "str"
      // },
      // "resourceDatas": [
      //     {
      //         "businessKeyValue": "task1",
      //         "id": "1"
      //     },
      //     {
      //         "businessKeyValue": "task2",
      //         "id": "2"
      //     }
      // ]
      console.log(requestBody);
      const { status, data, message } = await batchExecution(requestBody);
      if (status === "OK") {
        this.excuteResult = data;
        for (const key in data) {
          this.excuteBusinessKeySet.push(key);
        }
        this.filterBusinessKeySet = this.excuteBusinessKeySet;
        this.batchActionModalVisible = false;
        this.displayResultTableZone = false;
        this.displayExcuteResultZone = false;
      }
    }
  },
  components: {
    PathExp
  }
};
</script>

<style lang="scss" scope>
.ivu-tree-children li {
  margin: 0 !important;
  .ivu-checkbox-wrapper {
    margin: 0 !important;
  }
}
.ivu-form-item {
  margin-bottom: 0 !important;
}
.tree-checked {
  border-left: 2px solid gray;
  padding-left: 8px;
}
.search-btn {
  margin-top: 16px;
}
.we-table /deep/ .ivu-form-label-top {
  display: none;
}
.excute-result {
  padding: 8px;
  min-height: 300px;
}
.excute-result-search {
  margin-right: 16px;
  border-right: 1px solid #e8eaec;
}
.excute-result-json {
  border: 1px solid #e8eaec;
}
.business-key {
  padding: 0 4px;
  cursor: pointer;
  color: #2d8cf0;
}
.active-key {
  color: red;
}
</style>
