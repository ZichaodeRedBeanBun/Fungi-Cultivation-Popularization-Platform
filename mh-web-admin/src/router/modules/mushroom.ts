export default {
  path: "/mushroom",
  redirect: "/mushroom/index",
  meta: {
    icon: "stash:article-alt-solid",
    title: "菌种卡片管理",
    rank: 3
  },
  children: [
    {
      path: "/mushroom/index",
      name: "mushroomManagement",
      component: () => import("@/views/mushroom/index.vue"),
      meta: {
        title: "菌种卡片管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
