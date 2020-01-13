<template>
  <div class>
    <section class="search">
      <Card v-if="displaySearchZone">
        <div class="search-zone">
          <Form :label-width="170" label-colon>
            <FormItem :label="$t('bc_define_query_objects')">
              <a @click="setSearchConditions"
                >{{ $t('bc_define_query_objects') }}...</a
              >
            </FormItem>
            <FormItem :label="$t('bc_query_path')">
              <span v-if="dataModelExpression != ':'">
                {{ dataModelExpression }}
              </span>
              <span v-else>({{ $t('bc_empty') }})</span>
            </FormItem>
            <FormItem :label="$t('bc_query_condition')">
              <div v-if="searchParameters.length">
                <Row>
                  <Col
                    span="8"
                    v-for="(sp, spIndex) in searchParameters"
                    :key="spIndex"
                    style="padding:0 8px"
                  >
                    <label
                      >{{ sp.packageName }}-{{ sp.entityName }}.{{
                        sp.description
                      }}:</label
                    >
                    <Input v-model="sp.value" />
                  </Col>
                </Row>
              </div>
              <span v-else>({{ $t('bc_empty') }})</span>
            </FormItem>
          </Form>
        </div>
        <div class="search-btn">
          <Button
            type="primary"
            :disabled="!(!!currentPackageName && !!currentEntityName)"
            @click="excuteSearch"
            >{{ $t('bc_execute_query') }}</Button
          >
          <Button @click="clearParametes">{{
            $t('bc_clear_condition')
          }}</Button>
          <Button @click="resetParametes">{{ $t('bc_reset_query') }}</Button>
        </div>
      </Card>
      <div v-else>
        <a @click="reExcute('displaySearchZone')"
          >查询 资源实例 中满足以下条件的CI数据对象:</a
        >
        <ul>
          <li v-for="(sp, spIndex) in searchParameters" :key="spIndex">
            <span>
              {{ sp.packageName }}-{{ sp.entityName }}:[{{ sp.description }}:{{
                sp.value
              }}]
            </span>
          </li>
        </ul>
      </div>
    </section>
    <section
      v-if="!displaySearchZone"
      class="search-result-table"
      style="margin-top:20px;"
    >
      <div class="we-table">
        <Card v-if="displayResultTableZone">
          <p slot="title">
            查询结果：
          </p>
          <Button
            type="primary"
            :disabled="!seletedRows.length"
            @click="batchAction"
            >批量操作</Button
          >
          <WeTable
            :tableData="tableData"
            :tableOuterActions="[]"
            :tableInnerActions="null"
            :tableColumns="tableColumns"
            @getSelectedRows="onSelectedRowsChange"
            ref="table"
          />
        </Card>
        <a v-else @click="reExcute('displayResultTableZone')">
          找到 {{ tableData.length }} 个资源实例,选择了其中{{
            seletedRowsNum
          }}执行{{ serviceId }}
        </a>
      </div>
    </section>
    <section
      v-if="!displaySearchZone && !displayResultTableZone"
      style="margin-top:60px;"
    >
      <Card>
        <p slot="title">
          执行结果：
        </p>
        <Row>
          <Col span="6" class="excute-result excute-result-search">
            <Input v-model="businessKey" placeholder="请输入条件过滤" />
            <p class="excute-result-search-title">{{ serviceId }}</p>
            <ul v-if="filterBusinessKeySet.length">
              <li
                @click="activeResultKey = key"
                :class="[
                  activeResultKey === key ? 'active-key' : '',
                  'business-key',
                  excuteResult[key].errorCode === '1' ? 'error-key' : ''
                ]"
                v-for="(key, keyIndex) in filterBusinessKeySet"
                :key="keyIndex"
              >
                <span>{{ key }}</span>
              </li>
            </ul>
            <p v-else>暂无数据</p>
          </Col>
          <Col span="17" class="excute-result excute-result-json">
            <Input
              v-model="resultFilterKey"
              style="width:300px;visibility: hidden;"
            />
            <div>
              <!-- <highlight-code lang="json"><pre>{{ businessKeyContent }}</pre></highlight-code> -->
              <pre
                v-if="businessKeyContent"
              > <span v-html="JSON.stringify(businessKeyContent.result, null, 2)"></span></pre>
              <pre v-else> <span></span></pre>
              <!-- <p>{{ JSON.stringify(businessKeyContent, null, 2) }}</p> -->
            </div>
          </Col>
        </Row>
      </Card>
    </section>
    <Modal
      :width="700"
      v-model="isShowSearchConditions"
      :title="$t('bc_define_query_objects')"
    >
      <Form :label-width="110">
        <FormItem
          :rules="{ required: true }"
          :show-message="false"
          label="路径起点:"
        >
          <Select
            v-model="selectedEntityType"
            ref="select"
            filterable
            @on-change="changeEntityType"
          >
            <OptionGroup
              :label="pluginPackage.packageName"
              v-for="(pluginPackage, index) in allEntityType"
              :key="index"
            >
              <Option
                v-for="item in pluginPackage.pluginPackageEntities"
                :value="item.name"
                :key="item.name"
                :label="item.name"
              ></Option>
            </OptionGroup>
          </Select>
        </FormItem>
        <FormItem
          :rules="{ required: true }"
          :show-message="false"
          label="查询路径："
        >
          <PathExp
            :rootEntity="selectedEntityType"
            :allDataModelsWithAttrs="allEntityType"
            v-model="dataModelExpression"
          ></PathExp>
        </FormItem>
        <FormItem label="目标类型：">
          <span>{{ currentPackageName }}:{{ currentEntityName }}</span>
        </FormItem>
        <FormItem
          :rules="{ required: true }"
          :show-message="false"
          label="业务主键："
        >
          <Select filterable v-model="currentEntityAttr">
            <Option
              v-for="entityAttr in currentEntityAttrList"
              :value="entityAttr.name"
              :key="entityAttr.id"
              >{{ entityAttr.name }}</Option
            >
          </Select>
        </FormItem>
        <FormItem label="查询条件：" class="tree-style">
          <Row>
            <Col span="12">
              <Tree
                :data="allEntityAttr"
                @on-check-change="checkChange"
                show-checkbox
                multiple
              ></Tree>
            </Col>
            <Col span="12" class="tree-checked">
              <span>已选数据：</span>
              <ul>
                <li v-for="(tea, teaIndex) in targetEntityAttr" :key="teaIndex">
                  <span>
                    {{ tea.packageName }}-{{ tea.entityName }}:{{ tea.name }}
                  </span>
                </li>
              </ul>
            </Col>
          </Row>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="saveSearchCondition">
          {{ $t('confirm') }}
        </Button>
      </div>
    </Modal>

    <Modal v-model="batchActionModalVisible" title="批量操作">
      <Form label-position="right" :label-width="150">
        <FormItem
          :label="$t('plugin')"
          :rules="{ required: true }"
          :show-message="false"
        >
          <Select filterable clearable v-model="serviceId">
            <Option
              v-for="(item, index) in filteredPlugins"
              :value="item.serviceName"
              :key="index"
              >{{ item.serviceDisplayName }}</Option
            >
          </Select>
        </FormItem>
        <template v-for="(item, index) in selectedPluginParams">
          <FormItem :label="item.name" :key="index">
            <Input
              v-if="item.mappingType === 'constant'"
              v-model="item.bindValue"
            />
            <span v-else>{{
              item.mappingType === 'entity'
                ? '从CI数据对象获取'
                : '从系统参数获取'
            }}</span>
          </FormItem>
        </template>
      </Form>
      <div slot="footer">
        <Button
          type="primary"
          @click="excuteBatchAction"
          :disabled="!this.serviceId"
        >
          {{ $t('confirm') }}
        </Button>
      </div>
    </Modal>

    <Modal v-model="DelConfig.isDisplay" width="360">
      <p slot="header" style="color:#f60;text-align:center">
        <Icon type="ios-information-circle"></Icon>
        <span>确认</span>
      </p>
      <div style="text-align:center">
        <p>将要清除当前结果，是否继续？</p>
      </div>
      <div slot="footer">
        <Button type="warning" size="large" long @click="del">继续</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import PathExp from '@/pages/components/path-exp.vue'
