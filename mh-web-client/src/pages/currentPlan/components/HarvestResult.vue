<script setup lang="ts">
// 1. 补充缺失的 computed 导入（关键修复）
import { ref, onMounted, reactive, computed } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import type {
  PlantResultVO,
  PlantResultAddDTO,
  PlantResultUpdateDTO
} from '@/api/interface'
import {
  getPlantResultByPlanId,
  addPlantResult,
  updatePlantResult,
  deletePlantResult
} from '@/api/system'

const props = defineProps({
  planId: {
    type: Number,
    required: true,
    validator: (val: number) => val > 0 // 校验planId有效性
  }
})

// 成果数据
const resultInfo = ref<PlantResultVO | null>(null)
// 加载状态
const loading = ref(false)
// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加采收成果')
// 表单数据
const resultForm = reactive({
  resultId: 0,
  planId: props.planId,
  harvestTime: '',
  actualYield: undefined,
  yieldGrade: '',
  actualIncome: undefined,
  lossReason: '',
  summary: ''
})
// 表单规则
const rules = {
  harvestTime: [{ required: true, message: '请选择采收时间', trigger: 'blur' }],
  actualYield: [{ required: true, message: '请输入实际产量', trigger: 'blur' }]
}
// 编辑模式
const isEditMode = ref(false)
// 临时存储计划基本信息
const planInfo = ref({
  expectYield: 0,
  expectIncome: 0
})

// 产量等级选项
const yieldGradeOptions = [
  { value: '特级', label: '特级' },
  { value: '一级', label: '一级' },
  { value: '普通', label: '普通' }
]

// 获取成果数据
const fetchResultData = async () => {
  try {
    loading.value = true
    const res = await getPlantResultByPlanId(props.planId)
    if (res.code === 0 && res.data) {
      // 先进行类型断言
      const resultData = res.data as PlantResultVO
      resultInfo.value = resultData
      
      // 安全访问属性，使用可选链和空值合并
      planInfo.value = {
        expectYield: resultData.expectYield ?? 0,
        expectIncome: resultData.expectIncome ?? 0
      }
    } else {
      resultInfo.value = null
    }
  } catch {
    resultInfo.value = null
    ElNotification.error('获取成果数据失败')
  } finally {
    loading.value = false
  }
}

// 打开添加/编辑对话框
const openDialog = () => {
  if (resultInfo.value) {
    dialogTitle.value = '编辑采收成果'
    isEditMode.value = true
    Object.assign(resultForm, {
      resultId: resultInfo.value.resultId,
      planId: props.planId,
      harvestTime: resultInfo.value.harvestTime?.split(' ')[0] || '',
      actualYield: resultInfo.value.actualYield,
      yieldGrade: resultInfo.value.yieldGrade || '',
      actualIncome: resultInfo.value.actualIncome,
      lossReason: resultInfo.value.lossReason || '',
      summary: resultInfo.value.summary || ''
    })
  } else {
    dialogTitle.value = '添加采收成果'
    isEditMode.value = false
    const today = new Date().toISOString().split('T')[0]
    Object.assign(resultForm, {
      resultId: 0,
      planId: props.planId,
      harvestTime: today,
      actualYield: undefined,
      yieldGrade: '',
      actualIncome: undefined,
      lossReason: '',
      summary: ''
    })
  }
  dialogVisible.value = true
}

