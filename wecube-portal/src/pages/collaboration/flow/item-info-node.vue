<template>
  <div id="itemInfo">
    <Icon class="hide-panal" type="md-exit" size="24" @click="hideItem" />
    <div class="panal-name">节点属性：</div>
    <Collapse simple v-model="opendPanel">
      <Panel name="1">
        基础信息
        <template slot="content">
          <Form :label-width="80">
            <FormItem label="ID">
              <Input disabled v-model="itemCustomInfo.id"></Input>
            </FormItem>
            <FormItem :label="$t('name')">
              <Input v-model="itemCustomInfo.label"></Input>
            </FormItem>
            <FormItem :label="$t('node_type')" v-if="itemCustomInfo.customAttrs.taskCategory">
              <Input v-model="itemCustomInfo.customAttrs.taskCategory" disabled></Input>
            </FormItem>
          </Form>
        </template>
      </Panel>
      <Panel
        name="2"
        v-if="
          itemCustomInfo.customAttrs && ['human', 'automatic', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
        "
      >
        执行控制
        <template slot="content">
          <Form :label-width="80">
            <FormItem :label="$t('timeout')">
              <Select v-model="itemCustomInfo.customAttrs.timeout">
                <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index"
                  >{{ item.label }}
                </Option>
              </Select>
            </FormItem>
            <FormItem
              :label="$t('pre_check')"
              v-if="itemCustomInfo.customAttrs && !['data'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <i-switch v-model="itemCustomInfo.customAttrs.riskCheck" />
            </FormItem>
          </Form>
        </template>
      </Panel>
      <Panel
        name="3"
        v-if="
          itemCustomInfo.customAttrs && ['human', 'automatic', 'data'].includes(itemCustomInfo.customAttrs.nodeType)
        "
      >
        数据绑定
        <template slot="content">
          <Form :label-width="80">
            <FormItem
              :label="$t('dynamic_bind')"
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <i-switch v-model="itemCustomInfo.customAttrs.dynamicBind" />
            </FormItem>
            <FormItem
              :label="$t('bind_node')"
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
            >
              <Select
                v-model="itemCustomInfo.customAttrs.bindNodeId"
                @on-change="changeAssociatedNode"
                @on-open-change="getAssociatedNodes"
                clearable
                :disabled="!itemCustomInfo.customAttrs.dynamicBind"
              >
                <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{ i.nodeName }}</Option>
              </Select>
            </FormItem>
            <FormItem :label="$t('locate_rules')">
              <ItemFilterRulesGroup
                :isBatch="itemCustomInfo.customAttrs.taskCategory === 'SDTN'"
                ref="filterRulesGroupRef"
                :disabled="itemCustomInfo.customAttrs.dynamicBind && itemCustomInfo.customAttrs.bindNodeId"
                :routineExpression="itemCustomInfo.customAttrs.routineExpression"
                :allEntityType="allEntityType"
                :currentSelectedEntity="currentSelectedEntity"
              >
              </ItemFilterRulesGroup>
            </FormItem>
          </Form>
        </template>
      </Panel>
      <Panel
        name="4"
        v-if="itemCustomInfo.customAttrs && ['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
      >
        调用插件服务
        <template slot="content">
          <Form :label-width="80">
            <FormItem
              v-if="['human', 'automatic'].includes(itemCustomInfo.customAttrs.nodeType)"
              :label="$t('plugin')"
              style="margin-top: 8px"
            >
              <Select
                v-model="itemCustomInfo.customAttrs.serviceId"
                @on-open-change="getPlugin"
                @on-change="changePluginInterfaceList"
              >
                <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                  item.serviceDisplayName
                }}</Option>
              </Select>
            </FormItem>
          </Form>
        </template>
      </Panel>
    </Collapse>
    <div style="position: fixed; bottom: 20px; right: 400px">
      <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
      <Button @click="hideItem">{{ $t('cancel') }}</Button>
    </div>
  </div>