import {
  getAllDataModels,
  dmeAllEntities,
  dmeIntegratedQuery,
  entityView,
  getFilteredPluginInterfaceList,
  batchExecution
} from '@/api/server.js'

export default {
  name: '',
  data () {
    return {
      displaySearchZone: true,
      displayResultTableZone: false,
      displayExcuteResultZone: false,

      DelConfig: {
        isDisplay: false,
        displayConfig: {
          name: ''
        },
        key: null
      },

      isShowSearchConditions: false,
      selectedEntityName: '',
      selectedEntityType: null,
      allEntityType: [],

      dataModelExpression: '',
      currentEntityName: '',
      currentPackageName: '',
      currentEntityAttr: '',
      currentEntityAttrList: [],
      allEntityAttr: [],
      targetEntityAttr: [],

      searchParameters: [],

      tableData: [],
      seletedRows: [],
      seletedRowsNum: 0,
      tableColumns: [],

      batchActionModalVisible: false,
      serviceId: null,
      selectedPluginParams: [],
      allPlugins: [],
      filteredPlugins: [],

      excuteResult: {},
      excuteBusinessKeySet: [],
      filterBusinessKeySet: [],
      activeResultKey: '',
      businessKey: '',
      resultFilterKey: ''
    }
  },
  mounted () {},
  computed: {
    businessKeyContent: function () {
      return this.excuteResult[this.activeResultKey]
    }
  },
  watch: {
    dataModelExpression: async function (val) {
      if (val === ':') {
        return
      }
      const params = {
        dataModelExpression: val
      }
      const { data, status } = await dmeAllEntities(params)
      if (status === 'OK') {
        this.currentEntityName = data.slice(-1)[0].entityName
        this.currentPackageName = data.slice(-1)[0].packageName
        this.currentEntityAttrList = data.slice(-1)[0].attributes

        this.allEntityAttr = []
        data.forEach((single, index) => {
          const childNode = single.attributes.map(attr => {
            attr.key = single.packageName + single.entityName + index
            attr.index = index
            attr.title = attr.name
            attr.entityName = single.entityName
            attr.packageName = single.packageName
            return attr
          })
          this.allEntityAttr.push({
            title: `${single.packageName}-${single.entityName}`,
            children: childNode
          })
        })
      }
    },
    serviceId: function (val) {
      this.filteredPlugins.forEach(plugin => {
        if (plugin.serviceDisplayName === val) {
          this.selectedPluginParams = plugin.inputParameters
        }
      })
      this.selectedPluginParams = this.selectedPluginParams.map(_ => {
        _.bindValue = ''
        return _
      })
    },
    businessKey: function (val) {
      this.filterBusinessKeySet = []
      for (const key in this.excuteResult) {
        if (key.indexOf(this.businessKey) > -1) {
          this.filterBusinessKeySet.push(key)
        }
      }
    }
  },
  methods: {
    setSearchConditions () {
      this.getAllDataModels()
      if (document.querySelector('.wecube_attr-ul')) {
        document.querySelector('.wecube_attr-ul').style.width = '530px'
      }
      this.$refs.select.setQuery(null)
      this.dataModelExpression = ':'
      this.currentEntityAttr = null
      this.currentEntityAttrList = []
      this.currentPackageName = ''
      this.currentEntityName = ''
      this.allEntityAttr = []
      this.targetEntityAttr = []
      this.isShowSearchConditions = true
    },
    changeEntityType () {
      this.targetEntityAttr = []
    },
    async getAllDataModels () {
      this.selectedEntityType = null
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
        this.allEntityType = []
        this.allEntityType = data.map(_ => {
          // handle result sort by name
          return {
            ..._,
            pluginPackageEntities: _.pluginPackageEntities.sort(function (a, b) {
              var s = a.name.toLowerCase()
              var t = b.name.toLowerCase()
              if (s < t) return -1
              if (s > t) return 1
            })
          }
        })
      }
    },
    checkChange (totalChecked) {
      this.targetEntityAttr = totalChecked
    },
    saveSearchCondition () {
      if (!this.currentEntityAttr) {
        this.$Message.warning('业务主键不能为空！')
        return
      }
      this.isShowSearchConditions = false
      this.searchParameters = this.targetEntityAttr
    },
    async excuteSearch () {
      let { status, data } = await entityView(
        this.currentPackageName,
        this.currentEntityName
      )
      if (status === 'OK') {
        this.tableColumns = data.map((_, i) => {
          return {
            title: _.description,
            key: _.name,
            displaySeqNo: i + 1
          }
        })
      }
      this.entityData()
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
      const { status, data } = await dmeIntegratedQuery(requestParameter)
      if (status === 'OK') {
        if (data.length) {
          this.tableData = data
          this.displaySearchZone = false
          this.displayResultTableZone = true
        } else {
          this.$Message.warning('空数据！')
        }
      }
    },
    clearParametes () {
      this.searchParameters.forEach(item => {
        item.value = ''
      })
    },
    resetParametes () {
      this.dataModelExpression = ':'
      this.currentPackageName = null
      this.currentEntityName = null
      this.searchParameters = []
    },
    reExcute (key) {
      this.DelConfig.isDisplay = true
      this.DelConfig.key = key
    },
    del () {
      this.DelConfig.isDisplay = false

      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.displayExcuteResultZone = false
      this.businessKey = null
      this[this.DelConfig.key] = true
    },
    onSelectedRowsChange (rows, checkoutBoxdisable) {
      this.seletedRows = rows
      this.seletedRowsNum = this.seletedRows.length
    },
    batchAction () {
      this.getFilteredPluginInterfaceList()
      this.batchActionModalVisible = true
      this.selectedPluginParams = []
      this.serviceId = null
    },
    async getFilteredPluginInterfaceList () {
      const { status, data } = await getFilteredPluginInterfaceList(
        this.currentPackageName,
        this.currentEntityName
      )
      if (status === 'OK') {
        this.filteredPlugins = data
      }
    },
    async excuteBatchAction () {
      const plugin = this.filteredPlugins.find(_ => {
        return _.serviceName === this.serviceId
      })
      const inputParameterDefinitions = plugin.inputParameters.map(p => {
        const inputParameterValue =
          p.mappingType === 'constant'
            ? p.dataType === 'number'
              ? Number(p.bindValue)
              : p.bindValue
            : null
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        }
      })
      let currentEntity = this.currentEntityAttrList.find(_ => {
        return _.name === this.currentEntityAttr
      })
      const resourceDatas = this.seletedRows.map(_ => {
        return {
          id: _.id,
          businessKeyValue: _[this.currentEntityAttr]
        }
      })

      let requestBody = {
        packageName: this.currentPackageName,
        entityName: this.currentEntityName,
        pluginConfigInterface: plugin,
        inputParameterDefinitions,
        businessKeyAttribute: currentEntity,
        resourceDatas
      }

      const { status, data } = await batchExecution(requestBody)
      this.batchActionModalVisible = false
      this.$Message.info('执行可能需要一点时间！')
      this.seletedRows = []
      if (status === 'OK') {
        this.excuteResult = data
        this.excuteBusinessKeySet = this.filterBusinessKeySet = []
        for (const key in data) {
          this.excuteBusinessKeySet.push(key)
        }
        this.filterBusinessKeySet = this.excuteBusinessKeySet
        this.displayResultTableZone = false
        this.displayExcuteResultZone = false
      }
    }
  },
  components: {
    PathExp
  }
}
</script>

