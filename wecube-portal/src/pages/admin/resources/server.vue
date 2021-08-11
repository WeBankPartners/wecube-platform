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
  deleteServers
} from '@/api/server.js'
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

export default {
  data () {
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
          title: this.$t('table_login_username'),
          key: 'loginUsername',
          inputKey: 'loginUsername',
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_login_username')
        },
        {
          title: this.$t('table_login_password'),
          key: 'loginPassword',
          inputKey: 'loginPassword',
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_login_password')
        },
        {
          title: this.$t('table_port'),
          key: 'port',
          inputKey: 'port',
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_port')
        },
        {
          title: this.$t('table_purpose'),
          key: 'purpose',
          inputKey: 'purpose',
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_purpose')
        },
        {
          title: this.$t('table_status'),
          key: 'status',
          inputKey: 'status',
          searchSeqNo: 9,
          displaySeqNo: 9,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_status')
        },
        {
          title: this.$t('table_type'),
          key: 'type',
          inputKey: 'type',
          searchSeqNo: 10,
          displaySeqNo: 10,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_type')
        }
      ]
    }
  },
  methods: {
    async queryData () {
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
    async getResourceServerStatus () {
      const { status, data } = await getResourceServerStatus()
      if (status === 'OK') {
        this.setOptions(data, 'status')
      }
    },
    async getResourceServerType () {
      const { status, data } = await getResourceServerType()
      if (status === 'OK') {
        this.setOptions(data, 'type')
      }
    },
    setOptions (data, column) {
      let statusIndex
      this.tableColumns.find((_, i) => {
        if (_.key === column) {
          statusIndex = i
        }
      })
      const options = data.map(_ => {
        return {
          label: _,
          value: _,
          key: _
        }
      })
      this.$set(this.tableColumns[statusIndex], 'options', options)
    },
    handleSubmit (data) {
      this.payload.filters = data
      this.queryData()
    },
    sortHandler (data) {
      if (data.order === 'normal') {
        delete this.payload.sorting
      } else {
        this.payload.sorting = {
          asc: data.order === 'asc',
          field: data.key
        }
      }
      this.queryData()
    },
    pageChange (current) {
      this.pagination.currentPage = current
      this.queryData()
    },
    pageSizeChange (size) {
      this.pagination.pageSize = size
      this.queryData()
    },
    async productSerial (data) {
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
    actionFun (type, data) {
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
    onSelectedRowsChange (rows, checkoutBoxdisable) {
      if (rows.length > 0) {
        this.outerActions.forEach(_ => {
          _.props.disabled = _.actionType === 'add'
        })
      } else {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
      }
      this.seletedRows = rows
    },
    addHandler () {
      let emptyRowData = {}
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
    async saveHandler (data) {
      const setBtnsStatus = () => {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
        this.$refs.table.setAllRowsUneditable()
        this.$nextTick(() => {
          /* to get iview original data to set _ischecked flag */
          let objData = this.$refs.table.$refs.table.$refs.tbody.objData
          for (let obj in objData) {
            objData[obj]._isChecked = false
            objData[obj]._isDisabled = false
          }
        })
      }
      let d = JSON.parse(JSON.stringify(data))
      let addObj = d.find(_ => _.isNewAddedRow)
      let editAry = d.filter(_ => !_.isNewAddedRow)
      if (addObj) {
        let payload = {
          host: addObj.host,
          isAllocated: addObj.isAllocated === 'true',
          loginPassword: addObj.loginPassword,
          loginUsername: addObj.loginUsername,
          name: addObj.name,
          port: addObj.port,
          purpose: addObj.purpose,
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
        let payload = editAry.map(_ => {
          return {
            id: _.id,
            host: _.host,
            isAllocated: _.isAllocated === 'true',
            loginPassword: _.loginPassword,
            loginUsername: _.loginUsername,
            name: _.name,
            port: _.port,
            purpose: _.purpose,
            status: _.status,
            type: _.type
          }
        })
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
    editHandler () {
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
    deleteHandler (deleteData) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          const payload = deleteData.map(_ => {
            return {
              id: _.id
            }
          })
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
    cancelHandler () {
      const index = this.tableData.findIndex(item => item.isNewAddedRow === true)
      if (index > -1) {
        this.tableData.splice(index, 1)
      }
      this.$refs.table.setAllRowsUneditable()
      this.$refs.table.setCheckoutStatus()
      this.outerActions &&
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'export' || _.actionType === 'cancel')
        })
    },
    async exportHandler () {
      const { status, data } = await retrieveServers({})
      if (status === 'OK') {
        this.$refs.table.export({
          filename: this.$t('host'),
          data: formatData(data.contents)
        })
      }
    }
  },
  mounted () {
    this.getResourceServerStatus()
    this.getResourceServerType()
    this.queryData()
  }
}
</script>
