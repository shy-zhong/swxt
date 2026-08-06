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
          <button class="action-btn" @click="showDetail(product)">查看详情</button>
          <button class="action-btn" @click="addToCart(product)">加入购物车</button>
          <button class="action-btn buy-now" @click="buyNow(product)">立即购买</button>
        </div>
      </div>
    </div>

    <Pagination :page="currentPage" :total="allProducts.length" :totalPages="totalPages" @change="goToPage" />

    <div v-if="detailVisible" class="panel-overlay" @click.self="closeDetail">
      <div class="edit-panel">
        <h3>商品详情</h3>
        <div class="form-row form-row-image">
          <label>商品图片</label>
          <img v-if="detailProduct.image" class="form-image-preview" :src="normalizeImage(detailProduct.image)" alt="预览" />
          <span v-else class="no-image">🖼️ 无图</span>
        </div>
        <div class="form-row">
          <label>商品名</label>
          <input :value="detailProduct.name" type="text" disabled />
        </div>
        <div class="form-row">
          <label>分类ID</label>
          <input :value="detailProduct.categoryId" type="number" disabled />
        </div>
        <div class="form-row">
          <label>价格</label>
          <input :value="currencySymbol + formatPrice(detailProduct.price)" type="text" disabled />
        </div>
        <div class="form-row">
          <label>描述</label>
          <input :value="detailProduct.description || '暂无描述'" type="text" disabled />
        </div>
        <div class="form-row">
          <label>库存</label>
          <input :value="detailProduct.stock" type="number" disabled />
        </div>
        <div class="form-row">
          <label>状态</label>
          <input :value="detailProduct.status === 1 ? '启用' : '禁用'" type="text" disabled />
        </div>
        <div class="form-actions">
          <button class="action-btn" @click="addToCart(detailProduct)">加入购物车</button>
          <button class="action-btn buy-now" @click="buyNow(detailProduct)">立即购买</button>
          <button class="cancel-btn" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, onMounted, computed } from 'vue'
import { get, post } from '../../utils/request'
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

const detailVisible = ref(false)
const detailProduct = ref<Product>({ id: 0, name: '', categoryId: 0, price: 0, image: '', description: '', stock: 0, status: 1 })

const pageSize = 12
const currentPage = ref(1)

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

const totalPages = computed(() => Math.max(1, Math.ceil(allProducts.value.length / pageSize)))

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return allProducts.value.slice(start, start + pageSize)
})

function normalizeImage(src: string): string {
  if (!src) return ''
  if (/^https?:/i.test(src)) return src
  if (/^\//.test(src)) return src
  return '/' + src
}

/**
 * 保留两位小数
 */
function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

/**
 * 读取购物车总件数（后端按登录用户统计）
 */
async function refreshCartCount() {
  try {
    const res = await get<number>('/cart/count')
    cartCount.value = res.success && res.data ? res.data : 0
  } catch {
    cartCount.value = 0
  }
}

/**
 * 将商品加入购物车（调后端，已存在则数量累加），成功后刷新角标
 */
async function addToCart(product: Product) {
  try {
    const res = await post('/cart', { productId: product.id, quantity: 1 })
    if (!res.success) {
      error.value = res.message || '加入购物车失败'
      return false
    }
    error.value = ''
    await refreshCartCount()
    return true
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加入购物车失败'
    return false
  }
}

/**
 * 立即购买：加入购物车后跳转到购物车页
 */
async function buyNow(product: Product) {
  const ok = await addToCart(product)
  if (ok) {
    router.push('/user/cart')
  }
}

/**
 * 打开商品详情面板
 */
function showDetail(product: Product) {
  detailProduct.value = { ...product }
  detailVisible.value = true
}

/**
 * 关闭商品详情面板
 */
function closeDetail() {
  detailVisible.value = false
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
