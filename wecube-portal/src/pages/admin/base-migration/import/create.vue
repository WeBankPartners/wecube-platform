<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-import-create">
      <div class="steps">
        <BaseHeaderTitle title="导出步骤" :showExpand="false">
          <Steps :current="activeStep" direction="vertical">
            <Step title="输入链接,确认产品、环境" content="系统自动分析需要导出的系统及数据"></Step>
            <Step title="导入数据" content="确认依赖系统、CMDB、编排、ITSM等配置项正确"></Step>
            <Step title="执行自动化编排" content="执行编排"></Step>
            <Step title="配置监控" content="自动配置监控"></Step>
          </Steps>
        </BaseHeaderTitle>
      </div>
      <div class="content" ref="scrollView">
        <BaseHeaderTitle v-if="activeStep === 0" title="导入产品" :showExpand="false">
          <StepOne @nextStep="activeStep++"></StepOne>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 1" title="导入数据" :showExpand="false">
          <StepTwo :detailData="detailData" @nextStep="activeStep++"></StepTwo>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 2" title="执行自动化编排" :showExpand="false">
          <StepThree></StepThree>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 3" title="配置监控" :showExpand="false">
          <StepFour></StepFour>
        </BaseHeaderTitle>
      </div>
    </div>
    <Spin v-if="loading" size="large" fix></Spin>
  </Card>
</template>

<script>
import StepOne from './components/step-one.vue'
import StepTwo from './components/step-two.vue'
import StepThree from './components/step-three.vue'
import StepFour from './components/step-four.vue'
import { getExportDetail } from '@/api/server'
export default {
  components: {
    StepOne,
    StepTwo,
    StepThree,
    StepFour
  },
  data() {
    return {
      activeStep: 0,
      loading: false,
      detailData: {}
    }
  },
  async mounted() {
    await this.getDetailData()
  },
  methods: {
    // 获取导出详情数据
    async getDetailData() {
      const params = {
        params: {
          transExportId: 'tp_622c473d18d6219bdbac5'
        }
      }
      const { status, data } = await getExportDetail(params)
      if (status === 'OK') {
        this.detailData = {
          roleRes: data.roles,
          flowRes: data.workflows,
          batchRes: data.batchExecutions,
          itsmRes: data.requestTemplates,
          failMsg: data.createAndUploadFile && data.createAndUploadFile.errMsg,
          cmdbCIData: data.cmdbCI || [], // cmdb CI
          cmdbViewData: data.cmdbView || [], // cmdb视图
          cmdbReportData: data.cmdbReportForm || [], // cmdb报表
          artifactsData: data.artifacts || [], // 物料包
          monitorData: data.monitor || [], // 监控
          pluginsData: data.plugins || [], // 插件
          cmdbReportFormCount: data.cmdbReportFormCount || 0,
          cmdbViewCount: data.cmdbViewCount || 0,
          exportComponentLibrary: data.exportComponentLibrary, // 组件库
          ...data.transExport
        }
        this.detailData.roleRes.data = this.detailData.roleRes.data || []
        this.detailData.flowRes.data = this.detailData.flowRes.data || []
        this.detailData.batchRes.data = this.detailData.batchRes.data || []
        this.detailData.itsmRes.data = this.detailData.itsmRes.data || []
        this.detailData.associationSystems = this.detailData.associationSystems || []
        this.detailData.associationTechProducts = this.detailData.associationTechProducts || []
        this.detailData.businessName = this.detailData.businessName || ''
        this.detailData.businessNameList = (this.detailData.businessName && this.detailData.businessName.split(',')) || []
        this.detailData.business = this.detailData.business || ''
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.monitorCount = this.detailData.monitorData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.artifactsCount = this.detailData.artifactsData.reduce((sum, cur) => sum + cur.artifactLen, 0)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-create {
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
  }
  ::-webkit-scrollbar {
    width: 6px;
  }
  ::-webkit-scrollbar-track {
    background: transparent;
  }
  ::-webkit-scrollbar-thumb {
    background: #d4d4d4;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: #d4d4d4;
  }
}
</style>
