<template>

  <div class="home-container">
    <div class="home-card">
      <h2>管理后台</h2>
      <p class="welcome">管理员 {{ username }}，您好</p>
      <div class="menu">
        <div class="menu-item" v-on:click="goTo('/admin/users')">
          <span class="menu-icon">👥</span>
          <span>用户管理</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/admin/products')">
          <span class="menu-icon">📦</span>
          <span>商品管理</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/admin/orders')">
          <span class="menu-icon">📋</span>
          <span>订单查看</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/admin/statistics')">
          <span class="menu-icon">📊</span>
          <span>数据统计</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/admin/settings')">
          <span class="menu-icon">⚙️</span>
          <span>系统设置</span>
        </div>
        <div class="menu-item" v-on:click="goTo('/admin/logs')">
          <span class="menu-icon">📝</span>
          <span>系统日志</span>
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

/** 当前登录用户名 */
const username = ref<string>('')

onMounted(() => {
  const storedName = localStorage.getItem('username')
  if (storedName) {
    username.value = storedName
  }
})

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
