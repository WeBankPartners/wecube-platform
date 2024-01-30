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
                <Button icon="md-trash" type="error" ghost size="small" @click="removeUser(item)"></Button>
              </Tooltip>
              <Tooltip :content="$t('role')">
                <Button icon="ios-contacts" type="info" ghost size="small" @click="addRoleToUsers(item)"></Button>
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
                <span :style="item.status === 'Deleted' ? 'color:#dcdee2' : ''" :title="item.displayName">{{
                  item.name + '(' + item.displayName + ')'
                }}</span>
              </Tag>
              <Tooltip :content="$t('edit')">
                <Button icon="ios-create-outline" type="info" ghost size="small" @click="editRole(item)"></Button>
              </Tooltip>
              <Tooltip :content="$t('user')">
                <Button
                  icon="ios-person"
                  type="primary"
                  ghost
                  size="small"
                  :disabled="item.status === 'Deleted'"
                  @click="openUserManageModal(item.id)"
                ></Button>
              </Tooltip>
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
    <Modal v-model="addedRole.isShow" :title="addedRole.isAdd ? $t('add_role') : $t('edit_role_')">
      <Form :model="addedRole" label-position="right" :label-width="100">
        <FormItem v-if="addedRole.isAdd">
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('role') }}
          </label>
          <Input v-model="addedRole.params.name" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('display_name') }}
          </label>
          <Input v-model="addedRole.params.displayName" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('email') }}
          </label>
          <Input v-model="addedRole.params.email" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('role_admin') }}
          </label>
          <Select v-model="addedRole.params.administrator" filterable>
            <Option v-for="item in users" :value="item.id" :key="item.id">{{ item.username }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('status')" v-if="!addedRole.isAdd">
          <Checkbox v-model="addedRole.params.status">{{ $t('disable_role') }}</Checkbox>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button ghost @click="cancel">{{ $t('cancel') }}</Button>
        <Button type="primary" @click="addRole">{{ $t('save') }}</Button>
      </div>
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
          <Input v-model="newPassword" :placeholder="$t('please_input')" style="width: 300px" />
          <Icon @click="copyPassword" class="icon-copy" type="md-copy" />
        </FormItem>
      </Form>
    </Modal>
    <Modal
      v-model="addRoleToUser.isShow"
      :title="$t('add_role')"
      @on-ok="confirmAddRoleToUser"
      @on-cancel="addRoleToUser.isShow = false"
    >
      <Form class="validation-form" label-position="left" :label-width="100">
        <FormItem :label="$t('role')">
          <Select v-model="addRoleToUser.params.roles" multiple filterable>
            <Option v-for="item in addRoleToUser.allRoles" :value="item.id" :key="item.id">{{
              item.displayName
            }}</Option>
          </Select>
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
  updateRole,
  getRolesByUserName,
  getUsersByRoleId,
  grantRolesForUser,
  revokeRolesForUser,
  getAllMenusList,
  addRoleToUser,
  getMenusByUserName,
  getMenusByRoleId,
  updateRoleToMenusByRoleId,
  resetPassword
} from '@/api/server'
import { MENUS } from '@/const/menus.js'

