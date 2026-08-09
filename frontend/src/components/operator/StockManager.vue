<template>

  <div class="product-management">
    <div class="page-header">
      <h2>出入库管理</h2>
      <button class="back-btn" @click="router.push('/operator/home')">返回</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="successMsg" class="success-tip">{{ successMsg }}</div>

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

    <ModalPanel :visible="panelVisible" :title="operateForm.type === 'IN' ? '入库' : '出库'" @close="cancelPanel">
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
        <template #actions>
          <button @click="confirmOperate">确认</button>
          <button class="cancel-btn" @click="cancelPanel">取消</button>
        </template>
    </ModalPanel>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { get, post } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import SearchToolbar from '../common/SearchToolbar.vue'
import Pagination from '../common/Pagination.vue'
import ModalPanel from '../common/ModalPanel.vue'

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

/** 商品列表数据 */
const products = ref<Product[]>([])
const error = ref('')
const successMsg = ref('')

/** 搜索与分页 */
const searchKeyword = ref('')
const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)

/** 出入库弹窗 */
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

/** 确认出入库操作 */
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

onMounted(async () => {
  await loadProducts()
})
</script>