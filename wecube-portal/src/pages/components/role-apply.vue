<template>
  <div>
    <Modal
      v-model="showModal"
      :mask-closable="false"
      :fullscreen="isfullscreen"
      :footer-hide="true"
      :width="800"
      :title="$t('be_apply_roles')"
    >
      <div slot="header" class="custom-modal-header">
        <span>
          {{ $t('be_apply_roles') }}
        </span>
        <Icon v-if="isfullscreen" @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-contract" />
        <Icon v-else @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-expand" />
      </div>
      <div>
        <div class="title" style="margin-top: 0px">
          <div class="title-text">
            {{ $t('be_apply') }}
            <span class="underline"></span>
          </div>
        </div>
        <div>
          <Form :label-width="80">
            <FormItem :label="$t('role')">
              <Select
                v-model="selectedRole"
                @on-open-change="getApplyRoles"
                multiple
                filterable
                :max-tag-count="3"
                style="width: 60%; margin-right: 24px"
                :placeholder="$t('be_apply_roles')"
              >
                <Option v-for="role in roleList" :value="role.id" :key="role.id">{{ role.displayName }}</Option>
              </Select>
              <Button type="primary" :disabled="selectedRole.length === 0" @click="apply">{{ $t('be_apply') }}</Button>
            </FormItem>
          </Form>
        </div>
        <div class="title" style="margin-top: 0px">
          <div class="title-text">
            {{ $t('be_application_record') }}
            <span class="underline"></span>
          </div>
        </div>
        <Tabs type="card" :value="activeTab" @on-click="tabChange">
          <TabPane :label="$t('be_pending')" name="pending"></TabPane>
          <TabPane :label="$t('be_hasProcessed')" name="processed"></TabPane>
        </Tabs>
        <div>
          <Table
            :height="tableHeight"
            size="small"
            :columns="this.activeTab === 'pending' ? pendingColumns : processedColumns"
            :data="tableData"
          ></Table>
        </div>
      </div>
      <div slot="footer">
        <Button @click="showModal = false">{{ $t('cancel') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import { getApplyRoles, startApply, getApplyList } from '@/api/server.js'

export default {
  data () {
    return {
      showModal: false,
      isfullscreen: false,
      selectedRole: [],
      roleList: [],
      activeTab: 'pending',
      tableData: [],
      pendingColumns: [
        {
          title: this.$t('be_account'),
          key: 'createdBy'
        },
        {
          title: this.$t('be_apply_roles'),
          key: 'roleId',
          render: (h, params) => {
            return <div>{params.row.role.displayName}</div>
          }
        },
        {
          title: this.$t('be_application_time'),
          key: 'createdTime'
        }
      ],
      processedColumns: [
        {
          title: this.$t('be_apply_roles'),
          key: 'roleId',
          render: (h, params) => {
            return <div>{params.row.role.displayName}</div>
          }
        },
        {
          title: this.$t('be_application_time'),
          key: 'createdTime'
        },
        {
          title: `${this.$t('be_approver')}(${this.$t('be_role_administrator')})`,
          key: 'updatedBy',
          width: 210
        },
        {
          title: this.$t('be_processing_status'),
          key: 'status',
          render: (h, params) => {
            const status = params.row.status
            const statusTitle = status === 'approve' ? this.$t('be_approve') : this.$t('be_reject')
            return <div style={status === 'approve' ? 'color:#b8f27c' : 'color:red'}>{statusTitle}</div>
          }
        }
      ]
    }
  },
  computed: {
    tableHeight () {
      const innerHeight = window.innerHeight
      return this.isfullscreen ? innerHeight - 300 : 400
    }
  },
  methods: {
    openModal () {
      this.isfullscreen = false
      this.showModal = true
      this.selectedRole = []
      this.getTableData()
    },
    tabChange (val) {
      this.activeTab = val
      this.getTableData()
    },
    async getTableData () {
      let statusArr = this.activeTab === 'pending' ? ['init'] : ['approve', 'deny']
      const params = {
        filters: [
          {
            name: 'status',
            operator: 'in',
            value: statusArr
          }
        ],
        paging: true,
        pageable: {
          startIndex: 0,
          pageSize: 10000
        },
        sorting: [
          {
            asc: false,
            field: 'createdTime'
          }
        ]
      }
      const { status, data } = await getApplyList(params)
      if (status === 'OK') {
        this.tableData = data.contents || []
      }
    },
    async apply () {
      let data = {
        userName: localStorage.getItem('username'),
        roleIds: this.selectedRole
      }
      const { status } = await startApply(data)
      if (status === 'OK') {
        this.selectedRole = []
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('be_apply_success')
        })
        this.getTableData()
      }
    },
    async getApplyRoles () {
      const params = {
        all: 'N', // Y:所有(包括未激活和已删除的) N:激活的
        roleAdmin: false
      }
      const { status, data } = await getApplyRoles(params)
      if (status === 'OK') {
        this.roleList = data || []
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.tagContainers {
  overflow: auto;
  height: calc(100vh - 650px);
}
.item-style {
  padding: 2px 4px;
  border: 1px dashed #e8eaec;
  margin: 6px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  width: 80%;
  display: inline-block;
}
.active-item {
  background-color: #2db7f5;
}

.title {
  font-size: 14px;
  font-weight: bold;
  margin: 12px 0;
  display: inline-block;
  .title-text {
    display: inline-block;
    margin-left: 6px;
  }
  .underline {
    display: block;
    margin-top: -10px;
    margin-left: -6px;
    width: 100%;
    padding: 0 6px;
    height: 12px;
    border-radius: 12px;
    background-color: #c6eafe;
    box-sizing: content-box;
  }
}

.custom-modal-header {
  line-height: 20px;
  font-size: 16px;
  color: #17233d;
  font-weight: 500;
  .fullscreen-icon {
    float: right;
    margin-right: 28px;
    font-size: 18px;
    cursor: pointer;
  }
}
</style>
