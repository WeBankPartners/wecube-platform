<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem">
      <Icon type="ios-arrow-dropright" size="28" />
    </div>
    <div class="panal-name">编排属性：</div>
    <Form
      :label-width="120"
      ref="formValidate"
      :model="itemCustomInfo"
      :rules="ruleValidate"
      style="padding-right: 12px"
    >
      <FormItem label="编排ID">
        <Input disabled v-model="itemCustomInfo.id"></Input>
      </FormItem>
      <FormItem label="编排名称">
        <Input
          v-model="itemCustomInfo.label"
          @on-change="paramsChanged"
          :disable="!itemCustomInfo.enableModifyName"
        ></Input>
        <span style="position: absolute; left: 320px; top: 2px">{{ itemCustomInfo.label.length }}/30</span>
        <span v-if="itemCustomInfo.label.length > 30" style="color: red">编排名称不能大于30字符</span>
      </FormItem>
      <FormItem label="版本" prop="version">
        <InputNumber :min="1" disabled v-model="itemCustomInfo.version" style="width: 100%"></InputNumber>
      </FormItem>
      <FormItem label="对象类型" prop="rootEntity">
        <FilterRules
          @change="onEntitySelect"
          v-model="itemCustomInfo.rootEntity"
          :allDataModelsWithAttrs="allEntityType"
          style="width: 100%"
        ></FilterRules>
      </FormItem>
      <!-- @on-change="paramsChanged" -->
      <FormItem label="授权插件" style="margin-top: 22px">
        <Select v-model="itemCustomInfo.authPlugins" filterable multiple>
          <Option v-for="item in authPluginList" :value="item" :key="item">{{ item }} </Option>
        </Select>
      </FormItem>
      <FormItem label="分组" prop="scene">
        <Input v-model="itemCustomInfo.scene" @on-change="paramsChanged"></Input>
      </FormItem>
      <FormItem label="冲突检测">
        <i-switch v-model="itemCustomInfo.conflictCheck" @on-change="paramsChanged" />
      </FormItem>
      <FormItem label="描述">
        <Input v-model="itemCustomInfo.tags" @on-change="paramsChanged" type="textarea" :rows="4"></Input>
        <span style="position: relative; left: 320px; top: -28px">{{ itemCustomInfo.tags.length }}/200</span>
        <span v-if="itemCustomInfo.tags.length > 200" style="color: red">描述不能大于200字符</span>
      </FormItem>
      <div style="position: absolute; bottom: 20px; right: 280px; width: 200px">
        <Button v-if="editFlow !== 'false'" @click="saveItem" type="primary">{{ $t('save') }}</Button>
        <Button @click="hideItem">{{ $t('cancel') }}</Button>
      </div>
    </Form>
  </div>
</template>
<script>
import FilterRules from '@/pages/components/filter-rules.vue'
import { getAllDataModels, getPluginList } from '@/api/server.js'
export default {
  components: {
    FilterRules
  },
  data () {
    return {
      editFlow: true, // 在查看时隐藏按钮
      isParmasChanged: false, // 参数变化标志位，控制右侧panel显示逻辑
      itemCustomInfo: {
        id: '',
        label: '', // 编排名称
        version: 1, // 版本
        rootEntity: '', // 操作对象
        scene: '', // 使用场景，请求、发布、其他
        authPlugins: [], // 授权插件列表，taskman/monitor
        tags: '', // 标签
        conflictCheck: true, // 冲突检测
        permissionToRole: {
          MGMT: [], // 属主角色
          USE: [] // 使用角色
        }
      },
      ruleValidate: {
        rootEntity: [{ required: true, message: 'rootEntity at least one hobby', trigger: 'change' }]
      },
      allEntityType: [], // 系统中所有根CI
      authPluginList: [] // 待授权插件列表
    }
  },
  mounted () {},
  methods: {
    async showItemInfo (data, editFlow) {
      this.editFlow = editFlow
      this.isParmasChanged = false
      // 获取所有根CI类型
      this.getAllDataModels()
      await this.pluginList()
      const defaultNode = {
        id: '',
        label: '', // 编排名称
        version: 1, // 版本
        rootEntity: '', // 操作对象
        scene: '', // 使用场景，请求、发布、其他
        authPlugins: [], // 授权插件列表，taskman/monitor
        tags: '', // 标签
        conflictCheck: true, // 冲突检测
        permissionToRole: {
          MGMT: [], // 属主角色
          USE: [] // 使用角色
        }
      }
      const tmpData = JSON.parse(JSON.stringify(data))
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(defaultNode)
      keys.forEach(k => {
        this.itemCustomInfo[k] = tmpData[k]
      })
      this.itemCustomInfo.version = Number(this.itemCustomInfo.version) || 1
    },
    saveItem () {
      this.$refs['formValidate'].validate(valid => {
        if (valid) {
          let finalData = JSON.parse(JSON.stringify(this.itemCustomInfo))
          finalData.version += ''
          finalData.name = finalData.label
          this.$emit('sendItemInfo', finalData)
        }
      })
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: this.$t('confirm_discarding_changes') + 'canvas',
          content: this.$t('params_edit_confirm'),
          'z-index': 1000000,
          onOk: async () => {
            this.$refs['formValidate'].resetFields()
            this.$emit('hideItemInfo')
          },
          onCancel: () => {}
        })
      } else {
        this.$emit('hideItemInfo')
      }
    },

    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    onEntitySelect (v) {
      this.itemCustomInfo.rootEntity = v || ''
      this.paramsChanged()
    },

    // 获取插件列表
    async pluginList () {
      let { data, status } = await getPluginList()
      if (status === 'OK') {
        this.authPluginList = data
      }
    },
    // 监听参数变化
    paramsChanged () {
      this.isParmasChanged = true
    }
  }
}
</script>
<style lang="scss" scoped>
#itemInfo {
  position: absolute;
  top: 134px;
  right: 13px;
  bottom: 0;
  z-index: 10;
  width: 500px;
  height: 90%;
  background: white;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
  overflow: auto;
  height: calc(100vh - 160px);
}
.ivu-form-item {
  margin-bottom: 12px;
}

.panal-name {
  padding: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eaec;
  font-weight: bold;
}

.hide-panal {
  position: fixed;
  top: 400px;
  right: 500px;
  color: #2db7f5;
  cursor: pointer;
}
</style>
