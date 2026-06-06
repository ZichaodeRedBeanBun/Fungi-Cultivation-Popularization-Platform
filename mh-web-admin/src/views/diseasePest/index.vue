<script setup lang="ts">
import { ref } from "vue";
import { useDiseasePest } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import AddFill from "@iconify-icons/ri/add-circle-line";
import Delete from "@iconify-icons/ep/delete";
import Refresh from "@iconify-icons/ep/refresh";
// 新增：导入上传和更多图标
import Upload from "@iconify-icons/ri/upload-line";
import More from "@iconify-icons/ep/more-filled";

defineOptions({
  name: "DiseasePestManagement"
});

const formRef = ref();
const tableRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  selectedNum,
  pagination,
  openEditDialog,
  deviceDetection,
  onSearch,
  resetForm,
  onbatchDel,
  handleDelete,
  handleSizeChange,
  onSelectionCancel,
  handleCurrentChange,
  handleSelectionChange,
  // 新增：导入上传封面方法
  handleUpload
} = useDiseasePest(tableRef);
</script>

<template>
  <div :class="['flex', 'justify-between', deviceDetection() && 'flex-wrap']">
    <div :class="[deviceDetection() ? ['w-full', 'mt-2'] : 'w-[calc(100%-0px)]']">
      <el-form
        ref="formRef"
        :inline="true"
        :model="form"
        class="search-form bg-bg_color w-[99/100] pl-8 pt-[12px] overflow-auto"
      >
        <el-form-item label="病虫害名称" prop="diseaseName">
          <el-input
            v-model="form.diseaseName"
            placeholder="请输入病虫害名称"
            clearable
            class="!w-[180px]"
          />
        </el-form-item>
        <el-form-item label="类型" prop="itemType">
          <el-select v-model="form.itemType" placeholder="请选择类型" clearable class="!w-[140px]">
            <el-option label="病害" :value="1" />
            <el-option label="虫害" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="useRenderIcon('ri:search-line')"
            :loading="loading"
            @click="onSearch"
          >
            查询
          </el-button>
          <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 搜索表单下方 PureTableBar -->
    <PureTableBar title="病虫害管理" :columns="columns" @refresh="onSearch">
      <template #buttons>
        <el-button
          type="primary"
          :icon="useRenderIcon(AddFill)"
          @click="openEditDialog('新增')"
        >
          新增病虫害
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
              已选择 {{ selectedNum }} 条
            </span>
            <el-button type="primary" text @click="onSelectionCancel">
              取消选择
            </el-button>
          </div>
          <el-popconfirm title="确定删除选中的病虫害吗？" @confirm="onbatchDel">
            <template #reference>
              <el-button type="danger" text class="mr-1">
                批量删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>

        <pure-table
          ref="tableRef"
          row-key="id"
          adaptive
          :adaptiveConfig="{ offsetBottom: 108 }"
          align-whole="center"
          table-layout="auto"
          :loading="loading"
          :size="size"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="{ ...pagination, size, pageSizes: [10, 20, 50, 100] }"
          :header-cell-style="{
            background: 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-primary)'
          }"
          @selection-change="handleSelectionChange"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
        >
          <template #operation="{ row }">
            <el-button
              class="reset-margin"
              link
              type="primary"
              :size="size"
              icon="edit"
              @click="openEditDialog('修改', row)"
            >
              修改
            </el-button>
            <el-popconfirm
              :title="`确定删除病虫害【${row.diseaseName}】吗？`"
              @confirm="() => handleDelete(row)"
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
            <!-- 新增：下拉菜单用于上传封面 -->
            <el-dropdown>
              <el-button
                class="ml-3 mt-[2px]"
                link
                type="primary"
                :size="size"
                :icon="useRenderIcon(More)"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button
                      class="!h-[20px] reset-margin !text-gray-500 dark:!text-white dark:hover:!text-primary"
                      link
                      type="primary"
                      :size="size"
                      :icon="useRenderIcon(Upload)"
                      @click="handleUpload(row)"
                    >
                      上传封面
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
</style>