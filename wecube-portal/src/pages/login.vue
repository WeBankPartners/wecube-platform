<template>
  <div>
    <div class="body"></div>
    <div class="header-login">
      <div></div>
    </div>
    <br />
    <div class="login-form">
      <Input type="text" placeholder="username" v-model="username" name="user" @on-enter="login" />

      <Input
        type="password"
        password
        placeholder="password"
        v-model="password"
        name="password"
        @on-enter="login"
        style="margin-top: 20px"
      />
      <Button type="primary" long @click="login" :loading="loading" style="margin-top: 20px"> Login </Button>
      <!-- <Button type="success" long>SUBMIT</Button> -->
    </div>

    <Modal v-model="showRoleApply" :mask-closable="false" :closable="false" :title="$t('be_apply_roles')">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="80">
        <FormItem :label="$t('be_um_account')" prop="userName">
          <Input v-model="formValidate.userName" disabled></Input>
        </FormItem>
        <FormItem :label="$t('be_email')" prop="emailAddr">
          <Input v-model="formValidate.emailAddr" :placeholder="$t('be_email')"></Input>
        </FormItem>
        <FormItem :label="$t('role')" prop="roleIds">
          <Select
            v-model="formValidate.roleIds"
            @on-open-change="getApplyRoles"
            multiple
            filterable
            :placeholder="$t('be_apply_roles')"
          >
            <Option v-for="role in roleList" :value="role.id" :key="role.id">{{ role.displayName }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="handleReset('formValidate')">{{ $t('cancel') }}</Button>
        <Button @click="handleSubmit('formValidate')" type="primary">{{ $t('be_apply') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import CryptoJS from 'crypto-js'
import {
  login, getApplyRoles, registerUser, getEncryptKey
} from '../api/server'
import { setCookie, clearCookie } from './util/cookie'
export default {
  data() {
    return {
      username: '',
      password: '',
      encryptKey: '',
      loading: false,
      showRoleApply: false,
      formValidate: {
        userName: '',
        emailAddr: '',
        roleIds: []
      },
      ruleValidate: {
        emailAddr: [
          {
            required: true,
            message: `${this.$t('be_email')} ${this.$t('cannotBeEmpty')}`,
            trigger: 'blur'
          },
          {
            type: 'email',
            message: this.$t('be_email_incorrect_format'),
            trigger: 'blur'
          }
        ],
        roleIds: [
          {
            required: true,
            type: 'array',
            min: 1,
            message: `${this.$t('role')} ${this.$t('cannotBeEmpty')}`,
            trigger: 'change'
          }
        ]
      },
      roleList: []
    }
  },

  methods: {
    async login() {
      if (!this.username || !this.password) {
        return
      }
      this.loading = true
      await this.getEncryptKey()
      const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
      const config = {
        iv: CryptoJS.enc.Utf8.parse(Math.trunc(new Date() / 100000) * 100000000),
        mode: CryptoJS.mode.CBC
      }
      const encryptedPassword = CryptoJS.AES.encrypt(this.password, key, config).toString()
      const payload = {
        username: this.username,
        password: encryptedPassword
      }
      const { status, data } = await login(payload)
      if (status === 'OK') {
        const localStorage = window.localStorage
        setCookie(data)
        localStorage.setItem('username', this.username)
        const needRegister = data.needRegister || false
        if (needRegister) {
          this.showRoleApply = true
          this.formValidate.userName = this.username
        } else {
          this.$router.push('/homepage')
        }
      }
      this.loading = false
    },
    async getEncryptKey() {
      const { status, data } = await getEncryptKey()
      if (status === 'OK') {
        this.encryptKey = data
      }
    },
    async getApplyRoles() {
      const params = {
        all: 'N', // Y:所有(包括未激活和已删除的) N:激活的
        roleAdmin: false
      }
      const { status, data } = await getApplyRoles(params)
      if (status === 'OK') {
        this.roleList = data || []
      }
    },
    handleSubmit(name) {
      this.$refs[name].validate(async valid => {
        if (valid) {
          const { status } = await registerUser(this.formValidate)
          if (status === 'OK') {
            this.$Notice.success({
              title: this.$t('successful'),
              desc: this.$t('be_apply_success')
            })
            this.showRoleApply = false
          }
        }
      })
    },
    handleReset(name) {
      this.$refs[name].resetFields()
      this.showRoleApply = false
    },
    clearData() {
      const localStorage = window.localStorage
      localStorage.removeItem('username')
      clearCookie()
      window.needReLoad = true
    }
  },
  mounted() {
    this.clearData()
  }
}
</script>
<style scoped>
.body {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url('../assets/bg.jpg');
  background-size: cover;
  -webkit-filter: blur(3px);
  z-index: 0;
}

.header-login {
  position: absolute;
  top: calc(50% - 35px);
  left: calc(50% - 355px);
  z-index: 2;
}

.header-login div {
  width: 600px;
  height: 50px;
  background-image: url('../assets/wecube-logo.png');
  background-size: contain;
  background-repeat: no-repeat;
}

.login-form {
  position: absolute;
  top: calc(50% - 75px);
  left: calc(50% - 50px);
  height: 150px;
  width: 230px;
  padding: 10px;
  z-index: 2;
  text-align: center;
}
</style>
