<template>
  <div>
    <div class="body"></div>
    <div class="header-login">
      <div></div>
    </div>
    <br />
    <div class="login-form">
      <Input type="text" placeholder="username" v-model="username" name="user" @on-enter="handleLogin" />
      <Input
        type="password"
        password
        placeholder="password"
        v-model="password"
        name="password"
        @on-enter="handleLogin"
        style="margin-top: 20px"
      />
      <!-- 已完成绑定用户，直接输入TOTP验证码 -->
      <div v-if="needMfaCode && !qrCodeUrl" class="wc-login-totp-inline">
        <p class="wc-login-totp-inline-tip">Verify the code from the app</p>
        <div class="wc-login-totp-inline-row">
          <Input
            v-model="totpCode"
            :placeholder="$t('totp_code_placeholder')"
            :maxlength="6"
            class="wc-login-totp-code-inline"
            @on-enter="handleLogin"
          />
          <Button type="primary" class="wc-login-totp-verify-btn" @click="handleLogin" :loading="loading">
            Verify
          </Button>
        </div>
      </div>
      <Button
        v-else
        type="primary"
        long
        @click="handleLogin"
        :loading="loading"
        style="margin-top: 20px"
      >
        Login
      </Button>
    </div>

    <!-- TOTP二维码弹窗 -->
    <Modal v-model="showTotpVerify" :mask-closable="false" :closable="false" :title="$t('totp_setup_title')" width="680">
      <div class="wc-login-totp-modal">
        <p class="wc-login-totp-desc" v-html="getTotpSetupDesc()"></p>
        <!-- 二维码区域（首次登录时显示） -->
        <div v-if="qrCodeUrl" class="wc-login-totp-qr-section">
          <h3 class="wc-login-totp-section-title">{{ $t('totp_scan_qr_title') }}</h3>
          <p class="wc-login-totp-section-desc">
            {{ $t('totp_scan_qr_desc_prefix') }}
            <!-- <a href="#" @click.prevent="handleLearnMore">{{ $t('totp_learn_more') }}</a> -->
          </p>
          <div class="wc-login-totp-qr-wrapper">
            <canvas ref="qrCanvas" class="wc-login-totp-qr-code"></canvas>
          </div>
        </div>
        <!-- 验证码输入区域 -->
        <div class="wc-login-totp-verify-section">
          <h3 class="wc-login-totp-section-title">{{ $t('totp_verify_code_title') }}</h3>
          <Input
            v-model="totpCode"
            :placeholder="$t('totp_code_placeholder')"
            :maxlength="6"
            @on-enter="handleVerifyTotp"
          />
        </div>
      </div>
      <div slot="footer">
        <Button @click="handleCancelTotp">{{ $t('cancel') }}</Button>
        <Button type="primary" @click="handleVerifyTotp" :loading="totpVerifying">{{ $t('totp_continue') }}</Button>
      </div>
    </Modal>

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
import QRCode from 'qrcode'
import {
  login, verifyMfa, getApplyRoles, registerUser, getEncryptKey
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
      // TOTP 相关数据
      showTotpVerify: false,
      qrCodeUrl: '',
      totpCode: '',
      needMfaCode: false,
      totpVerifying: false,
      tempToken: '',
      encryptedPassword: '', // 保存加密后的密码，用于 TOTP 验证
      roleList: [],
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
      }
    }
  },

  methods: {
    /**
     * 如果返回 qrCodeUrl，则打开 TOTP 验证弹窗（未绑定）
     * 如果返回 needMfaCode，则在页面内输入验证码后再次点击登录完成校验（已绑定）
     * 未启用 MFA 时直接完成登录
     */
    async handleLogin() {
      // 已绑定用户的第二步校验
      if (this.needMfaCode && !this.qrCodeUrl) {
        if (!this.totpCode || this.totpCode.length !== 6) {
          this.$Notice.warning({
            title: this.$t('totp_tip'),
            desc: this.$t('totp_code_required')
          })
          return
        }
        await this.verifyMfaCode()
        return
      }

      // 首次登录或未启用 MFA 的登录
      this.clearData()
      if (!this.username || !this.password) {
        return
      }
      this.loading = true
      try {
        await this.getEncryptKey()
        const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
        const timeTag = Math.trunc(new Date() / 100000) * 100000000
        const iv = CryptoJS.enc.Utf8.parse(timeTag)
        const config = {
          iv,
          mode: CryptoJS.mode.CBC
        }
        const encryptedPassword = CryptoJS.AES.encrypt(this.password, key, config).toString() + '&\u0001' + timeTag
        // 保存加密后的密码，用于后续 TOTP 验证
        this.encryptedPassword = encryptedPassword
        const payload = {
          username: this.username,
          password: encryptedPassword
        }
        const { status, data } = await login(payload)
        if (status === 'OK') {
          this.handleLoginResponse(data)
        }
      } catch (error) {
        console.error('登录失败:', error)
        this.$Notice.error({
          title: this.$t('totp_error'),
          desc: error.message || this.$t('totp_verify_fail')
        })
      } finally {
        this.loading = false
      }
    },
    /**
     * 处理登录接口返回的三种场景
     * 1. data 为数组：未启用 MFA，直接登录
     * 2. 存在 qrCodeUrl：未绑定，展示二维码并等待验证码
     * 3. needMfaCode：已绑定，直接在页面输入验证码
     * @param {*} data 后端返回的 data
     */
    handleLoginResponse(data) {
      // 场景 1：未启用 MFA，直接返回 token 数组
      if (Array.isArray(data)) {
        this.finishLogin(data, data?.needRegister || false)
        return
      }

      // 场景 2：未绑定，展示二维码
      const qrUrl = data?.qrCodeUrl || ''
      if (qrUrl) {
        this.tempToken = data?.tempToken || ''
        this.qrCodeUrl = qrUrl
        this.needMfaCode = false
        this.totpCode = ''
        this.showTotpVerify = true
        this.$nextTick(() => {
          this.generateQRCode(qrUrl)
        })
        return
      }

      // 场景 3：已绑定，需要输入验证码
      if (data?.needMfaCode) {
        this.tempToken = data?.tempToken || ''
        this.needMfaCode = true
        this.qrCodeUrl = ''
        this.totpCode = ''
        this.$Notice.info({
          title: this.$t('totp_tip'),
          desc: this.$t('totp_code_required')
        })
        return
      }

      // 兜底：如果 data 仍是 token 数组或对象包含 tokens
      if (Array.isArray(data?.tokens)) {
        this.finishLogin(data.tokens, data?.needRegister || false)
        return
      }
      if (Array.isArray(data)) {
        this.finishLogin(data, data?.needRegister || false)
      }
    },
    /**
     * 获取 TOTP 设置说明文字（带链接）
     * @returns {string} HTML 字符串
     */
    getTotpSetupDesc() {
      const desc = this.$t('totp_setup_desc')
      return desc
        .replace('{1Password}', '<a href="https://1password.com/" target="_blank" rel="noopener noreferrer">1Password</a>')
        .replace('{Authy}', '<a href="https://authy.com/" target="_blank" rel="noopener noreferrer">Authy</a>')
        .replace('{MicrosoftAuthenticator}', '<a href="https://www.microsoft.com/zh-cn/security/mobile-authenticator-app" target="_blank" rel="noopener noreferrer">Microsoft Authenticator</a>')
    },
    /**
     * 生成二维码
     * @param {string} url - TOTP URL
     */
    async generateQRCode(url) {
      try {
        const canvas = this.$refs.qrCanvas
        if (canvas) {
          await QRCode.toCanvas(canvas, url, {
            width: 200,
            margin: 2
          })
        }
      } catch (error) {
        console.error('生成二维码失败:', error)
        this.$Notice.error({
          title: this.$t('totp_error'),
          desc: this.$t('totp_qr_generate_fail')
        })
      }
    },
    /**
     * 验证 TOTP 验证码（含未绑定弹窗场景）
     */
    async handleVerifyTotp() {
      if (!this.totpCode || this.totpCode.length !== 6) {
        this.$Notice.warning({
          title: this.$t('totp_tip'),
          desc: this.$t('totp_code_required')
        })
        return
      }
      await this.verifyMfaCode(true)
    },
    /**
     * 调用后端 MFA 验证接口
     * @param {boolean} isModal 是否来自二维码弹窗
     */
    async verifyMfaCode(isModal = false) {
      if (!this.tempToken) {
        this.$Notice.error({
          title: this.$t('totp_error'),
          desc: this.$t('totp_verify_fail')
        })
        return
      }
      const payload = {
        username: this.username,
        code: this.totpCode,
        tempToken: this.tempToken
      }
      if (isModal) {
        this.totpVerifying = true
      } else {
        this.loading = true
      }
      try {
        const { status, data } = await verifyMfa(payload)
        if (status === 'OK') {
          this.finishLogin(data)
          this.resetMfaState()
        }
      } catch (error) {
        console.error('TOTP 验证失败:', error)
        this.$Notice.error({
          title: this.$t('totp_verify_fail'),
          desc: error.message || this.$t('totp_code_error')
        })
      } finally {
        this.totpVerifying = false
        this.loading = false
      }
    },
    /**
     * 登录成功后通用处理
     * @param {Array} tokens 登录成功返回的 token 数组
     * @param {boolean} needRegister 是否需要角色注册
     */
    finishLogin(tokens, needRegister = false) {
      const localStorage = window.localStorage
      setCookie(tokens)
      localStorage.setItem('username', this.username)
      if (needRegister) {
        this.showRoleApply = true
        this.formValidate.userName = this.username
      } else {
        this.$router.push('/homepage')
      }
      this.resetMfaState()
      this.totpCode = ''
    },
    /**
     * 重置 MFA 相关状态
     */
    resetMfaState() {
      this.showTotpVerify = false
      this.qrCodeUrl = ''
      this.needMfaCode = false
      this.tempToken = ''
    },
    /**
     * 取消 TOTP 验证
     */
    handleCancelTotp() {
      this.showTotpVerify = false
      this.totpCode = ''
      this.qrCodeUrl = ''
      this.encryptedPassword = ''
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
  watch: {
    /**
     * 监控用户名变更，清空 MFA 相关状态，避免跨用户残留验证码
     */
    username() {
      this.resetMfaState()
      this.totpCode = ''
      this.totpVerifying = false
      this.encryptedPassword = ''
    }
  },
  mounted() {
    // this.clearData()
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
  filter: blur(3px);
  z-index: 0;
}

.header-login {
  position: absolute;
  top: calc(50% - 35px);
  left: calc(50% - 325px);
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
  width: 280px;
  padding: 10px;
  z-index: 2;
  text-align: left;
}

/* TOTP 验证弹窗样式 */
.wc-login-totp-modal {
  padding: 10px;
}

.wc-login-totp-desc {
  margin-bottom: 24px;
  line-height: 1.6;
  color: #515a6e;
  font-size: 14px;
}

.wc-login-totp-desc a {
  color: #2d8cf0;
  text-decoration: none;
}

.wc-login-totp-desc a:hover {
  text-decoration: underline;
}

.wc-login-totp-section-title {
  margin: 24px 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #17233d;
}

.wc-login-totp-section-desc {
  margin-bottom: 16px;
  line-height: 1.6;
  color: #515a6e;
  font-size: 14px;
}

.wc-login-totp-section-desc a {
  color: #2d8cf0;
  text-decoration: none;
}

.wc-login-totp-section-desc a:hover {
  text-decoration: underline;
}

.wc-login-totp-qr-section {
  margin-bottom: 24px;
}

.wc-login-totp-qr-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0;
  padding: 16px;
  background-color: #f8f8f9;
  border-radius: 4px;
}

.wc-login-totp-qr-code {
  display: block;
}

.wc-login-totp-setup-key-hint {
  margin-top: 16px;
  margin-bottom: 16px;
  line-height: 1.6;
  color: #515a6e;
  font-size: 14px;
}

.wc-login-totp-setup-key-hint a {
  color: #2d8cf0;
  text-decoration: none;
}

.wc-login-totp-setup-key-hint a:hover {
  text-decoration: underline;
}

.wc-login-totp-setup-key {
  margin-top: 16px;
}

.wc-login-totp-verify-section {
  margin-top: 24px;
}

.wc-login-totp-code-input {
  margin-top: 8px;
}

.wc-login-totp-code-input >>> input {
  text-align: center;
  font-size: 18px;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
}

/* 已完成绑定用户内联 TOTP 样式 */
.wc-login-totp-inline {
  margin-top: 12px;
}

.wc-login-totp-inline-tip {
  margin: 0 0 6px 0;
  color: #d6d0d0;
  font-weight: 500;
  font-size: 13px;
}

.wc-login-totp-code-inline {
  width: 160px;
}

.wc-login-totp-inline-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wc-login-totp-verify-btn {
  width: 100px;
}
</style>
