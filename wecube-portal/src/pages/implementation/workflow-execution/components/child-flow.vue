<template>
  <div class="workflow-execution-child-flow">
    <!--子编排实例信息-->
    <Form :label-width="90">
      <FormItem :label="$t('child_workflow')">
        <Select v-model="nodeInstance.subProcDefId">
          <Option v-for="item in subProcList" :value="item.procDefId" :key="item.procDefId">{{
            `${item.procDefName}【${item.procDefVersion}】`
          }}</Option>
        </Select>
        <span v-if="subProcRemoveFlag" style="color: red">{{ $t('fe_childFlow_permissionTips') }}</span>
      </FormItem>
      <template v-if="nodeInstance.subProcDefId && subProcItem">
        <FormItem :label="$t('child_flowId')">
          <span>{{ subProcItem.procDefId || nodeInstance.subProcDefId || '-' }}</span>
          <Button :disabled="subProcRemoveFlag" type="info" size="small" @click="viewParentFlowGraph">{{
            $t('view_workFlow')
          }}</Button>
        </FormItem>
        <FormItem :label="$t('instance_type')">
          <span>{{ subProcItem.rootEntity || '-' }}</span>
        </FormItem>
        <FormItem :label="$t('createdBy')">
          <span>{{ subProcItem.createUser || '-' }}</span>
        </FormItem>
        <FormItem :label="$t('updatedBy')">
          <span>{{ subProcItem.updateUser || '-' }}</span>
        </FormItem>
        <FormItem :label="$t('table_updated_date')">
          <span>{{ subProcItem.updatedTime || '-' }}</span>
        </FormItem>
      </template>
    </Form>
  </div>
</template>

<script>
import { getChildFlowListNew } from '@/api/server'
export default {
  props: {
    nodeInstance: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      subProcList: [],
      subProcItem: {},
      subProcRemoveFlag: false
    }
  },
  mounted() {
    this.getSubProcList()
  },
  methods: {
    // 获取子编排列表
    async getSubProcList() {
      const params = {
        entityExpr: this.nodeInstance.routineExpression
      }
      const { status, data } = await getChildFlowListNew(params)
      if (status === 'OK') {
        this.subProcRemoveFlag = false
        this.subProcList = data || []
        this.subProcItem = this.subProcList.find(i => i.procDefId === this.nodeInstance.subProcDefId) || {}
        this.$emit('getSubProcItem', this.subProcItem)
        // 查看编排，匹配不到对应数据，给出提示
        if (!this.subProcItem.procDefId) {
          this.subProcRemoveFlag = true
        }
      }
    },
    viewParentFlowGraph() {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/collaboration/workflow-mgmt?flowId=${this.nodeInstance.subProcDefId}&editFlow=false&flowListTab=deployed`
      window.open(path, '_blank')
    }
  }
}
</script>

<style lang="scss">
.workflow-execution-child-flow {
  .ivu-form-item-label {
    padding: 5px 12px 0px 0px !important;
  }
  label {
    margin-bottom: 0px;
    margin-top: 5px;
  }
}
</style>