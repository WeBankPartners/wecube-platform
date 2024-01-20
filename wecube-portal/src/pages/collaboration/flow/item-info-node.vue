<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem"></div>
    <div class="panal-name">节点属性：</div>
    <Collapse simple v-model="opendPanel">
      <Panel name="1">
        基础信息
        <template slot="content">
          <Form :label-width="120" ref="formValidate" :model="itemCustomInfo" :rules="baseRuleValidate">
            <FormItem label="ID">
              <Input disabled v-model="itemCustomInfo.id"></Input>
            </FormItem>
            <FormItem :label="$t('name')" prop="label">
              <Input v-model="itemCustomInfo.label" @on-change="paramsChanged"></Input>
            </FormItem>
            <FormItem :label="$t('node_type')">
              <Input v-model="itemCustomInfo.customAttrs.nodeType" disabled></Input>
            </FormItem>
            <template v-if="itemCustomInfo.customAttrs && itemCustomInfo.customAttrs.nodeType === 'fixedTime'">
              <FormItem :label="$t('date')">
                <DatePicker
                  type="datetime"
                  placeholder="Select date and time"
                  v-model="itemCustomInfo.customAttrs.timeConfig.date"
                  format="yyyy-MM-dd HH:mm:ss"
                  @on-change="dateChange"
                  style="width: 200px"
                ></DatePicker>
              </FormItem>
            </template>
            <template v-if="itemCustomInfo.customAttrs && itemCustomInfo.customAttrs.nodeType === 'timeInterval'">
              <FormItem :label="$t('duration')">
                <InputNumber :max="100" :min="0" v-model="itemCustomInfo.customAttrs.timeConfig.duration"></InputNumber>
                <Select v-model="itemCustomInfo.customAttrs.timeConfig.unit" style="width: 100px" filterable>
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
          <Form :label-width="80">
            <FormItem :label="$t('timeout')">
              <Select v-model="itemCustomInfo.customAttrs.timeout" filterable>
                <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index"
                  >{{ item.label }}
                </Option>
              </Select>
            </FormItem>
            <FormItem
              :label="$t('pre_check')"
              v-if="itemCustomInfo.customAttrs && !['data'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <i-switch v-model="itemCustomInfo.customAttrs.riskCheck" />
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
          <Form :label-width="80">
            <FormItem
              :label="$t('dynamic_bind')"
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <i-switch
                v-model="itemCustomInfo.customAttrs.dynamicBind"
                @on-change="itemCustomInfo.customAttrs.bindNodeId = ''"
              />
            </FormItem>
            <FormItem
              :label="$t('bind_node')"
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <Select
                v-model="itemCustomInfo.customAttrs.bindNodeId"
                @on-change="changeAssociatedNode"
                @on-open-change="getAssociatedNodes"
                clearable
                filterable
                :disabled="!itemCustomInfo.customAttrs.dynamicBind"
              >
                <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{ i.nodeName }}</Option>
              </Select>
            </FormItem>
            <FormItem :label="$t('locate_rules')">
              <ItemFilterRulesGroup
                :isBatch="itemCustomInfo.customAttrs.nodeType === 'data'"
                ref="filterRulesGroupRef"
                @filterRuleChanged="singleFilterRuleChanged"
                :disabled="itemCustomInfo.customAttrs.dynamicBind && itemCustomInfo.customAttrs.bindNodeId"
                :routineExpression="itemCustomInfo.customAttrs.routineExpression"
                :allEntityType="allEntityType"
                :currentSelectedEntity="currentSelectedEntity"
              >
              </ItemFilterRulesGroup>
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
          <Form :label-width="80">
            <FormItem
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
              :label="$t('plugin')"
              style="margin-top: 8px"
            >
              <Select
                v-model="itemCustomInfo.customAttrs.serviceName"
                @on-change="changePluginInterfaceList"
                filterable
              >
                <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                  item.serviceDisplayName
                }}</Option>
              </Select>
            </FormItem>
          </Form>
          <div v-if="itemCustomInfo.customAttrs.serviceName">
            <span style="margin-right: 20px"> 参数设置 </span>
            <Tabs type="card">
              <TabPane label="上下文参数">
                <div>
                  <span>设置[填充值来源-节点]列表：</span>
                  <Select
                    v-model="itemCustomInfo.customAttrs.contextParamNodes"
                    multiple
                    filterable
                    style="width: 50%"
                    @on-change="prevCtxNodeChange"
                    @on-open-change="getFlowNodes"
                  >
                    <Option v-for="(item, index) in nodeList" :value="item.value" :key="index">{{ item.label }}</Option>
                  </Select>
                </div>
                <div style="display: flex; justify-content: space-around; background: #dee3e8">
                  <div>填入参数(key)</div>
                  <div>填充值来源(value)</div>
                </div>
                <div style="background: #e5e9ee">
                  <div style="width: 24%; display: inline-block">参数名</div>
                  <div style="width: 25%; display: inline-block">节点</div>
                  <div style="width: 22%; display: inline-block">参数类型</div>
                  <div style="width: 25%; display: inline-block">参数名</div>
                </div>
                <div
                  v-for="(item, itemIndex) in itemCustomInfo.customAttrs.paramInfos"
                  :key="itemIndex"
                  style="margin: 4px"
                >
                  <template v-if="item.bindType === 'context'">
                    <div style="width: 24%; display: inline-block">{{ item.paramName }}</div>
                    <div style="width: 25%; display: inline-block">
                      <Select v-model="item.bindNodeId" filterable @on-change="onParamsNodeChange(itemIndex)">
                        <Option v-for="(item, index) in canSelectNode" :value="item.value" :key="index">{{
                          item.label
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
                        <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{ i.name }}</Option>
                      </Select>
                    </div>
                  </template>
                </div>
              </TabPane>
              <TabPane label="静态参数">
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
                    <div style="width: 30%; display: inline-block; text-align: right">{{ item.paramName }}：</div>
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
      <div style="position: absolute; bottom: 20px; right: 280px; width: 200px">
        <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
        <Button @click="hideItem">{{ $t('cancel') }}</Button>
      </div>
    </Collapse>
  </div>
</template>
<script>
import { getAssociatedNodes, getAllDataModels, getPluginFunByRule, getFlowById, getNodeParams } from '@/api/server.js'
import ItemFilterRulesGroup from './item-filter-rules-group.vue'
export default {
  data () {
    return {
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      needAddFirst: true,
      opendPanel: ['1', '3', '4'],
      currentSelectedEntity: 'wecmdb:subsystem', // 流程图根
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
      baseRuleValidate: {
        label: [
          { required: true, message: 'label cannot be empty', trigger: 'blur' },
          { type: 'string', max: 16, message: 'Label cannot exceed 16 words.', trigger: 'blur' }
        ]
      },
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
    async showItemInfo (data, needAddFirst = false) {
      this.isParmasChanged = false
      this.needAddFirst = needAddFirst
      const defaultNode = {
        id: '', // 节点id  nodeId
        label: '', // 节点名称 nodeName
        customAttrs: {
          procDefId: '', // 对应编排信息
          procDefKey: '', // 对应编排信息
          timeout: 30, // 超时时间
          description: null, // 描述说明
          dynamicBind: false, // 动态绑定
          bindNodeId: null, // 动态绑定关联节点id
          nodeType: '', // 节点类型，对应节点原始类型（start、end……
          routineExpression: 'wecmdb:subsystem', // 对应节点中的定位规则
          routineRaw: null, // 还未知作用
          serviceName: null, // 选择的插件名称
          riskCheck: true, // 高危检测
          paramInfos: [], // 存在插件注册处需要填写的字段
          timeConfig: {
            duration: 0, // 时间间隔
            unit: 'sec', // 时间间隔单位
            date: '' // 固定时间
          }
        }
      }
      const tmpData = JSON.parse(JSON.stringify(data))
      const customAttrs = tmpData.customAttrs || []
      delete tmpData.customAttrs
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(customAttrs)
      keys.forEach(k => {
        this.itemCustomInfo.customAttrs[k] = customAttrs[k]
      })
      this.getPlugin()
      this.getAssociatedNodes()
      this.mgmtParamInfos()
      console.log(this.itemCustomInfo)
      if (needAddFirst) {
        this.saveItem()
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

      const tmp = JSON.parse(JSON.stringify(this.itemCustomInfo))
      let customAttrs = tmp.customAttrs
      customAttrs.id = tmp.id
      customAttrs.name = tmp.label
      delete tmp.customAttrs
      let selfAttrs = tmp
      let finalData = {
        selfAttrs: selfAttrs,
        customAttrs: customAttrs
      }
      this.$emit('sendItemInfo', finalData, this.needAddFirst)
      this.needAddFirst = false
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: '放弃修改',
          'z-index': 1000000,
          onOk: async () => {
            this.$emit('hideItemInfo')
          },
          onCancel: () => {}
        })
      } else {
        this.$emit('hideItemInfo')
      }
    },
    // 获取当前节点的前序节点
    async getAssociatedNodes () {
      let { status, data } = await getAssociatedNodes(this.itemCustomInfo.customAttrs.procDefId, this.itemCustomInfo.id)
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
    },
    // #region 上下文参数相关
    // 改变插件时的响应
    changePluginInterfaceList (plugin) {
      if (plugin) {
        // const findPluginDetail = this.filteredPlugins.find(p => p.serviceName === plugin)
        // console.log(plugin, findPluginDetail)
        const findPluginDetail = {
          id: 'u16Qfy0Q44Y1',
          pluginConfigId: 'u16QfxVQ44XE',
          action: 'operation',
          serviceName: 'wecmdb/ci-data(confirm)/operation',
          serviceDisplayName: 'wecmdb/ci-data(confirm)/operation',
          path: '/wecmdb/plugin/ci-data/operation',
          httpMethod: 'POST',
          isAsyncProcessing: 'N',
          filterRule: '',
          description: null,
          type: 'EXECUTION',
          inputParameters: [
            {
              id: 'u16Qfy2Q44ZG',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'INPUT',
              name: 'ciType',
              dataType: 'string',
              mappingType: 'constant',
              mappingEntityExpression: '',
              mappingSystemVariableName: null,
              required: 'Y',
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            },
            {
              id: 'u16Qfy2Q4507',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'INPUT',
              name: 'operation',
              dataType: 'string',
              mappingType: 'system_variable',
              mappingEntityExpression: '',
              mappingSystemVariableName: 'WECMDB_CONFIRM',
              required: 'Y',
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            },
            {
              id: 'u16Qfy3Q4516',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'INPUT',
              name: 'jsonData',
              dataType: 'string',
              mappingType: 'context',
              mappingEntityExpression: '',
              mappingSystemVariableName: null,
              required: 'Y',
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            }
          ],
          outputParameters: [
            {
              id: 'u16Qfy4Q452t',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'OUTPUT',
              name: 'errorCode',
              dataType: 'string',
              mappingType: 'context',
              mappingEntityExpression: null,
              mappingSystemVariableName: null,
              required: null,
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            },
            {
              id: 'u16Qfy5Q453X',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'OUTPUT',
              name: 'errorMessage',
              dataType: 'string',
              mappingType: 'context',
              mappingEntityExpression: null,
              mappingSystemVariableName: null,
              required: null,
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            },
            {
              id: 'u16Qfy5Q454a',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'OUTPUT',
              name: 'guid',
              dataType: 'string',
              mappingType: 'context',
              mappingEntityExpression: null,
              mappingSystemVariableName: null,
              required: null,
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            }
          ],
          configurableInputParameters: [
            {
              id: 'u16Qfy2Q44ZG',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'INPUT',
              name: 'ciType',
              dataType: 'string',
              mappingType: 'constant',
              mappingEntityExpression: '',
              mappingSystemVariableName: null,
              required: 'Y',
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            },
            {
              id: 'u16Qfy3Q4516',
              pluginConfigInterfaceId: 'u16Qfy0Q44Y1',
              type: 'INPUT',
              name: 'jsonData',
              dataType: 'string',
              mappingType: 'context',
              mappingEntityExpression: '',
              mappingSystemVariableName: null,
              required: 'Y',
              sensitiveData: 'N',
              description: null,
              mappingValue: null,
              multiple: null,
              refObjectName: null,
              refObjectMeta: null
            }
          ]
        }
        this.itemCustomInfo.customAttrs.paramInfos = {}
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
      // this.routineExpressionCache = path
    },
    // 获取流程所有节点
    async getFlowNodes (val) {
      if (!val) return
      const { status, data } = await getFlowById(this.itemCustomInfo.customAttrs.procDefId)
      if (status === 'OK') {
        let nodes = data.taskNodeInfos.nodes || []
        this.nodeList = nodes
          .filter(node => node.customAttrs.id !== this.itemCustomInfo.id)
          .map(n => {
            const customAttrs = n.customAttrs
            return {
              label: customAttrs.name,
              value: customAttrs.id
            }
          })
      }
    },
    // 设置被预选中的节点
    prevCtxNodeChange (val) {
      this.canSelectNode = this.nodeList.filter(n => val.includes(n.value))
      console.log(val, this.canSelectNode)
    },
    // 改变节点及参数类型获取参数名
    onParamsNodeChange (index) {
      // this.editFormdata()
      this.getParamsOptionsByNode(index)
    },
    async getParamsOptionsByNode (index) {
      // currentParamNames
      // if (!this.currentFlow || !found) return
      let { status, data } = await getNodeParams(this.itemCustomInfo.customAttrs.procDefId, this.itemCustomInfo.id)
      if (status === 'OK') {
        let res = data.filter(_ => _.type === this.itemCustomInfo.customAttrs.paramInfos[index].bindParamType)
        this.$set(this.itemCustomInfo.customAttrs.paramInfos[index], 'currentParamNames', res)
      }
    },
    mgmtParamInfos () {
      console.log(11, this.itemCustomInfo.customAttrs.paramInfos)
      const paramInfos = this.itemCustomInfo.customAttrs.paramInfos
      paramInfos.forEach((p, pIndex) => {
        if (p.bindType === 'context') {
          this.getParamsOptionsByNode(pIndex)
        }
      })
    }
    // #endregion
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: absolute;
  top: 139px;
  right: 32px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 86%;
  background: white;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
  overflow: auto;
  height: calc(100vh - 160px);
}
.ivu-form-item {
  margin-bottom: 12px;
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
  width: 12px;
  height: 22px;
  border-radius: 10px 0 0 10px;
  background-color: white;
  border-top: 1px solid #0892ed80;
  border-bottom: 1px solid #0892ed80;
  border-left: 1px solid #0892ed80;
  overflow: hidden;

  position: fixed;
  top: 400px;
  right: 531px;
  cursor: pointer;
  box-shadow: 0 0 8px #0892ed80;
}
</style>
