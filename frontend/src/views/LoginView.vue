<template>
  <div class="login-view">
    <el-card class="login-card">
      <template #header>
        <h2>辽港伐谋 KM 平台登录</h2>
      </template>
      <el-form @submit.prevent="handleLogin">
        <el-alert
          v-if="mockMode"
          title="Sprint 1 mock 模式"
          type="info"
          :closable="false"
          description="Q-I2 辽港慧应用 APIKEY + 招商云 PAAS 订阅地址未提供。当前以 mock 角色登录。"
        />
        <el-form-item label="角色">
          <el-select v-model="selectedRole" placeholder="选择登录角色">
            <el-option label="系统管理员 ROLE-0001" value="ROLE-0001" />
            <el-option label="合规审核员 ROLE-0002" value="ROLE-0002" />
            <el-option label="算法工程师 ROLE-0003" value="ROLE-0003" />
            <el-option label="业务专家 ROLE-0004" value="ROLE-0004" />
            <el-option label="只读观察者 ROLE-0005" value="ROLE-0005" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :disabled="!selectedRole">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const selectedRole = ref<string>('ROLE-0001')
const mockMode = ref<boolean>(true) // Sprint 1 mock 模式

function handleLogin() {
  if (!selectedRole.value) return
  auth.mockLoginAs(selectedRole.value)
  router.push('/')
}
</script>

<style lang="scss" scoped>
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: var(--bg-canvas);
}
.login-card {
  width: 420px;
}
</style>
