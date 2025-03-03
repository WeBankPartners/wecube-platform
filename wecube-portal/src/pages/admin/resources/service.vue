<template>
  <div class="platform-resources-service">
    <WeTable
      :tableData="tableData"
      :tableOuterActions="outerActions"
      :tableInnerActions="null"
      :tableColumns="tableColumns"
      :pagination="pagination"
      ref="serviceTable"
      @actionFun="actionFun"
      @getSelectedRows="onSelectedRowsChange"
      @handleSubmit="handleSubmit"
      @sortHandler="sortHandler"
      @pageChange="pageChange"
      @pageSizeChange="pageSizeChange"
    />
    <BaseDrawer
      :title="operator === 'add' ? $t('full_word_add') : $t('edit')"
      :visible.sync="visible"
      :realWidth="900"
      :maskClosable="false"
    >
      <template slot="content">
        <Form :label-width="100" :model="form" :rules="rules" ref="form">
          <!--资源-->
          <FormItem :label="$t('resource')" prop="resourceServerId">
            <Select v-model="form.resourceServerId" @on-change="handleSelectResource" clearable>
              <Option v-for="item in resourceOptions" :key="item.id" :value="item.id">{{ item.name }}</Option>
            </Select>
          </FormItem>
          <!--类型-->
          <FormItem :label="$t('table_type')" prop="type">
            <Select v-model="form.type" disabled clearable>
              <Option v-for="(item, index) in typeOptions" :key="index" :value="item.itemType">{{
                item.itemType
              }}</Option>
            </Select>
          </FormItem>
          <!--名称-->
          <FormItem :label="$t('name')" prop="name">
            <Input v-model.trim="form.name" :maxlength="100" show-word-limit clearable></Input>
          </FormItem>
          <!--描述-->
          <FormItem :label="$t('table_purpose')" prop="purpose">
            <Input type="textarea" v-model.trim="form.purpose" :maxlength="255" show-word-limit clearable></Input>
          </FormItem>
          <!--是否分配-->
          <FormItem :label="$t('table_is_allocated')" prop="isAllocated">
            <i-switch v-model="form.isAllocated" :true-value="true" :false-value="false" size="default" />
          </FormItem>
          <!--账号-->
          <FormItem :label="$t('be_account')" prop="username">
            <Input v-model.trim="form.username" autocomplete="off" :maxlength="100" clearable />
          </FormItem>
          <!--密码-->
          <FormItem :label="$t('password')" prop="password">
            <input type="text" style="display: none" />
            <input type="password" autocomplete="new-password" style="display: none" />
            <Input v-model.trim="form.password" type="password" autocomplete="off" password :maxlength="100" />
          </FormItem>
        </Form>
      </template>
      <template slot="footer">
        <Button type="default" @click="visible = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="submitAddData">{{ $t('save') }}</Button>
      </template>
    </BaseDrawer>
  </div>
</template>

