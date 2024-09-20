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
            <Step title="确认导入结果" content="查看导入导出数据对比"></Step>
          </Steps>
        </BaseHeaderTitle>
      </div>
      <div class="content" ref="scrollView">
        <BaseHeaderTitle v-if="activeStep === 0" title="导入产品" :showExpand="false">
          111111111111111
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 1" title="导入数据" :showExpand="false">
          2222222222222222
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 2" title="执行自动化编排" :showExpand="false">
          333333333333333
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 3" title="配置监控" :showExpand="false">
          444444444444444
        </BaseHeaderTitle>
        <BaseHeaderTitle v-if="activeStep === 4" title="导入结果" :showExpand="false">
          555555555555555
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
              type="primary"
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
export default {
  components: {
  },
  data() {
    return {
      activeStep: 0,
      loading: false
    }
  },
  async mounted() {},
  methods: {}
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
