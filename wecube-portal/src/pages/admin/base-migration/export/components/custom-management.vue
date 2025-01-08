<!--
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-01-07 20:01:01
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-01-08 16:24:58
-->
<template>
  <BaseDrawer
    :title="$t('pi_custom_title')"
    :visible.sync="visible"
    :realWidth="1100"
    :scrollable="true"
    :maskClosable="false"
    class="platform-customer-list-drawer"
  >
    <div slot-scope="{maxHeight}" slot="content" style="display:flex;">
      <!--客户列表-->
      <BaseHeaderTitle
        :title="$t('pi_custom_list')"
        :fontSize="15"
        :showExpand="false"
        :style="{maxHeight: maxHeight + 'px', width: '250px', borderRight: '1px solid #e8eaec', marginRight: '20px', position: 'relative'}"
        class="left-area"
      >
        <Button type="success" size="small" class="custom-create" @click="handleCreate">{{ $t('pi_add_custom') }}</Button>
        <div v-if="customerList.length > 0" class="custom-list-wrap" :style="{maxHeight: maxHeight + 'px'}">
          <div
            v-for="i in customerList"
            :key="i.id"
            class="custom-list"
            :style="{background: selected === i.id ? '#c6eafe' : '#fff'}"
            @click="handleEdit(i)"
          >
            <span>{{ i.name }}</span>
            <Button type="error" size="small" icon="md-trash" @click="handleDelete($event, i)"></Button>
          </div>
        </div>
        <div v-else class="no-data">{{ $t('noData') }}</div>
      </BaseHeaderTitle>
      <!--客户信息-->
      <BaseHeaderTitle :title="$t('pi_custom_info')" :fontSize="15" :showExpand="false" style="width:calc(100% - 300px);">
        <Form ref="form" :label-width="100" :model="form">
          <!--创建人-->
          <FormItem :label="$t('createdBy')">
            <Input :value="username" disabled />
          </FormItem>
          <!--目标客户-->
          <FormItem :label="$t('pi_target_custom')" prop="name" :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur'}">
            <Input v-model.trim="form.name" :maxlength="200" />
          </FormItem>
          <!--nexus地址-->
          <FormItem :label="$t('pi_nexus_address')" prop="nexusAddr" :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur'}">
            <Input v-model.trim="form.nexusAddr" :maxlength="100" />
          </FormItem>
          <!--nexus账号-->
          <FormItem :label="$t('pi_nexus_account')" prop="nexusAccount" :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur'}">
            <Input v-model.trim="form.nexusAccount" :maxlength="50" />
          </FormItem>
          <!--nexus密码-->
          <FormItem :label="$t('pi_nexus_password')" prop="nexusPwd" :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur'}">
            <Input v-model.trim="form.nexusPwd" :maxlength="20" />
          </FormItem>
          <FormItem label="nexusrepo" prop="nexusRepo" :rules="{required: true, message: $t('fe_can_not_be_empty'), trigger: 'blur'}">
            <Input v-model.trim="form.nexusRepo" :maxlength="50" />
          </FormItem>
        </Form>
      </BaseHeaderTitle>
    </div>
    <div slot="footer">
      <Button @click="handleCancel">{{ $t('cancel') }}</Button>
      <Button type="primary" style="margin-left:10px;" @click="handleSave">{{ $t('save') }}</Button>
    </div>
  </BaseDrawer>
</template>
<script>
import { getCustomerList, addCustomer, deleteCustomer } from '@/api/server'
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
        name: '',
        nexusAddr: '',
        nexusAccount: '',
        nexusPwd: '',
        nexusRepo: ''
      },
      username: localStorage.getItem('username'),
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
        name: '',
        nexusAddr: '',
        nexusAccount: '',
        nexusPwd: '',
        nexusRepo: ''
      }
    },
    handleEdit(row) {
      this.selected = row.id
      this.form = Object.assign({}, this.form, {
        id: row.id,
        name: row.name,
        nexusAddr: row.nexusAddr,
        nexusAccount: row.nexusAccount,
        nexusPwd: row.nexusPwd,
        nexusRepo: row.nexusRepo
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
              this.form = {
                name: '',
                nexusAddr: '',
                nexusAccount: '',
                nexusPwd: '',
                nexusRepo: ''
              }
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
      padding: 5px 10px 5px 0px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      cursor: pointer;
      span {
        font-size: 15px;
        max-width: 160px;
      }
    }
  }
}
</style>
