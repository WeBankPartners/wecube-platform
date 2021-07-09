<template>
  <div>
    <Spin size="large" fix style="margin-top: 100px;" v-show="isLoading">
      <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
      <div>{{ $t('loading') }}</div>
    </Spin>
    <section class="execute-history">
      <Row>
        <Col span="5" :style="isShowHistoryMenu ? '' : 'display:none'" class="res-title">
          <div style="height:88px;padding: 8px;border-bottom:1px solid #e8eaec">
            <Form label-position="top" label-colon>
              <FormItem>
                <span slot="label" style="font-weight:500">{{ $t('bc_favorites_list') }}:</span>
                <Select
                  clearable
                  @on-clear="selectedCollectionId = null"
                  v-model="selectedCollectionId"
                  @on-open-change="getAllCollections"
                  @on-change="changeCollections"
                  filterable
                >
                  <Option
                    v-for="item in allCollections"
                    :value="item.favoritesId"
                    :key="item.favoritesId"
                    :label="item.collectionName"
                  >
                    <span>{{ item.collectionName }}</span>
                    <span style="float:right">
                      <Button
                        icon="ios-trash"
                        type="error"
                        size="small"
                        @click="showDeleteConfirm(item.favoritesId, item.collectionName)"
                      ></Button>
                    </span>
                    <span style="float:right;margin-right: 10px">
                      <Button
                        icon="ios-build"
                        type="primary"
                        @click="openEditCollectionModal(item)"
                        size="small"
                      ></Button>
                    </span>
                  </Option>
                </Select>
              </FormItem>
            </Form>
          </div>
          <div>
            <h6 style="margin: 8px">
              <span>{{ $t('bc_history_record') }} </span>
            </h6>
            <ul>
              <li
                @click="changeActiveExecuteHistory(keyIndex)"
                :class="[
                  activeExecuteHistoryKey === keyIndex ? 'active-key' : '',
                  'business-key',
                  'clear-default-style'
                ]"
                v-for="(key, keyIndex) in executeHistory"
                :key="keyIndex"
              >
                <span>
                  {{ keyIndex }}、{{ key.id }}
                  <Button style="margin-left: 8px" @click="openAddCollectionModal(key)" size="small">
                    {{ $t('bc_save') }}
                  </Button>
                </span>
              </li>
            </ul>
          </div>
        </Col>
        <Col :span="isShowHistoryMenu ? 19 : 24" class="res res-content">
          <Icon
            :style="isShowHistoryMenu ? '' : 'transform: rotate(90deg);'"
            class="history-record-menu"
            type="md-menu"
            @click="isShowHistoryMenu = !isShowHistoryMenu"
          />
          <div class="res-content-step">
            <Steps :current="5">
              <Step :title="$t('bc_query_conditions')">
                <div slot="content">
                  <Tooltip :max-width="500">
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content" style="white-space: normal;">
                      <ul>
                        <li>{{ $t('bc_query_path') }}:{{ dataModelExpression }}</li>
                        <li v-for="(sp, spIndex) in activeExecuteHistory.requestBody.searchParameters" :key="spIndex">
                          <span> {{ sp.packageName }}-{{ sp.entityName }}:[{{ sp.description }}:{{ sp.value }}] </span>
                        </li>
                      </ul>
                    </div>
                  </Tooltip>
                  <Button size="small" @click="changeSearchParams" type="primary" ghost>{{
                    $t('bc_set_condition')
                  }}</Button>
                </div>
              </Step>
              <Step :title="$t('bc_execution_instance')">
                <div slot="content">
                  <Tooltip>
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content" style="white-space: normal;">
                      <p
                        style="word-break: break-all"
                        :key="targetIndex"
                        v-for="(target, targetIndex) in activeExecuteHistory.requestBody.resourceDatas"
                      >
                        {{ target.businessKeyValue }}
                      </p>
                    </div>
                  </Tooltip>
                  <Button
                    size="small"
                    @click="changeTargetObject"
                    :disabled="!(!!currentPackageName && !!currentEntityName)"
                    type="primary"
                    ghost
                    >{{ $t('bc_change_instance') }}</Button
                  >
                </div>
              </Step>
              <Step :title="$t('bc_execution_plugin')" content="">
                <div slot="content">
                  <Tooltip>
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content" style="white-space: normal;">
                      {{ activeExecuteHistory.plugin.pluginName }}
                    </div>
                  </Tooltip>
                  <Button
                    size="small"
                    @click="changePlugin"
                    :disabled="!activeExecuteHistory.requestBody.resourceDatas.length > 0"
                    type="primary"
                    ghost
                    >{{ $t('bc_change_plugin') }}</Button
                  >
                </div>
              </Step>
              <Step :title="$t('bc_execution_parameter')" content="">
                <div slot="content">
                  <Tooltip :max-width="500">
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content" style="width:200px;white-space: normal;">
                      <ul>
                        <li v-for="(item, index) in activeExecuteHistory.plugin.pluginParams" :key="index">
                          <span v-if="item.mappingType === 'constant'"> {{ item.name }}: {{ item.bindValue }} </span>
                          <span v-else>{{
                            item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system')
                          }}</span>
                        </li>
                      </ul>
                    </div>
                  </Tooltip>
                  <Button
                    size="small"
                    type="primary"
                    :disabled="activeExecuteHistory.plugin.pluginParams.length === 0"
                    @click="changeParams"
                    ghost
                    >{{ $t('bc_complement_parameters') }}</Button
                  >
                </div>
              </Step>
              <Step :title="$t('bc_execute')" content="">
                <div slot="content">
                  <Button
                    size="small"
                    @click="executeAgain"
                    :disabled="!activeExecuteHistory.requestBody.inputParameterDefinitions"
                    type="primary"
                    ghost
                    :loading="btnLoading"
                    >{{ $t('bc_execute') }}</Button
                  >
                </div>
              </Step>
            </Steps>
          </div>
          <div class="res-content-result">
            <Row>
              <Col span="6" class="excute-result excute-result-search">
                <Input v-model="businessKey" placeholder="Filter instance" />
                <p class="excute-result-search-title">{{ activeExecuteHistory.plugin.pluginName }}</p>
                <ul v-if="activeExecuteHistory.filterBusinessKeySet.length" class="dispaly-instance-result">
                  <li
                    @click="changeActiveResultKey(key)"
                    :class="[
                      activeResultKey === key ? 'active-key' : '',
                      'business-key',
                      catchExecuteResult[key].errorCode === '1' ? 'error-key' : '',
                      'clear-default-style'
                    ]"
                    v-for="(key, keyIndex) in catchFilterBusinessKeySet"
                    :key="keyIndex"
                  >
                    <span>{{ key }}</span>
                  </li>
                </ul>
                <p v-else>No Data</p>
              </Col>
              <Col span="18" class="excute-result excute-result-json">
                <Row>
                  <Col span="4">
                    <Select filterable v-model="filterType" @on-change="filterTypeChange">
                      <Option v-for="item in filterTypeList" :value="item.value" :key="item.value">{{
                        item.label
                      }}</Option>
                    </Select>
                  </Col>
                  <Col span="17">
                    <Input v-model="filterParams" placeholder="Filter result, e.g :error or /[0-9]+/" />
                  </Col>
                  <Col span="2" offset="1">
                    <Button type="primary" @click="filterResult">
                      {{ $t('search') }}
                    </Button>
                  </Col>
                </Row>
                <div>
                  <pre
                    class="dispaly-result"
                    v-if="businessKeyContent"
                  > <span v-html="formatResult(businessKeyContent.result)"></span></pre>
                  <pre v-else> <span></span></pre>
                </div>
              </Col>
            </Row>
          </div>
        </Col>
      </Row>
    </section>

    <Modal
      v-model="operaModal"
      :mask-closable="false"
      :title="$t('bc_operation')"
      :width="1000"
      :closable="false"
      class="opera-modal"
    >
      <div style="height:400px;">
        <!-- 设置查询参数-开始 -->
        <section v-if="displaySearchZone" class="search">
          <Form :label-width="130" label-colon>
            <FormItem :rules="{ required: true }" :show-message="false" :label="$t('bc_query_path')">
              <FilterRules
                :allDataModelsWithAttrs="allEntityType"
                :needNativeAttr="false"
                :needAttr="true"
                v-model="dataModelExpression"
              ></FilterRules>
            </FormItem>
            <FormItem :label="$t('bc_target_type')">
              <Input disabled :value="currentPackageName + ':' + currentEntityName"></Input>
            </FormItem>
            <FormItem :rules="{ required: true }" :show-message="false" :label="$t('bc_primary_key')">
              <Select filterable v-model="primatKeyAttr">
                <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
                  entityAttr.name
                }}</Option>
              </Select>
            </FormItem>
            <FormItem :show-message="false">
              <span slot="label">
                <Tooltip :content="$t('bc_set_columns_tip')">
                  <Icon type="ios-help-circle-outline" />
                </Tooltip>
                {{ $t('bc_table_column') }}
              </span>
              <Select filterable multiple v-model="userTableColumns">
                <Option v-for="entityAttr in primatKeyAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
                  entityAttr.name
                }}</Option>
              </Select>
            </FormItem>
            <FormItem :label="$t('bc_query_condition')" class="tree-style">
              <Row style="max-height: 150px;overflow-y:auto;border: 1px solid #dcdee2;">
                <Col span="12">
                  <div>
                    <Tree :data="allEntityAttr" @on-check-change="checkChange" show-checkbox multiple></Tree>
                  </div>
                </Col>
                <Col span="12" class="tree-checked">
                  <span>{{ $t('bc_selected_data') }}：</span>
                  <ul>
                    <li v-for="(tea, teaIndex) in targetEntityAttr" :key="teaIndex">
                      <span> {{ tea.packageName }}-{{ tea.entityName }}:{{ tea.name }} </span>
                    </li>
                  </ul>
                </Col>
              </Row>
            </FormItem>
            <FormItem :label="$t('bc_query_condition')">
              <div v-if="searchParameters.length" style="height: 100px;overflow-y: auto;">
                <Row>
                  <Col span="8" v-for="(sp, spIndex) in searchParameters" :key="spIndex" style="padding:0 8px">
                    <label class="search-params-label">
                      {{ sp.packageName }}-{{ sp.entityName }}.{{ sp.description }}:</label
                    >
                    <Input v-model="sp.value" />
                  </Col>
                  <Col span="8" style="padding:0 8px">
                    <label class="search-params-label" style="visibility: hidden;"
                      >defaultdefaultdefaultdefaultdefault</label
                    >
                    <Button @click="clearParametes">{{ $t('bc_clear_condition') }}</Button>
                    <!-- <Button @click="resetParametes">{{ $t('bc_reset_query') }}</Button> -->
                  </Col>
                </Row>
              </div>
              <span v-else>({{ $t('bc_empty') }})</span>
            </FormItem>
          </Form>
        </section>
        <!-- 设置查询参数-结束 -->

        <!-- 选择执行对象-开始 -->
        <section v-if="displayResultTableZone" class="search-result-table" style="margin-top:20px;">
          <div style="margin-bottom:8px">
            <Input v-model="filterTableParams" :placeholder="$t('enter_search_keywords')" style="width: 300px" />
            <Button @click="filterTableData" type="primary">{{ $t('search') }}</Button>
            Selected: {{ seletedRows.length }}
          </div>
          <div class="we-table">
            <Card v-if="displayResultTableZone">
              <p slot="title">{{ $t('bc_search_result') }}：</p>
              <div style="height: 300px;overflow-y:auto">
                <Table
                  ref="currentRowTable"
                  @on-select="singleSelect"
                  @on-select-cancel="singleCancel"
                  @on-select-all-cancel="selectAllCancel"
                  @on-select-all="selectAll"
                  :columns="tableColumns"
                  :data="tableData"
                  size="small"
                  type="selection"
                ></Table>
              </div>
            </Card>
          </div>
        </section>
        <!-- 选择执行对象-结束 -->

        <!-- 选择插件配置参数-开始 -->
        <section v-if="batchActionModalVisible">
          <Form label-position="right" :label-width="150">
            <FormItem :label="$t('plugin')" :rules="{ required: true }" :show-message="false">
              <Select filterable clearable v-model="pluginId" @on-clear="clearPlugin">
                <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
                  item.serviceDisplayName
                }}</Option>
              </Select>
            </FormItem>
            <div style="max-height:350px;overflow-y:auto">
              <template v-for="(item, index) in selectedPluginParams">
                <FormItem :label="item.name" :key="index">
                  <Input v-if="item.mappingType === 'constant'" v-model="item.bindValue" />
                  <span v-else>{{ item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system') }}</span>
                </FormItem>
              </template>
            </div>
          </Form>
        </section>
        <!-- 选择插件配置参数-结束 -->

        <!-- 补充参数-开始 -->
        <section v-if="setPluginParamsModal">
          <Form label-position="right" :label-width="150" v-if="!!activeExecuteHistory.plugin">
            <template v-for="(item, index) in activeExecuteHistory.plugin.pluginParams">
              <FormItem :label="item.name" :key="index">
                <Input v-if="item.mappingType === 'constant'" v-model="item.bindValue" />
                <span v-else>{{ item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system') }}</span>
              </FormItem>
            </template>
          </Form>
        </section>
        <!-- 补充参数-结束 -->
      </div>
      <div slot="footer">
        <!-- 查询table数据 -->
        <Button
          type="primary"
          v-if="displaySearchZone"
          :disabled="!(!!currentPackageName && !!currentEntityName && !!primatKeyAttr)"
          @click="excuteSearch"
          >{{ $t('bc_execute_query') }}</Button
        >
        <!-- 选择插件 -->
        <Button type="primary" v-if="displayResultTableZone" :disabled="!seletedRows.length" @click="batchAction">{{
          $t('bc_change_plugin')
        }}</Button>
        <!-- 执行插件 -->
        <Button type="primary" v-if="batchActionModalVisible" @click="excuteBatchAction" :disabled="!this.pluginId">
          {{ $t('full_word_exec') }}
        </Button>
        <!-- 执行插件 -->
        <Button type="primary" v-if="setPluginParamsModal" @click="executeAgain" :loading="btnLoading">
          {{ $t('full_word_exec') }}
        </Button>
        <!-- 放弃功能 -->
        <Button @click="closeModal">
          {{ $t('cancel') }}
        </Button>
      </div>
    </Modal>
    <Modal v-model="collectionRoleManageModal" width="700" :title="$t('bc_edit_role')" :mask-closable="false">
      <div v-if="editCollectionName" style="margin-bottom:8px;">
        <span style="font-weight: 500;">{{ $t('bc_name') }}：</span>
        <Input v-model="collectionName" style="width:35%"></Input>
      </div>
      <div>
        <div class="role-transfer-title">{{ $t('mgmt_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRoles"
          :target-keys="MGMT"
          @on-change="handleMgmtRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div style="margin-top: 30px">
        <div class="role-transfer-title">{{ $t('use_role') }}</div>
        <Transfer
          :titles="transferTitles"
          :list-style="transferStyle"
          :data="allRolesBackUp"
          :target-keys="USE"
          @on-change="handleUseRoleTransferChange"
          filterable
        ></Transfer>
      </div>
      <div slot="footer">
        <Button @click="collectionRoleManageModal = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="confirmCollection">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>

    <Modal v-model="confirmModal.isShowConfirmModal" width="900">
      <div>
        <Icon :size="28" :color="'#f90'" type="md-help-circle" />
        <span class="confirm-msg">{{ $t('confirm_to_exect') }}</span>
      </div>
      <div style="max-height: 400px;overflow-y: auto;">
        <pre style="margin-left: 44px;margin-top: 22px;">{{ this.confirmModal.message }}</pre>
      </div>
      <div slot="footer">
        <span style="margin-left:30px;color:#ed4014;float: left;text-align:left">
          <Checkbox v-model="confirmModal.check">{{ $t('dangerous_confirm_tip') }}</Checkbox>
        </span>
        <Button type="text" @click="confirmModal.isShowConfirmModal = false">{{ $t('bc_cancel') }}</Button>
        <Button type="warning" :disabled="!confirmModal.check" @click="confirmToExecution">{{
          $t('bc_confirm')
        }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import FilterRules from '../components/filter-rules.vue'
import {
  getAllDataModels,
  dmeAllEntities,
  dmeIntegratedQuery,
  entityView,
  batchExecution,
  getAllCollections,
  deleteCollections,
  getRoleList,
  getRolesByCurrentUser,
  addCollectionsRole,
  saveBatchExecution,
  updateCollections,
  getPluginsByTargetEntityFilterRule
} from '@/api/server.js'
const BATCH_EXECUTION_URL = '/platform/v1/batch-execution/run'

export default {
  name: '',
  data () {
    return {
      btnLoading: false,
      operaModal: false,

      isLoading: false,
      displaySearchZone: false,
      displayResultTableZone: false,
      displayExecuteResultZone: false,

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

      dataModelExpression: ':',
      currentEntityName: '',
      currentPackageName: '',
      primatKeyAttr: '',
      primatKeyAttrList: [],
      allEntityAttr: [],
      targetEntityAttr: [],

      userTableColumns: [],
      searchParameters: [],

      filterTableParams: '',
      originTableData: [],
      tableData: [],
      seletedRows: [],
      tableColumns: [],

      batchActionModalVisible: false,
      isHistoryToBatchActionModal: false,
      pluginId: null,
      selectedPluginParams: [],
      allPlugins: [],
      filteredPlugins: [],

      setPluginParamsModal: false,

      isShowHistoryMenu: true,
      executeResult: {},
      filterBusinessKeySet: [],
      activeResultKey: null,
      businessKey: '',

      activeExecuteHistoryKey: 0,
      defaultActiveExecuteHistory: {
        plugin: {
          pluginName: '',
          pluginParams: []
        },
        requestBody: {
          searchParameters: [],
          resourceDatas: []
        },
        filterBusinessKeySet: []
      },
      activeExecuteHistory: {
        plugin: {
          pluginName: '',
          pluginParams: []
        },
        requestBody: {
          searchParameters: [],
          resourceDatas: []
        },
        filterBusinessKeySet: []
      },
      executeHistory: [],
      catchExecuteResult: {},
      catchFilterBusinessKeySet: [],
      filterParams: null,
      filterType: 'str',
      filterTypeList: [
        { label: this.$t('bc_filter_type_str'), value: 'str' },
        { label: this.$t('bc_filter_type_regex'), value: 'regex' }
      ],

      selectedCollectionId: null,
      selectedCollection: null,
      allCollections: [],
      collectionRoleManageModal: false,
      editCollectionName: false,
      collectionName: '',

      isAddCollect: false,
      toBeCollectedParams: null,
      allRoles: [],
      MGMT: [],
      allRolesBackUp: [],
      USE: [],
      transferTitles: [this.$t('unselected_role'), this.$t('selected_role')],
      transferStyle: { width: '300px' },

      confirmModal: {
        isShowConfirmModal: false,
        check: false,
        continueToken: '',
        message: '',
        requestBody: '',
        func: ''
      }
    }
  },
  mounted () {},
  computed: {
    businessKeyContent: function () {
      if (this.activeResultKey && this.catchExecuteResult) {
        return this.catchExecuteResult[this.activeResultKey]
      }
    }
    // seletedRowsNum: function () {
    //   return this.seletedRows.length
    // }
  },
  watch: {
    dataModelExpression: async function (val) {
      if (val === ':' || !val) {
        return
      }
      const params = {
        dataModelExpression: val
      }
      const { data, status } = await dmeAllEntities(params)
      if (status === 'OK') {
        this.currentEntityName = data.slice(-1)[0].entityName
        this.currentPackageName = data.slice(-1)[0].packageName
        this.primatKeyAttrList = data.slice(-1)[0].attributes

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
    pluginId: function (val) {
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
    selectedCollectionId: function (val) {
      this.isHistoryToBatchActionModal = !!val
    },
    activeExecuteHistory: function (val) {
      this.filterParams = null
      this.businessKey = null
      if (!val) {
        this.activeExecuteHistory = JSON.parse(JSON.stringify(this.defaultActiveExecuteHistory))
        this.catchExecuteResult = {}
        this.catchFilterBusinessKeySet = []
        this.dataModelExpression = ':'
        return
      }
      this.catchExecuteResult = val.executeResult
      this.catchFilterBusinessKeySet = val.filterBusinessKeySet
      this.dataModelExpression = val.requestBody.dataModelExpression
    },
    businessKey: function (val) {
      if (!val) {
        this.catchExecuteResult = this.activeExecuteHistory.executeResult
        this.catchFilterBusinessKeySet = this.activeExecuteHistory.filterBusinessKeySet
        return
      }
      this.filterParams = null
      this.catchFilterBusinessKeySet = []
      this.catchExecuteResult = {}
      this.activeExecuteHistory.filterBusinessKeySet.forEach(key => {
        if (key.indexOf(this.businessKey) > -1) {
          this.catchFilterBusinessKeySet.push(key)
          this.catchExecuteResult[key] = this.activeExecuteHistory.executeResult[key]
        }
      })
    }
  },
  methods: {
    singleSelect (selection, row) {
      this.seletedRows = this.seletedRows.concat(row)
    },
    singleCancel (selection, row) {
      const index = this.seletedRows.findIndex(cn => {
        return cn.id === row.id
      })
      this.seletedRows.splice(index, 1)
    },
    selectAll (selection) {
      let temp = []
      this.seletedRows.forEach(cntl => {
        temp.push(cntl.id)
      })
      selection.forEach(se => {
        if (!temp.includes(se.id)) {
          this.seletedRows.push(se)
        }
      })
    },
    selectAllCancel () {
      let temp = []
      this.tartetModels.forEach(tm => {
        temp.push(tm.id)
      })
      if (this.tableFilterParam) {
        this.seletedRows = this.seletedRows.filter(item => {
          return !temp.includes(item.id)
        })
      } else {
        this.seletedRows = []
      }
    },
    changeCollections (id) {
      if (!id) {
        return
      }
      this.activeExecuteHistoryKey = null
      this.activeExecuteHistory = null
      this.selectedCollection = this.allCollections.find(_ => {
        return _.favoritesId === id
      })
      this.activeExecuteHistory = JSON.parse(this.selectedCollection.data)
      this.activeExecuteHistory.executeResult = null
      this.activeExecuteHistory.filterBusinessKeySet = []
    },
    async getRoleList () {
      const { status, data } = await getRoleList()
      if (status === 'OK') {
        this.allRolesBackUp = data.map(_ => {
          return {
            ..._,
            key: _.name,
            label: _.displayName
          }
        })
      }
    },
    async getRolesByCurrentUser () {
      const { status, data } = await getRolesByCurrentUser()
      if (status === 'OK') {
        this.allRoles = data.map(_ => {
          return {
            ..._,
            key: _.name,
            label: _.displayName
          }
        })
      }
    },
    async getAllCollections () {
      const { status, data } = await getAllCollections()
      if (status === 'OK') {
        this.allCollections = data
      }
    },
    openAddCollectionModal (key) {
      this.isAddCollect = true
      this.toBeCollectedParams = key
      this.getRoleList()
      this.getRolesByCurrentUser()
      this.MGMT = []
      this.USE = []
      this.collectionRoleManageModal = true
      this.editCollectionName = true
    },
    openEditCollectionModal (collection) {
      this.isAddCollect = false
      this.selectedCollection = collection
      this.getRoleList()
      this.getRolesByCurrentUser()
      this.MGMT = collection.permissionToRole.MGMT
      this.USE = collection.permissionToRole.USE
      this.collectionRoleManageModal = true
      this.editCollectionName = false
    },
    async handleMgmtRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.MGMT = newTargetKeys
    },
    async handleUseRoleTransferChange (newTargetKeys, direction, moveKeys) {
      this.USE = newTargetKeys
    },
    showDeleteConfirm (id, name) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        content: name,
        onOk: () => {
          this.deleteCollection(id)
        },
        onCancel: () => {}
      })
    },
    async deleteCollection (id) {
      const { status, message } = await deleteCollections(id)
      if (status === 'OK') {
        this.selectedCollectionId = null
        this.$Message.success(message)
      }
    },
    async updateRoles (favoritesId) {
      const payload = {
        permissionToRole: { MGMT: this.MGMT, USE: this.USE }
      }
      const { status, message } = await addCollectionsRole(favoritesId, payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
      } else {
        this.$Notice.error({
          title: 'Fail',
          desc: message
        })
      }
    },
    async confirmCollection () {
      if (!this.isAddCollect) {
        await this.updateRoles(this.selectedCollection.favoritesId)
        this.collectionRoleManageModal = false
        return
      }
      if (!this.MGMT.length) {
        this.$Message.warning(this.$t('bc_mgmt_role_cannot_empty'))
        return
      }
      if (this.editCollectionName) {
        if (!this.collectionName.trim()) {
          this.$Message.warning(this.$t('bc_name_cannot_empty'))
          return
        }
        const { plugin, requestBody } = this.toBeCollectedParams
        let params = {
          collectionName: this.collectionName.trim(),
          permissionToRole: {
            MGMT: this.MGMT,
            USE: this.USE
          },
          data: JSON.stringify({
            plugin,
            requestBody
          })
        }
        const { status } = await saveBatchExecution(params)
        if (status === 'OK') {
          this.collectionRoleManageModal = false
          this.$Message.success(this.$t('save_successfully'))
        }
      } else {
        let params = JSON.parse(JSON.stringify(this.selectedCollection))
        params.permissionToRole.MGMT = this.MGMT
        params.permissionToRole.USE = this.USE
        const { status } = await updateCollections(params)
        if (status === 'OK') {
          this.collectionRoleManageModal = false
        }
      }
    },

    filterTypeChange () {
      this.filterParams = null
      this.catchExecuteResult = this.activeExecuteHistory.executeResult
      this.catchFilterBusinessKeySet = this.activeExecuteHistory.filterBusinessKeySet
    },
    filterResult () {
      if (!this.filterParams) {
        this.catchExecuteResult = this.activeExecuteHistory.executeResult
        this.catchFilterBusinessKeySet = this.activeExecuteHistory.filterBusinessKeySet
        return
      }
      this.businessKey = null
      this.$nextTick(() => {
        this.catchFilterBusinessKeySet = []
        this.catchExecuteResult = {}
        if (this.filterType === 'str') {
          this.activeExecuteHistory.filterBusinessKeySet.forEach(key => {
            let tmp = JSON.stringify(this.activeExecuteHistory.executeResult[key])
            if (tmp.indexOf(this.filterParams) > -1) {
              this.catchFilterBusinessKeySet.push(key)
              const reg = new RegExp(this.filterParams, 'g')
              tmp = tmp.replace(reg, "<span style='color:red'>" + this.filterParams + '</span>')
              this.catchExecuteResult[key] = JSON.parse(tmp)
            }
          })
        } else {
          let execRes = []
          let patt = null
          try {
            patt = new RegExp(this.filterParams, 'gmi')
            let er = JSON.stringify(this.activeExecuteHistory.executeResult)
            execRes = er.match(patt)
            execRes = this.unique(execRes)
            execRes.sort(function (a, b) {
              return b.length - a.length
            })
            execRes = execRes.filter(s => {
              return s && s.trim()
            })
          } catch (err) {
            console.log(err)
            this.$Message.error(this.$t('bc_filter_type_warn'))
            this.filterParams = null
            this.catchExecuteResult = this.activeExecuteHistory.executeResult
            this.catchFilterBusinessKeySet = this.activeExecuteHistory.filterBusinessKeySet
            return
          }
          this.activeExecuteHistory.filterBusinessKeySet.forEach(key => {
            let str = JSON.stringify(this.activeExecuteHistory.executeResult[key])
            let len = str.length
            execRes.forEach(keyword => {
              let reg = new RegExp(keyword, 'g')
              str = str.replace(reg, "<span style='color:red'>" + keyword + '</span>')
            })
            if (str.length !== len) {
              this.catchFilterBusinessKeySet.push(key)
              this.catchExecuteResult[key] = JSON.parse(str)
            }
          })
        }
      })
    },
    unique (arr) {
      return Array.from(new Set(arr))
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
    setSearchConditions () {
      this.getAllDataModels()
      if (document.querySelector('.wecube_attr-ul')) {
        document.querySelector('.wecube_attr-ul').style.width = '530px'
      }
      // this.$refs.select.setQuery(null)
      this.dataModelExpression = ':'
      this.primatKeyAttr = null
      this.primatKeyAttrList = []
      this.currentPackageName = ''
      this.currentEntityName = ''
      this.allEntityAttr = []
      this.targetEntityAttr = []
      this.filterTableParams = ''
      this.searchParameters = []
      this.userTableColumns = []
      this.seletedRows = []
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
    checkChange (totalChecked) {
      this.targetEntityAttr = totalChecked
      this.searchParameters = this.targetEntityAttr
    },
    saveSearchCondition () {
      if (!this.primatKeyAttr) {
        this.$Message.warning(this.$t('bc_primary_key') + this.$t('bc_warn_empty'))
        return
      }
      // this.isShowSearchConditions = false
      this.searchParameters = this.targetEntityAttr
    },
    async excuteSearch () {
      let { status, data } = await entityView(this.currentPackageName, this.currentEntityName)
      if (status === 'OK') {
        if (this.userTableColumns.length) {
          this.tableColumns = this.userTableColumns.map((_, i) => {
            return {
              title: _,
              key: _,
              width: 160,
              displaySeqNo: i + 1
            }
          })
        } else {
          this.tableColumns = data.map((_, i) => {
            return {
              title: _.name,
              key: _.name,
              width: 160,
              displaySeqNo: i + 1
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
    filterTableData () {
      const filtersKeys = this.userTableColumns.length ? this.userTableColumns : Object.keys(this.originTableData[0])
      this.tableData = []
      if (this.filterTableParams) {
        this.originTableData.forEach((item, index) => {
          // eslint-disable-next-line no-unused-vars
          let tmp = []
          filtersKeys.forEach(key => {
            tmp += item[key] + '@#$'
          })
          if (tmp.includes(this.filterTableParams)) {
            this.tableData.push(item)
          }
        })
      } else {
        this.tableData = this.originTableData
      }
      const selectTag = this.seletedRows.map(item => item.id)
      this.tableData.forEach(item => {
        if (selectTag.includes(item.id)) {
          item._checked = true
        }
      })
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
      this.isLoading = true
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
          this.originTableData = this.tableData
          this.displaySearchZone = false
          this.displayResultTableZone = true
        } else {
          this.$Message.warning(this.$t('bc_warn_empty'))
        }
        this.isLoading = false
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
      this.displayExecuteResultZone = false
      this.businessKey = null
      this[this.DelConfig.key] = true
    },
    batchAction () {
      this.activeExecuteHistory.requestBody.resourceDatas = this.seletedRows.map(_ => {
        return {
          id: _.id,
          businessKeyValue: _[this.activeExecuteHistory.requestBody.primatKeyAttr]
        }
      })

      this.displayResultTableZone = false

      this.getFilteredPluginInterfaceList()
      this.batchActionModalVisible = true
      this.selectedPluginParams = []
      this.pluginId = null
    },
    async getFilteredPluginInterfaceList () {
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
      const { status, data } = await await getPluginsByTargetEntityFilterRule(payload)
      if (status === 'OK') {
        this.filteredPlugins = data
      }

      // const { status, data } = await getFilteredPluginInterfaceList(this.currentPackageName, this.currentEntityName)
      // if (status === 'OK') {
      //   this.filteredPlugins = data
      // }
    },
    async excuteBatchAction () {
      let requestBody = {}
      const plugin = this.filteredPlugins.find(_ => {
        return _.serviceName === this.pluginId
      })
      const inputParameterDefinitions = this.selectedPluginParams.map(p => {
        const inputParameterValue =
          p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        }
      })
      if (this.isHistoryToBatchActionModal) {
        const {
          packageName,
          entityName,
          dataModelExpression,
          primatKeyAttr,
          searchParameters,
          businessKeyAttribute,
          resourceDatas
        } = this.activeExecuteHistory.requestBody
        requestBody = {
          packageName,
          entityName,
          dataModelExpression,
          primatKeyAttr,
          searchParameters,
          pluginConfigInterface: plugin,
          inputParameterDefinitions,
          businessKeyAttribute,
          resourceDatas
        }
      } else {
        let currentEntity = this.primatKeyAttrList.find(_ => {
          return _.name === this.primatKeyAttr
        })
        const resourceDatas = this.seletedRows.map(_ => {
          return {
            id: _.id,
            businessKeyValue: _[this.primatKeyAttr]
          }
        })
        requestBody = {
          packageName: this.currentPackageName,
          entityName: this.currentEntityName,
          dataModelExpression: this.dataModelExpression,
          primatKeyAttr: this.primatKeyAttr,
          searchParameters: this.searchParameters,
          pluginConfigInterface: plugin,
          inputParameterDefinitions,
          businessKeyAttribute: currentEntity,
          resourceDatas
        }
      }
      this.batchActionModalVisible = false
      this.operaModal = false
      this.$Notice.success({
        title: 'Success',
        desc: this.$t('bc_exect_tip'),
        duration: 1
      })

      const { status, data, message } = await batchExecution(BATCH_EXECUTION_URL, requestBody)
      // this.seletedRows = []
      if (status === 'OK') {
        this.manageExecutionResult(data, requestBody)
      }
      if (status === 'CONFIRM') {
        this.confirmModal.continueToken = data.continueToken
        this.confirmModal.check = false
        this.confirmModal.message = message
        this.confirmModal.requestBody = requestBody
        this.confirmModal.func = 'manageExecutionResult'
        this.confirmModal.isShowConfirmModal = true
      }
    },
    async confirmToExecution () {
      const { status, data } = await batchExecution(
        BATCH_EXECUTION_URL + `?continue_token=${this.confirmModal.continueToken}`,
        this.confirmModal.requestBody
      )
      if (status === 'OK') {
        this[this.confirmModal.func](data, this.confirmModal.requestBody)
        this.confirmModal.isShowConfirmModal = false
      }
    },
    manageExecutionResult (data, requestBody) {
      this.executeResult = data
      this.filterBusinessKeySet = []
      for (const key in data) {
        this.filterBusinessKeySet.push(key)
      }
      this.displayResultTableZone = false
      this.displayExecuteResultZone = false

      this.executeHistory.push({
        id: this.getCurrentDate(),
        plugin: {
          pluginName: this.pluginId,
          pluginParams: this.selectedPluginParams
        },
        requestBody: requestBody,
        executeResult: data,
        filterBusinessKeySet: this.filterBusinessKeySet
      })
      this.activeExecuteHistoryKey = this.executeHistory.length - 1
      this.activeExecuteHistory = JSON.parse(JSON.stringify(this.executeHistory[this.activeExecuteHistoryKey]))
    },
    getCurrentDate () {
      const timeStr = '-'
      const curDate = new Date()
      const curYear = curDate.getFullYear()
      const curMonth = curDate.getMonth() + 1
      const curDay = curDate.getDate()
      const curHour = curDate.getHours()
      const curMinute = curDate.getMinutes()
      const curSec = curDate.getSeconds()
      const Current = curYear + timeStr + curMonth + timeStr + curDay + ' ' + curHour + ':' + curMinute + ':' + curSec
      return Current
    },
    async executeAgain () {
      const inputParameterDefinitions = this.activeExecuteHistory.plugin.pluginParams.map(p => {
        const inputParameterValue =
          p.mappingType === 'constant' ? (p.dataType === 'number' ? Number(p.bindValue) : p.bindValue) : null
        return {
          inputParameter: p,
          inputParameterValue: inputParameterValue
        }
      })
      let requestBody = this.activeExecuteHistory.requestBody
      requestBody.inputParameterDefinitions = inputParameterDefinitions
      this.$Notice.success({
        title: 'Success',
        desc: this.$t('bc_exect_tip'),
        duration: 1
      })
      this.btnLoading = true
      const { status, data, message } = await batchExecution(BATCH_EXECUTION_URL, requestBody)
      this.btnLoading = false
      // this.seletedRows = []
      if (status === 'OK') {
        this.manageExecutionResultAgain(data, requestBody)
      }
      if (status === 'CONFIRM') {
        this.confirmModal.continueToken = data.continueToken
        this.confirmModal.message = message
        this.confirmModal.check = false
        this.confirmModal.requestBody = requestBody
        this.confirmModal.func = 'manageExecutionResultAgain'
        this.confirmModal.isShowConfirmModal = true
      }
    },
    manageExecutionResultAgain (data, requestBody) {
      this.setPluginParamsModal = false
      this.operaModal = false
      this.executeResult = data
      this.filterBusinessKeySet = []
      for (const key in data) {
        this.filterBusinessKeySet.push(key)
      }
      this.executeHistory.push({
        id: this.getCurrentDate(),
        plugin: this.activeExecuteHistory.plugin,
        requestBody: requestBody,
        executeResult: data,
        filterBusinessKeySet: this.filterBusinessKeySet
      })
      this.activeExecuteHistoryKey = this.executeHistory.length - 1
      this.activeExecuteHistory = JSON.parse(JSON.stringify(this.executeHistory[this.activeExecuteHistoryKey]))
    },
    changeActiveExecuteHistory (keyIndex) {
      if (this.executeHistory.length === 0) {
        this.clearTableSelect()
        this.clearPlugin()
        this.clearComplementParams()
        return
      }
      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.activeExecuteHistoryKey = keyIndex
      this.selectedCollectionId = null
      this.activeExecuteHistory = JSON.parse(JSON.stringify(this.executeHistory[keyIndex]))
    },
    changeActiveResultKey (key) {
      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.activeResultKey = key
      this.selectedCollectionId = null
    },
    async changePlugin () {
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
      const { status, data } = await await getPluginsByTargetEntityFilterRule(payload)

      // const { status, data } = await getFilteredPluginInterfaceList(
      //   this.activeExecuteHistory.requestBody.packageName,
      //   this.activeExecuteHistory.requestBody.entityName
      // )
      if (status === 'OK') {
        this.filteredPlugins = data
        // this.selectedPluginParams = []
        // this.pluginId = null
        this.clearPlugin()

        this.displaySearchZone = false
        this.displayResultTableZone = false
        this.batchActionModalVisible = true
        this.setPluginParamsModal = false

        this.operaModal = true
      }
    },
    changeTargetObject () {
      this.clearTableSelect()

      this.userTableColumns = []
      const { packageName, entityName, dataModelExpression } = this.activeExecuteHistory.requestBody
      this.currentPackageName = packageName
      this.currentEntityName = entityName
      this.dataModelExpression = dataModelExpression
      this.excuteSearch()

      this.displaySearchZone = false
      this.displayResultTableZone = true
      this.batchActionModalVisible = false
      this.setPluginParamsModal = false
      this.operaModal = true
    },
    changeSearchParams () {
      // const { dataModelExpression, searchParameters } = this.activeExecuteHistory.requestBody
      // this.searchParameters = searchParameters
      // this.dataModelExpression = dataModelExpression

      this.activeExecuteHistory = JSON.parse(JSON.stringify(this.defaultActiveExecuteHistory))

      this.setSearchConditions()

      this.displaySearchZone = true
      this.displayResultTableZone = false
      this.batchActionModalVisible = false
      this.setPluginParamsModal = false
      this.operaModal = true
    },
    changeParams () {
      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.batchActionModalVisible = false
      this.clearComplementParams()
      this.setPluginParamsModal = true
      this.operaModal = true
    },
    clearTableSelect () {
      this.seletedRows = []
      this.activeExecuteHistory.requestBody.resourceDatas = []
      this.clearPlugin()
    },
    clearPlugin () {
      this.pluginId = null
      // this.clearComplementParams()
      this.selectedPluginParams = []
      this.activeExecuteHistory.plugin.pluginName = ''
      this.activeExecuteHistory.plugin.pluginParams = []
    },
    clearComplementParams () {
      this.activeExecuteHistory.plugin.pluginParams.forEach(item => {
        item.bindValue = ''
      })
    },
    closeModal () {
      this.operaModal = false
      this.changeActiveExecuteHistory(this.activeExecuteHistoryKey)
    }
  },
  components: {
    FilterRules
  }
}
</script>
<style lang="scss" scoped>
.opera-modal /deep/ .ivu-modal {
  top: 50px !important;
}
$border-config: 1px solid #e8eaec;
.ivu-tree-children li {
  margin: 0 !important;
  .ivu-checkbox-wrapper {
    margin: 0 !important;
  }
}
.ivu-form-item-error .ivu-select-selection,
.ivu-form-item-error .ivu-select-arrow {
  border-color: #dcdee2 !important;
}
textarea:focus {
  outline: #96c5f7 solid 1px;
}
.tree-checked {
  border-left: 2px solid gray;
  padding-left: 8px;
}
.search-btn {
  margin-top: 8px;
}
.clear-default-style {
  list-style: none;
}
.execute-history {
  border-left: $border-config;
  border-bottom: $border-config;
  margin-top: 16px;
}
.history-record-menu {
  cursor: pointer;
  font-size: larger;
}
.res {
  border: $border-config;
}
.res-title {
  border-top: $border-config;
}
.res-content {
  left: -1px;
  .res-content-step,
  .res-content-params {
    padding: 8px;
    border-bottom: $border-config;
  }
  .res-content-result {
    margin: 2px;
  }
}
pre {
  margin-bottom: 0;
}

.we-table /deep/ .ivu-form-label-top {
  display: none;
}
.excute-result {
  right: -2px;
  padding-top: 4px;
  padding-right: 4px;
  height: calc(100vh - 210px);
}
.excute-result-search {
  // margin-right: 16px;
  border-right: $border-config;
  .excute-result-search-title {
    margin-top: 16px;
    font-size: 16px;
  }
  ul {
    margin: 4px 0;
  }
}
.excute-result-json {
  word-wrap: break-word;
  word-break: break-all;
  // overflow: scroll;
}
.business-key {
  padding: 4px 16px;
  cursor: pointer;
  color: #19be6b;
  word-break: break-all;
}
.active-key {
  background: #e5e2e2;
}
.error-key {
  color: red;
}
</style>
<style>
.ivu-card-body {
  padding: 8px !important;
}
.ivu-form-item-label {
  margin-bottom: 4px !important;
}
.ivu-form-item {
  margin-bottom: 0 !important;
}
.ivu-tree-children li {
  margin: 0 !important;
  line-height: 24px;
}
.role-transfer-title {
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  background-color: rgb(226, 222, 222);
  margin-bottom: 5px;
}
.search-params-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 260px;
}
.dispaly-instance-result {
  height: calc(100vh - 320px);
  overflow-y: auto;
}
.dispaly-result {
  height: calc(100vh - 300px);
  overflow-y: auto;
}
.confirm-msg {
  vertical-align: text-bottom;
  margin-left: 12px;
  font-size: 16px;
  color: #17233d;
  font-weight: 500;
}
</style>
