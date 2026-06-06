<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserStore } from '@/stores/modules/user'
import defaultAvatar from '@/assets/user.jpg'
import { updateUserInfo, getUserInfo, applyPopsciAuth } from '@/api/system'
import { useRouter } from 'vue-router'
import AuthTabs from '@/components/Auth/AuthTabs.vue'
import { UserInfo } from '@/stores/modules/user'

const router = useRouter()
const userStore = UserStore()
const loading = ref(false)
const userFormRef = ref<FormInstance>()
const authVisible = ref(false)

// 修改表单数据结构，包含科普号认证信息
const userForm = reactive({
  userId: userStore.userInfo.userId,
  username: userStore.userInfo.username || '',
  phone: userStore.userInfo.phone || '',
  email: userStore.userInfo.email || '',
  introduction: userStore.userInfo.introduction || '',
  userType: userStore.userInfo.userType || 1,
  // 科普号认证字段 - 使用空字符串作为默认值
  certificateNum: userStore.userInfo.certificateNum || '',
  affiliateUnit: userStore.userInfo.affiliateUnit || ''
})

// 表单验证规则
const userRules = reactive<FormRules>({
  // 科普号认证字段验证 - 保持必填，因为这是认证申请
  certificateNum: [
    { required: true, message: '请输入认证号码', trigger: 'blur' },
    { min: 5, max: 20, message: '认证号码长度在5到20个字符', trigger: 'blur' }
  ],
  affiliateUnit: [
    { required: true, message: '请输入所属单位', trigger: 'blur' },
    { min: 2, max: 50, message: '所属单位长度在2到50个字符', trigger: 'blur' }
  ]
})

// 检查登录状态并获取最新用户信息
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    authVisible.value = true
  } else {
    try {
      const response = await getUserInfo()
      if (response.code === 0) {
        const userData = response.data as UserInfo // Type assertion
        userStore.setUserInfo(userData, userStore.userInfo.token)
        userForm.userId = userData.userId
        userForm.username = userData.username || ''
        userForm.phone = userData.phone || ''
        userForm.email = userData.email || ''
        userForm.userType = userData.userType||1
        userForm.introduction = userData.introduction || ''
        userForm.certificateNum = userData.certificateNum || ''
        userForm.affiliateUnit = userData.affiliateUnit || ''
        console.log('用户表单数据:', userForm)
        console.log('用户信息完整数据:', userData)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})

// 判断是否可以编辑科普号认证信息
const canEditPopsciAuth = computed(() => {
  return userForm.userType !== 2 && userForm.userType !== 3
})

// 获取提交按钮的文本
const submitButtonText = computed(() => {
  if (userForm.userType === 2) {
    return '审核中'
  } else if (userForm.userType === 3) {
    return '认证成功'
  } else {
    return '提交认证'
  }
})

// 处理表单提交 - 修改为提交科普号认证
const handleSubmit = async () => {
  if (!canEditPopsciAuth.value) return // 如果不能编辑，则不处理提交
  
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      // 打印提交的表单数据到控制台
      console.log('提交的科普号认证表单数据:', {
        certificateNum: userForm.certificateNum,
        affiliateUnit: userForm.affiliateUnit,
        introduction: userForm.introduction
      })
      loading.value = true
      try {
          // 提交科普号认证申请
          const authResponse = await applyPopsciAuth({
            certificateNum: userForm.certificateNum,
            affiliateUnit: userForm.affiliateUnit,
            introduction: userForm.introduction
          })
          
          if (authResponse.code === 0) {
            // 重新获取用户信息
            const userInfoResponse = await getUserInfo()
            if (userInfoResponse.code === 0) {
              userStore.setUserInfo(userInfoResponse.data, userStore.userInfo.token)
              ElMessage.success('科普号认证申请已提交，请等待审核')
            } else {
              ElMessage.error(userInfoResponse.message || '获取用户信息失败')
            }
          } else {
            ElMessage.error(authResponse.message || '认证申请提交失败')
          }
        
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        loading.value = false
      }
    }
  })
}

</script>

<template>
  <div class="user-container">
    <h2 class="username">科普号认证</h2>

    <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="0" size="large" class="user-form">
      <!-- 头像显示区域 -->
      <div class="section">
        <div class="section-title">头像</div>
        <div class="avatar-preview">
          <el-avatar 
            :src="userStore.userInfo.userAvatar || defaultAvatar" 
            :size="120" 
            shape="circle"
          />
        </div>
      </div>

      <div class="section">
        <div class="section-title">用户名</div>
        <el-form-item prop="username">
          <el-input v-model="userForm.username" placeholder="用户名" disabled />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">邮箱</div>
        <el-form-item prop="email">
          <el-input v-model="userForm.email" placeholder="邮箱" disabled />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">联系电话</div>
        <el-form-item prop="phone">
          <el-input v-model="userForm.phone" placeholder="联系电话" disabled />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">简介</div>
        <el-form-item prop="introduction">
          <el-input v-model="userForm.introduction" type="textarea" :rows="4" placeholder="编辑个人简介" maxlength="100"
            show-word-limit disabled />
        </el-form-item>
      </div>

      <!-- 科普号认证信息 -->
      <div class="section">
        <div class="section-title">认证号码</div>
        <el-form-item prop="certificateNum">
          <el-input 
            v-model="userForm.certificateNum" 
            placeholder="请输入认证号码" 
            :disabled="!canEditPopsciAuth"
          />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">所属单位</div>
        <el-form-item prop="affiliateUnit">
          <el-input 
            v-model="userForm.affiliateUnit" 
            placeholder="请输入所属单位" 
            :disabled="!canEditPopsciAuth"
          />
        </el-form-item>
      </div>

      <el-form-item class="button-group">
        <div class="flex justify-end w-full">
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleSubmit" 
            class="submit-btn"
            :disabled="!canEditPopsciAuth"
          >
            {{ submitButtonText }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 登录对话框 -->
    <AuthTabs v-model="authVisible" />
  </div>
</template>

<style scoped>
.user-container {
  max-width: 1200px;
  margin: 80px auto;
  padding: 30px 40px 15px;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.username {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: var(--el-text-color-primary);
  font-weight: normal;
}

.user-form {
  max-width: 100%;
  margin: 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.submit-btn {
  border-radius: 8px;
  width: 140px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color-light) inset !important;
  cursor: not-allowed;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.avatar-preview {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 8px 0;
}

.button-group {
  margin-top: 40px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>