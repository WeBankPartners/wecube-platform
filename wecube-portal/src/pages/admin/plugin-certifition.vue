<template>
  <div class=" ">
    <Form inline>
      <!-- <FormItem prop="user">
        <Input type="text" v-model="name" style="width:300px" :placeholder="$t('t_name')"> </Input>
      </FormItem> -->
      <FormItem>
        <Upload
          ref="uploadButton"
          show-upload-list
          accept=".welic"
          name="uploadFile"
          :on-success="onImportProcessDefinitionSuccess"
          :on-error="onImportProcessDefinitionError"
          action="platform/v1/plugin-certifications/import"
          :headers="headers"
        >
          <Button style="float: right;margin-right:4px" type="primary" @click="getHeaders">{{
            $t('import_flow')
          }}</Button>
        </Upload>
      </FormItem>
    </Form>
    <Table border :columns="tableColumns" :data="tableData"></Table>
  </div>
</template>

<script>
import axios from 'axios'
import { setCookie, getCookie } from '@/pages//util/cookie'
import { getCertification, deleteCertification, exportCertification } from '@/api/server'
export default {
  name: '',
  data () {
    return {
      headers: {},
      tableColumns: [
        {
          title: this.$t('plugin'),
          key: 'plugin'
        },
        {
          title: this.$t('signature'),
          key: 'signature'
        },
        {
          title: this.$t('lpk'),
          key: 'lpk'
        },
        {
          title: this.$t('encrypt_data'),
          key: 'encryptData'
        },
        {
          title: this.$t('description'),
          key: 'description'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 150,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h(
                'Button',
                {
                  props: {
                    type: 'primary',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      this.export(params.row)
                    }
                  }
                },
                this.$t('export')
              ),
              h(
                'Button',
                {
                  props: {
                    type: 'error',
                    size: 'small'
                  },
                  on: {
                    click: () => {
                      this.remove(params.row)
                    }
                  }
                },
                this.$t('delete')
              )
            ])
          }
        }
      ],
      tableData: []
    }
  },
  mounted () {
    this.getTableData()
  },
  methods: {
    async getTableData () {
      const { status, data } = await getCertification()
      if (status === 'OK') {
        this.tableData = data
      }
    },
    export (row) {
      exportCertification(row.id)
    },
    remove (row) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        content: name,
        onOk: async () => {
          let { status, message } = await deleteCertification(row.id)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Success',
              desc: message
            })
            this.getTableData()
          }
        },
        onCancel: () => {}
      })
    },
    getHeaders () {
      this.isShowUploadList = true
      let refreshRequest = null
      const currentTime = new Date().getTime()
      const accessToken = getCookie('accessToken')
      if (accessToken) {
        const expiration = getCookie('accessTokenExpirationTime') * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + getCookie('refreshToken')
            }
          })
          console.log(1)
          refreshRequest.then(
            res => {
              setCookie(res.data.data)
              this.setUploadActionHeader()
              this.$refs.uploadButton.handleClick()
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
            }
          )
        } else {
          this.setUploadActionHeader()
          console.log(2)
          this.$refs.uploadButton.handleClick()
        }
      } else {
        window.location.href = window.location.origin + window.location.pathname + '#/login'
      }
    },
    setUploadActionHeader () {
      this.headers = {
        Authorization: 'Bearer ' + getCookie('accessToken')
      }
    },
    onImportProcessDefinitionSuccess (response) {
      if (response.status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: response.message || ''
        })
        this.getTableData()
      } else {
        this.$Notice.warning({
          title: 'Warning',
          desc: response.message || ''
        })
      }
    },
    onImportProcessDefinitionError (file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    }
  }
}
</script>

<style scoped lang="scss"></style>
