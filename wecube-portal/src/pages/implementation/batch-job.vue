<template>
  <div>
    <Row>
      <Col span="4" class="select-col">
        <span class="select-span">执行目标类型</span>
        <Select
          v-model="selectedTargetCI"
          filterable
          class="select"
          @on-change="onTargetAttrbIdChange"
        >
          <Option
            v-for="item in allCiTypes"
            :value="item.ciTypeId"
            :key="item.ciTypeId"
            >{{ item.name }}</Option
          >
        </Select>
      </Col>
      <Col span="4" offset="1" class="select-col">
        <span class="select-span">执行目标定位属性</span>
        <Select
          v-model="selectedTargetName"
          filterable
          class="select"
          :disabled="!selectedTargetCI"
          @on-change="selectTargetAttrbuteId"
        >
          <Option
            v-for="item in queryTargetAttr"
            :key="item.ciTypeAttrId"
            :value="item.ciTypeAttrId"
            >{{ item.name }}</Option
          >
        </Select>
      </Col>
      <Col span="4" offset="1" class="select-col">
        <span class="select-span">插件选择</span>
        <Select
          v-model="selectedPlugin"
          filterable
          class="select"
          :disabled="!selectedTargetCI"
        >
          <Option
            v-for="item in plugins"
            :key="item.serviceName"
            :value="item.serviceName"
            >{{ item.serviceDisplayName }}</Option
          >
        </Select>
      </Col>
      <Col span="4" offset="1" class="select-col">
        <span class="select-span">综合查询根CI</span>
        <Select
          v-model="selectedCI"
          filterable
          class="select"
          :disabled="!selectedTargetName"
          @on-change="onCITypeChange"
        >
          <Option
            v-for="item in allCiTypes"
            :value="item.ciTypeId"
            :key="item.ciTypeId"
            >{{ item.name }}</Option
          >
        </Select>
      </Col>
      <Col span="4" offset="1" class="select-col">
        <span class="select-span">综合查询名称</span>
        <Select
          v-model="selectedQueryName"
          filterable
          class="select"
          :disabled="!selectedCI"
          @on-change="onQueryNameSelectChange"
        >
          <Option
            v-for="item in queryNameList"
            :value="item.id"
            :key="item.id"
            >{{ item.name }}</Option
          >
        </Select>
      </Col>
    </Row>
    <Row style="margin-top:10px">
      <hr />
      <Alert v-if="warningMessage" type="warning" class="alert">{{
        warningMessage
      }}</Alert>
    </Row>
    <Row v-if="showTable" style="margin-top: 20px;">
      <WeTable
        :tableData="tableData"
        :tableInnerActions="null"
        :tableColumns="tableColumns"
        :pagination="pagination"
        :showCheckbox="true"
        @getSelectedRows="getSelectedRows"
        @actionFun="actionFun"
        @pageChange="pageChange"
        @pageSizeChange="pageSizeChange"
        @handleSubmit="handleSubmit"
        tableHeight="650"
        ref="table"
      ></WeTable>

      <Modal v-model="originDataModal" title="原始数据" footer-hide>
        <highlight-code lang="json">{{ showRowOriginData }}</highlight-code>
      </Modal>

      <Modal
        v-model="filtersAndResultModal"
        title="报文"
        footer-hide
        width="75"
      >
        <Row
          >请求URL:
          <highlight-code lang="json">{{ requestURL }}</highlight-code>
        </Row>
        <Row>
          <Col span="11"
            >Payload:
            <highlight-code lang="json">{{ payload }}</highlight-code>
          </Col>
          <Col span="12" offset="1"
            >Result:
            <highlight-code lang="json">{{ tableData }}</highlight-code>
          </Col>
        </Row>
      </Modal>
    </Row>
    <div v-if="isHostsSelected">
      <hr />
      <Row>
        <span
          style="margin-right:30px;display:block;margin-top:30px;width:100px;text-align: center"
          >操作管理</span
        >
      </Row>
      <Row>
        <Col span="2">
          <span
            style="margin-right:30px;display:block;margin-top:30px;width:100px;text-align: center"
            >执行命令</span
          >
          <label style="position:relative">
            <input
              type="button"
              style="margin-right:30px;margin-top:95px;width:100px"
              value="选择文件"
            />
            <input
              type="file"
              style="opacity:0;position:absolute;left:0;top:0"
              accept="accept"
              multiple="false"
              @change="loadShellFile"
            />
          </label>
        </Col>
        <Col span="16">
          <Input
            v-model="shellContent"
            ref="textarea"
            type="textarea"
            :rows="6"
            style="margin-top:30px;width:100%"
            placeholder="请输入..."
          ></Input>
        </Col>
        <Col span="5" offset="1">
          <div style="display:flex;float:right;width:100%">
            <Button
              v-show="false"
              style="margin-top:50px;width:100px;height:80px"
              type="primary"
              >高危检测</Button
            >
            <div>
              <Button
                v-show="false"
                style="margin-left:30px;width:100px;height:30px;display:block;margin-top:50px"
                type="primary"
                >提交审批</Button
              >
              <Button
                style="margin-left:10px;width:100px;height:30px;display:block;margin-top:18px;margin-top:135px"
                @click="executionShell"
                type="primary"
                >执行</Button
              >
            </div>
          </div>
        </Col>
      </Row>
    </div>

    <Row v-if="showLogResult" style="margin-top: 20px;">
      <div>
        <WeTable
          :tableData="logTableData"
          :pagination="logpagination"
          :tableInnerActions="logInnerActions"
          :tableColumns="logTableColumns"
          :showCheckbox="false"
          tableHeight="650"
          @actionFun="logActionFun"
          @handleSubmit="logSearchFunc"
          @pageChange="logpageChange"
          @pageSizeChange="logpageSizeChange"
        ></WeTable>

        <Modal
          v-model="logDataDetailModal"
          title="脚本执行结果详情"
          footer-hide
        >
          <highlight-code lang="json">{{ logDetail }}</highlight-code>
        </Modal>
      </div>
    </Row>
  </div>
