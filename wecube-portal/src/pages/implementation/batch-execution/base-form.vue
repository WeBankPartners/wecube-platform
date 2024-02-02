<template>
  <div class="batch-execute-base-form">
    <Row class="back-header">
      <Icon size="22" type="md-arrow-back" class="icon" @click="handleBack" />
      <span class="name">
        {{ `${data.name || ''}` }}
      </span>
    </Row>
    <Form :disabled="type === 'view'" label-position="right" :label-width="125">
      <HeaderTitle title="执行模板信息">
        <!--请求名-->
        <FormItem v-if="from === 'template' && type === 'add'" label="模板名称" required>
          <Input v-model="name" :maxlength="50" show-word-limit placeholder="请输入模板名称" class="form-item" />
        </FormItem>
        <template v-else>
          <div v-if="from === 'execute' && !data.batchExecutionTemplateId" style="padding: 0 20px">不是从模板发起</div>
          <div v-else class="template-info">
            <div class="item">
              <span>模板ID：</span>
              <span>{{ data.templateData.id }}</span>
            </div>
            <div class="item">
              <span>模板名：</span>
              <span>{{ data.templateData.name }}</span>
            </div>
            <div class="item">
              <span>创建人：</span>
              <span>{{ data.templateData.createdBy }}</span>
            </div>
            <div class="item">
              <span>创建时间</span>
              <span>{{ data.templateData.createdTime }}</span>
            </div>
            <div class="item">
              <span>属主角色：</span>
              <span>{{
                data.templateData.permissionToRole &&
                data.templateData.permissionToRole.MGMT &&
                data.templateData.permissionToRole.MGMT.join('，')
              }}</span>
            </div>
            <div class="item">
              <span>使用角色：</span>
              <span>{{
                data.templateData.permissionToRole &&
                data.templateData.permissionToRole.USE &&
                data.templateData.permissionToRole.USE.join('，')
              }}</span>
            </div>
          </div>
        </template>
      </HeaderTitle>
      <HeaderTitle v-if="showResult || (from === 'execute' && type === 'view')" title="执行结果">
        <div style="padding: 0 20px">
          <ExecuteResult ref="executeResult" from="create"></ExecuteResult>
        </div>
      </HeaderTitle>
      <HeaderTitle title="第1步 设置操作对象及查询条件">
        <!--批量名称-->
        <FormItem v-if="from === 'execute'" label="批量名称" required>
          <Input v-model="name" :maxlength="50" show-word-limit placeholder="请输入批量名称" class="form-item" />
        </FormItem>
        <!--操作对象查询路径-->
        <FormItem label="查询路径" required>
          <FilterRules
            :allDataModelsWithAttrs="allEntityType"
            :needNativeAttr="false"
            :needAttr="true"
            :disabled="type === 'view' || from === 'execute'"
            v-model="dataModelExpression"
            class="form-item"
          ></FilterRules>
        </FormItem>
        <!--操作对象类型-->
        <FormItem label="操作对象类型">
          <Input disabled :value="currentPackageName + ':' + currentEntityName" class="form-item"></Input>
        </FormItem>
        <!--查询结果主键-->
        <FormItem label="查询结果主键" required>
          <Select filterable v-model="primatKeyAttr" class="form-item" :disabled="from === 'execute'">
            <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
              entityAttr.name
            }}</Option>
          </Select>
        </FormItem>
        <!--查询结果展示列-->
        <FormItem required>
          <span slot="label">
            {{ '查询结果展示列' }}
            <Tooltip :content="$t('bc_set_columns_tip')">
              <Icon type="ios-help-circle-outline" />
            </Tooltip>
          </span>
          <Select
            filterable
            multiple
            v-model="userTableColumns"
            class="form-item"
            :disabled="from === 'execute'"
            @on-change="chooseUserTableColumns"
          >
            <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
              entityAttr.name
            }}</Option>
          </Select>
        </FormItem>
        <!--设置过滤条件-->
        <FormItem label="设置过滤条件">
          <Row class="dynamic-condition">
            <Button
              @click="editSearchParameters"
              :disabled="from === 'execute'"
              type="primary"
              icon="md-create"
              class="create"
            />
            <template v-if="searchParameters && searchParameters.length > 0">
              <Col v-for="(item, index) in searchParameters" :key="index" :span="24" class="item">
                <span color="success">{{ item.packageName }}-{{ item.entityName }}:{{ item.name }}</span>
                <span color="primary" style="margin-left: 10px">{{ item.value }}</span>
              </Col>
            </template>
            <div v-else style="color: #515a6e; text-align: center">请先设置过滤条件</div>
          </Row>
        </FormItem>
        <ConditionTree
          v-if="editSearchParamsVisible"
          :visible.sync="editSearchParamsVisible"
          :select="searchParameters"
          :data="searchParamsTree"
          @submit="handleSearchParamsChange"
        ></ConditionTree>
      </HeaderTitle>
      <HeaderTitle title="第2步 勾选执行实例">
        <div slot="header">
          <!-- <Button v-if="currentPackageName" type="success" size="small" icon="ios-refresh" @click="handleRefreshSearch">
            刷新查询结果
          </Button> -->
        </div>
        <!--勾选操作实例-->
        <FormItem label="勾选操作实例" required>
          <EntityTable
            :data="tableData"
            :columns="tableColumns"
            :loading="loading"
            @select="
              val => {
                seletedRows = val
              }
            "
          ></EntityTable>
        </FormItem>
      </HeaderTitle>
      <HeaderTitle title="第3步 设置插件服务及参数">
        <FormItem label="插件服务" required>
          <Select
            filterable
            clearable
            :disabled="from === 'execute'"
            v-model="pluginId"
            @on-change="choosePlugin"
            @on-clear="clearPlugin"
            class="form-item"
          >
            <Option v-for="(item, index) in pluginOptions" :value="item.serviceName" :key="index">{{
              item.serviceDisplayName
            }}</Option>
          </Select>
        </FormItem>
        <FormItem label="设置入参" required>
          <Row v-if="pluginInputParams && pluginInputParams.length > 0" class="border-box form-item">
            <Col v-for="(item, index) in pluginInputParams" :key="index" :span="24" style="margin-bottom: 12px">
              <span style="display: inline-block; width: 100px">{{ item.name }}</span>
              <Input v-if="item.mappingType === 'constant'" v-model="item.bindValue" style="width: 600px" />
              <span v-else>{{ item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system') }}</span>
            </Col>
          </Row>
          <div v-else class="no-data">请先选择插件服务</div>
        </FormItem>
        <FormItem label="执行结果展示列(插件出参)">
          <Select filterable clearable multiple v-model="resultTableParams" class="form-item">
            <Option v-for="(item, index) in pluginOutputParams" :value="item.name" :key="index">{{ item.name }}</Option>
          </Select>
        </FormItem>
      </HeaderTitle>
    </Form>
  </div>
</template>

<script>
import HeaderTitle from './components/header-title.vue'
import FilterRules from '../../components/filter-rules.vue'
import ConditionTree from './components/condition-tree.vue' // 过滤条件
import EntityTable from './components/entity-table.vue' // 选择实例表格
import ExecuteResult from './components/execute-result.vue' // 执行结果
import {
  getAllDataModels,
  dmeAllEntities,
  getPluginsByTargetEntityFilterRule,
  entityView,
  dmeIntegratedQuery
} from '@/api/server.js'
export default {
  components: {
    HeaderTitle,
    FilterRules,
    ConditionTree,
    EntityTable,
    ExecuteResult
  },
  props: {
    // 入口是模板还是执行
    from: {
      type: String,
      default: 'template'
    },
    // add创建，view查看
    type: {
      type: String,
      default: 'add'
    },
    data: {
      type: Object,
      default: () => {}
    }
  },
  data () {
    return {
      showResult: false, // 预执行后显示执行历史
      // 步骤一字段
      name: '', // 表单-名称
      dataModelExpression: ':', // 表单-查询路径
      allEntityType: [], // 查询路径数据源
      currentPackageName: '',
      currentEntityName: '',
      primatKeyAttrList: [],
      primatKeyAttr: '', // 表单-查询结果主键
      userTableColumns: [], // 表单-查询结果展示列
      searchParamsTree: [],
      searchParameters: [], // 表单-设置过滤条件
      editSearchParamsVisible: false,
      // 步骤二字段
      tableColumns: [], // 执行实例表格列
      tableData: [], // 执行实例表格数据
      seletedRows: [], // 勾选的执行实例
      loading: false,
      // 步骤三字段
      pluginId: '', // 表单-插件服务ID
      pluginOptions: [], // 插件下拉列表
      pluginInputParams: [], // 插件入参
      pluginOutputParams: [], // 插件出参
      resultTableParams: [] // 选择结果表出参
    }
  },
  watch: {
    dataModelExpression: async function (val) {
      // 清空查询路径操作
      if (val === ':' || !val) {
        this.currentEntityName = ''
        this.currentPackageName = ''
        this.primatKeyAttrList = []
        this.primatKeyAttr = ''
        this.userTableColumns = []
        return
      }
      // 获取插件服务列表
      this.getFilteredPluginList()
      const params = {
        dataModelExpression: val
      }
      const { data, status } = await dmeAllEntities(params)
      if (status === 'OK') {
        this.currentEntityName = data.slice(-1)[0].entityName
        this.currentPackageName = data.slice(-1)[0].packageName
        this.primatKeyAttrList = data.slice(-1)[0].attributes

        this.searchParamsTree = []
        data.forEach((single, index) => {
          const childNode = single.attributes.map(attr => {
            attr.key = single.packageName + single.entityName + index
            attr.index = index
            attr.title = attr.name
            attr.entityName = single.entityName
            attr.packageName = single.packageName
            return attr
          })
          this.searchParamsTree.push({
            title: `${single.packageName}-${single.entityName}`,
            children: childNode
          })
        })
      }
    },
    data: {
      handler (val) {
        if (val && val.id) {
          const { name, configData, sourceData } = val
          this.name = name
          this.dataModelExpression = configData.dataModelExpression
          this.currentPackageName = configData.packageName
          this.currentEntityName = configData.entityName
          this.primatKeyAttr = configData.primatKeyAttr
          this.searchParameters = configData.searchParameters
          this.pluginId = configData.pluginConfigInterface.serviceName
          if (sourceData) {
            const frontData = JSON.parse(sourceData)
            this.seletedRows = frontData.seletedRows
            this.pluginInputParams.push(...frontData.pluginInputParams)
            this.pluginOutputParams = frontData.pluginOutputParams
            this.resultTableParams = frontData.resultTableParams
            this.userTableColumns = frontData.userTableColumns
          }
          this.excuteSearch()
        }
      },
      deep: true
    }
  },
  mounted () {
    this.getAllDataModels()
  },
  methods: {
    handleBack () {
      const name = this.from === 'template' ? 'templateList' : 'executeList'
      this.$eventBusP.$emit('change-menu', name)
    },
    // 选择查询结果展示列
    chooseUserTableColumns () {
      this.excuteSearch()
    },
    // 选择插件
    choosePlugin (val) {
      this.pluginOptions.forEach(plugin => {
        if (plugin.serviceDisplayName === val) {
          this.pluginInputParams = plugin.inputParameters
          this.pluginOutputParams = plugin.outputParameters
        }
      })
      this.pluginInputParams = this.pluginInputParams.map(_ => {
        _.bindValue = ''
        return _
      })
      this.resultTableParams = []
    },
    // 获取批量执行结果
    getExecuteResult (id) {
      this.$refs.executeResult.getList(id)
    },
    async getAllDataModels () {
      this.selectedEntityType = null
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = []
        this.allEntityType = data.map(_ => {
          return {
            ..._,
            entities: _.entities.sort(function (a, b) {
              var s = a.name.toLowerCase()
              var t = b.name.toLowerCase()
              if (s < t) return -1
              if (s > t) return 1
            })
          }
        })
      }
    },
    editSearchParameters () {
      this.editSearchParamsVisible = true
    },
    // 设置过滤条件
    handleSearchParamsChange (val) {
      if (this.dataModelExpression === ':') return
      this.searchParameters = val
      this.excuteSearch()
    },
    // 更新执行实例表格
    handleRefreshSearch () {
      this.excuteSearch()
    },
    clearPlugin () {
      this.pluginId = null
      this.pluginInputParams = []
      this.resultTableParams = []
      this.pluginOutputParams = []
    },
    // 获取插件选择列表
    async getFilteredPluginList () {
      let pkg = ''
      let entity = ''
      let payload = {}
      // eslint-disable-next-line no-useless-escape
      const pathList = this.dataModelExpression.split(/[.~]+(?=[^\}]*(\{|$))/).filter(p => p.length > 1)
      const last = pathList[pathList.length - 1]
      const index = pathList[pathList.length - 1].indexOf('{')
      const isBy = last.indexOf(')')
      const current = last.split(':')
      const ruleIndex = current[1].indexOf('{')
      if (isBy > 0) {
        entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
        pkg = current[0].split(')')[1]
      } else {
        entity = ruleIndex > 0 ? current[1].slice(0, ruleIndex) : current[1]
        pkg = last.match(/[^>]+(?=:)/)[0]
      }
      payload = {
        pkgName: pkg,
        entityName: entity,
        targetEntityFilterRule: index > 0 ? pathList[pathList.length - 1].slice(index) : ''
      }
      const { status, data } = await getPluginsByTargetEntityFilterRule(payload)
      if (status === 'OK') {
        this.pluginOptions = data
      }
    },
    // 根据过滤条件获取执行实例表格列
    async excuteSearch () {
      let { status, data } = await entityView(this.currentPackageName, this.currentEntityName)
      if (status === 'OK') {
        if (this.userTableColumns.length) {
          this.tableColumns = this.userTableColumns.map((_, i) => {
            return {
              title: _,
              key: _,
              width: 200,
              displaySeqNo: i + 1,
              render: (h, params) => {
                return (
                  <Tooltip max-width="300" content={params.row[_].toString()}>
                    <span class="word-ellipsis">{params.row[_] || '--'}</span>
                  </Tooltip>
                )
              }
            }
          })
        } else {
          this.tableColumns = data.map((_, i) => {
            return {
              title: _.name,
              key: _.name,
              width: 200,
              displaySeqNo: i + 1,
              render: (h, params) => {
                return (
                  <div style="height:32px;">
                    <Tooltip max-width="300" content={params.row[_.name].toString()}>
                      <span class="word-ellipsis">{params.row[_.name] || '--'}</span>
                    </Tooltip>
                  </div>
                )
              }
            }
          })
        }
        this.tableColumns.unshift({
          type: 'selection',
          width: 60,
          fixed: 'left',
          align: 'center'
        })
        this.entityData()
      }
    },
    async entityData () {
      const requestParameter = {
        dataModelExpression: this.dataModelExpression,
        filters: []
      }
      let keySet = []
      this.searchParameters.forEach(sParameter => {
        const index = keySet.indexOf(sParameter.key)
        if (index > -1) {
          const { name, value } = sParameter
          if (value) {
            requestParameter.filters[index].attributeFilters.push({
              name,
              value,
              operator: 'eq'
            })
          }
        } else {
          keySet.push(sParameter.key)
          const { index, packageName, entityName, name, value } = sParameter
          if (value) {
            requestParameter.filters.push({
              index,
              packageName,
              entityName,
              attributeFilters: [
                {
                  name,
                  value,
                  operator: 'eq'
                }
              ]
            })
          }
        }
      })
      this.loading = true
      const { status, data } = await dmeIntegratedQuery(requestParameter)
      if (status === 'OK') {
        if (data.length) {
          const selectTag = this.seletedRows.map(item => item.id)
          this.tableData = data
          this.tableData.forEach(item => {
            if (selectTag.includes(item.id)) {
              item._checked = true
            }
          })
          // this.originTableData = this.tableData
        }
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execute-base-form {
  width: 100%;
  .template-info {
    width: 920px;
    display: flex;
    padding: 0 20px;
    flex-wrap: wrap;
    .item {
      width: 50%;
      margin-bottom: 10px;
      &:last-child {
        margin-bottom: 0px;
      }
      span {
        display: inline-block;
        text-align: left;
        &:first-child {
          width: 160px;
          padding-right: 40px;
        }
      }
    }
  }
  .back-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    .icon {
      cursor: pointer;
      width: 28px;
      height: 24px;
      color: #fff;
      border-radius: 2px;
      background: #2d8cf0;
    }
    .name {
      font-size: 16px;
      margin-left: 16px;
      display: flex;
      align-items: center;
    }
  }
  .dynamic-condition {
    width: 800px;
    min-height: 100px;
    border: 1px dashed #d7dadc;
    padding: 10px 20px 0px 20px;
    .item {
      margin-bottom: 10px;
    }
    .create {
      width: 24px;
      height: 24px;
    }
  }
  .border-box {
    border: 1px dashed #d7dadc;
    padding: 10px 20px 0px 20px;
  }
  .form-item {
    width: 800px;
  }
  .no-data {
    width: 800px;
    min-height: 100px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #515a6e;
    border: 1px dashed #d7dadc;
    padding: 10px 20px 0px 20px;
  }
}
</style>
<style lang="scss">
.batch-execute-base-form {
  .ivu-form-item {
    margin-bottom: 12px;
  }
  .word-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
  }
}
</style>
