<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <p v-if="error" class="error">{{ error }}</p>
    <form @submit.prevent="handleLogin">
      <div class="box">
        <span>用户名或用户编号：</span>
        <input v-model="username" type="text" placeholder="请输入用户名或用户编号" required />
      </div>
      <div class="box">
        <span>密码：</span>
        <input v-model="password" type="password" placeholder="请输入密码" required />
      </div>
      <button type="submit">登录</button>
      <div class="link-box">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { post } from '../../utils/request'

const route = useRoute()
const router = useRouter()
const username = ref(route.query.username as string || '')
const password = ref('')
const error = ref('')

/**
 * 处理登录：调用 /login 接口，成功后保存 token/role/username 并按角色跳转
 */
const handleLogin = async () => {
  error.value = ''
  try {
    const res = await post('/login', {
      username: username.value,
      password: password.value,
    })

    if (res.success) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.role)
      localStorage.setItem('username', res.data.username)
      const target = res.data.role === 'ADMIN' ? '/admin/home' : '/user/home'
      await router.push(target)
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误，请稍后重试'
  }
}
</script>
