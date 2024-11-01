<template>
  <div class="workflow-execution-plugin-service">
    <Row :gutter="40">
      <Col :span="12" style="border-right:1px solid #e8eaec;">
        <Form inline :label-width="80" label-position="left">
          <FormItem :label="$t('pluginService')">
            <Select v-model="nodeObj.serviceName" disabled>
              <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                item.serviceDisplayName
              }}</Option>
            </Select>
          </FormItem>
        </Form>
        <Form inline :label-width="80" label-position="left">
          <FormItem :label="$t('parameterSettings')">
            <Tabs type="card">
              <TabPane :label="$t('context_parameters')">
                <template v-if="paramInfos && paramInfos.filter(p => p.bindType === 'context').length > 0">
                  <div>
                    <span>{{ $t('sourceNodeList') }}：</span>
                    <Select
                      v-model="contextParamNodes"
                      multiple
                      filterable
                      disabled
                      style="width:100%;margin-bottom:10px;"
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
                    v-for="(item, itemIndex) in paramInfos"
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
                          disabled
                        >
                          <Option v-for="(item, index) in prevCtxNodeChange()" :value="item.nodeId" :key="index">{{
                            item.name
                          }}</Option>
                        </Select>
                      </div>
                      <div style="width: 22%; display: inline-block">
                        <Select
                          v-model="item.bindParamType"
                          filterable
                          disabled
                        >
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
              </TabPane>
              <TabPane :label="$t('constant_parameters')">
                <template v-if="paramInfos && paramInfos.filter(p => p.bindType === 'constant').length > 0">
                  <div style="background: #e5e9ee">
                    <div style="width: 30%; display: inline-block">{{ $t('parameterskey') }}</div>
                    <div style="width: 68%; display: inline-block">{{ $t('sourceVale') }}</div>
                  </div>
                  <div
                    v-for="(item, itemIndex) in paramInfos"
                    :key="itemIndex"
                    style="margin: 4px"
                  >
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
              </TabPane>
            </Tabs>
          </FormItem>
        </Form>
      </Col>
      <Col :span="12">
        <Form inline :label-width="80" label-position="left">
          <FormItem label="过滤规则">
            <template v-if="filterRules && filterRules.length > 0">
              <div v-for="(i, index) in filterRules" :key="index" style="display:flex;margin-bottom:5px;">
                <div style="width:35%;margin-right:5px;">
                  <Input v-model="i.key" disabled />
                </div>
                <div style="width:30%;margin-right:5px;">
                  <Input v-model="i.operation" disabled />
                </div>
                <div style="width:35%;">
                  <Input v-model="i.value" disabled />
                </div>
              </div>
            </template>
            <template v-else>
              <span style="color:#515a6e;">暂无数据</span>
            </template>
          </FormItem>
        </Form>
      </Col>
    </Row>
  </div>
</template>

<script>
import { getPluginFunByRule, getSourceNode, getNodeParams } from '@/api/server'
export default {
  props: {
    nodeInstance: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      nodeObj: {},
      filteredPlugins: [],
      paramInfos: [],
      contextParamNodes: [],
      nodeList: [],
      filterRules: [],
      paramsTypes: [
        {
          value: 'INPUT',
          label: this.$t('input')
        },
        {
          value: 'OUTPUT',
          label: this.$t('output')
        }
      ]
    }
  },
  watch: {
    nodeInstance: {
      handler(val) {
        if (val.nodeId) {
          this.nodeObj = JSON.parse(JSON.stringify(val))
          this.contextParamNodes = this.nodeObj.contextParamNodes || []
          this.paramInfos = this.nodeObj.paramInfos || []
          this.getFilteredPluginInterfaceList(this.nodeObj.routineExpression)
          this.getRootNode()
          this.mgmtParamInfos()
          this.getFilterRules()
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    // 获取插件函数列表
    async getFilteredPluginInterfaceList(path) {
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
          targetEntityFilterRule: index > 0 ? pathList[pathList.length - 1].slice(index) : ''
        }
      } else {
        payload = {
          pkgName: '',
          entityName: '',
          targetEntityFilterRule: ''
        }
      }

      payload.nodeType = this.nodeObj.nodeType
      const { status, data } = await getPluginFunByRule(payload)
      if (status === 'OK') {
        this.filteredPlugins = data || []
      }
    },
    // 获取可选根节点
    async getRootNode() {
      const { status, data } = await getSourceNode(this.nodeObj.procDefId)
      if (status === 'OK') {
        this.nodeList = (data || []).filter(r => r.nodeId !== this.nodeObj.nodeId)
      }
    },
    // 设置被预选中的节点
    prevCtxNodeChange() {
      return (
        this.nodeList.filter(n => (this.contextParamNodes || []).includes(n.nodeId)) || []
      )
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
        const { status, data } = await getNodeParams(
          this.nodeObj.procDefId,
          paramInfos[index].bindNodeId
        )
        if (status === 'OK') {
          const res = (data || []).filter(
            _ => _.type === this.paramInfos[index].bindParamType
          )
          this.$set(this.paramInfos[index], 'currentParamNames', res)
        }
      }
    },
    getFilterRules() {
      const pattern = /{[^}]+}/g
      const array = this.nodeObj.filterRule && this.nodeObj.filterRule.match(pattern) || []
      this.filterRules = array.map(item => {
        const withoutBrackets = item.slice(1, -1);
        const parts = withoutBrackets.split(" ");
        const [key, operation, value] = parts.map(part => part.replace(/['"]/g, ""))
        return { key, operation, value }
      })
    }
  }
}
</script>

