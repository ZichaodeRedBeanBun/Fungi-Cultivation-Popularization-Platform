<script setup lang="ts">
import { usePopSciRecommend } from './usePopSci'
import PopSciDetailDialog from './PopSciDetailDialog.vue'
import defaultCover from '@/assets/default.jpg'
import defaultAvatar from '@/assets/user.jpg'
import { VideoPlay } from '@element-plus/icons-vue'
import { ElMessage, ElSkeleton, ElIcon, ElTag } from 'element-plus'
import { useRouter } from 'vue-router'

// 仅解构推荐相关的所有变量/方法，和原首页一致
const {
  recommendArticles,
  articleTotal,
  articleLoading,
  recommendVideos,
  videoTotal,
  videoLoading,
  videoAccountGroups,
  currentAccountGroup,
  loadRecommendArticles,
  loadRecommendVideos,
  changeVideoPage,
  refreshVideoAccount,
  getCurrentAccountList,
  popSciDetailDialogVisible,
  selectedPopSci,
  openPopSciDetail,
  closePopSciDetail,
  popSciDetailLoading,
   accountLoading,
   handleCollectionUpdate // 导入收藏状态更新方法
} = usePopSciRecommend()

const router = useRouter()

// 处理科普号点击事件
const handleAccountClick = (accountId: string) => {
  if (accountId) {
    router.push(`/create/${accountId}`)
  }
}

console.log(PopSciDetailDialog); // 保留原有打印
</script>

<template>
  <div class="recommend-wrapper space-y-8">
    <!-- 推荐图文+视频+科普号模块（完全迁移原首页代码，无修改） -->
    <div class="content-wrapper">
      <!-- 左侧：推荐图文 -->
      <div class="article-container bg-white rounded-lg shadow p-4">
        <div class="section-header flex justify-between items-center mb-4">
          <div class="title-wrapper flex items-center">
            <img src="@/assets/cover.png" alt="图标" class="title-icon w-6 h-6 mr-2" />
            <h2 class="section-title text-xl font-semibold text-gray-800">推荐图文</h2>
          </div>
          <button class="more-btn text-primary underline hover:no-underline text-sm font-medium" @click="loadRecommendArticles">
            换一换
          </button>
        </div>
        <div class="content-area">
          <div v-if="articleLoading" class="loading-state">
            <el-skeleton :rows="6" animated />
          </div>
          <div v-else-if="recommendArticles.length === 0" class="empty-state py-8 text-center text-gray-500">
            暂无推荐图文
          </div>
          <div v-else class="article-list-grid">
            <div
              v-for="item in recommendArticles"
              :key="item.id"
              class="article-item"
              @click="openPopSciDetail(item)"
            >
              <div class="article-cover-wrapper">
                <img :src="item.articleCoverUrl || defaultCover" alt="图文封面" class="article-cover" loading="lazy" />
              </div>
              <div class="article-content">
                <h3 :title="item.theme">{{ item.theme }}</h3>
                <p class="desc-text" :title="item.description">{{ item.description }}</p>
                <div class="meta-info">
                  <span v-if="item.mushroomName" class="mushroom-tag" :title="item.mushroomName">
                    {{ item.mushroomName }}
                  </span>
                  <span :title="item.authorName">作者：{{ item.authorName }}</span>
                  <span :title="item.publishDate">发布时间：{{ item.publishDate }}</span>
                  <span :title="item.contentType">{{ item.contentType === "article" ? "图文" : item.contentType }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：推荐视频 + 推荐科普号 -->
      <div class="right-container">
        <!-- 推荐视频模块 -->
        <div class="video-container">
          <div class="section-header">
            <div class="title-wrapper">
              <img src="@/assets/cover.png" alt="图标" class="title-icon" />
              <h2 class="section-title">热门视频</h2>
            </div>
            <div class="refresh-btn" @click="loadRecommendVideos">
              <span>换一换</span>
            </div>
          </div>

          <div class="content-area">
            <div v-if="videoLoading" class="loading-state">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else-if="recommendVideos.length === 0" class="empty-state">
              暂无热门视频
            </div>
            <div v-else class="video-list">
              <div 
                v-for="item in recommendVideos" 
                :key="item.id"
                class="video-item"
                @click="openPopSciDetail(item)"
              >
                <div class="video-cover-wrapper">
                  <img 
                    :src="item.videoCoverUrl || defaultCover" 
                    alt="视频封面" 
                    class="video-cover" 
                    loading="lazy" 
                  />
                  <el-icon class="play-btn" :size="40">
                    <VideoPlay />
                  </el-icon>
                </div>
                <h3 class="video-title" :title="item.theme">{{ item.theme }}</h3>
                <p class="video-author" :title="item.authorName">{{ item.authorName }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 推荐科普号模块 -->
        <div class="account-container">
          <div class="section-header">
            <div class="title-wrapper">
              <img src="@/assets/cover.png" alt="图标" class="title-icon" />
              <h2 class="section-title">推荐科普号</h2>
            </div>
            <div class="refresh-btn" @click="refreshVideoAccount">
              <span>换一换</span>
            </div>
          </div>

          <div class="account-list">
    <!-- 加载中状态 -->
    <div v-if="accountLoading" class="loading-state w-full">
      <el-skeleton :rows="6" animated />
    </div>
    <!-- 空状态 -->
    <div v-else-if="!getCurrentAccountList || getCurrentAccountList.length === 0" class="empty-state w-full py-8 text-center text-gray-500">
      暂无推荐科普号
    </div>
    <!-- 正常展示科普号 -->
    <div 
      v-else
      v-for="item in getCurrentAccountList" 
      :key="item.id"
      class="account-item"
      @click="handleAccountClick(item.id.toString())"
    >
      <div class="account-avatar">
        <!-- 兜底：头像为空时使用 defaultAvatar -->
        <img 
          :src="item.avatarUrl || defaultAvatar" 
          alt="视频号头像" 
          class="avatar-img"
        />
      </div>
      <h3 class="account-name" :title="item.name">{{ item.name }}</h3>
    </div>
     </div>
        </div>
      </div>
    </div>

    <!-- 科普详情弹窗（完全迁移原首页代码） -->
    <PopSciDetailDialog
  v-model="popSciDetailDialogVisible"
  :detail-data="selectedPopSci"
  :loading="popSciDetailLoading"
  @collection-updated="handleCollectionUpdate" 
/>
  </div>
</template>

<style scoped lang="less">
/* 仅保留推荐模块专属样式：图文、视频、科普号+科普详情弹窗相关 */
/* ========== 推荐模块通用样式 ========== */
// 文本截断通用类（推荐模块专属）
h3 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

// 卡片 hover 阴影通用效果（推荐模块专属）
.article-item:hover,
.disease-pest-item:hover {
  box-shadow: 0 4px 12px rgb(0 0 0 / 0.1);
  transition: box-shadow 0.3s ease;
}

/* ========== 推荐图文/视频/科普号模块样式 ========== */
// 推荐模块外层容器
.recommend-wrapper {
  width: 100%;
}

// 整体布局
.content-wrapper {
  display: flex;
  flex-grow: 1;
  gap: 24px;
  overflow: hidden;
}

// 推荐图文容器
.article-container {
  flex: 2;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .content-area {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  // 推荐图文列表
  .article-list-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    flex: 1;
    min-height: 0;
    height: 100%;
    align-content: stretch;
    grid-auto-rows: minmax(0, 1fr);

    // 图文卡片
    .article-item {
      border: 1px solid #ebeef5;
      border-radius: 8px;
      padding: 18px 20px;
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: box-shadow 0.3s ease;
      min-height: 180px;
      overflow: hidden;

      &:hover {
        box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
      }

      // 封面图
      img {
        flex-shrink: 0;
        margin-right: 18px;
        border-radius: 8px;
        width: 120px;
        aspect-ratio: 4/3;
        object-fit: cover;
        display: block;
        overflow: hidden;
      }

      // 文字内容区
      .article-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-width: 0;

        h3 {
          font-weight: 600;
          color: #333;
          font-size: 16px;
          line-height: 1.35;
          margin: 0 0 10px 0;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .desc-text {
          @extend .line-clamp-3;
          color: #606266;
          font-size: 15px;
          line-height: 1.55;
          margin: 0 0 10px 0;
        }

        .meta-info {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 10px;
          font-size: 13px;
          color: #909399;

          .mushroom-tag {
            background-color: #f5f7fa;
            color: var(--el-color-primary);
            padding: 4px 10px;
            border-radius: 6px;
          }
        }
      }
    }
  }
}

// 右侧容器（视频+科普号）
.right-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow: hidden;
}

// 视频模块
.video-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    .title-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      .title-icon {
        width: 24px;
        height: 24px;
      }
      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0;
      }
    }
  }

  .content-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .loading-state, .empty-state {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px 0;
    }

    .video-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      align-items: stretch;
      flex: 1;
      overflow-y: auto;
      &::-webkit-scrollbar {
        display: none;
      }
    }
  }

  .video-item {
    cursor: pointer;
    border-radius: 8px;
    padding: 8px;
    transition: background 0.3s ease;
    display: flex;
    flex-direction: column;
    height: 100%;

    &:hover {
      background: #f9fafb;
    }

    .video-cover-wrapper {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      flex: 1;
      aspect-ratio: 16/9;
      display: flex;
      align-items: center;
      justify-content: center;

      .video-cover {
        width: 100%;
        height: 100%;
        display: block;
        border-radius: 8px;
        object-fit: cover;
      }

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

    .video-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin: 8px 0 4px 0;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .video-author {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin: 0;
    }
  }
}

