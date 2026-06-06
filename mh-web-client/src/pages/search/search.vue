<template>
  
  <div class="search-page">
    <div class="search-card-container">
    <!-- 粘性标签栏 -->
    <div class="reds-sticky-box search-sticky">
      <div class="reds-sticky">
        <!-- 外层标签：五个tab -->
        <div class="tertiary center reds-tabs-list">
          <div
            :class="activeTab === 'mushroom' ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="switchTab('mushroom')"
          >
            <span>菌种卡片</span>
          </div>
          <div
            :class="activeTab === 'diseasePest' ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="switchTab('diseasePest')"
          >
            <span>病虫害</span>
          </div>
          <div
            :class="activeTab === 'article' ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="switchTab('article')"
          >
            <span>图文</span>
          </div>
          <div
            :class="activeTab === 'video' ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="switchTab('video')"
          >
            <span>视频</span>
          </div>
          <div
            :class="activeTab === 'popsciAuthor' ? 'reds-tab-item active' : 'reds-tab-item'"
            @click="switchTab('popsciAuthor')"
          >
            <span>科普号</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 内容展示区 -->
    <div class="feeds-tab-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated style="width: 100%" />
      </div>

      <!-- 内容展示 -->
      <div v-else class="content-wrapper">
        <div class="content-tip" v-if="!hasContent">
          <el-empty :description="getEmptyTip()" />
        </div>
        <div v-else class="content-list" :class="{ 'video-list': activeTab === 'video' }">
          <!-- 菌种卡片列表 -->
          <div 
            v-if="activeTab === 'mushroom'"
            class="card-item" 
            v-for="item in mushroomList" 
            :key="item.id"
            @click="handleMushroomClick(item)"
          >
            <div class="card-cover-wrapper">
              <img 
                :src="item.coverUrl || defaultCover" 
                alt="菌种封面" 
                class="card-cover" 
                loading="lazy" 
              />
            </div>
            <div class="card-text-content">
              <h3 class="card-title" :title="item.mushroomName">{{ item.mushroomName }}</h3>
              <p class="card-desc">{{ item.intro }}</p>
            </div>
          </div>

          <!-- 病虫害列表 -->
          <div 
            v-if="activeTab === 'diseasePest'"
            class="card-item" 
            v-for="item in diseasePestList" 
            :key="item.id"
            @click="handleDiseasePestClick(item)"
          >
            <div class="card-cover-wrapper">
              <img 
                :src="item.coverUrl || defaultCover" 
                alt="病虫害封面" 
                class="card-cover" 
                loading="lazy" 
              />
            </div>
            <div class="card-text-content">
              <h3 class="card-title" :title="item.diseaseName">{{ item.diseaseName }}</h3>
              <p class="card-desc">{{ item.brief }}</p>
              <span class="disease-type-tag" :class="{'pest-tag': item.itemType === DiseasePestTypeEnum.PEST}">
                {{ item.itemType === DiseasePestTypeEnum.DISEASE ? '病害' : '虫害' }}
              </span>
            </div>
          </div>

          <!-- 图文列表 -->
          <div 
            v-if="activeTab === 'article'"
            class="article-item" 
            v-for="item in popSciContentList" 
            :key="item.id"
            @click="handlePopSciContentClick(item)"
          >
            <div class="article-cover-wrapper">
              <img 
                :src="item.articleCoverUrl || defaultCover" 
                alt="图文封面" 
                class="article-cover" 
                loading="lazy" 
              />
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

          <!-- 视频列表 -->
          <div 
            v-if="activeTab === 'video'"
            class="video-item" 
            v-for="item in popSciContentList" 
            :key="item.id"
            @click="handlePopSciContentClick(item)"
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
            <div class="video-text-content">
              <h3 class="video-title" :title="item.theme">{{ item.theme }}</h3>
              <div class="video-meta">
                <span v-if="item.mushroomName" class="mushroom-tag" :title="item.mushroomName">
                  {{ item.mushroomName }}
                </span>
                <span :title="item.authorName">作者：{{ item.authorName }}</span>
                <span :title="item.publishDate">发布时间：{{ item.publishDate }}</span>
              </div>
            </div>
          </div>

          <!-- 科普号列表 -->
          <div 
            v-if="activeTab === 'popsciAuthor'"
            class="author-item" 
            v-for="item in popSciAuthorList" 
            :key="item.userId"
            @click="handlePopSciAuthorClick(item)"
          >
            <div class="author-avatar">
              <img 
                :src="item.avatar || defaultAvatar" 
                alt="科普号头像" 
                class="author-avatar-img" 
                loading="lazy" 
              />
            </div>
            <div class="author-info">
              <h3 class="author-name">{{ item.authorName }}</h3>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页器 -->
      <div v-if="hasContent" class="pagination-container">
        <el-pagination
          v-model:currentPage="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 【新增】病虫害详情弹窗 -->
    <DiseasePestsDialog
      v-model="diseasePestDetailDialogVisible"
      :disease-pest-detail="selectedDiseasePest"
      :loading="diseasePestDetailLoading"
      @close="closeDiseasePestDetail"
    />
    
    <!-- 【新增】科普详情弹窗 -->
    <PopSciDetailDialog
      v-model="popSciDetailDialogVisible"
      :detail-data="selectedPopSci"
      :loading="popSciDetailLoading"
    />
    
    <!-- 【新增】菌种详情弹窗 -->
    <MushroomDetailDialog
      v-model="mushroomDetailDialogVisible"
      :mushroom-id="selectedMushroomId"
    />
  </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElEmpty, ElSkeleton, ElPagination, ElMessage, ElIcon } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import defaultCover from '@/assets/default.jpg'
