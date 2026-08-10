<template>

    <div class="product-management">
        <div class="page-header">
            <h2>商品管理</h2>
            <button class="back-btn" @click="router.push('/admin/home')">返回</button>
        </div>

        <div class="tab-bar tab-bar-square">
            <button :class="['tab-btn-square', { active: activeTab === 'product' }]" @click="activeTab = 'product'">商品管理</button>
            <button :class="['tab-btn-square', { active: activeTab === 'category' }]" @click="activeTab = 'category'">分类管理</button>
        </div>

        <div v-if="error" class="error">{{ error }}</div>
        <ModalPanel :visible="isCreating" title="新增商品" @close="cancelCreate">
            <div class="form-row form-row-image">
                <label>商品图片</label>
                <input type="file" accept="image/png,image/jpeg,image/gif,image/webp"
                    @change="(e) => handleImageUpload(e, 'create')" />
                <img v-if="createForm.image" class="form-image-preview" :src="normalizeImage(createForm.image)"
                    alt="预览" />
            </div>
            <div class="form-row">
                <label>商品名</label>
                <input v-model="createForm.name" type="text" />
            </div>
            <div class="form-row">
                <label>分类</label>
                <select v-model.number="createForm.categoryId">
                    <option :value="0">请选择分类</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ categoryPath(cat.id) }}</option>
                </select>
            </div>
            <div class="form-row">
                <label>价格</label>
                <input v-model.number="createForm.price" type="number" />
            </div>
            <div class="form-row">
                <label>描述</label>
                <input v-model="createForm.description" type="text" />
            </div>
            <div class="form-row">
                <label>库存</label>
                <input v-model.number="createForm.stock" type="number" />
            </div>
            <div class="form-row">
                <label>状态</label>
                <select v-model="createForm.status">
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                </select>
            </div>
            <template #actions>
                <button @click="createNewProduct">确认新增</button>
                <button class="cancel-btn" @click="cancelCreate">取消</button>
            </template>
        </ModalPanel>

        <ModalPanel :visible="showing" :title="editing ? '编辑商品' : '商品详情'" @close="cancelEdit">
            <div class="form-row form-row-image">
                <label>商品图片</label>
                <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" :disabled="!editing"
                    @change="(e) => handleImageUpload(e, 'edit')" />
                <img v-if="editForm.image" class="form-image-preview" :src="normalizeImage(editForm.image)" alt="预览" />
            </div>
            <div class="form-row">
                <label>商品名</label>
                <input v-model="editForm.name" type="text" :disabled="!editing" />
            </div>
            <div class="form-row">
                <label>分类</label>
                <select v-model.number="editForm.categoryId" :disabled="!editing">
                    <option :value="0">请选择分类</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ categoryPath(cat.id) }}</option>
                </select>
            </div>
            <div class="form-row">
                <label>价格</label>
                <input v-model.number="editForm.price" type="number" :disabled="!editing" />
            </div>
            <div class="form-row">
                <label>描述</label>
                <input v-model="editForm.description" type="text" :disabled="!editing" />
            </div>
            <div class="form-row">
                <label>库存</label>
                <input v-model.number="editForm.stock" type="number" :disabled="!editing" />
            </div>
            <div class="form-row">
                <label>状态</label>
                <select v-model="editForm.status" :disabled="!editing">
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                </select>
            </div>
            <template #actions>
                <button v-if="editing" @click="saveEdit">保存</button>
                <button v-if="!editing" @click="editing = !editing">编辑</button>
                <button class="cancel-btn" @click="cancelEdit">取消</button>
            </template>
        </ModalPanel>

        <template v-if="activeTab === 'category'">
            <component :is="CategoryManager" @close="activeTab = 'product'" />
        </template>

        <SearchToolbar v-if="activeTab === 'product'" v-model="searchKeyword" placeholder="搜索商品名..." @search="handleSearch">
            <template #actions>
                <button class="toolbar-btn" @click="openCreate">新增商品</button>
            </template>
        </SearchToolbar>

        <div class="filter-bar" v-if="activeTab === 'product'">
            <div class="filter-item">
                <label>分类</label>
                <select v-model="filterCategoryId">
                    <option :value="0">全部分类</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ categoryPath(cat.id) }}</option>
                </select>
            </div>
            <div class="filter-item">
                <label>价格</label>
                <input v-model.number="filterPriceMin" type="number" placeholder="最低价" />
                <span class="filter-sep">~</span>
                <input v-model.number="filterPriceMax" type="number" placeholder="最高价" />
            </div>
            <div class="filter-item">
                <label>状态</label>
                <select v-model="filterStatus">
                    <option :value="-1">全部状态</option>
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                </select>
            </div>
            <div class="filter-actions">
                <button class="filter-btn" @click="handleSearch">应用筛选</button>
                <button class="filter-btn reset-btn" @click="resetFilters">重置</button>
            </div>
        </div>

        <div class="table-wrap" v-if="activeTab === 'product'">
            <table class="product-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>图片</th>
                        <th>商品名</th>
                        <th>分类</th>
                        <th>价格</th>
                        <th>库存</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="product in products" :key="product.id"
                        :class="{ 'low-stock-row': isLowStock(product.stock) }">
                        <td>{{ product.id }}</td>
                        <td class="product-img-cell">
                            <img v-if="product.image && !imageErrorMap[product.id]" class="product-img-thumb"
                                :src="normalizeImage(product.image)" :alt="product.name"
                                @error="onImageError(product.id)" />
                            <img v-else class="product-img-thumb" src="/default-image.svg" alt="暂无图片" />
                        </td>
                        <td>{{ product.name }}</td>
                        <td>{{ product.categoryName || product.categoryId }}</td>
                        <td>{{ configStore.configs['currency_symbol'] || '¥' }}{{ formatPrice(product.price) }}</td>
                        <td :class="{ 'low-stock-cell': isLowStock(product.stock) }">{{ product.stock }}</td>
                        <td>{{ product.status === 1 ? '启用' : '禁用' }}</td>
                        <td>
                            <button class="action-btn" @click="showProduct(product)">查询</button>
                            <button class="action-btn delete" @click="deleteProduct(product)">删除</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <Pagination v-if="activeTab === 'product'" :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />


        <div v-if="stockAlertVisible" class="stock-alert-overlay" @click.self="stockAlertVisible = false">
            <div class="stock-alert-modal">
                <h3>⚠️ 库存预警</h3>
                <p>以下商品库存低于预警阈值（{{ alertThreshold }}），请及时补货：</p>
                <ul class="stock-alert-list">
                    <li v-for="item in lowStockItems" :key="item.id">
                        <span class="alert-name">{{ item.name }}</span>
                        <span class="alert-stock">剩余 {{ item.stock }} 件</span>
                    </li>
                </ul>
                <button class="stock-alert-btn" @click="stockAlertVisible = false">知道了</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post, put, del, upload } from '../../utils/request'
