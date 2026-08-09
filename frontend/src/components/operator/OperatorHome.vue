<template>

  <div class="home-container">
    <div class="home-card">
      <h2>操作员中心</h2>
      <p class="welcome">操作员 {{ username }}，您好</p>
      <div class="menu">
        <div class="menu-item" v-on:click="goTo('/operator/stockManager')">
          <span class="menu-icon">📦</span>
          <span>出入库管理</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/operator/orders')">
          <span class="menu-icon">📋</span>
          <span>订单管理</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/operator/stockLog')">
          <span class="menu-icon">📦</span>
          <span>出入库记录</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/operator/settings')">
          <span class="menu-icon">⚙️</span>
          <span>系统设置</span>
        </div>
      </div>
      <button class="exit-btn" v-on:click="exit">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">


import { ref, onMounted } from 'vue'
import router from '../../route/Router'
import { get } from '../../utils/request'
const username = ref<string>('')

onMounted(() => {
  const storedName = localStorage.getItem('username')
  if (storedName) {
    username.value = storedName
  }
})

/**
 * 退出登录：通知后端登出并清除本地 token/role/username，跳转登录页
 */
async function exit() {
  try {
    await get('/login-out');
  } catch (e) {
    console.error(e)
  }
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/login')
}

function goTo(path: string) {
  router.push(path)
}
</script>