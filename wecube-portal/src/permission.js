import router from './router'
import { i18n } from './locale/i18n/index.js'
// import { getChildRouters } from './pages/util/router.js'
// import { getGlobalMenus } from '@/const/util.js'
import store from './store'

// const findPath = (routes, path) => {
//   let found
//   window.routers.concat(routes).forEach(route => {
//     if (route.children) {
//       route.children.forEach(child => {
//         if (child.path === path || child.redirect === path || findSideMenuPath(child)) {
//           found = true
//         }
//       })
//     }
//     if (route.path === path || route.redirect === path) {
//       found = true
//     }
//     if (path.includes(route.path) && route.path !== '/') {
//       found = true
//     }
//   })
//   // 适配平台侧边菜单栏，父路由配置有子路由，判断子路由权限
//   function findSideMenuPath(child) {
//     if (Array.isArray(child.children) && child.children.length > 0) {
//       return child.children.some(item => item.path === path)
//     }
//     return false
//   }
//   return found
// }

router.beforeEach(async (to, from, next) => {
  // 每次进入页面，先重置侧边菜单展开状态
  store.commit('setSideExpand', false)
  if (to.path === '/homepage') {
    return
  }
  document.title = i18n.t('fd_platform')
  next()
  // if (['/404', '/login', '/homepage'].includes(to.path)) {
  //   return next()
  // }
  // const found = findPath(router.options.routes, to.path)
  // if (!found) {
  //   next()
  // } else {
  //   if (window.myMenus || ((await getGlobalMenus()) && window.myMenus)) {
  //     const isHasPermission = []
  //       .concat(...window.myMenus.map(_ => _.submenus), window.childRouters)
  //       .find(_ => to.path.startsWith(_.link) && _.active)
  //     if (
  //       (isHasPermission && isHasPermission.active)
  //       || ['/collaboration/workflow-mgmt', '/collaboration/registrationDetail'].includes(to.path)
  //     ) {
  //       next()
  //     } else {
  //       /* has no permission */
  //       next('/404')
  //     }
  //   } else {
  //     next('/login')
  //   }
  // }
})