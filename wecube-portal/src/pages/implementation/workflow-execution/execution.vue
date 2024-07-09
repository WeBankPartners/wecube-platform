<!--编排执行和查看页面-->
<template>
  <div class="workflow-execution">
    <div class="workflow-execution-header">
      <div class="back-header">
        <Icon size="22" type="md-arrow-back" class="icon" @click="handleBack" />
      </div>
      <div class="form">
        <Form label-position="left">
          <FormItem v-if="isEnqueryPage" :label-width="100" :label="$t('orchs')">
            <Select
              v-model="selectedFlowInstance"
              style="width: 60%"
              filterable
              clearable
              :placeholder="$t('fe_flowname_placeholder')"
              @on-open-change="getProcessInstances(false)"
              @on-clear="clearHistoryOrch"
              @on-change="queryHandler"
            >
              <Option
                v-for="item in allFlowInstances"
                :value="item.id"
                :key="item.id"
                :label="
                  item.procInstName +
                  '  ' +
                  '[' +
                  item.version +
                  ']  ' +
                  item.entityDisplayName +
                  '  ' +
                  (item.operator || 'operator') +
                  '  ' +
                  (item.createdTime || '0000-00-00 00:00:00') +
                  '  ' +
                  getStatusStyleAndName(item.status, 'label')
                "
              >
                <div style="display: flex; justify-content: space-between">
                  <div>
                    <span style="color: #2b85e4">{{ item.procInstName + ' ' }}</span>
                    <span style="color: #2b85e4">{{ '[' + item.version + '] ' }}</span>
                    <Tag style="color: #515a6e">{{ item.entityDisplayName + ' ' }}</Tag>
                  </div>
                  <div style="display: flex; align-items: center">
                    <span style="color: #515a6e; margin-right: 20px">{{ item.operator || 'operator' }}</span>
                    <span style="color: #ccc">{{ (item.createdTime || '0000-00-00 00:00:00') + ' ' }}</span>
                    <div style="width: 100px">
                      <span :style="getStatusStyleAndName(item.status, 'style')">{{
                        getStatusStyleAndName(item.status, 'label')
                      }}</span>
                    </div>
                  </div>
                </div>
              </Option>
            </Select>
            <!-- <Button type="info" @click="queryHandler">{{ $t('query_orch') }}</Button> -->
            <!--暂停执行-->
            <Button
              type="warning"
              @click="flowControlHandler('stop')"
              style="background-color: #826bea; border-color: #826bea"
              v-if="currentInstanceStatusForNodeOperation === 'InProgress'"
              icon="md-pause"
              >{{ $t('be_pause') }}</Button
            >
            <!--继续执行-->
            <Button
              type="success"
              @click="flowControlHandler('recover')"
              v-if="currentInstanceStatusForNodeOperation === 'Stop'"
              icon="md-play"
              >{{ $t('be_continue') }}</Button
            >
            <!--终止执行-->
            <Button
              v-if="currentInstanceStatusForNodeOperation === 'InProgress' && subProc !== 'sub'"
              type="warning"
              @click="stopHandler"
              icon="md-square"
              >{{ $t('stop_orch') }}</Button
            >
            <!-- disabled="currentInstanceStatus || stopSuccess"  stop_orch -->
            <!--定时执行-->
            <Button
              v-if="currentInstanceStatusForNodeOperation === 'Completed'"
              type="primary"
              @click="setTimedExecution"
              icon="md-stopwatch"
              >{{ $t('timed_execution') }}</Button
            >
            <!-- :disabled="canAbleToSetting" timed_execution -->
          </FormItem>
          <Col v-if="!isEnqueryPage" span="7">
            <FormItem :label-width="100" :label="$t('select_orch')">
              <Select
                label
                v-model="selectedFlow"
                :disabled="Boolean(subProcId)"
                @on-change="orchestrationSelectHandler"
                @on-open-change="getAllFlow"
                filterable
                clearable
                @on-clear="clearFlow"
              >
                <Option v-for="item in allFlows" :value="item.procDefId" :key="item.procDefId"
                  >{{ item.procDefName }} [{{ item.procDefVersion }}] {{ item.createdTime }}</Option
                >
              </Select>
            </FormItem>
          </Col>
          <Col v-if="!isEnqueryPage" span="12" offset="0">
            <FormItem required :label-width="100" :label="$t('target_object')">
              <Select
                style="width: 80%"
                label
                v-model="selectedTarget"
                :disabled="Boolean(subProcId)"
                @on-change="onTargetSelectHandler"
                @on-open-change="getTargetOptions"
                filterable
                clearable
                @on-clear="clearTarget"
              >
                <Option v-for="item in allTarget" :value="item.id" :key="item.id">{{ item.displayName }}</Option>
              </Select>
              <Button
                :disabled="
                  isExecuteActive ||
                  !showExcution ||
                  !this.selectedTarget ||
                  !this.selectedFlow ||
                  !isShowExect ||
                  Boolean(subProcId)
                "
                :loading="btnLoading"
                type="info"
                @click="excutionFlow"
                >{{ $t('execute') }}</Button
              >
            </FormItem>
          </Col>
          <!--模板收藏功能-->
          <Col v-if="!isEnqueryPage && selectedFlow" span="5" style="text-align: right">
            <Tooltip :content="selectedFlowObj.collected ? $t('be_cancel_save') : $t('bc_save')" placement="left">
              <Icon
                style="cursor: pointer; margin-right: 10px"
                :size="28"
                :color="selectedFlowObj.collected ? '#ebac42' : ''"
                :type="selectedFlowObj.collected ? 'ios-star' : 'ios-star-outline'"
                @click="handleStar(selectedFlowObj)"
              />
            </Tooltip>
          </Col>
        </Form>
      </div>
    </div>
    <Row id="graph-container">
      <Col span="7" style="border-right: 1px solid #d3cece; text-align: center; height: 100%; position: relative">
        <div class="graph-container" id="flow" style="height: 90%"></div>
        <!--重置-->
        <Button class="reset-button" size="small" @click="ResetFlow">ResetZoom</Button>
        <span class="set-data-title">{{ $t('fe_view_flow') }}</span>
        <!--预览数据-->
        <Button
          v-if="!isEnqueryPage && selectedFlow && selectedTarget"
          class="set-data-button"
          size="small"
          type="primary"
          @click="setFlowDataForAllNodes"
          >{{ $t('fe_node_datalist') }}</Button
        >
      </Col>
      <Col span="17" style="text-align: center; text-align: center; height: 100%; position: relative">
        <div class="graph-container" id="graph" style="height: 90%"></div>
        <!--重置-->
        <Button class="reset-button" size="small" @click="ResetModel">ResetZoom</Button>
        <span class="set-data-title">{{ $t('fe_preview_using_data') }}</span>
        <!--预览数据-->
        <Button
          v-if="selectedFlow && selectedTarget"
          class="set-data-button"
          size="small"
          type="primary"
          @click="showModelDataWithFlow"
          >{{ $t('fe_data_nodelist') }}</Button
        >
        <Spin size="large" fix v-show="isLoading">
          <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
          <div>{{ $t('loading') }}</div>
        </Spin>
      </Col>
    </Row>
    <!--左侧预览弹窗(新建)-->
    <BaseDrawer :title="$t('overview')" :visible.sync="flowNodesWithDataModalVisible" width="70%" :scrollable="true">
      <template slot-scope="{ maxHeight }" slot="content">
        <Table
          border
          :columns="flowNodesWithModelDataColums"
          :max-height="maxHeight"
          :data="allFlowNodesModelData"
          @on-select="allFlowNodesSingleSelect"
          @on-select-cancel="allFlowNodesSingleCancel"
          @on-select-all-cancel="allFlowNodesSelectAllCancel"
          @on-select-all="allFlowNodesSelectAll"
          :span-method="flowNodeDataHandleSpan"
        >
          <template slot-scope="{ row }" slot="orderedNo">
            <span>{{ row.orderedNo + ' ' + row.nodeName }}</span>
          </template>
        </Table>
      </template>
      <template slot="footer">
        <Button type="default" @click="flowNodesWithDataModalVisible = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="flowNodesTargetModelConfirm">{{ $t('submit') }}</Button>
      </template>
    </BaseDrawer>
    <!--右侧预览弹窗(新建、查看)-->
    <BaseDrawer :title="$t('overview')" :visible.sync="targetWithFlowModalVisible" width="70%" :scrollable="true">
      <template slot-scope="{ maxHeight }" slot="content">
        <Table
          border
          :columns="targetWithFlowModelColums"
          :max-height="maxHeight"
          :data="modelDataWithFlowNodes"
          :span-method="modelDataHandleSpan"
        >
          <template slot-scope="{ row }" slot="nodeTitle">
            <div style="margin-bottom: 5px" v-for="title in row.nodeTitle.split(';')" :key="title">
              {{ title || '-' }}
            </div>
          </template>
        </Table>
      </template>
    </BaseDrawer>
    <!--节点选择操作弹窗(查看)-->
    <BaseDrawer
      :title="$t('select_an_operation')"
      :visible.sync="workflowActionModalVisible"
      width="70%"
      :scrollable="true"
      class="json-viewer"
    >
      <template slot="content">
        <HeaderTitle title="节点操作">
          <div ref="action" style="padding-left: 20px">
            <Button
              style="background-color: #bf22e0; color: white"
              v-if="
                ['Risky'].includes(currentNodeStatus) && currentInstanceStatusForNodeOperation != 'InternallyTerminated'
              "
              @click="workFlowActionHandler('risky')"
              :loading="btnLoading"
              >{{ $t('dangerous_confirm') }}</Button
            >
            <Button
              type="primary"
              v-if="
                ['NotStarted', 'Risky'].includes(currentNodeStatus) &&
                currentInstanceStatusForNodeOperation != 'InternallyTerminated'
              "
              @click="workFlowActionHandler('dataSelection')"
              :loading="btnLoading"
              >{{ $t('data_selection') }}</Button
            >
            <Button
              type="primary"
              v-if="
                ['Faulted', 'Timeouted'].includes(currentNodeStatus) &&
                currentInstanceStatusForNodeOperation != 'InternallyTerminated'
              "
              @click="workFlowActionHandler('partialRetry')"
              :loading="btnLoading"
              >{{ $t('partial_retry') }}</Button
            >
            <Button
              type="warning"
              v-if="
                ['Faulted', 'Timeouted', 'Risky'].includes(currentNodeStatus) &&
                currentInstanceStatusForNodeOperation != 'InternallyTerminated'
              "
              @click="workFlowActionHandler('skip')"
              :loading="btnLoading"
              style="margin-left: 10px"
              >{{ $t('skip') }}</Button
            >
            <div v-if="noActionFlag" class="no-data">暂无操作</div>
          </div>
        </HeaderTitle>
        <HeaderTitle title="节点信息">
          <template v-if="nodeDetailResponseHeader && Object.keys(nodeDetailResponseHeader).length > 0">
            <json-viewer :value="nodeDetailResponseHeader" :expand-depth="5"></json-viewer>
          </template>
          <div v-else class="no-data">暂无数据</div>
        </HeaderTitle>
        <HeaderTitle title="API调用">
          <Table :columns="nodeDetailColumns" tooltip="true" :data="nodeDetailIO"> </Table>
        </HeaderTitle>
      </template>
    </BaseDrawer>
    <!--节点重试/反选数据弹窗(查看)-->
    <Modal
      :title="currentNodeTitle"
      v-model="retryTargetModalVisible"
      :scrollable="true"
      :ok-text="$t('submit')"
      class="model_target"
      :width="1100"
      @on-ok="retryTargetModelConfirm"
    >
      <Input
        v-model="retryTableFilterParam"
        placeholder="displayName filter"
        style="width: 300px; margin-bottom: 8px"
      />
      <Table
        border
        ref="selection"
        max-height="500"
        @on-select="retrySingleSelect"
        @on-select-cancel="retrySingleCancel"
        @on-select-all-cancel="retrySelectAllCancel"
        @on-select-all="retrySelectAll"
        :columns="retryTargetModelColums.filter(col => !col.disabled)"
        :data="retryTartetModels"
      >
      </Table>
      <div slot="footer">
        <span v-if="isNodeCanBindData" style="font-size: 12px; color: red; margin-right: 8px">{{
          $t('be_dynamic_binding_warning')
        }}</span>
        <Button @click="retryTargetModalVisible = false">{{ $t('cancel') }}</Button>
        <Button type="primary" :disabled="isNodeCanBindData" @click="retryTargetModelConfirm">{{
          $t('submit')
        }}</Button>
      </div>
    </Modal>
    <!--左侧编排节点弹窗(新建)-->
    <BaseDrawer :title="currentNodeTitle" :visible.sync="targetModalVisible" width="70%" :scrollable="true">
      <template slot-scope="{ maxHeight }" slot="content">
        <Input
          v-model="tableFilterParam"
          :placeholder="$t('please_input') + $t('object')"
          style="width: 400px; margin-bottom: 8px"
        />
        <!-- {{ catchNodeTableList.length }} -->
        <Table
          border
          ref="selection"
          :max-height="maxHeight - 100"
          @on-select="singleSelect"
          @on-select-cancel="singleCancel"
          @on-select-all-cancel="selectAllCancel"
          @on-select-all="selectAll"
          :columns="targetModelColums.filter(col => !col.disabled)"
          :data="tartetModels"
        >
          <template slot-scope="{ row }" slot="action">
            <div style="display: flex; justify-content: space-around">
              <Button type="info" size="small" @click="modelGraphMouseenterHandler(row)">{{
                $t('view') + $t('object')
              }}</Button>
              <Button
                v-if="row.nodeType === 'subProc'"
                type="default"
                size="small"
                style="margin-left: 5px"
                @click="viewSubProcExecution(row)"
                >{{ $t('fe_view_childFlow') }}</Button
              >
            </div>
          </template>
        </Table>
        <span v-if="isNodeCanBindData" style="font-size: 12px; color: red; margin-right: 8px">{{
          $t('be_dynamic_binding_warning')
        }}</span>
      </template>
      <template slot="footer">
        <Button @click="targetModalVisible = false">{{ $t('cancel') }}</Button>
        <Button type="primary" :disabled="isNodeCanBindData" @click="targetModelConfirm(false)">{{
          $t('submit')
        }}</Button>
      </template>
    </BaseDrawer>
    <!--对象查看弹框-->
    <Modal v-model="showNodeDetail" :fullscreen="nodeDetailFullscreen" width="1000" :styles="{ top: '50px' }">
      <p slot="header">
        <span>{{ nodeTitle }}</span>
        <Icon v-if="!nodeDetailFullscreen" @click="zoomModal" class="header-icon" type="ios-expand" />
        <Icon v-else @click="nodeDetailFullscreen = false" class="header-icon" type="ios-contract" />
      </p>
      <div :style="[{ overflow: 'auto', margin: '0 6px 6px' }, fullscreenModalContentStyle]">
        <json-viewer :value="nodeDetail || {}" :expand-depth="5"></json-viewer>
      </div>
    </Modal>
    <!--高危确认二次弹框(查看)-->
    <Modal v-model="confirmModal.isShowConfirmModal" width="1000">
      <div>
        <Icon :size="28" :color="'#f90'" type="md-help-circle" />
        <span class="confirm-msg">{{ $t('confirm_to_exect') }}</span>
      </div>
      <div style="max-height: 390px; overflow: auto">
        <pre style="margin-left: 44px; margin-top: 22px">{{ this.confirmModal.message }}</pre>
      </div>
      <div slot="footer">
        <span style="margin-left: 30px; color: #ed4014; float: left; text-align: left">
          <Checkbox v-model="confirmModal.check">{{ $t('dangerous_confirm_tip') }}</Checkbox>
        </span>
        <Button type="text" @click="confirmModal.isShowConfirmModal = false">{{ $t('bc_cancel') }}</Button>
        <Button type="warning" :disabled="!confirmModal.check" @click="confirmToExecution">{{
          $t('bc_confirm')
        }}</Button>
      </div>
    </Modal>
    <!--定时执行弹框(查看)-->
    <Modal v-model="timeConfig.isShow" :title="$t('timed_execution')">
      <Form :label-width="100" label-colon>
        <FormItem :label="$t('timing_type')">
          <Select
            v-model="timeConfig.params.scheduleMode"
            @on-change="timeConfig.params.time = '00:00:00'"
            style="width: 95%"
          >
            <Option v-for="item in timeConfig.scheduleModeOptions" :key="item.value" :value="item.value">{{
              item.label
            }}</Option>
          </Select>
        </FormItem>
        <FormItem
          v-if="['Monthly', 'Weekly'].includes(timeConfig.params.scheduleMode)"
          :label="timeConfig.params.scheduleMode === 'Monthly' ? $t('day') : $t('week')"
        >
          <Select v-model="timeConfig.params.cycle" style="width: 95%">
            <Option
              v-for="item in timeConfig.modeToValue[timeConfig.params.scheduleMode]"
              :key="item.value"
              :value="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('execute_date')">
          <TimePicker
            :value="timeConfig.params.time"
            @on-change="changeTimePicker"
            style="width: 355px"
            :disabled-hours="
              timeConfig.params.scheduleMode === 'Hourly'
                ? [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
                : []
            "
            :clearable="false"
            format="HH:mm:ss"
          ></TimePicker>
        </FormItem>
        <FormItem :label="$t('be_mgmt_role')">
          <Select v-model="timeConfig.params.role" style="width: 370px">
            <Option v-for="item in timeConfig.currentUserRoles" :key="item.name" :value="item.name">{{
              item.displayName
            }}</Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('be_email_push')">
          <Select v-model="timeConfig.params.mailMode" style="width: 370px">
            <Option v-for="item in timeConfig.mailModeOptions" :key="item.value" :value="item.value">{{
              item.label
            }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="timeConfig.isShow = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="saveTime">{{ $t('save') }}</Button>
      </div>
    </Modal>
    <!--日志查看input列嵌套详情弹框(查看)-->
    <Modal v-model="attrValue.isShow" :title="attrValue.attr">
      <Form :label-width="120" label-colon>
        <FormItem :label="$t('attribute_type')">
          <span style="word-break: break-word">{{ attrValue.data.type }}</span>
        </FormItem>
        <FormItem :label="$t('attribute')">
          <span style="word-break: break-word">{{ attrValue.data.value }}</span>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="attrValue.isShow = false">{{ $t('bc_cancel') }}</Button>
      </div>
    </Modal>
    <!--手动跳过-->
    <Modal
      :title="$t('select_an_operation')"
      v-model="manualSkipVisible"
      :footer-hide="true"
      :mask-closable="false"
      :z-index="10"
      width="500"
    >
      <span>{{ $t('be_expected_completion_time') }}：【{{ manualSkipParams.dateToDisplay }}】</span>
      <div class="workflowActionModal-container" style="text-align: center; margin-top: 20px">
        <Button @click="confirmSkip" type="warning">{{ $t('be_manual_skip') }}</Button>
      </div>
    </Modal>
    <!-- 执行分支 -->
    <Modal
      :title="$t('select_an_operation')"
      v-model="executeBranchVisible"
      :footer-hide="true"
      :mask-closable="false"
      :scrollable="true"
    >
      <div style="width: 120px; display: inline-block; text-align: right">{{ $t('be_decision_branch') }}：</div>
      <Select v-model="manualSkipParams.message" style="width: 350px">
        <Option v-for="item in manualSkipParams.branchOption" :value="item" :key="item">{{ item }}</Option>
      </Select>
      <div class="workflowActionModal-container" style="text-align: center; margin-top: 20px">
        <Button type="warning" @click="confirmExecuteBranch" :disabled="!manualSkipParams.message">{{
          $t('be_execute_branch')
        }}</Button>
      </div>
    </Modal>
    <!-- 非本人编排提示 -->
    <Modal
      :title="$t('be_workflow_non_owner_title')"
      v-model="isShowNonOwnerModal"
      :mask-closable="false"
      :scrollable="true"
      :z-index="10000"
    >
      <span style="margin: 8px">
        {{ $t('be_workflow_non_owner_tip1') }}[{{ flowOwner }}]{{ $t('be_workflow_non_owner_tip2') }}
      </span>
      <div slot="footer">
        <Button type="warning" @click="isShowNonOwnerModal = false">{{ $t('be_i_aware') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import {
  getAllFlow,
  getFlowOutlineByID,
  // getTargetOptions,
  getTreePreviewData,
  createFlowInstance,
  getProcessInstances,
  getProcessInstance,
  retryProcessInstance,
  getModelNodeDetail,
  getNodeBindings,
  getNodeContext,
  getDataByNodeDefIdAndProcessSessionId,
  setDataByNodeDefIdAndProcessSessionId,
  getAllBindingsProcessSessionId,
  getTargetModelByProcessDefId,
  getPreviewEntitiesByInstancesId,
  createWorkflowInstanceTerminationRequest,
  getTaskNodeInstanceExecBindings,
  updateTaskNodeInstanceExecBindings,
  setUserScheduledTasks,
  getMetaData,
  instancesWithPaging,
  getExecutionTimeByNodeId,
  skipNode,
  getBranchByNodeId,
  executeBranch,
  pauseAndContinueFlow,
  getCurrentUserRoles,
  collectFlow,
  unCollectFlow
} from '@/api/server'
import JsonViewer from 'vue-json-viewer'
import HeaderTitle from '@/pages/components/header-title.vue'
import BaseDrawer from '@/pages/components/base-drawer.vue'
import * as d3 from 'd3-selection'
// eslint-disable-next-line no-unused-vars
import * as d3Graphviz from 'd3-graphviz'
import { addEvent, removeEvent } from '@/pages/util/event.js'
import { debounce } from '@/const/util'
export default {
  components: {
    JsonViewer,
    HeaderTitle,
    BaseDrawer
  },
  data () {
    return {
      // 属性值展示
      attrValue: {
        attr: '',
        isShow: false,
        data: {
          type: '',
          value: ''
        }
      },
      currentAction: '',
      allFlowNodesModelData: [],
      selectedFlowNodesModelData: [],
      modelDataWithFlowNodes: [],
      targetWithFlowModalVisible: false,
      flowNodesWithDataModalVisible: false,
      indexArray: [0],
      flowIndexArray: [0],
      currentTab: 'create_new_workflow_job',
      btnLoading: false,
      currentModelNodeRefs: [],
      showNodeDetail: false,
      nodeDetailFullscreen: false,
      fullscreenModalContentStyle: { 'max-height': '500px' },
      tableMaxHeight: 400,
      nodeTitle: null,
      nodeDetail: null,
      graph: {},
      flowGraph: {},
      modelData: [],
      flowData: {},
      currentNodeTitle: null,
      rowContent: null,
      allFlowInstances: [],
      allFlows: [],
      allTarget: [],
      currentFlowNodeId: '',
      selectedFlowInstance: '', // 选择的编排任务
      selectedFlow: '', // 选的的编排
      selectedFlowObj: {},
      selectedTarget: '',
      showExcution: true,
      isExecuteActive: false,
      isEnqueryPage: false,
      workflowActionModalVisible: false,
      targetModalVisible: false,
      retryTargetModalVisible: false,
      tableFilterParam: null,
      retryTableFilterParam: null,
      tartetModels: [],
      retryTartetModels: [],
      catchTartetModels: [],
      flowNodesWithModelDataColums: [
        // 节点名
        {
          title: this.$t('fe_nodeName'),
          slot: 'orderedNo',
          width: 240
        },
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        // 数据类型
        {
          title: this.$t('data_type'),
          key: 'entity',
          width: 270
        },
        // 对象
        {
          title: this.$t('object'),
          key: 'displayName'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 210,
          align: 'center',
          render: (h, params) => {
            return (
              <div style="display: flex; justify-content: space-around">
                <Button
                  type="info"
                  size="small"
                  onClick={() => {
                    this.modelGraphMouseenterHandler(params.row)
                  }}
                >
                  {`${this.$t('view')}${this.$t('object')}`}
                </Button>
                {params.row.nodeType === 'subProc' && (
                  <Button
                    color="#808695"
                    size="small"
                    onClick={() => {
                      this.viewSubProcExecution(params.row)
                    }}
                  >
                    {this.$t('fe_view_childFlow')}
                  </Button>
                )}
              </div>
            )
          }
        }
      ],
      targetWithFlowModelColums: [
        // 节点名
        {
          title: this.$t('fe_nodeName'),
          slot: 'nodeTitle',
          width: 240
        },
        // 数据类型
        {
          title: this.$t('data_type'),
          key: 'entity'
        },
        // 对象
        {
          title: this.$t('object'),
          key: 'displayName'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 150,
          align: 'center',
          render: (h, params) => {
            return (
              <Button
                type="info"
                size="small"
                onClick={() => {
                  this.modelGraphMouseenterHandler(params.row)
                }}
              >
                {`${this.$t('view')}${this.$t('object')}`}
              </Button>
            )
          }
        }
      ],
      retryTargetModelColums: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: 'PackageName',
          key: 'packageName'
        },
        {
          title: 'EntityName',
          key: 'entityName'
        },
        {
          title: 'DisplayName',
          key: 'entityDisplayName'
        },
        {
          title: 'Status',
          key: 'status'
        },
        {
          title: 'Message',
          key: 'message',
          render: (h, params) => {
            let data = {
              props: {
                content: params.row.message || '',
                delay: 500,
                placement: 'right',
                'max-width': '350'
              }
            }
            return (
              <Tooltip {...data}>
                <div style="text-overflow: ellipsis;overflow: hidden;white-space: nowrap;">{params.row.message}</div>
              </Tooltip>
            )
          }
        }
      ],
      targetModelColums: [
        {
          type: 'selection',
          width: 60,
          align: 'center',
          disabled: true
        },
        {
          title: this.$t('package_name'),
          key: 'packageName'
        },
        {
          title: this.$t('entity_name'),
          key: 'entityName'
        },
        {
          title: this.$t('object'),
          key: 'displayName'
        },
        {
          title: this.$t('table_action'),
          slot: 'action',
          width: 195,
          align: 'center'
        }
      ],
      nodeDetailColumns: [
        {
          title: 'inputs',
          key: 'inputs',
          render: (h, params) => {
            const jsonData = params.row.inputs
            return (
              <div style="white-space: nowrap; overflow: auto;">
                {jsonData.map((data, index) => (
                  <div key={index} style="margin-left:5px;">
                    {'{'}
                    {Object.entries(data).map(([key, value]) => (
                      <div style="margin-left:5px;">
                        <Icon
                          type="md-search"
                          onClick={() => this.handleClick(key, value)}
                          style="cursor:pointer;color:#2d8cf0"
                        />
                        {key}: <span style="color:#42b983;">{value}</span>
                      </div>
                    ))}
                    {'}'}
                  </div>
                ))}
              </div>
            )
          }
        },
        {
          title: 'outputs',
          key: 'outputs',
          render: (h, params) => {
            const strOutput = params.row.outputs
            const noData = strOutput.every(i => i && Object.keys(i).length === 0)
            if (noData) {
              return <span>-</span>
            } else {
              return (
                <div style="white-space: nowrap; overflow: auto;">
                  {strOutput.map((data, index) => (
                    <div key={index} style="margin-left:5px;">
                      {'{'}
                      {Object.entries(data).map(([key, value]) => (
                        <div style="margin-left:5px;">
                          {key}: <span style="color:#42b983;">{value}</span>
                        </div>
                      ))}
                      {'}'}
                    </div>
                  ))}
                </div>
              )
            }
          }
        }
      ],
      nodeDetailIO: [],
      nodeDetailResponseHeader: null,
      currentFailedNodeID: '',
      timer: null,
      modelDetailTimer: null,
      flowNodesBindings: [],
      flowDetailTimer: null,
      isLoading: false,
      catchNodeTableList: [],
      retryCatchNodeTableList: [],
      processSessionId: '',
      allBindingsList: [],
      isShowExect: false, // 模型查询返回，激活执行按钮
      stopSuccess: false,

      confirmModal: {
        isShowConfirmModal: false,
        check: false,
        message: '',
        requestBody: ''
      },
      currentInstanceStatusForNodeOperation: '', // 流程状态
      currentInstanceStatus: true,

      timeConfig: {
        isShow: false,
        params: {
          scheduleMode: 'Monthly',
          time: '00:00:00',
          cycle: '',
          role: '',
          mailMode: 'node'
        },
        scheduleModeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ],
        modeToValue: {
          Monthly: [
            { label: '1', value: 1 },
            { label: '2', value: 2 },
            { label: '3', value: 3 },
            { label: '4', value: 4 },
            { label: '5', value: 5 },
            { label: '6', value: 6 },
            { label: '7', value: 7 },
            { label: '8', value: 8 },
            { label: '9', value: 9 },
            { label: '10', value: 10 },
            { label: '11', value: 11 },
            { label: '12', value: 12 },
            { label: '13', value: 13 },
            { label: '14', value: 14 },
            { label: '15', value: 15 },
            { label: '16', value: 16 },
            { label: '17', value: 17 },
            { label: '18', value: 18 },
            { label: '19', value: 19 },
            { label: '20', value: 20 },
            { label: '21', value: 21 },
            { label: '22', value: 22 },
            { label: '23', value: 23 },
            { label: '24', value: 24 },
            { label: '25', value: 25 },
            { label: '26', value: 26 },
            { label: '27', value: 27 },
            { label: '28', value: 28 },
            { label: '29', value: 29 },
            { label: '30', value: 30 },
            { label: '31', value: 31 }
          ],
          Weekly: [
            { label: this.$t('Mon'), value: 1 },
            { label: this.$t('Tue'), value: 2 },
            { label: this.$t('Wed'), value: 3 },
            { label: this.$t('Thu'), value: 4 },
            { label: this.$t('Fri'), value: 5 },
            { label: this.$t('Sat'), value: 6 },
            { label: this.$t('Sun'), value: 7 }
          ]
        },
        mailModeOptions: [
          { label: this.$t('be_role_email'), value: 'role' },
          { label: this.$t('be_user_email'), value: 'user' },
          { label: this.$t('be_not_send'), value: 'none' }
        ],
        currentUserRoles: []
      },
      pluginInfo: '',
      nodesCannotBindData: [], // 初始化不能绑定数据的节点
      isNodeCanBindData: false,
      manualSkipVisible: false, // 手动跳过
      executeBranchVisible: false, // 执行分支
      hasExecuteBranchVisible: false, // 同一编排在进入只进行一次展示
      manualSkipParams: {
        act: '', // 执行动作
        procInstId: '',
        nodeInstId: '',
        message: '', // 选择执行的分支
        branchOption: [] // 可执行分支
      },
      isShowNonOwnerModal: false, // 查询非本人用户编排提示
      flowOwner: '',
      subProcId: '', // 新建执行预览子编排从链接上传过来的子编排ID
      subProc: this.$route.query.subProc || '', // 执行详情是否为子编排 main主编排 sub子编排
      noActionFlag: false
    }
  },
  computed: {
    canAbleToSetting () {
      const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
      if (found && found.status === 'Completed') {
        return false
      }
      return true
    },
    currentNodeStatus () {
      if (!this.flowData.flowNodes) {
        return ''
      }
      const found = this.flowData.flowNodes.find(_ => _.nodeId === this.currentFailedNodeID)
      if (found) {
        return found.status
      } else {
        return ''
      }
    },
    getStatusStyleAndName () {
      return function (status, type) {
        const list = [
          { label: this.$t('fe_notStart'), value: 'NotStarted', color: '#808695' },
          { label: this.$t('fe_inProgressFaulted'), value: 'InProgress(Faulted)', color: '#ed4014' },
          { label: this.$t('fe_inProgressTimeouted'), value: 'InProgress(Timeouted)', color: '#ed4014' },
          { label: this.$t('fe_stop'), value: 'Stop', color: '#ed4014' },
          { label: this.$t('fe_inProgress'), value: 'InProgress', color: '#1990ff' },
          { label: this.$t('fe_completed'), value: 'Completed', color: '#7ac756' },
          { label: this.$t('fe_faulted'), value: 'Faulted', color: '#e29836' },
          { label: this.$t('fe_internallyTerminated'), value: 'InternallyTerminated', color: '#e29836' }
        ]
        const findObj = list.find(i => i.value === status)
        if (type === 'style') {
          return {
            display: 'inline-block',
            backgroundColor: findObj.color,
            padding: '4px 10px',
            width: 'fit-content',
            color: '#fff',
            borderRadius: '4px',
            float: 'right',
            fontSize: '12px',
            marginLeft: '5px'
          }
        } else {
          return findObj.label
        }
      }
    }
  },
  watch: {
    selectedFlowInstance: {
      handler (val, oldVal) {
        if (val !== oldVal) {
          this.stopSuccess = false
          this.currentInstanceStatus = true
        }
      }
    },
    targetModalVisible: function (val) {
      this.tableFilterParam = null
      if (!val) {
        this.catchNodeTableList = []
      }
    },
    retryTableFilterParam: function (filter) {
      if (!filter) {
        this.retryTartetModels = this.retryCatchNodeTableList
      } else {
        this.retryTartetModels = this.retryCatchNodeTableList.filter(item => {
          return item.entityDisplayName.includes(filter)
        })
      }
      this.retryTartetModels.forEach(tm => {
        tm._checked = false
        this.retryCatchNodeTableList.forEach(cn => {
          if (tm.id === cn.id) {
            tm._checked = true
          }
        })
      })
    },
    tableFilterParam: function (filter) {
      if (!filter) {
        this.tartetModels = this.catchTartetModels
      } else {
        this.tartetModels = this.catchTartetModels.filter(item => {
          return item.displayName.includes(filter)
        })
      }
      this.tartetModels.forEach(tm => {
        tm._checked = false
        this.catchNodeTableList.forEach(cn => {
          if (tm.id === cn.id) {
            tm._checked = true
          }
        })
      })
    },
    nodeDetailFullscreen: function (tag) {
      tag ? (this.fullscreenModalContentStyle = {}) : (this.fullscreenModalContentStyle['max-height'] = '500px')
    },
    // 节点操作弹窗打开，直接查看日志信息
    workflowActionModalVisible (val) {
      if (val === true) {
        this.pluginInfo = ''
        this.nodeDetailResponseHeader = null
        this.nodeDetailIO = []
        this.flowGraphMouseenterHandler(this.currentFailedNodeID)
        this.noActionFlag = false
        this.$nextTick(() => {
          if (this.$refs.action.children.length === 0) {
            this.noActionFlag = true
          }
        })
      }
    }
  },
  mounted () {
    const id = this.$route.query.id || ''
    const templateId = this.$route.query.templateId || ''
    this.subProcId = this.$route.query.subProcId || ''
    // 查看执行历史
    if (id) {
      this.jumpToHistory(id)
    }
    // 选择模板新建执行
    if (templateId) {
      this.selectedFlow = templateId
      this.getAllFlow()
      this.orchestrationSelectHandler()
    }
    // 查看子编排执行(新建执行的时候点击子编排节点跳转进来)
    if (this.subProcId) {
      this.selectedFlow = this.subProcId
      this.orchestrationSelectHandler()
      this.getAllFlow()
      this.selectedTarget = this.$route.query.entityDataId || ''
      this.getTargetOptions()
      this.onTargetSelectHandler()
    }
    // this.getProcessInstances()
    // this.getAllFlow()
    // this.createHandler()
  },
  destroyed () {
    clearInterval(this.timer)
    localStorage.removeItem('history-execution-search-params')
  },
  methods: {
    handleBack () {
      this.$router.back()
      // this.$router.push({
      //   path: '/implementation/workflow-execution/normal-template'
      // })
    },
    // 收藏or取消收藏
    handleStar: debounce(async function ({ procDefId, collected }) {
      const method = collected ? unCollectFlow : collectFlow
      const params = {
        procDefId: procDefId
      }
      const { status } = await method(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('successful')
        })
        this.selectedFlowObj.collected = !collected
      }
    }, 300),
    // 【新建执行】子编排节点支持跳转预览子编排详情
    viewSubProcExecution (row) {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/implementation/workflow-execution/normal-create?subProcId=${row.subProcDefId}&entityDataId=${row.entityDataId}&sessionId=${row.subPreviewSessionId}`
      window.open(path, '_blank')
    },
    // 【执行详情】子编排调用API列表支持跳转预览子编排详情
    viewSubProcExecutionDetail (id) {
      window.sessionStorage.currentPath = '' // 先清空session缓存页面，不然打开新标签页面会回退到缓存的页面
      const path = `${window.location.origin}/#/implementation/workflow-execution/view-execution?id=${id}&subProc=sub`
      window.open(path, '_blank')
    },
    // 查看日志调用API详情
    async handleClick (key, value) {
      this.attrValue.attr = key
      const params = {
        paramName: key,
        serviceId: this.pluginInfo
      }
      let { status, data } = await getMetaData(params)
      if (status === 'OK') {
        this.attrValue.data.type = data.mappingType
        switch (data.mappingType) {
          case 'system_variable':
            this.attrValue.data.value = data.mappingSystemVariableName
            break
          case 'context':
            this.attrValue.data.value = 'N/A'
            break
          case 'constant':
            this.attrValue.data.value = data.mappingValue
            break
          case 'entity':
            this.attrValue.data.value = data.mappingEntityExpression
            break
          default:
            this.attrValue.data.value = ''
        }
        this.attrValue.isShow = true
      }
    },
    async jumpToHistory (id) {
      // await this.queryHistory()
      this.querySelectedFlowInstanceId = id
      await this.getProcessInstances()
      this.selectedFlowInstance = id
      this.$nextTick(() => {
        this.queryHandler()
      })
    },
    changeTimePicker (time) {
      this.timeConfig.params.time = time
    },
    async saveTime () {
      const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
      let scheduleExpr = ''
      if (['Hourly', 'Daily'].includes(this.timeConfig.params.scheduleMode)) {
        scheduleExpr = this.timeConfig.params.time
        if (this.timeConfig.params.scheduleMode === 'Hourly') {
          scheduleExpr = this.timeConfig.params.time.substring(3)
        }
      } else {
        scheduleExpr = this.timeConfig.params.cycle + ' ' + this.timeConfig.params.time
      }
      let params = {
        scheduleMode: this.timeConfig.params.scheduleMode,
        scheduleExpr: scheduleExpr,
        procDefName: found.procInstName,
        procDefId: found.procDefId,
        entityDataName: found.entityDisplayName,
        entityDataId: found.entityDataId,
        mailMode: this.timeConfig.params.mailMode,
        role: this.timeConfig.params.role
      }
      const { status } = await setUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
        this.timeConfig.isShow = false
        this.currentTab = 'timed_execution'
      }
    },
    async setTimedExecution () {
      await this.getCurrentUserRoles()
      this.timeConfig.params.scheduleMode = 'Monthly'
      this.timeConfig.params.time = '00:00:00'
      this.timeConfig.params.cycle = ''
      this.timeConfig.params.role = ''
      this.timeConfig.params.mailMode = 'none'
      this.timeConfig.isShow = true
    },
    async getCurrentUserRoles () {
      const { status, data } = await getCurrentUserRoles()
      if (status === 'OK') {
        this.timeConfig.currentUserRoles = data
      }
    },
    async stopHandler () {
      this.$Modal.confirm({
        title: this.$t('bc_confirm') + ' ' + this.$t('stop_orch'),
        'z-index': 10,
        onOk: async () => {
          // createWorkflowInstanceTerminationRequest
          const instance = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
          const payload = {
            procInstId: this.selectedFlowInstance,
            procInstKey: instance.procInstKey
          }
          const { status } = await createWorkflowInstanceTerminationRequest(payload)
          if (status === 'OK') {
            this.getProcessInstances()
            this.stopSuccess = true
            this.$Notice.success({
              title: 'Success',
              desc: 'Success'
            })
          }
        },
        onCancel: () => {}
      })
    },
    tabChanged (v) {
      this.selectedFlowInstance = ''
      this.currentInstanceStatusForNodeOperation = ''
      // create_new_workflow_job   enquery_new_workflow_job
      this.currentTab = v
      if (v === 'create_new_workflow_job') {
        this.createHandler()
      }
      if (v === 'enquery_new_workflow_job') {
        this.queryHistory()
      }
      if (v === 'timed_execution') {
        // this.queryHistory()
      }
    },
    async getDetail (row) {
      if (!row.packageName || !row.entityName || !row.dataId) return
      let params = {
        additionalFilters: [{ attrName: 'id', op: 'eq', condition: row.dataId }]
      }
      const { status, data } = await getModelNodeDetail(row.packageName, row.entityName, params)
      if (status === 'OK') {
        this.rowContent = data
      }
    },
    async retryTargetModelConfirm (visible) {
      const found = this.flowData.flowNodes.find(_ => _.nodeId === this.currentFailedNodeID)
      let tem = []
      this.retryTartetModels.forEach(d => {
        const f = this.retryCatchNodeTableList.find(c => c.id === d.id)
        tem.push({ ...d, bound: f.bound, confirmToken: f.confirmToken })
      })
      const payload = {
        nodeInstId: found.id,
        procInstId: found.procInstId,
        data: tem
      }
      const { status } = await updateTaskNodeInstanceExecBindings(payload)
      if (status === 'OK') {
        if (this.currentAction === 'dataSelection') {
          this.$Notice.success({
            title: 'Success',
            desc: 'Success'
          })
          this.workflowActionModalVisible = false
          this.retryTargetModalVisible = false
          return
        }
        const retry = await retryProcessInstance({
          act: 'retry',
          nodeInstId: found.id,
          procInstId: found.procInstId
        })
        if (retry.status === 'OK') {
          this.$Notice.success({
            title: 'Success',
            desc: 'Retry' + ' action is proceed successfully'
          })
          this.workflowActionModalVisible = false
          this.retryTargetModalVisible = false
          this.processInstance()
        }
      }
    },
    async targetModelConfirm (visible) {
      // TODO:
      this.targetModalVisible = visible
      if (!visible) {
        await this.updateNodeInfo()
        this.formatRefNodeIds()
        this.renderModelGraph()
      }
    },
    singleSelect (selection, row) {
      this.catchNodeTableList = this.catchNodeTableList.concat(row)
    },
    singleCancel (selection, row) {
      const index = this.catchNodeTableList.findIndex(cn => {
        return cn.id === row.id
      })
      this.catchNodeTableList.splice(index, 1)
    },
    selectAll (selection) {
      let temp = []
      this.catchNodeTableList.forEach(cntl => {
        temp.push(cntl.id)
      })
      selection.forEach(se => {
        if (!temp.includes(se.id)) {
          this.catchNodeTableList.push(se)
        }
      })
    },
    selectAllCancel () {
      let temp = []
      this.tartetModels.forEach(tm => {
        temp.push(tm.id)
      })
      if (this.tableFilterParam) {
        this.catchNodeTableList = this.catchNodeTableList.filter(item => {
          return !temp.includes(item.id)
        })
      } else {
        this.catchNodeTableList = []
      }
    },
    retrySingleSelect (selection, row) {
      let find = this.retryCatchNodeTableList.find(item => item.id === row.id)
      find.bound = 'Y'
    },
    retrySingleCancel (selection, row) {
      let find = this.retryCatchNodeTableList.find(cn => {
        return cn.id === row.id
      })
      find.bound = 'N'
    },
    retrySelectAll (selection) {
      this.retryCatchNodeTableList.forEach(item => {
        item.bound = 'Y'
      })
    },
    retrySelectAllCancel () {
      this.retryCatchNodeTableList.forEach(item => {
        item.bound = 'N'
      })
    },
    allFlowNodesSingleSelect (selection, row) {
      this.selectedFlowNodesModelData = this.selectedFlowNodesModelData.concat(row)
    },
    allFlowNodesSingleCancel (selection, row) {
      const index = this.selectedFlowNodesModelData.findIndex(cn => {
        return cn.id === row.id
      })
      this.selectedFlowNodesModelData.splice(index, 1)
    },
    allFlowNodesSelectAll (selection) {
      let temp = []
      this.selectedFlowNodesModelData.forEach(cntl => {
        temp.push(cntl.id)
      })
      selection.forEach(se => {
        if (!temp.includes(se.id)) {
          this.selectedFlowNodesModelData.push(se)
        }
      })
    },
    allFlowNodesSelectAllCancel () {
      let temp = []
      this.tartetModels.forEach(tm => {
        temp.push(tm.id)
      })
      this.selectedFlowNodesModelData = this.selectedFlowNodesModelData.filter(item => {
        return !temp.includes(item.id)
      })
    },
    async setFlowDataForAllNodes () {
      let compare = (a, b) => {
        if (a.orderedNo * 1 < b.orderedNo * 1) {
          return -1
        }
        if (a.orderedNo * 1 > b.orderedNo * 1) {
          return 1
        }
        return 0
      }
      let allPromises = []
      this.flowData.flowNodes
        .filter(_ => _.orderedNo)
        .forEach(node => {
          allPromises.push(getDataByNodeDefIdAndProcessSessionId(node.nodeDefId, this.processSessionId))
        })
      const dataArray = await Promise.all(allPromises)
      this.selectedFlowNodesModelData = []
      this.allFlowNodesModelData = []
        .concat(
          ...dataArray.map(_ => {
            return _.data.map(d => {
              const found = this.modelData.find(j => j.dataId === d.entityDataId)
              const flowNode = this.flowData.flowNodes.find(j => j.orderedNo === d.orderedNo)
              const res = {
                ...d,
                _checked: d.bound === 'Y',
                ...found,
                entity: found.packageName + ':' + found.entityName,
                nodeName: flowNode.nodeName,
                nodeDefId: flowNode.nodeDefId,
                nodeType: flowNode.nodeType
              }
              if (d.bound === 'Y') {
                this.selectedFlowNodesModelData.push(res)
              }
              return res
            })
          })
        )
        .sort(compare)
      let start = 0
      for (let i = 0; i < this.allFlowNodesModelData.length; i++) {
        let startName = this.allFlowNodesModelData[start].orderedNo + this.allFlowNodesModelData[start].nodeName
        const node = this.allFlowNodesModelData[i]
        if (node.orderedNo + node.nodeName !== startName) {
          start = i
          this.flowIndexArray.push(i)
        }
      }
      this.flowNodesWithDataModalVisible = true
    },
    flowNodeDataHandleSpan ({ row, column, rowIndex, columnIndex }) {
      return this.rowSpanComputed(this.allFlowNodesModelData, this.flowIndexArray, rowIndex, columnIndex)
    },
    modelDataHandleSpan ({ row, column, rowIndex, columnIndex }) {
      return this.rowSpanComputed(this.modelDataWithFlowNodes, this.indexArray, rowIndex, columnIndex)
    },
    rowSpanComputed (data, indexArray, rowIndex, columnIndex) {
      let arr = []
      for (let i = 0; i < indexArray.length; i++) {
        if (rowIndex === indexArray[i] && columnIndex === 0) {
          arr = [indexArray[i + 1] - indexArray[i], 1]
        } else if (rowIndex > indexArray[i - 1] && rowIndex < indexArray[i] && columnIndex === 0) {
          arr = [0, 0]
        }
      }
      if (rowIndex === indexArray[indexArray.length - 1] && columnIndex === 0) {
        arr = [data.length - indexArray[indexArray.length - 1], 1]
      }
      if (rowIndex > indexArray[indexArray.length - 1] && columnIndex === 0) {
        arr = [0, 0]
      }
      return arr
    },
    showModelDataWithFlow () {
      this.modelDataWithFlowNodes = this.modelData.map(_ => {
        return {
          ..._,
          entity: _.packageName + ':' + _.entityName,
          nodeTitle:
            _.refFlowNodeIds.length > 0
              ? _.refFlowNodeIds
                .map(id => {
                  const found = this.flowData.flowNodes.find(n => n.orderedNo === id)
                  return found.orderedNo + ' ' + found.nodeName
                })
                .join(';')
              : ''
        }
      })
      this.targetWithFlowModalVisible = true
      let start = 0
      for (let i = 0; i < this.modelDataWithFlowNodes.length; i++) {
        let startEntity = this.modelDataWithFlowNodes[start].entity
        if (this.modelDataWithFlowNodes[i].entity !== startEntity) {
          start = i
          this.indexArray.push(i)
        }
      }
    },
    async flowNodesTargetModelConfirm () {
      let obj = {}
      this.selectedFlowNodesModelData.forEach(_ => {
        if (!obj[_.nodeDefId]) {
          obj[_.nodeDefId] = []
          obj[_.nodeDefId].push(_)
        } else {
          obj[_.nodeDefId].push(_)
        }
      })
      let promiseArray = []
      Object.keys(obj).forEach(key => {
        promiseArray.push(setDataByNodeDefIdAndProcessSessionId(key, this.processSessionId, obj[key]))
      })
      await Promise.all(promiseArray)
      this.$Notice.success({
        title: 'Success',
        desc: 'Success'
      })
    },
    async updateNodeInfo () {
      const currentNode = this.flowData.flowNodes.find(_ => {
        return _.nodeId === this.currentFlowNodeId
      })
      const payload = this.catchNodeTableList.map(_ => {
        return { ..._, bound: 'Y' }
      })
      await setDataByNodeDefIdAndProcessSessionId(currentNode.nodeDefId, this.processSessionId, payload)
      const filter = this.allBindingsList.filter(_ => _.nodeDefId !== currentNode.nodeDefId)
      this.$Notice.success({
        title: 'Success',
        desc: 'Success'
      })
      this.allBindingsList = filter.concat(payload)
    },
    async getProcessInstances (isAfterCreate = false, createResponse = undefined) {
      this.currentInstanceStatusForNodeOperation = ''
      let { status, data } = await getProcessInstances()
      if (status === 'OK') {
        this.allFlowInstances = data
        let routeFlowInstanceId = this.$route.query.id || ''
        const flag = this.allFlowInstances.some(i => i.id === routeFlowInstanceId)
        // 如果传入的id不在500条数据之内，插入该条数据
        if (!flag) {
          const params = {
            id: routeFlowInstanceId,
            pageable: {
              startIndex: 0,
              pageSize: 500
            }
          }
          let { status, data } = await instancesWithPaging(params)
          if (status === 'OK' && Array.isArray(data.contents) && data.contents.length > 0) {
            this.allFlowInstances.unshift(data.contents[0])
          }
        }
        if (isAfterCreate) {
          this.selectedFlowInstance = createResponse.id
          this.processInstance()
        }
      }
    },
    async getNodeBindings (id) {
      if (!id) return
      const { status, data } = await getNodeBindings(id)
      if (status === 'OK') {
        this.flowNodesBindings = data
      }
    },
    async getAllFlow () {
      let { status, data } = await getAllFlow(false)
      if (status === 'OK') {
        this.allFlows = data.sort((a, b) => {
          let s = a.createdTime.toLowerCase()
          let t = b.createdTime.toLowerCase()
          if (s > t) return -1
          if (s < t) return 1
        })
        this.selectedFlowObj = this.allFlows.find(i => i.procDefId === this.selectedFlow) || {}
      }
    },
    clearFlow () {
      d3.select('#flow').selectAll('*').remove()
      this.clearTarget()
    },
    orchestrationSelectHandler () {
      this.currentFlowNodeId = ''
      this.currentModelNodeRefs = []
      this.getFlowOutlineData(this.selectedFlow)
      this.selectedFlowObj = this.allFlows.find(i => i.procDefId === this.selectedFlow) || {}
      if (this.selectedFlow && this.isEnqueryPage === false) {
        this.showExcution = true
        this.selectedTarget = ''
        this.modelData = []
        this.formatRefNodeIds()
        this.renderModelGraph()
      }
    },
    async getTargetOptions () {
      if (!(this.selectedFlow && this.selectedFlow.length > 0)) return
      const { status, data } = await getTargetModelByProcessDefId(this.selectedFlow)
      if (status === 'OK') {
        this.allTarget = data
      }
    },
    clearHistoryOrch () {
      this.stop()
      this.selectedFlow = ''
      this.selectedTarget = ''
      this.currentInstanceStatusForNodeOperation = ''
      d3.select('#flow').selectAll('*').remove()
      d3.select('#graph').selectAll('*').remove()
    },
    getCurrentInstanceStatus () {
      const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
      if (found && ['Completed', 'InternallyTerminated', 'Faulted'].includes(found.status)) {
        this.currentInstanceStatus = true
      } else {
        this.currentInstanceStatus = false
      }
    },
    queryHandler () {
      this.hasExecuteBranchVisible = false
      this.currentInstanceStatusForNodeOperation = ''
      this.stop()
      if (!this.selectedFlowInstance) return
      this.getStatus()
      this.getCurrentInstanceStatus()
      this.isEnqueryPage = true
      this.$nextTick(async () => {
        const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
        if (!(found && found.id)) return
        this.currentInstanceStatusForNodeOperation = found.status
        this.selectedFlow = found.procDefId
        this.selectedTarget = found.entityDataId
        this.processInstance()
        this.getNodeBindings(found.id)
        let { status, data } = await getProcessInstance(found.id)
        if (status === 'OK') {
          this.flowData = {
            ...data,
            flowNodes: data.taskNodeInstances
          }
          // this.getTargetOptions()
          removeEvent('.retry', 'click', this.retryHandler)
          removeEvent('.normal', 'click', this.normalHandler)
          removeEvent('.time-node', 'click', this.timeNodeHandler)
          removeEvent('.decision-node', 'click', this.executeBranchHandler)
          this.initFlowGraph(true)
          this.showExcution = false
          this.nodesCannotBindData = data.taskNodeInstances
            .filter(d => [1, 2].includes(d.dynamicBind) && d.status === 'NotStarted')
            .map(d => d.nodeId)
        }
        this.getModelData()
        this.tipForNonOwner(found)
      })
    },
    tipForNonOwner (flow) {
      if (
        ['InProgress', 'Timeouted', 'Stop'].includes(flow.status) &&
        flow.operator !== localStorage.getItem('username')
      ) {
        this.flowOwner = flow.operator
        this.isShowNonOwnerModal = true
      }
    },
    queryHistory () {
      this.selectedTarget = null
      this.stop()
      this.isEnqueryPage = true
      this.showExcution = false
      this.selectedFlow = ''
      this.modelData = []
      this.flowData = {}
      this.$nextTick(() => {
        this.initModelGraph()
        this.initFlowGraph()
      })
    },
    createHandler () {
      this.selectedTarget = null
      this.stop()
      this.isEnqueryPage = false
      this.selectedFlowInstance = ''
      this.selectedFlow = ''
      this.modelData = []
      this.flowData = {}
      this.showExcution = false
      this.$nextTick(() => {
        this.initModelGraph()
        this.initFlowGraph()
      })
    },
    clearTarget () {
      this.isExecuteActive = false
      this.showExcution = false
      this.selectedTarget = ''
      d3.select('#graph').selectAll('*').remove()
    },
    onTargetSelectHandler () {
      this.isShowExect = false
      this.showExcution = true
      this.processSessionId = ''
      if (!this.selectedTarget) return
      this.currentModelNodeRefs = []
      this.getModelData()
    },
    async getModelData () {
      this.modelData = []
      if ((!this.selectedFlow || !this.selectedTarget) && !this.isEnqueryPage) {
        this.renderModelGraph()
        return
      }
      this.isLoading = true
      let { status, data } = this.isEnqueryPage
        ? await getPreviewEntitiesByInstancesId(this.selectedFlowInstance)
        : await getTreePreviewData(this.selectedFlow, this.selectedTarget, this.$route.query.sessionId || '')
      this.isLoading = false
      if (!this.selectedTarget && !this.isEnqueryPage) return
      if (status === 'OK') {
        if (!this.isEnqueryPage) {
          this.isShowExect = true
        }
        if (data.processSessionId) {
          this.processSessionId = data.processSessionId
          const binds = await getAllBindingsProcessSessionId(data.processSessionId)
          this.allBindingsList = binds.data
        }
        this.modelData = data.entityTreeNodes.map(_ => {
          return {
            ..._,
            refFlowNodeIds: []
          }
        })
        this.formatRefNodeIds()
      }
      this.renderModelGraph()
    },
    async getFlowOutlineData (id) {
      if (!id) return
      let { status, data } = await getFlowOutlineByID(id)
      if (status === 'OK') {
        this.flowData = data
        this.initFlowGraph()
        this.getTargetOptions()
        this.nodesCannotBindData = data.flowNodes.filter(d => d.dynamicBind === 'Y').map(d => d.nodeId)
      }
    },
    formatRefNodeIds () {
      this.modelData.forEach(i => {
        i.refFlowNodeIds = []
        this.allBindingsList.forEach(j => {
          if (j.entityTypeId + ':' + j.entityDataId === i.id) {
            i.refFlowNodeIds.push(j.orderedNo)
          }
        })
      })
    },
    // 绘制数据模型图
    renderModelGraph () {
      let nodes = this.modelData.map((_, index) => {
        const nodeId = _.id
        // '-' 在viz.js中存在渲染问题
        const nodeTitle = '"' + nodeId.replace(/-/g, '_') + '"'
        let color = _.isHighlight ? '#5DB400' : 'black'
        // const isRecord = _.refFlowNodeIds.length > 0
        // const shape = isRecord ? 'ellipse' : 'ellipse'
        let fillcolor = 'white'
        _.refFlowNodeIds.sort((a, b) => {
          if (a * 1 < b * 1) {
            return -1
          }
          if (a * 1 > b * 1) {
            return 1
          }
          return 0
        })
        let refNodes = []
        let completedNodes = []
        _.refFlowNodeIds.forEach(id => {
          const node = this.flowData.flowNodes.find(_ => _.orderedNo === id)
          refNodes.push(node)
        })
        completedNodes = refNodes.filter(_ => _.status === 'Completed')
        const completedNodesLen = completedNodes.length
        const refNodesLen = refNodes.length
        if (completedNodesLen === refNodesLen && refNodesLen !== 0) {
          fillcolor = '#5DB400'
        }

        if (completedNodesLen > 0 && completedNodesLen < refNodesLen) {
          fillcolor = '#3C83F8'
        }
        const str = _.displayName || _.id
        const refStr = _.refFlowNodeIds.toString().replace(/,/g, '/')
        // const len = refStr.length - _.displayName.length > 0 ? refStr.length : _.displayName.length
        const firstLabel = str.length > 30 ? `${str.slice(0, 1)}...${str.slice(-29)}` : str
        // const fontSize = Math.min((58 / len) * 3, 16)
        const label = firstLabel + '\n' + refStr
        return `${nodeTitle} [label="${label}" class="model" id="${nodeId}" flowInstanceId="" color="${color}" fontsize="6" style="filled" fillcolor="${fillcolor}" shape="box"]`
      })
      let genEdge = () => {
        let pathAry = []

        this.modelData.forEach(_ => {
          if (_.succeedingIds.length > 0) {
            const nodeId = _.id
            let current = []
            current = _.succeedingIds.map(to => {
              return '"' + nodeId + '"' + ' -> ' + '"' + to + '"'
            })
            pathAry.push(current)
          }
        })
        return pathAry.flat().toString().replace(/,/g, ';')
      }
      let nodesToString = Array.isArray(nodes) && nodes.length > 0 ? nodes.toString().replace(/,/g, ';') + ';' : ''
      let nodesString =
        'digraph G { ' +
        'splines="polyline";' +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, shape="ellipse"];' +
        'Edge [fontname=Arial, minlen="1", color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        '}'
      this.reloadGraph()
      this.graph.graphviz.transition().renderDot(nodesString)
      // .on('end', this.setFontSizeForText)
      removeEvent('.model text', 'click', this.modelGraphClickHandler)
      removeEvent('#graph svg', 'click', this.resetcurrentModelNodeRefs)
      addEvent('.model text', 'click', this.modelGraphClickHandler)
      addEvent('#graph svg', 'click', this.resetcurrentModelNodeRefs)
    },
    setFontSizeForText () {
      const nondes = d3.selectAll('#graph svg g .node')._groups[0]
      for (let i = 0; i < nondes.length; i++) {
        const len = nondes[i].children[2].innerHTML.replace(/&nbsp;/g, '').length
        const fontsize = Math.min((nondes[i].children[1].rx.baseVal.value / len) * 3, 16)
        for (let j = 2; j < nondes[i].children.length; j++) {
          nondes[i].children[j].setAttribute('font-size', fontsize)
        }
      }
    },
    resetcurrentModelNodeRefs () {
      if (!this.isEnqueryPage) {
        this.currentModelNodeRefs = []
        this.renderFlowGraph()
      }
    },
    modelGraphClickHandler (e) {
      e.preventDefault()
      e.stopPropagation()
      if (!this.isEnqueryPage) {
        const refEle = e.target.parentNode.children[3]
        if (refEle) {
          this.currentModelNodeRefs = refEle.innerHTML.trim().split('/')
          this.renderFlowGraph()
        }
      }
    },
    // 查看对象弹框
    modelGraphMouseenterHandler (row) {
      clearTimeout(this.modelDetailTimer)
      this.modelDetailTimer = setTimeout(async () => {
        this.nodeTitle = `${row.displayName}`
        let params = {
          additionalFilters: [{ attrName: 'id', op: 'eq', condition: row.dataId || row.id }],
          procInstId: this.selectedFlowInstance + ''
        }
        const { status, data } = await getModelNodeDetail(row.packageName, row.entityName, params)
        if (status === 'OK') {
          // this.nodeDetail = JSON.stringify(data)
          //   .split(',')
          //   .join(',<br/>')
          this.nodeDetail = data
        }
        this.nodeDetailFullscreen = false
        this.showNodeDetail = true
        this.nodeDetailFullscreen = false
      }, 0)
    },
    ResetFlow () {
      if (this.flowGraph.graphviz) {
        this.flowGraph.graphviz.resetZoom()
      }
    },
    ResetModel () {
      if (this.graph.graphviz) {
        this.graph.graphviz.resetZoom()
      }
    },
    // 绘制编排流程图
    renderFlowGraph (excution) {
      const statusColor = {
        Completed: '#5DB400',
        deployed: '#7F8A96',
        InProgress: '#3C83F8',
        Faulted: '#FF6262',
        Risky: '#BF22E0',
        Timeouted: '#F7B500',
        NotStarted: '#7F8A96'
      }
      let nodes =
        this.flowData &&
        this.flowData.flowNodes &&
        this.flowData.flowNodes
          .filter(i => i.status !== 'predeploy')
          .map((_, index) => {
            const shapeMap = {
              start: 'circle', // 开始
              end: 'doublecircle', // 结束
              abnormal: 'doublecircle', // 异常
              decision: 'diamond', // 判断开始
              decisionMerge: 'diamond', // 判断结束
              fork: 'Mdiamond', // 并行开始
              merge: 'Mdiamond', // 并行结束
              human: 'ellipse', // 人工
              automatic: 'rect', // 自动
              data: 'cylinder', // 数据
              subProc: 'doubleoctagon', // 子编排
              date: 'cds', // 固定时间
              timeInterval: 'cds' // 时间间隔
            }
            if (['start', 'end', 'abnormal'].includes(_.nodeType)) {
              const defaultLabel = _.nodeType
              return `${_.nodeId} [label="${
                _.nodeName || defaultLabel
              }", fontsize="10", width="0.5", class="flow", style="${excution ? 'filled' : 'none'}" color="${
                excution ? statusColor[_.status] : '#7F8A96'
              }" shape="${shapeMap[_.nodeType]}", id="${_.nodeId}"]`
            } else {
              // const className = _.status === 'Faulted' || _.status === 'Timeouted' ? 'retry' : 'normal'
              let className = 'retry'
              // 【时间节点】手动跳过功能
              if (['timeInterval', 'date'].includes(_.nodeType) && _.status === 'InProgress') {
                className = 'time-node'
              }
              // 【判断开始】节点可以执行分支选择功能
              if (['decision'].includes(_.nodeType) && _.status === 'InProgress') {
                className = 'decision-node'
              }
              if (['decision'].includes(_.nodeType) && _.status === 'Faulted') {
                className = ''
              }
              const isModelClick = this.currentModelNodeRefs.indexOf(_.orderedNo) > -1
              return `${_.nodeId} [fixedsize=false label="${
                (_.orderedNo ? _.orderedNo + ' ' : '') + _.nodeName
              }" class="flow ${className}" style="${excution || isModelClick ? 'filled' : 'none'}" color="${
                excution
                  ? statusColor[_.status]
                  : isModelClick
                    ? '#ff9900'
                    : _.nodeId === this.currentFlowNodeId
                      ? '#5DB400'
                      : '#7F8A96'
              }"  shape="${shapeMap[_.nodeType]}" id="${_.nodeId}" ]`
            }
          })
      let genEdge = () => {
        let lineName = {}
        this.flowData.nodeLinks &&
          this.flowData.nodeLinks.forEach(link => {
            lineName[link.source + link.target] = link.name
          })
        let pathAry = []
        this.flowData &&
          this.flowData.flowNodes &&
          this.flowData.flowNodes.forEach(_ => {
            if (_.succeedingNodeIds.length > 0) {
              let current = []
              current = _.succeedingNodeIds.map(to => {
                return (
                  '"' +
                  _.nodeId +
                  '"' +
                  ' -> ' +
                  `${'"' + to + '"'} [label="${lineName[_.nodeId + to]}" color="${
                    excution ? statusColor[_.status] : 'black'
                  }"]`
                )
              })
              pathAry.push(current)
            }
          })
        return pathAry.flat().toString().replace(/,/g, ';')
      }
      let nodesToString = Array.isArray(nodes) ? nodes.toString().replace(/,/g, ';') + ';' : ''
      let nodesString =
        'digraph G {' +
        'bgcolor="transparent";' +
        'Node [fontname=Arial, height=".3", fontsize=12];' +
        'Edge [fontname=Arial, color="#7f8fa6", fontsize=10];' +
        nodesToString +
        genEdge() +
        '}'

      this.flowGraph.graphviz
        .transition()
        .renderDot(nodesString)
        .on('end', () => {
          if (this.isEnqueryPage) {
            removeEvent('.retry', 'click', this.retryHandler)
            removeEvent('.normal', 'click', this.normalHandler)
            removeEvent('.time-node', 'click', this.timeNodeHandler)
            removeEvent('.decision-node', 'click', this.executeBranchHandler)
            addEvent('.retry', 'click', this.retryHandler)
            addEvent('.normal', 'click', this.normalHandler)
            addEvent('.time-node', 'click', this.timeNodeHandler)
            addEvent('.decision-node', 'click', this.executeBranchHandler)
            d3.selectAll('.retry').attr('cursor', 'pointer')
            d3.selectAll('.time-node').attr('cursor', 'pointer')
            d3.selectAll('.decision-node').attr('cursor', 'pointer')
          } else {
            removeEvent('.retry', 'click', this.retryHandler)
            removeEvent('.normal', 'click', this.normalHandler)
            removeEvent('.time-node', 'click', this.timeNodeHandler)
            removeEvent('.decision-node', 'click', this.executeBranchHandler)
          }
        })
      this.bindFlowEvent()
    },
    async excutionFlow () {
      // 区分已存在的flowInstance执行 和 新建的执行
      if (this.isEnqueryPage) {
        this.processInstance()
        this.showExcution = false
      } else {
        if (!this.selectedTarget || !this.selectedFlow) {
          this.$Message.warning(this.$t('workflow_exec_empty_tip'))
          return
        }
        this.isExecuteActive = true
        const currentTarget = this.allTarget.find(_ => _.id === this.selectedTarget)
        let taskNodeBinds = []
        this.modelData.forEach(_ => {
          let temp = []
          _.refFlowNodeIds.forEach(i => {
            temp.push({
              ..._,
              flowOrderNo: i
            })
          })
          taskNodeBinds = taskNodeBinds.concat(temp)
        })

        let payload = {
          entityDataId: currentTarget.id,
          processSessionId: this.processSessionId,
          entityDisplayName: currentTarget.displayName,
          entityTypeId: this.flowData.rootEntity,
          procDefId: this.flowData.procDefId,
          taskNodeBinds: taskNodeBinds.map(_ => {
            const node = this.flowData.flowNodes.find(node => node.orderedNo === _.flowOrderNo)
            return {
              entityDataId: _.dataId,
              entityTypeId: this.flowData.rootEntity,
              nodeDefId: (node && node.nodeDefId) || '',
              orderedNo: _.flowOrderNo
            }
          })
        }
        this.btnLoading = true
        setTimeout(() => {
          this.btnLoading = false
        }, 5000)
        let { status, data } = await createFlowInstance(payload)
        this.btnLoading = false
        if (status === 'OK') {
          this.processSessionId = ''
          this.getProcessInstances(true, data)
          this.isExecuteActive = false
          this.showExcution = false
          this.isEnqueryPage = true
          this.currentTab = 'enquery_new_workflow_job'
        }
      }
    },
    start () {
      if (this.timer === null) {
        this.getStatus()
      }
      if (this.timer !== null) {
        this.stop()
      }
      this.timer = setInterval(() => {
        this.getStatus()
      }, 5000)
    },
    stop () {
      clearInterval(this.timer)
    },
    async getStatus () {
      const found = this.allFlowInstances.find(_ => _.id === this.selectedFlowInstance)
      if (!(found && found.id)) return
      let { status, data } = await getProcessInstance(found.id)
      if (status === 'OK') {
        this.currentInstanceStatusForNodeOperation = data.status
        const inProcessNode = data.taskNodeInstances.find(
          node => ['decision'].includes(node.nodeType) && node.status === 'InProgress'
        )
        // 正在执行分支为判断分支时，拉起分支选择
        if (this.currentInstanceStatusForNodeOperation !== 'Stop' && !this.hasExecuteBranchVisible && inProcessNode) {
          this.executeBranchHandler(null, inProcessNode.nodeId)
        }
        if (
          !this.flowData.flowNodes ||
          (this.flowData.flowNodes && this.comparativeData(this.flowData.flowNodes, data.taskNodeInstances))
        ) {
          this.flowData = {
            ...data,
            flowNodes: data.taskNodeInstances
          }
          removeEvent('.retry', 'click', this.retryHandler)
          removeEvent('.normal', 'click', this.normalHandler)
          removeEvent('.time-node', 'click', this.timeNodeHandler)
          removeEvent('.decision-node', 'click', this.executeBranchHandler)
          this.initFlowGraph(true)
          this.renderModelGraph()
        }
        if (['Completed', 'InternallyTerminated', 'Faulted'].includes(data.status)) {
          this.stopSuccess = true
          this.stop()
          this.getProcessInstances(false)
          this.refreshModelData()
        }
        this.refreshModelData()
      }
    },
    async refreshModelData () {
      await this.getModelData()
      // this.modelData = []
      if ((!this.selectedFlow || !this.selectedTarget) && !this.isEnqueryPage) {
        this.renderModelGraph()
        return
      }
      if (this.processSessionId) {
        const binds = await getAllBindingsProcessSessionId(this.processSessionId)
        this.allBindingsList = binds.data
        this.renderModelGraph()
      }
    },
    comparativeData (old, newData) {
      let isNew = false
      newData.forEach(_ => {
        const found = old.find(d => d.nodeId === _.nodeId)
        if (found && found.status !== _.status) {
          isNew = true
        }
      })
      return isNew
    },
    processInstance () {
      this.start()
    },
    retryHandler (e) {
      this.currentFailedNodeID = e.target.parentNode.getAttribute('id')
      this.isNodeCanBindData = this.nodesCannotBindData.includes(this.currentFailedNodeID)
      this.retryTargetModelColums[0].disabled = this.isNodeCanBindData
      this.workflowActionModalVisible = true
      this.targetModalVisible = false
      this.showNodeDetail = false
    },
    // 显示时间节点手动跳过
    async timeNodeHandler (e) {
      const flowInstanceNode = this.flowData.flowNodes.find(
        node => node.nodeId === e.target.parentNode.getAttribute('id')
      )
      if (flowInstanceNode) {
        this.manualSkipParams.act = 'skip'
        this.manualSkipParams.message = ''
        this.manualSkipParams.procInstId = flowInstanceNode.procInstId
        this.manualSkipParams.nodeInstId = flowInstanceNode.id
        const { status, data } = await getExecutionTimeByNodeId(this.manualSkipParams.nodeInstId)
        if (status === 'OK') {
          this.manualSkipParams.dateToDisplay = data
          this.manualSkipVisible = true
        }
      }
    },
    // 确定手动跳过
    async confirmSkip () {
      const { status } = await skipNode(this.manualSkipParams)
      if (status === 'OK') {
        this.manualSkipVisible = false
        this.$Notice.success({
          title: 'Success',
          desc: 'Skip action is proceed successfully'
        })
      }
    },
    // 判断节点-显示可执行分支
    async executeBranchHandler (e, nodeId) {
      if (this.executeBranchVisible) return
      const flowInstanceNode =
        this.flowData.flowNodes &&
        this.flowData.flowNodes.find(node => node.nodeId === (nodeId || e.target.parentNode.getAttribute('id')))
      if (flowInstanceNode) {
        this.manualSkipParams.act = 'choose'
        this.manualSkipParams.message = ''
        this.manualSkipParams.procInstId = flowInstanceNode.procInstId
        this.manualSkipParams.nodeInstId = flowInstanceNode.id
        const { status, data } = await getBranchByNodeId(this.manualSkipParams.nodeInstId)
        if (status === 'OK') {
          this.manualSkipParams.branchOption = data
          this.executeBranchVisible = true
          this.hasExecuteBranchVisible = true
        }
      }
    },
    // 判断分支-执行分支
    async confirmExecuteBranch () {
      const { status } = await executeBranch(this.manualSkipParams)
      if (status === 'OK') {
        this.executeBranchVisible = false
        this.$Notice.success({
          title: 'Success',
          desc: 'Execute branch action is proceed successfully'
        })
      }
    },
    // 暂时屏蔽了，没起作用
    normalHandler (e) {
      this.flowGraphMouseenterHandler(e.target.parentNode.getAttribute('id'))
    },
    async workFlowActionHandler (type) {
      const found = this.flowData.flowNodes.find(_ => _.nodeId === this.currentFailedNodeID)
      if (!found) {
        return
      }
      if (type === 'showlog') {
        // 查看日志
        this.flowGraphMouseenterHandler(this.currentFailedNodeID)
      } else if (type === 'skip') {
        // 节点跳过
        this.$Modal.confirm({
          title: this.$t('confirm_to_skip'),
          'z-index': 100,
          onOk: async () => {
            const payload = {
              act: type,
              nodeInstId: found.id,
              procInstId: found.procInstId
            }
            this.btnLoading = true
            setTimeout(() => {
              this.btnLoading = false
            }, 5000)
            const { status } = await retryProcessInstance(payload)
            this.btnLoading = false
            if (status === 'OK') {
              this.$Notice.success({
                title: 'Success',
                desc: (type === 'retry' ? 'Retry' : 'Skip') + ' action is proceed successfully'
              })
              this.workflowActionModalVisible = false
              this.processInstance()
            }
          },
          onCancel: () => {}
        })
      } else if (type === 'retry') {
        this.executeRetry(found, type)
      } else if (type === 'risky') {
        // 高危确认
        this.executeRisky(found)
      } else {
        const payload = {
          nodeInstId: found.id,
          procInstId: found.procInstId
        }
        this.currentAction = type
        this.currentNodeTitle = `${found.orderedNo}${found.orderedNo ? '、' : ''}${found.nodeName}`
        this.getTaskNodeInstanceExecBindings(payload)
        this.retryTargetModalVisible = true
      }
    },
    async executeRetry (nodeInfo, type) {
      const payload = {
        act: type,
        nodeInstId: nodeInfo.id,
        procInstId: nodeInfo.procInstId
      }
      this.btnLoading = true
      setTimeout(() => {
        this.btnLoading = false
      }, 5000)
      const { status } = await retryProcessInstance(payload)
      this.btnLoading = false
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: (type === 'retry' ? 'Retry' : 'Skip') + ' action is proceed successfully'
        })
        this.workflowActionModalVisible = false
        this.processInstance()
      }
    },
    async executeRisky (nodeInfo) {
      this.confirmModal.message = ''
      this.confirmModal.requestBody = ''
      this.confirmModal.check = false
      const { status, data } = await getNodeContext(nodeInfo.procInstId, nodeInfo.id)
      if (status === 'OK') {
        this.confirmModal.message = data.errorMessage
        this.confirmModal.check = false
        this.confirmModal.isShowConfirmModal = true
        this.confirmModal.requestBody = nodeInfo
      }
    },
    confirmToExecution () {
      this.confirmModal.isShowConfirmModal = false
      this.executeRetry(this.confirmModal.requestBody, 'retry')
    },
    async getTaskNodeInstanceExecBindings (payload) {
      const erroInfo = await this.getErrorLog(this.currentFailedNodeID)
      const { status, data } = await getTaskNodeInstanceExecBindings(payload)
      if (status === 'OK') {
        this.retryTartetModels = data
        this.retryTartetModels.forEach(item => {
          item._checked = false
        })
        this.retryCatchNodeTableList = JSON.parse(JSON.stringify(data))
        this.retryCatchNodeTableList.forEach((tm, index) => {
          tm._checked = false
          const find = erroInfo.find(info => info.id === tm.entityDataId)
          let retryTartetModelsSingle = this.retryTartetModels[index]
          if (find) {
            if (find.errorCode === '-1') {
              tm.confirmToken = 'Y'
              retryTartetModelsSingle.status = 'Confirm'
            } else if (find.errorCode === '1') {
              tm.confirmToken = ''
              retryTartetModelsSingle.status = 'Error'
            } else if (find.errorCode === '0') {
              tm.confirmToken = ''
              retryTartetModelsSingle.status = ''
            } else {
              tm.confirmToken = ''
              retryTartetModelsSingle.status = ''
            }
            retryTartetModelsSingle.message = find.errorMessage
          } else {
            tm.confirmToken = ''
            retryTartetModelsSingle.status = ''
            retryTartetModelsSingle.message = ''
          }
          this.retryTartetModels.forEach(cn => {
            if (tm.id === cn.id && tm.bound === 'Y' && tm.confirmToken === '') {
              cn._checked = true
            }
          })
        })
      }
    },
    async getErrorLog (id) {
      const found = this.flowData.flowNodes.find(_ => _.nodeId === id)
      const { status, data } = await getNodeContext(found.procInstId, found.id)
      if (status === 'OK') {
        const errorInfo = data.requestObjects.map(item => {
          return {
            id: item.callbackParameter,
            errorMessage: (item.outputs[0] && item.outputs[0].errorMessage) || '',
            errorCode: (item.outputs[0] && item.outputs[0].errorCode) || ''
          }
        })
        return errorInfo
      }
    },
    bindFlowEvent () {
      if (this.isEnqueryPage !== true) {
        addEvent('.flow', 'mouseover', e => {
          e.preventDefault()
          e.stopPropagation()
          d3.selectAll('g').attr('cursor', 'pointer')
        })
        removeEvent('.flow', 'click', this.flowNodesClickHandler)
        addEvent('.flow', 'click', this.flowNodesClickHandler)
      } else {
        removeEvent('.flow', 'click', this.flowNodesClickHandler)
      }
    },
    // 查看日志
    flowGraphMouseenterHandler (id) {
      clearTimeout(this.flowDetailTimer)
      this.flowDetailTimer = setTimeout(async () => {
        const found = this.flowData.flowNodes.find(_ => _.nodeId === id)
        this.nodeTitle = (found.orderedNo ? found.orderedNo + '、' : '') + found.nodeName
        const { status, data } = await getNodeContext(found.procInstId, found.id)
        if (status === 'OK') {
          this.nodeDetailResponseHeader = JSON.parse(JSON.stringify(data))
          this.pluginInfo = this.nodeDetailResponseHeader.pluginInfo
          delete this.nodeDetailResponseHeader.requestObjects
          this.nodeDetailIO = data.requestObjects.map(ro => {
            ro['inputs'] = this.replaceParams(ro['inputs'])
            ro['outputs'] = this.replaceParams(ro['outputs'])
            return ro
          })
          // 日志input output表格添加子编排查看按钮
          if (this.nodeDetailResponseHeader && this.nodeDetailResponseHeader.nodeType === 'subProc') {
            const hasFlag = this.nodeDetailColumns.some(i => i.key === 'procDefId')
            if (!hasFlag) {
              this.nodeDetailColumns.push({
                title: this.$t('child_workflow'),
                key: 'procDefId',
                width: 200,
                render: (h, params) => {
                  let procDefName = ''
                  let procInsId = ''
                  let version = ''
                  if (Array.isArray(params.row.outputs) && params.row.outputs.length > 0) {
                    procDefName = params.row.outputs[0].procDefName || '-'
                    procInsId = params.row.outputs[0].procInsId || ''
                    version = params.row.outputs[0].version || ''
                  }
                  return (
                    <span
                      style="cursor:pointer;color:#5cadff;"
                      onClick={() => {
                        this.viewSubProcExecutionDetail(procInsId)
                      }}
                    >
                      {procDefName}
                      <Tag style="margin-left:2px">{version}</Tag>
                    </span>
                  )
                }
              })
            }
          } else {
            this.nodeDetailColumns = this.nodeDetailColumns.filter(i => i.key !== 'procDefId')
          }
        }
        this.tableMaxHeight = 400
      }, 200)
    },
    replaceParams (obj) {
      let placeholder = new Array(16).fill('&nbsp;')
      placeholder.unshift('<br/>')
      for (let key in obj) {
        if (obj[key] !== null && typeof obj[key] === 'string') {
          obj[key] = obj[key].replace('\r\n', placeholder.join(''))
        }
      }
      return obj
    },
    flowNodesClickHandler (e) {
      e.preventDefault()
      e.stopPropagation()
      let g = e.currentTarget
      this.currentFlowNodeId = g.id
      const currentNode = this.flowData.flowNodes.find(_ => {
        return _.nodeId === this.currentFlowNodeId
      })
      this.currentNodeTitle = `${currentNode.orderedNo}${currentNode.orderedNo ? '、' : ''}${currentNode.nodeName}`
      this.highlightModel(g.id, currentNode.nodeDefId)
      this.renderFlowGraph()
    },
    async highlightModel (nodeId, nodeDefId) {
      if (nodeDefId && this.processSessionId) {
        let { status, data } = await getDataByNodeDefIdAndProcessSessionId(nodeDefId, this.processSessionId)
        if (status === 'OK') {
          this.tartetModels = data.map(_ => {
            const { nodeType } = this.flowData.flowNodes.find(i => i.nodeId === _.nodeDefId) || { nodeType: '' }
            return {
              ..._,
              ...this.modelData.find(j => j.dataId === _.entityDataId),
              nodeType
            }
          })
        } else {
          this.tartetModels = []
        }
      } else {
        return
      }

      this.catchTartetModels = []
      this.catchNodeTableList = []
      this.catchTartetModels = JSON.parse(JSON.stringify(this.tartetModels))
      this.isNodeCanBindData = this.nodesCannotBindData.includes(nodeId)
      this.targetModelColums[0].disabled = this.isNodeCanBindData
      this.targetModalVisible = true
      this.showNodeDetail = false
      this.$nextTick(() => {
        let objData = this.$refs.selection.objData
        Object.keys(objData).forEach(i => {
          this.allBindingsList.forEach(j => {
            if (j.nodeDefId === nodeDefId && j.entityDataId === objData[i].entityDataId) {
              if (objData[i].bound === 'Y') {
                objData[i]._isChecked = true
              }
              this.catchNodeTableList.push(objData[i])
            }
          })
        })
      })
    },
    reloadGraph () {
      const graphEl = document.getElementById('graph')
      const initEvent = () => {
        let graph
        graph = d3.select(`#graph`)
        graph.on('dblclick.zoom', null).on('wheel.zoom', null).on('mousewheel.zoom', null)
        this.graph.graphviz = graph
          .graphviz()
          .fit(true)
          .zoom(true)
          .height(graphEl.offsetHeight - 10)
          .width(graphEl.offsetWidth - 10)
      }
      initEvent()
    },
    initModelGraph () {
      const graphEl = document.getElementById('graph')
      const initEvent = () => {
        let graph
        graph = d3.select(`#graph`)
        graph.on('dblclick.zoom', null).on('wheel.zoom', null).on('mousewheel.zoom', null)
        this.graph.graphviz = graph
          .graphviz()
          .fit(true)
          .zoom(true)
          .height(graphEl.offsetHeight - 10)
          .width(graphEl.offsetWidth - 10)
      }
      initEvent()
      this.formatRefNodeIds()
      this.renderModelGraph()
    },
    initFlowGraph (excution = false) {
      const graphEl = document.getElementById('flow')
      let graph
      graph = d3.select(`#flow`)
      graph.on('dblclick.zoom', null)
      this.flowGraph.graphviz = graph
        .graphviz()
        .fit(true)
        .zoom(true)
        .height(graphEl.offsetHeight - 10)
        .width(graphEl.offsetWidth - 10)
      this.renderFlowGraph(excution)
    },
    zoomModal () {
      this.tableMaxHeight = document.body.scrollHeight - 410
      this.nodeDetailFullscreen = true
    },
    // #region 暂停、继续编排
    async flowControlHandler (operateType) {
      let payload = {
        procInstId: this.selectedFlowInstance,
        act: operateType
      }
      const { status } = await pauseAndContinueFlow(payload)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
        this.currentInstanceStatusForNodeOperation = operateType === 'stop' ? 'Stop' : 'InProgress'
        this.getProcessInstances()
      }
    }
    // #endregion
  }
}
</script>
<style lang="scss">
.platform-base-drawer .jv-container .jv-code {
  overflow: hidden;
  padding: 0px 20px;
}
</style>
<style lang="scss" scoped>
.workflow-execution {
  &-header {
    display: flex;
  }
  .back-header {
    width: 30px;
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
  .form {
    flex: 1;
  }
}
body {
  color: #e5f173; //#15a043;
}
.pages ::v-deep .ivu-select-dropdown {
  height: 500px !important;
}
::v-deep .jv-code {
  padding: 10px 10px !important;
}
.header-icon {
  margin: 3px 40px 0 0 !important;
}
#graph-container {
  border: 1px solid #d3cece;
  border-radius: 3px;
  padding: 5px;
  height: calc(100vh - 180px);
}
.model_target .ivu-modal-content-drag {
  right: 40px;
}
.pages ::v-deep .ivu-card-body {
  padding: 8px;
}
.ivu-form-item {
  margin-bottom: 0 !important;
  padding-left: 15px;
}
.excution-serach {
  margin: 5px 6px 0 0;
}
.graph-container {
  overflow: auto;
}
.header-icon {
  float: right;
  margin: 3px 20px 0 0;
}
.reset-button {
  position: absolute;
  right: 20px;
  bottom: 5px;
  font-size: 12px;
}
.set-data-title {
  position: absolute;
  left: 10px;
  top: 5px;
  font-size: 14px;
}
.set-data-button {
  position: absolute;
  right: 10px;
  top: 5px;
  font-size: 12px;
}
.no-data {
  padding: 10px;
}
</style>
