// src/views/home/usePopSciRecommend.ts
import { ref, computed, onMounted } from 'vue'
import { ElNotification, ElMessage } from 'element-plus'
import { getRecommendedArticles, getRecommendedVideos,getPopSciContentById,Result,getRandomPopSciAuthors, getPopSciAuthorDetail } from '@/api/system'
import { PopSciContentVO,PopSciContentSummaryVO ,PopSciContentVOWithExtra,PopSciAuthorVO, UserVO} from '@/api/interface'
import { useRouter } from 'vue-router' // 如需路由跳转可保留，当前未使用

// 推荐图文/视频/科普号 独立功能封装
export function usePopSciRecommend() {
  // ========== 1. 推荐图文核心变量 ==========
  const recommendArticles = ref<PopSciContentSummaryVO[]>([])
  const articleTotal = ref(0)
  const articleLoading = ref(false)

  // ========== 2. 热门视频核心变量 ==========
  const recommendVideos = ref<PopSciContentSummaryVO[]>([])
  const videoTotal = ref(0)
  const videoTotalPages = ref(1)
  const videoLoading = ref(false)
  const currentVideoPage = ref(1)
  const videoPageSize = ref(4)

  // ========== 3. 推荐科普号核心变量 ==========
  const videoAccountGroups = ref<{
  id: number;
  name: string;
  avatarUrl: string;
}[][]>([]) // 清空硬编码数据，改为空数组
const currentAccountGroup = ref(0)
const accountLoading = ref(false) // 新增：科普号加载状态

  // ========== 4. 科普详情弹窗（关联推荐图文/视频） ==========
  const popSciDetailDialogVisible = ref(false)
  const selectedPopSci = ref<PopSciContentVOWithExtra | null>(null)
  const popSciDetailLoading = ref(false)
  
  // 【新增】处理收藏状态更新的方法
  const handleCollectionUpdate = (update: { id: number; isCollection: boolean; collectCount: number }) => {
    if (!selectedPopSci.value || selectedPopSci.value.id !== update.id) return;
    
    // 更新当前弹窗中的数据（selectedPopSci 是 PopSciContentVOWithExtra 类型，包含 isCollection 和 collectCount）
    selectedPopSci.value.isCollection = update.isCollection;
    selectedPopSci.value.collectCount = update.collectCount;
    
    // 注意：recommendArticles 和 recommendVideos 是 PopSciContentSummaryVO 类型
    // 该类型没有 isCollection 和 collectCount 字段，所以不再更新列表项，避免潜在错误
    // 如未来需同步列表状态，应由后端在 SummaryVO 中也返回这两个字段
  };
  // ========== 5. 推荐图文核心方法 ==========
  const loadRecommendArticles = async () => {
    try {
      articleLoading.value = true
      const res = await getRecommendedArticles()
      if (res.code === 0 && Array.isArray(res.data)) {
        recommendArticles.value = res.data
        articleTotal.value = res.data.length
      }
    } catch (error) {
      ElMessage.error('获取推荐图文失败，请稍后重试')
      console.error('加载图文失败：', error)
    } finally {
      articleLoading.value = false
    }
  }
// 添加高度计算
const commentListHeight = ref(0)

onMounted(() => {
  nextTick(() => {
    const commentListEl = document.querySelector('.comment-list')
    if (commentListEl) {
      commentListHeight.value = commentListEl.clientHeight
      console.log('评论列表高度:', commentListHeight.value)
    }
  })
})
  // ========== 6. 热门视频核心方法 ==========
  const loadRecommendVideos = async () => {
    try {
      videoLoading.value = true
      const res = await getRecommendedVideos()
      if (res.code === 0 && Array.isArray(res.data)) {
        recommendVideos.value = res.data
        videoTotal.value = res.data.length
        videoTotalPages.value = 1
      }
    } catch (error) {
      ElMessage.error('获取推荐视频失败，请稍后重试')
      console.error('加载视频失败：', error)
    } finally {
      videoLoading.value = false
    }
  }

  // 视频分页切换（无更多内容，仅提示）
  const changeVideoPage = (direction: 'prev' | 'next') => {
    ElMessage.info('已加载全部推荐视频，暂无更多内容')
  }

  // ========== 7. 推荐科普号核心方法 ==========
  // ========== 新增：加载随机科普号方法 ==========
const loadRandomPopSciAuthors = async () => {
  try {
    accountLoading.value = true
    const res = await getRandomPopSciAuthors()
    if (res.code === 0 && Array.isArray(res.data)) {
      const popSciList = res.data as (UserVO)[]
      // 将接口返回的10条数据拆分为两组（参考原有分组结构：6条+4条 或 6条+6条）
      const group1 = popSciList.slice(0, 6).map(item => ({
        id: item.userId,
        name: item.username,
        avatarUrl: item.userAvatar || '' // 兼容avatar/userAvatar字段
      }))
      const group2 = popSciList.slice(6).map(item => ({
        id: item.userId,
        name: item.username,
        avatarUrl: item.userAvatar || ''
      }))
      videoAccountGroups.value = [group1, group2]
    } else {
      ElMessage.warning('暂无随机科普号数据，展示默认内容')
    }
  } catch (error) {
    ElMessage.error('获取随机科普号失败，请稍后重试')
    console.error('加载科普号失败：', error)
  } finally {
    accountLoading.value = false
  }
}
  // 科普号换一换：重新请求接口获取新的随机数据
const refreshVideoAccount = async () => {
  // 重置分组索引为0（每次换一换默认显示第一组）
  currentAccountGroup.value = 0;
  // 直接调用加载方法，重新请求接口
  await loadRandomPopSciAuthors();
  // 可选：提示换一换成功（如果需要）
  // ElMessage.success('已为您刷新科普号列表');
};

  // 获取当前科普号列表
  const getCurrentAccountList = computed(() => {
    return videoAccountGroups.value[currentAccountGroup.value] || []
  })

  // ========== 8. 科普详情弹窗交互方法（核心重构：通过ID查详情） ==========
  /**
   * 打开科普详情弹窗 - 重构后：接收列表摘要项 → 按ID查完整详情 → 填充弹窗
   * @param item 列表中的摘要数据（PopSciContentSummaryVO，仅含ID/标题/封面等基础信息）
   */
  const openPopSciDetail = async (item: PopSciContentSummaryVO) => {
    // 边界判断：item无ID直接返回，避免无效请求
    if (!item || !item.id) {
      ElMessage.warning('内容ID不存在，无法查看详情')
      return
    }
    try {
      // 1. 开启加载状态，重置弹窗数据
      popSciDetailLoading.value = true
      selectedPopSci.value = null
      // 2. 调用详情API，根据ID查询完整信息（核心！）
      const res = await getPopSciContentById(item.id)as Result<PopSciContentVOWithExtra>;
      // 3. 接口请求成功，且有数据
      if (res.code === 0 && res.data) {
        selectedPopSci.value = res.data // 填充完整VO数据
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

  // 关闭科普详情弹窗 - 优化：重置数据，避免下次打开残留旧数据
  const closePopSciDetail = () => {
    popSciDetailDialogVisible.value = false
    selectedPopSci.value = null
    // 可选：关闭时也重置点赞状态（如果弹窗内的点赞状态在useHome中，需联动）
  }

  // ========== 9. 初始化推荐数据 ==========
  onMounted(() => {
    loadRecommendArticles()
    loadRecommendVideos()
    loadRandomPopSciAuthors() // 新增：加载科普号数据
  })

  // ========== 导出所有推荐相关功能 ==========
  return {
    // 推荐图文
    recommendArticles,
    articleTotal,
    articleLoading,
    loadRecommendArticles,
    // 热门视频
    recommendVideos,
    videoTotal,
    videoTotalPages,
    videoLoading,
    currentVideoPage,
    videoPageSize,
    loadRecommendVideos,
    changeVideoPage,
    // 推荐科普号
    videoAccountGroups,
    currentAccountGroup,
    refreshVideoAccount,
    getCurrentAccountList,
    accountLoading, // 科普号加载状态（用于 UI 加载中提示）
    // 科普详情弹窗（🚨 新增 popSciDetailLoading 导出）
    popSciDetailDialogVisible,
    popSciDetailLoading, // 弹窗加载状态
    selectedPopSci,
    openPopSciDetail,
    closePopSciDetail,
    handleCollectionUpdate, // 【新增】收藏状态更新处理方法
     commentListHeight
  }
}