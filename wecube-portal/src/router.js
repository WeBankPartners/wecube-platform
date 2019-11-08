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
          path: "/implementation/batch-job",
          name: "batchJobExecution",
          component: () => import("@/pages/implementation/batch-job"),
          params: {},
          props: true
        },
        // {
        //   path: "/designing/application-deployment",
        //   name: "deploymentDesign",
        //   component: () => import("@/pages/designing/application-deployment"),
        //   props: true
        // },
        {
          path: "/collaboration/workflow-orchestration",
          name: "flowManage",
          component: () =>
            import("@/pages/collaboration/workflow-orchestration"),
          props: true
        },
        {
          path: "/admin/permission-management",
          name: "permissions",
          component: () => import("@/pages/admin/permission-management")
        },
        {
          path: "/collaboration/plugin-management",
          name: "pluginManage",
          component: () => import("@/pages/collaboration/plugin-management")
        },
        // {
        //   path: "/admin/cmdb-model-management",
        //   name: "ciDesign",
        //   component: () => import("@/pages/admin/cmdb-model-management")
        // },
        {
          path: "/admin/core/base-data-management",
          name: "baseData",
          component: () => import("@/pages/admin/enums")
        },
        {
          path: "/designing/core/enum-management",
          name: "enumManage",
          component: () => import("@/pages/admin/enums")
        },
        {
          path: "/designing/core/enum-enquiry",
          name: "enumEnquiry",
          component: () => import("@/pages/admin/enums")
        },
        // {
        //   path: "/designing/ci-data-management",
        //   name: "ciDataManage",
        //   component: () => import("@/pages/designing/ci-data")
        // },
        // {
        //   path: "/designing/ci-data-enquiry",
        //   name: "ciDataEnquiry",
        //   component: () => import("@/pages/designing/ci-data")
        // },
        // {
        //   path: "/designing/ci-integrated-query-execution",
        //   name: "integrateQuery",
        //   component: () =>
        //     import("@/pages/designing/ci-integrated-query-execution")
        // },
        // {
        //   path: "/designing/ci-integrated-query-management",
        //   name: "integrateQueryMgmt",
        //   component: () =>
        //     import("@/pages/designing/ci-integrated-query-management")
        // },
        // {
        //   path: "/designing/application-architecture",
        //   name: "architectureDesign",
        //   component: () => import("@/pages/designing/application-architecture")
        // },
        {
          path: "/implementation/artifact-management",
          name: "artifactManagement",
          component: () => import("@/pages/implementation/artifact-management")
        },
        // {
        //   path: "/designing/planning",
        //   name: "designPlanning",
        //   component: () => import("@/pages/designing/planning")
        // },
        // {
        //   path: "/designing/resource-planning",
        //   name: "resourcePlanning",
        //   component: () => import("@/pages/designing/resource-planning")
        // },
        {
          path: "/implementation/application-deployment",
          name: "deployment",
          component: () =>
            import("@/pages/implementation/application-deployment")
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
