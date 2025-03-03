<template>
  <div class="batch-execute-create">
    <BaseForm ref="form" :type="type" from="execute" :data="detailData" @back="handleBack" />
    <div v-if="type !== 'view'" class="footer-button">
      <!--执行-->
      <Button type="primary" @click="saveExcute">{{ $t('execute') }}</Button>
    </div>
    <div v-else class="footer-button">
      <!--重新执行-->
      <Button type="primary" @click="relaunch">{{ $t('be_re_execute') }}</Button>
    </div>
    <!--高危检测弹框-->
    <DangerousModal
      :visible.sync="confirmModal.isShowConfirmModal"
      :data="confirmModal"
      @success="forwardToList"
    ></DangerousModal>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'
import BaseForm from '../base-form.vue'
import DangerousModal from '../components/dangerous-modal.vue'
import { debounce } from '@/const/util'
import {
  getBatchExecuteTemplateDetail,
  batchExecuteHistory,
  saveBatchExecute,
  getInputParamsEncryptKey
} from '@/api/server.js'
export default {
  components: {
    BaseForm,
    DangerousModal
  },
  data() {
    return {
      id: this.$route.query.id || '', // 批量执行id
      type: this.$route.query.type || 'add', // 新增，查看
      from: this.$route.query.from || '', // template代表从选择模板页进入
      detailData: {},
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
    if (this.$route.query.id) {
      if (this.$route.query.from === 'template') {
        this.handleChooseTemplate() // 选择模板创建执行
      } else {
        this.getExecuteDetail() // 获取执行详情
      }
    }
  },
  methods: {
    handleBack() {
      this.$router.back()
    },
    forwardToList() {
      this.$router.push('/implementation/batch-execution/execution-history')
    },
    async getInputParamsEncryptKey() {
      const { status, data } = await getInputParamsEncryptKey()
      if (status === 'OK') {
        this.encryptKey = data
      }
    },
    // 选择模板创建
    async handleChooseTemplate() {
      // 选择模板创建的时候，使用模板ID调用模板详情接口，获取详情信息
      const { status, data } = await getBatchExecuteTemplateDetail(this.id)
      if (status === 'OK') {
        this.detailData = {
          ...data,
          templateData: data
        }
        this.detailData.name = `${this.detailData.name}${new Date().getTime()}`
      }
    },
    // 获取执行详情
    async getExecuteDetail() {
      const { status, data } = await batchExecuteHistory(this.id)
      if (status === 'OK') {
        // 有模板ID，获取模板详情数据，拼接到执行历史数据里面
        if (data.batchExecutionTemplateId) {
          const { data: templateData } = await getBatchExecuteTemplateDetail(data.batchExecutionTemplateId)
          this.detailData = {
            ...data,
            isDangerousBlock: data.configData.isDangerousBlock,
            templateData
          }
        } else {
          this.detailData = {
            ...data,
            isDangerousBlock: data.configData.isDangerousBlock,
            templateData: {
              id: '',
              name: ''
            }
          }
        }
        if (this.type === 'copy') {
          this.detailData.name = `${this.detailData.name} (1)`
        }
      }
    },
    // 重新执行
    relaunch() {
      this.type = 'copy'
      this.getExecuteDetail()
    },
    // 执行
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
        pluginOutputParams,
        resultTableParams,
        primatKeyAttrList,
        primatKeyAttr,
        seletedRows,
        searchParameters,
        userTableColumns
      } = this.$refs.form
      // 插件入参为敏感字段，需要加密
      const encryptFlag = pluginInputParams.some(i => i.sensitiveData === 'Y')
      if (encryptFlag) {
        await this.getInputParamsEncryptKey()
        const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
        const config = {
          iv: key,
          mode: CryptoJS.mode.CBC
        }
        pluginInputParams.forEach(item => {
          if (
            item.mappingType === 'constant'
            && item.sensitiveData === 'Y'
            && item.bindValue
            && !item.bindValue.startsWith('encrypt ')
          ) {
            item.bindValue = 'encrypt ' + CryptoJS.AES.encrypt(item.bindValue, key, config).toString()
          }
        })
      }
      // 当前插件数据
      const storageData = JSON.parse(this.detailData.sourceData)
      let plugin = null
      // 有缓存读取缓存数据，无缓存手动更改插件ID，手动方式为兜底方案，建议重新配置模板(解决插件服务重新发起执行时，ID会变化的问题)
      if (storageData.pluginObj) {
        plugin = storageData.pluginObj
      } else {
        plugin = pluginOptions.find(item => item.serviceName === pluginId)
        const realId = (pluginInputParams && pluginInputParams[0] && pluginInputParams[0].pluginConfigInterfaceId) || ''
        plugin.id = realId
        plugin.inputParameters.forEach(i => (i.pluginConfigInterfaceId = realId))
        plugin.outputParameters.forEach(i => (i.pluginConfigInterfaceId = realId))
      }
      // 缓存前端数据，页面回显使用
      const frontData = {
        userTableColumns,
        seletedRows,
        pluginInputParams,
        pluginOutputParams,
        pluginObj: plugin,
        resultTableParams
      }
      // 查询结果主键
      const currentEntity = (primatKeyAttrList && primatKeyAttrList.find(item => item.name === primatKeyAttr)) || {}
      const resourceDatas = seletedRows.map(item => ({
        id: item.id,
        businessKeyValue: item[primatKeyAttr]
      }))
      // 插件入参
      const inputParameterDefinitions = pluginInputParams.map(p => {
        const inputParameterValue = p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        // 解决重新发起插件ID会发生变化的问题
        plugin.id = p.pluginConfigInterfaceId
        return {
          inputParameter: p,
          inputParameterValue
        }
      })
      // 插件出参
      const outputParameterDefinitions = pluginOutputParams.filter(i => {
        let flag = false
        resultTableParams.forEach(j => {
          if (i.name === j) {
            flag = true
          }
        })
        return flag
      })
      const { templateData } = this.detailData
      const params = {
        isDangerousBlock: templateData.id ? templateData.isDangerousBlock : true, // 是否开启高危检测
        batchExecutionTemplateId: templateData.id || '',
        batchExecutionTemplateName: templateData.name || '',
        name,
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
      const { status, data } = await saveBatchExecute(params)
      this.$Spin.hide()
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.$router.push('/implementation/batch-execution/execution-history')
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
.batch-execute-create {
  width: 100%;
  .footer-button {
    width: 920px;
    display: flex;
    justify-content: center;
    padding-bottom: 30px;
  }
}
</style>
