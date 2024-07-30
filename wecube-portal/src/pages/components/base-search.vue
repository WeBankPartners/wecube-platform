<template>
  <div class="platform-base-search">
    <div class="platform-base-search-form" :style="{maxHeight: expand ? '200px' : '40px'}">
      <Form :inline="true" :model="value" label-position="right">
        <slot name="prepend"></slot>
        <template v-for="(i, index) in options">
          <FormItem v-if="!i.hidden" :prop="i.key" :key="index">
            <div style="display: flex; align-items: center">
              <!--输入框-->
              <span v-if="i.label">{{ i.label }}：</span>
              <Input
                v-if="i.component === 'input'"
                v-model.trim="value[i.key]"
                :placeholder="i.placeholder"
                clearable
                :style="{width: i.width || 195 + 'px'}"
                @on-change="handleInputChange"
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
                :style="{width: i.width || 200 + 'px'}"
                @on-change="$emit('search')"
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
                :style="{width: i.width || 200 + 'px'}"
                @on-change="$emit('search')"
              >
                <template v-for="(j, idx) in i.list">
                  <Option :key="idx" :value="j.value">{{ j.label }}</Option>
                </template>
              </Select>
              <!--标签形式下拉框-->
              <Select
                v-else-if="i.component === 'tag-select'"
                v-model="value[i.key]"
                :placeholder="i.placeholder"
                clearable
                :multiple="i.multiple || false"
                :filterable="i.filterable || true"
                :max-tag-count="1"
                :style="{width: i.width || 200 + 'px'}"
                @on-change="$emit('search')"
              >
                <template v-for="(j, idx) in i.list">
                  <Option :key="idx" :value="j.value" :label="j.label">
                    <div
                      :style="{
                        backgroundColor: j.color,
                        padding: '4px 15px',
                        width: 'fit-content',
                        color: '#fff',
                        borderRadius: '4px'
                      }"
                    >
                      {{ j.label }}
                    </div>
                  </Option>
                </template>
              </Select>
              <!--switch开关类型-->
              <i-Switch
                v-else-if="i.component === 'switch'"
                v-model="value[i.key]"
                @on-change="$emit('search')"
                style="margin-right: 5px"
              >
              </i-Switch>
              <!--标签组-->
              <RadioGroup
                v-else-if="i.component === 'radio-group'"
                v-model="value[i.key]"
                @on-change="$emit('search')"
                type="button"
                button-style="solid"
                style="margin-right: 5px"
              >
                <Radio v-for="(j, idx) in i.list" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
              </RadioGroup>
              <!--自定义时间选择器-->
              <div v-else-if="i.component === 'custom-time'" class="custom-time">
                <RadioGroup
                  v-if="i.dateType !== 4"
                  v-model="i.dateType"
                  @on-change="handleDateTypeChange(i.key, i.dateType, i.dateRange)"
                  type="button"
                  size="small"
                  style="margin-top: -2px"
                >
                  <Radio v-for="(j, idx) in i.dateRange" :label="j.dateType" :key="idx" border>{{ j.label }}</Radio>
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
                      i.dateType = 1
                      handleDateTypeChange(i.key, 1, i.dateRange)
                    "
                  />
                </div>
              </div>
            </div>
          </FormItem>
        </template>
      </Form>
    </div>
    <div v-if="showBtn" class="platform-base-search-button">
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
      <Button @click="handleSearch" size="small" type="primary">{{ $t('search') }}</Button>
      <Button @click="handleReset" size="small" style="margin-left: 5px">{{ $t('reset') }}</Button>
    </div>
  </div>
</template>

