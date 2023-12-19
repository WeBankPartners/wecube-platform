<template>
  <Drawer title="配置" :closable="false" v-model="isDrawerOpen" :mask-closable="false" :width="500">
    <Form :label-width="80">
      <template>
        <FormItem label="id">
          <Input v-model="flowInfo.id"></Input>
        </FormItem>
        <FormItem label="label">
          <Input v-model="flowInfo.name"></Input>
        </FormItem>
      </template>
    </Form>
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
        name: ''
      },
      nodeInfo: {}
    }
  },
  methods: {
    openDrawer (type, data) {
      if (type === 'flow') {
        this.flowInfo = data
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
