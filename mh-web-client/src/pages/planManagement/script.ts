import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage,ElForm, ElMessageBox, ElNotification, ElLoading } from 'element-plus'
import { useRenderIcon } from "@/components/icon/ReIcon/src/hooks";
import Refresh from "@iconify-icons/ep/refresh";
import AddFill from "@iconify-icons/ri/add-circle-line";
import Upload from "@iconify-icons/ep/upload-filled";
import Download from "@iconify-icons/ep/download";
import More from "@iconify-icons/ep/more";
import { useRouter } from 'vue-router'
import * as XLSX from 'xlsx'
import type { FormInstance } from 'element-plus'
import{
  PlantPlanVO,
  PlantPlanPageQueryDTO,
  PlantPlanAddDTO,
  PlantPlanUpdateDTO,
  PlantPlanStatusEnum,
  PlantPlanStatusDescMap,
  PlantNodeVO,
  PlantDataVO,
  PlantResultVO
} from '@/api/interface'
import {
  getPlantPlanPage,
  addPlantPlan,
  updatePlantPlan,
  deletePlantPlan,
  deletePlantPlans,
  getPlantPlanById,
  getPlantNodeListByPlanId,
  getPlantDataPage,
  getPlantResultByPlanId,
  addPlantNode,
   addPlantData,
   addPlantResult
} from '@/api/system'
export function usePlanManagement() {
// ===================== 路由实例 =====================
const router = useRouter()

// ===================== 基础状态 =====================
const loading = ref(false)
const planList = ref<PlantPlanVO[]>([])
const searchForm = reactive({
  planName: '',
  status: ''
})
const selectedPlanIds = ref<number[]>([]) // 用于存储选中的计划ID
const importInput = ref<HTMLInputElement | null>(null)
// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const state = reactive({
  size: 'default',
  disabled: false,
  background: false,
  layout: 'total, sizes, prev, pager, next, jumper',
  total: 0,
  pageSizes: [10, 20, 30, 40],
})

// ===================== 详情弹窗控制 =====================
const detailDialogVisible = ref(false)
const currentPlanDetail = ref<PlantPlanVO | null>(null)

// ===================== 可视化弹窗控制 =====================
const timelineDialogVisible = ref(false)
const selectedPlanForTimeline = ref<PlantPlanVO | null>(null)

// ===================== 导入导出控制 =====================
const importDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const selectedExportPlan = ref<PlantPlanVO | null>(null)
const importFile = ref<File | null>(null)
const importLoading = ref(false)
const exportLoading = ref(false)

// ===================== 弹窗控制 =====================
const planDialogVisible = ref(false)
const planDialogTitle = ref('新增种植计划')
const editingPlanId = ref<number | null>(null)
const planForm = reactive<PlantPlanAddDTO>({
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
const planFormRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  fungusType: [{ required: true, message: '请选择菌型', trigger: 'blur' }]
}

// ===================== 数据获取 =====================
const fetchPlantPlans = async () => {
  try {
    loading.value = true
    const params: PlantPlanPageQueryDTO = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      planName: searchForm.planName,
      status: searchForm.status as PlantPlanStatusEnum | ''
    }
    
    const res = await getPlantPlanPage(params)
    if (res.code === 0 && res.data) {
      planList.value = res.data.items || []
      state.total = res.data.total || 0
      // 清除不再存在的选中项
      selectedPlanIds.value = selectedPlanIds.value.filter(id => 
        planList.value.some(plan => plan.planId === id)
      )
    } else {
      ElNotification.error('获取计划列表失败')
      planList.value = []
      state.total = 0
    }
  } catch {
    ElNotification.error('获取计划列表失败')
    planList.value = []
    state.total = 0
  } finally {
    loading.value = false
  }
}
// ===================== 导入导出相关函数 =====================

// 更新 FIELD_MAP，添加缺失的字段映射
const IMPORT_FIELD_MAP: Record<string, string> = {
  // 计划基础信息
  'planName': 'planName',
  '计划名称': 'planName',
  'fungusType': 'fungusType',
  '菌种类型': 'fungusType',
  'plantArea': 'plantArea',
  '种植面积': 'plantArea',
  'startTime': 'startTime',
  '开始时间': 'startTime',
  'endTime': 'endTime',
  '结束时间': 'endTime',
  'expectYield': 'expectYield',
  '预期产量': 'expectYield',
  'expectIncome': 'expectIncome',
  '预期收入': 'expectIncome',
  'status': 'status',
  '状态': 'status',
  'remark': 'remark',
  '备注': 'remark',
  
  // 种植节点
  'nodeType': 'nodeType',
  '节点类型': 'nodeType',
  'nodeName': 'nodeName',
  '节点名称': 'nodeName',
  'planTime': 'planTime',
  '计划时间': 'planTime',
  'remindTime': 'remindTime',
  '提醒时间': 'remindTime',
  'actualTime': 'actualTime',
  '实际时间': 'actualTime',
  
  // 种植数据
  'recordTime': 'recordTime',
  '记录时间': 'recordTime',
  'envTemp': 'envTemp',
  '环境温度': 'envTemp',
  'envHumidity': 'envHumidity',
  '环境湿度': 'envHumidity',
  'co2Concentration': 'co2Concentration',
  'CO₂浓度': 'co2Concentration',
  'farmingOper': 'farmingOper',
  '农事操作': 'farmingOper',
  'growthStatus': 'growthStatus',
  '生长状态': 'growthStatus',
  'diseaseStatus': 'diseaseStatus',
  '病害情况': 'diseaseStatus',
  'inputCost': 'inputCost',
  '投入成本': 'inputCost',
  
  // 采收成果
  'harvestTime': 'harvestTime',
  '采收时间': 'harvestTime',
  'actualYield': 'actualYield',
  '实际产量': 'actualYield',
  'yieldGrade': 'yieldGrade',
  '产量品质': 'yieldGrade',
  'actualIncome': 'actualIncome',
  '实际收益': 'actualIncome',
  'lossReason': 'lossReason',
  '减产/损失原因': 'lossReason',
  'summary': 'summary',
  '计划总结': 'summary'
}

