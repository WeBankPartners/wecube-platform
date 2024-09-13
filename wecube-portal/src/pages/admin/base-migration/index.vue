<!--底座迁移-->
<template>
  <div id="base-migration">
    <div :style="benchStyle">
      <transition name="fade" mode="out-in">
        <router-view :key="$route.fullPath"></router-view>
      </transition>
      <BaseMenu :menuList="menuList"></BaseMenu>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      expand: true,
      menuList: [
        {
          title: '一键导出',
          icon: 'md-cloud-download',
          name: '1',
          children: [
            {
              title: '创建',
              path: '/admin/base-migration/export',
              name: '1-1'
            },
            {
              title: '历史',
              path: '/admin/base-migration/export-history',
              name: '1-2'
            }
          ]
        },
        {
          title: '一键导入',
          icon: 'md-cloud-upload',
          name: '2',
          children: [
            {
              title: '创建',
              path: '/admin/base-migration/import',
              name: '2-1'
            },
            {
              title: '历史',
              path: '/admin/base-migration/import-history',
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
