<template>

  <div class="cart-page">
    <div class="page-header">
      <h2>我的购物车</h2>
      <button class="back-btn" @click="router.back">返回</button>
    </div>

    <div v-if="successTip" class="success-tip">{{ successTip }}</div>
    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="cartItems.length === 0" class="empty-tip cart-empty">
      <p>空</p>
      <button class="go-shop-btn" @click="router.push('/user/shop')">去购物</button>
    </div>

    <template v-else>
      <table class="product-table cart-table">
        <thead>
          <tr>
            <th>商品图片</th>
            <th>商品名</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in cartItems" :key="item.id">
            <td class="product-img-cell">
              <img
                v-if="item.image"
                class="product-img-thumb"
                :src="normalizeImage(item.image)"
                :alt="item.name"
              />
              <img v-else class="product-img-thumb" src="/default-image.svg" alt="暂无图片" />
            </td>
            <td>{{ item.name }}</td>
            <td>{{ currencySymbol }}{{ formatPrice(item.price) }}</td>
            <td>
              <div class="qty-control">
                <button class="qty-btn" @click="setQty(item.id,item.quantity-1)">-</button>
                <input type="number" class="qty-num" v-value="item.quantity" @change="e => setQty(item.id,Number((e.target as HTMLInputElement).value))" />
                <button class="qty-btn" @click="setQty(item.id,item.quantity+1)">+</button>
              </div>
            </td>
            <td>{{ currencySymbol }}{{ formatPrice(item.price * item.quantity) }}</td>
            <td>
              <button class="action-btn delete" @click="removeItem(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="cart-footer">
        <div class="cart-total">
          总计：<span class="total-price">{{ currencySymbol }}{{ formatPrice(totalPrice) }}</span>
        </div>
        <button class="checkout-btn" @click="checkout">一键购买</button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getConfig } from '../../utils/configStore'
import { get, post, put, del } from '../../utils/request'
import router from '../../route/Router'

interface CartItem {
  id: number
  productId: number
  name: string
  price: number
  image: string
  quantity: number
  stock: number
}

/** 购物车列表 */
const cartItems = ref<CartItem[]>([])
const successTip = ref('')
const error = ref('')

const currencySymbol = computed(() => getConfig('currency_symbol') || '¥')

const totalPrice = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

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

async function loadCart() {
  try {
    const res = await get<CartItem[]>('/cart')
    cartItems.value = res.success && res.data ? res.data : []
  } catch {
    cartItems.value = []
  }
}

async function setQty(id: number, quantity: number) {
  if (quantity < 1) return
  const item = cartItems.value.find(i => i.id === id)
  if (!item) return
  try {
    const res = await put('/cart', { id, quantity })
    if (res.success) {
      item.quantity = quantity
    } else {
      error.value = res.message || '操作失败'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function removeItem(id: number) {
  if (!confirm('确定从购物车移除该商品吗？')) return
  const res = await del(`/cart/${id}`)
  if (res.success) {
    cartItems.value = cartItems.value.filter(i => i.id !== id)
  } else {
    error.value = res.message || '移除失败'
  }
}

/**
 * 一键购买：调用后端 POST /orders 下单（后端扣库存并清空购物车），成功后清空本地列表
 */
async function checkout() {
  if (cartItems.value.length === 0) return
  if (!confirm('确认购买购物车中的全部商品吗？')) return

  const items = cartItems.value.map((i) => ({ productId: i.productId, quantity: i.quantity }))
  try {
    const res = await post<{ id: number }>('/orders', { items, remark: '购物车结算' })
    if (!res.success) {
      error.value = res.message || '下单失败'
      return
    }
    cartItems.value = []
    error.value = ''
    successTip.value = '购买成功'
    setTimeout(() => {
      successTip.value = ''
    }, 3000)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '下单失败，请稍后重试'
  }
}

onMounted(() => {
  loadCart()
})
</script>
