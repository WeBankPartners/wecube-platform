<template>
  <div class="base-migration-import-two">
    <div class="export-message">
      <Alert v-if="detailData.stepTwoRes.status === 'doing'" type="info" show-icon>
        <template #desc>{{ $t('pi_importing') }}...</template>
      </Alert>
      <Alert v-else-if="detailData.stepTwoRes.status === 'fail'" type="error" show-icon>
        {{ $t('pi_import_fail') }}！
        <template #desc>{{ detailData.stepTwoRes.errMsg || '' }}</template>
      </Alert>
      <Alert v-else-if="detailData.stepTwoRes.status === 'success'" type="success" show-icon>
        <template #desc>{{ $t('pi_import_success') }}！</template>
      </Alert>
    </div>
    <!--环境产品-->
    <div class="item">
      <BaseHeaderTitle :title="$t('pe_env_product_sysytem')" :fontSize="15">
        <div slot="sub-title" class="item-header">
          <span class="item-header-e">{{ $t('pe_env') }}<span class="number">{{ detailData.environmentName || '-' }}</span></span>
          <span class="item-header-p">{{ $t('pe_product') }}<span class="number">{{ detailData.businessNameList.length }}</span></span>
          <span class="item-header-s">{{ $t('pe_system') }}<span class="number">{{ detailData.associationSystems.length || 0 }}</span></span>
        </div>
        <card style="margin-top: 5px">
          <div class="content">
            <div class="content-list">
              <span>{{ $t('pe_selected_env') }}</span>
              <Tag>{{ detailData.environmentName || '-' }}</Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_select_busProduct') }}</span>
              <Tag v-for="(i, index) in detailData.businessNameList" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_relate_baseProduct') }}</span>
              <Tag v-for="(i, index) in detailData.associationTechProducts" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
            <div class="content-list">
              <span>{{ $t('pe_relate_system') }}</span>
              <Tag v-for="(i, index) in detailData.associationSystems" class="tag" :key="index">
                {{ i }}
              </Tag>
            </div>
          </div>
        </card>
      </BaseHeaderTitle>
    </div>
    <!--角色列表-->
    <div class="item" v-if="detailData.roleRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('role')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.roleRes.data.length }}</span>
          <span v-if="detailData.roleRes.status === 'success'" class="success">({{ $t('pi_import_success') }})</span>
          <span v-if="detailData.roleRes.status === 'fail'" class="fail">({{ $t('pi_import_fail') }}：<span>{{ detailData.roleRes.errMsg }}</span>)</span>
        </div>
        <Table
          :border="false"
          size="small"
          :columns="roleTableColumns"
          :max-height="400"
          :data="detailData.roleRes.data"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--CMDB-->
    <div class="item" v-if="detailData.cmdbRes.status !== 'notStart'">
      <BaseHeaderTitle title="CMDB" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}CI<span class="number">{{ detailData.cmdbCICount }}</span>
          <span class="name">{{ $t('pe_view') }}</span><span class="number">{{ detailData.cmdbViewCount }}</span> <span class="name">{{ $t('pe_report') }}</span><span class="number">{{ detailData.cmdbReportFormCount }}</span>
          <span v-if="detailData.cmdbRes.status === 'success'" class="success">({{ $t('pi_import_success') }})</span>
          <span v-if="detailData.cmdbRes.status === 'fail'" class="fail">({{ $t('pi_import_fail') }}：<span>{{ detailData.cmdbRes.errMsg }}</span>)</span>
        </div>
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
            <Card :title="$t('pe_view')">
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
            <Card :title="$t('pe_report')">
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
      </BaseHeaderTitle>
    </div>
    <!--插件服务-->
    <div class="item" v-if="detailData.pluginsRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('pluginService')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select_configType') }}<span class="number">{{ detailData.pluginsRes.data.length }}</span>
          <span v-if="detailData.pluginsRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.pluginsRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.pluginsRes.errMsg }}</span>)</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="pluginColumns"
                :max-height="400"
                :data="detailData.pluginsRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--编排列表-->
    <div class="item" v-if="detailData.flowRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('m_procDefId')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.flowRes.data.length }}</span>
          <span v-if="detailData.flowRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.flowRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.flowRes.errMsg }}</span>)</span>
        </div>
        <Table
          :border="false"
          size="small"
          :columns="flowTableColumns"
          :max-height="400"
          :data="detailData.flowRes.data"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--批量执行列表-->
    <div class="item" v-if="detailData.batchRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('bc_operation')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.batchRes.data.length }}</span>
          <span v-if="detailData.batchRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.batchRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.batchRes.errMsg }}</span>)</span>
        </div>
        <Table
          :border="false"
          size="small"
          :columns="batchTableColumns"
          :max-height="400"
          :data="detailData.batchRes.data"
        >
        </Table>
      </BaseHeaderTitle>
    </div>
    <!--物料包-->
    <div class="item" v-if="detailData.artifactsRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('pe_articles')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.artifactsCount }}</span>
          <span v-if="detailData.artifactsRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.artifactsRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.artifactsRes.errMsg }}</span>)</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="artifactsColumns"
                :max-height="400"
                :data="detailData.artifactsRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--监控-->
    <div class="item" v-if="detailData.monitorRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('pe_monitor_config')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select_configType') }}<span class="number">{{ detailData.monitorRes.data.length }}</span>
          <span class="name">{{ $t('pe_total') }}</span><span class="number">{{ detailData.monitorCount }}</span>
          <span v-if="detailData.monitorRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.monitorRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.monitorRes.errMsg }}</span>)</span>
        </div>
        <Row :gutter="10">
          <Col :span="16">
            <Card>
              <Table
                :border="false"
                size="small"
                :columns="monitorColumns"
                :max-height="400"
                :data="detailData.monitorRes.data"
              />
            </Card>
          </Col>
        </Row>
      </BaseHeaderTitle>
    </div>
    <!--ITSM列表-->
    <div class="item" v-if="detailData.itsmRes.status !== 'notStart'">
      <BaseHeaderTitle :title="$t('pe_itsm')" :fontSize="15">
        <div slot="sub-title" class="title">
          {{ $t('pe_select') }}<span class="number">{{ detailData.itsmRes.data.length }}</span>
          <span v-if="detailData.itsmRes.status === 'success'" class="success">({{ $t('pe_export_success') }})</span>
          <span v-if="detailData.itsmRes.status === 'fail'" class="fail">({{ $t('pe_export_fail') }}：<span>{{ detailData.itsmRes.errMsg }}</span>)</span>
        </div>
        <div style="margin: 10px 0">
          {{ $t('pe_export_library') }}：<i-switch disabled v-model="detailData.componentLibraryRes.exportComponentLibrary"></i-switch>
        </div>
        <div>
          <Table
            :border="false"
            size="small"
            :columns="itsmTableColumns"
            :max-height="400"
            :data="detailData.itsmRes.data"
          >
          </Table>
        </div>
      </BaseHeaderTitle>
    </div>
    <div class="footer">
      <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
      <Button v-if="['success'].includes(detailData.stepTwoRes.status)" type="primary" @click="handleNext">{{
        $t('next_step')
      }}</Button>
    </div>
  </div>
