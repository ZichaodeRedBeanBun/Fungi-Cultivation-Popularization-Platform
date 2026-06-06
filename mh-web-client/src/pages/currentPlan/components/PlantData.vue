<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import{
  PlantDataVO,
  PlantDataAddDTO,
  PlantDataUpdateDTO,
  PlantGrowthStatusEnum,
  PlantGrowthStatusDescMap,
  PlantGrowthStatusIdMap
} from '@/api/interface'
import {
  getPlantDataPage,
  addPlantData,
  updatePlantData,
  deletePlantData
} from '@/api/system'

const props = defineProps<{ planId: number }>()

// 数据列表
const dataList = ref<PlantDataVO[]>([])
// 加载状态
const loading = ref(false)
// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加种植数据')
// 表单数据
const dataForm = reactive({
  dataId: 0,
  planId: props.planId,
  recordTime: '',
  envTemp: undefined,
  envHumidity: undefined,
  co2Concentration: undefined,
  farmingOper: '',
  growthStatus: '',
  diseaseStatus: '',
  inputCost: undefined
})
// 表单规则
const rules = {
  recordTime: [{ required: true, message: '请选择记录时间', trigger: 'blur' }]
}
// 编辑模式
const isEditMode = ref(false)
// 分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 生长状态选项
const growthStatusOptions = [
  { value: '1', label: PlantGrowthStatusDescMap[PlantGrowthStatusEnum.EXCELLENT] },
  { value: '2', label: PlantGrowthStatusDescMap[PlantGrowthStatusEnum.GOOD] },
  { value: '3', label: PlantGrowthStatusDescMap[PlantGrowthStatusEnum.POOR] }
]

