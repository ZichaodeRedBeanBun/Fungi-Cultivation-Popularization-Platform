<template>
  <el-dialog
    class="disease-pest-dialog"
    v-model="dialogVisible"
    width="1200px"
    destroy-on-close
    @close="closeDialog"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    
    <div v-else class="disease-pest-detail-content">
      <!-- 核心布局：强制等高布局 -->
      <div class="equal-height-container">
        <!-- 左侧封面图区域 -->
        <div v-if="coverUrl" class="left-image-equal">
          <el-image
            :src="coverUrl"
            :preview-src-list="[coverUrl]"
            class="cover-image"
            fit="cover"
            hide-on-click-modal
          />
        </div>
        
        <!-- 右侧详情文本区域，与左侧等高 -->
        <div class="right-content-equal">
          <div v-if="diseasePestDetail" class="scrollable-content space-y-4 text-gray-700">
            <p v-if="diseasePestDetail.diseaseName"><strong>病害名称：</strong> {{ diseasePestDetail.diseaseName }}</p>
            <p v-if="diseasePestDetail.brief"><strong>简介：</strong> {{ diseasePestDetail.brief }}</p>
            <p v-if="diseasePestDetail.symptom"><strong>为害症状：</strong> {{ diseasePestDetail.symptom }}</p>
            <p v-if="diseasePestDetail.pathogen"><strong>病原：</strong> {{ diseasePestDetail.pathogen }}</p>
            <p v-if="diseasePestDetail.infection"><strong>侵染：</strong> {{ diseasePestDetail.infection }}</p>
            <p v-if="diseasePestDetail.occurrenceRule"><strong>发生规律：</strong> {{ diseasePestDetail.occurrenceRule }}</p>
            <p v-if="diseasePestDetail.prevention"><strong>防治：</strong> {{ diseasePestDetail.prevention }}</p>
            <p v-if="diseasePestDetail.morphology"><strong>形态：</strong> {{ diseasePestDetail.morphology }}</p>
            <p v-if="diseasePestDetail.habits"><strong>习性：</strong> {{ diseasePestDetail.habits }}</p>
            <p v-if="diseasePestDetail.mushroomList !== undefined && diseasePestDetail.mushroomList !== null"><strong>关联菌种：</strong>
              <span v-if="diseasePestDetail.mushroomList?.length">
                <el-tag
                  v-for="m in diseasePestDetail.mushroomList"
                  :key="m.id"
                  size="small"
                  class="mr-1 mb-1"
                >
                  {{ m.mushroomName }}
                </el-tag>
              </span>
              <span v-else>无</span>
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="closeDialog">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElDialog, ElButton, ElTag, ElSkeleton, ElImage } from 'element-plus';
import type { DiseasePestVO } from '@/api/interface';

const props = defineProps<{
  modelValue: boolean;
  diseasePestDetail: DiseasePestVO | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'close'): void;
}>();

const dialogVisible = ref(props.modelValue);
const loading = ref(false);

// 监听外部 modelValue 变化
watch(() => props.modelValue, (newValue) => {
  dialogVisible.value = newValue;
});

// 监听内部 dialogVisible 变化，同步到父组件
watch(dialogVisible, (newValue) => {
  emit('update:modelValue', newValue);
  if (!newValue) {
    emit('close');
  }
});

// 只获取封面图
const coverUrl = computed(() => {
  console.log('coverUrl', props.diseasePestDetail?.coverUrl);
  return props.diseasePestDetail?.coverUrl || '';
});

const closeDialog = () => {
  dialogVisible.value = false;
  emit('close');
};
</script>

<style scoped>
/* 弹窗基础样式 */
.disease-pest-dialog {
  :deep(.el-dialog) {
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    
    .el-dialog__body {
      flex: 1;
      min-height: 0;
      padding: 32px; /* 修改：将 padding 从 20px 增大到 32px */
      overflow: hidden;
    }
    
    .el-dialog__footer {
      padding: 12px 32px; /* 修改：将 footer 的水平 padding 也调整为 32px 以保持一致性 */
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

/* 左侧图片 - 固定宽度 */
.left-image-equal {
  flex: 0 0 380px;
  height: 100%;
  min-height: 500px;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  
  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 4px;
  }
}

/* 右侧内容 - 与左侧等高 */
.right-content-equal {
  flex: 1;
  height: 100%; /* 与左侧等高 */
  display: flex;
  flex-direction: column;
  min-width: 0;
  
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
}

/* 文本样式 */
:deep(.scrollable-content p strong) {
  color: #303133;
  font-size: 20px;
  font-weight: 700;
  display: inline-block;
  min-width: 80px;
}

:deep(.scrollable-content p) {
  margin-bottom: 16px;
  font-size: 16px;
}

:deep(.scrollable-content .el-tag) {
  font-size: 16px;
  padding: 6px 12px;
  height: 32px;
  line-height: 14px;
  margin: 6px 8px 6px 0;
  border-radius: 6px;
  font-weight: 500;
}

/* 弹窗加载中样式 */
.loading-container {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>