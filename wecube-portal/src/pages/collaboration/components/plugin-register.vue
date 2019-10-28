<template>
  <Row>
    <Col span="3">
      <div v-if="plugins.length < 1">暂无插件</div>
      <Menu
        theme="light"
        :active-name="currentPlugin"
        @on-select="selectPlugin"
      >
        <MenuItem
          v-for="(plugin, index) in plugins"
          :name="plugin.name"
          :key="index"
        >
          <Icon type="md-flower" />
          {{ plugin.name }}
        </MenuItem>
      </Menu>
    </Col>
    <Col span="18" offset="3">
      <Form v-if="currentPlugin.length > 0" :model="form">
        <Row>
          <Col span="10" offset="0">
            <FormItem :label-width="100" label="目标对象类型:">
              <Select
                @on-change="selectCiType"
                v-model="selectedCiType"
                disabled
              >
                <Option
                  v-for="(ci, index) in ciTypes"
                  :value="ci.id || ''"
                  :key="index"
                  >{{ ci.displayName }}</Option
                >
              </Select>
            </FormItem>
          </Col>
        </Row>
        <hr />
        <Row style="margin-bottom:10px;margin-top:10px">
          <Col span="3">
            <strong style="font-size:15px;">操作</strong>
          </Col>
          <Col span="3">
            <strong style="font-size:15px;">参数类型</strong>
          </Col>
          <Col span="4" offset="1">
            <strong style="font-size:15px;">参数名</strong>
          </Col>
          <Col span="5" offset="1">
            <strong style="font-size:15px;">属性</strong>
          </Col>
        </Row>
        <Row
          style="margin-top:20px; border-bottom: 1px solid #2c3e50"
          v-for="(interfaces, index) in currentPluginObj.interfaces"
          :key="index"
        >
          <Col span="3">
            <FormItem :label-width="0">
              <span>{{ interfaces.action }}</span>
            </FormItem>
          </Col>
          <Col span="21">
            <Row>
              <Col span="3">
                <FormItem :label-width="0">
                  <span>输入参数</span>
                </FormItem>
              </Col>
              <Col span="16" offset="1">
                <Row
                  v-for="param in interfaces['inputParameters']"
                  :key="param.id"
                >
                  <Col span="6">
                    <FormItem :label-width="0">
                      <Tooltip :content="param.name">
                        <span
                          style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                        >
                          {{ param.name }}
                        </span>
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="15" offset="3">
                    <FormItem :label-width="0">
                      <span v-if="param.mappingType === 'system_variable'">{{
                        param.mappingSystemVariableId || "N/A"
                      }}</span>
                      <span v-if="param.mappingType === 'entity'">{{
                        param.mappingEntityExpression || "N/A"
                      }}</span>
                      <span v-if="param.mappingType === 'context'">N/A</span>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
            <Row>
              <Col span="3">
                <FormItem :label-width="0">
                  <span>输出参数</span>
                </FormItem>
              </Col>
              <Col span="17" offset="1">
                <Row
                  v-for="outPut in interfaces['outputParameters']"
                  :key="outPut.id + 1000"
                >
                  <Col span="5">
                    <FormItem :label-width="0">
                      <Tooltip :content="outPut.name">
                        <span
                          style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                        >
                          {{ outPut.name }}
                        </span>
                      </Tooltip>
                    </FormItem>
                  </Col>
                  <Col span="18">
                    <FormItem :label-width="0">
                      <!-- <Select
                        placeholder="请选择"
                        v-model="outPut.mappingSystemVariableId"
                        clearable
                      >
                        <Option
                          v-for="attr in currentCiTyPeAttr"
                          :key="attr.codeId"
                          :value="attr.codeId"
                          :label="attr.value"
                        ></Option>
                      </Select> -->
                      <span v-if="outPut.mappingType === 'system_variable'">{{
                        outPut.mappingSystemVariableId || "N/A"
                      }}</span>
                      <span v-if="outPut.mappingType === 'entity'">{{
                        outPut.mappingEntityExpression || "N/A"
                      }}</span>
                      <span v-if="outPut.mappingType === 'context'">N/A</span>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
          </Col>
        </Row>
        <Row style="margin:20px auto">
          <Col span="5" offset="10">
            <!-- <Button
                type="primary"
                v-if="
                  currentPlugin.status === 'NOT_CONFIGURED' ||
                    currentPlugin.status === 'CONFIGURED' ||
                    currentPlugin.status === 'DECOMMISSIONED'
                "
                @click="pluginSave"
                >保存</Button
              > -->
            <Button
              type="primary"
              v-if="
                currentPluginObj.status === 'NOT_CONFIGURED' ||
                  currentPluginObj.status === 'CONFIGURED' ||
                  currentPluginObj.status === 'DECOMMISSIONED'
              "
              @click="regist"
              >注册</Button
            >
            <Button
              type="error"
              v-if="currentPluginObj.status === 'ONLINE'"
              @click="removePlugin"
              >注销</Button
            >
          </Col>
        </Row>
      </Form>
    </Col>
  </Row>
</template>
<script>
import {
  getAllPluginByPkgId,
  getAllSystemEnumCodes,
  getAllDataModels,
  registerPlugin,
  deletePlugin,
  savePluginConfig
} from "@/api/server";
export default {
  data() {
    return {
      currentPlugin: "",
      plugins: [],
      ciTypes: [],
      currentCiTyPeAttr: [],
      selectedCiType: "",
      form: {}
      // pluginInterfaces:[]
    };
  },
  computed: {
    currentPluginObj() {
      const found = this.plugins.find(
        plugin => plugin.name === this.currentPlugin
      );
      return found ? found : {};
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  watch: {},
  methods: {
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
      }
    },
    selectPlugin(val) {
      this.currentPlugin = val;
      this.selectedCiType = this.plugins.find(
        plugin => plugin.name === val
      ).entityId;
    },
    selectCiType(val) {},
    async getAllDataModels() {
      const { data, status, message } = await getAllDataModels();
      if (status === "OK") {
        this.ciTypes = data;
      }
    },
    async getAllSystemEnumCodes() {
      const { data, status, message } = await getAllSystemEnumCodes({
        filters: [],
        paging: false
      });
      if (status === "OK") {
        this.currentCiTyPeAttr = data.contents
          .filter(i => i.cat.catName != "tab_query_of_deploy_design")
          .filter(i => i.cat.catName != "tab_query_of_architecture_design");
      }
    }
  },
  created() {
    this.getAllPluginByPkgId();
    this.getAllDataModels();
    this.getAllSystemEnumCodes();
  }
};
</script>
<style lang="scss"></style>
