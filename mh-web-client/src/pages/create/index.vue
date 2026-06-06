<template>
  <div class="user-page">
    <!-- 用户信息区 -->
    <div class="user">
      <div class="user-info">
        <!-- 头像 -->
        <div class="avatar">
          <div class="avatar-wrapper">
            <img 
              :src="userInfo.avatar || defaultAvatar" 
              class="user-image" 
              style="border: 1px solid rgba(0, 0, 0, 0.08)" 
              alt="用户头像"
            />
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="info-part">
          <div class="info">
            <!-- 用户名 -->
            <div class="basic-info">
              <div class="user-basic">
                <div class="user-nickname">
                  <div class="user-name">{{ userInfo.username || '匿名用户' }}</div>
                </div>
              </div>
            </div>

            <!-- 个人简介 -->
            <div class="user-desc">
              <span v-if="!userInfo.description">这个人什么都没有写～</span>
              <span v-else>{{ userInfo.description }}</span>
            </div>

            <!-- 数据统计 -->
            <!-- <div class="data-info">
              <div class="user-interactions">
                <div>
                  <span class="count">{{ userInfo.trendCount || 0 }}</span>
                  <span class="shows">作品</span>
                </div>
                <div>
                  <span class="count">--</span>
                  <span class="shows">关注</span>
                </div>
                <div>
                  <span class="count">--</span>
                  <span class="shows">粉丝</span>
                </div>
              </div>
            </div> -->
          </div>
        </div>

        <!-- 更多按钮 -->
        <div class="more-btn" @click="goToCurrentPage" v-if="isOwner">
          <el-icon :size="20">
            <MoreFilled />
          </el-icon>
        </div>

        <!-- 关注按钮（权限控制：非当前用户才显示） -->
        <!-- <div class="tool-btn" v-show="uid !== currentUid">
          <el-button 
            type="info" 
            round 
            v-if="_isFollow" 
            @click="follow(uid, 1)"
            disabled
          >已关注</el-button>
          <el-button 
            type="danger" 
            round 
            v-else 
            @click="follow(uid, 0)"
            disabled
          >关注</el-button>
        </div> -->
      </div>
    </div>

    <!-- 粘性标签栏（笔记/收藏）+ 子tab（图文/视频） -->
    <div class="reds-sticky-box user-page-sticky">
      <div class="reds-sticky">
        <!-- 外层标签：笔记/收藏 -->
        <div class="tertiary center reds-tabs-list">
          <div
            v-if="showNoteTab"
            :class="type == 1 ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="toPage(1)"
          >
            <span>作品</span>
          </div>
          <div
            v-if="showCollectTab"
            :class="type == 3 ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="toPage(3)"
          >
            <span>收藏</span>
          </div>
        </div>

        <!-- 内层子tab：图文/视频（根据外层type显示） -->
        <div class="sub-tabs" v-if="(type === 1 || type === 3) && showNoteTab">
          <el-tabs 
            v-model="subType" 
            type="card" 
            class="sub-tab-container"
            @tab-change="handleSubTabChange"
          >
            <el-tab-pane label="图文" name="article">图文</el-tab-pane> <!-- 核心修改：imageText → article -->
            <el-tab-pane label="视频" name="video">视频</el-tab-pane>
          </el-tabs>
          <!-- ========== 修改：发布笔记按钮移至此处 ========== -->
          <!-- 发布按钮仅主人可见 -->
          <div class="publish-btn-container" v-if="isOwner">
            <el-button 
              type="primary" 
              @click="handlePublishClick"
            >
              发布笔记
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <!-- ========== 新增：批量操作栏 ========== -->
    <!-- 批量操作栏仅主人可见 -->
    <div v-if="contentList.length > 0 && isOwner" class="batch-actions flex justify-end mb-4 w-4/5 mx-auto">
      <el-checkbox 
        :model-value="selectedIds.length === contentList.length && contentList.length > 0"
        @change="toggleSelectAll" 
        class="mr-4"
      >
        全选
      </el-checkbox>
      <el-button 
        type="primary" text
        :disabled="!hasSelectedItems" 
        class="mr-1" 
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
      <span class="selected-count ml-2 text-gray-500 text-sm" v-if="selectedIds.length > 0">
        已选 {{ selectedIds.length }} 条
      </span>
    </div>
    <!-- 内容展示区 -->
    <div class="feeds-tab-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated style="width: 100%" />
      </div>

      <!-- 内容展示（传递type和subType） -->
      <div v-else class="content-wrapper">
        <div class="content-tip" v-if="!hasContent">
          <el-empty :description="getContentEmptyTip()" />
        </div>
        <div v-else class="content-list">
          <!-- 重构内容项：参考热门视频样式 -->
          <div 
            class="video-item" 
            v-for="item in contentList" 
            :key="item.id"
            @click="handleItemClick(item)" 
          >
          <!-- ========== 新增：多选框 ========== -->
            <!-- 修复复选框：用 :model-value + @click.stop -->
            <div class="select-checkbox" @click.stop="toggleSelectPlan(item.id)" v-if="isOwner">
              <el-checkbox :model-value="isPlanSelected(item.id)" />
            </div>
            <!-- 封面容器：区分图文/视频 -->
            <div class="video-cover-wrapper">
              <img 
                :src="(item.contentType === 'article' ? item.articleCoverUrl : item.videoCoverUrl) || defaultCover" 
                :alt="item.contentType === 'article' ? '图文封面' : '视频封面'" 
                class="video-cover" 
                loading="lazy" 
              />
              <!-- 视频添加播放图标 -->
              <el-icon class="play-btn" :size="40" v-if="item.contentType === 'video'">
                <VideoPlay />
              </el-icon>
            </div>
            <!-- 标题：复用热门视频的video-title样式 -->
            <div class="video-text-content">
              <h3 class="video-title" :title="item.theme">{{ item.theme }}</h3>
              <div class="video-meta">
                <span :title="item.publishDate">发布时间：{{ item.publishDate }}</span>
              </div>
              <span v-if="item.mushroomName" class="mushroom-tag" :title="item.mushroomName">
                {{ item.mushroomName }}
              </span>
            </div>
            
            <!-- ========== Moved audit status to top-right ========== -->
            <!-- 审核状态标签仅在笔记Tab且showNoteTab为true时显示 -->
            <span 
              v-if="type === 1 && item.contentStatus !== undefined && showNoteTab && isOwner" 
              class="audit-status-tag"
              :class="getAuditStatusClass(item.contentStatus)"
              :title="getAuditStatusText(item.contentStatus)"
            >
              {{ getAuditStatusText(item.contentStatus) }}
            </span>
            
            <!-- ========== 新增：编辑按钮 ========== -->
            <!-- 操作按钮组：触发正确的方法 -->
              <div class="item-actions" v-if="isOwner">
                <!-- 笔记tab下显示全部按钮 -->
                <template v-if="type === 1">
                  <el-button 
                    size="default" 
                    type="info" 
                    text 
                    @click.stop="handleViewClick(item)"
                  >
                    查看
                  </el-button>
                  <el-button 
                    size="default" 
                    type="primary" text
                    @click.stop="handleEditClick(item)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                  size="default" 
                  type="danger" 
                  text 
                  @click.stop="handleSingleDelete(item.id)"
                >
                  删除
                </el-button>
                </template>
                <!-- 收藏tab下只显示删除按钮 -->
                <template v-else-if="type === 3">
                  <el-button 
                    size="default" 
                    type="danger" 
                    text 
                    @click.stop="handleSingleUnfavorite(item.id)"
                  >
                    删除
                  </el-button>
                </template>
              </div>
          </div>
        </div>
        <!-- 【新增】分页组件 -->
        <div v-if="contentList.length > 0" class="pagination-container mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
    <!-- ========== 新增：编辑弹窗 ========== -->
      <PopSciEditDialog
        v-model="editDialogVisible"
        :edit-data="selectedEditItem"
        :content-type="editContentType"
        :author-id="currentUid"
        @save-success="handleEditSuccess"
      />
      
      <!-- 原有详情弹窗（保留） -->
      <PopSciDetailDialog
      v-model="openDetailModal"
      :detail-data="contentDetail"
      :loading="detailLoading"
    />
    </div>
