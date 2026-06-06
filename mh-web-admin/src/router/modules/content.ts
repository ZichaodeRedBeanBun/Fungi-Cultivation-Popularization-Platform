export default {
  path: "/content",
  redirect: "/content/index",
  meta: {
    icon: "mingcute:mushroom-fill",
    title: "科普内容管理",
    rank: 4
  },
  children: [
    {
      path: "/content/index",
      name: "contentManagement",
      component: () => import("@/views/content/index.vue"),
      meta: {
        title: "科普内容管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
