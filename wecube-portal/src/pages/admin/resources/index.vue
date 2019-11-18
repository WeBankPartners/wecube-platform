<template>
  <Tabs type="card" :value="currentTab" @on-click="handleTabClick">
    <TabPane :closable="false" name="hosts" :label="$t('hosts')">
      <WeServer />
    </TabPane>
    <TabPane :closable="false" name="instances" :label="$t('instances')">
      <WeService :servers="servers" />
    </TabPane>
  </Tabs>
</template>

<script>
import {
  getResourceItemStatus,
  getResourceItemType,
  retrieveServers
} from "@/api/server.js";
import WeServer from "./server";
import WeService from "./service";

export default {
  components: {
    WeServer,
    WeService
  },
  data() {
    return {
      currentTab: "hosts",
      servers: []
    };
  },
  methods: {
    handleTabClick(tab) {
      this.$refs[tab].queryData();
    },
    async queryServers() {
      const { status, message, data } = await retrieveServers({});
      if (status === "OK") {
        this.servers = data.contents.map(_ => {
          return {
            label: _.id,
            value: _.id,
            key: _.id
          };
        });
      }
    }
  },
  mounted() {
    this.queryServers();
  }
};
</script>
