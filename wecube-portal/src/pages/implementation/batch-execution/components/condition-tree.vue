<template>
  <div>
    <Drawer
      :title="$t('be_edit_filter')"
      v-model="drawerVisible"
      width="50%"
      :mask-closable="false"
      :lock-scroll="true"
      @on-close="handleCancel"
      class="condition-tree-drawer"
    >
      <div class="content" :style="{maxHeight: maxHeight + 'px'}">
        <Tree :data="treeData" show-checkbox multiple></Tree>
      </div>
      <div class="drawer-footer">
        <Button style="margin-right: 8px" @click="handleCancel">{{ $t('cancel') }}</Button>
        <Button type="primary" class="primary" @click="handleSubmit">{{ $t('confirm') }}</Button>
      </div>
    </Drawer>
  </div>
</template>

<script>
import { debounce, deepClone } from '@/const/util'
export default {
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    data: {
      type: Array,
      default: () => []
    },
    select: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    drawerVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  data() {
    return {
      maxHeight: 500,
      treeData: [],
      operatorList: ['eq', 'contains', 'like', 'in', 'lt', 'gt', 'neq', 'notNull', 'null']
    }
  },
  watch: {
    data: {
      handler(val) {
        this.treeData = deepClone(val)
        this.treeData.forEach(i => {
          this.$set(i, 'expand', true)
          if (i.children && i.children.length) {
            i.children.forEach(j => {
              this.$set(j, 'value', '')
              this.$set(j, 'operator', 'contains')
              // 勾选数据回显
              this.select.forEach(select => {
                if (select.id === j.id) {
                  this.$set(j, 'checked', true)
                  j.value = select.value
                  j.operator = select.operator
                }
              })
              j.render = (h, { data }) => (
                <div class="tree-item">
                  <span>{data.title}</span>
                  {data.checked && (
                    <div style="display:flex;justify-content:flex-start;width:280px;">
                      <Select
                        v-model={data.operator}
                        style="width:90px;"
                        class={{ 'ivu-form-item-error': !data.operator }}
                        on-on-change={() => {
                          data.value = ''
                        }}
                      >
                        {this.operatorList.map((item, index) => (
                          <Option value={item} key={index}>
                            {item}
                          </Option>
                        ))}
                      </Select>
                      {!['notNull', 'null'].includes(data.operator) && (
                        <Input
                          v-model={data.value}
                          style="width:180px;margin-left:5px;"
                          class={{ 'ivu-form-item-error': !data.value }}
                          placeholder={`${data.operator === 'in' ? 'eg：a,b,c' : ''}`}
                        />
                      )}
                    </div>
                  )}
                </div>
              )
            })
          }
        })
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    this.maxHeight = document.body.clientHeight - 150
    window.addEventListener(
      'resize',
      debounce(() => {
        this.maxHeight = document.body.clientHeight - 150
      }, 100)
    )
  },
  methods: {
    // checkChange (totalChecked) {
    //   // 去除全选时最外层数据
    //   this.selectData = totalChecked.filter(i => i.nodeKey !== 0)
    // },
    handleSubmit() {
      const selectData = []
      this.treeData.forEach(i => {
        i.children.forEach(j => {
          if (j.checked) {
            selectData.push(j)
          }
        })
      })
      const flag = selectData.every(i => ['notNull', 'null'].includes(i.operator) || i.value)
      if (flag) {
        this.$emit('update:visible', false)
        this.$emit('submit', selectData)
      } else {
        return this.$Notice.warning({
          title: this.$t('warning'),
          desc: this.$t('be_required_tips')
        })
      }
    },
    handleCancel() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.condition-tree-drawer {
  .content {
    min-height: 500px;
    padding: 20px;
    overflow-y: auto;
  }
  .drawer-footer {
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;
    border-top: 1px solid #e8e8e8;
    padding: 10px 16px;
    text-align: center;
    background: #fff;
  }
}
</style>
<style lang="scss">
.condition-tree-drawer {
  .ivu-tree-title-selected,
  .ivu-tree-title-selected:hover {
    background-color: #fff;
  }
  .tree-item {
    width: 450px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
