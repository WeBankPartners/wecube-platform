<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem">
      <Icon type="ios-arrow-dropright" size="28" />
    </div>
    <div class="panal-name">{{ $t('edgeProperties') }}：</div>
    <Form :label-width="120" style="padding-right: 12px">
      <template>
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem :label="$t('name')">
          <Input v-model="itemCustomInfo.name" @on-change="paramsChanged"></Input>
          <span style="position: absolute; left: 320px; top: 2px; line-height: 30px; background: white"
            >{{ (itemCustomInfo.name && itemCustomInfo.name.length) || 0 }}/30</span
          >
          <span v-if="itemCustomInfo.name && itemCustomInfo.name.length > 30" style="color: red"
            >{{ $t('name') }}{{ $t('cannotExceed') }} 30 {{ $t('characters') }}</span
          >
        </FormItem>
        <div style="position: absolute; bottom: 20px; right: 280px; width: 200px">
          <Button v-if="editFlow !== 'false'" :disabled="isSaveBtnActive()" @click="saveItem" type="primary">{{
            $t('save')
          }}</Button>
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
      editFlow: true, // 在查看时隐藏按钮
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      needAddFirst: true,
      itemCustomInfo: {
        // id: '',
        // label: ''
      }
    }
  },
  methods: {
    showItemInfo (data, needAddFirst = false, editFlow) {
      this.editFlow = editFlow
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
      tmpData.label = tmpData.name
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
    isSaveBtnActive () {
      let res = false
      if (this.itemCustomInfo.name && this.itemCustomInfo.name.length > 30) {
        res = true
      }
      return res
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: `${this.$t('confirm_discarding_changes')}`,
          content: `${this.itemCustomInfo.name}:${this.$t('params_edit_confirm')}`,
          'z-index': 1000000,
          okText: this.$t('save'),
          cancelText: this.$t('abandon'),
          onOk: async () => {
            this.saveItem()
          },
          onCancel: () => {
            this.$emit('hideItemInfo')
          }
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
  top: 134px;
  right: 13px;
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
  position: fixed;
  top: 400px;
  right: 500px;
  color: #2db7f5;
  cursor: pointer;
}
</style>
