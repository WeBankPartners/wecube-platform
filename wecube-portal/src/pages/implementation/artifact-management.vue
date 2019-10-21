<template>
  <Row class="artifact-management">
    <Col span="6">
      <Card>
        <p slot="title">{{ $t("system_design_version") }}</p>
        <Select
          @on-change="selectSystemDesignVersion"
          label-in-name
          v-model="systemDesignVersion"
        >
          <Option
            v-for="version in systemDesignVersions"
            :value="version.guid || ''"
            :key="version.guid"
            >{{
              version.fixed_date
                ? `${version.name}[${version.fixed_date}]`
                : version.name
            }}</Option
          >
        </Select>
      </Card>
      <Card class="artifact-management-bottom-card">
        <p slot="title">{{ $t("system_design_list") }}</p>
        <div class="artifact-management-tree-body">
          <Tree :data="treeData" @on-select-change="selectTreeNode"></Tree>
          <Spin size="large" fix v-if="treeLoading">
            <Icon type="ios-loading" size="24" class="spin-icon-load"></Icon>
            <div>loading...</div>
          </Spin>
        </div>
      </Card>
    </Col>
    <Col span="17" offset="1">
      <Card v-if="guid" class="artifact-management-top-card">
        <Upload
          :action="`/artifact/unit-designs/${guid}/packages/upload`"
          :headers="setUploadActionHeader"
          :on-success="uploadPackagesSuccess"
          slot="title"
        >
          <Button icon="ios-cloud-upload-outline">{{
            $t("new_package")
          }}</Button>
        </Upload>
        <SimpleTable
          :loading="tableLoading"
          :columns="tableColumns"
          :data="tableData"
          :page="pageInfo"
          @pageChange="pageChange"
          @pageSizeChange="pageSizeChange"
          @rowClick="rowClick"
        ></SimpleTable>
        <Modal
          v-model="isShowFilesModal"
          :title="$t('script_configuration')"
          :okText="$('save')"
          :loading="loadingForSave"
          @on-ok="saveConfigFiles"
          @on-cancel="closeModal"
        >
          <Card class="artifact-management-files-card">
            <div slot="title">
              <span>{{ $t("differentiation_document") }}</span>
              <Button @click="() => showTreeModal(0)" size="small">{{
                $t("select_file")
              }}</Button>
            </div>
            <span>{{
              currentPackage.diff_conf_file || $t("not_selected")
            }}</span>
          </Card>
          <Card class="artifact-management-files-card">
            <div slot="title">
              <span>{{ $t("startup_script") }}</span>
              <Button @click="() => showTreeModal(1)" size="small">{{
                $t("select_file")
              }}</Button>
            </div>
            <span>{{ currentPackage.start_file || $t("not_selected") }}</span>
          </Card>
          <Card class="artifact-management-files-card">
            <div slot="title">
              <span>{{ $t("stop_script") }}</span>
              <Button @click="() => showTreeModal(2)" size="small">{{
                $t("select_file")
              }}</Button>
            </div>
            <span>{{ currentPackage.stop_file || $t("not_selected") }}</span>
          </Card>
          <Card class="artifact-management-files-card">
            <div slot="title">
              <span>{{ $t("deployment_script") }}</span>
              <Button @click="() => showTreeModal(3)" size="small">{{
                $t("select_file")
              }}</Button>
            </div>
            <span>{{ currentPackage.deploy_file || $t("not_selected") }}</span>
          </Card>
        </Modal>
        <Modal
          v-model="isShowTreeModal"
          :title="currentTreeModal.title"
          @on-ok="onOk"
          @on-cancel="closeTreeModal"
        >
          <RadioGroup v-model="selectFile">
            <Tree :data="filesTreeData" @on-toggle-expand="expandNode"></Tree>
          </RadioGroup>
        </Modal>
      </Card>
      <Card
        v-if="tabData.length ? true : false"
        class="artifact-management-bottom-card artifact-management-top-card"
      >
        <Tabs v-model="activeTab" @on-click="tabChange">
          <TabPane
            v-for="(item, index) in tabData"
            :label="item.title"
            :name="item.title"
            :key="index"
          >
            <Table
              :data="item.tableData || []"
              :columns="attrsTableColomnOptions"
            ></Table>
          </TabPane>
        </Tabs>
      </Card>
    </Col>
  </Row>
