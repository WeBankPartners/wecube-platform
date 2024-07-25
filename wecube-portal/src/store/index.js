import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const state = {
  searchMap: new Map()
}

const mutations = {
  setSearchMap (state, { key, data }) {
    state.searchMap.set(key, data)
  }
}

export default new Vuex.Store({
  state,
  mutations
})