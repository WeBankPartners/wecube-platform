<template>
  <div class="base-migration-import-two">
    <ImportData v-if="detailData.id" :detailData="detailData" from="import"></ImportData>
    <div class="footer">
      <Button v-if="['doing', 'fail'].includes(detailData.status)" type="error" @click="handleStop">终止</Button>
      <Button v-if="['fail'].includes(detailData.status)" type="warning" @click="handleRetry">重试</Button>
      <Button type="default" @click="handleLast">上一步</Button>
      <Button v-if="['success'].includes(detailData.stepTwoRes.status)" type="primary" @click="handleSave">下一步</Button>
    </div>
  </div>
</template>

<script>
import ImportData from '../../export/components/step-result.vue'
import { saveImportData } from '@/api/server'
import { debounce } from '@/const/util'
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
  mounted() {
    // this.stepData = {
    //   ...this.detailData,
    //   status: 'doing'
    // }
    // const {
    //   artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes
    // } = this.detailData
    // const importData = [artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes]
    // const success = importData.every(i => i.status === 'success')
    // const fail = importData.some(i => i.status === 'fail')
    // if (success) {
    //   this.stepData.status = 'success'
    // }
    // if (fail) {
    //   this.stepData.status = 'fail'
    // }
  },
  methods: {
    handleStop() {},
    handleRetry() {},
    handleLast() {
      this.$emit('lastStep')
    },
    handleSave: debounce(async function () {
      if (this.detailData.step > 2) {
        this.$emit('nextStep')
      } else {
        const params = {
          transImportId: this.detailData.id,
          step: 3
        }
        const { status } = await saveImportData(params)
        if (status === 'OK') {
          // 执行导入，生成ID
          this.$emit('saveStepTwo')
        }
      }
    }, 500)
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
