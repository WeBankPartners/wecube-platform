<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-import-create">
      <div class="steps">
        <!--导入步骤-->
        <BaseHeaderTitle :title="$t('pi_import_steps')" :showExpand="false" class="custom-header">
          <div class="back-header">
            <Icon size="24" type="md-arrow-back" class="icon" @click="handleBack" />
          </div>
          <div v-if="statusObj.label && type !== 'republish'" class="status-group">
            <div class="status">
              {{ $t('status') }}：<Tag type="border" :color="statusObj.color">{{ statusObj.label }}</Tag>
            </div>
            <!--终止-->
            <Button v-if="['doing', 'fail'].includes(detailData.status)" type="error" @click="handleStop">{{
              $t('stop_orch')
            }}</Button>
            <!--重新发起-->
            <Button v-if="['success', 'exit'].includes(detailData.status)" type="success" @click="handleReLauch">{{
              $t('be_republish')
            }}</Button>
            <!--重试-->
            <Button v-if="['fail'].includes(detailData.status) && activeStep === 1" type="success" @click="handleRetry">{{
              $t('be_retry')
            }}</Button>
          </div>
          <Steps :current="activeStep" direction="vertical">
            <Step :title="$t('pi_import_step1')" :content="$t('pi_import_step1_tips')"></Step>
            <Step :title="$t('pi_import_step2')" :content="$t('pi_import_step2_tips')"></Step>
            <Step :title="$t('pi_import_step3')" :content="$t('pi_import_step3_tips')"></Step>
            <Step :title="$t('pi_import_step4')" :content="$t('pi_import_step4_tips')"></Step>
            <Step :title="$t('pi_import_step5')" :content="$t('pi_import_step5_tips')"></Step>
          </Steps>
        </BaseHeaderTitle>
      </div>
      <div class="content" ref="scrollView">
        <!--导入产品-->
        <BaseHeaderTitle v-if="activeStep === 0" :title="$t('pi_import_product')" :showExpand="false">
          <StepOne
            :detailData="detailData"
            @saveStepOne="handleSaveStepOne"
            @nextStep="activeStep++"
            @startLoading="startLoading"
            @stopLoading="stopLoading"
          ></StepOne>
        </BaseHeaderTitle>
        <!--导入数据-->
        <BaseHeaderTitle v-if="activeStep === 1" :title="$t('pi_import_step2')" :showExpand="false">
          <StepTwo
            :detailData="detailData"
            @saveStepTwo="handleSaveStepTwo"
            @lastStep="activeStep--"
            @nextStep="activeStep++"
            @startLoading="startLoading"
            @stopLoading="stopLoading"
            @fetchDetail="handleFetchDetail"
          ></StepTwo>
        </BaseHeaderTitle>
        <!--修改数据-->
        <BaseHeaderTitle v-if="activeStep === 2" :title="$t('pi_import_step3')" :showExpand="false">
          <StepThree
            :detailData="detailData"
            @saveStepThree="handleSaveStepThree"
            @lastStep="activeStep--"
            @nextStep="activeStep++"
            @startLoading="startLoading"
            @stopLoading="stopLoading"
          ></StepThree>
        </BaseHeaderTitle>
        <!--执行自动化编排-->
        <BaseHeaderTitle v-if="activeStep === 3" :title="$t('pi_import_step4')" :showExpand="false">
          <StepFour
            :detailData="detailData"
            @saveStepFour="handleSaveStepFour"
            @lastStep="activeStep--"
            @nextStep="activeStep++"
            @startLoading="startLoading"
            @stopLoading="stopLoading"
          ></StepFour>
        </BaseHeaderTitle>
        <!--监控配置-->
        <BaseHeaderTitle v-if="activeStep === 4" :title="$t('pi_import_step5')" :showExpand="false">
          <StepFive
            :detailData="detailData"
            @saveStepFive="handleSaveStepFive"
            @lastStep="activeStep--"
            @startLoading="startLoading"
            @stopLoading="stopLoading"
          ></StepFive>
        </BaseHeaderTitle>
      </div>
    </div>
    <Spin v-if="loading" size="large" fix></Spin>
  </Card>
</template>

<script>
/**
 * 导入创建页面组件
 * 提供五步导入流程：导入产品、导入数据、修改数据、执行自动化编排、监控配置
 */
