<template>
  <div class="platform-date-group">
    <span v-if="label">{{ label }}：</span>
    <RadioGroup
      v-if="dateType !== 4"
      v-model="dateType"
      @on-change="handleDateTypeChange"
      type="button"
      button-style="solid"
      size="small"
    >
      <Radio v-for="(j, idx) in typeList" :label="j.dateType" :key="idx" border>{{ j.label }}</Radio>
    </RadioGroup>
    <div v-else>
      <DatePicker
        :value="dateTime"
        @on-change="handleDateRange"
        type="daterange"
        split-panels
        placement="bottom-end"
        format="yyyy-MM-dd"
        :placeholder="label"
        style="width: 200px"
      />
      <Icon
        size="18"
        style="cursor: pointer"
        type="md-close-circle"
        @click="
          dateType = 1
          handleDateTypeChange()
        "
      />
    </div>
  </div>
</template>

<script>
import dayjs from 'dayjs'
export default {
  props: {
    typeList: {
      type: Array,
      default: () => []
    },
    label: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dateType: 1,
      dateTime: []
    }
  },
  mounted() {
    this.handleDateTypeChange()
    this.$emit('change', this.dateTime)
  },
  methods: {
    handleDateTypeChange() {
      this.dateTime = []
      if (this.dateType === 4) {
        this.dateTime = ['', '']
      } else {
        const { type, value } = this.typeList.find(i => i.dateType === this.dateType)
        const cur = dayjs().format('YYYY-MM-DD')
        const pre = dayjs().subtract(value, type)
          .format('YYYY-MM-DD')
        this.dateTime = [pre, cur]
      }
      this.$emit('change', this.dateTime)
    },
    handleDateRange(dateArr) {
      if (dateArr && dateArr[0] && dateArr[1]) {
        this.dateTime = [...dateArr]
      } else {
        this.dateTime = ['', '']
      }
      this.$emit('change', this.dateTime)
    }
  }
}
</script>

<style lang="scss" scoped>
.platform-date-group {
  display: flex;
  align-items: center;
  .ivu-radio {
    display: none;
  }
  .ivu-radio-wrapper {
    height: 32px !important;
    line-height: 32px !important;
    font-size: 12px !important;
  }
  .ivu-radio-wrapper-checked.ivu-radio-border {
    background-color: #5384ff;
    color: #fff;
  }
}
</style>
