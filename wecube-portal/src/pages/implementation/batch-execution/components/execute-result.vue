<template>
  <div class="batch-execute-result">
    <Card v-if="from === 'list'" :style="{ minHeight: maxHeight + 'px' }">
      <div class="custom-header" slot="title">
        <span class="title">执行结果</span>
        <!--搜索条件-->
        <Input v-model="form.operateObject" placeholder="操作对象类型" style="width: 300px" />
        <Select v-model="form.status" placeholder="单条执行状态" clearable style="width: 300px; margin-left: 20px">
          <Option v-for="(i, index) in statusList" :key="index" :value="i.value">{{ i.label }}</Option>
        </Select>
        <Button type="primary" @click="handleSearch" style="margin-left: 20px">搜索</Button>
      </div>
      <Table size="small" :columns="tableColumns" :data="tableData" :loading="loading" width="100%"></Table>
    </Card>
    <div v-else>
      <div style="margin-bottom: 10px">
        <Input v-model="form.operateObject" placeholder="操作对象类型" style="width: 300px" />
        <Select v-model="form.status" placeholder="单条执行状态" clearable style="width: 300px; margin-left: 20px">
          <Option v-for="(i, index) in statusList" :key="index" :value="i.value">{{ i.label }}</Option>
        </Select>
        <Button type="primary" @click="handleSearch" style="margin-left: 20px">搜索</Button>
      </div>
      <Table
        v-if="tableColumns.length > 0"
        size="small"
        :columns="tableColumns"
        :data="tableData"
        :loading="loading"
        width="100%"
      ></Table>
      <div v-else class="no-data">暂无数据</div>
    </div>
    <Drawer
      title="输入输出"
      v-model="visible"
      width="800"
      :mask-closable="true"
      :lock-scroll="true"
      @on-close="handleCancel"
      class="json-drawer"
    >
      <div class="content" :style="{ maxHeight: maxHeight + 'px' }">
        <div style="margin: 10px 0">输入</div>
        <JsonViewer :value="jsonData.input" :expand-depth="5" boxed copyable></JsonViewer>
        <div style="margin: 10px 0">输出</div>
        <JsonViewer :value="jsonData.output" :expand-depth="5" boxed copyable></JsonViewer>
      </div>
    </Drawer>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import { batchExecuteHistory } from '@/api/server'
export default {
  components: {
    JsonViewer
  },
  props: {
    id: {
      type: String,
      default: ''
    },
    // list列表页，create创建页
    from: {
      type: String,
      default: 'list'
    }
  },
  data () {
    return {
      form: {
        operateObject: '',
        status: ''
      },
      statusList: [
        { label: '全部', value: '' },
        { label: '成功', value: '0' },
        { label: '失败', value: '1' }
      ],
      tableColumns: [],
      tableData: [],
      loading: false,
      maxHeight: 500,
      visible: false,
      jsonData: {
        input: {},
        output: {}
      }
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 150
  },
  methods: {
    handleSearch () {},
    async getList (id) {
      this.loading = true
      const { status, data } = await batchExecuteHistory(id)
      this.loading = false
      if (status === 'OK') {
        this.tableColumns = []
        this.tableData = data.batchExecutionJobs || []
        const dynamicColumns = data.configData.outputParameterDefinitions || []
        this.tableColumns.push(
          ...[
            {
              title: '操作对象类型',
              width: 200,
              key: 'entityName',
              render: (h, params) => {
                return <span>{params.row.packageName + ':' + params.row.entityName}</span>
              }
            },
            {
              title: '状态',
              minWidth: 100,
              key: 'errorCode',
              render: (h, params) => {
                return (
                  <Tag color={params.row.errorCode === '0' ? 'success' : 'error'}>
                    {params.row.errorCode === '0' ? '成功' : '失败'}
                  </Tag>
                )
              }
            }
          ]
        )
        dynamicColumns.forEach(item => {
          this.tableColumns.push({
            title: item.name,
            minWidth: 150,
            key: item.name,
            render: (h, params) => {
              return <span>{params.row[item.name] || '--'}</span>
            }
          })
        })
        this.tableColumns.push({
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
                  disabled={false}
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
        })
      }
    },
    handleJsonDetail (row) {
      this.visible = true
      this.jsonData.input = JSON.parse(row.inputJson)
      this.jsonData.output = JSON.parse(row.returnJson)
    },
    handleCancel () {
      this.visible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execute-result {
  .custom-header {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    height: 50px;
    .title {
      margin-right: 20px;
      font-size: 14px;
      font-weight: bold;
    }
  }
  .no-data {
    width: 800px;
    min-height: 100px;
    border: 1px dashed #d7dadc;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #515a6e;
  }
}
</style>
<style lang="scss">
.batch-execute-result {
  .ivu-card-head {
    padding: 0 16px !important;
    border: none;
  }
  .ivu-card-body {
    padding: 0 16px 16px 16px !important;
  }
}
</style>
