<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-export-create">
      <div class="steps">
        <BaseHeaderTitle :title="$t('pe_export_steps')" :showExpand="false" class="custom-header">
          <div class="back-header">
            <Icon size="24" type="md-arrow-back" class="icon" @click="handleBack" />
          </div>
          <Steps :current="activeStep" direction="vertical">
            <Step :title="$t('pe_step1')" :content="$t('pe_step1_tips')"></Step>
            <Step :title="$t('pe_step2')" :content="$t('pe_step2_tips')"></Step>
            <Step :title="$t('pe_step3')" :content="$t('pe_step3_tips')"></Step>
          </Steps>
        </BaseHeaderTitle>
      </div>
      <div class="content" ref="scrollView">
        <!--导出产品-->
        <BaseHeaderTitle v-if="activeStep === 0" :title="$t('pe_export_product')" :showExpand="false">
          <StepEnviroment ref="env" :detailData="detailData"></StepEnviroment>
        </BaseHeaderTitle>
        <!--导出数据-->
        <BaseHeaderTitle v-if="activeStep === 1" :title="$t('pe_export_data')" :showExpand="false">
          <StepSelectData ref="data" :detailData="detailData"></StepSelectData>
        </BaseHeaderTitle>
        <!--导出结果-->
        <BaseHeaderTitle v-if="activeStep === 2" :title="$t('pe_export_result')" :showExpand="false">
          <StepResult :detailData="detailData"></StepResult>
        </BaseHeaderTitle>
        <div class="footer">
          <template v-if="activeStep === 0">
            <Button type="info" @click="handleSaveEnvBusiness">{{ $t('next_step') }}</Button>
          </template>
          <template v-else-if="activeStep === 1">
            <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
            <Button type="primary" @click="handleSaveExport" style="margin-left: 10px">{{
              $t('pe_execute_export')
            }}</Button>
          </template>
          <template v-else-if="activeStep === 2">
            <Button type="default" @click="handleToHistory" style="margin-left: 10px">{{
              $t('pe_history_list')
            }}</Button>
            <Button
              v-if="['success', 'fail'].includes(detailData.status)"
              type="success"
              @click="handleReLauch"
              style="margin-left: 10px"
            >{{ $t('be_republish') }}</Button>
          </template>
        </div>
      </div>
    </div>
    <Spin v-if="loading" size="large" fix></Spin>
  </Card>
</template>