// 科普号模块
.account-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .account-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    flex-grow: 1;
    overflow-y: auto;

    .account-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      transition: background 0.3s ease;

      &:hover {
        background: #f9fafb;
      }

      .account-avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        overflow: hidden;
        margin-bottom: 8px;

        .avatar-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .account-name {
        font-size: 14px;
        color: #303133;
        margin: 0;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        text-align: center;
      }
    }
  }
}

// 模块头部通用样式
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;

  .title-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;

    .title-icon {
      width: 24px;
      height: 24px;
    }

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .more-btn {
    color: var(--el-color-primary);
    text-decoration: underline;
    font-size: 14px;
    font-weight: 500;
    background: transparent;
    border: none;
    cursor: pointer;
  }
}

// 内容区通用样式
.content-area {
  flex-grow: 1;
  overflow-y: auto;
  padding-right: 8px;
}

// 加载/空状态通用样式
.loading-state, .empty-state {
  padding: 32px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

// 视频分页样式
.video-pagination {
  display: flex;
  align-items: center;
  gap: 8px;

  .page-btn {
    width: 20px;
    height: 20px;
    cursor: pointer;
    opacity: 0.8;

    &.page-disabled {
      opacity: 0.5;
      cursor: not-allowed;
      pointer-events: none;
    }
  }

  .page-num {
    font-size: 14px;
    color: #606266;
  }
}

// 科普号刷新按钮
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  cursor: pointer;
  font-size: 14px;
  text-decoration: underline;

  .refresh-icon {
    width: 16px;
    height: 16px;
  }
}
</style>