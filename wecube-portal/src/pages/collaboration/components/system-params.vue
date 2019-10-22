<template>
  <div>
    <Table :columns="tableColumns" :data="tableData"></Table>
  </div>
</template>

<script>
import { getSysParams } from "@/api/server";
export default {
  name: "sys-params",
  data() {
    return {
      tableData: [],
      tableColumns: [
        {
          title: "归属",
          key: "scope_type"
        },
        {
          title: "参数名称",
          key: "name"
        },
        {
          title: "参数值",
          key: "value"
        },
        {
          title: "说明",
          key: "description"
        },
        {
          title: "状态",
          key: "status"
        }
      ]
    };
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData();
      }
    }
  },
  props: {
    pkgId: {
      required: true,
      type: Number
    }
  },
  created() {
    this.getData();
  },
  methods: {
    async getData() {
      // let { status, data, message } = await getSysParams(this.pkgId);
      let { status, data, message } = await getSysParams(1);
      if (status === "OK") {
        this.tableData = data;
      }
    }
  }
};
</script>