</template>

<script lang="ts" setup>
// 引入Composables函数
import { useUserProfile } from './user.ts';
import defaultCover from '@/assets/default.jpg'
import defaultAvatar from '@/assets/user.jpg'
import PopSciEditDialog from './popSciEditDialog.vue'
import PopSciDetailDialog from '../popSci/PopSciDetailDialog.vue'; // 替换为你的实际路径
// 静态资源&UI组件
import { ElEmpty, ElSkeleton, ElTabs, ElTabPane, ElIcon, ElMessage, ElMessageBox, ElPagination } from "element-plus";
import { deletePopSciContent, deletePopSciContents,getPopSciContentById, cancelCollectPopSci, batchCancelCollectPopSci ,getUserInfo} from '@/api/system' // 菌种列表接口
import { PopSciContentVOWithExtra } from '@/api/interface'
import { usePopSciRecommend } from '../popSci/usePopSci.ts'
// 关键：导入VideoPlay图标并注册
import { VideoPlay, MoreFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

// 解构获取所有变量和方法
const {
  // 响应式数据
  currentUid,
  uid, // 添加 uid
  type,
  subType,
  _isFollow,
  loading,
  hasContent,
  contentList,
  userInfo,
  // 【新增】分页相关
  currentPage,
  pageSize,
  total,
  toPage,
  handleSubTabChange,
  handlePageChange, // 【新增】分页变化处理
  loadContent, // 【新增】内容加载方法
  follow,
  getContentEmptyTip,
} = useUserProfile();

const router = useRouter();

// 更多按钮点击事件
const goToCurrentPage = () => {
  router.push('/user');
}

// ========== 新增：isOwner 计算属性 ==========
const isOwner = computed(() => {
  console.log("进入loadUserInfo :",currentUid.value);
      // 这里需要调用获取用户信息的接口，但根据现有代码，我们暂时使用userStore的信息
      // 如果是查看他人主页，需要额外的API来获取目标用户信息
  console.log("当前uid的值：", uid.value)
  return currentUid.value === uid.value;
});

// ========== 新增：showNoteTab 计算属性 ==========
const showNoteTab = computed(() => {
  // 本人：userType !== 1 时显示笔记 Tab
  if (isOwner.value) {
    return userInfo.value?.userType !== 1;
  }
  // 访客：默认显示笔记 Tab
  return true;
});

// ========== 新增：showCollectTab 计算属性 ==========
const showCollectTab = computed(() => {
  // 只有本人可以查看收藏
  return isOwner.value;
});

// ========== 优化默认 Tab 逻辑 ==========
// 普通用户本人：默认进入收藏 Tab
// 其他情况：默认进入笔记 Tab
const isOwnerValue = currentUid.value === uid.value;
const isNormalUser = userInfo.value?.userType === 1;
if (!type.value) {
  if (isOwnerValue && isNormalUser) {
    type.value = 3; // 收藏
  } else {
    type.value = 1; // 笔记
  }
}
// 如果没有subType值，默认设置为'article'（图文）
if (!subType.value) {
  subType.value = 'article';
}

// ========== 新增：审核状态工具函数 ==========
const getAuditStatusText = (status: number | string): string => {
  // 先处理字符串枚举值
  if (typeof status === 'string') {
    const stringMap: Record<string, string> = {
      'PENDING': '审核中',
      'PASSED': '已通过', 
      'REJECTED': '未通过'
    }
    return stringMap[status] || '未知状态'
  }
  // 处理数字值（兼容旧逻辑）
  const statusMap: Record<number, string> = {
    1: '审核中',
    2: '已通过',
    3: '未通过'
  }
  return statusMap[status] || '未知状态'
}

const getAuditStatusClass = (status: number | string): string => {
  // 先处理字符串枚举值
  if (typeof status === 'string') {
    const stringMap: Record<string, string> = {
      'PENDING': 'audit-pending',
      'PASSED': 'audit-passed',
      'REJECTED': 'audit-rejected'
    }
    return stringMap[status] || 'audit-unknown'
  }
  // 处理数字值（兼容旧逻辑）
  const classMap: Record<number, string> = {
    0: 'audit-pending',
    1: 'audit-passed',
    2: 'audit-rejected'
  }
  return classMap[status] || 'audit-unknown'
}

// ========== 新增：计算属性用于过滤内容列表 ========== 
const filteredContentList = computed(() => {
  console.log("进入filteredContentList :",type.value);
  console.log("进入filteredContentList :",contentList.value);
  // 如果不是笔记或收藏类型，返回全部内容
  if (type.value !== 1 && type.value !== 3) {
    console.log("进入filteredContentList :",contentList.value);
    return contentList.value;
  }
  
  // 根据 subType 过滤内容
  return contentList.value.filter(item => {
    // 统一处理 contentType 转换
    let itemContentType: string;
    if (typeof item.contentType === 'number') {
      itemContentType = item.contentType === 1 ? 'article' : 'video';
    } else {
      // 处理字符串值，确保标准化
      const normalized = String(item.contentType).toLowerCase();
      itemContentType = normalized === 'imagetext' ? 'article' : normalized;
    }
    
    return itemContentType === subType.value;
  });
});

// ========== 详情弹窗相关数据（严格匹配 PopSciDetailDialog 的 Props） ==========
const openDetailModal = ref(false) // 控制详情弹窗显隐
const contentDetail = ref<any>(null) // 传递给弹窗的详情数据
const detailLoading = ref(false) // 弹窗加载状态

// ========== 编辑弹窗相关数据（保留） ==========
const editDialogVisible = ref(false)
const selectedEditItem = ref<any>(null)
const editContentType = ref<'article' | 'video'>('article')

// ========== 新增：发布笔记方法 ==========
const handlePublishClick = () => {
  // Clear any existing edit data to ensure create mode
  selectedEditItem.value = null
  editContentType.value = 'article' // Default to article type
  editDialogVisible.value = true
}

// 多选删除相关数据（保留）
const selectedIds = ref<number[]>([])
const selectAll = ref(false)
// ========== 修正：查看按钮触发详情弹窗 ==========
const { openPopSciDetail } = usePopSciRecommend()

// ========== 修正：查看按钮直接调用复用的方法 ==========
// ========== 2. 修正 handleViewClick：抄核心逻辑，用自己的变量 ==========
const handleViewClick = async (item: any) => {
  if (!item || !item.id) {
    ElMessage.warning('内容ID不存在，无法查看详情')
    return
  }
  
  try {
    // 1. 开启加载状态
    detailLoading.value = true
    contentDetail.value = null
    
    // 2. 核心：抄 usePopSciRecommend 里的接口调用
    const res = await getPopSciContentById(item.id) as any
    
    if (res.code === 0 && res.data) {
      // 3. 用自己的 contentDetail 存数据
      contentDetail.value = res.data as PopSciContentVOWithExtra
      // 4. 用自己的 openDetailModal 打开弹窗
      openDetailModal.value = true
    } else {
      ElMessage.warning('该科普内容已下架或不存在')
    }
  } catch (error) {
    ElMessage.error('获取科普详情失败，请稍后重试')
    console.error('加载科普详情失败：', error)
  } finally {
    detailLoading.value = false
  }
}

// ========== 编辑按钮触发编辑弹窗（保留） ==========
const handleEditClick = async (item: any) => {
  if (!item || !item.id) {
    ElMessage.warning('内容ID不存在，无法编辑')
    return
  }

  try {
    // 1. 先调详情接口，拿到完整数据（和详情弹窗一样）
    const res = await getPopSciContentById(item.id) as any
    
    if (res.code === 0 && res.data) {
      // 2. 把完整数据传给编辑弹窗
      selectedEditItem.value = res.data as PopSciContentVOWithExtra
      
      // ✅ 关键：兼容枚举值和字符串
      const contentType = res.data.contentType
      editContentType.value = (contentType === 'article' || contentType === 1) ? 'article' : 'video'
      
      // 4. 打开编辑弹窗
      editDialogVisible.value = true
    } else {
      ElMessage.warning('该科普内容已下架或不存在')
    }
  } catch (error) {
    ElMessage.error('获取内容详情失败，请稍后重试')
    console.error('加载内容详情失败：', error)
  }
}


// ========== 编辑成功回调（保留） ==========
const handleEditSuccess = (data: any) => {
  ElMessage.success('笔记编辑成功！')
  editDialogVisible.value = false
  // 刷新列表（替换为实际刷新方法）
  // loadContentList()
}

// ========== 新增：多选逻辑 ==========
// ========== 新增缺失的计算属性和方法 ==========
// 1. 缺失的 hasSelectedItems
const hasSelectedItems = computed(() => {
  return selectedIds.value.length > 0
})

// 2. 缺失的 isPlanSelected
const isPlanSelected = (id: number) => {
  return selectedIds.value.includes(Number(id))
}

// 3. 缺失的 toggleSelectPlan
const toggleSelectPlan = (id: number) => {
  const numId = Number(id)
  const index = selectedIds.value.findIndex(item => item === numId)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(numId)
  }
}