// 获取数据列表
const fetchDataList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      planId: props.planId
    }
    const res = await getPlantDataPage(params)
    if (res.code === 0 && res.data) {
      dataList.value = res.data.items || []
      pagination.total = res.data.total || 0
    } else {
      ElNotification.error('获取数据列表失败')
      dataList.value = []
      pagination.total = 0
    }
  } catch {
    ElNotification.error('获取数据列表失败')
    dataList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 打开添加数据对话框
const openAddDialog = () => {
  dialogTitle.value = '添加种植数据'
  isEditMode.value = false
  const today = new Date().toISOString().split('T')[0]
  Object.assign(dataForm, {
    dataId: 0,
    planId: props.planId,
    recordTime: today,
    envTemp: undefined,
    envHumidity: undefined,
    co2Concentration: undefined,
    farmingOper: '',
    growthStatus: '',
    diseaseStatus: '',
    inputCost: undefined
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (data: PlantDataVO) => {
  dialogTitle.value = '编辑种植数据'
  isEditMode.value = true
  Object.assign(dataForm, {
    dataId: data.dataId,
    planId: props.planId,
    recordTime: data.recordTime?.split(' ')[0] || '',
    envTemp: data.envTemp,
    envHumidity: data.envHumidity,
    co2Concentration: data.co2Concentration,
    farmingOper: data.farmingOper || '',
    growthStatus: data.growthStatus ? String(data.growthStatus) : '',
    diseaseStatus: data.diseaseStatus || '',
    inputCost: data.inputCost
  })
  dialogVisible.value = true
}
const formRef = ref<FormInstance | null>(null)
// 提交表单
const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
    
    // 处理日期格式
    const submitData = {
      ...dataForm,
      recordTime: dataForm.recordTime ? `${dataForm.recordTime} ${new Date().toLocaleTimeString()}` : undefined,
      growthStatus: dataForm.growthStatus ? Number(dataForm.growthStatus) : undefined
    }
    
    if (isEditMode.value) {
        let growthStatusValue: PlantGrowthStatusEnum | undefined = undefined
      if (dataForm.growthStatus) {
        const statusId = Number(dataForm.growthStatus)
        growthStatusValue = PlantGrowthStatusIdMap[statusId as keyof typeof PlantGrowthStatusIdMap]
      }
      // 更新数据
      const updateData: PlantDataUpdateDTO = {
        dataId: dataForm.dataId,
        recordTime: dataForm.recordTime ? `${dataForm.recordTime} ${new Date().toLocaleTimeString()}` : undefined,
        envTemp: dataForm.envTemp,
        envHumidity: dataForm.envHumidity,
        co2Concentration: dataForm.co2Concentration,
        farmingOper: dataForm.farmingOper,
        growthStatus: growthStatusValue,
        diseaseStatus: dataForm.diseaseStatus,
        inputCost: dataForm.inputCost
      }
      const res = await updatePlantData(updateData)
      if (res.code === 0) {
        ElMessage.success('数据更新成功')
        fetchDataList()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
        let growthStatusValue: PlantGrowthStatusEnum | undefined = undefined
      if (dataForm.growthStatus) {
        const statusId = Number(dataForm.growthStatus)
        growthStatusValue = PlantGrowthStatusIdMap[statusId as keyof typeof PlantGrowthStatusIdMap]
      }
      // 添加数据
      const addData: PlantDataAddDTO = {
        planId: dataForm.planId,
        recordTime: dataForm.recordTime ? `${dataForm.recordTime} ${new Date().toLocaleTimeString()}` : undefined,
        envTemp: dataForm.envTemp,
        envHumidity: dataForm.envHumidity,
        co2Concentration: dataForm.co2Concentration,
        farmingOper: dataForm.farmingOper,
        growthStatus: growthStatusValue,
        diseaseStatus: dataForm.diseaseStatus,
        inputCost: dataForm.inputCost
      }
      const res = await addPlantData(addData)
      if (res.code === 0) {
        ElMessage.success('数据添加成功')
        fetchDataList()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
    dialogVisible.value = false
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 删除数据
const deleteData = (data: PlantDataVO) => {
  ElMessageBox.confirm(`确定要删除这条种植数据吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePlantData(data.dataId)
      if (res.code === 0) {
        ElMessage.success('数据删除成功')
        fetchDataList()
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

// 格式化生长状态
const formatGrowthStatus = (status?: number) => {
  if (!status) return '-'
  const statusMap: Record<number, string> = {
    1: '优',
    2: '良',
    3: '差'
  }
  return statusMap[status] || '-'
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchDataList()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchDataList()
}

onMounted(() => {
  fetchDataList()
})

// 暴露刷新方法
defineExpose({ refreshData: fetchDataList })
</script>

<template>
  <div class="data-container">
    <!-- 1. 调整卡片margin，header加宽度控制 -->
    <el-card class="mb-12">
      <template #header>
        <div class="flex items-center justify-between w-full max-w-[2200px] mx-auto px-4">
          <span class="text-xl font-medium">种植数据记录</span>
          <el-button type="primary" size="large" @click="openAddDialog">
            <el-icon name="Plus" size="18" />
            添加记录
          </el-button>
        </div>
      </template>
      
      <!-- 2. 表格加size="large"、行高，调整列宽 -->
      <el-table
        :data="dataList"
        v-loading="loading"
        border
        class="w-full"
        stripe
        size="large"
        :row-height="50"
      >
        <el-table-column prop="recordTime" label="记录时间" width="180" />
        <el-table-column prop="envTemp" label="温度(℃)" width="100" align="center" />
        <el-table-column prop="envHumidity" label="湿度(%)" width="100" align="center" />
        <el-table-column prop="co2Concentration" label="CO₂(‰)" width="120" align="center" />
        <el-table-column prop="growthStatus" label="生长状态" width="120">
          <template #default="{ row }">
            <el-tag :type="{
              1: 'success',
              2: 'warning',
              3: 'danger'
            }[row.growthStatus || 0]" size="large">
              {{ formatGrowthStatus(row.growthStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="farmingOper" label="农事操作" min-width="220" show-overflow-tooltip />
        <el-table-column prop="inputCost" label="投入(元)" width="150" align="right">
          <template #default="{ row }">
            {{ row.inputCost?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="default" type="warning" link @click="openEditDialog(row)">
              编辑
            </el-button>
            <!-- 3. 增加删除按钮间距 -->
            <el-button style="margin-left: 20px;" size="default" type="danger" link @click="deleteData(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 4. 分页调整margin，适配整体 -->
      <div class="mt-6 flex justify-center">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[5, 10, 15, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          size="large"
        />
      </div>
      
      <!-- 5. 空状态放大图标和文字 -->
      <div v-if="dataList.length === 0 && !loading" class="empty-state">
        <el-icon name="Document" class="text-5xl text-gray-400 mb-3" />
        <p class="text-lg text-gray-500">暂无种植数据，请添加数据记录</p>
      </div>
    </el-card>

    <!-- 6. 弹窗调整宽度、表单尺寸 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      class="custom-data-dialog"
      size="large"
    >
      <el-form 
        ref="formRef" 
        :model="dataForm" 
        :rules="rules" 
        label-width="120px"
        class="data-form"
        size="large"
      >
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker
            v-model="dataForm.recordTime"
            type="date"
            placeholder="选择记录日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="环境温度(℃)">
          <el-input-number 
            v-model="dataForm.envTemp" 
            :min="0" 
            :precision="1" 
            placeholder="请输入温度"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="环境湿度(%)">
          <el-input-number 
            v-model="dataForm.envHumidity" 
            :min="0" 
            :max="100" 
            :precision="1" 
            placeholder="请输入湿度"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="CO₂浓度(‰)">
          <el-input-number 
            v-model="dataForm.co2Concentration" 
            :min="0" 
            :precision="1" 
            placeholder="请输入浓度"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="生长状态">
          <el-select v-model="dataForm.growthStatus" class="w-full" placeholder="请选择生长状态">
            <el-option
              v-for="item in growthStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="农事操作">
          <el-input
            v-model="dataForm.farmingOper"
            type="textarea"
            :rows="3"
            placeholder="请输入农事操作（如：浇水5L/㎡；灭菌2小时）"
          />
        </el-form-item>
        <el-form-item label="病虫害情况">
          <el-input
            v-model="dataForm.diseaseStatus"
            type="textarea"
            :rows="3"
            placeholder="请输入病虫害情况"
          />
        </el-form-item>
        <el-form-item label="当日投入(元)">
          <el-input-number 
            v-model="dataForm.inputCost" 
            :min="0" 
            :precision="2" 
            placeholder="请输入投入成本（可选）"
            class="w-full"
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
/* 核心：容器放大宽度、字体、内边距 */
.data-container {
  width: 100%;
  max-width: 2400px; /* 最大宽度适配大屏 */
  margin: 0 auto;
  border-radius: 12px;
  padding: 20px; /* 内边距放大 */
  font-size: 18px; /* 基础字体放大 */
  line-height: 1.8; /* 行高提升可读性 */
}

/* 表格样式优化 */
:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f5f7fa;
  font-size: 16px; /* 表格字体放大 */
  
  /* 单元格内边距增加 */
  .el-table__cell {
    padding: 12px 16px;
  }
  
  /* 表头字体放大加粗 */
  .el-table__header-wrapper {
    font-size: 17px;
    font-weight: 600;
  }
  
  /* 操作按钮样式适配 */
  .el-button.is-link {
    padding: 0 8px;
    height: auto;
    line-height: 1.8;
    font-size: 16px;
  }
  
  /* 状态标签放大 */
  .el-tag {
    height: 32px;
    line-height: 30px;
    font-size: 15px;
    padding: 0 12px;
  }
}

/* 空状态样式放大 */
.empty-state {
  text-align: center;
  padding: 60px 0; /* 内边距增加 */
}

/* 弹窗样式优化 */
.custom-data-dialog {
  font-size: 16px;
  
  .el-dialog__header {
    background-color: #f9fafb;
    border-bottom: 1px solid #ebeef5;
    font-weight: 600;
    font-size: 20px; /* 弹窗标题放大 */
    color: #303133;
    padding: 20px 24px; /* 标题内边距增加 */
  }
  
  .el-dialog__body {
    padding: 24px;
    font-size: 16px;
  }
}

/* 弹窗底部按钮栏 */
:deep(.el-dialog__footer) {
  padding: 16px 24px; /* 内边距增加 */
  border-top: 1px solid #ebeef5;
}

/* 表单控件字体放大 */
:deep(.el-form-item) {
  margin-bottom: 20px; /* 表单项间距增加 */
  
  .el-form-item__label {
    font-size: 16px; /* 表单标签字体 */
    font-weight: 500;
  }
  
  .el-input, .el-select, .el-date-picker, .el-input-number {
    font-size: 16px; /* 输入类控件字体 */
  }
  
  .el-textarea {
    font-size: 16px; /* 文本域字体 */
  }
}

/* 按钮通用样式放大 */
:deep(.el-button) {
  font-size: 16px; /* 按钮字体 */
  padding: 12px 20px; /* 按钮点击区域放大 */
}

/* 分页控件样式适配 */
:deep(.el-pagination) {
  font-size: 16px; /* 分页字体放大 */
}
</style>