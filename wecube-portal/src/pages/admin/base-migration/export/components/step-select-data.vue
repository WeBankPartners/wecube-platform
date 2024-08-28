<template>
  <div class="export-step-data">
    <div class="export-message">
      <Alert v-if="status === 'waiting'" type="warning" show-icon>
        <template #desc> 正在导出内容，请稍后... </template>
      </Alert>
      <Alert v-else-if="status === 'error'" type="error" show-icon>
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
    <div class="item">
      <span class="title">角色：已选<span class="number">{{ 10 }}</span></span>
      <div>
        <Table
          :border="false"
          size="small"
          :loading="roleTableLoading"
          :columns="roleTableColumns"
          :max-height="500"
          :data="roleTableData"
          @on-selection-change="handleSelectChange('role')"
        >
        </Table>
      </div>
    </div>
    <div class="item">
      <span class="title">编排：已选<span class="number">{{ 10 }}</span></span>
      <div>
        <BaseSearch
          :showBtn="false"
          :options="flowSearchOptions"
          v-model="flowSearchParams"
          @search="getTableList('flow')"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="flowTableLoading"
          :columns="flowTableColumns"
          :max-height="500"
          :data="flowTableData"
          @on-selection-change="handleSelectChange('flow')"
        >
        </Table>
      </div>
    </div>
    <div class="item">
      <span class="title">批量执行：已选<span class="number">{{ 10 }}</span></span>
      <div>
        <BaseSearch
          :showBtn="false"
          :options="flowSearchOptions"
          v-model="flowSearchParams"
          @search="getTableList('flow')"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="flowTableLoading"
          :columns="flowTableColumns"
          :max-height="500"
          :data="flowTableData"
          @on-selection-change="handleSelectChange('flow')"
        >
        </Table>
      </div>
    </div>
    <div class="item">
      <span class="title">ITSM流程：已选<span class="number">{{ 10 }}</span></span>
      <div>
        <BaseSearch
          :showBtn="false"
          :options="flowSearchOptions"
          v-model="flowSearchParams"
          @search="getTableList('flow')"
        ></BaseSearch>
        <Table
          :border="false"
          size="small"
          :loading="flowTableLoading"
          :columns="flowTableColumns"
          :max-height="500"
          :data="flowTableData"
          @on-selection-change="handleSelectChange('flow')"
        >
        </Table>
      </div>
    </div>
    <StaticData></StaticData>
  </div>
</template>

<script>
import StaticData from './static-data.vue'
import tableConfig from '../selection-table'
export default {
  components: {
    StaticData
  },
  mixins: [tableConfig],
  props: {
    status: String
  },
  data() {
    return {}
  },
  methods: {
    handleSelectChange() {},
    getTableList() {}
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
  .ivu-tag {
    height: 28px;
    line-height: 28px;
  }
  .ivu-alert-with-desc .ivu-alert-icon {
    left: 16px;
    margin-top: -20px;
    font-size: 26px;
  }
  .ivu-alert-with-desc.ivu-alert-with-icon {
    padding: 10px 16px 10px 55px;
  }
}
</style>
