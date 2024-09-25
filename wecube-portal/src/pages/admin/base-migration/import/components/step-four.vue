<template>
  <div class="base-migration-import-four">
    <div class="import-status">
      <Alert v-if="detailData.status === 'doing'" type="info" show-icon>
        <template #desc>正在导入内容，请稍后... </template>
      </Alert>
      <Alert v-else-if="detailData.status === 'fail'" type="error" show-icon>
        <template #desc>导入失败！</template>
      </Alert>
      <Alert v-else-if="detailData.status === 'success'" type="success" show-icon>
        <template #desc>导入成功！</template>
      </Alert>
    </div>
    <div class="item">
      <span class="title">
        监控配置：<span class="sub-title">已选 <span class="name">配置类型</span><span class="number">{{ detailData.monitorRes.data.length }}</span>
          <span class="name">总条数</span><span class="number">{{ detailData.monitorCount }}</span>
        </span>
      </span>
      <Table
        :border="false"
        size="small"
        :columns="monitorColumns"
        :max-height="maxHeight"
        :data="detailData.monitorRes.data"
      />
    </div>
    <div class="footer">
      <Button v-if="['fail'].includes(detailData.status)" type="default" @click="handleRetry">重试</Button>
      <Button v-if="['doing', 'fail'].includes(detailData.status)" type="default" @click="handleStop">终止</Button>
      <Button type="default" @click="handleLast">上一步</Button>
      <Button v-if="['success'].includes(detailData.status)" type="primary" @click="handleComplete">完成导入</Button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    detailData: Object
  },
  data() {
    return {
      importLink: '',
      loading: false,
      detail: {},
      // 监控数据
      monitorColumns: [
        {
          title: '数据类型',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {this.$t(`m_${params.row.name}`) || '-'}
            </span>
          )
        },
        {
          title: '监控配置查询条件',
          key: 'conditions',
          render: (h, params) => <span>{params.row.conditions || '-'}</span>
        },
        {
          title: '已选',
          key: 'total',
          width: 100,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              <div style="width:25px">{params.row.count}</div>
              <Icon type="ios-list" size="36" style="cursor:pointer;" />
            </span>
          )
        }
      ],
      maxHeight: 500
    }
  },
  mounted() {
    this.maxHeight = document.body.clientHeight - 280
  },
  methods: {
    handleRetry() {},
    handleStop() {},
    handleLast() {},
    handleComplete() {}
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-four {
  .import-status {
    margin-top: -10px;
    margin-bottom: 16px;
  }
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
    button {
      &:not(:first-child) {
        margin-left: 10px;
      }
    }
  }
}
</style>
<style lang="scss">
.base-migration-import-four {
  .ivu-alert-with-desc .ivu-alert-icon {
    left: 16px;
    top: 26px;
    font-size: 28px;
  }
  .ivu-alert-with-desc.ivu-alert-with-icon {
    padding: 10px 16px 10px 55px;
  }
}
</style>
