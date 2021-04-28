<template>
  <div class=" ">
    <Row>
      <template v-for="(express, index) in routineExpressionItem">
        <div :key="express.routineExpression + index">
          <Col :span="isBatch ? 21 : 24">
            <FilterRules
              :needAttr="true"
              ref="filterRules"
              style="height:44px"
              v-model="express.routineExpression"
              :allDataModelsWithAttrs="allEntityType"
            >
            </FilterRules>
          </Col>
          <Col span="3" v-if="isBatch">
            <template>
              <Button
                ghost
                type="primary"
                style="vertical-align: top;margin-left:8px"
                @click="addFilterRule"
                icon="ios-add"
              ></Button>
              <Button
                ghost
                style="vertical-align: top;"
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
  props: ['isBatch', 'allEntityType', 'routineExpression', 'rootEntity'],
  mounted () {
    this.changeRoutineExpressionItem(this.routineExpression)
  },
  methods: {
    changeRoutineExpressionItem (routineExpression) {
      this.routineExpressionItem = []
      if (routineExpression) {
        routineExpression.split('#DME#').forEach(item => {
          this.routineExpressionItem.push({
            routineExpression: item
          })
        })
      }
    },
    addFilterRule () {
      this.routineExpressionItem.push({
        routineExpression: this.rootEntity
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

<style scoped lang="scss"></style>