</template>

<script>
import selectTableConfig from '../../export/selection-table'
import staticTableConfig from '../../export/static-table'
import { saveImportData } from '@/api/server'
import { debounce } from '@/const/util'

export default {
  mixins: [selectTableConfig, staticTableConfig],
  props: {
    detailData: Object
  },
  data() {
    return {
      // 监控列表
      monitorColumns: [
        {
          title: this.$t('data_type'),
          render: (h, params) => {
            const nameMap = {
              monitor_type: this.$t('p_general_type'), // 基础类型
              endpoint_group: this.$t('p_endpoint_group'), // 对象组
              log_monitor_template: this.$t('p_log_monitor_template'), // 指标-业务日志模版
              log_monitor_service_group: this.$t('p_log_monitor_config'), // 指标-业务配置
              logKeyword_service_group: this.$t('p_keyword_list'), // 告警关键字
              dashboard: this.$t('p_dashboard'), // 自定义看板
              endpoint: this.$t('p_endpoint'), // 对象(仅分析)
              service_group: this.$t('p_endpoint_level'), // 层级对象(仅分析)
              custom_metric_monitor_type: this.$t('p_metric_monitor_type'),
              custom_metric_endpoint_group: this.$t('p_metric_endpoint_group'),
              custom_metric_service_group: this.$t('p_metric_service_group'),
              strategy_service_group: this.$t('p_strategy_service_group'),
              strategy_endpoint_group: this.$t('p_strategy_endpoint_group')
            }
            return (
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.jumpToHistory(params.row)
                }}
              >
                {nameMap[params.row.name] || '-'}
              </span>
            )
          }
        },
        {
          title: this.$t('pe_monitor_query'),
          key: 'conditions',
          render: (h, params) => {
            const conditionsMap = {
              monitor_type: this.$t('p_monitor_type_des'),
              endpoint: this.$t('p_endpoint_des'),
              endpoint_group: this.$t('p_endpoint_group_des'),
              service_group: this.$t('p_service_group_des'),
              log_monitor_template: this.$t('p_log_monitor_template_des'),
              log_monitor_service_group: this.$t('p_log_monitor_service_group_des'),
              logKeyword_service_group: this.$t('p_logKeyword_service_group_des'),
              dashboard: this.$t('p_dashboard_des'),
              custom_metric_monitor_type: this.$t('p_custom_metric_monitor_type_des'),
              custom_metric_endpoint_group: this.$t('p_custom_metric_endpoint_group_des'),
              custom_metric_service_group: this.$t('p_custom_metric_service_group_des'),
              strategy_service_group: this.$t('p_strategy_service_group_des'),
              strategy_endpoint_group: this.$t('p_strategy_endpoint_group_des')
            }
            return <span>{conditionsMap[params.row.name] || '-'}</span>
          }
        },
        {
          title: this.$t('pe_select'),
          key: 'total',
          width: 100,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.count}</div>
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  this.handleDetai(params.row, 'monitor')
                }}
              />
            </span>
          )
        }
      ]
    }
  },
  mounted() {
    // 去掉表格复选框
    this.roleTableColumns.splice(0, 1)
    this.flowTableColumns.splice(0, 1)
    this.batchTableColumns.splice(0, 1)
    this.itsmTableColumns.splice(0, 1)
  },
  methods: {
    handleNext: debounce(async function () {
      if (this.detailData.step > 2 || this.detailData.status === 'success') {
        this.$emit('nextStep')
      } else {
        const params = {
          transImportId: this.detailData.id,
          step: 3
        }
        const { status } = await saveImportData(params)
        if (status === 'OK') {
          // 执行导入
          this.$emit('saveStepTwo')
        }
      }
    }, 500),
    // 上一步
    handleLast() {
      this.$emit('lastStep')
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-two {
  .export-message {
    margin-top: -10px;
    margin-bottom: 16px;
  }
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 30px;
    width: 100%;
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
          font-weight: bold;
          color: #2d8cf0;
          margin-left: 6px;
        }
      }
    }
    .title {
      font-size: 14px;
      font-weight: 600;
      .name {
        margin-left: 10px;
      }
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
      .success {
        color: #19be6b;
      }
      .fail {
        color: #ed4014;
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
  .footer {
    position: fixed;
    bottom: 10px;
    display: flex;
    justify-content: center;
    width: calc(100% - 460px);
    button {
      &:not(:first-child) {
        margin-left: 10px;
      }
    }
  }
}
</style>
<style lang="scss">
.base-migration-import-two {
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
  .ivu-table-body {
    overflow: hidden;
  }
  .ivu-table-body:hover {
    overflow: auto;
  }
}
</style>
