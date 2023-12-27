<template>
  <div class=" ">
    <Row>
      <template v-for="(express, index) in routineExpressionItem">
        <div :key="express.routineExpression + index">
          <Col :span="isBatch ? 19 : 22">
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
          <Col span="2" v-if="isBatch">
            <Input v-model="express.operate" placeholder="Operation" />
          </Col>
          <Col span="3" v-if="isBatch">
            <template>
              <Button
                ghost
                type="primary"
                style="vertical-align: top; margin-left: 8px"
                @click="addFilterRule"
                icon="ios-add"
              ></Button>
              <Button
                ghost
                style="vertical-align: top"
                @click="deleteFilterRule(index)"
                v-if="routineExpressionItem.length > 1"
                type="error"
                icon="ios-trash-outline"
              ></Button>
            </template>
          </Col>
        </div>
      </template>
    </Row>
  </div>
</template>

<script>
import FilterRules from '../../components/filter-rules.vue'
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
      if (routineExpression) {
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
