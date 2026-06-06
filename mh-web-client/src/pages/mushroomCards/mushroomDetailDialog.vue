<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mushroomDetail?.mushroomName || '菌种详情'"
    width="1200px"
    :before-close="closeDialog"
    destroy-on-close
    class="custom-mushroom-dialog"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else class="mushroom-detail-content flex gap-6">
      <el-carousel
        v-if="imageList.length"
        :interval="0"
        arrow="always"
        class="image-carousel"
        indicator-position="none"
        :initial-index="previewIndex"
        @change="previewIndex = $event"
      >
        <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
          <el-image
            :src="img"
            :preview-src-list="imageList"
            :initial-preview-index="previewIndex"
            class="carousel-image"
            fit="cover"
            hide-on-click-modal
          />
        </el-carousel-item>
      </el-carousel>

      <div class="detail-text flex-1 flex flex-col justify-between max-h-[400px] overflow-auto">
        <div>
          <h3 class="text-3xl font-semibold mb-6">{{ mushroomDetail?.mushroomName }}</h3>
          <section class="mb-6">
            <h4 class="text-xl font-semibold mb-2 border-b border-gray-300 pb-1">简介</h4>
            <p class="whitespace-pre-wrap text-gray-700">{{ mushroomDetail?.intro }}</p>
          </section>
          <section class="mb-6">
            <h4 class="text-xl font-semibold mb-2 border-b border-gray-300 pb-1">形态特征</h4>
            <p class="whitespace-pre-wrap text-gray-700">{{ mushroomDetail?.contentMorphology }}</p>
          </section>
          <section class="mb-6">
            <h4 class="text-xl font-semibold mb-2 border-b border-gray-300 pb-1">环境要求</h4>
            <p class="whitespace-pre-wrap text-gray-700">{{ mushroomDetail?.contentEnvironment }}</p>
          </section>
          <section class="mb-6">
            <h4 class="text-xl font-semibold mb-2 border-b border-gray-300 pb-1">栽培技术</h4>
            <p class="whitespace-pre-wrap text-gray-700">{{ mushroomDetail?.contentCultivation }}</p>
          </section>
          <section class="mb-6">
            <h4 class="text-xl font-semibold mb-2 border-b border-gray-300 pb-1">其它信息</h4>
            <p class="whitespace-pre-wrap text-gray-700">{{ mushroomDetail?.otherDetail }}</p>
          </section>
        </div>
        <div class="text-right text-gray-500 text-sm mt-4">
          <p>创建时间：{{ formatDate(mushroomDetail?.createTime) || '-' }}</p>
          <p>更新时间：{{ formatDate(mushroomDetail?.updateTime) || '-' }}</p>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="closeDialog">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElSkeleton, ElDialog, ElCarousel, ElCarouselItem,  ElButton } from 'element-plus'
import type { MushroomVO } from '@/api/interface'

const props = defineProps<{
  modelValue: boolean
  mushroomDetail: MushroomVO | null
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}>()

const dialogVisible = ref(props.modelValue)
const previewVisible = ref(false)
const previewIndex = ref(0)

watch(() => props.modelValue, (newValue) => {
  dialogVisible.value = newValue
})

watch(dialogVisible, (newValue) => {
  emit('update:modelValue', newValue)
})

const imageList = computed(() => {
  if (!props.mushroomDetail) return []
  const pics = props.mushroomDetail.detailPicUrls || []
  return [props.mushroomDetail.coverUrl, ...pics.map(p => p.picUrl || p.picUrl)]
})

function openPreview(index: number) {
  previewIndex.value = index
  previewVisible.value = true
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

function closeDialog() {
  dialogVisible.value = false
  emit('close')
}
</script>

<style scoped lang="less">
// 菌种详情弹窗整体样式
.custom-mushroom-dialog .el-dialog__header {
  background-color: #f9fafb;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  font-size: 20px;
  color: #303133;
  max-height: 1000px;
  overflow-y: auto;
}

// 弹窗内容区通用样式
.el-dialog__body {
  max-height: 1000px;
  overflow-y: auto;
  padding: 20px 24px;
}

// 菌种详情内容容器
.mushroom-detail-content {
  display: flex;
  gap: 30px;
  overflow: auto;
  height: 100%;
}

// 图片轮播容器 - 修改为参照科普编辑弹窗样式
.image-carousel {
  min-width: 400px;   
  max-width: 800px;   
  border-radius: 8px; // 修改: 与科普编辑弹窗一致的圆角
  background: #000;   // 修改: 黑色背景
  height: 100%;       // 修改: 高度自适应
  cursor: pointer;
}

// 轮播图片样式 - 修改为参照科普编辑弹窗样式
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;  // 修改: 使用 cover 模式
  border-radius: 8px; // 修改: 与容器一致的圆角
  user-select: none;
}

// 轮播项样式 - 修改为参照科普编辑弹窗样式
:deep(.el-carousel-item) {
  min-width: 400px;   
  max-width: 800px;   
  height: 100%;       // 修改: 高度自适应
  box-sizing: border-box;
}

// 文字详情区域
.detail-text {
  overflow-y: auto;
  padding-right: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  max-height: 400px;
}

.detail-text h3 {
  font-weight: 700;
  font-size: 28px;
  margin-bottom: 24px;
  color: #303133;
}

.detail-text section {
  margin-bottom: 24px;
}

.detail-text section h4 {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 8px;
  border-bottom: 1px solid #e2e2e2;
  padding-bottom: 6px;
  color: #444;
}

.detail-text section p {
  white-space: pre-wrap;
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
}

.detail-text > div.text-right {
  text-align: right;
  color: #909399;
  font-size: 13px;
  margin-top: 12px;
}

// 弹窗加载中样式
.loading-container {
  padding: 40px 0;
  text-align: center;
}
</style>