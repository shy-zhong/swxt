<template>

  <div class="shop-page">
    <div class="page-header">
      <h2>商品商城</h2>
      <div class="header-actions">
        <div class="cart-icon" @click="router.push('/user/cart')">
          <span class="icon">🛒</span>
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        </div>
        <button class="back-btn" @click="router.push('/user/home')">返回</button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <SearchToolbar v-model="searchKeyword" placeholder="搜索商品名..." @search="handleSearch">
      <template #append>
        <button class="reset-btn" @click="resetSearch">重置</button>
      </template>
    </SearchToolbar>

    <div v-if="loading" class="empty-tip">加载中...</div>

    <div v-else-if="pagedProducts.length === 0" class="empty-tip">暂无商品</div>

    <div v-else class="shop-grid">
      <div class="shop-card" v-for="product in pagedProducts" :key="product.id">
        <img
          v-if="product.image"
          class="shop-card-img"
          :src="normalizeImage(product.image)"
          :alt="product.name"
        />
        <div v-else class="shop-card-img shop-card-noimg">🖼️ 无图</div>
        <div class="shop-card-body">
          <h3 class="shop-card-name">{{ product.name }}</h3>
          <p class="shop-card-desc">{{ product.description || '暂无描述' }}</p>
          <div class="shop-card-meta">
            <span class="shop-card-price">{{ currencySymbol }}{{ formatPrice(product.price) }}</span>
            <span class="shop-card-stock">库存 {{ product.stock }}</span>
          </div>
        </div>
        <div class="shop-card-actions">
          <button class="action-btn" @click="addToCart(product)">加入购物车</button>
          <button class="action-btn buy-now" @click="buyNow(product)">立即购买</button>
        </div>
      </div>
    </div>

    <Pagination :page="currentPage" :total="allProducts.length" :totalPages="totalPages" @change="goToPage" />
  </div>
</template>

<script setup lang="ts">

import { ref, onMounted, computed } from 'vue'
import { get } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import Pagination from '../common/Pagination.vue'
import SearchToolbar from '../common/SearchToolbar.vue'

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

interface CartItem {
  id: number
  name: string
  price: number
  image: string
  quantity: number
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

const allProducts = ref<Product[]>([])
const searchKeyword = ref('')
const isSearching = ref(false)
const loading = ref(false)
const error = ref('')
const cartCount = ref(0)

const pageSize = 12
const currentPage = ref(1)

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

const totalPages = computed(() => Math.max(1, Math.ceil(allProducts.value.length / pageSize)))

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return allProducts.value.slice(start, start + pageSize)
})

/**
 * 图片地址规范化：绝对 URL 原样返回，其余补上前导 /
 */
function normalizeImage(src: string): string {
  if (!src) return ''
  if (/^https?:/i.test(src)) return src
  if (/^\//.test(src)) return src
  return '/' + src
}

/**
 * 价格格式化：保留两位小数
 */
function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

/**
 * 从 localStorage 读取购物车并更新右上角数量
 */
function refreshCartCount() {
  try {
    const raw = localStorage.getItem('cart')
    const cart: CartItem[] = raw ? JSON.parse(raw) : []
    cartCount.value = cart.reduce((sum, item) => sum + item.quantity, 0)
  } catch {
    cartCount.value = 0
  }
}

/**
 * 将商品加入购物车（已存在则数量 +1），同步 localStorage 并刷新数量
 */
function addToCart(product: Product) {
  let cart: CartItem[] = []
  try {
    const raw = localStorage.getItem('cart')
    cart = raw ? JSON.parse(raw) : []
  } catch {
    cart = []
  }
  const existing = cart.find(item => item.id === product.id)
  if (existing) {
    existing.quantity += 1
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.image,
      quantity: 1
    })
  }
  localStorage.setItem('cart', JSON.stringify(cart))
  refreshCartCount()
}

/**
 * 立即购买：加入购物车后跳转到购物车页
 */
function buyNow(product: Product) {
  addToCart(product)
  router.push('/user/cart')
}

/**
 * 加载全部商品（一次性拉取，前端分页展示）
 */
async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    const res = await get<PageResult<Product>>('/products?page=1&size=1000')
    if (res.success && res.data?.list) {
      allProducts.value = res.data.list
    } else {
      error.value = res.message || '加载商品列表失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

/**
 * 按商品名搜索
 */
async function searchProducts() {
  loading.value = true
  error.value = ''
  try {
    const res = await get<PageResult<Product>>(`/products/search?value=${encodeURIComponent(searchKeyword.value)}&page=1&size=1000`)
    if (res.success && res.data?.list) {
      allProducts.value = res.data.list
    } else {
      error.value = res.message || '搜索失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

/**
 * 触发搜索：关键字为空时回退到全量加载
 */
async function handleSearch() {
  currentPage.value = 1
  if (searchKeyword.value.trim() === '') {
    isSearching.value = false
    await loadProducts()
  } else {
    isSearching.value = true
    await searchProducts()
  }
}

/**
 * 重置搜索条件并重新加载全部商品
 */
function resetSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  currentPage.value = 1
  loadProducts()
}

/**
 * 前端分页跳页
 */
function goToPage(target: number) {
  if (target < 1 || target > totalPages.value) return
  currentPage.value = target
}

onMounted(() => {
  refreshCartCount()
  loadProducts()
})
</script>
