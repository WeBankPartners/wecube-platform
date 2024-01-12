<template>
  <div style="display: flex; justify-content: space-between">
    <div class="flow-name">
      041_test_flow_normal
      <Tag>v1</Tag>
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
      <Button type="info">
        <Icon type="ios-person-outline" size="16"></Icon>
        {{ $t('config_permission') }}
      </Button>
      <Button type="error">
        <Icon type="ios-trash-outline" size="16"></Icon>
        {{ $t('delete') }}
      </Button>
    </div>
  </div>
</template>

<script>
import FilterRules from '@/pages/components/filter-rules.vue'
import FlowAuth from '@/pages/collaboration/flow/flow-auth.vue'
import { getAllFlow, getAllDataModels } from '@/api/server.js'
export default {
  components: {
    FilterRules,
    FlowAuth
  },
  data () {
    return {
      isAdd: false, // 是否为新增流程
      selectedFlow: '', // 当前编辑中的流程
      allFlows: [], // 流程列表
      currentFlow: {
        tags: ''
      },
      currentSelectedEntity: '', // 当前显示的根CI
      allEntityType: [], // 系统中所有根CI
      mgmtRolesKeyToFlow: [],
      useRolesKeyToFlow: []
    }
  },
  mounted () {
    // 获取所有根CI类型
    this.getAllDataModels()
  },
  watch: {
    selectedFlow: {
      handler (val, oldVal) {
        // this.isFormDataChange = false
        const flowInfo = this.allFlows.find(flow => flow.procDefId === val)
        this.currentSelectedEntity = flowInfo.rootEntity
        // this.show = false
        // this.selectedFlowData = {}
        if (val) {
          // this.selectedFlowData =
          //   this.allFlows.find(_ => {
          //     return _.procDefId === val
          //   }) || {}
          // this.getFlowXml(val)
          // this.getPermissionByProcess(val)
          // this.pluginForm.paramInfos = []
          // this.currentflowsNodes = []
        }
      }
    }
  },
  methods: {
    async getAllFlows (s) {
      if (s) {
        const { data, status } = await getAllFlow()
        if (status === 'OK') {
          this.allFlows = data
        }
      }
    },
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    onEntitySelect (v) {
      this.currentSelectedEntity = v || ''
      // if (this.currentSelectedEntity.split('{')[0] !== this.pluginForm.routineExpression.split('{')[0]) {
      //   if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = []
      //   this.pluginForm = {
      //     ...this.defaultPluginForm,
      //     routineExpression: v
      //   }
      //   this.resetNodePluginConfig()
      // }
    },
    clearFlow () {
      this.currentSelectedEntity = ''
      this.currentFlow.tags = ''
    },
    // 创建新编排
    createNewFlow () {
      // 1、清理现有配置

      // 2、初始化部分新配置
      // 3、启动权限配置
      this.$refs.flowAuthRef.startAuth(this.mgmtRolesKeyToFlow, this.useRolesKeyToFlow)
    },
    setAuth (mgmtRolesKeyToFlow, useRolesKeyToFlow) {
      this.mgmtRolesKeyToFlow = mgmtRolesKeyToFlow
      this.useRolesKeyToFlow = useRolesKeyToFlow
      this.$emit('canLoadGraph')
    }
  }
}
</script>

<style scoped lang="scss">
.flow-name {
  line-height: 32px;
}
</style>
