<template>
  <div class=" ">
    <Row>
      <template>
        <div v-for="(express, index) in routineExpressionItem" :key="index">
          <Col :span="isBatch ? 16 : 22">
            <FilterRules
              :needAttr="true"
              ref="filterRules"
              :disabled="disabled"
              style="height: 35px"
              @change="filterRuleChanged"
              v-model="express.routineExpression"
              :allDataModelsWithAttrs="allEntityType"
            >
            </FilterRules>
          </Col>
          <Col span="5" v-if="isBatch">
            <Input v-model="express.operate" placeholder="Operation" />
          </Col>
          <Col span="2" v-if="isBatch">
            <Button
              ghost
              style="vertical-align: top"
              @click="deleteFilterRule(index)"
              v-if="routineExpressionItem.length > 1"
              type="error"
              icon="ios-trash-outline"
            ></Button>
          </Col>
        </div>
      </template>
    </Row>
    <div v-if="isBatch">
      <Button ghost type="primary" style="" @click="addFilterRule" icon="ios-add"></Button>
    </div>
  </div>
</template>

<script>
import FilterRules from './item-filter-rules.vue'
export default {
  name: '',
  data () {
    return {
      routineExpressionItem: []
    }
  },
  watch: {
    isBatch: {
      handler (val) {
        if (!this.isBatch) {
          this.$nextTick(() => {
            this.routineExpressionItem = [this.routineExpressionItem[0]]
          })
        }
      },
      immediate: true
    }
  },
  props: ['isBatch', 'allEntityType', 'routineExpression', 'currentSelectedEntity', 'disabled'],
  mounted () {
    this.changeRoutineExpressionItem(this.routineExpression)
  },
  methods: {
    filterRuleChanged (val) {
      if (!this.isBatch) {
        this.$emit('filterRuleChanged', val)
      }
    },
    changeRoutineExpressionItem (routineExpression) {
      this.routineExpressionItem = []
      if (routineExpression !== '') {
        routineExpression.split('#DME#').forEach(item => {
          const itemSplit = item.split('#DMEOP#')
          this.routineExpressionItem.push({
            routineExpression: itemSplit[0],
            operate: itemSplit[1] || ''
          })
        })
      }
    },
    addFilterRule () {
      this.routineExpressionItem.push({
        routineExpression: this.currentSelectedEntity.split('{')[0],
        operate: ''
      })
    },
    deleteFilterRule (index) {
      this.routineExpressionItem.splice(index, 1)
    }
  },
  components: {
    FilterRules
  }
}
</script>

<style scoped lang="scss">
.ivu-form-item-content {
  background: white;
}
</style>
