<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type {
  PlantNodeVO,
  PlantNodeAddDTO,
  PlantNodeUpdateDTO,
  PlantNodeTypeEnum
} from '@/api/interface'
import {
  getPlantNodeListByPlanId,
  addPlantNode,
  updatePlantNode,
  deletePlantNode
} from '@/api/system'

const props = defineProps({
  planId: {
    type: Number,
    required: true,
    validator: (val: number) => val > 0 // 校验planId有效性
  }
})

// 节点列表
const nodeList = ref<PlantNodeVO[]>([])
// 加载状态
const loading = ref(false)
// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加种植节点')
// 表单数据
const nodeForm = reactive({
  nodeId: 0,
  planId: props.planId,
  nodeType: '备料' as PlantNodeTypeEnum,
  nodeName: '',
  planTime: '',
  remindTime: '',
  actualTime: '',
  status: 1,
  remark: ''
})
// 表单规则
const rules = {
  nodeType: [{ required: true, message: '请选择节点类型', trigger: 'blur' }],
  nodeName: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  planTime: [{ required: true, message: '请选择计划时间', trigger: 'blur' }]
}
// 编辑模式
const isEditMode = ref(false)

// 节点类型选项
const nodeTypeOptions = [
  { value: '备料', label: '备料' },
  { value: '灭菌', label: '灭菌' },
  { value: '接种', label: '接种' },
  { value: '发菌', label: '发菌' },
  { value: '出菇', label: '出菇' },
  { value: '采收', label: '采收' }
]

// 节点状态映射
const nodeStatusMap = {
  1: { label: '未完成', type: 'info' },
  2: { label: '已完成', type: 'success' },
  3: { label: '逾期', type: 'warning' }
}

