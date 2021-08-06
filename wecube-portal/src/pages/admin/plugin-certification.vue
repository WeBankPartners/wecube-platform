<template>
  <div class=" ">
    <div>
      <Upload
        ref="uploadButton"
        show-upload-list
        accept=".welic"
        name="uploadFile"
        :on-success="onImportSuccess"
        :on-error="onImportError"
        action="platform/v1/plugin-certifications/import"
        :headers="headers"
      >
        <Button style="float: right;margin-right:4px" type="primary" @click="getHeaders">{{
          $t('import_flow')
        }}</Button>
      </Upload>
    </div>
    <Table border :columns="tableColumns" :data="tableData"></Table>
  </div>
</template>

<script>
import axios from 'axios'
import { setCookie, getCookie } from '@/pages//util/cookie'
import { getCertification, deleteCertification } from '@/api/server'
export default {
  name: '',
  data () {
    return {
      headers: {},
      tableColumns: [
        {
          title: this.$t('plugin'),
          width: 200,
          key: 'plugin'
        },
        {
          title: this.$t('description'),
          key: 'description',
          render: (h, params) => {
            return h('pre', {}, params.row.description)
          }
        },
        {
          title: this.$t('table_created_date'),
          width: 200,
          key: 'createdTime'
        },
        {
          title: this.$t('table_updated_date'),
          width: 200,
          key: 'updatedTime'
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
      const accessToken = getCookie('accessToken')
      axios({
        method: 'GET',
        url: `platform/v1/plugin-certifications/${row.id}/export`,
        headers: {
          Authorization: 'Bearer ' + accessToken
        },
        responseType: 'blob'
      })
        .then(response => {
          if (response.status < 400) {
            let content = response.data
            const contentDispositionHeader = response.headers['content-disposition']
            let filename = 'file'
            if (contentDispositionHeader) {
              filename = contentDispositionHeader
                .split(';')
                .find(x => ~x.indexOf('filename'))
                .split('=')[1]
            }
            if (filename === null || filename === undefined || filename === '') {
              filename = 'file'
            } else {
              filename = decodeURI(filename)
            }
            let blob = content
            if ('msSaveOrOpenBlob' in navigator) {
              window.navigator.msSaveOrOpenBlob(blob, filename)
            } else {
              if ('download' in document.createElement('a')) {
                // 非IE下载
                let elink = document.createElement('a')
                elink.download = filename
                elink.style.display = 'none'
                elink.href = URL.createObjectURL(blob)
                document.body.appendChild(elink)
                elink.click()
                URL.revokeObjectURL(elink.href) // 释放URL 对象
                document.body.removeChild(elink)
              } else {
                // IE10+下载
                navigator.msSaveOrOpenBlob(blob, filename)
              }
            }
          }
        })
        .catch(() => {
          this.$Message.warning('Error')
        })
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
          // this.$refs.uploadButton.handleClick()
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
    onImportSuccess (response) {
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
    onImportError (file) {
      this.$Notice.error({
        title: 'Error',
        desc: file.message || ''
      })
    }
  }
}
</script>

<style scoped lang="scss"></style>
