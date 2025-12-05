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
/**
 * 资源管理主页面组件
 * 通过标签页展示资源服务器和资源实例两个管理页面
 */
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
      // 当前激活的标签页
      currentTab: 'resource',
      // 服务器列表（传递给资源实例组件）
      servers: []
    }
  },
  methods: {
    /**
     * 处理标签页点击
     * @param {string} tab - 标签页名称
     */
    handleTabClick(tab) {
      this.$refs[tab].queryData()
    },
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
