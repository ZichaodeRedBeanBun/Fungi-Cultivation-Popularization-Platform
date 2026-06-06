<script setup lang="ts">
import { ref, computed } from "vue";
import ReCol from "@/components/ReCol";
import type { DiseasePestFormProps, DiseasePestFormItemProps,MushroomInfo } from "../utils/types";
import { formRules } from "../utils/rule";
import { useDiseasePest, } from "../utils/hook";

const tableRef = ref();
const {
  strainOptions,
  filterStrain, // 新增：全量菌种搜索方法（从hook导入）
  handleStrainClear // 新增：清空选择器逻辑（从hook导入）
} = useDiseasePest(tableRef);
const props = withDefaults(defineProps<DiseasePestFormProps>(), {
  formInline: () => ({
    formTitle: "新增",
    id: 0,
    diseaseName: "",
    brief: "",
    mushroomList:[],
    mushroomIds: [] as number[], // 用于绑定多选下拉框的选中菌种id数组
    symptom: "",
    pathogen: "",
    infection: "",
    occurrenceRule: "",
    prevention: "",
    itemType: "",
    createTime: "",
    updateTime: "",
    morphology: "",
    habits: ""
  })
});
const itemTypeOptions = [
  { label: "病害", value: "disease" },
  { label: "虫害", value: "pest" }
];

const newFormInline = ref({ ...props.formInline });
const ruleFormRef = ref();

function getRef() {
  return ruleFormRef.value;
}

function getFormData() {
  return JSON.parse(JSON.stringify(newFormInline.value));
}

defineExpose({ getRef, getFormData });

import { watch } from "vue";

watch(
  () => props.formInline,
  (newVal) => {
    Object.assign(newFormInline.value, newVal);
  },
  { immediate: true, deep: true }
);
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="formRules"
    label-width="100px"
  >
    <el-row :gutter="20">
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="病虫害名称" prop="diseaseName" required>
          <el-input v-model="newFormInline.diseaseName" placeholder="请输入病虫害名称" />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="类型" prop="itemType" required>
          <el-select v-model="newFormInline.itemType" placeholder="请选择类型">
            <el-option
              v-for="option in itemTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="关联菌种" prop="mushroomIds">
          <el-select
            v-model="newFormInline.mushroomIds"
            multiple
            filterable
            clearable
            placeholder="请选择关联菌种"
            :options="strainOptions"
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
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="简介" prop="brief">
          <el-input
            v-model="newFormInline.brief"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入简介"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="为害症状" prop="symptom" >
          <el-input
            v-model="newFormInline.symptom"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入为害症状"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="病原" prop="pathogen" >
          <el-input
            v-model="newFormInline.pathogen"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入病原"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="侵染" prop="infection">
          <el-input
            v-model="newFormInline.infection"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入侵染"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="发生规律" prop="occurrenceRule">
          <el-input
            v-model="newFormInline.occurrenceRule"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入发生规律"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="防治" prop="prevention" >
          <el-input
            v-model="newFormInline.prevention"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入防治措施"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="形态" prop="morphology">
          <el-input
            v-model="newFormInline.morphology"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入形态"
          />
        </el-form-item>
      </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="习性" prop="habits">
          <el-input
            v-model="newFormInline.habits"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入习性"
          />
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
