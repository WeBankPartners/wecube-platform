<template>
  <div>
    <Modal
      v-model="showModal"
      :mask-closable="false"
      :fullscreen="isfullscreen"
      :footer-hide="true"
      :width="800"
      :title="$t('be_user_mgmt')"
    >
      <div slot="header" class="custom-modal-header">
        <span>
          {{ $t('be_user_mgmt') }}
        </span>
        <Icon v-if="isfullscreen" @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-contract" />
        <Icon v-else @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-expand" />
      </div>
      <div>
        <div class="title" style="margin-top: 0px">
          <div class="title-text">
            {{ $t('be_handle_application') }}
            <span class="underline"></span>
          </div>
        </div>
        <Tabs type="card" :value="activeTab" @on-click="tabChange">
          <TabPane :label="$t('be_pending')" name="pending"></TabPane>
          <TabPane :label="$t('be_hasProcessed')" name="processed"></TabPane>
        </Tabs>
        <div>
          <Table
            height="200"
            size="small"
            :columns="this.activeTab === 'pending' ? pendingColumns : processedColumns"
            :data="tableData"
          ></Table>
        </div>
      </div>
      <div class="title" style="margin-top: 20px">
        <div class="title-text">
          {{ $t('be_role_list') }}
          <span class="underline"></span>
        </div>
      </div>
      <Row>
        <Col span="12">
          <Card>
            <p slot="title" style="height: 24px">{{ $t('role') }}</p>
            <div class="tagContainers" :style="{ height: tableHeight + 'px' }">
              <div class="role-item" v-for="item in roleList" :key="item.id">
                <div
                  class="item-style"
                  @click="handleRoleClick(item)"
                  :class="activeRole === item.id ? 'active-item' : ''"
                >
                  {{ item.displayName }}
                </div>
              </div>
            </div>
          </Card>
        </Col>
        <Col span="12">
          <Card>
            <p slot="title" style="height: 24px">
              {{ $t('user') }}
              <Button
                type="success"
                :disabled="!activeRole"
                @click="startAddUser"
                size="small"
                ghost
                style="margin-left: 20px"
                >{{ $t('add_user') }}</Button
              >
            </p>
            <div class="tagContainers" :style="{ height: tableHeight + 'px' }">
              <div class="role-item" v-for="item in userList" :key="item.id">
                <div class="item-style" style="width: 80%; display: inline-block">
                  {{ item.username }}
                </div>
                <Button @click="removeUser(item)" size="small" icon="md-trash" ghost type="error"></Button>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </Modal>
    <Modal v-model="showSelectModel" :title="$t('add_user')" :mask-closable="false">
      <Form :label-width="120">
        <FormItem :label="$t('user')">
          <Select style="width: 80%" v-model="pendingUser" multiple filterable @on-open-change="getPendingUserOptions">
            <Option v-for="item in pendingUserOptions" :value="item.id" :key="item.id">{{ item.username }}</Option>
          </Select>
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="showSelectModel = false">{{ $t('cancel') }}</Button>
        <Button @click="okSelect" :disabled="pendingUser.length === 0" type="primary">{{ $t('confirm') }}</Button>
      </template>
    </Modal>
  </div>
</template>
<script>
import {
  getProcessableList,
  getApplyRoles,
  getUserByRole,
  removeUserFromRole,
  addUserForRole,
  getAllUser,
  handleApplication
} from '@/api/server.js'

export default {
  data () {
    return {
      showModal: false,
      isfullscreen: false,
      activeTab: 'pending',
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
          title: this.$t('actions'),
          key: 'address',
          render: (h, params) => {
            return (
              <div style="text-align: left; cursor: pointer;display: inline-flex;">
                <Button
                  size="small"
                  type="primary"
                  onClick={() => this.handle(params.row, 'approve')}
                  style="margin-right:5px;"
                >
                  {this.$t('be_approve')}
                </Button>
                <Button
                  size="small"
                  type="error"
                  onClick={() => this.handle(params.row, 'deny')}
                  style="margin-right:5px;"
                >
                  {this.$t('be_reject')}
                </Button>
              </div>
            )
          }
        }
      ],
      processedColumns: [
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
          title: this.$t('be_processing_time'),
          key: 'updatedTime'
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
      ],
      tableData: [],
      roleList: [],
      activeRole: '',
      userList: [],
      showSelectModel: false,
      pendingUser: [], // 待添加用户
      pendingUserOptions: [] // 待添加用户列表
    }
  },
  computed: {
    tableHeight () {
      const innerHeight = window.innerHeight
      return this.isfullscreen ? innerHeight - 540 : innerHeight - 700
    }
  },
  methods: {
    openModal () {
      this.isfullscreen = false
      this.showModal = true
      this.getTableData()
      this.getRoles()
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
      const { status, data } = await getProcessableList(params)
      if (status === 'OK') {
        this.tableData = data.contents || []
      }
    },
    async getRoles () {
      const params = {
        all: 'N', // Y:所有(包括未激活和已删除的) N:激活的
        roleAdmin: true
      }
      const { status, data } = await getApplyRoles(params)
      if (status === 'OK') {
        this.roleList = data || []
        if (this.roleList.length > 0) {
          this.activeRole = this.roleList[0].id
          this.getUserByRole(this.roleList[0].id)
        }
      }
    },
    async getUserByRole (roleId) {
      const { status, data } = await getUserByRole(roleId)
      if (status === 'OK') {
        this.userList = data || []
      }
    },
    async handleRoleClick (item) {
      this.activeRole = item.id
      this.getUserByRole(this.activeRole)
    },
    async removeUser (item) {
      this.$Modal.confirm({
        title: this.$t('confirm_delete'),
        content: `${this.$t('confirm_delete_content')}${item.username}`,
        'z-index': 1000000,
        loading: true,
        onOk: async () => {
          this.$Modal.remove()
          let data = [
            {
              id: item.id
            }
          ]
          let res = await removeUserFromRole(this.activeRole, data)
          if (res.status === 'OK') {
            this.$Notice.success({
              title: this.$t('successful'),
              desc: this.$t('successful')
            })
            this.getUserByRole(this.activeRole)
          }
        },
        onCancel: () => {}
      })
    },
    startAddUser () {
      this.showSelectModel = true
      this.pendingUser = []
    },
    // 获取待添加用户列表
    async getPendingUserOptions () {
      const { status, data } = await getAllUser()
      if (status === 'OK') {
        this.pendingUserOptions = (data || []).filter(user => {
          const findIndex = this.userList.findIndex(u => u.id === user.id)
          if (findIndex === -1) {
            return user
          }
        })
      }
    },
    async okSelect () {
      let data = this.pendingUser.map(userId => {
        return {
          id: userId
        }
      })
      const { status } = await addUserForRole(this.activeRole, data)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.showSelectModel = false
        this.getUserByRole(this.activeRole)
      }
    },
    // 处理申请
    async handle (item, statusCode) {
      let data = [
        {
          id: item.id,
          status: statusCode
        }
      ]
      const { status } = await handleApplication(data)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.getTableData()
      }
    },
    tabChange (val) {
      this.activeTab = val
      this.getTableData()
    }
  }
}
</script>
<style lang="scss" scoped>
.tagContainers {
  overflow: auto;
  // height: calc(100vh - 650px);
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
