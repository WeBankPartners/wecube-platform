<template>
  <div class="filter_rules_contain" :class="disabled ? 'disabled-filter' : ''" ref="filter_rules_contain">
    <Poptip v-model="poptipVisable" placement="bottom">
      <div ref="wecube_cmdb_attr" class="filter_rules_path_contains">
        <span
          class="path_exp"
          :class="index % 2 === 1 ? 'odd_span' : 'even_span'"
          v-for="(path, index) in pathList"
          :key="path.pathExp"
          @click="showPoptip(path, index)"
          >{{ path.pathExp.replace(/@@[0-9A-Za-z_]*@@/g, '') }}</span
        >
        <Button
          :class="disabled ? 'disabled-filter' : ''"
          v-if="pathList.length > 0"
          type="dashed"
          icon="md-copy"
          @click.stop.prevent="copyPathExp"
          size="small"
          >{{ $t('copy') }}</Button
        >
        <Button v-if="pathList.length === 0" class="arrow-icon" icon="ios-arrow-down" long size="small"></Button>
      </div>
      <div slot="content">
        <div v-show="!disabled" ref="filter_rules_path_options" class="filter_rules_path_options">
          <ul>
            <li id="paste" style="margin-bottom: 5px;" v-if="pathList.length === 0">
              <input
                class="paste_input"
                v-model="pasteValue"
                @input="inputHandler"
                :placeholder="$t('please_paste_here')"
                @paste="pastePathExp($event)"
              />
            </li>
            <li class v-if="pathList.length > 0" style="color: #ed4014" @click="deleteCurrentNode">
              {{ $t('delete_node') }}
            </li>
            <li
              class
              style="color: #2d8cf0"
              v-if="pathList.length > 0 && currentNode.nodeType === 'entity'"
              @click="addFilterRuleForCurrentNode"
            >
              {{ $t('add_filter_rule') }}
            </li>
            <li>
              <Input prefix="ios-search" v-model="filterString" size="small" style="width:100%" />
            </li>
          </ul>
          <hr style="margin-top:5px;" />
          <div style="max-height: 145px;overflow: auto;margin-top:5px;">
            <ul
              v-if="!needNativeAttr"
              v-for="opt in filterCurrentLeafOptiongs"
              :key="opt.pathExp + Math.random() * 1000"
            >
              <li style="color:rgb(49, 104, 4)" @click="optClickHandler(opt, 'leaf')">{{ opt.pathExp }}</li>
            </ul>
            <ul v-for="opt in filterCurrentRefOptiongs" :key="opt.pathExp + Math.random() * 1000">
              <li style="color:rgb(64, 141, 218)" @click="optClickHandler(opt, 'up')">{{ opt.pathExp }}</li>
            </ul>
            <ul v-for="opt in filterCurrentOptiongs" :key="opt.pathExp + Math.random() * 1000">
              <li style="color:rgb(211, 82, 32)" @click="optClickHandler(opt, 'down')">{{ opt.pathExp }}</li>
            </ul>
          </div>
        </div>
      </div>
    </Poptip>
    <Modal v-model="modelVisable" :title="$t('filter_rule')" @on-ok="okHandler" @on-cancel="cancelHandler">
      <Row style="margin-bottom: 10px" v-for="(rule, index) in currentPathFilterRules" :key="index">
        <Col span="1" style="margin-top: 4px;"
          ><Button type="error" icon="ios-trash-outline" @click="deleteFilterRule(index)" size="small"></Button
        ></Col>
        <Col span="8" offset="1">
          <Select filterable v-model="rule.attr" @on-change="attrChangeHandler($event, rule)">
            <Option v-for="(attr, index) in currentNodeEntityAttrs" :key="index" :value="attr.name">{{
              attr.name
            }}</Option>
          </Select>
        </Col>
        <Col span="4" offset="1">
          <Select filterable v-model="rule.op" @on-change="opChangeHandler($event, rule)">
            <Option v-for="(op, index) in filterRuleOp" :key="index" :value="op">{{ op }}</Option>
          </Select>
        </Col>
        <Col span="8" offset="1">
          <Input v-if="!rule.isRef && !(rule.op === 'is' || rule.op === 'isnot')" v-model="rule.value"></Input>
          <Select
            filterable
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
        <Button type="primary" @click="addRules" long size="small">{{ $t('add_filter_rule') }}</Button>
      </Row>
    </Modal>
  </div>
