<template>
  <WeTable
    :tableData="tableData"
    :tableOuterActions="null"
    :tableInnerActions="null"
    :tableColumns="tableColumns"
    :showCheckbox="false"
    :pagination="pagination"
    ref="serviceTable"
    @handleSubmit="handleSubmit"
    @sortHandler="sortHandler"
    @pageChange="pageChange"
    @pageSizeChange="pageSizeChange"
  />
</template>

<script>
import {
  getResourceItemStatus,
  getResourceItemType,
  createItems,
  deleteItems,
  retrieveItems,
  updateItems,
  retrieveServers
} from "@/api/server.js";
import moment from "moment";

const booleanOptions = [
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
];

export default {
  props: {
    servers: {}
  },
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
          title: "type",
          key: "type",
          inputKey: "type",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "WeSelect",
          inputType: "select",
          placeholder: "type"
        },
        {
          title: "resourceServerId",
          key: "resourceServerId",
          inputKey: "resourceServerId",
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: "WeSelect",
          inputType: "select",
          placeholder: "resourceServerId"
        },
        {
          title: "Is Allocated",
          key: "isAllocated",
          inputKey: "isAllocated",
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: "WeSelect",
          inputType: "select",
          placeholder: "isAllocated",
          options: booleanOptions
        },
        {
          title: "purpose",
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
          title: "createdDate",
          key: "createdDate",
          inputKey: "createdDate",
          searchSeqNo: 10,
          displaySeqNo: 10,
          component: "DatePicker",
          type: "datetimerange",
          inputType: "date",
          placeholder: "createdDate"
        },
        {
          title: "updatedDate",
          key: "updatedDate",
          inputKey: "updatedDate",
          searchSeqNo: 11,
          displaySeqNo: 11,
          component: "DatePicker",
          type: "datetimerange",
          inputType: "date",
          placeholder: "updatedDate"
        }
      ]
    };
  },
  watch: {
    servers(val) {
      let statusIndex;
      this.tableColumns.find((_, i) => {
        if (_.key === "resourceServerId") {
          statusIndex = i;
        }
      });
      this.$set(this.tableColumns[statusIndex], "options", this.servers);
    }
  },
  methods: {
    async queryData() {
      this.payload.pageable.pageSize = this.pagination.pageSize;
      this.payload.pageable.startIndex =
        (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const { status, message, data } = await retrieveItems(this.payload);
      if (status === "OK") {
        this.tableData = data.contents.map(_ => {
          _.isAllocated = _.isAllocated ? "true" : "false";
          _.createdDate = moment(_.createdDate).format("YYYY-MM-DD hh:mm:ss");
          _.updatedDate = moment(_.updatedDate).format("YYYY-MM-DD hh:mm:ss");
          return _;
        });
        this.pagination.total = data.pageInfo.totalRows;
      }
    },
    handleSubmit(data) {
      this.payload.filters = data.map(_ => {
        if (_.name === "createdDate" || _.name === "updatedDate") {
          _.value = +new Date(_.value);
        }
        if (_.name === "isAllocated") {
          _.value = _.value === "true" ? true : false;
        }
        return _;
      });
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
    async getResourceItemStatus() {
      const { status, message, data } = await getResourceItemStatus({});
      if (status === "OK") {
        this.setOptions(data, "status");
      }
    },
    async getResourceItemType() {
      const { status, message, data } = await getResourceItemType({});
      if (status === "OK") {
        this.setOptions(data, "type");
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
    }
  },
  mounted() {
    this.getResourceItemStatus();
    this.getResourceItemType();
    this.queryData();
  }
};
</script>
