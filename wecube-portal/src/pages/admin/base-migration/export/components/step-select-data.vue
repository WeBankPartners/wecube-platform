<template>
  <div class="export-step-data">
    <div class="export-message">
      <Alert v-if="status === 'doing'" type="warning" show-icon>
        <template #desc> 正在导出内容，请稍后... </template>
      </Alert>
      <Alert v-else-if="status === 'fail'" type="error" show-icon>
        导出失败！
        <template #desc>
          角色已选3条数据,导出失败1条数据,角色-【角色key】失败,失败信息:xxxx请查看导出信息,修复问题之后重新发起导入
        </template>
      </Alert>
      <Alert v-else-if="status === 'success'" type="success" show-icon>
        导出成功！
        <template #desc>
          <div>
            恭喜！全部数据已导出成功,nexus链接:
            <span class="link">http://106.52.160.142:9000/minio/wecube-plugin-package-bucket/</span>
            <Icon type="md-copy" size="22" color="#2d8cf0" />
          </div>
          <div>请打开需要迁移环境的wecube,进入【系统-一键迁移-一键导入】,粘贴当前链接,执行导入</div>
        </template>
      </Alert>
    </div>
    <div class="item">
      <div class="item-header">
        <span class="item-header-t">环境产品系统<Icon type="ios-information-circle" size="20" /></span>
        <span class="item-header-e">环境<span class="number">DEV_开发环境</span></span>
        <span class="item-header-p">产品<span class="number">10</span></span>
        <span class="item-header-s">系统<span class="number">100</span></span>
      </div>
      <card style="margin-top: 5px">
        <div class="content">
          <div class="content-list">
            <span>已选环境</span>
            <Tag>DEV_开发环境</Tag>
          </div>
          <div class="content-list">
            <span>已选业务产品</span>
            <Tag class="tag">零售新产品</Tag>
            <Tag class="tag">外汇新产品</Tag>
            <Tag class="tag">存款新产品</Tag>
          </div>
          <div class="content-list">
            <span>关联底座产品(自动分析)</span>
            <Tag class="tag">零售新产品</Tag>
            <Tag class="tag">外汇新产品</Tag>
            <Tag class="tag">存款新产品</Tag>
          </div>
          <div class="content-list">
            <span>关联系统(自动分析)</span>
            <Tag class="tag">零售新产品</Tag>
            <Tag class="tag">外汇新产品</Tag>
            <Tag class="tag">存款新产品</Tag>
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
          @on-selection-change="selection => handleSelectChange('execution', selection)"
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
    <div class="item">
      <span class="title">
        CMDB：<span class="sub-title">
          已选 CI<span class="number">{{ 10 }}</span> 视图<span class="number">{{ 10 }}</span> 报表<span
            class="number"
          >{{ 10 }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="10">
          <Card title="CI">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbData"> </Table>
          </Card>
        </Col>
        <Col :span="7">
          <Card title="视图">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbData"> </Table>
          </Card>
        </Col>
        <Col :span="7">
          <Card title="报表">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbData"> </Table>
          </Card>
        </Col>
      </Row>
    </div>
    <div class="item">
      <span class="title">
        物料包：已选<span class="number">{{ 10 }}</span>
      </span>
      <Row :gutter="10">
        <Col :span="17">
          <Card>
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbData"> </Table>
          </Card>
        </Col>
      </Row>
    </div>
    <div class="item">
      <span class="title">
        监控配置：<span class="sub-title">
          已选配置类型<span class="number">{{ 10 }}</span> 总条数<span class="number">{{ 10 }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="17">
          <Card>
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbData"> </Table>
          </Card>
        </Col>
      </Row>
    </div>
  </div>
</template>

<script>
import selectTableConfig from '../selection-table'
import staticTableConfig from '../static-table'
import {
  getCurrentUserRoles, getAllExportFlows, getAllExportBatch, getAllExportItsm
} from '@/api/server.js'
export default {
  mixins: [selectTableConfig, staticTableConfig],
  props: {
    status: String
  },
  data() {
    return {}
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
    },
    // 表格搜索
    handleSearchTable(type) {
      if (type === 'itsm') {
        //
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
        this.itsmSelectionList = this.itsmTableData
        this.itsmSelectionList.push({
          id: 'taskman_component_library',
          name: '组件库',
          _checked: true
        })
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
        margin-right: 10px;
      }
    }
  }
}
</style>
<style lang="scss">
.export-step-data {
  .ivu-card-body {
    padding: 12px;
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
