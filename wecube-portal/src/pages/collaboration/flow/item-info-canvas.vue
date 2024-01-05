<template>
  <div id="itemInfo">
    <Icon class="hide-panal" type="md-exit" size="24" @click="hideItem" />
    <div class="panal-name">编排属性：</div>
    <Form :label-width="80">
      <FormItem label="编排ID">
        <Input disabled v-model="itemCustomInfo.id"></Input>
      </FormItem>
      <FormItem label="编排名称">
        <Input v-model="itemCustomInfo.label"></Input>
      </FormItem>
      <FormItem label="操作对象">
        <FilterRules
          @change="onEntitySelect"
          v-model="itemCustomInfo.currentSelectedEntity"
          :allDataModelsWithAttrs="allEntityType"
          style="width: 100%"
        ></FilterRules>
      </FormItem>
      <FormItem label="授权插件">
        <Select v-model="itemCustomInfo.authPlugins" multiple>
          <Option v-for="item in authPluginList" :value="item.value" :key="item.value">{{ item.label }} </Option>
        </Select>
      </FormItem>
      <FormItem label="使用场景">
        <Select v-model="itemCustomInfo.useCase" multiple>
          <Option v-for="item in sceneList" :value="item.value" :key="item.value">{{ item.label }} </Option>
        </Select>
      </FormItem>
      <FormItem label="标签">
        <Input v-model="itemCustomInfo.tags"></Input>
      </FormItem>
      <FormItem label="冲突检测">
        <i-switch v-model="itemCustomInfo.conflictCheck" />
      </FormItem>
    </Form>
    <div style="position: fixed; bottom: 20px; right: 400px">
      <Button @click="saveItem" type="primary">{{ $t('save') }}</Button>
      <Button @click="hideItem">{{ $t('cancel') }}</Button>
    </div>
  </div>
</template>
<script>
import FilterRules from '@/pages/components/filter-rules.vue'
import { getAllDataModels } from '@/api/server.js'
export default {
  components: {
    FilterRules
  },
  data () {
    return {
      itemCustomInfo: {
        id: '',
        label: '',
        currentSelectedEntity: '', // 操作对象
        useCase: '', // 使用场景
        authPlugins: [], // 授权插件列表
        tags: '',
        conflictCheck: true // 冲突检测
      },
      allEntityType: [], // 系统中所有根CI
      authPluginList: [
        // 待授权插件列表
        { label: 'monitor', value: 'monitor' },
        { label: 'taskman', value: 'taskman' },
        { label: 'wecmdb', value: 'wecmdb' }
      ],
      sceneList: [
        // 可使用场景列表
        { label: 'monitor', value: 'monitor' },
        { label: 'taskman', value: 'taskman' },
        { label: 'wecmdb', value: 'wecmdb' }
      ]
    }
  },
  mounted () {
    // 获取所有根CI类型
    this.getAllDataModels()
  },
  methods: {
    showItemInfo (data) {
      this.itemCustomInfo = data
    },
    saveItem () {
      this.$emit('sendItemInfo', this.itemCustomInfo)
    },
    hideItem () {
      this.$emit('hideItemInfo')
    },

    async getAllDataModels () {
      let { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = data
      }
    },
    onEntitySelect (v) {
      this.itemCustomInfo.currentSelectedEntity = v || ''
    }
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
  margin: 8px;
  cursor: pointer;
}

.panal-name {
  padding-bottom: 4px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eaec;
}
</style>
