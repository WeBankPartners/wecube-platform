<template>
  <div class="export-step-data">
    <div class="export-message">
      <Alert v-if="detailData.status === 'doing'" type="warning" show-icon>
        <template #desc> 正在导出内容，请稍后... </template>
      </Alert>
      <Alert v-else-if="detailData.status === 'fail'" type="error" show-icon>
        导出失败！
        <template #desc></template>
      </Alert>
      <Alert v-else-if="detailData.status === 'success'" type="success" show-icon>
        导出成功！
        <template #desc>
          <div>
            恭喜！全部数据已导出成功,nexus链接:
            <span class="link">{{ detailData.outputUrl || '-' }}</span>
            <Icon
              type="md-copy"
              size="22"
              color="#2d8cf0"
              style="cursor: pointer"
              @click="copyText(detailData.outputUrl)"
            />
          </div>
          <div>请打开需要迁移环境的wecube,进入【系统-一键迁移-一键导入】,粘贴当前链接,执行导入</div>
        </template>
      </Alert>
    </div>
    <div class="item">
      <div class="item-header">
        <span class="item-header-t">环境产品系统<Icon type="ios-information-circle" size="20" /></span>
        <span class="item-header-e">环境<span class="number">{{ detailData.environmentName || '-' }}</span></span>
        <span class="item-header-p">产品<span class="number">{{ detailData.businessName.split(',').length || 0 }}</span></span>
        <span class="item-header-s">系统<span class="number">{{ detailData.associationSystems.length || 0 }}</span></span>
      </div>
      <card style="margin-top: 5px">
        <div class="content">
          <div class="content-list">
            <span>已选环境</span>
            <Tag>{{ detailData.environmentName || '-' }}</Tag>
          </div>
          <div class="content-list">
            <span>已选业务产品</span>
            <Tag v-for="(i, index) in detailData.businessName.split(',')" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
          <div class="content-list">
            <span>关联底座产品(自动分析)</span>
            <Tag v-for="(i, index) in detailData.associationTechProducts" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
          <div class="content-list">
            <span>关联系统(自动分析)</span>
            <Tag v-for="(i, index) in detailData.associationSystems" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
        </div>
      </card>
    </div>
    <!--角色列表-->
    <div class="item">
      <span class="title">
        角色：已选<span class="number">{{ detailData.roleData.length }}</span>
        <Alert v-if="detailData.roleStatus === 'fail'" type="error" show-icon>导出失败</Alert>
      </span>
      <div>
        <Table :border="false" size="small" :columns="roleTableColumns" :max-height="400" :data="detailData.roleData">
        </Table>
      </div>
    </div>
    <!--ITSM列表-->
    <div class="item">
      <span class="title">
        ITSM流程：已选<span class="number">{{ detailData.itsmData.length }}</span>
        <Alert v-if="detailData.itsmStatus === 'fail'" type="error" show-icon>导出失败</Alert>
      </span>
      <div style="margin: 10px 0">
        是否导出组件库：<i-switch disabled v-model="detailData.exportComponentLibrary"></i-switch>
      </div>
      <div>
        <!-- <BaseSearch
          :onlyShowReset="true"
          :options="itsmSearchOptions"
          v-model="itsmSearchParams"
          @search="handleSearchTable('itsm')"
        ></BaseSearch> -->
        <Table :border="false" size="small" :columns="itsmTableColumns" :max-height="400" :data="detailData.itsmData">
        </Table>
      </div>
    </div>
    <!--编排列表-->
    <div class="item">
      <span class="title">
        编排：已选<span class="number">{{ detailData.flowData.length }}</span>
        <Alert v-if="detailData.flowStatus === 'fail'" type="error" show-icon>导出失败</Alert>
      </span>
      <div>
        <!-- <BaseSearch
          :onlyShowReset="true"
          :options="flowSearchOptions"
          v-model="flowSearchParams"
          @search="handleSearchTable('flow')"
        ></BaseSearch> -->
        <Table :border="false" size="small" :columns="flowTableColumns" :max-height="400" :data="detailData.flowData">
        </Table>
      </div>
    </div>
    <!--批量执行列表-->
    <div class="item">
      <span class="title">
        批量执行：已选<span class="number">{{ detailData.batchData.length }}</span>
        <Alert v-if="detailData.batchStatus === 'fail'" type="error" show-icon>导出失败</Alert>
      </span>
      <div>
        <!-- <BaseSearch
          :onlyShowReset="true"
          :options="batchSearchOptions"
          v-model="batchSearchParams"
          @search="handleSearchTable('batch')"
        ></BaseSearch> -->
        <Table :border="false" size="small" :columns="batchTableColumns" :max-height="400" :data="detailData.batchData">
        </Table>
      </div>
    </div>
    <!--CMDB-->
    <div class="item">
      <span class="title">
        CMDB：<span class="sub-title">
          已选
          <span class="name">CI</span><span class="number">{{ detailData.cmdbCICount }}</span>
          <span class="name">视图</span><span class="number">{{ detailData.cmdbViewCount }}</span>
          <span class="name">报表</span><span class="number">{{ detailData.cmdbReportFormCount }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="8">
          <Card title="CI">
            <Table
              :border="false"
              size="small"
              :columns="cmdbCIColumns"
              :max-height="400"
              :data="detailData.cmdbCIData"
            />
          </Card>
        </Col>
        <Col :span="8">
          <Card title="视图">
            <Table
              :border="false"
              size="small"
              :columns="cmdbViewColumns"
              :max-height="400"
              :data="detailData.cmdbViewData"
            />
          </Card>
        </Col>
        <Col :span="8">
          <Card title="报表">
            <Table
              :border="false"
              size="small"
              :columns="cmdbReportColumns"
              :max-height="400"
              :data="detailData.cmdbReportData"
            />
          </Card>
        </Col>
      </Row>
    </div>
    <!--物料包-->
    <div class="item">
      <span class="title">
        物料包：已选<span class="number">{{ detailData.artifactsData.length }}</span>
      </span>
      <Row :gutter="10">
        <Col :span="12">
          <Card>
            <Table
              :border="false"
              size="small"
              :columns="artifactsColumns"
              :max-height="400"
              :data="detailData.artifactsData"
            />
          </Card>
        </Col>
      </Row>
    </div>
    <!--监控-->
    <div class="item">
      <span class="title">
        监控配置：<span class="sub-title">已选 <span class="name">配置类型</span><span class="number">{{ detailData.monitorData.length }}</span>
          <span class="name">总条数</span><span class="number">{{ detailData.monitorCount }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="12">
          <Card>
            <Table
              :border="false"
              size="small"
              :columns="monitorColumns"
              :max-height="400"
              :data="detailData.monitorData"
            />
          </Card>
        </Col>
      </Row>
    </div>
    <!--插件服务-->
    <div class="item">
      <span class="title">
        插件服务：<span class="sub-title">
          已选配置类型<span class="number">{{ 10 }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="12">
          <Card>
            <Table
              :border="false"
              size="small"
              :columns="pluginColumns"
              :max-height="400"
              :data="detailData.pluginsData"
            />
          </Card>
        </Col>
      </Row>
    </div>
  </div>
</template>

<script>
import selectTableConfig from '../selection-table'
import staticTableConfig from '../static-table'
export default {
  mixins: [selectTableConfig, staticTableConfig],
  props: {
    detailData: {
      type: Object,
      default() {
        return {
          roleData: [],
          flowData: [],
          batchData: [],
          itsmData: [],
          cmdbCIData: [],
          cmdbViewData: [],
          cmdbReportData: [],
          artifactsData: [],
          monitorData: [],
          componentLibrary: false,
          associationSystems: [],
          associationTechProducts: [],
          businessName: '',
          business: ''
        }
      }
    }
  },
  data() {
    return {}
  },
  mounted() {
    // 去掉表格复选框
    this.roleTableColumns.splice(0, 1)
    this.flowTableColumns.splice(0, 1)
    this.batchTableColumns.splice(0, 1)
    this.itsmTableColumns.splice(0, 1)
  },
  methods: {
    // 表格搜索
    handleSearchTable(type) {
      if (type === 'itsm') {
        //
      }
    },
    copyText(val) {
      const textArea = document.createElement('textarea')
      textArea.value = val
      document.body.appendChild(textArea)
      textArea.select()
      try {
        document.execCommand('copy')
        this.$Message.success('复制成功')
      }
      catch (err) {
        console.error('复制失败:', err)
      }
      document.body.removeChild(textArea)
    }
  }
}
</script>

<style lang="scss" scoped>
.export-step-data {
  .export-message {
    margin-top: -10px;
    margin-bottom: 16px;
  }
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 50px;
    &-header {
      &-t {
        font-weight: bold;
      }
      &-e,
      &-p,
      &-s {
        margin-left: 16px;
        .number {
          font-size: 18px;
          color: #2d8cf0;
          margin-left: 6px;
        }
      }
    }
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      .name {
        margin-left: 10px;
      }
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
    }
    .content {
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      &-list {
        display: flex;
        flex-direction: column;
        width: 220px;
        margin-right: 20px;
        span {
          margin-bottom: 2px;
        }
      }
    }
  }
}
</style>
<style lang="scss">
.export-step-data {
  .ivu-card-head {
    padding: 8px 10px;
    p {
      font-size: 14px;
    }
  }
  .ivu-card-body {
    padding: 10px;
  }
  .ivu-alert-with-desc .ivu-alert-icon {
    left: 16px;
    top: 24px;
    font-size: 28px;
  }
  .ivu-alert-with-desc.ivu-alert-with-icon {
    padding: 10px 16px 10px 55px;
  }
  .common-base-search-button {
    width: fit-content;
  }
}
</style>
