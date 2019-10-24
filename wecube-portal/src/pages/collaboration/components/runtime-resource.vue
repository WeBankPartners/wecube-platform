<template>
  <div>
    <Collapse :value="[1, 2, 3]">
      <Panel name="1">
        <span style="font-size: 12px">运行容器</span>
        <p slot="content" v-for="(item, index) in data.docker" :key="index">
          {{ index + 1 + ": " + JSON.stringify(item) }}
        </p>
      </Panel>
      <Panel name="2">
        <span style="font-size: 12px">数据库</span>
        <p slot="content" v-for="(item, index) in data.mysql" :key="index">
          {{ index + 1 + ": " + JSON.stringify(item) }}
        </p>
      </Panel>
      <Panel name="3">
        <span style="font-size: 12px">对象存储</span>
        <p slot="content" v-for="(item, index) in data.s3" :key="index">
          {{ index + 1 + ": " + JSON.stringify(item) }}
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
      let { status, data, message } = await getRuntimeResource(this.pkgId);
      if (status === "OK") {
        this.data = data;
      }
    }
  }
};
</script>
