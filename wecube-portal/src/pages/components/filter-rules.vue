<template>
  <div class="filter_rules_contain">
    <Poptip v-model="poptipVisable" placement="bottom">
      <div ref="wecube_cmdb_attr" class="filter_rules_path_contains">
        <span class="path_exp" v-for="(path, index) in pathList" :key="path.pathExp" @click="showPoptip(path, index)">{{
          path.pathExp
        }}</span>
        <Button v-if="pathList.length === 0" type="dashed" icon="md-add-circle" long size="small">请选择</Button>
      </div>
      <div slot="content">
        <div class="filter_rules_path_options">
          <ul>
            <li class v-if="pathList.length > 0" @click="deleteCurrentNode">删除此节点</li>
            <li class v-if="pathList.length > 0" @click="addFilterRuleForCurrentNode">添加过滤规则</li>
          </ul>
          <ul v-for="opt in currentOptiongs" :key="opt.pathExp">
            <li class @click="optClickHandler(opt)">{{ opt.pathExp }}</li>
          </ul>
        </div>
      </div>
    </Poptip>
    <Modal v-model="modelVisable" title="过滤规则" @on-ok="okHandler" @on-cancel="cancelHandler">
      <Row style="margin-bottom: 10px" v-for="(rule, index) in currentPathFilterRules" :key="index">
        <Col span="8">
          <Select v-model="rule.attr" @on-change="attrChangeHandler($event, rule)">
            <Option v-for="(attr, index) in currentNodeEntityAttrs" :key="index" :value="attr.name">{{
              attr.description
            }}</Option>
          </Select>
        </Col>
        <Col span="6" offset="1">
          <Select v-model="rule.op" @on-change="opChangeHandler($event, rule)">
            <Option v-for="(op, index) in filterRuleOp" :key="index" :value="op">{{ op }}</Option>
          </Select>
        </Col>
        <Col span="8" offset="1">
          <Input v-if="!rule.isRef" v-model="rule.value"></Input>
          <Select v-if="rule.isRef" v-model="rule.value" :multiple="rule.op === 'in' || rule.op === 'like'">
            <Option v-for="(e, i) in rule.enums" :key="i" :value="e.key_name">{{ e.key_name }}</Option>
          </Select>
        </Col>
      </Row>
      <Row style="margin-top: 10px">
        <Button type="primary" @click="addRules" long size="small">添加过滤规则</Button>
      </Row>
    </Modal>
  </div>
</template>
<script>
import { getTargetOptions } from '@/api/server'
export default {
  name: 'FilterRules',
  data () {
    return {
      filterRuleOp: ['eq', 'neq', 'in', 'like', 'gt', 'lt'],
      pathList: [],
      currentPathFilterRules: [], // 对象数组
      currentOptiongs: [],
      modelVisable: false,
      poptipVisable: false,
      optionsFilter: '',
      currentNodeIndex: -1,
      currentNode: {},
      currentNodeEntityAttrs: []
    }
  },
  props: {
    value: {
      required: false
    },
    disabled: {},
    allDataModelsWithAttrs: {}
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
    optClickHandler (opt) {
      this.pathList = this.pathList.slice(0, this.currentNodeIndex + 1)
      this.pathList.push(opt)
      this.formatNextCurrentOptions(opt)
      this.currentNodeIndex++
      this.currentNode = opt
    },
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
      const multiple = v === 'in' || v === 'like'
      rule.value = multiple ? [] : ''
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
      let rules = ''
      this.currentPathFilterRules.forEach((rule, index) => {
        const isMultiple = Array.isArray(rule.value)
        const concatStr = index === this.currentPathFilterRules.length - 1 ? '' : 'and'
        rules += isMultiple
          ? `{${rule.attr} ${rule.op} [${rule.value}]}${concatStr}`
          : `{${rule.attr} ${rule.op} ${rule.value}}${concatStr}`
      })
      this.pathList[this.currentNodeIndex].pathExp = this.pathList[this.currentNodeIndex].pathExp.split('{')[0] + rules
      this.currentPathFilterRules = []
    },
    cancelHandler () {
      this.modelVisable = false
      this.currentPathFilterRules = []
    },
    showPoptip (node, index) {
      this.currentNodeIndex = index
      this.currentNode = node
      this.formatNextCurrentOptions(node)
      //   this.poptipVisable = true
    },
    deleteCurrentNode () {
      this.pathList = this.pathList.slice(0, this.currentNodeIndex)
      this.poptipVisable = false
      this.formatNextCurrentOptions(this.currentNode)
    },
    addFilterRuleForCurrentNode () {
      this.currentNodeEntityAttrs = this.allEntity.find(_ => _.name === this.currentNode.entity).attributes
      const rules = this.currentNode.pathExp.match(/[^{]+(?=})/g)
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
          value = value.indexOf('[') > -1 ? value.slice(1, -1).split(',') : value
          this.currentPathFilterRules.push({ op, value, enums, isRef, attr })
        })
      }
      this.poptipVisable = false
      this.modelVisable = true
    },
    formatFirstCurrentOptions () {
      if (this.value && this.value.indexOf(':') > -1) {
      } else {
        this.currentOptiongs = this.allEntity.map(_ => {
          return {
            pkg: _.packageName,
            entity: _.name,
            pathExp: `${_.packageName}:${_.name}`,
            nodeType: 'entity'
          }
        })
      }
    },
    formatNextCurrentOptions (opt) {
      if (this.pathList.length === 0) {
        this.formatFirstCurrentOptions()
        return
      }
      if (opt.nodeType === 'attr') {
        this.currentOptiongs = []
      } else {
        const entity = this.allEntity.find(_ => _.name === opt.entity)
        this.currentOptiongs = entity.attributes.map(attr => {
          const isRef = attr.refEntityName
          return {
            pkg: isRef ? attr.refPackageName : attr.packageName,
            entity: isRef ? attr.refEntityName : attr.name,
            pathExp: isRef ? `.${attr.name}>${attr.refPackageName}:${attr.refEntityName}` : `.${attr.name}`,
            nodeType: isRef ? 'entity' : 'attr'
          }
        })
        this.currentOptiongs = entity.referenceByEntityList
          .map(e => {
            return {
              pkg: e.packageName,
              entity: e.name,
              pathExp: `~${e.packageName}:${e.name}`,
              nodeType: 'entity'
            }
          })
          .concat(this.currentOptiongs)
      }
    }
  },
  mounted () {
    this.formatFirstCurrentOptions()
  }
}
</script>
<style lang="scss">
* {
  padding: 0;
  margin: 0;
  list-style: none;
  font-size: 14px;
}
.filter_rules_path_options {
  width: 100%;
  z-index: 3000;
  background: white;
  max-height: 200px;
  overflow: auto;
}
.filter_rules_contain {
  //   margin-top: -5px;
}
.filter_rules_contain .ivu-poptip {
  width: 100%;
}
.filter_rules_contain .ivu-poptip .ivu-poptip-rel {
  width: 100%;
}
.filter_rules_path_contains {
  width: 100%;
  .path_exp {
    //   text-decoration:underline;
    word-wrap: break-word;
    word-break: break-all;
    &:hover {
      color: rgb(58, 160, 219);
      cursor: pointer;
    }
  }
}
.filter_rules_path_options ul {
  width: 100%;
  border-radius: 3px;
}
.ul-li-selected {
  color: rgb(6, 130, 231);
}
.filter_rules_path_options ul li {
  width: 100%;
  height: 25px;
  line-height: 25px;
  cursor: pointer;
  &:hover {
    background-color: rgb(227, 231, 235);
  }
}
</style>
