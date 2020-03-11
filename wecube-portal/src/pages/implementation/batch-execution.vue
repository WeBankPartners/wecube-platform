<template>
  <div>
    <section v-if="displaySearchZone" class="search">
      <Card v-if="displaySearchZone">
        <div class="search-zone">
          <Form :label-width="170" label-colon>
            <FormItem :label="$t('bc_define_query_objects')">
              <a @click="setSearchConditions">{{ $t('bc_define_query_objects') }}...</a>
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
                  <Col span="8" v-for="(sp, spIndex) in searchParameters" :key="spIndex" style="padding:0 8px">
                    <label>{{ sp.packageName }}-{{ sp.entityName }}.{{ sp.description }}:</label>
                    <Input v-model="sp.value" />
                  </Col>
                </Row>
              </div>
              <span v-else>({{ $t('bc_empty') }})</span>
            </FormItem>
          </Form>
        </div>
        <div class="search-btn">
          <Button type="primary" @click="excuteSearch">{{ $t('bc_execute_query') }}</Button>
          <Button @click="clearParametes">{{ $t('bc_clear_condition') }}</Button>
          <Button @click="resetParametes">{{ $t('bc_reset_query') }}</Button>
        </div>
      </Card>
      <div v-else>
        <a @click="reExcute('displaySearchZone')">{{ $t('bc_query_condition_title') }}:</a>
        <ul>
          <li v-for="(sp, spIndex) in searchParameters" :key="spIndex">
            <span> {{ sp.packageName }}-{{ sp.entityName }}:[{{ sp.description }}:{{ sp.value }}] </span>
          </li>
        </ul>
      </div>
    </section>
    <section v-if="displayResultTableZone" class="search-result-table" style="margin-top:20px;">
      <div class="we-table">
        <Card v-if="displayResultTableZone">
          <p slot="title">{{ $t('bc_search_result') }}：</p>
          <Button type="primary" :disabled="!seletedRows.length" @click="batchAction">{{
            $t('bc_batch_operation')
          }}</Button>
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
          {{ $t('bc_find') }} {{ tableData.length }} {{ $t('bc_instance') }},{{ $t('bc_selected') }}{{ seletedRowsNum
          }}{{ $t('bc_item') }},{{ $t('full_word_exec') }}{{ pluginId }}
        </a>
      </div>
    </section>
    <!-- v-if="executeHistory.length" -->
    <section v-if="executeHistory.length" class="execute-history">
      <Row>
        <Col span="4" :style="isShowHistoryMenu ? '' : 'display:none'" class="res-title">
          <h6 style="margin: 8px">
            <span>{{ $t('bc_history_record') }} </span>
          </h6>
          <ul>
            <li
              @click="changeActiveExecuteHistory(keyIndex)"
              :class="[activeExecuteHistoryKey === keyIndex ? 'active-key' : '', 'business-key']"
              v-for="(key, keyIndex) in executeHistory"
              :key="keyIndex"
            >
              <span>{{ keyIndex }}、{{ key.id }}</span>
            </li>
          </ul>
        </Col>
        <Col v-if="activeExecuteHistory" :span="isShowHistoryMenu ? 20 : 24" class="res res-content">
          <Icon
            :style="isShowHistoryMenu ? '' : 'transform: rotate(90deg);'"
            class="history-record-menu"
            type="md-menu"
            @click="isShowHistoryMenu = !isShowHistoryMenu"
          />
          <div class="res-content-step">
            <Steps :current="3">
              <Step :title="$t('bc_query_conditions')">
                <div slot="content">
                  <Tooltip :max-width="500">
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content">
                      <ul>
                        <li>{{ $t('bc_query_path') }}:{{ dataModelExpression }}</li>
                        <li v-for="(sp, spIndex) in activeExecuteHistory.requestBody.searchParameters" :key="spIndex">
                          <span> {{ sp.packageName }}-{{ sp.entityName }}:[{{ sp.description }}:{{ sp.value }}] </span>
                        </li>
                      </ul>
                    </div>
                  </Tooltip>
                  <Button size="small" @click="changeSearchParams" type="primary" ghost>{{
                    $t('bc_reset_query')
                  }}</Button>
                </div>
              </Step>
              <Step :title="$t('bc_execution_instance')">
                <div slot="content">
                  <Tooltip>
                    <Icon type="ios-information-circle-outline" />
                    <div slot="content">
                      <p
                        :key="targetIndex"
                        v-for="(target, targetIndex) in activeExecuteHistory.requestBody.resourceDatas"
                      >
                        {{ target.businessKeyValue }}
                      </p>
                    </div>
                  </Tooltip>
                  <Button size="small" @click="changeTargetObject" type="primary" ghost>{{
                    $t('bc_change_instance')
                  }}</Button>
                </div>
              </Step>
              <Step :title="$t('bc_execution_plugin')" content="">
                <div slot="content">
                  <Tooltip :content="activeExecuteHistory.plugin.pluginName">
                    <Icon type="ios-information-circle-outline" />
                  </Tooltip>
                  <Button size="small" @click="changePlugin" type="primary" ghost>{{ $t('bc_change_plugin') }}</Button>
                </div>
              </Step>
            </Steps>
          </div>
          <div v-if="activeExecuteHistory" class="res-content-params">
            <Form label-position="right" :label-width="100">
              <Row>
                <template v-for="(item, index) in activeExecuteHistory.plugin.pluginParams">
                  <Col span="8" v-if="item.mappingType === 'constant'" :key="index">
                    <FormItem :label="item.name" :key="index">
                      <Input v-model="item.bindValue" />
                    </FormItem>
                  </Col>
                </template>
                <Col :span="executeAgainBtnSpan">
                  <Button type="primary" style="float:right" @click="executeAgain">{{ $t('bc_execute') }}</Button>
                </Col>
              </Row>
            </Form>
          </div>
          <div class="res-content-result">
            <Row>
              <Col span="6" class="excute-result excute-result-search">
                <Input v-model="businessKey" placeholder="Filter instance" />
                <p class="excute-result-search-title">{{ activeExecuteHistory.plugin.pluginName }}</p>
                <ul v-if="activeExecuteHistory.filterBusinessKeySet.length">
                  <li
                    @click="changeActiveResultKey(key)"
                    :class="[
                      activeResultKey === key ? 'active-key' : '',
                      'business-key',
                      catchExecuteResult[key].errorCode === '1' ? 'error-key' : ''
                    ]"
                    v-for="(key, keyIndex) in catchFilterBusinessKeySet"
                    :key="keyIndex"
                  >
                    <!-- activeExecuteHistory.executeResult[key].errorCode === '1' ? 'error-key' : '' -->
                    <span>{{ key }}</span>
                  </li>
                </ul>
                <p v-else>No Data</p>
              </Col>
              <Col span="18" class="excute-result excute-result-json">
                <Input v-model="filterParams" placeholder="Filter result, e.g :error or /[0-9]+/" />
                <div>
                  <pre
                    style="min-height: 300px;"
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
    <Modal :width="700" v-model="isShowSearchConditions" :title="$t('bc_define_query_objects')">
      <Form :label-width="130" label-colon>
        <FormItem :rules="{ required: true }" :show-message="false" :label="$t('bc_start_path')">
          <Select v-model="selectedEntityType" ref="select" filterable @on-change="changeEntityType">
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
        <FormItem :rules="{ required: true }" :show-message="false" :label="$t('bc_query_path')">
          <PathExp
            :rootEntity="selectedEntityType"
            :allDataModelsWithAttrs="allEntityType"
            v-model="dataModelExpression"
          ></PathExp>
        </FormItem>
        <FormItem :label="$t('bc_target_type')">
          <span>{{ currentPackageName }}:{{ currentEntityName }}</span>
        </FormItem>
        <FormItem :rules="{ required: true }" :show-message="false" :label="$t('bc_primary_key')">
          <Select filterable v-model="currentEntityAttr">
            <Option v-for="entityAttr in currentEntityAttrList" :value="entityAttr.name" :key="entityAttr.id">{{
              entityAttr.name
            }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('bc_query_condition')" class="tree-style">
          <Row>
            <Col span="12">
              <Tree :data="allEntityAttr" @on-check-change="checkChange" show-checkbox multiple></Tree>
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
      </Form>
      <div slot="footer">
        <Button type="primary" @click="saveSearchCondition">
          {{ $t('confirm') }}
        </Button>
      </div>
    </Modal>

    <Modal v-model="batchActionModalVisible" :title="$t('bc_batch_operation')">
      <Form label-position="right" :label-width="150">
        <FormItem :label="$t('plugin')" :rules="{ required: true }" :show-message="false">
          <Select filterable clearable v-model="pluginId">
            <Option v-for="(item, index) in filteredPlugins" :value="item.serviceName" :key="index">{{
              item.serviceDisplayName
            }}</Option>
          </Select>
        </FormItem>
        <template v-for="(item, index) in selectedPluginParams">
          <FormItem :label="item.name" :key="index">
            <Input v-if="item.mappingType === 'constant'" v-model="item.bindValue" />
            <span v-else>{{ item.mappingType === 'entity' ? $t('bc_from_CI') : $t('bc_from_system') }}</span>
          </FormItem>
        </template>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="excuteBatchAction" :disabled="!this.pluginId">
          {{ $t('confirm') }}
        </Button>
      </div>
    </Modal>

    <Modal v-model="DelConfig.isDisplay" width="360">
      <p slot="header" style="color:#f60;text-align:center">
        <Icon type="ios-information-circle"></Icon>
        <span>{{ $t('confirm') }}</span>
      </p>
      <div style="text-align:center">
        <p>{{ $t('bc_warn_del') }}</p>
      </div>
      <div slot="footer">
        <Button type="warning" size="large" long @click="del">{{ $t('bc_continue') }}</Button>
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
      isHistoryToBatchActionModal: false,
      pluginId: null,
      selectedPluginParams: [],
      allPlugins: [],
      filteredPlugins: [],

      isShowHistoryMenu: true,
      executeResult: {},
      filterBusinessKeySet: [],
      activeResultKey: null,
      businessKey: '',

      activeExecuteHistoryKey: 0,
      activeExecuteHistory: {},
      executeHistory: [],
      catchExecuteResult: {},
      catchFilterBusinessKeySet: [],
      // activeExecuteHistory: {
      //   id: '2020-03-10 16:26:08',
      //   plugin: {
      //     pluginName: 'qcloud/vm/stop',
      //     pluginParams: [
      //       {
      //         id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //         pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //         type: 'INPUT',
      //         name: 'guid',
      //         dataType: 'string',
      //         mappingType: 'entity',
      //         mappingEntityExpression: 'wecmdb:resource_instance.id',
      //         mappingSystemVariableName: null,
      //         required: 'Y',
      //         sensitiveData: null,
      //         bindValue: ''
      //       },
      //       {
      //         id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //         pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //         type: 'INPUT',
      //         name: 'id',
      //         dataType: 'string',
      //         mappingType: 'entity',
      //         mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //         mappingSystemVariableName: null,
      //         required: 'Y',
      //         sensitiveData: null,
      //         bindValue: ''
      //       },
      //       {
      //         id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //         pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //         type: 'INPUT',
      //         name: 'provider_params',
      //         dataType: 'string',
      //         mappingType: 'entity',
      //         mappingEntityExpression:
      //           'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //         mappingSystemVariableName: null,
      //         required: 'Y',
      //         sensitiveData: null,
      //         bindValue: ''
      //       }
      //     ]
      //   },
      //   requestBody: {
      //     packageName: 'wecmdb',
      //     entityName: 'resource_instance',
      //     dataModelExpression: 'wecmdb:resource_instance',
      //     searchParameters: [],
      //     pluginConfigInterface: {
      //       id: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //       pluginConfigId: 'qcloud__v1.8.1__vm',
      //       action: 'stop',
      //       serviceName: 'qcloud/vm/stop',
      //       serviceDisplayName: 'qcloud/vm/stop',
      //       path: '/qcloud/v1/vm/stop',
      //       httpMethod: '',
      //       isAsyncProcessing: 'N',
      //       inputParameters: [
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'guid',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.id',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'id',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'provider_params',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression:
      //             'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         }
      //       ],
      //       outputParameters: [
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__errorCode',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'OUTPUT',
      //           name: 'errorCode',
      //           dataType: 'string',
      //           mappingType: 'context',
      //           mappingEntityExpression: null,
      //           mappingSystemVariableName: null,
      //           required: 'N',
      //           sensitiveData: null
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__errorMessage',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'OUTPUT',
      //           name: 'errorMessage',
      //           dataType: 'string',
      //           mappingType: 'context',
      //           mappingEntityExpression: null,
      //           mappingSystemVariableName: null,
      //           required: 'N',
      //           sensitiveData: null
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__guid',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'OUTPUT',
      //           name: 'guid',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.id',
      //           mappingSystemVariableName: null,
      //           required: 'N',
      //           sensitiveData: null
      //         }
      //       ]
      //     },
      //     inputParameterDefinitions: [
      //       {
      //         inputParameter: {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'guid',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.id',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         inputParameterValue: null
      //       },
      //       {
      //         inputParameter: {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'id',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         inputParameterValue: null
      //       },
      //       {
      //         inputParameter: {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'provider_params',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression:
      //             'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         inputParameterValue: null
      //       }
      //     ],
      //     businessKeyAttribute: {
      //       id: 'wecmdb__8__resource_instance__key_name',
      //       pluginPackageAttribute: null,
      //       name: 'key_name',
      //       description: '唯一名称',
      //       dataType: 'str',
      //       key: 'wecmdbresource_instance0',
      //       index: 0,
      //       title: 'key_name',
      //       entityName: 'resource_instance',
      //       packageName: 'wecmdb',
      //       nodeKey: 15
      //     },
      //     resourceDatas: [
      //       {
      //         id: '0015_0000000013',
      //         businessKeyValue: 'GZP4_SF_CS_APP_10.128.36.10'
      //       },
      //       {
      //         id: '0015_0000000014',
      //         businessKeyValue: 'GZP4_SF_CS_APP_10.128.36.11'
      //       }
      //     ]
      //   },
      //   executeResult: {
      //     'GZP4_SF_CS_APP_10.128.36.11': {
      //       errorCode: '1',
      //       result: {
      //         errorCode: '1',
      //         errorMessage: 'this is response one'
      //       }
      //     },
      //     'GZP4_SF_CS_APP_10.128.36.10': {
      //       errorCode: '1',
      //       result: {
      //         errorCode: '1',
      //         errorMessage: 'this is response two'
      //       }
      //     }
      //   },
      //   filterBusinessKeySet: ['GZP4_SF_CS_APP_10.128.36.11', 'GZP4_SF_CS_APP_10.128.36.10']
      // },
      // executeHistory: [
      //   {
      //     id: '2020-03-10 16:26:08',
      //     plugin: {
      //       pluginName: 'qcloud/vm/stop',
      //       pluginParams: [
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'guid',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.id',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'id',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         },
      //         {
      //           id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //           pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //           type: 'INPUT',
      //           name: 'provider_params',
      //           dataType: 'string',
      //           mappingType: 'entity',
      //           mappingEntityExpression:
      //             'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //           mappingSystemVariableName: null,
      //           required: 'Y',
      //           sensitiveData: null,
      //           bindValue: ''
      //         }
      //       ]
      //     },
      //     requestBody: {
      //       packageName: 'wecmdb',
      //       entityName: 'resource_instance',
      //       dataModelExpression: 'wecmdb:resource_instance',
      //       searchParameters: [],
      //       pluginConfigInterface: {
      //         id: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //         pluginConfigId: 'qcloud__v1.8.1__vm',
      //         action: 'stop',
      //         serviceName: 'qcloud/vm/stop',
      //         serviceDisplayName: 'qcloud/vm/stop',
      //         path: '/qcloud/v1/vm/stop',
      //         httpMethod: '',
      //         isAsyncProcessing: 'N',
      //         inputParameters: [
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'guid',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression: 'wecmdb:resource_instance.id',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           },
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'id',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           },
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'provider_params',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression:
      //               'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           }
      //         ],
      //         outputParameters: [
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__errorCode',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'OUTPUT',
      //             name: 'errorCode',
      //             dataType: 'string',
      //             mappingType: 'context',
      //             mappingEntityExpression: null,
      //             mappingSystemVariableName: null,
      //             required: 'N',
      //             sensitiveData: null
      //           },
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__errorMessage',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'OUTPUT',
      //             name: 'errorMessage',
      //             dataType: 'string',
      //             mappingType: 'context',
      //             mappingEntityExpression: null,
      //             mappingSystemVariableName: null,
      //             required: 'N',
      //             sensitiveData: null
      //           },
      //           {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__OUTPUT__guid',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'OUTPUT',
      //             name: 'guid',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression: 'wecmdb:resource_instance.id',
      //             mappingSystemVariableName: null,
      //             required: 'N',
      //             sensitiveData: null
      //           }
      //         ]
      //       },
      //       inputParameterDefinitions: [
      //         {
      //           inputParameter: {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__guid',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'guid',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression: 'wecmdb:resource_instance.id',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           },
      //           inputParameterValue: null
      //         },
      //         {
      //           inputParameter: {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__id',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'id',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression: 'wecmdb:resource_instance.asset_code',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           },
      //           inputParameterValue: null
      //         },
      //         {
      //           inputParameter: {
      //             id: 'qcloud__v1.8.1__vm__stop__resource_instance__INPUT__provider_params',
      //             pluginConfigInterfaceId: 'qcloud__v1.8.1__vm__stop__resource_instance',
      //             type: 'INPUT',
      //             name: 'provider_params',
      //             dataType: 'string',
      //             mappingType: 'entity',
      //             mappingEntityExpression:
      //               'wecmdb:resource_instance.resource_set>wecmdb:resource_set.business_zone>wecmdb:business_zone.network_zone>wecmdb:network_zone.data_center>wecmdb:data_center.auth_parameter',
      //             mappingSystemVariableName: null,
      //             required: 'Y',
      //             sensitiveData: null,
      //             bindValue: ''
      //           },
      //           inputParameterValue: null
      //         }
      //       ],
      //       businessKeyAttribute: {
      //         id: 'wecmdb__8__resource_instance__key_name',
      //         pluginPackageAttribute: null,
      //         name: 'key_name',
      //         description: '唯一名称',
      //         dataType: 'str',
      //         key: 'wecmdbresource_instance0',
      //         index: 0,
      //         title: 'key_name',
      //         entityName: 'resource_instance',
      //         packageName: 'wecmdb',
      //         nodeKey: 15
      //       },
      //       resourceDatas: [
      //         {
      //           id: '0015_0000000013',
      //           businessKeyValue: 'GZP4_SF_CS_APP_10.128.36.10'
      //         },
      //         {
      //           id: '0015_0000000014',
      //           businessKeyValue: 'GZP4_SF_CS_APP_10.128.36.11'
      //         }
      //       ]
      //     },
      //     executeResult: {
      //       'GZP4_SF_CS_APP_10.128.36.11': {
      //         errorCode: '1',
      //         result: {
      //           errorCode: '1',
      //           errorMessage: 'this is response one'
      //         }
      //       },
      //       'GZP4_SF_CS_APP_10.128.36.10': {
      //         errorCode: '1',
      //         result: {
      //           errorCode: '1',
      //           errorMessage: 'this is response two'
      //         }
      //       }
      //     },
      //     filterBusinessKeySet: ['GZP4_SF_CS_APP_10.128.36.11', 'GZP4_SF_CS_APP_10.128.36.10']
      //   }
      // ],
      // catchExecuteResult: {
      //   'GZP4_SF_CS_APP_10.128.36.11': {
      //     errorCode: '1',
      //     result: {
      //       errorCode: '1',
      //       errorMessage: 'this is response one'
      //     }
      //   },
      //   'GZP4_SF_CS_APP_10.128.36.10': {
      //     errorCode: '1',
      //     result: {
      //       errorCode: '1',
      //       errorMessage: 'this is response two'
      //     }
      //   }
      // },
      // catchFilterBusinessKeySet: ['GZP4_SF_CS_APP_10.128.36.11', 'GZP4_SF_CS_APP_10.128.36.10'],
      filterParams: null
    }
  },
  mounted () {},
  computed: {
    businessKeyContent: function () {
      if (this.activeResultKey !== null) {
        return this.catchExecuteResult[this.activeResultKey]
      }
    },
    // businessKeyContent: function () {
    //   if (this.activeResultKey !== null) {
    //     return this.activeExecuteHistory.executeResult[this.activeResultKey]
    //   }
    // },
    executeAgainBtnSpan: function () {
      const paramsNum = this.activeExecuteHistory.plugin.pluginParams.filter(item => {
        return item.mappingType === 'constant'
      }).length
      return 24 - (paramsNum % 3) * 8
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
    activeExecuteHistory: function (val) {
      this.catchExecuteResult = val.executeResult
      this.catchFilterBusinessKeySet = val.filterBusinessKeySet
      this.filterParams = null
      this.businessKey = null
    },
    businessKey: function (val) {
      // this.filterBusinessKeySet = []
      // for (const key in this.executeResult) {
      //   if (key.indexOf(this.businessKey) > -1) {
      //     this.filterBusinessKeySet.push(key)
      //   }
      // }
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
    },
    filterParams (val) {
      console.log(val)
      if (!val) {
        this.catchExecuteResult = this.activeExecuteHistory.executeResult
        this.catchFilterBusinessKeySet = this.activeExecuteHistory.filterBusinessKeySet
        return
      }
      this.businessKey = null
      this.catchFilterBusinessKeySet = []
      this.catchExecuteResult = {}
      this.activeExecuteHistory.filterBusinessKeySet.forEach(key => {
        let tmp = JSON.stringify(this.activeExecuteHistory.executeResult[key])
        if (tmp.indexOf(val) > -1) {
          this.catchFilterBusinessKeySet.push(key)
          const reg = new RegExp(val, 'g')
          // let tempHistory = JSON.parse(tmp)
          // for (let k in tempHistory.result) {
          //   console.log(k)
          //   console.log(tempHistory.result)
          //   tempHistory.result[k] = tempHistory.result[k].replace(
          //     reg,
          //     "<span style='color:red'>" + val + '</span>'
          //   )
          // }
          tmp = tmp.replace(reg, "<span style='color:red'>" + val + '</span>')
          this.catchExecuteResult[key] = JSON.parse(tmp)
          // this.catchExecuteResult[key] = tempHistory
        }
      })
      console.log(this.catchFilterBusinessKeySet)
    }
  },
  methods: {
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
        this.$Message.warning(this.$t('bc_primary_key') + this.$t('bc_warn_empty'))
        return
      }
      this.isShowSearchConditions = false
      this.searchParameters = this.targetEntityAttr
    },
    async excuteSearch () {
      let { status, data } = await entityView(this.currentPackageName, this.currentEntityName)
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
          this.$Message.warning(this.$t('bc_warn_empty'))
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
      this.displayExecuteResultZone = false
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
      this.pluginId = null
    },
    async getFilteredPluginInterfaceList () {
      const { status, data } = await getFilteredPluginInterfaceList(this.currentPackageName, this.currentEntityName)
      if (status === 'OK') {
        this.filteredPlugins = data
      }
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
          searchParameters,
          businessKeyAttribute,
          resourceDatas
        } = this.activeExecuteHistory.requestBody
        requestBody = {
          packageName,
          entityName,
          dataModelExpression,
          searchParameters,
          pluginConfigInterface: plugin,
          inputParameterDefinitions,
          businessKeyAttribute,
          resourceDatas
        }
      } else {
        let currentEntity = this.currentEntityAttrList.find(_ => {
          return _.name === this.currentEntityAttr
        })
        const resourceDatas = this.seletedRows.map(_ => {
          return {
            id: _.id,
            businessKeyValue: _[this.currentEntityAttr]
          }
        })
        requestBody = {
          packageName: this.currentPackageName,
          entityName: this.currentEntityName,
          dataModelExpression: this.dataModelExpression,
          searchParameters: this.searchParameters,
          pluginConfigInterface: plugin,
          inputParameterDefinitions,
          businessKeyAttribute: currentEntity,
          resourceDatas
        }
      }
      this.batchActionModalVisible = false

      const { status, data } = await batchExecution(requestBody)
      this.seletedRows = []
      if (status === 'OK') {
        this.executeResult = data
        this.filterBusinessKeySet = []
        for (const key in data) {
          this.filterBusinessKeySet.push(key)
        }
        this.displayResultTableZone = false
        this.displayExecuteResultZone = false

        this.executeHistory.push({
          id: new Date().format('yyyy-MM-dd hh:mm:ss'),
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
      }
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
      const { status, data } = await batchExecution(requestBody)
      this.seletedRows = []
      if (status === 'OK') {
        this.executeResult = data
        this.filterBusinessKeySet = []
        for (const key in data) {
          this.filterBusinessKeySet.push(key)
        }
        this.executeHistory.push({
          id: new Date().format('yyyy-MM-dd hh:mm:ss'),
          plugin: this.activeExecuteHistory.plugin,
          requestBody: requestBody,
          executeResult: data,
          filterBusinessKeySet: this.filterBusinessKeySet
        })
        this.activeExecuteHistoryKey = this.executeHistory.length - 1
        this.activeExecuteHistory = JSON.parse(JSON.stringify(this.executeHistory[this.activeExecuteHistoryKey]))
      }
    },
    changeActiveExecuteHistory (keyIndex) {
      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.activeExecuteHistoryKey = keyIndex
      this.activeExecuteHistory = JSON.parse(JSON.stringify(this.executeHistory[keyIndex]))
    },
    changeActiveResultKey (key) {
      this.displaySearchZone = false
      this.displayResultTableZone = false
      this.activeResultKey = key
    },
    async changePlugin () {
      const { status, data } = await getFilteredPluginInterfaceList(
        this.activeExecuteHistory.requestBody.packageName,
        this.activeExecuteHistory.requestBody.entityName
      )
      if (status === 'OK') {
        this.filteredPlugins = data
        this.selectedPluginParams = []
        this.pluginId = null
        this.batchActionModalVisible = true
        this.isHistoryToBatchActionModal = true
      }
    },
    changeTargetObject () {
      this.displaySearchZone = false
      this.displayResultTableZone = true
      const { packageName, entityName, dataModelExpression } = this.activeExecuteHistory.requestBody
      this.currentPackageName = packageName
      this.currentEntityName = entityName
      this.dataModelExpression = dataModelExpression
      this.excuteSearch()
    },
    changeSearchParams () {
      const { dataModelExpression, searchParameters } = this.activeExecuteHistory.requestBody
      this.searchParameters = searchParameters
      this.dataModelExpression = dataModelExpression
      this.displaySearchZone = true
      this.displayResultTableZone = false
    }
  },
  components: {
    PathExp
  }
}
</script>
<style lang="scss" scoped>
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
  padding-right: 4px;
  min-height: 300px;
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
  border: $border-config;
  word-wrap: break-word;
  word-break: break-all;
  // overflow: scroll;
}
.business-key {
  padding: 0 16px;
  cursor: pointer;
  color: #19be6b;
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
</style>
