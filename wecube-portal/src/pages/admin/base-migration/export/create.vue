<template>
  <Card :bordered="false" dis-hover :padding="0">
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
        <BaseHeaderTitle v-if="activeStep === 0" title="导出产品">
          <stepEnviroment ref="env" :detailData="detailData"></stepEnviroment>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 1" title="导出数据">
          <stepSelectData ref="data" :detailData="detailData"></stepSelectData>
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 2" title="导出结果">
          <stepResult :detailData="detailData"></stepResult>
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
            <Button type="primary" @click="handleReLauch" style="margin-left: 10px">重新发起</Button>
          </template>
        </div>
      </div>
    </div>
    <Spin v-if="loading" size="large" fix></Spin>
  </Card>
</template>

<script>
import stepEnviroment from './components/step-enviroment.vue'
import stepSelectData from './components/step-select-data.vue'
import stepResult from './components/step-result.vue'
import { debounce } from '@/const/util'
import { saveEnvBusiness, exportBaseMigration, getExportDetail } from '@/api/server'
export default {
  components: {
    stepEnviroment,
    stepSelectData,
    stepResult
  },
  data() {
    return {
      id: this.$route.query.id || '',
      activeStep: -1,
      // 导出状态start草稿、doing执行中、success成功、fail失败
      loading: false,
      detailData: {}
    }
  },
  async mounted() {
    if (this.$route.query.id) {
      await this.getDetailData()
      if (this.detailData.status === 'start') {
        this.activeStep = 1
      }
      else if (['doing', 'success', 'fail'].includes(this.detailData.status)) {
        this.activeStep = 2
      }
    }
    else {
      this.activeStep = 0
    }
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
        const tableData = data.detail || []
        const getTableData = key => {
          const obj = tableData.find(i => i.name === key) || {}
          let arr = []
          try {
            arr = JSON.parse(obj.output)
          }
          catch {
            arr = []
          }
          return arr
        }
        this.detailData = {
          roleData: getTableData('role'),
          flowData: getTableData('workflow'),
          batchData: getTableData('batchExecution'),
          itsmData: getTableData('requestTemplate'),
          cmdbData: getTableData('cmdb'),
          artifactsData: getTableData('artifacts'),
          monitorData: getTableData('monitor'),
          ...data.transExport
        }
        // 成功或失败，取消轮询查状态
        if (['success', 'fail'].includes(this.detailData.status)) {
          clearInterval(this.interval)
        }
      }
    },
    // 保存环境和产品
    handleSaveEnvBusiness: debounce(async function () {
      const { env, selectionList } = this.$refs.env
      const pIds = (selectionList && selectionList.map(item => item.id)) || []
      if (pIds.length === 0) {
        return this.$Message.warning('至少勾选一条产品数据')
      }
      const params = {
        pIds,
        pNames: (selectionList && selectionList.map(item => item.displayName)) || [],
        env
      }
      this.loading = true
      const { status, data } = await saveEnvBusiness(params)
      this.loading = false
      if (status === 'OK') {
        // 保存第一步会返回id
        this.id = data
        // 这里通过路由跳转到下一步，避免页面刷新，丢失id
        this.$router.push({
          path: '/admin/base-migration/export',
          query: {
            type: 'edit',
            id: this.id
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
        roleSelectionList, itsmSelectionList, flowSelectionList, batchSelectionList
      } = this.$refs.data
      const roleArr = (roleSelectionList && roleSelectionList.map(item => item.name)) || []
      const itsmArr = (itsmSelectionList && itsmSelectionList.map(item => item.id)) || []
      const flowArr = (flowSelectionList && flowSelectionList.map(item => item.id)) || []
      const batchArr = (batchSelectionList && batchSelectionList.map(item => item.id)) || []
      const params = {
        transExportId: this.id,
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
        // 定时查询导出状态
        this.interval = setInterval(() => {
          this.getDetailData()
        }, 60 * 1000)
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
      this.$router.push({
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
