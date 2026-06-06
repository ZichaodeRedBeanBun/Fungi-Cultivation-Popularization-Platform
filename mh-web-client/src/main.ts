import { createApp } from 'vue'
import App from './App.vue'
import router from './routers/index'
import Store from '@/stores'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 1. 补充 MotionPlugin 导入（v-motion-fade 指令依赖此插件）
import { MotionPlugin } from '@vueuse/motion'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './style/index.scss'
// 2. 正确导入 @pureadmin/table 插件
import Table from "@pureadmin/table";

const app = createApp(App)

// 注册顺序优化：先核心库，再自定义插件
// 3. 先注册 ElementPlus（@pureadmin/table 依赖它）
app.use(ElementPlus, {
  locale: zhCn,
})
// 4. 注册 MotionPlugin（解决 v-motion-fade 指令报错）
app.use(MotionPlugin)
// 5. 注册 @pureadmin/table 插件（全局注册 PureTable/PureTableBar 等）
app.use(Table)
// 6. 注册路由和状态管理
app.use(router)
app.use(Store)

app.mount('#app')