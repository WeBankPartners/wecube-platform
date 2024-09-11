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
      <BaseHeaderTitle v-if="activeStep === 0" title="导出产品">
        <stepEnviroment></stepEnviroment>
      </BaseHeaderTitle>
      <BaseHeaderTitle v-if="activeStep === 1" title="导出数据">
        <stepSelectData></stepSelectData>
      </BaseHeaderTitle>
      <BaseHeaderTitle v-if="activeStep === 2" title="导出结果">
        <stepSelectData :status="status"></stepSelectData>
      </BaseHeaderTitle>
      <div class="footer">
        <template v-if="activeStep === 0">
          <Button type="info" @click="handleNext">下一步</Button>
        </template>
        <template v-else-if="activeStep === 1">
          <Button type="default" @click="handleLast">上一步</Button>
          <Button type="primary" @click="handleSubmit" style="margin-left: 10px">执行导出</Button>
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
export default {
  components: {
    stepEnviroment,
    stepSelectData
  },
  data() {
    return {
      activeStep: 0,
      status: 'waiting' // 导出状态waiting、error、success(前端根据接口响应自己定义)
    }
  },
  methods: {
    handleNext() {
      this.activeStep++
    },
    handleLast() {
      this.activeStep--
    },
    handleSubmit() {
      this.activeStep++
    },
    handleToHistory() {},
    handleReLauch() {}
  }
}
</script>

<style lang="scss" scoped>
.base-migration-export-create {
  display: flex;
  height: calc(100vh - 100px);
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
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 16px;
}
::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: 16px;
}
</style>