import StepOne from './components/step-one.vue'
import StepTwo from './components/step-two.vue'
import StepThree from './components/step-three.vue'
import StepFour from './components/step-four.vue'
import StepFive from './components/step-five.vue'
import { groupArrayByKey } from '@/const/util'
import { getImportDetail, updateImportStatus, importRetry } from '@/api/server'
export default {
  components: {
    StepOne,
    StepTwo,
    StepThree,
    StepFour,
    StepFive
  },
  data() {
    return {
      // 导入记录ID
      id: this.$route.query.id || '',
      // 操作类型：edit编辑、detail查看、republish重新发起
      type: this.$route.query.type || '',
      // 当前激活的步骤索引
      activeStep: -1,
      // 加载状态
      loading: false,
      // 导入详情数据
      detailData: {},
      // 当前状态对象
      statusObj: {},
      // 状态列表配置
      statusList: [
        {
          label: this.$t('fe_inProgress'), // 执行中
          value: 'doing',
          color: '#5384ff'
        },
        {
          label: this.$t('be_success'), // 成功
          value: 'success',
          color: '#00cb91'
        },
        {
          label: this.$t('be_error'), // 失败
          value: 'fail',
          color: '#ff4d4f'
        },
        {
          label: this.$t('stop_orch'), // 终止
          value: 'exit',
          color: '#f29360'
        }
      ]
    }
  },
  /**
   * 组件挂载后初始化
   * 根据路由参数判断是新建、编辑还是查看，并设置对应的步骤
   */
  async mounted() {
    // 查看或编辑操作
    if (this.id && this.type !== 'republish') {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      // 后台返回当前步骤
      if (this.detailData.status !== 'success') {
        this.activeStep = this.detailData.step - 1
      } else {
        this.activeStep = 4
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
  /**
   * 组件销毁前清理定时器
   */
  beforeDestroy() {
    clearTimeout(this.interval)
  },
  methods: {
    /**
     * 获取导入详情数据
     * 从接口获取导入任务的详细信息，包括各模块的导入状态和数据
     * 处理数据格式化、统计、错误信息提取等逻辑
     * 如果任务正在执行中，会设置定时器轮询查询状态
     */
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
          modifyNewEnvDataRes: data.modifyNewEnvData || {},
          componentLibraryRes: data.componentLibrary || {}, // 组件库
          step: data.step,
          ...data.transExport
        }
        this.statusObj = this.statusList.find(i => i.value === this.detailData.status)
        this.detailData.roleRes.data = this.detailData.roleRes.data || []
        this.detailData.flowRes.data = this.detailData.flowRes.data || []
        this.detailData.batchRes.data = this.detailData.batchRes.data || []
        this.detailData.itsmRes.data = this.detailData.itsmRes.data || []
        this.detailData.artifactsRes.data = this.detailData.artifactsRes.data || []
        this.detailData.monitorRes.data = this.detailData.monitorRes.data || []
        this.detailData.pluginsRes.data = this.detailData.pluginsRes.data || []
        this.detailData.roleRes.title = this.$t('role')
        this.detailData.flowRes.title = this.$t('m_procDefId')
        this.detailData.batchRes.title = this.$t('bc_operation')
        this.detailData.itsmRes.title = this.$t('pe_itsm')
        this.detailData.componentLibraryRes.title = this.$t('pi_component_library')
        this.detailData.artifactsRes.title = this.$t('pe_articles')
        this.detailData.monitorRes.title = this.$t('pe_monitor_config')
        this.detailData.pluginsRes.title = this.$t('pluginService')
        this.detailData.cmdbRes.title = 'CMDB'
        this.detailData.initWorkflowRes.data = this.detailData.initWorkflowRes.data || []
        this.detailData.monitorBusinessRes.data = this.detailData.monitorBusinessRes.data || []
        this.detailData.modifyNewEnvDataRes.data = this.detailData.modifyNewEnvDataRes.data || {}
        this.detailData.associationSystems = (this.detailData.associationSystem && this.detailData.associationSystem.split(',')) || []
        this.detailData.associationTechProducts = (this.detailData.associationProduct && this.detailData.associationProduct.split(',')) || []
        this.detailData.businessName = this.detailData.businessName || ''
        this.detailData.businessNameList = (this.detailData.businessName && this.detailData.businessName.split(',')) || []
        this.detailData.business = this.detailData.business || ''
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.artifactsCount = this.detailData.artifactsRes.data.reduce(
          (sum, cur) => sum + cur.artifactLen,
          0
        )
        this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.monitorBusinessCount = this.detailData.monitorBusinessRes.data.reduce(
          (sum, cur) => sum + cur.count,
          0
        )
        // CMDB CI 数据按分组展示
        this.detailData.cmdbCIData = groupArrayByKey(this.detailData.cmdbCIData, 'group')
        this.detailData.cmdbCIData = this.detailData.cmdbCIData.flat()
        // 判断第二步导入状态：所有模块都成功则为成功，任一失败则为失败
        const {
          artifactsRes,
          batchRes,
          cmdbRes,
          monitorRes,
          pluginsRes,
          itsmRes,
          roleRes,
          flowRes,
          componentLibraryRes
        } = this.detailData
        const stepTwoData = [
          artifactsRes,
          batchRes,
          cmdbRes,
          monitorRes,
          pluginsRes,
          itsmRes,
          roleRes,
          flowRes,
          componentLibraryRes
        ]
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
        // 如果任务正在执行中，设置定时器轮询查询状态
        const intervalFlag = (!['success', 'fail'].includes(this.detailData.stepTwoRes.status)
            || !['success', 'fail'].includes(this.detailData.initWorkflowRes.status)
            || !['success', 'fail'].includes(this.detailData.monitorBusinessRes.status))
          && this.detailData.status === 'doing'
        if (intervalFlag) {
          this.interval = setTimeout(() => {
            this.getDetailData()
          }, 10 * 1000)
        } else {
          if (this.interval) {
            clearTimeout(this.interval)
          }
        }
      }
    },
    /**
     * 保存第一步数据
     * @param {string} id - 导入记录ID
     */
    async handleSaveStepOne(id) {
      this.id = id
      this.$router.replace({
        path: '/admin/base-migration/import',
        query: {
          type: 'edit',
          id: this.id,
          timestamp: new Date().getTime() // 解决页面 fullpath 一样，不刷新问题
        }
      })
    },
    /**
     * 开始加载
     */
    startLoading() {
      this.loading = true
    },
    /**
     * 停止加载
     */
    stopLoading() {
      this.loading = false
    },
    /**
     * 获取详情数据
     */
    async handleFetchDetail() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
    },
    /**
     * 保存第二步数据并进入下一步
     */
    async handleSaveStepTwo() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    /**
     * 保存第三步数据并进入下一步
     */
    async handleSaveStepThree() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    /**
     * 保存第四步数据并进入下一步
     */
    async handleSaveStepFour() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      this.activeStep++
    },
    /**
     * 保存第五步数据
     */
    async handleSaveStepFive() {
      this.loading = true
      await this.getDetailData()
      this.loading = false
    },
    /**
     * 终止导入任务
     */
    handleStop() {
      this.$Modal.confirm({
        title: this.$t('pi_tips'),
        content: `${this.$t('confirm') + this.$t('stop_orch')}？`,
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
    /**
     * 重新发起导入
     */
    handleReLauch() {
      this.$router.replace({
        path: '/admin/base-migration/import',
        query: {
          type: 'republish',
          id: this.id
        }
      })
    },
    /**
     * 重试导入
     * 物料包失败时可以执行重试
     */
    async handleRetry() {
      const params = {
        transImportId: this.detailData.id,
        step: 2
      }
      this.loading = true
      const { status } = await importRetry(params)
      if (status === 'OK') {
        this.handleFetchDetail()
      } else {
        this.loading = false
      }
    },
    /**
     * 返回历史列表
     */
    handleBack() {
      return this.$router.push({
        path: '/admin/base-migration/import-history'
      })
    }
  },
  /**
   * 路由离开前处理
   * 如果监控配置导入成功且任务正在执行中，需要用户确认完成导入
   */
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
    position: relative;
    width: 250px;
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
        margin-left: 5px;
        height: 30px;
      }
    }
    .back-header {
      width: 30px;
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      position: absolute;
      left: 0px;
      top: 0px;
      .icon {
        cursor: pointer;
        width: 28px;
        height: 24px;
        color: #fff;
        border-radius: 2px;
        background: #5384ff;
      }
      .name {
        font-size: 16px;
        margin-left: 16px;
        display: flex;
        align-items: center;
      }
    }
  }
  .content {
    width: calc(100% - 250px);
    padding-left: 15px;
    overflow-y: auto;
  }
  ::-webkit-scrollbar {
    width: 6px;
    height: 10px;
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
  .custom-header {
    .w-header-title,
    .title {
      margin-left: 40px !important;
    }
    .w-content {
      padding: 20px 5px;
    }
  }
  .status-group .ivu-btn {
    font-size: 12px;
    padding: 0 10px !important;
  }
}
</style>
