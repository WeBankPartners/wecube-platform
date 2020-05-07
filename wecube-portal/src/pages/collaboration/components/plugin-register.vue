<template>
  <div class="plugin-register-page">
    <Row>
      <Col span="6">
        <div v-if="plugins.length < 1">{{ $t('no_plugin') }}</div>
        <Menu theme="light" :active-name="currentPlugin" @on-select="selectPlugin" style="width: 100%;z-index:10">
          <Submenu v-for="(plugin, index) in plugins" :name="plugin.pluginConfigName" style="padding: 0;" :key="index">
            <template slot="title">
              <Icon type="md-flower" />
              <span style="font-size: 17px; font-weight:500">{{ plugin.pluginConfigName }}</span>
              <Button
                size="small"
                type="text"
                style="color: #2d8cf0;"
                icon="md-add-circle"
                @click.stop.prevent="addPluginConfigDto(plugin)"
                >{{ $t('add') }}</Button
              >
            </template>
            <MenuItem
              v-for="(dto, index) in plugin.pluginConfigDtoList.filter(dto => dto.registerName)"
              :name="dto.id"
              :key="index"
              style="padding: 5px 30px;"
            >
              <span style="font-size: 15px; font-weight:400">{{ dto.name }}-({{ dto.registerName }})</span>
              <Button
                size="small"
                type="text"
                style="color: #19be6b;"
                icon="md-copy"
                @click.stop.prevent="copyPluginConfigDto(dto.id)"
                >{{ $t('copy') }}</Button
              >
            </MenuItem>
          </Submenu>
        </Menu>
      </Col>
      <Col span="18" offset="0" style="padding-left: 10px">
        <Spin size="large" fix style="margin-top: 200px;" v-show="isLoading">
          <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
          <div>{{ $t('loading') }}</div>
        </Spin>
        <Form :model="form" v-show="hidePanal">
          <Row style="border-bottom: 1px solid #bbb7b7; margin-top: 20px">
            <Col span="12" offset="0">
              <FormItem :label-width="100" :label="$t('regist_name')">
                <Input v-model="registerName" ref="registerName" :disabled="currentPluginObj.status === 'ENABLED'" />
              </FormItem>
            </Col>
            <Col span="12" offset="0">
              <FormItem :label-width="100" :label="$t('target_type')">
                <FilterRules
                  v-model="selectedEntityType"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  :allDataModelsWithAttrs="allEntityType"
                ></FilterRules>
              </FormItem>
            </Col>
          </Row>
          <div id="paramsContainer">
            <Collapse v-model="activePanel" accordion>
              <Panel
                v-for="(inter, index) in currentPluginObj.interfaces"
                :key="index + inter.action"
                :name="index + inter.action"
              >
                <Input
                  :value="inter.action"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  @click.stop.native="onFocus($event, inter)"
                  @input.stop.native="actionInputHandler($event, inter)"
                  @on-blur="actionBlurHandler($event, inter)"
                  style="width:200px"
                  size="small"
                />
                <InterfaceFilterRule
                  v-model="inter.filterRule"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  :rootEntity="rootEntity"
                  :allDataModelsWithAttrs="allEntityType"
                ></InterfaceFilterRule>
                <Button
                  size="small"
                  type="success"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  ghost
                  icon="md-copy"
                  @click.stop.prevent="copyInterface(inter)"
                  >{{ $t('copy') }}</Button
                >
                <Tooltip :content="$t('completely_deleted')" placement="top">
                  <Button
                    size="small"
                    type="error"
                    :disabled="currentPluginObj.status === 'ENABLED'"
                    ghost
                    icon="ios-trash-outline"
                    @click.stop.prevent="deleteInterface(index)"
                    >{{ $t('remove') }}</Button
                  >
                </Tooltip>
                <div slot="content">
                  <Row style="border-bottom: 1px solid gray;margin-bottom:5px">
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('params_type') }}</strong>
                    </Col>
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('params_name') }}</strong>
                    </Col>
                    <Col span="3" offset="0" style="text-align: center">
                      <strong style="font-size:15px;">{{ $t('data_type') }}</strong>
                    </Col>
                    <Col span="3" offset="0">
                      <strong style="font-size:15px;">{{ $t('sensitive') }}</strong>
                    </Col>
                    <Col span="8" offset="0">
                      <strong style="font-size:15px;">{{ $t('attribute') }}</strong>
                    </Col>
                    <Col span="3" offset="1">
                      <strong style="font-size:15px;">
                        {{ $t('attribute_type') }}
                      </strong>
                    </Col>
                  </Row>
                  <div class="interfaceContainers">
                    <Row>
                      <Col span="3">
                        <FormItem :label-width="0">
                          <span>{{ $t('input_params') }}</span>
                        </FormItem>
                      </Col>
                      <Col span="21" offset="0">
                        <Row v-for="(param, index) in inter['inputParameters']" :key="index">
                          <Col span="5">
                            <FormItem :label-width="0">
                              <Tooltip :content="param.name" style="width: 100%">
                                <div>
                                  <span v-if="param.required === 'Y'" style="color:red">*</span>
                                  <span
                                    style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                                    >{{ param.name }}</span
                                  >
                                </div>
                              </Tooltip>
                            </FormItem>
                          </Col>
                          <Col span="2" offset="0">
                            <FormItem :label-width="0">
                              <span>{{ param.dataType }}</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="0">
                            <FormItem :label-width="0">
                              <Select
                                v-model="param.sensitiveData"
                                size="small"
                                style="width:50px"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                              >
                                <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                                  item.label
                                }}</Option>
                              </Select>
                            </FormItem>
                          </Col>
                          <Col span="10" offset="0">
                            <FormItem :label-width="0">
                              <FilterRules
                                v-if="param.mappingType === 'entity'"
                                v-model="param.mappingEntityExpression"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                :allDataModelsWithAttrs="allEntityType"
                                :rootEntity="selectedEntityType"
                                :needNativeAttr="true"
                                :needAttr="true"
                              ></FilterRules>
                              <Select
                                v-if="param.mappingType === 'system_variable'"
                                v-model="param.mappingSystemVariableName"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                @on-open-change="retrieveSystemVariables"
                              >
                                <Option
                                  v-for="(item, index) in allSystemVariables"
                                  v-if="item.status === 'active'"
                                  :value="item.name"
                                  :key="index"
                                  >{{ item.name }}</Option
                                >
                              </Select>
                              <span v-if="param.mappingType === 'context' || param.mappingType === 'constant'"
                                >N/A</span
                              >
                            </FormItem>
                          </Col>
                          <Col span="3" offset="1">
                            <FormItem :label-width="0">
                              <Select
                                size="small"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                v-model="param.mappingType"
                                @on-change="mappingTypeChange($event, param)"
                              >
                                <Option value="context" key="context">context</Option>
                                <Option value="system_variable" key="system_variable">system_variable</Option>
                                <Option value="entity" key="entity">entity</Option>
                                <Option value="constant" key="constant">constant</Option>
                              </Select>
                            </FormItem>
                          </Col>
                        </Row>
                      </Col>
                    </Row>
                    <Row>
                      <Col span="3">
                        <FormItem :label-width="0">
                          <span>{{ $t('output_params') }}</span>
                        </FormItem>
                      </Col>
                      <Col span="21" offset="0">
                        <Row v-for="(outPut, index) in inter['outputParameters']" :key="index">
                          <Col span="5">
                            <FormItem :label-width="0">
                              <Tooltip :content="outPut.name" style="width: 100%">
                                <span v-if="outPut.required === 'Y'" style="color:red">*</span>
                                <span
                                  style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                                  >{{ outPut.name }}</span
                                >
                              </Tooltip>
                            </FormItem>
                          </Col>
                          <Col span="2" offset="0">
                            <FormItem :label-width="0">
                              <span>{{ outPut.dataType }}</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="0">
                            <FormItem :label-width="0">
                              <Select
                                v-model="outPut.sensitiveData"
                                size="small"
                                style="width:50px"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                              >
                                <Option v-for="item in sensitiveData" :value="item.value" :key="item.value">{{
                                  item.label
                                }}</Option>
                              </Select>
                            </FormItem>
                          </Col>
                          <Col span="10" offset="0">
                            <FormItem :label-width="0">
                              <FilterRules
                                v-if="outPut.mappingType === 'entity'"
                                v-model="outPut.mappingEntityExpression"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                :allDataModelsWithAttrs="allEntityType"
                                :rootEntity="selectedEntityType"
                                :needNativeAttr="true"
                                :needAttr="true"
                              ></FilterRules>
                              <span v-if="outPut.mappingType === 'context'">N/A</span>
                            </FormItem>
                          </Col>
                          <Col span="3" offset="1">
                            <FormItem :label-width="0">
                              <Select
                                size="small"
                                :disabled="currentPluginObj.status === 'ENABLED'"
                                v-model="outPut.mappingType"
                              >
                                <Option value="context" key="context">context</Option>
                                <Option value="entity" key="entity">entity</Option>
                              </Select>
                            </FormItem>
                          </Col>
                        </Row>
                      </Col>
                    </Row>
                  </div>
                </div>
              </Panel>
            </Collapse>
          </div>
          <Row v-if="currentPluginObjKeysLength > 1" style="margin:20px auto">
            <Col span="9" offset="8">
              <Button type="primary" v-if="currentPluginObj.status === 'DISABLED'" @click="pluginSave">{{
                $t('save')
              }}</Button>
              <Button type="primary" v-if="currentPluginObj.status === 'DISABLED'" @click="regist">{{
                $t('regist')
              }}</Button>
              <Button type="error" v-if="currentPluginObj.status === 'DISABLED'" @click="deleteRegisterSource">{{
                $t('delete')
              }}</Button>
              <Button type="error" v-if="currentPluginObj.status === 'ENABLED'" @click="removePlugin">{{
                $t('decommission')
              }}</Button>
            </Col>
          </Row>
        </Form>
      </Col>
    </Row>
  </div>
