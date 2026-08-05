<template>

  <div class="log-page">
    <div class="page-header">
      <h2>系统日志</h2>
      <button class="back-btn" @click="router.push('/admin/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div class="filter-bar">
      <div class="filter-item">
        <label>用户名</label>
        <input v-model="filterUsername" type="text" placeholder="按用户名筛选" @keyup.enter="applyFilter" />
      </div>
      <div class="filter-item">
        <label>操作类型</label>
        <select v-model="filterActionType">
          <option value="">全部</option>
          <option v-for="t in actionTypeOptions" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
      </div>
      <div class="filter-item">
        <label>操作对象</label>
        <select v-model="filterTargetType">
          <option value="">全部</option>
          <option v-for="t in targetTypeOptions" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
      </div>
      <div class="filter-item">
        <label>结果</label>
        <select v-model="filterResult">
          <option value="">全部</option>
          <option v-for="r in resultOptions" :key="r.value" :value="r.value">{{ r.label }}</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="filter-btn" @click="applyFilter">查询</button>
        <button class="filter-btn reset-btn" @click="resetFilter">重置</button>
        <button class="filter-btn clear-btn" @click="logClear">清空日志</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="log-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>操作类型</th>
          <th>操作对象</th>
          <th>对象ID</th>
          <th>结果</th>
          <th>IP 地址</th>
          <th>操作时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="log in logs" :key="log.id">
          <td>{{ log.id }}</td>
          <td>{{ log.username || '-' }}</td>
          <td>
            <span class="log-tag">{{ actionTypeLabel(log.actionType) }}</span>
          </td>
          <td>
            <span class="log-tag">{{ targetTypeLabel(log.targetType) }}</span>
          </td>
          <td>{{ log.targetId || '-' }}</td>
          <td>
            <span class="status-tag" :class="log.result === 'SUCCESS' ? 'tag-on' : 'tag-off'">
              {{ log.result === 'SUCCESS' ? '成功' : '失败' }}
            </span>
          </td>
          <td>{{ log.ip || '-' }}</td>
          <td>{{ log.createdAt }}</td>
        </tr>
        <tr v-if="logs.length === 0">
          <td colspan="8" class="empty-tip">暂无匹配的日志记录</td>
        </tr>
      </tbody>
      </table>
    </div>

    <Pagination :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />
  </div>
</template>

<script setup lang="ts">



import { ref, onMounted } from 'vue'
import { get, del } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import Pagination from '../common/Pagination.vue'

interface SystemLog {
  id: number
  username: string
  userId: number | null
  actionType: string
  targetType: string
  targetId: number | null
  result: string
  ip: string
  createdAt: string
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

const actionTypeOptions: { value: string; label: string }[] = [
  { value: 'CREATE', label: '新增' },
  { value: 'UPDATE', label: '修改' },
  { value: 'DELETE', label: '删除' },
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'REGISTER', label:'注册'}
]

const targetTypeOptions: { value: string; label: string }[] = [
  { value: 'USER', label: '用户' },
  { value: 'PRODUCT', label: '商品' },
  { value: 'CATEGORY', label: '分类' },
  { value: 'STOCK', label: '库存' },
  { value: 'CONFIG', label: '配置' },
]

const resultOptions: { value: string; label: string }[] = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAIL', label: '失败' },
]

const actionLabelMap = new Map(actionTypeOptions.map((t) => [t.value, t.label]))
const targetLabelMap = new Map(targetTypeOptions.map((t) => [t.value, t.label]))

const logs = ref<SystemLog[]>([])
const error = ref('')
const filterUsername = ref('')
const filterActionType = ref('')
const filterTargetType = ref('')
const filterResult = ref('')

const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)

/**
 * 将操作类型枚举转换为展示名称
 */
function actionTypeLabel(t: string): string {
  return actionLabelMap.get(t) || t || '-'
}

/**
 * 将操作对象枚举转换为展示名称
 */
function targetTypeLabel(t: string): string {
  return targetLabelMap.get(t) || t || '-'
}

/**
 * 加载日志列表：带当前筛选条件与分页参数请求后端
 */
async function loadLogs() {
  error.value = ''
  try {
    const params = [
      `page=${page.value}`,
      `size=${size.value}`,
    ]
    if (filterUsername.value) params.push(`username=${encodeURIComponent(filterUsername.value)}`)
    if (filterActionType.value) params.push(`actionType=${filterActionType.value}`)
    if (filterTargetType.value) params.push(`targetType=${filterTargetType.value}`)
    if (filterResult.value) params.push(`result=${filterResult.value}`)

    const res = await get<PageResult<SystemLog>>(`/system-logs?${params.join('&')}`)
    if (res.success && res.data) {
      logs.value = res.data.list || []
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      page.value = res.data.page
    } else {
      error.value = res.message || '加载日志失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

/**
 * 应用筛选条件并重置到第一页
 */
function applyFilter() {
  page.value = 1
  loadLogs()
}

/**
 * 重置全部筛选条件并回到第一页
 */
function resetFilter() {
  filterUsername.value = ''
  filterActionType.value = ''
  filterTargetType.value = ''
  filterResult.value = ''
  page.value = 1
  loadLogs()
}

/**
 * 跳转到指定页码并重新加载
 */
function goToPage(target: number) {
  if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
  page.value = target
  loadLogs()
}

onMounted(async () => {
  await loadLogs()
})

/**
 * 清空日志
 */
async function logClear() {
  if (!confirm('确认清空所有日志?')) return
  try {
    const res = await del('/system-logs')
    if (res.success) {
      page.value = 1
      await loadLogs()
    } else {
      error.value = res.message || '清空失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}
</script>
