<template>
  <WeTable
    :tableData="tableData"
    :tableOuterActions="outerActions"
    :tableInnerActions="null"
    :tableColumns="tableColumns"
    @actionFun="actionFun"
    ref="table"
  />
</template>

<script>
import { getAllSystemVariables } from "@/api/server.js";
import { outerActions } from "@/const/actions.js";
export default {
  data() {
    return {
      outerActions,
      tableData: [],
      tableColumns: [
        {
          title: "id",
          key: "id",
          inputKey: "id",
          searchSeqNo: 1,
          displaySeqNo: 1,
          component: "Input",
          inputType: "text",
          placeholder: "id"
        },
        {
          title: "name",
          key: "name",
          inputKey: "name",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          inputType: "text",
          placeholder: "name"
        },
        {
          title: "value",
          key: "value",
          inputKey: "value",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "Input",
          inputType: "text",
          placeholder: "value"
        },
        {
          title: "defaultValue",
          key: "defaultValue",
          inputKey: "defaultValue",
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: "Input",
          inputType: "text",
          placeholder: "defaultValue"
        },
        {
          title: "scopeType",
          key: "scopeType",
          inputKey: "scopeType",
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: "WeSelect",
          inputType: "select",
          placeholder: "scopeType",
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
          title: "scopeValue",
          key: "scopeValue",
          inputKey: "scopeValue",
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: "Input",
          inputType: "text",
          placeholder: "scopeValue"
        },
        {
          title: "seqNo",
          key: "seqNo",
          inputKey: "seqNo",
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: "Input",
          inputType: "text",
          placeholder: "seqNo"
        },
        {
          title: "status",
          key: "status",
          inputKey: "status",
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: "Input",
          inputType: "text",
          placeholder: "status"
        }
      ]
    };
  },
  methods: {
    async queryData() {
      const { status, message, data } = await getAllSystemVariables();
      if (status === "OK") {
        this.tableData = data;
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
        default:
          break;
      }
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
      console.log(JSON.parse(JSON.stringify(data)));
      const setBtnsStatus = () => {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(
            _.actionType === "add" || _.actionType === "export"
          );
        });
      };
      setBtnsStatus();
    }
  },
  mounted() {
    this.queryData();
  }
};
</script>

<style lang="scss" scoped></style>
