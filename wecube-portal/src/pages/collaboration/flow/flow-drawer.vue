<template>
  <Drawer title="配置" :closable="false" v-model="isDrawerOpen" :mask-closable="false" :width="500">
    <Form :label-width="80">
      {{ currentType }}
      <template v-if="currentType === 'flow'">
        <FormItem label="id">
          <Input v-model="flowInfo.id"></Input>
        </FormItem>
        <FormItem label="label">
          <Input v-model="flowInfo.label"></Input>
        </FormItem>
      </template>
      <template v-else-if="currentType === 'node'">
        <FormItem label="id">
          <Input v-model="nodeInfo.id"></Input>
        </FormItem>
        <FormItem label="label">
          <Input v-model="nodeInfo.label"></Input>
        </FormItem>
      </template>
    </Form>
    <Button @click="isDrawerOpen = false" style="float: right" type="primary">{{ $t('save') }}</Button>
  </Drawer>
</template>
<script>
export default {
  data () {
    return {
      isDrawerOpen: false,
      currentType: '', // flow、node、edge
      flowInfo: {
        id: '',
        label: ''
      },
      nodeInfo: {}
    }
  },
  methods: {
    openDrawer (type, data) {
      this.currentType = type
      if (type === 'flow') {
        this.flowInfo = data
      }
      if (type === 'node') {
        this.nodeInfo = data
      }
      this.isDrawerOpen = true
    },
    closeDrawer () {
      this.isDrawerOpen = false
    }
  }
}
</script>
<style lang="scss" scoped>
.ivu-form-item {
  margin-bottom: 0;
}
</style>