// 4. 方法名统一：handleSelectAll → toggleSelectAll
const toggleSelectAll = (val: boolean) => {
  if (val) {
    selectedIds.value = contentList.value.map(item => Number(item.id))
  } else {
    selectedIds.value = []
  }
}

// 5. 单个删除中同步 toggleSelectPlan
const handleSingleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条内容吗？删除后无法恢复',
      '删除确认',
      { type: 'warning' }
    );

    const res = await deletePopSciContent(id);
    if (res.code === 0) {
      ElMessage.success('删除成功');
      // 修改：使用严格相等比较，避免类型转换问题
      contentList.value = contentList.value.filter(item => item.id !== id);
      // 同步选中状态
      toggleSelectPlan(id)
    } else {
      ElMessage.error(`删除失败：${res.message || '操作异常'}`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作异常，请重试');
    }
  }
};

// ========== 新增：收藏tab下单个取消收藏 ========== 
const handleSingleUnfavorite = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消收藏这条内容吗？',
      '取消收藏确认',
      { type: 'warning' }
    );

    const res = await cancelCollectPopSci(id);
    if (res.code === 0) {
      ElMessage.success('取消收藏成功');
      // 修改：使用严格相等比较，避免类型转换问题
      contentList.value = contentList.value.filter(item => item.id !== id);
      // 同步选中状态
      toggleSelectPlan(id)
    } else {
      ElMessage.error(`取消收藏失败：${res.message || '操作异常'}`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消收藏操作异常，请重试');
    }
  }
};

