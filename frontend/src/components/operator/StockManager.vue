<template>

  <div class="product-management">
    <div class="page-header">
      <h2>出入库管理</h2>
      <button class="back-btn" @click="router.push('/operator/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="successMsg" class="success-tip">{{ successMsg }}</div>

    <div class="tab-bar tab-bar-square">
      <button :class="['tab-btn-square', { active: activeTab === 'product' }]" @click="switchTab('product')">商品出入库</button>
      <button :class="['tab-btn-square', { active: activeTab === 'order' }]" @click="switchTab('order')">订单出入库</button>
    </div>

    <div v-if="activeTab === 'product'">
    <SearchToolbar v-model="searchKeyword" placeholder="搜索商品名..." @search="handleSearch">
      <template #actions>
        <button class="toolbar-btn" @click="loadProducts">刷新</button>
      </template>
    </SearchToolbar>

    <table class="product-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>商品名</th>
          <th>价格</th>
          <th>库存</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in products" :key="product.id" :class="{ 'low-stock-row': isLowStock(product.stock) }">
          <td>{{ product.id }}</td>
          <td>{{ product.name }}</td>
          <td>{{ currencySymbol }}{{ formatPrice(product.price) }}</td>
          <td :class="{ 'low-stock-cell': isLowStock(product.stock) }">{{ product.stock }}</td>
          <td>
            <button class="action-btn" @click="openPanel(product, 'IN')">入库</button>
            <button class="action-btn delete" @click="openPanel(product, 'OUT')">出库</button>
          </td>
        </tr>
        <tr v-if="products.length === 0">
          <td colspan="5" style="text-align:center;">暂无商品数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />
    </div>

    <div v-if="activeTab === 'order'" class="order-stock-panel">
      <div class="toolbar-row"><button class="toolbar-btn" @click="loadOrders">刷新订单</button></div>
      <table class="product-table">
        <thead><tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th><th>下单时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.id }}</td><td>{{ order.username }}</td>
            <td>{{ currencySymbol }}{{ formatPrice(order.totalAmount) }}</td>
            <td>{{ orderStatusText(order.status) }}</td>
            <td>{{ formatTime(order.createdAt) }}</td>
            <td>
              <button class="action-btn" v-if="order.status === 'PENDING'" @click="stockOutByOrder(order.id)">出库</button>
              <button class="action-btn delete" v-if="order.status === 'COMPLETED'" @click="stockInByOrder(order.id)">退货入库</button>
              <button class="action-btn" @click="showOrderDetail(order)">详情</button>
            </td>
          </tr>
          <tr v-if="orders.length === 0"><td colspan="6" style="text-align:center;">暂无订单数据</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="panelVisible" class="panel-overlay" @click.self="cancelPanel">
      <div class="edit-panel">
        <h3>{{ operateForm.type === 'IN' ? '入库' : '出库' }}</h3>
        <div class="form-row">
          <label>商品名</label>
          <input :value="operateForm.productName" type="text" disabled />
        </div>
        <div class="form-row">
          <label>操作类型</label>
          <select v-model="operateForm.type">
            <option value="IN">入库</option>
            <option value="OUT">出库</option>
          </select>
        </div>
        <div class="form-row">
          <label>数量</label>
          <input v-model.number="operateForm.quantity" type="number" min="1" />
        </div>
        <div class="form-row">
          <label>备注</label>
          <input v-model="operateForm.remark" type="text" placeholder="可选备注" />
        </div>
        <div class="form-actions">
          <button @click="confirmOperate">确认</button>
          <button class="cancel-btn" @click="cancelPanel">取消</button>
        </div>
      </div>
    </div>

    <div v-if="orderDetailVisible" class="panel-overlay" @click.self="closeOrderDetail">
      <div class="edit-panel">
        <h3>订单详情 #{{ detailOrder?.id }}</h3>
        <div class="form-row"><label>用户</label><input :value="detailOrder?.username" type="text" disabled /></div>
        <div class="form-row"><label>总金额</label><input :value="currencySymbol + formatPrice(detailOrder?.totalAmount || 0)" type="text" disabled /></div>
        <div class="form-row"><label>状态</label><input :value="orderStatusText(detailOrder?.status || '')" type="text" disabled /></div>
        <table class="product-table" style="margin:10px 0;">
          <thead><tr><th>商品名</th><th>单价</th><th>数量</th></tr></thead>
          <tbody>
            <tr v-for="item in detailOrder?.items" :key="item.id">
              <td>{{ item.productName }}</td><td>{{ currencySymbol }}{{ formatPrice(item.price) }}</td><td>{{ item.quantity }}</td>
            </tr>
          </tbody>
        </table>
        <div class="form-actions"><button class="cancel-btn" @click="closeOrderDetail">关闭</button></div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">


