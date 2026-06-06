<script setup lang="ts">
import { ref } from "vue";
import { useMushroom } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";

import Upload from "@iconify-icons/ri/upload-line";
import More from "@iconify-icons/ep/more-filled";
import Delete from "@iconify-icons/ep/delete";
import EditPen from "@iconify-icons/ep/edit-pen";
import Refresh from "@iconify-icons/ep/refresh";
import AddFill from "@iconify-icons/ri/add-circle-line";
// 新增：导入详情图图标（和Upload同风格的iconify图标）
import Picture from "@iconify-icons/ep/picture";
defineOptions({
  name: "MushroomManagement"
});

const formRef = ref();
const tableRef = ref();

// 补充导出所有需要的变量/方法
const {
  form,
  loading,
  columns,
  dataList,
  strainOptions,
  selectedNum,
  pagination,
  buttonClass,
  deviceDetection,
  detailPicDialogVisible,
  detailFileList,
  detailUploadLoading,
  detailUploadRef,
  uploadedPics,
  onSearch,
  resetForm,
  onbatchDel,
  openDialog,
  openDetailPicUploadDialog,
  showDeleteConfirm,
  submitDetailPicUpload,
  handleDetailUploadChange,
  handleDialogClose,
  handleUpdate,
  handleDelete,
  handleUpload,
  handleSizeChange,
  onSelectionCancel,
  handleCurrentChange,
  handleSelectionChange,
  handleBeforeDetailUpload,
  confirmDeleteDetailPic,
  filterStrain, // 新增：全量菌种搜索方法（从hook导入）
  handleStrainClear // 新增：清空选择器逻辑（从hook导入）
} = useMushroom(tableRef);
</script>

