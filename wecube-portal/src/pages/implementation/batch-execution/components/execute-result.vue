<template>
  <div class="batch-execute-result">
    <Card :style="{ minHeight: maxHeight + 'px' }">
      <div class="title" slot="title">
        <span>执行结果</span>
        <!--搜索条件-->
        <Input v-model="form.operateObject" placeholder="操作对象主键" style="width: 300px" />
        <Select v-model="form.status" placeholder="操作对象主键" style="width: 300px; margin-left: 20px">
          <Option v-for="(i, index) in statusList" :key="index" :value="i.value">{{ i.label }}</Option>
        </Select>
      </div>
      <Table size="small" :columns="tableColumns" :data="tableData" width="100%"></Table>
    </Card>
  </div>
</template>

<script>
export default {
  data () {
    return {
      form: {
        operateObject: '',
        status: ''
      },
      statusList: [
        { label: '成功', value: '0' },
        { label: '失败', value: '1' }
      ],
      tableColumns: [
        {
          title: '操作对象类型',
          width: 200,
          key: 'operateObject'
        },
        {
          title: '状态',
          minWidth: 100,
          key: 'errorCode',
          render: (h, params) => {
            return <Tag>{params.row.errorCode === '0' ? '成功' : '失败'}</Tag>
          }
        },
        {
          title: '操作',
          key: 'action',
          width: 80,
          fixed: 'right',
          align: 'center',
          render: (h, params) => {
            return (
              <Tooltip content={'查看完整输入输出'} placement="top">
                <Button
                  size="small"
                  type="info"
                  onClick={() => {
                    this.handleJsonDetail(params.row)
                  }}
                  style="margin-right:5px;"
                >
                  <Icon type="md-eye" size="16"></Icon>
                </Button>
              </Tooltip>
            )
          }
        }
      ],
      tableData: [],
      maxHeight: 500
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 170
  },
  methods: {
    handleQuery () {},
    handleJsonDetail (row) {}
  }
}
</script>

<style lang="scss">
.batch-execute-result {
  .title {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    height: 50px;
    span {
      margin-right: 20px;
      font-size: 14px;
      font-weight: bold;
    }
  }
  .ivu-card-head {
    padding: 0 16px !important;
    border: none;
  }
  .ivu-card-body {
    padding: 0 16px 16px 16px !important;
  }
}
</style>