// ========== 修改：批量删除逻辑根据tab类型区分处理 ==========
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的内容');
    return;
  }

  try {
    // 根据当前tab类型显示不同的确认信息和调用不同接口
    const confirmMessage = type.value === 1 
      ? `确定要删除选中的 ${selectedIds.value.length} 条内容吗？删除后无法恢复`
      : `确定要取消收藏选中的 ${selectedIds.value.length} 条内容吗？`;
    const confirmTitle = type.value === 1 
      ? '批量删除确认'
      : '批量取消收藏确认';

    await ElMessageBox.confirm(
      confirmMessage,
      confirmTitle,
      { type: 'warning' }
    );

    let res;
    if (type.value === 1) {
      // 笔记tab：调用内容删除接口
      res = await deletePopSciContents(selectedIds.value);
    } else if (type.value === 3) {
      // 收藏tab：调用批量取消收藏接口
      res = await batchCancelCollectPopSci(selectedIds.value);
    }

    if (res.code === 0) {
      const successMessage = type.value === 1 
        ? `成功删除 ${selectedIds.value.length} 条内容`
        : `成功取消收藏 ${selectedIds.value.length} 条内容`;
      ElMessage.success(successMessage);
      
      // 修改：使用includes检查，避免类型转换问题
      contentList.value = contentList.value.filter(
        item => !selectedIds.value.includes(item.id)
      );
      selectedIds.value = [];
    } else {
      const errorMessage = type.value === 1 
        ? `批量删除失败：${res.message || '操作异常'}`
        : `批量取消收藏失败：${res.message || '操作异常'}`;
      ElMessage.error(errorMessage);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作异常，请重试');
    }
  }
};

