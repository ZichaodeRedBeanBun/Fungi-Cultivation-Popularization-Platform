// src/views/home/index.ts
import { ref, computed, onMounted, watch } from 'vue'
import { ElNotification, ElMessage } from 'element-plus'
import defaultCover from '@/assets/default.jpg'
// 引入【新API】：概要列表 + 详情API
import { getMushroomSummaryPage, getDiseasePestSummaryPage, getMushroomById, getDiseasePestById, getUserPopSciBannerList, getPopSciContentById } from '@/api/system'
// 引入【正确类型】：概要VO+详情VO+DTO+枚举
import {
  MushroomVO,
  MushroomPageQueryDTO,
  DiseasePestVO,
  DiseasePestPageQueryDTO,
  MushroomSummaryVO,
  DiseasePestSummaryVO,
  isDiseasePestVO,
  PopSciContentVO,
  PopSciContentSummaryVO,
  PopSciContentVOWithExtra
} from '@/api/interface'
import { useRouter } from 'vue-router'

export function useHome() {
  const router = useRouter()

  // ========== 1. 变量初始化：修正列表类型为【概要VO】，新增弹窗相关状态 ==========
  // 轮播图（改为从API获取）- 更新类型包含 contentId
  const bannerList = ref<{ bannerId: number; bannerUrl: string; contentId?: number }[]>([])
  // 菌种列表：改为【菌种概要VO】（新接口返回）
  const popSciContents = ref<MushroomSummaryVO[]>([])
  // 病虫害列表：改为【病虫害概要VO】（新接口返回，核心修正）
  const diseasePests = ref<DiseasePestSummaryVO[]>([])

  // 菌种弹窗：仅存ID+显隐，详情由API请求，对接独立弹窗组件
  const mushroomDetailDialogVisible = ref(false)
  const mushroomId = ref<number | null>(null) 
  const loadingMushroomDetail = ref(false) // 菌种弹窗加载状态

  // 病虫害弹窗：存ID+显隐+详情+加载状态，详情由API请求
  const diseasePestDialogVisible = ref(false)
  const diseasePestId = ref<number | null>(null)
  const selectedDiseasePest = ref<DiseasePestVO | null>(null)
  const loadingDiseasePest = ref(false) // 病虫害弹窗加载状态

  // 新增：科普详情弹窗（关联轮播图点击）
  const popSciDetailDialogVisible = ref(false)
  const selectedPopSci = ref<PopSciContentVO | null>(null)
  const popSciDetailLoading = ref(false)

  // 图片预览：独立弹窗组件已内置，此处仅保留基础状态（给独立弹窗）
  const previewVisible = ref(false)
  const previewIndex = ref(0)

  // ========== 2. 列表数据获取：替换为【新概要VO分页API】 ==========
  // 获取菌种概要列表（原fetchPopSciContents，直接调用新API，无需map）
  const fetchPopSciContents = async () => {
    try {
      const params: MushroomPageQueryDTO = { pageNum: 1, pageSize: 6 }
      // 调用新的菌种概要VO接口
      const res = await getMushroomSummaryPage(params)
      if (res.code === 0 && res.data && Array.isArray(res.data.items)) {
        // 直接赋值，新接口已返回概要VO，无需手动map
        popSciContents.value = res.data.items.map(item => ({
          ...item,
          coverUrl: item.coverUrl || defaultCover, // 兜底默认封面
          updateTime: item.updateTime ? item.updateTime.split(' ')[0] : '' // 格式化时间
        }))
      } else {
        ElNotification.error('获取菌种列表失败')
      }
    } catch (error) {
      ElNotification.error('获取菌种列表失败')
      console.error('菌种列表请求异常：', error)
    }
  }

  // 获取病虫害概要列表（替换为新的病虫害概要VO接口）
  const fetchDiseasePests = async () => {
    try {
      const params: DiseasePestPageQueryDTO = { pageNum: 1, pageSize: 6 } // 首页只展示6条，无需1000
      // 调用新的病虫害概要VO接口
      const res = await getDiseasePestSummaryPage(params)
      if (res.code === 0 && res.data && Array.isArray(res.data.items)) {
        diseasePests.value = res.data.items
      } else {
        ElNotification.error('获取病虫害列表失败')
      }
    } catch (error) {
      ElNotification.error('获取病虫害列表失败')
      console.error('病虫害列表请求异常：', error)
    }
  }

  // 轮播图（替换为API调用）
  const fetchBannerData = async () => {
    try {
      const res = await getUserPopSciBannerList()
      if (res.code === 0 && res.data && Array.isArray(res.data)) {
        bannerList.value = res.data
      } else {
        ElNotification.error('获取轮播图数据失败')
      }
    } catch {
      ElNotification.error('获取轮播图数据失败')
    }
  }

  // ========== 3. 弹窗交互：改造为【传ID→调用详情API】 ==========
  // 打开菌种弹窗：仅传ID，打开弹窗（独立弹窗内部请求API）
  const openMushroomDetailDialog = (id: number) => {
    mushroomId.value = id
    mushroomDetailDialogVisible.value = true
  }
  // 关闭菌种弹窗：重置所有状态，防止残留
  const closeMushroomDetailDialog = () => {
    mushroomDetailDialogVisible.value = false
    mushroomId.value = null
    loadingMushroomDetail.value = false
    previewVisible.value = false
    previewIndex.value = 0
  }

 // 找到openDiseasePestDialog函数，替换为以下代码
const openDiseasePestDialog = async (id: number) => {
  diseasePestId.value = id
  diseasePestDialogVisible.value = true
  loadingDiseasePest.value = true
  try {
    const res = await getDiseasePestById(id)
    if (res.code === 0) {
      // 核心：用类型谓词函数判断，TS会自动识别类型
      if (isDiseasePestVO(res.data)) {
        selectedDiseasePest.value = res.data // 无任何类型报错！
      } else {
        ElMessage.error('病虫害详情数据格式异常，缺少核心字段')
        selectedDiseasePest.value = null
      }
    } else {
      ElMessage.error('获取病虫害详情失败：' + (res.message || '请求失败'))
      selectedDiseasePest.value = null
    }
  } catch (error) {
    ElMessage.error('获取病虫害详情失败，请检查网络')
    selectedDiseasePest.value = null
    console.error('病虫害详情请求异常：', error)
  } finally {
    loadingDiseasePest.value = false
  }
}
  // 关闭病虫害弹窗：重置所有状态
  const closeDiseasePestDialog = () => {
    diseasePestDialogVisible.value = false
    diseasePestId.value = null
    selectedDiseasePest.value = null
    loadingDiseasePest.value = false
  }

  // ========== 新增：科普详情弹窗交互方法（参考usePopSci.ts的openPopSciDetail） ==========
  /**
   * 打开科普详情弹窗 - 接收contentId → 按ID查完整详情 → 填充弹窗
   * @param contentId 科普内容ID
   */
  const openPopSciDetail = async (contentId: number) => {
    // 边界判断：contentId不存在直接返回，避免无效请求
    if (!contentId) {
      ElMessage.warning('内容ID不存在，无法查看详情')
      return
    }
    try {
      // 1. 开启加载状态，重置弹窗数据
      popSciDetailLoading.value = true
      selectedPopSci.value = null
      // 2. 调用详情API，根据ID查询完整信息
      const res = await getPopSciContentById(contentId);
      // 3. 接口请求成功，且有数据
      if (res.code === 0 && res.data) {
        const data =res.data as PopSciContentVOWithExtra;
        selectedPopSci.value = data // 填充完整VO数据
        popSciDetailDialogVisible.value = true // 打开弹窗
      } else {
        // 接口返回成功，但无数据
        ElMessage.warning('该科普内容已下架或不存在')
      }
    } catch (error) {
      // 接口请求失败（网络/后端异常）
      ElMessage.error('获取科普详情失败，请稍后重试')
      console.error('加载科普详情失败：', error)
    } finally {
      // 无论成功/失败，关闭加载状态
      popSciDetailLoading.value = false
    }
  }

  // 关闭科普详情弹窗
  const closePopSciDetail = () => {
    popSciDetailDialogVisible.value = false
    selectedPopSci.value = null
  }

  // ========== 4. 工具方法+路由跳转（保留原有逻辑） ==========
  const goToMoreDiseasePests = () => router.push('/diseasepests')
  const goToMorePopSci = () => router.push('/mushroomCards')
  const formatDate = (dateStr?: string) => dateStr ? dateStr.split(' ')[0] : ''
  // 图片预览方法：独立弹窗已内置，此处保留为兼容
  const openPreview = (index: number) => {
    previewIndex.value = index
    previewVisible.value = true
  }

  // ========== 5. 初始化数据（保留原有调用） ==========
  onMounted(() => {
    fetchBannerData()
    fetchPopSciContents()
    fetchDiseasePests()
  })

  // ========== 6. 导出变量/方法：仅导出需要的，清理冗余 ==========
  return {
    bannerList, popSciContents, diseasePests,
    // 菌种弹窗
    mushroomDetailDialogVisible, mushroomId, loadingMushroomDetail,
    // 病虫害弹窗
    diseasePestDialogVisible, selectedDiseasePest, loadingDiseasePest,
    // 科普详情弹窗（新增）
    popSciDetailDialogVisible,
    popSciDetailLoading,
    selectedPopSci,
    openPopSciDetail,
    closePopSciDetail,
    // 图片预览
    previewVisible, previewIndex,
    // 方法
    fetchBannerData, fetchPopSciContents, fetchDiseasePests,
    goToMoreDiseasePests, goToMorePopSci,
    openMushroomDetailDialog, closeMushroomDetailDialog,
    openDiseasePestDialog, closeDiseasePestDialog,
    openPreview, formatDate
  }
}