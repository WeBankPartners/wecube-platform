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
        // 一键导出
        {
          title: this.$t('pe_one_export'),
          icon: 'md-cloud-download',
          name: '1',
          children: [
            {
              title: this.$t('p_create'),
              path: '/admin/base-migration/export',
              name: '1-1'
            },
            {
              title: this.$t('fe_history'),
              path: '/admin/base-migration/export-history',
              name: '1-2'
            }
          ]
        },
        // 一键导入
        {
          title: this.$t('pe_one_import'),
          icon: 'md-cloud-upload',
          name: '2',
          children: [
            {
              title: this.$t('p_create'),
              path: '/admin/base-migration/import',
              name: '2-1'
            },
            {
              title: this.$t('fe_history'),
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
