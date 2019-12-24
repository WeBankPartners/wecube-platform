<template>
  <div class="">
    <section class="search">
      <Card v-if="displaySearchZone">
        <div class="search-zone">
          <Form :label-width="110">
            <FormItem label="查询操作对象：">
              <a @click="setSearchConditions">定义查询...</a>
            </FormItem>
            <FormItem label="查询路径：">
              <span v-if="dataModelExpression != ':'">{{
                dataModelExpression
              }}</span>
              <span v-else>(无)</span>
            </FormItem>
            <FormItem label="查询条件：">
              <div v-if="searchParameters.length">
                <Row>
                  <Col
                    span="6"
                    v-for="(sp, spIndex) in searchParameters"
                    :key="spIndex"
                    style="padding:0 8px"
                  >
                    <label for="">{{ sp.description }}:</label>
                    <Input v-model="sp.value" />
                  </Col>
                </Row>
              </div>
              <span v-else>(无)</span>
            </FormItem>
          </Form>
        </div>
        <div class="search-btn">
          <Button type="primary" @click="excuteSearch">执行查询</Button>
          <Button @click="clearParametes">清空条件</Button>
          <Button @click="resetParametes">重置查询</Button>
        </div>
      </Card>
      <a v-else @click="reExcute('displaySearchZone')"
        >查询 资源实例 中满足以下条件的CI数据对象</a
      >
    </section>
    <section
      v-if="!displaySearchZone"
      class="search-result-table"
      style="margin-top:60px;"
    >
      <WeTable
        v-if="displayResultTableZone"
        :tableData="tableData"
        :tableOuterActions="[]"
        :tableInnerActions="null"
        :tableColumns="tableColumns"
        :pagination="pagination"
        @handleSubmit="handleSubmit"
        @sortHandler="sortHandler"
        @getSelectedRows="onSelectedRowsChange"
        @pageChange="pageChange"
        @pageSizeChange="pageSizeChange"
        ref="table"
      />
      <a v-else @click="reExcute('displayResultTableZone')">找到20个资源实例</a>
      <Button type="primary" @click="excuteAction">执行</Button>
    </section>
    <section
      v-if="!displaySearchZone && !displayResultTableZone"
      class="excute-result"
      style="margin-top:60px;"
    >
      <Timeline>
        <TimelineItem>
          <p class="time">1976年</p>
          <p class="content">Apple I 问世</p>
        </TimelineItem>
        <TimelineItem>
          <p class="time">1984年</p>
          <p class="content">发布 Macintosh</p>
        </TimelineItem>
        <TimelineItem>
          <p class="time">2007年</p>
          <p class="content">发布 iPhone</p>
        </TimelineItem>
        <TimelineItem>
          <p class="time">2010年</p>
          <p class="content">发布 iPad</p>
        </TimelineItem>
        <TimelineItem>
          <p class="time">2011年10月5日</p>
          <p class="content">史蒂夫·乔布斯去世</p>
        </TimelineItem>
      </Timeline>
    </section>
    <Modal
      :width="904"
      v-model="isShowSearchConditions"
      title="定义操作对象的查询方式"
    >
      <Form :label-width="110">
        <FormItem label="路径起点：">
          <Select
            v-model="selectedEntityType"
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
        <FormItem label="查询路径：">
          <PathExp
            :rootEntity="selectedEntityType"
            :allDataModelsWithAttrs="allEntityType"
            v-model="dataModelExpression"
          ></PathExp>
        </FormItem>
        <FormItem label="目标类型：">
          <span>{{ currentPackageName }}:{{ currentEntityName }}</span>
        </FormItem>
        <FormItem label="业务主键：">
          <Select v-model="currentEntityAttr">
            <Option
              v-for="entityAttr in currentEntityAttrList"
              :value="entityAttr.id"
              :key="entityAttr.id"
              >{{ entityAttr.name }}</Option
            >
          </Select>
        </FormItem>
        <FormItem label="查询条件：" class="transfer-style">
          <Transfer
            :data="allEntityAttr"
            :target-keys="targetEntityAttr"
            :render-format="renderFormat"
            @on-change="handleChange"
          >
          </Transfer>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="saveSearchCondition">{{
          $t("save")
        }}</Button>
      </div>
    </Modal>
    <Modal v-model="DelConfig.isDisplay" width="360">
      <p slot="header" style="color:#f60;text-align:center">
        <Icon type="ios-information-circle"></Icon>
        <span>删除确认</span>
      </p>
      <div style="text-align:center">
        <p>
          <span style="color:#2d8cf0"> 后续数据 </span>
          即将被删除
        </p>
      </div>
      <div slot="footer">
        <Button type="error" size="large" long @click="del">删除</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import PathExp from "@/pages/components/path-exp.vue";
