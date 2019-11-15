<template>
  <div class="attr_input">
    <Poptip v-model="optionsHide" placement="bottom">
      <div ref="wecube_cmdb_attr" class="input_in">
        <textarea
          ref="textarea"
          :rows="1"
          @input="inputHandler"
          :value="inputVal"
        ></textarea>
        <!-- <span v-if="!isEndWithCIType" class="wecube-error-message">{{
          $t("select_non_ci_attr")
        }}</span> -->
      </div>
      <div slot="content">
        <div class="attr-ul"></div>
      </div>
    </Poptip>
    <!-- <span v-else>{{ displayValue }}</span> -->
  </div>
</template>
<script>
export default {
  name: "PathExp",
  data() {
    return {
      optionsHide: false,
      currentEntity: "", //节点变化时更新
      currentPkg: "", //节点变化时更新
      inputVal: ""
    };
  },
  props: {
    rootPkg: {},
    rootEntity: {},
    allDataModelsWithAttrs: {} //组件外层调用getDataModelByPackageName传入
  },
  mounted() {
    this.inputVal = `${this.rootPkg}:${this.rootEntity}`;
    console.log(this.inputVal);
  },
  methods: {
    inputHandler(v) {}
  }
};
</script>
<style lang="scss"></style>
