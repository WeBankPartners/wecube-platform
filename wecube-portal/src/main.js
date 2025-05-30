import 'core-js/stable'
// import 'regenerator-runtime/runtime'
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

import WeSelect from '../src/pages/components/select.vue'
import WeTable from '../src/pages/components/table.js'
import indexCom from './pages/index'
import req from './api/base'
import implicitRoutes from './implicitRoutes.js'
import { getChildRouters } from './pages/util/router.js'
import { getGlobalMenus } from '@/const/util.js'
import { pluginNameMap } from '@/const/util.js'
// 引用wecube公共组件
import commonUI from 'wecube-common-ui'
import 'wecube-common-ui/lib/wecube-common-ui.css'
Vue.use(commonUI)

const eventBus = new Vue()
Vue.prototype.$eventBusP = eventBus
Vue.component('WeSelect', WeSelect)
Vue.component('WeTable', WeTable)
Vue.config.productionTip = false

Vue.use(ViewUI, {
  transfer: true,
  size: 'default',
  VueI18n,
  locale: i18n.locale === 'en-US' ? viewDesignEn : viewDesignZh
})

window.request = req
window.needReLoad = true
window.routers = []

class UserWatch {
  constructor() {
    this.handles = {}
    this.data = []
  }
  on(eventType, handle) {
    if (!Object.prototype.hasOwnProperty.call(this.handles, eventType)) {
      this.handles[eventType] = []
    }
    if (typeof handle === 'function') {
      this.handles[eventType].push(handle)
    }
    return this
  }
  emit(eventType, path) {
    if (Object.prototype.hasOwnProperty.call(this.handles, eventType)) {
      this.handles[eventType].forEach(item => {
        item.apply(null, path)
      })
    }
    return this
  }
}
const WatchRouter = new UserWatch()
WatchRouter.on('change', oldPath => {
  let path = ''
  if (window.needReLoad) {
    return
  }
  if (oldPath === '/login' || oldPath === '/404') {
    path = '/homepage'
  }
  window.location.href = window.location.origin + '/#' + path
})

const getDocumentTitleMap = (routeArr, name) => {
  if (routeArr.length > 0) {
    routeArr.forEach(item => {
      pluginNameMap[item.path] = `p_${name}`
    })
  }
}

window.childRouters = []

window.addRoutes = (route, name) => {
  getDocumentTitleMap(route, name)
  window.routers = window.routers.concat(route)
  getChildRouters(route)
  router.addRoutes([
    {
      path: '/',
      name,
      redirect: '/homepage',
      component: indexCom,
      children: route
    }
  ])
  if (window.sessionStorage.currentPath) {
    WatchRouter.emit('change', [window.sessionStorage.currentPath])
  }
}
window.addRoutersWithoutPermission = routes => {
  window.childRouters = window.childRouters.concat(
    routes.map(r => ({
      ...r,
      link: r.path,
      active: true
    }))
  )
}
window.implicitRoutes = implicitRoutes
window.addImplicitRoute = routes => {
  window.implicitRoutes = Object.assign(window.implicitRoutes, routes)
}
window.homepageComponent = new UserWatch()
window.addHomepageComponent = compObj => {
  const found = window.homepageComponent.data.find(_ => _.name() === compObj.name())
  if (!found) {
    window.homepageComponent.data.push(compObj)
  }
  if (router.app.$route.path === '/homepage') {
    window.homepageComponent.emit('change', [])
  }
}

window.component = (name, comp) => {
  Vue.component(name, comp)
}
window.use = (lib, options) => {
  Vue.use(lib, options)
}

const findPath = (routes, path) => {
  let found
  window.routers.concat(routes).forEach(route => {
    if (route.children) {
      route.children.forEach(child => {
        if (child.path === path || child.redirect === path || findSideMenuPath(child)) {
          found = true
        }
      })
    }
    if (route.path === path || route.redirect === path) {
      found = true
    }
    if (path.includes(route.path) && route.path !== '/') {
      found = true
    }
  })
  // 适配平台侧边菜单栏，父路由配置有子路由，判断子路由权限
  function findSideMenuPath(child) {
    if (Array.isArray(child.children) && child.children.length > 0) {
      return child.children.some(item => item.path === path)
    }
    return false
  }
  return found
}

router.beforeEach(async (to, from, next) => {
  if (window.isLoadingPlugin && to.path === '/homepage') {
    return
  }
  document.title = i18n.t('fd_platform')
  if (['/404', '/login', '/homepage'].includes(to.path)) {
    return next()
  }
  const found = findPath(router.options.routes, to.path)
  if (!found) {
    window.sessionStorage.setItem('currentPath', to.fullPath)
    next({
      path: '/homepage',
      query: { type: 'isInitStatus' }
    })
  } else {
    if (window.myMenus || ((await getGlobalMenus()) && window.myMenus)) {
      const isHasPermission = []
        .concat(...window.myMenus.map(_ => _.submenus), window.childRouters)
        .find(_ => to.path.startsWith(_.link) && _.active)
      if (
        (isHasPermission && isHasPermission.active)
        || ['/collaboration/workflow-mgmt', '/collaboration/registrationDetail'].includes(to.path)
      ) {
        /* has permission */
        window.sessionStorage.setItem(
          'currentPath',
          to.path === '/404' || to.path === '/login' ? '/homepage' : to.fullPath
        )
        next()
      } else {
        /* has no permission */
        next('/404')
      }
    } else {
      if (!['/404', '/login', '/homepage'].includes(to.path)) {
        window.sessionStorage.setItem('currentPath', to.fullPath)
      }
      next('/login')
    }
  }
})
const vm = new Vue({
  router,
  render: h => h(App),
  i18n
})

// 创建vuex实例$globalStore(监控把自己的$store挂载到平台了，需要区分)
Vue.use(store)
// eslint-disable-next-line no-proto
vm.__proto__.$globalStore = store

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

window.addOptions = options => {
  Object.keys(options).forEach(key => {
    // eslint-disable-next-line no-proto
    vm.__proto__[key] = options[key]
  })
}

vm.$mount('#wecube_app')