export default {
  data () {
    return {
      addRoleToUser: {
        isShow: false,
        params: {
          id: '',
          username: '',
          roles: []
        },
        allRoles: []
      },
      showNewPassword: false,
      newPassword: '',
      currentRoleId: 0,
      users: [],
      selectedUser: '',
      roles: [],
      addedUser: {
        authType: 'LOCAL'
      },
      addedRole: {
        isShow: false,
        isAdd: false,
        params: {
          id: '',
          name: '',
          displayName: '',
          email: '',
          administrator: '',
          status: false
        }
      },
      addedRoleValue: '',
      transferTitles: [this.$t('unselected_user'), this.$t('selected_user')],
      transferStyle: { width: '300px' },
      usersKeyBySelectedRole: [],
      allUsersForTransfer: [],
      addUserModalVisible: false,
      userManageModal: false,
      originMenus: [],
      menus: [],
      menuTreeLoading: false
    }
  },
  methods: {
    compare (prop) {
      return function (obj1, obj2) {
        var val1 = obj1[prop]
        var val2 = obj2[prop]
        if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
          val1 = Number(val1)
          val2 = Number(val2)
        }
        if (val1 < val2) {
          return -1
        } else if (val1 > val2) {
          return 1
        } else {
          return 0
        }
      }
    },
    async confirmAddRoleToUser () {
      let { status } = await addRoleToUser(this.addRoleToUser.params.id, this.addRoleToUser.params.roles)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: ''
        })
        this.handleUserClick(true, this.addRoleToUser.params.username)
      }
    },
    async addRoleToUsers (item) {
      this.addRoleToUser.params = { ...item }
      let { status, data } = await getRolesByUserName(item.username)
      if (status === 'OK') {
        this.addRoleToUser.params.roles = data.map(d => d.id)
      }
      let res = await getRoleList()
      if (res.status === 'OK') {
        this.addRoleToUser.allRoles = res.data
        this.addRoleToUser.isShow = true
      }
    },
    removeUser (item) {
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
      let params = { all: 'Y' }
      let { status, data } = await getRoleList(params)
      if (status === 'OK') {
        this.roles = data.map(_ => {
          return {
            ..._,
            checked: false,
            color: 'success'
          }
        })
        this.roles.sort(this.compare('status')).reverse()
      }
    },
    async handleUserClick (checked, name) {
      this.selectedUser = name
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
      const find = this.roles.filter(r => r.id === id)
      if (find[0].status === 'Deleted') return
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
    async confirmUser () {
      if (this.selectedUser) {
        await this.handleUserClick(true, this.selectedUser)
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
      if (this.addedRole.isAdd && !this.addedRole.params.name) {
        this.$Message.warning(`${this.$t('role')}${this.$t('cannotBeEmpty')}`)
        return
      }
      if (!this.addedRole.params.displayName) {
        this.$Message.warning(`${this.$t('display_name')}${this.$t('cannotBeEmpty')}`)
        return
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!this.addedRole.params.email) {
        this.$Message.warning(`${this.$t('email')}${this.$t('cannotBeEmpty')}`)
        return
      } else if (!emailRegex.test(this.addedRole.params.email)) {
        this.$Message.warning(`${this.$t('email')}${this.$t('invalidFormat')}`)
        return
      }
      if (!this.addedRole.params.administrator) {
        this.$Message.warning(`${this.$t('role_admin')}${this.$t('cannotBeEmpty')}`)
        return
      }
      this.addedRole.params.status = this.addedRole.params.status ? 'Deleted' : 'NotDeleted'
      const method = this.addedRole.isAdd
        ? roleCreate(this.addedRole.params)
        : updateRole(this.addedRole.params.id, this.addedRole.params)
      let { status, message } = await method
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.cancel()
        this.getAllRoles()
      }
    },
    openAddRoleModal () {
      this.addedRole.isAdd = true
      this.addedRole.params.name = ''
      this.addedRole.params.displayName = ''
      this.addedRole.params.email = ''
      this.addedRole.params.administrator = ''
      this.addedRole.isShow = true
    },
    editRole (item) {
      this.addedRole.isAdd = false
      this.addedRole.params = { ...item }
      if (item.status === 'Deleted') {
        this.addedRole.params.status = true
      } else {
        this.addedRole.params.status = false
      }
      this.addedRole.isShow = true
    },
    openAddUserModal () {
      this.addedUser.username = ''
      this.addedUser.authType = 'LOCAL'
      this.addedUser.password = ''
      this.addUserModalVisible = true
    },
    cancel () {
      this.addedRole.isShow = false
    }
  },
  created () {
    this.getAllUsers()
    this.getAllRoles()
    this.getAllMenus()
  }
}
</script>
<style lang="scss" scoped>
.xxx {
  width: 80%;
  display: inline-block;
  border: 1px solid #d3d6d7;
  padding: 2px;
  margin: 2px;
}
.ivu-form-item {
  margin-bottom: 8px;
}
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
