/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-01-20 09:58:50
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-03-06 12:06:31
 */
import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const state = {
  searchMap: new Map(),
  sideExpand: false, // 侧边栏展开状态
  implicitRoute: {}, // 需要额外配置面包屑的路由
  childRouters: [], // 不需要鉴权的子路由
  routes: []
}

const getters = {
  searchMap(state) {
    return state.searchMap
  },
  sideExpand(state) {
    return state.sideExpand
  },
  implicitRoute(state) {
    return state.implicitRoute
  },
  childRouters(state) {
    return state.childRouters
  },
  routes(state) {
    return state.routes
  }
}

const mutations = {
  setSearchMap(state, { key, data }) {
    state.searchMap.set(key, data)
  },
  setSideExpand(state, val) {
    state.sideExpand = val
  },
  setImplicitRoute(state, val) {
    state.implicitRoute = Object.assign(state.implicitRoute, val)
  },
  setChildRouters(state, val) {
    state.childRouters = state.childRouters.concat(
      val.map(r => ({
        ...r,
        link: r.path,
        active: true
      }))
    )
  },
  setRoutes(state, val) {
    state.routes = state.routes.contact(val)
  }
}

export default new Vuex.Store({
  state,
  mutations,
  getters
})
