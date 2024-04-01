import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)
let router = new Router({
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/homepage',
      component: () => import('@/pages/index'),
      children: [
        {
          path: '/homepage',
          name: 'homepage',
          component: () => import('@/pages/home-page.js'),
          params: {},
          props: true
        },
        {
          path: '/coming-soon',
          name: 'comingsoon',
          component: () => import('@/pages/coming-soon'),
          params: {},
          props: true
        },
        {
          path: '/collaboration/workflow-mgmt',
          name: 'flow',
          component: () => import('@/pages/collaboration/workflow-mgmt'),
          props: true
        },
        {
          path: '/collaboration/workflow',
          name: 'flowManage',
          component: () => import('@/pages/collaboration/workflow'),
          props: true
        },
        {
          path: '/admin/system-params',
          name: 'systemParams',
          component: () => import('@/pages/admin/system-params')
        },
        {
          path: '/admin/resources',
          name: 'resources',
          component: () => import('@/pages/admin/resources/index')
        },
        {
          path: '/admin/certification',
          name: 'certification',
          component: () => import('@/pages/admin/plugin-certification')
        },
        {
          path: '/admin/user-role-management',
          name: 'userRoleManagement',
          component: () => import('@/pages/admin/user-role-management')
        },
        {
          path: '/admin/workflow-report',
          name: 'workflowReport',
          component: () => import('@/pages/admin/workflow-report/index')
        },
        {
          path: '/collaboration/plugin-management',
          name: 'pluginManage',
          component: () => import('@/pages/collaboration/plugin-management')
        },
        {
          path: '/implementation/workflow-execution',
          name: 'workflowExecution',
          component: () => import('@/pages/implementation/workflow-execution')
        },
        {
          path: '/implementation/batch-execution',
          name: 'batchExecution',
          component: () => import('@/pages/implementation/batch-execution/index')
        },
        {
          path: '/admin/system-data-model',
          name: 'systemDataModel',
          component: () => import('@/pages/admin/system-data-model')
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/login'),
      params: {},
      props: true
    },
    {
      path: '/404',
      name: '404',
      component: () => import('@/pages/404'),
      params: {},
      props: true
    }
  ]
})

const keys = ['push', 'replace']
keys.forEach(key => {
  const original = Router.prototype[key]
  Router.prototype[key] = function push (localtion, onResolve, onReject) {
    if (onResolve || onReject) return original.call(this, localtion, onResolve, onReject)
    return original.call(this, localtion).catch(err => err)
  }
})

export default router
