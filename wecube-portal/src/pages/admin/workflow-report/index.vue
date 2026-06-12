<template>
  <div class="">
    <Tabs value="workflowAspect">
      <TabPane :label="$t('workflow_report_aspect')" name="workflowAspect">
        <WorkflowAspect></WorkflowAspect>
      </TabPane>
      <TabPane :label="$t('workflow_task_node_aspect')" name="tasknodeAspect">
        <TasknodeAspect></TasknodeAspect>
      </TabPane>
      <TabPane :label="$t('pluginService')" name="pluginAspect">
        <PluginAspect></PluginAspect>
      </TabPane>
    </Tabs>
  </div>
</template>

<script>
/**
 * 工作流报表主页面组件
 * 通过标签页展示三种报表维度：工作流维度、任务节点维度、插件服务维度
 */
import WorkflowAspect from './workflow-aspect'
import PluginAspect from './plugin-aspect'
import TasknodeAspect from './tasknode-aspect'
export default {
  name: '',
  data() {
    return {}
  },
  methods: {},
  components: {
    WorkflowAspect,
    PluginAspect,
    TasknodeAspect
  }
}
</script>

<style scoped lang="scss"></style>
