<template>
  <div class="platform-date-group">
    <RadioGroup
      v-if="dateType !== 4"
      v-model="dateType"
      @on-change="handleDateTypeChange(dateType)"
      type="button"
      size="small"
      style="margin-top: -2px"
    >
      <Radio v-for="(j, idx) in dateTypeList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
    </RadioGroup>
    <div v-else>
      <DatePicker
        :value="dateTime"
        @on-change="
          val => {
            handleDateRange(val)
          }
        "
        type="daterange"
        split-panels
        placement="bottom-end"
        format="yyyy-MM-dd"
        :placeholder="i.label"
        style="width: 200px"
      />
      <Icon
        size="18"
        style="cursor: pointer"
        type="md-close-circle"
        @click="
          dateType = 1
          handleDateTypeChange(1)
        "
      />
    </div>
  </div>
</template>

<script>
import dayjs from 'dayjs'
export default {
  data () {
    return {
      dateTime: []
    }
  },
  methods: {
    // 自定义时间控件转化时间格式值
    handleDateTypeChange (dateType) {
      const cur = dayjs().format('YYYY-MM-DD')
      if (dateType === 1) {
        const pre = dayjs().subtract(3, 'day').format('YYYY-MM-DD')
        this.dateTime = [pre, cur]
      } else if (dateType === 2) {
        const pre = dayjs().subtract(7, 'day').format('YYYY-MM-DD')
        this.dateTime = [pre, cur]
      } else if (dateType === 3) {
        const pre = dayjs().subtract(1, 'month').format('YYYY-MM-DD')
        this.dateTime = [pre, cur]
      } else if (dateType === 4) {
        this.dateTime = []
      }
    },
    handleDateRange (dateArr) {
      if (dateArr && dateArr[0] && dateArr[1]) {
        this.dateTime = [...dateArr]
      } else {
        this.dateTime = []
      }
    }
  }
}
</script>

<style lang="scss" scoped></style>
