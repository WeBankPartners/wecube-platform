<template>
  <div>
    <div id="itemInfo">
      <div class="hide-panal" @click="hideItem">
        <Icon type="ios-arrow-dropright" size="28" />
      </div>
      <div class="panal-name">{{ $t('nodeProperties') }}：</div>
      <div class="panel-content">
        <Alert v-if="isShowAlert" show-icon type="error">
          {{ $t('be_plugin_service_no_permission_tip1') }}【{{ mgmtRole }}】{{
            $t('be_plugin_service_no_permission_tip2')
          }}【{{ itemCustomInfo.customAttrs.serviceName }}】{{ $t('be_plugin_service_no_permission_tip3') }}
          {{ mgmtRole }}】
          {{ $t('be_plugin_service_no_permission_tip4') }}
        </Alert>
        <Collapse v-model="opendPanel">
          <!--基础信息-->
          <Panel name="1">
            {{ $t('basicInfo') }}
            <template slot="content">
              <Form :label-width="120">
                <FormItem label="ID">
                  <Input disabled v-model="itemCustomInfo.customAttrs.id"></Input>
                </FormItem>
                <FormItem>
                  <label slot="label">
                    <span style="color: red">*</span>
                    {{ $t('name') }}
                  </label>
                  <Input v-model.trim="itemCustomInfo.customAttrs.name" @on-change="paramsChanged"></Input>
                  <span style="position: absolute; left: 310px; top: 2px; line-height: 28px; background: #ffffff">{{ (itemCustomInfo.customAttrs.name && itemCustomInfo.customAttrs.name.length) || 0 }}/30</span>
                  <span
                    v-if="itemCustomInfo.customAttrs.name && itemCustomInfo.customAttrs.name.length > 30"
                    style="color: red"
                  >{{ $t('name') }}{{ $t('cannotExceed') }} 30 {{ $t('characters') }}</span>
                </FormItem>
                <FormItem :label="$t('node_type')">
                  <Input v-model="itemCustomInfo.customAttrs.nodeType" disabled></Input>
                </FormItem>
                <template v-if="itemCustomInfo.customAttrs && itemCustomInfo.customAttrs.nodeType === 'date'">
                  <FormItem>
                    <label slot="label">
                      <span style="color: red">*</span>
                      {{ $t('date') }}
                    </label>
                    <DatePicker
                      type="datetime"
                      placeholder="Select date and time"
                      v-model="tempDate"
                      format="yyyy-MM-dd HH:mm:ss"
                      @on-change="dateChange"
                      :editable="false"
                      style="width: 100%"
                    ></DatePicker>
                    <span v-if="itemCustomInfo.customAttrs.timeConfig.date === ''" style="color: red">{{ $t('date') }}{{ $t('cannotBeEmpty') }}</span>
                  </FormItem>
                </template>
                <template v-if="itemCustomInfo.customAttrs && itemCustomInfo.customAttrs.nodeType === 'timeInterval'">
                  <FormItem :label="$t('duration')">
                    <label slot="label">
                      <span style="color: red">*</span>
                      {{ $t('duration') }}
                    </label>
                    <InputNumber
                      :max="100"
                      :min="1"
                      style="width: 49%"
                      v-model="itemCustomInfo.customAttrs.timeConfig.duration"
                      @on-change="durationChange"
                    ></InputNumber>
                    <Select
                      v-model="itemCustomInfo.customAttrs.timeConfig.unit"
                      style="width: 49%"
                      filterable
                      @on-change="paramsChanged"
                    >
                      <Option v-for="item in unitOptions" :value="item" :key="item">{{ item }}</Option>
                    </Select>
                  </FormItem>
                </template>
                <template
                  v-if="
                    itemCustomInfo.customAttrs && ['date', 'timeInterval'].includes(itemCustomInfo.customAttrs.nodeType)
                  "
                >
                  <FormItem>
                    <label slot="label">
                      {{ $t('be_allow_skip') }}
                    </label>
                    <i-switch v-model="itemCustomInfo.customAttrs.allowContinue" @on-change="paramsChanged" />
                  </FormItem>
                </template>
              </Form>
            </template>
          </Panel>
          <!--执行控制-->
          <Panel
            name="2"
            v-if="
              itemCustomInfo.customAttrs &&
                ['human', 'automatic', 'subProc', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
            "
          >
            {{ $t('controlOfExecution') }}
            <template slot="content">
              <Form :label-width="120">
                <FormItem
                  :label="$t('timeout')"
                  v-if="
                    itemCustomInfo.customAttrs &&
                      ['automatic', 'subProc', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
                  "
                >
                  <Select v-model="itemCustomInfo.customAttrs.timeout" filterable @on-change="paramsChanged">
                    <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index">{{ item.label }}
                    </Option>
                  </Select>
                </FormItem>
                <FormItem
                  :label="$t('pre_check')"
                  v-if="itemCustomInfo.customAttrs && !['data'].includes(itemCustomInfo.customAttrs.nodeType)"
                >
                  <i-switch v-model="itemCustomInfo.customAttrs.riskCheck" @on-change="paramsChanged" />
                </FormItem>
              </Form>
            </template>
          </Panel>
          <!--数据绑定-->
          <Panel
            name="3"
            v-if="
              itemCustomInfo.customAttrs &&
                ['human', 'automatic', 'subProc', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
            "
          >
            {{ $t('dataBinding') }}
            <template slot="content">
              <Form :label-width="120">
                <FormItem
                  :label="$t('locate_approach')"
                  v-if="['human', 'automatic', 'subProc'].includes(itemCustomInfo.customAttrs.nodeType)"
                >
                  <Select v-model="itemCustomInfo.customAttrs.dynamicBind" @on-change="changDynamicBind">
                    <Option v-for="item in dynamicBindOptions" :value="item.value" :key="item.value">{{
                      item.label
                    }}</Option>
                  </Select>
                </FormItem>
                <FormItem
                  v-if="
                    ['human', 'automatic', 'subProc'].includes(itemCustomInfo.customAttrs.nodeType) &&
                      [1].includes(itemCustomInfo.customAttrs.dynamicBind)
                  "
                >
                  <label slot="label">
                    <span style="color: red" v-if="itemCustomInfo.customAttrs.dynamicBind === 1">*</span>
                    {{ $t('bind_node') }}
                  </label>
                  <Select
                    v-model="itemCustomInfo.customAttrs.bindNodeId"
                    @on-change="changeBindNode"
                    @on-open-change="getAssociatedNodes"
                    clearable
                    filterable
                    :disabled="[0, 2].includes(itemCustomInfo.customAttrs.dynamicBind)"
                  >
                    <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{
                      i.nodeName
                    }}</Option>
                  </Select>
                  <span
                    v-if="
                      [1].includes(itemCustomInfo.customAttrs.dynamicBind) &&
                        itemCustomInfo.customAttrs.bindNodeId === ''
                    "
                    style="color: red"
                  >{{ $t('bind_node') }}{{ $t('cannotBeEmpty') }}</span>
                </FormItem>
                <FormItem>
                  <label slot="label">
                    <span style="color: red">*</span>
                    {{ $t('locate_rules') }}
                  </label>
                  <template v-if="itemCustomInfo.customAttrs.routineExpression === ''">
                    {{ $t('setRootEntity') }}
                  </template>
                  <template v-else>
                    <ItemFilterRulesGroup
                      :isBatch="itemCustomInfo.customAttrs.nodeType === 'data'"
                      ref="filterRulesGroupRef"
                      @filterRuleChanged="singleFilterRuleChanged"
                      :disabled="[1].includes(itemCustomInfo.customAttrs.dynamicBind)"
                      :routineExpression="itemCustomInfo.customAttrs.routineExpression || currentSelectedEntity"
                      :allEntityType="allEntityType"
                      :currentSelectedEntity="currentSelectedEntity"
                    >
                    </ItemFilterRulesGroup>
                  </template>
                </FormItem>
              </Form>
            </template>
          </Panel>
          <!--调用插件服务-->
          <Panel
            name="4"
            v-if="itemCustomInfo.customAttrs && ['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
          >
            {{ $t('calledPluginService') }}
            <template slot="content">
              <Form :label-width="120">
                <FormItem
                  v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
                  style="margin-top: 8px"
                >
                  <label slot="label">
                    <span style="color: red">*</span>
                    {{ $t('pluginService') }}
                  </label>
                  <Select
                    v-model="itemCustomInfo.customAttrs.serviceName"
                    @on-change="changePluginInterfaceList"
                    filterable
                  >
                    <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                      item.serviceDisplayName
                    }}</Option>
                  </Select>
                  <span v-if="itemCustomInfo.customAttrs.serviceName === ''" style="color: red">{{ $t('pluginService') }} {{ $t('cannotBeEmpty') }}</span>
                </FormItem>
              </Form>
              <div v-if="itemCustomInfo.customAttrs.serviceName">
                <span style="margin-right: 20px"> {{ $t('parameterSettings') }} </span>
                <Tabs type="card">
                  <TabPane :label="$t('context_parameters')">
                    <template
                      v-if="
                        itemCustomInfo.customAttrs.paramInfos &&
                          itemCustomInfo.customAttrs.paramInfos.filter(p => p.bindType === 'context').length > 0
                      "
                    >
                      <div>
                        <span>{{ $t('sourceNodeList') }}：</span>
                        <Select
                          v-model="itemCustomInfo.customAttrs.contextParamNodes"
                          multiple
                          filterable
                          style="width: 50%"
                          @on-change="changeContextParamNodes"
                          @on-open-change="getRootNode"
                        >
                          <Option v-for="item in nodeList" :value="item.nodeId" :key="item.nodeId">{{
                            item.name
                          }}</Option>
                        </Select>
                      </div>
                      <div style="display: flex; background: #dee3e8">
                        <div style="width: 25%">{{ $t('parameterskey') }}</div>
                        <div style="width: 72%">{{ $t('sourceVale') }}</div>
                      </div>
                      <div style="background: #e5e9ee">
                        <div style="width: 24%; display: inline-block">{{ $t('params_name') }}</div>
                        <div style="width: 25%; display: inline-block">{{ $t('node') }}</div>
                        <div style="width: 22%; display: inline-block">{{ $t('params_type') }}</div>
                        <div style="width: 25%; display: inline-block">{{ $t('params_value') }}</div>
                      </div>
                      <div
                        v-for="(item, itemIndex) in itemCustomInfo.customAttrs.paramInfos"
                        :key="itemIndex"
                        style="margin: 4px"
                      >
                        <template v-if="item.bindType === 'context'">
                          <div style="width: 24%; display: inline-block">
                            <span style="color: red" v-if="item.required === 'Y'">*</span>
                            {{ item.paramName }}
                          </div>
                          <div style="width: 25%; display: inline-block">
                            <Select
                              v-model="item.bindNodeId"
                              filterable
                              @on-change="onParamsNodeChange(itemIndex, true)"
                            >
                              <Option v-for="(item, index) in prevCtxNodeChange()" :value="item.nodeId" :key="index">{{
                                item.name
                              }}</Option>
                            </Select>
                          </div>
                          <div style="width: 22%; display: inline-block">
                            <Select
                              v-model="item.bindParamType"
                              @on-change="onParamsNodeChange(itemIndex, true)"
                              filterable
                            >
                              <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">{{ i.label }}</Option>
                            </Select>
                          </div>
                          <div style="width: 25%; display: inline-block">
                            <Select filterable v-model="item.bindParamName" @on-change="paramsChanged">
                              <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{
                                i.name
                              }}</Option>
                            </Select>
                          </div>
                        </template>
                      </div>
                    </template>
                  </TabPane>
                  <TabPane :label="$t('constant_parameters')">
                    <template
                      v-if="
                        itemCustomInfo.customAttrs.paramInfos &&
                          itemCustomInfo.customAttrs.paramInfos.filter(p => p.bindType === 'constant').length > 0
                      "
                    >
                      <div style="background: #e5e9ee">
                        <div style="width: 30%; display: inline-block">{{ $t('parameterskey') }}</div>
                        <div style="width: 68%; display: inline-block">{{ $t('sourceVale') }}</div>
                      </div>
                      <div
                        v-for="(item, itemIndex) in itemCustomInfo.customAttrs.paramInfos"
                        :key="itemIndex"
                        style="margin: 4px"
                      >
                        <template v-if="item.bindType === 'constant'">
                          <div style="width: 30%; display: inline-block; text-align: right">
                            <span style="color: red" v-if="item.required === 'Y'">*</span>
                            {{ item.paramName }}
                          </div>
                          <div style="width: 68%; display: inline-block">
                            <Input v-model="item.bindValue" @on-change="paramsChanged" />
                          </div>
                        </template>
                      </div>
                    </template>
                  </TabPane>
                </Tabs>
              </div>
            </template>
          </Panel>
          <!--调用子编排-->
          <Panel
            name="5"
            v-if="itemCustomInfo.customAttrs && ['subProc'].includes(itemCustomInfo.customAttrs.nodeType)"
          >
            {{ $t('call_childFlow') }}
            <template slot="content">
              <Form :label-width="120">
                <FormItem style="margin-top: 8px">
                  <label slot="label">
                    <span style="color: red">*</span>
                    {{ $t('child_workflow') }}
                  </label>
                  <Select v-model="itemCustomInfo.customAttrs.subProcDefId" @on-change="changeSubProc" filterable>
                    <Option v-for="item in subProcList" :value="item.procDefId" :key="item.procDefId">{{
                      `${item.procDefName}【${item.procDefVersion}】`
                    }}</Option>
                  </Select>
                  <span v-if="itemCustomInfo.customAttrs.subProcDefId === ''" style="color: red">{{ $t('child_workflow') }} {{ $t('cannotBeEmpty') }}</span>
                  <span v-if="subProcRemoveFlag" style="color: red">{{ $t('fe_childFlow_permissionTips') }}</span>
                </FormItem>
                <template v-if="itemCustomInfo.customAttrs.subProcDefId && subProcItem">
                  <FormItem :label="$t('child_flowId')">
                    <span>{{ subProcItem.procDefId || itemCustomInfo.customAttrs.subProcDefId || '-' }}</span>
                    <Button :disabled="subProcRemoveFlag" type="info" size="small" @click="viewParentFlowGraph">{{
                      $t('view_workFlow')
                    }}</Button>
                  </FormItem>
                  <FormItem :label="$t('instance_type')">
                    <span>{{ subProcItem.rootEntity || '-' }}</span>
                  </FormItem>
                  <FormItem :label="$t('createdBy')">
                    <span>{{ subProcItem.createUser || '-' }}</span>
                  </FormItem>
                  <FormItem :label="$t('updatedBy')">
                    <span>{{ subProcItem.updateUser || '-' }}</span>
                  </FormItem>
                  <FormItem :label="$t('table_updated_date')">
                    <span>{{ subProcItem.updatedTime || '-' }}</span>
                  </FormItem>
                </template>
              </Form>
            </template>
          </Panel>
        </Collapse>
      </div>
    </div>
    <div class="item-footer">
      <Button v-if="editFlow !== 'false'" :disabled="isSaveBtnActive" @click="saveItem" type="primary">{{
        $t('save')
      }}</Button>
      <Button v-if="editFlow !== 'false'" @click="hideItem" class="btn-gap">{{ $t('cancel') }}</Button>
    </div>
  </div>
