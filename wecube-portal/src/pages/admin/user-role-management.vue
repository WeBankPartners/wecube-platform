<template>
  <div>
    <Row>
      <Col span="7">
        <Card>
          <p slot="title">
            {{ $t('user') }}
            <Button icon="ios-add" type="success" ghost size="small" @click="openAddUserModal">{{
              $t('add_user')
            }}</Button>
          </p>
          <div class="tagContainers">
            <div class="role-item" v-for="item in users" :key="item.id">
              <Tag
                :name="item.username"
                :color="item.color"
                :checked="item.checked"
                checkable
                :fade="false"
                @on-change="handleUserClick"
              >
                <span :title="` ${item.username} `">
                  {{ ` ${item.username} ` }}
                </span>
              </Tag>
              <Tooltip :content="$t('delete')">
                <Button icon="md-trash" type="error" ghost size="small" @click="removeRole(item)"></Button>
              </Tooltip>
              <Tooltip :content="$t('reset_password')">
                <Button
                  icon="ios-unlock-outline"
                  type="primary"
                  ghost
                  size="small"
                  @click="resetRolePassword(item)"
                ></Button>
              </Tooltip>
            </div>
          </div>
        </Card>
      </Col>
      <Col span="7" offset="1">
        <Card>
          <p slot="title">
            {{ $t('role') }}
            <Button icon="ios-add" type="success" ghost size="small" @click="openAddRoleModal">{{
              $t('add_role')
            }}</Button>
          </p>
          <div class="tagContainers">
            <div class="role-item" v-for="item in roles" :key="item.id">
              <Tag
                :name="item.id"
                :color="item.color"
                :checked="item.checked"
                checkable
                :fade="false"
                @on-change="handleRoleClick"
              >
                <span :title="item.displayName">{{ item.name + '(' + item.displayName + ')' }}</span>
              </Tag>
              <Button icon="ios-person" type="primary" ghost size="small" @click="openUserManageModal(item.id)">{{
                $t('user')
              }}</Button>
            </div>
          </div>
        </Card>
      </Col>
      <Col span="7" offset="1">
        <Card>
          <p slot="title">{{ $t('menus') }}</p>
          <div class="tagContainers">
            <Spin size="large" fix v-if="menuTreeLoading">
              <Icon type="ios-loading" size="24" class="spin-icon-load"></Icon>
              <div>{{ $t('loading') }}</div>
            </Spin>
            <Tree :data="menus" show-checkbox @on-check-change="handleMenuTreeCheck"></Tree>
          </div>
        </Card>
      </Col>
    </Row>
    <Modal v-model="addUserModalVisible" :title="$t('add_user')" @on-ok="addUser" @on-cancel="cancel">
      <Form class="validation-form" ref="addedUserForm" :model="addedUser" label-position="left" :label-width="100">
        <FormItem :label="$t('username')" prop="username">
          <Input v-model="addedUser.username" />
        </FormItem>
        <FormItem :label="$t('auth_type')" prop="authtype">
          <RadioGroup v-model="addedUser.authType">
            <Radio label="LOCAL"></Radio>
            <Radio label="UM"></Radio>
          </RadioGroup>
        </FormItem>
        <FormItem v-if="addedUser.authType === 'LOCAL'" :label="$t('password')" prop="password">
          <Input type="password" v-model="addedUser.password" />
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="addRoleModalVisible" :title="$t('add_role')" @on-ok="addRole" @on-cancel="cancel">
      <Form class="validation-form" :model="addedRole" label-position="left" :label-width="100">
        <FormItem :label="$t('role')">
          <Input v-model="addedRole.name" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem :label="$t('display_name')">
          <Input v-model="addedRole.displayName" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem :label="$t('email')">
          <Input v-model="addedRole.email" :placeholder="$t('please_input')" />
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="userManageModal" width="700" :title="$t('edit_user')">
      <Transfer
        :titles="transferTitles"
        :list-style="transferStyle"
        :data="allUsersForTransfer"
        :target-keys="usersKeyBySelectedRole"
        :render-format="renderUserNameForTransfer"
        @on-change="handleUserTransferChange"
        filterable
      ></Transfer>
      <div slot="footer">
        <Button type="primary" @click="confirmUser">{{ $t('close') }}</Button>
      </div>
    </Modal>
    <Modal v-model="showNewPassword" :title="$t('new_password')">
      <Form class="validation-form" label-position="left" :label-width="100">
        <FormItem :label="$t('new_password')">
          <Input v-model="newPassword" :placeholder="$t('please_input')" style="width:300px" />
          <Icon @click="copyPassword" class="icon-copy" type="md-copy" />
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import {
  userCreate,
  removeUser,
  getUserList,
  roleCreate,
  getRoleList,
  getRolesByUserName,
  getUsersByRoleId,
  grantRolesForUser,
  revokeRolesForUser,
  getAllMenusList,
  getMenusByUserName,
  getMenusByRoleId,
  updateRoleToMenusByRoleId,
  resetPassword
} from '@/api/server'
import { MENUS } from '@/const/menus.js'