import { innerActions } from "@/const/actions.js";
import {
  getAllDataModels,
  retrieveSystemVariables,
  dmeAllEntities
} from "@/api/server.js";
import { formatData } from "../util/format.js";

export default {
  name: "",
  data() {
    return {
      displaySearchZone: true,
      displayResultTableZone: false,
      displayExcuteResultZone: false,

      DelConfig: {
        isDisplay: false,
        displayConfig: {
          name: ""
        },
        key: null
      },

      isShowSearchConditions: false,
      selectedEntityName: "",
      selectedEntityType: "",
      allEntityType: [],

      dataModelExpression: "wecmdb:subsys",
      currentEntityName: "",
      currentPackageName: "",
      currentEntityAttr: "",
      currentEntityAttrList: [],
      allEntityAttr: [],
      targetEntityAttr: [],

      searchParameters: [
        {
          id: "wecmdbsubsys0",
          packageName: "wecmdb",
          entityName: "subsys",
          description: "业务区域",
          name: "business_zone",
          dataType: "ref",
          index: "0",
          ai: "0",
          value: null
        },
        {
          id: "wecmdbsubsys0",
          packageName: "wecmdb",
          entityName: "subsys",
          description: "编码",
          name: "code",
          dataType: "str",
          index: "0",
          ai: "1",
          value: null
        }
      ],

      payload: {
        filters: [],
        pageable: {
          pageSize: 10,
          startIndex: 0
        },
        paging: true
      },
      pagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      tableData: [],
      innerActions,
      tableColumns: [
        {
          title: this.$t("table_id"),
          key: "id",
          displaySeqNo: 1
        },
        {
          title: this.$t("table_name"),
          key: "name",
          inputKey: "name",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          inputType: "text",
          placeholder: this.$t("table_name")
        }
      ]
    };
  },
  mounted() {
    this.queryData();
  },
  watch: {
    dataModelExpression: async function(val) {
      if (val === ":") {
        return;
      }
      const params = {
        dataModelExpression: val
      };
      const { data, status, message } = await dmeAllEntities(params);
      if (status === "OK") {
        this.currentEntityName = data.slice(-1)[0].entityName;
        this.currentPackageName = data.slice(-1)[0].packageName;
        this.currentEntityAttrList = data.slice(-1)[0].attributes;

        this.allEntityAttr = data
          .map((single, index) => {
            return single.attributes.map((attr, ai) => {
              attr.key = `${single.packageName}&${single.entityName}&${attr.description}&${attr.name}&${attr.dataType}&${index}&${ai}`;
              attr.label = attr.name;
              attr.entityName = single.entityName;
              attr.packageName = single.packageName;
              return attr;
            });
          })
          .flat(2);
      }
    }
  },
  methods: {
    setSearchConditions() {
      this.getAllDataModels();
      this.isShowSearchConditions = true;
      if (document.querySelector(".wecube_attr-ul")) {
        document.querySelector(".wecube_attr-ul").style.width = "730px";
      }

      this.selectedEntityType = "";
      this.allEntityType = [];
      this.dataModelExpression = ":";
      this.currentEntityAttr = "";
      this.currentEntityAttrList = [];
      this.currentPackageName = "";
      this.currentEntityName = "";
      this.allEntityAttr = [];
      this.targetEntityAttr = [];
    },
    changeEntityType() {
      this.targetEntityAttr = [];
    },
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
    },
    handleChange(newTargetKeys) {
      this.targetEntityAttr = newTargetKeys;
    },
    renderFormat(item) {
      return `(${item.packageName}:${item.entityName})-${item.label}`;
    },
    saveSearchCondition() {
      if (!this.currentEntityAttr) {
        this.$Message.warning("业务主键不能为空！");
        return;
      }
      if (this.targetEntityAttr == false) {
        this.$Message.warning("查询条件不能为空！");
        return;
      }
      this.isShowSearchConditions = false;
      this.searchParameters = this.targetEntityAttr.map(teAttr => {
        const [
          packageName,
          entityName,
          description,
          name,
          dataType,
          index,
          ai,
          value
        ] = teAttr.split("&");
        return {
          id: packageName + entityName + index,
          packageName,
          entityName,
          description,
          name,
          dataType,
          index,
          ai,
          value: null
        };
      });
    },

    excuteSearch() {
      const requestParameter = {
        dataModelExpression: this.dataModelExpression,
        filters: []
      };
      let keySet = [];
      this.searchParameters.forEach(sParameter => {
        const index = keySet.indexOf(sParameter.id);
        if (index > -1) {
          const { name, value } = sParameter;
          requestParameter.filters[index].attributeFilters.push({
            name,
            value,
            operator: "eq"
          });
        } else {
          keySet.push(sParameter.id);
          const { index, packageName, entityName, name, value } = sParameter;
          requestParameter.filters.push({
            index,
            packageName,
            entityName,
            attributeFilters: [
              {
                name,
                value,
                operator: "eq"
              }
            ]
          });
        }
      });
      console.log(requestParameter);
      // this.displaySearchZone = false;
      // this.displayResultTableZone = true;
    },
    clearParametes() {
      this.searchParameters.forEach(item => {
        item.value = "";
      });
    },
    resetParametes() {
      this.dataModelExpression = ":";
      this.searchParameters = [];
    },
    reExcute(key) {
      this.DelConfig.isDisplay = true;
      this.DelConfig.key = key;
    },
    excuteAction() {
      this.displayResultTableZone = false;
      this.displayExcuteResultZone = false;
    },
    del() {
      this.DelConfig.isDisplay = false;

      this.displaySearchZone = false;
      this.displayResultTableZone = false;
      this.displayExcuteResultZone = false;
      this[this.DelConfig.key] = true;
    },

    async queryData() {
      this.payload.pageable.pageSize = this.pagination.pageSize;
      this.payload.pageable.startIndex =
        (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const { status, message, data } = await retrieveSystemVariables(
        this.payload
      );
      if (status === "OK") {
        this.tableData = data.contents;
        this.pagination.total = data.pageInfo.totalRows;
      }
    },
    handleSubmit(data) {
      this.payload.filters = data;
      this.queryData();
    },
    sortHandler(data) {
      if (data.order === "normal") {
        delete this.payload.sorting;
      } else {
        this.payload.sorting = {
          asc: data.order === "asc",
          field: data.key
        };
      }
      this.queryData();
    },
    pageChange(current) {
      this.pagination.currentPage = current;
      this.queryData();
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size;
      this.queryData();
    },
    actionFun(type, data) {
      switch (type) {
        case "cancel":
          this.cancelHandler();
          break;
        default:
          break;
      }
    },
    onSelectedRowsChange(rows, checkoutBoxdisable) {
      if (rows.length > 0) {
      } else {
      }
      this.seletedRows = rows;
    }
  },
  components: {
    PathExp
  }
};
</script>

<style lang="scss" scope>
.transfer-style /deep/ .ivu-transfer-list {
  width: 350px;
}
.ivu-form-item {
  margin-bottom: 8px !important;
}

.search-btn {
  margin-top: 16px;
}
</style>
