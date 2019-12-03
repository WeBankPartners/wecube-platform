<template>
  <Row>
    <Col span="5">
      <div v-if="plugins.length < 1">{{ $t("no_plugin") }}</div>
      <Menu
        theme="light"
        :active-name="currentPlugin"
        @on-select="selectPlugin"
        style="width: 100%;z-index:10"
      >
        <MenuItem
          v-for="(plugin, index) in plugins"
          :name="plugin.name"
          :key="index"
          style="padding: 10px 5px;"
        >
          <Icon type="md-flower" />
          {{ plugin.name }}
        </MenuItem>
      </Menu>
    </Col>
    <Col span="19" offset="0" style="padding-left: 10px">
      <Form v-if="currentPlugin.length > 0" :model="form">
        <Row>
          <Col span="10" offset="0">
            <FormItem :label-width="100" :label="$t('target_type')">
              <Select
                @on-change="onSelectEntityType"
                v-model="selectedEntityType"
                :disabled="currentPluginObj.status === 'ENABLED'"
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
          </Col>
        </Row>
        <hr />
        <Row style="margin-bottom:10px;margin-top:10px">
          <Col span="3">
            <strong style="font-size:15px;">{{ $t("operation") }}</strong>
          </Col>
          <Col span="3">
            <strong style="font-size:15px;">{{ $t("params_type") }}</strong>
          </Col>
          <Col span="3" offset="0">
            <strong style="font-size:15px;">{{ $t("params_name") }}</strong>
          </Col>
          <Col span="6" offset="1">
            <strong style="font-size:15px;">{{ $t("attribute") }}</strong>
          </Col>
          <Col span="3" offset="4">
            <strong style="font-size:15px;">{{ $t("attribute_type") }}</strong>
          </Col>
        </Row>
        <Row
          style="margin-top:20px; border-bottom: 1px solid #2c3e50"
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
                  <span>{{ $t("input_params") }}</span>
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
                          style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 95%;"
                          >{{ param.name }}</span
                        >
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="13" offset="0">
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
                        v-model="param.mappingSystemVariableId"
                        :disabled="currentPluginObj.status === 'ENABLED'"
                      >
                        <Option
                          v-for="item in allSystemVariables"
                          :value="item.id"
                          :key="item.id"
                          >{{ item.name }}</Option
                        >
                      </Select>
                      <span v-if="param.mappingType === 'context'">N/A</span>
                    </FormItem>
                  </Col>
                  <Col span="4" offset="1">
                    <FormItem :label-width="0">
                      <Select
                        :disabled="currentPluginObj.status === 'ENABLED'"
                        v-model="param.mappingType"
                        @on-change="mappingTypeChange($event, param)"
                      >
                        <Option value="context" key="context">context</Option>
                        <Option value="system_variable" key="system_variable"
                          >system_variable</Option
                        >
                        <Option value="entity" key="entity">entity</Option>
                      </Select>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
            <Row>
              <Col span="3">
                <FormItem :label-width="0">
                  <span>{{ $t("output_params") }}</span>
                </FormItem>
              </Col>
              <Col span="20" offset="0">
                <Row
                  v-for="outPut in interfaces['outputParameters']"
                  :key="outPut.id + 1000"
                >
                  <Col span="4">
                    <FormItem :label-width="0">
                      <Tooltip :content="outPut.name">
                        <span
                          style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                          >{{ outPut.name }}</span
                        >
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="14" offset="1">
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
                      <span v-if="outPut.mappingType === 'context'">N/A</span>
                    </FormItem>
                  </Col>
                  <Col span="4" offset="1">
                    <FormItem :label-width="0">
                      <Select
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
          </Col>
        </Row>
        <Row style="margin:20px auto">
          <Col span="5" offset="10">
            <Button
              type="primary"
              v-if="currentPluginObj.status === 'DISABLED'"
              @click="pluginSave"
              >{{ $t("save") }}</Button
            >
            <Button
              type="primary"
              v-if="currentPluginObj.status === 'DISABLED'"
              @click="regist"
              >{{ $t("regist") }}</Button
            >
            <Button
              type="error"
              v-if="currentPluginObj.status === 'ENABLED'"
              @click="removePlugin"
              >{{ $t("decommission") }}</Button
            >
          </Col>
        </Row>
      </Form>
    </Col>
  </Row>
