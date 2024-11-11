<template>
  <div class="workflow-execution-plugin-service">
    <Row>
      <Col :span="24">
        <Form inline :label-width="90">
          <FormItem :label="$t('pluginService')">
            <Input :value="nodeObj.serviceName" style="width: 700px" disabled></Input>
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
  </div>
</template>

<script>
import { getSourceNode, getNodeParams } from '@/api/server'
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
    }
  }
}
</script>