// ========== 新增：处理列表项点击，打开详情弹窗 ==========
const handleItemClick = async (item: any) => {
  console.log('点击的item:', item);
  // 修改：放宽验证条件，允许id为0的情况
  if (!item || item.id === null || item.id === undefined || item.id === '') {
    console.info(item)
    ElMessage.warning('内容ID不存在，无法查看详情')
    return
  }
  
  try {
    // 1. 开启加载状态
    detailLoading.value = true
    contentDetail.value = null
    
    // 2. 调用接口获取详情
    const res = await getPopSciContentById(item.id) as any
    
    if (res.code === 0 && res.data) {
      // 3. 保存详情数据
      contentDetail.value = res.data as PopSciContentVOWithExtra
      // 4. 打开详情弹窗
      openDetailModal.value = true
    } else {
      ElMessage.warning('该科普内容已下架或不存在')
    }
  } catch (error) {
    ElMessage.error('获取科普详情失败，请稍后重试')
    console.error('加载科普详情失败：', error)
  } finally {
    detailLoading.value = false
  }
}

// ========== 新增：分页大小变化处理 ==========
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1; // 重置为第一页
  loadContent();
};

</script>

<style lang="less" scoped>
// 批量操作栏
.batch-actions {
  width: 80%;
  margin: 16px auto 0;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  
  .selected-count {
    font-size: 12px;
  }
  
  @media screen and (max-width: 1200px) {
    width: 90%;
  }
}

