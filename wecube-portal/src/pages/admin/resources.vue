<template>
  <WeTable
    :tableData="tableData"
    :tableOuterActions="outerActions"
    :tableInnerActions="null"
    :tableColumns="tableColumns"
    @actionFun="actionFun"
    @getSelectedRows="onSelectedRowsChange"
    ref="table"
  />
</template>

<script>
import {
  getResourceServerType,
  getResourceServerStatus,
  retrieveServers,
  createServers,
  updateServers,
  deleteServers
} from "@/api/server.js";
import { outerActions } from "@/const/actions.js";
export default {
  data() {
    return {
      outerActions,
      tableData: [],
      tableColumns: [
        {
          title: "Id",
          key: "id",
          inputKey: "id",
          searchSeqNo: 1,
          displaySeqNo: 1,
          component: "Input",
          inputType: "text",
          placeholder: "id"
        },
        {
          title: "Name",
          key: "name",
          inputKey: "name",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          inputType: "text",
          placeholder: "name"
        },
        {
          title: "Host",
          key: "host",
          inputKey: "host",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "Input",
          inputType: "text",
          placeholder: "host"
        },
        {
          title: "Is Allocated",
          key: "isAllocated",
          inputKey: "isAllocated",
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: "WeSelect",
          inputType: "select",
          placeholder: "isAllocated",
          options: [
            {
              label: "true",
              value: "true",
              key: "true"
            },
            {
              label: "false",
              value: "false",
              key: "false"
            }
          ]
        },
        {
          title: "Login Username",
          key: "loginUsername",
          inputKey: "loginUsername",
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: "Input",
          inputType: "text",
          placeholder: "loginUsername"
        },
        {
          title: "Login Password",
          key: "loginPassword",
          inputKey: "loginPassword",
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: "Input",
          inputType: "text",
          placeholder: "loginPassword"
        },
        {
          title: "Port",
          key: "port",
          inputKey: "port",
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: "Input",
          inputType: "text",
          placeholder: "port"
        },
        {
          title: "Purpose",
          key: "purpose",
          inputKey: "purpose",
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: "Input",
          inputType: "text",
          placeholder: "purpose"
        },
        {
          title: "Status",
          key: "status",
          inputKey: "status",
          searchSeqNo: 9,
          displaySeqNo: 9,
          component: "WeSelect",
          inputType: "select",
          placeholder: "status"
        },
        {
          title: "Type",
          key: "type",
          inputKey: "type",
          searchSeqNo: 10,
          displaySeqNo: 10,
          component: "WeSelect",
          inputType: "select",
          placeholder: "type"
        }
      ]
    };
  },
  methods: {
    async queryData() {
      const { status, message, data } = await retrieveServers({});
      if (status === "OK") {
        this.tableData = data.contents.map(_ => {
          _.isAllocated = _.isAllocated ? "true" : "false";
          return _;
        });
      }
    },
    async getResourceServerStatus() {
      const { status, message, data } = await getResourceServerStatus();
      if (status === "OK") {
        let statusIndex;
        this.tableColumns.find((_, i) => {
          if (_.key === "status") {
            statusIndex = i;
          }
        });
        const statusOptions = data.map(_ => {
          return {
            label: _,
            value: _,
            key: _
          };
        });
        this.$set(this.tableColumns[statusIndex], "options", statusOptions);
      }
    },
    async getResourceServerType() {
      const { status, message, data } = await getResourceServerType();
      if (status === "OK") {
        const typeOptions = data.map(_ => {
          return {
            label: _,
            value: _,
            key: _
          };
        });
        this.$set(
          this.tableColumns[this.tableColumns.length - 1],
          "options",
          typeOptions
        );
      }
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
          host: addObj.host,
          isAllocated: addObj.isAllocated === "true" ? true : false,
          loginPassword: addObj.loginPassword,
          loginUsername: addObj.loginUsername,
          name: addObj.name,
          port: addObj.port,
          purpose: addObj.purpose,
          status: addObj.status,
          type: addObj.type
        };
        const { status, message, data } = await createServers([payload]);
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
            host: _.host,
            isAllocated: _.isAllocated === "true" ? true : false,
            loginPassword: _.loginPassword,
            loginUsername: _.loginUsername,
            name: _.name,
            port: _.port,
            purpose: _.purpose,
            status: _.status,
            type: _.type
          };
        });
        const { status, message, data } = await updateServers(payload);
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
          const { status, message, data } = await deleteServers(payload);
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
    // TODO
    async exportHandler() {}
  },
  mounted() {
    this.getResourceServerType();
    this.getResourceServerStatus();
    this.queryData();
  }
};
</script>

<style lang="scss" scoped></style>