</template>
<script>
import { getTargetOptions, getEntityRefsByPkgNameAndEntityName } from '@/api/server'
export default {
  name: 'FilterRules',
  data () {
    return {
      filterRuleOp: ['eq', 'neq', 'in', 'like', 'gt', 'lt', 'is', 'isnot'],
      pathList: [],
      currentPathFilterRules: [],
      currentOptiongs: [],
      currentRefOptiongs: [],
      currentLeafOptiongs: [],
      filterCurrentOptiongs: [],
      filterCurrentRefOptiongs: [],
      filterCurrentLeafOptiongs: [],
      modelVisable: false,
      poptipVisable: false,
      optionsFilter: '',
      currentNodeIndex: -1,
      currentNode: {},
      currentNodeEntityAttrs: [],
      pasteValue: '',
      filterString: '',
      lastSelectType: '' // 标记最后选中项内容
    }
  },
  props: {
    rootEntity: {
      required: false
    },
    value: {
      required: false
    },
    needAttr: {
      type: Boolean,
      default: false
    },
    needNativeAttr: {
      type: Boolean,
      default: false
    },
    disabled: {},
    allDataModelsWithAttrs: {},
    rootEntityFirst: {
      required: false,
      default: false
    }
  },
  watch: {
    filterString: {
      handler (val) {
        this.filterCurrentOptiongs = this.currentOptiongs.filter(opt => opt.pathExp.indexOf(val) > -1)
        this.filterCurrentRefOptiongs = this.currentRefOptiongs.filter(opt => opt.pathExp.indexOf(val) > -1)
        this.filterCurrentLeafOptiongs = this.currentLeafOptiongs.filter(opt => opt.pathExp.indexOf(val) > -1)
      }
    },
    value: {
      handler (val) {
        // if (val === this.fullPathExp) return
        this.formatFirstCurrentOptions()
      }
    },
    allDataModelsWithAttrs: {
      handler (val) {
        this.formatCurrentOptions()
      }
    },
    rootEntity: {
      handler (val) {
        this.restorePathExp(val)
        this.$emit('input', this.fullPathExp)
        this.$emit('change', this.fullPathExp)
      }
    }
  },
  computed: {
    allEntity () {
      let entity = []
      this.allDataModelsWithAttrs.forEach(_ => {
        if (_.entities) {
          entity = entity.concat(_.entities).map(i => {
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
    },
    fullPathExp () {
      return this.pathList.map(path => path.pathExp).join('')
    }
  },
  methods: {
    copyPathExp () {
      let inputElement = document.createElement('input')
      inputElement.value = this.fullPathExp
      document.body.appendChild(inputElement)
      inputElement.select()
      document.execCommand('Copy')
      this.$Notice.success({
        title: 'Success',
        desc: this.$t('copy_success')
      })
      inputElement.remove()
    },
    inputHandler (v) {
      this.pasteValue = ''
    },
    pastePathExp (e) {
      let clipboardData = e.clipboardData
      if (!clipboardData) {
        clipboardData = e.originalEvent.clipboardData
      }
      let data = clipboardData.getData('Text')
      this.restorePathExp(data)
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
    },
    restorePathExp (PathExp) {
      this.pathList = []
      // eslint-disable-next-line no-useless-escape
      const pathList = PathExp.split(/[.~]+(?=[^\}]*(\{|$))/).filter(p => p.length > 1)
      let path = {}
      pathList.forEach((_, i) => {
        const ifEntity = _.indexOf(':')
        if (ifEntity > 0) {
          const isBy = _.indexOf(')')
          const current = _.split(':')
          const ruleIndex = current[1].indexOf('{')
          if (isBy > 0) {
            path = {
              entity: ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1],
              pkg: current[0].split(')')[1],
              pathExp: `~${_}`,
              nodeType: 'entity'
            }
          } else {
            path = {
              entity: ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1],
              pkg: _.match(/[^>]+(?=:)/)[0],
              pathExp: `${i > 0 ? '.' : ''}${_}`,
              nodeType: 'entity'
            }
          }
        } else {
          const previous = pathList[i - 1]
          const previousSplit = previous.split(':')
          const ruleIndex = previousSplit[1].indexOf('{')
          const isBy = previous.indexOf(')')
          path = {
            entity: ruleIndex > 0 ? previousSplit[1].slice(0, ruleIndex) : previousSplit[1],
            pkg: isBy > 0 ? previousSplit[0].split(')')[1] : previousSplit[0],
            pathExp: `.${_}`,
            nodeType: 'attr'
          }
        }
        this.pathList.push(path)
      })
      // this.$emit('input', this.fullPathExp)
      // this.$emit('change', this.fullPathExp)
      this.poptipVisable = false
    },
    optClickHandler (opt, lastSelectType) {
      this.lastSelectType = lastSelectType
      this.pathList = this.pathList.slice(0, this.currentNodeIndex + 1)
      this.pathList.push(opt)
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
      this.formatNextCurrentOptions(opt)
      this.currentNodeIndex++
      this.currentNode = opt
      this.poptipVisable = this.needAttr || this.needNativeAttr
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
      const multiple = (v === 'in' || v === 'like') && rule.isRef
      rule.value = multiple ? [] : ''
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
            str = noQuotation ? `{${rule.attr} ${rule.op} ${rule.value}}` : `{${rule.attr} ${rule.op} '${rule.value}'}`
          }
          rules += str
        })
      this.pathList[this.currentNodeIndex].pathExp = this.pathList[this.currentNodeIndex].pathExp.split('{')[0] + rules
      this.currentPathFilterRules = []
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
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
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
      this.formatNextCurrentOptions(this.currentNode)
    },
    addFilterRuleForCurrentNode () {
      if (!this.currentNode) return
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
    },
    formatCurrentOptions () {
      let compare = (a, b) => {
        if (a.pathExp < b.pathExp) {
          return -1
        }
        if (a.pathExp > b.pathExp) {
          return 1
        }
        return 0
      }
      this.currentOptiongs = this.allEntity
        .map(_ => {
          return {
            pkg: _.packageName,
            entity: _.name,
            pathExp: `${_.packageName}:${_.name}`,
            nodeType: 'entity'
          }
        })
        .sort(compare)
      this.filterCurrentOptiongs = this.currentOptiongs
      this.filterCurrentRefOptiongs = this.currentRefOptiongs
      this.filterCurrentLeafOptiongs = this.currentLeafOptiongs
    },
    formatFirstCurrentOptions () {
      this.pathList = []
      if (this.value && this.value.indexOf(':') > -1) {
        this.restorePathExp(this.value)
      } else {
        this.formatCurrentOptions()
      }
    },
    async formatNextCurrentOptions (opt) {
      if (this.pathList.length === 0) {
        this.currentOptiongs = []
        this.currentRefOptiongs = []
        this.currentLeafOptiongs = []
        this.formatCurrentOptions()
        return
      }
      if (opt.nodeType === 'leaf' || !this.needAttr || opt.nodeType === 'attr') {
        this.currentOptiongs = []
        this.currentRefOptiongs = []
        this.currentLeafOptiongs = []
      } else {
        const { status, data } = await getEntityRefsByPkgNameAndEntityName(opt.pkg, opt.entity)
        if (status === 'OK') {
          this.currentRefOptiongs = data.referenceByEntityList.map(e => {
            return {
              pkg: e.packageName,
              entity: e.name,
              pathExp: `~(${e.relatedAttribute.name})${e.packageName}:${e.name}`,
              nodeType: 'entity'
            }
          })
          this.currentOptiongs = data.referenceToEntityList.map(e => {
            return {
              pkg: e.relatedAttribute.refPackageName,
              entity: e.relatedAttribute.refEntityName,
              pathExp: `.${e.relatedAttribute.name}>${e.relatedAttribute.refPackageName}:${e.relatedAttribute.refEntityName}`,
              nodeType: 'entity'
            }
          })
          this.currentLeafOptiongs = []
          if (this.needNativeAttr) {
            const foundEntity = this.allEntity.find(i => i.packageName === opt.pkg && i.name === opt.entity)
            const attrOption = foundEntity.attributes
              // .filter(attr => attr.dataType !== 'ref')
              .map(a => {
                return {
                  pkg: a.packageName,
                  entity: a.entityName,
                  pathExp: `.${a.name}`,
                  nodeType: 'attr'
                }
              })
            this.currentOptiongs = this.currentOptiongs.concat(attrOption)
          } else {
            let referenceToEntityList = []
            data.leafEntityList.referenceToEntityList.forEach(e => {
              const index = referenceToEntityList.indexOf(e.filterRule)
              if (index < 0) {
                const found = data.referenceToEntityList.filter(
                  _ => `${_.packageName}:${_.name}` === `${e.packageName}:${e.entityName}`
                )
                found.forEach(j => {
                  this.currentLeafOptiongs.push({
                    pkg: e.packageName,
                    entity: e.name,
                    pathExp: `.${j.relatedAttribute.name}>${e.filterRule}`,
                    nodeType: 'entity'
                  })
                })
                referenceToEntityList.push(e.filterRule)
              }
            })
            let referenceByEntityList = []
            data.leafEntityList.referenceByEntityList.forEach(e => {
              const index = referenceByEntityList.indexOf(e.filterRule)
              if (index < 0) {
                const found = data.referenceByEntityList.filter(
                  _ => `${_.packageName}:${_.name}` === `${e.packageName}:${e.entityName}`
                )
                found.forEach(j => {
                  this.currentLeafOptiongs.push({
                    pkg: e.packageName,
                    entity: e.name,
                    pathExp: `~(${j.relatedAttribute.name})${e.filterRule}`,
                    nodeType: 'leaf'
                  })
                })
                referenceByEntityList.push(e.filterRule)
              }
            })
          }
        }
      }
      this.filterString = ''
      this.filterCurrentOptiongs = this.currentOptiongs
      this.filterCurrentRefOptiongs = this.currentRefOptiongs
      this.filterCurrentLeafOptiongs = this.currentLeafOptiongs
    }
  },
  mounted () {
    // this.bindPastePathExp()
    if (!this.value && this.rootEntity) {
      this.restorePathExp(this.rootEntity)
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
      return
    }
    this.formatFirstCurrentOptions()
    if (this.rootEntityFirst && !this.rootEntity) {
      this.restorePathExp('')
      this.$emit('input', this.fullPathExp)
      this.$emit('change', this.fullPathExp)
    }
    this.$refs.filter_rules_path_options.style.width = this.$refs.filter_rules_contain.offsetWidth - 32 + 'px'
  }
}
</script>
<style lang="scss">
* {
  padding: 0;
  margin: 0;
  list-style: none;
  // font-size: 14px;
}
.paste_input {
  width: 100%;
  border: 1px solid #dcdee2;
  border-radius: 4px;
  margin-bottom: 10px;
  outline: none;
}
.disabled-filter {
  background-color: #f3f3f3 !important;
  span {
    color: #cccccc !important;
  }
}
.filter_rules_path_options {
  width: 100%;
  z-index: 3000;
  background: white;
  min-height: 10px;
  max-height: 250px;
  overflow: auto;
}
.filter_rules_contain .ivu-poptip {
  width: 100%;
}
.filter_rules_contain .ivu-poptip .ivu-poptip-rel {
  width: 100%;
}
.filter_rules_path_contains {
  width: 100%;
  border: 1px solid #dcdee2;
  border-radius: 4px;
  padding-left: 10px;
  padding-right: 10px;
  position: relative;
  min-height: 32px;
  &:hover {
    border-color: rgb(39, 166, 240);
    cursor: pointer;
  }
  .path_exp {
    // text-decoration:underline;
    word-wrap: break-word;
    word-break: break-all;
    &:hover {
      color: rgb(39, 166, 240);
      cursor: pointer;
    }
  }
  .odd_span {
    color: rgb(33, 42, 119);
  }
  .even_span {
    color: rgb(39, 25, 17);
  }
  button {
    border: none;
  }
  .arrow-icon {
    position: absolute;
    right: 0;
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
    background-color: rgb(220, 226, 231); //  rgb(139, 137, 6)  rgb(44, 85, 5)
  }
}
</style>