import defaultAvatar from '@/assets/user.jpg'

// API imports
import { 
  getMushroomSummaryPage, 
  getDiseasePestSummaryPage, 
  getAllPopSciContents,
  searchPopSciAuthors
} from '@/api/system'

// Interface imports
import { 
  MushroomSummaryVO, 
  DiseasePestSummaryVO, 
  PopSciContentSummaryVO,
  PopSciAuthorNameVO,
  DiseasePestTypeEnum
} from '@/api/interface'

// 【新增】导入病虫害详情弹窗组件
import DiseasePestsDialog from '@/pages/diseasepests/diseasepestsDialog.vue'
// 【新增】导入科普详情弹窗组件
import PopSciDetailDialog from '@/pages/popSci/PopSciDetailDialog.vue'
// 【新增】导入菌种详情弹窗组件
import MushroomDetailDialog from '@/pages/home/mushroomDetailDialog.vue'

import { useSearch } from './search'  // 未改动

// 调用组合式函数，解构出所有需要的数据和方法
const {
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
  // 【新增】病虫害详情弹窗相关状态
  diseasePestDetailDialogVisible,
  selectedDiseasePest,
  diseasePestDetailLoading,
  closeDiseasePestDetail,
  // 【新增】弹窗相关状态
  popSciDetailDialogVisible,
  selectedPopSci,
  popSciDetailLoading,
  mushroomDetailDialogVisible,
  selectedMushroomId
} = useSearch()

</script>