import { configStore, getConfig } from '../../utils/configStore'
import router from '../../route/Router'
import CategoryManager from './CategoryManager.vue'
import { buildPathMap } from '../../utils/category'
import type { Category } from '../../utils/category'
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

interface LowStockItem {
    id: number
    name: string
    stock: number
}

const products = ref<Product[]>([])
const error = ref('')
const searchKeyword = ref('')
const imageErrorMap = ref<Record<number, boolean>>({})

const categories = ref<Category[]>([])
const categoryPathMap = ref<Map<number, string>>(new Map())

function categoryPath(id: number): string {
    return categoryPathMap.value.get(id) || categories.value.find(c => c.id === id)?.name || ''
}

const filterCategoryId = ref(0)
const filterPriceMin = ref<number | null>(null)
const filterPriceMax = ref<number | null>(null)
const filterStatus = ref(-1)

const createForm = ref<{ name: string; categoryId: number; price: number; image: string; description: string; stock: number; status: number }>({
    name: '',
    categoryId: 0,
    price: 0,
    image: '',
    description: '',
    stock: 0,
    status: 1,
})

const editForm = ref<Product>({
    id: 0,
    name: '',
    categoryId: 0,
    price: 0,
    image: '',
    description: '',
    stock: 0,
    status: 1
})
const showing = ref(false)
const editing = ref(false)
const isCreating = ref<boolean>(false)
const activeTab = ref<'product' | 'category'>('product')

const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)
const isSearching = ref(false)

const stockAlertVisible = ref(false)
const lowStockItems = ref<LowStockItem[]>([])
const alertThreshold = ref(0)

