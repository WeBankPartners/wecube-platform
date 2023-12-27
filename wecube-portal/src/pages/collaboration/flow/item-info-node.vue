<template>
  <div id="itemInfo">
    <Form :label-width="80">
      node
      <template>
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem :label="$t('name')">
          <Input v-model="itemCustomInfo.label"></Input>
        </FormItem>
        <template v-if="['human', 'automatic', 'data'].includes(itemCustomInfo.nodeType)">
          <FormItem :label="$t('description')">
            <Input v-model="itemCustomInfo.description"></Input>
          </FormItem>
          <FormItem :label="$t('timeout')">
            <Select v-model="itemCustomInfo.timeoutExpression">
              <Option v-for="(item, index) in timeSelection" :value="item.mins" :key="index">{{ item.label }} </Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('dynamic_bind')">
            <Select v-model="itemCustomInfo.dynamicBind">
              <Option v-for="item in yOn" :value="item" :key="item">{{ item }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('bind_node')">
            <Select
              v-model="itemCustomInfo.associatedNodeId"
              @on-change="changeAssociatedNode"
              @on-open-change="getAssociatedNodes"
              clearable
              :disabled="itemCustomInfo.dynamicBind !== 'Y'"
            >
              <Option v-for="(i, index) in associatedNodes" :value="i.nodeId" :key="index">{{ i.nodeName }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('pre_check')">
            <Select v-model="itemCustomInfo.preCheck">
              <Option v-for="item in yOn" :value="item" :key="item">{{ item }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('locate_rules')">
            <ItemFilterRulesGroup
              :isBatch="itemCustomInfo.taskCategory === 'SDTN'"
              ref="filterRulesGroupRef"
              :disabled="itemCustomInfo.dynamicBind === 'Y' && itemCustomInfo.associatedNodeId"
              :routineExpression="itemCustomInfo.routineExpression"
              :allEntityType="allEntityType"
              :currentSelectedEntity="currentSelectedEntity"
            >
            </ItemFilterRulesGroup>
          </FormItem>
          <FormItem :label="$t('plugin')">
            <Select
              v-model="itemCustomInfo.serviceId"
              @on-open-change="getPlugin"
              @on-change="changePluginInterfaceList"
            >
              <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                item.serviceDisplayName
              }}</Option>
            </Select>
          </FormItem>
        </template>
      </template>
      <Button @click="saveItem" type="primary" style="float: right">保存</Button>
    </Form>
  </div>
</template>
<script>
import { getAssociatedNodes, getAllDataModels } from '@/api/server.js'
import ItemFilterRulesGroup from './item-filter-rules-group.vue'
const defaultNode = {
  procDefId: '', // 对应编排信息
  procDefKey: '', // 对应编排信息
  id: '', // 节点id  nodeId  ----OK
  label: '', // 节点名称 nodeName ----OK
  taskCategory: '', // 节点类型，SSTN自动节点 SUTN人工节点 SDTN数据写入节点 ----OK
  timeoutExpression: '30', // 超时时间 ----OK
  description: null, // 描述说明  ----OK
  dynamicBind: 'N', // 动态绑定
  associatedNodeId: null, // 动态绑定关联节点id
  nodeType: '', // 节点类型，对应节点原始类型（start、end……）
  routineExpression: 'wecmdb:app_instance', // 对应节点中的定位规则
  routineRaw: null, // 还未知作用
  serviceId: null, // 选择的插件id
  serviceName: null, // 选择的插件名称
  preCheck: 'N', // 高危检测
  paramInfos: [] // 存在插件注册处需要填写的字段
}
export default {
  data () {
    return {
      currentSelectedEntity: 'wecmdb:app_instance', // 流程图根
      itemCustomInfo: JSON.parse(JSON.stringify(defaultNode)),
      // 超时时间选项
      timeSelection: [
        {
          mins: '5',
          label: '5 ' + this.$t('mins')
        },
        {
          mins: '10',
          label: '10 ' + this.$t('mins')
        },
        {
          mins: '30',
          label: '30 ' + this.$t('mins')
        },
        {
          mins: '60',
          label: '1 ' + this.$t('hours')
        },
        {
          mins: '720',
          label: '12 ' + this.$t('hours')
        },
        {
          mins: '1440',
          label: '1 ' + this.$t('days')
        },
        {
          mins: '2880',
          label: '2 ' + this.$t('days')
        },
        {
          mins: '4320',
          label: '3 ' + this.$t('days')
        }
      ],
      yOn: ['Y', 'N'],
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
      console.log(11, defaultNode)
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, data)))
      this.$nextTick(() => {
        this.$refs.filterRulesGroupRef &&
          this.$refs.filterRulesGroupRef.changeRoutineExpressionItem(this.itemCustomInfo.routineExpression)
      })
      console.log(22, this.itemCustomInfo, data)
    },
    saveItem () {
      const routineExpressionItem = this.$refs.filterRulesGroupRef.routineExpressionItem
      this.itemCustomInfo.routineExpression = routineExpressionItem.reduce((tmp, item, index) => {
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
      this.itemCustomInfo.routineExpression = val
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
  top: 145px;
  right: 32px;
  bottom: 0;
  z-index: 10;
  width: 400px;
  height: 86%;
  background: #f8f8f8;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
}
.ivu-form-item {
  margin-bottom: 0;
}
</style>