</template>
<script>
import FilterRules from '../../components/filter-rules.vue'
import InterfaceFilterRule from '../../components/interface-filter-rule.vue'
import {
  getAllPluginByPkgId,
  getAllDataModels,
  registerPlugin,
  deletePlugin,
  deleteRegisterSource,
  savePluginConfig,
  retrieveSystemVariables,
  getPluginConfigsByPackageId,
  getInterfacesByPluginConfigId
} from '@/api/server'

export default {
  data () {
    return {
      inputtingValue: '',
      isLoading: false,
      activePanel: null,
      allDataModelsWithAttrs: {},
      currentPlugin: '',
      plugins: [],
      addRegistModal: false,
      currentPluginObj: {},
      sourceList: [],
      selectedSource: '',
      registerName: '',
      addRegisterName: '',
      hasNewSource: false,
      hidePanal: false,
      allEntityType: [],
      selectedEntityType: '',
      form: {},
      allSystemVariables: [],
      sensitiveData: [
        {
          value: 'Y',
          label: 'Y'
        },
        {
          value: 'N',
          label: 'N'
        }
      ]
    }
  },
  components: {
    FilterRules,
    InterfaceFilterRule
  },
  computed: {
    rootEntity () {
      if (this.selectedEntityType && this.selectedEntityType.length > 0) {
        return this.selectedEntityType.split('{')[0].split(':')[1]
      } else {
        return ''
      }
      // return ''
    },
    currentPluginObjKeysLength () {
      return Object.keys(this.currentPluginObj).length
    },
    allPluginConfigs () {
      return [].concat(...this.plugins.map(p => p.pluginConfigDtoList))
    }
  },
  props: {
    pkgId: {
      required: true
    },
    pkgName: {
      required: true
    }
  },
  watch: {
    selectedEntityType: {
      handler (val) {}
    }
  },
  methods: {
    async retrieveSystemVariables () {
      const { data, status } = await retrieveSystemVariables({
        filters: [],
        paging: false
      })
      if (status === 'OK') {
        this.allSystemVariables = data.contents
      }
    },
    async pluginSave () {
      if (this.registerName.length === 0) {
        this.$refs.registerName.focus()
        this.$Notice.warning({
          title: 'Warning',
          desc: '输入注册名称'
        })
        return
      }
      let currentPluginForSave = JSON.parse(JSON.stringify(this.currentPluginObj))
      currentPluginForSave.registerName = this.registerName
      currentPluginForSave.targetEntityWithFilterRule = this.selectedEntityType
      if (this.hasNewSource) {
        delete currentPluginForSave.id
        currentPluginForSave.interfaces.map(_ => {
          delete _.id
        })
      }
      const { data, status, message } = await savePluginConfig(currentPluginForSave)
      if (status === 'OK') {
        const id = data.id
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        if (this.hasNewSource) {
          this.hasNewSource = false
        }
        await this.getAllPluginByPkgId()
        this.getInterfacesByPluginConfigId(id)
      }
    },
    mappingTypeChange (v, param) {
      if (v === 'entity') {
        param.mappingEntityExpression = null
      }
    },
    async regist () {
      this.currentPluginObj.registerName = this.registerName
      let currentPluginForSave = JSON.parse(JSON.stringify(this.currentPluginObj))
      currentPluginForSave.targetEntityWithFilterRule = this.selectedEntityType
      if (this.hasNewSource) {
        delete currentPluginForSave.id
        currentPluginForSave.interfaces.map(_ => {
          delete _.id
        })
      }
      const saveRes = await savePluginConfig(currentPluginForSave)
      if (saveRes.status === 'OK') {
        if (this.hasNewSource) {
          this.hasNewSource = false
        }
        const { status, message } = await registerPlugin(saveRes.data.id)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Success',
            desc: message
          })
          await this.getAllPluginByPkgId()
          this.getInterfacesByPluginConfigId(saveRes.data.id)
        }
      }
    },
    async deleteRegisterSource () {
      const { status, message } = await deleteRegisterSource(this.currentPluginObj.id)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        const { data, status } = await getAllPluginByPkgId(this.pkgId)
        if (status === 'OK') {
          this.plugins = data
        }
        this.hidePanal = false
      }
    },
    addRegistMsg () {
      this.addRegisterName = ''
      this.addRegistModal = true
      this.hasNewSource = false
    },
    ok () {
      this.registerName = this.addRegisterName
      this.currentPluginObj.id = new Date().getMilliseconds() + ''
      this.currentPluginObj.registerName = this.registerName
      this.sourceList.push(this.currentPluginObj)
      this.selectedSource = this.currentPluginObj.id
      this.hasNewSource = true
    },
    async removePlugin () {
      const { status, message } = await deletePlugin(this.currentPluginObj.id)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        await this.getAllPluginByPkgId()
        this.getInterfacesByPluginConfigId(this.currentPluginObj.id)
      }
    },
    actionInputHandler (e, i) {
      console.log(e)
      if (e.inputType === 'insertText') {
        this.inputtingValue += e.data
      } else {
        this.inputtingValue = this.inputtingValue.substr(0, this.inputtingValue.length - 1)
      }
      // i.action += e.data
      // e.target.focus()
    },
    onFocus (e, i) {
      this.inputtingValue = i.action
    },
    actionBlurHandler (e, i) {
      i.action = this.inputtingValue
      this.inputtingValue = ''
      console.log(i)
    },
    copyInterface (interfaces) {
      const found = this.currentPluginObj.interfaces.find(_ => _.action === interfaces.action + '-(copy)')
      if (found) return
      let i = { ...interfaces }
      i.action = interfaces.action + '-(copy)'
      i.id = null
      i.inputParameters = i.inputParameters.map(_ => {
        return { ..._, id: null }
      })
      i.outputParameters = i.outputParameters.map(_ => {
        return { ..._, id: null }
      })
      this.currentPluginObj.interfaces.push(i)
    },
    deleteInterface (index) {
      this.currentPluginObj.interfaces.splice(index, 1)
    },
    async getAllPluginByPkgId () {
      const { data, status } = await getPluginConfigsByPackageId(this.pkgId)
      if (status === 'OK') {
        this.plugins = data
      }
    },
    async copyPluginConfigDto (id) {
      this.currentPlugin = ''
      this.hasNewSource = true
      await this.getInterfacesByPluginConfigId(id)
      this.registerName = this.currentPluginObj.registerName + '-(copy)'
      this.currentPluginObj.status = 'DISABLED'
      this.$refs.registerName.focus()
    },
    async addPluginConfigDto (plugin) {
      this.hasNewSource = true
      const id = plugin.pluginConfigDtoList.find(_ => _.registerName === null).id
      await this.getInterfacesByPluginConfigId(id)
      this.registerName = ''
      this.currentPluginObj.status = 'DISABLED'
      this.$refs.registerName.focus()
    },
    selectPlugin (val) {
      this.hasNewSource = false
      this.currentPlugin = val
      this.getInterfacesByPluginConfigId(val)
    },
    async getInterfacesByPluginConfigId (id) {
      this.hidePanal = false
      this.isLoading = true
      this.currentPluginObj = {}
      let currentConfig = this.allPluginConfigs.find(s => s.id === id)
      const { data, status } = await getInterfacesByPluginConfigId(id)
      if (status === 'OK') {
        currentConfig.interfaces = data.map(_ => {
          return {
            ..._,
            filterRule: _.filterRule ? _.filterRule : ''
          }
        })
      }
      this.currentPluginObj = currentConfig
      this.selectedEntityType = currentConfig.targetEntityWithFilterRule
      this.registerName = this.currentPluginObj.registerName
      this.hidePanal = true
      this.isLoading = false
    },
    copyRegistSource (v) {
      this.registSourceChange(v)
      this.currentPluginObj.status = 'DISABLED'
    },
    async getAllDataModels () {
      const { data, status } = await getAllDataModels()
      if (status === 'OK') {
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
    }
  },
  created () {
    this.getAllPluginByPkgId()
    this.getAllDataModels()
    this.retrieveSystemVariables()
  }
}
</script>
<style lang="scss">
.plugin-register-page {
  .interfaceContainers {
    overflow: auto;
    height: calc(100vh - 450px);
  }
  .ivu-menu-vertical .ivu-menu-submenu-title {
    padding: 8px 0px;
  }
  .ivu-menu-vertical .ivu-menu-submenu-title-icon {
    right: 0;
  }
  .ivu-menu-vertical .ivu-menu-opened > * > .ivu-menu-submenu-title-icon {
    color: #2d8cf0;
  }
  .ivu-menu-opened {
    .ivu-menu-submenu-title {
      background: rgb(224, 230, 231);
      border-radius: 5px;
    }
  }
}
</style>