<style lang="less" scoped>
.search-page {

  display: flex;
  flex-direction: column;
  min-height: 100vh;

  // 粘性标签栏
  .reds-sticky-box {
    --sticky-transition: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s;
    transition: var(--sticky-transition);

    .reds-sticky {
      padding: 30px 0;
      z-index: 5 !important;
      background: hsla(0, 0%, 100%, 0.98);
      position: sticky;
      top: 0;
      width: 80%;
      margin: 0 auto;

      // 外层标签：五个tab
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
    }
  }

  // 新增：卡片容器样式
  .search-card-container {
    width: 90%;
    margin: 20px auto 0;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(33, 3, 3, 0.1);
    padding: 20px;
    min-height: 100px; // 设置最小高度以便看到容器
  }

  // 内容展示区
  .feeds-tab-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    width: 80%;
    margin: 30px auto 0;
    padding-bottom: 40px;

    .loading-container {
      padding: 20px;
    }
    // 内容包装器（包含内容列表或空状态）
    .content-wrapper,
    .loading-container {
    flex: 1 1 auto; // 可伸缩，占据剩余空间
    display: flex;
    flex-direction: column;
    }
    .content-wrapper {
      .content-tip {
        padding: 40px 0;
        text-align: center;
      }

      // 修改：使用 grid 布局，一行显示两个列表项
      .content-list {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
        margin-bottom: 20px;
      }
      
      // 视频tab特殊处理：一行显示四个列表项
      .video-list {
        grid-template-columns: repeat(4, 1fr);
      }

      // 通用卡片样式
      .card-item {
        cursor: pointer;
        border-radius: 8px;
        padding: 12px;
        transition: background 0.3s ease;
        position: relative;
        
        &:hover {
          background: #f9fafb;
        }
      }

      // 菌种卡片和病虫害卡片样式
      .card-item {
        display: flex;
        gap: 16px;
        align-items: flex-start;
        // 增加最小高度，使列表项长度更大
        min-height: 180px;

        .card-cover-wrapper {
          width: 120px;
          height: 120px;
          border-radius: 8px;
          overflow: hidden;
          flex-shrink: 0;

          .card-cover {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .card-text-content {
          flex: 1;
          min-width: 0;

          .card-title {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin: 0 0 8px 0;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 1;
            overflow: hidden;
          }

          .card-desc {
            font-size: 14px;
            color: #666;
            line-height: 1.5;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
            margin-bottom: 8px;
          }

          .disease-type-tag {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 12px;
            background-color: #f0f9eb;
            color: #67c23a;
          }

          .pest-tag {
            background-color: #fef0f0;
            color: #f56c6c;
          }
        }
      }

      // 图文内容样式
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

        .article-cover-wrapper {
          flex-shrink: 0;
          margin-right: 18px;
          border-radius: 8px;
          width: 120px;
          aspect-ratio: 4/3;
          overflow: hidden;

          .article-cover {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
          }
        }

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
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
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

      // 视频内容样式（参考个人中心的视频样式）
      .video-item {
        cursor: pointer;
        border-radius: 8px;
        padding: 8px; /* 减小内边距 */
        transition: background 0.3s ease;
        display: flex;
        flex-direction: column;
        height: 100%;
        position: relative;

        .video-meta {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #909399;
          line-height: 20px; /* 减小行高 */
          align-items: baseline;
        }

        .video-text-content {
          flex: 1;
          margin-bottom: 4px; /* 减小底部间距 */
          
          .mushroom-tag {
            position: static !important;
            display: inline-block;
            margin-top: 6px; /* 减小与发布时间的间距 */
            background-color: #f5f7fa;
            color: var(--el-color-primary);
            padding: 3px 8px; /* 减小内边距 */
            border-radius: 6px;
            font-size: 12px;
            line-height: 14px;
            height: 20px; /* 减小高度 */
            display: inline-flex;
            align-items: center;
          }
        }

        .video-title {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          margin: 0 0 4px 0; /* 减小底部间距 */
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }

        &:hover {
          background: #f9fafb;
        }

        .video-cover-wrapper {
          position: relative;
          border-radius: 8px;
          overflow: hidden;
          flex: 0 0 auto;
          aspect-ratio: 16/9;
          margin-bottom: 8px; /* 减小与文字区的间距 */

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
            padding: 6px; /* 减小播放按钮内边距 */
            transition: opacity 0.2s ease-in-out;
          }
        }

        &:hover .play-btn {
          opacity: 1;
          pointer-events: auto;
        }
      }

      // 科普号列表样式
          // 科普号列表样式
    .author-item {
      display: flex;
      gap: 18px;              // 与图文项的 margin-right 保持一致
      align-items: center;     // 垂直居中（可根据需要改为 flex-start）
      min-height: 180px;       // 与图文项的最小高度一致
      padding: 18px 20px;      // 与图文项的内边距一致
      border: 1px solid #ebeef5;
      border-radius: 8px;
      background-color: #fff;
      cursor: pointer;
      transition: box-shadow 0.3s ease;

      &:hover {
        box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
      }

      .author-avatar {
        flex-shrink: 0;
        width: 120px;          // 放大至与图文封面同宽
        height: 120px;         // 固定高度，保持方形
        border-radius: 8px;    // 改为方形圆角，与图文封面统一
        overflow: hidden;

        .author-avatar-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .author-info {
        flex: 1;
        min-width: 0;

        .author-name {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin: 0 0 8px 0;   // 增加底部间距
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        // 如果存在其他字段（如简介、单位），可在此添加样式
        .author-cert,
        .author-unit {
          font-size: 14px;
          color: #666;
          margin: 4px 0 0;
        }
      }
    }
        }

        // 分页器
        .pagination-container {
          flex-shrink: 0;
          margin-top: 20px;
          display: flex;
          justify-content: center;
          height:48px;
        }
      }
}

// 响应式适配
@media screen and (max-width: 1200px) {
  .search-page {
    .reds-sticky-box .reds-sticky {
      width: 90%;
    }
    
    .search-card-container {
      width: 90%;
      margin-top: 15px;
    }

    .feeds-tab-container {
      width: 90%;
    }
  }
}

@media screen and (max-width: 768px) {
  .search-page {
    .search-card-container {
      padding: 15px;
      min-height: 80px;
    }
    
    .content-list {
      grid-template-columns: 1fr; // 移动端单列显示
    }
    .content-list,
    .content-tip {
        min-height: 1600px; // 8行高度
    }
    .card-item,
    .author-item {
      flex-direction: column;
      align-items: center;
      
      .card-cover-wrapper,
      .author-avatar {
        width: 100px;
        height: 100px;
      }
    }
    
    .article-item {
      flex-direction: column;
      align-items: flex-start;
      padding: 16px;
      min-height: auto;
      
      .article-cover-wrapper {
        margin-right: 0;
        margin-bottom: 12px;
        width: 100%;
        aspect-ratio: 16/9;
      }
    }
    
    .video-item {
      flex-direction: column;
      align-items: flex-start;
      padding: 12px;
      min-height: auto;
      
      .video-cover-wrapper {
        margin-bottom: 10px;
        width: 100%;
        aspect-ratio: 16/9;
      }
    }
  }
}
</style>