<template>
  <div class="export-step-data">
    <div class="item">
      <BaseHeaderTitle :title="$t('pe_env_product_sysytem')" :fontSize="15">
        <div slot="sub-title" class="item-header">
          <span class="item-header-title">{{ $t('pi_target_custom') }}<span class="number">{{ detailData.customerName || '-' }}</span></span>
          <span class="item-header-title">{{ $t('pe_env') }}<span class="number">{{ detailData.environmentName || '-' }}</span></span>
          <span class="item-header-title">{{ $t('pe_product') }}<span class="number">{{ detailData.businessNameList.length }}</span></span>
          <span class="item-header-title">{{ $t('pe_system') }}<span class="number">{{ detailData.associationSystems.length }}</span></span>
          <span class="item-header-title">{{ $t('pi_data_confirmTime') }}<span class="number">{{ detailData.lastConfirmTime || '-' }}</span></span>
        </div>
        <card style="margin-top: 5px">
          <div class="content">
            <div class="content-list">
              <span>{{ $t('pe_selected_env') }}</span>
              <Tag>{{ detailData.environmentName }}</Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_select_busProduct') }}</span>
              <Tag v-for="(i, index) in detailData.businessNameList" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_relate_baseProduct') }}</span>
              <Tag v-for="(i, index) in detailData.associationTechProducts || []" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_relate_system') }}</span>
              <Tag v-for="(i, index) in detailData.associationSystems || []" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
          </div>
        </card>
      </BaseHeaderTitle>
    </div>
    <!--角色列表-->
    <div class="item">
      <BaseHeaderTitle :title="$t('role')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ roleSelectionList.length }}</span>
        </div>
        <Table
          :border="false"
          size="small"
          :loading="roleTableLoading"
          :columns="roleTableColumns"
          :max-height="500"
          :data="roleTableData"
          @on-selection-change="selection => handleSelectChange('role', selection)"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--ITSM列表-->
    <div class="item">
      <BaseHeaderTitle :title="$t('pe_itsm')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ itsmSelectionList.length }}</span>
        </div>
        <BaseSearch
          :onlyShowReset="true"
          :options="itsmSearchOptions"
          v-model="itsmSearchParams"
          @search="handleSearchTable('itsm')"
        ></BaseSearch>
        <div style="margin: 10px 0">
          {{ $t('pe_export_library') }}：<i-switch v-model="exportComponentLibrary"></i-switch>
        </div>
        <Table
          :border="false"
          size="small"
          :loading="itsmTableLoading"
          :columns="itsmTableColumns"
          :max-height="500"
          :data="itsmTableData"
          @on-selection-change="selection => handleSelectChange('itsm', selection)"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--编排列表-->
    <div class="item">
      <BaseHeaderTitle :title="$t('m_procDefId')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ flowSelectionList.length }}</span>
        </div>
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
          :max-height="500"
          :data="flowTableData"
          @on-selection-change="selection => handleSelectChange('flow', selection)"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--批量执行列表-->
    <div class="item">
      <BaseHeaderTitle :title="$t('bc_operation')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ batchSelectionList.length }}</span>
        </div>
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
          :max-height="500"
          :data="batchTableData"
          @on-selection-change="selection => handleSelectChange('batch', selection)"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--插件服务-->
    <div class="item">
      <BaseHeaderTitle :title="$t('pluginService')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select_configType') }}<span class="number">{{ detailData.pluginsRes.data.length }}</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="pluginColumns"
                :max-height="500"
                :data="detailData.pluginsRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--CMDB-->
    <div class="item">
      <BaseHeaderTitle title="CMDB" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}CI<span class="number">{{ detailData.cmdbCICount }}</span>
          <span class="name">{{ $t('pe_view') }}</span><span class="number">{{ detailData.cmdbViewCount }}</span> <span class="name">{{ $t('pe_report') }}</span><span class="number">{{ detailData.cmdbReportFormCount }}</span>
        </div>
        <Row :gutter="10">
          <Col :span="8">
            <Card title="CI">
              <Table
                :border="false"
                size="small"
                :columns="cmdbCIColumns"
                :max-height="500"
                :data="detailData.cmdbCIData"
              />
            </Card>
          </Col>
          <Col :span="8">
            <Card :title="$t('pe_view')">
              <Table
                :border="false"
                size="small"
                :columns="cmdbViewColumns"
                :max-height="500"
                :data="detailData.cmdbViewData"
              />
            </Card>
          </Col>
          <Col :span="8">
            <Card :title="$t('pe_report')">
              <Table
                :border="false"
                size="small"
                :columns="cmdbReportColumns"
                :max-height="500"
                :data="detailData.cmdbReportData"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--物料包-->
    <div class="item">
      <BaseHeaderTitle :title="$t('pe_articles')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.artifactsCount }}</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="artifactsColumns"
                :max-height="500"
                :data="detailData.artifactsRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--监控-->
    <div class="item">
      <BaseHeaderTitle :title="$t('pe_monitor_config')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select_configType') }}<span class="number">{{ detailData.monitorRes.data.length }}</span>
          <span class="name">{{ $t('pe_total') }}</span><span class="number">{{ detailData.monitorCount }}</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="monitorColumns"
                :max-height="500"
                :data="detailData.monitorRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <BaseDrawer
      :title="detailTitle"
      :visible.sync="detailVisible"
      realWidth="60%"
      :scrollable="true"
      :maskClosable="false"
    >
      <template slot-scope="{maxHeight}" slot="content">
        <Table :border="false" :columns="detailColumns" :max-height="maxHeight" :data="detailTableData" size="small">
        </Table>
      </template>
    </BaseDrawer>
  </div>
</template>

<script>
import selectTableConfig from '../selection-table'
import staticTableConfig from '../static-table'
import { deepClone } from '@/const/util'
import {
  getRoleList, getAllExportFlows, getAllExportBatch, getAllExportItsm
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
      } else if (type === 'itsm') {
        this.itsmSelectionList = selection
      } else if (type === 'flow') {
        this.flowSelectionList = selection
      } else if (type === 'batch') {
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
      } else if (type === 'flow') {
        this.flowTableData = this.flowOriginTableData.filter(item => {
          const nameFlag = item.name.toLowerCase().indexOf(this.flowSearchParams.name.toLowerCase()) > -1
          const idFlag = item.id.indexOf(this.flowSearchParams.id) > -1
          if (nameFlag && idFlag) {
            return true
          }
        })
      } else if (type === 'batch') {
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
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.roleTableData = data.map(_ => ({
          ..._,
          _checked: true,
          _disabled: true
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
        this.itsmTableData = (data
            && data.map(_ => ({
              ..._,
              _checked: true
            })))
          || []
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
    margin-bottom: 30px;
    position: relative;
    width: 100%;
    &-header {
      display: flex;
      flex-direction: row;
    }
    &-header-title {
      margin-left: 16px;
      display: flex;
      align-items: center;
      .number {
        font-size: 16px;
        font-weight: bold;
        color: #5384ff;
        margin-left: 6px;
        max-width: 200px;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
      }
    }
    .title {
      font-size: 14px;
      font-weight: 600;
      margin-left: 10px;
      .name {
        margin-left: 10px;
      }
      .number {
        font-size: 18px;
        color: #5384ff;
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
    button {
      width: 55px;
      height: 32px;
      line-height: 32px;
    }
  }
  .ivu-table-body {
    overflow: hidden;
  }
  .ivu-table-body:hover {
    overflow: auto;
  }
  .common-ui-header-title .w-content {
    padding: 10px;
  }
}
</style>
