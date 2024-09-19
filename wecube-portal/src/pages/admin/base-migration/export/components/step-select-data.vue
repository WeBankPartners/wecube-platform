<template>
  <div class="export-step-data">
    <div class="item">
      <div class="item-header">
        <span class="item-header-t">环境产品系统<Icon type="ios-information-circle" size="20" /></span>
        <span class="item-header-e">环境<span class="number">{{ detailData.environmentName || '-' }}</span></span>
        <span class="item-header-p">产品<span class="number">{{ detailData.businessName.split(',').length || 0 }}</span></span>
        <span class="item-header-s">系统<span class="number">{{
          (detailData.associationSystems && detailData.associationSystems.length) || 0
        }}</span></span>
      </div>
      <card style="margin-top: 5px">
        <div class="content">
          <div class="content-list">
            <span>已选环境</span>
            <Tag>{{ detailData.environmentName }}</Tag>
          </div>
          <div class="content-list">
            <span>已选业务产品</span>
            <Tag v-for="(i, index) in detailData.businessName.split(',')" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
          <div class="content-list">
            <span>关联底座产品(自动分析)</span>
            <Tag v-for="(i, index) in detailData.associationTechProducts || []" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
          <div class="content-list">
            <span>关联系统(自动分析)</span>
            <Tag v-for="(i, index) in detailData.associationSystems || []" class="tag" :key="index">
              {{ i }}
            </Tag>
          </div>
        </div>
      </card>
    </div>
    <!--角色列表-->
    <div class="item">
      <span class="title">角色：已选<span class="number">{{ roleSelectionList.length }}</span></span>
      <div>
        <Table
          :border="false"
          size="small"
          :loading="roleTableLoading"
          :columns="roleTableColumns"
          :max-height="400"
          :data="roleTableData"
          @on-selection-change="selection => handleSelectChange('role', selection)"
        >
        </Table>
      </div>
    </div>
    <!--ITSM列表-->
    <div class="item">
      <span class="title">ITSM流程：已选<span class="number">{{ itsmSelectionList.length }}</span></span>
      <div>
        <BaseSearch
          :onlyShowReset="true"
          :options="itsmSearchOptions"
          v-model="itsmSearchParams"
          @search="handleSearchTable('itsm')"
        ></BaseSearch>
        <div style="margin: 10px 0">是否导出组件库：<i-switch v-model="exportComponentLibrary"></i-switch></div>
        <Table
          :border="false"
          size="small"
          :loading="itsmTableLoading"
          :columns="itsmTableColumns"
          :max-height="400"
          :data="itsmTableData"
          @on-selection-change="selection => handleSelectChange('itsm', selection)"
        >
        </Table>
      </div>
    </div>
    <!--编排列表-->
    <div class="item">
      <span class="title">编排：已选<span class="number">{{ flowSelectionList.length }}</span></span>
      <div>
        <BaseSearch
          :onlyShowReset="true"
          :options="flowSearchOptions"
          v-model="flowSearchParams"
          @search="handleSearchTable('flow')"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="flowTableLoading"
          :columns="flowTableColumns"
          :max-height="400"
          :data="flowTableData"
          @on-selection-change="selection => handleSelectChange('flow', selection)"
        >
        </Table>
      </div>
    </div>
    <!--批量执行列表-->
    <div class="item">
      <span class="title">批量执行：已选<span class="number">{{ batchSelectionList.length }}</span></span>
      <div>
        <BaseSearch
          :onlyShowReset="true"
          :options="batchSearchOptions"
          v-model="batchSearchParams"
          @search="handleSearchTable('batch')"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="batchTableLoading"
          :columns="batchTableColumns"
          :max-height="400"
          :data="batchTableData"
          @on-selection-change="selection => handleSelectChange('batch', selection)"
        >
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
import { deepClone } from '@/const/util'
import {
  getCurrentUserRoles, getAllExportFlows, getAllExportBatch, getAllExportItsm
} from '@/api/server.js'
export default {
  mixins: [selectTableConfig, staticTableConfig],
  props: {
    detailData: {
      type: Object
    }
  },
  data() {
    return {
      exportComponentLibrary: true,
      roleOriginTableData: [],
      itsmOriginTableData: [],
      flowOriginTableData: [],
      batchOriginTableData: []
    }
  },
  mounted() {
    this.getRoleTableList()
    this.getFlowTableList()
    this.getBatchTableList()
    this.getItsmTableList()
  },
  methods: {
    // 表格数据勾选
    handleSelectChange(type, selection) {
      if (type === 'role') {
        this.roleSelectionList = selection
      }
      else if (type === 'itsm') {
        this.itsmSelectionList = selection
      }
      else if (type === 'flow') {
        this.flowSelectionList = selection
      }
      else if (type === 'batch') {
        this.batchSelectionList = selection
      }
    },
    // 表格搜索
    handleSearchTable(type) {
      if (type === 'itsm') {
        this.itsmTableData = this.itsmOriginTableData.filter(item => {
          const nameFlag = item.name.toLowerCase().indexOf(this.itsmSearchParams.name.toLowerCase()) > -1
          const sceneFlag = !this.itsmSearchParams.scene || item.type.toString() === this.itsmSearchParams.scene
          if (nameFlag && sceneFlag) {
            return true
          }
        })
      }
      else if (type === 'flow') {
        this.flowTableData = this.flowOriginTableData.filter(item => {
          const nameFlag = item.name.toLowerCase().indexOf(this.flowSearchParams.name.toLowerCase()) > -1
          const idFlag = item.id.indexOf(this.flowSearchParams.id) > -1
          if (nameFlag && idFlag) {
            return true
          }
        })
      }
      else if (type === 'batch') {
        this.batchTableData = this.batchOriginTableData.filter(item => {
          const nameFlag = item.name.toLowerCase().indexOf(this.batchSearchParams.name.toLowerCase()) > -1
          const idFlag = item.id.indexOf(this.batchSearchParams.id) > -1
          if (nameFlag && idFlag) {
            return true
          }
        })
      }
    },
    // 获取角色
    async getRoleTableList() {
      const { status, data } = await getCurrentUserRoles()
      if (status === 'OK') {
        this.roleTableData = data.map(_ => ({
          ..._,
          _checked: true
        }))
        this.roleOriginTableData = deepClone(this.roleTableData)
        this.roleSelectionList = this.roleTableData
      }
    },
    // 获取编排
    async getFlowTableList() {
      const { status, data } = await getAllExportFlows()
      if (status === 'OK') {
        this.flowTableData = data.map(_ => ({
          ..._,
          _checked: true
        }))
        this.flowOriginTableData = deepClone(this.flowTableData)
        this.flowSelectionList = this.flowTableData
      }
    },
    // 获取批量执行
    async getBatchTableList() {
      const { status, data } = await getAllExportBatch()
      if (status === 'OK') {
        this.batchTableData = data.map(_ => ({
          ..._,
          _checked: true
        }))
        this.batchOriginTableData = deepClone(this.batchTableData)
        this.batchSelectionList = this.batchTableData
      }
    },
    // 获取ITSM
    async getItsmTableList() {
      const { statusCode, data } = await getAllExportItsm()
      if (statusCode === 'OK') {
        this.itsmTableData = data.map(_ => ({
          ..._,
          _checked: true
        }))
        this.itsmOriginTableData = deepClone(this.itsmTableData)
        this.itsmSelectionList = this.itsmTableData
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.export-step-data {
  .export-message {
    margin-top: -10px;
  }
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 50px;
    position: relative;
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
    margin-top: -20px;
    font-size: 26px;
  }
  .ivu-alert-with-desc.ivu-alert-with-icon {
    padding: 10px 16px 10px 55px;
  }
  .common-base-search-button {
    width: fit-content;
  }
}
</style>
