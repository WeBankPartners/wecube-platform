<template>
  <div>
    <div style="display: flex; justify-content: space-between">
      <div class="flow-name" @click="openCanvasPanel">
        <span style="vertical-align: middle">{{ itemCustomInfo.name }}</span>
        <Tag>v{{ itemCustomInfo.version }}</Tag>
        <img src="../../../assets/icon/edit-black.png" style="width: 16px; vertical-align: middle" alt="" />
      </div>
      <div>
        <Button type="primary" v-if="['draft'].includes(itemCustomInfo.status)" @click="releaseFlow">
          <Icon type="ios-paper-plane-outline" size="16"></Icon>
          {{ $t('release_flow') }}
        </Button>
        <Button type="success" v-if="['draft', 'deployed', 'disabled'].includes(itemCustomInfo.status)">
          <Icon type="ios-download-outline" size="16"></Icon>
          {{ $t('export') }}
        </Button>
        <Button type="info" v-if="['draft', 'deployed'].includes(itemCustomInfo.status)" @click="changePermission">
          <Icon type="ios-person-outline" size="16"></Icon>
          {{ $t('config_permission') }}
        </Button>
        <Button
          type="error"
          v-if="['draft'].includes(itemCustomInfo.status)"
          @click="changeStatus('deleted', 'delete')"
        >
          <Icon type="ios-trash-outline" size="16"></Icon>
          {{ $t('delete') }}
        </Button>
        <Button
          type="error"
          v-if="['deployed'].includes(itemCustomInfo.status)"
          @click="changeStatus('disabled', 'disable')"
        >
          <img src="../../../assets/icon/disable.png" style="width: 16px; vertical-align: middle" alt="" />
          {{ $t('disable') }}
        </Button>
        <Button
          type="success"
          v-if="['disabled'].includes(itemCustomInfo.status)"
          @click="changeStatus('enabled', 'enable')"
        >
          <img src="../../../assets/icon/enable.png" style="width: 16px; vertical-align: middle" alt="" />
          {{ $t('enable') }}
        </Button>
      </div>
    </div>

    <FlowAuth ref="flowAuthRef" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import FlowAuth from '@/pages/collaboration/flow/flow-auth.vue'
import { flowStatusChange, flowRelease } from '@/api/server.js'
export default {
  components: {
    FlowAuth
  },
  data () {
    return {
      itemCustomInfo: {}
    }
  },
  methods: {
    showItemInfo (data) {
      const defaultNode = {
        id: '',
        label: '', // 编排名称
        version: 1, // 版本
        rootEntity: '', // 操作对象
        scene: '', // 使用场景，请求、发布、其他
        authPlugins: [], // 授权插件列表，taskman/monitor
        tags: '', // 标签
        conflictCheck: true, // 冲突检测
        permissionToRole: {
          MGMT: [], // 属主角色
          USE: [] // 使用角色
        }
      }
      const tmpData = JSON.parse(JSON.stringify(data))
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(defaultNode)
      keys.forEach(k => {
        this.itemCustomInfo[k] = tmpData[k]
      })
      this.itemCustomInfo.version = Number(this.itemCustomInfo.version) || 1
    },
    // #region
    // 修改权限
    changePermission () {
      this.$refs.flowAuthRef.startAuth(
        this.itemCustomInfo.permissionToRole.MGMT,
        this.itemCustomInfo.permissionToRole.USE
      )
    },
    updateAuth (mgmt, use) {
      this.$emit('updateAuth', mgmt, use)
    },
    // #endregion
    openCanvasPanel () {
      this.$emit('openCanvasPanel', '')
    },
    // 发布编排
    async releaseFlow () {
      const { status } = await flowRelease(this.itemCustomInfo.id)
      if (status === 'OK') {
        this.$Message.success(this.$t('release_flow') + this.$t('action_successful'))
        this.$emit('updateFlowData', '')
      }
    },
    async changeStatus (statusCode, actionTip) {
      const params = {
        procDefIds: [this.itemCustomInfo.id],
        status: statusCode
      }
      const { status } = await flowStatusChange(params)
      if (status === 'OK') {
        this.$Message.success(this.$t(actionTip) + this.$t('action_successful'))
        this.$emit('updateFlowData', '')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.flow-name {
  line-height: 32px;
  cursor: pointer;
}
</style>