import { ref, onMounted, computed } from 'vue'
import { get, post } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import SearchToolbar from '../common/SearchToolbar.vue'
import Pagination from '../common/Pagination.vue'

interface Product {
  id: number
  name: string
  categoryId: number
  price: number
  image: string
  description: string
  stock: number
  status: number
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

const products = ref<Product[]>([])
const error = ref('')
const successMsg = ref('')

const activeTab = ref<'product' | 'order'>('product')

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
  createdAt: string
  items: OrderItem[]
}
const orders = ref<OrderInfo[]>([])
const orderDetailVisible = ref(false)
const detailOrder = ref<OrderInfo | null>(null)

const searchKeyword = ref('')
const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)

const panelVisible = ref(false)
const operateForm = ref<{
  productId: number
  productName: string
  type: 'IN' | 'OUT'
  quantity: number
  remark: string
}>({
  productId: 0,
  productName: '',
  type: 'IN',
  quantity: 1,
  remark: '',
})

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

function isLowStock(stock: number): boolean {
  return getConfig('enable_stock_warning') === 'true' && stock < parseInt(getConfig('low_stock_threshold') || '50', 10)
}

/**
 * 加载商品列表：支持关键词搜索与分页
 */
async function loadProducts() {
  error.value = ''
  try {
    const query = searchKeyword.value
      ? `/products/search?value=${encodeURIComponent(searchKeyword.value)}&page=${page.value}&size=${size.value}`
      : `/products?page=${page.value}&size=${size.value}`
    const res = await get<PageResult<Product>>(query)
    if (res.success && res.data?.list) {
      products.value = res.data.list
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      page.value = res.data.page
    } else {
      error.value = res.message || '加载商品列表失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

function handleSearch() {
  page.value = 1
  loadProducts()
}

function goToPage(target: number) {
  if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
  page.value = target
  loadProducts()
}


function openPanel(product: Product, type: 'IN' | 'OUT') {
  operateForm.value = {
    productId: product.id,
    productName: product.name,
    type,
    quantity: 1,
    remark: '',
  }
  error.value = ''
  successMsg.value = ''
  panelVisible.value = true
}

function cancelPanel() {
  panelVisible.value = false
  error.value = ''
}

/**
 * 确认出入库
 */
async function confirmOperate() {
  error.value = ''
  successMsg.value = ''
  if (!operateForm.value.quantity || operateForm.value.quantity <= 0) {
    error.value = '请输入有效的数量'
    return
  }
  const payload = {
    productId: operateForm.value.productId,
    quantity: operateForm.value.quantity,
    remark: operateForm.value.remark,
  }
  try {
    const url = operateForm.value.type === 'IN' ? '/stock/in' : '/stock/out'
    const res = await post(url, payload)
    if (res.success) {
      panelVisible.value = false
      successMsg.value = operateForm.value.type === 'IN' ? '入库成功' : '出库成功'
      await loadProducts()
    } else {
      error.value = res.message || (operateForm.value.type === 'IN' ? '入库失败' : '出库失败')
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

function switchTab(tab: 'product' | 'order') {
  activeTab.value = tab
  error.value = ''
  successMsg.value = ''
  if (tab === 'order') {
    loadOrders()
  }
}

async function loadOrders() {
  error.value = ''
  try {
    const res = await get<PageResult<OrderInfo>>('/orders?page=1&size=100')
    if (res.success && res.data) {
      orders.value = res.data.list
    } else {
      error.value = res.message || '加载订单列表失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

async function stockOutByOrder(orderId: number) {
  error.value = ''
  successMsg.value = ''
  try {
    const res = await post(`/stock/order/${orderId}/out`, {})
    if (res.success) {
      successMsg.value = '订单出库成功'
      await loadOrders()
    } else {
      error.value = res.message || '订单出库失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

async function stockInByOrder(orderId: number) {
  error.value = ''
  successMsg.value = ''
  try {
    const res = await post(`/stock/order/${orderId}/in`, {})
    if (res.success) {
      successMsg.value = '订单退货入库成功'
      await loadOrders()
    } else {
      error.value = res.message || '订单退货入库失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  }
}

function showOrderDetail(order: OrderInfo) {
  detailOrder.value = order
  orderDetailVisible.value = true
}

function closeOrderDetail() {
  orderDetailVisible.value = false
  detailOrder.value = null
}

function orderStatusText(status: string): string {
  const map: Record<string, string> = { PENDING: '待出库', COMPLETED: '已出库', CANCELLED: '已退货' }
  return map[status] || status
}

function formatTime(time: string | undefined): string {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  await loadProducts()
})
</script>