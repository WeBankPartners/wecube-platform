<template>
  <BaseDrawer
    :title="form.id ? $t('pi_edit_custom') : $t('pi_add_custom')"
    :visible.sync="visible"
    :realWidth="1100"
    :scrollable="true"
    :maskClosable="false"
  >
    <div slot-scope="{maxHeight}" slot="content" style="display: flex" class="platform-customer-content">
      <!--客户列表-->
      <BaseHeaderTitle
        :title="$t('pi_custom_list')"
        :fontSize="15"
        :showExpand="false"
        :style="{
          maxHeight: maxHeight + 'px',
          width: '250px',
          borderRight: '1px solid #e8eaec',
          marginRight: '20px',
          position: 'relative'
        }"
        class="left-area"
      >
        <Button type="success" size="small" class="custom-create" @click="handleCreate">{{
          $t('pi_add_custom')
        }}</Button>
        <div v-if="customerList.length > 0" class="custom-list-wrap" :style="{maxHeight: maxHeight + 'px'}">
          <div
            v-for="i in customerList"
            :key="i.id"
            class="custom-list"
            :style="{background: selected === i.id ? '#c6eafe' : '#fff'}"
            @click="handleEdit(i)"
          >
            <div class="tag">{{ i.name }}</div>
            <Button type="error" size="small" icon="md-trash" @click="handleDelete($event, i)"></Button>
          </div>
        </div>
        <div v-else class="no-data">{{ $t('noData') }}</div>
      </BaseHeaderTitle>
      <!--客户信息-->
      <BaseHeaderTitle
        :title="$t('pi_custom_info')"
        :fontSize="15"
        :showExpand="false"
        style="width: calc(100% - 280px); position: relative"
        class="right-area"
      >
        <Button type="primary" size="small" class="init-custom" @click="handleInitCustomInfo">{{
          $t('pi_custom_default_config')
        }}</Button>
        <Form ref="form" :label-width="100" :model="form">
          <!--创建人-->
          <FormItem :label="$t('createdBy')">
            <Input :value="form.createdUser" disabled />
          </FormItem>
          <!--目标客户-->
          <FormItem
            :label="$t('pi_target_custom')"
            prop="name"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <Input v-model.trim="form.name" :maxlength="100" />
          </FormItem>
          <!--nexus地址-->
          <FormItem
            :label="$t('pi_nexus_address')"
            prop="nexusAddr"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <Input v-model.trim="form.nexusAddr" :maxlength="200" />
          </FormItem>
          <!--nexus账号-->
          <FormItem
            :label="$t('pi_nexus_account')"
            prop="nexusAccount"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <Input v-model.trim="form.nexusAccount" :maxlength="50" />
          </FormItem>
          <!--nexus密码-->
          <FormItem
            :label="$t('pi_nexus_password')"
            prop="nexusPwd"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <input type="text" style="display: none" />
            <input type="password" autocomplete="new-password" style="display: none" />
            <Input v-model.trim="form.nexusPwd" type="password" autocomplete="off" password :maxlength="20" />
          </FormItem>
          <FormItem
            label="nexusrepo"
            prop="nexusRepo"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <Input v-model.trim="form.nexusRepo" :maxlength="50" />
          </FormItem>
          <FormItem
            :label="$t('execute_workflow_ids')"
            prop="execWorkflowIds"
            :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur, change'}"
          >
            <Input v-model.trim="form.execWorkflowIds" />
          </FormItem>
        </Form>
      </BaseHeaderTitle>
    </div>
    <div slot="footer">
      <Button @click="handleCancel">{{ $t('cancel') }}</Button>
      <Button type="primary" style="margin-left: 10px" @click="handleSave">{{ $t('save') }}</Button>
    </div>
  </BaseDrawer>