export default {
  data () {
    return {
      showNewPassword: false,
      newPassword: '',
      currentRoleId: 0,
      users: [],
      roles: [],
      addedUser: {
        authType: 'LOCAL'
      },
      addedRole: {},
      addedRoleValue: '',
      transferTitles: [this.$t('unselected_user'), this.$t('selected_user')],
      transferStyle: { width: '300px' },
      usersKeyBySelectedRole: [],
      allUsersForTransfer: [],
      addUserModalVisible: false,
      addRoleModalVisible: false,
      userManageModal: false,
      originMenus: [],
      menus: [],
      menuTreeLoading: false
    }
  },
  methods: {
    removeRole (item) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          let { status } = await removeUser(item.id)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: ''
            })
            this.getAllUsers()
            this.getAllRoles()
            this.getAllMenus()
          }
        },
        onCancel: () => {}
      })
    },
    async resetRolePassword (item) {
      this.$Modal.confirm({
        title: this.$t('reset_password'),
        'z-index': 1000000,
        onOk: async () => {
          const params = {
            username: item.username
          }
          let { status, data } = await resetPassword(params)
          if (status === 'OK') {
            this.newPassword = data
            this.showNewPassword = true
          }
        },
        onCancel: () => {}
      })
    },
    copyPassword (password) {
      let inputElement = document.createElement('input')
      inputElement.value = this.newPassword
      document.body.appendChild(inputElement)
      inputElement.select()
      document.execCommand('Copy')
      this.$Notice.success({
        title: 'Success',
        desc: this.$t('copy_success')
      })
      inputElement.remove()
      this.showNewPassword = false
    },
    async handleMenuTreeCheck (allChecked, currentChecked) {
      this.menuTreeLoading = true
      const menuCodes = allChecked.filter(i => i.category).map(_ => _.code)
      const { status, message } = await updateRoleToMenusByRoleId(this.currentRoleId, menuCodes)
      await this.handleRoleClick(true, this.currentRoleId)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
      }
      this.menuTreeLoading = false
    },
    async getAllMenus () {
      const { status, data } = await getAllMenusList()
      if (status === 'OK') {
        this.originMenus = data
      }
      this.menus = this.menusResponseHandeler(this.originMenus)
    },
    menusPermissionSelected (allMenus, menusPermissions = [], disabled) {
      allMenus.forEach(_ => {
        _.children.forEach(m => {
          const subMenu = menusPermissions.find(n => m.code === n.code)
          m.checked = !!subMenu
          m.disabled = disabled
        })
        _.indeterminate = false
        _.checked = false
        _.disabled = disabled
      })
    },
    menusResponseHandeler (data, disabled = true) {
      let menus = []
      data.forEach(_ => {
        if (!_.category) {
          let menuObj = MENUS.find(m => m.code === _.code)
          menus.push({
            ..._,
            title: _.source === 'SYSTEM' ? (this.$lang === 'zh-CN' ? menuObj.cnName : menuObj.enName) : _.displayName,
            id: _.id,
            expand: true,
            checked: false,
            children: [],
            disabled
          })
        }
      })
      data.forEach(_ => {
        if (_.category) {
          let menuObj = MENUS.find(m => m.code === _.code)
          menus.forEach(h => {
            if (_.category === h.id) {
              h.children.push({
                ..._,
                title:
                  _.source === 'SYSTEM'
                    ? this.$lang === 'zh-CN'
                      ? menuObj.cnName
                      : menuObj.enName
                    : this.$lang === 'zh-CN'
                      ? _.localDisplayName
                      : _.displayName,
                id: _.id,
                expand: true,
                checked: false,
                disabled
              })
            }
          })
        }
      })
      return menus
    },
    renderUserNameForTransfer (item) {
      return item.label
    },
    async getAllUsers () {
      let { status, data } = await getUserList()
      if (status === 'OK') {
        this.users = data.map(_ => {
          return {
            ..._,
            checked: false,
            color: '#5cadff'
          }
        })
      }
    },
    async getAllRoles () {
      let { status, data } = await getRoleList()
      if (status === 'OK') {
        this.roles = data.map(_ => {
          return {
            ..._,
            checked: false,
            color: 'success'
          }
        })
      }
    },
    async handleUserClick (checked, name) {
      this.currentRoleId = 0
      this.users.forEach(_ => {
        _.checked = false
        if (name === _.username) {
          _.checked = checked
        }
      })
      if (checked) {
        let permissions = await getMenusByUserName(name)
        if (permissions.status === 'OK') {
          const userMenus = [].concat(...permissions.data.map(_ => _.menuList))
          this.menusPermissionSelected(this.menus, userMenus, true)
        }
        let { status, data } = await getRolesByUserName(name)
        if (status === 'OK') {
          this.roles.forEach(_ => {
            _.checked = false
            const found = data.find(item => item.id === _.id)
            if (found) {
              _.checked = checked
            }
          })
        }
      } else {
        this.roles.forEach(_ => {
          _.checked = false
        })
        this.menusPermissionSelected(this.menus, [], true)
      }
    },
    async handleRoleClick (checked, id) {
      this.currentRoleId = id
      this.roles.forEach(_ => {
        _.checked = false
        if (id === _.id) {
          _.checked = checked
        }
      })
      this.menus = this.menusResponseHandeler(this.originMenus, !checked)
      if (checked) {
        let permissions = await getMenusByRoleId(id)
        if (permissions.status === 'OK') {
          this.menusPermissionSelected(this.menus, permissions.data.menuList, false)
        }
        let { status, data } = await getUsersByRoleId(id)
        if (status === 'OK') {
          this.users.forEach(_ => {
            _.checked = false
            const found = data.find(item => item.username === _.username)
            if (found) {
              _.checked = checked
            }
          })
        }
      } else {
        this.users.forEach(_ => {
          _.checked = false
        })
      }
    },
    async handleUserTransferChange (newTargetKeys, direction, moveKeys) {
      if (direction === 'right') {
        let { status, message } = await grantRolesForUser(moveKeys, this.selectedRole)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'success',
            desc: message
          })
          this.usersKeyBySelectedRole = newTargetKeys
        }
      } else {
        let { status, message } = await revokeRolesForUser(moveKeys, this.selectedRole)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'success',
            desc: message
          })
          this.usersKeyBySelectedRole = newTargetKeys
        }
      }
    },
    confirmUser () {
      if (this.currentRoleId !== 0) {
        this.handleRoleClick(true, this.currentRoleId)
      }
      this.userManageModal = false
    },
    async openUserManageModal (id) {
      this.usersKeyBySelectedRole = []
      this.allUsersForTransfer = []
      this.selectedRole = id
      let { status, data } = await getUsersByRoleId(id)
      if (status === 'OK') {
        this.usersKeyBySelectedRole = data.map(_ => _.id)
      }
      this.allUsersForTransfer = this.users.map(_ => {
        return {
          key: _.id,
          username: _.username,
          label: _.username || ''
        }
      })
      this.userManageModal = true
    },
    async addUser () {
      let { status, message } = await userCreate(this.addedUser)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.addedUser = {
          authType: 'LOCAL'
        }
        this.getAllUsers()
      }
    },
    async addRole () {
      if (!this.addedRole.name) {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('role_cannot_empty')
        })
        return
      }
      let { status, message } = await roleCreate(this.addedRole)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.getAllRoles()
      }
    },
    openAddRoleModal () {
      this.addRoleModalVisible = true
    },
    openAddUserModal () {
      this.addUserModalVisible = true
    },
    cancel () {}
  },
  created () {
    this.getAllUsers()
    this.getAllRoles()
    this.getAllMenus()
  }
}
</script>
<style lang="scss" scoped>
.ivu-card-head-inner,
.ivu-card-head p {
  height: 30px;

  button {
    margin-left: 10px;
  }
}
.tagContainers {
  overflow: auto;
  height: calc(100vh - 210px);
}
.ivu-tag {
  display: block;
  border: #515a61 1px dashed !important;
  .ivu-tag-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    display: block;
  }
}

.role-item {
  .ivu-tag {
    display: inline-block;
    width: 65%;
  }
}
.data-permissions {
  width: 100%;
  height: 30px;
  border: 1px dashed gray;
  padding-left: 5px;
  padding-right: 5px;
  border-radius: 5px;
  margin-bottom: 5px;
}
.ciTypes-options {
  float: right;
  line-height: 30px;
}
.ciTypes {
  float: left;
  line-height: 30px;
}
.ciTypes-options {
  .ivu-checkbox-indeterminate .ivu-checkbox-inner {
    background-color: #4ee643;
  }
  .ivu-checkbox-disabled.ivu-checkbox-checked .ivu-checkbox-inner {
    background-color: #2d8cf0;
  }
}
.icon-copy {
  font-size: 19px;
  cursor: pointer;
  color: #42b983;
}
</style>
