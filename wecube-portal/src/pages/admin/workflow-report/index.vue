<template>
  <div class="">
    <Tabs value="workflowAspect">
      <TabPane :label="$t('workflow_report_aspect')" name="workflowAspect">
        <WorkflowAspect></WorkflowAspect>
      </TabPane>
      <TabPane :label="$t('workflow_task_node_aspect')" name="tasknodeAspect">
        <TasknodeAspect></TasknodeAspect>
      </TabPane>
      <TabPane :label="$t('pluginService ')" name="pluginAspect">
        <PluginAspect></PluginAspect>
      </TabPane>
    </Tabs>
  </div>
</template>

<script>
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
