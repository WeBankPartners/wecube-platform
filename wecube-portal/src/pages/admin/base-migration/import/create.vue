<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-import-create">
      <div class="steps">
        <BaseHeaderTitle title="导入步骤" :showExpand="false">
          <div v-if="statusObj.label && type !== 'republish'" class="status-group">
            <div class="status">
              状态：<Tag type="border" :color="statusObj.color">{{ statusObj.label }}</Tag>
            </div>
            <Button v-if="['doing', 'fail'].includes(detailData.status)" type="error" @click="handleStop">终止</Button>
            <Button v-if="['success', 'exit'].includes(detailData.status)" type="success" @click="handleReLauch">重新发起</Button>
          </div>
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
          <StepFour :detailData="detailData" @saveStepFour="handleSaveStepFour" @lastStep="activeStep--"></StepFour>
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
import { getImportDetail, updateImportStatus } from '@/api/server'
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
      type: this.$route.query.type || '',
      activeStep: -1,
      loading: false,
      detailData: {},
      statusObj: {},
      statusList: [
        {
          label: '执行中',
          value: 'doing',
          color: '#2d8cf0'
        },
        {
          label: '成功',
          value: 'success',
          color: '#19be6b'
        },
        {
          label: '失败',
          value: 'fail',
          color: '#ed4014'
        },
        {
          label: '终止',
          value: 'exit',
          color: '#ff9900'
        }
      ]
    }
  },
  async mounted() {
    // 查看编辑操作
    if (this.id && this.type !== 'republish') {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      // 后台返回当前步骤
      if (this.detailData.status !== 'success') {
        this.activeStep = this.detailData.step - 1
      } else {
        this.activeStep = 3
      }
    }
    // 重新发起
    if (this.id && this.type === 'republish') {
      await this.getDetailData()
      this.activeStep = 0
    }
    // 新建操作
    if (!this.id) {
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
          initWorkflowRes: data.procInstance || {},
          monitorBusinessRes: data.monitorBusiness || {},
          exportComponentLibrary: data.exportComponentLibrary, // 组件库
          step: data.step,
          ...data.transExport
        }
        this.detailData.roleRes.data = this.detailData.roleRes.data || []
        this.detailData.roleRes.title = '角色'
        this.detailData.flowRes.data = this.detailData.flowRes.data || []
        this.detailData.flowRes.title = '编排'
        this.detailData.batchRes.data = this.detailData.batchRes.data || []
        this.detailData.batchRes.title = '批量执行'
        this.detailData.itsmRes.data = this.detailData.itsmRes.data || []
        this.detailData.itsmRes.title = 'ITSM流程'
        this.detailData.artifactsRes.data = this.detailData.artifactsRes.data || []
        this.detailData.artifactsRes.title = '物料包'
        this.detailData.monitorRes.data = this.detailData.monitorRes.data || []
        this.detailData.monitorRes.title = '监控配置'
        this.detailData.pluginsRes.data = this.detailData.pluginsRes.data || []
        this.detailData.pluginsRes.title = '插件服务'
        this.detailData.cmdbRes.title = 'CMDB'
        this.detailData.initWorkflowRes.data = this.detailData.initWorkflowRes.data || []
        this.detailData.monitorBusinessRes.data = this.detailData.monitorBusinessRes.data || []
        this.detailData.associationSystems = (this.detailData.associationSystem && this.detailData.associationSystem.split(',')) || []
        this.detailData.associationTechProducts = (this.detailData.associationProduct && this.detailData.associationProduct.split(',')) || []
        this.detailData.businessName = this.detailData.businessName || ''
        this.detailData.businessNameList = (this.detailData.businessName && this.detailData.businessName.split(',')) || []
        this.detailData.business = this.detailData.business || ''
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        // this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        // this.detailData.monitorBusinessCount = this.detailData.monitorBusinessRes.data.reduce(
        //   (sum, cur) => sum + cur.count,
        //   0
        // )
        this.detailData.artifactsCount = this.detailData.artifactsRes.data.reduce(
          (sum, cur) => sum + cur.artifactLen,
          0
        )
        this.statusObj = this.statusList.find(i => i.value === this.detailData.status)
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
        // 第二步monitor数据拆分
        this.detailData.monitorRes.data = this.detailData.monitorRes.data.filter(i =>
          ['monitor_type', 'endpoint', 'endpoint_group', 'service_group', 'log_monitor_template'].includes(i.name))
        this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        // 第四步monitor数据拆分
        this.detailData.monitorBusinessRes.data = this.detailData.monitorBusinessRes.data.filter(i =>
          [
            'metric_list',
            'log_monitor_service_group',
            'strategy_list',
            'logKeyword_service_group',
            'dashboard'
          ].includes(i.name))
        this.detailData.monitorBusinessCount = this.detailData.monitorBusinessRes.data.reduce(
          (sum, cur) => sum + cur.count,
          0
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
          errMsg: `${failObj.title}：${failObj.errMsg}`
        }
        if (success) {
          this.detailData.stepTwoRes.status = 'success'
        }
        if (fail) {
          this.detailData.stepTwoRes.status = 'fail'
        }
        if (this.detailData.step === 2) {
          if (!['success', 'fail'].includes(this.detailData.stepTwoRes.status)) {
            if (!this.interval) {
              this.interval = setInterval(() => {
                this.getDetailData()
              }, 30 * 1000)
            }
          } else {
            clearInterval(this.interval)
          }
        }
        // 第三步导入状态判断
        if (this.detailData.step === 3) {
          if (!['success', 'fail'].includes(this.detailData.initWorkflowRes.status)) {
            if (!this.interval) {
              this.interval = setInterval(() => {
                this.getDetailData()
              }, 30 * 1000)
            }
          } else {
            clearInterval(this.interval)
          }
        }
        // 第四步导入状态判断
        if (this.detailData.step === 4) {
          if (!['success', 'fail'].includes(this.detailData.monitorBusinessRes.status)) {
            if (!this.interval) {
              this.interval = setInterval(() => {
                this.getDetailData()
              }, 30 * 1000)
            }
          } else {
            clearInterval(this.interval)
          }
        }
      }
    },
    async handleSaveStepOne(id) {
      this.id = id
      this.$router.replace({
        path: '/admin/base-migration/import',
        query: {
          type: 'edit',
          id: this.id,
          timestamp: new Date().getTime() // 解决页面fullpath一样，不刷新问题
        }
      })
    },
    async handleSaveStepTwo() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    async handleSaveStepThree() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    async handleSaveStepFour() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
    },
    // 终止
    handleStop() {
      this.$Modal.confirm({
        title: '提示',
        content: '确认终止吗？',
        onOk: async () => {
          const params = {
            transImportId: this.id,
            status: 'exit'
          }
          const { status } = await updateImportStatus(params)
          if (status === 'OK') {
            this.loading = true
            await this.getDetailData()
            this.loading = false
          }
        },
        onCancel: () => {}
      })
    },
    // 重新发起
    handleReLauch() {
      this.$router.replace({
        path: '/admin/base-migration/import',
        query: {
          type: 'republish',
          id: this.id
        }
      })
    }
  },
  beforeRouteLeave(to, from, next) {
    if (
      this.detailData.monitorBusinessRes
      && this.detailData.monitorBusinessRes.status === 'success'
      && this.detailData.status === 'doing'
    ) {
      this.$Modal.confirm({
        title: '提示',
        content: '当前导入需要手动确认',
        okText: '完成导入',
        onOk: async () => {
          const params = {
            transImportId: this.id,
            status: 'completed'
          }
          const { status } = await updateImportStatus(params)
          if (status === 'OK') {
            // 用户确认离开
            next()
          }
        },
        onCancel: () => {
          next()
        }
      })
    } else {
      next()
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
    padding-right: 10px;
    border-right: 1px solid #e8eaec;
    height: 100%;
    .status-group {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin: -10px 0 10px 0;
      padding: 5px;
      .status {
        font-size: 14px;
        font-weight: bold;
      }
      button {
        margin-left: 10px;
        height: 30px;
      }
    }
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
<style lang="scss">
.base-migration-import-create {
  .ivu-tag-border {
    height: 30px;
    line-height: 30px;
  }
}
</style>
