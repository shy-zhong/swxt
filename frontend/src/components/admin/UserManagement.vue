<template>

  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <button class="back-btn" @click="router.push('/admin/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <ModalPanel :visible="isCreating" title="新增用户" @close="cancelCreate">
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
      <template #actions>
        <button @click="createNewUser">确认新增</button>
        <button class="cancel-btn" @click="cancelCreate">取消</button>
      </template>
    </ModalPanel>

    <ModalPanel :visible="editing" title="编辑用户" @close="cancelEdit">
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
      <template #actions>
        <button @click="saveEdit">保存</button>
        <button class="cancel-btn" @click="cancelEdit">取消</button>
      </template>
    </ModalPanel>

    <SearchToolbar v-model="searchKeyword" placeholder="请输入搜索内容..." @search="handleSearch">
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

    <div class="table-wrap">
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

    <Pagination :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post, put, del } from '../../utils/request'
import router from '../../route/Router'
import { getConfig } from '../../utils/configStore'
import Pagination from '../common/Pagination.vue'
import SearchToolbar from '../common/SearchToolbar.vue'
import ModalPanel from '../common/ModalPanel.vue'

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

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

/** 列表数据 */
const users = ref<User[]>([])
const error = ref('')
const searchKeyword = ref('')
const searchMethod = ref('')

const createForm = ref<{ username: string; password: string; realName: string; phone: string; email: string; role: string }>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'USER',
})
const editForm = ref<User>({ id: 0, username: '', realName: '', phone: '', email: '', role: 'USER', wechatOpenid: '', status: 1 })
const isCreating = ref<boolean>(false)
const editing = ref(false)

const page = ref(1)
const total = ref(0)
const totalPages = ref(0)
const isSearching = ref(false)

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

async function loadData() {
  if (isSearching.value) {
    await searchUsers()
  } else {
    await loadUsers()
  }
}

async function handleSearch() {
  page.value = 1
  isSearching.value = searchMethod.value !== ''
  await loadData()
}

function goToPage(target: number) {
  if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
  page.value = target
  loadData()
}

function openCreate() {
  createForm.value = { username: '', password: '', realName: '', phone: '', email: '', role: 'USER' }
  error.value = ''
  editing.value = false
  isCreating.value = true
}

function cancelCreate() {
  isCreating.value = false
  error.value = ''
}

function editUser(user: User) {
  editForm.value = { ...user }
  isCreating.value = false
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  error.value = ''
}

async function createNewUser() {
  error.value = ''
  try {
    const res = await post('/register?usermanager=admin', {...createForm.value})
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

async function saveEdit() {
  error.value = ''
  try {
    const res = await put('/users', {...editForm.value})
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

onMounted(async () => {
  await loadUsers()
})
</script>
