<template>
  <div id="itemInfo">
    <Form :label-width="80">
      canvas
      <FormItem label="ID">
        <Input disabled v-model="itemCustomInfo.id"></Input>
      </FormItem>
      <FormItem :label="$t('name')">
        <Input v-model="itemCustomInfo.label"></Input>
      </FormItem>
      <Button @click="saveItem" type="primary" style="float: right">保存</Button>
    </Form>
  </div>
</template>
<script>
export default {
  data () {
    return {
      itemCustomInfo: {
        id: '',
        label: ''
      }
    }
  },
  methods: {
    showItemInfo (data) {
      this.itemCustomInfo = data
    },
    saveItem () {
      this.$emit('sendItemInfo', this.itemCustomInfo)
    }
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: fixed;
  top: 145px;
  right: 32px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 86%;
  background: #f8f8f8;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
}
.ivu-form-item {
  margin-bottom: 0;
}
</style>
