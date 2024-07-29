<template>
  <div id="workflow-execution">
    <div :style="benchStyle">
      <transition name="fade" mode="out-in">
        <router-view class="pages" :key="$route.name"></router-view>
      </transition>
      <BenchMenu :menuList="menuList"></BenchMenu>
    </div>
  </div>
</template>

<script>
import BenchMenu from '@/pages/components/bench-menu'
export default {
  components: {
    BenchMenu
  },
  data() {
    return {
      expand: true,
      menuList: [
        {
          // 普通执行
          title: this.$t('fe_normalExecute'),
          icon: 'ios-hammer',
          name: '1',
          children: [
            {
              title: this.$t('create'),
              path: '/implementation/workflow-execution/normal-template',
              name: '1-1'
            },
            {
              title: this.$t('fe_history'),
              path: '/implementation/workflow-execution/normal-history',
              name: '1-2'
            }
          ]
        },
        {
          // 定时执行
          title: this.$t('timed_execution'),
          icon: 'ios-time',
          name: '2',
          children: [
            {
              title: this.$t('create'),
              path: '/implementation/workflow-execution/time-create',
              name: '2-1'
            },
            {
              title: this.$t('fe_history'),
              path: '/implementation/workflow-execution/time-history',
              name: '2-2'
            }
          ]
        }
      ]
    }
  },
  computed: {
    benchStyle() {
      return {
        paddingLeft: this.expand ? '140px' : '0px'
      }
    }
  },
  mounted() {
    this.$eventBusP.$on('expand-menu', val => {
      this.expand = val
    })
  },
  methods: {}
}
</script>
<style lang="scss">
#workflow-execution {
  .ivu-tag {
    display: inline-block;
    line-height: 16px;
    height: auto;
    padding: 5px 6px;
  }
}
</style>
