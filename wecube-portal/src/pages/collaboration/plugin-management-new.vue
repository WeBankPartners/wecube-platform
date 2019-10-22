<template>
  <Row style="padding:20px">
    <Col span="6">
      <Row>
        <Card>
          <p slot="title">{{ $t("upload_plugin_pkg_title") }}</p>
          <Upload
            show-upload-list
            accept=".zip"
            name="zip-file"
            :on-success="onSuccess"
            :on-error="onError"
            action="/plugin/upload"
            :headers="setUploadActionHeader"
          >
            <Button icon="ios-cloud-upload-outline">{{
              $t("upload_plugin_btn")
            }}</Button>
          </Upload>
        </Card>
      </Row>
      <Row class="plugins-tree-container" style="margin-top: 20px">
        <Card dis-hover>
          <p slot="title">{{ $t("plugins_list") }}</p>
          <div style="height: 70%; overflow: auto">
            <Collapse accordion @on-change="pluginPackageChangeHandler">
              <Panel
                :name="plugin.id + ''"
                v-for="plugin in plugins"
                :key="plugin.id"
              >
                {{ plugin.name + "_" + plugin.version }}
                <span style="float: right; margin-right: 10px">
                  <Tooltip content="删除插件" placement="top-start">
                    <Button
                      @click.stop.prevent="deletePlugin(plugin.id)"
                      size="small"
                      icon="ios-trash"
                    ></Button>
                  </Tooltip>
                </span>
                <p slot="content">
                  <Button
                    @click="configPlugin(plugin.id)"
                    size="small"
                    icon="ios-construct"
                    >注册配置</Button
                  >
                  <Button
                    @click="manageRuntimePlugin(plugin.id)"
                    size="small"
                    icon="ios-settings"
                    >运行管理</Button
                  >
                </p>
              </Panel>
            </Collapse>
          </div>
        </Card>
      </Row>
    </Col>
    <Col span="17" offset="1" v-if="isShowConfigPanel">
      <Tabs type="card" :value="currentTab" @on-click="handleTabClick">
        <TabPane name="dependency" label="依赖分析">
          <DependencyAnalysis
            v-if="currentTab === 'dependency'"
            :pkgId="currentPackageId"
          ></DependencyAnalysis>
        </TabPane>
        <TabPane name="menus" label="菜单注入">
          <div>菜单注入</div>
        </TabPane>
        <TabPane name="models" label="数据模型">
          <DataModel
            v-if="currentTab === 'models'"
            :pkgId="currentPackageId"
          ></DataModel>
        </TabPane>
        <TabPane name="systemParameters" label="系统参数">
          <div>系统参数</div>
        </TabPane>
        <TabPane name="authorities" label="权限设定">
          <div>权限设定</div>
        </TabPane>
        <TabPane name="runtimeResources" label="运行资源">
          <div>运行资源</div>
        </TabPane>
        <TabPane name="plugins" label="插件注册">
          <PluginRegister
            v-if="currentTab === 'plugins'"
            :pkgId="currentPackageId"
          ></PluginRegister>
        </TabPane>
      </Tabs>
    </Col>
    <Col span="17" offset="1" v-if="isShowRuntimeManagementPanel">
      <div v-if="Object.keys(currentPlugin).length > 0">
        <div v-if="currentPlugin.children">
          <Row class="instances-container">
            <Collapse value="1">
              <Panel name="1">
                <span style="font-size: 14px; font-weight: 600">运行容器</span>
                <p slot="content">
                  <Card dis-hover>
                    <Row>
                      <Select
                        placeholder="请选择实例"
                        @on-change="selectHost"
                        multiple
                        style="width: 40%"
                        :max-tag-count="4"
                        v-model="selectHosts"
                      >
                        <Option
                          v-for="item in allAvailiableHosts"
                          :value="item"
                          :key="item"
                          >{{ item }}</Option
                        >
                      </Select>
                      <Button
                        size="small"
                        type="success"
                        @click="getAvailablePortByHostIp"
                        >端口预览</Button
                      >
                      <div v-if="availiableHostsWithPort.length > 0">
                        <p style="margin-top: 20px">可用端口:</p>

                        <div
                          v-for="item in availiableHostsWithPort"
                          :key="item.ip + item.port"
                        >
                          <div
                            class="instance-item-container"
                            style="border-bottom: 1px solid gray; padding: 10px 0"
                          >
                            <div class="instance-item">
                              {{ item.ip + ":" + item.port }}
                            </div>
                            <span>启动参数:</span>
                            <Input
                              type="textarea"
                              style="width: 50%"
                              :autosize="true"
                              v-model="item.createParams"
                            />
                            <Button
                              size="small"
                              type="success"
                              @click="
                                createPluginInstanceByPackageIdAndHostIp(
                                  item.ip,
                                  item.port,
                                  item.createParams
                                )
                              "
                              >{{ $t("create") }}</Button
                            >
                          </div>
                        </div>
                      </div>
                    </Row>
                    <Row>
                      <p style="margin-top: 20px">{{ $t("running_node") }}:</p>
                      <div v-if="allInstances.length === 0">暂无运行节点</div>
                      <div v-else>
                        <div v-for="item in allInstances" :key="item.id">
                          <div class="instance-item-container">
                            <div class="instance-item">
                              {{ item.displayLabel }}
                            </div>

                            <Button
                              size="small"
                              type="error"
                              @click="removePluginInstance(item.id)"
                              >{{ $t("ternmiante") }}</Button
                            >
                          </div>
                        </div>
                      </div>
                    </Row>
                  </Card>

                  <Card style="margin-top: 20px">
                    <p>插件日志查询</p>
                    <div style="padding: 0 0 50px 0;margin-top: 20px">
                      <WeTable
                        :tableData="tableData"
                        :tableInnerActions="innerActions"
                        :tableColumns="logTableColumns"
                        :pagination="pagination"
                        @actionFun="actionFun"
                        @handleSubmit="handleSubmit"
                        @pageChange="pageChange"
                        @pageSizeChange="pageSizeChange"
                        :showCheckbox="false"
                        tableHeight="650"
                        ref="table"
                      ></WeTable>
                    </div>

                    <Modal
                      v-model="logDetailsModalVisible"
                      title="日志详情"
                      footer-hide
                      width="70"
                    >
                      <div
                        lang="json"
                        style="white-space: pre-wrap;"
                        v-html="logDetails"
                      ></div>
                    </Modal>
                  </Card>
                </p>
              </Panel>
              <Panel name="2">
                <span style="font-size: 14px; font-weight: 600">数据库</span>
                <Row slot="content">
                  <Row>
                    <Col span="16">
                      <Input
                        v-model="dbQueryCommandString"
                        type="textarea"
                        placeholder="显示使用的数据库,限定只能执行select"
                      />
                    </Col>
                    <Col span="4" offset="1">
                      <Button @click="queryDBHandler">执行</Button>
                    </Col>
                  </Row>
                  <Row>
                    查询结果
                    <Table
                      :columns="dbQueryColumns"
                      :data="dbQueryData"
                    ></Table>
                  </Row>
                </Row>
              </Panel>
              <Panel name="3">
                <span style="font-size: 14px; font-weight: 600">对象存储</span>
                <Row slot="content">
                  <Table
                    :columns="storageServiceColumns"
                    :data="storageServiceData"
                  ></Table>
                </Row>
              </Panel>
            </Collapse>
          </Row>
        </div>
      </div>
    </Col>
  </Row>
