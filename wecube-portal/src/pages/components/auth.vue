<template>
  <div>
    <Modal v-model="flowRoleManageModal" width="700" :title="$t('edit_role')" :mask-closable="false">
      <div>
        <div class="role-transfer-title">{{ $t('mgmt_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRoles"
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
          :data="allRolesBackUp"
          :target-keys="useRolesKeyToFlow"
          :render-format="renderRoleNameForTransfer"
          @on-change="handleUseRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div slot="footer">
        <Button type="primary" :disabled="disabled" @click="confirmRole">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import { getRoleList } from '@/api/server.js'
export default {
  props: {
    useRolesRequired: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      isAdd: false, // 标记编排状态
      flowRoleManageModal: false, // 权限弹窗控制
      transferTitles: [this.$t('unselected_role'), this.$t('selected_role')],
      transferStyle: { width: '300px' },
      allRolesBackUp: [],
      currentUserRoles: [],
      mgmtRolesKeyToFlow: [], // 管理角色
      useRolesKeyToFlow: [] // 使用角色
    }
  },
  computed: {
    allRoles () {
      return this.isAdd ? this.currentUserRoles : this.allRolesBackUp
    },
    disabled () {
      if (this.useRolesRequired) {
        return this.mgmtRolesKeyToFlow.length === 0 || this.useRolesKeyToFlow.length === 0
      } else {
        return this.mgmtRolesKeyToFlow.length === 0
      }
    }
  },
  methods: {
    renderRoleNameForTransfer (item) {
      return item.label
    },
    handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      if (newTargetKeys.length > 1) {
        this.$Message.warning(this.$t('chooseOne'))
      } else {
        this.mgmtRolesKeyToFlow = newTargetKeys
      }
    },
    handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.useRolesKeyToFlow = newTargetKeys
    },
    async confirmRole () {
      this.$emit('sendAuth', this.mgmtRolesKeyToFlow, this.useRolesKeyToFlow)
      this.flowRoleManageModal = false
    },
    async getRoleList () {
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.allRolesBackUp = data.map(_ => {
          return {
            ..._,
            key: _.name,
            label: _.displayName
          }
        })
      }
    },
    // 启动入口
    async startAuth (mgmtRolesKeyToFlow, useRolesKeyToFlow) {
      this.mgmtRolesKeyToFlow = mgmtRolesKeyToFlow
      this.useRolesKeyToFlow = useRolesKeyToFlow
      await this.getRoleList()
      this.flowRoleManageModal = true
    }
  }
}
</script>
