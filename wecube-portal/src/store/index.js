import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const state = {
  searchMap: new Map(),
  sideExpand: true
}

const getters = {
  searchMap(state) {
    return state.searchMap
  },
  sideExpand(state) {
    return state.sideExpand
  }
}

const mutations = {
  setSearchMap(state, { key, data }) {
    state.searchMap.set(key, data)
  },
  setSideExpand(state, val) {
    state.sideExpand = val
  }
}

export default new Vuex.Store({
  state,
  mutations,
  getters
})
