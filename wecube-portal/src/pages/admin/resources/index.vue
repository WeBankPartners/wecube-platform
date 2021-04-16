<template>
  <Tabs type="card" :value="currentTab" @on-click="handleTabClick">
    <TabPane :closable="false" name="resource" :label="$t('resource')">
      <WeServer ref="resource" />
    </TabPane>
    <TabPane :closable="false" name="resource_instance" :label="$t('resource_instance')">
      <WeService :servers="servers" ref="resource_instance" />
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
  data () {
    return {
      currentTab: 'resource',
      servers: []
    }
  },
  methods: {
    handleTabClick (tab) {
      this.$refs[tab].queryData()
    },
    async queryServers () {
      const { status, data } = await retrieveServers({})
      if (status === 'OK') {
        this.servers = data.contents.map(_ => {
          return {
            label: _.name,
            value: _.id,
            key: _.id
          }
        })
      }
    }
  },
  mounted () {
    this.queryServers()
  }
}
</script>