// 映射Excel表头
const mapExcelHeaders = (workbook: XLSX.WorkBook) => {
  console.log('=== 开始映射Excel表头 ===')
  
  const sheetNames = ['计划基础信息', '种植节点', '种植数据', '采收成果']
  
  for (const sheetName of sheetNames) {
    if (!workbook.Sheets[sheetName]) {
      console.warn(`Sheet "${sheetName}" 不存在`)
      continue
    }
    
    const sheet = workbook.Sheets[sheetName]
    const headers = XLSX.utils.sheet_to_json(sheet, { header: 1 })[0] as string[]
    
    if (!headers || headers.length === 0) {
      console.warn(`Sheet "${sheetName}" 无表头`)
      continue
    }
    
    console.log(`原始表头(${sheetName}):`, headers)
    
    // 映射表头（中文 -> 英文）
    const mappedHeaders = headers.map(header => {
      const key = header?.toString().trim()
      return IMPORT_FIELD_MAP[key] || key
    })
    
    console.log(`映射后表头(${sheetName}):`, mappedHeaders)
    
    // 更新工作表表头
    for (let i = 0; i < mappedHeaders.length; i++) {
      const cellAddress = XLSX.utils.encode_cell({ r: 0, c: i })
      if (sheet[cellAddress]) {
        sheet[cellAddress].v = mappedHeaders[i]
      } else {
        sheet[cellAddress] = { v: mappedHeaders[i], t: 's' }
      }
    }
  }
  
  console.log('=== Excel表头映射完成 ===')
}

// 验证Excel数据
const validateExcelData = (workbook: XLSX.WorkBook): string[] => {
  console.log('=== 开始验证Excel数据 ===')
  const errors: string[] = []
  
  // 1. 验证计划基础信息（必须）
  if (!workbook.Sheets['计划基础信息']) {
    errors.push('缺少【计划基础信息】sheet页')
    return errors
  }
  
  const planSheet = workbook.Sheets['计划基础信息']
  const planData = XLSX.utils.sheet_to_json(planSheet)
  console.log('验证时的计划基础信息数据:', planData)
  
  if (planData.length === 0) {
    errors.push('计划基础信息不能为空')
  } else {
    const firstRowKeys = Object.keys(planData[0])
    console.log('验证时的计划基础信息字段:', firstRowKeys)
    
    // 关键：使用中文字段验证
    const requiredFields = ['计划名称', '菌种类型', '开始时间', '结束时间']
    const missingFields = requiredFields.filter(field => 
      !firstRowKeys.includes(field)
    )
    
    if (missingFields.length > 0) {
      errors.push(`计划基础信息缺少必要字段: ${missingFields.join(', ')}`)
    }
  }
  
  console.log('=== Excel数据验证完成，错误数量:', errors.length, '===')
  return errors
}

// 提取Excel数据
const extractExcelData = (workbook: XLSX.WorkBook): any => {
  const result = {
    plan: null as any,
    nodes: [] as any[],
    data: [] as any[],
    result: [] as any[]
  }
  
  // 1. 提取计划基础信息
  const planSheet = workbook.Sheets['计划基础信息']
  const planData = XLSX.utils.sheet_to_json(planSheet)
  if (planData.length > 0) {
    result.plan = planData[0]
    console.log('提取的计划基础信息:', result.plan)
  }
  
  // 2. 提取种植节点
  const nodeSheet = workbook.Sheets['种植节点']
  const nodeData = XLSX.utils.sheet_to_json(nodeSheet)
  if (nodeData.length > 0) {
    result.nodes = nodeData
    console.log('提取的种植节点:', result.nodes)
  }
  
  // 3. 提取种植数据
  const dataSheet = workbook.Sheets['种植数据']
  const dataData = XLSX.utils.sheet_to_json(dataSheet)
  if (dataData.length > 0) {
    result.data = dataData
    console.log('提取的种植数据:', result.data)
  }
  
  // 4. 提取采收成果
  const resultSheet = workbook.Sheets['采收成果']
  const resultData = XLSX.utils.sheet_to_json(resultSheet)
  if (resultData.length > 0) {
    result.result = resultData
    console.log('提取的采收成果:', result.result)
  }
  
  return result
}
// 转换计划数据为PlantPlanAddDTO格式
const convertToPlanAddDTO = (planData: any): PlantPlanAddDTO => {
  // 从planData中提取数据，支持中英文字段
  const getValue = (field: string, defaultValue: any = undefined) => {
    return planData[field] || planData[IMPORT_FIELD_MAP[field]] || defaultValue
  }
  
  // 状态映射：中文 -> 枚举值
  const statusMap: Record<string, PlantPlanStatusEnum> = {
    '执行中': PlantPlanStatusEnum.EXECUTING,
    '已完成': PlantPlanStatusEnum.COMPLETED,
    '暂停': PlantPlanStatusEnum.PAUSED,
    '作废': PlantPlanStatusEnum.INVALID,
    '1': PlantPlanStatusEnum.EXECUTING,
    '2': PlantPlanStatusEnum.COMPLETED,
    '3': PlantPlanStatusEnum.PAUSED,
    '4': PlantPlanStatusEnum.INVALID
  }
  
  const statusStr = getValue('status') || getValue('状态')
  const status = statusMap[statusStr] || PlantPlanStatusEnum.COMPLETED
  
  // 处理日期时间格式
  const startTime = getValue('startTime') || getValue('开始时间')
  const endTime = getValue('endTime') || getValue('结束时间')
  
  // 转换为后端要求的格式：yyyy-MM-dd HH:mm:ss
  const formatDateTime = (dateStr: string) => {
    if (!dateStr) return undefined
    // 如果已经是完整日期时间，直接返回
    if (dateStr.includes(' ')) return dateStr
    // 如果只有日期，补充时间
    return `${dateStr} 00:00:00`
  }
  
  return {
    planName: getValue('planName') || getValue('计划名称', ''),
    fungusType: getValue('fungusType') || getValue('菌种类型', ''),
    plantArea: getNumber(getValue('plantArea') || getValue('种植面积', 0)),
    startTime: startTime ? formatDateTime(startTime) : undefined,
    endTime: endTime ? formatDateTime(endTime) : undefined,
    expectYield: getNumber(getValue('expectYield') || getValue('预期产量')),
    expectIncome: getNumber(getValue('expectIncome') || getValue('预期收入', 0)),
    status: status,
    remark: getValue('remark') || getValue('备注', '')
  }
}
// 如果没有 BigNumber，可以使用 Number 替代
const getNumber = (value: any): number | undefined => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const num = Number(value)
  return isNaN(num) ? undefined : num
}
// 转换节点数据为PlantNodeAddDTO格式
const convertToNodeAddDTO = (nodeData: any, planId: number): any => {
  return {
    planId: planId,
    nodeType: nodeData.nodeType || nodeData['节点类型'] || '',
    nodeName: nodeData.nodeName || nodeData['节点名称'] || '',
    planTime: nodeData.planTime || nodeData['计划时间'] 
      ? formatNodeDateTime(nodeData.planTime || nodeData['计划时间'])
      : undefined,
    remindTime: nodeData.remindTime || nodeData['提醒时间']
      ? formatNodeDateTime(nodeData.remindTime || nodeData['提醒时间'])
      : undefined,
    actualTime: nodeData.actualTime || nodeData['实际时间']
      ? formatNodeDateTime(nodeData.actualTime || nodeData['实际时间'])
      : undefined,
    status: nodeData.status || nodeData['状态'] || 0,
    remark: nodeData.remark || nodeData['备注'] || ''
  }
}

