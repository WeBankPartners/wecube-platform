<template>
  <div class="base-migration-export-create">
    <div class="steps">
      <BaseHeaderTitle title="导出步骤">
        <Steps :current="activeStep" direction="vertical">
          <Step title="选择产品、环境" content="系统自动分析需要导出的系统及数据"></Step>
          <Step title="选择数据,执行导出" content="确认依赖系统、CMDB、编排、ITSM等配置项正确"></Step>
          <Step title="确认导出结果" content="查看导出数据对比"></Step>
        </Steps>
      </BaseHeaderTitle>
    </div>
    <div class="content">
      <BaseHeaderTitle v-show="activeStep === 0" title="导出产品">
        <stepEnviroment ref="env"></stepEnviroment>
      </BaseHeaderTitle>
      <BaseHeaderTitle v-if="activeStep === 1" title="导出数据">
        <stepSelectData ref="data"></stepSelectData>
      </BaseHeaderTitle>
      <BaseHeaderTitle v-if="activeStep === 2" title="导出结果">
        <stepResult :id="id"></stepResult>
      </BaseHeaderTitle>
      <div class="footer">
        <template v-if="activeStep === 0">
          <Button type="info" @click="handleSaveEnvBusiness">下一步</Button>
        </template>
        <template v-else-if="activeStep === 1">
          <Button type="default" @click="handleLast">上一步</Button>
          <Button type="primary" :loading="loading" @click="handleSaveExport" style="margin-left: 10px">执行导出</Button>
        </template>
        <template v-else-if="activeStep === 2">
          <Button type="default" @click="handleToHistory" style="margin-left: 10px">历史列表</Button>
          <Button type="primary" @click="handleReLauch" style="margin-left: 10px">重新发起</Button>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import stepEnviroment from './components/step-enviroment.vue'
import stepSelectData from './components/step-select-data.vue'
import stepResult from './components/step-result.vue'
import { debounce } from '@/const/util'
import {
  saveEnvBusiness,
  exportBaseMigration,
} from '@/api/server'
export default {
  components: {
    stepEnviroment,
    stepSelectData,
    stepResult
  },
  data() {
    return {
      id: '',
      activeStep: 0,
      status: '', // 导出状态start草稿、doing执行中、success成功、fail失败
      loading: false
    }
  },
  mounted() {
    this.id = this.$route.query.id || ''
    this.status = this.$route.query.status || ''
    if (this.status === 'start') {
      this.activeStep = 1
    } else if (['doing', 'success', 'fail'].includes(this.status)) {
      this.activeStep = 2
    }
  },
  methods: {
    // 保存环境和产品
    handleSaveEnvBusiness: debounce(async function() {
      const { env, selectionList } = this.$refs.env
      const pIds = selectionList && selectionList.map(item => item.id) || []
      if (pIds.length === 0) {
        return this.$Message.warning('至少勾选一条产品数据')
      }
      this.loading = true
      const { status } = await saveEnvBusiness({pIds, env})
      this.loading = false
      if (status === 'OK') {
        this.activeStep++
      }
    }, 500),
    // 上一步
    handleLast() {
      this.activeStep--
    },
    // 执行导出
    handleSaveExport: debounce(async function() {
      const { roleSelectionList, itsmSelectionList, flowSelectionList, batchSelectionList } = this.$refs.data
      const roleArr = roleSelectionList && roleSelectionList.map(item => item.name) || []
      const itsmArr = itsmSelectionList && itsmSelectionList.map(item => item.id) || []
      const flowArr = flowSelectionList && flowSelectionList.map(item => item.id) || []
      const batchArr = batchSelectionList && batchSelectionList.map(item => item.id) || []
      const params = {
        transExportId: this.id,
        roles: roleArr,
        workflowIds: flowArr,
        batchExecutionIds: batchArr,
        requestTemplateIds: itsmArr
      }
      const { status } = await exportBaseMigration(params)
      if (status === 'OK') {
        this.activeStep++
      }
    }, 500),
    // 跳转到历史列表
    handleToHistory() {},
    // 重新发起
    handleReLauch() {}
  }
}
</script>

<style lang="scss" scoped>
.base-migration-export-create {
  display: flex;
  height: calc(100vh - 100px);
  overflow: hidden;
  .steps {
    width: 260px;
    padding-right: 15px;
    border-right: 1px solid #e8eaec;
    height: 100%;
  }
  .content {
    width: calc(100% - 260px);
    padding-left: 15px;
    overflow-y: auto;
    padding-bottom: 60px;
    .footer {
      position: fixed;
      bottom: 10px;
      display: flex;
      justify-content: center;
      width: calc(100% - 460px);
    }
  }
}
</style>
