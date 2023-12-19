<template>
  <div>
    <Row style="margin-bottom: 8px">
      <Col span="4" style="margin-right: 10px">
        <span style="margin-right: 10px">{{ $t('flow_name') }}</span>
        <Select
          clearable
          @on-clear="clearFlow"
          @on-change="currentFlow.tags = ''"
          v-model="selectedFlow"
          style="width: 65%"
          @on-open-change="getAllFlows"
          filterable
        >
          <Option
            v-for="(item, index) in allFlows"
            :value="item.procDefId"
            :key="index"
            :label="(item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')"
          >
            <span>{{
              (item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')
            }}</span>
            <span style="float: right">
              <Button
                @click="
                  showDeleteConfirm(
                    item.procDefId,
                    (item.procDefName || 'Null') + ' ' + item.createdTime + (item.status === 'draft' ? '*' : '')
                  )
                "
                icon="ios-trash"
                type="error"
                size="small"
              ></Button>
            </span>
            <span style="float: right; margin-right: 10px">
              <Button @click="setFlowPermission(item.procDefId)" icon="ios-person" type="primary" size="small"></Button>
            </span>
          </Option>
        </Select>
      </Col>
      <Col span="7" style="margin-right: 10px">
        <span style="margin-right: 10px">{{ $t('instance_type') }}</span>
        <div style="width: 80%; display: inline-block; vertical-align: middle">
          <FilterRules
            @change="onEntitySelect"
            v-model="currentSelectedEntity"
            :allDataModelsWithAttrs="allEntityType"
            style="width: 100%"
          ></FilterRules>
        </div>
      </Col>
      <Col span="3" style="">
        <span style="margin-right: 10px">{{ $t('tag') }}</span>
        <div style="width: 60%; display: inline-block; vertical-align: middle">
          <Input v-model="currentFlow.tags" />
        </div>
      </Col>
      <Checkbox style="margin-right: 25px" border :disabled="!selectedFlow && !isAdd" v-model="excludeMode">{{
        $t('conflict_test')
      }}</Checkbox>
      <!-- <Button style="margin-top: -1px" type="info" :disabled="isSaving || !selectedFlow" @click="saveDiagram(false)">
        {{ $t('release_flow') }}
      </Button>
      <Button
        @click="setFlowPermission(selectedFlow)"
        :disabled="!selectedFlow"
        style="margin-top: -1px"
        type="primary"
      >
        {{ $t('permission_for_flow') }}
      </Button>
      <Button :disabled="!selectedFlow" style="margin-top: -1px" type="info" @click="exportProcessDefinition(false)">
        {{ $t('export_flow') }}
      </Button>

      <Button style="float: right" @click="createNewDiagram()" type="success">
        {{ $t('create') }}
      </Button>
      <Button style="float: right; margin-right: 4px" type="primary" @click="getHeaders">{{
        $t('import_flow')
      }}</Button>

      <Upload
        v-show="isShowUploadList"
        ref="uploadButton"
        show-upload-list
        accept=".pds"
        name="uploadFile"
        :on-success="onImportProcessDefinitionSuccess"
        :on-error="onImportProcessDefinitionError"
        action="platform/v1/process/definitions/import"
        :headers="headers"
      >
        <Button style="display: none">{{ $t('import_flow') }}</Button>
      </Upload> -->
    </Row>
  </div>
</template>

<script>
import FilterRules from '@/pages/components/filter-rules.vue'
import { getAllFlow, getAllDataModels } from '@/api/server.js'
export default {
  components: {
    FilterRules
  },
  data () {
    return {
      isAdd: false, // 是否为新增流程
      selectedFlow: '', // 当前编辑中的流程
      allFlows: [], // 流程列表
      currentFlow: {
        tags: ''
      },
      currentSelectedEntity: '', // 当前显示的根CI
      allEntityType: [], // 系统中所有根CI
      excludeMode: true // 冲突检测
    }
  },
  mounted () {
    // 获取所有根CI类型
    this.getAllDataModels()
  },
  watch: {
    selectedFlow: {
      handler (val, oldVal) {
        // this.isFormDataChange = false
        const flowInfo = this.allFlows.find(flow => flow.procDefId === val)
        this.currentSelectedEntity = flowInfo.rootEntity
        // this.show = false
        // this.selectedFlowData = {}
        if (val) {
          // this.selectedFlowData =
          //   this.allFlows.find(_ => {
          //     return _.procDefId === val
          //   }) || {}
          // this.getFlowXml(val)
          // this.getPermissionByProcess(val)
          // this.pluginForm.paramInfos = []
          // this.currentflowsNodes = []
        }
      }
    }
  },
  methods: {
    async getAllFlows (s) {
      if (s) {
        const { data, status } = await getAllFlow()
        if (status === 'OK') {
          this.allFlows = data
        }
      }
    },
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    onEntitySelect (v) {
      this.currentSelectedEntity = v || ''
      // if (this.currentSelectedEntity.split('{')[0] !== this.pluginForm.routineExpression.split('{')[0]) {
      //   if (this.serviceTaskBindInfos.length > 0) this.serviceTaskBindInfos = []
      //   this.pluginForm = {
      //     ...this.defaultPluginForm,
      //     routineExpression: v
      //   }
      //   this.resetNodePluginConfig()
      // }
    },
    clearFlow () {
      this.currentSelectedEntity = ''
      this.currentFlow.tags = ''
      this.excludeMode = false
    }
  }
}
</script>

<style scoped lang="scss"></style>
