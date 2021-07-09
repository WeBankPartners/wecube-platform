<template>
  <div class="test">
    <ul>
      <li v-for="(objItem, itemIndex) in treeData.refObjectMeta.propertyMetas" class="tree-border" :key="itemIndex">
        <div class="tree-title" :style="stylePadding">
          <!-- @click="hide(itemIndex)" -->
          <Form :label-width="80">
            <Row>
              <Col span="2">
                <FormItem :label="$t('params_name')">
                  <span v-if="objItem.required === 'Y'" style="color:red;vertical-align: text-bottom;">*</span>
                  <Tooltip content="">
                    <span
                      style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                      >{{ objItem.name }}</span
                    >
                    <div slot="content" style="white-space: normal;">
                      <span>{{ objItem.description }}</span>
                    </div>
                  </Tooltip>
                </FormItem>
              </Col>
              <Col span="2" offset="1">
                <FormItem :label="$t('data_type')">
                  <span>{{ objItem.dataType }}</span>
                </FormItem>
              </Col>
              <Col span="3" offset="1">
                <FormItem :label="$t('sensitive')">
                  <Select
                    v-model="objItem.sensitiveData"
                    :disabled="status === 'ENABLED'"
                    filterable
                    style="width:150px"
                  >
                    <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                      item.label
                    }}</Option>
                  </Select>
                </FormItem>
              </Col>
              <Col span="3" offset="1">
                <FormItem :label="$t('attribute_type')">
                  <Select
                    filterable
                    v-model="objItem.mappingType"
                    :disabled="status === 'ENABLED'"
                    @on-change="mappingTypeChange($event, objItem)"
                  >
                    <Option value="context" key="context">context</Option>
                    <Option value="system_variable" key="system_variable">system_variable</Option>
                    <Option value="entity" key="entity">entity</Option>
                    <Option value="constant" key="constant">constant</Option>
                    <Option value="object" key="object">object</Option>
                  </Select>
                </FormItem>
              </Col>
              <Col span="8" offset="1">
                <FormItem :label="$t('attribute')">
                  <FilterRulesRef
                    v-if="objItem.mappingType === 'entity'"
                    :disabled="status === 'ENABLED'"
                    v-model="objItem.mappingEntityExpression"
                    :allDataModelsWithAttrs="allEntityType"
                    :rootEntity="clearedEntityType"
                    :needNativeAttr="true"
                    :needAttr="true"
                    :rootEntityFirst="true"
                  ></FilterRulesRef>
                  <Select
                    filterable
                    v-if="objItem.mappingType === 'system_variable'"
                    v-model="objItem.mappingEntityExpression"
                    :disabled="status === 'ENABLED'"
                    @on-open-change="retrieveSystemVariables"
                  >
                    <Option
                      v-for="(item, index) in allSystemVariables"
                      v-if="item.status === 'active'"
                      :value="item.name"
                      :key="index"
                      >{{ item.name }}</Option
                    >
                  </Select>
                  <span v-if="objItem.mappingType === 'context' || objItem.mappingType === 'constant'">N/A</span>

                  <span v-if="objItem.mappingType === 'object'">
                    <div style="width: 50%;display:inline-block;vertical-align: top;">
                      <FilterRulesRef
                        v-model="objItem.mappingEntityExpression"
                        :disabled="status === 'ENABLED'"
                        :allDataModelsWithAttrs="allEntityType"
                        :rootEntity="clearedEntityType"
                        :needNativeAttr="true"
                        :needAttr="true"
                        :rootEntityFirst="true"
                      ></FilterRulesRef>
                    </div>
                    <Button type="primary" size="small">{{ $t('configuration') }}</Button>
                  </span>
                </FormItem>
              </Col>
            </Row>
          </Form>
        </div>
        <transition name="fade">
          <div v-if="objItem.dataType === 'object'">
            <recursive
              :ref="'recursive' + count"
              :status="status"
              :increment="count"
              :treeData="objItem"
              :clearedEntityType="clearedEntityType"
              :allEntityType="allEntityType"
            >
            </recursive>
          </div>
        </transition>
      </li>
    </ul>
  </div>
</template>

<script>
import FilterRulesRef from '../../components/filter-rules-ref.vue'
export default {
  name: 'recursive',
  data () {
    return {
      sensitiveData: [
        {
          value: 'Y',
          label: 'Y'
        },
        {
          value: 'N',
          label: 'N'
        }
      ]
    }
  },
  props: ['treeData', 'clearedEntityType', 'allEntityType', 'increment', 'status'],
  computed: {
    count () {
      var c = this.increment
      return ++c
    },
    stylePadding () {
      return {
        'margin-left': this.count * 24 + 'px',
        'border-left': '1px solid #e8eaec'
      }
    }
  },
  mounted () {},
  methods: {
    hide (index) {
      this.recursiveViewConfig[index]._isShow = !this.recursiveViewConfig[index]._isShow
      this.$set(this.recursiveViewConfig, index, this.recursiveViewConfig[index])
    },
    mappingTypeChange (v, param) {
      if (v === 'entity') {
        param.mappingEntityExpression = null
      }
    }
  },
  components: {
    FilterRulesRef
  }
}
</script>

<style scoped lang="scss">
.test /deep/ .ivu-form-item {
  margin-bottom: 0;
}
ul {
  padding: 0;
  margin: 0;
  list-style: none;
}
.tree-menu {
  height: 100%;
  padding: 0px 12px;
  border-right: 1px solid #e6e9f0;
}

.tree-menu-comm span {
  display: block;
  font-size: 12px;
  position: relative;
}

.tree-menu-comm span strong {
  display: block;
  width: 82%;
  position: relative;
  line-height: 22px;
  padding: 2px 0;
  padding-left: 5px;
  color: #161719;
  font-weight: normal;
}

.tree-title {
  margin-top: 1px;
  cursor: pointer;
}
.tree-border {
  // border-top: 1px solid #9966;
  // border-right: none;
  // border-left: none;
  // border-top: none;
  // padding: 4px 0;
  // margin: 4px 0;
}
.box {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
}
.box .list {
  width: 580px;
}
</style>
