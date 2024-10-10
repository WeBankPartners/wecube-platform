<template>
  <div class="base-migration-import-three">
    <div class="import-status">
      <Alert v-if="detailData.initWorkflowRes.status === 'doing'" type="info" show-icon>
        <template #desc>{{ $t('pi_importing_tips') }}... </template>
      </Alert>
      <Alert v-else-if="detailData.initWorkflowRes.status === 'fail'" type="error" show-icon>
        {{ $t('pi_import_fail') }}！
        <template #desc>{{ detailData.initWorkflowRes.errMsg }}</template>
      </Alert>
      <Alert v-else-if="detailData.initWorkflowRes.status === 'success'" type="success" show-icon>
        <template #desc>{{ $t('pi_import_success') }}！</template>
      </Alert>
    </div>
    <Table
      :border="false"
      size="small"
      :columns="tableColumns"
      :max-height="400"
      :data="detailData.initWorkflowRes.data"
    />
    <div class="footer">
      <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
      <Button v-if="['success'].includes(detailData.initWorkflowRes.status)" type="primary" @click="handleNext">{{
        $t('next_step')
      }}</Button>
    </div>
  </div>
</template>

<script>
import { instancesWithPaging, saveImportData } from '@/api/server'
import { debounce } from '@/const/util'
export default {
  props: {
    detailData: Object
  },
  data() {
    return {
      tableColumns: [
        {
          title: this.$t('flow_name'),
          minWidth: 180,
          key: 'procInstName',
          render: (h, params) => (
            <div>
              <span
                style="cursor:pointer;color:#5cadff;"
                onClick={() => {
                  this.jumpToHistory(params.row)
                }}
              >
                {params.row.procInstName}
                <Tag style="margin-left:2px">{params.row.version}</Tag>
              </span>
            </div>
          )
        },
        {
          title: this.$t('fe_flowInstanceId'),
          minWidth: 180,
          key: 'id'
        },
        {
          title: this.$t('flow_status'),
          key: 'status',
          minWidth: 140,
          render: (h, params) => {
            const list = [
              {
                label: this.$t('fe_notStart'),
                value: 'NotStarted',
                color: '#808695'
              },
              {
                label: this.$t('fe_notStart'),
                value: 'InPreparation',
                color: '#808695'
              },
              {
                label: this.$t('fe_inProgressFaulted'),
                value: 'InProgress(Faulted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgressTimeouted'),
                value: 'InProgress(Timeouted)',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_stop'),
                value: 'Stop',
                color: '#ed4014'
              },
              {
                label: this.$t('fe_inProgress'),
                value: 'InProgress',
                color: '#1990ff'
              },
              {
                label: this.$t('fe_completed'),
                value: 'Completed',
                color: '#7ac756'
              },
              {
                label: this.$t('fe_faulted'),
                value: 'Faulted',
                color: '#e29836'
              },
              {
                label: this.$t('fe_internallyTerminated'),
                value: 'InternallyTerminated',
                color: '#e29836'
              }
            ]
            const findObj = list.find(item => item.value === params.row.status) || {}
            return <Tag color={findObj.color}>{findObj.label}</Tag>
          }
        },
        // 操作对象
        {
          title: this.$t('bc_execution_instance'),
          key: 'entityDisplayName',
          minWidth: 160,
          render: (h, params) => {
            if (params.row.entityDisplayName !== '') {
              return <span>{params.row.entityDisplayName}</span>
            }
            return <span>-</span>
          }
        },
        {
          title: this.$t('executor'),
          key: 'operator',
          minWidth: 120
        },
        {
          title: this.$t('execute_date'),
          key: 'createdTime',
          minWidth: 150
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        }
      ]
    }
  },
  methods: {
    // 查看编排详情
    async jumpToHistory(row) {
      const params = {
        id: row.id,
        pageable: {
          startIndex: 0,
          pageSize: 5000
        }
      }
      const { status, data } = await instancesWithPaging(params)
      if (status === 'OK') {
        const detail = Array.isArray(data.contents) && data.contents[0]
        // 能获取到历史记录，跳转，否则给出提示
        if (detail && detail.id) {
          window.sessionStorage.currentPath = ''
          const path = `${window.location.origin}/#/implementation/workflow-execution/view-execution?id=${row.id}&from=normal`
          window.open(path, '_blank')
        }
      }
      this.$Notice.warning({
        title: '',
        desc: this.$t('no_detail_warning')
      })
    },
    handleNext: debounce(async function () {
      if (this.detailData.step > 3 || this.detailData.status === 'success') {
        this.$emit('nextStep')
      } else {
        const params = {
          transImportId: this.detailData.id,
          step: 4
        }
        const { status } = await saveImportData(params)
        if (status === 'OK') {
          // 执行导入，生成ID
          this.$emit('saveStepThree')
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
.base-migration-import-three {
  .import-status {
    margin-top: -10px;
    margin-bottom: 16px;
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
.base-migration-import-three {
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
