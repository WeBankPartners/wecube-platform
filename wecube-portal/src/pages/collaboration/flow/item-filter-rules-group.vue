<template>
  <div class=" ">
    <Row>
      <template>
        <div v-for="(express, index) in routineExpressionItem" :key="index">
          <Col :span="isBatch ? 16 : 24">
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
          <Col span="6" v-if="isBatch">
            <Input v-model="express.operate" placeholder="Operation" :disabled="disabled" />
          </Col>
          <Col span="2" v-if="isBatch && !disabled">
            <Button
              size="small"
              ghost
              style="vertical-align: center; margin-left: 5px"
              @click="deleteFilterRule(index)"
              v-if="routineExpressionItem.length > 1"
              type="error"
              icon="ios-trash-outline"
            ></Button>
          </Col>
        </div>
      </template>
    </Row>
    <div v-if="isBatch && !disabled">
      <Button type="success" ghost @click="addFilterRule" size="small" icon="md-add"></Button>
    </div>
  </div>
</template>

<script>
import FilterRules from './item-filter-rules.vue'
export default {
  name: '',
  data() {
    return {
      routineExpressionItem: []
    }
  },
  watch: {
    isBatch: {
      handler() {
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
  mounted() {
    this.changeRoutineExpressionItem(this.routineExpression)
  },
  methods: {
    setRoutineExpressionItem(routineExpression) {
      this.changeRoutineExpressionItem(routineExpression)
    },
    filterRuleChanged(val) {
      if (!this.isBatch) {
        if (val === '' || !val.startsWith(this.currentSelectedEntity)) {
          this.$nextTick(() => {
            this.routineExpressionItem[0].routineExpression = this.currentSelectedEntity
            this.$emit('filterRuleChanged', this.currentSelectedEntity)
          })
        } else {
          this.$emit('filterRuleChanged', val)
        }
      } else {
        this.$nextTick(() => {
          this.routineExpressionItem.forEach(item => {
            if (!item.routineExpression || !val.startsWith(this.currentSelectedEntity)) {
              item.routineExpression = this.currentSelectedEntity
            }
          })
        })
      }
    },
    changeRoutineExpressionItem(routineExpression) {
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
    addFilterRule() {
      this.routineExpressionItem.push({
        routineExpression: this.currentSelectedEntity,
        operate: ''
      })
    },
    deleteFilterRule(index) {
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
