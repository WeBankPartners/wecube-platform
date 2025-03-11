<template>
  <div class="platform-homepage">
    <Tabs v-if="comps.length !== 0" name="home" @on-click="jumpPage">
      <TabPane v-for="i in comps" :key="i.code" :name="i.path" :label="i.name" tab="home">
      </TabPane>
    </Tabs>
    <DefaultComp v-else />
    <router-view></router-view>
  </div>
</template>
<script>

import DefaultComp from './home.vue'
import { getGlobalMenus, deepClone } from '@/const/util.js'
export default {
  name: 'homepage',
  components: { DefaultComp },
  data() {
    return {
      comps: [],
      homePageList: [
        { 
          code: 'TASK_WORKBENCH',
          name: '工作台',
          path: '/taskman/workbench/dashboard'
        },
        { 
          code: 'WECMDB_DATA_QUERY_CI',
          name: '数据视图',
          path: '/wecmdb/data-query-ci'
        }
      ] // 首页列表
    }
  },
  mounted() {
    this.fetchHomePage()
  },
  methods: {
    async fetchHomePage() {
      await getGlobalMenus()
      let homePageList = deepClone(this.homePageList)
      homePageList.forEach(c => {
        c.deleteFalg = false
        if (c.code && Array.isArray(window.myMenus) && window.myMenus.length > 0) {
          // taskman、monitor根据二级菜单判断首页权限
          const permission = window.myMenus.some(i => i.submenus.some(j => j.code === c.code && j.active))
          if (!permission) {
            c.deleteFalg = true
          }
        }
      })
      // 首页根据菜单权限隐藏相关页面
      homePageList = homePageList.filter(i => !i.deleteFalg)
      this.comps = homePageList
    },
    jumpPage(path) {
      this.$router.push(path)
    }
  }
}
</script>