// 转换种植数据为PlantDataAddDTO格式
const convertToDataAddDTO = (dataItem: any, planId: number): any => {
  // 生长状态映射
  const growthStatusMap: Record<string, number> = {
    '优': 1,
    '良': 2,
    '差': 3,
    '1': 1,
    '2': 2,
    '3': 3
  }
  
  const growthStatus = dataItem.growthStatus || dataItem['生长状态']
  
  return {
    planId: planId,
    recordTime: dataItem.recordTime || dataItem['记录时间']
      ? formatNodeDateTime(dataItem.recordTime || dataItem['记录时间'])
      : undefined,
    envTemp: dataItem.envTemp || dataItem['环境温度'],
    envHumidity: dataItem.envHumidity || dataItem['环境湿度'],
    co2Concentration: dataItem.co2Concentration || dataItem['CO₂浓度'],
    farmingOper: dataItem.farmingOper || dataItem['农事操作'],
    growthStatus: growthStatus ? growthStatusMap[growthStatus] : undefined,
    diseaseStatus: dataItem.diseaseStatus || dataItem['病害情况'],
    inputCost: dataItem.inputCost || dataItem['投入成本']
  }
}

// 转换采收成果为PlantResultAddDTO格式
const convertToResultAddDTO = (resultItem: any, planId: number): any => {
  return {
    planId: planId,
    harvestTime: resultItem.harvestTime || resultItem['采收时间']
      ? formatNodeDateTime(resultItem.harvestTime || resultItem['采收时间'])
      : undefined,
    actualYield: resultItem.actualYield || resultItem['实际产量'],
    yieldGrade: resultItem.yieldGrade || resultItem['产量品质'],
    actualIncome: resultItem.actualIncome || resultItem['实际收益'],
    lossReason: resultItem.lossReason || resultItem['减产/损失原因'],
    summary: resultItem.summary || resultItem['计划总结']
  }
}

