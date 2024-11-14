<!--批量执行-模板新增-->
<template>
  <div class="batch-execution-template-create">
    <BaseForm ref="form" from="template" :type="type" :data="detailData" @back="handleBack" />
    <div v-if="type !== 'view'" class="footer-button">
      <!--预执行-->
      <Button type="success" @click="saveExcute">{{ $t('be_pre_execute') }}</Button>
      <!--保存草稿-->
      <Button type="default" @click="getAuth('draft')" style="margin-left: 10px">{{ $t('be_save_draft') }}</Button>
      <!--发布模板-->
      <Button type="primary" :disabled="templateDisabled" @click="getAuth('published')" style="margin-left: 10px">{{
        $t('be_publish_template')
      }}</Button>
    </div>
    <!--权限弹窗-->
    <AuthDialog ref="authDialog" :useRolesRequired="true" @sendAuth="saveTemplate" />
    <!--高危检测弹框-->
    <DangerousModal
      :visible.sync="confirmModal.isShowConfirmModal"
      :data="confirmModal"
      @success="showExecuteResult"
    ></DangerousModal>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'
import BaseForm from '../base-form.vue'
import AuthDialog from '@/pages/components/auth.vue'
import DangerousModal from '../components/dangerous-modal.vue'
import { debounce } from '@/const/util'
import {
  saveBatchExecute,
  saveBatchExecuteTemplate,
  getBatchExecuteTemplateDetail,
  getInputParamsEncryptKey
} from '@/api/server.js'
export default {
  components: {
    BaseForm,
    AuthDialog,
    DangerousModal
  },
  data() {
    return {
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
      },
      encryptKey: ''
    }
  },
  mounted() {
    if (this.id) {
      this.getTemplateDetail()
    }
  },
  methods: {
    handleBack() {
      this.$router.push({
        path: '/implementation/batch-execution/template-list',
        query: {
          status: this.type === 'edit' ? 'draft' : 'published'
        }
      })
    },
    async getInputParamsEncryptKey() {
      const { status, data } = await getInputParamsEncryptKey()
      if (status === 'OK') {
        this.encryptKey = data
      }
    },
    showExecuteResult(data) {
      this.$refs.form.showResult = true
      this.templateDisabled = false
      this.$nextTick(() => {
        this.$refs.form.getExecuteResult(data.batchExecId)
      })
    },
    // 获取属主&使用角色
    getAuth(status) {
      this.templateStatus = status
      if (this.validRequired()) {
        let mgmtRole = []
        let useRole = []
        if (this.detailData && this.detailData.permissionToRole) {
          mgmtRole = this.detailData.permissionToRole.MGMT || []
          useRole = this.detailData.permissionToRole.USE || []
        }
        this.$refs.authDialog.startAuth(mgmtRole, useRole)
      }
    },
    // 获取模板详情
    async getTemplateDetail() {
      const { status, data } = await getBatchExecuteTemplateDetail(this.id)
      if (status === 'OK') {
        this.detailData = {
          ...data,
          templateData: data
        }
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
      if (!this.validRequired()) {
        return
      }
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
      // 插件入参为敏感字段，需要加密
      const encryptFlag = pluginInputParams.some(i => i.sensitiveData === 'Y')
      if (encryptFlag) {
        await this.getInputParamsEncryptKey()
        pluginInputParams.forEach(item => {
          if (
            item.mappingType === 'constant'
            && item.sensitiveData === 'Y'
            && item.bindValue
            && !item.bindValue.startsWith('encrypt ')
          ) {
            const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
            const config = {
              iv: key,
              mode: CryptoJS.mode.CBC
            }
            item.bindValue = 'encrypt ' + CryptoJS.AES.encrypt(item.bindValue, key, config).toString()
          }
        })
      }
      // 缓存前端数据，页面回显使用
      const frontData = {
        userTableColumns,
        seletedRows,
        pluginInputParams,
        pluginOutputParams,
        resultTableParams
      }
      // 查询结果主键
      const currentEntity = primatKeyAttrList.find(item => item.name === primatKeyAttr)
      const resourceDatas = seletedRows.map(item => ({
        id: item.id,
        businessKeyValue: item[primatKeyAttr]
      }))
      // 当前插件
      const plugin = pluginOptions.find(item => item.serviceName === pluginId)
      // 插件入参
      const inputParameterDefinitions = pluginInputParams.map(p => {
        const inputParameterValue = p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        return {
          inputParameter: p,
          inputParameterValue
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
        isDangerousBlock, // 是否开启高危检测
        batchExecutionTemplateId: '',
        batchExecutionTemplateName: name,
        name: name + 'test', // 预执行名称用模板名拼上test
        packageName: currentPackageName,
        entityName: currentEntityName,
        dataModelExpression,
        primatKeyAttr,
        searchParameters,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        outputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas,
        sourceData: JSON.stringify(frontData)
      }
      this.$Spin.show()
      const { status, data } = await saveBatchExecute('/platform/v1/batch-execution/job/run', params)
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

    // publishStatus为published发布模板，为draft保存模板
    async saveTemplate(mgmtRole, useRole) {
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
      // 插件入参为敏感字段，需要加密
      const encryptFlag = pluginInputParams.some(i => i.sensitiveData === 'Y')
      if (encryptFlag) {
        await this.getInputParamsEncryptKey()
        pluginInputParams.forEach(item => {
          if (
            item.mappingType === 'constant'
            && item.sensitiveData === 'Y'
            && item.bindValue
            && !item.bindValue.startsWith('encrypt ')
          ) {
            const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
            const config = {
              iv: key,
              mode: CryptoJS.mode.CBC
            }
            item.bindValue = 'encrypt ' + CryptoJS.AES.encrypt(item.bindValue, key, config).toString()
          }
        })
      }
      // 缓存前端数据，页面回显使用
      const frontData = {
        userTableColumns,
        seletedRows,
        pluginInputParams,
        pluginOutputParams,
        resultTableParams
      }
      // 查询结果主键
      const currentEntity = primatKeyAttrList.find(item => item.name === primatKeyAttr)
      const resourceDatas = seletedRows.map(item => ({
        id: item.id,
        businessKeyValue: item[primatKeyAttr]
      }))
      // 当前插件
      const plugin = pluginOptions.find(item => item.serviceName === pluginId)
      // 插件入参
      const inputParameterDefinitions = pluginInputParams.map(p => {
        const inputParameterValue = p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        // 解决插件入参pluginConfigInterfaceId和插件ID不同的问题
        p.pluginConfigInterfaceId = plugin.id
        return {
          inputParameter: p,
          inputParameterValue
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
        dataModelExpression,
        primatKeyAttr,
        searchParameters,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        outputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas
      }
      const params = {
        id: this.saveTemplateId || '',
        publishStatus: this.templateStatus,
        name,
        operateObject: dataModelExpression,
        pluginService: plugin.serviceDisplayName || '',
        isDangerousBlock, // 是否开启高危检测
        configData,
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
        this.$router.push({
          path: '/implementation/batch-execution/template-list',
          query: {
            status: this.templateStatus === 'draft' ? 'draft' : 'published'
          }
        })
      }
    },
    validRequired() {
      const {
        name, dataModelExpression, pluginId, pluginInputParams, primatKeyAttr, userTableColumns, seletedRows
      } = this.$refs.form
      if (!name) {
        this.$Message.warning(this.$t('be_template_name_required'))
        return false
      }
      if (!dataModelExpression) {
        this.$Message.warning(this.$t('be_query_path_required'))
        return false
      }
      if (!primatKeyAttr) {
        this.$Message.warning(this.$t('be_result_key_required'))
        return false
      }
      if (userTableColumns && userTableColumns.length === 0) {
        this.$Message.warning(this.$t('be_result_column_required'))
        return false
      }
      if (seletedRows && seletedRows.length === 0) {
        this.$Message.warning(this.$t('be_instance_required'))
        return false
      }
      if (!pluginId) {
        this.$Message.warning(this.$t('be_plugin_server_required'))
        return false
      }
      const pluginInputParamsFlag = pluginInputParams.every(item => {
        if (item.required === 'Y' && item.mappingType === 'constant' && !item.bindValue) {
          return false
        }
        return true
      })
      if ((pluginInputParams && pluginInputParams.length === 0) || !pluginInputParamsFlag) {
        this.$Message.warning(this.$t('be_setting_input_required'))
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
