<template>
  <div class="workflow-execution-data-bind">
    <Row :gutter="40">
      <!--数据绑定-->
      <Col :span="12" style="border-right: 1px solid #e8eaec">
        <Form :label-width="90">
          <FormItem :label="$t('locate_approach')" v-if="['human', 'automatic', 'subProc'].includes(nodeObj.nodeType)">
            <Select v-model="nodeObj.dynamicBindInt" disabled>
              <Option v-for="item in dynamicBindOptions" :value="item.value" :key="item.value">{{ item.label }}</Option>
            </Select>
          </FormItem>
          <FormItem
            v-if="['human', 'automatic', 'subProc'].includes(nodeObj.nodeType) && [1].includes(nodeObj.dynamicBindInt)"
          >
            <label slot="label">
              <span style="color: red" v-if="nodeObj.dynamicBindInt === 1">*</span>
              {{ $t('bind_node') }}
            </label>
            <Select v-model="nodeObj.bindNodeId" disabled>
              <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{ i.nodeName }}</Option>
            </Select>
            <span v-if="[1].includes(nodeObj.dynamicBindInt) && nodeObj.bindNodeId === ''" style="color: red">{{ $t('bind_node') }}{{ $t('cannotBeEmpty') }}</span>
          </FormItem>
          <FormItem>
            <label slot="label">
              <span style="color: red">*</span>
              {{ $t('locate_rules') }}
            </label>
            <template v-if="nodeObj.routineExpression === ''">
              {{ $t('setRootEntity') }}
            </template>
            <template v-else>
              <ItemFilterRulesGroup
                :isBatch="nodeObj.nodeType === 'data'"
                ref="filterRulesGroupRef"
                :disabled="true"
                :routineExpression="nodeObj.routineExpression || currentSelectedEntity"
                :allEntityType="allEntityTypeGroup"
                :currentSelectedEntity="currentSelectedEntity"
              >
              </ItemFilterRulesGroup>
            </template>
          </FormItem>
        </Form>
      </Col>
      <!--过滤规则-->
      <Col :span="12">
        <Form inline :label-width="80" label-position="left">
          <FormItem :label="$t('Filtering_rule')">
            <template v-if="nodeObj.nodeType === 'subProc'">
              <FilterRules
                v-model="subProcItem.rootEntity"
                :allDataModelsWithAttrs="allEntityType"
                :rootOnly="true"
                style="width: 100%"
              ></FilterRules>
            </template>
            <template v-else>
              <div v-for="(i, index) in filterRules" :key="index" style="display: flex; margin-bottom: 5px">
                <div style="width: 35%; margin-right: 5px">
                  <Input v-model="i.key" disabled />
                </div>
                <div style="width: 30%; margin-right: 5px">
                  <Input v-model="i.operation" disabled />
                </div>
                <div style="width: 35%">
                  <Input v-model="i.value" disabled />
                </div>
              </div>
              <span v-if="filterRules.length === 0" style="color: #515a6e">-</span>
            </template>
          </FormItem>
        </Form>
      </Col>
    </Row>
  </div>
</template>

<script>
import { getAssociatedNodes, getAllDataModels } from '@/api/server'
import ItemFilterRulesGroup from '@/pages/collaboration/flow/item-filter-rules-group.vue'
import FilterRules from '@/pages/collaboration/flow/item-filter-rules.vue'
export default {
  components: { ItemFilterRulesGroup, FilterRules },
  props: {
    currentSelectedEntity: {
      type: String,
      default: ''
    },
    nodeInstance: {
      type: Object,
      default: () => {}
    },
    subProcItem: {
      type: Object,
      default: () => {
        return {
          rootEntity: ''
        }
      }
    }
  },
  data() {
    return {
      nodeObj: {},
      associatedNodes: [], // 绑定节点
      allEntityType: [], // 系统中所有根CI
      allEntityTypeGroup: [], 
      filterRules: [], // 过滤规则
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
      ]
    }
  },
  watch: {
    nodeInstance: {
      handler(val) {
        if (val.nodeId) {
          this.nodeObj = JSON.parse(JSON.stringify(val))
          this.$nextTick(() => {
            this.changeDynamicBind()
          })
          this.getAssociatedNodes()
          this.getAllDataModels()
          this.getFilterRules()
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    changeDynamicBind() {
      this.$refs.filterRulesGroupRef.setRoutineExpressionItem(this.nodeObj.routineExpression)
    },
    // 获取当前节点的前序节点
    async getAssociatedNodes() {
      const { status, data } = await getAssociatedNodes(
        this.nodeObj.procDefId, // 编排设计ID
        this.nodeObj.procDefNodeId // 编排设计nodeID
      )
      if (status === 'OK') {
        this.associatedNodes = data
      }
    },
    // 获取所有根数据
    async getAllDataModels() {
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityTypeGroup = data.filter(d => d.packageName === this.currentSelectedEntity.split(':')[0])
        this.allEntityType = data || []
      }
    },
    // 解析过滤规则
    getFilterRules() {
      const pattern = /{[^}]+}/g
      const array = (this.nodeObj.filterRule && this.nodeObj.filterRule.match(pattern)) || []
      this.filterRules = array.map(item => {
        const withoutBrackets = item.slice(1, -1)
        const parts = withoutBrackets.split(' ')
        const [key, operation, value] = parts.map(part => part.replace(/['"]/g, ''))
        return {
          key,
          operation,
          value
        }
      }) || []
    }
  }
}
</script>
