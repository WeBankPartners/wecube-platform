<template>
  <Modal
    v-model="flowRoleManageModal"
    width="800"
    :title="$t('role_drawer_title')"
    :mask-closable="false"
    class="platform-base-role-transfer"
  >
    <div style="width: 100%; overflow-x: auto">
      <div style="min-width: 760px" class="content">
        <div>
          <div class="role-transfer-title">{{ $t('mgmt_role') }}</div>
          <Transfer
            :titles="transferTitles"
            :list-style="transferStyle"
            :data="currentUserRoles"
            :target-keys="mgmtRolesKeyToFlow"
            :render-format="renderRoleNameForTransfer"
            @on-change="handleMgmtRoleTransferChange"
            filterable
          ></Transfer>
        </div>
        <div style="margin-top: 30px">
          <div class="role-transfer-title">{{ $t('use_role') }}</div>
          <Transfer
            :titles="transferTitles"
            :list-style="transferStyle"
            :data="allRoles"
            :target-keys="useRolesKeyToFlow"
            :render-format="renderRoleNameForTransfer"
            @on-change="handleUseRoleTransferChange"
            filterable
          ></Transfer>
        </div>
      </div>
    </div>
    <div slot="footer">
      <Button type="primary" :disabled="disabled" @click="confirmRole">{{ $t('bc_confirm') }}</Button>
    </div>
  </Modal>
</template>
<script>
import { getRoleList, getCurrentUserRoles } from '@/api/server.js'
export default {
  props: {
    useRolesRequired: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isAdd: false, // 标记编排状态
      flowRoleManageModal: false, // 权限弹窗控制
      transferTitles: [this.$t('unselected_role'), this.$t('selected_role')],
      transferStyle: { width: '300px' },
      allRoles: [],
      currentUserRoles: [],
      mgmtRolesKeyToFlow: [], // 管理角色
      useRolesKeyToFlow: [] // 使用角色
    }
  },
  computed: {
    disabled() {
      if (this.useRolesRequired) {
        return this.mgmtRolesKeyToFlow.length === 0 || this.useRolesKeyToFlow.length === 0
      }

      return this.mgmtRolesKeyToFlow.length === 0
    }
  },
  methods: {
    renderRoleNameForTransfer(item) {
      return item.label
    },
    handleMgmtRoleTransferChange(newTargetKeys) {
      if (newTargetKeys.length > 1) {
        this.$Message.warning(this.$t('chooseOne'))
      } else {
        this.mgmtRolesKeyToFlow = newTargetKeys
      }
    },
    handleUseRoleTransferChange(newTargetKeys) {
      this.useRolesKeyToFlow = newTargetKeys
    },
    async confirmRole() {
      this.$emit('sendAuth', this.mgmtRolesKeyToFlow, this.useRolesKeyToFlow)
      this.flowRoleManageModal = false
    },
    async getRoleList() {
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.allRoles = data.map(_ => ({
          ..._,
          key: _.name,
          label: _.displayName
        }))
      }
    },
    async getCurrentUserRoles() {
      const { status, data } = await getCurrentUserRoles()
      if (status === 'OK') {
        this.currentUserRoles = data.map(_ => ({
          ..._,
          key: _.name,
          label: _.displayName
        }))
      }
    },
    // 启动入口
    async startAuth(mgmtRolesKeyToFlow, useRolesKeyToFlow) {
      this.mgmtRolesKeyToFlow = mgmtRolesKeyToFlow
      this.useRolesKeyToFlow = useRolesKeyToFlow
      await this.getRoleList()
      await this.getCurrentUserRoles()
      this.flowRoleManageModal = true
    }
  }
}
</script>
<style lang="scss" scoped>
.platform-base-role-transfer {
  .content {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
}
</style>
