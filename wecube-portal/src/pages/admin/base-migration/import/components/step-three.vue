<template>
  <div class="base-migration-import-three">
    <div class="import-status">
      <Alert v-if="detailData.initWorkflowRes.status === 'doing'" type="info" show-icon>
        <template #desc>正在导入内容，请稍后... </template>
      </Alert>
      <Alert v-else-if="detailData.initWorkflowRes.status === 'fail'" type="error" show-icon>
        <template #desc>导入失败！</template>
      </Alert>
      <Alert v-else-if="detailData.initWorkflowRes.status === 'success'" type="success" show-icon>
        <template #desc>导入成功！</template>
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
      <Button v-if="['doing', 'fail'].includes(detailData.status)" type="error" @click="handleStop">终止</Button>
      <Button v-if="['fail'].includes(detailData.status)" type="success" @click="handleRetry">重试</Button>
      <Button type="default" @click="handleLast">上一步</Button>
      <Button v-if="['success'].includes(detailData.initWorkflowRes.status)" type="primary" @click="handleNext">下一步</Button>
    </div>
  </div>
</template>

<script>
import { instancesWithPaging, saveImportData } from '@/api/server'
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
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 130,
          align: 'center',
          fixed: 'right',
          render: (h, params) => (
            <div style="display:flex;justify-content:center;">
              <Tooltip content={this.$t('be_details')} placement="top">
                <Button
                  size="small"
                  type="info"
                  onClick={() => {
                    this.jumpToHistory(params.row) // 查看
                  }}
                  style="margin-right:5px;"
                >
                  <Icon type="md-eye" size="16"></Icon>
                </Button>
              </Tooltip>
            </div>
          )
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
    handleStop() {},
    handleRetry() {},
    handleLast() {
      this.$emit('lastStep')
    },
    async handleNext() {
      if (this.detailData.step > 3) {
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
