<template>
  <Modal
    v-model="configTreeManageModal"
    width="700"
    :title="$t('p_batch_export')"
    :mask-closable="false"
    @on-ok="setConfigTreeHandler"
    @on-cancel="closeTreeModal"
    @on-visible-change="onVisibleChange"
  >
    <Checkbox v-model="isSelectAll" @on-change="selectOrCancelAll" border>
      {{ $t('select_cancel_all') }}
    </Checkbox>
    <div style="height: 500px; overflow: auto">
      <Tree ref="configTree" :data="configTree" show-checkbox multiple></Tree>
    </div>
  </Modal>
</template>

<script>
import { getConfigByPkgId, batchExportConfig } from '@/api/server'

export default {
  name: '',
  props: {
    pluginId: {
      type: String,
      default: ''
    },
    isBatchModalShow: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    isBatchModalShow(val) {
      if (typeof val === 'boolean') {
        this.configTreeManageModal = val
        this.isSelectAll = false
        if (val) {
          this.getConfigByPkgId()
        }
      }
    }
  },
  data() {
    return {
      configTree: [],
      configTreeManageModal: false,
      isSelectAll: false
    }
  },
  methods: {
    async setConfigTreeHandler() {
      const payload = this.$refs.configTree.data.map(_ => ({
        ..._,
        pluginConfigs: _.children.map(child => ({
          ...child,
          status: child.checked ? 'ENABLED' : 'DISABLED'
        }))
      }))
      await batchExportConfig(this.pluginId, payload)
      this.$Notice.success({
        title: 'Success',
        desc: 'Success'
      })
      this.closeModal()
    },
    closeTreeModal() {
      this.closeModal()
    },
    onVisibleChange(state) {
      if (!state) {
        this.closeModal()
      }
    },
    closeModal() {
      this.isSelectAll = false
      this.$emit('close')
    },
    selectOrCancelAll(val) {
      this.isSelectAll = val
      this.$nextTick(() => {
        this.configTree.forEach(parent => {
          if (!parent.disabled) {
            parent.checked = val
            parent.expand = false
            if (parent.children.length > 0) {
              parent.children.forEach(child => {
                if (!child.disabled) {
                  child.checked = val
                }
              })
            }
          }
        })
        this.configTree.forEach(parent => {
          parent.expand = true
        })
      })
    },
    async getConfigByPkgId() {
      const { status, data } = await getConfigByPkgId(this.pluginId)
      if (status === 'OK') {
        this.configTree = data.map(_ => {
          const hasPermission = _.pluginConfigs.find(i => i.hasMgmtPermission === true)
          return {
            ..._,
            title: _.name,
            expand: true,
            disabled: !hasPermission,
            children: _.pluginConfigs.map(config => ({
              ...config,
              title: `${config.name}-(${config.registerName})`,
              expand: true,
              checked: config.status === 'ENABLED',
              disabled: !config.hasMgmtPermission
            }))
          }
        })
      }
    }
  }
}
</script>
