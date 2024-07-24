<template>
  <div id="batch-execution">
    <div :style="benchStyle">
      <transition name="fade" mode="out-in">
        <router-view></router-view>
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
          title: this.$t('execute'),
          icon: 'md-hammer',
          name: '1',
          children: [
            {
              title: this.$t('be_new_execute'),
              path: '/implementation/batch-execution/choose-template',
              name: '1-1'
            },
            {
              title: this.$t('be_execute_history'),
              path: '/implementation/batch-execution/execution-history',
              name: '1-2'
            }
          ]
        },
        {
          title: this.$t('template'),
          icon: 'md-document',
          name: '2',
          children: [
            {
              title: this.$t('be_new_template'),
              path: '/implementation/batch-execution/template-create',
              name: '2-1'
            },
            {
              title: this.$t('be_template_manage'),
              path: '/implementation/batch-execution/template-list',
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
