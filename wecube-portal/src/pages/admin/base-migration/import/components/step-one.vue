<template>
  <div class="base-migration-import-one">
    <div class="item">
      <span class="title">导入链接：</span>
      <div class="link">
        <Input v-model="importLink" placeholder="请输入导入链接" clearable style="width: 600px" />
        <Button type="primary" :disabled="!importLink" @click="handleImportLink">确认</Button>
      </div>
    </div>
    <Card :bordered="false" dis-hover :padding="0" style="min-height: 400px">
      <Enviroment v-if="detail.id" from="import"></Enviroment>
      <Spin v-if="loading" size="large" fix></Spin>
    </Card>
    <div class="footer">
      <Button type="info" @click="handleSave" :disabled="!detail.id">执行导入</Button>
    </div>
  </div>
</template>

<script>
import Enviroment from '../../export/components/step-enviroment.vue'
export default {
  components: {
    Enviroment
  },
  data() {
    return {
      importLink: '',
      loading: false,
      detail: {}
    }
  },
  methods: {
    handleImportLink() {
      this.loading = true
      setTimeout(() => {
        this.detail = { id: '1111111111' }
        this.loading = false
      }, 2000)
    },
    handleSave() {
      this.$emit('nextStep')
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-one {
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
    padding-left: 12px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
    }
  }
  .footer {
    position: fixed;
    bottom: 10px;
    display: flex;
    justify-content: center;
    width: calc(100% - 460px);
  }
}
</style>