<style lang="scss" scope>
.ivu-tree-children li {
  margin: 0 !important;
  .ivu-checkbox-wrapper {
    margin: 0 !important;
  }
}
.ivu-form-item {
  margin-bottom: 6px !important;
}
.ivu-form-item-error .ivu-select-selection,
.ivu-form-item-error .ivu-select-arrow {
  border-color: #dcdee2 !important;
}
textarea:focus {
  // outline: none;
  outline: #96c5f7 solid 1px;
}
.tree-checked {
  border-left: 2px solid gray;
  padding-left: 8px;
}
.search-btn {
  margin-top: 16px;
}
.we-table /deep/ .ivu-form-label-top {
  display: none;
}
.excute-result {
  padding: 8px;
  min-height: 300px;
}
.excute-result-search {
  margin-right: 16px;
  border-right: 1px solid #e8eaec;
  .excute-result-search-title {
    margin-top: 16px;
    font-size: 16px;
    // font-weight: 500;
  }
  ul {
    margin: 4px 0;
  }
}
.excute-result-json {
  border: 1px solid #e8eaec;
  word-wrap: break-word;
  word-break: break-all;
  overflow: scroll;
}
.business-key {
  padding: 0 16px;
  cursor: pointer;
  color: #2d8cf0;
}
.active-key {
  background: #e5e2e2;
}
.error-key {
  color: red;
}
</style>
