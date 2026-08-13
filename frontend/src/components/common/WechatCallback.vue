<template>
  <div class="wechat-callback">
    <p v-if="status === 'loading'">正在登录...</p>
    <p v-else-if="status === 'error'" class="error">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { post } from '../../utils/request'

const route = useRoute()
const router = useRouter()

const status = ref<'loading' | 'error'>('loading')
const error = ref('')

/**
 * 微信授权回调
 */
onMounted(async () => {
  const code = route.query.code as string
  const state = route.query.state as string
  const savedState = localStorage.getItem('wechat_state')
  localStorage.removeItem('wechat_state')

  if (!code || !state || state !== savedState) {
    status.value = 'error'
    error.value = '微信授权校验失败，请重新登录'
    return
  }

  try {
    const res = await post('/login/wechat/callback', { code })
    if (res.success) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.role)
      localStorage.setItem('username', res.data.username)
      const target = res.data.role === 'ADMIN' ? '/admin/home' : '/user/home'
      await router.replace(target)
    } else {
      status.value = 'error'
      error.value = res.message || '微信登录失败'
    }
  } catch (e) {
    status.value = 'error'
    error.value = e instanceof Error ? e.message : '网络错误，请稍后重试'
  }
})
</script>

<style scoped>
.wechat-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-size: 16px;
}
.error {
  color: #e53935;
}
</style>
