<template>
  <div class="workflow-execution-data-bind">
    <Row>
      <Col :span="14">
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
                :allEntityType="allEntityType"
                :currentSelectedEntity="currentSelectedEntity"
              >
              </ItemFilterRulesGroup>
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
export default {
  components: { ItemFilterRulesGroup },
  props: {
    currentSelectedEntity: {
      type: String,
      default: ''
    },
    nodeInstance: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      nodeObj: {},
      associatedNodes: [],
      allEntityType: [],
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
        this.allEntityType = data.filter(d => d.packageName === this.currentSelectedEntity.split(':')[0])
      }
    }
  }
}
</script>
