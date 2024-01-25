<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem"></div>
    <div class="panal-name">连接属性：</div>
    <Form :label-width="120" style="padding-right: 12px">
      <template>
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem :label="$t('name')">
          <Input v-model="itemCustomInfo.label" @on-change="paramsChanged"></Input>
        </FormItem>
        <div style="position: absolute; bottom: 20px; right: 280px; width: 200px">
          <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
          <Button @click="hideItem">{{ $t('cancel') }}</Button>
        </div>
      </template>
    </Form>
  </div>
</template>
<script>
export default {
  data () {
    return {
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      needAddFirst: true,
      itemCustomInfo: {
        // id: '',
        // label: ''
      }
    }
  },
  methods: {
    showItemInfo (data, needAddFirst = false) {
      this.needAddFirst = needAddFirst
      delete data.sourceNode
      delete data.targetNode
      this.isParmasChanged = false
      this.itemCustomInfo = JSON.parse(JSON.stringify(data))
      if (needAddFirst) {
        this.saveItem()
      }
    },
    saveItem () {
      let tmpData = JSON.parse(JSON.stringify(this.itemCustomInfo))
      tmpData.name = tmpData.label

      let finalData = {
        customAttrs: {
          id: tmpData.id,
          name: tmpData.name,
          source: tmpData.source,
          target: tmpData.target
        },
        selfAttrs: tmpData
      }
      this.$emit('sendItemInfo', finalData, this.needAddFirst)
      this.needAddFirst = false
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: this.$t('confirm_discarding_changes') + 'edge',
          content: this.$t('params_edit_confirm'),
          'z-index': 1000000,
          onOk: async () => {
            this.$emit('hideItemInfo')
          },
          onCancel: () => {}
        })
      } else {
        this.$emit('hideItemInfo')
      }
    },
    // 监听参数变化
    paramsChanged () {
      this.isParmasChanged = true
    }
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: absolute;
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
  overflow: auto;
  height: calc(100vh - 160px);
}
.ivu-form-item {
  margin-bottom: 12px;
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
