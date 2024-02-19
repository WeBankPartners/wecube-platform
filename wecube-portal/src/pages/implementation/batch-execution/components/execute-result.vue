<template>
  <div class="batch-execute-result">
    <Card v-if="from === 'list'" :style="{ minHeight: maxHeight + 'px' }">
      <div class="custom-header" slot="title">
        <Icon :size="28" type="md-reorder" class="expand" @click="$emit('expand')" />
        <span class="title">执行结果</span>
      </div>
      <!--搜索条件-->
      <Row :gutter="10" style="width: 100%; margin-bottom: 10px">
        <Col :span="5">
          <Input v-model="form.operateObject" placeholder="操作对象" clearable @on-change="handleSearch" />
        </Col>
        <Col :span="4">
          <Select v-model="form.errorCode" placeholder="单条执行状态" clearable @on-change="handleSearch">
            <Option v-for="(i, index) in statusList" :key="index" :value="i.value">{{ i.label }}</Option>
          </Select>
        </Col>
        <Col :span="15" style="display: flex">
          <Select v-model="form.filterType" @on-change="filterTypeChange" style="width: 140px">
            <Option v-for="item in filterTypeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
          </Select>
          <Input
            v-model="form.filterParams"
            placeholder="Filter result, e.g :error or /[0-9]+/"
            clearable
            @on-change="handleSearch"
          >
          </Input>
        </Col>
      </Row>
      <Table size="small" :columns="tableColumns" :data="tableData" :loading="loading" width="100%"></Table>
    </Card>
    <div v-else>
      <template v-if="tableColumns.length > 0">
        <div style="display: flex; align-items: center">
          <div style="margin-right: 20px">
            <span>执行状态：</span>
            <Tag :color="detailData.errorCode === '0' ? 'success' : 'error'">
              {{ detailData.errorCode === '0' ? '成功' : '失败' }}
            </Tag>
          </div>
          <div v-if="detailData.errorCode === '1'">
            <span>失败原因：</span>
            <span>{{ detailData.errorMessage }}</span>
          </div>
        </div>
        <div style="margin: 10px 0">
          <Input
            v-model="form.operateObject"
            placeholder="操作对象"
            clearable
            style="width: 200px"
            @on-change="handleSearch"
          />
          <Select
            v-model="form.errorCode"
            placeholder="单条执行状态"
            clearable
            style="width: 200px; margin-left: 10px"
            @on-change="handleSearch"
          >
            <Option v-for="(i, index) in statusList" :key="index" :value="i.value">{{ i.label }}</Option>
          </Select>
          <Select v-model="form.filterType" @on-change="filterTypeChange" style="width: 120px; margin-left: 10px">
            <Option v-for="item in filterTypeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
          </Select>
          <Input
            v-model="form.filterParams"
            placeholder="Filter result, e.g :error or /[0-9]+/"
            clearable
            style="width: 300px"
            @on-change="handleSearch"
          >
          </Input>
        </div>
      </template>
      <Table
        v-if="tableColumns.length > 0"
        size="small"
        :columns="tableColumns"
        :data="tableData"
        :loading="loading"
        :width="150 * tableColumns.length"
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
        <!-- <div style="margin: 10px 0">输入</div>
        <JsonViewer :value="jsonData.input" :expand-depth="5" boxed copyable></JsonViewer>
        <div style="margin: 10px 0">输出</div>
        <JsonViewer :value="jsonData.output" :expand-depth="5" boxed copyable></JsonViewer> -->
        <div>
          <a @click="isShow = !isShow">show requestData</a>
          <span v-if="isShow">
            <pre>{{ jsonData.input }}</pre>
            <Divider />
          </span>
        </div>
        <div>
          <pre class="display-result" v-if="jsonData.output">
            <span v-html="formatResult(jsonData.output)"></span>
          </pre>
          <pre v-else>
            <span></span>
          </pre>
        </div>
      </div>
    </Drawer>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import { batchExecuteHistory } from '@/api/server'
