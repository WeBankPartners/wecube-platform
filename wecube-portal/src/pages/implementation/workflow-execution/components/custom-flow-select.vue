<!--
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2024-10-14 15:05:46
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-02-08 10:24:56
-->
<template>
  <div>
    <Poptip
      v-model="visible"
      transfer
      popper-class="flow-custom-select-popper"
      placement="bottom-start"
      class="flow-custom-select"
    >
      <!--模拟输入框显示效果-->
      <div ref="input">
        <!--关键字搜索-->
        <Input
          v-model="form.search"
          @input="handleSearch"
          @on-click="handleClear"
          :placeholder="$t('fe_flowname_placeholder')"
          :disabled="disabled"
          :readonly="selected ? true : false"
          :icon="form.search ? 'ios-close-circle' : ''"
          style="width: 100%"
        >
        </Input>
      </div>
      <!--模拟下拉框显示效果-->
      <div
        slot="content"
        class="flow-custom-select-content"
        :style="{minWidth: width + 'px', width: 'fit-content', maxWidth: '1500px'}"
      >
        <div class="flow-custom-select-content-wrap">
          <div v-if="!selected" class="switch-group">
            <i-switch v-model="form.onlyShowMyFlow" @on-change="handleSearch" size="default" />
            <span class="title">{{ $t('pi_only_showMyself_records') }}</span>
            <span v-if="from === 'detail'" class="tips">{{ $t('pi_only_three_month_records') }}</span>
            <span v-else class="tips">{{ $t('pi_only_three_month_records1') }}</span>
          </div>
          <div v-if="filterOptions.length > 0" class="dropdown-wrap">
            <div
              v-for="item in filterOptions"
              :key="item.id"
              :class="{'dropdown-wrap-item': true, 'dropdown-wrap-item-active': item.checked}"
              @click="handleSelectItem(item)"
            >
              <div style="display: flex; justify-content: space-between; width: 100%">
                <div style="display: flex; align-items: center">
                  <span style="color: #2b85e4">{{ item.procInstName + ' ' }}</span>
                  <span style="color: #2b85e4">{{ '[' + item.version + '] ' }}</span>
                  <div
                    v-if="item.entityDisplayName"
                    :style="{
                      backgroundColor: '#c5c8ce',
                      padding: '4px 15px',
                      width: 'fit-content',
                      color: '#fff',
                      borderRadius: '4px',
                      display: 'inline-block',
                      marginLeft: '10px'
                    }"
                  >
                    {{ item.entityDisplayName + ' ' }}
                  </div>
                </div>
                <div style="display: flex; align-items: center">
                  <span style="color: #515a6e; margin-right: 20px">{{ item.operator || 'operator' }}</span>
                  <span style="color: #ccc">{{ (item.createdTime || '0000-00-00 00:00:00') + ' ' }}</span>
                  <div style="width: 100px">
                    <span :style="getStatusStyleAndName(item.displayStatus, 'style')">{{
                      getStatusStyleAndName(item.displayStatus, 'label')
                    }}</span>
                  </div>
                </div>
              </div>
              <!-- <Icon v-if="item.checked" type="ios-checkmark" size="24" color="#5384ff" /> -->
            </div>
          </div>
          <div v-else class="no-data">{{ $t('noData') }}</div>
        </div>
      </div>
    </Poptip>
  </div>
