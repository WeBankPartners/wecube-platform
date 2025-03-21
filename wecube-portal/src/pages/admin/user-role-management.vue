<template>
  <div>
    <Row>
      <Col span="8">
        <Card>
          <p slot="title">
            {{ $t('user') }}
            <Button icon="ios-add" type="success" ghost size="small" @click="openAddUserModal">{{
              $t('add_user')
            }}</Button>
          </p>
          <div style="display: flex">
            <Input
              v-model="userFilter"
              clearable 
              :placeholder="$t('username')" 
              @on-change="onUserFilterChange"
              style="margin-bottom: 12px; margin-right: 5px; width: 40%" 
            />
            <Select
              v-model="currentRoleId" 
              filterable 
              clearable
              :placeholder="$t('please_choose') + $t('role')"
              @on-change="onFilterUserRoleChange" 
              @on-clear="onFilterUserRoleClear"
              style="width: 50%" 
            >
              <Option v-for="item in roles" :value="item.id" :key="item.id" :label="item.username">
                {{ item.name + '(' + item.displayName + ')' }}
              </Option>
            </Select>
          </div>
          <div class="user-tag-containers">
            <div class="user-item" v-for="item in userFilterRes" :key="item.id">
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
              <Tooltip :content="$t('edit')">
                <Button
                  icon="ios-create-outline"
                  type="primary"
                  ghost
                  size="small"
                  @click="editUserEmail(item)"
                ></Button>
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
            <div 
              v-if="!userFilterRes.length" 
              style="display: flex; justify-content: center; margin-top: 10px"
