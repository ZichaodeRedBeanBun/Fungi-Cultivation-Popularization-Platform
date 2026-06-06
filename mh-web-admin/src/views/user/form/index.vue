<script setup lang="ts">
import { ref } from "vue";
import ReCol from "@/components/ReCol";
import { baseRules } from "../utils/rule"; // 导入基础规则
import { FormProps } from "../utils/types";
import { usePublicHooks } from "../hooks";
import type { FormItemRule } from "element-plus"; // 导入Element Plus类型
// src/views/user/form/index.vue
import { watch } from "vue";



const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    title: "新增",
    userId: 0,
    username: "",
    password: "",
    userType: 1,
    email: "",
    certificateNum: "", // 新增：科普认证号码
    affiliateUnit: "" ,  // 新增：科普单位
    introduction: "",
    userStatus: 0
  })
});

const ruleFormRef = ref();
const { switchStyle } = usePublicHooks();
const newFormInline = ref(props.formInline);
// 3. 定义用户身份下拉选项（对应后端UserTypeEnum）
const userTypeOptions = ref([
  { label: "普通用户", value: 1 },
  { label: "待认证科普号", value: 2 },
  { label: "已认证科普号", value: 3 }
]);
// ✅ 核心：合并基础规则 + 动态规则（组件内可直接访问newFormInline）
const formRules = ref<Record<string, FormItemRule[]>>({
  ...baseRules, // 继承基础规则（username、password、userType、email等）
  // 新增科普认证号码的动态验证规则
  certificateNum: [
    {
      validator: (rule: FormItemRule, value: string, callback: Function) => {
        const userType = newFormInline.value.userType; // 组件内可直接访问
        if ((userType === 2 || userType === 3) && !value) {
          callback(new Error("科普认证号码不能为空"));
        } else {
          callback(); // 验证通过必须调用callback
        }
      },
      trigger: "blur"
    }
  ],
  // 新增科普单位的动态验证规则
  affiliateUnit: [
    {
      validator: (rule: FormItemRule, value: string, callback: Function) => {
        const userType = newFormInline.value.userType;
        if ((userType === 2 || userType === 3) && !value) {
          callback(new Error("科普单位不能为空"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
});

// 监听userType变化，清空科普字段（可选）
watch(
  () => newFormInline.value.userType,
  (newVal) => {
    if (newVal !== 2 && newVal !== 3) {
      newFormInline.value.certificateNum = "";
      newFormInline.value.affiliateUnit = "";
    }
  },
  { immediate: true }
);

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
    label-width="82px"
  >
    <el-row :gutter="30">
      <re-col
        v-if="newFormInline.title === '修改'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="用户编号" prop="userId">
          <el-input
            v-model="newFormInline.userId"
            disabled
            placeholder="newFormInline.userId"
          />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户名" prop="username" required>
          <el-input
            v-model="newFormInline.username"
            clearable
            placeholder="请输入用户名"
          />
        </el-form-item>
      </re-col>

      <re-col
        v-if="newFormInline.title === '新增'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="用户密码" prop="password" required>
          <el-input
            v-model="newFormInline.password"
            clearable
            placeholder="请输入用户密码"
          />
        </el-form-item>
      </re-col>
      <!-- 2. 新增用户身份下拉框 -->
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户身份" prop="userType" required>
          <el-select
            v-model="newFormInline.userType"
            clearable
            placeholder="请选择用户身份"
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

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="邮箱" prop="email" required>
          <el-input
            v-model="newFormInline.email"
            clearable
            placeholder="请输入邮箱"
          />
        </el-form-item>
      </re-col>
     <!-- 新增：科普认证号码（仅用户身份为2/3时显示） -->
  <re-col v-if="newFormInline.userType === 2 || newFormInline.userType === 3" :value="12" :xs="24" :sm="24">
    <el-form-item label="认证号码" prop="certificateNum" required>
      <el-input v-model="newFormInline.certificateNum" clearable placeholder="请输入科普认证号码" />
    </el-form-item>
  </re-col>

  <!-- 新增：科普单位（仅用户身份为2/3时显示） -->
  <re-col v-if="newFormInline.userType === 2 || newFormInline.userType === 3" :value="12" :xs="24" :sm="24">
    <el-form-item label="科普单位" prop="affiliateUnit" required>
      <el-input v-model="newFormInline.affiliateUnit" clearable placeholder="请输入科普单位" />
    </el-form-item>
  </re-col> 
      <re-col>
        <el-form-item label="简介">
          <el-input
            v-model="newFormInline.introduction"
            placeholder="请输入简介"
            type="textarea"
          />
        </el-form-item>
      </re-col>

      <re-col
        v-if="newFormInline.title === '新增'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="用户状态">
          <el-switch
            v-model="newFormInline.userStatus"
            inline-prompt
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
            :style="switchStyle"
          />
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
