<template>
  <WeTable
    :tableData="tableData"
    :tableOuterActions="outerActions"
    :tableInnerActions="null"
    :tableColumns="tableColumns"
    :pagination="pagination"
    @actionFun="actionFun"
    @handleSubmit="handleSubmit"
    @sortHandler="sortHandler"
    @getSelectedRows="onSelectedRowsChange"
    @pageChange="pageChange"
    @pageSizeChange="pageSizeChange"
    ref="table"
  />
</template>

<script>
import {
  retrieveSystemVariables,
  createSystemVariables,
  updateSystemVariables,
  deleteSystemVariables,
  getResourceServerStatus
} from "@/api/server.js";
import { outerActions } from "@/const/actions.js";
import { formatData } from "../util/format.js";

export default {
  data() {
    return {
      payload: {
        filters: [],
        pageable: {
          pageSize: 10,
          startIndex: 0
        },
        paging: true
      },
      pagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      outerActions,
      tableData: [],
      tableColumns: [
        {
          title: this.$t("table_id"),
          key: "id",
          inputKey: "id",
          searchSeqNo: 1,
          displaySeqNo: 1,
          disEditor: true,
          disAdded: true,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_id")
        },
        {
          title: this.$t("table_name"),
          key: "name",
          inputKey: "name",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_name")
        },
        {
          title: this.$t("table_value"),
          key: "value",
          inputKey: "value",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_value")
        },
        {
          title: this.$t("table_default_value"),
          key: "defaultValue",
          inputKey: "defaultValue",
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_default_value")
        },
        {
          title: this.$t("table_scope_type"),
          key: "scopeType",
          inputKey: "scopeType",
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: "WeSelect",
          inputType: "select",
          placeholder: this.$t("table_scope_type"),
          options: [
            {
              label: "global",
              value: "global",
              key: "global"
            },
            {
              label: "plugin-package",
              value: "plugin-package",
              key: "plugin-package"
            }
          ]
        },
        {
          title: this.$t("table_scope_value"),
          key: "scopeValue",
          inputKey: "scopeValue",
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_scope_value")
        },
        {
          title: this.$t("table_seq_no"),
          key: "seqNo",
          inputKey: "seqNo",
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_seq_no")
        },
        {
          title: this.$t("table_status"),
          key: "status",
          inputKey: "status",
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: "WeSelect",
          inputType: "select",
          placeholder: this.$t("table_status")
        }
      ]
    };
  },
  methods: {
    async queryData() {
      this.payload.pageable.pageSize = this.pagination.pageSize;
      this.payload.pageable.startIndex =
        (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const { status, message, data } = await retrieveSystemVariables(
        this.payload
      );
      if (status === "OK") {
        this.tableData = data.contents;
        this.pagination.total = data.pageInfo.totalRows;
      }
    },
    async getStatus() {
      const { status, message, data } = await getResourceServerStatus({});
      if (status === "OK") {
        this.setOptions(data, "status");
      }
    },
    setOptions(data, column) {
      let statusIndex;
      this.tableColumns.find((_, i) => {
        if (_.key === column) {
          statusIndex = i;
        }
      });
      const options = data.map(_ => {
        return {
          label: _,
          value: _,
          key: _
        };
      });
      this.$set(this.tableColumns[statusIndex], "options", options);
    },
    handleSubmit(data) {
      this.payload.filters = data;
      this.queryData();
    },
    sortHandler(data) {
      if (data.order === "normal") {
        delete this.payload.sorting;
      } else {
        this.payload.sorting = {
          asc: data.order === "asc",
          field: data.key
        };
      }
      this.queryData();
    },
    pageChange(current) {
      this.pagination.currentPage = current;
      this.queryData();
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size;
      this.queryData();
    },
    actionFun(type, data) {
      switch (type) {
        case "add":
          this.addHandler();
          break;
        case "save":
          this.saveHandler(data);
          break;
        case "edit":
          this.editHandler();
          break;
        case "delete":
          this.deleteHandler(data);
          break;
        case "cancel":
          this.cancelHandler();
          break;
        case "export":
          this.exportHandler();
          break;
        default:
          break;
      }
    },
    onSelectedRowsChange(rows, checkoutBoxdisable) {
      if (rows.length > 0) {
        this.outerActions.forEach(_ => {
          _.props.disabled = _.actionType === "add";
        });
      } else {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(
            _.actionType === "add" ||
            _.actionType === "export" ||
            _.actionType === "cancel"
          );
        });
      }
      this.seletedRows = rows;
    },
    addHandler() {
      let emptyRowData = {};
      this.tableColumns.forEach(_ => {
        emptyRowData[_.inputKey] = "";
      });
      emptyRowData.isRowEditable = true;
      emptyRowData.isNewAddedRow = true;
      emptyRowData.weTableRowId = new Date().getTime();
      this.tableData.unshift(emptyRowData);
      this.$nextTick(() => {
        this.$refs.table.pushNewAddedRowToSelections();
        this.$refs.table.setCheckoutStatus(true);
      });
      this.outerActions.forEach(_ => {
        _.props.disabled = _.actionType === "add";
      });
    },
    async saveHandler(data) {
      const setBtnsStatus = () => {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(
            _.actionType === "add" ||
            _.actionType === "export" ||
            _.actionType === "cancel"
          );
        });
        this.$refs.table.setAllRowsUneditable();
        this.$nextTick(() => {
          /* to get iview original data to set _ischecked flag */
          let objData = this.$refs.table.$refs.table.$refs.tbody.objData;
          for (let obj in objData) {
            objData[obj]._isChecked = false;
            objData[obj]._isDisabled = false;
          }
        });
      };
      let d = JSON.parse(JSON.stringify(data));
      let addObj = d.find(_ => _.isNewAddedRow);
      let editAry = d.filter(_ => !_.isNewAddedRow);
      if (addObj) {
        let payload = {
          defaultValue: addObj.defaultValue,
          name: addObj.name,
          pluginPackageId: addObj.pluginPackageId,
          pluginPackageName: addObj.pluginPackageName,
          scopeType: addObj.scopeType,
          status: addObj.status
        };
        const { status, message, data } = await createSystemVariables([
          payload
        ]);
        if (status === "OK") {
          this.$Notice.success({
            title: "Add Success",
            desc: message
          });
          setBtnsStatus();
          this.queryData();
        }
      }
      if (editAry.length > 0) {
        let payload = editAry.map(_ => {
          return {
            id: _.id,
            defaultValue: _.defaultValue,
            name: _.name,
            pluginPackageId: _.pluginPackageId,
            pluginPackageName: _.pluginPackageName,
            scopeType: _.scopeType,
            status: _.status
          };
        });
        const { status, message, data } = await updateSystemVariables(payload);
        if (status === "OK") {
          this.$Notice.success({
            title: "Update Success",
            desc: message
          });
          setBtnsStatus();
          this.queryData();
        }
      }
    },
    editHandler() {
      this.$refs.table.swapRowEditable(true);
      this.outerActions.forEach(_ => {
        if (_.actionType === "save") {
          _.props.disabled = false;
        }
      });
      this.$nextTick(() => {
        this.$refs.table.setCheckoutStatus(true);
      });
    },
    deleteHandler(deleteData) {
      this.$Modal.confirm({
        title: this.$t("confirm_to_delete"),
        "z-index": 1000000,
        onOk: async () => {
          const payload = deleteData.map(_ => {
            return {
              id: _.id
            };
          });
          const { status, message, data } = await deleteSystemVariables(
            payload
          );
          if (status === "OK") {
            this.$Notice.success({
              title: "Delete Success",
              desc: message
            });
            this.outerActions.forEach(_ => {
              _.props.disabled =
                _.actionType === "save" ||
                _.actionType === "edit" ||
                _.actionType === "delete";
            });
            this.queryData();
          }
        },
        onCancel: () => {}
      });
    },
    cancelHandler() {
      this.$refs.table.setAllRowsUneditable();
      this.$refs.table.setCheckoutStatus();
      this.outerActions &&
        this.outerActions.forEach(_ => {
          _.props.disabled = !(
            _.actionType === "add" ||
            _.actionType === "export" ||
            _.actionType === "cancel"
          );
        });
    },
    async exportHandler() {
      const { status, message, data } = await retrieveSystemVariables({});
      if (status === "OK") {
        this.$refs.table.export({
          filename: "System Params",
          data: formatData(data.contents)
        });
      }
    }
  },
  mounted() {
    this.getStatus();
    this.queryData();
  }
};
</script>

<style lang="scss" scoped></style>
