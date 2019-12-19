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
              <span>(无)</span>
            </FormItem>
            <FormItem label="查询条件：">
              <span>(无)</span>
            </FormItem>
          </Form>
        </div>
        <div class="search-btn">
          <Button type="primary" @click="excuteSearch">执行查询</Button>
          <Button>清空条件</Button>
          <Button>重置查询</Button>
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
      :width="600"
      v-model="isShowSearchConditions"
      title="定义操作对象的查询方式"
    >
      <Form :label-width="110">
        <FormItem label="路径起点：">
          <Select v-model="selectedEntityType">
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
            v-model="mappingEntityExpression"
          ></PathExp>
        </FormItem>
        <FormItem label="目标类型：">
          <span>(无)</span>
        </FormItem>
        <FormItem label="业务主键：">
          <Select v-model="model1">
            <Option
              v-for="item in cityList"
              :value="item.value"
              :key="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>
        <FormItem label="查询条件：">
          <Transfer :data="[]" :target-keys="[]"> </Transfer>
        </FormItem>
      </Form>
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
import { getAllDataModels, retrieveSystemVariables } from "@/api/server.js";
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
      mappingEntityExpression: "",
      input: "",
      cityList: [
        {
          value: "New York",
          label: "New York"
        },
        {
          value: "London",
          label: "London"
        }
      ],
      model1: "",

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
  methods: {
    setSearchConditions() {
      this.getAllDataModels();
      this.isShowSearchConditions = true;
      if (document.querySelector(".wecube_attr-ul")) {
        document.querySelector(".wecube_attr-ul").style.width = "430px";
      }
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
        console.log(this.allEntityType);
      }
    },

    excuteSearch() {
      this.displaySearchZone = false;
      this.displayResultTableZone = true;
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

<style scoped lang="scss">
.ivu-form-item {
  margin-bottom: 0;
}
</style>
