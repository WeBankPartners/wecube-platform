<template>
  <div id="itemInfo">
    <Form :label-width="80">
      {{ currentType }}
      <template>
        <FormItem label="id">
          <Input disabled v-model="itemInfo.id"></Input>
        </FormItem>
        <FormItem label="label">
          <Input v-model="itemInfo.label"></Input>
        </FormItem>
      </template>
      <Button @click="saveItem">保存</Button>
    </Form>
  </div>
</template>
<script>
export default {
  data () {
    return {
      currentType: '', // flow、node、edge
      itemInfo: {
        id: '',
        label: ''
      }
    }
  },
  methods: {
    showItemInfo (type, data) {
      this.currentType = type
      this.itemInfo = data
    },
    saveItem () {
      this.$emit('sendItemInfo', this.currentType, this.itemInfo)
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
  width: 300px;
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