</template>

<script>
import {
  getAllCITypes,
  queryIntHeader,
  excuteIntQuery,
  getCiTypeAttr,
  getLatestOnlinePluginInterfaces,
  getQueryNamesByAttrId,
  createBatchJob,
  execBatchJob,
  getBatchJobExecLog,
  getBatchJobExecLogDetail,
  getEnumCodesByCategoryId
} from "@/api/server";
import { components } from "../../const/actions.js";

const logInnerActions = [
  {
    label: "显示详情",
    props: {
      type: "info",
      size: "small"
    },
    actionType: "showLogDetail"
  }
];

export default {
  components: {},
  data() {
    return {
      warningMessage: "",
      shellContent: "",
      selectedHostIps: [],
      showShellExecutetTable: false,
      currentBatchJobId: -1,
      showLogResult: false,
      logTableData: [],
      logInnerActions,
      seachLogFilters: [],
      logDataDetailModal: false,
      logDetail: "",
      logTableColumns: [
        {
          title: "对象",
          key: "instance",
          inputKey: "instance",
          searchSeqNo: 1,
          displaySeqNo: 1,
          component: "WeSelect",
          isMultiple: true,
          placeholder: "对象",
          span: 5,
          width: "200px",
          options: []
        },
        {
          title: "行号",
          key: "line_number",
          inputKey: "line_number",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          isNotFilterable: true,
          placeholder: "行号",
          width: "100px"
        },
        {
          title: "匹配内容",
          key: "log",
          inputKey: "log",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "Input",
          placeholder: "支持正则表达式"
        }
      ],

      isHostsSelected: false,
      formItem: "",
      selectedCI: "",
      selectedTargetCI: "",
      queryTargetAttr: [],
      showTable: false,
      plugins: [],
      selectedPlugin: "",
      targetAttrbuteId: "",
      allCiTypes: [],
      selectedTargetName: "",
      selectedQueryName: "",
      queryNameList: [],
      tableData: [],
      tableColumns: [],
      pagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      logpagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      payload: {
        filters: [],
        pageable: {
          pageSize: 10,
          startIndex: 0
        },
        paging: true
      },
      originDataModal: false,
      showRowOriginData: "",
      filtersAndResultModal: false,
      showfiltersAndResultModalData: "",
      requestURL: ""
    };
  },
  created() {
    this.getAllCITypes();
  },
  methods: {
    onTargetAttrbIdChange(value) {
      this.getAttrbuteId(value);
      this.getLatestOnlinePluginInterfaces(value);
    },
    selectTargetAttrbuteId(value) {
      this.targetAttrbuteId = value;
      if (this.selectedCI) {
        this.getQueryNameList(this.selectedCI);
      }
    },

    loadShellFile(evt) {
      const reader = new FileReader();
      const _this = this;
      reader.onload = function(e) {
        _this.shellContent = e.target.result;
      };
      reader.readAsText(evt.target.files[0]);
    },
    async executionShell() {
      const payload = {
        creator: "test",
        hosts: this.selectedHostIps,
        scriptContent: this.shellContent
      };

      if (this.shellContent === "") {
        this.$Notice.warning({
          title: "Create Batch Job Failed",
          desc: "shell content is empty"
        });
        return;
      }

      let createResp = await createBatchJob(payload);
      if (createResp.status !== "OK") {
        return;
      }
      //exec batch
      this.currentBatchJobId = createResp.data.batchJobId;
      let { data, status, message } = await execBatchJob(
        this.currentBatchJobId
      );
      if (status === "OK") {
        this.showLogResult = true;
        this.getHostsForLogTableFilter();
        this.$Notice.success({
          title: "Exec Batch Job Success",
          desc: message
        });
      }
    },
    pageChange(current) {
      this.pagination.currentPage = current;
      this.payload.pageable.startIndex =
        (current - 1) * this.pagination.pageSize;
      this.getTableData();
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size;
      this.payload.pageable.startIndex =
        (current - 1) * this.pagination.pageSize;
      this.getTableData();
    },
    logpageChange(current) {
      this.logpagination.currentPage = current;
      this.getLogTableData();
    },
    logpageSizeChange(size) {
      this.logpagination.pageSize = size;
      this.getLogTableData();
    },
    async getAllCITypes() {
      let { status, data, message } = await getAllCITypes();
      if (status === "OK") {
        this.allCiTypes = data.filter(
          _ => _.status === "created" || _.status === "dirty"
        );
      }
    },
    onCITypeChange(value) {
      this.getQueryNameList(value);
    },
    onQueryNameSelectChange(value) {
      if (value) {
        this.warningMessage = "";
        this.getTableHeader(value);
        this.requestURL = `/cmdb/intQuery/${value}/execute`;
      } else {
        this.showTable = false;
      }
    },
    async getTableHeader(id) {
      this.currentSelectQueryNameId = id;
      let { status, data, message } = await queryIntHeader(id);

      if (status === "OK") {
        this.tableColumns = [];
        data.forEach(_ => {
          if (_.attrUnits) {
            let children = _.attrUnits
              .map(child => {
                return {
                  title: child.attr.name,
                  parentTitle: _.ciTypeName,
                  key: child.attrKey,
                  inputKey: child.attrKey,
                  type: "text",
                  ...components[child.attr.inputType],
                  placeholder: child.attr.name,
                  ...child.attr,
                  ciType: { id: child.attr.referenceId, name: "" }
                };
              })
              .filter(i => i.isDisplayed);
            this.tableColumns.push({
              title: _.ciTypeName,
              align: "center",
              children
            });
          }
        });
        if (this.checkSelectedQuery(data)) {
          this.getTableData();
          this.$nextTick(() => {
            this.getColumnOptions();
          });
        }
      }
    },
    checkSelectedQuery(data) {
      let attrs = [];
      data.forEach(ciType => {
        if (ciType.attrUnits) {
          ciType.attrUnits.forEach(_ => {
            attrs.push(_.attr.ciTypeAttrId);
          });
        }
      });
      if (attrs.indexOf(this.selectedTargetName) === -1) {
        this.showTable = false;
        const selectedQueryFound = this.queryNameList.find(
          _ => _.id === this.selectedQueryName
        );
        const selectQueryName = selectedQueryFound
          ? selectedQueryFound.name
          : "";
        const targetFound = this.queryTargetAttr.find(
          _ => _.ciTypeAttrId === this.selectedTargetName
        );
        const targetName = targetFound ? targetFound.name : "";
        this.warningMessage = `该综合查询（${selectQueryName}）不包含执行目标定位属性（${targetName}）`;
        return false;
      } else {
        this.showTable = true;
        return true;
      }
    },
    getColumnOptions() {
      this.tableColumns.forEach(_ => {
        if (_.children) {
          _.children.forEach(async child => {
            if (child.inputType === "select") {
              const { data, status, message } = await getEnumCodesByCategoryId(
                0,
                child.referenceId
              );
              let opts = [];
              if (status === "OK") {
                opts = data.map(_ => {
                  return {
                    value: _.codeId,
                    label: _.value
                  };
                });
              }
              this.$set(child, "options", opts);
            }
          });
        }
      });
    },
    async getTableData() {
      let { status, data, message } = await excuteIntQuery(
        this.currentSelectQueryNameId,
        this.payload
      );
      if (status === "OK") {
        this.tableData = data.contents;
        this.pagination.total = data.pageInfo.totalRows;
        this.selectedHostIps = data.contents.map(
          item =>
            item[
              this.tableColumns[this.tableColumns.length - 1].children[
                this.tableColumns[this.tableColumns.length - 1].children
                  .length - 1
              ].inputKey
            ]
        );
        this.selectedHostIps = [...new Set(this.selectedHostIps)];
      }
    },
    getHostsForLogTableFilter() {
      this.logTableColumns[0].options = this.selectedHostIps.map(_ => {
        return {
          label: _,
          value: _
        };
      });
    },
    reset() {
      this.queryNameList = [];
      this.selectedQueryName = "";
      this.isHostsSelected = false;
      this.showLogResult = false;
      this.shellContent = "";
    },
    async getAttrbuteId(ciTypeId) {
      this.selectedHostIps = [];
      let { status, data, message } = await getCiTypeAttr(ciTypeId);
      if (status === "OK") {
        this.queryTargetAttr = data;
      }
    },
    async getLatestOnlinePluginInterfaces(ciTypeId) {
      this.plugins = [];
      const { status, data, message } = await getLatestOnlinePluginInterfaces(
        ciTypeId
      );
      if (status === "OK") {
        this.plugins = data;
      }
    },
    async getQueryNameList(ciTypeId) {
      this.reset();
      let { status, data, message } = await getQueryNamesByAttrId(
        ciTypeId,
        this.targetAttrbuteId
      );
      if (status === "OK") {
        this.queryNameList = data;
      }
    },
    logSearchFunc(data) {
      this.seachLogFilters = data;
      this.getLogTableData();
    },
    handleSubmit(data) {
      this.payload.filters = data;
      this.getTableData();
    },
    getSelectedRows(data) {
      this.selectedHostIps = data.map(
        _ =>
          _[
            this.tableColumns[this.tableColumns.length - 1].children[
              this.tableColumns[this.tableColumns.length - 1].children.length -
                1
            ].inputKey
          ]
      );
      this.isHostsSelected = this.selectedHostIps.length > 0;
    },
    actionFun(type, data) {
      if (type === "showOriginData") {
        this.showRowOriginData = data.weTableForm;
        this.originDataModal = true;
      }
      if (type === "showFiltersAndResult") {
        this.filtersAndResultModal = true;
      }
    },
    logActionFun(type, data) {
      if (type === "showLogDetail") {
        this.getLogDetail(data);
      }
    },
    handleLogPaginationByFE() {
      this.logpagination.total = this.logTableData.length;
      let temp = Array.from(this.logTableData);
      this.logTableData = temp.splice(
        (this.logpagination.currentPage - 1) * this.logpagination.pageSize,
        this.logpagination.pageSize
      );
    },
    async getLogDetail(inputData) {
      const payload = {
        batchId: this.currentBatchJobId,
        lineNumber: inputData.line_number,
        host: inputData.instance,
        offSet: 10
      };
      let { data, status, message } = await getBatchJobExecLogDetail(payload);
      if (status !== "OK") {
        return;
      }

      this.logDataDetailModal = true;
      this.logDetail = data.outputs[0].context;
    },
    async getLogTableData() {
      let errorFlag = false;
      let ipArray = [];
      this.seachLogFilters.forEach(_ => {
        if (_.name === "instance") {
          ipArray = _.value;
        }
      });
      let command = "";
      this.seachLogFilters.forEach(_ => {
        if (_.name === "log") {
          command = _.value;
        }
      });

      if (ipArray.length === 0) {
        this.$Notice.warning({
          title: "Warning",
          desc: "请选择实例"
        });
        errorFlag = true;
      }
      if (command === "") {
        this.$Notice.warning({
          title: "Warning",
          desc: "请输入搜索关键字"
        });
        errorFlag = true;
      }
      if (errorFlag) return;
      const payload = {
        batchId: this.currentBatchJobId,
        hosts: ipArray,
        pattern: command
      };
      let { status, data, message } = await getBatchJobExecLog(payload);
      if (status === "OK") {
        this.logTableData = [];
        let arr = [];
        for (let i in data.outputs) {
          this.logTableData = arr.concat(
            data.outputs[i].result.map(_ => {
              return {
                instance: data.outputs[i].host,
                line_number: _.lineNum,
                log: _.lineText
              };
            })
          );
        }
        this.handleLogPaginationByFE();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.select-col {
  align-items: center;
  display: flex;
  justify-content: space-between;
}
.select-span {
  margin-right: 10px;
}
.select {
  flex: 1;
}
.alert {
  margin-top: 5px;
}
</style>
