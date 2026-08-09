<template>
    <div class="chart-card">
        <h3 class="chart-title">入库/出库记录</h3>
        <SearchToolbar v-model="searchKeyword" placeholder="搜索商品名/操作人..." @search="handleSearch" />
        <div v-if="error" class="error">{{ error }}</div>
        <div class="table-wrap">
            <table class="stats-table">
                <thead>
                    <tr>
                        <th>商品名称</th>
                        <th>类型</th>
                        <th>数量</th>
                        <th>操作人</th>
                        <th>备注</th>
                        <th>时间</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="r in stockRecords" :key="r.id">
                        <td>{{ r.productName || '-' }}</td>
                        <td>
                            <span class="status-tag" :class="r.type === 'IN' ? 'tag-on' : 'tag-off'">
                                {{ r.type === 'IN' ? '入库' : '出库' }}
                            </span>
                        </td>
                        <td>{{ r.quantity }}</td>
                        <td>{{ r.operator || '-' }}</td>
                        <td>{{ r.remark || '-' }}</td>
                        <td>{{ r.createdAt }}</td>
                    </tr>
                    <tr v-if="stockRecords.length === 0">
                        <td colspan="6" class="empty-tip">暂无数据</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <Pagination :page="page" :total="total" :totalPages="totalPages" @change="goToPage" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get } from '../../utils/request'
import { getConfig } from '../../utils/configStore'
import Pagination from './Pagination.vue'
import SearchToolbar from './SearchToolbar.vue'

interface StockRecordItem {
    id: number
    productId: number
    productName: string
    type: string
    quantity: number
    operator: string
    remark: string
    createdAt: string
}

interface PageResult<T> {
    list: T[]
    total: number
    page: number
    size: number
    totalPages: number
}

/** 列表数据 */
const stockRecords = ref<StockRecordItem[]>([])
const error = ref('')
const searchKeyword = ref('')

/** 分页 */
const page = ref(1)
const size = ref(parseInt(getConfig('page_size')) || 10)
const total = ref(0)
const totalPages = ref(0)
const isSearching = ref(false)

async function loadStockRecords() {
    try {
        const res = await get<PageResult<StockRecordItem>>(
            `/statistics/stock-records?page=${page.value}&size=${size.value}`
        )
        if (res.success && res.data) {
            stockRecords.value = res.data.list || []
            total.value = res.data.total
            totalPages.value = res.data.totalPages
            page.value = res.data.page
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

async function searchStockRecords() {
    error.value = ''
    try {
        const res = await get<PageResult<StockRecordItem>>(
            `/statistics/stock-records/search?value=${encodeURIComponent(searchKeyword.value)}&page=${page.value}&size=${size.value}`
        )
        if (res.success && res.data) {
            stockRecords.value = res.data.list || []
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

async function loadData() {
    if (isSearching.value) {
        await searchStockRecords()
    } else {
        await loadStockRecords()
    }
}

function goToPage(target: number) {
    if (target < 1 || (totalPages.value > 0 && target > totalPages.value)) return
    page.value = target
    loadData()
}

async function handleSearch() {
    page.value = 1
    isSearching.value = searchKeyword.value !== ''
    await loadData()
}

function refresh() {
    page.value = 1
    searchKeyword.value = ''
    isSearching.value = false
    loadData()
}

onMounted(() => {
    loadData()
})

defineExpose({ refresh })
</script>
