<template>
  <Card :bordered="false" dis-hover :padding="0">
    <div class="base-migration-export-create">
      <div class="steps">
        <BaseHeaderTitle title="导出步骤" :showExpand="false">
          <Steps :current="activeStep" direction="vertical">
            <Step title="选择产品、环境" content="系统自动分析需要导出的系统及数据"></Step>
            <Step title="选择数据,执行导出" content="确认依赖系统、CMDB、编排、ITSM等配置项正确"></Step>
            <Step title="确认导出结果" content="查看导出数据对比"></Step>
          </Steps>
        </BaseHeaderTitle>
      </div>
      <div class="content" ref="scrollView">
        <BaseHeaderTitle v-if="activeStep === 0" title="导出产品" :showExpand="false">
          <StepEnviroment ref="env" :detailData="detailData"></StepEnviroment>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 1" title="导出数据" :showExpand="false">
          <StepSelectData ref="data" :detailData="detailData"></StepSelectData>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 2" title="导出结果" :showExpand="false">
          <StepResult :detailData="detailData"></StepResult>
        </BaseHeaderTitle>
        <div class="footer">
          <template v-if="activeStep === 0">
            <Button type="info" @click="handleSaveEnvBusiness">下一步</Button>
          </template>
          <template v-else-if="activeStep === 1">
            <Button type="default" @click="handleLast">上一步</Button>
            <Button type="primary" @click="handleSaveExport" style="margin-left: 10px">执行导出</Button>
          </template>
          <template v-else-if="activeStep === 2">
            <Button type="default" @click="handleToHistory" style="margin-left: 10px">历史列表</Button>
            <Button
              v-if="['success', 'fail'].includes(detailData.status)"
              type="success"
              @click="handleReLauch"
              style="margin-left: 10px"
            >重新发起</Button>
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
import { debounce } from '@/const/util'
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
      // 导出状态start草稿、doing执行中、success成功、fail失败
      loading: false,
      detailData: {}
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
          failMsg: '',
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
          artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes
        } = this.detailData
        const exportData = [artifactsRes, batchRes, cmdbRes, monitorRes, pluginsRes, itsmRes, roleRes, flowRes]
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
      const { env, envList, selectionList } = this.$refs.env
      const pIds = selectionList.map(item => item.id)
      const pNames = selectionList.map(item => item.displayName)
      const envName = envList.find(item => item.value === env).label
      if (pIds.length === 0) {
        return this.$Message.warning('至少勾选一条产品数据')
      }
      const params = {
        pIds,
        pNames,
        env,
        envName
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
    width: 260px;
    padding-right: 15px;
    border-right: 1px solid #e8eaec;
    height: 100%;
  }
  .content {
    width: calc(100% - 260px);
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