// 格式化节点日期时间
const formatNodeDateTime = (dateStr: string): string => {
  if (!dateStr) return ''
  // 如果已经是完整日期时间，直接返回
  if (dateStr.includes(' ')) return dateStr
  // 如果只有日期，补充默认时间
  return `${dateStr} 12:00:00`
}
const importPlantPlans = async () => {
  console.log('1. 开始导入计划函数')
  
  if (!importFile.value) {
    console.log('2. 没有选择文件')
    ElMessage.warning('请选择要导入的Excel文件')
    return
  }
  
  console.log('3. 选择的文件:', importFile.value.name, importFile.value.size, 'bytes')
  
  try {
    importLoading.value = true
    console.log('4. 开始加载，设置loading为true')
    
    const reader = new FileReader()
    
    reader.onloadstart = () => {
      console.log('5. 开始读取文件')
    }
    
    reader.onerror = (error) => {
      console.error('6. 文件读取错误:', error)
      ElMessage.error('文件读取失败')
      importLoading.value = false
    }
    
    reader.onload = async (e) => {
      console.log('7. 文件读取完成')
      
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer)
        console.log('8. 文件数据长度:', data.length)
        
        // 读取Excel
        const workbook = XLSX.read(data, { type: 'array' })
        console.log('9. Excel解析完成，sheet列表:', workbook.SheetNames)
        
        // 详细打印每个sheet的内容
        workbook.SheetNames.forEach(sheetName => {
          console.log(`=== Sheet: ${sheetName} ===`)
          const sheet = workbook.Sheets[sheetName]
          const jsonData = XLSX.utils.sheet_to_json(sheet, { header: 1 })
          console.log('表头行:', jsonData[0])
          console.log('数据行数:', jsonData.length - 1)
        })
        
        // 验证必须的sheet页
        const validSheets = ['计划基础信息', '种植节点', '种植数据', '采收成果']
        const missingSheets = validSheets.filter(sheet => !workbook.SheetNames.includes(sheet))
        
        if (missingSheets.length > 0) {
          console.error('缺少必要的sheet页:', missingSheets)
          ElMessage.error(`缺少必要的sheet页: ${missingSheets.join(', ')}`)
          return
        }
        
        // 验证数据
        console.log('10. 开始验证数据')
        const validationErrors = validateExcelData(workbook)
        console.log('11. 验证结果:', validationErrors)
        
        if (validationErrors.length > 0) {
          ElMessage.error(`数据验证失败: ${validationErrors.join('; ')}`)
          return
        }
        
        // 映射表头
        console.log('12. 开始映射表头')
        mapExcelHeaders(workbook)
        
        // 提取数据
        console.log('13. 开始提取数据')
        const importData = extractExcelData(workbook)
        console.log('14. 提取的数据结构:', importData)
        
        // ===================== 调用API保存数据 =====================
console.log('15. 开始调用API保存数据')

let createdPlanId: number | null = null

try {
  // 1. 保存计划基础信息
  if (!importData.plan) {
    throw new Error('缺少计划基础信息')
  }
  
  // 转换计划数据格式
  const planData = convertToPlanAddDTO(importData.plan)
  console.log('16. 转换后的计划数据:', planData)
  
  // 调用创建计划API
  const planRes = await addPlantPlan(planData)
  console.log('17. 创建计划API响应（完整）:', planRes)
  console.log('17.1 响应数据结构:', JSON.stringify(planRes, null, 2))
  
  if (planRes.code !== 0) {
    throw new Error(planRes.message || '创建计划失败')
  }
  
  // 获取创建的计划ID - 直接从响应数据中获取
  if (planRes.code === 0 && planRes.data) {
    console.log('18.1 获取创建的计划ID:', planRes.data)
    // API 现在保证返回 planId
    createdPlanId = Number(planRes.data)
  }
  
  // 如果还是没有获取到，抛出错误
  if (!createdPlanId) {
    throw new Error('无法获取创建的计划ID')
  }
  
  console.log('18. 计划创建成功，planId:', createdPlanId)
  
  // 2. 保存种植节点（如果有）
  if (importData.nodes && importData.nodes.length > 0 && createdPlanId) {
    console.log('19. 开始保存种植节点，数量:', importData.nodes.length)
    
    for (const node of importData.nodes) {
      try {
        const nodeData = convertToNodeAddDTO(node, createdPlanId)
        const nodeRes = await addPlantNode(nodeData)
        
        if (nodeRes.code !== 0) {
          console.warn(`节点保存失败: ${nodeRes.message}`, node)
        } else {
          console.log(`节点保存成功: ${node.nodeName || node['节点名称']}`)
        }
      } catch (nodeError) {
        console.error('保存节点时出错:', nodeError)
      }
    }
  }
  
  // 3. 保存种植数据（如果有）
  if (importData.data && importData.data.length > 0 && createdPlanId) {
    console.log('20. 开始保存种植数据，数量:', importData.data.length)
    
    for (const dataItem of importData.data) {
      try {
        const plantData = convertToDataAddDTO(dataItem, createdPlanId)
        const dataRes = await addPlantData(plantData)
        
        if (dataRes.code !== 0) {
          console.warn(`种植数据保存失败: ${dataRes.message}`, dataItem)
        } else {
          console.log(`种植数据保存成功: ${dataItem.recordTime || dataItem['记录时间']}`)
        }
      } catch (dataError) {
        console.error('保存种植数据时出错:', dataError)
      }
    }
  }
  
  // 4. 保存采收成果（如果有）
  if (importData.result && importData.result.length > 0 && createdPlanId) {
    console.log('21. 开始保存采收成果，数量:', importData.result.length)
    
    for (const resultItem of importData.result) {
      try {
        const resultData = convertToResultAddDTO(resultItem, createdPlanId)
        const resultRes = await addPlantResult(resultData)
        
        if (resultRes.code !== 0) {
          console.warn(`采收成果保存失败: ${resultRes.message}`, resultItem)
        } else {
          console.log(`采收成果保存成功: ${resultItem.harvestTime || resultItem['采收时间']}`)
        }
      } catch (resultError) {
        console.error('保存采收成果时出错:', resultError)
      }
    }
  }
  
  console.log('22. 导入完成')
  ElMessage.success(`导入成功！计划名称: ${planData.planName}`)
  
} catch (apiError: any) {
  console.error('23. 调用API失败:', apiError)
  ElMessage.error(`导入失败: ${apiError.message || '未知错误'}`)
  return
}
        
        // 关闭对话框
        importDialogVisible.value = false
        importFile.value = null
        if (importInput.value) {
          importInput.value.value = ''
        }
        
        // 刷新列表
        await fetchPlantPlans()
        
      } catch (error) {
        console.error('24. 解析Excel失败:', error)
        ElMessage.error('解析Excel文件失败，请检查文件格式')
      } finally {
        importLoading.value = false
        console.log('25. 完成，设置loading为false')
      }
    }
    
    reader.readAsArrayBuffer(importFile.value)
    
  } catch (error) {
    console.error('26. 外层捕获错误:', error)
    ElMessage.error('导入计划失败')
    importLoading.value = false
  }
}
// ===================== 操作处理 =====================
const handleSearch = () => {
  currentPage.value = 1
  fetchPlantPlans()
}

const resetSearch = () => {
  searchForm.planName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchPlantPlans()
}
const exportOptions = reactive({
  basicInfo: true,
  nodes: true,
  data: true,
  result: true
})

// 跳转到当前计划详情页
const goToCurrentPlan = (planId: number) => {
  router.push({
    path: `/currentPlan/${planId}`,
    query: { from: 'list' } // 标记是从列表页来的
  })
}
// const importInput = ref<HTMLInputElement | null>(null)