</template>

<script>
import {
  getPackageCiTypeId,
  getAllCITypesByLayerWithAttr,
  getSystemDesignVersions,
  getSystemDesignVersion,
  queryPackages,
  deleteCiDatas,
  operateCiState,
  getFiles,
  getKeys,
  saveConfigFiles,
  saveDiffConfigEnumCodes,
  getDiffConfigEnumCodes,
  getAllSystemEnumCodes
} from "@/api/server.js";

export default {
  data() {
    return {
      packageCiType: 0,
      statusOperations: [],
      systemDesignVersions: [],
      systemDesignVersion: "",
      ciTypesObj: {},
      ciTypeAttributeObj: {},
      ciTypes: [],
      treeData: [],
      treeLoading: false,
      loadingForSave: false,
      selectFile: "",
      filesTreeData: [],
      guid: "",
      currentPackage: {},
      packageId: "",
      isShowFilesModal: false,
      isShowTreeModal: false,
      treeModalOpt: [
        {
          title: this.$t("select_differentiation_document"),
          key: "diff_conf_file",
          inputType: "checkbox"
        },
        {
          title: this.$t("select_startup_script"),
          key: "start_file",
          inputType: "radio"
        },
        {
          title: this.$t("select_stop_script"),
          key: "stop_file",
          inputType: "radio"
        },
        {
          title: this.$t("select_deployment_script"),
          key: "deploy_file",
          inputType: "radio"
        }
      ],
      currentTreeModal: {},
      tableLoading: false,
      tableData: [],
      tableColumns: [
        {
          title: this.$t("package_name"),
          key: "name"
        },
        {
          title: this.$t("upload_time"),
          width: 150,
          key: "upload_time"
        },
        {
          title: this.$t("md5_value"),
          key: "md5_value"
        },
        {
          title: this.$t("upload_by"),
          key: "updated_by"
        },
        {
          title: this.$t("differentiation_document"),
          key: "diff_conf_file"
        },
        {
          title: this.$t("startup_script"),
          key: "start_file"
        },
        {
          title: this.$t("stop_script"),
          key: "stop_file"
        },
        {
          title: this.$t("deployment_script"),
          key: "deploy_file"
        },
        {
          title: this.$t("table_action"),
          key: "state",
          width: 150,
          render: (h, params) => {
            return (
              <div style="padding-top:5px">
                {this.renderActionButton(params)}
              </div>
            );
          }
        }
      ],
      pageInfo: {
        pageSize: 5,
        currentPage: 1,
        total: 0
      },
      activeTab: "",
      diffTabData: "",
      tabData: [],
      selectNode: [],
      attrsTableColomnOptions: [
        {
          title: this.$t("index"),
          key: "index",
          width: 60
        },
        {
          title: this.$t("file_line_number"),
          key: "line"
        },
        {
          title: "properties-key",
          key: "key"
        },
        {
          title: "CMDB-ATTR",
          render: (h, params) => {
            return params.row.attrInputValue ? (
              <AttrInput
                style="margin-top:5px;"
                allCiTypes={this.ciTypes}
                rootCiType={15}
                isReadOnly={true}
                ciTypesObj={this.ciTypesObj}
                ciTypeAttributeObj={this.ciTypeAttributeObj}
                sourceData={params.row.attrInputValue}
                onUpdateRoutine={val => this.updateRoutine(val, params.index)}
              />
            ) : (
              <div style="align-items:center;display:flex;">
                <AttrInput
                  style="margin-top:5px;width:calc(100% - 55px);"
                  allCiTypes={this.ciTypes}
                  rootCiType={15}
                  ciTypesObj={this.ciTypesObj}
                  ciTypeAttributeObj={this.ciTypeAttributeObj}
                  onUpdateRoutine={val => this.updateRoutine(val, params.index)}
                />
                <Button
                  size="small"
                  type="primary"
                  style="margin-left:10px"
                  onClick={() => this.saveAttr(params.index)}
                >
                  {this.$t("save")}
                </Button>
              </div>
            );
          }
        }
      ]
    };
  },
  computed: {
    setUploadActionHeader() {
      let uploadToken = document.cookie
        .split(";")
        .find(i => i.indexOf("XSRF-TOKEN") !== -1);
      return {
        "X-XSRF-TOKEN": uploadToken && uploadToken.split("=")[1]
      };
    },
    nowTab() {
      let result = 0;
      this.tabData.find((_, i) => {
        if (_.title === this.activeTab) {
          result = i;
          return true;
        }
      });
      return result;
    }
  },
  methods: {
    uploadPackagesSuccess(response, file, fileList) {
      if (response.status === "ERROR") {
        this.$Notice.error({
          title: "Error",
          desc: response.message || ""
        });
      } else {
        this.queryPackages();
      }
    },

    renderActionButton(params) {
      const row = params.row;
      return this.statusOperations
        .filter(_ => row.nextOperations.indexOf(_.type) >= 0)
        .map(_ => {
          return (
            <Button
              {...{ props: { ..._.props } }}
              style="margin-right:5px;margin-bottom:5px;"
              onClick={() => this.changeStatus(row, _.type)}
            >
              {_.label}
            </Button>
          );
        });
    },
    async fetchData() {
      const [sysData, packageCiType] = await Promise.all([
        getSystemDesignVersions(),
        getPackageCiTypeId()
      ]);
      if (sysData.status === "OK" && sysData.data.contents instanceof Array) {
        this.systemDesignVersions = sysData.data.contents.map(_ => _.data);
      }
      if (packageCiType.status === "OK") {
        this.packageCiType = packageCiType.data;
      }
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
        this.ciTypes = tempCITypes;

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

    async getSystemDesignVersion(guid) {
      this.treeLoading = true;
      let { status, data, message } = await getSystemDesignVersion(guid);
      if (status === "OK") {
        this.treeData = this.formatTreeData(data, 1);
        this.treeLoading = false;
      }
    },
    async queryPackages() {
      this.tableLoading = true;
      let { status, data, message } = await queryPackages(this.guid, {
        sorting: {
          asc: false,
          field: "upload_time"
        },
        paging: true,
        pageable: {
          pageSize: this.pageInfo.pageSize,
          startIndex: (this.pageInfo.currentPage - 1) * this.pageInfo.pageSize
        }
      });
      if (status === "OK") {
        this.tableLoading = false;
        this.tableData = data.contents.map(_ => {
          return {
            ..._.data,
            nextOperations: _.meta.nextOperations || []
          };
        });
        const { pageSize, totalRows: total } = data.pageInfo;
        const currentPage = this.pageInfo.currentPage;
        this.pageInfo = { currentPage, pageSize, total };
      }
    },
    async getFiles(packageId, currentDir) {
      this.packageId = packageId;
      let { status, data, message } = await getFiles(this.guid, packageId, {
        currentDir
      });
      if (status === "OK") {
        this.isShowFilesModal = true;
        this.genFilesTreedata({ files: data.outputs[0].files, currentDir });
      }
    },
    async getKeys(options) {
      if (!options) return;
      let { status, data, message } = await getKeys(this.guid, this.packageId, {
        filePath: options.path
      });
      if (status === "OK") {
        const diffConfigEnums = await getDiffConfigEnumCodes();
        if (diffConfigEnums.status === "OK") {
          const result = data.outputs[0].config_key_infos.map((_, i) => {
            _.index = i + 1;
            const found = diffConfigEnums.data.find(
              item => item.value === _.key
            );
            if (found) {
              _.attrInputValue = found.code;
            }
            return _;
          });
          this.$set(options, "tableData", result);
        }
      }
    },
    async saveConfigFiles() {
      this.loadingForSave = true;
      const obj = {
        configFilesWithPath: this.diffTabData.split("|"),
        startFile: this.currentPackage.start_file || "",
        stopFile: this.currentPackage.stop_file || "",
        deployFile: this.currentPackage.deploy_file || ""
      };
      let { status, data, message } = await saveConfigFiles(
        this.guid,
        this.packageId,
        obj
      );
      if (status === "OK") {
        this.loadingForSave = false;
        this.$Notice.success({
          title: this.$t("save_successfully")
        });
      }
      this.queryPackages();
      this.getTabDatas(this.diffTabData);
    },
    async saveDiffConfigEnumCodes(obj) {
      let { status, data, message } = await saveDiffConfigEnumCodes(obj);
      if (status === "OK") {
        this.$Notice.success({
          title: this.$t("save_successfully")
        });
        this.getKeys(this.tabData[this.nowTab]);
      }
    },
    selectSystemDesignVersion(guid) {
      this.getSystemDesignVersion(guid);
    },
    formatTreeData(array, level) {
      return array.map(_ => {
        _.title = _.data.name;
        _.level = level;
        if (_.children && _.children.length) {
          _.expand = true;
          _.children = this.formatTreeData(_.children, level + 1);
        }
        return _;
      });
    },
    selectTreeNode(node) {
      if (node.length && node[0].level === 3) {
        this.guid = node[0].data.r_guid;
        this.queryPackages();
        this.tabData = [];
      }
    },
    pageChange(currentPage) {
      this.pageInfo.currentPage = currentPage;
      this.queryPackages();
    },
    pageSizeChange(pageSize) {
      this.pageInfo.pageSize = pageSize;
      this.queryPackages();
    },
    genFilesTreedata(data) {
      const { files, currentDir } = data;
      if (currentDir) {
        const filesArray = currentDir.split("/");
        let targetNode = this.filesTreeData;
        filesArray.forEach((dir, index) => {
          if (index) {
            targetNode = targetNode.children;
          }
          targetNode.find(_ => {
            if (dir === _.title) {
              targetNode = _;
              return true;
            }
          });
        });
        targetNode.children = this.formatChildrenData({
          files,
          currentDir,
          level: targetNode.level + 1
        });
      } else {
        this.filesTreeData = this.formatChildrenData({
          files,
          currentDir,
          level: 1
        });
      }
    },
    formatChildrenData(val) {
      const { files, currentDir, level } = val;
      if (!(files instanceof Array)) {
        return;
      }
      const tagName = this.currentTreeModal.inputType;
      return files.map(_ => {
        let obj = {
          title: _.name,
          path: currentDir ? `${currentDir}/${_.name}` : _.name,
          level: level
        };
        if (_.isDir) {
          obj.children = [{}];
        } else {
          obj.render = (h, params) => {
            return this.currentTreeModal.inputType === "checkbox" ? (
              <Checkbox
                on-on-change={value => this.checkboxChange(value, params.data)}
              >
                <span>{params.data.title}</span>
              </Checkbox>
            ) : (
              <Radio label={params.data.path}>
                <span>{params.data.title}</span>
              </Radio>
            );
          };
        }
        return obj;
      });
    },
    expandNode(node) {
      if (node.expand && !node.children[0].title) {
        this.getFiles(this.packageId, node.path);
      }
    },
    rowClick(row) {
      this.packageId = row.guid;
      this.getTabDatas(row.diff_conf_file);
    },
    changeStatus(row, status) {
      switch (status) {
        case "update":
          this.showFilesModal(row);
          break;
        case "delete":
          this.handleDelete(row, status);
          break;
        default:
          this.handleStatusChange(row, status);
          break;
      }
    },
    async handleDelete(row) {
      this.$Modal.confirm({
        title: this.$t("confirm_to_delete"),
        "z-index": 1000000,
        onOk: async () => {
          const { status, data, message } = await deleteCiDatas({
            id: this.packageCiType,
            deleteData: [row.guid]
          });
          if (status === "OK") {
            this.$Notice.success({
              title: "Delete data Success",
              desc: message
            });
            this.queryPackages();
          }
        }
      });
    },
    async handleStatusChange(row, state) {
      const { data, status, message } = await operateCiState(
        this.packageCiType,
        row.guid,
        state
      );
      if (status === "OK") {
        this.$Notice.success({
          title: state,
          desc: message
        });
        this.queryPackages();
      }
    },
    showFilesModal(row) {
      this.tabData = [];
      this.currentPackage = JSON.parse(JSON.stringify(row));
      this.packageId = this.currentPackage.guid;
      this.diffTabData = row.diff_conf_file || "";
      this.isShowFilesModal = true;
    },

    getTabDatas(diffFile) {
      if (diffFile) {
        const files = diffFile.split("|");
        this.tabData = files.map(_ => {
          const f = _.split("/");
          return {
            path: _,
            title: f[f.length - 1]
          };
        });
        this.activeTab = this.tabData.length ? this.tabData[0].title : "";
        this.getKeys(this.tabData[0]);
      } else {
        this.tabData = [];
      }
    },
    showTreeModal(type) {
      this.currentTreeModal = this.treeModalOpt[type];
      if (!this.filesTreeData.length)
        this.getFiles(this.currentPackage.guid, "");
      this.isShowTreeModal = true;
    },
    closeModal() {
      this.currentPackage = {};
    },
    onOk() {
      if (this.currentTreeModal.key === "diff_conf_file") {
        this.diffTabData = "";
        let files = [];
        this.selectNode.forEach((_, index) => {
          index === 0 ? files.push(_.path) : files.push("/" + _.path);
        });
        this.diffTabData = files.join("|");
        this.currentPackage.diff_conf_file = files.join("|");
        this.selectNode = [];
        this.filesTreeData = [];
      } else {
        this.currentPackage[this.currentTreeModal.key] = this.selectFile;
      }
    },
    closeTreeModal() {
      this.selectNode = [];
      this.filesTreeData = [];
    },
    checkboxChange(value, data) {
      if (value) {
        this.selectNode.push(data);
      } else {
        let i = 0;
        this.selectNode.find((_, index) => {
          if (_.path === data.path) {
            i = index;
            return true;
          }
        });
        this.selectNode.splice(i, 1);
      }
    },
    tabChange(tabName) {
      this.tabData.find(_ => {
        if (_.title === tabName && !(_.tableData instanceof Array)) {
          this.getKeys(_);
          return true;
        }
      });
    },
    updateRoutine(val, row) {
      this.tabData[this.nowTab].tableData[row].routine = JSON.stringify(val);
    },
    saveAttr(row) {
      this.saveDiffConfigEnumCodes({
        value: this.tabData[this.nowTab].tableData[row].key,
        code: this.tabData[this.nowTab].tableData[row].routine
      });
    },
    async getAllSystemEnumCodes() {
      const { status, data, message } = await getAllSystemEnumCodes({
        filters: [
          {
            name: "cat.catName",
            operator: "eq",
            value: "state_transition_operation"
          }
        ],
        paging: false
      });
      if (status === "OK" && data.contents instanceof Array) {
        const buttonTypes = {
          confirm: "info",
          delete: "error",
          discard: "warning",
          update: "primary"
        };
        this.statusOperations = data.contents
          .filter(
            _ =>
              _.code === "confirm" ||
              _.code === "delete" ||
              _.code === "discard" ||
              _.code === "update"
          )
          .map(_ => {
            return {
              type: _.code,
              label: _.code !== "update" ? _.value : this.$t("configuration"),
              props: {
                type: buttonTypes[_.code] || "info",
                size: "small"
              },
              actionType: _.code
            };
          });
      }
    }
  },
  created() {
    this.fetchData();
    this.getAllCITypesByLayerWithAttr();
    this.getAllSystemEnumCodes();
  }
};
</script>

<style lang="scss" scoped>
.artifact-management {
  padding: 20px;

  &-top-card {
    padding-bottom: 40px;
  }

  &-bottom-card {
    margin-top: 30px;
  }

  &-tree-body {
    position: relative;
  }

  &-save-button {
    float: right;
    margin-top: 10px;
  }

  &-files-card {
    margin-top: 10px;

    &:first-of-type {
      margin-top: 0;
    }
  }
}
</style>
