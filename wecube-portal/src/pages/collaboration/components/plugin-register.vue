<template>
  <Row>
    <Col span="3">
      <Menu
        theme="light"
        :active-name="currentPlugin"
        @on-select="selectPlugin"
      >
        <MenuItem v-for="plugin in plugins" :name="plugin.name">
          <Icon type="md-flower" />
          {{ plugin.name }}
        </MenuItem>
      </Menu>
    </Col>
    <Col span="21">
      <Form :model="form">
        <Row>
          <Col span="6" offset="1">
            <FormItem :label-width="100" label="CI Type">
              <Select
                @on-change="selectCiType"
                label-in-value
                v-model="selectedCiType"
              >
                <OptionGroup
                  v-for="ci in ciTypes"
                  :label="ci.value || '-'"
                  :key="ci.code"
                >
                  <Option
                    v-for="item in ci.ciTypes"
                    :value="item.ciTypeId || ''"
                    :key="item.ciTypeId"
                    >{{ item.name }}</Option
                  >
                </OptionGroup>
              </Select>
            </FormItem>
          </Col>
        </Row>
        <hr />
        <Row style="margin-bottom:10px;margin-top:10px">
          <Col span="3">
            <span>操作</span>
          </Col>
          <Col span="2">
            <span>参数类型</span>
          </Col>
          <Col span="3">
            <span>参数名</span>
          </Col>
          <Col span="5" offset="1">
            <span>属性</span>
          </Col>
        </Row>
        <Row
          style="margin-top:20px; border-bottom: 1px solid #2c3e50"
          v-for="(interfaces, index) in pluginInterfaces"
          :key="interfaces.id"
        >
          <Col span="3">
            <Tooltip :content="interfaces.action">
              <span
                style="display: inline-block;max-width: 95%;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
              >
                {{ interfaces.action }}
              </span>
            </Tooltip>
          </Col>
          <Col span="21">
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>输入参数</span>
                </FormItem>
              </Col>
              <Col span="17" offset="1">
                <Row
                  v-for="param in interfaces['input-parameters']"
                  :key="param.id"
                >
                  <Col span="5">
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
                  <Col span="18">
                    <FormItem :label-width="0">
                      <!-- <CmdbAttrInput
                          :allCodes="allCodes"
                          :allCiTypes="ciTypes"
                          :paramData="param"
                          :rootCiType="selectedCiType"
                          v-model="param.cmdbAttr"
                          :ciTypesObj="ciTypesObj"
                          :ciTypeAttributeObj="ciTypeAttributeObj"
                        /> -->
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
            <Row>
              <Col span="2">
                <FormItem :label-width="0">
                  <span>输出参数</span>
                </FormItem>
              </Col>
              <Col span="17" offset="1">
                <Row
                  v-for="outPut in interfaces['output-parameters']"
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
                      <Select
                        style="width:150px"
                        placeholder="请选择"
                        v-model="outPut.mappingSystemVariableId"
                        clearable
                      >
                        <Option
                          v-for="attr in currentCiTyPeAttr"
                          :key="attr.propertyName"
                          :value="attr.propertyName"
                          :label="attr.name"
                        ></Option>
                      </Select>
                    </FormItem>
                  </Col>
                </Row>
              </Col>
            </Row>
          </Col>
        </Row>
        <!-- <Row style="margin:20px auto">
            <Col span="5" offset="10">
              <Button
                type="primary"
                v-if="
                  currentPlugin.status === 'NOT_CONFIGURED' ||
                    currentPlugin.status === 'CONFIGURED' ||
                    currentPlugin.status === 'DECOMMISSIONED'
                "
                @click="pluginSave"
                >保存</Button
              >
              <Button
                type="primary"
                v-if="
                  currentPlugin.status === 'CONFIGURED' ||
                    currentPlugin.status === 'DECOMMISSIONED'
                "
                @click="regist"
                >注册</Button
              >
              <Button
                type="error"
                v-if="currentPlugin.status === 'ONLINE'"
                @click="removePlugin"
                >注销</Button
              >
            </Col>
          </Row> -->
      </Form>
    </Col>
  </Row>
</template>
<script>
import { getAllPluginByPkgId } from "@/api/server";
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
    pluginInterfaces() {
      const found = this.plugins.find(
        plugin => plugin.name === this.currentPlugin
      );
      console.log(found);
      return found ? found.interface : [];
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  watch: {},
  methods: {
    async getAllPluginByPkgId() {
      const { data, status, message } = await getAllPluginByPkgId(3);
      if (status === "OK") {
        this.plugins = data;
      }
    },
    selectPlugin(val) {
      this.currentPlugin = val;
    },
    selectCiType(val) {}
  },
  created() {
    this.getAllPluginByPkgId();
  }
};
</script>
<style lang="scss"></style>