// 打开计划详情对话框
const openPlanDetail = async (planId: number) => {
  try {
    const res = await getPlantPlanById(planId)
    if (res.code === 0 && res.data) {
      currentPlanDetail.value = res.data as PlantPlanVO
      detailDialogVisible.value = true
    } else {
      ElMessage.error('获取计划详情失败')
    }
  } catch {
    ElMessage.error('获取计划详情失败')
  }
}
//===================
// 时间轴相关数据和方法
const timelineNodes = ref<PlantNodeVO[]>([])
const timelineData = ref<PlantDataVO[]>([])
const tooltipVisible = ref(false)
const tooltipContent = ref('')
const tooltipPosition = ref({ x: 0, y: 0 })

// 获取节点数据点
const getNodeDataPoints = (node: PlantNodeVO) => {
  if (!node.planTime) return []
  
  const nodeStartTime = new Date(node.planTime).getTime()
  let nodeEndTime = Date.now()
  
  // 找到下一个节点
  const currentIndex = timelineNodes.value.findIndex(n => n.nodeId === node.nodeId)
  if (currentIndex >= 0 && currentIndex < timelineNodes.value.length - 1) {
    const nextNode = timelineNodes.value[currentIndex + 1]
    if (nextNode.planTime) {
      nodeEndTime = new Date(nextNode.planTime).getTime()
    }
  }
  
  // 过滤出在节点周期内的数据
  return timelineData.value.filter(data => {
    if (!data.recordTime) return false
    const recordTime = new Date(data.recordTime).getTime()
    return recordTime >= nodeStartTime && recordTime <= nodeEndTime
  }).sort((a, b) => new Date(b.recordTime).getTime() - new Date(a.recordTime).getTime())
}

// 生长状态相关方法
const getGrowthStatusText = (status?: number) => {
  const map: Record<number, string> = {
    1: '优',
    2: '良',
    3: '差'
  }
  return map[status || 0] || '未知'
}

const getGrowthStatusColor = (status?: number) => {
  const map: Record<number, string> = {
    1: 'text-green-600',
    2: 'text-yellow-600',
    3: 'text-red-600'
  }
  return map[status || 0] || 'text-gray-500'
}

// Tooltip 相关方法
const showDataTooltip = (data: PlantDataVO, event: MouseEvent) => {
  console.log('[TOOLTIP] Triggered for data:', data.recordTime); // 关键调试日志
  let content = `<div class="tooltip-title">${formatDate(data.recordTime)}</div>`
  content += '<div class="tooltip-content">'
  // 每个字段外层加 div.tooltip-row，强制独占一行
  if (data.envTemp) content += `<div class="tooltip-row"><span class="label">温度:</span><span>${data.envTemp}℃</span></div>`;
  if (data.envHumidity) content += `<div class="tooltip-row"><span class="label">湿度:</span><span>${data.envHumidity}%</span></div>`;
  if (data.co2Concentration) content += `<div class="tooltip-row"><span class="label">CO₂:</span><span>${data.co2Concentration}‰</span></div>`;
  if (data.farmingOper) content += `<div class="tooltip-row"><span class="label">操作:</span><span>${data.farmingOper}</span></div>`;
  if (data.growthStatus) {
    content += `<div class="tooltip-row"><span class="label">生长:</span><span class="${getGrowthStatusColor(data.growthStatus)}">${getGrowthStatusText(data.growthStatus)}</span></div>`;
  }
  if (data.inputCost) content += `<div class="tooltip-row"><span class="label">投入:</span><span>${data.inputCost}元</span></div>`;
  
  content += '</div>';
  
  tooltipContent.value = content
  tooltipPosition.value = {
    x: event.clientX + 10,
    y: event.clientY + 10
  }
  tooltipVisible.value = true
}

const hideDataTooltip = () => {
  tooltipVisible.value = false
}
// 节点状态图例
const nodeStatusLegend = computed(() => {
  return '已完成 (绿色) | 未完成 (黄色) | 逾期 (红色)'
})
// 打开时间轴可视化
const openTimelineDialog = async (plan: PlantPlanVO) => {
  try {
    loading.value = true
    selectedPlanForTimeline.value = plan
    
    // 获取节点列表
    const nodeRes = await getPlantNodeListByPlanId(plan.planId)
    if (nodeRes.code === 0 && nodeRes.data) {
      // 确保数据是数组格式
      const nodes = Array.isArray(nodeRes.data) ? nodeRes.data : []
      timelineNodes.value = [...nodes].sort((a, b) => 
        new Date(a.planTime).getTime() - new Date(b.planTime).getTime()
      )
    } else {
      timelineNodes.value = []
    }

    // 获取数据列表
    const dataRes = await getPlantDataPage({
      pageNum: 1,
      pageSize: 1000,
      planId: plan.planId
    })
    if (dataRes.code === 0 && dataRes.data?.items) {
      // 确保数据是数组格式
      const dataItems = Array.isArray(dataRes.data.items) ? dataRes.data.items : []
      timelineData.value = [...dataItems]
    } else {
      timelineData.value = []
    }
    console.log('[TIMELINE] Data loaded:', timelineData.value.length)
    timelineDialogVisible.value = true
  } catch {
    ElMessage.error('获取计划数据失败')
  } finally {
    loading.value = false
  }
}
// 根据状态决定按钮行为
const handlePlanAction = (plan: PlantPlanVO) => {
  if (plan.status === PlantPlanStatusEnum.EXECUTING) {
    goToCurrentPlan(plan.planId)
  } else {
    openPlanDetail(plan.planId)
  }
}

