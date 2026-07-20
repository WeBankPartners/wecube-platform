<template>
  <div class="password-encrypt-tool">
    <div class="tool-panel">
      <div class="tool-title">{{ $t('config_password_tool') }}</div>
      <Form ref="form" :model="form" :rules="rules" :label-width="140">
        <FormItem :label="$t('password_1')" prop="password">
          <Input v-model="form.password" type="password" password />
        </FormItem>
        <FormItem :label="$t('password_2')" prop="confirmPassword">
          <Input v-model="form.confirmPassword" type="password" password />
        </FormItem>
        <FormItem>
          <Button type="primary" icon="md-lock" :loading="loading" @click="encryptPassword">
            {{ $t('encrypt_data') }}
          </Button>
        </FormItem>
        <FormItem v-if="ciphertext" :label="$t('ciphertext')">
          <Input v-model="ciphertext" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" readonly />
          <Button class="copy-btn" icon="md-copy" @click="copyCiphertext">{{ $t('copy') }}</Button>
        </FormItem>
      </Form>
    </div>
  </div>
</template>

<script>
import { encryptConfigPassword } from '@/api/server'

export default {
  data() {
    return {
      loading: false,
      ciphertext: '',
      form: {
        password: '',
        confirmPassword: ''
      },
      rules: {
        password: [
          {
            required: true,
            message: this.$t('password_cannot_empty'),
            trigger: 'blur'
          }
        ],
        confirmPassword: [
          {
            required: true,
            message: this.$t('password_cannot_empty'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    encryptPassword() {
      this.$refs.form.validate(async valid => {
        if (!valid) {
          return
        }
        if (this.form.password !== this.form.confirmPassword) {
          this.$Message.warning(this.$t('confirm_password_error'))
          return
        }
        this.loading = true
        try {
          const { status, data } = await encryptConfigPassword(this.form)
          if (status === 'OK') {
            this.ciphertext = data.ciphertext
          }
        } finally {
          this.loading = false
        }
      })
    },
    copyCiphertext() {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.ciphertext)
          .then(() => {
            this.$Message.success(this.$t('copy_success'))
          })
          .catch(() => {
            this.copyCiphertextWithTextarea()
          })
        return
      }
      this.copyCiphertextWithTextarea()
    },
    copyCiphertextWithTextarea() {
      const input = document.createElement('textarea')
      input.value = this.ciphertext
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$Message.success(this.$t('copy_success'))
    }
  }
}
</script>

<style lang="scss" scoped>
.password-encrypt-tool {
  padding: 16px;

  .tool-panel {
    max-width: 760px;
    padding: 20px 24px;
    background: #fff;
    border: 1px solid #e8eaec;
    border-radius: 4px;
  }

  .tool-title {
    margin-bottom: 18px;
    color: #17233d;
    font-size: 16px;
    font-weight: 600;
  }

  .copy-btn {
    margin-top: 8px;
  }
}
</style>
