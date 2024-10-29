<template>
  <div class="base-migration-import-five">
    <div v-if="detailData.status === 'success'" class="import-status">
      <Alert type="success" show-icon>
        <template #desc>{{ $t('pi_all_successTips') }}！</template>
      </Alert>
    </div>
    <div v-else class="import-status">
      <Alert v-if="detailData.monitorBusinessRes.status === 'doing'" type="info" show-icon>
        <template #desc>{{ $t('pi_importing_tips') }}... </template>
      </Alert>
      <Alert v-else-if="detailData.monitorBusinessRes.status === 'fail'" type="error" show-icon>
        {{ $t('pi_import_fail') }}！
        <template #desc>{{ detailData.monitorBusinessRes.errMsg }}</template>
      </Alert>
      <Alert v-else-if="detailData.monitorBusinessRes.status === 'success'" type="success" show-icon>
        <template #desc>{{ $t('pi_import_success') }}！</template>
      </Alert>
    </div>
    <div class="item">
      <span class="title">
        {{ $t('pe_monitor_config') }}：<span class="sub-title">{{ $t('pe_select_configType') }}<span class="number">{{ detailData.monitorBusinessRes.data.length }}</span>
          <span class="name">{{ $t('pe_total') }}</span><span class="number">{{ detailData.monitorBusinessCount }}</span>
        </span>
      </span>
      <Table
        :border="false"
        size="small"
        :columns="monitorColumns"
        :max-height="maxHeight"
        :data="detailData.monitorBusinessRes.data"
      />
    </div>
    <div class="footer">
      <template v-if="detailData.status !== 'success'">
        <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
        <Button
          v-if="['success'].includes(detailData.monitorBusinessRes.status)"
          type="primary"
          @click="handleComplete"
        >{{ $t('pi_complete_import') }}</Button>
      </template>
      <template v-else>
        <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
        <Button type="default" @click="handleToHistory">{{ $t('pe_history_list') }}</Button>
      </template>
    </div>
  </div>
</template>

<script>
import { updateImportStatus } from '@/api/server'
import { debounce } from '@/const/util'
export default {
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
      ],
      maxHeight: 500
    }
  },
  mounted() {
    this.maxHeight = document.body.clientHeight - 280
  },
  methods: {
    // 完成导入
    handleComplete: debounce(async function () {
      const params = {
        transImportId: this.detailData.id,
        status: 'completed'
      }
      const { status } = await updateImportStatus(params)
      if (status === 'OK') {
        this.$emit('saveStepFive')
      }
    }, 500),
    // 上一步
    handleLast() {
      this.$emit('lastStep')
    },
    // 跳转到历史列表
    handleToHistory() {
      this.$router.push({
        path: '/admin/base-migration/import-history'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-five {
  .import-status {
    margin-top: -10px;
    margin-bottom: 16px;
  }
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
    padding-left: 12px;
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
.base-migration-import-five {
  .ivu-alert-with-desc .ivu-alert-icon {
    left: 16px;
    top: 26px;
    font-size: 28px;
  }
  .ivu-alert-with-desc.ivu-alert-with-icon {
    padding: 10px 16px 10px 55px;
  }
}
</style>