<template>
  <div :class="['flex', 'justify-between', deviceDetection() && 'flex-wrap']">
    <div
      :class="[deviceDetection() ? ['w-full', 'mt-2'] : 'w-[calc(100%-0px)]']"
    >
      <el-form
        ref="formRef"
        :inline="true"
        :model="form"
        
        class="search-form bg-bg_color w-[99/100] pl-8 pt-[12px] overflow-auto"
      >
       <!-- 替换为菌菇名选择器（带模糊搜索+清空） -->
    <!-- 搜索表单中的菌种选择器 -->
      <el-form-item label="菌种：" prop="mushroomName">
          <!-- 正确：注释写在标签外，属性直接声明（filterable/clearable是布尔属性，直接写即可） -->
          <el-select
            v-model="form.mushroomName"
            placeholder="请选择/搜索菌种名"
            filterable  
            clearable  
            class="!w-[180px]" 
             @clear="handleStrainClear" 
            :filter-method="filterStrain" 
          >
            <el-option
              v-for="strain in strainOptions"
              :key="strain.value"
              :label="strain.label"
              :value="strain.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="useRenderIcon('ri:search-line')"
            :loading="loading"
            @click="onSearch"
          >
            搜索
          </el-button>
          <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <PureTableBar title="菌种卡片管理" :columns="columns" @refresh="onSearch">
        <template #buttons>
          <el-button
            type="primary"
            :icon="useRenderIcon(AddFill)"
            @click="openDialog()"
          >
            新增菌种
          </el-button>
        </template>
        <template v-slot="{ size, dynamicColumns }">
          <div
            v-if="selectedNum > 0"
            v-motion-fade
            class="bg-[var(--el-fill-color-light)] w-full h-[46px] mb-2 pl-4 flex items-center"
          >
            <div class="flex-auto">
              <span
                style="font-size: var(--el-font-size-base)"
                class="text-[rgba(42,46,54,0.5)] dark:text-[rgba(220,220,242,0.5)]"
              >
                已选 {{ selectedNum }} 项
              </span>
              <el-button type="primary" text @click="onSelectionCancel">
                取消选择
              </el-button>
            </div>
            <el-popconfirm title="是否确认删除?" @confirm="onbatchDel">
              <template #reference>
                <el-button type="danger" text class="mr-1">
                  批量删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <pure-table
            ref="tableRef"
            row-key="mushroomId"
            adaptive
            :adaptiveConfig="{ offsetBottom: 108 }"
            align-whole="center"
            table-layout="auto"
            :loading="loading"
            :size="size"
            :data="dataList"
            :columns="dynamicColumns"
            :pagination="{ ...pagination, size }"
            :header-cell-style="{
              background: 'var(--el-fill-color-light)',
              color: 'var(--el-text-color-primary)'
            }"
            @selection-change="handleSelectionChange"
            @page-size-change="handleSizeChange"
            @page-current-change="handleCurrentChange"
          >
             <!-- 操作列 -->
            <template #operation="{ row }">
              <el-button
                class="reset-margin"
                link
                type="primary"
                :size="size"
                :icon="useRenderIcon(EditPen)"
                @click="openDialog('修改', row)"
              >
                修改
              </el-button>
              <el-popconfirm
                :title="`是否确认删除菌种编号为 ${row.mushroomId} 的这条数据`"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    class="reset-margin"
                    link
                    type="primary"
                    :size="size"
                    :icon="useRenderIcon(Delete)"
                  >
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
              <el-dropdown>
                <el-button
                  class="ml-3 mt-[2px]"
                  link
                  type="primary"
                  :size="size"
                  :icon="useRenderIcon(More)"
                  @click="handleUpdate(row)"
                />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <el-button
                        :class="buttonClass"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Upload)"
                        @click="handleUpload(row)"
                      >
                        上传封面
                      </el-button>
                    </el-dropdown-item>
                    <!-- 新增：上传详情图（独立入口，和列内按钮功能一致） -->
                    <!-- 优化：上传详情图按钮（完全参考上传封面写法） -->
                      <el-dropdown-item>
                        <el-button
                        :class="buttonClass"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Picture)"
                        @click="openDetailPicUploadDialog(row)"
                      >
                        上传详情图
                      </el-button>
                      </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </pure-table>
        </template>
      </PureTableBar>
    </div>
   <!-- 详情图上传弹窗（支持多张） -->
   <el-dialog
    v-model="detailPicDialogVisible"
    title="上传菌种详情图"
    width="50%"
    :close-on-click-modal="false"
    :before-close="handleDialogClose"
  >
    <!-- 上传区域 -->
    <el-upload
      ref="detailUploadRef"
      action=""
      multiple
      drag
      :file-list="detailFileList"
      :auto-upload="false"
      accept=".jpg,.jpeg,.png,.webp"
      :before-upload="handleBeforeDetailUpload"
      @change="handleDetailUploadChange"
      class="upload-area"
    >
      <i class="el-icon-upload" style="font-size: 28px;"></i>
      <div class="el-upload__text">拖拽文件到此处上传，或<em>点击上传</em></div>
      <div class="el-upload__tip">支持多文件上传，仅允许jpg/png/webp格式，单文件不超过5MB</div>
    </el-upload>

    <!-- 已上传详情图预览列表（优化为横向并排） -->
    <div class="uploaded-list" v-if="uploadedPics.length > 0">
      <div
        v-for="(pic, index) in uploadedPics"
        :key="pic.url + index"
        class="pic-item"
      >
        <!-- 点击图片触发删除确认框，美化样式 -->
        <el-image
          :src="pic.url"
          fit="cover"
          class="pic-preview"
          preview-src-list="[pic.url]"
          @click="showDeleteConfirm(pic)"
          :style="{ cursor: 'pointer' }"
        />
      </div>
    </div>

    <!-- 修复弹窗底部插槽写法（Vue3标准语法） -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="detailPicDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="detailUploadLoading"
          @click="submitDetailPicUpload"
        >
          上传
        </el-button>
      </div>
    </template>
  </el-dialog>

  </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

:deep(.el-button:focus-visible) {
  outline: none;
}

.main-content {
  margin: 24px 24px 0 !important;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
// 详情图列表样式
:deep(.detail-pic-list) {
  .el-image {
    position: relative;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;
    .el-button {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      display: none;
    }
    &:hover .el-button {
      display: block;
    }
  }
}

:deep(.image-error) {
  background-color: #f5f7fa;
}
/* 其他原有样式不变，新增/修改以下样式 */
:deep(.upload-area) {
  margin-bottom: 20px; // 上传区域与预览列表拉开间距
}

/* 详情图预览列表：横向并排 + 自动换行 */
.uploaded-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px; // 图片之间的间距
  margin-top: 20px;
}

/* 单个图片项：美化样式 + hover效果 */
.pic-item {
  position: relative;
}
.pic-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px; // 圆角
  border: 1px solid #ebeef5; // 浅色边框
  transition: all 0.3s ease; // 过渡动画

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15); // hover时添加阴影
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
