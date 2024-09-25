<template>
  <div class="base-migration-import-two">
    <ImportData v-if="stepData.id" :detailData="stepData" from="import"></ImportData>
    <div class="footer">
      <Button v-if="['doing', 'fail'].includes(stepData.status)" type="error" @click="handleStop">终止</Button>
      <Button v-if="['fail'].includes(stepData.status)" type="warning" @click="handleRetry">重试</Button>
      <Button type="default" @click="handleLast">上一步</Button>
      <Button v-if="['success'].includes(stepData.status)" type="primary" @click="handleNext">下一步</Button>
    </div>
  </div>
</template>

<script>
import ImportData from '../../export/components/step-result.vue'
export default {
  components: {
    ImportData
  },
  props: {
    detailData: Object
  },
  data() {
    return {
      stepData: {}
    }
  },
  mounted() {
    this.stepData = {
      ...this.detailData,
      status: 'doing'
    }
    // 前端遍历所有导出数据，判断第二步单独导入状态
    const {
      artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes
    } = this.detailData
    const importData = [artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes]
    const success = importData.every(i => i.status === 'success')
    const fail = importData.some(i => i.status === 'fail')
    if (success) {
      this.stepData.status = 'success'
    }
    if (fail) {
      this.stepData.status = 'fail'
    }
  },
  methods: {
    handleStop() {},
    handleRetry() {},
    handleLast() {
      this.$emit('lastStep')
    },
    handleNext() {
      this.$emit('nextStep')
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-two {
  .footer {
    position: fixed;
    bottom: 10px;
    display: flex;
    justify-content: center;
    width: calc(100% - 460px);
    button {
      &:not(:first-child) {
        margin-left: 10px;
      }
    }
  }
}
</style>