// 添加在 setup 脚本中
const getGradeTagType = (grade?: string) => {
  const typeMap: Record<string, 'success' | 'warning' | 'info' | 'primary' | 'danger'> = {
    '特级': 'success',
    '一级': 'warning',
    '普通': 'info'
  }
  // 确保返回值是 Element Plus 支持的类型
  return typeMap[grade || ''] || 'info' // 默认使用 info 类型
}
const formRef = ref<FormInstance | null>(null)
// 提交表单
const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
    
    // 处理日期格式
    const submitData = {
      ...resultForm,
      harvestTime: resultForm.harvestTime ? `${resultForm.harvestTime} ${new Date().toLocaleTimeString()}` : undefined
    }
    
    if (isEditMode.value) {
      // 更新成果
      const updateData: PlantResultUpdateDTO = {
        resultId: resultForm.resultId,
        harvestTime: resultForm.harvestTime ? `${resultForm.harvestTime} ${new Date().toLocaleTimeString()}` : undefined,
        actualYield: resultForm.actualYield,
        yieldGrade: resultForm.yieldGrade,
        actualIncome: resultForm.actualIncome,
        lossReason: resultForm.lossReason,
        summary: resultForm.summary
      }
      const res = await updatePlantResult(updateData)
      if (res.code === 0) {
        ElMessage.success('成果更新成功')
        fetchResultData()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 添加成果
      const addData: PlantResultAddDTO = {
        planId: resultForm.planId,
        harvestTime: `${resultForm.harvestTime} ${new Date().toLocaleTimeString()}`,
        actualYield: resultForm.actualYield!,
        yieldGrade: resultForm.yieldGrade,
        actualIncome: resultForm.actualIncome,
        lossReason: resultForm.lossReason,
        summary: resultForm.summary
      }
      const res = await addPlantResult(addData)
      if (res.code === 0) {
        ElMessage.success('成果添加成功')
        fetchResultData()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
    dialogVisible.value = false
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 删除成果
const deleteResult = () => {
  ElMessageBox.confirm(`确定要删除采收成果记录吗？删除后无法恢复。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (!resultInfo.value) return
      const res = await deletePlantResult(resultInfo.value.resultId)
      if (res.code === 0) {
        ElMessage.success('成果删除成功')
        resultInfo.value = null
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除操作失败')
    }
  }).catch(() => {})
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

// 计算产量对比
const yieldCompare = computed(() => {
  if (!resultInfo.value || !resultInfo.value.actualYield || !planInfo.value.expectYield) return '-'
  
  const diff = resultInfo.value.actualYield - planInfo.value.expectYield
  if (Math.abs(diff) < 0.01) return '符合预期'
  return diff > 0 ? `超出 ${diff.toFixed(2)} kg` : `不足 ${Math.abs(diff).toFixed(2)} kg`
})

// 计算收益对比
const incomeCompare = computed(() => {
  if (!resultInfo.value || !resultInfo.value.actualIncome || !planInfo.value.expectIncome) return '-'
  
  const diff = resultInfo.value.actualIncome - planInfo.value.expectIncome
  if (Math.abs(diff) < 0.01) return '符合预期'
  return diff > 0 ? `超出 ${diff.toFixed(2)} 元` : `不足 ${Math.abs(diff).toFixed(2)} 元`
})
watch(
  () => props.planId,
  () => {
    fetchResultData()
  },
  { immediate: true }
)
onMounted(() => {
  fetchResultData()
})

// 暴露刷新方法
defineExpose({ refreshData: fetchResultData })
</script>

<template>
  <div class="result-container">
    <!-- 2. 卡片样式优化：增加margin，header宽度控制，按钮尺寸放大 -->
    <el-card class="mb-8">
      <template #header>
        <div class="flex items-center justify-between w-full max-w-[1800px] mx-auto px-4">
          <span class="text-xl font-medium">采收成果</span>
          <div class="flex gap-3"> <!-- 按钮间距增加 -->
            <el-button 
              v-if="!resultInfo" 
              type="primary" 
              @click="openDialog"
              size="large"
            >
              <el-icon name="Plus" size="18" />
              添加成果
            </el-button>
            <el-button 
              v-else 
              type="primary" 
              @click="openDialog"
              size="large"
            >
              编辑成果
            </el-button>
            <el-button 
              v-if="resultInfo" 
              type="warning" 
              @click="deleteResult"
              size="large"
            >
              删除成果
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 3. 成果信息展示区域：字体放大，间距增加，标签样式优化 -->
      <div v-if="resultInfo" class="grid grid-cols-1 md:grid-cols-2 gap-8 px-4">
        <div class="info-wrapper">
          <div class="info-item mb-4">
            <span class="info-label">采收时间：</span>
            <span class="info-value text-lg">{{ formatDate(resultInfo.harvestTime) }}</span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">实际产量：</span>
            <span class="info-value text-lg font-medium">
              {{ resultInfo.actualYield?.toFixed(2) }} kg
            </span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">产量等级：</span>
            <el-tag :type="getGradeTagType(resultInfo.yieldGrade)" size="large">
                {{ resultInfo.yieldGrade || '未评级' }}
            </el-tag>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">预期产量：</span>
            <span class="info-value text-lg">{{ planInfo.expectYield.toFixed(2) }} kg</span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">产量对比：</span>
            <span class="info-value text-lg" :class="{
              'text-green-600 font-medium': yieldCompare.includes('超出'),
              'text-red-600 font-medium': yieldCompare.includes('不足'),
              'text-gray-600': yieldCompare.includes('符合预期')
            }">
              {{ yieldCompare }}
            </span>
          </div>
        </div>
        <div class="info-wrapper">
          <div class="info-item mb-4">
            <span class="info-label">实际收益：</span>
            <span class="info-value text-lg font-medium">
              {{ resultInfo.actualIncome?.toFixed(2) }} 元
            </span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">减产原因：</span>
            <span class="info-value text-lg">{{ resultInfo.lossReason || '无' }}</span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">预期收益：</span>
            <span class="info-value text-lg">{{ planInfo.expectIncome.toFixed(2) }} 元</span>
          </div>
          <div class="info-item mb-4">
            <span class="info-label">收益对比：</span>
            <span class="info-value text-lg" :class="{
              'text-green-600 font-medium': incomeCompare.includes('超出'),
              'text-red-600 font-medium': incomeCompare.includes('不足'),
              'text-gray-600': incomeCompare.includes('符合预期')
            }">
              {{ incomeCompare }}
            </span>
          </div>
        </div>
        <div class="col-span-full mt-8 pt-6 border-t border-gray-200 px-4">
          <div class="info-item">
            <span class="info-label block mb-2">计划总结：</span>
            <div class="info-value whitespace-pre-wrap text-lg leading-relaxed">
              {{ resultInfo.summary || '暂无总结' }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 4. 空状态优化：图标放大，文字放大，按钮尺寸调整 -->
      <div v-else class="empty-state">
        <el-icon name="Trophy" class="text-5xl text-gray-400 mb-3" />
        <p class="text-lg text-gray-500 mb-6">暂未记录采收成果</p>
        <el-button type="primary" size="large" @click="openDialog">添加采收成果</el-button>
      </div>
    </el-card>

    <!-- 5. 弹窗优化：宽度放大，表单尺寸统一，按钮样式调整 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      class="custom-result-dialog"
      size="large"
    >
      <el-form 
        ref="formRef" 
        :model="resultForm" 
        :rules="rules" 
        label-width="120px"
        class="result-form"
        size="large"
      >
        <el-form-item label="采收时间" prop="harvestTime" class="mb-4">
          <el-date-picker
            v-model="resultForm.harvestTime"
            type="date"
            placeholder="选择采收日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="实际产量(kg)" prop="actualYield" class="mb-4">
          <el-input-number 
            v-model="resultForm.actualYield" 
            :min="0" 
            :precision="2" 
            placeholder="请输入实际产量"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="产量品质" class="mb-4">
          <el-select v-model="resultForm.yieldGrade" class="w-full" placeholder="请选择品质">
            <el-option
              v-for="item in yieldGradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实际收益(元)" class="mb-4">
          <el-input-number 
            v-model="resultForm.actualIncome" 
            :min="0" 
            :precision="2" 
            placeholder="请输入实际收益（可选）"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="减产原因" class="mb-4">
          <el-input
            v-model="resultForm.lossReason"
            type="textarea"
            :rows="3"
            placeholder="请输入减产或损失原因（可选）"
          />
        </el-form-item>
        <el-form-item label="计划总结">
          <el-input
            v-model="resultForm.summary"
            type="textarea"
            :rows="4"
            placeholder="请输入计划总结（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="dialogVisible = false">取消</el-button>
        <el-button size="large" type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 6. 容器样式统一：和前两个组件保持一致 */
.result-container {
  width: 100%;
  max-width: 1800px; /* 和种植节点组件一致 */
  margin: 0 auto;
  border-radius: 12px;
  padding: 20px;
  font-size: 18px;
  line-height: 1.8;
}

/* 卡片样式强化 */
:deep(.el-card) {
  display: block !important;
  width: 100% !important;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.08) !important;
  margin-bottom: 24px;
}

/* 信息项样式优化 */
.info-wrapper {
  padding: 8px 0;
}

.info-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.info-label {
  flex: 0 0 120px; /* 标签宽度增加，更协调 */
  font-weight: 500;
  color: var(--el-text-color-regular);
  font-size: 16px;
}

.info-value {
  flex: 1;
  color: var(--el-text-color-primary);
  font-size: 16px;
}

/* 空状态样式统一 */
.empty-state {
  text-align: center;
  padding: 60px 0; /* 和前两个组件一致 */
}

/* 弹窗样式统一 */
.custom-result-dialog {
  font-size: 16px;
  
  .el-dialog__header {
    background-color: #f9fafb;
    border-bottom: 1px solid #ebeef5;
    font-weight: 600;
    font-size: 20px;
    color: #303133;
    padding: 20px 24px;
  }
  
  .el-dialog__body {
    padding: 24px;
    font-size: 16px;
  }
}

/* 弹窗底部按钮栏 */
:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

/* 表单样式统一 */
:deep(.el-form-item) {
  margin-bottom: 20px;
  
  .el-form-item__label {
    font-size: 16px;
    font-weight: 500;
  }
  
  .el-input, .el-select, .el-date-picker, .el-input-number {
    font-size: 16px;
  }
  
  .el-textarea {
    font-size: 16px;
  }
}

/* 按钮样式统一 */
:deep(.el-button) {
  font-size: 16px;
  padding: 12px 20px;
}

/* 状态标签样式放大 */
:deep(.el-tag) {
  height: 32px;
  line-height: 30px;
  font-size: 15px;
  padding: 0 12px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-label {
    flex: 0 0 auto;
    margin-bottom: 4px;
  }
}
</style>