</template>
<script>
import {
  getAllCITypesByLayerWithAttr,
  getAllPluginPkgs,
  getPluginInterfaces,
  getRefCiTypeFrom,
  getRefCiTypeTo,
  getCiTypeAttr,
  getAllInstancesByPackageId,
  createPluginInstanceByPackageIdAndHostIp,
  removePluginInstance,
  savePluginInstance,
  queryLog,
  getPluginInstanceLogDetail,
  getCiTypeAttrRefAndSelect,
  getEnumCodesByCategoryId,
  getAllSystemEnumCodes,
  decommissionPluginConfig,
  releasePluginConfig,
  getAvailableContainerHosts,
  getAvailablePortByHostIp,
  preconfigurePluginPackage
} from "@/api/server.js";

const innerActions = [
  {
    label: "显示详情",
    props: {
      type: "info",
      size: "small"
    },
    actionType: "showLogDetails"
  }
];

const logTableColumns = [
  {
    title: "插件运行实例",
    key: "instance",
    inputKey: "instance",
    searchSeqNo: 1,
    displaySeqNo: 1,
    component: "WeSelect",
    isMultiple: true,
    placeholder: "插件运行实例",
    span: 5,
    width: "200px",
    options: []
  },
  {
    title: "文件名",
    key: "file_name",
    inputKey: "file_name",
    searchSeqNo: 2,
    displaySeqNo: 2,
    component: "Input",
    isNotFilterable: true,
    placeholder: "文件名",
    width: "200px"
  },
  {
    title: "行号",
    key: "line_number",
    inputKey: "line_number",
    searchSeqNo: 3,
    displaySeqNo: 3,
    component: "Input",
    isNotFilterable: true,
    placeholder: "行号",
    width: "100px"
  },
  {
    title: "匹配内容",
    key: "log",
    inputKey: "log",
    searchSeqNo: 4,
    displaySeqNo: 4,
    component: "Input",
    placeholder: "支持正则表达式"
  }
];
const pagination = {
  pageSize: 10,
  currentPage: 1,
  total: 0
};
const storageServiceColumns = [
  {
    title: "文件",
    key: "file"
  },
  {
    title: "路径",
    key: "path"
  },
  {
    title: "hash",
    key: "hash"
  },
  {
    title: "上传时间",
    key: "uploadTime"
  }
];

import DataModel from "./components/data-model.vue";
import DependencyAnalysis from "./components/dependency-analysis.vue";
import PluginRegister from "./components/plugin-register.vue";

