<template>
  <span>
    <Button size="small" v-if="!disabled" type="primary" ghost @click.stop.prevent="addFilters">{{
      $t('set_filter_rule')
    }}</Button>
    <Button size="small" v-if="disabled" type="primary" ghost @click.stop.prevent="viewFilters">{{
      $t('query_filter_rule')
    }}</Button>
    <Modal v-model="modelVisable" :title="$t('filter_rule')" @on-ok="okHandler" @on-cancel="cancelHandler">
      <Row style="margin-bottom: 10px" v-for="(rule, index) in currentPathFilterRules" :key="index">
        <Col span="1" style="margin-top: 4px;"
          ><Button
            type="error"
            :disabled="disabled"
            icon="ios-trash-outline"
            @click="deleteFilterRule(index)"
            size="small"
          ></Button
        ></Col>
        <Col span="8" offset="1">
          <Select :disabled="disabled" v-model="rule.attr" @on-change="attrChangeHandler($event, rule)">
            <Option v-for="(attr, index) in currentNodeEntityAttrs" :key="index" :value="attr.name">{{
              attr.name
            }}</Option>
          </Select>
        </Col>
        <Col span="4" offset="1">
          <Select :disabled="disabled" v-model="rule.op" @on-change="opChangeHandler($event, rule)">
            <Option v-for="(op, index) in filterRuleOp" :key="index" :value="op">{{ op }}</Option>
          </Select>
        </Col>
        <Col span="8" offset="1">
          <Input
            :disabled="disabled"
            v-if="!rule.isRef && !(rule.op === 'is' || rule.op === 'isnot')"
            v-model="rule.value"
          ></Input>
          <Select
            :disabled="disabled"
            v-if="rule.isRef && !(rule.op === 'is' || rule.op === 'isnot')"
            v-model="rule.value"
            :multiple="rule.op === 'in' || rule.op === 'like'"
          >
            <Option v-for="(e, i) in rule.enums" :key="i" :value="'@@' + e.id + '@@' + e.key_name">{{
              e.key_name
            }}</Option>
          </Select>
          <span v-if="rule.op === 'is' || rule.op === 'isnot'">NULL</span>
        </Col>
      </Row>
      <Row style="margin-top: 10px">
        <Button type="primary" :disabled="disabled" @click="addRules" long size="small">{{
          $t('add_filter_rule')
        }}</Button>
      </Row>
    </Modal>
  </span>
</template>
<script>
import { getTargetOptions } from '@/api/server'
export default {
  name: 'InterfaceFilterRule',
  data () {
    return {
      filterRuleOp: ['eq', 'neq', 'in', 'like', 'gt', 'lt', 'is', 'isnot'],
      currentPathFilterRules: [],
      currentNodeEntityAttrs: [],
      modelVisable: false
    }
  },
  props: {
    value: {
      default: ''
    },
    disabled: {
      default: false
    },
    rootEntity: {},
    allDataModelsWithAttrs: {}
  },
  watch: {
    rootEntity: {
      handler (val) {
        this.$emit('input', '')
      }
    }
  },
  computed: {
    allEntity () {
      let entity = []
      this.allDataModelsWithAttrs.forEach(_ => {
        if (_.pluginPackageEntities) {
          entity = entity.concat(_.pluginPackageEntities).map(i => {
            let noneFound = i.attributes.find(_ => _.name === 'NONE')
            return {
              ...i,
              attributes: noneFound
                ? i.attributes
                : i.attributes.concat({
                  dataType: 'str',
                  description: 'NONE',
                  entityName: i.name,
                  id: i.id + '__NONE',
                  name: 'NONE',
                  packageName: _.packageName,
                  refAttributeName: null,
                  refEntityName: null,
                  refPackageName: null
                })
            }
          })
        }
      })
      return entity
    }
  },
  methods: {
    async attrChangeHandler (v, rule) {
      const found = this.currentNodeEntityAttrs.find(_ => _.name === v)
      const isRef = found.dataType === 'ref'
      rule.isRef = isRef
      if (isRef) {
        const { status, data } = await getTargetOptions(found.refPackageName, found.refEntityName)
        if (status === 'OK') {
          rule.enums = data
        }
      }
    },
    opChangeHandler (v, rule) {
      const multiple = (v === 'in' || v === 'like') && rule.isRef
      rule.value = multiple ? [] : ''
    },
    viewFilters () {
      this.addFilters()
    },
    addFilters () {
      if (this.rootEntity && this.rootEntity.length > 0) {
        this.currentPathFilterRules = []
        this.currentNodeEntityAttrs = this.allEntity.find(_ => _.name === this.rootEntity).attributes
        console.log(this.value)
        const rules = this.value.match(/[^{]+(?=})/g)
        console.log(rules)
        if (rules) {
          rules.forEach(async r => {
            let enums = []
            let isRef = false
            let [attr, op, value] = r.split(' ')
            const found = this.currentNodeEntityAttrs.find(a => a.name === attr)
            if (found.dataType === 'ref') {
              const { status, data } = await getTargetOptions(found.refPackageName, found.refEntityName)
              if (status === 'OK') {
                enums = data
                isRef = true
              }
            }
            value =
              value.indexOf('[') > -1 && found.dataType === 'ref'
                ? value
                  .slice(1, -1)
                  .split(',')
                  .map(v => v.slice(1, -1))
                : value.indexOf("'") > -1
                  ? value.slice(1, -1)
                  : value
            this.currentPathFilterRules.push({ op, value, enums, isRef, attr })
          })
        }
        this.poptipVisable = false
        this.modelVisable = true
      }
    },
    deleteFilterRule (index) {
      this.currentPathFilterRules.splice(index, 1)
    },
    addRules () {
      this.currentPathFilterRules.push({
        op: '',
        attr: '',
        value: '',
        enums: [],
        isRef: false
      })
    },
    okHandler () {
      this.modelVisable = false
      if (!this.disabled) {
        let rules = ''
        this.currentPathFilterRules
          .filter(r => r.op && r.attr)
          .forEach((rule, index) => {
            const isMultiple = Array.isArray(rule.value)
            let str = ''
            if (isMultiple) {
              str = `{${rule.attr} ${rule.op} [${rule.value.map(v => `'${v}'`)}]}`
            } else if (rule.op === 'is' || rule.op === 'isnot') {
              str = `{${rule.attr} ${rule.op} NULL}`
            } else {
              const noQuotation = rule.op === 'gt' || rule.op === 'lt'
              str = noQuotation
                ? `{${rule.attr} ${rule.op} ${rule.value}}`
                : `{${rule.attr} ${rule.op} '${rule.value}'}`
            }
            rules += str
          })
        this.currentPathFilterRules = []
        this.$emit('input', rules)
      }
    },
    cancelHandler () {
      this.modelVisable = false
      this.currentPathFilterRules = []
    }
  }
}
</script>
<style lang="scss" scoped></style>