</template>
<script>
import { debounce } from '@/const/util'
export default {
  props: {
    options: {
      type: Array,
      default: () => []
    },
    value: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    from: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      form: {
        search: '',
        onlyShowMyFlow: true
      },
      selected: '',
      optionsData: [],
      filterOptions: [],
      width: 300,
      isMultiple: false,
      visible: false,
      onlyMyFlowFlag: false
    }
  },
  computed: {
    getStatusStyleAndName() {
      return function (status, type) {
        const list = [
          {
            label: this.$t('fe_notStart'),
            value: 'NotStarted',
            color: '#808695'
          },
          {
            label: this.$t('fe_stop'),
            value: 'Stop',
            color: '#ff4d4f'
          },
          {
            label: this.$t('fe_inProgressFaulted'),
            value: 'InProgress(Faulted)',
            color: '#ff4d4f'
          },
          {
            label: this.$t('fe_inProgressTimeouted'),
            value: 'InProgress(Timeouted)',
            color: '#ff4d4f'
          },
          {
            label: this.$t('fe_inProgress'),
            value: 'InProgress',
            color: '#1990ff'
          },
          {
            label: this.$t('fe_completed'),
            value: 'Completed',
            color: '#7ac756'
          },
          {
            label: this.$t('fe_faulted'),
            value: 'Faulted',
            color: '#e29836'
          },
          {
            label: this.$t('fe_internallyTerminated'),
            value: 'InternallyTerminated',
            color: '#e29836'
          }
        ]
        const findObj = list.find(i => i.value === status) || {}
        if (type === 'style') {
          return {
            display: 'inline-block',
            backgroundColor: findObj.color,
            padding: '4px 10px',
            width: 'fit-content',
            color: '#fff',
            borderRadius: '4px',
            float: 'right',
            fontSize: '12px',
            marginLeft: '5px'
          }
        }
        return findObj.label
      }
    }
  },
  watch: {
    options: {
      handler(val) {
        if (val && Array.isArray(val)) {
          this.optionsData = JSON.parse(JSON.stringify(val))
          this.optionsData.forEach(i => {
            this.$set(i, 'checked', false)
            this.$set(i, 'isShow', true)
          })
          this.initData()
          if (this.selected) {
            this.form.search = this.getDisplayName()
          }
        }
      },
      immediate: true,
      deep: true
    },
    value: {
      handler(val) {
        if (val) {
          this.selected = val
          if (Array.isArray(this.optionsData) && this.optionsData.length > 0) {
            this.form.search = this.getDisplayName()
          }
        }
        this.initData()
      },
      immediate: true,
      deep: true
    },
    optionsData: {
      handler() {
        if (this.selected) {
          this.filterOptions = this.optionsData.filter(i => i.id === this.selected)
        } else {
          this.filterOptions = this.optionsData
        }
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {
    this.width = this.$refs.input.clientWidth - 32
  },
  methods: {
    initData() {
      if (this.optionsData.length > 0) {
        this.optionsData.forEach(i => {
          if (this.selected === i.id) {
            i.checked = true
          }
        })
      }
    },
    getDisplayName() {
      if (this.selected && Array.isArray(this.optionsData) && this.optionsData.length > 0) {
        const obj = this.optionsData.find(i => i.id === this.selected)
        return (
          obj.procInstName
          + '  '
          + '['
          + obj.version
          + ']  '
          + obj.entityDisplayName
          + '  '
          + (obj.operator || 'operator')
          + '  '
          + (obj.createdTime || '0000-00-00 00:00:00')
          + '  '
          + this.getStatusStyleAndName(obj.displayStatus, 'label')
        )
      }
      return ''
    },
    // 刷新下拉列表数据
    handleSearch: debounce(function () {
      this.visible = true
      this.$emit('search', this.form)
    }, 500),
    // 选择下拉项回调
    handleSelectItem(item) {
      if (this.disabled) {
        return
      }
      this.optionsData.forEach(i => {
        if (i.id !== item.id) {
          i.checked = false
        } else {
          i.checked = true
          this.selected = item.id
          this.form.search = this.getDisplayName()
        }
      })
      this.$emit('input', this.selected)
      this.$emit('change', this.selected)
      this.visible = false
    },
    handleClear(e) {
      e.stopPropagation()
      this.selected = ''
      this.form.search = ''
      this.handleSearch()
      this.$emit('input', '')
      this.$emit('clear')
    }
  }
}
</script>
<style lang="scss">
.flow-custom-select {
  width: 100%;
  &-input {
    width: 100%;
    height: 32px;
    padding: 0 3px;
    overflow: hidden;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border: 1px solid #dcdee2;
    border-radius: 4px;
    .icon {
      width: 20px;
    }
  }
  &-disabled {
    color: #ccc;
    background: #f3f3f3;
    cursor: not-allowed;
  }
  &-content {
    min-width: 100%;
    &-wrap {
      .switch-group {
        padding: 12px 0 0 0;
        display: flex;
        align-items: center;
        .title {
          margin-left: 5px;
        }
        .tips {
          margin-left: 10px;
        }
      }
    }
    .dropdown {
      min-width: 100%;
      &-selected {
        margin-bottom: 5px;
        width: 100;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        &-title {
          font-weight: bold;
          margin-right: 5px;
        }
      }
      &-wrap {
        max-height: 400px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        padding-top: 10px;
        &-item {
          padding: 5px 0px;
          cursor: pointer;
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: space-between;
          &:hover {
            background: #f3f3f3;
          }
        }
        &-item-active {
          color: #5384ff;
        }
      }
    }
    .no-data {
      font-size: 12px;
      text-align: center;
      margin-top: 10px;
    }
  }
  .ivu-poptip-rel {
    width: 100%;
  }
  .ivu-select-dropdown {
    display: none;
  }
  .ivu-tag-text {
    display: inline-block;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: #515a6e;
  }
  .ivu-tag {
    width: fit-content;
    display: flex;
    align-items: center;
    line-height: 12px;
    padding: 4px 6px;
  }
}
.flow-custom-select-popper .ivu-poptip-body {
  padding: 0px 8px 8px 16px !important;
}
</style>
