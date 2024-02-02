<!--批量执行-模板新增-->
<template>
  <div class="batch-execution-template-create">
    <BaseForm ref="form" :from="from" :type="type" :data="detailData" />
    <div v-if="type !== 'view'" class="footer-button">
      <Button type="primary" @click="saveExcute">预执行</Button>
      <Button type="primary" :disabled="templateDisabled" @click="getAuth" style="margin-left: 10px">保存模板</Button>
    </div>
    <!--权限弹窗-->
    <AuthDialog ref="authDialog" @sendAuth="saveTemplate" />
  </div>
</template>

<script>
import BaseForm from './base-form.vue'
import AuthDialog from '../../components/auth.vue'
import { saveBatchExecute, saveBatchExecuteTemplate, getBatchExecuteTemplateDetail } from '@/api/server.js'
export default {
  components: {
    BaseForm,
    AuthDialog
  },
  data () {
    return {
      from: 'template', // 模板，执行
      id: this.$route.query.id || '', // 模板id
      type: this.$route.query.type || 'add', // 新增，编辑，查看
      detailData: {},
      templateDisabled: true
    }
  },
  mounted () {
    if (this.id) {
      this.getTemplateDetail()
    }
  },
  methods: {
    // 获取批量执行模板详情
    async getTemplateDetail () {
      const { status, data } = await getBatchExecuteTemplateDetail(this.id)
      if (status === 'OK') {
        this.detailData = { ...data, templateData: data }
      }
    },
    // 执行操作
    async saveExcute () {
      if (!this.validRequired()) return
      const {
        name,
        currentPackageName,
        currentEntityName,
        dataModelExpression,
        pluginId,
        pluginOptions,
        pluginInputParams,
        resultTableParams,
        primatKeyAttrList,
        primatKeyAttr,
        seletedRows,
        searchParameters,
        userTableColumns,
        pluginOutputParams
      } = this.$refs.form
      // 缓存前端数据，页面回显使用
      const frontData = {
        userTableColumns,
        seletedRows,
        pluginInputParams,
        pluginOutputParams,
        resultTableParams
      }
      // 查询结果主键
      let currentEntity = primatKeyAttrList.find(item => {
        return item.name === primatKeyAttr
      })
      const resourceDatas = seletedRows.map(item => {
        return {
          id: item.id,
          businessKeyValue: item[primatKeyAttr]
        }
      })
      // 当前插件
      const plugin = pluginOptions.find(item => {
        return item.serviceName === pluginId
      })
      // 插件入参
      const inputParameterDefinitions = pluginInputParams.map(p => {
        const inputParameterValue =
          p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        }
      })
      const outputParameterDefinitions = pluginOutputParams.filter(i => {
        let flag = false
        resultTableParams.forEach(j => {
          if (i.name === j) {
            flag = true
          }
        })
        return flag
      })
      const params = {
        isDangerousBlock: true, // 是否开启高危检测
        batchExecutionTemplateId: '',
        batchExecutionTemplateName: name,
        name: name + 'test', // 批量名用模板名拼上test
        packageName: currentPackageName,
        entityName: currentEntityName,
        dataModelExpression: dataModelExpression,
        primatKeyAttr: primatKeyAttr,
        searchParameters: searchParameters,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        outputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas,
        sourceData: JSON.stringify(frontData)
      }
      const { status, data } = await saveBatchExecute(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        if (data.batchExecId) {
          this.showResult = true
          this.templateDisabled = false
          this.$refs.form.getExecuteResult(data.batchExecId)
        }
      }
    },
    // 获取属主&使用角色
    getAuth () {
      if (this.validRequired()) {
        this.$refs.authDialog.startAuth([], [])
      }
    },
    // 保存模板
    async saveTemplate (mgmtRole, useRole) {
      const {
        name,
        currentPackageName,
        currentEntityName,
        dataModelExpression,
        pluginId,
        pluginOptions,
        pluginInputParams,
        resultTableParams,
        primatKeyAttrList,
        primatKeyAttr,
        seletedRows,
        searchParameters,
        userTableColumns,
        pluginOutputParams
      } = this.$refs.form
      // 缓存前端数据，页面回显使用
      const frontData = {
        userTableColumns,
        seletedRows,
        pluginInputParams,
        pluginOutputParams,
        resultTableParams
      }
      // 查询结果主键
      let currentEntity = primatKeyAttrList.find(item => {
        return item.name === primatKeyAttr
      })
      const resourceDatas = seletedRows.map(item => {
        return {
          id: item.id,
          businessKeyValue: item[primatKeyAttr]
        }
      })
      // 当前插件
      const plugin = pluginOptions.find(item => {
        return item.serviceName === pluginId
      })
      // 插件入参
      const inputParameterDefinitions = pluginInputParams.map(p => {
        const inputParameterValue =
          p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        }
      })
      const outputParameterDefinitions = pluginOutputParams.filter(i => {
        let flag = false
        resultTableParams.forEach(j => {
          if (i.name === j) {
            flag = true
          }
        })
        return flag
      })
      const configData = {
        packageName: currentPackageName,
        entityName: currentEntityName,
        dataModelExpression: dataModelExpression,
        primatKeyAttr: primatKeyAttr,
        searchParameters: searchParameters,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        outputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas
      }
      const params = {
        id: '',
        name: name,
        operateObject: dataModelExpression,
        pluginService: plugin.serviceDisplayName || '',
        isDangerousBlock: true, // 是否开启高危检测
        configData: configData,
        permissionToRole: {
          MGMT: mgmtRole,
          USE: useRole
        },
        sourceData: JSON.stringify(frontData)
      }
      const { status } = await saveBatchExecuteTemplate(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.$eventBusP.$emit('change-menu', 'templateList')
      }
    },
    validRequired () {
      const { name, dataModelExpression, pluginId, pluginInputParams, primatKeyAttr, userTableColumns, seletedRows } =
        this.$refs.form
      if (!name) {
        this.$Message.warning('模板名称必填')
        return false
      }
      if (!dataModelExpression) {
        this.$Message.warning('查询路径必填')
        return false
      }
      if (!primatKeyAttr) {
        this.$Message.warning('查询结果主键必填')
        return false
      }
      if (userTableColumns && userTableColumns.length === 0) {
        this.$Message.warning('查询结果展示列必填')
        return false
      }
      if (seletedRows && seletedRows.length === 0) {
        this.$Message.warning('操作实例必填')
        return false
      }
      if (!pluginId) {
        this.$Message.warning('插件服务必填')
        return false
      }
      if (pluginInputParams && pluginInputParams.length === 0) {
        this.$Message.warning('插件服务入参必填')
        return false
      }
      return true
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execution-template-create {
  width: 100%;
  .footer-button {
    width: 920px;
    display: flex;
    justify-content: center;
    padding-bottom: 30px;
  }
}
</style>
