<template>
  <div>
    <Row>
      <Col span="11">
        <Card>
          <p slot="title">
            {{ $t("user") }}
            <Button
              icon="ios-add"
              type="dashed"
              size="small"
              @click="openAddUserModal"
              >{{ $t("add_user") }}</Button
            >
          </p>
          <Tag
            v-for="item in users"
            :key="item.id"
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
        </Card>
      </Col>
      <Col span="12" offset="1">
        <Card>
          <p slot="title">
            {{ $t("role") }}
            <Button
              icon="ios-add"
              type="dashed"
              size="small"
              @click="openAddRoleModal"
              >{{ $t("add_role") }}</Button
            >
          </p>
          <div class="role-item" v-for="item in roles" :key="item.id">
            <Tag
              :name="item.id"
              :color="item.color"
              :checked="item.checked"
              checkable
              :fade="false"
              @on-change="handleRoleClick"
            >
              <span :title="item.displayName">{{ item.displayName }}</span>
            </Tag>
            <Button
              icon="ios-build"
              type="dashed"
              size="small"
              @click="openUserManageModal(item.id)"
              >{{ $t("user") }}</Button
            >
          </div>
        </Card>
      </Col>
    </Row>
    <Modal
      v-model="addUserModalVisible"
      :title="$t('add_user')"
      @on-ok="addUser"
      @on-cancel="cancel"
    >
      <Form
        class="validation-form"
        ref="addedUserForm"
        :model="addedUser"
        label-position="left"
        :label-width="100"
      >
        <FormItem :label="$t('username')" prop="username">
          <Input v-model="addedUser.username" />
        </FormItem>
        <FormItem :label="$t('password')" prop="fullName">
          <Input type="password" v-model="addedUser.password" />
        </FormItem>
      </Form>
    </Modal>
    <Modal
      v-model="addRoleModalVisible"
      :title="$t('add_role')"
      @on-ok="addRole"
      @on-cancel="cancel"
    >
      <Form
        class="validation-form"
        :model="addedRole"
        label-position="left"
        :label-width="100"
      >
        <FormItem :label="$t('role')">
          <Input v-model="addedRole.name" :placeholder="$t('please_input')" />
        </FormItem>
        <FormItem :label="$t('display_name')">
          <Input
            v-model="addedRole.displayName"
            :placeholder="$t('please_input')"
          />
        </FormItem>
      </Form>
    </Modal>
    <Modal
      v-model="userManageModal"
      width="700"
      :title="$t('edit_user')"
      @on-ok="confirmUser"
      @on-cancel="confirmUser"
    >
      <Transfer
        :titles="transferTitles"
        :list-style="transferStyle"
        :data="allUsersForTransfer"
        :target-keys="usersKeyBySelectedRole"
        :render-format="renderUserNameForTransfer"
        @on-change="handleUserTransferChange"
        filterable
      ></Transfer>
    </Modal>
  </div>
</template>
<script>
import {
  userCreate,
  getUserList,
  deleteUser,
  roleCreate,
  getRoleList,
  deleteRole,
  getRolesByUserName,
  getUsersByRoleId,
  grantRolesForUser,
  revokeRolesForUser
} from "@/api/server";
export default {
  data() {
    return {
      users: [],
      roles: [],
      addedUser: {},
      addedRole: {},
      addedRoleValue: "",
      transferTitles: [this.$t("unselected_user"), this.$t("selected_user")],
      transferStyle: { width: "300px" },
      usersKeyBySelectedRole: [],
      allUsersForTransfer: [],
      addUserModalVisible: false,
      addRoleModalVisible: false,
      userManageModal: false
    };
  },
  methods: {
    renderUserNameForTransfer(item) {
      return item.label;
    },
    async getAllUsers() {
      let { status, data, message } = await getUserList();
      if (status === "OK") {
        this.users = data.map(_ => {
          return {
            ..._,
            checked: false,
            color: "#5cadff"
          };
        });
      }
    },
    async getAllRoles() {
      let { status, data, message } = await getRoleList();
      if (status === "OK") {
        this.roles = data.map(_ => {
          return {
            ..._,
            checked: false,
            color: "success"
          };
        });
      }
    },
    async handleUserClick(checked, name) {},
    async handleRoleClick(checked, id) {},
    async handleUserTransferChange(newTargetKeys, direction, moveKeys) {
      if (direction === "right") {
        let { status, data, message } = await addUsersToRole(
          moveKeys,
          this.selectedRole
        );
        if (status === "OK") {
          this.$Notice.success({
            title: "success",
            desc: message
          });
          this.usersKeyBySelectedRole = newTargetKeys;
        }
      } else {
        let { status, data, message } = await romoveUsersFromRole(
          moveKeys,
          this.selectedRole
        );
        if (status === "OK") {
          this.$Notice.success({
            title: "success",
            desc: message
          });
          this.usersKeyBySelectedRole = newTargetKeys;
        }
      }
    },
    confirmUser() {
      if (this.currentRoleId !== 0) {
        this.handleRoleClick(true, this.currentRoleId);
      }
    },
    async openUserManageModal(id) {
      this.userManageModal = true;
    },
    async addUser() {
      if (!this.addedUser.username) {
        this.$Notice.warning({
          title: "Warning",
          desc: this.$t("username_cannot_empty")
        });
        return;
      }
      if (!this.addedUser.password) {
        this.$Notice.warning({
          title: "Warning",
          desc: this.$t("password_cannot_empty")
        });
        return;
      }
      let { status, data, message } = await userCreate(this.addedUser);
      if (status === "OK") {
        this.$Notice.success({
          title: "success",
          desc: message
        });
        this.addedUser = {};
        this.getAllUsers();
      }
    },
    async addRole() {
      if (!this.addedRole.name) {
        this.$Notice.warning({
          title: "Warning",
          desc: this.$t("role_cannot_empty")
        });
        return;
      }
      let { status, data, message } = await roleCreate(this.addedRole);
      if (status === "OK") {
        this.$Notice.success({
          title: "success",
          desc: message
        });
        this.getAllRoles();
      }
    },
    openAddRoleModal() {
      this.addRoleModalVisible = true;
    },
    openAddUserModal() {
      this.addUserModalVisible = true;
    },
    cancel() {}
  },
  created() {
    this.getAllUsers();
    this.getAllRoles();
  }
};
</script>
<style lang="scss" scoped>
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
</style>
