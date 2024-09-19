import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)
const router = new Router({
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
          path: '/collaboration/registrationDetail',
          name: 'registrationDetail',
          component: () => import('@/pages/collaboration/plugin-registration-detail'),
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
          // component: () => import('@/pages/collaboration/plugin-management')
          component: () => import('@/pages/collaboration/plugin-registration-list')
        },
        // 编排执行
        {
          path: '/implementation/workflow-execution',
          name: 'workflowExecution',
          redirect: '/implementation/workflow-execution/normal-template',
          component: () => import('@/pages/implementation/workflow-execution/index'),
          children: [
            // 普通执行-模板选择
            {
              path: '/implementation/workflow-execution/normal-template',
              name: 'normalTemplate',
              component: () => import('@/pages/implementation/workflow-execution/normal-execution/template')
            },
            // 普通执行-新建
            {
              path: '/implementation/workflow-execution/normal-create',
              name: 'normalCreate',
              component: () => import('@/pages/implementation/workflow-execution/execution')
            },
            // 普通执行-历史
            {
              path: '/implementation/workflow-execution/normal-history',
              name: 'normalHistory',
              component: () => import('@/pages/implementation/workflow-execution/normal-execution/history')
            },
            // 定时执行-新建
            {
              path: '/implementation/workflow-execution/time-create',
              name: 'timeCreate',
              component: () => import('@/pages/implementation/workflow-execution/time-execution/create')
            },
            // 定时执行-历史
            {
              path: '/implementation/workflow-execution/time-history',
              name: 'timeHistory',
              component: () => import('@/pages/implementation/workflow-execution/time-execution/history')
            },
            // 查看执行
            {
              path: '/implementation/workflow-execution/view-execution',
              name: 'viewExecution',
              component: () => import('@/pages/implementation/workflow-execution/execution')
            }
          ]
        },
        // 批量执行
        {
          path: '/implementation/batch-execution',
          name: 'batchExecution',
          redirect: '/implementation/batch-execution/choose-template',
          component: () => import('@/pages/implementation/batch-execution/index'),
          children: [
            // 执行-模板选择
            {
              path: '/implementation/batch-execution/choose-template',
              name: 'chooseTemplate',
              component: () => import('@/pages/implementation/batch-execution/execution/choose-template.vue')
            },
            // 执行-新建新建
            {
              path: '/implementation/batch-execution/create-execution',
              name: 'createExecution',
              component: () => import('@/pages/implementation/batch-execution/execution/create.vue')
            },
            // 执行-执行历史
            {
              path: '/implementation/batch-execution/execution-history',
              name: 'executionHistory',
              component: () => import('@/pages/implementation/batch-execution/execution/list.vue')
            },
            // 模板-新建模板
            {
              path: '/implementation/batch-execution/template-create',
              name: 'templateCreate',
              component: () => import('@/pages/implementation/batch-execution/template/create.vue')
            },
            // 模板-模板管理
            {
              path: '/implementation/batch-execution/template-list',
              name: 'templateList',
              component: () => import('@/pages/implementation/batch-execution/template/list.vue')
            }
          ]
        },
        // 底座迁移
        {
          path: '/admin/base-migration',
          name: 'baseMigration',
          redirect: '/admin/base-migration/export',
          component: () => import('@/pages/admin/base-migration/index'),
          children: [
            // 一键导出-创建
            {
              path: '/admin/base-migration/export',
              name: 'migrationExport',
              component: () => import('@/pages/admin/base-migration/export/create.vue')
            },
            // 一键导出-历史
            {
              path: '/admin/base-migration/export-history',
              name: 'migrationExportHistory',
              component: () => import('@/pages/admin/base-migration/export/history.vue')
            },
            // 一键导入-创建
            {
              path: '/admin/base-migration/import',
              name: 'migrationImport',
              component: () => import('@/pages/admin/base-migration/import/create.vue')
            },
            // 一键导入-历史
            {
              path: '/admin/base-migration/import-history',
              name: 'migrationImportHistory',
              component: () => import('@/pages/admin/base-migration/import/history.vue')
            }
          ]
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

export default router