<script>
import StepEnviroment from './components/step-enviroment.vue'
import StepSelectData from './components/step-select-data.vue'
import StepResult from './components/step-result.vue'
import { debounce, groupArrayByKey } from '@/const/util'
import {
  saveEnvBusiness, updateEnvBusiness, exportBaseMigration, getExportDetail
} from '@/api/server'
export default {
  components: {
    StepEnviroment,
    StepSelectData,
    StepResult
  },
  data() {
    return {
      id: this.$route.query.id || '',
      type: this.$route.query.type || '',
      activeStep: -1,
      loading: false,
      detailData: {} // 导出状态start草稿、doing执行中、success成功、fail失败
    }
  },
  async mounted() {
    const _id = this.$route.query.id
    const _type = this.$route.query.type
    if (_id && _type !== 'republish') {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      if (this.detailData.status === 'start') {
        this.activeStep = 1
      } else if (['doing', 'success', 'fail'].includes(this.detailData.status)) {
        this.activeStep = 2
      }
    }
    // 重新发起
    if (_id && _type === 'republish') {
      await this.getDetailData()
      this.activeStep = 0
    }
    // 新建操作
    if (!_id) {
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
          transExportId: this.id
        }
      }
      const { status, data } = await getExportDetail(params)
      if (status === 'OK') {
        this.detailData = {
          roleRes: data.roles || {},
          flowRes: data.workflows || {},
          batchRes: data.batchExecutions || {},
          itsmRes: data.requestTemplates || {},
          cmdbRes: data.cmdb, // cmdb
          cmdbCIData: data.cmdbCI || [], // cmdb CI
          cmdbViewData: data.cmdbView || [], // cmdb视图
          cmdbReportData: data.cmdbReportForm || [], // cmdb报表
          cmdbReportFormCount: data.cmdbReportFormCount || 0,
          cmdbViewCount: data.cmdbViewCount || 0,
          artifactsRes: data.artifacts || {}, // 物料包
          monitorRes: data.monitor || {}, // 监控
          pluginsRes: data.plugins || {}, // 插件
          exportComponentLibrary: data.exportComponentLibrary, // 组件库
          createAndUploadFileRes: data.createAndUploadFile,
          failMsg: '',
          ...data.transExport
        }
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
        this.detailData.artifactsRes.title = this.$t('pe_articles')
        this.detailData.monitorRes.title = this.$t('pe_monitor_config')
        this.detailData.pluginsRes.title = this.$t('pluginService')
        this.detailData.cmdbRes.title = 'CMDB'
        this.detailData.createAndUploadFileRes.title = this.$t('pi_system_variable')
        this.detailData.associationSystems = this.detailData.associationSystems || []
        this.detailData.associationTechProducts = this.detailData.associationTechProducts || []
        this.detailData.businessName = this.detailData.businessName || ''
        this.detailData.businessNameList = (this.detailData.businessName && this.detailData.businessName.split(',')) || []
        this.detailData.business = this.detailData.business || ''
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.artifactsCount = this.detailData.artifactsRes.data.reduce(
          (sum, cur) => sum + cur.artifactLen,
          0
        )
        // cmdbCI分组展示
        this.detailData.cmdbCIData = groupArrayByKey(this.detailData.cmdbCIData, 'group')
        this.detailData.cmdbCIData = this.detailData.cmdbCIData.flat()
        // 处理时间为空
        if (this.detailData.lastConfirmTime === '0000-00-00 00:00:00') {
          this.detailData.lastConfirmTime = ''
        }
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
        // 错误信息提取
        const {
          artifactsRes,
          batchRes,
          cmdbRes,
          monitorRes,
          pluginsRes,
          itsmRes,
          roleRes,
          flowRes,
          createAndUploadFileRes
        } = this.detailData
        const exportData = [
          artifactsRes,
          batchRes,
          cmdbRes,
          monitorRes,
          pluginsRes,
          itsmRes,
          roleRes,
          flowRes,
          createAndUploadFileRes
        ]
        const failObj = exportData.find(i => i.status === 'fail') || {}
        this.detailData.failMsg = `${failObj.title}：${failObj.errMsg}`
        // 成功或失败，取消轮询查状态
        if (['success', 'fail'].includes(this.detailData.status)) {
          clearInterval(this.interval)
        }
      }
    },
    // 保存or更新环境和产品
    handleSaveEnvBusiness: debounce(async function () {
      const {
        env, lastConfirmTime, envList, selectionList
      } = this.$refs.env
      const pIds = selectionList.map(item => item.id)
      const pNames = selectionList.map(item => item.displayName)
      const envName = envList.find(item => item.value === env).label
      if (pIds.length === 0) {
        return this.$Message.warning(this.$t('pi_product_requiredTips'))
      }
      const params = {
        pIds,
        pNames,
        env,
        envName,
        lastConfirmTime
      }
      this.loading = true
      const { status, data } = await (this.id && this.type !== 'republish'
        ? updateEnvBusiness({
          ...params,
          transExportId: this.id
        })
        : saveEnvBusiness(params))
      this.loading = false
      if (status === 'OK') {
        // 保存第一步会返回id
        if (data) {
          this.id = data
        }
        // 这里通过路由跳转到下一步，避免页面刷新，丢失id
        this.$router.replace({
          path: '/admin/base-migration/export',
          query: {
            type: 'edit',
            id: this.id,
            timestamp: new Date().getTime() // 解决页面fullpath一样，不刷新问题
          }
        })
      }
    }, 500),
    // 上一步
    handleLast() {
      this.activeStep--
    },
    // 执行导出
    handleSaveExport: debounce(async function () {
      const {
        roleSelectionList, itsmSelectionList, flowSelectionList, batchSelectionList, exportComponentLibrary
      } = this.$refs.data
      const roleArr = (roleSelectionList && roleSelectionList.map(item => item.name)) || []
      const itsmArr = (itsmSelectionList && itsmSelectionList.map(item => item.id)) || []
      const flowArr = (flowSelectionList && flowSelectionList.map(item => item.id)) || []
      const batchArr = (batchSelectionList && batchSelectionList.map(item => item.id)) || []
      const params = {
        transExportId: this.id,
        exportComponentLibrary,
        roles: roleArr,
        workflowIds: flowArr,
        batchExecutionIds: batchArr,
        requestTemplateIds: itsmArr
      }
      this.loading = true
      const { status } = await exportBaseMigration(params)
      this.loading = false
      if (status === 'OK') {
        this.activeStep++
        this.getDetailData()
        this.$refs.scrollView.scrollTop = 0
        // 定时查询导出状态
        this.interval = setInterval(() => {
          this.getDetailData()
        }, 30 * 1000)
      }
    }, 500),
    // 跳转到历史列表
    handleToHistory() {
      this.$router.push({
        path: '/admin/base-migration/export-history'
      })
    },
    // 重新发起
    handleReLauch() {
      this.$router.replace({
        path: '/admin/base-migration/export',
        query: {
          type: 'republish',
          id: this.id
        }
      })
    },
    handleBack() {
      return this.$router.push({
        path: '/admin/base-migration/export-history'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-export-create {
  display: flex;
  height: calc(100vh - 100px);
  overflow: hidden;
  .steps {
    position: relative;
    width: 250px;
    padding-right: 15px;
    border-right: 1px solid #e8eaec;
    height: 100%;
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
        background: #2d8cf0;
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
    padding-left: 10px;
    overflow-y: auto;
    .footer {
      position: fixed;
      bottom: 10px;
      display: flex;
      justify-content: center;
      width: calc(100% - 460px);
    }
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
.base-migration-export-create {
  .custom-header {
    .w-header-title,
    .title {
      margin-left: 40px !important;
    }
    .w-content {
      padding: 20px 5px;
    }
  }
}
</style>
