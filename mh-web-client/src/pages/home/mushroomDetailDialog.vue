<template>
  <el-dialog
    v-model="localVisible"
    title=""
    destroy-on-close
    class="pop-sci-detail-dialog"
    :show-close="false"
    width="1200px"  
    top="10vh"     
    :modal-append-to-body="false"
    :append-to-body="true"
  >
    <!-- 加载状态 -->
    <div v-if="loadingMushroomDetail || !mushroomDetail" class="loading-container">
      <el-skeleton :rows="8" animated style="width: 100%;" />
    </div>

    <!-- 核心布局：强制等高布局 -->
    <div v-else class="equal-height-container">
      <!-- 左侧：轮播图 -->
      <div class="left-carousel-equal">
        <div v-if="imageList.length > 0" class="carousel-equal-wrapper">
          <el-carousel
            :interval="0"
            arrow="hover"
            indicator-position="none"
            :height="carouselHeight"
            @change="previewIndex = $event"
          >
            <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
              <div class="image-equal-wrapper" @click="openPreview(idx)">
                <img 
                  :src="img" 
                  :alt="'菌种图片' + (idx + 1)" 
                  class="equal-image"
                />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div v-else class="no-image-equal">
          <div class="no-image-content">
            <i class="el-icon-picture-outline"></i>
            <p>暂无菌种图片</p>
          </div>
        </div>
      </div>

      <!-- 右侧：正文内容，与左侧等高 -->
      <div class="right-content-equal">
        <div class="content-header">
          <h2 class="equal-title">{{ mushroomDetail.mushroomName || '未知菌种' }}</h2>
          <div class="equal-meta">
            <span>发布时间：{{ formatDate(mushroomDetail.createTime) }}</span>
          </div>
        </div>
        
        <!-- 可滚动内容区域 -->
        <div class="scrollable-content">
          <section class="equal-section">
            <h3 class="equal-subtitle">简介</h3>
            <p class="equal-text">{{ mushroomDetail.intro || '暂无简介' }}</p>
          </section>
          
          <section class="equal-section">
            <h3 class="equal-subtitle">形态特征</h3>
            <p class="equal-text">{{ mushroomDetail.contentMorphology || '暂无信息' }}</p>
          </section>
          
          <section class="equal-section">
            <h3 class="equal-subtitle">环境要求</h3>
            <p class="equal-text">{{ mushroomDetail.contentEnvironment || '暂无信息' }}</p>
          </section>
          
          <section class="equal-section">
            <h3 class="equal-subtitle">栽培技术</h3>
            <p class="equal-text">{{ mushroomDetail.contentCultivation || '暂无信息' }}</p>
          </section>
          
          <section class="equal-section">
            <h3 class="equal-subtitle">其它信息</h3>
            <p class="equal-text">{{ mushroomDetail.otherDetail || '暂无信息' }}</p>
          </section>
          
          <div class="equal-update">
            <span>最后更新：{{ formatDate(mushroomDetail.updateTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 修复：使用el-image-viewer而不是el-image-preview -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="imageList"
      :initial-index="previewIndex"
      @close="previewVisible = false"
    />

    <!-- 自定义关闭按钮 -->
    <div class="equal-close" @click="closeMushroomDetailDialog">
      <el-button circle class="close-button-equal">
        <Close style="width: 1.2em; height: 1.2em; color: #666" />
      </el-button>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="closeMushroomDetailDialog" type="primary">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElImageViewer } from 'element-plus' // 导入ElImageViewer
import { Close } from '@element-plus/icons-vue'
import { MushroomVO, isMushroomVO } from '@/api/interface'
import { getMushroomById } from '@/api/system'

const props = defineProps<{
  modelValue: boolean;
  mushroomId: number | null;
}>()
const emit = defineEmits(['update:modelValue', 'close'])

const localVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loadingMushroomDetail = ref(false)
const previewVisible = ref(false)
const previewIndex = ref(0)
const mushroomDetail = ref<MushroomVO | null>(null)
const carouselHeight = ref('500px') // 固定高度

// 图片列表
const imageList = computed(() => {
  if (!mushroomDetail.value) return []
  const coverUrl = mushroomDetail.value.coverUrl
  const detailPics = mushroomDetail.value.detailPicUrls?.map(p => p.picUrl || '') || []
  const allPics = [coverUrl, ...detailPics].filter(pic => pic && pic.trim() !== '')
  return [...new Set(allPics)]
})

// 工具方法
const formatDate = (dateStr?: string) => dateStr ? dateStr.split(' ')[0] : '暂无日期'

const openPreview = (index: number) => {
  previewIndex.value = index
  previewVisible.value = true
}

const closeMushroomDetailDialog = () => {
  localVisible.value = false
  mushroomDetail.value = null
  previewVisible.value = false
  previewIndex.value = 0
  emit('close')
}

// 监听弹窗显隐请求详情
watch(
  () => props.modelValue,
  async (isVisible) => {
    if (isVisible && props.mushroomId !== null && typeof props.mushroomId === 'number') {
      loadingMushroomDetail.value = true
      try {
        const res = await getMushroomById(props.mushroomId)
        if (res.code === 0) {
          isMushroomVO(res.data) ? mushroomDetail.value = res.data : ElMessage.error('菌种详情数据格式异常')
        } else {
          ElMessage.error('获取菌种详情失败：' + (res.message || '请求失败'))
        }
      } catch (error) {
        ElMessage.error('获取菌种详情失败，请检查网络')
        console.error('菌种详情请求异常：', error)
      } finally {
        loadingMushroomDetail.value = false
      }
    } else if (!isVisible) {
      mushroomDetail.value = null
      loadingMushroomDetail.value = false
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="less">
/* 弹窗基础样式 */
.pop-sci-detail-dialog {
  :deep(.el-dialog) {
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    
    .el-dialog__body {
      flex: 1;
      min-height: 0;
      padding: 20px;
      overflow: hidden;
    }
    
    .el-dialog__footer {
      padding: 12px 20px;
      border-top: 1px solid #ebeef5;
      flex-shrink: 0;
    }
  }
}

/* 等高容器 - 关键 */
.equal-height-container {
  display: flex;
  height: 500px; /* 固定高度确保两侧等高 */
  gap: 20px;
  width: 100%;
}

/* 左侧轮播 - 固定高度 */
.left-carousel-equal {
  flex: 0 0 380px;
  height: 100%;
  min-height: 500px;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  
  .carousel-equal-wrapper {
    width: 100%;
    height: 100%;
    
    :deep(.el-carousel) {
      width: 100%;
      height: 100%;
      
      .el-carousel__container {
        height: 100% !important;
      }
      
      .el-carousel__item {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
  
  .image-equal-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 10px;
    box-sizing: border-box;
    
    .equal-image {
      width: 100% !important; /* 强制宽度100% */
      height: 100% !important; /* 强制高度100% */
      object-fit: contain;
      border-radius: 4px;
    }
  }
  
  .no-image-equal {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .no-image-content {
      text-align: center;
      color: #909399;
      
      i {
        font-size: 48px;
        color: #c0c4cc;
        display: block;
        margin-bottom: 10px;
      }
      
      p {
        font-size: 14px;
      }
    }
  }
}

/* 右侧内容 - 与左侧等高 */
.right-content-equal {
  flex: 1;
  height: 100%; /* 与左侧等高 */
  display: flex;
  flex-direction: column;
  min-width: 0;
  
  .content-header {
    flex-shrink: 0;
  }
  
  .equal-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }
  
  .equal-meta {
    font-size: 14px;
    color: #606266;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
  }
  
  /* 可滚动区域 - 确保内容超出时滚动 */
  .scrollable-content {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding-right: 8px;
    
    /* 自定义滚动条 */
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
      
      &:hover {
        background: #a8a8a8;
      }
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }
  
  .equal-section {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    
    &:last-of-type {
      border-bottom: none;
    }
    
    .equal-subtitle {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 10px 0;
    }
    
    .equal-text {
      font-size: 16px;
      color: #606266;
      line-height: 1.7;
      margin: 0;
      white-space: pre-line;
    }
  }
  
  .equal-update {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    text-align: right;
    font-size: 14px;
    color: #909399;
  }
}

/* 关闭按钮 */
.equal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  
  .close-button-equal {
    background: white;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: #f5f7fa;
      transform: scale(1.05);
      transition: transform 0.2s;
    }
  }
}

/* 加载容器 */
.loading-container {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>