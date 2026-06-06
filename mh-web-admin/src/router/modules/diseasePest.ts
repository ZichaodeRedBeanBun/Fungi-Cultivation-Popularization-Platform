export default {
  path: "/diseasePest",
  redirect: "/diseasePest/index",
  meta: {
    icon: "mdi:bug-outline", // 你可以换成合适的图标
    title: "病虫害管理",
    rank: 6 // 根据实际排序调整
  },
  children: [
    {
      path: "/diseasePest/index",
      name: "diseasePestManagement",
      component: () => import("@/views/diseasePest/index.vue"),
      meta: {
        title: "病虫害管理"
      }
    }
  ]
} satisfies RouteConfigsTable;

