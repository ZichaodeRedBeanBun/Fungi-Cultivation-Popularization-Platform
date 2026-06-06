<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useRenderIcon } from "@/components/icon/ReIcon/src/hooks";
import type { FormInstance } from 'element-plus'
import EditPen from "@iconify-icons/ep/edit-pen";
import{
  PlantPlanVO,
  PlantPlanUpdateDTO,
  PlantPlanStatusEnum,
  PlantPlanStatusDescMap
} from '@/api/interface'
import {
  getPlantPlanById,
  updatePlantPlan
} from '@/api/system'

const props = defineProps({
  planId: {
    type: Number,
    required: true,
    validator: (val: number) => val > 0 // 校验planId有效性
  }
})

// 表单数据
const planForm = reactive({
  planId: 0,
  planName: '',
  fungusType: '',
  plantArea: undefined,
  startTime: '',
  endTime: '',
  expectYield: undefined,
  expectIncome: undefined,
  status: PlantPlanStatusEnum.EXECUTING,
  remark: ''
})

// 表单规则
const rules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  fungusType: [{ required: true, message: '请输入菌型', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
}

// 加载状态
const loading = ref(false)
// 是否编辑模式
const isEditMode = ref(false)

// 获取计划详情
const fetchPlanDetail = async () => {
  try {
    loading.value = true
    const res = await getPlantPlanById(props.planId)
    if (res.code === 0 && res.data) {
      const data = res.data as PlantPlanVO
      Object.assign(planForm, {
        planId: data.planId,
        planName: data.planName,
        fungusType: data.fungusType,
        plantArea: data.plantArea,
        startTime: data.startTime?.split(' ')[0] || '',
        endTime: data.endTime?.split(' ')[0] || '',
        expectYield: data.expectYield,
        expectIncome: data.expectIncome,
        status: data.status || PlantPlanStatusEnum.EXECUTING,
        remark: data.remark || ''
      })
    } else {
      ElNotification.error('获取计划详情失败')
    }
  } catch {
    ElNotification.error('获取计划详情失败')
  } finally {
    loading.value = false
  }
}
const formRef = ref<FormInstance | null>(null)
// 提交表单
const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
    
    const updateData: PlantPlanUpdateDTO = {
      planId: planForm.planId,
      planName: planForm.planName,
      fungusType: planForm.fungusType,
      plantArea: planForm.plantArea,
      startTime: planForm.startTime ? `${planForm.startTime} 00:00:00` : undefined,
      endTime: planForm.endTime ? `${planForm.endTime} 23:59:59` : undefined,
      expectYield: planForm.expectYield,
      expectIncome: planForm.expectIncome,
      status: planForm.status,
      remark: planForm.remark
    }
    
    const res = await updatePlantPlan(updateData)
    if (res.code === 0) {
      ElMessage.success('计划信息更新成功')
      isEditMode.value = false
      fetchPlanDetail()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 切换编辑模式
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    fetchPlanDetail() // 退出编辑时刷新数据
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

// 状态选项
const statusOptions = [
  { value: PlantPlanStatusEnum.EXECUTING, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.EXECUTING] },
  { value: PlantPlanStatusEnum.COMPLETED, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.COMPLETED] },
  { value: PlantPlanStatusEnum.PAUSED, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.PAUSED] },
  { value: PlantPlanStatusEnum.INVALID, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.INVALID] }
]
watch(
  () => props.planId,
  () => {
    fetchPlanDetail()
  },
  { immediate: true }
)
onMounted(() => {
  fetchPlanDetail()
})

// 暴露刷新方法
defineExpose({ refreshData: fetchPlanDetail })
</script>

<template>
  <div class="user-container">
    <el-form 
      ref="formRef" 
      :model="planForm" 
      :rules="rules" 
      label-width="160px" 
      class="basic-info-form"
      v-loading="loading"
    >
      <div class="section">
        <div class="section-title">计划名称</div>
        <el-form-item prop="planName">
          <el-input 
            v-model="planForm.planName" 
            placeholder="请输入计划名称" 
            :disabled="!isEditMode" 
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">种植菌型</div>
        <el-form-item prop="fungusType">
          <el-input 
            v-model="planForm.fungusType" 
            placeholder="例如：香菇、金针菇、羊肚菌" 
            :disabled="!isEditMode" 
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">种植面积(㎡)</div>
        <el-form-item>
          <el-input-number 
            v-model="planForm.plantArea" 
            :min="0" 
            :precision="2" 
            placeholder="请输入面积" 
            class="w-full"
            :disabled="!isEditMode"
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">计划时间</div>
        <div class="flex space-x-4 items-end">
          <el-form-item prop="startTime" class="flex-1">
            <el-date-picker
              v-model="planForm.startTime"
              type="date"
              placeholder="开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="w-full"
              :disabled="!isEditMode"
            />
          </el-form-item>
          <el-form-item prop="endTime" class="flex-1">
            <el-date-picker
              v-model="planForm.endTime"
              type="date"
              placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="w-full"
              :disabled="!isEditMode"
            />
          </el-form-item>
        </div>
      </div>

      <div class="section">
        <div class="section-title">预期产量(kg)</div>
        <el-form-item>
          <el-input-number 
            v-model="planForm.expectYield" 
            :min="0" 
            :precision="2" 
            placeholder="请输入预期产量" 
            class="w-full"
            :disabled="!isEditMode"
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">预期收益(元)</div>
        <el-form-item>
          <el-input-number 
            v-model="planForm.expectIncome" 
            :min="0" 
            :precision="2" 
            placeholder="请输入预期收益" 
            class="w-full"
            :disabled="!isEditMode"
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">计划状态</div>
        <el-form-item>
          <el-select 
            v-model="planForm.status" 
            class="w-full" 
            placeholder="请选择状态" 
            :disabled="!isEditMode"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">备注</div>
        <el-form-item>
          <el-input 
            v-model="planForm.remark" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入备注信息" 
            maxlength="200"
            show-word-limit
            :disabled="!isEditMode"
          />
        </el-form-item>
      </div>

      <el-form-item class="button-group">
        <div class="flex justify-end w-full">
          <el-button 
            size="default"
            v-if="!isEditMode" 
            type="primary" 
            :icon="useRenderIcon(EditPen)"
            @click="toggleEditMode"
            class="submit-btn"
          >
            编辑信息
          </el-button>
          <div v-else class="flex space-x-3">
            <el-button type="info" @click="toggleEditMode" class="submit-btn">
              取消
            </el-button>
            <el-button type="primary" @click="submitForm" class="submit-btn">
              保存修改
            </el-button>
          </div>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.user-container {
  max-width: 1800px;
  margin: 0 auto;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 40px 50px 20px;
}
:deep(.el-form-item) {
  margin-bottom: 30px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;

}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.submit-btn {
  border-radius: 8px;
  width: 140px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-select__wrapper) {
  border-radius: 8px;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

.section {
  margin-bottom: 28px;
}

.section-title {
  margin-bottom: 18px;
  color: var(--el-text-color-regular);
  font-size: 18px;
  font-weight: 500;
}

.button-group {
  margin-top: 40px;
}
</style>
