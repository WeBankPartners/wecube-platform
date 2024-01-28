<template>
  <div class="platform-base-search">
    <div class="form" :style="{ maxHeight: expand ? '200px' : '45px' }">
      <Form :inline="true" :model="value" label-position="right">
        <template v-for="(i, index) in options">
          <FormItem v-if="!i.hidden" :prop="i.key" :key="index">
            <div style="display: flex; align-items: center">
              <!--输入框-->
              <span v-if="i.label">{{ i.label }}：</span>
              <Input
                v-if="i.component === 'input'"
                v-model="value[i.key]"
                :placeholder="i.placeholder"
                clearable
                :style="{ width: i.width || 200 + 'px' }"
              ></Input>
              <!--下拉选择-->
              <Select
                v-else-if="i.component === 'select'"
                v-model="value[i.key]"
                :placeholder="i.placeholder"
                clearable
                :multiple="i.multiple || false"
                :filterable="i.filterable || true"
                :max-tag-count="1"
                :style="{ width: i.width || 200 + 'px' }"
              >
                <template v-for="(j, idx) in i.list">
                  <Option :key="idx" :value="j.value">{{ j.label }}</Option>
                </template>
              </Select>
              <!--获取接口的下拉选择-->
              <Select
                v-else-if="i.component === 'remote-select'"
                v-model="value[i.key]"
                @on-open-change="getRemoteData(i)"
                :placeholder="i.placeholder"
                clearable
                :multiple="i.multiple || false"
                :filterable="i.filterable || true"
                :max-tag-count="1"
                :style="{ width: i.width || 200 + 'px' }"
              >
                <template v-for="(j, idx) in i.list">
                  <Option :key="idx" :value="j.value">{{ j.label }}</Option>
                </template>
              </Select>
              <!--标签组-->
              <RadioGroup
                v-else-if="i.component === 'radio-group'"
                v-model="value[i.key]"
                @on-change="$emit('search')"
                style="margin-right: 32px"
              >
                <Radio v-for="(j, idx) in i.list" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
              </RadioGroup>
              <!--自定义时间选择器-->
              <div v-else-if="i.component === 'custom-time'" class="custom-time">
                <RadioGroup
                  v-if="i.dateType !== 4"
                  v-model="i.dateType"
                  @on-change="handleDateTypeChange(i.key, i.dateType)"
                  type="button"
                  size="small"
                  style="margin-top: -2px"
                >
                  <Radio v-for="(j, idx) in dateTypeList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
                </RadioGroup>
                <div v-else>
                  <DatePicker
                    :value="value[i.key]"
                    @on-change="
                      val => {
                        handleDateRange(val, i.key)
                      }
                    "
                    type="daterange"
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
                      i.dateType = 1
                      handleDateTypeChange(i.key, 1)
                    "
                  />
                </div>
              </div>
            </div>
          </FormItem>
        </template>
      </Form>
    </div>
    <template v-if="showExpand">
      <Icon
        v-show="!expand"
        @click="handleExpand"
        size="28"
        color="#2d8cf0"
        type="ios-arrow-down"
        style="cursor: pointer; margin-right: 10px"
      />
      <Icon
        v-show="expand"
        @click="handleExpand"
        size="28"
        color="#2d8cf0"
        type="ios-arrow-up"
        style="cursor: pointer; margin-right: 10px"
      />
    </template>
    <div class="button-group">
      <Button @click="handleSearch" size="small" type="primary">{{ $t('search') }}</Button>
      <Button @click="handleReset" size="small" style="margin-left: 5px">{{ $t('reset') }}</Button>
    </div>
  </div>
</template>

<script>
import dayjs from 'dayjs'
export default {
  props: {
    value: {
      type: Object,
      default: () => {}
    },
    options: {
      type: Array,
      default: () => []
    },
    showExpand: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    formData () {
      return this.value
    }
  },
  data () {
    return {
      expand: false,
      dateTypeList: [
        { label: this.$t('tw_recent_three_month'), value: 1 },
        { label: this.$t('tw_recent_half_year'), value: 2 },
        { label: this.$t('tw_recent_one_year'), value: 3 },
        { label: this.$t('tw_auto'), value: 4 }
      ]
    }
  },
  methods: {
    handleExpand () {
      this.expand = !this.expand
    },
    handleSearch () {
      this.$emit('search')
    },
    // 重置表单
    handleReset () {
      Object.keys(this.formData).forEach(key => {
        if (Array.isArray(this.formData[key])) {
          this.formData[key] = []
        } else {
          this.formData[key] = ''
        }
      })
      // 处理时间类型默认值
      this.options.forEach(i => {
        if (i.component === 'custom-time' && i.initValue) {
          i.dateType = 1
        } else {
          i.dateType = 4
        }
      })
      // 点击清空按钮需要给默认值的表单选项
      const initOptions = this.options.filter(i => i.initValue !== undefined)
      initOptions.forEach(i => {
        this.formData[i.key] = i.initValue
      })
      this.$emit('input', this.formData)
      this.handleSearch()
    },
    // 自定义时间控件转化时间格式值
    handleDateTypeChange (key, dateType) {
      this.formData[key] = []
      const cur = dayjs().format('YYYY-MM-DD')
      if (dateType === 1) {
        const pre = dayjs().subtract(3, 'month').format('YYYY-MM-DD')
        this.formData[key] = [pre, cur]
      } else if (dateType === 2) {
        const pre = dayjs().subtract(6, 'month').format('YYYY-MM-DD')
        this.formData[key] = [pre, cur]
      } else if (dateType === 3) {
        const pre = dayjs().subtract(1, 'year').format('YYYY-MM-DD')
        this.formData[key] = [pre, cur]
      } else if (dateType === 4) {
        this.formData[key] = []
      }
      // 同步更新父组件form数据
      this.$emit('input', this.formData)
    },
    handleDateRange (dateArr, key) {
      this.formData[key] = [...dateArr]
      this.$emit('input', this.formData)
    },
    // 获取远程下拉框数据
    async getRemoteData (i) {
      const res = await i.remote()
      this.$set(i, 'list', res)
    }
  }
}
</script>

<style lang="scss">
.platform-base-search {
  display: flex;
  .ivu-form-item {
    margin-bottom: 15px !important;
    display: inline-block !important;
  }
  .ivu-radio {
    display: none;
  }
  .ivu-radio-wrapper {
    // border-radius: 5px;
    height: 30px !important;
    line-height: 30px !important;
    // padding: 0 10px;
    font-size: 12px !important;
    color: #000;
    // background: #f5f8fa;
    // border: none;
  }
  .ivu-radio-wrapper-checked.ivu-radio-border {
    border-color: #2d8cf0;
    // background: #2d8cf0;
    color: #2d8cf0;
  }
  .ivu-select-multiple .ivu-tag {
    max-width: 90px;
  }
  .form {
    flex: 1;
    transition: all 0.2s;
    overflow: hidden;
  }
  .button-group {
    height: 30px;
    display: inline-block;
    border-left: 1px solid #0000000f;
    padding-left: 12px;
    box-sizing: content-box;
    button {
      width: auto;
      padding: 0 12px;
      height: 30px;
      line-height: 30px;
      font-size: 13px;
    }
  }
}
</style>
