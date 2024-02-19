<template>
  <div class="batch-execution-dangerous-modal">
    <!--高危检测弹框-->
    <Modal v-model="visible" width="900">
      <div>
        <Icon :size="28" :color="'#f90'" type="md-help-circle" />
        <span class="confirm-msg">{{ $t('confirm_to_exect') }}</span>
      </div>
      <div style="max-height: 400px; overflow-y: auto">
        <pre style="margin-left: 44px; margin-top: 22px">{{ data.message }}</pre>
      </div>
      <div slot="footer">
        <span style="margin-left: 30px; color: #ed4014; float: left; text-align: left">
          <Checkbox v-model="checked">{{ $t('dangerous_confirm_tip') }}</Checkbox>
        </span>
        <Button type="text" @click="$emit('update:visible', false)">{{ $t('bc_cancel') }}</Button>
        <Button type="warning" :disabled="!checked" @click="confirmDangerous">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import { saveBatchExecute } from '@/api/server'
export default {
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    data: {
      type: Object,
      default: () => {}
    }
  },
  data () {
    return {
      checked: false
    }
  },
  methods: {
    // 提交高危检测
    async confirmDangerous () {
      this.$Spin.show()
      const { status } = await saveBatchExecute(
        `/platform/v1/batch-execution/job/run?continueToken=${this.data.continueToken}`,
        this.data.params
      )
      this.$Spin.hide()
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped></style>
