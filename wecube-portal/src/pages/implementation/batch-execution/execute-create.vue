<template>
  <div class="batch-execute-create">
    <ChooseTemplate v-if="step === 1" from="execute" @select="handleChooseTemplate"></ChooseTemplate>
    <template v-else>
      <BaseForm ref="form" :type="type" :from="from" :data="detailData" />
      <div v-if="type !== 'view'" class="footer-button">
        <Button type="primary" :disabled="false" @click="saveExcute">执行</Button>
      </div>
    </template>
  </div>
</template>

<script>
import ChooseTemplate from './template.vue'
import BaseForm from './base-form.vue'
import { getBatchExecuteTemplateDetail, batchExecuteHistory, saveBatchExecute } from '@/api/server.js'
export default {
  components: {
    ChooseTemplate,
    BaseForm
  },
  data () {
    return {
      step: '',
      from: 'execute', // 模板，执行
      id: this.$route.query.id || '', // 批量执行id
      type: this.$route.query.type || 'add', // 新增，复制，查看
      detailData: {}
    }
  },
  mounted () {
    if (this.id) {
      this.step = 2
      this.getExecuteDetail()
    } else {
      this.step = 1
    }
  },
  methods: {
    handleChooseTemplate (row) {
      this.step = 2
      // 选择模板创建的时候，使用模板ID调用模板详情接口，获取详情信息
      this.getTemplateDetail(row.id)
    },
    // 获取模板详情
    async getTemplateDetail (id) {
      const { status, data } = await getBatchExecuteTemplateDetail(id)
      if (status === 'OK') {
        this.detailData = { ...data, templateData: data }
      }
    },
    // 获取执行详情
    async getExecuteDetail () {
      const { status, data } = await batchExecuteHistory(this.id)
      if (status === 'OK') {
        // 模板创建的执行
        if (data.batchExecutionTemplateId) {
          const { data: templateData } = await getBatchExecuteTemplateDetail(data.batchExecutionTemplateId)
          this.detailData = { ...data, templateData }
        } else {
          // 预执行数据(无模板ID)
          this.detailData = data
        }
        this.$refs.form.getExecuteResult(this.id)
      }
    },
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
        isDangerousBlock: this.detailData.batchExecutionTemplateId
          ? this.detailData.templateData.isDangerousBlock
          : true, // 是否开启高危检测
        batchExecutionTemplateId: this.detailData.batchExecutionTemplateId ? this.detailData.templateData.id : '',
        batchExecutionTemplateName: this.detailData.batchExecutionTemplateId ? this.detailData.templateData.name : '',
        name: name,
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
      const { status } = await saveBatchExecute(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.$eventBusP.$emit('change-menu', 'executeList')
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