</template>
<script>
import { getAssociatedNodes, getAllDataModels } from '@/api/server.js'
import ItemFilterRulesGroup from './item-filter-rules-group.vue'
export default {
  data () {
    return {
      opendPanel: ['1', '3', '4'],
      currentSelectedEntity: 'wecmdb:app_instance', // 流程图根
      itemCustomInfo: {
        customAttrs: {
          taskCategory: ''
        }
      },
      // 超时时间选项
      timeSelection: [
        {
          mins: 5,
          label: '5 ' + this.$t('mins')
        },
        {
          mins: 10,
          label: '10 ' + this.$t('mins')
        },
        {
          mins: 30,
          label: '30 ' + this.$t('mins')
        },
        {
          mins: 60,
          label: '1 ' + this.$t('hours')
        },
        {
          mins: 720,
          label: '12 ' + this.$t('hours')
        },
        {
          mins: 1440,
          label: '1 ' + this.$t('days')
        },
        {
          mins: 2880,
          label: '2 ' + this.$t('days')
        },
        {
          mins: 4320,
          label: '3 ' + this.$t('days')
        }
      ],
      associatedNodes: [], // 可选择的前序节点
      allEntityType: [], // 所有模型
      filteredPlugins: [] // 可选择的插件函数，根据定位规则获取
    }
  },
  components: {
    ItemFilterRulesGroup
  },
  mounted () {
    this.getAllDataModels()
  },
  methods: {
    showItemInfo (data) {
      const defaultNode = {
        id: '', // 节点id  nodeId
        label: '', // 节点名称 nodeName
        customAttrs: {
          procDefId: '', // 对应编排信息
          procDefKey: '', // 对应编排信息
          timeout: 30, // 超时时间
          description: null, // 描述说明
          dynamicBind: false, // 动态绑定
          bindNodeId: null, // 动态绑定关联节点id
          nodeType: '', // 节点类型，对应节点原始类型（start、end……
          routineExpression: 'wecmdb:app_instance', // 对应节点中的定位规则
          routineRaw: null, // 还未知作用
          serviceId: null, // 选择的插件id
          serviceName: null, // 选择的插件名称
          riskCheck: true, // 高危检测
          paramInfos: [] // 存在插件注册处需要填写的字段
        }
      }
      const tmpData = JSON.parse(JSON.stringify(data))
      const customAttrs = tmpData.customAttrs || []
      delete tmpData.customAttrs
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(customAttrs)
      keys.forEach(k => {
        this.itemCustomInfo.customAttrs[k] = customAttrs[k]
      })
      console.log(11, this.itemCustomInfo)
      // this.$nextTick(() => {
      //   this.$refs.filterRulesGroupRef &&
      //     this.$refs.filterRulesGroupRef.changeRoutineExpressionItem(this.itemCustomInfo.customAttrs.routineExpression)
      // })
    },
    saveItem () {
      const routineExpressionItem = this.$refs.filterRulesGroupRef.routineExpressionItem
      this.itemCustomInfo.customAttrs.routineExpression = routineExpressionItem.reduce((tmp, item, index) => {
        return (
          tmp +
          item.routineExpression +
          '#DMEOP#' +
          item.operate +
          (index === routineExpressionItem.length - 1 ? '' : '#DME#')
        )
      }, '')
      this.$emit('sendItemInfo', this.itemCustomInfo)
    },
    hideItem () {
      this.$emit('hideItemInfo')
    },
    // 获取当前节点的前序节点
    async getAssociatedNodes () {
      let params = {
        taskNodeId: '',
        procDefData: ''
      }
      let { status, data } = await getAssociatedNodes(params)
      if (status === 'OK') {
        this.associatedNodes = data
      }
    },
    // 更新关联节点的响应
    changeAssociatedNode () {},

    // #region 定位规则

    // 获取所有根数据
    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    // 定位规则回传
    singleFilterRuleChanged (val) {
      console.log(44, val)
      this.itemCustomInfo.customAttrs.routineExpression = val
    },
    // 获取可选插件
    getPlugin () {
      this.filteredPlugins = []
    },
    // 改变插件时的响应
    changePluginInterfaceList () {}
    // #endregion
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: fixed;
  top: 139px;
  right: 32px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 86%;
  background: white;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
}
.ivu-form-item {
  margin-bottom: 0;
}

.hide-panal {
  position: fixed;
  top: 400px;
  right: 520px;
  cursor: pointer;
}

.panal-name {
  padding: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eaec;
  font-weight: bold;
}

.ivu-collapse {
  border: none !important;
}
.ivu-collapse > .ivu-collapse-item {
  border-top: none !important;
}
</style>
