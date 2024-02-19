<template>
  <div>
    <div id="itemInfo">
      <div class="hide-panal" @click="hideItem">
        <Icon type="ios-arrow-dropright" size="28" />
      </div>
      <div class="panal-name">{{ $t('workFlowProperties') }}：</div>
      <Form :label-width="120" style="padding-right: 12px">
        <FormItem label="ID">
          <Input disabled v-model="itemCustomInfo.id"></Input>
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('name') }}
          </label>

          <Input
            v-model="itemCustomInfo.label"
            @on-change="paramsChanged"
            :disabled="!itemCustomInfo.enableModifyName"
          ></Input>
          <span style="position: absolute; left: 314px; top: 2px; line-height: 30px; background: #ffffff"
            >{{ itemCustomInfo.label.length || 0 }}/30</span
          >
          <span
            class="custom-error-tag"
            v-if="itemCustomInfo.label.length > 30 || itemCustomInfo.label.length === 0"
            style="color: red"
            >{{ $t('name') }}{{ $t('cannotExceed') }} 30 {{ $t('characters') }}</span
          >
        </FormItem>
        <FormItem :label="$t('version')" prop="version">
          <InputNumber :min="1" disabled v-model="itemCustomInfo.version" style="width: 100%"></InputNumber>
        </FormItem>
        <FormItem>
          <label slot="label">
            <span style="color: red">*</span>
            {{ $t('instance_type') }}
          </label>
          <FilterRules
            @change="onEntitySelect"
            v-model="itemCustomInfo.rootEntity"
            :allDataModelsWithAttrs="allEntityType"
            style="width: 100%"
          ></FilterRules>
          <span class="custom-error-tag" v-if="itemCustomInfo.rootEntity === ''" style="color: red"
            >{{ $t('instance_type') }}{{ $t('cannotBeEmpty') }}</span
          >
        </FormItem>
        <!-- @on-change="paramsChanged" -->
        <FormItem :label="$t('authPlugin')">
          <Select v-model="itemCustomInfo.authPlugins" filterable multiple>
            <Option v-for="item in authPluginList" :value="item" :key="item">{{ item }} </Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('group')" prop="scene">
          <Input v-model="itemCustomInfo.scene" @on-change="paramsChanged"></Input>
          <span style="position: absolute; left: 320px; top: 2px; line-height: 30px; background: #ffffff"
            >{{ itemCustomInfo.scene.length || 0 }}/30</span
          >
          <span class="custom-error-tag" v-if="itemCustomInfo.scene.length > 30" style="color: red"
            >{{ $t('group') }} {{ $t('cannotExceed') }} 30 {{ $t('characters') }}</span
          >
        </FormItem>
        <FormItem :label="$t('conflict_test')">
          <i-switch v-model="itemCustomInfo.conflictCheck" @on-change="paramsChanged" />
        </FormItem>
        <FormItem :label="$t('description')">
          <Input v-model="itemCustomInfo.tags" @on-change="paramsChanged" type="textarea" :rows="4"></Input>
          <span style="position: relative; left: 310px; top: -28px; background: #ffffff"
            >{{ itemCustomInfo.tags.length || 0 }}/200</span
          >
          <span v-if="itemCustomInfo.tags.length > 200" style="color: red"
            >{{ $t('description') }}{{ $t('cannotExceed') }} 200 {{ $t('characters') }}</span
          >
        </FormItem>
      </Form>
    </div>
    <div class="item-footer">
      <Button v-if="editFlow !== 'false'" :disabled="isSaveBtnActive()" @click="saveItem" type="primary">{{
        $t('save')
      }}</Button>
      <Button v-if="editFlow !== 'false'" @click="hideItem">{{ $t('cancel') }}</Button>
    </div>
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
      allEntityType: [], // 系统中所有根CI
      authPluginList: [], // 待授权插件列表
      oriRootEntity: '' // 缓存编排最初对象类型，在修改时提示
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
      this.oriRootEntity = tmpData.rootEntity
      this.itemCustomInfo = JSON.parse(JSON.stringify(Object.assign(defaultNode, tmpData)))
      const keys = Object.keys(defaultNode)
      keys.forEach(k => {
        this.itemCustomInfo[k] = tmpData[k]
      })
      this.itemCustomInfo.version = Number(this.itemCustomInfo.version[1])
    },
    saveItem () {
      let finalData = JSON.parse(JSON.stringify(this.itemCustomInfo))
      finalData.version += ''
      finalData.name = finalData.label
      this.$emit('sendItemInfo', finalData)
    },
    isSaveBtnActive () {
      let res = false
      if (this.itemCustomInfo.label.length > 30 || this.itemCustomInfo.label.length === 0) {
        res = true
      }
      if (this.itemCustomInfo.scene.length > 30) {
        res = true
      }
      if (this.itemCustomInfo.rootEntity === '') {
        res = true
      }
      return res
    },
    panalStatus () {
      return this.isParmasChanged
    },
    hideItem () {
      if (this.isParmasChanged) {
        this.$Modal.confirm({
          title: `${this.$t('confirm_discarding_changes')}`,
          content: `${this.itemCustomInfo.label}:${this.$t('params_edit_confirm')}`,
          'z-index': 1000000,
          okText: this.$t('save'),
          cancelText: this.$t('abandon'),
          onOk: async () => {
            this.saveItem()
          },
          onCancel: () => {
            this.$emit('hideItemInfo')
          }
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
      if (this.oriRootEntity !== '' && this.oriRootEntity !== this.itemCustomInfo.rootEntity) {
        this.$Modal.confirm({
          title: this.$t('instance_type'),
          content: this.$t('changeInstanceTypeTip'),
          'z-index': 1000000,
          onOk: async () => {
            this.oriRootEntity = this.itemCustomInfo.rootEntity
          },
          onCancel: () => {
            this.itemCustomInfo.rootEntity = this.oriRootEntity
          }
        })
      } else {
        this.paramsChanged()
      }
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
  height: calc(100vh - 154px);
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
.item-footer {
  position: absolute;
  z-index: 10;
  bottom: 26px;
  right: 12px;
  width: 500px;
  padding: 8px 24px;
  background: #ffffff;
  height: 32px;
}
</style>
