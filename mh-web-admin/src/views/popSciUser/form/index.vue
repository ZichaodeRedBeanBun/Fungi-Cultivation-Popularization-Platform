<script setup lang="ts">
import { ref } from "vue";
import ReCol from "@/components/ReCol";
import { formRules } from "../utils/rule";
import { FormProps } from "../utils/types";

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    title: "新增",
    userId: 0,
    username: "",
    userType: null,
    certificateNum: "",  // 新增
    affiliateUnit: ""
  })
});



const userTypeOptions = [
  { value: "PENDING_POPSCI", label: "待审核" },
  { value: "AUTHED_POPSCI", label: "审核通过" }
];


const ruleFormRef = ref();
const newFormInline = ref(props.formInline);

function getRef() {
  return ruleFormRef.value;
}

defineExpose({ getRef });
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="formRules"
    label-width="100px"
  >
    <el-row :gutter="30">
      <!-- 编辑时显示ID -->
      <re-col
        v-if="newFormInline.title === '修改'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="科普号ID" prop="userId">
          <el-input
            v-model="newFormInline.userId"
            disabled
            placeholder="科普号ID"
          />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="科普号名称" prop="username" required>
          <el-input
            v-model="newFormInline.username"
            clearable
            placeholder="请输入科普号名称"
          />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="科普号类型" prop="userType" required>
          <el-select
              v-model="newFormInline.userType"
              placeholder="请选择科普号类型"
              class="w-full"
              clearable
            >
              <el-option
                v-for="item in userTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>

        </el-form-item>
      </re-col>
        <re-col :value="24" :xs="24" :sm="24">
          <el-form-item label="认证号码" prop="certificateNum">
            <el-input
              v-model="newFormInline.certificateNum"
              clearable
              placeholder="请输入科普号认证号码"
            />
          </el-form-item>
        </re-col>

      <re-col :value="24" :xs="24" :sm="24">
        <el-form-item label="所属单位" prop="affiliateUnit">
          <el-input
            v-model="newFormInline.affiliateUnit"
            clearable
            placeholder="请输入所属单位"
          />
        </el-form-item>
      </re-col>

    </el-row>
  </el-form>
</template>
