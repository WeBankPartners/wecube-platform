<!--批量执行-模板新增-->
<template>
  <div class="batch-execution-template-create">
    <BaseForm ref="form" :from="from" :type="type" :data="detailData" @back="handleBack" />
    <div v-if="type !== 'view'" class="footer-button">
      <Button type="success" @click="saveExcute">预执行</Button>
      <Button type="default" @click="getAuth('draft')" style="margin-left: 10px">保存草稿</Button>
      <Button type="primary" :disabled="templateDisabled" @click="getAuth('published')" style="margin-left: 10px"
        >发布模板</Button
      >
    </div>
    <!--权限弹窗-->
    <AuthDialog ref="authDialog" :useRolesRequired="true" @sendAuth="saveTemplate" />
    <!--高危检测弹框-->
    <DangerousModal :visible.sync="confirmModal.isShowConfirmModal" :data="confirmModal"></DangerousModal>
  </div>
</template>

<script>
import BaseForm from './base-form.vue'
import AuthDialog from '../../components/auth.vue'
import DangerousModal from './components/dangerous-modal.vue'
import { debounce } from '@/const/util'
import { saveBatchExecute, saveBatchExecuteTemplate, getBatchExecuteTemplateDetail } from '@/api/server.js'
export default {
  components: {
    BaseForm,
    AuthDialog,
    DangerousModal
  },
  data () {
    return {
      from: 'template', // 模板，执行
      id: this.$route.query.id || '', // 模板id
      type: this.$route.query.type || 'add', // 新增，编辑，查看
      detailData: {},
      templateDisabled: true,
      templateStatus: '', // draft草稿，published正常发布
      saveTemplateId: '', // 用于提交保存的模板ID
      confirmModal: {
        continueToken: '',
        message: '',
        params: {},
        isShowConfirmModal: false
      }
    }
  },
  mounted () {
    if (this.id) {
      this.getTemplateDetail()
    }
  },
  methods: {
    handleBack () {
      this.$eventBusP.$emit('change-menu', 'templateList')
    },
    // 获取模板详情
    async getTemplateDetail () {
      const { status, data } = await getBatchExecuteTemplateDetail(this.id)
      if (status === 'OK') {
        this.detailData = { ...data, templateData: data }
        if (this.type === 'edit') {
          this.saveTemplateId = data.id
        }
        if (this.type === 'copy') {
          this.detailData.name = `${this.detailData.name} (1)`
        }
      }
    },
    // 预执行操作
    saveExcute: debounce(async function () {
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
        pluginOutputParams,
        isDangerousBlock
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
        isDangerousBlock: isDangerousBlock, // 是否开启高危检测
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
      this.$Spin.show()
      const { status, data } = await saveBatchExecute(`/platform/v1/batch-execution/job/run`, params)
      this.$Spin.hide()
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        if (data.batchExecId) {
          this.$refs.form.showResult = true
          this.templateDisabled = false
          this.$nextTick(() => {
            this.$refs.form.getExecuteResult(data.batchExecId)
          })
        }
      } else if (status === 'CONFIRM') {
        // 高危检测命中，则弹窗让用户手动确认是否继续执行，若继续，则带id和continueToken再执行一次
        if (data.dangerousCheckResult) {
          params.batchExecId = data.batchExecId
          this.confirmModal.continueToken = data.batchExecId
          this.confirmModal.message = data.dangerousCheckResult.text
          this.confirmModal.params = params
          this.confirmModal.isShowConfirmModal = true
        }
      }
    }, 100),
    // 获取属主&使用角色
    getAuth (status) {
      this.templateStatus = status
      if (this.validRequired()) {
        let mgmtRole = []
        let useRole = []
        if (this.type === 'edit') {
          mgmtRole = this.detailData.permissionToRole.MGMT || []
          useRole = this.detailData.permissionToRole.USE || []
        }
        this.$refs.authDialog.startAuth(mgmtRole, useRole)
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
        pluginOutputParams,
        isDangerousBlock
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
        id: this.saveTemplateId || '',
        publishStatus: this.templateStatus,
        name: name,
        operateObject: dataModelExpression,
        pluginService: plugin.serviceDisplayName || '',
        isDangerousBlock: isDangerousBlock, // 是否开启高危检测
        configData: configData,
        permissionToRole: {
          MGMT: mgmtRole,
          USE: useRole
        },
        sourceData: JSON.stringify(frontData)
      }
      this.$Spin.show()
      const { status, data } = await saveBatchExecuteTemplate(params)
      this.$Spin.hide()
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.saveTemplateId = data.id
        this.$eventBusP.$emit('change-menu', 'templateList')
        this.$router.replace({
          name: this.$route.name,
          query: {
            status: this.templateStatus
          }
        })
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
      const pluginInputParamsFlag = pluginInputParams.every(item => {
        if (item.required === 'Y' && item.mappingType === 'constant' && !item.bindValue) {
          return false
        } else {
          return true
        }
      })
      if ((pluginInputParams && pluginInputParams.length === 0) || !pluginInputParamsFlag) {
        this.$Message.warning('设置入参必填')
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
