<script setup lang="ts">
import { useHome } from './home'
import defaultCover from '@/assets/default.jpg'
import { ElNotification, ElMessage, ElImageViewer } from 'element-plus'
import { ElCarousel, ElCarouselItem, ElImage, ElButton } from 'element-plus'
// 核心：引入【独立菌种详情弹窗组件】（替换首页内的冗余弹窗）
import MushroomDetailDialog from './mushroomDetailDialog.vue' // 确保路径正确
// 新增：引入病虫害详情弹窗组件
import DiseasePestsDialog from '@/pages/diseasepests/diseasepestsDialog.vue'
// 新增：引入科普详情弹窗组件
import PopSciDetailDialog from '@/pages/popSci/PopSciDetailDialog.vue'

// 解构useHome，仅取需要的，清理冗余
const {
  bannerList, popSciContents, diseasePests,
  // 菌种弹窗
  mushroomDetailDialogVisible, mushroomId, loadingMushroomDetail,
  // 病虫害弹窗
  diseasePestDialogVisible, selectedDiseasePest, loadingDiseasePest,
  // 科普详情弹窗
  popSciDetailDialogVisible,
  popSciDetailLoading,
  selectedPopSci,
  openPopSciDetail,
  closePopSciDetail,
  // 方法
  goToMoreDiseasePests, goToMorePopSci,
  openMushroomDetailDialog, closeMushroomDetailDialog,
  openDiseasePestDialog, closeDiseasePestDialog,
  formatDate
} = useHome()

// 新增：处理轮播图点击事件
const handleBannerClick = (bannerItem: { bannerId: number; bannerUrl: string; contentId?: number }) => {
  if (bannerItem.contentId) {
    openPopSciDetail(bannerItem.contentId)
  }
}
</script>

<template>
  <div class="p-4 space-y-8">
    <!-- 1. 首页轮播图：添加点击事件处理 -->
    <el-carousel :interval="4000" type="card" height="360px">
      <el-carousel-item v-for="item in bannerList" :key="item.bannerId">
        <img 
          :src="item.bannerUrl" 
          class="w-full h-full object-cover rounded-lg cursor-pointer" 
          @click="handleBannerClick(item)"
        />
      </el-carousel-item>
    </el-carousel>

    <!-- 2. 菌种+病虫害列表：修正点击触发为【传ID】（核心） -->
    <div class="flex gap-6" style="height: 550px;">
      <!-- 科普菌种列表：点击传item.id -->
      <div class="flex-[2] bg-white rounded-lg shadow p-4 overflow-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-gray-800">科普菌种</h2>
          <button @click="goToMorePopSci" class="text-primary underline hover:no-underline text-sm font-medium">
            更多
          </button>
        </div>
        <div class="grid grid-cols-2 gap-6">
          <div
            v-for="item in popSciContents"
            :key="item.id"
            class="border rounded-lg p-4 cursor-pointer hover:shadow-md transition flex"
            @click="openMushroomDetailDialog(item.id)"
          >
            <img :src="item.coverUrl" alt="菌种封面" class="w-24 h-24 object-cover rounded-lg flex-shrink-0 mr-4" loading="lazy" />
            <div class="flex flex-col justify-between">
              <h3 class="font-semibold text-gray-900 truncate mb-1" :title="item.mushroomName">{{ item.mushroomName }}</h3>
              <p class="text-gray-600 text-sm line-clamp-3 mb-2" :title="item.intro">{{ item.intro }}</p>
              <p class="text-gray-500 text-xs">{{ item.updateTime }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 病虫害列表：点击传item.id -->
      <div class="flex-1 bg-white rounded-lg shadow p-4 flex flex-col" style="height: 100%;">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-gray-800">病虫害列表</h2>
          <button @click="goToMoreDiseasePests" class="text-primary underline hover:no-underline text-sm font-medium">
            更多
          </button>
        </div>
        <div class="flex flex-col divide-y divide-gray-200 overflow-y-auto flex-grow">
          <div
            v-for="item in diseasePests"
            :key="item.id"
            class="py-4 cursor-pointer hover:bg-purple-50 px-3 rounded"
            @click="openDiseasePestDialog(item.id)"
          >
            <h3 class="text-lg font-semibold text-gray-900 mb-1 truncate" :title="item.diseaseName">{{ item.diseaseName }}</h3>
            <p class="text-gray-600 text-sm line-clamp-3 mb-2" :title="item.brief">{{ item.brief }}</p>
            <p class="text-gray-400 text-xs text-right">{{ item.updateTime?.split(' ')[0] || '' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心：引入【独立菌种详情弹窗组件】，传递ID和显隐状态 -->
    <MushroomDetailDialog
      v-model="mushroomDetailDialogVisible"
      :mushroom-id="mushroomId"
      @close="closeMushroomDetailDialog"
    />

    <!-- 使用独立的病虫害详情弹窗组件 -->
    <DiseasePestsDialog
      v-model="diseasePestDialogVisible"
      :disease-pest-detail="selectedDiseasePest"
      @close="closeDiseasePestDialog"
    />

    <!-- 新增：科普详情弹窗（参考index.vue 183-192） -->
    <PopSciDetailDialog
      v-model="popSciDetailDialogVisible"
      :detail-data="selectedPopSci"
      :loading="popSciDetailLoading"
    />
  </div>
</template>

<!-- 样式完全保留，无需修改 -->
<style scoped lang="less">
// 你的原有样式，一字不改
</style>