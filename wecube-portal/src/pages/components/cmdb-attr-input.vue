<template>
  <Row>
    <Col span="19">
      <AttrInput
        v-if="mappingType === 'CMDB_CI_TYPE'"
        :allCiTypes="allCiTypes"
        :cmdbColumnSource="paramData.cmdbColumnSource"
        :rootCiType="rootCiType"
        v-model="cmdbAttr"
        :ciTypesObj="ciTypesObj"
        :ciTypeAttributeObj="ciTypeAttributeObj"
        @change="cmdbAttrChangeHandler"
      />
      <Select
        v-if="mappingType === 'CMDB_ENUM_CODE'"
        @on-change="cmdbEnumCodeChangeHandler"
        v-model="cmdbEnumCode"
        filterable
      >
        <Option
          v-for="item in allCodes"
          :value="item.value"
          :key="item.value"
          >{{ item.label }}</Option
        >
      </Select>
    </Col>
    <Col span="4" offset="1">
      <Select @on-change="mappingTypeChangeHandler" v-model="mappingType">
        <Option value="CMDB_CI_TYPE" key="CMDB_CI_TYPE">CiType属性</Option>
        <Option value="CMDB_ENUM_CODE" key="CMDB_ENUM_CODE">枚举类型</Option>
      </Select>
    </Col>
  </Row>
</template>
<script>
import AttrInput from "./attr-input";
export default {
  components: {
    AttrInput
  },
  data() {
    return {
      mappingType: "CMDB_CI_TYPE",
      cmdbEnumCode: 0,
      cmdbAttr: ""
    };
  },
  props: {
    allCiTypes: { required: true },
    allCodes: { required: true },
    rootCiType: { required: true },
    ciTypesObj: { required: true },
    ciTypeAttributeObj: { required: true },
    paramData: { required: true },
    value: {}
  },
  watch: {
    paramData: {
      handler(val) {
        this.mappingType = val.mappingType;
        this.cmdbEnumCode = val.cmdbEnumCode;
      },
      immediate: true
    }
  },
  computed: {
    cmdbCiType() {
      return {
        mappingType: this.mappingType,
        cmdbEnumCode: null,
        cmdbColumnSource: this.cmdbAttr.cmdbColumnSource,
        cmdbColumnCriteria: this.cmdbAttr.cmdbColumnCriteria
      };
    },
    cmdbEnumCodeValue() {
      return {
        mappingType: this.mappingType,
        cmdbEnumCode: this.cmdbEnumCode,
        cmdbColumnSource: null,
        cmdbColumnCriteria: null
      };
    }
  },
  methods: {
    mappingTypeChangeHandler(v) {
      if (v === "CMDB_CI_TYPE") {
        this.$emit("input", this.cmdbCiType);
      } else {
        this.$emit("input", this.cmdbEnumCodeValue);
      }
    },
    cmdbAttrChangeHandler(v) {
      this.$emit("input", this.cmdbCiType);
    },
    cmdbEnumCodeChangeHandler(v) {
      this.$emit("input", this.cmdbEnumCodeValue);
    }
  }
};
</script>