// 获取节点列表
const fetchNodeList = async () => {
  try {
    loading.value = true
    const res = await getPlantNodeListByPlanId(props.planId)
    if (res.code === 0 && res.data) {
      nodeList.value = res.data as PlantNodeVO[]
    } else {
      ElNotification.error('获取节点列表失败')
      nodeList.value = []
    }
  } catch {
    ElNotification.error('获取节点列表失败')
    nodeList.value = []
  } finally {
    loading.value = false
  }
}
watch(
  () => props.planId,
  () => {
    fetchNodeList()
  },
  { immediate: true }
)
// 打开添加节点对话框
const openAddDialog = () => {
  dialogTitle.value = '添加种植节点'
  isEditMode.value = false
  Object.assign(nodeForm, {
    nodeId: 0,
    planId: props.planId,
    nodeType: '备料',
    nodeName: '',
    planTime: '',
    remindTime: '',
    actualTime: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (node: PlantNodeVO) => {
  dialogTitle.value = '编辑种植节点'
  isEditMode.value = true
  Object.assign(nodeForm, {
    nodeId: node.nodeId,
    planId: props.planId,
    nodeType: node.nodeType || '备料',
    nodeName: node.nodeName,
    planTime: node.planTime?.split(' ')[0] || '',
    remindTime: node.remindTime?.split(' ')[0] || '',
    actualTime: node.actualTime?.split(' ')[0] || '',
    status: node.status || 1,
    remark: node.remark || ''
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
      ...nodeForm,
      planTime: nodeForm.planTime ? `${nodeForm.planTime} 08:00:00` : undefined,
      remindTime: nodeForm.remindTime ? `${nodeForm.remindTime} 08:00:00` : undefined,
      actualTime: nodeForm.actualTime ? `${nodeForm.actualTime} 08:00:00` : undefined
    }
    
    if (isEditMode.value) {
      // 更新节点
      const updateData: PlantNodeUpdateDTO = {
        nodeId: nodeForm.nodeId,
        nodeType: nodeForm.nodeType,
        nodeName: nodeForm.nodeName,
        planTime: nodeForm.planTime ? `${nodeForm.planTime} 08:00:00` : undefined,
        remindTime: nodeForm.remindTime ? `${nodeForm.remindTime} 08:00:00` : undefined,
        actualTime: nodeForm.actualTime ? `${nodeForm.actualTime} 08:00:00` : undefined,
        status: nodeForm.status,
        remark: nodeForm.remark
      }
      const res = await updatePlantNode(updateData)
      if (res.code === 0) {
        ElMessage.success('节点更新成功')
        fetchNodeList()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 添加节点
      const addData: PlantNodeAddDTO = {
        planId: nodeForm.planId,
        nodeType: nodeForm.nodeType,
        nodeName: nodeForm.nodeName,
        planTime: `${nodeForm.planTime} 08:00:00`,
        remindTime: nodeForm.remindTime ? `${nodeForm.remindTime} 08:00:00` : undefined,
        remark: nodeForm.remark
      }
      const res = await addPlantNode(addData)
      if (res.code === 0) {
        ElMessage.success('节点添加成功')
        fetchNodeList()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
    dialogVisible.value = false
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 删除节点
const deleteNode = (node: PlantNodeVO) => {
  ElMessageBox.confirm(`确定要删除节点【${node.nodeName}】吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePlantNode(node.nodeId)
      if (res.code === 0) {
        ElMessage.success('节点删除成功')
        fetchNodeList()
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

// 今日判断
const isToday = (dateStr?: string) => {
  if (!dateStr) return false
  const today = new Date().toISOString().split('T')[0]
  return dateStr.split(' ')[0] === today
}

onMounted(() => {
  fetchNodeList()
})

// 暴露刷新方法
defineExpose({ refreshData: fetchNodeList })
</script>

<template>
  <div class="node-container">
    <!-- 1. 卡片增加margin，header加宽度控制和样式优化 -->
    <el-card class="mb-8">
      <template #header>
        <div class="flex items-center justify-between w-full max-w-[1800px] mx-auto px-4">
          <span class="text-xl font-medium">种植节点列表</span>
          <el-button type="primary" size="large" @click="openAddDialog">
            <el-icon name="Plus" size="18" />
            添加节点
          </el-button>
        </div>
      </template>
      
      <!-- 2. 表格优化：列宽调整、单元格内边距增加、表头样式强化 -->
      <el-table
        :data="nodeList"
        v-loading="loading"
        border
        class="w-full"
        stripe
        size="large"
        :row-height="50"
      >
        <el-table-column prop="nodeType" label="节点类型" width="140" />
        <el-table-column prop="nodeName" label="节点名称" width="240" show-overflow-tooltip />
        <el-table-column prop="planTime" label="计划时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'text-danger font-medium': isToday(row.planTime) }">
              {{ formatDate(row.planTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remindTime" label="提醒时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.remindTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="actualTime" label="完成时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.actualTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="nodeStatusMap[row.status]?.type" size="large">
              {{ nodeStatusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="default" type="warning" link @click="openEditDialog(row)" >
              编辑
            </el-button>
            <el-button style="margin-left: 20px;" size="default" type="danger" link @click="deleteNode(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 3. 空状态优化：内边距增加、样式更协调 -->
      <div v-if="nodeList.length === 0 && !loading" class="empty-state">
        <el-icon name="Document" class="text-5xl text-gray-400 mb-3" />
        <p class="text-lg text-gray-500">暂无种植节点，请添加节点</p>
      </div>
    </el-card>

    <!-- 4. 弹窗优化：宽度放大、表单尺寸放大、按钮样式统一 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      class="custom-node-dialog"
      size="large"
    >
      <el-form 
        ref="formRef" 
        :model="nodeForm" 
        :rules="rules" 
        label-width="120px"
        class="node-form"
        size="large"
      >
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="nodeForm.nodeType" class="w-full" placeholder="请选择节点类型">
            <el-option
              v-for="item in nodeTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称" prop="nodeName">
          <el-input v-model="nodeForm.nodeName" placeholder="例如：香菇接种后7天翻堆" />
        </el-form-item>
        <el-form-item label="计划时间" prop="planTime">
          <el-date-picker
            v-model="nodeForm.planTime"
            type="date"
            placeholder="选择计划日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-date-picker
            v-model="nodeForm.remindTime"
            type="date"
            placeholder="选择提醒日期（默认提前1天）"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item v-if="isEditMode" label="完成时间">
          <el-date-picker
            v-model="nodeForm.actualTime"
            type="date"
            placeholder="选择完成日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item v-if="isEditMode" label="节点状态">
          <el-select v-model="nodeForm.status" class="w-full" placeholder="请选择状态">
            <el-option :value="1" label="未完成" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="逾期" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="nodeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
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
/* 核心：容器样式和种植数据组件统一 */
.node-container {
  width: 100%;
  max-width: 1800px; /* 和你设定的最大宽度保持一致 */
  margin: 0 auto;
  border-radius: 12px;
  padding: 20px;
  font-size: 18px;
  line-height: 1.8;
}

/* 卡片样式强化：确保宽度100% + 阴影优化 */
:deep(.el-card) {
  display: block !important;
  width: 100% !important;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.08) !important;
  margin-bottom: 24px;
}

/* 表格样式统一优化 */
:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f5f7fa;
  font-size: 16px;
  
  /* 单元格内边距增加，提升可读性 */
  .el-table__cell {
    padding: 12px 16px;
  }
  
  /* 表头样式强化 */
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

/* 空状态样式优化 */
.empty-state {
  text-align: center;
  padding: 60px 0; /* 内边距增加，和数据组件一致 */
}

/* 弹窗样式统一 */
.custom-node-dialog {
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
  
  .el-input, .el-select, .el-date-picker {
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
</style>