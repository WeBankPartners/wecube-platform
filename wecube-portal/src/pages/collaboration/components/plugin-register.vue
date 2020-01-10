<template>
  <div>
    <Row>
      <Col span="5">
        <div v-if="plugins.length < 1">{{ $t('no_plugin') }}</div>
        <Menu
          theme="light"
          :active-name="currentPlugin"
          @on-select="selectPlugin"
          style="width: 100%;z-index:10"
        >
          <MenuItem
            v-for="(plugin, index) in plugins"
            :name="plugin.pluginConfigName"
            :key="index"
            style="padding: 10px 5px;"
          >
            <Icon type="md-flower" />
            {{ plugin.pluginConfigName }}
          </MenuItem>
        </Menu>
      </Col>
      <Col span="19" offset="0" style="padding-left: 10px" v-show="hidePanal">
        <Form v-show="currentPlugin.length > 0" :model="form">
          <Row>
            <Col style="border-bottom: 2px solid #bbb7b7;">
              <FormItem :label-width="100" :label="$t('regist_source')">
                <Select
                  v-model="selectedSource"
                  @on-change="registSourceChange"
                  :placeholder="$t('regist_source_placeholder')"
                >
                  <Option value="add" key="add">
                    <Button @click="addRegistMsg" type="success" long>
                      <Icon type="md-add" />
                    </Button>
                  </Option>
                  <Option
                    v-for="item in sourceList"
                    :value="item.id"
                    :key="item.id"
                    >{{ item.name }}-({{ item.registerName }})</Option
                  >
                </Select>
              </FormItem>
            </Col>
          </Row>
          <Row style="margin-top: 20px;">
            <Col span="12" offset="0">
              <FormItem :label-width="100" :label="$t('regist_name')">
                <Input
                  v-model="registerName"
                  :disabled="currentPluginObj.status === 'ENABLED'"
                />
              </FormItem>
            </Col>
            <Col span="12" offset="0">
              <FormItem :label-width="100" :label="$t('target_type')">
                <Select
                  @on-change="onSelectEntityType"
                  v-model="selectedEntityType"
                  label-in-value
                  :disabled="currentPluginObj.status === 'ENABLED'"
                  @on-open-change="getAllDataModels"
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
                      >{{ item.name
                      }}<span style="display:none"
                        >**{{ pluginPackage.packageName }}</span
                      >
                    </Option>
                  </OptionGroup>
                </Select>
              </FormItem>
            </Col>
          </Row>
          <hr />
          <Row style="margin-bottom:10px;margin-top:10px">
            <Col span="3">
              <strong style="font-size:15px;">{{ $t('operation') }}</strong>
            </Col>
            <Col span="3">
              <strong style="font-size:15px;">{{ $t('params_type') }}</strong>
            </Col>
            <Col span="3" offset="0">
              <strong style="font-size:15px;">{{ $t('params_name') }}</strong>
            </Col>
            <Col span="3" offset="0">
              <strong style="font-size:15px;">{{ $t('data_type') }}</strong>
            </Col>
            <Col span="3" offset="1">
              <strong style="font-size:15px;">{{ $t('attribute') }}</strong>
            </Col>
            <Col span="3" offset="4">
              <strong style="font-size:15px;">{{
                $t('attribute_type')
              }}</strong>
            </Col>
          </Row>
          <div id="paramsContainer">
            <Row
              class="interfaceContainer"
              v-for="(interfaces, index) in currentPluginObj.interfaces"
              :key="index"
            >
              <Col span="3">
                <FormItem :label-width="0">
                  <Tooltip :content="interfaces.action" style="width: 100%">
                    <span
                      style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;width: 90%;"
                      >{{ interfaces.action }}</span
                    >
                  </Tooltip>
                </FormItem>
              </Col>
              <Col span="21">
                <Row>
                  <Col span="3">
                    <FormItem :label-width="0">
                      <span>{{ $t('input_params') }}</span>
                    </FormItem>
                  </Col>
                  <Col span="21" offset="0">
                    <Row
                      v-for="param in interfaces['inputParameters']"
                      :key="param.id"
                    >
                      <Col span="5">
                        <FormItem :label-width="0">
                          <Tooltip :content="param.name" style="width: 100%">
                            <span
                              v-if="param.required === 'Y'"
                              style="color:red"
                              >*</span
                            >
                            <span
                              style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                              >{{ param.name }}</span
                            >
                          </Tooltip>
                        </FormItem>
                      </Col>
                      <Col span="3">
                        <FormItem :label-width="0">
                          <span>{{ param.dataType }}</span>
                        </FormItem>
                      </Col>
                      <Col span="10" offset="0">
                        <FormItem :label-width="0">
                          <PathExp
                            v-if="param.mappingType === 'entity'"
                            :rootPkg="pkgName"
                            :rootEntity="selectedEntityType"
                            :allDataModelsWithAttrs="allEntityType"
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            v-model="param.mappingEntityExpression"
                          ></PathExp>
                          <Select
                            v-if="param.mappingType === 'system_variable'"
                            v-model="param.mappingSystemVariableName"
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            @on-open-change="retrieveSystemVariables"
                          >
                            <Option
                              v-for="item in allSystemVariables"
                              v-if="item.status === 'active'"
                              :value="item.name"
                              :key="item.name"
                              >{{ item.name }}</Option
                            >
                          </Select>
                          <span
                            v-if="
                              param.mappingType === 'context' ||
                                param.mappingType === 'constant'
                            "
                            >N/A</span
                          >
                        </FormItem>
                      </Col>
                      <Col span="4" offset="1">
                        <FormItem :label-width="0">
                          <Select
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            v-model="param.mappingType"
                            @on-change="mappingTypeChange($event, param)"
                          >
                            <Option value="context" key="context"
                              >context</Option
                            >
                            <Option
                              value="system_variable"
                              key="system_variable"
                              >system_variable</Option
                            >
                            <Option value="entity" key="entity">entity</Option>
                            <Option value="constant" key="constant"
                              >constant</Option
                            >
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
                    <Row
                      v-for="outPut in interfaces['outputParameters']"
                      :key="outPut.id + 1000"
                    >
                      <Col span="4">
                        <FormItem :label-width="0">
                          <Tooltip :content="outPut.name" style="width: 100%">
                            <span
                              v-if="outPut.required === 'Y'"
                              style="color:red"
                              >*</span
                            >
                            <span
                              style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 90%;"
                              >{{ outPut.name }}</span
                            >
                          </Tooltip>
                        </FormItem>
                      </Col>
                      <Col span="3" offset="1">
                        <FormItem :label-width="0">
                          <span>{{ outPut.dataType }}</span>
                        </FormItem>
                      </Col>
                      <Col span="10" offset="0">
                        <FormItem :label-width="0">
                          <!-- <Select
                          v-if="outPut.mappingType === 'entity'"
                          v-model="outPut.mappingEntityExpression"
                          :disabled="currentPluginObj.status === 'ENABLED'"
                        >
                          <Option
                            v-for="attr in currentEntityAttr"
                            :key="attr.name"
                            :value="attr.name"
                            :label="attr.name"
                          ></Option>
                        </Select> -->
                          <PathExp
                            v-if="outPut.mappingType === 'entity'"
                            :rootPkg="pkgName"
                            :rootEntity="selectedEntityType"
                            :allDataModelsWithAttrs="allEntityType"
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            v-model="outPut.mappingEntityExpression"
                          ></PathExp>
                          <span v-if="outPut.mappingType === 'context'"
                            >N/A</span
                          >
                        </FormItem>
                      </Col>
                      <Col span="4" offset="1">
                        <FormItem :label-width="0">
                          <Select
                            :disabled="currentPluginObj.status === 'ENABLED'"
                            v-model="outPut.mappingType"
                          >
                            <Option value="context" key="context"
                              >context</Option
                            >
                            <Option value="entity" key="entity">entity</Option>
                          </Select>
                        </FormItem>
                      </Col>
                    </Row>
                  </Col>
                </Row>
              </Col>
            </Row>
          </div>
          <Row style="margin:20px auto">
            <Col span="9" offset="8">
              <Button
                type="primary"
                v-if="currentPluginObj.status === 'DISABLED'"
                @click="pluginSave"
                >{{ $t('save') }}</Button
              >
              <Button
                type="primary"
                v-if="currentPluginObj.status === 'DISABLED'"
                @click="regist"
                >{{ $t('regist') }}</Button
              >
              <Button
                type="error"
                v-if="currentPluginObj.status === 'DISABLED'"
                @click="deleteRegisterSource"
                >{{ $t('delete') }}</Button
              >
              <Button
                type="error"
                v-if="currentPluginObj.status === 'ENABLED'"
                @click="removePlugin"
                >{{ $t('decommission') }}</Button
              >
            </Col>
          </Row>
        </Form>
      </Col>
    </Row>
    <Modal
      v-model="addRegistModal"
      :title="$t('create_regist_source')"
      @on-ok="ok"
      @on-cancel="cancel"
    >
      <Form ref="formValidate" :label-width="120">
        <FormItem :label="$t('regist_name')">
          <Input v-model="addRegisterName" placeholder=""></Input>
        </FormItem>
        <FormItem :label="$t('copy_source')">
          <Select v-model="selectedSource" @on-change="copyRegistSource">
            <Option v-for="item in sourceList" :value="item.id" :key="item.id"
              >{{ item.name }}({{ item.registerName }})</Option
            >
          </Select>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import PathExp from '../../components/path-exp.vue'
