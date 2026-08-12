<template>

  <div class="shop-page">
    <div class="page-header">
      <h2>商品商城</h2>
      <div class="header-actions">
        <div class="cart-icon" @click="router.push('/user/cart')">
          <span class="cart-entry-icon">🛒</span>
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
        <img v-else class="shop-card-img" src="/default-image.svg" alt="暂无图片" />
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
          <button class="action-btn buy-now" @click="openBuyPanel(product)">立即购买</button>
        </div>
      </div>
    </div>

    <Pagination :page="currentPage" :total="allProducts.length" :totalPages="totalPages" @change="goToPage" />

    <ModalPanel :visible="detailVisible" title="商品详情" @close="detailVisible = false">
        <div class="form-row form-row-image">
          <label>商品图片</label>
          <img v-if="detailProduct.image" class="form-image-preview" :src="normalizeImage(detailProduct.image)" alt="预览" />
          <img v-else class="form-image-preview" src="/default-image.svg" alt="暂无图片" />
        </div>
        <div class="form-row">
          <label>商品名</label>
          <input :value="detailProduct.name" type="text" disabled />
        </div>
        <div class="form-row">
          <label>分类</label>
          <input :value="detailProduct.categoryName || detailProduct.categoryId" type="text" disabled />
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
        <template #actions>
          <button class="action-btn" @click="addToCart(detailProduct)">加入购物车</button>
          <button class="action-btn buy-now" @click="openBuyPanel(detailProduct)">立即购买</button>
          <button class="cancel-btn" @click="detailVisible = false">关闭</button>
        </template>
    </ModalPanel>

    <ModalPanel :visible="buyVisible" title="确认订单" @close="buyVisible = false">
        <div class="form-row">
          <label>商品</label>
          <input :value="buyProduct.name" type="text" disabled />
        </div>
        <div class="form-row">
          <label>单价</label>
          <input :value="currencySymbol + formatPrice(buyProduct.price)" type="text" disabled />
        </div>
        <div class="form-row">
          <label>数量</label>
          <div class="qty-control">
            <button class="qty-btn" @click="decreaseBuyQty">-</button>
            <span class="qty-num">{{ buyQuantity }}</span>
            <button class="qty-btn" @click="increaseBuyQty">+</button>
          </div>
        </div>
        <div class="form-row">
          <label>收货人</label>
          <input v-model="buyForm.receiverName" type="text" placeholder="请输入收货人姓名" />
        </div>
        <div class="form-row">
          <label>手机号</label>
          <input v-model="buyForm.receiverPhone" type="text" placeholder="请输入联系电话" />
        </div>
        <div class="form-row">
          <label>收货地址</label>
          <input v-model="buyForm.receiverAddress" type="text" placeholder="请输入收货地址" />
        </div>
        <template #actions>
          <button class="action-btn buy-now" @click="confirmBuy">确认购买</button>
          <button class="cancel-btn" @click="buyVisible = false">取消</button>
        </template>
    </ModalPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { get, post } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import Pagination from '../common/Pagination.vue'
import SearchToolbar from '../common/SearchToolbar.vue'
import ModalPanel from '../common/ModalPanel.vue'

interface Product {
  id: number
  name: string
  categoryId: number
  categoryName?: string
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

interface UserInfo {
  realName: string
  phone: string
  email: string
}

/** 商品列表与搜索 */
const allProducts = ref<Product[]>([])
const searchKeyword = ref('')
const isSearching = ref(false)
const loading = ref(false)
const error = ref('')

/** 购物车角标 */
const cartCount = ref(0)

/** 商品详情弹窗 */
const detailVisible = ref(false)
const detailProduct = ref<Product>({ id: 0, name: '', categoryId: 0, price: 0, image: '', description: '', stock: 0, status: 1 })

/** 立即购买弹窗 */
const buyVisible = ref(false)
const buyProduct = ref<Product>({ id: 0, name: '', categoryId: 0, price: 0, image: '', description: '', stock: 0, status: 1 })
const buyQuantity = ref(1)
const buyForm = ref({ receiverName: '', receiverPhone: '', receiverAddress: '' })

/** 前端分页 */
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

function formatPrice(n: number): string {
  const num = Number(n)
  if (Number.isNaN(num)) return String(n)
  return num.toFixed(2)
}

/** 加载全部商品（一次性拉取，前端分页展示） */
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

/** 关键字为空时回退到全量加载 */
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

function resetSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  currentPage.value = 1
  loadProducts()
}

function goToPage(target: number) {
  if (target < 1 || target > totalPages.value) return
  currentPage.value = target
}

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
 * 打开商品详情面板：先调用独立详情接口 GET /products/{id} 拉取最新数据（含分类名称），失败时回退到列表行数据
 */
async function showDetail(product: Product) {
  detailProduct.value = { ...product }
  detailVisible.value = true
  try {
    const res = await get<Product>(`/products/${product.id}`)
    if (res.success && res.data) {
      detailProduct.value = res.data
    } else {
      error.value = res.message || '加载详情失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载详情失败'
  }
}

/**
 * 立即购买：打开确认面板（确认数量与收货信息），不再经过购物车
 */
async function openBuyPanel(product: Product) {
  buyProduct.value = { ...product }
  buyQuantity.value = 1
  buyForm.value = { receiverName: '', receiverPhone: '', receiverAddress: '' }
  error.value = ''
  buyVisible.value = true
  try {
    const res = await get<UserInfo>('/users/me')
    if (res.success && res.data) {
      buyForm.value.receiverName = res.data.realName || ''
      buyForm.value.receiverPhone = res.data.phone || ''
    }
  } catch(e) {
    console.error(e); 
  }
}

function increaseBuyQty() {
  if (buyQuantity.value < buyProduct.value.stock) {
    buyQuantity.value += 1
  }
}

function decreaseBuyQty() {
  if (buyQuantity.value > 1) {
    buyQuantity.value -= 1
  }
}

/** 确认购买：校验数量与收货信息后直接调用 POST /orders 下单，成功后跳转我的订单页 */
async function confirmBuy() {
  if (buyQuantity.value < 1 || buyQuantity.value > buyProduct.value.stock) {
    error.value = '购买数量超出库存范围'
    return
  }
  if (!buyForm.value.receiverName.trim() || !buyForm.value.receiverPhone.trim() || !buyForm.value.receiverAddress.trim()) {
    error.value = '请填写完整的收货信息'
    return
  }
  try {
    const res = await post<{ id: number }>('/orders?fromCart=false', {
      items: [{ productId: buyProduct.value.id, quantity: buyQuantity.value }],
      remark: '立即购买',
      receiverName: buyForm.value.receiverName.trim(),
      receiverPhone: buyForm.value.receiverPhone.trim(),
      receiverAddress: buyForm.value.receiverAddress.trim()
    })
    if (!res.success) {
      error.value = res.message || '下单失败'
      return
    }
    buyVisible.value = false
    error.value = ''
    router.push('/user/myOrders')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '下单失败，请稍后重试'
  }
}

onMounted(() => {
  refreshCartCount()
  loadProducts()
})
</script>
