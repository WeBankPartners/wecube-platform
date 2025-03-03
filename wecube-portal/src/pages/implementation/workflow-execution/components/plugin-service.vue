<template>
  <div class="workflow-execution-plugin-service">
    <Row>
      <Col :span="24">
        <Form inline :label-width="90">
          <FormItem :label="$t('pluginService')">
            <Input :value="nodeObj.serviceName" style="width: 700px" disabled></Input>
            <Button type="info" @click="handleViewParaters">{{ $t('pi_view_params') }}</Button>
          </FormItem>
        </Form>
        <Form inline :label-width="90">
          <FormItem :label="$t('parameterSettings')">
            <Tabs type="card" style="width: 700px">
              <TabPane :label="$t('context_parameters')">
                <template v-if="paramInfos && paramInfos.filter(p => p.bindType === 'context').length > 0">
                  <div>
                    <span>{{ $t('sourceNodeList') }}：</span>
                    <Select
                      v-model="contextParamNodes"
                      multiple
                      filterable
                      disabled
                      style="width: 100%; margin-bottom: 10px"
                    >
                      <Option v-for="item in nodeList" :value="item.nodeId" :key="item.nodeId">{{ item.name }}</Option>
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
                  <div v-for="(item, itemIndex) in paramInfos" :key="itemIndex" style="margin: 4px">
                    <template v-if="item.bindType === 'context'">
                      <div style="width: 24%; display: inline-block">
                        <span style="color: red" v-if="item.required === 'Y'">*</span>
                        {{ item.paramName }}
                      </div>
                      <div style="width: 25%; display: inline-block">
                        <Select v-model="item.bindNodeId" filterable disabled>
                          <Option v-for="(item, index) in prevCtxNodeChange()" :value="item.nodeId" :key="index">{{
                            item.name
                          }}</Option>
                        </Select>
                      </div>
                      <div style="width: 22%; display: inline-block">
                        <Select v-model="item.bindParamType" filterable disabled>
                          <Option v-for="i in paramsTypes" :value="i.value" :key="i.value">{{ i.label }}</Option>
                        </Select>
                      </div>
                      <div style="width: 25%; display: inline-block">
                        <Select filterable disabled v-model="item.bindParamName">
                          <Option v-for="i in item.currentParamNames" :value="i.name" :key="i.name">{{
                            i.name
                          }}</Option>
                        </Select>
                      </div>
                    </template>
                  </div>
                </template>
                <template v-else><div style="padding: 0px 10px">{{ $t('noData') }}</div></template>
              </TabPane>
              <TabPane :label="$t('constant_parameters')">
                <template v-if="paramInfos && paramInfos.filter(p => p.bindType === 'constant').length > 0">
                  <div style="background: #e5e9ee">
                    <div style="width: 30%; display: inline-block">{{ $t('parameterskey') }}</div>
                    <div style="width: 68%; display: inline-block">{{ $t('sourceVale') }}</div>
                  </div>
                  <div v-for="(item, itemIndex) in paramInfos" :key="itemIndex" style="margin: 4px">
                    <template v-if="item.bindType === 'constant'">
                      <div style="width: 30%; display: inline-block; text-align: left">
                        <span style="color: red" v-if="item.required === 'Y'">*</span>
                        {{ item.paramName }}
                      </div>
                      <div style="width: 68%; display: inline-block">
                        <Input v-model="item.bindValue" disabled />
                      </div>
                    </template>
                  </div>
                </template>
                <template v-else><div style="padding: 0px 10px">{{ $t('noData') }}</div></template>
              </TabPane>
            </Tabs>
          </FormItem>
        </Form>
      </Col>
    </Row>
    <Modal
      v-model="paraterVisible"
      width="80%"
      class-name="vertical-center-modal"
      :title="$t('pi_plugin_params')"
      :mask-closable="false"
    >
      <div class="workflow-modal-paramsContainer">
        <Row style="border-bottom: 1px solid #e5dfdf; margin-bottom: 5px">
          <Col span="2" offset="0">
            <strong style="font-size: 15px">{{ $t('params_type') }}</strong>
          </Col>
          <Col span="2" offset="0">
            <strong style="font-size: 15px">{{ $t('params_name') }}</strong>
          </Col>
          <Col span="2" offset="0" style="text-align: center">
            <strong style="font-size: 15px">{{ $t('data_type') }}</strong>
          </Col>
          <Col span="1" style="margin-left: 60px" offset="0">
            <strong style="font-size: 15px">{{ $t('core_multiple') }}</strong>
          </Col>
          <Col span="1" style="margin-left: 45px" offset="0">
            <strong style="font-size: 15px">{{ $t('sensitive') }}</strong>
          </Col>
          <Col span="2" offset="1">
            <strong style="font-size: 15px">
              {{ $t('attribute_type') }}
            </strong>
          </Col>
          <Col span="7" style="margin-left: 122px" offset="1">
            <strong style="font-size: 15px">{{ $t('attribute') }}</strong>
          </Col>
        </Row>
        <div class="modal-interfaceContainers">
          <Form disabled>
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>{{ $t('input_params') }}</span>
                </FormItem>
              </Col>
              <Col span="22" offset="0">
                <Row v-for="(param, index) in currentInter['inputParameters']" :key="index">
                  <Col span="3">
                    <FormItem :label-width="0">
                      <span v-if="param.required === 'Y'" style="color: red; vertical-align: text-bottom">*</span>
                      <Tooltip content="">
                        <span
                          style="display: inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis"
                        >{{ param.name }}</span>
                        <div slot="content" style="white-space: normal">
                          <span>{{ param.description }}</span>
                        </div>
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="2" offset="0">
                    <FormItem :label-width="0">
                      <span>{{ param.dataType }}</span>
                    </FormItem>
                  </Col>
                  <Col span="1" offset="0">
                    <FormItem :label-width="0">
                      <Select v-model="param.multiple" filterable style="width: 70px">
                        <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                          item.label
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="1" offset="1">
                    <FormItem :label-width="0">
                      <Select v-model="param.sensitiveData" filterable style="width: 70px">
                        <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                          item.label
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="3" offset="1">
                    <FormItem :label-width="0">
                      <Select filterable v-model="param.mappingType" @on-change="mappingTypeChange($event, param)">
                        <Option v-for="item in mappingTypeOptions" :value="item.value" :key="item.key">{{
                          item.value
                        }}</Option>
                      </Select>
                    </FormItem>
                  </Col>
                  <Col span="11" offset="1">
                    <FormItem :label-width="0">
                      <Select
                        filterable
                        v-if="param.mappingType === 'system_variable'"
                        v-model="param.mappingSystemVariableName"
                        @on-open-change="retrieveSystemVariables"
                      >
                        <Option
                          v-for="(item, index) in allSystemVariables.filter(i => i.status === 'active')"
                          :value="item.name"
                          :key="index"
                        >{{ item.name }}</Option>
                      </Select>
                      <span v-if="param.mappingType === 'context'">N/A</span>
                      <span v-if="param.mappingType === 'constant'">
                        <Input v-model="param.mappingValue" placeholder="" />
                      </span>
                      <span v-if="param.mappingType === 'entity'">
                        <div style="width: 100%; display: inline-block; vertical-align: top">
                          <FilterRulesRef
                            v-model="param.mappingEntityExpression"
                            :allDataModelsWithAttrs="allEntityType"
                            :rootEntity="clearedEntityType"
                            :needNativeAttr="true"
                            :needAttr="true"
                            :rootEntityFirst="true"
                            :disabled="true"
                          ></FilterRulesRef>
                        </div>
                        <Button
                          v-if="param.dataType === 'object' && param.refObjectMeta"
                          type="primary"
                          size="small"
                          @click="showObjectConfig(param)"
                        >{{ $t('configuration') }}</Button>
                      </span>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
            <hr />
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>{{ $t('output_params') }}</span>
                </FormItem>
              </Col>
              <Col span="22" offset="0">
                <Row v-for="(outPut, index) in currentInter['outputParameters']" :key="index">
                  <template v-if="outPut.mappingType !== 'assign'">
                    <Col span="3">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color: red; vertical-align: text-bottom">*</span>
                        <Tooltip content="">
                          <span
                            style="
                              display: inline-block;
                              white-space: nowrap;
                              overflow: hidden;
                              text-overflow: ellipsis;
                            "
                          >{{ outPut.name }}</span>
                          <div slot="content" style="white-space: normal">
                            <span>{{ outPut.description }}</span>
                          </div>
                        </Tooltip>
                      </FormItem>
                    </Col>
                    <Col span="2" offset="0">
                      <FormItem :label-width="0">
                        <span>{{ outPut.dataType }}</span>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="0">
                      <FormItem :label-width="0">
                        <Select v-model="outPut.multiple" filterable style="width: 70px">
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="1">
                      <FormItem :label-width="0">
                        <Select filterable v-model="outPut.sensitiveData" style="width: 70px">
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="3" offset="1">
                      <FormItem :label-width="0">
                        <Select v-model="outPut.mappingType">
                          <Option value="context" key="context">context</Option>
                          <Option value="entity" key="entity">entity</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="11" offset="1">
                      <FormItem :label-width="0">
                        <FilterRulesRef
                          v-if="outPut.mappingType === 'entity'"
                          v-model="outPut.mappingEntityExpression"
                          :allDataModelsWithAttrs="allEntityType"
                          :rootEntity="clearedEntityType"
                          :needNativeAttr="true"
                          :needAttr="true"
                          :rootEntityFirst="true"
                          :disabled="true"
                        ></FilterRulesRef>
                        <span v-if="outPut.mappingType === 'context'">N/A</span>
                      </FormItem>
                    </Col>
                  </template>
                  <template v-else>
                    <Col span="3">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color: red; vertical-align: text-bottom">*</span>
                        <Input v-model="outPut.name" placeholder="key" style="width: 80%" />
                      </FormItem>
                    </Col>
                    <Col span="2" offset="0">
                      <FormItem :label-width="0">
                        <span>{{ outPut.dataType }}</span>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="0">
                      <FormItem :label-width="0">
                        <Select v-model="outPut.multiple" filterable style="width: 70px">
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="1" offset="1">
                      <FormItem :label-width="0">
                        <Select filterable v-model="outPut.sensitiveData" style="width: 70px">
                          <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                            item.label
                          }}</Option>
                        </Select>
                      </FormItem>
                    </Col>
                    <Col span="3" offset="1">
                      <FormItem :label-width="0">
                        <span v-if="outPut.required === 'Y'" style="color: red; vertical-align: text-bottom">*</span>
                        <Input v-model="outPut.mappingValue" placeholder="value" />
                      </FormItem>
                    </Col>
                    <Col span="11" offset="1">
                      <FormItem :label-width="0">
                        <FilterRulesRef
                          v-model="outPut.mappingEntityExpression"
                          :allDataModelsWithAttrs="allEntityType"
                          :rootEntity="clearedEntityType"
                          :needNativeAttr="true"
                          :needAttr="true"
                          :rootEntityFirst="true"
                          :disabled="true"
                        ></FilterRulesRef>
                        <span v-if="outPut.mappingType === 'context'">N/A</span>
                      </FormItem>
                    </Col>
                  </template>
                </Row>
              </Col>
            </Row>
          </Form>
        </div>
        <Spin fix v-if="spinShow"></Spin>
      </div>
      <div slot="footer"></div>
    </Modal>
  </div>
