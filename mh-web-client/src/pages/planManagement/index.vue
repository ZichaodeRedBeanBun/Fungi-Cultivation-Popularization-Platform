<script setup lang="ts">
import { onMounted } from 'vue'
import { usePlanManagement } from './script'
import{
  PlantPlanVO,
  PlantPlanPageQueryDTO,
  PlantPlanAddDTO,
  PlantPlanUpdateDTO,
  PlantPlanStatusEnum,
  PlantPlanStatusDescMap,
  PlantNodeVO,
  PlantDataVO
} from '@/api/interface'
// 解构 script 中暴露的所有属性和方法
const {
  loading,
  planList,
  searchForm,
  selectedPlanIds,
  currentPage,
  pageSize,
  state,
  detailDialogVisible,
  currentPlanDetail,
  planDialogVisible,
  planDialogTitle,
  planForm,
  timelineDialogVisible,
  importDialogVisible,
  exportDialogVisible,
  selectedExportPlan,
  importFile,
  importLoading,
  exportLoading,
  handleSearch,
  resetSearch,
  goToCurrentPlan,
  openPlanDetail,
  handlePlanAction,
  openAddPlanDialog,
  submitPlanForm,
  openEditPlanDialog,
  deletePlan,
  batchDeletePlans,
  toggleSelectAll,
  toggleSelectPlan,
  isPlanSelected,
  openTimelineDialog,
  openImportDialog,
  handleFileChange,
  importPlantPlans,
  openExportDialog,
  exportPlantPlan,
  formatDate,
  formatStatus,
  getStatusTagType,
  getActionButtonText,
  hasSelectedItems,
  useRenderIcon,
  Refresh,
  AddFill,
  Upload,
  Download,
  More,
  exportOptions,
  statusOptions,
  planFormRules,
  selectedPlanForTimeline,
  fetchPlantPlans,
  getNodeDataPoints,
  showDataTooltip,
  importInput,
  nodeStatusLegend,
  hideDataTooltip,
  getGrowthStatusText,
  getGrowthStatusColor,
  timelineNodes,
  planFormRef,
  tooltipVisible,
  tooltipPosition,
  tooltipContent
} = usePlanManagement()
onMounted(() => {
  // 列表页初始化操作
  fetchPlantPlans()
})

// 解决从导航栏返回不刷新的问题
onActivated(() => {
  const currentRoute = useRoute()
  // 假设列表页的路由name是planManagement，需和路由配置一致
  if (currentRoute.name === 'planManagement') {
    if (currentRoute.query.from !== 'list') {
      fetchPlantPlans()
    }
  }
})
</script>

