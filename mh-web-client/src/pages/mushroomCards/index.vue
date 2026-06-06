<script setup lang="ts">
import { getBanner, getAllMushrooms, getAllDiseasePests } from '@/api/system'
import { ElNotification, ElMessage } from 'element-plus'
import defaultCover from '@/assets/default.jpg'
import type {
  MushroomVO,
  MushroomPageQueryDTO,
  MushroomSummaryVO,
} from '@/api/interface'

// 修改：导入home目录下的蘑菇详情弹窗组件
import MushroomDetailDialog from '@/pages/home/mushroomDetailDialog.vue'

// 分页组件状态（保留state，统一管理分页参数）
const currentPage = ref(1) // 当前页
const pageSize = ref(10) // 每页显示的数量
const state = reactive({
  size: 'default',
  disabled: false,
  background: false,
  layout: 'total, sizes, prev, pager, next, jumper',
  total: 0, // 总条数（从接口返回赋值）
  pageSizes: [10, 20, 30, 40],
})

// 监听分页变化（无需修改，确保调用fetchMushroomCards）
const handleSizeChange = () => {
  fetchMushroomCards()
}
// 监听当前页变化（无需修改）
const handleCurrentChange = () => {
  fetchMushroomCards()
}

const allMushrooms = ref<MushroomVO[]>([])
const popSciContents = ref<MushroomSummaryVO[]>([])
const mushroomDetailDialogVisible = ref(false)
const mushroomDetail = ref<MushroomVO | null>(null)
const loadingMushroomDetail = ref(false)

// 核心修改：分页参数关联响应式变量，不再写死
const fetchMushroomCards = async () => {
  try {
    const params: MushroomPageQueryDTO = {
      pageNum: currentPage.value, // 改：从写死1 → 绑定currentPage
      pageSize: pageSize.value,   // 改：从写死12 → 绑定pageSize
    }
    const res = await getAllMushrooms(params)
    if (res.code === 0 && res.data && Array.isArray(res.data.items)) {
      allMushrooms.value = res.data.items
      popSciContents.value = allMushrooms.value.map((item: any) => ({
        id: item.id,
        mushroomName: item.mushroomName,
        intro: item.intro,
        coverUrl: item.coverUrl || defaultCover,
        createTime: item.createTime ? item.createTime.split(' ')[0] : '',
        updateTime: item.updateTime ? item.updateTime.split(' ')[0] : '',
      }))
      // 改：将接口返回的总条数赋值给state.total（分页组件需要）
      state.total = res.data.total || 0
    } else {
      ElNotification.error('获取菌种列表失败')
    }
  } catch {
    ElNotification.error('获取菌种列表失败')
  }
}

const openMushroomDetailDialog = (id: number) => {
  const found = allMushrooms.value.find(item => item.id === id)
  if (found) {
    mushroomDetail.value = found
    mushroomDetailDialogVisible.value = true
  } else {
    ElMessage.error('未找到对应菌种详情')
  }
}

const closeMushroomDetailDialog = () => {
  mushroomDetailDialogVisible.value = false
  mushroomDetail.value = null
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

onMounted(() => {
  fetchMushroomCards()
})
</script>

<template>
  <div class="flex flex-col h-full flex-1 overflow-hidden bg-background px-4 py-2">
    <div class="py-4">
      <div class="flex flex-col sm:flex-row gap-4">
      </div>
    </div>

    <!-- 菌菇卡片核心展示区域 -->
    <div class="flex-grow flex flex-col overflow-x-hidden cursor-pointer">
      <div class="flex-1 overflow-x-hidden my-2">
        <!-- 修改: 保持 grid-cols-5 不变，但增大 gap 并缩小卡片尺寸 -->
        <div class="grid grid-cols-5 gap-12"> 
          <div
            v-for="item in popSciContents"
            :key="item.id"
            class="rounded-lg hover:bg-background transition duration-300 border bg-card text-card-foreground shadow-sm overflow-hidden hover:shadow-lg cursor-pointer"
            @click="openMushroomDetailDialog(item.id)"
          >
            <div class="relative">
              <img
                :src="item.coverUrl || defaultCover"
                alt="菌种封面"
                class="w-full aspect-square object-cover"
                loading="lazy"
              />
            </div>
            <div class="px-2 pb-1 pt-1">
              <h3 
                class="font-semibold text-gray-900 truncate mb-1 line-clamp-1 text-sm" 
                :title="item.mushroomName"
              >
                {{ item.mushroomName }}
              </h3>
              <p 
                class="text-gray-600 text-xs line-clamp-2 mb-1" 
                :title="item.intro"
              >
                {{ item.intro }}
              </p>
              <p class="text-gray-500 text-xs">
                {{ item.updateTime }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页组件：核心修改 → total绑定state.total，page-size绑定state.size（可选） -->
    <nav class="mx-auto flex w-full justify-center mt-3">
      <el-pagination 
        v-model:page-size="pageSize" 
        v-model:current-page="currentPage" 
        :total="state.total"
        :page-sizes="state.pageSizes" 
        :layout="state.layout"
        @size-change="handleSizeChange" 
        @current-change="handleCurrentChange" 
        class="mb-3" 
      />
    </nav>

    <!-- 使用home目录下的菌种详情弹窗组件 -->
    <MushroomDetailDialog
      v-model="mushroomDetailDialogVisible"
      :mushroomId="mushroomDetail?.id || null"
      @close="closeMushroomDetailDialog"
    />
  </div>
</template>
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

// 图片轮播容器
.image-carousel {
  min-width: 400px;   // 保证最小显示区域
  max-width: 800px;   // 避免过宽
  border-radius: 12px;
  box-shadow: 0 4px 12px rgb(0 0 0 / 0.1);
  cursor: pointer;
  height: auto;       // 高度自适应图片
}

// 轮播图片样式
.carousel-image {
  width: 100%;
  object-fit: contain;
  background-color: #f5f5f5;
  border-radius: 12px;
  user-select: none;
}

// 轮播项样式（适配图片容器）
:deep(.el-carousel-item) {
  min-width: 400px;   // 最小宽度，和.image-carousel一致
  max-width: 800px;   // 最大宽度，和.image-carousel一致
  height: 600px;      // 固定高度，匹配图片显示
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