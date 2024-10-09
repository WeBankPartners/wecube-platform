<template>
  <div class="base-migration-import-four">
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
        <Button v-if="['success'].includes(detailData.monitorBusinessRes.status)" type="primary" @click="handleComplete">{{ $t('pi_complete_import') }}</Button>
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
      // 监控数据
      monitorColumns: [
        {
          title: this.$t('data_type'),
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {this.$t(`m_${params.row.name}`) || '-'}
            </span>
          )
        },
        {
          title: this.$t('pe_monitor_query'),
          key: 'conditions',
          render: (h, params) => <span>{params.row.conditions || '-'}</span>
        },
        {
          title: this.$t('pe_select'),
          key: 'total',
          width: 100,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.count}</div>
              <Icon type="ios-list" size="36" style="cursor:pointer;" />
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
        this.$emit('saveStepFour')
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
.base-migration-import-four {
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
.base-migration-import-four {
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
