<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <p v-if="error" class="error">{{ error }}</p>
    <form @submit.prevent="handleLogin">
      <div class="form-field">
        <span>用户名或用户编号：</span>
        <input v-model="username" type="text" placeholder="请输入用户名或用户编号" required />
      </div>
      <div class="form-field">
        <span>密码：</span>
        <input v-model="password" type="password" placeholder="请输入密码" required />
      </div>
      <button type="submit">登录</button>
      <br><br>
      <div class="wechat-login">
        <button type="button" class="wechat-btn" @click="handleWechatLogin">微信登录</button>
      </div>
      <div v-if="qrSceneId" class="qr-area">
        <img :src="qrImageUrl" alt="扫码登录" />
        <p v-if="qrStatus === 'waiting'">请用手机微信扫码</p>
        <p v-else-if="qrStatus === 'expired'" class="error">二维码已过期，请刷新重试</p>
        <button v-if="qrStatus === 'expired'" type="button" @click="startQrLogin">刷新二维码</button>
      </div>
      <div class="link-box">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post } from '../../utils/request'

const route = useRoute()
const router = useRouter()

const username = ref(route.query.username as string || '')
const password = ref('')
const error = ref('')

/** 二维码登录状态 */
const qrSceneId = ref('')
const qrImageUrl = ref('')
const qrStatus = ref<'waiting' | 'expired'>('waiting')
let qrTimer: number | undefined

/**
 * 生成二维码登录会话并开始轮询
 */
const startQrLogin = async () => {
  stopQrPolling()
  error.value = ''
  qrStatus.value = 'waiting'
  try {
    const res = await post('/login/wechat/qr/create')
    if (!res.success || !res.data?.sceneId) {
      error.value = res.message || '生成二维码失败'
      return
    }
    qrSceneId.value = res.data.sceneId
    qrImageUrl.value = `data:image/png;base64,${res.data.qrBase64}`
    qrTimer = window.setInterval(pollQrStatus, 2000)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误，请稍后重试'
  }
}

const pollQrStatus = async () => {
  if (!qrSceneId.value) return
  try {
    const res = await get<{ status: string; token?: string; username?: string; role?: string }>(
      `/login/wechat/qr/status?sceneId=${qrSceneId.value}`
    )
    if (!res.success) return
    if (res.data.status === 'success' && res.data.token) {
      stopQrPolling()
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.role || 'USER')
      localStorage.setItem('username', res.data.username || '')
      const target = res.data.role === 'ADMIN' ? '/admin/home' : '/user/home'
      await router.push(target)
    } else if (res.data.status === 'invalid') {
      stopQrPolling()
      qrStatus.value = 'expired'
    }
  } catch {

  }
}

/**
 * 停止轮询
 */
const stopQrPolling = () => {
  if (qrTimer) {
    window.clearInterval(qrTimer)
    qrTimer = undefined
  }
}

onUnmounted(stopQrPolling)

const handleWechatLogin = () => {
  const isWechat = /MicroMessenger/i.test(navigator.userAgent)
  if (!isWechat) {
    startQrLogin()
    return
  }
  const appid = (import.meta.env.VITE_WECHAT_APPID as string) || ''
  const redirectUri = encodeURIComponent(
    `${location.origin}/wechat/callback`
  )
  const state = Math.random().toString(36).slice(2)
  localStorage.setItem('wechat_state', state)
  const authUrl =
    'https://open.weixin.qq.com/connect/oauth2/authorize' +
    `?appid=${appid}` +
    `&redirect_uri=${redirectUri}` +
    '&response_type=code' +
    '&scope=snsapi_base' +
    `&state=${state}` +
    '#wechat_redirect'
  location.href = authUrl
}

/**
 * 处理登录
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
      let target = res.data.role === 'ADMIN' ? '/admin/home' : '/user/home'
      target = res.data.role === 'OPERATOR' ? '/operator/home' : '/user/home'
      await router.push(target)
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误，请稍后重试'
  }
}
</script>

<style scoped>
.qr-area {
  margin-top: 12px;
  text-align: center;
}
.qr-area img {
  width: 180px;
  height: 180px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.qr-area p {
  margin: 8px 0;
  font-size: 13px;
  color: #666;
}
.wechat-login {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.wechat-btn {
  padding: 6px 14px;
}
</style>
