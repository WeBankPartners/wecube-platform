<template>
  <div class=" ">
    <Row>
      <template v-for="(express, index) in routineExpressionItem">
        <div :key="express.routineExpression + index">
          <Col :span="isBatch ? 22 : 24">
            <FilterRules
              :needAttr="true"
              ref="filterRules"
              style="height:44px"
              v-model="express.routineExpression"
              :allDataModelsWithAttrs="allEntityType"
            >
            </FilterRules>
          </Col>
          <Col span="2" v-if="isBatch">
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
      // routineExpression: 'kubernetes:cluster#DME#wecmdb:app_instance',
      routineExpressionItem: []
    }
  },
  props: ['isBatch', 'allEntityType', 'routineExpression'],
  mounted () {
    if (this.routineExpression) {
      this.routineExpression.split('#DME#').forEach(item => {
        this.routineExpressionItem.push({
          routineExpression: item
        })
      })
    }
  },
  methods: {
    addFilterRule () {
      this.routineExpressionItem.push({
        routineExpression: ''
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