function normalizeImage(src: string): string {
    if (!src) return ''
    if (/^https?:/i.test(src)) return src
    if (/^\//.test(src)) return src
    return '/' + src
}

function onImageError(id: number) {
    imageErrorMap.value[id] = true
}

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
        const res = await get<PageResult<Product>>(`/products?page=${page.value}&size=${size.value}`)
        if (res.success) {
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

async function loadCategories() {
    try {
        const res = await get<Category[]>('/categories')
        if (res.success && Array.isArray(res.data)) {
            categories.value = res.data
            categoryPathMap.value = buildPathMap(res.data)
        }
    } catch {
        // 分类加载失败不阻塞商品列表
    }
}

async function loadData() {
    if (isSearching.value) {
        await searchProducts()
    } else {
        await loadProducts()
    }
}

/**
 * 组装综合筛选查询参数（keyword/categoryId/priceMin/priceMax/status），空条件不携带
 */
function buildFilterParams(): string {
    const params = new URLSearchParams()
    if (searchKeyword.value.trim() !== '') params.set('keyword', searchKeyword.value.trim())
    if (filterCategoryId.value > 0) params.set('categoryId', String(filterCategoryId.value))
    if (filterPriceMin.value != null && !Number.isNaN(filterPriceMin.value)) params.set('priceMin', String(filterPriceMin.value))
    if (filterPriceMax.value != null && !Number.isNaN(filterPriceMax.value)) params.set('priceMax', String(filterPriceMax.value))
    if (filterStatus.value >= 0) params.set('status', String(filterStatus.value))
    return params.toString()
}

/**
 * 是否存在生效的筛选条件（决定是否走综合筛选查询）
 */
function hasActiveFilter(): boolean {
    return searchKeyword.value.trim() !== ''
        || filterCategoryId.value > 0
        || (filterPriceMin.value != null && !Number.isNaN(filterPriceMin.value))
        || (filterPriceMax.value != null && !Number.isNaN(filterPriceMax.value))
        || filterStatus.value >= 0
}

async function searchProducts() {
    error.value = ''
    try {
        const qs = buildFilterParams()
        const res = await get<PageResult<Product>>(`/products?page=${page.value}&size=${size.value}${qs ? '&' + qs : ''}`)
        if (res.success) {
            products.value = res.data.list
            total.value = res.data.total
            totalPages.value = res.data.totalPages
            page.value = res.data.page
        } else {
            error.value = res.message || '搜索失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

async function handleSearch() {
    page.value = 1
    isSearching.value = hasActiveFilter()
    await loadData()
}

function resetFilters() {
    searchKeyword.value = ''
    filterCategoryId.value = 0
    filterPriceMin.value = null
    filterPriceMax.value = null
    filterStatus.value = -1
    page.value = 1
    isSearching.value = false
    loadProducts()
}

/**
 * 检查低库存商品并按预警阈值过滤，有则弹出预警
 */
async function checkLowStock() {
    if (getConfig('enable_stock_warning') !== 'true') return
    alertThreshold.value = parseInt(getConfig('low_stock_threshold') || '50', 10)
    try {
        const res = await get<PageResult<Product>>(`/products?page=1&size=1000`)
        if (res.success && res.data?.list) {
            lowStockItems.value = res.data.list.filter(p => p.stock < alertThreshold.value)
            if (lowStockItems.value.length > 0) {
                stockAlertVisible.value = true
            }
        }
    } catch {

    }
}

function goToPage(target: number) {
    if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
    page.value = target
    loadData()
}

function openCreate() {
    createForm.value = { name: '', categoryId: 0, price: 0, image: '', description: '', stock: 0, status: 1 }
    error.value = ''
    showing.value = false
    isCreating.value = true
}

function cancelCreate() {
    isCreating.value = false
    error.value = ''
}

async function createNewProduct() {
    error.value = ''
    try {
        const res = await post<{ code: number; message: string }>('/products', {...createForm.value})
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

/**
 * 图片上传：FormData 发送到 /upload，成功后写入对应表单的 image 字段
 */
async function handleImageUpload(e: Event, target: 'create' | 'edit') {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return

    try {
        const formData = new FormData()
        formData.append('file', file)
        const res = await upload<string>('/upload', formData)
        if (res.success && res.data) {
            if (target === 'create') {
                createForm.value.image = res.data
            } else {
                editForm.value.image = res.data
            }
        } else {
            error.value = res.message || '图片上传失败'
        }
    } catch (err) {
        error.value = err instanceof Error ? err.message : '网络错误'
    }
    input.value = ''
}

function showProduct(product: Product) {
    editForm.value = { ...product }
    editing.value = false
    isCreating.value = false
    showing.value = true
}

function cancelEdit() {
    showing.value = false
    editing.value = false
    error.value = ''
}

async function saveEdit() {
    error.value = ''
    editing.value = !editing.value
    try {
        const payload = {
            id: editForm.value.id,
            name: editForm.value.name,
            categoryId: editForm.value.categoryId,
            price: editForm.value.price,
            image: editForm.value.image,
            description: editForm.value.description,
            stock: editForm.value.stock,
            status: editForm.value.status,
        }
        const res = await put<{ code: number; message: string }>('/products', payload)
        if (res.success) {
            showing.value = false
            await loadData()
        } else {
            error.value = res.message || '更新失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

async function deleteProduct(product: Product) {
    if (!confirm(`确定删除商品 ${product.name} 吗？`)) return
    error.value = ''
    try {
        const res = await del<{ code: number; message: string }>(`/products/${product.id}`)
        if (res.success) {
            if (products.value.length === 1 && page.value > 1) {
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
    await loadProducts()
    checkLowStock()
    loadCategories()
})
</script>