</template>
<script>
import PathExp from "../../components/path-exp.vue";
import {
  getAllPluginByPkgId,
  getAllDataModels,
  registerPlugin,
  deletePlugin,
  savePluginConfig,
  retrieveSystemVariables
} from "@/api/server";

export default {
  data() {
    return {
      allDataModelsWithAttrs: {},
      currentPlugin: "",
      plugins: [],
      allEntityType: [],
      selectedEntityType: "",
      form: {},
      allSystemVariables: []
      // pluginInterfaces:[]
    };
  },
  components: {
    PathExp
  },
  computed: {
    currentPluginObj() {
      const found = this.plugins.find(
        plugin => plugin.name === this.currentPlugin
      );
      return found ? found : {};
    },
    currentEntityAttr() {
      const allEntity = [].concat(
        ...this.allEntityType.map(_ => _.pluginPackageEntities)
      );
      const found = allEntity.find(i => i.name === this.selectedEntityType);
      return found ? found.attributes : [];
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
    async retrieveSystemVariables() {
      const { data, status, message } = await retrieveSystemVariables({
        filters: [],
        paging: false
      });
      if (status === "OK") {
        this.allSystemVariables = data.contents;
      }
    },
    async pluginSave() {
      this.currentPluginObj.entityName = this.selectedEntityType;
      const entitys = [].concat(
        ...this.allEntityType.map(_ => _.pluginPackageEntities)
      );
      const entityId = entitys.find(i => i.name === this.selectedEntityType).id;
      this.currentPluginObj.entityId = entityId;
      const { data, status, message } = await savePluginConfig(
        this.currentPluginObj
      );
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: message
        });
        this.getAllPluginByPkgId();
      }
    },
    mappingTypeChange(v, param) {
      if (v === "entity") {
        param.mappingEntityExpression = null;
      }
    },
    async regist() {
      const saveRes = await savePluginConfig(this.currentPluginObj);
      if (saveRes.status === "OK") {
        const { data, status, message } = await registerPlugin(
          this.currentPluginObj.id
        );
        if (status === "OK") {
          this.$Notice.success({
            title: "Success",
            desc: message
          });
          this.getAllPluginByPkgId();
        }
      }
    },
    async removePlugin() {
      const { data, status, message } = await deletePlugin(
        this.currentPluginObj.id
      );
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: message
        });
        this.getAllPluginByPkgId();
      }
    },
    async getAllPluginByPkgId() {
      const { data, status, message } = await getAllPluginByPkgId(this.pkgId);
      if (status === "OK") {
        this.plugins = data;
        if (data.length === 1) {
          this.selectPlugin(data[0].name || "");
        }
      }
    },
    selectPlugin(val) {
      this.currentPlugin = val;
      this.selectedEntityType = this.plugins.find(
        plugin => plugin.name === val
      ).entityId;
      this.selectedEntityType = this.currentPluginObj.entityName;
    },
    onSelectEntityType(val) {},
    async getAllDataModels() {
      const { data, status, message } = await getAllDataModels();
      if (status === "OK") {
        this.allEntityType = data.map(_ => {
          // handle result sort by name
          return {
            ..._,
            pluginPackageEntities: _.pluginPackageEntities.sort(function(a, b) {
              var s = a.name.toLowerCase();
              var t = b.name.toLowerCase();
              if (s < t) return -1;
              if (s > t) return 1;
            })
          };
        });
      }
    }
  },
  created() {
    this.getAllPluginByPkgId();
    this.getAllDataModels();
    this.retrieveSystemVariables();
    this.selectedEntityType = this.currentPluginObj.entityName;
  }
};
</script>
<style lang="scss"></style>