export default {
  components: {
    DataModel,
    DependencyAnalysis,
    PluginRegister
  },
  data() {
    return {
      plugins: [],
      isShowConfigPanel: false,
      isShowRuntimeManagementPanel: false,
      currentTab: "dependency",
      currentPlugin: {},
      tableData: [],
      totalTableData: [],
      innerActions,
      logTableColumns,
      pagination,
      allAvailiableHosts: [],
      allInstances: [],
      searchFilters: [],
      logDetailsModalVisible: false,
      logDetails: "",
      dbQueryCommandString: "",
      dbQueryColumns: [],
      dbQueryData: [],
      storageServiceColumns,
      storageServiceData: []
    };
  },
  methods: {
    async onSuccess(response, file, filelist) {
      this.$Notice.success({
        title: "Success",
        desc: response.message || ""
      });
      if (response.status === "OK") {
        this.getAllPluginPkgs();
      }
    },
    onError(error, file, filelist) {
      this.$Notice.error({
        title: "Error",
        desc: file.message || ""
      });
    },
    swapPanel(isShowConfigPanel) {
      this.isShowConfigPanel = isShowConfigPanel;
      this.isShowRuntimeManagementPanel = !isShowConfigPanel;
    },
    deletePlugin(packageId) {},
    configPlugin(packageId) {
      this.swapPanel(true);
      this.currentPlugin = this.plugins.find(_ => _.id === packageId);
      this.selectedCiType = this.currentPlugin.cmdbCiTypeId || "";
      this.currentPackageId = this.currentPlugin.id;
    },
    async manageRuntimePlugin(packageId) {
      this.swapPanel(false);

      let currentPlugin = this.plugins.find(_ => _.id === packageId);
      this.selectedCiType = currentPlugin.cmdbCiTypeId || "";
      this.currentPlugin = currentPlugin;

      if (currentPlugin.pluginConfigs) {
        this.selectHosts = [];
        this.availiableHostsWithPort = [];
        this.currentPackageId = currentPlugin.id;
        this.getAllInstancesByPackageId(this.currentPackageId);
      }
      this.getAvailableContainerHosts();
      this.resetLogTable();
    },
    pluginPackageChangeHandler(key) {
      console.log("key", key);
      this.isShowConfigPanel = this.isShowRuntimeManagementPanel = false;
      this.dbQueryCommandString = "";
    },
    async getAllInstancesByPackageId(id) {
      let { data, status, message } = await getAllInstancesByPackageId(id);
      if (status === "OK") {
        this.allInstances = data.map(_ => {
          if (_.status !== "REMOVED") {
            return {
              id: _.id,
              hostIp: _.host,
              port: _.port,
              displayLabel: _.host + ":" + _.port
            };
          }
        });
        this.getHostsForTableFilter();
      }
    },
    getHostsForTableFilter() {
      this.logTableColumns[0].options = this.allInstances.map(_ => {
        return {
          label: _.hostIp + ":" + _.port,
          value: _.id
        };
      });
    },
    async getAvailableContainerHosts() {
      const { data, status, message } = await getAvailableContainerHosts();
      if (status === "OK") {
        this.allAvailiableHosts = data;
      }
    },
    getAvailablePortByHostIp() {
      this.availiableHostsWithPort = [];
      this.selectHosts.forEach(async _ => {
        const { data, status, message } = await getAvailablePortByHostIp(_);
        if (status === "OK") {
          this.availiableHostsWithPort.push({
            ip: _,
            port: data,
            createParams: this.defaultCreateParams
          });
        }
      });
    },
    handleSubmit(data) {
      this.searchFilters = data;
      this.getTableData();
    },
    pageChange(current) {
      this.pagination.currentPage = current;
      this.handlePaginationByFE();
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size;
      this.handlePaginationByFE();
    },
    actionFun(type, data) {
      if (type === "showLogDetails") {
        this.getLogDetail(data);
      }
    },
    resetLogTable() {
      this.tableData = [];
      this.totalTableData = [];
      this.$refs.table && this.$refs.table.reset();
    },
    selectHost(v) {
      this.selectHosts = v;
    },
    handleTabClick(name) {
      this.currentTab = name;
    },
    async getAllPluginPkgs() {
      let { status, data, message } = await getAllPluginPkgs();
      if (status === "OK") {
        this.plugins = data.map(_ => {
          return {
            ..._,
            title: `${_.name}[${_.version}]`,
            id: _.id,
            expand: false,
            checked: false,
            children: _.pluginConfigs.map(i => {
              return {
                ...i,
                title: i.name,
                id: i.id,
                expand: true,
                checked: false
              };
            })
          };
        });
      }
    },
    queryDBHandler() {
      console.log("db query", this.dbQueryCommandString);
    }
  },
  created() {
    this.getAllPluginPkgs();
  },
  computed: {
    setUploadActionHeader() {
      let uploadToken = document.cookie
        .split(";")
        .find(i => i.indexOf("XSRF-TOKEN") !== -1);
      return {
        "X-XSRF-TOKEN": uploadToken && uploadToken.split("=")[1]
      };
    }
  }
};
</script>
<style lang="scss"></style>
