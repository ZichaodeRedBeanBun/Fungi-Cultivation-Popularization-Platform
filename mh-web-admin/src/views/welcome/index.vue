<script setup lang="ts">
import { ref } from "vue";
import ReCol from "@/components/ReCol";
import { useDark } from "./utils";
import { ReNormalCountTo } from "@/components/ReCountTo";
import { ChartBar, ChartLine, ChartPie1, ChartPie2 } from "./components/charts";
import useChartData from "./hooks/useChartData"; 
// 替换图标：匹配业务场景
import GroupLine from "@iconify-icons/ri/group-line";
import MushroomIcon from "@iconify-icons/ri/plant-line"; 
import ContentIcon from "@iconify-icons/ri/file-text-line"; 
import DiseaseIcon from "@iconify-icons/ri/virus-line"; 

defineOptions({
  name: "Welcome"
});

const { isDark } = useDark();

// 从hooks获取所有数据
const {
  userCount,
  artistCount: mushroomCount, 
  songCount: contentCount,     
  playlistCount: diseaseCount, 
  westernPopCount,
  chinesePopCount,
  cantonesePopCount,
  koreanPopCount,
  classicCount,
  hiphopCount,
  rockCount,
  electronicCount,
  jazzCount,
  lightCount,
  countAmerica,
  countChina,
  countKorea,
  countJapan,
  countGermany,
  countBritain,
  maleCount,
  femaleCount,
  pieChartData1,       
  userIdentityBarData, 
  contentTypePieData   
} = useChartData();
</script>

<template>
  <div class="main-content" :style="{ '--card-bg-color': isDark ? '#1f2937' : '#ffffff' }">
    <el-row :gutter="24" justify="space-around">
      <!-- 原有4个卡片（兼容保留） -->
      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 80 * 1
          }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">用户数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{
                backgroundColor: isDark ? 'transparent' : '#effaff'
              }"
            >
              <IconifyIconOffline :icon="GroupLine" :color="'#41b6ff'" width="18" />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="100"
                :endVal="userCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#41b6ff'"
              :data="[210, 439, 530, 496, 575, 680, 845]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 80 * 2
          }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">菌种卡片数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{
                backgroundColor: isDark ? 'transparent' : '#fff5f4'
              }"
            >
              <IconifyIconOffline :icon="MushroomIcon" :color="'#e85f33'" width="18" />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="100"
                :endVal="mushroomCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#e85f33'"
              :data="[210, 399, 500, 492, 652, 688, 850]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 80 * 3
          }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">科普内容数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{
                backgroundColor: isDark ? 'transparent' : '#eff8f4'
              }"
            >
              <IconifyIconOffline :icon="ContentIcon" :color="'#26ce83'" width="18" />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="100"
                :endVal="contentCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#26ce83'"
              :data="[2101, 3399, 5300, 4962, 6952, 6808, 8450]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 80 * 4
          }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">病虫害数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{
                backgroundColor: isDark ? 'transparent' : '#f6f4fe'
              }"
            >
              <IconifyIconOffline :icon="DiseaseIcon" :color="'#7846e5'" width="18" />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="100"
                :endVal="diseaseCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#7846e5'"
              :data="[2101, 5288, 4239, 4962, 6752, 5208, 7450]"
            />
          </div>
        </el-card>
      </re-col>

      <!-- 核心图表区域 -->
      <re-col
        v-motion
        class="mb-[18px]"
        :value="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 400
          }
        }"
      >
        <el-card class="bar-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">菌种的科普内容比例</span>
          </div>
          <div class="flex justify-between items-start mt-3">
            <ChartPie1 :chartData="pieChartData1" />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="12"
        :xs="24"
        :initial="{
          opacity: 0,
          y: 100
        }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: {
            delay: 400
          }
        }"
      >
        <el-card class="bar-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">用户身份分布</span>
          </div>
          <div class="flex justify-between items-start mt-3">
            <ChartBar 
              :barChartData="userIdentityBarData" 
              :xAxisLabels="['普通用户', '待认证科普号', '已认证科普号']"
              seriesName="用户数量"
            />
          </div>
        </el-card>

        <el-card class="bar-card bar-position" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">科普内容类型比例</span>
          </div>
          <div class="flex justify-between items-start mt-3">
            <ChartPie2 :chartData="contentTypePieData" />
          </div>
        </el-card>
      </re-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
// 完全保留原有所有样式
:deep(.el-card) {
  --el-card-border-color: none;
  // 恢复卡片背景色默认值（原歌单页面是白色/深色适配）
  background-color: var(--card-bg-color);
  // 核心修复：添加和原歌单页面一致的卡片阴影
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
  border-radius: 8px; // 原歌单页面卡片圆角

  /* 解决概率进度条宽度 */
  .el-progress--line {
    width: 85%;
  }

  /* 解决概率进度条字体大小 */
  .el-progress-bar__innerText {
    font-size: 15px;
  }

  /* 隐藏 el-scrollbar 滚动条 */
  .el-scrollbar__bar {
    display: none;
  }

  /* el-timeline 每一项上下、左右边距 */
  .el-timeline-item {
    margin: 0 6px;
  }
}

.main-content {
  margin: 20px 20px 0 !important;
  // 定义CSS变量默认值（和原歌单页面一致）
  --card-bg-color: #ffffff;
  padding: 0 16px; // 原歌单页面的容器内边距
}

.bar-position {
  margin-top: 18px;
}

// 恢复图表容器原有高度（还原饼图大小）
:deep(.bar-card) {
  .el-card__body {
    padding: 16px;
  }
  // ChartPie1恢复原高度587px
  :deep(.chart-pie1-container) {
    width: 100%;
    height: 587px !important;
  }
  // ChartBar恢复原高度253px
  :deep(.chart-bar-container) {
    width: 100%;
    height: 253px !important;
  }
  // ChartPie2保持原高度240px
  :deep(.chart-pie2-container) {
    width: 100%;
    height: 240px !important;
  }
}

// 折线图卡片样式（和原歌单页面一致）
:deep(.line-card) {
  .el-card__body {
    padding: 20px;
  }
}
</style>