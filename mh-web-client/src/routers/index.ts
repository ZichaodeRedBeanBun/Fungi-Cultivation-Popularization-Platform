import {
  createRouter,
  createWebHistory,
  createWebHashHistory,
} from 'vue-router'

const mode = import.meta.env.VITE_ROUTER_MODE

const routerMode = {
  hash: () => createWebHashHistory(),
  history: () => createWebHistory(),
}

const router = createRouter({
  history: routerMode[mode](),
  strict: false,
  scrollBehavior: () => ({ left: 0, top: 0 }),
  routes: [
    {
      path: '/',
      component: () => import('@/pages/home/home.vue'),
    },
    {
      path: '/search',
      component: () => import('@/pages/search/search.vue'),
    },
    {
      path: '/data',
      component: () => import('@/pages/data/data.vue'),
    },
    {
      path: '/currentPlan/:planId', // 添加动态参数 :planId
      name: 'CurrentPlan',
      component: () => import('@/pages/currentPlan/index.vue'),
      props: true // 允许将路由参数作为props传递给组件
    },
    {
      path: '/planManagement',
      component: () => import('@/pages/planManagement/index.vue'),
    },
    {
      path: '/aiChat',
      component: () => import('@/pages/aiChat/chat.vue'),
    },
    {
      path: '/popSci',
      component: () => import('@/pages/popSci/index.vue'),
    },
    {
      path: '/create/:id?',
      component: () => import('@/pages/create/index.vue'),
    },
    {
      path: '/user',
      component: () => import('@/pages/user/index.vue'),
      // children: [
      //   {
      //     path: 'audit',
      //     component: () => import('@/pages/user/Audit.vue'),
      //   }
      // ]
    },
    {
      path: '/user/audit',
      component: () => import('@/pages/user/Audit.vue'),
    },
    {
      path: '/diseasepests',
      component: () => import('@/pages/diseasepests/index.vue'),
    },
    {
      path: '/mushroomCards',
      component: () => import('@/pages/mushroomCards/index.vue'),
    }
  ],
})

export default router