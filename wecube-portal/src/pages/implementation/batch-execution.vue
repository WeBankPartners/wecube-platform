<template>
  <div class="">
    <section>
      <Card>
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
          <Button type="primary">执行查询</Button>
          <Button>清空条件</Button>
          <Button>重置查询</Button>
        </div>
      </Card>
    </section>
    {{ model2 }}
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
  </div>
</template>

<script>
import PathExp from "@/pages/components/path-exp.vue";
import { getAllDataModels } from "@/api/server";

export default {
  name: "",
  data() {
    return {
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
      model2: ""
    };
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
