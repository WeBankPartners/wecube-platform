import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);
let router = new Router({
  routes: [
    {
      path: "/",
      name: "home",
      redirect: "/homepage",
      component: () => import("@/pages/index"),
      children: [
        {
          path: "/homepage",
          name: "homepage",
          component: () => import("@/pages/home-page"),
          params: {},
          props: true
        },
        {
          path: "/coming-soon",
          name: "comingsoon",
          component: () => import("@/pages/coming-soon"),
          params: {},
          props: true
        },
        {
          path: "/collaboration/workflow-orchestration",
          name: "flowManage",
          component: () =>
            import("@/pages/collaboration/workflow-orchestration"),
          props: true
        },
        {
          path: "/admin/system-params",
          name: "systemParams",
          component: () => import("@/pages/admin/system-params")
        },
        {
          path: "/admin/resources",
          name: "resources",
          component: () => import("@/pages/admin/resources/index")
        },
        {
          path: "/admin/user-role-management",
          name: "userRoleManagement",
          component: () => import("@/pages/admin/user-role-management")
        },
        {
          path: "/collaboration/plugin-management",
          name: "pluginManage",
          component: () => import("@/pages/collaboration/plugin-management")
        },
        {
          path: "/implementation/workflow-execution",
          name: "workflowExecution",
          component: () => import("@/pages/implementation/workflow-execution")
        }
      ]
    },
    {
      path: "/login",
      name: "login",
      component: () => import("@/pages/login"),
      params: {},
      props: true
    },
    {
      path: "/404",
      name: "404",
      component: () => import("@/pages/404"),
      params: {},
      props: true
    }
  ]
});

export default router;
