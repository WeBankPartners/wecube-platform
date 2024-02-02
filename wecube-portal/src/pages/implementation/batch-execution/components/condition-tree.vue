<template>
  <div>
    <Drawer
      title="编辑过滤条件"
      v-model="drawerVisible"
      width="600"
      :mask-closable="false"
      :lock-scroll="true"
      @on-close="handleCancel"
      class="condition-tree-drawer"
    >
      <div class="content" :style="{ maxHeight: maxHeight + 'px' }">
        <Tree :data="treeData" @on-check-change="checkChange" show-checkbox multiple></Tree>
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
      get () {
        return this.visible
      },
      set (val) {
        this.$emit('update:visible', val)
      }
    }
  },
  data () {
    return {
      maxHeight: 500,
      treeData: [],
      selectData: []
    }
  },
  watch: {
    data: {
      handler (val) {
        this.treeData = deepClone(val)
        this.treeData.forEach(i => {
          this.$set(i, 'expand', true)
          if (i.children && i.children.length) {
            i.children.forEach(j => {
              this.$set(j, 'value', '')
              // 勾选数据回显
              this.select.forEach(select => {
                if (select.id === j.id) {
                  this.$set(j, 'checked', true)
                  j.value = select.value
                }
              })
              j.render = (h, { data }) => {
                return (
                  <div class={{ 'tree-item': true, 'ivu-form-item-error': !data.value }}>
                    <span>{data.title}</span>
                    {data.checked && <Input v-model={data.value} style="width:280px;" />}
                  </div>
                )
              }
            })
          }
        })
      },
      deep: true,
      immediate: true
    }
  },
  mounted () {
    this.maxHeight = document.body.clientHeight - 150
    window.addEventListener(
      'resize',
      debounce(() => {
        this.maxHeight = document.body.clientHeight - 150
      }, 100)
    )
  },
  methods: {
    checkChange (totalChecked) {
      // 去除全选时最外层数据
      this.selectData = totalChecked.filter(i => i.nodeKey !== 0)
    },
    handleSubmit () {
      const flag = this.selectData.every(i => i.value)
      if (flag) {
        this.$emit('update:visible', false)
        this.$emit('submit', this.selectData)
      } else {
        return this.$Notice.warning({
          title: this.$t('warning'),
          desc: this.$t('required_tip')
        })
      }
    },
    handleCancel () {
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
