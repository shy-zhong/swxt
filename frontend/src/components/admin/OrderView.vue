<template>

  <div class="product-management">
    <div class="page-header">
      <h2>订单查看</h2>
      <button class="back-btn" @click="router.push('/admin/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <SearchToolbar v-model="searchKeyword" placeholder="搜索用户名/订单号..." @search="handleSearch">
      <template #actions>
        <select v-model="statusFilter" class="search-select" @change="handleFilter">
          <option value="">全部状态</option>
          <option value="PENDING">待出库</option>
          <option value="COMPLETED">已出库</option>
          <option value="CANCELLED">已退货</option>
        </select>
        <button class="toolbar-btn" @click="loadOrders">刷新</button>
      </template>
    </SearchToolbar>

    <table class="product-table">
      <thead>
        <tr>
          <th>订单号</th>
          <th>用户名</th>
          <th>总金额</th>
          <th>出入库状态</th>
          <th>下单时间</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td>{{ order.id }}</td>
          <td>{{ order.username }}</td>
          <td>{{ currencySymbol }}{{ formatPrice(order.totalAmount) }}</td>
          <td><span :class="['status-tag', statusClass(order.status)]">{{ orderStatusText(order.status) }}</span></td>
          <td>{{ formatTime(order.createdAt) }}</td>
          <td>{{ order.remark || '-' }}</td>
          <td>
            <button class="action-btn" @click="showDetail(order)">查看详情</button>
          </td>
        </tr>
        <tr v-if="orders.length === 0">
          <td colspan="7" style="text-align:center;">暂无订单数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />

    <ModalPanel :visible="detailVisible" :title="`订单详情 #${detailOrder?.id}`" @close="closeDetail">
        <div class="form-row">
          <label>用户名</label>
          <input :value="detailOrder?.username" type="text" disabled />
        </div>
        <div class="form-row">
          <label>总金额</label>
          <input :value="currencySymbol + formatPrice(detailOrder?.totalAmount || 0)" type="text" disabled />
        </div>
        <div class="form-row">
          <label>出入库状态</label>
          <input :value="orderStatusText(detailOrder?.status || '')" type="text" disabled />
        </div>
        <div class="form-row">
          <label>下单时间</label>
          <input :value="formatTime(detailOrder?.createdAt || '')" type="text" disabled />
        </div>
        <div class="form-row">
          <label>备注</label>
          <input :value="detailOrder?.remark || '-'" type="text" disabled />
        </div>
        <div class="form-row">
          <label>收货人</label>
          <input :value="detailOrder?.receiverName || '-'" type="text" disabled />
        </div>
        <div class="form-row">
          <label>收货电话</label>
          <input :value="detailOrder?.receiverPhone || '-'" type="text" disabled />
        </div>
        <div class="form-row">
          <label>收货地址</label>
          <input :value="detailOrder?.receiverAddress || '-'" type="text" disabled />
        </div>
        <table class="product-table" style="margin:10px 0;">
          <thead>
            <tr><th>商品名</th><th>单价</th><th>数量</th><th>小计</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in detailOrder?.items" :key="item.id">
              <td>{{ item.productName }}</td>
              <td>{{ currencySymbol }}{{ formatPrice(item.price) }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ currencySymbol }}{{ formatPrice(item.price * item.quantity) }}</td>
            </tr>
          </tbody>
        </table>
        <template #actions>
          <button class="cancel-btn" @click="closeDetail">关闭</button>
        </template>
    </ModalPanel>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import SearchToolbar from '../common/SearchToolbar.vue'
import Pagination from '../common/Pagination.vue'
import ModalPanel from '../common/ModalPanel.vue'

interface OrderItem {
  id: number
  productName: string
  price: number
  quantity: number
}

interface OrderInfo {
  id: number
  username: string
  totalAmount: number
  status: string
  remark: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createdAt: string
  items: OrderItem[]
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

/** 列表数据 */
const orders = ref<OrderInfo[]>([])
const error = ref('')
const searchKeyword = ref('')
const statusFilter = ref('')

/** 分页 */
const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)

/** 详情弹窗 */
const detailVisible = ref(false)
const detailOrder = ref<OrderInfo | null>(null)

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

function orderStatusText(status: string): string {
  const map: Record<string, string> = { PENDING: '待出库', COMPLETED: '已出库', CANCELLED: '已退货' }
  return map[status] || status
}

function statusClass(status: string): string {
  const map: Record<string, string> = { PENDING: 'status-pending', COMPLETED: 'status-completed', CANCELLED: 'status-cancelled' }
  return map[status] || ''
}

function formatTime(time: string | undefined): string {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

/**
 * 构建分页查询 URL（拼接 status 与 keyword 参数）
 */
function buildQueryUrl(): string {
  const params = [`page=${page.value}`, `size=${size.value}`]
  if (statusFilter.value) {
    params.push(`status=${encodeURIComponent(statusFilter.value)}`)
  }
  if (searchKeyword.value.trim()) {
    params.push(`keyword=${encodeURIComponent(searchKeyword.value.trim())}`)
  }
  return `/orders?${params.join('&')}`
}

async function loadOrders() {
  error.value = ''
  try {
    const res = await get<PageResult<OrderInfo>>(buildQueryUrl())
    if (res.success && res.data) {
      orders.value = res.data.list
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      page.value = res.data.page
    } else {
      error.value = res.message || '加载订单列表失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

function handleSearch() {
  page.value = 1
  loadOrders()
}

function handleFilter() {
  page.value = 1
  loadOrders()
}

function goToPage(target: number) {
  if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
  page.value = target
  loadOrders()
}

function showDetail(order: OrderInfo) {
  detailOrder.value = order
  detailVisible.value = true
}

function closeDetail() {
  detailVisible.value = false
  detailOrder.value = null
}

onMounted(async () => {
  await loadOrders()
})
</script>