<template>
  <div class="flex flex-col h-full flex-1 overflow-hidden bg-background px-4 py-2">
    <!-- 搜索区域 -->
    <div class="py-4">
      <el-card class="mb-4">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-medium">种植计划管理</span>
            <div class="flex space-x-3">
              <el-button type="primary" :icon="useRenderIcon(Upload)" @click="openImportDialog">
                导入计划
              </el-button>
              <el-button type="primary" :icon="useRenderIcon(Download)" @click="openExportDialog"
                :disabled="!hasSelectedItems">
                导出计划
              </el-button>
              <el-button type="primary" :icon="useRenderIcon(AddFill)" @click="openAddPlanDialog">
                新增计划
              </el-button>
            </div>
          </div>
        </template>
        <el-form :model="searchForm" label-width="80px" class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <el-form-item label="计划名称">
            <el-input v-model="searchForm.planName" placeholder="请输入计划名称" clearable
              @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="计划状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="w-full">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <div class="flex items-end justify-end space-x-4">
            <el-button type="primary" :icon="useRenderIcon('ri:search-line')" :loading="loading"
              @click="handleSearch">搜索</el-button>
            <el-button :icon="useRenderIcon(Refresh)" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 批量操作栏（放在搜索框下方，靠右对齐） -->
      <div v-if="planList.length > 0" class="flex justify-end mb-4">
        <el-checkbox :model-value="selectedPlanIds.length === planList.length && planList.length > 0"
          @change="toggleSelectAll" class="mr-4">
          全选
        </el-checkbox>
        <el-button type="primary" :disabled="!hasSelectedItems" text class="mr-1" @click="batchDeletePlans">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 计划卡片区域 -->
    <div class="flex-grow flex flex-col overflow-x-hidden">
      <div class="flex-1 overflow-x-hidden my-2 relative" v-loading="loading">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="item in planList" :key="item.planId"
            class="rounded-xl hover:bg-background transition duration-300 border bg-card text-card-foreground shadow-md overflow-hidden hover:shadow-xl relative cursor-pointer"
            :class="{ 'border-primary-500 ring-2 ring-primary-200': isPlanSelected(item.planId) }"
            @click="handlePlanAction(item)">
            <!-- 选择复选框 -->
            <div class="absolute top-0 right-1 z-10" @click.stop="toggleSelectPlan(item.planId)">
              <el-checkbox :model-value="isPlanSelected(item.planId)" />
            </div>

            <div class="p-5 h-full flex flex-col">
              <div class="flex justify-between items-start mb-4">
                <h3 class="font-bold text-lg text-gray-900 truncate max-w-[220px]" :title="item.planName">
                  {{ item.planName }}
                </h3>
                <el-tag :type="getStatusTagType(item.status)" class="px-2 py-1 text-sm font-medium">
                  {{ formatStatus(item.status) }}
                </el-tag>
              </div>

              <div class="flex-1 space-y-3 text-base text-gray-700">
                <p class="flex items-center">
                  <span class="text-gray-500 mr-2 text-lg">🍄</span>
                  <span class="font-medium text-gray-900">{{ item.fungusType }}</span>
                </p>
                <p class="flex items-center">
                  <span class="text-gray-500 mr-2 text-lg">📅</span>
                  <span class="font-medium">{{ formatDate(item.startTime) }} 至 {{ formatDate(item.endTime)
                  }}</span>
                </p>
                <p class="flex items-center">
                  <span class="text-gray-500 mr-2 text-lg">🌾</span>
                  <span class="font-medium">预期产量: {{ item.expectYield?.toFixed(2) || '-' }} kg</span>
                </p>
                <p class="flex items-center">
                  <span class="text-gray-500 mr-2 text-lg">💰</span>
                  <span class="font-medium">预期收益: {{ item.expectIncome?.toFixed(2) || '-' }} 元</span>
                </p>
                <div class="mt-3 pt-3 border-t border-gray-200">
                  <p class="line-clamp-3 text-gray-800 text-sm font-medium">{{ item.remark || '暂无备注' }}</p>
                </div>
              </div>

              <div class="mt-5 pt-4 border-t border-gray-200 flex justify-between items-center text-base">
                <span class="text-gray-600 font-medium">创建: {{ formatDate(item.createTime) }}</span>
                <div class="space-x-3">
                  <!-- 根据状态显示不同按钮 -->
                   <!-- 执行中显示【编辑】，跳转路由 -->
                      <el-button 
                        v-if="item.status === PlantPlanStatusEnum.EXECUTING"
                        type="primary" 
                        text 
                        class="mr-1" 
                        @click.stop="goToCurrentPlan(item.planId)"
                      >
                        <el-icon name="Edit" />
                        编辑
                      </el-button>
                      
                      <!-- 非执行中显示【查看】，打开弹窗 -->
                      <el-button 
                        v-else
                        type="info" 
                        text 
                        class="mr-1" 
                        @click.stop="openPlanDetail(item.planId)"
                      >
                        <el-icon name="View" />
                        查看
                      </el-button>
                  <el-button type="warning"text class="mr-1" @click.stop="openTimelineDialog(item)">
                    <el-icon name="ChartHistogram" />
                    更多
                  </el-button>
                  <el-button  type="danger" text class="mr-1"@click.stop="deletePlan(item)"
                    :disabled="item.status === PlantPlanStatusEnum.EXECUTING">
                    <el-icon name="Delete" />
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态提示 -->
        <div v-if="!loading && planList.length === 0"
          class="absolute inset-0 flex flex-col items-center justify-center text-gray-500">
          <el-icon name="Document" class="text-4xl mb-2" />
          <p class="text-xl font-medium mb-4">暂无种植计划</p>
          <el-button type="primary" size="large" class="px-8 py-3 text-lg" @click="openAddPlanDialog">
            <el-icon name="Plus" class="mr-2" />
            创建新计划
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <nav class="mx-auto flex w-full justify-center mt-3">
      <el-pagination v-model:page-size="pageSize" v-model:current-page="currentPage" :total="state.total"
        :page-sizes="state.pageSizes" :layout="state.layout" @size-change="fetchPlantPlans"
        @current-change="fetchPlantPlans" class="mb-3" />
    </nav>

    <!-- 计划详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="计划详情" width="650px"
      :before-close="() => detailDialogVisible = false" destroy-on-close class="custom-plan-dialog">
      <div v-if="currentPlanDetail" class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div>
            <p class="text-gray-600 mb-1 font-medium">计划名称：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ currentPlanDetail.planName }}</p>
          </div>
          <div>
            <p class="text-gray-600 mb-1 font-medium">种植菌型：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ currentPlanDetail.fungusType }}</p>
          </div>
          <div>
            <p class="text-gray-600 mb-1 font-medium">种植面积：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ currentPlanDetail.plantArea?.toFixed(2) || '-' }} ㎡</p>
          </div>
          <div>
            <p class="text-gray-600 mb-1 font-medium">计划周期：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ formatDate(currentPlanDetail.startTime) }} 至 {{
              formatDate(currentPlanDetail.endTime) }}</p>
          </div>
          <div>
            <p class="text-gray-600 mb-1 font-medium">预期产量：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ currentPlanDetail.expectYield?.toFixed(2) || '-' }} kg</p>
          </div>
          <div>
            <p class="text-gray-600 mb-1 font-medium">预期收益：</p>
            <p class="font-semibold text-gray-900 text-lg">{{ currentPlanDetail.expectIncome?.toFixed(2) || '-' }} 元</p>
          </div>
          <div class="md:col-span-2">
            <p class="text-gray-600 mb-1 font-medium">备注：</p>
            <p class="font-medium text-gray-800 whitespace-pre-wrap text-lg">{{ currentPlanDetail.remark || '暂无备注'
            }}</p>
          </div>
        </div>

        <div class="pt-4 border-t border-gray-200">
          <p class="text-gray-600 mb-1 font-medium">创建时间：{{ formatDate(currentPlanDetail.createTime) }}</p>
          <p class="text-gray-600 font-medium">更新时间：{{ formatDate(currentPlanDetail.updateTime) }}</p>
        </div>
      </div>
      <template #footer>
        <el-button text class="mr-1" @click="detailDialogVisible = false">关闭</el-button>
        <el-button text class="mr-1" type="primary" @click="openEditPlanDialog(currentPlanDetail)"
          v-if="currentPlanDetail && currentPlanDetail.status !== PlantPlanStatusEnum.EXECUTING">
          编辑
        </el-button>
      </template>
    </el-dialog>

    <!-- 计划管理弹窗 -->
    <el-dialog v-model="planDialogVisible" :title="planDialogTitle" width="650px"
      :before-close="() => planDialogVisible = false" destroy-on-close class="custom-plan-dialog">
      <el-form ref="planFormRef" :model="planForm" :rules="planFormRules" label-width="110px" class="plan-form"
        size="large">
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入计划名称" class="text-lg" />
        </el-form-item>
        <el-form-item label="种植菌型" prop="fungusType">
          <el-input v-model="planForm.fungusType" placeholder="例如：香菇、金针菇、羊肚菌" class="text-lg" />
        </el-form-item>
        <el-form-item label="种植面积(㎡)">
          <el-input-number v-model="planForm.plantArea" :min="0" :precision="2" placeholder="请输入面积"
            class="w-full text-lg" />
        </el-form-item>
        <el-form-item label="时间范围" required>
          <div class="flex items-center space-x-4">
            <el-date-picker v-model="planForm.startTime" type="date" placeholder="开始日期"
              format="YYYY-MM-DD" value-format="YYYY-MM-DD" class="flex-1 text-lg" />
            <span class="text-gray-500 text-lg font-medium">至</span>
            <el-date-picker v-model="planForm.endTime" type="date" placeholder="结束日期"
              format="YYYY-MM-DD" value-format="YYYY-MM-DD" class="flex-1 text-lg" />
          </div>
        </el-form-item>
        <el-form-item label="预期产量(kg)">
          <el-input-number v-model="planForm.expectYield" :min="0" :precision="2" placeholder="请输入预期产量"
            class="w-full text-lg" />
        </el-form-item>
        <el-form-item label="预期收益(元)">
          <el-input-number v-model="planForm.expectIncome" :min="0" :precision="2" placeholder="请输入预期收益"
            class="w-full text-lg" />
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select v-model="planForm.status" class="w-full text-lg" placeholder="请选择状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.remark" type="textarea" :rows="4" placeholder="请输入备注信息（可选）"
            class="text-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="planDialogVisible = false">取消</el-button>
        <el-button size="large" type="primary" @click="submitPlanForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 时间轴可视化弹窗 -->
    <el-dialog v-model="timelineDialogVisible" title="种植计划时间轴" width="1000px"
      :before-close="() => timelineDialogVisible = false" destroy-on-close class="custom-timeline-dialog">
      <div v-if="selectedPlanForTimeline" class="mb-6">
        <h3 class="text-xl font-bold text-gray-900 mb-1">{{ selectedPlanForTimeline.planName }}</h3>
        <p class="text-gray-600">菌型: {{ selectedPlanForTimeline.fungusType }} | 周期: {{
          formatDate(selectedPlanForTimeline.startTime) }} 至 {{ formatDate(selectedPlanForTimeline.endTime) }}</p>
      </div>
      
      <div class="timeline-container h-96 overflow-y-auto border rounded-lg p-4 bg-gray-50">
  <!-- 这里将放置时间轴可视化组件 -->
      <div v-if="timelineNodes.length === 0" class="text-center py-10 text-gray-500">
        <el-icon name="Document" class="text-4xl mb-2" />
        <p>暂无种植节点数据</p>
      </div>
      <div v-else class="space-y-6">
        <div v-for="(node, index) in timelineNodes" :key="node.nodeId" class="timeline-node">
          <div class="flex items-center mb-2">
            <div class="w-3 h-3 rounded-full bg-primary mr-2"></div>
            <span class="font-medium text-lg">{{ node.nodeName }} ({{ node.nodeType }})</span>
            <span class="ml-3 text-gray-600 text-base">计划: {{ formatDate(node.planTime) }} | 
              <span :class="{
                'text-green-600': Number(node.status) === 2,
                'text-yellow-600': Number(node.status) === 1,
                'text-red-600': Number(node.status) === 3
              }">
                {{ 
                  Number(node.status) === 2 ? '已完成' : 
                  Number(node.status) === 3 ? '逾期' : '未完成' 
                }}
              </span>
            </span>
          </div>
          <div class="ml-5 pl-2 border-l-2 border-gray-300">
            <!-- 节点周期内的数据点 -->
            <div v-if="getNodeDataPoints(node).length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 mt-2">
              <div v-for="(data, dataIndex) in getNodeDataPoints(node)" :key="dataIndex"
                class="bg-white p-3 rounded-lg shadow-sm hover:shadow-md transition cursor-pointer"
                @mouseenter="(event) => showDataTooltip(data, event)" 
                @mouseleave="hideDataTooltip">
                <p class="font-medium text-blue-600">{{ formatDate(data.recordTime) }}</p>
                <div class="grid grid-cols-2 gap-2 mt-1 text-sm">
                  <div v-if="data.envTemp">
                    <span class="text-gray-500">温度:</span>
                    <span class="font-medium">{{ data.envTemp }}℃</span>
                  </div>
                  <div v-if="data.envHumidity">
                    <span class="text-gray-500">湿度:</span>
                    <span class="font-medium">{{ data.envHumidity }}%</span>
                  </div>
                  <div v-if="data.co2Concentration">
                    <span class="text-gray-500">CO₂:</span>
                    <span class="font-medium">{{ data.co2Concentration }}‰</span>
                  </div>
                  <div v-if="data.growthStatus">
                    <span class="text-gray-500">状态:</span>
                    <span class="font-medium" :class="getGrowthStatusColor(data.growthStatus)">
                      {{ getGrowthStatusText(data.growthStatus) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-gray-500 italic mt-2 ml-2">
              该节点周期内暂无种植数据记录
            </div>
          </div>
        </div>
      </div>
    </div>

      
      <div class="text-sm text-gray-500 mt-4">
        <p>注：数据点显示在对应节点的时间周期内，鼠标悬停可查看详细信息</p>
        <p>节点状态：{{ nodeStatusLegend }}</p>
      </div>
    </el-dialog>

    <!-- 导入计划对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入种植计划" width="500px"
      :before-close="() => importDialogVisible = false" destroy-on-close>
      <div class="text-center py-8">
        <div 
            class="border-2 border-dashed border-gray-300 rounded-lg p-8 mb-6 cursor-pointer hover:border-primary"
            @click="importInput?.click()"
          >
            <el-icon name="UploadFilled" class="text-4xl text-gray-400 mb-3" />
            <p class="text-gray-600 mb-1">点击上传Excel文件或拖拽到此处</p>
            <p class="text-xs text-gray-500">支持.xlsx格式，包含4个sheet页（计划基础信息、种植节点、种植数据、采收成果）</p>
            <input 
              type="file" 
              ref="importInput" 
              class="hidden" 
              accept=".xlsx, .xls" 
              @change="handleFileChange" 
            />
          </div>
        
        <div v-if="importFile" class="mt-4">
          <el-tag type="success" class="text-lg px-3 py-1">
            <el-icon name="Document" class="mr-1" />
            {{ importFile.name }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button size="large" @click="importDialogVisible = false">取消</el-button>
        <el-button size="large" type="primary" :loading="importLoading" @click="importPlantPlans">
          确认导入
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 导出计划对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出种植计划" width="500px"
      :before-close="() => exportDialogVisible = false" destroy-on-close>
      <div v-if="selectedExportPlan" class="space-y-4">
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p class="text-blue-800 font-medium">将导出计划: <span class="text-lg">{{ selectedExportPlan.planName }}</span></p>
          <p class="text-blue-700 mt-1">包含: 计划基础信息、种植节点、种植数据、采收成果</p>
        </div>
        
        <div class="space-y-2">
          <p class="font-medium text-gray-700">导出选项:</p>
          <el-checkbox v-model="exportOptions.basicInfo" disabled>计划基础信息</el-checkbox>
          <el-checkbox v-model="exportOptions.nodes" disabled>种植节点数据</el-checkbox>
          <el-checkbox v-model="exportOptions.data" disabled>种植记录数据</el-checkbox>
          <el-checkbox v-model="exportOptions.result" disabled>采收成果数据</el-checkbox>
        </div>

        
        <div class="text-sm text-gray-500 mt-2">
          <p>文件格式: Excel (.xlsx)</p>
          <p>文件命名: {{ selectedExportPlan.planName }}_种植计划.xlsx</p>
        </div>
      </div>
      <template #footer>
        <el-button size="large" @click="exportDialogVisible = false">取消</el-button>
        <el-button size="large" type="primary" :loading="exportLoading" @click="exportPlantPlan">
          确认导出
        </el-button>
      </template>
    </el-dialog>
  </div>
  <div 
  v-show="tooltipVisible" 
  :style="{ 
    left: `${tooltipPosition.x}px`, 
    top: `${tooltipPosition.y}px`,
    opacity: tooltipVisible ? 1 : 0
  }"
  class="tooltip-container" 
>
  <div v-html="tooltipContent"></div>
</div>
</template>

<style scoped lang="less">
// 与菌种页面完全一致的卡片样式
.custom-plan-dialog,
.custom-timeline-dialog {
  .el-dialog__header {
    background-color: #f9fafb;
    border-bottom: 1px solid #ebeef5;
    font-weight: 600;
    font-size: 20px;
    color: #303133;
  }
}

:deep(.el-dialog__footer) {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-tag) {
  margin-left: 4px;
  height: 26px;
  line-height: 24px;
  font-weight: 500;
}

:deep(.el-button) {
  font-weight: 500;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-1px);
  }
  // 2. Primary 类型按钮的禁用样式（重点适配你的场景）
  &--primary.is-disabled {
    // 主题色浅版背景（示例：假设主题色是 #409eff，用 rgba 降低饱和度/亮度）
    background-color: rgba(178, 101, 199, 0.4) !important; 
    // 主题色浅版边框（如果按钮有边框）
    border-color: rgba(178, 101, 199, 0.4) !important;
    // 禁用时取消 hover 位移（避免视觉冲突）
    &:hover {
      transform: none;
    }
  }

  // 3. Primary + Text 类型按钮的禁用样式（适配你的「批量删除」按钮）
  &--primary.is-text.is-disabled {
    background-color: transparent !important; // 保持透明背景
    color: rgba(178, 101, 199, 0.4) !important; // 仅文字变浅
    border: none !important;
  }
}

