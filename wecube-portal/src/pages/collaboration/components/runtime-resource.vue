<template>
  <div>
    <Collapse :value="[1, 2, 3]">
      <Panel name="1">
        <span style="font-size: 12px">{{ $t("runtime_container") }}</span>
        <p slot="content" v-for="(item, index) in data.docker" :key="index">
          <highlight-code lang="json">{{ item }}</highlight-code>
        </p>
      </Panel>
      <Panel name="2">
        <span style="font-size: 12px">{{ $t("database") }}</span>
        <p slot="content" v-for="(item, index) in data.mysql" :key="index">
          <highlight-code lang="json">{{ item }}</highlight-code>
        </p>
      </Panel>
      <Panel name="3">
        <span style="font-size: 12px">{{ $t("storage_service") }}</span>
        <p slot="content" v-for="(item, index) in data.s3" :key="index">
          <highlight-code lang="json">{{ item }}</highlight-code>
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
