<script setup lang="ts">
import { ref ,computed} from "vue";
import ReCol from "@/components/ReCol";
import { formRules } from "../utils/rule";
import type { FormProps } from "../utils/types";

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    formTitle: "查看",
    id: 0,
    mushroomName: "", // 关联菌种名称（只读展示）
    content: "",
    theme: "",
    authorName: "",
    contentType: "ARTICLE" as "ARTICLE" | "VIDEO",
    publishDate: "",
    description: "",
    contentStatus: "PENDING", // 1: 待审核, 2: 审核通过, 3: 审核驳回
    rejectReason: "",
  })
});

const contentTypeLabel = computed(() => {
  if (newFormInline.value.contentType === "ARTICLE") return "图文";
  if (newFormInline.value.contentType === "VIDEO") return "视频";
  return "";
});
const ruleFormRef = ref();
const newFormInline = ref({
  ...props.formInline
});

function getRef() {
  return ruleFormRef.value;
}
function getFormData() {
  return JSON.parse(JSON.stringify(newFormInline.value));
}

defineExpose({ getRef, getFormData });
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="formRules"
    label-width="82px"
  >
    <el-row :gutter="30">
      <!-- 内容ID 只读展示 -->
      <re-col 
      v-if="newFormInline.formTitle === '审核'"
      :value="12" :xs="24" :sm="24">
        <el-form-item label="内容ID" prop="id">
          <el-input v-model="newFormInline.id" disabled />
        </el-form-item>
      </re-col>
      <!-- 标题 只读展示 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="标题" prop="theme">
          <el-input v-model="newFormInline.theme" disabled />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
      <el-form-item :label="contentTypeLabel" prop="content">
        <template v-if="newFormInline.contentType === 'VIDEO'">
          <video
            v-if="newFormInline.videoUrl"
            :src="newFormInline.videoUrl"
            controls
            style="width: 100%; max-height: 400px;"
          />
          <div v-else class="text-center text-gray-500">暂无视频链接</div>
        </template>
        <template v-else>
          <el-input
            v-model="newFormInline.content"
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 10 }"
            disabled
          />
        </template>
      </el-form-item>
    </re-col>

      
      <!-- 作者 只读展示 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="作者" prop="authorName">
          <el-input v-model="newFormInline.authorName" disabled />
        </el-form-item>
      </re-col>

      <!-- 内容类型 只读展示 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="内容类型" prop="contentType">
          <el-input :value="contentTypeLabel" disabled />
        </el-form-item>
      </re-col>
      <!-- 关联菌种 只读展示 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="关联菌种" prop="mushroomName">
          <el-input v-model="newFormInline.mushroomName" disabled />
        </el-form-item>
      </re-col>

      <!-- 发布日期 只读展示 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="发布日期" prop="publishDate">
          <el-input v-model="newFormInline.publishDate" disabled />
        </el-form-item>
      </re-col>

      <!-- 描述 只读展示 -->
      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="newFormInline.description"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            disabled
          />
        </el-form-item>
      </re-col>

      <!-- 审核状态 可编辑 -->
      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="审核状态" prop="contentStatus" required>
          <el-select v-model="newFormInline.contentStatus" placeholder="请选择审核状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="审核通过" value="PASSED" />
            <el-option label="审核驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
      </re-col>

      <!-- 驳回原因 仅审核驳回时显示且必填 -->
      <re-col :value="24" :xs="24" :sm="24" v-if="newFormInline.contentStatus === 'REJECTED'">
        <el-form-item label="驳回原因" prop="rejectReason" required>
          <el-input
            v-model="newFormInline.rejectReason"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
