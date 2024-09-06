<template>
  <WeTable
    :tableData="tableData"
    :tableOuterActions="outerActions"
    :tableInnerActions="innerActions"
    :tableColumns="tableColumns"
    :pagination="pagination"
    @actionFun="actionFun"
    @handleSubmit="handleSubmit"
    @sortHandler="sortHandler"
    @getSelectedRows="onSelectedRowsChange"
    @pageChange="pageChange"
    @pageSizeChange="pageSizeChange"
    ref="table"
  />
</template>

<script>
import {
  getResourceServerStatus,
  getResourceServerType,
  retrieveServers,
  createServers,
  updateServers,
  productSerial,
  deleteServers,
  getInputParamsEncryptKey
} from '@/api/server.js'
import CryptoJS from 'crypto-js'
import { outerActions } from '@/const/actions.js'
import { formatData } from '../../util/format.js'

const booleanOptions = [
  {
    label: 'true',
    value: 'true',
    key: 'true'
  },
  {
    label: 'false',
    value: 'false',
    key: 'false'
  }
]
const loginMethodOptions = [
  {
    label: 'PASSWD',
    value: 'PASSWD',
    key: 'PASSWD'
  },
  {
    label: 'KEY',
    value: 'KEY',
    key: 'KEY'
  }
]

