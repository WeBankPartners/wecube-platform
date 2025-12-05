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
/**
 * 导出创建页面组件
 * 提供三步导出流程：选择产品、选择数据、查看结果
 */
import StepEnviroment from './components/step-enviroment.vue'
import StepSelectData from './components/step-select-data.vue'
import StepResult from './components/step-result.vue'
import { debounce, groupArrayByKey } from '@/const/util'
import dayjs from 'dayjs'
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
      // 导出记录ID
      id: this.$route.query.id || '',
      // 操作类型：edit编辑、detail查看、republish重新发起
      type: this.$route.query.type || '',
      // 当前激活的步骤索引
      activeStep: -1,
      // 加载状态
      loading: false,
      // 导出详情数据，状态包括：start草稿、doing执行中、success成功、fail失败
      detailData: {}
    }
  },
  /**
   * 组件挂载后初始化
   * 根据路由参数判断是新建、编辑还是查看，并设置对应的步骤
   */
  async mounted() {
    const _id = this.$route.query.id
    const _type = this.$route.query.type
    // 编辑或查看操作
    if (_id && _type !== 'republish') {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      // 根据状态设置步骤：草稿状态显示第二步，执行中/成功/失败显示第三步
      if (this.detailData.status === 'start') {
        this.activeStep = 1
      } else if (['doing', 'success', 'fail'].includes(this.detailData.status)) {
        this.activeStep = 2
      }
    }
    // 重新发起操作
    if (_id && _type === 'republish') {
      this.loading = true
      await this.getDetailData()
      this.loading = false
      // 重置确认时间为当前时间
      this.detailData.lastConfirmTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
      this.activeStep = 0
    }
    // 新建操作
    if (!_id) {
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
     * 获取导出详情数据
     * 从接口获取导出任务的详细信息，包括各模块的导出状态和数据
     * 处理数据格式化、统计、错误信息提取等逻辑
     * 如果任务正在执行中，会设置定时器轮询查询状态
     */
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
        // 统计各模块的数据数量
        this.detailData.cmdbCICount = this.detailData.cmdbCIData.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.monitorCount = this.detailData.monitorRes.data.reduce((sum, cur) => sum + cur.count, 0)
        this.detailData.artifactsCount = this.detailData.artifactsRes.data.reduce(
          (sum, cur) => sum + cur.artifactLen,
          0
        )
        // CMDB CI 数据按分组展示
        this.detailData.cmdbCIData = groupArrayByKey(this.detailData.cmdbCIData, 'group')
        this.detailData.cmdbCIData = this.detailData.cmdbCIData.flat()
        // 处理无效的时间格式
        if (this.detailData.lastConfirmTime === '0000-00-00 00:00:00') {
          this.detailData.lastConfirmTime = ''
        }
        // 合并监控数据：将多个相关的监控配置项合并为统一的展示项
        const metric_list_obj = {
          name: 'metric_list',
          data: [],
          count: 0
        }
        const strategy_list_obj = {
          name: 'strategy_list',
          data: [],
          count: 0
        }
        this.detailData.monitorRes.data.forEach(i => {
          if (
            ['custom_metric_service_group', 'custom_metric_endpoint_group', 'custom_metric_monitor_type'].includes(
              i.name
            )
          ) {
            metric_list_obj.count += i.count
            metric_list_obj.data = [...metric_list_obj.data, ...(i.data || [])]
          }
          if (['strategy_service_group', 'strategy_endpoint_group'].includes(i.name)) {
            strategy_list_obj.count += i.count
            strategy_list_obj.data = [...strategy_list_obj.data, ...(i.data || [])]
          }
        })
        const metricIndex = this.detailData.monitorRes.data.findIndex(i => i.name === 'custom_metric_service_group')
        const strategyIndex = this.detailData.monitorRes.data.findIndex(i => i.name === 'strategy_service_group')
        this.detailData.monitorRes.data.splice(metricIndex, 0, metric_list_obj)
        this.detailData.monitorRes.data.splice(strategyIndex, 0, strategy_list_obj)
        // 过滤掉已合并的原始监控配置项
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
        // 提取失败模块的错误信息
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
        // 如果导出已完成（成功/失败/草稿），取消轮询；否则每10秒轮询一次查询状态
        if (['success', 'fail', 'start'].includes(this.detailData.status)) {
          if (this.interval) {
            clearTimeout(this.interval)
          }
        } else {
          this.interval = setTimeout(() => {
            this.getDetailData()
          }, 10 * 1000)
        }
      }
    },
    /**
     * 保存或更新环境和产品信息
     * 防抖处理，避免重复提交
     */
    handleSaveEnvBusiness: debounce(async function () {
      const {
        customerId, env, lastConfirmTime, envList, selectionList,
        productData, excludeDeployZone, deployZone, zoneList
      } = this.$refs.env
      const pIds = selectionList.map(item => item.id)
      const pNames = selectionList.map(item => item.displayName)
      const envName = envList.find(item => item.value === env).label
      // 选中的区域名称
      const selectedZoneNames = zoneList.filter(item => deployZone.includes(item.guid))
        .map(item => item.displayName) || []
      if (!customerId) {
        return this.$Message.warning(this.$t('pi_target_custom') + this.$t('required'))
      }
      if (!lastConfirmTime) {
        return this.$Message.warning(this.$t('pi_data_confirmTime') + this.$t('required'))
      }
      if (pIds.length === 0) {
        return this.$Message.warning(this.$t('pi_product_requiredTips'))
      }
      const params = {
        pIds,
        pNames,
        env,
        envName,
        lastConfirmTime,
        customerId,
        selectedTreeJson: JSON.stringify(productData),
        excludeDeployZone,
        deployZones: selectedZoneNames.join(',')
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
    /**
     * 返回上一步
     */
    handleLast() {
      this.activeStep--
    },
    /**
     * 执行导出操作
     * 防抖处理，避免重复提交
     */
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
        this.loading = true
        await this.getDetailData()
        this.loading = false
        this.$refs.scrollView.scrollTop = 0
      }
    }, 500),
    /**
     * 跳转到历史列表页面
     */
    handleToHistory() {
      this.$router.push({
        path: '/admin/base-migration/export-history'
      })
    },
    /**
     * 重新发起导出
     */
    handleReLauch() {
      this.$router.replace({
        path: '/admin/base-migration/export',
        query: {
          type: 'republish',
          id: this.id
        }
      })
    },
    /**
     * 返回历史列表
     */
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