const openAddPlanDialog = () => {
  planDialogTitle.value = '新增种植计划'
  editingPlanId.value = null
  Object.assign(planForm, {
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
  planDialogVisible.value = true
}


const planFormRef = ref<FormInstance | null>(null)
const submitPlanForm = async () => {
  try {
    // 使用nextTick确保DOM更新完成
    await nextTick()
    if (!planFormRef.value) {
      console.error('表单实例未绑定')
      return
    }
    const valid = await planFormRef.value.validate()
    if (!valid) return
    
    const submitData = {
      ...planForm,
      startTime: planForm.startTime ? `${planForm.startTime} 00:00:00` : undefined,
      endTime: planForm.endTime ? `${planForm.endTime} 23:59:59` : undefined
    }
    
    if (editingPlanId.value) {
      // 更新计划
      const updateData: PlantPlanUpdateDTO = {
        planId: editingPlanId.value,
        ...submitData
      }
      const res = await updatePlantPlan(updateData)
      if (res.code === 0) {
        ElMessage.success('计划更新成功')
        fetchPlantPlans()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 新增计划
      const res = await addPlantPlan(submitData as PlantPlanAddDTO)
      if (res.code === 0) {
        ElMessage.success('计划创建成功')
        fetchPlantPlans()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
    planDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const openEditPlanDialog = (row: PlantPlanVO) => {
  planDialogTitle.value = '编辑种植计划'
  editingPlanId.value = row.planId
  Object.assign(planForm, {
    planName: row.planName,
    fungusType: row.fungusType,
    plantArea: row.plantArea,
    startTime: row.startTime?.split(' ')[0] || '',
    endTime: row.endTime?.split(' ')[0] || '',
    expectYield: row.expectYield,
    expectIncome: row.expectIncome,
    status: row.status || PlantPlanStatusEnum.EXECUTING,
    remark: row.remark || ''
  })
  planDialogVisible.value = true
}

const deletePlan = (row: PlantPlanVO) => {
  ElMessageBox.confirm(`确定要删除计划【${row.planName}】吗？删除后无法恢复。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePlantPlan(row.planId)
      if (res.code === 0) {
        ElMessage.success('计划删除成功')
        fetchPlantPlans()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除操作失败')
    }
  }).catch(() => {})
}

// 批量删除计划
const batchDeletePlans = () => {
  if (selectedPlanIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的计划')
    return
  }
  
  // 检查是否包含执行中的计划
  const executingPlans = planList.value.filter(p => 
    selectedPlanIds.value.includes(p.planId) && 
    p.status === PlantPlanStatusEnum.EXECUTING
  )
  
  if (executingPlans.length > 0) {
    ElMessageBox.confirm(
      `选中的计划中包含 ${executingPlans.length} 个"执行中"状态的计划。删除执行中的计划可能导致数据不一致，确定要继续删除吗？`, 
      '警告', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      executeBatchDelete()
    }).catch(() => {})
  } else {
    executeBatchDelete()
  }
}

const executeBatchDelete = async () => {
  try {
    const res = await deletePlantPlans(selectedPlanIds.value)
    if (res.code === 0) {
      ElMessage.success('批量删除成功')
      fetchPlantPlans()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch {
    ElMessage.error('批量删除操作失败')
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (selectedPlanIds.value.length === planList.value.length) {
    selectedPlanIds.value = []
  } else {
    selectedPlanIds.value = planList.value.map(plan => plan.planId)
  }
}

// 切换单个计划选择状态
const toggleSelectPlan = (planId: number) => {
  const index = selectedPlanIds.value.indexOf(planId)
  if (index === -1) {
    selectedPlanIds.value.push(planId)
  } else {
    selectedPlanIds.value.splice(index, 1)
  }
}

// 检查计划是否被选中
const isPlanSelected = (planId: number) => {
  return selectedPlanIds.value.includes(planId)
}

// ===================== 导入导出功能 =====================
// 打开导入对话框
const openImportDialog = () => {
  importDialogVisible.value = true
  importFile.value = null
}

// 处理文件选择
const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    importFile.value = input.files[0]
  } else {
    importFile.value = null
  }
}

// 导入计划
// const importPlantPlans = async () => {
//   if (!importFile.value) {
//     ElMessage.warning('请选择要导入的Excel文件')
//     return
//   }
  
//   try {
//     importLoading.value = true
    
//     // 读取Excel文件
//     const reader = new FileReader()
//     reader.onload = async (e) => {
//       try {
//         const data = new Uint8Array(e.target?.result as ArrayBuffer)
//         const workbook = XLSX.read(data, { type: 'array' })
        
//         // 处理计划数据（第一张sheet）
//         const planSheet = workbook.Sheets[workbook.SheetNames[0]]
//         const planData = XLSX.utils.sheet_to_json(planSheet)
        
//         if (planData.length === 0) {
//           ElMessage.error('未找到有效的计划数据')
//           return
//         }
        
//         // 这里需要根据实际情况处理数据
//         // 由于后端API需要，我们假设有一个批量导入API
//         // 伪代码：const res = await batchImportPlantPlans(planData)
        
//         ElMessage.success(`成功导入 ${planData.length} 个种植计划`)
//         fetchPlantPlans()
//         importDialogVisible.value = false
//       } catch (error) {
//         console.error('解析Excel失败:', error)
//         ElMessage.error('解析Excel文件失败，请检查文件格式')
//       } finally {
//         importLoading.value = false
//       }
//     }
//     reader.readAsArrayBuffer(importFile.value)
//   } catch (error) {
//     console.error('导入失败:', error)
//     ElMessage.error('导入计划失败')
//     importLoading.value = false
//   }
// }

// 打开导出对话框
const openExportDialog = () => {
  if (selectedPlanIds.value.length === 0) {
    ElMessage.warning('请先选择要导出的计划')
    return
  }
  
  if (selectedPlanIds.value.length > 1) {
    ElMessage.warning('仅支持导出单个计划，请只选择一个计划')
    return
  }
  
  const selectedPlan = planList.value.find(p => p.planId === selectedPlanIds.value[0])
  if (selectedPlan) {
    selectedExportPlan.value = selectedPlan
    exportDialogVisible.value = true
  }
}
/**
 * 计算每个字段的最大长度
 * @param data 要导出的数据数组
 * @param fields 字段列表
 * @returns 每个字段的最大长度
 */
function calculateMaxFieldLengths(data: any[], fields: string[]): number[] {
  const maxLengths: number[] = new Array(fields.length).fill(0);
  
  data.forEach(item => {
    fields.forEach((field, index) => {
      const value = item[field];
      if (value === undefined || value === null) return;
      
      // 处理字符串类型，计算显示长度
      if (typeof value === 'string') {
        maxLengths[index] = Math.max(maxLengths[index], value.length);
      } 
      // 处理数字类型，考虑格式化后的长度
      else if (typeof value === 'number') {
        maxLengths[index] = Math.max(maxLengths[index], value.toString().length);
      }
      // 其他类型转换为字符串计算
      else {
        maxLengths[index] = Math.max(maxLengths[index], String(value).length);
      }
    });
  });
  
  return maxLengths;
}

/**
 * 生成动态列宽配置
 * @param maxLengths 各字段最大长度
 * @param headerMap 中文表头映射
 * @returns 列宽配置数组
 */
function generateDynamicColumnWidths(maxLengths: number[], headerMap: any): any[] {
  return maxLengths.map((maxLen, index) => {
    // 获取对应的中文表头
    const chineseHeader = Object.values(headerMap)[index];
    
    // 计算表头长度（中文字符按2个字符计算）
    const headerLen = typeof chineseHeader === 'string' 
  ? chineseHeader.length * 3 
  : 0;
    
    // 取表头长度和数据最大长度中的较大值
    const finalLen = Math.max(headerLen, maxLen);
    
    // 根据最终长度设置列宽（1个字符约等于1.5个Excel单位）
    return { wch: Math.min(finalLen * 1.5, 80) }; // 限制最大宽度为50
  });
}

// === 定义中文表头映射 ===
const headerMap = {
  // 计划基础信息
  planName: '计划名称',
  fungusType: '菌种类型',
  plantArea: '种植面积',
  startTime: '开始时间',
  endTime: '结束时间',
  expectYield: '预期产量',
  expectIncome: '预期收入',
  status: '状态',
  remark: '备注',
  // 种植节点
  nodeType: '节点类型',
  nodeName: '节点名称',
  planTime: '计划时间',
  remindTime: '提醒时间',
  actualTime: '实际时间',
  nodeStatus: '节点状态',
  nodeRemark: '节点备注',
  // 种植数据
  recordTime: '记录时间',
  envTemp: '环境温度',
  envHumidity: '环境湿度',
  co2Concentration: 'CO₂浓度',
  farmingOper: '农事操作',
  growthStatus: '生长状态',
  diseaseStatus: '病害情况',
  inputCost: '投入成本',
  // 采收成果（根据PlantResultVO接口精确匹配）
  harvestTime: '采收时间',
  actualYield: '实际产量',
  yieldGrade: '产量品质',
  actualIncome: '实际收益',
  lossReason: '减产/损失原因',
  summary: '计划总结',
  yieldCompare: '产量对比',
  incomeCompare: '收益对比'
};

// 导出计划
const exportPlantPlan = async () => {
  if (!selectedExportPlan.value) return;
  try {
    exportLoading.value = true;
    
    // 模拟获取完整计划数据
    const planRes = await getPlantPlanById(selectedExportPlan.value.planId);
    if (planRes.code !== 0 || !planRes.data) {
      ElMessage.error('获取计划基础信息失败');
      return;
    }
    
    const nodeRes = await getPlantNodeListByPlanId(selectedExportPlan.value.planId);
    if (nodeRes.code !== 0) {
      ElMessage.error('获取种植节点失败');
      return;
    }
    
    const dataRes = await getPlantDataPage({
      pageNum: 1,
      pageSize: 1000,
      planId: selectedExportPlan.value.planId
    });
    if (dataRes.code !== 0) {
      ElMessage.error('获取种植数据失败');
      return;
    }
    const resultRes = await getPlantResultByPlanId(selectedExportPlan.value.planId);
    if (resultRes.code !== 0) {
      ElMessage.error('获取种植成果失败');
      return;
    }
    
    // 创建工作簿
    const wb = XLSX.utils.book_new();
    
    // 1. 计划基础信息
    if (planRes.data && typeof planRes.data === 'object') {
      const planData = [planRes.data as PlantPlanVO];
      const enFields = [
        'planName', 'fungusType', 'plantArea', 'startTime', 'endTime',
        'expectYield', 'expectIncome', 'status', 'remark'
      ];
      
      // 计算动态列宽
      const maxLengths = calculateMaxFieldLengths(planData, enFields);
      const columnWidths = generateDynamicColumnWidths(maxLengths, headerMap);
      
      // 构建二维数组
      const planRows = [
        enFields.map(field => headerMap[field]),
        enFields.map(field => {
          const value = planData[0][field as keyof PlantPlanVO];
          if ((field === 'startTime' || field === 'endTime') && value) {
            if(typeof value === 'string'){
              return value.split(' ')[0];
            }
            return value;
          }
          if (field === 'status') {
            return formatStatus(value as string);
          }
          return value || '';
        })
      ];
      
      const planSheet = XLSX.utils.aoa_to_sheet(planRows);
      planSheet['!cols'] = columnWidths;
      XLSX.utils.book_append_sheet(wb, planSheet, '计划基础信息');
    }
    
    // 2. 种植节点
    const nodes = Array.isArray(nodeRes.data) ? nodeRes.data : [];
    if (nodes.length > 0) {
      const nodeFields = [
        'nodeType', 'nodeName', 'planTime', 'remindTime', 'actualTime',
        'status', 'remark'
      ];
      
      // 计算动态列宽
      const maxLengths = calculateMaxFieldLengths(nodes, nodeFields);
      const columnWidths = generateDynamicColumnWidths(maxLengths, headerMap);
      
      // 构建二维数组
      const nodeRows = [
        nodeFields.map(field => headerMap[field])
      ];
      
      nodes.forEach((node: any) => {
        const row = nodeFields.map(field => {
          let value = node[field];
          if ((field === 'planTime' || field === 'remindTime' || field === 'actualTime') && value) {
            return value.split(' ')[0];
          }
          if (field === 'status') {
            return value ? '已完成' : '未完成';
          }
          return value || '';
        });
        nodeRows.push(row);
      });
      
      const nodeSheet = XLSX.utils.aoa_to_sheet(nodeRows);
      nodeSheet['!cols'] = columnWidths;
      XLSX.utils.book_append_sheet(wb, nodeSheet, '种植节点');
    }
    
    // 3. 种植数据
    const datas = dataRes.data?.items || [];
    if (datas.length > 0) {
      const dataFields = [
        'recordTime', 'envTemp', 'envHumidity', 'co2Concentration',
        'farmingOper', 'growthStatus', 'diseaseStatus', 'inputCost'
      ];
      
      // 计算动态列宽
      const maxLengths = calculateMaxFieldLengths(datas, dataFields);
      const columnWidths = generateDynamicColumnWidths(maxLengths, headerMap);
      
      // 构建二维数组
      const dataRows = [
        dataFields.map(field => headerMap[field])
      ];
      
      datas.forEach(data => {
        const row = dataFields.map(field => {
          let value = data[field];
          if (field === 'recordTime' && value) {
            return value.split(' ')[0];
          }
          return value || '';
        });
        dataRows.push(row);
      });
      
      const dataSheet = XLSX.utils.aoa_to_sheet(dataRows);
      dataSheet['!cols'] = columnWidths;
      XLSX.utils.book_append_sheet(wb, dataSheet, '种植数据');
    }
    
    // 4. 采收成果
    const results = resultRes.code === 0 && resultRes.data 
      ? (Array.isArray(resultRes.data) ? resultRes.data : [resultRes.data]) 
      : [];
      
    if (results.length > 0) {
      const resultFields = [
        'harvestTime', 'actualYield', 'yieldGrade', 'actualIncome',
        'lossReason', 'summary'
      ];
      
      // 计算动态列宽
      const maxLengths = calculateMaxFieldLengths(results, resultFields);
      const columnWidths = generateDynamicColumnWidths(maxLengths, headerMap);
      
      // 构建二维数组
      const resultRows = [
        resultFields.map(field => headerMap[field])
      ];
      
      results.forEach(result => {
        const row = resultFields.map(field => {
          let value = result[field as keyof PlantResultVO];
          if (field === 'harvestTime' && typeof value === 'string') {
            return value.split(' ')[0];
          }
          if ((field === 'actualYield' || field === 'actualIncome') && typeof value === 'number') {
            return value;
          }
          return value || '';
        });
        resultRows.push(row);
      });
      
      const resultSheet = XLSX.utils.aoa_to_sheet(resultRows);
      resultSheet['!cols'] = columnWidths;
      XLSX.utils.book_append_sheet(wb, resultSheet, '采收成果');
    }
    
    // 生成文件
    const fileName = `${selectedExportPlan.value.planName}_种植计划.xlsx`;
    XLSX.writeFile(wb, fileName);
    
    ElMessage.success('计划导出成功');
    exportDialogVisible.value = false;
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出计划失败');
  } finally {
    exportLoading.value = false;
  }
};

// ===================== 工具函数 =====================
const statusOptions = [
  { value: PlantPlanStatusEnum.EXECUTING, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.EXECUTING] },
  { value: PlantPlanStatusEnum.COMPLETED, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.COMPLETED] },
  { value: PlantPlanStatusEnum.INVALID, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.INVALID] },
  { value: PlantPlanStatusEnum.PAUSED, label: PlantPlanStatusDescMap[PlantPlanStatusEnum.PAUSED] }
]

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

const formatStatus = (status?: string) => {
  if (!status) return '-'
  return PlantPlanStatusDescMap[status as keyof typeof PlantPlanStatusDescMap] || status
}

const getStatusTagType = (status?: string) => {
  const typeMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    [PlantPlanStatusEnum.EXECUTING]: 'success',
    [PlantPlanStatusEnum.COMPLETED]: 'info',
    [PlantPlanStatusEnum.PAUSED]: 'warning',
    [PlantPlanStatusEnum.INVALID]: 'danger'
  }
  return typeMap[status || ''] || 'info' // 默认使用info类型
}

// 获取操作按钮文本
const getActionButtonText = (status?: string) => {
  return status === PlantPlanStatusEnum.EXECUTING ? '编辑' : '查看'
}

// ===================== 生命周期 =====================
onMounted(() => {
  fetchPlantPlans()
  nextTick(() => {
    if (!planFormRef.value) {
      console.error('表单实例未绑定：检查模板中的ref名称是否匹配')
    } else {
      console.log('表单实例已成功绑定:', planFormRef.value)
    }
  })
})

// 计算属性：是否有选中的项
const hasSelectedItems = computed(() => selectedPlanIds.value.length > 0)

return{
  // 暴露给模板使用的数据和方法
  planFormRef,
  planFormRules,
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
  
  // 方法
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
  
  // 工具方法
  formatDate,
  formatStatus,
  getStatusTagType,
  getActionButtonText,
  hasSelectedItems,
  
  // 图标
  useRenderIcon,
  Refresh,
  AddFill,
  Upload,
  Download,
  More,
  //补充
  exportOptions,
  statusOptions,
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
  tooltipVisible,
  tooltipPosition,
  tooltipContent,
  IMPORT_FIELD_MAP// 如果需要暴露给模板
}
}
