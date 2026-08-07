<template>

  <div class="shop-page">
    <div class="page-header">
      <h2>我的订单</h2>
      <button class="back-btn" @click="router.push('/user/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="loading" class="empty-tip">加载中...</div>

    <div v-else-if="orders.length === 0" class="empty-tip">暂无订单</div>

    <table v-else class="product-table">
      <thead>
        <tr>
          <th>订单号</th>
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
          <td>{{ currencySymbol }}{{ formatPrice(order.totalAmount) }}</td>
          <td><span :class="['status-tag', statusClass(order.status)]">{{ orderStatusText(order.status) }}</span></td>
          <td>{{ formatTime(order.createdAt) }}</td>
          <td>{{ order.remark || '-' }}</td>
          <td>
            <button class="action-btn" @click="showDetail(order)">查看详情</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="detailVisible" class="panel-overlay" @click.self="closeDetail">
      <div class="edit-panel">
        <h3>订单详情 #{{ detailOrder?.id }}</h3>
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
        <div class="form-actions">
          <button class="cancel-btn" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, computed, onMounted } from 'vue'
import { get } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'

interface OrderItem {
  id: number
  productName: string
  price: number
  quantity: number
}

interface OrderInfo {
  id: number
  totalAmount: number
  status: string
  remark: string
  createdAt: string
  items: OrderItem[]
}

const orders = ref<OrderInfo[]>([])
const loading = ref(false)
const error = ref('')

const detailVisible = ref(false)
const detailOrder = ref<OrderInfo | null>(null)

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

/**
 * 订单状态映射为出入库状态文案
 */
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
 * 加载当前用户的全部订单（含订单项）
 */
async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const res = await get<OrderInfo[]>('/orders/my')
    if (res.success && res.data) {
      orders.value = res.data
    } else {
      error.value = res.message || '加载订单失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

function showDetail(order: OrderInfo) {
  detailOrder.value = order
  detailVisible.value = true
}

function closeDetail() {
  detailVisible.value = false
  detailOrder.value = null
}

onMounted(() => {
  loadOrders()
})
</script>
