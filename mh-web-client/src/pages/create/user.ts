import { ref, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import { UserStore } from '@/stores/modules/user';
import { ElMessage } from "element-plus";
import { getUserContentList, getUserFavoritePopSciList,getUserById} from '@/api/system';
import { PopSciContentSummaryVO, PopSciContentVO, PopSciContentTypeEnum,UserVO } from '@/api/interface';

// 响应式数据补充
const contentDetail = ref<PopSciContentVO | null>(null);
const openDetailModal = ref<boolean>(false);

export function useUserProfile() {
  const route = useRoute();
  const userStore = UserStore();

  // ---------------- 响应式数据 ----------------
  const currentUid = ref(userStore.userInfo.userId?.toString() || ''); // 当前登录用户ID
  // 修改：优先使用 userId 参数，其次使用 id 参数，最后使用当前用户ID
  const uid = ref<string>(
    route.params.userId 
      ? route.params.userId.toString() 
      : (route.params.id ? route.params.id.toString() : currentUid.value)
  );
  const type = ref<number>(1); // 1=笔记，3=收藏
  const subType = ref<'article' | 'video'>('article');
  const _isFollow = ref<boolean>(false);
  const loading = ref<boolean>(true);
  const hasContent = ref<boolean>(false);
  const contentList = ref<PopSciContentSummaryVO[]>([]);
  
  // 【新增】分页相关数据
  const currentPage = ref<number>(1);
  const pageSize = ref<number>(8);
  const total = ref<number>(0);
  
  const userInfo = ref({
    avatar: '',
    username: '',
    description: '',
    userType: 1,
    trendCount: 0 
  });

  // ---------------- 核心方法 ----------------
  const toPage = (tabType: 1 | 3) => {
    type.value = tabType;
    currentPage.value = 1; // 【新增】切换主 tab 时重置页码为 1
    loadContent();
  };

  const handleSubTabChange = (val: string) => {
    subType.value = val as 'article' | 'video';
    currentPage.value = 1; // 【新增】切换子 tab 时重置页码为 1
    loadContent();
  };
  
  // 【新增】分页变化处理
  const handlePageChange = (page: number) => {
    currentPage.value = page;
    loadContent();
  };

  // 关注方法
  const follow = (fid: string, actionType: number) => {
    ElMessage.info('关注功能暂未实现');
  };

  /**
   * 加载用户信息
   */
  const loadUserInfo = async () => {
    try {
      // console.log("进入loadUserInfo :",currentUid.value);
      // console.log("当前uid的值：", uid.value)
      // 添加安全检查：确保 uid.value 不为空
      if (!uid.value) {
        console.error('uid is empty, using current user ID');
        uid.value = currentUid.value;
      }
      
      if (uid.value === currentUid.value && userStore.isLoggedIn) {

        // 查看自己
        userInfo.value = {
          avatar: userStore.userInfo.avatarUrl || '',
          username: userStore.userInfo.username || '',
          description: userStore.userInfo.introduction || '',
          userType: userStore.userInfo.userType || 1,
          trendCount: 0 
        };
      } else {
        // 查看他人，调用接口获取用户信息
      const res = await getUserById(uid.value); // 假设 getUserInfo 接收用户 ID
      if (res.code === 0 && res.data) {
        const data = res.data as UserVO; // 断言
        userInfo.value = {
          avatar: data.userAvatar || '',
          username: data.username || '',
          description: data.introduction || '',
          userType: data.userType || 1,
          trendCount:  0
        };
      } else {
        // 处理失败情况
        console.error('获取用户信息失败', res.message);
        // 可选：给出默认值
        userInfo.value = {
          avatar: '',
          username: '用户' + uid.value,
          description: '',
          userType: 1,
          trendCount: 0
        };
      }
    }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    }
  };

  /**
   * 重构 loadContent：支持查看自己和他人
   */
  const loadContent = () => {
    // 基础校验：检查目标用户ID
    if (!uid.value) {
      loading.value = false;
      hasContent.value = false;
      ElMessage.error('用户ID 不能为空');
      return;
    }

    loading.value = true;

    // 区分笔记/收藏调用不同API，传入目标用户ID
    if (type.value === 1) {
      // 笔记：调用getUserContentList（传目标用户ID）
      getUserContentList({
        userId: uid.value, 
        mediaType: subType.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }).then(res => {
        loading.value = false;
        if (res.code === 0) {
          // 【新增】强制限制最多显示 8 条数据，确保符合项目规范
          const items = res.data.items || [];
          contentList.value = items.slice(0, pageSize.value);
          total.value = res.data.total || 0;
          hasContent.value = contentList.value.length > 0;
        } else {
          contentList.value = [];
          total.value = 0;
          hasContent.value = false;
        }
      }).catch((error) => {
        console.error('加载用户笔记失败:', error);
        ElMessage.error('加载用户笔记失败');
        loading.value = false;
        hasContent.value = false;
        contentList.value = [];
        total.value = 0;
      });
    } else if (type.value === 3) {
      // 收藏：调用getUserFavoritePopSciList（传目标用户ID）
      getUserFavoritePopSciList({
        userId: uid.value,
        favoriteType: subType.value === 'article' ? 1 : 2,
        mushroomId: null,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }).then(res => {
        loading.value = false;
        if (res.code === 0) {
          // 【新增】强制限制最多显示 8 条数据，确保符合项目规范
          const items = res.data.items || [];
          contentList.value = items.slice(0, pageSize.value);
          total.value = res.data.total || 0;
          hasContent.value = contentList.value.length > 0;
        } else {
          contentList.value = [];
          total.value = 0;
          hasContent.value = false;
        }
      }).catch((error) => {
        console.error('加载用户收藏失败:', error);
        ElMessage.error('加载用户收藏失败');
        loading.value = false;
        hasContent.value = false;
        contentList.value = [];
        total.value = 0;
      });
    }
  };

  const getContentEmptyTip = (): string => {
    const typeText = type.value === 1 ? '笔记' : '收藏';
    const subTypeText = subType.value === 'article' ? '图文' : '视频';
    return `暂无${typeText}-${subTypeText}内容`;
  };

  // 重构 initData 函数，移除对 userInfo.value 的依赖
  const initData = async () => {
    // 先加载用户信息
    await loadUserInfo();
    
    // 重新获取用户类型（从 userInfo.value 获取）
    const isOwner = currentUid.value === uid.value;
    const isNormalUser = userInfo.value?.userType === 1;
    
    // 重置 type 和 subType
    if (isOwner && isNormalUser) {
      // 普通用户本人：默认进入收藏 Tab
      type.value = 3;
    } else {
      // 其他情况（包括访客查看他人、专家用户本人）：默认进入笔记 Tab
      type.value = 1;
    }
    
    // 重置 subType
    if (!subType.value) {
      subType.value = 'article';
    }
    
    // 加载内容
    loadContent();
  };

  // ---------------- 生命周期 & 监听 ----------------
  // ---------------- 生命周期 & 监听 ----------------
onMounted(async () => {
  // 首次加载：await 确保异步完成
  await initData();
  
  // 修复：watch 移到 onMounted 内部但独立声明（正确语法）
  // 监听路由参数变化（添加 immediate: true 确保首次也触发校验）
  const stopWatch = watch(
    () => [route.params.userId, route.params.id],
    async ([newUserId, newId]) => {
      let newUid = '';
      // 优化：空值判断更严谨
      if (newUserId != null && newUserId !== '') {
        newUid = String(newUserId);
      } else if (newId != null && newId !== '') {
        newUid = String(newId);
      } else {
        newUid = currentUid.value;
      }
      
      // 仅当目标用户变化时才重新加载
      if (uid.value !== newUid) {
        uid.value = newUid;
        // 异步等待 initData 完成，避免数据加载混乱
        await initData();
      }
    },
    { 
      immediate: true, // 关键：首次加载就触发监听（补全原有逻辑）
      deep: true // 监听对象/数组内部变化
    }
  );

  // 可选：组件卸载时停止监听（防止内存泄漏）
  onUnmounted(() => {
    stopWatch();
  });
});

  // ---------------- 返回对外暴露的变量/方法 ----------------
  return {
    currentUid,
    uid,
    type,
    subType,
    _isFollow,
    loading,
    hasContent,
    contentList,
    userInfo,
    contentDetail,
    openDetailModal,
    // 【新增】分页相关
    currentPage,
    pageSize,
    total,
    toPage,
    handleSubTabChange,
    handlePageChange, // 【新增】导出分页处理方法
    follow,
    loadContent,
    getContentEmptyTip,
    initData
  };
}