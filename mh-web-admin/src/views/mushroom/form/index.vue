<script setup lang="ts">
import { ref,watch } from "vue";
import ReCol from "@/components/ReCol";
import { formRules } from "../utils/rule";
import { FormProps } from "../utils/types";

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    formTitle: "新增",
    mushroomId: 0,
    mushroomName: "",
    intro: "", // 替换introduction
    morphologicalCharacteristics: "",
    environmentalRequirements: "",
    cultivationTechnology: "",
    // TODO 要不要改掉详情图
    detailPicUrls: [], // 详情图URL默认空数组
    otherDetail:""
  })
});
const detailFileList = ref<any[]>([]); // 待上传的文件列表
const detailUploadLoading = ref(false); // 上传加载状态
const ruleFormRef = ref();
// const newFormInline = ref(props.formInline);
const newFormInline = ref({
  ...props.formInline
});

function getRef() {
  return ruleFormRef.value;
}
function getFormData() {
  // 深拷贝避免引用问题，确保返回的是纯数据
  return JSON.parse(JSON.stringify(newFormInline.value));
}

defineExpose({ getRef, getFormData  });
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="formRules"
    label-width="82px"
  >
    <el-row :gutter="30">
      <re-col
        v-if="newFormInline.formTitle === '修改'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="菌种编号" prop="mushroomId">
          <el-input
            v-model="newFormInline.mushroomId"
            disabled
            placeholder="newFormInline.mushroomId"
          />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="菌种" prop="mushroomName" required>
          <el-input
            v-model="newFormInline.mushroomName"
            clearable
            placeholder="请输入菌种名字"
          />
        </el-form-item>
      </re-col>

      <re-col>
        <el-form-item label="基本信息" prop="intro" required>
          <el-input
            v-model="newFormInline.intro"
            placeholder="请输入基本信息"
            type="textarea"
            line-number
            :autosize="{ minRows: 4, maxRows: 10 }"
          />
        </el-form-item>
      </re-col>
      <!-- 形态特征（补充漏加的字段） -->
      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="形态特征" prop="morphologicalCharacteristics" required>
          <el-input
            v-model="newFormInline.morphologicalCharacteristics"
            placeholder="请输入形态特征"
            type="textarea"
            line-number
            :autosize="{ minRows: 4, maxRows: 10 }"
          />
        </el-form-item>
      </re-col>

      <!-- 环境要求 -->
      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="环境要求" prop="environmentalRequirements" required>
          <el-input
            v-model="newFormInline.environmentalRequirements"
            placeholder="请输入环境要求（温度/湿度/光照等）"
            type="textarea"
            line-number
            :autosize="{ minRows: 4, maxRows: 10 }"
          />
        </el-form-item>
      </re-col>

      <!-- 栽培技术 -->
      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="栽培技术" prop="cultivationTechnology" required>
          <el-input
            v-model="newFormInline.cultivationTechnology"
            placeholder="请输入栽培技术（培养基/接种/管理等）"
            type="textarea"
            line-number
            :autosize="{ minRows: 4, maxRows: 10 }"
          />
        </el-form-item>
      </re-col>
      <re-col>
        <el-form-item label="其它信息">
          <el-input
            v-model="newFormInline.otherDetail"
            placeholder="请输入其它信息"
            type="textarea"
            line-number
            :autosize="{ minRows: 4, maxRows: 10 }"
          />
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
