<template>
  <div class="flex flex-col h-full flex-1 overflow-hidden bg-background px-4 py-2">
    <h1 class="text-2xl font-bold mb-4">病虫害库</h1>

    <!-- 核心修改1：调整左右布局比例（左1/3 右2/3） -->
    <div class="flex flex-grow gap-4 h-full">
      <!-- 左侧菌种标签：占1/3 + 每行2个标签 -->
      <div class="flex-[1] bg-white rounded-lg shadow p-4 overflow-auto">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">菌种列表</h2>
        <!-- 核心修改2：grid-cols-2 实现每行2个标签 -->
        <div class="grid grid-cols-2 gap-4">
          <div
            v-for="mushroom in uniqueMushrooms"
            :key="mushroom.id"
            class="tag-vertical cursor-pointer"
            @click="fetchDiseasePestsByMushroom(mushroom.id)"
          >
            {{ mushroom.mushroomName }}
          </div>
        </div>
      </div>

      <!-- 右侧病虫害列表：占2/3 + 每行2个卡片 + 可滑动 -->
      <div class="flex-[2] bg-white rounded-lg shadow p-4 flex flex-col h-full">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">病虫害列表</h2>

        <!-- 病害列表 -->
        <div class="mb-6 flex-grow">
          <h3 class="text-lg font-semibold text-gray-800 mb-2">病害（{{ filteredDiseasePests.filter(p => p.itemType === 'disease').length }}种病）</h3>
          <!-- 核心修改3：grid-cols-2 实现每行2个卡片 + 固定高度 + 可滑动 -->
          <div class="grid grid-cols-2 gap-4 max-h-[300px] overflow-y-auto pb-2">
            <div
              v-for="item in filteredDiseasePests.filter(p => p.itemType === 'disease')"
              :key="item.id"
              class="py-4 px-3 rounded border hover:bg-purple-50 cursor-pointer"
              @click="openDiseasePestDialog(item)"
            >
              <h4 class="text-lg font-semibold text-gray-900 mb-1 truncate" :title="item.diseaseName">{{ item.diseaseName }}</h4>
              <p class="text-gray-600 text-sm line-clamp-3 mb-2" :title="item.brief">{{ item.brief }}</p>
            </div>
          </div>
        </div>

        <!-- 虫害列表 -->
        <div class="flex-grow">
          <h3 class="text-lg font-semibold text-gray-800 mb-2">虫害（{{ filteredDiseasePests.filter(p => p.itemType === 'pest').length }}种虫）</h3>
          <!-- 核心修改4：grid-cols-2 实现每行2个卡片 + 固定高度 + 可滑动 -->
          <div class="grid grid-cols-2 gap-4 max-h-[300px] overflow-y-auto">
            <div
              v-for="item in filteredDiseasePests.filter(p => p.itemType === 'pest')"
              :key="item.id"
              class="py-4 px-3 rounded border hover:bg-purple-50 cursor-pointer"
              @click="openDiseasePestDialog(item)"
            >
              <h4 class="text-lg font-semibold text-gray-900 mb-1 truncate" :title="item.diseaseName">{{ item.diseaseName }}</h4>
              <p class="text-gray-600 text-sm line-clamp-3 mb-2" :title="item.brief">{{ item.brief }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 病虫害详情弹窗 -->
    <DiseasePestsDialog
      v-model="diseasePestDialogVisible"
      :disease-pest-detail="selectedDiseasePest"
      @close="closeDiseasePestDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElNotification } from 'element-plus';
import { getAllDiseasePests,getDiseasePestById } from '@/api/system';
import type { MushroomVO, DiseasePestVO } from '@/api/interface';
// 导入新的病虫害详情弹窗组件
import DiseasePestsDialog from './diseasepestsDialog.vue';

const diseasePests = ref<DiseasePestVO[]>([]);
const filteredDiseasePests = ref<DiseasePestVO[]>([]);
const uniqueMushrooms = ref<MushroomVO[]>([]);
const diseasePestDialogVisible = ref(false);
const selectedDiseasePest = ref<DiseasePestVO | null>(null);
const loadingDetail = ref(false); // 添加加载状态
// 获取所有病虫害
const fetchAllDiseasePests = async () => {
  try {
    const res = await getAllDiseasePests({ pageNum: 1, pageSize: 1000 });
    if (res.code === 0) {
      diseasePests.value = res.data.items;
      // 初始显示所有病虫害
      filteredDiseasePests.value = diseasePests.value;
      // 从病虫害列表中提取出相应的菌种并去重
      uniqueMushrooms.value = Array.from(new Map(diseasePests.value.flatMap(pest => pest.mushroomList.map(m => [m.id, m]))).values());
    } else {
      ElNotification.error('获取病虫害列表失败');
    }
  } catch (error) {
    ElNotification.error('获取病虫害列表失败');
  }
}
// 根据选定的菌种获取病虫害（修复筛选逻辑，确保显示所有符合条件的卡片）
const fetchDiseasePestsByMushroom = (mushroomId: number) => {
  // 核心修改6：筛选逻辑优化，确保匹配所有关联该菌种的病虫害
  const filtered = diseasePests.value.filter(pest =>
    pest.mushroomList.some(m => m.id === mushroomId)
  );
  filteredDiseasePests.value = filtered;
  if (filtered.length === 0) {
    ElNotification.info('该菌种暂无关联的病虫害');
  }
};

// 打开病虫害详情对话框 - 修改为异步获取详情
const openDiseasePestDialog = async (item: DiseasePestVO) => {
  loadingDetail.value = true;
  try {
    // 调用接口获取病虫害详细信息
    const res = await getDiseasePestById(item.id);
    if (res.code === 0) {
      selectedDiseasePest.value = res.data as DiseasePestVO;
      diseasePestDialogVisible.value = true;
    } else {
      ElNotification.error('获取病虫害详情失败');
    }
  } catch (error) {
    ElNotification.error('获取病虫害详情失败');
  } finally {
    loadingDetail.value = false;
  }
};

// 关闭对话框
const closeDiseasePestDialog = () => {
  diseasePestDialogVisible.value = false;
  selectedDiseasePest.value = null;
};

// 挂载时获取数据
onMounted(async () => {
  await fetchAllDiseasePests();
});
</script>

<style scoped>
.tag-vertical {
  background-color: white;
  color: #6b5b95;
  border: 1px solid #6b5b95;
  font-size: 14px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  /* 优化标签高度，适配每行2个布局 */
  min-height: 60px;
}

.tag-vertical:hover {
  background-color: #7e57c2;
  color: white;
  border-color: #7e57c2;
}

/* 病虫害卡片样式优化 */
:deep(.border) {
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

:deep(.border:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>