import { debounce } from '@/const/util'
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
        errorCode: '',
        filterParams: null,
        filterType: 'str'
      },
      filterTypeList: [
        { label: this.$t('bc_filter_type_str'), value: 'str' },
        { label: this.$t('bc_filter_type_regex'), value: 'regex' }
      ],
      statusList: [
        { label: '成功', value: '0' },
        { label: '失败', value: '1' }
      ],
      tableColumns: [],
      tableData: [],
      sourceData: [],
      detailData: {},
      loading: false,
      maxHeight: 500,
      visible: false,
      jsonData: {
        input: {},
        output: {}
      },
      isShow: false
    }
  },
  watch: {
    id: {
      handler (val) {
        if (val) {
          this.getList(val)
        }
      },
      immediate: true
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 150
  },
  methods: {
    handleSearch: debounce(function () {
      const { errorCode, operateObject, filterType, filterParams } = this.form
      this.tableData = this.sourceData.filter(item => {
        let errorCodeFlag = !errorCode || errorCode === item.errorCode
        let businessKeyFlag = item.businessKey.indexOf(operateObject) > -1
        let returnJsonFlag = true
        // 字符串匹配
        if (filterType === 'str' && filterParams) {
          returnJsonFlag = item.returnJson.indexOf(filterParams) > -1
          const reg = new RegExp(filterParams, 'g')
          item.returnJsonFormat = item.returnJson.replace(reg, "<span style='color:red'>" + filterParams + '</span>')
        }
        // 正则匹配
        if (filterType === 'regex' && filterParams) {
          let execRes = []
          let patt = null
          try {
            patt = new RegExp(filterParams, 'gmi')
            execRes = item.returnJson.match(patt)
            execRes = Array.from(new Set(execRes))
            execRes.sort(function (a, b) {
              return b.length - a.length
            })
            execRes = execRes.filter(s => {
              return s && s.trim()
            })
          } catch (err) {
            console.log(err)
            this.$Message.error(this.$t('bc_filter_type_warn'))
            this.form.filterParams = null
            return
          }
          let str = item.returnJson
          let len = str.length
          execRes.forEach(keyword => {
            let reg = new RegExp(keyword, 'g')
            str = str.replace(reg, "<span style='color:red'>" + keyword + '</span>')
          })
          if (str.length !== len) {
            returnJsonFlag = true
            item.returnJsonFormat = str
          } else {
            returnJsonFlag = false
          }
        }
        return errorCodeFlag && businessKeyFlag && returnJsonFlag
      })
    }, 200),
    async getList (id) {
      this.loading = true
      const { status, data } = await batchExecuteHistory(id)
      this.loading = false
      if (status === 'OK') {
        this.tableColumns = []
        this.tableData = data.batchExecutionJobs || []
        this.sourceData = data.batchExecutionJobs || []
        this.detailData = data || {}
        const dynamicColumns = data.configData.outputParameterDefinitions || []
        this.tableColumns.push(
          ...[
            {
              title: '操作对象',
              width: 200,
              render: (h, params) => {
                return <span>{params.row.businessKey || '--'}</span>
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
              const returnJson = JSON.parse(params.row.returnJson)
              return <span>{returnJson[item.name] || '--'}</span>
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
      this.jsonData.output = row.returnJsonFormat ? JSON.parse(row.returnJsonFormat) : JSON.parse(row.returnJson)
    },
    handleCancel () {
      this.visible = false
    },
    filterTypeChange () {
      this.form.filterParams = null
    },
    formatResult (result) {
      if (!result) {
        return
      }
      for (let key in result) {
        if (result[key] !== null && typeof result[key] === 'string') {
          result[key] = result[key].split('\n').join('<br/>            ')
        }
      }
      return JSON.stringify(result, null, 2)
    },
    reset () {
      this.form = {
        operateObject: '',
        errorCode: '',
        filterParams: null,
        filterType: 'str'
      }
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
    .expand {
      margin-right: 5px;
      cursor: pointer;
    }
  }
  .search {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
  }
  .display-result {
    height: calc(100vh - 300px);
    overflow-y: auto;
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
