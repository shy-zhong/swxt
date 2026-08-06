<template>

  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <button class="back-btn" @click="router.push('/admin/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="isCreating" class="edit-panel">
      <h3>新增用户</h3>
      <div class="form-row">
        <label>用户名</label>
        <input v-model="createForm.username" type="text" />
      </div>
      <div class="form-row">
        <label>密码</label>
        <input v-model="createForm.password" type="password" />
      </div>
      <div class="form-row">
        <label>真实姓名</label>
        <input v-model="createForm.realName" type="text" />
      </div>
      <div class="form-row">
        <label>手机号</label>
        <input v-model="createForm.phone" type="text" />
      </div>
      <div class="form-row">
        <label>邮箱</label>
        <input v-model="createForm.email" type="text" />
      </div>
      <div class="form-row">
        <label>身份</label>
        <div class="radio-group">
          <label>
            <input v-model="createForm.role" type="radio" value="USER" />
            普通用户
          </label>
          <label>
            <input v-model="createForm.role" type="radio" value="OPERATOR" />
            操作员
          </label>
          <label>
            <input v-model="createForm.role" type="radio" value="ADMIN" />
            管理员
          </label>
        </div>
      </div>
      <div class="form-actions">
        <button @click="createNewUser">确认新增</button>
        <button class="cancel-btn" @click="cancelCreate">取消</button>
      </div>
    </div>

    <div v-else-if="editing" class="edit-panel">
      <h3>编辑用户</h3>
      <div class="form-row">
        <label>用户名</label>
        <input v-model="editForm.username" type="text" />
      </div>
      <div class="form-row">
        <label>真实姓名</label>
        <input v-model="editForm.realName" type="text" />
      </div>
      <div class="form-row">
        <label>手机号</label>
        <input v-model="editForm.phone" type="text" />
      </div>
      <div class="form-row">
        <label>邮箱</label>
        <input v-model="editForm.email" type="text" />
      </div>
      <div class="form-row">
        <label>角色</label>
        <select v-model="editForm.role">
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
        </select>
      </div>
      <div class="form-actions">
        <button @click="saveEdit">保存</button>
        <button class="cancel-btn" @click="cancelEdit">取消</button>
      </div>
    </div>

    <SearchToolbar v-else v-model="searchKeyword" placeholder="请输入搜索内容..." @search="handleSearch">
      <template #prepend>
        <select v-model="searchMethod">
          <option value="">全部</option>
          <option value="id">编号</option>
          <option value="username">用户名</option>
          <option value="realName">真实姓名</option>
          <option value="phone">手机号</option>
          <option value="email">邮箱</option>
          <option value="role">角色</option>
        </select>
      </template>
      <template #actions>
        <button class="toolbar-btn" @click="openCreate">新增用户</button>
      </template>
    </SearchToolbar>

    <div class="table-wrap" v-if="!isCreating && !editing">
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>真实姓名</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>操作</th>
          </tr>
        </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.realName || '-' }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>{{ user.email || '-' }}</td>
              <td>{{ user.role }}</td>
              <td>
                <button class="action-btn" @click="editUser(user)">编辑</button>
                <button class="action-btn delete" @click="deleteUser(user)">删除</button>
              </td>
            </tr>
          </tbody>
      </table>
    </div>

    <Pagination v-if="!isCreating && !editing" :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />
  </div>
</template>

<script setup lang="ts">



import { ref, onMounted } from 'vue'
import { get, post, put, del } from '../../utils/request'
import router from '../../route/Router'
import { getConfig } from '../../utils/configStore'
import Pagination from '../common/Pagination.vue'
import SearchToolbar from '../common/SearchToolbar.vue'

interface User {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  role: string
  wechatOpenid?: string
  status?: number
}

const users = ref<User[]>([])
const error = ref('')
const searchKeyword = ref('')
const searchMethod = ref('')
const editing = ref(false)
const editForm = ref<User>({ id: 0, username: '', realName: '', phone: '', email: '', role: 'USER', wechatOpenid: '', status: 1 })
const isCreating = ref<boolean>(false)
const createForm = ref<{ username: string; password: string; realName: string; phone: string; email: string; role: string }>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'USER',
})

