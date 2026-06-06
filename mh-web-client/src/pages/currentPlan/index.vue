<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification, ElMessage } from 'element-plus'
// 导入4个标签页子组件
import BasicInfo from './components/BasicInfo.vue'
import PlantNode from './components/PlantNode.vue'
import PlantData from './components/PlantData.vue'
import HarvestResult from './components/HarvestResult.vue'
// 导入类型定义
import type { PlantPlanVO } from '@/api/interface'
// 导入基础信息接口（获取计划详情）
import { getPlantPlanById } from '@/api/system'

// 路由实例（获取planId参数）
const route = useRoute()
const router = useRouter()
// 标签页列表
const tabList = [
  { name: '基础信息', value: 'basicInfo' },
  { name: '种植节点', value: 'plantNode' },
  { name: '种植数据', value: 'plantData' },
  { name: '采收成果', value: 'harvestResult' }
]

// 当前激活的标签（默认基础信息）
const activeTab = ref('basicInfo')
// 计划ID（从路由参数获取）
const planId = ref<number>(0)
// 计划基本信息（用于标题展示）
const planBasicInfo = ref<PlantPlanVO | null>(null)
// 加载状态
const loading = ref(false)
// 处理无效 planId
// 同步修复 handleInvalidPlanId 中的路由判断
// const handleInvalidPlanId = () => {
//   // 仅当前是CurrentPlan路由时，才跳回列表页
//   if (route.name === 'CurrentPlan') {
//     ElNotification.error({
//       message: '未获取到有效的种植计划ID',
//       duration: 2000
//     })
//     router.push('/planManagement') // 列表页路径
//   }
// }

// 【核心】仅校验一次planId（进入详情页时），通过后更新planId
const initPlanId = () => {
  const id = Number(route.params.planId)
  // 1. 校验planId有效性
  if (isNaN(id) || id <= 0) {
    ElNotification.error({
      message: '未获取到有效的种植计划ID',
      duration: 2000
    })
    router.push('/planManagement')
    return false
  }
  // 2. 校验通过，更新planId（关键：传给子组件的核心数据）
  planId.value = id
  return true
}
// 组件挂载时：初始化planId + 获取基础信息（支撑标签页更新）
onMounted(async () => {
  // 先校验planId，无效则直接跳走，不执行后续逻辑
  if (!initPlanId()) return
  // 校验通过，获取基础信息（同时planId已赋值，传给子组件）
  await fetchPlanBasicInfo()
})

// 返回列表：主动跳转，不依赖history.back
const goBackToList = () => {
  router.push('/planManagement')
}
// 获取计划基本信息
const fetchPlanBasicInfo = async () => {
  try {
    loading.value = true
    const res = await getPlantPlanById(planId.value)
    if (res.code === 0 && res.data) {
      planBasicInfo.value = res.data as PlantPlanVO
    } else {
      ElMessage.error('获取计划信息失败')
    }
  } catch (error) {
    ElMessage.error('获取计划信息失败')
  } finally {
    loading.value = false
  }
}

// 切换标签页
const switchTab = (tabValue: string) => {
  activeTab.value = tabValue
}
</script>

<template>
  <div class="flex flex-col h-full flex-1 overflow-hidden bg-background px-4 py-2">
    <!-- 页面标题和基础信息 -->
    <div class="py-4 border-b pb-3">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 mb-1">{{ planBasicInfo?.planName || '种植计划详情' }}</h1>
          <div class="flex items-center space-x-4 text-sm text-gray-500">
            <span>菌型: {{ planBasicInfo?.fungusType || '-' }}</span>
            <span>状态: 
              <el-tag :type="{
                'EXECUTING': 'success',
                'COMPLETED': 'info',
                'PAUSED': 'warning',
                'INVALID': 'danger'
              }[planBasicInfo?.status || '']">
                {{ planBasicInfo?.status === 'EXECUTING' ? '执行中' : 
                    planBasicInfo?.status === 'COMPLETED' ? '已完成' : 
                    planBasicInfo?.status === 'PAUSED' ? '暂停' : '作废' }}
              </el-tag>
            </span>
          </div>
        </div>
        <el-button type="primary"  @click="goBackToList" class="mt-4 md:mt-0">
          <el-icon name="ArrowLeftBold" />
          返回列表
        </el-button>
      </div>
    </div>

    <!-- 标签页切换栏（沿用项目风格） -->
    <div class="py-4 border-b pb-1">
      <div class="inline-flex h-10 items-center rounded-lg bg-muted/70 p-1 text-muted-foreground w-full justify-start mb-2 overflow-x-auto">
        <button
          v-for="tab in tabList"
          :key="tab.value"
          @click="switchTab(tab.value)"
          :class="{
            'bg-activeMenuBg text-foreground shadow-sm': activeTab === tab.value,
            'hover:bg-muted/90': activeTab !== tab.value
          }"
          class="inline-flex items-center justify-center whitespace-nowrap rounded-sm px-4 py-1.5 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        >
          {{ tab.name }}
        </button>
      </div>
    </div>

    <!-- 标签页内容容器（仅当planId有效时渲染） -->
    <div v-if="planId" class="flex-grow overflow-x-hidden overflow-y-auto">
      <div class="max-w-1000 mx-auto bg-bg-color rounded-12 shadow-sm p-4 mb-4">
        <BasicInfo v-if="activeTab === 'basicInfo'" :plan-id="planId" />
        <PlantNode v-if="activeTab === 'plantNode'" :plan-id="planId" />
        <PlantData v-if="activeTab === 'plantData'" :plan-id="planId" />
        <HarvestResult v-if="activeTab === 'harvestResult'" :plan-id="planId" />
      </div>
    </div>
    
    <!-- 无planId时的占位提示 -->
    <div v-else class="flex-grow flex items-center justify-center">
      <div class="text-center text-gray-500">
        <el-icon size="48" class="mb-2"><Warning /></el-icon>
        <p>未获取到种植计划ID，请返回列表页重新选择</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 统一项目样式变量（匹配个人中心/首页风格） */
:root {
  --bg-color: var(--el-bg-color);
  --activeMenuBg: var(--el-color-primary-light-9);
  --max-w-1000: max-width: 1500px; /* 从1000px增大到2000px */
  --rounded-12: border-radius: 12px;
}

.bg-background {
  background-color: var(--el-bg-color-page);
}

.bg-muted\/70 {
  background-color: rgba(var(--el-color-muted-rgb), 0.7);
}

.text-muted-foreground {
  color: var(--el-text-color-regular);
}

.text-foreground {
  color: var(--el-text-color-primary);
}

.max-w-1000 {
  max-width: 1500px; /* 从1000px增大到2000px */
}

.rounded-12 {
  border-radius: 12px;
}

.shadow-sm {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>