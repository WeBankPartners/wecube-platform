<template>
  <div>
    <div style="display: flex; justify-content: space-between">
      <div class="flow-name" @click="openCanvasPanel">
        {{ itemCustomInfo.name }}
        <Tag>v{{ itemCustomInfo.version }}</Tag>
        <Icon type="ios-nutrition"></Icon>
      </div>
      <div>
        <Button type="primary">
          <Icon type="ios-paper-plane-outline" size="16"></Icon>
          {{ $t('release_flow') }}
        </Button>
        <Button type="success">
          <Icon type="ios-download-outline" size="16"></Icon>
          {{ $t('export') }}
        </Button>
        <Button type="info" @click="changePermission">
          <Icon type="ios-person-outline" size="16"></Icon>
          {{ $t('config_permission') }}
        </Button>
        <Button type="error">
          <Icon type="ios-trash-outline" size="16"></Icon>
          {{ $t('delete') }}
        </Button>
      </div>
    </div>

    <FlowAuth ref="flowAuthRef"></FlowAuth>
  </div>
</template>

<script>
import FlowAuth from '@/pages/collaboration/flow/flow-auth.vue'
export default {
  components: {
    FlowAuth
  },
  data () {
    return {
      itemCustomInfo: {},
      mgmtRolesKeyToFlow: [],
      useRolesKeyToFlow: []
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
      console.log(12, this.itemCustomInfo)
    },
    // #region
    // 修改权限
    changePermission () {
      console.log(4)
      this.$refs.flowAuthRef.startAuth(
        this.itemCustomInfo.permissionToRole.MGMT,
        this.itemCustomInfo.permissionToRole.USE
      )
    },
    // #endregion
    openCanvasPanel () {
      this.$emit('openCanvasPanel', '')
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
