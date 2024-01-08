<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem"></div>
    <div class="panal-name">连接属性：</div>
    <Form :label-width="80">
      <template>
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem :label="$t('name')">
          <Input v-model="itemCustomInfo.label"></Input>
        </FormItem>
      </template>
    </Form>
    <div style="position: fixed; bottom: 20px; right: 400px">
      <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
      <Button @click="hideItem">{{ $t('cancel') }}</Button>
    </div>
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
      this.itemCustomInfo.id = data.id
      this.itemCustomInfo.label = data.label
    },
    saveItem () {
      this.$emit('sendItemInfo', this.itemCustomInfo)
    },
    hideItem () {
      this.$emit('hideItemInfo')
    }
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: fixed;
  top: 139px;
  right: 32px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 86%;
  background: white;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
}
.ivu-form-item {
  margin-bottom: 0;
}

.panal-name {
  padding: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eaec;
  font-weight: bold;
}

.hide-panal {
  width: 12px;
  height: 22px;
  border-radius: 10px 0 0 10px;
  background-color: white;
  border-top: 1px solid #0892ed80;
  border-bottom: 1px solid #0892ed80;
  border-left: 1px solid #0892ed80;
  overflow: hidden;

  position: fixed;
  top: 400px;
  right: 531px;
  cursor: pointer;
  box-shadow: 0 0 8px #0892ed80;
}
</style>