</template>

<script>
import {
  getSourceNode,
  getNodeParams,
  getPluginFunByRule,
  retrieveSystemVariables,
  getAllDataModels
} from '@/api/server'
import FilterRulesRef from '@/pages/components/filter-rules-ref.vue'
export default {
  components: { FilterRulesRef },
  props: {
    nodeInstance: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      nodeObj: {},
      paramInfos: [],
      contextParamNodes: [],
      nodeList: [],
      paraterVisible: false,
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
      filteredPlugins: [],
      currentInter: {},
      allEntityType: [],
      clearedEntityType: '',
      allSystemVariables: [],
      sensitiveData: [
        {
          value: 'Y',
          label: 'Y'
        },
        {
          value: 'N',
          label: 'N'
        }
      ],
      mappingTypeOptions: [
        {
          label: 'context',
          value: 'context'
        },
        {
          label: 'system_variable',
          value: 'system_variable'
        },
        {
          label: 'entity',
          value: 'entity'
        },
        {
          label: 'constant',
          value: 'constant'
        }
      ],
      spinShow: false
    }
  },
  watch: {
    nodeInstance: {
      handler(val) {
        if (val.nodeId) {
          this.nodeObj = JSON.parse(JSON.stringify(val))
          this.contextParamNodes = this.nodeObj.contextParamNodes || []
          this.paramInfos = this.nodeObj.paramInfos || []
          this.getRootNode()
          this.mgmtParamInfos()
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    // 获取可选根节点
    async getRootNode() {
      const { status, data } = await getSourceNode(this.nodeObj.procDefId)
      if (status === 'OK') {
        this.nodeList = (data || []).filter(r => r.nodeId !== this.nodeObj.nodeId)
      }
    },
    // 设置被预选中的节点
    prevCtxNodeChange() {
      return this.nodeList.filter(n => (this.contextParamNodes || []).includes(n.nodeId)) || []
    },
    mgmtParamInfos() {
      this.paramInfos
        && this.paramInfos.forEach((p, index) => {
          if (p.bindType === 'context') {
            this.getParamsOptionsByNode(index)
          }
        })
    },
    async getParamsOptionsByNode(index) {
      this.$set(this.paramInfos[index], 'currentParamNames', [])
      const paramInfos = this.paramInfos || []
      if (paramInfos[index].bindNodeId !== '') {
        const { status, data } = await getNodeParams(this.nodeObj.procDefId, paramInfos[index].bindNodeId)
        if (status === 'OK') {
          const res = (data || []).filter(_ => _.type === this.paramInfos[index].bindParamType)
          this.$set(this.paramInfos[index], 'currentParamNames', res)
        }
      }
    },
    // 查看插件参数
    async handleViewParaters() {
      this.paraterVisible = true
      await this.getFilteredPluginInterfaceList()
      this.retrieveSystemVariables()
      this.getAllDataModels()
    },
    // 根据定位规则获取插件列表
    async getFilteredPluginInterfaceList() {
      const path = this.nodeObj.routineExpression
      let pkg = ''
      let entity = ''
      let payload = {}
      if (path) {
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
          targetEntityFilterRule: index > 0 ? pathList[pathList.length - 1].slice(index) : '',
          procDefId: this.nodeObj.procDefId
        }
      } else {
        payload = {
          pkgName: '',
          entityName: '',
          targetEntityFilterRule: '',
          procDefId: this.nodeObj.procDefId
        }
      }

      payload.nodeType = this.nodeObj.nodeType
      this.spinShow = true
      const { status, data } = await getPluginFunByRule(payload)
      this.spinShow = false
      if (status === 'OK') {
        this.currentInter = (data && data.find(item => item.serviceName === this.nodeObj.serviceName)) || {}
        this.clearedEntityType = this.nodeObj.routineExpression.split('{')[0]
      }
    },
    async retrieveSystemVariables() {
      const { data, status } = await retrieveSystemVariables({
        filters: [
          {
            'name': 'status',
            'operator': 'eq',
            'value': 'active'
          }
        ],
        paging: false
      })
      if (status === 'OK') {
        this.allSystemVariables = data.contents
      }
    },
    async getAllDataModels() {
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data.map(_ =>
          // handle result sort by name
          ({
            ..._,
            entities: _.entities.sort(function (a, b) {
              const s = a.name.toLowerCase()
              const t = b.name.toLowerCase()
              if (s < t) {
                return -1
              }
              if (s > t) {
                return 1
              }
            })
          }))
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.workflow-modal-paramsContainer {
  min-height: 500px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>
<style lang="scss">
.vertical-center-modal .ivu-modal {
  top: 40px;
}
</style>
