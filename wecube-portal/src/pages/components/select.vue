<template>
  <div>
    <Select v-if="!isGroup" :value="value" :multiple="isMultiple" filterable clearable @on-change="changeValue">
      <Option v-for="item in opts" :value="item.value" :key="item.value">{{ item.label }}</Option>
    </Select>
    <Select
      v-else
      :value="value"
      :multiple="isMultiple"
      filterable
      clearable
      @on-change="changeValue"
      :max-tag-count="maxTags"
    >
      <OptionGroup v-for="(group, index) in opts" :key="index" :label="group.label">
        <Option v-for="item in group.children" :value="item.value" :key="item.value">{{ item.label }}</Option>
      </OptionGroup>
    </Select>
  </div>
</template>
<script>
const DEFAULT_TAG_NUMBER = 2
export default {
  name: 'WeSelect',

  props: {
    value: {},
    isMultiple: { default: () => false },
    isGroup: { default: () => false },
    options: { default: () => [] },
    maxTags: { default: () => DEFAULT_TAG_NUMBER },
    filterParams: {}
  },
  data() {
    return {
      filterOpts: []
    }
  },
  watch: {},
  computed: {
    opts() {
      if (this.filterParams) {
        return this.filterOpts
      }

      return this.options
    }
  },
  mounted() {},
  methods: {
    changeValue(val) {
      this.$emit('input', val || null)
      this.$emit('change', val || null)
    }
  }
}
</script>