// 【新增】分页组件样式
.pagination-container {
  width: 80%;
  margin: 0 auto;
  padding: 20px 0;
  
  :deep(.el-pagination) {
    justify-content: center;
    
    .el-pager {
      li {
        &.is-active {
          color: var(--el-color-primary);
        }
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
    
    .btn-prev,
    .btn-next {
      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
  
  @media screen and (max-width: 1200px) {
    width: 90%;
  }
}

// 整体布局（匹配项目现有风格）
.user-page {
  background: #fff;
  min-height: 100vh;

  // 用户信息区
  .user {
    padding-top: 72px;
    display: flex;
    align-items: center;
    justify-content: center;

    .user-info {
      display: flex;
      justify-content: center;
      padding: 48px 0;
      width: 80%;
      position: relative;

      // 更多按钮样式
      .more-btn {
        position: absolute;
        top: 12px;
        right: 0;
        cursor: pointer;
        color: #666;
        transition: color 0.3s;
        
        &:hover {
          color: #333;
        }
      }

      // 头像
      .avatar {
        .avatar-wrapper {
          text-align: center;
          width: 250px;
          height: 175px;

          .user-image {
            border-radius: 50%;
            margin: 0 auto;
            width: 70%;
            height: 100%;
            object-fit: cover;
          }
        }
      }

      // 基础信息
      .info-part {
        position: relative;
        width: 100%;
        margin-left: 32px;

        .info {
          .basic-info {
            .user-basic {
              .user-nickname {
                .user-name {
                  font-weight: 600;
                  font-size: 24px;
                  line-height: 120%;
                  color: #333;
                }
              }
            }
          }

          .user-desc {
            width: 100%;
            font-size: 14px;
            line-height: 140%;
            color: #333;
            margin-top: 16px;
            white-space: pre-line;
          }

          // 数据统计
          .data-info {
            margin-top: 20px;

            .user-interactions {
              display: flex;
              align-items: center;

              > div {
                display: flex;
                align-items: center;
                margin-right: 16px;

                .count {
                  font-weight: 500;
                  font-size: 14px;
                  margin-right: 4px;
                }

                .shows {
                  color: rgba(51, 51, 51, 0.6);
                  font-size: 14px;
                  line-height: 120%;
                }
              }
            }
          }
        }
      }

      // 关注按钮
      .tool-btn {
        display: flex;
        align-items: center;
        margin-left: 20px;
      }
    }
  }

  // 粘性标签栏
  .reds-sticky-box {
    --sticky-transition: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s;
    transition: var(--sticky-transition);

    .reds-sticky {
      padding: 16px 0;
      z-index: 5 !important;
      background: hsla(0, 0%, 100%, 0.98);
      position: sticky;
      top: 0;
      width: 80%;
      margin: 0 auto;

      // 外层标签：笔记/收藏
      .reds-tabs-list {
        display: flex;
        justify-content: center;
        position: relative;
        font-size: 16px;

        .reds-tab-item {
          padding: 0 16px;
          height: 40px;
          display: flex;
          align-items: center;
          cursor: pointer;
          color: rgba(51, 51, 51, 0.8);
          border-radius: 20px;
          margin: 0 8px;
          transition: all 0.3s;

          &.active {
            background-color: rgba(0, 0, 0, 0.03);
            font-weight: 600;
          }

          &:hover {
            color: #333;
          }
        }
      }

      // 内层子tab：图文/视频
      .sub-tabs {
        margin-top: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .sub-tab-container {
          :deep(.el-tabs__header) {
            margin: 0;
          }

          :deep(.el-tabs__content) {
            display: none; // 仅展示tab切换，内容在下方展示
          }
        }
        
        // 发布按钮容器
        .publish-btn-container {
          margin-left: auto;
        }
      }
    }
  }

  // 内容展示区样式重构（参考热门视频）
  .feeds-tab-container {
    width: 80%;
    margin: 20px auto 0;
    padding-bottom: 40px;

    .loading-container {
      padding: 20px;
    }

    .content-wrapper {
      .content-tip {
        padding: 40px 0;
        text-align: center;
      }

      // 内容列表：复用热门视频的grid布局
      .content-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px; // 调整间距匹配热门视频
        margin-top: 20px;

        // 复用热门视频的.video-item样式
        .video-item {
          cursor: pointer;
          border-radius: 8px;
          padding: 12px; // 增大内边距适配标签
          transition: background 0.3s ease;
          display: flex;
          flex-direction: column; // 改为纵向布局
          height: 100%;
          position: relative; // 相对定位，用于标签绝对定位
          // 新增：作者+日期容器样式
          .video-meta {
            display: flex;
            align-items: center;
            gap: 4px; // 作者和日期之间的间距
            font-size: 12px;
            color: #909399;
            line-height: 22px; // 与标签行高统一
            align-items: baseline; // 基线对齐，保证文字等高
          }
          // 文字内容区：占满剩余空间，留出标签位置
          .video-text-content {
            flex: 1;
            margin-bottom: 5px; // 与标签间距
            .mushroom-tag {
              position: static !important; // 覆盖原有绝对定位
              display: inline-block;
              margin-top: 8px; // 与发布时间间距
              background-color: #f5f7fa;
              color: var(--el-color-primary);
              padding: 4px 10px;
              border-radius: 6px;
              font-size: 12px;
              line-height: 14px;
              height: 22px;
              display: inline-flex;
              align-items: center;
            }
          }

          // 标题样式：调整字号和行高
          .video-title {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin: 0 0 6px 0;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
          }
          // 作者名样式（保留原有，仅调整margin）
          .video-author {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            margin: 0;
          }

          // 新增：分隔符样式
          .meta-divider {
            color: #c0c4cc;
            margin: 0 2px;
          }
          // 多选框样式
          .select-checkbox {
            position: absolute;
            top: 12px;
            left: 12px;
            z-index: 10;
            padding: 2px;
            border-radius: 4px;
          }
          
          // 操作按钮组：调整间距适配删除按钮
          .item-actions {
            position: absolute;
            bottom: 12px;
            right: 12px; // 和原菌种标签位置一致
            display: flex;
            gap: 8px;
            opacity: 0;
            transition: opacity 0.3s ease;
            background: rgba(255, 255, 255, 0.9);
            padding: 4px;
            border-radius: 4px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            
            :deep(.el-button--danger) {
              color: var(--el-color-danger);
            }
            // 按钮样式优化
            :deep(.el-button) {
              padding: 4px 8px;
              height: auto;
              line-height: 1;
            }
          }
          
          // 悬浮显示按钮
          &:hover .item-actions {
            opacity: 1;
          }
          // 新增：发布日期样式
          .video-date {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          &:hover {
            background: #f9fafb;
          }

          // 封面容器：参考热门视频
          .video-cover-wrapper {
            position: relative;
            border-radius: 8px;
            overflow: hidden;
            flex: 0 0 auto; // 取消弹性占比
            aspect-ratio: 16/9;
            margin-bottom: 10px; // 与文字区间距

            .video-cover {
              width: 100%;
              height: 100%;
              display: block;
              border-radius: 8px;
              object-fit: cover;
            }

            // 播放按钮：参考热门视频
            .play-btn {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              margin: auto;
              opacity: 0;
              pointer-events: none;
              color: #ffffff;
              background: rgba(0, 0, 0, 0.5);
              border-radius: 50%;
              padding: 8px;
              transition: opacity 0.2s ease-in-out;
            }
          }

          &:hover .play-btn {
            opacity: 1;
            pointer-events: auto;
          }

          // ========== Moved audit status styling ========== 
          .audit-status-tag {
            position: absolute;
            top: 12px;  // Changed from bottom to top
            right: 12px;
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            line-height: 1.5;
            z-index: 5;
          }
          
          .audit-pending { background-color: #ecf5ff; color: #409eff; }
          .audit-passed { background-color: #f0f9eb; color: #67c23a; }
          .audit-rejected { background-color: #fef0f0; color: #f56c6c; }
          
        }
      }
    }
  }

  // 响应式适配
  @media screen and (max-width: 1200px) {
    .user-page {
      .user .user-info {
        width: 90%;
      }

      .reds-sticky-box .reds-sticky {
        width: 90%;
      }

      .feeds-tab-container {
        width: 90%;
      }
      .feeds-tab-container .content-list {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      }
    }
  }
}
</style>