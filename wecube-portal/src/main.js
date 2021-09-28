import Vue from 'vue'
import App from './App.vue'
import router from './router'

import ViewUI from 'view-design'
import 'view-design/dist/styles/iview.css'

import VueI18n from 'vue-i18n'
import locale from 'view-design/dist/locale/en-US'
import './locale/i18n'

import WeSelect from '../src/pages/components/select.vue'
import WeTable from '../src/pages/components/table.js'
import indexCom from './pages/index'
import req from './api/base'
import { getChildRouters } from './pages/util/router.js'

Vue.component('WeSelect', WeSelect)
Vue.component('WeTable', WeTable)
Vue.config.productionTip = false

Vue.use(ViewUI, {
  transfer: true,
  size: 'default',
  VueI18n,
  locale
})

window.request = req
window.needReLoad = true
window.routers = []

class UserWatch {
  constructor () {
    this.handles = {}
    this.data = []
  }
  on (eventType, handle) {
    if (!this.handles.hasOwnProperty(eventType)) {
      this.handles[eventType] = []
    }
    if (typeof handle === 'function') {
      this.handles[eventType].push(handle)
    }
    return this
  }
  emit (eventType, path) {
    if (this.handles.hasOwnProperty(eventType)) {
      this.handles[eventType].forEach((item, key, arr) => {
        item.apply(null, path)
      })
    }
    return this
  }
}
let WatchRouter = new UserWatch()
WatchRouter.on('change', path => {
  if (window.needReLoad) return
  if (path === '/login' || path === '/404') {
    path = '/homepage'
  }
  window.location.href = window.location.origin + '/#' + path
})

window.childRouters = []

window.addRoutes = (route, name) => {
  window.routers = window.routers.concat(route)
  getChildRouters(route)
  router.addRoutes([
    {
      path: '/',
      name: name,
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
    routes.map(r => {
      return {
        ...r,
        link: r.path,
        active: true
      }
    })
  )
}
window.implicitRoutes = {}
window.addImplicitRoute = routes => {
  window.implicitRoutes = Object.assign(window.implicitRoutes, routes)
}
window.homepageComponent = new UserWatch()
window.addHomepageComponent = compObj => {
  // compObj = {
  //   name: () => {},
  //   component: component
  // }
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
        if (child.path === path || child.redirect === path) {
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
  return found
}

router.beforeEach((to, from, next) => {
  const found = findPath(router.options.routes, to.path)
  if (!found) {
    window.location.href = window.location.origin + '#/homepage'
    next('/homepage')
  } else {
    if (window.myMenus) {
      let isHasPermission = []
        .concat(...window.myMenus.map(_ => _.submenus), window.childRouters)
        .find(_ => _.link === to.path)
      if (
        (isHasPermission && isHasPermission.active) ||
        to.path === '/404' ||
        to.path === '/login' ||
        to.path === '/homepage'
      ) {
        /* has permission */
        window.sessionStorage.setItem('currentPath', to.path === '/404' || to.path === '/login' ? '/homepage' : to.path)
        next()
      } else {
        /* has no permission */
        next('/404')
      }
    } else {
      next()
    }
  }
})
const vm = new Vue({
  router,
  render: h => h(App)
})
window.vm = vm
window.locale = (key, obj) => {
  const lang = vm._$lang.locales[key]
  let newLang = {}
  if (lang) {
    newLang = { ...lang, ...obj }
    vm._$lang.locales[key] = newLang
  }
}
window.addOptions = options => {
  Object.keys(options).forEach(key => {
    // eslint-disable-next-line no-proto
    vm.__proto__[key] = options[key]
  })
}

vm.$mount('#wecube_app')