</template>
<script>
import {
  getCustomerList, addCustomer, deleteCustomer, getNexusConfig
} from '@/api/server'
import { debounce } from 'lodash'
export default {
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: {
        createdUser: localStorage.getItem('username'),
        name: '',
        nexusAddr: '',
        nexusAccount: '',
        nexusPwd: '',
        nexusRepo: '',
        execWorkflowIds: ''
      },
      customerList: [],
      selected: ''
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  mounted() {
    this.getCustomerList()
  },
  methods: {
    async handleInitCustomInfo() {
      const { status, data } = await getNexusConfig()
      if (status === 'OK') {
        const {
          nexusUser, nexusPwd, nexusUrl, nexusRepo
        } = data || {}
        this.form.nexusAddr = nexusUrl
        this.form.nexusAccount = nexusUser
        this.form.nexusPwd = nexusPwd
        this.form.nexusRepo = nexusRepo
      }
    },
    // 获取目标客户列表
    async getCustomerList() {
      const { status, data } = await getCustomerList()
      if (status === 'OK') {
        this.customerList = data || []
      }
    },
    handleCreate() {
      this.selected = ''
      this.form = {
        createdUser: localStorage.getItem('username'),
        name: '',
        nexusAddr: '',
        nexusAccount: '',
        nexusPwd: '',
        nexusRepo: '',
        execWorkflowIds: ''
      }
    },
    handleEdit(row) {
      this.selected = row.id
      this.form = Object.assign({}, this.form, {
        id: row.id,
        createdUser: row.createdUser,
        name: row.name,
        nexusAddr: row.nexusAddr,
        nexusAccount: row.nexusAccount,
        nexusPwd: row.nexusPwd,
        nexusRepo: row.nexusRepo,
        execWorkflowIds: row.execWorkflowIds
      })
    },
    handleDelete(e, row) {
      e.stopPropagation()
      this.$Modal.confirm({
        title: this.$t('pi_tips'),
        content: this.$t('confirm_to_delete'),
        onOk: async () => {
          const { status } = await deleteCustomer(row.id)
          if (status === 'OK') {
            this.$Message.success(this.$t('be_success'))
            if (this.selected === row.id) {
              this.$refs.form.resetFields()
              this.selected = ''
              this.form = {
                createdUser: localStorage.getItem('username'),
                name: '',
                nexusAddr: '',
                nexusAccount: '',
                nexusPwd: '',
                nexusRepo: '',
                execWorkflowIds: ''
              }
            }
            this.getCustomerList()
          }
        },
        onCancel: () => {}
      })
    },
    handleSave: debounce(function () {
      this.$refs.form.validate(async valid => {
        if (valid) {
          const { status } = await addCustomer(this.form)
          if (status === 'OK') {
            if (!this.form.id) {
              this.$refs.form.resetFields()
            }
            this.$Message.success(this.$t('save_successfully'))
            this.getCustomerList()
          }
        }
      })
    }, 300),
    handleCancel() {
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.left-area {
  .custom-create {
    position: absolute;
    right: 20px;
    top: 0;
  }
  .custom-list-wrap {
    overflow-y: auto;
    .custom-list {
      width: 220px;
      padding: 5px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      cursor: pointer;
      .tag {
        font-size: 14px;
        max-width: 170px;
        text-overflow: ellipsis;
        white-space: nowrap;
        background: #f7f7f7;
        overflow: hidden;
        border-radius: 4px;
        padding: 2px 8px;
      }
    }
  }
  ::-webkit-scrollbar {
    width: 8px;
    height: 10px;
  }
  ::-webkit-scrollbar-track {
    background: transparent;
  }
  ::-webkit-scrollbar-thumb {
    background: #d4d4d4;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: #d4d4d4;
  }
}
.right-area {
  .init-custom {
    position: absolute;
    right: 0px;
    top: 0px;
  }
}
</style>
<style lang="scss">
.platform-customer-content {
  overflow-y: hidden;
  .common-ui-header-title .w-content,
  .content {
    padding: 20px 0px !important;
  }
}
</style>
