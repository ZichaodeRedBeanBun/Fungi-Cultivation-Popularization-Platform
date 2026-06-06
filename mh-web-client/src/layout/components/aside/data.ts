export const MenuData = [
  {
    title: '发现',
    children: [
      { title: '首页', icon: 'ri:home-smile-2-line', router: '/' },
     { title: '推荐', icon: 'ri-function-ai-line', router: '/popSci'}, // 替换：推荐专属图标，更贴合+加载稳定
      { title: '菌种产效可视化', icon: 'ri:bar-chart-2-line', router: '/data' },
      { title: '毒菇识别', icon: 'ri:scan-line', router: '/aiChat' },
    ]
  },
  {
    title: '发布',
    children: [
      {
        title: '计划管理',
        icon: 'ri-calendar-todo-fill', // 替换：计划专属极简图标，加载无压力
        router: '/planManagement'
      },
      { title: '个人中心', icon: 'ri:user-line', router: '/create' },
    ],
  },
]