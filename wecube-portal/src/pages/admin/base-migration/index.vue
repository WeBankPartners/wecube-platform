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
/**
 * 基础迁移主入口组件
 * 提供一键导出和一键导入功能的导航菜单
 */
export default {
  data() {
    return {
      // 菜单是否展开
      expand: true,
      // 菜单列表配置
      menuList: [
        // 一键导出菜单项
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
        // 一键导入菜单项
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
    /**
     * 计算内容区域的样式
     * 根据菜单展开状态调整左侧内边距
     * @returns {Object} 样式对象
     */
    benchStyle() {
      return {
        paddingLeft: this.expand ? '140px' : '0px'
      }
    }
  },
  /**
   * 组件挂载后监听菜单展开/收起事件
   */
  mounted() {
    this.$eventBusP.$on('expand-menu', val => {
      this.expand = val
    })
  },
  methods: {}
}
</script>