.timeline-container {
  position: relative;
  
  .timeline-node {
    position: relative;
    padding-bottom: 24px;
    
    &:last-child {
      padding-bottom: 0;
    }
    
    &:before {
      content: '';
      position: absolute;
      left: 6px;
      top: 11px;
      bottom: -16px;
      width: 2px;
      background-color: #e2e8f0;
    }
    
    &:last-child:before {
      display: none;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .grid.grid-cols-1.sm\:grid-cols-2.lg\:grid-cols-3 {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 640px) {
  .el-form-item__content {
    width: 100%;
  }
  
  .el-date-editor {
    width: 100%;
  }
  
  // 调整移动端的批量操作区域
  .flex.justify-end.mb-4 {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .flex.justify-end.mb-4 .el-checkbox {
    margin-bottom: 8px;
  }
}

// Tooltip样式（修复语法错误 + 提升层级 + 匹配DOM类名）
.tooltip-container {
  position: fixed;
  z-index: 9999 !important; // 高于Element弹窗的2000
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px 16px;
  max-width: 350px;
  font-size: 14px;
  line-height: 1.5;
  pointer-events: none;
  transition: opacity 0.2s ease;
  opacity: 0; // 初始透明，v-show触发时变为1
  color: #333;
  box-sizing: border-box;

  :deep(.tooltip-title) {
    font-weight: 600;
    color: #1890ff;
    margin-bottom: 8px;
    font-size: 15px;
  }

  :deep(.tooltip-content) {
    display: flex;
    flex-direction: column;
    gap: 6px;

    .label {
      font-weight: 500;
      color: #666;
      width: 50px;
      display: inline-block;
    }

    span:not(.label) {
      color: #444;
    }

    .status-tag {
      display: inline-block;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
      margin-left: 4px;
    }
  }
}
</style>
