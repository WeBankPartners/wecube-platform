<template>
  <div>
    <Collapse :value="[1, 2, 3]">
      <Panel name="1">
        <span style="font-size: 12px">运行容器</span>
        <p slot="content">
          {{ data.docker }}
        </p>
      </Panel>
      <Panel name="2">
        <span style="font-size: 12px">数据库</span>
        <p slot="content">
          {{ data.mysql }}
        </p>
      </Panel>
      <Panel name="3">
        <span style="font-size: 12px">对象存储</span>
        <p slot="content">
          {{ data.s3 }}
        </p>
      </Panel>
    </Collapse>
  </div>
</template>

<script>
import { getRuntimeResource } from "@/api/server";
export default {
  name: "runtime-resources",
  data() {
    return {
      data: {}
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
      // let { status, data, message } = await getRuntimeResource(this.pkgId);
      let { status, data, message } = await getRuntimeResource(1);
      if (status === "OK") {
        this.data = data;
      }
    }
  }
};
</script>