>
              {{ $t('noData') }}
            </div>
          </div>
          <Page
            :styles="{marginBottom: '-10px'}"
            :total="pagination.total"
            @on-change="
              e => {
                pagination.page = e
                this.getAllUsers()
              }
            "
            :current="pagination.page"
            :page-size="pagination.size"
            size="small"
            show-total
          />
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
          <Input v-model="roleFilter" clearable :placeholder="$t('pi_enter_to_filter')" style="margin-bottom: 12px" />
          <div class="tagContainers">
            <div class="role-item" v-for="item in roleFilterRes" :key="item.id">
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
          <div class="tagContainers-menus">
            <Spin size="large" fix v-if="menuTreeLoading">
              <Icon type="ios-loading" size="24" class="spin-icon-load"></Icon>
              <div>{{ $t('loading') }}</div>
            </Spin>
            <Tree :data="menus" show-checkbox @on-check-change="handleMenuTreeCheck"></Tree>
          </div>
        </Card>
      </Col>
    </Row>
    <Modal v-model="addUserModalVisible" :title="$t('add_user')">
      <Form
        v-if="addUserModalVisible"
        class="validation-form"
        ref="addedUserForm"
        :model="addedUser"
        label-position="left"
        :label-width="100"
      >
        <FormItem :label="$t('username')" prop="username">
          <Input v-model.trim="addedUser.username" />
        </FormItem>
        <FormItem :label="$t('auth_type')" prop="authtype">
          <RadioGroup v-model="addedUser.authType">
            <Radio label="LOCAL"></Radio>
            <Radio label="UM"></Radio>
          </RadioGroup>
        </FormItem>
        <FormItem v-if="addedUser.authType === 'LOCAL'" :label="$t('password')" prop="password">
          <Input type="password" v-model.trim="addedUser.password" />
        </FormItem>
        <FormItem :label="$t('email')">
          <Input v-model.trim="addedUser.email" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="addUserModalVisible = false">{{ $t('cancel') }}</Button>
        <Button type="primary" @click="addUser">{{ $t('save') }}</Button>
      </div>
    </Modal>
    <Modal v-model="addedRole.isShow" :title="addedRole.isAdd ? $t('add_role') : $t('edit_role_')">
      <Form :model="addedRole" label-position="right" :label-width="100">
        <FormItem v-if="addedRole.isAdd">
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('role') }}
          </label>
          <Input v-model.trim="addedRole.params.name" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('display_name') }}
          </label>
          <Input v-model.trim="addedRole.params.displayName" clearable :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('email') }}
          </label>
          <Input v-model.trim="addedRole.params.email" clearable :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('role_admin') }}
          </label>
          <Select v-model="addedRole.params.administrator" filterable clearable>
            <Option v-for="item in initAllUserInfo" :value="item.id" :key="item.id" :label="item.username">{{
              item.username
            }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('status')" v-if="!addedRole.isAdd">
          <Checkbox v-model="addedRole.params.status">{{ $t('disable_role') }}</Checkbox>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="cancel">{{ $t('cancel') }}</Button>
        <Button type="primary" @click="addRole">{{ $t('save') }}</Button>
      </div>
    </Modal>
    <Modal v-model="editUser.isShow" :title="$t('edit') + $t('user')">
      <Form label-position="right" :label-width="100">
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('email') }}
          </label>
          <Input v-model.trim="editUser.params.emailAddr" :placeholder="$t('please_input')" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="editUser.isShow = false">{{ $t('cancel') }}</Button>
        <Button type="primary" :disabled="editUser.params.emailAddr === ''" @click="confirmEditUserEmail">{{
          $t('save')
        }}</Button>
      </div>
    </Modal>
    <Modal v-model="userManageModal" width="800" :title="$t('edit_user')">
      <div style="width: 100%; overflow-x: auto">
        <div style="min-width: 760px; display: flex; justify-content: center">
          <Transfer
            v-if="userManageModal"
            :titles="transferTitles"
            :list-style="transferStyle"
            :data="allUsersForTransfer"
            :target-keys="usersKeyBySelectedRole"
            :render-format="renderUserNameForTransfer"
            @on-change="handleUserTransferChange"
            filterable
          ></Transfer>
        </div>
      </div>
      <div slot="footer">
        <Button type="primary" @click="confirmUser">{{ $t('close') }}</Button>
      </div>
    </Modal>
    <Modal v-model="showNewPassword" :title="$t('new_password')">
      <Form class="validation-form" label-position="left" :label-width="100">
        <FormItem :label="$t('new_password')">
          <Input v-model.trim="newPassword" :placeholder="$t('please_input')" style="width: 300px" />
          <Icon @click="copyPassword" class="icon-copy" type="md-copy" />
        </FormItem>
      </Form>
    </Modal>
    <Modal v-model="addRoleToUser.isShow" :title="$t('add_role')">
      <Form class="validation-form" label-position="left" :label-width="100">
        <FormItem :label="$t('role')">
          <Select v-model="addRoleToUser.params.roles" multiple filterable>
            <Option v-for="item in addRoleToUser.allRoles" :value="item.id" :key="item.id">{{
              item.displayName
            }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="addRoleToUser.isShow = false">{{ $t('cancel') }}</Button>
        <Button :disabled="addRoleToUser.params.roles.length === 0" type="primary" @click="confirmAddRoleToUser">{{
          $t('save')
        }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import {
  userCreate,
  removeUser,
  getUserList,
  getAllUserList,
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
  resetPassword,
  editUser
} from '@/api/server'
import { debounce } from '@/const/util'
import { MENUS } from '@/const/menus.js'

export default {
  data() {
    return {
      userFilter: '', // 用户过滤条件
      userFilterRes: [],
      roleFilter: '', // 用户过滤条件
      roleFilterRes: [],
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
      currentRoleId: '',
      users: [],
      selectedUser: '',
      roles: [],
      addedUser: {
        username: '',
        authType: 'LOCAL',
        email: '',
        password: ''
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
      editUser: {
        isShow: false,
        params: {
          emailAddr: ''
        }
      },
      addedRoleValue: '',
      transferTitles: [this.$t('unselected_user'), this.$t('selected_user')],
      transferStyle: {
        width: '300px',
        height: '300px'
      },
      usersKeyBySelectedRole: [],
      allUsersForTransfer: [],
      addUserModalVisible: false,
      userManageModal: false,
      originMenus: [],
      menus: [],
      menuTreeLoading: false,
      pagination: {
        total: 50,
        page: 1,
        size: 20
      },
      initAllUserInfo: []
    }
  },
  watch: {
    roleFilter: {
      handler: debounce(function (newValue) {
        const filter = newValue.trim()
        if (filter === '') {
          this.roleFilterRes = this.roles
        } else {
          this.roleFilterRes = this.roles.filter(r => r.name.includes(filter) || r.displayName.includes(filter))
        }
      }, 300), // 300ms防抖时间'
      immediate: true
    }
  },
  methods: {
    compare(prop) {
      return function (obj1, obj2) {
        let val1 = obj1[prop]
        let val2 = obj2[prop]
        if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
          val1 = Number(val1)
          val2 = Number(val2)
        }
        if (val1 < val2) {
          return -1
        } else if (val1 > val2) {
          return 1
        }
        return 0
      }
    },
    async confirmAddRoleToUser() {
      const { status } = await addRoleToUser(this.addRoleToUser.params.id, this.addRoleToUser.params.roles)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: ''
        })
        this.handleUserClick(true, this.addRoleToUser.params.username)
        this.addRoleToUser.isShow = false
      }
    },
    async addRoleToUsers(item) {
      this.addRoleToUser.params = { ...item }
      const allPromiseArr = []
      allPromiseArr.push(getRolesByUserName(item.username), getRoleList())
      const finallArr = await Promise.all(allPromiseArr)
      const { status, data } = finallArr[0]
      if (status === 'OK') {
        this.addRoleToUser.params.roles = data.map(d => d.id)
      }
      const res = finallArr[1]
      if (res.status === 'OK') {
        this.addRoleToUser.allRoles = res.data
        this.addRoleToUser.isShow = true
      }
    },
    removeUser(item) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          const { status } = await removeUser(item.id)
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
    async resetRolePassword(item) {
      this.$Modal.confirm({
        title: this.$t('reset_password'),
        'z-index': 1000000,
        onOk: async () => {
          const params = {
            username: item.username
          }
          const { status, data } = await resetPassword(params)
          if (status === 'OK') {
            this.newPassword = data
            this.showNewPassword = true
          }
        },
        onCancel: () => {}
      })
    },
    copyPassword() {
      const inputElement = document.createElement('input')
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
    async handleMenuTreeCheck(allChecked) {
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
    async getAllMenus() {
      const { status, data } = await getAllMenusList()
      if (status === 'OK') {
        this.originMenus = data
      }
      this.menus = this.menusResponseHandeler(this.originMenus)
    },
    menusPermissionSelected(allMenus, menusPermissions = [], disabled) {
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
    menusResponseHandeler(data, disabled = true) {
      const menus = []
      data.forEach(_ => {
        if (!_.category) {
          const menuObj = MENUS.find(m => m.code === _.code)
          menus.push({
            ..._,
            title:
              _.source === 'SYSTEM' ? (this.$i18n.locale === 'zh-CN' ? menuObj.cnName : menuObj.enName) : _.displayName,
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
          const menuObj = MENUS.find(m => m.code === _.code)
          menus.forEach(h => {
            if (_.category === h.id) {
              h.children.push({
                ..._,
                title:
                  _.source === 'SYSTEM'
                    ? this.$i18n.locale === 'zh-CN'
                      ? menuObj.cnName
                      : menuObj.enName
                    : this.$i18n.locale === 'zh-CN'
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
    renderUserNameForTransfer(item) {
      return item.label
    },
    async getAllUsers(isfullRequest = true) {
      const params = {
        roleId: this.currentRoleId,
        userName: this.userFilter.trim(),
        pageSize: this.pagination.size,
        startIndex: (this.pagination.page - 1) * this.pagination.size
      }
      const { status, data } = await getAllUserList(params)
      if (status === 'OK') {
        const tempUsers = data.contents || []
        this.pagination.total = data.pageInfo.totalRows
        this.users = tempUsers.map(_ => ({
          ..._,
          checked: this.currentRoleId ? true : false,
          color: '#5cadff'
        }))
        this.userFilterRes = this.users
      }
      if (isfullRequest) {
        this.getAllRoles()
        this.getInitAllUserInfo()
      }
    },
    async getAllRoles() {
      const params = { all: 'Y' }
      const { status, data } = await getRoleList(params)
      if (status === 'OK') {
        this.roles = data.map(_ => ({
          ..._,
          checked: false,
          color: 'success'
        }))
        this.roles.sort(this.compare('status')).reverse()
        this.roleFilterRes = this.roles
      }
    },
    async handleUserClick(checked, name) {
      this.selectedUser = name
      this.currentRoleId = ''
      this.users.forEach(_ => {
        _.checked = false
        if (name === _.username) {
          _.checked = checked
        }
      })
      if (checked) {
        getMenusByUserName(name).then(permissions => {
          if (permissions.status === 'OK') {
            const userMenus = [].concat(...(permissions.data || []).map(_ => _.menuList))
            this.menusPermissionSelected(this.menus, userMenus, true)
          }
        })
        getRolesByUserName(name).then(res => {
          if (res.status === 'OK') {
            this.roles.forEach(_ => {
              _.checked = false
              const found = res.data.find(item => item.id === _.id)
              if (found) {
                _.checked = checked
              }
            })
          }
        })
      } else {
        this.roles.forEach(_ => {
          _.checked = false
        })
        this.menusPermissionSelected(this.menus, [], true)
      }
    },
    async handleRoleClick(checked, id) {
      const find = this.roles.filter(r => r.id === id)
      if (find[0].status === 'Deleted') {
        return
      }
      this.currentRoleId = id
      this.roles.forEach(_ => {
        _.checked = false
        if (id === _.id) {
          _.checked = checked
        }
      })
      this.menus = this.menusResponseHandeler(this.originMenus, !checked)
      if (checked) {
        getMenusByRoleId(id).then(permissions => {
          if (permissions.status === 'OK') {
            this.menusPermissionSelected(this.menus, permissions.data.menuList, false)
          }
        })
        this.userFilter = ''
        this.getAllUsers(false)
      } else {
        this.users.forEach(_ => {
          _.checked = false
        })
      }
    },
    async handleUserTransferChange(newTargetKeys, direction, moveKeys) {
      if (direction === 'right') {
        const { status, message } = await grantRolesForUser(moveKeys, this.selectedRole)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'success',
            desc: message
          })
          this.usersKeyBySelectedRole = newTargetKeys
        }
      } else {
        const { status, message } = await revokeRolesForUser(moveKeys, this.selectedRole)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'success',
            desc: message
          })
          this.usersKeyBySelectedRole = newTargetKeys
        }
      }
    },
    async confirmUser() {
      if (this.selectedUser) {
        await this.handleUserClick(true, this.selectedUser)
      }
      this.userManageModal = false
    },
    async openUserManageModal(id) {
      this.transferStyle.height = window.innerHeight - 260 + 'px'
      this.usersKeyBySelectedRole = []
      this.allUsersForTransfer = []
      this.selectedRole = id
      const { status, data } = await getUsersByRoleId(id)
      if (status === 'OK') {
        this.usersKeyBySelectedRole = data.map(_ => _.id)
      }
      this.allUsersForTransfer = this.initAllUserInfo.map(_ => ({
        key: _.id,
        username: _.username,
        label: _.username || ''
      }))
      this.userManageModal = true
    },
    editUserEmail(item) {
      this.editUser.params = JSON.parse(JSON.stringify(item))
      this.editUser.isShow = true
    },
    async confirmEditUserEmail() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!this.editUser.params.emailAddr) {
        this.$Message.warning(`${this.$t('email')}${this.$t('cannotBeEmpty')}`)
        return
      } else if (!emailRegex.test(this.editUser.params.emailAddr)) {
        this.$Message.warning(`${this.$t('email')}${this.$t('invalidFormat')}`)
        return
      }
      this.editUser.params.email = this.editUser.params.emailAddr
      const { status, message } = await editUser(this.editUser.params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.editUser.isShow = false
        this.getAllUsers()
      }
    },
    async addUser() {
      if (!this.addedUser.username) {
        this.$Message.warning(`${this.$t('username_cannot_empty')}`)
        return
      }
      // eslint-disable-next-line no-useless-escape
      if (this.addedUser.authType === 'LOCAL') {
        const res = []
        res.push(/[A-Z]/.test(this.addedUser.password))
        res.push(/.*[a-z].*/.test(this.addedUser.password))
        res.push(/\d/.test(this.addedUser.password))
        res.push(/[^\w\s]/.test(this.addedUser.password))
        if (res.filter(item => item === false).length > 1) {
          this.$Message.warning(`${this.$t('be_warning_for_password')}`)
          return
        }
        if (this.addedUser.password.length < 8) {
          this.$Message.warning(`${this.$t('password')}${this.$t('atLeast')}8${this.$t('characters')}`)
          return
        }
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!this.addedUser.email) {
        this.$Message.warning(`${this.$t('email')}${this.$t('cannotBeEmpty')}`)
        return
      } else if (!emailRegex.test(this.addedUser.email)) {
        this.$Message.warning(`${this.$t('email')}${this.$t('invalidFormat')}`)
        return
      }

      const { status, message } = await userCreate(this.addedUser)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.addedUser = {
          authType: 'LOCAL'
        }
        this.addUserModalVisible = false
        this.pagination.page = 1
        this.getAllUsers()
      }
    },
    async addRole() {
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
      const { status, message } = await method
      if (status === 'OK') {
        this.$Notice.success({
          title: 'success',
          desc: message
        })
        this.cancel()
        this.getAllRoles()
      }
    },
    openAddRoleModal() {
      this.addedRole.isAdd = true
      this.addedRole.params.name = ''
      this.addedRole.params.displayName = ''
      this.addedRole.params.email = ''
      this.addedRole.params.administrator = ''
      this.addedRole.isShow = true
    },
    editRole(item) {
      this.addedRole.isAdd = false
      this.addedRole.params = { ...item }
      if (item.status === 'Deleted') {
        this.addedRole.params.status = true
      } else {
        this.addedRole.params.status = false
      }
      this.addedRole.isShow = true
    },
    openAddUserModal() {
      this.addedUser.username = ''
      this.addedUser.authType = 'LOCAL'
      this.addedUser.password = ''
      this.addedUser.email = ''
      this.addUserModalVisible = true
    },
    cancel() {
      this.addedRole.isShow = false
    },
    async getInitAllUserInfo() {
      const { status, data } = await getUserList()
      if (status === 'OK') {
        this.initAllUserInfo = data
      }
    },
    getFirstPageUsers() {
      this.pagination.page = 1
      this.getAllUsers(false)
    },
    onUserFilterChange: debounce(function () {
      this.getFirstPageUsers()
    }, 300),
    onFilterUserRoleChange(id) {
      if (!id) return
      this.roles.forEach(item => {
        if (item.id === id) {
          item.checked = true
        } else {
          item.checked = false
        }
      })
      this.handleRoleClick(true, id)
      
    },
    onFilterUserRoleClear() {
      this.roles.forEach(_ => {
        _.checked = false
      })
      this.getFirstPageUsers()
    }
  },
  created() {
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
.user-tag-containers {
  overflow: auto;
  height: calc(100vh - 265px);
}
.tagContainers {
  overflow: auto;
  height: calc(100vh - 250px);
}
.tagContainers-menus {
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

.user-item {
  .ivu-tag {
    display: inline-block;
    width: calc(100% - 140px);
  }
}
.role-item {
  .ivu-tag {
    display: inline-block;
    width: calc(100% - 74px);
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
    background-color: #5384ff;
  }
}
.icon-copy {
  font-size: 19px;
  cursor: pointer;
  color: #42b983;
}
</style>
