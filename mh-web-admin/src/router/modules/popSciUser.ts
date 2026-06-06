export default {
  path: "/popSciUser",
  redirect: "/popSciUser/index",
  meta: {
    icon: "material-symbols:article-person-outline-rounded",
    title: "科普号审核管理",
    rank: 5
  },
  children: [
    {
      path: "/popSciUser/index",
      name: "popSciUserManagement",
      component: () => import("@/views/popSciUser/index.vue"),
      meta: {
        title: "科普号审核管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