const page = ref(1)
const total = ref(0)
const totalPages = ref(0)
const isSearching = ref(false)

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

/**
 * 打开新增用户表单并重置默认值
 */
function openCreate() {
  createForm.value = { username: '', password: '', realName: '', phone: '', email: '', role: 'USER' }
  error.value = ''
  isCreating.value = true
}

/**
 * 取消新增，关闭表单
 */
function cancelCreate() {
  isCreating.value = false
  error.value = ''
}

/**
 * 按当前搜索状态加载数据：搜索中调 searchUsers，否则调 loadUsers
 */
async function loadData() {
  if (isSearching.value) {
    await searchUsers()
  } else {
    await loadUsers()
  }
}

/**
 * 按搜索条件调用 /users/search 接口查询用户并刷新列表
 */
async function searchUsers() {
  error.value = ''
  try {
    const res = await get<PageResult<User>>(`/users/search?key=${searchMethod.value}&value=${searchKeyword.value}&page=${page.value}&size=${getConfig('page_size')}`)
    if (res.success) {
      users.value = res.data.list
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      page.value = res.data.page
    } else {
      error.value = res.message || '搜索失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '错误'
  }
}

/**
 * 设置搜索状态并重置到第一页后加载数据
 */
async function handleSearch() {
  page.value = 1
  isSearching.value = searchMethod.value !== ''
  await loadData()
}

onMounted(async () => {
  await loadUsers()
})

/**
 * 加载用户分页列表
 */
async function loadUsers() {
  console.log(getConfig('page_size'))
  error.value = ''
  try {
    const res = await get<PageResult<User>>(`/users?page=${page.value}&size=${getConfig('page_size')}`)
    if (res.success) {
      users.value = res.data.list
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      page.value = res.data.page
    } else {
      error.value = res.message || '加载用户列表失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '错误'
  }
}

/**
 * 跳转到指定页码并重新加载
 */
function goToPage(target: number) {
  if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
  page.value = target
  loadData()
}

/**
 * 打开编辑表单并回填用户信息
 */
function editUser(user: User) {
  editForm.value = { ...user }
  editing.value = true
}

/**
 * 取消编辑，关闭表单
 */
function cancelEdit() {
  editing.value = false
  error.value = ''
}

/**
 * 新增用户：调用注册接口创建
 */
async function createNewUser() {
  error.value = ''
  try {
    const payload = {
      username: createForm.value.username,
      password: createForm.value.password,
      realName: createForm.value.realName,
      phone: createForm.value.phone,
      email: createForm.value.email,
      role: createForm.value.role,
    }
    const res = await post('/register?usermanager=admin', payload)
    if (res.success) {
      isCreating.value = false
      await loadData()
    } else {
      error.value = res.message || '新增失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

/**
 * 保存用户编辑（含角色权限修改）
 */
async function saveEdit() {
  error.value = ''
  try {
    const payload = {
      id: editForm.value.id,
      username: editForm.value.username,
      realName: editForm.value.realName,
      phone: editForm.value.phone,
      email: editForm.value.email,
      role: editForm.value.role,
      wechatOpenid: editForm.value.wechatOpenid,
      status: editForm.value.status,
    }
    const res = await put('/users', payload)
    if (res.success) {
      editing.value = false
      await loadData()
    } else {
      error.value = res.message || '更新失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

/**
 * 删除用户:删除当前页最后一条时自动回退页码
 */
async function deleteUser(user: User) {
  if (!confirm(`确定删除用户 ${user.username} 吗？`)) return
  error.value = ''
  try {
    const res = await del<{ code: number; message: string }>(`/users/${user.id}`)
    if (res.success) {
      if (users.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      await loadData()
    } else {
      error.value = res.message || '删除失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}
</script>
