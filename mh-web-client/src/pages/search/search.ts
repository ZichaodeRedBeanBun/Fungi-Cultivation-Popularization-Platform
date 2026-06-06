// search.ts
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'  // 注意：在函数内调用，而不是顶层
import { ElMessage } from 'element-plus'
import { 
  getMushroomSummaryPage, 
  getDiseasePestSummaryPage, 
  getAllPopSciContents,
  searchPopSciAuthors
} from '@/api/system'
import type { 
  MushroomSummaryVO, 
  DiseasePestSummaryVO, 
  PopSciContentSummaryVO,
  PopSciAuthorNameVO
} from '@/api/interface'

// 【新增】导入科普内容详情接口
import { getPopSciContentById } from '@/api/system'
import type { PopSciContentVO } from '@/api/interface'

// 【新增】导入病虫害详情接口
import { getDiseasePestById } from '@/api/system'
import type { DiseasePestVO } from '@/api/interface'
import { title } from 'node:process'

export function useSearch() {
  // 在函数内部调用 useRoute 和 useRouter，确保在 setup 上下文中
  const route = useRoute()
  const router = useRouter()
  const total = ref(0);
  // 响应式数据
  const activeTab = ref('mushroom')
  const currentPage = ref(1)
  const pageSize = ref(8)  // 修改：每页8条
  const loading = ref(false)

  // 【新增】科普详情弹窗相关状态
  const popSciDetailDialogVisible = ref(false)
  const selectedPopSci = ref<PopSciContentVO | null>(null)
  const popSciDetailLoading = ref(false)
  
  // 【新增】菌种详情弹窗相关状态
  const mushroomDetailDialogVisible = ref(false)
  const selectedMushroomId = ref<number | null>(null)

  // 【新增】病虫害详情弹窗相关状态
  const diseasePestDetailDialogVisible = ref(false)
  const selectedDiseasePest = ref<DiseasePestVO | null>(null)
  const diseasePestDetailLoading = ref(false)

  const mushroomList = ref<MushroomSummaryVO[]>([])
  const diseasePestList = ref<DiseasePestSummaryVO[]>([])
  const popSciContentList = ref<PopSciContentSummaryVO[]>([])
  const popSciAuthorList = ref<PopSciAuthorNameVO[]>([])

  // 计算属性：是否有内容（改为检查当前列表是否为空）
  const hasContent = computed(() => {
    if (activeTab.value === 'mushroom') return mushroomList.value.length > 0
    if (activeTab.value === 'diseasePest') return diseasePestList.value.length > 0
    if (activeTab.value === 'article' || activeTab.value === 'video') return popSciContentList.value.length > 0
    if (activeTab.value === 'popsciAuthor') return popSciAuthorList.value.length > 0
    return false
  })
  // ========== 新增：处理科普号点击，跳转到用户主页 ==========
const handlePopSciAuthorClick = (item: PopSciAuthorNameVO) => {
  if (!item.userId) {
    ElMessage.warning('用户ID不存在');
    return;
  }
  console.log(item.userId) // 跳转到用户主页，传递userId作为路由参数
  router.push(`/create/${item.userId}`);
};
  // 空状态提示
  const getEmptyTip = () => {
    const tips = {
      mushroom: '暂无匹配的菌种卡片',
      diseasePest: '暂无匹配的病虫害信息',
      article: '暂无匹配的图文内容',
      video: '暂无匹配的视频内容',
      popsciAuthor: '暂无匹配的科普号'
    }
    return tips[activeTab.value as keyof typeof tips] || '暂无数据'
  }

  // 获取搜索关键词
  const getSearchKeyword = () => {
    return route.query.query ? String(route.query.query) : ''
  }

  // 切换 tab
  const switchTab = (tab: string) => {
    activeTab.value = tab
    currentPage.value = 1
    fetchData()
  }

  // 统一数据获取方法
  const fetchData = () => {
    if (activeTab.value === 'mushroom') {
      fetchMushroomList()
    } else if (activeTab.value === 'diseasePest') {
      fetchDiseasePestList()
    } else if (activeTab.value === 'article' || activeTab.value === 'video') {
      fetchPopSciContentList()
    } else if (activeTab.value === 'popsciAuthor') {
      fetchPopSciAuthorList()
    }
  }

  // 菌种列表获取
  const fetchMushroomList = async () => {
    loading.value = true
    try {
      const res = await getMushroomSummaryPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,  // 现在是8
        mushroomName: getSearchKeyword()
      })
      if (res.code === 0 && res.data) {
        mushroomList.value = res.data.items || []
        total.value = res.data.total || 0
      } else {
        mushroomList.value = []
        total.value = 0
      }
    } catch (error) {
      console.error('获取菌种列表失败:', error)
      ElMessage.error('获取菌种列表失败')
    } finally {
      loading.value = false
    }
  }

  // 病虫害列表获取（类似修改其他 fetch 函数）
  const fetchDiseasePestList = async () => {
    loading.value = true
    try {
      const res = await getDiseasePestSummaryPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,  // 现在是8
        diseaseName: getSearchKeyword(),
        // type 按需处理
      })
      if (res.code === 0 && res.data) {
        diseasePestList.value = res.data.items || []
        total.value = res.data.total || 0
      } else {
        diseasePestList.value = []
        total.value = 0
      }
    } catch (error) {
      console.error('获取病虫害列表失败:', error)
      ElMessage.error('获取病虫害列表失败')
    } finally {
      loading.value = false
    }
  }

  // 科普内容获取
  const fetchPopSciContentList = async () => {
    loading.value = true
    try {
      const contentType = activeTab.value === 'article' ? 'ARTICLE' : 'VIDEO'
      const res = await getAllPopSciContents({
        pageNum: currentPage.value,
        pageSize: pageSize.value,  // 现在是8
        title: getSearchKeyword(),
        contentType
      })
      if (res.code === 0 && res.data) {
        popSciContentList.value = res.data.items || []
        total.value = res.data.total || 0
      } else {
        popSciContentList.value = []
        total.value = 0
      }
    } catch (error) {
      console.error('获取科普内容失败:', error)
      ElMessage.error('获取科普内容失败')
    } finally {
      loading.value = false
    }
  }

  // 科普号获取
  const fetchPopSciAuthorList = async () => {
    loading.value = true
    try {
      const res = await searchPopSciAuthors({
        pageNum: currentPage.value,
        pageSize: pageSize.value,  // 现在是8
        username: getSearchKeyword()
      })
      if (res.code === 0 && res.data) {
        popSciAuthorList.value = res.data.items || []
        total.value = res.data.total || 0
      } else {
        popSciAuthorList.value = []
        total.value = 0
      }
    } catch (error) {
      console.error('获取科普号失败:', error)
      ElMessage.error('获取科普号失败')
    } finally {
      loading.value = false
    }
  }

  // 分页变化处理
  const handleSizeChange = (val: number) => {
    pageSize.value = val
    currentPage.value = 1
    fetchData()
  }

  const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
  }

  // 【新增】打开科普详情弹窗方法
  const openPopSciDetail = async (item: PopSciContentSummaryVO) => {
    popSciDetailLoading.value = true
    try {
      const res = await getPopSciContentById(item.id)
      if (res.code === 0 && res.data) {
        selectedPopSci.value = res.data as PopSciContentVO
        popSciDetailDialogVisible.value = true
      } else {
        ElMessage.error('获取科普详情失败')
      }
    } catch (error) {
      console.error('获取科普详情失败:', error)
      ElMessage.error('获取科普详情失败')
    } finally {
      popSciDetailLoading.value = false
    }
  }

  // 【新增】关闭科普详情弹窗方法
  const closePopSciDetail = () => {
    popSciDetailDialogVisible.value = false
    selectedPopSci.value = null
  }

  // 【修改】点击处理：改为打开菌种详情弹窗而非跳转
  const handleMushroomClick = (item: MushroomSummaryVO) => {
    selectedMushroomId.value = item.id;
    mushroomDetailDialogVisible.value = true;
  }

  // 【修改】点击处理：改为打开病虫害详情弹窗而非跳转
  const handleDiseasePestClick = async (item: DiseasePestSummaryVO) => {
    diseasePestDetailLoading.value = true
    try {
      const res = await getDiseasePestById(item.id)
      if (res.code === 0 && res.data) {
        selectedDiseasePest.value = res.data as DiseasePestVO
        diseasePestDetailDialogVisible.value = true
      } else {
        ElMessage.error('获取病虫害详情失败')
      }
    } catch (error) {
      console.error('获取病虫害详情失败:', error)
      ElMessage.error('获取病虫害详情失败')
    } finally {
      diseasePestDetailLoading.value = false
    }
  }

  // 【新增】关闭病虫害详情弹窗方法
  const closeDiseasePestDetail = () => {
    diseasePestDetailDialogVisible.value = false
    selectedDiseasePest.value = null
  }

  // 【修改】图文/视频点击处理：改为打开弹窗而非跳转
  const handlePopSciContentClick = (item: PopSciContentSummaryVO) => {
    openPopSciDetail(item)
  }

  // 监听路由参数变化（当 query 变化时重新搜索）
  watch(
    () => route.query.query,
    (newQuery, oldQuery) => {
      if (newQuery !== oldQuery) {
        currentPage.value = 1
        fetchData()
      }
    },
    { immediate: true }
  )

  // 返回所有需要暴露的属性和方法
  return {
    activeTab,
    currentPage,
    pageSize,
    total,
    loading,
    mushroomList,
    diseasePestList,
    popSciContentList,
    popSciAuthorList,
    hasContent,
    getEmptyTip,
    switchTab,
    handleSizeChange,
    handleCurrentChange,
    handleMushroomClick,
    handleDiseasePestClick,
    handlePopSciContentClick,
    handlePopSciAuthorClick,
    // 【新增】弹窗相关状态和方法
    popSciDetailDialogVisible,
    selectedPopSci,
    popSciDetailLoading,
    openPopSciDetail,
    closePopSciDetail,
    // 【新增】菌种详情弹窗相关状态
    mushroomDetailDialogVisible,
    selectedMushroomId,
    // 【新增】病虫害详情弹窗相关状态和方法
    diseasePestDetailDialogVisible,
    selectedDiseasePest,
    diseasePestDetailLoading,
    closeDiseasePestDetail,
  }
}