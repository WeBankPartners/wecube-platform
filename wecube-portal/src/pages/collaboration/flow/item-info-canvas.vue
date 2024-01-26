<template>
  <div id="itemInfo">
    <div class="hide-panal" @click="hideItem"></div>
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
      <FormItem label="编排名称" prop="label">
        <Input
          v-model="itemCustomInfo.label"
          style="width: 85%"
          @on-change="paramsChanged"
          :disable="!itemCustomInfo.enableModifyName"
        ></Input>
        <span :style="nameLen > 30 ? 'color:red' : ''">{{ nameLen }}/30</span>
      </FormItem>
      <FormItem label="版本" prop="version">
        <InputNumber :min="1" disabled v-model="itemCustomInfo.version"></InputNumber>
      </FormItem>
      <FormItem label="操作对象类型" prop="rootEntity">
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
      <FormItem label="使用场景" prop="scene">
        <Select v-model="itemCustomInfo.scene" @on-change="paramsChanged" filterable>
          <Option v-for="item in sceneList" :value="item.value" :key="item.value">{{ item.label }} </Option>
        </Select>
      </FormItem>
      <FormItem label="标签" prop="tags">
        <Input v-model="itemCustomInfo.tags" @on-change="paramsChanged"></Input>
      </FormItem>
      <FormItem label="冲突检测">
        <i-switch v-model="itemCustomInfo.conflictCheck" @on-change="paramsChanged" />
      </FormItem>
      <div style="position: absolute; bottom: 20px; right: 280px; width: 200px">
        <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
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
        label: [
          { required: true, message: 'label cannot be empty', trigger: 'blur' },
          { type: 'string', max: 30, message: 'Label cannot exceed 30 words.', trigger: 'blur' }
        ],
        scene: [{ required: true, message: 'scene at least one hobby', trigger: 'change' }],
        rootEntity: [{ required: true, message: 'rootEntity at least one hobby', trigger: 'change' }]
      },
      allEntityType: [], // 系统中所有根CI
      authPluginList: [], // 待授权插件列表
      sceneList: [
        // 可使用场景列表
        { label: '请求', value: 'request' },
        { label: '发布', value: 'release' },
        { label: '其他', value: 'other' }
      ]
    }
  },
  computed: {
    nameLen () {
      return this.itemCustomInfo.label.length
    }
  },
  mounted () {},
  methods: {
    async showItemInfo (data) {
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
  width: 12px;
  height: 22px;
  border-radius: 10px 0 0 10px;
  background-color: white;
  border-top: 1px solid #0892ed80;
  border-bottom: 1px solid #0892ed80;
  border-left: 1px solid #0892ed80;
  overflow: hidden;

  position: fixed;
  top: 400px;
  right: 514px;
  cursor: pointer;
  box-shadow: 0 0 8px #0892ed80;
}
</style>
