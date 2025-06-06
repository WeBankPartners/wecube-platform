<template>
  <div>
    <div style="display: flex; justify-content: space-between">
      <div class="flow-name" @click="openCanvasPanel">
        <Icon size="22" type="md-arrow-back" class="back-icon" @click="backToFlowList"></Icon>
        <span class="flow-name">{{ itemCustomInfo.name }}</span>
        <Tag>{{ itemCustomInfo.version }}</Tag>
        <img src="../../../assets/icon/edit-black.png" style="width: 16px; vertical-align: middle" alt="" />
      </div>
      <div>
        <template v-if="editFlow !== 'false'">
          <Button type="primary" v-if="['draft'].includes(itemCustomInfo.status) && !nodeHasAlert" @click="releaseFlow">
            <Icon type="ios-paper-plane-outline" size="16"></Icon>
            {{ $t('release_flow') }}
          </Button>
          <Button
            type="warning"
            v-if="['draft', 'deployed'].includes(itemCustomInfo.status)"
            @click="changePermission"
            class="btn-gap"
          >
            <Icon type="ios-person-outline" size="16"></Icon>
            {{ $t('config_permission') }}
          </Button>
          <Button
            type="error"
            v-if="['deployed'].includes(itemCustomInfo.status)"
            @click="changeStatus('disabled', 'disable')"
            class="btn-gap"
          >
            <img src="../../../assets/icon/disable.png" style="width: 16px; vertical-align: middle" alt="" />
            {{ $t('disable') }}
          </Button>
          <Button
            type="success"
            v-if="['disabled'].includes(itemCustomInfo.status)"
            @click="changeStatus('enabled', 'enable')"
            class="btn-gap"
          >
            <img src="../../../assets/icon/enable.png" style="width: 16px; vertical-align: middle" alt="" />
            {{ $t('enable') }}
          </Button>
          <Button class="btn-upload btn-gap" v-if="['deployed'].includes(itemCustomInfo.status)" @click="exportFlow">
            <img src="@/assets/icon/DownloadOutlined.svg" class="upload-icon" />
            {{ $t('export_flow') }}{{ editFlow }}
          </Button>
        </template>
      </div>
    </div>

    <FlowAuth ref="flowAuthRef" :useRolesRequired="true" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import axios from 'axios'
import dayjs from 'dayjs'
import FlowAuth from '@/pages/components/auth.vue'
import { getCookie } from '@/pages/util/cookie'
import { flowBatchChangeStatus, flowRelease } from '@/api/server.js'

export const custom_api_enum = [
  {
    "url": "/platform/v1/process/definitions/export",
    "method": "post"
  }
]
export default {
  components: {
    FlowAuth
  },
  data() {
    return {
      nodeHasAlert: false,
      flowListTab: '', // 对应编排列表状态tab
      subProc: '', // main主编排、sub子编排
      editFlow: true, // 在查看时隐藏按钮
      itemCustomInfo: {}
    }
  },
  methods: {
    showItemInfo(data, editFlow, flowListTab) {
      this.nodeHasAlert = false
      this.editFlow = editFlow
      this.flowListTab = flowListTab
      this.subProc = data.subProc ? 'sub' : 'main'
      const defaultNode = {
        id: '',
        label: '', // 编排名称
        version: '', // 版本
        rootEntity: '', // 操作对象
        scene: '', // 使用场景，请求、发布、其他
        authPlugins: [], // 授权插件列表，taskman/monitor
        tags: '', // 标签
        conflictCheck: true, // 冲突检测
        permissionToRole: {
          MGMT: [], // 属主角色
          USE: [] // 使用角色
        }
      }
      const tmpData = JSON.parse(JSON.stringify(data))
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(defaultNode)
      keys.forEach(k => {
        this.itemCustomInfo[k] = tmpData[k]
      })
    },
    // #region
    // 修改权限
    changePermission() {
      this.$refs.flowAuthRef.startAuth(
        this.itemCustomInfo.permissionToRole.MGMT,
        this.itemCustomInfo.permissionToRole.USE
      )
    },
    updateAuth(mgmt, use) {
      this.$emit('updateAuth', mgmt, use)
    },
    // #endregion
    openCanvasPanel() {
      this.$emit('openCanvasPanel', '')
    },
    // 发布编排
    async releaseFlow() {
      const { status } = await flowRelease(this.itemCustomInfo.id)
      if (status === 'OK') {
        this.$Message.success(this.$t('release_flow') + this.$t('action_successful'))
        this.$router.push({
          path: '/collaboration/workflow',
          query: {
            flowListTab: 'deployed',
            subProc: this.subProc
          }
        })
      }
    },
    async changeStatus(statusCode, actionTip) {
      const statusToTip = {
        disabled: {
          title: this.$t('disable'),
          content: `确认禁用编排: [${this.itemCustomInfo.name}] 吗?`
        },
        enabled: {
          title: this.$t('enable'),
          content: `确认启用编排: [${this.itemCustomInfo.name}] 吗?`
        }
      }

      this.$Modal.confirm({
        title: statusToTip[statusCode].title,
        content: statusToTip[statusCode].content,
        onOk: async () => {
          const data = {
            procDefIds: [this.itemCustomInfo.id],
            status: statusCode
          }
          const { status } = await flowBatchChangeStatus(data)
          if (status === 'OK') {
            this.$Message.success(this.$t(actionTip) + this.$t('action_successful'))
            if (statusCode === 'deleted') {
              this.backToFlowList()
            } else {
              this.$emit('updateFlowData', '')
            }
          }
        },
        onCancel: () => {}
      })
    },
    async exportFlow() {
      const accessToken = getCookie('accessToken')
      const headers = {
        Authorization: 'Bearer ' + accessToken
      }
      axios({
        method: 'post',
        url: 'platform/v1/process/definitions/export',
        headers,
        data: {
          procDefIds: this.itemCustomInfo.id
        },
        responseType: 'blob'
      })
        .then(response => {
          if (response.status < 400) {
            const fileName = `${this.itemCustomInfo.name}_${dayjs().format('YYMMDDHHmmss')}.json`
            const blob = new Blob([response.data])
            if ('msSaveOrOpenBlob' in navigator) {
              window.navigator.msSaveOrOpenBlob(blob, fileName)
            } else {
              if ('download' in document.createElement('a')) {
                // 非IE下载
                const elink = document.createElement('a')
                elink.download = fileName
                elink.style.display = 'none'
                elink.href = URL.createObjectURL(blob)
                document.body.appendChild(elink)
                elink.click()
                URL.revokeObjectURL(elink.href) // 释放URL 对象
                document.body.removeChild(elink)
              } else {
                // IE10+下载
                navigator.msSaveOrOpenBlob(blob, fileName)
              }
            }
          }
        })
        .catch(() => {
          this.$Message.warning('Error')
        })
    },
    backToFlowList() {
      this.$router.push({
        path: '/collaboration/workflow',
        query: {
          flowListTab: this.flowListTab,
          subProc: this.subProc
        }
      })
    },
    hideReleaseBtn() {
      this.nodeHasAlert = true
    }
  }
}
</script>

<style scoped lang="scss">
// .flow-name {
//   line-height: 32px;
//   cursor: pointer;
// }

.btn-img {
  width: 16px;
  vertical-align: middle;
}
.back-icon {
  cursor: pointer;
  width: 28px;
  height: 24px;
  color: #fff;
  border-radius: 2px;
  background: #5384ff;
  margin-right: 8px;
}
.flow-name {
  font-size: 14px;
  display: flex;
  align-items: center;
}
</style>