<script>
import {
  getResourceItemStatus,
  getResourceItemType,
  retrieveItems,
  getInputParamsEncryptKey,
  retrieveServers,
  addResourceInstance,
  updateResourceInstance,
  deleteResourceInstance
} from '@/api/server.js'
import { outerActions } from '@/const/actions.js'
import CryptoJS from 'crypto-js'
import moment from 'moment'

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
  props: {
    servers: {
      type: Array,
      default: () => []
    }
  },
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
      form: {
        resourceServerId: '',
        type: '',
        name: '',
        purpose: '',
        isAllocated: true,
        username: '',
        password: ''
      },
      resourceOptions: [], // 资源下拉列表
      typeOptions: [], // 类型下拉列表
      visible: false,
      operator: 'add', // add新增，edit编辑
      outerActions,
      tableData: [],
      tableColumns: [
        {
          title: this.$t('table_id'),
          key: 'id',
          inputKey: 'id',
          searchSeqNo: 1,
          displaySeqNo: 1,
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
          title: this.$t('table_type'),
          key: 'type',
          inputKey: 'type',
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_type')
        },
        {
          title: this.$t('table_resource_server'),
          key: 'resourceServer',
          inputKey: 'resourceServerId',
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_resource_server')
        },
        {
          title: this.$t('table_is_allocated'),
          key: 'isAllocated',
          inputKey: 'isAllocated',
          searchSeqNo: 5,
          displaySeqNo: 5,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_isAllocated'),
          options: booleanOptions
        },
        {
          title: this.$t('table_purpose'),
          key: 'purpose',
          inputKey: 'purpose',
          searchSeqNo: 6,
          displaySeqNo: 6,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_purpose')
        },
        {
          title: this.$t('table_status'),
          key: 'status',
          inputKey: 'status',
          searchSeqNo: 7,
          displaySeqNo: 7,
          component: 'WeSelect',
          inputType: 'select',
          placeholder: this.$t('table_status')
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdDate',
          inputKey: 'createdDate',
          searchSeqNo: 8,
          displaySeqNo: 8,
          component: 'DatePicker',
          type: 'datetimerange',
          inputType: 'date',
          placeholder: this.$t('table_created_date')
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedDate',
          inputKey: 'updatedDate',
          searchSeqNo: 9,
          displaySeqNo: 9,
          component: 'DatePicker',
          type: 'datetimerange',
          inputType: 'date',
          placeholder: this.$t('table_updated_date')
        },
        {
          title: this.$t('table_port'),
          key: 'port',
          inputKey: 'port',
          searchSeqNo: 10,
          displaySeqNo: 10,
          component: 'Input',
          inputType: 'text',
          placeholder: this.$t('table_port')
        }
      ],
      rules: {
        resourceServerId: [
          {
            required: true,
            message: this.$t('please_input') + this.$t('resource'),
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: this.$t('please_choose') + this.$t('table_type'),
            trigger: 'blur'
          }
        ],
        name: [
          {
            required: true,
            message: this.$t('please_input') + this.$t('name'),
            trigger: 'blur'
          }
        ],
        purpose: [
          {
            required: true,
            message: this.$t('please_input') + this.$t('table_purpose'),
            trigger: 'blur'
          }
        ],
        username: [
          {
            required: true,
            message: this.$t('please_input') + this.$t('be_account'),
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            message: this.$t('please_input') + this.$t('password'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  watch: {
    // 资源服务器赋值
    servers: {
      handler(val) {
        let statusIndex
        this.tableColumns.find((_, i) => {
          if (_.key === 'resourceServer') {
            statusIndex = i
          }
        })
        val && this.$set(this.tableColumns[statusIndex], 'options', val)
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {
    this.outerActions = this.outerActions.filter(i => ['add', 'edit', 'delete', 'cancel'].includes(i.actionType))
    this.getResourceItemStatus()
    this.getResourceItemType()
    this.getResourceOptions()
    this.queryData()
  },
  methods: {
    onSelectedRowsChange(rows) {
      if (rows.length > 0) {
        this.outerActions.forEach(_ => {
          _.props.disabled = _.actionType === 'add'
        })
        if (rows.length > 1) {
          this.outerActions.forEach(_ => {
            _.props.disabled = _.actionType === 'edit' || _.actionType === 'add'
          })
        }
      } else {
        this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'cancel')
        })
      }
      this.seletedRows = rows
    },
    // 获取资源下拉列表
    async getResourceOptions() {
      const payload = {
        filters: [],
        pageable: {
          pageSize: 1000,
          startIndex: 0
        },
        paging: true
      }
      payload.filters.push({
        name: 'type',
        operator: 'eq',
        value: 'mysql'
      })
      const { status, data } = await retrieveServers(payload)
      if (status === 'OK') {
        this.resourceOptions = data.contents || []
      }
    },
    // 新增数据时选择资源自动带出类型
    handleSelectResource(val) {
      if (val) {
        const item = this.resourceOptions.find(_ => _.id === val)
        this.typeOptions.forEach(_ => {
          if (_.resourceType === item.type) {
            this.form.type = _.itemType
          }
        })
      } else {
        this.form.type = ''
      }
    },
    async queryData() {
      this.payload.pageable.pageSize = this.pagination.pageSize
      this.payload.pageable.startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const { status, data } = await retrieveItems(this.payload)
      if (status === 'OK') {
        this.tableData = data.contents.map(_ => {
          _.isAllocated = _.isAllocated ? 'true' : 'false'
          _.createdDate = moment(_.createdDate).format('YYYY-MM-DD hh:mm:ss')
          _.updatedDate = moment(_.updatedDate).format('YYYY-MM-DD hh:mm:ss')
          return _
        })
        this.pagination.total = data.pageInfo.totalRows
      }
    },
    handleSubmit(data) {
      this.payload.filters = data.map(_ => {
        if (_.name === 'createdDate' || _.name === 'updatedDate') {
          _.value = +new Date(_.value)
        }
        if (_.name === 'isAllocated') {
          _.value = _.value === 'true'
        }
        return _
      })
      this.pagination.currentPage = 1
      this.queryData()
    },
    actionFun(type, data) {
      switch (type) {
        case 'add':
          this.addHandler()
          break
        case 'edit':
          this.editHandler(data)
          break
        case 'delete':
          this.deleteHandler(data)
          break
        case 'cancel':
          this.cancelHandler()
          break
        default:
          break
      }
    },
    addHandler() {
      this.form = {
        resourceServerId: '',
        type: '',
        name: '',
        purpose: '',
        isAllocated: true,
        username: '',
        password: ''
      }
      this.visible = true
      this.operator = 'add'
    },
    submitAddData() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          const { data } = await getInputParamsEncryptKey()
          const key = CryptoJS.enc.Utf8.parse(data)
          const config = {
            iv: CryptoJS.enc.Utf8.parse(Math.trunc(new Date() / 100000) * 100000000),
            mode: CryptoJS.mode.CBC
          }
          this.form.password = CryptoJS.AES.encrypt(this.form.password, key, config).toString()
          const params = JSON.parse(JSON.stringify([this.form]))
          const method = this.operator === 'add' ? addResourceInstance : updateResourceInstance
          const { status } = await method(params)
          if (status === 'OK') {
            this.visible = false
            this.$Message.success(this.$t('Create Success'))
            this.queryData()
          }
        }
      })
    },
    editHandler(row) {
      this.visible = true
      this.operator = 'edit'
      let {
        id, resourceServerId, type, name, purpose, isAllocated, username, password
      } = row[0]
      isAllocated = isAllocated === 'true' ? true : false
      this.form = Object.assign({}, this.form, {
        id,
        resourceServerId,
        type,
        name,
        purpose,
        isAllocated,
        username,
        password
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
          const { status, message } = await deleteResourceInstance(payload)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Delete Success',
              desc: message
            })
            this.outerActions.forEach(_ => {
              _.props.disabled = _.actionType === 'edit' || _.actionType === 'delete'
            })
            this.queryData()
          }
        },
        onCancel: () => {}
      })
    },
    cancelHandler() {
      this.$refs.serviceTable.setAllRowsUneditable()
      this.$refs.serviceTable.setCheckoutStatus()
      this.outerActions
        && this.outerActions.forEach(_ => {
          _.props.disabled = !(_.actionType === 'add' || _.actionType === 'cancel')
        })
    },
    sortHandler(data) {
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
    pageChange(current) {
      this.pagination.currentPage = current
      this.queryData()
    },
    pageSizeChange(size) {
      this.pagination.currentPage = 1
      this.pagination.pageSize = size
      this.queryData()
    },
    async getResourceItemStatus() {
      const { status, data } = await getResourceItemStatus({})
      if (status === 'OK') {
        this.setOptions(data, 'status')
      }
    },
    async getResourceItemType() {
      const { status, data } = await getResourceItemType({})
      if (status === 'OK') {
        const options = data && data.map(item => item.itemType)
        this.setOptions(options, 'type')
        this.typeOptions = data || []
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
    }
  }
}
</script>
