<template>
  <div class="wecube_attr_input">
    <Poptip v-model="optionsHide" placement="bottom">
      <div ref="wecube_cmdb_attr" class="wecube_input_in">
        <textarea
          ref="textarea"
          :rows="3"
          @input="inputHandler"
          :value="inputVal"
          :disabled="disabled"
        ></textarea>
        <!-- <span v-if="!isEndWithCIType" class="wecube-error-message">{{
          $t("select_non_ci_attr")
        }}</span>-->
      </div>
      <div slot="content">
        <div class="wecube_attr-ul">
          <ul v-for="opt in options" :key="opt.id">
            <li class="" @click="optClickHandler(opt)">
              {{
                opt.dataType === "ref"
                  ? isRefBy
                    ? `(${opt.name})${opt.packageName}:${opt.entityName}`
                    : `${opt.name}>${opt.refPackageName}:${opt.refEntityName}`
                  : opt.name
              }}
            </li>
          </ul>
        </div>
      </div>
    </Poptip>
    <!-- <span v-else>{{ displayValue }}</span> -->
  </div>
</template>
<script>
import { getRefByIdInfoByPackageNameAndEntityName } from "@/api/server";
export default {
  name: "PathExp",
  data() {
    return {
      optionsHide: false,
      currentEntity: "", //节点变化时更新
      currentPkg: "", //节点变化时更新
      inputVal: "",
      options: [],
      currentOperator: "",
      isRefBy: false,
      entityPath: [],
      isLastNode: false
    };
  },
  props: {
    value: {
      required: false
    },
    rootPkg: {},
    disabled: {},
    rootEntity: {},
    allDataModelsWithAttrs: {} //组件外层调用getDataModelByPackageName传入
  },
  watch: {
    currentPkg: {
      handler(val) {
        // this.$emit("getPluginPkgDataModel", 'service-mt');
        // console.log(this.allEntity);
      }
    },
    value: {
      handler(val) {},
      immediate: true
    },
    allDataModelsWithAttrs: {
      handler(val) {}
    },
    rootEntity: {
      handler(val) {
        const found = this.allEntity.find(_ => _.name === val);
        this.currentPkg = found.packageName;
        this.currentEntity = val;
        this.inputVal = `${this.currentPkg}:${val}`;
        this.entityPath = [
          {
            entity: this.currentEntity,
            pkg: this.currentPkg
          }
        ];
        this.options = [];
        this.isLastNode = false;
        this.$emit("input", this.inputVal.replace(/\s/g, ""));
      }
    }
  },
  computed: {
    allEntity() {
      let entity = [];
      this.allDataModelsWithAttrs.forEach(_ => {
        if (_.pluginPackageEntities) {
          entity = entity.concat(_.pluginPackageEntities);
        }
      });
      return entity;
    }
  },
  mounted() {
    this.restorePathExp();
    this.currentEntity = this.rootEntity;
    this.currentPkg = this.rootPkg;

    this.$emit("input", this.inputVal.replace(/\s/g, ""));
    if (document.querySelector(".wecube_attr-ul")) {
      document.querySelector(".wecube_attr-ul").style.width =
        document.querySelector(".wecube_input_in textarea").clientWidth + "px";
    }
  },
  methods: {
    restorePathExp() {
      if (this.value) {
        this.inputVal = this.value
          .replace(/\~/g, " ~")
          .replace(/\>/g, " >")
          .replace(/\./g, " .");
        const pathList = this.value.split(/[~.>]/);
        let path = {};
        pathList.forEach(_ => {
          const ifEntity = _.indexOf(":");
          if (ifEntity > 0) {
            const isBy = _.indexOf(")");
            const current = _.split(":");
            if (isBy > 0) {
              path = {
                entity: current[1],
                pkg: current[0].split(")")[1]
              };
            } else {
              path = {
                entity: current[1],
                pkg: current[0]
              };
            }
          }
          this.entityPath.push(path);
        });
      } else {
        this.inputVal = `${this.rootPkg}:${this.rootEntity || ""}`;
        this.entityPath.push({
          entity: this.currentEntity,
          pkg: this.currentPkg
        });
      }
    },
    optClickHandler(item) {
      this.optionsHide = false;
      this.isLastNode = !(item.dataType === "ref");
      const newValue =
        item.dataType === "ref"
          ? this.isRefBy
            ? `(${item.name})${item.packageName}:${item.entityName}`
            : `${item.name}>${item.refPackageName}:${item.refEntityName}`
          : item.name;
      this.currentPkg = this.isRefBy ? item.packageName : item.refPackageName;
      this.currentEntity = this.isRefBy ? item.entityName : item.refEntityName;
      this.entityPath.push({
        entity: this.currentEntity,
        pkg: this.currentPkg
      });
      this.inputVal = this.inputVal + " " + this.currentOperator + newValue;
      this.options = [];
      this.$refs["textarea"].focus();
      this.$emit("input", this.inputVal.replace(/\s/g, ""));
    },
    inputHandler(v) {
      if (!v.data) {
        // 删除的逻辑
        this.isRefBy = false;
        let valList = this.inputVal.split(" ");
        if (valList.length > 1) {
          valList.splice(-1, 1);
          this.inputVal = valList.join(" ");
          this.entityPath.splice(-1, 1);
          this.$emit("input", this.inputVal.replace(/\s/g, ""));
        } else if (valList.length < 2) {
          this.inputVal = valList[0];
          this.$emit("input", this.inputVal.replace(/\s/g, ""));
        }
        this.$refs.textarea.value = this.inputVal;
        this.isLastNode = false;
        return;
      } else {
        if (!(v.data === "." || v.data === "~")) {
          this.$Message.error({
            content: this.$t("input_correct_operator")
          });
          this.$refs.textarea.value = this.inputVal;
          return;
        }
        if (this.isLastNode) {
          this.optionsHide = false;
          this.$Message.warning({
            content: this.$t("is_model_attribute")
          });
          this.$refs.textarea.value = this.inputVal;
          return;
        }
        if (v.data === ".") {
          this.currentOperator = v.data;
          this.isRefBy = false;
          this.optionsHide = true;
          this.getAttrByEntity();
        }
        if (v.data === "~") {
          this.currentOperator = v.data;
          this.isRefBy = true;
          this.optionsHide = true;
          this.options = [];
          this.getRefByEntity();
        }
      }
    },
    getAttrByEntity() {
      //获取当前选中entity的属性作为下拉选项
      this.options = [];
      this.allEntity.forEach(e => {
        if (e.name == this.entityPath[this.entityPath.length - 1].entity) {
          this.options = e.attributes;
        }
      });
    },
    async getRefByEntity() {
      //获取当前entity被哪些属性引用作为下拉选项
      const current = this.entityPath[this.entityPath.length - 1];
      const {
        status,
        message,
        data
      } = await getRefByIdInfoByPackageNameAndEntityName(
        current.pkg,
        current.entity
      );
      if (status === "OK") {
        this.options = data;
      }
    }
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
.wecube_attr_input {
  margin-top: -5px;
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
    line-height: 20px;
    word-break: break-all;
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
