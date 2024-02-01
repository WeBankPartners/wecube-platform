<template>
  <div>
    <div id="itemInfo">
      <div class="hide-panal" @click="hideItem">
        <Icon type="ios-arrow-dropright" size="28" />
      </div>
      <div class="panal-name">{{ $t('nodeProperties') }}：</div>
      <div class="panel-content">
        <Collapse v-model="opendPanel">
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
                  <Input v-model="itemCustomInfo.customAttrs.name" @on-change="paramsChanged"></Input>
                  <span style="position: absolute; left: 310px; top: 2px; line-height: 30px; background: #ffffff"
                    >{{ (itemCustomInfo.customAttrs.name && itemCustomInfo.customAttrs.name.length) || 0 }}/30</span
                  >
                  <span
                    v-if="itemCustomInfo.customAttrs.name && itemCustomInfo.customAttrs.name.length > 30"
                    style="color: red"
                    >{{ $t('name') }}{{ $t('cannotExceed') }} 30 {{ $t('characters') }}</span
                  >
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
                      v-model="itemCustomInfo.customAttrs.timeConfig.date"
                      format="yyyy-MM-dd HH:mm:ss"
                      @on-change="dateChange"
                      style="width: 100%"
                    ></DatePicker>
                    <span v-if="itemCustomInfo.customAttrs.timeConfig.date === ''" style="color: red"
                      >{{ $t('date') }}{{ $t('cannotBeEmpty') }}</span
                    >
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
                      @on-change="paramsChanged"
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
              </Form>
            </template>
          </Panel>
          <Panel
            name="2"
            v-if="
              itemCustomInfo.customAttrs && ['human', 'automatic', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
            "
          >
            执行控制
            <template slot="content">
              <Form :label-width="120">
                <FormItem :label="$t('timeout')">
                  <Select v-model="itemCustomInfo.customAttrs.timeout" filterable @on-change="paramsChanged">
                    <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index"
                      >{{ item.label }}
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
          <Panel
            name="3"
            v-if="
              itemCustomInfo.customAttrs && ['human', 'automatic', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
            "
          >
            数据绑定
            <template slot="content">
              <Form :label-width="120">
                <FormItem
                  :label="$t('dynamic_bind')"
                  v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
                >
                  <i-switch v-model="itemCustomInfo.customAttrs.dynamicBind" @on-change="changDynamicBind" />
                </FormItem>
                <FormItem v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)">
                  <label slot="label">
                    <span style="color: red" v-if="itemCustomInfo.customAttrs.dynamicBind">*</span>
                    {{ $t('bind_node') }}
                  </label>
                  <Select
                    v-model="itemCustomInfo.customAttrs.bindNodeId"
                    @on-change="paramsChanged"
                    @on-open-change="getAssociatedNodes"
                    clearable
                    filterable
                    :disabled="!itemCustomInfo.customAttrs.dynamicBind"
                  >
                    <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{
                      i.nodeName
                    }}</Option>
                  </Select>
                  <span
                    v-if="itemCustomInfo.customAttrs.dynamicBind && itemCustomInfo.customAttrs.bindNodeId === ''"
                    style="color: red"
                    >{{ $t('bind_node') }}{{ $t('cannotBeEmpty') }}</span
                  >
                </FormItem>
                <FormItem>
                  <label slot="label">
                    <span style="color: red" v-if="!itemCustomInfo.customAttrs.dynamicBind">*</span>
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
                      :disabled="itemCustomInfo.customAttrs.dynamicBind"
                      :routineExpression="itemCustomInfo.customAttrs.routineExpression || routineExpression"
                      :allEntityType="allEntityType"
                      :currentSelectedEntity="currentSelectedEntity"
                    >
                    </ItemFilterRulesGroup>
                  </template>
                </FormItem>
              </Form>
            </template>
          </Panel>
          <Panel
            name="4"
            v-if="itemCustomInfo.customAttrs && ['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
          >
            调用插件服务
            <template slot="content">
              <Form :label-width="120">
                <FormItem
                  v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
                  style="margin-top: 8px"
                >
                  <label slot="label">
                    <span style="color: red">*</span>
                    插件服务
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
                  <span v-if="itemCustomInfo.customAttrs.serviceName === ''" style="color: red"
                    >插件服务{{ $t('cannotBeEmpty') }}</span
                  >
                </FormItem>
              </Form>
              <div v-if="itemCustomInfo.customAttrs.serviceName">
                <span style="margin-right: 20px"> {{ $t('parameterSettings') }} </span>
                <Tabs type="card">
                  <TabPane :label="$t('context_parameters')">
                    <div>
                      <span>设置[填充值来源-节点]列表：</span>
                      <Select
                        v-model="itemCustomInfo.customAttrs.contextParamNodes"
                        multiple
                        filterable
                        style="width: 50%"
                        @on-change="prevCtxNodeChange"
                        @on-open-change="getRootNode"
                      >
                        <Option v-for="item in nodeList" :value="item.nodeId" :key="item.nodeId">{{
                          item.name
                        }}</Option>
                      </Select>
                    </div>
                    <div style="display: flex; background: #dee3e8">
                      <div style="width: 25%">填入参数(key)</div>
                      <div style="width: 72%">填充值来源(value)</div>
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
                          <Select v-model="item.bindNodeId" filterable @on-change="onParamsNodeChange(itemIndex)">
                            <Option v-for="(item, index) in canSelectNode" :value="item.nodeId" :key="index">{{
                              item.name
                            }}</Option>
                          </Select>
                        </div>
                        <div style="width: 22%; display: inline-block">
                          <Select v-model="item.bindParamType" @on-change="onParamsNodeChange(itemIndex)" filterable>
                            <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">{{ i.label }}</Option>
                          </Select>
                        </div>
                        <div style="width: 25%; display: inline-block">
                          <Select filterable v-model="item.bindParamName">
                            <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{
                              i.name
                            }}</Option>
                          </Select>
                        </div>
                      </template>
                    </div>
                  </TabPane>
                  <TabPane :label="$t('constant_parameters')">
                    <div style="background: #e5e9ee">
                      <div style="width: 30%; display: inline-block">填入参数(key)</div>
                      <div style="width: 68%; display: inline-block">填充值(value)</div>
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
                          <Input v-model="item.bindValue" />
                        </div>
                      </template>
                    </div>
                  </TabPane>
                </Tabs>
              </div>
            </template>
          </Panel>
        </Collapse>
      </div>
    </div>
    <div class="item-footer">
      <Button v-if="editFlow !== 'false'" :disabled="isSaveBtnActive()" @click="saveItem" type="primary">{{
        $t('save')
      }}</Button>
      <Button @click="hideItem">{{ $t('cancel') }}</Button>
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
  getNodeDetailById
} from '@/api/server.js'
import ItemFilterRulesGroup from './item-filter-rules-group.vue'
export default {
  data () {
    return {
      editFlow: true, // 在查看时隐藏按钮
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      needAddFirst: true,
      opendPanel: ['1', '2', '3', '4'],
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
      unitOptions: ['sec', 'min', 'hour', 'day'],
      date: '',
      nodeList: [], // 编排中的所有节点，供上下文中绑定使用
      canSelectNode: [], // 存储在上下文中可以选择使用的节点
      paramsTypes: [
        { value: 'INPUT', label: this.$t('input') },
        { value: 'OUTPUT', label: this.$t('output') }
      ]
    }
  },
  components: {
    ItemFilterRulesGroup
  },
  mounted () {
    this.getAllDataModels()
  },
  methods: {
    async showItemInfo (nodeData, needAddFirst = false, rootEntity, editFlow) {
      this.editFlow = editFlow
      this.currentSelectedEntity = rootEntity
      this.needAddFirst = needAddFirst
      if (this.needAddFirst) {
        let tmpData = JSON.parse(JSON.stringify(nodeData))
        let customAttrs = JSON.parse(JSON.stringify(tmpData.customAttrs || {}))
        // delete tmpData.customAttrs
        customAttrs.routineExpression = rootEntity
        this.itemCustomInfo = {
          customAttrs,
          selfAttrs: tmpData
        }
        this.saveItem()
      } else {
        let { status, data } = await getNodeDetailById(nodeData.customAttrs.procDefId, nodeData.id)
        if (status === 'OK') {
          this.itemCustomInfo = data
          this.itemCustomInfo.selfAttrs = JSON.parse(data.selfAttrs)
          this.itemCustomInfo.customAttrs.timeConfig = JSON.parse(data.customAttrs.timeConfig)
          if (this.itemCustomInfo.customAttrs.routineExpression === '') {
            this.itemCustomInfo.customAttrs.routineExpression = rootEntity
          }
          this.getPlugin()
          this.getAssociatedNodes()
          this.getRootNode()
          this.mgmtParamInfos()
        }
      }
    },
    saveItem () {
      if (['human', 'automatic', 'data'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        const routineExpressionItem =
          this.$refs.filterRulesGroupRef && this.$refs.filterRulesGroupRef.routineExpressionItem
        if (routineExpressionItem) {
          this.itemCustomInfo.customAttrs.routineExpression = routineExpressionItem.reduce((tmp, item, index) => {
            return (
              tmp +
              item.routineExpression +
              '#DMEOP#' +
              item.operate +
              (index === routineExpressionItem.length - 1 ? '' : '#DME#')
            )
          }, '')
        }
      }
      if (['human', 'automatic'].includes(this.itemCustomInfo.customAttrs.nodeType) && this.checkParamsInfo()) return
      const tmpData = JSON.parse(JSON.stringify(this.itemCustomInfo))
      let selfAttrs = tmpData.selfAttrs
      selfAttrs.label = tmpData.customAttrs.name
      let finalData = {
        selfAttrs: selfAttrs,
        customAttrs: tmpData.customAttrs
      }
      this.$emit('sendItemInfo', finalData, this.needAddFirst)
      this.needAddFirst = false
    },
    checkParamsInfo () {
      let res = false
      this.itemCustomInfo.customAttrs.paramInfos.forEach(item => {
        if (item.bindType === 'constant' && item.required === 'Y' && item.bindValue === '') {
          res = true
        }
        if (item.bindType === 'context' && item.required === 'Y' && item.bindNodeId === '') {
          res = true
        }
      })
      if (res) {
        this.$Message.warning(this.$t('constant_parameters'))
      }
      return res
    },
    isSaveBtnActive () {
      let res = false
      if (this.itemCustomInfo.customAttrs.name && this.itemCustomInfo.customAttrs.name.length > 30) {
        res = true
      }
      if (this.itemCustomInfo.customAttrs.nodeType === 'date') {
        if (this.itemCustomInfo.customAttrs.timeConfig.date === '') {
          res = true
        }
      }
      if (['human', 'automatic'].includes(this.itemCustomInfo.customAttrs.nodeType)) {
        if (this.itemCustomInfo.customAttrs.dynamicBind) {
          if (this.itemCustomInfo.customAttrs.bindNodeId === '') {
            res = true
          }
        } else if (this.itemCustomInfo.customAttrs.routineExpression === '') {
          res = true
        }
        if (this.itemCustomInfo.customAttrs.serviceName === '') {
          res = true
        }
      }
      return res
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
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
    async getAssociatedNodes () {
      let { status, data } = await getAssociatedNodes(
        this.itemCustomInfo.customAttrs.procDefId,
        this.itemCustomInfo.customAttrs.id
      )
      if (status === 'OK') {
        this.associatedNodes = data
      }
    },
    // 更新关联节点的响应
    changeAssociatedNode () {},

    // #region 定位规则

    // 获取所有根数据
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },

    // 定位规则回传
    singleFilterRuleChanged (val) {
      this.itemCustomInfo.customAttrs.routineExpression = val
      this.getPlugin()
      this.paramsChanged()
    },
    // 获取可选插件
    getPlugin () {
      this.getFilteredPluginInterfaceList(this.itemCustomInfo.customAttrs.routineExpression)
    },
    // #endregion
    // 监听参数变化
    paramsChanged () {
      this.isParmasChanged = true
    },
    dateChange (dateStr) {
      this.itemCustomInfo.customAttrs.timeConfig.date = dateStr
      this.paramsChanged()
    },
    // #region 上下文参数相关
    // 改变插件时的响应
    changePluginInterfaceList (plugin) {
      this.paramsChanged()
      if (plugin) {
        const findPluginDetail = this.filteredPlugins.find(p => p.serviceName === plugin)
        this.itemCustomInfo.customAttrs.paramInfos = []
        if (findPluginDetail) {
          let needParams = findPluginDetail.configurableInputParameters.filter(
            _ => _.mappingType === 'context' || _.mappingType === 'constant'
          )
          this.itemCustomInfo.customAttrs.paramInfos = needParams.map(_ => {
            return {
              paramName: _.name,
              bindNodeId: '',
              bindParamType: 'INPUT',
              bindParamName: '',
              bindType: _.mappingType,
              bindValue: '',
              required: _.required
            }
          })
        }
      }
    },
    // 获取插件函数列表
    async getFilteredPluginInterfaceList (path) {
      let pkg = ''
      let entity = ''
      let payload = {}
      this.filteredPlugins = []
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
        this.filteredPlugins = data
      }
    },
    // 设置被预选中的节点
    prevCtxNodeChange (val) {
      this.canSelectNode = this.nodeList.filter(n => val.includes(n.nodeId))
    },
    // 改变节点及参数类型获取参数名
    onParamsNodeChange (index) {
      // this.editFormdata()
      this.getParamsOptionsByNode(index)
    },
    async getParamsOptionsByNode (index) {
      this.$set(this.itemCustomInfo.customAttrs.paramInfos[index], 'currentParamNames', [])
      const paramInfos = this.itemCustomInfo.customAttrs.paramInfos
      if (paramInfos[index].bindNodeId !== '') {
        let { status, data } = await getNodeParams(
          this.itemCustomInfo.customAttrs.procDefId,
          paramInfos[index].bindNodeId
        )
        if (status === 'OK') {
          let res = (data || []).filter(_ => _.type === this.itemCustomInfo.customAttrs.paramInfos[index].bindParamType)
          this.$set(this.itemCustomInfo.customAttrs.paramInfos[index], 'currentParamNames', res)
        }
      }
    },
    mgmtParamInfos () {
      const paramInfos = this.itemCustomInfo.customAttrs.paramInfos
      paramInfos &&
        paramInfos.forEach((p, pIndex) => {
          if (p.bindType === 'context') {
            this.onParamsNodeChange(pIndex)
          }
        })
    },
    // 获取可选根节点
    async getRootNode () {
      let { status, data } = await getSourceNode(this.itemCustomInfo.customAttrs.procDefId)
      if (status === 'OK') {
        this.nodeList = (data || []).filter(r => r.nodeId !== this.itemCustomInfo.customAttrs.id)
      }
    },
    // #endregion
    changDynamicBind () {
      this.paramsChanged()
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
  // height: calc(100vh - 160px);
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
  color: #2db7f5;
  cursor: pointer;
}
.item-footer {
  position: absolute;
  z-index: 10;
  bottom: 19px;
  right: 12px;
  width: 500px;
  padding: 8px 24px;
  background: #ffffff;
}
</style>
