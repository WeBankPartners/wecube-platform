<template>
  <div class="base-migration-import-two">
    <ImportData v-if="detailData.id" :detailData="detailData"></ImportData>
    <div class="footer">
      <Button v-if="['doing', 'fail'].includes(detailData.status)" type="default" @click="handleStop">终止</Button>
      <Button v-if="['fail'].includes(detailData.status)" type="default" @click="handleRetry">重试</Button>
      <Button type="default" @click="handleLast">上一步</Button>
      <Button v-if="['success'].includes(detailData.status)" type="primary" @click="handleNext">下一步</Button>
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
    return {}
  },
  methods: {
    handleStop() {},
    handleRetry() {},
    handleLast() {},
    handleNext() {}
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

