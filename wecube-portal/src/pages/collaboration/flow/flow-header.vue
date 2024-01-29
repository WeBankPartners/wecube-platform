<template>
  <div>
    <div style="display: flex; justify-content: space-between">
      <div class="flow-name" @click="openCanvasPanel">
        <span style="vertical-align: middle">{{ itemCustomInfo.name }}</span>
        <Tag>v{{ itemCustomInfo.version }}</Tag>
        <img src="../../../assets/icon/edit-black.png" style="width: 16px; vertical-align: middle" alt="" />
      </div>
      <div>
        <Button type="primary" ghost @click="backToFlowList">
          <Icon type="ios-undo-outline" size="16"></Icon>
          {{ $t('return') }}
        </Button>
        <template v-if="editFlow !== 'false'">
          <Button type="primary" v-if="['draft'].includes(itemCustomInfo.status)" @click="releaseFlow">
            <Icon type="ios-paper-plane-outline" size="16"></Icon>
            {{ $t('release_flow') }}
          </Button>
          <Button type="success" v-if="['deployed'].includes(itemCustomInfo.status)" @click="exportFlow">
            <img src="../../../assets/icon/export.png" class="btn-img" alt="" />
            {{ $t('export_flow') }}{{ editFlow }}
          </Button>
          <Button type="warning" v-if="['draft', 'deployed'].includes(itemCustomInfo.status)" @click="changePermission">
            <Icon type="ios-person-outline" size="16"></Icon>
            {{ $t('config_permission') }}
          </Button>
          <Button
            type="error"
            v-if="['deployed'].includes(itemCustomInfo.status)"
            @click="changeStatus('disabled', 'disable')"
          >
            <img src="../../../assets/icon/disable.png" style="width: 16px; vertical-align: middle" alt="" />
            {{ $t('disable') }}
          </Button>
          <Button
            type="success"
            v-if="['disabled'].includes(itemCustomInfo.status)"
            @click="changeStatus('enabled', 'enable')"
          >
            <img src="../../../assets/icon/enable.png" style="width: 16px; vertical-align: middle" alt="" />
            {{ $t('enable') }}
          </Button>
        </template>
      </div>
    </div>

    <FlowAuth ref="flowAuthRef" @sendAuth="updateAuth"></FlowAuth>
  </div>
</template>

<script>
import axios from 'axios'
import { getCookie } from '@/pages/util/cookie'
import dayjs from 'dayjs'
import FlowAuth from '@/pages/components/auth.vue'
import { flowBatchChangeStatus, flowRelease } from '@/api/server.js'
export default {
  components: {
    FlowAuth
  },
  data () {
    return {
      editFlow: true, // 在查看时隐藏按钮
      itemCustomInfo: {}
    }
  },
  methods: {
    showItemInfo (data, editFlow) {
      this.editFlow = editFlow
      const defaultNode = {
        id: '',
        label: '', // 编排名称
        version: 1, // 版本
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
      this.itemCustomInfo.version = Number(this.itemCustomInfo.version) || 1
    },
    // #region
    // 修改权限
    changePermission () {
      this.$refs.flowAuthRef.startAuth(
        this.itemCustomInfo.permissionToRole.MGMT,
        this.itemCustomInfo.permissionToRole.USE
      )
    },
    updateAuth (mgmt, use) {
      this.$emit('updateAuth', mgmt, use)
    },
    // #endregion
    openCanvasPanel () {
      this.$emit('openCanvasPanel', '')
    },
    // 发布编排
    async releaseFlow () {
      const { status } = await flowRelease(this.itemCustomInfo.id)
      if (status === 'OK') {
        this.$Message.success(this.$t('release_flow') + this.$t('action_successful'))
        // this.$emit('updateFlowData', '')
        this.backToFlowList()
      }
    },
    async changeStatus (statusCode, actionTip) {
      const statusToTip = {
        disabled: {
          title: this.$t('disable'),
          content: `确认禁用编排: [${this.itemCustomInfo.name}] 吗?`
        },
        deleted: {
          title: this.$t('delete'),
          content: `确认删除编排: [${this.itemCustomInfo.name}] 吗?`
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
          let { status } = await flowBatchChangeStatus(data)
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
    async exportFlow () {
      const accessToken = getCookie('accessToken')
      const headers = {
        Authorization: 'Bearer ' + accessToken
      }
      axios({
        method: 'post',
        url: `platform/v1/process/definitions/export`,
        headers: headers,
        data: {
          procDefIds: this.itemCustomInfo.id
        },
        responseType: 'blob'
      })
        .then(response => {
          if (response.status < 400) {
            let fileName = `${this.itemCustomInfo.name}_${dayjs().format('YYMMDDHHmmss')}.json`
            let blob = new Blob([response.data])
            if ('msSaveOrOpenBlob' in navigator) {
              window.navigator.msSaveOrOpenBlob(blob, fileName)
            } else {
              if ('download' in document.createElement('a')) {
                // 非IE下载
                let elink = document.createElement('a')
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
    backToFlowList () {
      this.$router.push({ path: '/collaboration/workflow' })
    }
  }
}
</script>

<style scoped lang="scss">
.flow-name {
  line-height: 32px;
  cursor: pointer;
}

.btn-img {
  width: 16px;
  vertical-align: middle;
}
</style>