import {
  getAllPluginByPkgId,
  getAllDataModels,
  registerPlugin,
  deletePlugin,
  deleteRegisterSource,
  savePluginConfig,
  retrieveSystemVariables
} from '@/api/server'

export default {
  data () {
    return {
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
      hidePanal: true,
      allEntityType: [],
      selectedEntityType: '',
      targetPackage: '',
      form: {},
      allSystemVariables: []
      // pluginInterfaces:[]
    }
  },
  components: {
    PathExp
  },
  computed: {
    currentEntityAttr () {
      const allEntity = [].concat(
        ...this.allEntityType.map(_ => _.pluginPackageEntities)
      )
      const found = allEntity.find(i => i.name === this.selectedEntityType)
      return found ? found.attributes : []
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
  watch: {},
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
      this.currentPluginObj.entityName = this.selectedEntityType
      const entitys = [].concat(
        ...this.allEntityType.map(_ => _.pluginPackageEntities)
      )
      if (this.selectedEntityType) {
        const entityId = entitys.find(i => i.name === this.selectedEntityType)
          .id
        this.currentPluginObj.entityId = entityId
        this.currentPluginObj.targetEntity = this.selectedEntityType
        this.currentPluginObj.targetPackage = this.targetPackage
      } else {
        this.currentPluginObj.targetPackage = null
      }

      this.currentPluginObj.registerName = this.registerName
      let currentPluginForSave = JSON.parse(
        JSON.stringify(this.currentPluginObj)
      )
      if (this.hasNewSource) {
        delete currentPluginForSave.id
        currentPluginForSave.interfaces.map(_ => {
          delete _.id
        })
      }
      const { data, status, message } = await savePluginConfig(
        currentPluginForSave
      )
      const id = data.id
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        if (this.hasNewSource) {
          this.hasNewSource = false
          const { data, status } = await getAllPluginByPkgId(this.pkgId)
          if (status === 'OK') {
            // this.hidePanal = false;
            this.plugins = data
            this.currentPluginObj.id = id
            this.selectedSource = id
          }
          // return
        }
        this.getAllPluginByPkgId()
      }
    },
    mappingTypeChange (v, param) {
      if (v === 'entity') {
        param.mappingEntityExpression = null
      }
    },
    async regist () {
      const pluginConfigDtoList = this.plugins.find(
        plugin => plugin.pluginConfigName === this.currentPlugin
      ).pluginConfigDtoList
      const plugin = pluginConfigDtoList.find(
        dto => dto.id === this.currentPluginObj.id
      )
      const saveRes = await savePluginConfig(plugin)
      if (saveRes.status === 'OK') {
        const { status, message } = await registerPlugin(
          this.currentPluginObj.id
        )
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Success',
            desc: message
          })
          this.getAllPluginByPkgId()
          this.currentPluginObj.status = 'ENABLED'
        }
      }
    },
    async deleteRegisterSource () {
      const { status, message } = await deleteRegisterSource(
        this.currentPluginObj.id
      )
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
    cancel () {
      this.registerName = ''
      this.selectedEntityType = ''
      this.currentPluginObj = {}
      this.currentPluginData = {}
    },
    async removePlugin () {
      const { status, message } = await deletePlugin(this.currentPluginObj.id)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getAllPluginByPkgId()
        this.currentPluginObj.status = 'DISABLED'
      }
    },
    async getAllPluginByPkgId () {
      const { data, status } = await getAllPluginByPkgId(this.pkgId)
      if (status === 'OK') {
        this.plugins = data
        if (data.length === 1) {
          this.selectPlugin(data[0].name || '')
        }
      }
    },
    selectPlugin (val) {
      this.hasNewSource = false
      this.hidePanal = true
      this.currentPlugin = val
      let currentPluginData = this.plugins.find(
        plugin => plugin.pluginConfigName === val
      )
      this.sourceList = currentPluginData
        ? currentPluginData.pluginConfigDtoList
        : []
    },
    registSourceChange (v) {
      if (!v || v === 'add') {
        this.registerName = ''
        this.selectedEntityType = ''
        this.currentPluginObj = {}
        return
      }

      this.currentPluginObj = JSON.parse(
        JSON.stringify(this.sourceList.find(source => source.id === v))
      )
      this.selectedEntityType = this.currentPluginObj.entityName
      this.registerName = this.currentPluginObj.registerName
      this.selectedEntityType = this.currentPluginObj.targetEntity
      this.targetPackage = this.currentPluginObj.targetPackage
      this.hasNewSource = false
    },
    copyRegistSource (v) {
      this.registSourceChange(v)
      this.currentPluginObj.status = 'DISABLED'
    },
    onSelectEntityType (val) {
      this.targetPackage = val ? val.label.split('**')[1] : ''
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
    this.selectedEntityType = this.currentPluginObj.entityName
  }
}
</script>
<style lang="scss" scoped>
.interfaceContainer {
  margin-top: 20px;
  border-bottom: 1px solid #2c3e50;
}
#paramsContainer {
  overflow: auto;
  height: calc(100vh - 450px);
}
</style>
