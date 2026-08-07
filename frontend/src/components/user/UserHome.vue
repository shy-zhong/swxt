<template>
  
  <div class="home-container">
    <div class="home-card">
      <h2>用户主页</h2>
      <p class="welcome">欢迎回来，{{ username }}</p>
      <div class="menu">
        <div class="menu-item" @click="goTo('/user/shop')">
          <span class="icon">🛍️</span>
          <span>商品商城</span>
        </div>
        <div class="menu-item" @click="goTo('/user/cart')">
          <span class="icon">🛒</span>
          <span>我的购物车</span>
        </div>
        <div class="menu-item" @click="goTo('/user/myOrders')">
          <span class="icon">📦</span>
          <span>我的订单</span>
        </div>
        <div class="menu-item">
          <span class="icon">👤</span>
          <span>个人信息</span>
        </div>
      </div>
      <button class="exit-btn" @click="exit">退出登录</button>
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
 * 退出登录
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

/**
 * 菜单点击：跳转到指定页面
 */
function goTo(path: string) {
  router.push(path)
}
</script>