export default {
  data() {
    return {
      payload: {
        filters: [],
        pageable: {
          pageSize: 10,
          startIndex: 0
        },
        paging: true
      },
      pagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      outerActions,
      innerActions: [
        {
          label: this.$t('product_serial'),
          actionType: 'product_serial',
          operationMultiple: 'yes',
          operation_en: 'product_serial',
          props: {
            type: 'primary',
            disabled: false,
            size: 'small'
          }
        }
      ],
      tableData: [],
      tableColumns: [
        {
          title: this.$t('table_id'),
          key: 'id',
          inputKey: 'id',
          searchSeqNo: 1,
          displaySeqNo: 1,
          disEditor: true,
          disAdded: true,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_id')
        },
        {
          title: this.$t('table_name'),
          key: 'name',
          inputKey: 'name',
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_name')
        },
        {
          title: this.$t('ip_address'),
          key: 'host',
          inputKey: 'host',
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('ip_address')
        },
        {
          title: this.$t('table_is_allocated'),
          key: 'isAllocated',
          inputKey: 'isAllocated',
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_isAllocated'),
          options: booleanOptions
        },
        {
          title: this.$t('login_method'),
          key: 'loginMode',
          inputKey: 'loginMode',
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('login_method'),
          options: loginMethodOptions
        },
        {
          title: this.$t('table_login_username'),
          key: 'loginUsername',
          inputKey: 'loginUsername',
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_login_username')
        },
        {
          title: this.$t('table_login_password'),
          key: 'loginPassword',
          inputKey: 'loginPassword',
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_login_password')
        },
        {
          title: this.$t('table_port'),
          key: 'port',
          inputKey: 'port',
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_port')
        },
        {
          title: this.$t('table_purpose'),
          key: 'purpose',
          inputKey: 'purpose',
          searchSeqNo: 9,
          displaySeqNo: 9,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_purpose')
        },
        {
          title: this.$t('table_status'),
          key: 'status',
          inputKey: 'status',
          searchSeqNo: 10,
          displaySeqNo: 10,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_status')
        },
        {
          title: this.$t('table_type'),
          key: 'type',
          inputKey: 'type',
          searchSeqNo: 11,
          displaySeqNo: 11,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_type')
        }
      ],
      encryptKey: ''
    }
  },
  methods: {
    async queryData() {
      this.payload.pageable.pageSize = this.pagination.pageSize
      this.payload.pageable.startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const { status, data } = await retrieveServers(this.payload)
      if (status === 'OK') {
        this.tableData = data.contents.map(_ => {
          _.isAllocated = _.isAllocated ? 'true' : 'false'
          return _
        })
        this.pagination.total = data.pageInfo.totalRows
      }
    },
    async getResourceServerStatus() {
      const { status, data } = await getResourceServerStatus()
      if (status === 'OK') {
        this.setOptions(data, 'status')
      }
    },
    async getResourceServerType() {
      const { status, data } = await getResourceServerType()
      if (status === 'OK') {
        this.setOptions(data, 'type')
      }
    },
    setOptions(data, column) {
      let statusIndex
      this.tableColumns.find((_, i) => {
        if (_.key === column) {
          statusIndex = i
        }
      })
      const options = data.map(_ => ({
        label: _,
        value: _,
        key: _
      }))
      this.$set(this.tableColumns[statusIndex], 'options', options)
    },
    handleSubmit(data) {
      this.payload.filters = data
      this.queryData()
    },
    sortHandler(data) {
      if (data.order === 'normal') {
        delete this.payload.sorting
      }
      else {
        this.payload.sorting = {
          asc: data.order === 'asc',
          field: data.key
        }
      }
      this.queryData()
    },
    pageChange(current) {
      this.pagination.currentPage = current
      this.queryData()
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size
      this.queryData()
    },
    async productSerial(data) {
      if (data.type !== 'docker') {
        this.$Notice.warning({
          title: 'Warning',
          desc: this.$t('product_serial_tip')
        })
        return
      }
      const res = await productSerial(data.id)
      if (res.status === 'OK') {
        this.$Modal.info({
          title: this.$t('product_serial'),
          content: (res.data && res.data.productSerial) || 'null'
        })
      }
    },
    actionFun(type, data) {
      switch (type) {
        case 'product_serial':
          this.productSerial(data)
          break
        case 'add':
          this.addHandler()
          break
        case 'save':
          this.saveHandler(data)
          break
        case 'edit':
          this.editHandler()
          break
        case 'delete':
          this.deleteHandler(data)
          break
        case 'cancel':
          this.cancelHandler()
          break
        case 'export':
          this.exportHandler()
          break
        default:
          break
      }
    },
    onSelectedRowsChange(rows) {
      if (rows.length > 0) {
        this.outerActions.forEach(_ => {
          _.props.disabled = _.actionType === 'add'
        })
      }
      else {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
      }
      this.seletedRows = rows
    },
    addHandler() {
      const emptyRowData = {}
      this.tableColumns.forEach(_ => {
        emptyRowData[_.inputKey] = ''
      })
      emptyRowData.isRowEditable = true
      emptyRowData.isNewAddedRow = true
      emptyRowData.weTableRowId = new Date().getTime()
      this.tableData.unshift(emptyRowData)
      this.$nextTick(() => {
        this.$refs.table.pushNewAddedRowToSelections()
        this.$refs.table.setCheckoutStatus(true)
      })
      this.outerActions.forEach(_ => {
        _.props.disabled = _.actionType === 'add'
      })
    },
    async getInputParamsEncryptKey() {
      const { status, data } = await getInputParamsEncryptKey()
      if (status === 'OK') {
        this.encryptKey = data
      }
    },
    async saveHandler(data) {
      const setBtnsStatus = () => {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
        this.$refs.table.setAllRowsUneditable()
        this.$nextTick(() => {
          /* to get iview original data to set _ischecked flag */
          const objData = this.$refs.table.$refs.table.$refs.tbody.objData
          for (const obj in objData) {
            objData[obj]._isChecked = false
            objData[obj]._isDisabled = false
          }
        })
      }
      const d = JSON.parse(JSON.stringify(data))
      const addObj = d.find(_ => _.isNewAddedRow)
      const editAry = d.filter(_ => !_.isNewAddedRow)
      await this.getInputParamsEncryptKey()
      const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
      const config = {
        iv: CryptoJS.enc.Utf8.parse(Math.trunc(new Date() / 100000) * 100000000),
        mode: CryptoJS.mode.CBC
      }
      if (addObj) {
        const payload = {
          host: addObj.host,
          isAllocated: addObj.isAllocated === 'true',
          loginPassword: CryptoJS.AES.encrypt(addObj.loginPassword, key, config).toString(),
          loginUsername: addObj.loginUsername,
          name: addObj.name,
          port: addObj.port,
          purpose: addObj.purpose,
          loginMode: addObj.loginMode,
          status: addObj.status,
          type: addObj.type
        }
        const { status, message } = await createServers([payload])
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Add Success',
            desc: message
          })
          setBtnsStatus()
          this.queryData()
        }
      }
      if (editAry.length > 0) {
        const payload = editAry.map(_ => ({
          id: _.id,
          host: _.host,
          isAllocated: _.isAllocated === 'true',
          loginPassword: CryptoJS.AES.encrypt(_.loginPassword, key, config).toString(),
          loginUsername: _.loginUsername,
          name: _.name,
          port: _.port,
          purpose: _.purpose,
          loginMode: _.loginMode,
          status: _.status,
          type: _.type
        }))
        const { status, message } = await updateServers(payload)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Update Success',
            desc: message
          })
          setBtnsStatus()
          this.queryData()
        }
      }
    },
    editHandler() {
      this.$refs.table.swapRowEditable(true)
      this.outerActions.forEach(_ => {
        if (_.actionType === 'save') {
          _.props.disabled = false
        }
      })
      this.$nextTick(() => {
        this.$refs.table.setCheckoutStatus(true)
      })
    },
    deleteHandler(deleteData) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          const payload = deleteData.map(_ => ({
            id: _.id
          }))
          const { status, message } = await deleteServers(payload)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Delete Success',
              desc: message
            })
            this.outerActions.forEach(_ => {
              _.props.disabled = _.actionType === 'save' || _.actionType === 'edit' || _.actionType === 'delete'
            })
            this.queryData()
          }
        },
        onCancel: () => {}
      })
    },
    cancelHandler() {
      const index = this.tableData.findIndex(item => item.isNewAddedRow === true)
      if (index > -1) {
        this.tableData.splice(index, 1)
      }
      this.$refs.table.setAllRowsUneditable()
      this.$refs.table.setCheckoutStatus()
      this.outerActions
        && this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
    },
    async exportHandler() {
      const { status, data } = await retrieveServers({})
      if (status === 'OK') {
        this.$refs.table.export({
          filename: this.$t('host'),
          data: formatData(data.contents)
        })
      }
    }
  },
  mounted() {
    this.getResourceServerStatus()
    this.getResourceServerType()
    this.queryData()
  }
}
</script>
