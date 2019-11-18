<template>
  <div class="wecube_attr_input">
    <Poptip v-model="optionsHide" placement="bottom">
      <div ref="wecube_cmdb_attr" class="wecube_input_in">
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
        <div class="wecube_attr-ul"></div>
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
  watch: {
    currentPkg: {
      handler(val) {
        this.$emit("getPluginPkgDataModel", val);
        console.log(this.allDataModelsWithAttrs);
      }
    }
  },
  mounted() {
    this.inputVal = `${this.rootPkg}:${this.rootEntity}`;
    this.currentEntity = this.rootEntity;
    this.currentPkg = this.rootPkg;
    if (document.querySelector(".wecube_attr-ul")) {
      document.querySelector(".wecube_attr-ul").style.width =
        document.querySelector(".wecube_input_in textarea").clientWidth + "px";
    }
  },
  methods: {
    inputHandler(v) {}
  }
};
</script>
<style lang="scss">
* {
  padding: 0;
  margin: 0;
  list-style: none;
  font-size: 14px;
}
.wecube_attr-ul {
  width: 100%;
  z-index: 3000;
  background: white;
  max-height: 200px;
  overflow: auto;
}
.wecube_attr_input .ivu-poptip {
  width: 100%;
}
.wecube_attr_input .ivu-poptip .ivu-poptip-rel {
  width: 100%;
}
.wecube_input_in {
  width: 100%;
  display: flex;
  flex-direction: column;

  textarea {
    font-size: 11px;
    line-height: 28px;
    width: 100%;
    border-radius: 5px;
  }
  .wecube-error-message {
    display: none;
  }

  &.wecube-error {
    textarea {
      border: 1px solid #f00;
    }
    .wecube-error-message {
      display: block;
      height: 20px;
      line-height: 16px;
      color: #f00;
      padding: 2px 0;
      font-size: 12px;
    }
  }
}
.wecube_attr-ul ul {
  width: 100%;
  border-radius: 3px;
}
.ul-li-selected {
  color: rgb(6, 130, 231);
}
.wecube_attr-ul ul li {
  width: 100%;
  height: 25px;
  line-height: 25px;
  cursor: pointer;
  &:hover {
    background-color: rgb(227, 231, 235);
  }
}
</style>
