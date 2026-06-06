<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { usePopSciContent } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import EditPen from "@iconify-icons/ep/edit-pen";
import Delete from "@iconify-icons/ep/delete";
import Refresh from "@iconify-icons/ep/refresh";



defineOptions({
  name: "PopSciContentManagement"
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
  buttonClass,
  openAuditDialog,
  deviceDetection,
  onSearch,
  resetForm,
  onbatchDel,
  handleDelete,
  handleSizeChange,
  onSelectionCancel,
  handleCurrentChange,
  handleSelectionChange,
  filterStrain,
  handleStrainClear,
  strainOptions
} = usePopSciContent(tableRef);

const auditForm = reactive({
  id: null as number | null,
  theme: "",
  authorName: "",
  publishDate: "",
  contentStatus: "",
  rejectReason: ""
});

const contentStatusOptions = [
  { label: "待审核", value: "PENDING" },
  { label: "审核通过", value: "PASSED" },
  { label: "审核驳回", value: "REJECTED" }
];

// 监听审核状态变化，清空驳回原因（非驳回时）
watch(
  () => auditForm.contentStatus,
  (val) => {
    if (val !== "REJECTED") {
      auditForm.rejectReason = "";
    }
  }
);
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
        <el-form-item label="作者" prop="authorName">
          <el-input
            v-model="form.authorName"
            placeholder="请输入作者"
            clearable
            class="!w-[180px]"
          />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-select
            v-model="form.contentType"
            placeholder="请选择内容类型"
            clearable
            class="!w-[180px]"
          >
            <el-option label="图文" value="article" />
            <el-option label="视频" value="video" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="theme">
          <el-input
            v-model="form.theme"
            placeholder="请输入标题"
            clearable
            class="!w-[180px]"
          />
        </el-form-item>
        <el-form-item label="关联菌种" prop="mushroomName">
          <el-select
            v-model="form.mushroomName"
            placeholder="请选择关联菌种"
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

      <PureTableBar title="科普内容管理" :columns="columns" @refresh="onSearch">
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
          row-key="id"
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
          <template #operation="{ row }">
            <el-button
              class="reset-margin"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(EditPen)"
              @click="openAuditDialog('审核',row)"
            >
              审核
            </el-button>
            <el-popconfirm
              :title="`是否确认删除科普内容ID: ${row.id} ?`"
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
          </template>
        </pure-table>
      </template>
    </PureTableBar>
    </div>
  </div>
</template>

<style scoped lang="scss">
.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
