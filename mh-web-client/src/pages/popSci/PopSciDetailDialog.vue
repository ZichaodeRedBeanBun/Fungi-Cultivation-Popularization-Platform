<!-- PopSciDetailDialog.vue -->
<script setup lang="ts" name='PopSciDetailDialog'>
import { usePopSciRecommend } from './usePopSci'
import defaultAvatar from '@/assets/user.jpg'
import defaultVideoAvatar from '@/assets/default.jpg'
import {
  PopSciContentVO,
  PopSciContentTypeEnum,
  FavoriteTypeEnum,
  PopSciContentVOWithExtra,
  CommentPopSciVO,
  CommentPopSciDTO
} from '@/api/interface'
import { Icon } from '@iconify/vue'
import {
  collectPopSci,
  cancelCollectPopSci,
  addPopSciComment,
  likeComment,
  cancelLikeComment,
  praisePopSci,
  cancelPraisePopSci,
  listPopSciComment,
  deletePopSciComment,
  getUserFavoritePopSciList // 【新增】导入获取用户收藏列表接口
} from '@/api/system'
import { ElDialog, ElImage, ElCarousel, ElCarouselItem, ElSkeleton, ElButton, ElMessage, ElAvatar, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue' // 【修改】显式导入所有用到的VueAPI

// 【新增】===== 复用项目Pinia用户仓库 + 登录弹窗 =====
import { UserStore } from '@/stores/modules/user' // 同个人主页的用户仓库
import AuthTabs from '@/components/Auth/AuthTabs.vue' // 同个人主页的登录/注册弹窗
const userStore = UserStore() // 实例化用户仓库（和个人主页完全一致）
const authVisible = ref(false) // 控制登录弹窗显示隐藏
// 【新增结束】===== 复用项目Pinia用户仓库 + 登录弹窗 =====

// ========== Props 定义 ==========
const props = defineProps<{
  modelValue: boolean;
  detailData: PopSciContentVO | null;
  loading: boolean;
}>()

const emit = defineEmits(['update:modelValue', 'collection-updated'])

// ========== 数据处理 ==========
const localVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
const videoPlaying = ref(false)
const content = ref<PopSciContentVOWithExtra | null>(
  props.detailData ? (props.detailData as PopSciContentVOWithExtra) : null
)
const newCommentText = ref('')
const commentList = ref<CommentPopSciVO[]>([])
const isPraise = ref(false)

// 【新增】===== 评论删除权限判断方法 =====
// 判断当前登录用户是否有权限删除该评论：仅「评论作者本人」可删
const canDeleteComment = (commentUserId: number | bigint, contentAuthorId: number | bigint) => {
  if (!userStore.isLoggedIn) return false;
  
  const currentUserId = userStore.userInfo.userId;
  
  // 权限判断：评论作者本人 OR 科普内容作者
  return (
    currentUserId === commentUserId || 
    currentUserId === contentAuthorId
  );
};
// 【新增结束】===== 评论删除权限判断方法 =====

// 【新增】===== 获取用户收藏状态方法 =====
/**
 * 检查当前登录用户是否收藏了指定的科普内容
 * @param popsciId 科普内容 ID
 */
const checkUserCollectionStatus = async (popsciId: number | string) => {
  if (!userStore.isLoggedIn) {
    // 未登录，设置为未收藏
    if (content.value) {
      content.value.isCollection = false;
    }
    return;
  }
  
  try {
    // 确定内容类型
    const favoriteType = content.value?.contentType === PopSciContentTypeEnum.ARTICLE ? 1 : 2;
    
    // 调用接口获取用户收藏列表
    const res = await getUserFavoritePopSciList({
      userId: userStore.userInfo.userId?.toString() || '',
      favoriteType: favoriteType,
      mushroomId: null,
      pageNum: 1,
      pageSize: 100 // 获取足够多的数据以确保包含当前内容
    });
    
    if (res.code === 0 && res.data?.items) {
      // 检查当前内容是否在收藏列表中
      const isCollectedByUser = res.data.items.some(
        (item: any) => item.id.toString() === popsciId.toString()
      );
      
      // 同步更新 content.value.isCollection
      if (content.value) {
        content.value.isCollection = isCollectedByUser;
      }
      
      console.log(`科普内容 ${popsciId} 的收藏状态:`, isCollectedByUser);
    } else {
      // 接口返回异常，设置为未收藏
      if (content.value) {
        content.value.isCollection = false;
      }
    }
  } catch (error) {
    console.error('获取用户收藏状态失败:', error);
    if (content.value) {
      content.value.isCollection = false;
    }
  }
};
// 【新增结束】===== 获取用户收藏状态方法 =====
import { onMounted, nextTick } from 'vue'

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
// ========== 计算属性（核心：加测试图片兜底，确保轮播图先显示） ==========
const coverUrl = computed(() => {
  if (!content.value) return ''
  return content.value.contentType === PopSciContentTypeEnum.ARTICLE
    ? content.value.articleCoverUrl
    : content.value.videoCoverUrl
})

const contentImageList = computed(() => {
  if (!content.value) return []
  const realList = content.value.pictureList?.map(p => p.picUrl) || []
  console.log('图文轮播图真实列表：', realList);
  return realList
})

const isVideoType = computed(() => {
  return content.value?.contentType === PopSciContentTypeEnum.VIDEO
})

const title = computed(() => {
  return content.value?.theme || '未命名内容'
})

const publishDate = computed(() => {
  return content.value?.publishDate || ''
})

// ========== 交互函数 ==========
const handleClose = () => {
  localVisible.value = false
  videoPlaying.value = false
  newCommentText.value = ''
  commentList.value = []
  content.value = null
  authVisible.value = false // 【新增】关闭弹窗时同时关闭登录窗
}

const handleCollect = async () => {
  if (!content.value || !content.value.id || !content.value.mushroomName) {
    ElMessage.warning('内容未加载完成，请勿进行收藏操作');
    return;
  }
  try {
    const favoriteType = content.value.contentType === PopSciContentTypeEnum.ARTICLE
      ? FavoriteTypeEnum.ARTICLE
      : FavoriteTypeEnum.VIDEO;

    if (content.value.isCollection) {
      await cancelCollectPopSci(content.value.id);
      content.value.collectCount = Math.max(0, (content.value.collectCount || 0) - 1);
      content.value.isCollection = false;
      ElMessage.success('已取消收藏');
      // 【新增】通知父组件更新收藏状态
      emit('collection-updated', {
        id: content.value.id,
        isCollection: false,
        collectCount: content.value.collectCount
      });
    } else {
      await collectPopSci({
        popsciId: content.value.id,
        favoriteType,
        mushroomName: content.value.mushroomName
      });
      content.value.collectCount = (content.value.collectCount || 0) + 1;
      content.value.isCollection = true;
      ElMessage.success('收藏成功');
      // 【新增】通知父组件更新收藏状态
      emit('collection-updated', {
        id: content.value.id,
        isCollection: true,
        collectCount: content.value.collectCount
      });
    }
  } catch (error) {
    ElMessage.error('收藏操作失败，请重试');
    console.error('收藏异常：', error);
  }
};

const handlePraise = async () => {
  if (!content.value) return
  try {
    const popsciId = content.value.id
    if (isPraise.value) {
      await cancelPraisePopSci(popsciId)
      content.value.praiseCount = Math.max(0, (content.value.praiseCount || 0) - 1)
      isPraise.value = false
      ElMessage.success('已取消点赞')
    } else {
      await praisePopSci(popsciId)
      content.value.praiseCount = (content.value.praiseCount || 0) + 1
      isPraise.value = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('点赞操作失败，请重试')
    console.error('点赞异常：', error)
  }
}

// 【修改】===== 评论提交方法：新增未登录判断 + 真实用户信息 =====
const handleComment = async () => {
  // 1. 内容空判断（原有逻辑保留）
  if (!content.value || !newCommentText.value.trim()) return
  // 2. 新增：未登录则弹出登录弹窗
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再发表评论~')
    authVisible.value = true
    return
  }
  // 3. 已登录则提交评论（原有逻辑优化）
  try {
    const commentDto: CommentPopSciDTO = {
      popsciId: content.value.id,
      content: newCommentText.value.trim()
    }
    await addPopSciComment(commentDto)
    // 本地渲染：使用Pinia中的【真实用户信息】（替换原有默认值）
    commentList.value.unshift({
      commentId: Date.now(),
      userId: userStore.userInfo.userId, // 新增：真实用户ID（用于权限判断）
      contentAuthorId:userStore.userInfo.contentAuthorId,
      username: userStore.userInfo.username, // 真实用户名
      userAvatar: userStore.userInfo.avatarUrl || '', // 真实头像（为空则用el-avatar默认占位）
      content: newCommentText.value.trim(),
      createTime: new Date().toLocaleString(),
      likeCount: 0
    })
    // 更新评论数（原有逻辑保留）
    content.value.reviewCount = (content.value.reviewCount || 0) + 1
    newCommentText.value = ''
    ElMessage.success('评论发布成功')
  } catch (error) {
    ElMessage.error('评论发送失败，请重试')
    console.error('评论异常：', error)
  }
}
// 【修改结束】===== 评论提交方法 =====

// 【新增】===== 评论删除方法：带权限校验 + 接口调用 =====
// 【修改】评论删除方法：带权限校验 + 接口调用
const handleDeleteComment = async (commentId: number | bigint, index: number) => {
  try {
    // 二次确认删除
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后无法恢复',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 将 commentId 转换为 number（bigint 转换为 number）
    const commentIdNum = Number(commentId)
    
    // 调用后端删除评论接口
    await deletePopSciComment(commentIdNum)
    
    // 本地删除评论
    commentList.value.splice(index, 1)
    
    // 更新评论数
    if (content.value) {
      content.value.reviewCount = Math.max(0, (content.value.reviewCount || 1) - 1)
    }
    
    ElMessage.success('评论删除成功')
  } catch (error) {
    if (error !== 'cancel') { // 排除用户取消操作
      ElMessage.error('评论删除失败，请重试')
      console.error('删除评论异常：', error)
    }
  }
}
// 【新增结束】===== 评论删除方法 =====

// 监听详情数据变化，从后端查询真实评论列表
watch(() => props.detailData, async (newVal) => {
  if (newVal) {
    content.value = newVal as PopSciContentVOWithExtra
    isPraise.value = false
    
    // 【修改】保存当前视频播放状态
    const currentVideoPlaying = videoPlaying.value
    
    // 【新增】先检查用户收藏状态
    await checkUserCollectionStatus(newVal.id);
    
    // 【修改】从后端查询评论列表，确保类型安全
    try {
      const res = await listPopSciComment(newVal.id)
      console.log('评论列表API响应:', res)
      
      let comments: CommentPopSciVO[] = []
      
      if (res.code === 0) {
        // 确保 data 是数组类型
        if (Array.isArray(res.data)) {
          comments = res.data as CommentPopSciVO[]
        } else if (res.data && typeof res.data === 'object') {
          // 如果 data 是对象，尝试从中提取列表
          const dataObj = res.data as any
          if (Array.isArray(dataObj.list)) {
            comments = dataObj.list as CommentPopSciVO[]
          } else if (Array.isArray(dataObj.items)) {
            comments = dataObj.items as CommentPopSciVO[]
          } else if (Array.isArray(dataObj.data)) {
            comments = dataObj.data as CommentPopSciVO[]
          } else {
            console.warn('评论列表数据格式未知:', dataObj)
            comments = []
          }
        } else {
          console.warn('评论列表返回数据格式错误:', res.data)
          comments = []
        }
        
        // 【新增】处理头像为空的情况
        commentList.value = comments.map(comment => {
          console.log('单条评论原始数据:', comment);
          // 如果用户头像为空，设置为默认头像
          if (!comment.userAvatar || comment.userAvatar.trim() === '') {
            return {
              ...comment,
              userAvatar: defaultAvatar
            }
          }
          return comment
        })
      } else {
        console.warn('获取评论列表失败:', res.message)
        commentList.value = []
      }
    } catch (error) {
      console.error('查询评论列表失败：', error)
      commentList.value = []
    }
    
    // 【修改】恢复之前的视频播放状态，而不是强制设为false
    videoPlaying.value = currentVideoPlaying
  } else {
    content.value = null
    isPraise.value = false
    commentList.value = []
    videoPlaying.value = false
  }
  authVisible.value = false
}, { immediate: true, deep: true })
</script>

<template>
  <el-dialog
    v-model="localVisible"
    width="85%"
    top="auto"
    destroy-on-close
    @close="handleClose"
    class="pop-sci-detail-dialog"
    :show-close="false"
  >
    <div v-if="loading || !content" class="loading-container">
      <el-skeleton :rows="8" animated style="width: 100%" />
    </div>

    <div v-else class="detail-container">
      <!-- 左侧内容区 -->
      <div class="content-box">
        <!-- 视频类型 -->
        <div v-if="isVideoType" class="video-content">
          <div class="video-container relative bg-black h-full" @click.stop>
            <template v-if="!videoPlaying">
              <el-image
                :src="coverUrl || defaultVideoAvatar"
                fit="cover" 
                class="video-cover w-full h-full cursor-pointer"
                style="width: 100%; height: 100%;"
                @click="videoPlaying = true"
              >
                <template #error>
                  <div class="error-container" style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;">
                    <!-- 错误状态下的默认图片：强制裁剪适配 -->
                    <img 
                      :src="defaultVideoAvatar" 
                      style="width: 100%; height: 100%; object-fit: cover; object-position: center;"
                    />
                  </div>
                </template>
              </el-image>
              
              <div class="play-mask absolute inset-0 flex items-center justify-center" @click="videoPlaying = true">
                <el-button type="primary" circle class="play-button">▶</el-button>
              </div>
            </template>
            <video
              v-else
              :src="content.videoUrl"
              controls
              autoplay
              class="w-full h-full object-contain"
            >
              您的浏览器不支持HTML5视频播放，请升级浏览器！
            </video>
          </div>
        </div>

        <!-- 图文类型：flex布局必须写全 -->
        <div v-else class="article-content">
          <!-- 图文轮播图：v-if只判断长度，样式由CSS控制 -->
            <el-carousel
                v-if="contentImageList.length > 0"
                class="article-carousel"
                :interval="4000"
                style="height: 300px; background: #000;"
              >
                <el-carousel-item v-for="(img, index) in contentImageList" :key="index">
                  <el-image
                    :src="img"
                    fit="cover"
                    class="carousel-img"
                    :preview-src-list="contentImageList"
                    :initial-index="index"
                  >
                    <template #placeholder>
                      <div class="img-placeholder flex items-center justify-center">图片加载中...</div>
                    </template>
                    <template #error>
                      <div class="img-error flex items-center justify-center">图片加载失败</div>
                    </template>
                  </el-image>
                </el-carousel-item>
              </el-carousel>

          <!-- 图文正文 -->
          <div class="article-info" :class="{ 'no-carousel': contentImageList.length === 0 }">
            <h2 class="article-title text-2xl font-bold text-gray-800 mb-3">{{ title }}</h2>
            <div class="article-meta flex items-center justify-between text-sm text-gray-500 mb-4 pb-3 border-b">
              <span>作者：{{ content.authorName || '匿名作者' }}</span>
              <span>发布时间：{{ publishDate || '未知时间' }}</span>
            </div>
            <div class="article-body text-gray-700 leading-8 text-base">
              {{ content.content || content.description || '暂无该科普内容的详细介绍，敬请期待！' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧互动区 -->
      <div class="interact-box">
        <div class="interact-stats p-6 border-b">
          <div class="stats-item flex items-center justify-between mb-4">
            <span class="text-lg font-semibold">互动数据</span>
          </div>
          <div class="stats-count flex justify-around text-center mb-4">
            <div>
              <p class="num text-xl font-bold text-primary">{{ content.praiseCount || 0 }}</p>
              <p class="label text-sm text-gray-500">点赞</p>
            </div>
            <div>
              <p class="num text-xl font-bold text-gray-800">{{ content.reviewCount || 0 }}</p>
              <p class="label text-sm text-gray-500">评论</p>
            </div>
            <div>
              <p class="num text-xl font-bold text-danger">{{ content.collectCount || 0 }}</p>
              <p class="label text-sm text-gray-500">收藏</p>
            </div>
          </div>
        </div>

        <div class="interact-buttons p-6 border-b">
          <div class="comment-input flex items-center gap-2">
            <el-input
              v-model="newCommentText"
              placeholder="写下你的评论..."
              clearable
              class="flex-1"
              @keyup.enter="handleComment"
            />
            <el-button type="primary" @click="handleComment" :disabled="loading">发送</el-button>
            <el-button 
              @click="handlePraise" 
              class="praise-btn" 
              :class="{ 'is-active': isPraise }"
              :disabled="loading || !content"
            >
              <Icon :icon="isPraise ? 'mdi:thumb-up' : 'mdi:thumb-up-outline'" width="1.2em" height="1.2em" />
            </el-button>
            <el-button
              @click="handleCollect"
              class="collect-btn"
              :class="{ 'is-active': content?.isCollection }"
              :disabled="loading || !content"
            >
              <Icon
                :icon="content?.isCollection ? 'mdi:star' : 'mdi:star-outline'"
                width="1.2em"
                height="1.2em"
              />
            </el-button>
          </div>
        </div>

        <div class="comment-list p-6">
          <h3 class="comment-title text-lg font-semibold mb-4">全部评论</h3>
          <!-- 调试：显示当前区域高度 -->
              <div v-if="false" class="debug-info" style="position: absolute; top: 0; right: 0; background: red; color: white; padding: 4px; font-size: 12px;">
                Height: {{ commentListHeight }}
              </div>
          <div class="comments-container">
          <div
            v-for="(comment, index) in commentList"
            :key="comment.commentId"
            class="comment-item mb-4 pb-4 border-b last:border-0"
            style="position: relative;"
          >
            <!-- 【修改】评论头部：新增删除按钮，按权限显示 -->
            <div class="comment-header flex items-center justify-between gap-3 mb-2">
              <div class="flex items-center gap-3">
                <el-avatar :size="36" :src="comment.userAvatar" />
                <div>
                  <p class="username font-medium">{{ comment.username }}</p>
                  <p class="time text-xs text-gray-400">{{ comment.createTime }}</p>
                </div>
              </div>
              <!-- 新增：删除按钮 - 仅评论用户与作者可见 -->
              <el-button
                  v-if="canDeleteComment(comment.userId, comment.contentAuthorId)"
                  circle
                  size="small"
                  class="comment-delete-btn"
                  @click="handleDeleteComment(comment.commentId, index)"
                >
                  <Icon icon="mdi:delete-outline" style="width: 1em; height: 1em; color: var(--el-color-primary)" />
                </el-button>
            </div>
            <p class="comment-content text-gray-700 text-sm">{{ comment.content }}</p>
          </div>
          <div v-if="commentList.length === 0" class="no-comment text-center text-gray-400 py-8">
            <p>暂无评论，快来抢沙发吧～</p>
          </div>
        </div>
        </div>
      </div>
    </div>

    <!-- 关闭按钮 -->
    <div class="close-btn" @click="handleClose">
      <el-button circle class="bg-white shadow-lg hover:bg-gray-50">
        <Close style="width: 1.2em; height: 1.2em; color: #666" />
      </el-button>
    </div>

    <!-- 【新增】登录弹窗：和个人主页完全复用AuthTabs组件 -->
    <AuthTabs v-model="authVisible" />
  </el-dialog>
</template>

<style scoped lang="less">
// 科普弹窗核心样式：无冲突高度 + 修复深度选择器 + 强化高度继承
.pop-sci-detail-dialog {
  .loading-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  // 核心修复：确保整个详情容器有固定高度
  .detail-container {
    display: flex;
    width: 100%;
    height: 85vh !important; // 改为固定vh单位
    min-height: 600px; // 最小高度保证
    max-height: 90vh; // 最大高度限制
    background: #fff;
    border-radius: 12px;
    position: relative;
  }

  // 左侧内容区域 - 固定高度
  .content-box {
    width: 65%;
    height: 100% !important;
    background: #f5f5f5;
    overflow: hidden !important;
    padding: 0;
  }
  
  .video-content {
    width: 100%;
    height: 100%;
  }
  
  .video-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    position: relative;
    background: #000;
    
    // 视频封面图片容器：强化裁剪适配
    :deep(.video-cover) {
      width: 100%;
      height: 100%;
      display: block;
      overflow: hidden;
      
      // el-image内部图片
      :deep(.el-image__inner) {
        width: 100% !important;
        height: 100% !important;
        object-fit: cover !important;
        object-position: center !important;
        position: absolute;
        top: 0;
        left: 0;
      }
      
      :deep(.el-image) {
        width: 100%;
        height: 100%;
        position: relative;
        display: block;
      }
      
      // 错误状态容器
      .error-container {
        width: 100%;
        height: 100%;
        background: #000;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    
    // 播放时的视频
    video {
      width: 100% !important;
      height: 100% !important;
      object-fit: contain !important;
      background: #000 !important;
      display: block;
    }
  }

  .play-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
  }
  
  .play-button {
    width: 70px;
    height: 70px;
    font-size: 30px;
    background: rgba(255, 255, 255, 0.2);
    border: 2px solid #fff;
    backdrop-filter: blur(8px);
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  // 图文内容样式保持不变
  .article-info.no-carousel {
    flex: 1 !important;
    height: auto !important;
    padding: 24px;
    box-sizing: border-box !important;
    overflow-y: auto !important;
    overflow-x: hidden;
  }

  .article-content {
    width: 100%;
    height: 100% !important;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }

  .article-carousel {
    width: 80%;
    height: 40%;
    flex-shrink: 0 !important;
    background: #000;
    min-height: 400px !important;
    margin: 0 auto;
    padding: 0;
    
    :deep(.el-image__inner) {
      width: 100% !important;
      height: 100% !important;
      object-fit: cover !important;
    }
    
    :deep(.el-carousel__container),
    :deep(.el-carousel__item) {
      width: 100% !important;
      height: 100% !important;
      overflow: hidden;
    }
    
    .carousel-img {
      width: 100% !important;
      height: 100% !important;
      display: block !important;
      object-fit: cover !important;
    }
    
    :deep(.el-carousel__arrow) {
      display: flex !important;
      background: rgba(0, 0, 0, 0.5) !important;
      
      &:hover {
        background: rgba(0, 0, 0, 0.8) !important;
      }
    }
    
    :deep(.el-carousel__indicator-button) {
      background: rgba(255, 255, 255, 0.5) !important;
      
      &.is-active {
        background: #409eff !important;
      }
    }
    
    .img-placeholder,
    .img-error {
      width: 100%;
      height: 100%;
      background: #222;
      color: #fff;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .article-info {
    width: 100%;
    padding: 24px;
    background: #fff;
    box-sizing: border-box;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    
    .article-title {
      line-height: 1.4;
      word-break: break-all;
      font-size: 20px;
      font-weight: 600;
    }
    
    .article-body {
      margin-top: 20px;
      word-break: break-all;
      white-space: pre-wrap;
      line-height: 1.8;
      min-height: 200px;
    }
    
    &::-webkit-scrollbar {
      width: 6px !important;
      display: block !important;
    }
    
    &::-webkit-scrollbar-thumb {
      border-radius: 3px !important;
      background: #ddd !important;
    }
    
    &::-webkit-scrollbar-track {
      background: #f0f0f0 !important;
    }
  }

  // 核心修复：右侧互动区 - 关键修改
  .interact-box {
    width: 35%;
    height: 100% !important; // 继承父容器高度
    background: #fff;
    border-left: 1px solid #eee;
    display: flex;
    flex-direction: column;
    overflow: hidden; // 防止整个右侧滚动
  }

  .interact-stats,
  .interact-buttons {
    flex-shrink: 0; // 固定高度，不参与flex伸缩
    padding: 24px;
    box-sizing: border-box;
    border-bottom: 1px solid #eee;
  }

  .stats-count {
    display: flex;
    justify-content: space-around;
    text-align: center;
    
    .num {
      color: #333;
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .label {
      font-size: 14px;
      color: #999;
    }
  }

  .comment-input {
    margin-top: 10px;
    display: flex;
    gap: 8px;
  }

  .praise-btn.is-active {
    color: var(--el-color-primary) !important;
  }
  
  .collect-btn.is-active {
    color: var(--el-color-danger) !important;
  }

  // 核心修复：评论列表区域 - 确保可滚动
  .comment-list {
    flex: 1; // 关键：占据剩余所有空间
    min-height: 0; // 关键：允许flex项目压缩
    padding: 24px;
    overflow-y: auto; // 允许垂直滚动
    overflow-x: hidden;
    box-sizing: border-box;
    
    .comment-item {
      word-break: break-all;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #eee;
      
      &:last-child {
        margin-bottom: 0;
        border-bottom: none;
      }
      
      .comment-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
        
        .username {
          font-weight: 500;
          color: #333;
        }
        
        .time {
          font-size: 12px;
          color: #999;
        }
      }
      
      .comment-content {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }
    }
    
    .no-comment {
      text-align: center;
      color: #999;
      padding: 40px 0;
    }
    
    // 滚动条样式
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      border-radius: 3px;
      background: #ddd;
      
      &:hover {
        background: #bbb;
      }
    }
    
    &::-webkit-scrollbar-track {
      background: #f5f5f5;
    }
  }

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    z-index: 9999 !important;
    
    :deep(.el-button) {
      width: 40px;
      height: 40px;
      padding: 0;
      background: #fff !important;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1) !important;
      border-radius: 50% !important;
      
      &:hover {
        background: #f5f5f5 !important;
      }
    }
  }

  @media (max-width: 1200px) {
    .detail-container {
      flex-direction: column;
      height: 90vh !important;
    }
    
    .content-box {
      width: 100% !important;
      height: 60% !important; // 移动端：左边占60%
    }
    
    .interact-box {
      width: 100% !important;
      height: 40% !important; // 移动端：右边占40%
      border-left: none;
      border-top: 1px solid #eee;
    }
    
    .close-btn {
      top: 10px;
      right: 10px;
      
      :deep(.el-button) {
        width: 36px;
        height: 36px;
      }
    }
    
    .article-carousel {
      min-height: 300px !important;
    }
  }
}
</style>

<style>
/* 全局修复弹窗高度问题 */
.pop-sci-detail-dialog.el-dialog {
  top: 50% !important;
  transform: translateY(-50%) !important;
  margin: 0 auto !important;
  width: 75% !important;
  border-radius: 16px !important;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2) !important;
  max-height: 90vh !important;
  overflow: hidden !important;
}

.pop-sci-detail-dialog.el-dialog__body {
  height: 90vh !important; 
  padding: 0 !important;
  background: #f8f8f8 !important;
}

.pop-sci-detail-dialog.detail-container {
  height: 85vh !important; 
}

.pop-sci-detail-dialog.article-info,
.pop-sci-detail-dialog.comment-list {
  overflow-y: auto !important;
  box-sizing: border-box !important;
}

@media (max-width: 1200px) {
  .pop-sci-detail-dialog.el-dialog {
    width: 95% !important;
  }
  
  .pop-sci-detail-dialog.el-dialog__body {
    height: 90vh !important;
  }
}
</style>
