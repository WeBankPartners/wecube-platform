/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-01-20 09:58:50
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-03-07 21:06:54
 */
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import { registerMicroApps, start, setDefaultMountApp, initGlobalState } from 'qiankun'

import ViewUI from 'view-design'
import './styles/index.less'

import VueI18n from 'vue-i18n'
import { i18n } from './locale/i18n/index.js'
import viewDesignEn from 'view-design/dist/locale/en-US'
import viewDesignZh from 'view-design/dist/locale/zh-CN'
import mainApp from './main-app'

// 引用wecube公共组件
import commonUI from 'wecube-common-ui'
import 'wecube-common-ui/lib/wecube-common-ui.css'
Vue.use(commonUI)

import WeSelect from '@/pages/components/select.vue'
import WeTable from '@/pages/components/table.js'
import implicitRoutes from './implicitRoutes.js'
import './permission.js'

Vue.component('WeSelect', WeSelect)
Vue.component('WeTable', WeTable)

const eventBus = new Vue()
Vue.prototype.$eventBusP = eventBus
Vue.config.productionTip = false

Vue.use(ViewUI, {
  transfer: true,
  size: 'default',
  VueI18n,
  locale: i18n.locale === 'en-US' ? viewDesignEn : viewDesignZh
})

// 注册qiankun
registerMicroApps(mainApp, {
  beforeLoad: app => {
    console.log('before load app.name====>>>>>', app.name)
  },
  beforeMount: [
    app => {
      console.log('[LifeCycle] before mount %c%s', 'color: green;', app.name)
    }
  ],
  afterMount: [
    app => {
      console.log('[LifeCycle] after mount %c%s', 'color: green;', app.name)
    }
  ],
  afterUnmount: [
    app => {
      console.log('[LifeCycle] after unmount %c%s', 'color: green;', app.name)
    }
  ]
})

setDefaultMountApp('/#/taskman')

start({
  sandbox: {
    strictStyleIsolation: false // 开启严格的样式隔离（Shadow DOM 模式）
  },
  prefetch: false
})

// vue初始化
const vm = new Vue({
  router,
  store,
  render: h => h(App),
  i18n
})

vm.$mount('#wecube_app')

window.vm = vm
window.locale = (key, obj) => {
  const lang = vm._i18n.messages[key]
  let newLang = {}
  if (lang) {
    newLang = {
      ...lang,
      ...obj
    }
    i18n.setLocaleMessage(key, newLang)
  }
}

// qiankun父子应用通信
const globalState = {
  expand: false, // 解决子应用侧边栏折叠，面包屑不左右移动的问题
  implicitRoute: implicitRoutes, // 存放子应用的路由信息，用于面包屑导航显示
  childRouters: [], // 存放子应用没有权限配置的子路由
  routes: []
}
const actions = initGlobalState(globalState)
actions.onGlobalStateChange((state) => {
  console.log("主应用监听到状态变化:", state)

  if (Object.prototype.hasOwnProperty.call(state, 'expand')) {
    store.commit('setSideExpand', state.expand)
  }

  if (Object.prototype.hasOwnProperty.call(state, 'implicitRoute')) {
    store.commit('setImplicitRoute', state.implicitRoute)
  }

  if (Object.prototype.hasOwnProperty.call(state, 'childRouters')) {
    store.commit('setChildRouters', state.childRouters)
  }

  // if (Object.prototype.hasOwnProperty.call(state, 'routes')) {
  //   store.commit('setRoutes', state.routes)
  // }
})
actions.setGlobalState(globalState)
//actions.offGlobalStateChange()
