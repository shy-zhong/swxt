<template>
  
  <div class="login-container">
    <h2>用户注册</h2>
    <p v-if="error" class="error">{{ error }}</p>
    <form @submit.prevent="handleRegister">
      <div class="form-field">
        <span>用户名或用户编号：</span>
        <input v-model="username" type="text" placeholder="请输入用户名或用户编号" required />
      </div>
      <div class="form-field">
        <span>密码：</span>
        <input v-model="password" type="password" placeholder="请输入密码" required />
      </div>
      <div class="form-field">
        <div class="role-options">
            身份:
          <label>
            <input v-model="role" type="radio" value="USER" />
            普通用户
          </label>
          <label>
            <input v-model="role" type="radio" value="OPERATOR" />
            操作员
          </label>
          <label>
            <input v-model="role" type="radio" value="ADMIN" />
            管理员
          </label>
        </div>
      </div>
      <button type="submit">注册</button>
      <div class="link-box">
        <router-link to="/login">返回登录</router-link>
      </div>
    </form>
  </div>
</template>
<script setup lang="ts">


import {ref} from 'vue'
import { post } from '../../utils/request.ts'
import router from '../../route/Router';

type Role = "ADMIN" | "OPERATOR" | "USER";

const error = ref("");
const username = ref("")
const password = ref("");
const role = ref<Role>("USER")

/**
 * 处理注册：调用 /register，成功后携带用户名跳转登录页
 */
const handleRegister = async() =>{
    error.value = ""
    try {
        const res = await post("/register",{
            username: username.value,
            password: password.value,
            role: role.value
        })
        if (res.success) {
            router.push({ path: '/login', query: { username: res.data.username } })
        } else {
            error.value = res.message || '注册失败'
        }
    } catch(e) {
        error.value = e instanceof Error ? e.message : '网络错误，请稍后重试'
    }
}
</script>
