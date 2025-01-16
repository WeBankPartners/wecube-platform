<template>
  <Tabs type="card" v-model="currentTab">
    <TabPane :closable="false" name="resource" :label="$t('resource')">
      <WeServer v-if="currentTab === 'resource'" ref="resource" />
    </TabPane>
    <TabPane :closable="false" name="resource_instance" :label="$t('resource_instance')">
      <WeService v-if="currentTab === 'resource_instance'" :servers="servers" ref="resource_instance" />
    </TabPane>
  </Tabs>
</template>

<script>
import { retrieveServers } from '@/api/server.js'
import WeServer from './server'
import WeService from './service'

export default {
  components: {
    WeServer,
    WeService
  },
  data() {
    return {
      currentTab: 'resource',
      servers: []
    }
  },
  methods: {
    async queryServers() {
      const { status, data } = await retrieveServers({})
      if (status === 'OK') {
        this.servers = (data.contents
            && data.contents.map(_ => ({
              label: _.name,
              value: _.id,
              key: _.id
            })))
          || []
      }
    }
  },
  mounted() {
    this.queryServers()
  }
}
</script>
