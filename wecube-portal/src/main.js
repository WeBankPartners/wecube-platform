/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-01-20 09:58:50
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-03-07 19:38:28
 */
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import ViewUI from 'view-design'
import './styles/index.less'

import VueI18n from 'vue-i18n'
import { i18n } from './locale/i18n/index.js'
import viewDesignEn from 'view-design/dist/locale/en-US'
import viewDesignZh from 'view-design/dist/locale/zh-CN'

// 引用wecube公共组件
import commonUI from 'wecube-common-ui'
import 'wecube-common-ui/lib/wecube-common-ui.css'
Vue.use(commonUI)

import WeSelect from '@/pages/components/select.vue'
import WeTable from '@/pages/components/table.js'
import './permission.js'
import './main-app'

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