<script>
import dayjs from 'dayjs'
import { debounce } from '@/const/util'
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
    },
    showBtn: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    formData() {
      return this.value
    }
  },
  data() {
    return {
      expand: false,
      dateTypeList: [
        {
          label: this.$t('be_threeDays_recent'),
          value: 1
        },
        {
          label: this.$t('be_oneWeek_recent'),
          value: 2
        },
        {
          label: this.$t('be_oneMonth_recent'),
          value: 3
        },
        {
          label: this.$t('be_auto'),
          value: 4
        }
      ]
    }
  },
  mounted() {
    this.options.forEach(i => {
      if (i.component === 'custom-time' && !i.dateType) {
        this.$set(i, 'dateType', i.initDateType)
      }
    })
  },
  methods: {
    handleExpand() {
      this.expand = !this.expand
    },
    handleSearch() {
      this.$emit('search')
    },
    handleInputChange: debounce(function () {
      this.$emit('search')
    }, 300),
    // 重置表单
    handleReset() {
      Object.keys(this.formData).forEach(key => {
        const formKeysArr = this.options.map(i => i.key)
        if (formKeysArr.includes(key)) {
          if (Array.isArray(this.formData[key])) {
            this.formData[key] = []
          }
          else {
            this.formData[key] = ''
          }
        }
      })
      // 处理时间类型默认值
      this.options.forEach(i => {
        if (i.component === 'custom-time') {
          this.$set(i, 'dateType', i.initDateType)
          this.handleDateTypeChange(i.key, i.dateType, i.dateRange, false)
        }
      })
      // 清空表单需要初始化默认值
      const initOptions = this.options.filter(i => i.initValue !== undefined)
      initOptions.forEach(i => {
        this.formData[i.key] = i.initValue
      })
      this.$emit('input', this.formData)
      this.handleSearch()
    },
    /**
     * 自定义时间控件转化时间格式值
     * @params key
     * @params dateType(1、2、3、4)按钮组key, 4代表自定义组
     * @params dateRange 时间组集合
      [
        { label: '近一月', type: 'month', value: 1, dateType: 1 },
        { label: '近半年', type: 'month', value: 6, dateType: 2 },
        { label: '近一年', type: 'year', value: 1, dateType: 3 },
        { label: this.$t('be_auto'), dateType: 4 }
      ]
     * @params remote 默认是否调用查询接口
    */
    handleDateTypeChange(key, dateType, dateRange, remote = true) {
      this.formData[key] = []
      if (dateType === 4) {
        this.formData[key] = []
      }
      else {
        const { type, value } = dateRange.find(i => i.dateType === dateType)
        const cur = dayjs().format('YYYY-MM-DD')
        const pre = dayjs().subtract(value, type)
          .format('YYYY-MM-DD')
        this.formData[key] = [pre, cur]
      }
      // 同步更新父组件form数据
      if (remote) {
        this.$emit('input', this.formData)
        this.$emit('search')
      }
    },
    handleDateRange(dateArr, key) {
      if (dateArr && dateArr[0] && dateArr[1]) {
        this.formData[key] = [...dateArr]
      }
      else {
        this.formData[key] = []
      }
      this.$emit('input', this.formData)
      this.$emit('search')
    },
    // 获取远程下拉框数据
    async getRemoteData(i) {
      const res = await i.remote()
      this.$set(i, 'list', res)
    }
  }
}
</script>

<style lang="scss">
.platform-base-search {
  width: 100%;
  display: flex;
  &-form {
    max-width: calc(100% - 146px);
    transition: all 0.2s;
    overflow: hidden;
    padding-right: 10px;
  }
  &-button {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    width: 146px;
    height: 30px;
    box-sizing: content-box;
    button {
      width: auto;
      height: 28px;
      line-height: 28px;
      font-size: 13px;
    }
  }
  .ivu-form-item {
    margin-bottom: 15px !important;
    display: inline-block !important;
  }
  .ivu-radio {
    display: none;
  }
  .ivu-radio-wrapper {
    height: 30px !important;
    line-height: 30px !important;
    font-size: 12px !important;
    // color: #000;
  }
  .ivu-radio-wrapper-checked.ivu-radio-border {
    background-color: #2d8cf0;
    color: #fff;
  }
  .ivu-select-multiple .ivu-tag {
    max-width: 90px;
  }
}
</style>
