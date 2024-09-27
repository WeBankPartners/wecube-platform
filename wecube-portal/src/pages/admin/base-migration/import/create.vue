<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-import-create">
      <div class="steps">
        <BaseHeaderTitle title="导入步骤" :showExpand="false">
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
          <StepOne :detailData="detailData" @saveStepOne="handleSaveStepOne" @nextStep="activeStep++"></StepOne>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 1" title="导入数据" :showExpand="false">
          <StepTwo
            :detailData="detailData"
            @saveStepTwo="handleSaveStepTwo"
            @lastStep="activeStep--"
            @nextStep="activeStep++"
          ></StepTwo>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 2" title="执行自动化编排" :showExpand="false">
          <StepThree
            :detailData="detailData"
            @saveStepThree="handleSaveStepThree"
            @lastStep="activeStep--"
            @nextStep="activeStep++"
          ></StepThree>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 3" title="配置监控" :showExpand="false">
          <StepFour :detailData="detailData" @lastStep="activeStep--"></StepFour>
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
import { getImportDetail } from '@/api/server'
export default {
  components: {
    StepOne,
    StepTwo,
    StepThree,
    StepFour
  },
  data() {
    return {
      id: this.$route.query.id || '',
      activeStep: -1,
      loading: false,
      detailData: {}
    }
  },
  async mounted() {
    if (this.id) {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      // 后台返回当前步骤
      this.activeStep = this.detailData.step - 1
    } else {
      this.activeStep = 0
    }
  },
  beforeDestroy() {
    clearInterval(this.interval)
  },
  methods: {
    // 获取导出详情数据
    async getDetailData() {
      const params = {
        params: {
          transImportId: this.id
        }
      }
      const { status, data } = await getImportDetail(params)
      if (status === 'OK') {
        this.detailData = {
          roleRes: data.roles || {},
          flowRes: data.workflows || {},
          batchRes: data.batchExecutions || {},
          itsmRes: data.requestTemplates || {},
          cmdbRes: data.cmdb || {}, // cmdb
          cmdbCIData: data.cmdbCI || [], // cmdb CI
          cmdbViewData: data.cmdbView || [], // cmdb视图
          cmdbReportData: data.cmdbReportForm || [], // cmdb报表
          cmdbReportFormCount: data.cmdbReportFormCount || 0,
          cmdbViewCount: data.cmdbViewCount || 0,
          artifactsRes: data.artifacts || {}, // 物料包
          monitorRes: data.monitorBase || {}, // 监控
          pluginsRes: data.plugins || {}, // 插件
          initWorkflowRes: data.initWorkflow || {},
          monitorBusinessRes: data.monitorBusiness || {},
          exportComponentLibrary: data.exportComponentLibrary, // 组件库
          step: data.step,
          ...data.transExport
        }
        this.detailData.roleRes.data = this.detailData.roleRes.data || []
        this.detailData.flowRes.data = this.detailData.flowRes.data || []
        this.detailData.batchRes.data = this.detailData.batchRes.data || []
        this.detailData.itsmRes.data = this.detailData.itsmRes.data || []
        this.detailData.artifactsRes.data = this.detailData.artifactsRes.data || []
        this.detailData.monitorRes.data = this.detailData.monitorRes.data || []
        this.detailData.pluginsRes.data = this.detailData.pluginsRes.data || []
        this.detailData.initWorkflowRes.data = this.detailData.initWorkflowRes.data || []
        this.detailData.monitorBusinessRes.data = this.detailData.monitorBusinessRes.data || []
        this.detailData.associationSystems = this.detailData.associationSystems || []
        this.detailData.associationTechProducts = this.detailData.associationTechProducts || []
        this.detailData.businessName = this.detailData.businessName || ''
        this.detailData.businessNameList = (this.detailData.businessName && this.detailData.businessName.split(',')) || []
        this.detailData.business = this.detailData.business || ''
        // 统计数量
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.artifactsCount = this.detailData.artifactsRes.data.reduce(
          (sum, cur) => sum + cur.artifactLen,
          0
        )
        // 合并monitor数据
        const metric_list_obj = {
          name: 'metric_list',
          count: 0
        }
        const strategy_list_obj = {
          name: 'strategy_list',
          count: 0
        }
        this.detailData.monitorRes.data.forEach(i => {
          if (
            ['custom_metric_service_group', 'custom_metric_endpoint_group', 'custom_metric_monitor_type'].includes(
              i.name
            )
          ) {
            metric_list_obj.count += i.count
          }
          if (['strategy_service_group', 'strategy_endpoint_group'].includes(i.name)) {
            strategy_list_obj.count += i.count
          }
        })
        const metricIndex = this.detailData.monitorRes.data.findIndex(i => i.name === 'custom_metric_service_group')
        const strategyIndex = this.detailData.monitorRes.data.findIndex(i => i.name === 'strategy_service_group')
        this.detailData.monitorRes.data.splice(metricIndex, 0, metric_list_obj)
        this.detailData.monitorRes.data.splice(strategyIndex, 0, strategy_list_obj)
        this.detailData.monitorRes.data = this.detailData.monitorRes.data.filter(
          i =>
            ![
              'strategy_service_group',
              'strategy_endpoint_group',
              'custom_metric_service_group',
              'custom_metric_endpoint_group',
              'custom_metric_monitor_type'
            ].includes(i.name)
        )
        // 第二步导入状态判断
        const {
          artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes
        } = this.detailData
        const stepTwoData = [artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes]
        const success = stepTwoData.every(i => i.status === 'success')
        const fail = stepTwoData.some(i => i.status === 'fail')
        const failObj = stepTwoData.find(i => i.status === 'fail') || {}
        this.detailData.stepTwoRes = {
          status: 'doing',
          errMsg: failObj.errMsg
        }
        if (success) {
          this.detailData.stepTwoRes.status = 'success'
        }
        if (fail) {
          this.detailData.stepTwoRes.status = 'fail'
        }
        if (this.detailData.step === 2) {
          if (this.detailData.stepTwoRes.status === 'doing') {
            this.interval = setInterval(() => {
              this.getDetailData()
            }, 30 * 1000)
          } else {
            clearInterval(this.interval)
          }
        }
        // 第三步导入状态判断
        if (this.detailData.step === 3) {
          if (!['success', 'fail'].includes(this.detailData.initWorkflowRes.status)) {
            this.interval = setInterval(() => {
              this.getDetailData()
            }, 30 * 1000)
          } else {
            clearInterval(this.interval)
          }
        }
        // 第四步导入状态判断
        if (this.detailData.step === 4) {
          if (!['success', 'fail'].includes(this.detailData.monitorBusinessRes.status)) {
            this.interval = setInterval(() => {
              this.getDetailData()
            }, 30 * 1000)
          } else {
            clearInterval(this.interval)
          }
        }
      }
    },
    async handleSaveStepOne(id) {
      this.id = id
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    async handleSaveStepTwo() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    async handleSaveStepThree () {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
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
