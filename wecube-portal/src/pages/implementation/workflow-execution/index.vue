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
  data () {
    return {
      expand: true,
      menuList: [
        {
          title: '普通执行',
          icon: 'ios-hammer',
          name: '1',
          children: [
            { title: '新建', path: '/implementation/workflow-execution/normal-template', name: '1-1' },
            { title: '历史', path: '/implementation/workflow-execution/normal-history', name: '1-2' }
          ]
        },
        {
          title: '定时执行',
          icon: 'ios-time',
          name: '2',
          children: [
            { title: '新建', path: '/implementation/workflow-execution/time-create', name: '2-1' },
            { title: '历史', path: '/implementation/workflow-execution/time-history', name: '2-2' }
          ]
        }
      ]
    }
  },
  computed: {
    benchStyle () {
      return {
        paddingLeft: this.expand ? '140px' : '0px'
      }
    }
  },
  mounted () {
    this.$eventBusP.$on('expand-menu', val => {
      this.expand = val
    })
  },
  methods: {}
}
</script>
