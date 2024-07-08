<template>
  <div>
    <Drawer
      :title="title"
      v-model="drawerVisible"
      :width="width"
      :mask-closable="true"
      :scrollable="scrollable"
      :lock-scroll="true"
      @on-close="handleCancel"
      class="platform-base-drawer"
    >
      <div class="content" :style="{ maxHeight: maxHeight + 'px' }">
        <slot name="content" :maxHeight="maxHeight"></slot>
      </div>
      <div v-if="hasFooter" class="drawer-footer">
        <slot name="footer"></slot>
      </div>
    </Drawer>
  </div>
</template>

<script>
import { debounce } from '@/const/util'
export default {
  props: {
    title: {
      type: String,
      defailt: ''
    },
    visible: {
      type: Boolean,
      default: false
    },
    width: {
      type: String | Number,
      default: 1000
    },
    scrollable: {
      type: Boolean,
      default: false
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
      hasFooter: false
    }
  },
  mounted () {
    // 判断是否有底部按钮
    if (this.$slots.footer && this.$slots.footer.length > 0) {
      this.hasFooter = true
    } else {
      this.hasFooter = false
    }
    this.maxHeight = document.body.clientHeight - (this.hasFooter ? 150 : 100)
    window.addEventListener(
      'resize',
      debounce(() => {
        this.maxHeight = document.body.clientHeight - (this.hasFooter ? 150 : 100)
      }, 100)
    )
  },
  methods: {
    handleSubmit () {
      this.$emit('update:visible', false)
      this.$emit('submit')
    },
    handleCancel () {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.platform-base-drawer {
  .content {
    min-height: 500px;
    padding: 0px 10px;
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
