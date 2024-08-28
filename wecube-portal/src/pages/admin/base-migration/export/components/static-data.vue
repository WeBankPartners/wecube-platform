<template>
  <div class="export-static-data">
    <div class="item">
      <span class="title">
        CMDB：<span class="sub-title">
          已选 CI<span class="number">{{ 10 }}</span> 视图<span class="number">{{ 10 }}</span> 报表<span
            class="number"
          >{{ 10 }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="10">
          <Card title="CI">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbTableData">
            </Table>
          </Card>
        </Col>
        <Col :span="7">
          <Card title="视图">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbTableData">
            </Table>
          </Card>
        </Col>
        <Col :span="7">
          <Card title="报表">
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbTableData">
            </Table>
          </Card>
        </Col>
      </Row>
    </div>
    <div class="item">
      <span class="title">
        物料包：已选<span class="number">{{ 10 }}</span>
      </span>
      <Row :gutter="10">
        <Col :span="17">
          <Card>
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbTableData">
            </Table>
          </Card>
        </Col>
      </Row>
    </div>
    <div class="item">
      <span class="title">
        监控配置：<span class="sub-title">
          已选配置类型<span class="number">{{ 10 }}</span> 总条数<span class="number">{{ 10 }}</span>
        </span>
      </span>
      <Row :gutter="10">
        <Col :span="17">
          <Card>
            <Table :border="false" size="small" :columns="cmdbTableColumns" :max-height="360" :data="cmdbTableData">
            </Table>
          </Card>
        </Col>
      </Row>
    </div>
  </div>
</template>

<script>
import tableConfig from '../static-table'
export default {
  mixins: [tableConfig],
  data() {
    return {}
  },
  methods: {
    jumpToHistory() {}
  }
}
</script>

<style lang="scss" scoped>
.export-static-data {
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 30px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      .sub-title {
        font-weight: 600;
      }
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-right: 6px;
        margin-left: 1px;
      }
    }
  }
}
</style>
<style lang="scss">
.export-static-data .ivu-card-head {
  padding: 10px 16px !important;
}
</style>
