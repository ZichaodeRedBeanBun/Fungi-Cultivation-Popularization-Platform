export default {
  path: "/banner",
  redirect: "/banner/index",
  meta: {
    icon: "basil:hotspot-solid",
    title: "行业热点管理",
    rank: 2
  },
  children: [
    {
      path: "/banner/index",
      name: "BannerManagement",
      component: () => import("@/views/banner/index.vue"),
      meta: {
        title: "行业热点管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
