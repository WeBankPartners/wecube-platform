<template>
  <div class="batch-execute-base-form">
    <Row class="back-header">
      <Icon size="22" type="md-arrow-back" class="icon" @click="$emit('back')" />
      <span class="name">
        {{ `${data.name || $t('be_new_template')}` }}
      </span>
    </Row>
    <Form :disabled="type === 'view'" label-position="right" :label-width="125">
      <!--执行模板信息-->
      <BaseHeaderTitle :title="$t('be_execute_templateinfo')">
        <!--模板名称-->
        <FormItem v-if="from === 'template' && type !== 'view'" :label="$t('be_template_name')" required>
          <Input
            v-model="name"
            :maxlength="100"
            show-word-limit
            :placeholder="$t('be_template_name_placeholder')"
            class="form-item"
          />
        </FormItem>
        <template v-else>
          <!--预执行记录-->
          <div v-if="!data.templateData || !data.templateData.id" style="padding: 0 20px">
            {{ $t('be_pre_execute_record') }}
          </div>
          <!--执行记录-->
          <div v-else class="template-info">
            <div class="item">
              <span>{{ $t('be_template_id') }}：</span>
              <span>{{ data.templateData.id }}</span>
            </div>
            <div class="item">
              <span>{{ $t('be_template_name') }}：</span>
              <span>{{ data.templateData.name }}</span>
            </div>
            <div class="item">
              <span>{{ $t('createdBy') }}：</span>
              <span>{{ data.templateData.createdBy }}</span>
            </div>
            <div class="item">
              <span>{{ $t('table_created_date') }}：</span>
              <span>{{ data.templateData.createdTime }}</span>
            </div>
            <div class="item">
              <span>{{ $t('mgmt_role') }}：</span>
              <span>{{
                data.templateData.permissionToRole &&
                  data.templateData.permissionToRole.MGMTDisplayName &&
                  data.templateData.permissionToRole.MGMTDisplayName.join('，')
              }}</span>
            </div>
            <div class="item">
              <span>{{ $t('use_role') }}：</span>
              <span>{{
                data.templateData.permissionToRole &&
                  data.templateData.permissionToRole.USEDisplayName &&
                  data.templateData.permissionToRole.USEDisplayName.join('，')
              }}</span>
            </div>
          </div>
        </template>
      </BaseHeaderTitle>
      <!--第1步 设置操作对象及查询条件-->
      <BaseHeaderTitle :title="$t('be_step1_title')">
        <!--批量名称-->
        <FormItem v-if="from === 'execute'" :label="$t('be_batch_name')" required>
          <Input
            v-model="name"
            :maxlength="100"
            show-word-limit
            :placeholder="$t('be_batch_name_placeholder')"
            class="form-item"
          />
        </FormItem>
        <!--操作对象查询路径-->
        <FormItem :label="$t('bc_query_path')" required>
          <FilterRules
            :allDataModelsWithAttrs="allEntityType"
            :needNativeAttr="false"
            :needAttr="true"
            :hiddenFilterRule="true"
            :disabled="type === 'view' || from === 'execute'"
            v-model="dataModelExpression"
            class="form-item"
          ></FilterRules>
        </FormItem>
        <!--操作对象类型-->
        <FormItem :label="$t('be_instance_type')">
          <Input
            disabled
            :value="currentPackageName ? currentPackageName + ':' + currentEntityName : ''"
            class="form-item"
          ></Input>
        </FormItem>
        <!--查询结果主键-->
        <FormItem :label="$t('be_query_result_key')" required>
          <Select
            filterable
            v-model="primatKeyAttr"
            class="form-item"
            :disabled="from === 'execute'"
            @on-change="fetchTableColumns"
          >
            <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
              entityAttr.name
            }}</Option>
          </Select>
        </FormItem>
        <!--查询结果展示列-->
        <FormItem required>
          <span slot="label">
            {{ $t('be_query_result_column') }}
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
            @on-change="fetchTableColumns"
          >
            <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
              entityAttr.name
            }}</Option>
          </Select>
        </FormItem>
        <!--设置过滤条件-->
        <FormItem :label="$t('be_setting_filter')">
          <Row class="dynamic-condition">
            <div style="display: flex; justify-content: space-between">
              <Button
                @click="editSearchParameters"
                :disabled="from === 'execute'"
                type="primary"
                icon="md-create"
                class="create"
              />
              <Button
                v-if="searchParameters && searchParameters.length > 0"
                @click="clearSearchParameters"
                :disabled="from === 'execute'"
                type="error"
                icon="md-close"
                class="create"
              />
            </div>
            <template v-if="searchParameters && searchParameters.length > 0">
              <Col v-for="(item, index) in searchParameters" :key="index" :span="24" class="item">
                <span color="success">{{ item.packageName }}-{{ item.entityName }}:{{ item.name }}</span>
                <span color="primary" style="margin-left: 10px; color: #5384ff">{{ item.operator }}</span>
                <span color="primary" style="margin-left: 10px">{{ item.value }}</span>
              </Col>
            </template>
            <div v-else style="color: #515a6e; text-align: center">{{ $t('be_setting_filter_tips') }}</div>
          </Row>
        </FormItem>
        <ConditionTree
          v-if="editSearchParamsVisible"
          :visible.sync="editSearchParamsVisible"
          :select="searchParameters"
          :data="searchParamsTree"
          @submit="handleSearchParamsChange"
        ></ConditionTree>
      </BaseHeaderTitle>
      <!--第2步 勾选执行实例-->
      <BaseHeaderTitle :title="$t('be_step2_title')">
        <!--勾选操作实例-->
        <FormItem :label="$t('be_choose_instance')" required>
          <EntityTable
            :data="tableData"
            :initSelectedRows="initSelectedRows"
            :columns="tableColumns"
            :loading="loading"
            @select="
              val => {
                seletedRows = val
              }
            "
            @changePage="handlePageChange"
            @changePageSize="handlePageSizeChange"
            @search="handleSearch"
            :pagination="pagination"
          ></EntityTable>
        </FormItem>
      </BaseHeaderTitle>
      <!--第3步 设置插件服务及参数-->
      <BaseHeaderTitle :title="$t('be_step3_title')">
        <!--插件服务-->
        <FormItem :label="$t('pluginService')" required>
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
        <!--设置入参-->
        <FormItem :label="$t('be_setting_inputparam')" required>
          <Row v-if="pluginInputParams && pluginInputParams.length > 0" class="border-box form-item">
            <Col v-for="(item, index) in pluginInputParams" :key="index" :span="24" style="margin-bottom: 12px">
              <span
                :class="{required: item.required === 'Y' && item.mappingType === 'constant'}"
                style="display: inline-block; width: 200px;"
              >{{ item.name }}</span>
              <Input v-if="item.mappingType === 'constant'" style="width: 500px;" v-model="item.bindValue" />
              <span v-else>{{ item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system') }}</span>
            </Col>
          </Row>
          <div v-else class="no-data">{{ $t('be_choose_pluginserver_tips') }}</div>
        </FormItem>
        <!--执行结果展示列(插件出参)-->
        <FormItem :label="$t('be_execute_result_column')">
          <Select filterable clearable multiple v-model="resultTableParams" class="form-item">
            <Option v-for="(item, index) in pluginOutputParams" :value="item.name" :key="index">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <!--高危检测-->
        <FormItem :label="$t('high_risk_detection')">
          <i-switch v-model="isDangerousBlock" disabled size="large">
            <span slot="open">{{ $t('be_turn_on') }}</span>
            <span slot="close">{{ $t('be_turn_off') }}</span>
          </i-switch>
        </FormItem>
      </BaseHeaderTitle>
    </Form>
    <!--执行结果-->
    <BaseHeaderTitle v-if="showResult || (from === 'execute' && type === 'view')" :title="$t('bc_execution_result')">
      <div style="padding: 0 20px">
        <ExecuteResult ref="executeResult" from="create" :id="showResult ? '' : data.id"></ExecuteResult>
      </div>
    </BaseHeaderTitle>
  </div>
</template>

<script>
import FilterRules from '../../components/filter-rules.vue'
import ConditionTree from './components/condition-tree.vue' // 过滤条件
import EntityTable from './components/entity-table.vue' // 选择实例表格
import ExecuteResult from './components/execute-result.vue' // 执行结果
import { deepClone } from '@/const/util.js'
import {
  getAllDataModels,
  dmeAllEntities,
  getPluginsByTargetEntityFilterRule,
  entityView,
  dmeIntegratedQuery
} from '@/api/server.js'
export default {
  components: {
    FilterRules,
    ConditionTree,
    EntityTable,
    ExecuteResult
  },
  props: {
    // template模板，execute执行
    from: {
      type: String,
      default: 'template'
    },
    // add创建，edit编辑，copy复制，view查看
    type: {
      type: String,
      default: 'add'
    },
    data: {
      type: Object,
      default: () => {}
    }
  },
  data() {
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
      initSelectedRows: [],
      loading: false,
      // 步骤三字段
      pluginId: '', // 表单-插件服务ID
      pluginOptions: [], // 插件下拉列表
      pluginInputParams: [], // 插件入参
      pluginOutputParams: [], // 插件出参
      resultTableParams: [], // 选择结果表出参
      isDangerousBlock: true, // 是否开启高危检测
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 50
      }
    }
  },
  watch: {
    async dataModelExpression(val) {
      this.showResult = false
      // 清空查询路径操作
      if (val === ':' || !val) {
        this.currentEntityName = ''
        this.currentPackageName = ''
        this.primatKeyAttrList = []
        this.primatKeyAttr = ''
        this.userTableColumns = []
        this.searchParamsTree = []
        this.searchParameters = []
        this.tableColumns = [] // 执行实例表格列
        this.tableData = [] // 执行实例表格数据
        this.seletedRows = [] // 勾选的执行实例
        this.pluginId = '' // 表单-插件服务ID
        this.pluginOptions = [] // 插件下拉列表
        this.pluginInputParams = [] // 插件入参
        this.pluginOutputParams = [] // 插件出参
        this.resultTableParams = [] // 选择结果表出参
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
        this.fetchTableData()
        this.searchParamsTree = []
        data.forEach((single, index) => {
          const childNode = (single.attributes
              && single.attributes.map(attr => {
                attr.key = single.packageName + single.entityName + index
                attr.index = index
                attr.title = attr.name
                attr.entityName = single.entityName
                attr.packageName = single.packageName
                return attr
              }))
            || []
          this.searchParamsTree.push({
            title: `${single.packageName}-${single.entityName}`,
            children: childNode
          })
        })
      }
    },
    data: {
      handler(val) {
        if (val && val.id) {
          const {
            name, isDangerousBlock, configData, sourceData
          } = val
          this.name = name
          this.dataModelExpression = configData.dataModelExpression
          this.currentPackageName = configData.packageName
          this.currentEntityName = configData.entityName
          this.primatKeyAttr = configData.primatKeyAttr
          this.searchParameters = configData.searchParameters
          this.pluginId = configData.pluginConfigInterface.serviceName
          this.isDangerousBlock = isDangerousBlock
          if (sourceData) {
            const frontData = JSON.parse(sourceData)
            this.seletedRows = deepClone(frontData.seletedRows)
            this.initSelectedRows = deepClone(frontData.seletedRows)
            this.pluginInputParams = frontData.pluginInputParams
            this.pluginOutputParams = frontData.pluginOutputParams
            this.resultTableParams = frontData.resultTableParams
            this.userTableColumns = frontData.userTableColumns
          }
          this.fetchTableColumns()
          this.fetchTableData()
        }
      },
      deep: true
    }
  },
  mounted() {
    this.getAllDataModels()
  },
  methods: {
    // 选择插件
    choosePlugin(val) {
      this.showResult = false
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
      this.resultTableParams = this.pluginOutputParams.map(item => item.name) || []
    },
    // 获取批量执行结果
    getExecuteResult(id) {
      this.$refs.executeResult.getList(id)
    },
    async getAllDataModels() {
      this.selectedEntityType = null
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = []
        this.allEntityType = data.map(_ => ({
          ..._,
          entities: _.entities.sort(function (a, b) {
            const s = a.name.toLowerCase()
            const t = b.name.toLowerCase()
            if (s < t) {
              return -1
            }
            if (s > t) {
              return 1
            }
          })
        }))
      }
    },
    editSearchParameters() {
      this.editSearchParamsVisible = true
    },
    clearSearchParameters() {
      this.searchParameters = []
      this.fetchTableData()
    },
    // 设置过滤条件
    handleSearchParamsChange(val) {
      if (this.dataModelExpression === ':' || !this.dataModelExpression) {
        return
      }
      this.searchParameters = val
      this.fetchTableData()
    },
    clearPlugin() {
      this.showResult = false
      this.pluginId = null
      this.pluginInputParams = []
      this.resultTableParams = []
      this.pluginOutputParams = []
    },
    // 获取插件下拉列表
    async getFilteredPluginList() {
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
        // 过滤不需要的数据
        this.pluginOptions = this.pluginOptions.filter(item => {
          const flag = item.inputParameters && item.inputParameters.every(param => param.mappingType !== 'context')
          return item.isAsyncProcessing === 'N' && flag
        })
        // 若详情接口返回的插件在接口中查不出来，手动拼接
        if (this.data && this.data.configData) {
          const { pluginConfigInterface } = this.data.configData
          const hasFlag = this.pluginOptions.some(item => item.serviceName === pluginConfigInterface.serviceName)
          if (!hasFlag) {
            this.pluginOptions.push(pluginConfigInterface)
          }
        }
      }
    },
    // 根据过滤条件获取执行实例表格列
    async fetchTableColumns() {
      if (!this.currentPackageName || !this.currentEntityName) {
        return
      }
      const { status, data } = await entityView(this.currentPackageName, this.currentEntityName)
      if (status === 'OK') {
        if (this.userTableColumns.length || this.primatKeyAttr) {
          const combineColumns = [...this.userTableColumns]
          if (this.userTableColumns.includes(this.primatKeyAttr)) {
            const index = this.userTableColumns.findIndex(i => i === this.primatKeyAttr)
            combineColumns.splice(index, 1)
            combineColumns.unshift(this.primatKeyAttr)
          }
          if (!this.userTableColumns.includes(this.primatKeyAttr) && this.primatKeyAttr) {
            combineColumns.unshift(this.primatKeyAttr)
          }
          this.tableColumns = combineColumns.map(_ => ({
            title: _,
            key: _,
            width: 200,
            render: (h, params) => (
              <Tooltip max-width="300" content={params.row[_].toString()}>
                <span class="word-ellipsis">{params.row[_] || '--'}</span>
              </Tooltip>
            )
          }))
        } else {
          this.tableColumns = data.map(_ => ({
            title: _.name,
            key: _.name,
            width: 200,
            render: (h, params) => (
              <div style="height:32px;">
                <Tooltip max-width="300" content={params.row[_.name].toString()}>
                  <span class="word-ellipsis">{params.row[_.name] || '--'}</span>
                </Tooltip>
              </div>
            )
          }))
        }
        this.tableColumns.unshift({
          type: 'selection',
          width: 60,
          fixed: 'left',
          align: 'center'
        })
      }
    },
    async fetchTableData(query = '') {
      const requestParameter = {
        dataModelExpression: this.dataModelExpression,
        filters: [],
        query,
        startIndex: (this.pagination.currentPage - 1) * this.pagination.pageSize,
        pageSize: 50
      }
      const keySet = []
      this.searchParameters.forEach(sParameter => {
        const index = keySet.indexOf(sParameter.key)
        if (index > -1) {
          const { name, value, operator } = sParameter
          if (value) {
            requestParameter.filters[index].attributeFilters.push({
              name,
              value: operator === 'in' ? value.split(',') : value,
              operator: operator || 'contains'
            })
          }
        } else {
          keySet.push(sParameter.key)
          const {
            index, packageName, entityName, name, value, operator
          } = sParameter
          if (value) {
            requestParameter.filters.push({
              index,
              packageName,
              entityName,
              attributeFilters: [
                {
                  name,
                  value: operator === 'in' ? value.split(',') : value,
                  operator: operator || 'contains'
                }
              ]
            })
          }
        }
      })
      this.loading = true
      const { status, data } = await dmeIntegratedQuery(requestParameter)
      this.loading = false
      if (status === 'OK') {
        this.tableData = data.contents || []
        const selectTag = this.seletedRows.map(item => item.id)
        this.pagination.total = data.pageInfo.totalRows
        this.tableData.forEach(item => {
          if (selectTag.includes(item.id)) {
            item._checked = true
          }
        })
      }
    },
    handleSearch(val) {
      if (this.dataModelExpression === ':' || !this.dataModelExpression) {
        return
      }
      this.pagination.currentPage = 1
      this.fetchTableData(val)
    },
    handlePageChange(val) {
      this.pagination.currentPage = val
      this.fetchTableData()
    },
    handlePageSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.fetchTableData()
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
      background: #5384ff;
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
  .required {
    &::before {
      content: '*';
      display: inline-block;
      margin-right: 4px;
      line-height: 1;
      font-size: 14px;
      color: #ff4d4f;
    }
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