</template>
<script>
import {
  getAssociatedNodes,
  getAllDataModels,
  getPluginFunByRule,
  getNodeParams,
  getSourceNode,
  getNodeDetailById,
  getRoleList,
  getChildFlowListNew
} from '@/api/server.js'
import ItemFilterRulesGroup from './item-filter-rules-group.vue'
export default {
  data() {
    return {
      editFlow: true, // 在查看时隐藏按钮
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      needAddFirst: true,
      opendPanel: ['1', '2', '3', '4', '5'],
      currentSelectedEntity: '', // 流程图根
      itemCustomInfo: {
        customAttrs: {
          serviceName: '',
          contextParamNodes: [], // 根任务节点
          paramInfos: []
        }
      },
      // 超时时间选项
      timeSelection: [
        {
          mins: 5,
          label: '5 ' + this.$t('mins')
        },
        {
          mins: 10,
          label: '10 ' + this.$t('mins')
        },
        {
          mins: 30,
          label: '30 ' + this.$t('mins')
        },
        {
          mins: 60,
          label: '1 ' + this.$t('hours')
        },
        {
          mins: 720,
          label: '12 ' + this.$t('hours')
        },
        {
          mins: 1440,
          label: '1 ' + this.$t('days')
        },
        {
          mins: 2880,
          label: '2 ' + this.$t('days')
        },
        {
          mins: 4320,
          label: '3 ' + this.$t('days')
        }
      ],
      associatedNodes: [], // 可选择的前序节点
      allEntityType: [], // 所有模型
      filteredPlugins: [], // 可选择的插件函数，根据定位规则获取
      subProcList: [], // 子编排列表
      subProcItem: {}, // 选中的子编排
      subProcRemoveFlag: false, // 子编排权限被移除或者禁用
      unitOptions: ['sec', 'min', 'hour', 'day'],
      date: '',
      nodeList: [], // 编排中的所有节点，供上下文中绑定使用
      canSelectNode: [], // 存储在上下文中可以选择使用的节点
      paramsTypes: [
        {
          value: 'INPUT',
          label: this.$t('input')
        },
        {
          value: 'OUTPUT',
          label: this.$t('output')
        }
      ],
      mgmtRole: '', // 编排属主角色，供错误提示用
      isShowAlert: false, // 在服务插件有值，但无可选项是提示
      dynamicBindOptions: [
        {
          label: this.$t('during_startup'),
          value: 0
        },
        {
          label: this.$t('during_runtime'),
          value: 2
        },
        {
          label: this.$t('dynamic_bind'),
          value: 1
        }
      ],
      tempDate: ''
    }
  },
  components: {
    ItemFilterRulesGroup
  },
  computed: {
    isSaveBtnActive() {
      let res = false
      if (!this.itemCustomInfo.customAttrs.name || this.itemCustomInfo.customAttrs.name.length > 30) {
        res = true
      }
      if (this.itemCustomInfo.customAttrs.nodeType === 'date') {
        if (this.itemCustomInfo.customAttrs.timeConfig.date === '') {
          res = true
        }
      }
      if (['human', 'automatic', 'subProc'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        if (this.itemCustomInfo.customAttrs.dynamicBind === 1) {
          if (this.itemCustomInfo.customAttrs.bindNodeId === '') {
            res = true
          }
        } else if (this.itemCustomInfo.customAttrs.routineExpression === '') {
          res = true
        }
      }
      // 插件服务必填校验
      if (['human', 'automatic'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        if (!this.itemCustomInfo.customAttrs.serviceName) {
          res = true
        }
      }
      // 子编排必填校验
      if (['subProc'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        if (!this.itemCustomInfo.customAttrs.subProcDefId) {
          res = true
        }
      }
      return res
    }
  },
  mounted() {
    this.getAllDataModels()
  },
  methods: {
    async showItemInfo(nodeData, needAddFirst = false, rootEntity, editFlow, permissionToRole) {
      this.isShowAlert = false
      this.editFlow = editFlow
      this.currentSelectedEntity = rootEntity
      this.needAddFirst = needAddFirst
      if (this.needAddFirst) {
        const tmpData = JSON.parse(JSON.stringify(nodeData))
        const customAttrs = JSON.parse(JSON.stringify(tmpData.customAttrs || {}))
        // delete tmpData.customAttrs
        customAttrs.routineExpression = rootEntity
        this.itemCustomInfo = {
          customAttrs,
          selfAttrs: tmpData
        }
        this.saveItem()
      } else {
        const { status, data } = await getNodeDetailById(nodeData.customAttrs.procDefId, nodeData.id)
        if (status === 'OK') {
          this.itemCustomInfo = data
          this.itemCustomInfo.selfAttrs = JSON.parse(data.selfAttrs)
          this.itemCustomInfo.customAttrs.timeConfig = JSON.parse(data.customAttrs.timeConfig)
          // 临时存储时间中间值，解决时间字符串直接绑定转为UTC时间问题
          this.tempDate = this.itemCustomInfo.customAttrs.timeConfig.date
          if (this.itemCustomInfo.customAttrs.routineExpression === '') {
            this.itemCustomInfo.customAttrs.routineExpression = rootEntity
          }
          this.getPlugin()
          this.getAssociatedNodes()
          this.getRootNode()
          this.mgmtParamInfos()
          this.getSubProcList() // 获取子编排列表
        }
      }
      this.getRoleDisplayName(permissionToRole)
    },
    saveItem() {
      if (['human', 'automatic', 'subProc', 'data'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        const routineExpressionItem = this.$refs.filterRulesGroupRef && this.$refs.filterRulesGroupRef.routineExpressionItem
        if (routineExpressionItem) {
          this.itemCustomInfo.customAttrs.routineExpression = routineExpressionItem.reduce(
            (tmp, item, index) =>
              tmp
              + item.routineExpression
              + '#DMEOP#'
              + item.operate
              + (index === routineExpressionItem.length - 1 ? '' : '#DME#'),
            ''
          )
        }
      }
      if (
        this.itemCustomInfo.customAttrs.routineExpression !== ''
        && this.itemCustomInfo.customAttrs.routineExpression.endsWith('#DMEOP#')
      ) {
        this.itemCustomInfo.customAttrs.routineExpression = this.itemCustomInfo.customAttrs.routineExpression.replace(
          /#DMEOP#$/,
          ''
        )
      }
      // 定位规则操作必填校验
      if (['data'].includes(this.itemCustomInfo.customAttrs.nodeType) && this.needAddFirst === false) {
        const routineExpressionItem = (this.$refs.filterRulesGroupRef && this.$refs.filterRulesGroupRef.routineExpressionItem) || []
        const operateFlag = routineExpressionItem.every(i => i.operate !== '')
        if (!operateFlag) {
          return this.$Message.warning(this.$t('fe_locationRuleTips'))
        }
      }
      // 插件服务校验
      if (['human', 'automatic'].includes(this.itemCustomInfo.customAttrs.nodeType) && this.checkParamsInfo()) {
        return
      }
      const tmpData = JSON.parse(JSON.stringify(this.itemCustomInfo))
      const selfAttrs = tmpData.selfAttrs
      selfAttrs.label = tmpData.customAttrs.name
      const finalData = {
        selfAttrs,
        customAttrs: tmpData.customAttrs
      }
      this.$emit('sendItemInfo', finalData, this.needAddFirst)
      this.needAddFirst = false
    },
    checkParamsInfo() {
      let res = false
      this.itemCustomInfo.customAttrs.paramInfos
        && this.itemCustomInfo.customAttrs.paramInfos.forEach(item => {
          if (item.bindType === 'constant' && item.required === 'Y' && item.bindValue === '') {
            res = true
          }
          if (
            item.bindType === 'context'
            && item.required === 'Y'
            && (item.bindNodeId === '' || item.bindParamType === '' || item.bindParamName === '')
          ) {
            res = true
          }
        })
      if (res) {
        this.$Message.warning(this.$t('checkContextParameter'))
      }
      return res
    },
    panalStatus() {
      return this.isParmasChanged
    },
    hideItem() {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: `${this.$t('confirm_discarding_changes')}`,
          content: `${this.itemCustomInfo.customAttrs.name}:${this.$t('params_edit_confirm')}`,
          'z-index': 1000000,
          okText: this.$t('save'),
          cancelText: this.$t('abandon'),
          onOk: async () => {
            this.saveItem()
          },
          onCancel: () => {
            this.$emit('hideItemInfo')
          }
        })
      } else {
        this.$emit('hideItemInfo')
      }
    },
    // 获取当前节点的前序节点
    async getAssociatedNodes() {
      const { status, data } = await getAssociatedNodes(
        this.itemCustomInfo.customAttrs.procDefId,
        this.itemCustomInfo.customAttrs.id
      )
      if (status === 'OK') {
        this.associatedNodes = data
      }
    },
    // 更新动态绑定节点逻辑
    changeBindNode() {
      const find = this.associatedNodes.find(node => node.nodeId === this.itemCustomInfo.customAttrs.bindNodeId)
      if (find) {
        this.$nextTick(() => {
          this.itemCustomInfo.customAttrs.routineExpression = find.routineExpression
          this.$refs.filterRulesGroupRef.setRoutineExpressionItem(this.itemCustomInfo.customAttrs.routineExpression)
          this.getPlugin()
        })
      }
    },
    // 更新关联节点的响应
    changeAssociatedNode() {},

    // #region 定位规则

    // 获取所有根数据
    async getAllDataModels() {
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data.filter(d => d.packageName === this.currentSelectedEntity.split(':')[0])
      }
    },

    // 定位规则回传
    singleFilterRuleChanged(val) {
      if (val === '') {
        this.changDynamicBind()
      } else {
        this.itemCustomInfo.customAttrs.routineExpression = val
        this.getPlugin()
        this.getSubProcList()
        this.paramsChanged()
      }
    },
    // 获取可选插件
    getPlugin() {
      this.getFilteredPluginInterfaceList(this.itemCustomInfo.customAttrs.routineExpression)
    },
    // #endregion
    // 监听参数变化
    paramsChanged() {
      this.isParmasChanged = true
    },
    dateChange(dateStr) {
      this.itemCustomInfo.customAttrs.timeConfig.date = dateStr
      this.paramsChanged()
    },
    // #region 上下文参数相关
    // 改变插件时的响应
    changePluginInterfaceList(plugin) {
      this.paramsChanged()
      this.itemCustomInfo.customAttrs.contextParamNodes = []
      if (plugin) {
        const findPluginDetail = this.filteredPlugins.find(p => p.serviceName === plugin)
        this.itemCustomInfo.customAttrs.paramInfos = []
        if (findPluginDetail && findPluginDetail.configurableInputParameters) {
          const needParams = findPluginDetail.configurableInputParameters.filter(
            _ => _.mappingType === 'context' || _.mappingType === 'constant'
          )
          this.itemCustomInfo.customAttrs.paramInfos = needParams.map(_ => ({
            paramName: _.name,
            bindNodeId: '',
            bindParamType: 'INPUT',
            bindParamName: '',
            bindType: _.mappingType,
            bindValue: '',
            required: _.required
          }))
        }
      }
    },
    // 改变上下文中的源节点列表清除对应数据的响应
    changeContextParamNodes(selection) {
      this.itemCustomInfo.customAttrs.paramInfos
        && this.itemCustomInfo.customAttrs.paramInfos.forEach(pInfo => {
          if (pInfo.bindNodeId !== '' && !selection.includes(pInfo.bindNodeId)) {
            pInfo.bindNodeId = ''
            pInfo.bindParamName = ''
          }
        })
    },
    // 获取插件函数列表
    async getFilteredPluginInterfaceList(path) {
      let pkg = ''
      let entity = ''
      let payload = {}
      // this.filteredPlugins = []
      if (path) {
        // eslint-disable-next-line no-useless-escape
        const pathList = path.split(/[.~]+(?=[^\}]*(\{|$))/).filter(p => p.length > 1)
        const last = pathList[pathList.length - 1]
        const index = pathList[pathList.length - 1].indexOf('{')
        const isBy = last.indexOf(')')
        const current = last.split(':')
        const ruleIndex = current[1].indexOf('{')
        if (isBy > 0) {
          entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
          pkg = current[0].split(')')[1]
        } else {
          entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
          pkg = last.match(/[^>]+(?=:)/)[0]
        }
        payload = {
          pkgName: pkg,
          entityName: entity.split('#DMEOP#')[0],
          targetEntityFilterRule: index > 0 ? pathList[pathList.length - 1].slice(index) : ''
        }
      } else {
        payload = {
          pkgName: '',
          entityName: '',
          targetEntityFilterRule: ''
        }
      }

      payload.nodeType = this.itemCustomInfo.customAttrs.nodeType
      const { status, data } = await getPluginFunByRule(payload)
      if (status === 'OK') {
        this.filteredPlugins = data || []
        if (this.itemCustomInfo.customAttrs.serviceName !== '' && this.filteredPlugins.length === 0) {
          this.isShowAlert = true
          this.$emit('hideReleaseBtn')
        }
      }
    },
    // 设置被预选中的节点
    prevCtxNodeChange() {
      return (
        this.nodeList.filter(n => (this.itemCustomInfo.customAttrs.contextParamNodes || []).includes(n.nodeId)) || []
      )
    },
    // 改变节点及参数类型获取参数名
    onParamsNodeChange(index, paramsChanged) {
      this.getParamsOptionsByNode(index)
      if (paramsChanged) {
        this.paramsChanged()
      }
    },
    async getParamsOptionsByNode(index) {
      this.$set(this.itemCustomInfo.customAttrs.paramInfos[index], 'currentParamNames', [])
      const paramInfos = this.itemCustomInfo.customAttrs.paramInfos || []
      if (paramInfos[index].bindNodeId !== '') {
        const { status, data } = await getNodeParams(
          this.itemCustomInfo.customAttrs.procDefId,
          paramInfos[index].bindNodeId
        )
        if (status === 'OK') {
          const res = (data || []).filter(
            _ => _.type === this.itemCustomInfo.customAttrs.paramInfos[index].bindParamType
          )
          this.$set(this.itemCustomInfo.customAttrs.paramInfos[index], 'currentParamNames', res)
        }
      }
    },
    mgmtParamInfos() {
      const paramInfos = this.itemCustomInfo.customAttrs.paramInfos
      paramInfos
        && paramInfos.forEach((p, pIndex) => {
          if (p.bindType === 'context') {
            this.onParamsNodeChange(pIndex, false)
          }
        })
    },
    // 获取可选根节点
    async getRootNode() {
      const { status, data } = await getSourceNode(this.itemCustomInfo.customAttrs.procDefId)
      if (status === 'OK') {
        this.nodeList = (data || []).filter(r => r.nodeId !== this.itemCustomInfo.customAttrs.id)
      }
    },
    // #endregion
    changDynamicBind() {
      this.itemCustomInfo.customAttrs.bindNodeId = ''
      this.itemCustomInfo.customAttrs.routineExpression = this.currentSelectedEntity
      this.itemCustomInfo.customAttrs.serviceName = ''
      this.itemCustomInfo.customAttrs.paramInfos = []
      this.$nextTick(() => {
        this.$refs.filterRulesGroupRef.setRoutineExpressionItem(this.itemCustomInfo.customAttrs.routineExpression)
        this.getPlugin()
      })
      this.paramsChanged()
    },
    durationChange() {
      if (!this.itemCustomInfo.customAttrs.timeConfig.duration) {
        this.itemCustomInfo.customAttrs.timeConfig.duration = 0
      }
      this.paramsChanged()
    },
    async getRoleDisplayName(permissionToRole) {
      if (permissionToRole.MGMT && permissionToRole.MGMT.length > 0) {
        const params = { all: 'Y' }
        const { status, data } = await getRoleList(params)
        if (status === 'OK') {
          const find = data.find(d => d.name === permissionToRole.MGMT[0])
          if (find) {
            this.mgmtRole = find.displayName
          }
        }
      }
    },
    // 选择子编排
    changeSubProc(val) {
      this.paramsChanged()
      this.subProcItem = this.subProcList.find(i => i.procDefId === val) || {}
    },
    // 获取子编排列表
    async getSubProcList() {
      if (this.itemCustomInfo.customAttrs.nodeType === 'subProc') {
        const params = {
          entityExpr: this.itemCustomInfo.customAttrs.routineExpression
        }
        const { status, data } = await getChildFlowListNew(params)
        if (status === 'OK') {
          this.subProcRemoveFlag = false
          this.subProcList = data || []
          if (this.itemCustomInfo.customAttrs.subProcDefId) {
            this.subProcItem = this.subProcList.find(i => i.procDefId === this.itemCustomInfo.customAttrs.subProcDefId) || {}
            // 编辑操作，匹配不到对应子编排，删除子编排
            if (!this.subProcItem.procDefId && this.editFlow !== 'false') {
              this.itemCustomInfo.customAttrs.subProcDefId = ''
            }
            // 查看编排，匹配不到对应数据，给出提示
            if (!this.subProcItem.procDefId && this.editFlow === 'false') {
              this.subProcRemoveFlag = true
            }
          }
        }
      }
    },
    viewParentFlowGraph() {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/collaboration/workflow-mgmt?flowId=${this.itemCustomInfo.customAttrs.subProcDefId}&editFlow=false&flowListTab=deployed`
      window.open(path, '_blank')
    }
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: absolute;
  top: 134px;
  right: 13px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 86%;
  background: white;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
  // overflow: auto;
  height: calc(100vh - 154px);
}
.panel-content {
  overflow: auto;
  height: calc(100vh - 240px);
}

.ivu-form-item {
  margin-bottom: 0px;
}

.panal-name {
  padding: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eaec;
  font-weight: bold;
}

.ivu-collapse {
  border: none !important;
}
.ivu-collapse > .ivu-collapse-item {
  border-top: none !important;
}

.hide-panal {
  position: fixed;
  top: 400px;
  right: 500px;
  color: #5384ff;
  cursor: pointer;
}
.item-footer {
  position: absolute;
  z-index: 10;
  bottom: 26px;
  right: 12px;
  width: 500px;
  padding: 8px 24px;
  background: #ffffff;
  height: 32px;
}
</style>
