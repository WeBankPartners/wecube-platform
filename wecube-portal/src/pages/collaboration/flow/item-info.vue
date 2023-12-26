<template>
  <div id="itemInfo">
    <Form :label-width="80">
      {{ currentType }}
      <template v-if="currentType === 'node'">
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem :label="$t('name')">
          <Input v-model="itemCustomInfo.label"></Input>
        </FormItem>
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
          <FilterRulesGroup
            :isBatch="itemCustomInfo.taskCategory === 'SDTN'"
            ref="filterRulesGroup"
            @filterRuleChanged="singleFilterRuleChanged"
            :disabled="itemCustomInfo.dynamicBind === 'Y' && itemCustomInfo.associatedNodeId"
            :routineExpression="itemCustomInfo.routineExpression"
            :allEntityType="allEntityType"
            :currentSelectedEntity="currentSelectedEntity"
          >
          </FilterRulesGroup>
        </FormItem>
        <FormItem :label="$t('plugin')">
          <Select v-model="itemCustomInfo.serviceId" @on-open-change="getPlugin" @on-change="changePluginInterfaceList">
            <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
              item.serviceDisplayName
            }}</Option>
          </Select>
        </FormItem>
      </template>
      <Button @click="saveItem">保存</Button>
    </Form>
  </div>
</template>
<script>
import { getAssociatedNodes, getAllDataModels } from '@/api/server.js'
import FilterRulesGroup from '../components/filter-rules-group'
export default {
  data () {
    return {
      currentType: '', // flow、node、edge
      currentSelectedEntity: 'wecmdb:app_instance', // 流程图根
      itemCustomInfo: {
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
      },
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
      allEntityType: [] // 所有模型
    }
  },
  components: {
    FilterRulesGroup
  },
  mounted () {
    this.getAllDataModels()
  },
  methods: {
    showItemInfo (type, data) {
      this.currentType = type
      this.itemCustomInfo = Object.assign(this.itemCustomInfo, data)
      this.$nextTick(() => {
        this.$refs.filterRulesGroup.changeRoutineExpressionItem(this.itemCustomInfo.routineExpression)
      })
      console.log(22, this.itemCustomInfo, data)
    },
    saveItem () {
      this.$emit('sendItemInfo', this.currentType, this.itemCustomInfo)
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
      this.itemCustomInfo.routineExpression = val
    },
    // 获取可选插件
    getPlugin () {
      this.filteredPlugins = []
    }
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
  width: 300px;
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
