<template>
  
    <div class="statistics-page">
        <div class="page-header">
            <h2>数据统计</h2>
            <button class="back-btn" @click="router.push('/admin/home')">返回</button>
        </div>

        <div v-if="error" class="error">{{ error }}</div>
        <div v-if="loading" class="loading">数据加载中...</div>

        <template v-else>
            <div class="summary-cards">
                <div class="summary-card card-blue">
                    <div class="summary-icon">📦</div>
                    <div class="summary-body">
                        <div class="summary-label">商品总数</div>
                        <div class="summary-value">{{ summary.totalProducts }}</div>
                        <div class="summary-sub">启用 {{ summary.activeProducts }} / 禁用 {{ summary.inactiveProducts }}
                        </div>
                    </div>
                </div>
                <div class="summary-card card-green">
                    <div class="summary-icon">👥</div>
                    <div class="summary-body">
                        <div class="summary-label">用户总数</div>
                        <div class="summary-value">{{ summary.totalUsers }}</div>
                        <div class="summary-sub">管理员 {{ summary.adminUsers }} / 普通用户 {{ summary.normalUsers }}</div>
                    </div>
                </div>
                <div class="summary-card card-orange">
                    <div class="summary-icon">📈</div>
                    <div class="summary-body">
                        <div class="summary-label">库存总量</div>
                        <div class="summary-value">{{ summary.totalStock }}</div>
                        <div class="summary-sub">库存价值 {{ configStore.configs['currency_symbol'] || '¥' }}{{ formatNum(summary.stockValue) }}</div>
                    </div>
                </div>
                <div class="summary-card card-purple">
                    <div class="summary-icon">💰</div>
                    <div class="summary-body">
                        <div class="summary-label">平均价格</div>
                        <div class="summary-value">{{ configStore.configs['currency_symbol'] || '¥' }}{{ formatNum(summary.avgPrice) }}</div>
                        <div class="summary-sub">最高 {{ configStore.configs['currency_symbol'] || '¥' }}{{ formatNum(summary.maxPrice) }} / 最低 {{ configStore.configs['currency_symbol'] || '¥' }}{{
                            formatNum(summary.minPrice) }}</div>
                    </div>
                </div>
            </div>

            <div class="chart-grid">
                <div class="chart-card">
                    <h3 class="chart-title">商品分类分布</h3>
                    <div class="bar-chart">
                        <div v-for="item in categoryStats" :key="item.categoryId" class="bar-row">
                            <div class="bar-label">{{ item.categoryName }}</div>
                            <div class="bar-track">
                                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }">
                                    <span class="bar-value">{{ item.count }}</span>
                                </div>
                            </div>
                            <div class="bar-percent">{{ item.percent }}%</div>
                        </div>
                        <div v-if="categoryStats.length === 0" class="empty-tip">暂无数据</div>
                    </div>
                </div>

                <div class="chart-card">
                    <h3 class="chart-title">商品状态分布</h3>
                    <div class="donut-wrap">
                        <div class="donut" :style="donutStyle">
                            <div class="donut-hole">
                                <div class="donut-total">{{ summary.totalProducts }}</div>
                                <div class="donut-label">商品</div>
                            </div>
                        </div>
                        <div class="legend">
                            <div class="legend-item">
                                <span class="legend-dot" style="background:#52c41a"></span>
                                <span class="legend-text">启用</span>
                                <span class="legend-count">{{ summary.activeProducts }}</span>
                            </div>
                            <div class="legend-item">
                                <span class="legend-dot" style="background:#f5222d"></span>
                                <span class="legend-text">禁用</span>
                                <span class="legend-count">{{ summary.inactiveProducts }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="chart-card">
                    <h3 class="chart-title">用户角色分布</h3>
                    <div class="progress-list">
                        <div class="progress-row">
                            <div class="progress-head">
                                <span><span class="status-dot" style="background:#667eea"></span>管理员</span>
                                <span>{{ summary.adminUsers }} 人 ({{ adminPercent }}%)</span>
                            </div>
                            <div class="progress-track">
                                <div class="progress-fill"
                                    :style="{ width: adminPercent + '%', background: '#667eea' }"></div>
                            </div>
                        </div>
                        <div class="progress-row">
                            <div class="progress-head">
                                <span><span class="status-dot" style="background:#52c41a"></span>普通用户</span>
                                <span>{{ summary.normalUsers }} 人 ({{ normalPercent }}%)</span>
                            </div>
                            <div class="progress-track">
                                <div class="progress-fill"
                                    :style="{ width: normalPercent + '%', background: '#52c41a' }"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="chart-card">
                    <h3 class="chart-title">价格区间分布</h3>
                    <div class="column-chart">
                        <div v-for="(item, idx) in priceRangeStats" :key="idx" class="column-col">
                            <div class="column-value">{{ item.count }}</div>
                            <div class="column-bar" :style="{ height: item.height + '%', background: item.color }">
                            </div>
                            <div class="column-label">{{ item.label }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <StockRecordTable />
        </template>
    </div>
</template>

<script setup lang="ts">



import { ref, computed, onMounted } from 'vue'
import { get } from '../../utils/request'
import { configStore } from '../../utils/configStore'
import router from '../../route/Router'
import StockRecordTable from '../common/StockRecordTable.vue'

interface Summary {
    totalProducts: number
    activeProducts: number
    inactiveProducts: number
    totalUsers: number
    adminUsers: number
    normalUsers: number
    totalStock: number
    stockValue: number
    avgPrice: number
    maxPrice: number
    minPrice: number
}

interface CategoryStat {
    categoryId: number
    categoryName: string
    count: number
    stock: number
    avgPrice: number
    percent: number
}

interface PriceRangeStat {
    label: string
    rangeKey: string
    count: number
    percent: number
}

interface StatisticsVO {
    summary: Summary
    categoryStats: CategoryStat[]
    priceRangeStats: PriceRangeStat[]
}


const CATEGORY_BAR_COLOR = '#667eea'
const PRICE_COLORS = ['#52c41a', '#667eea', '#faad14', '#13c2c2', '#f5222d']

const EMPTY_SUMMARY: Summary = {
    totalProducts: 0, activeProducts: 0, inactiveProducts: 0,
    totalUsers: 0, adminUsers: 0, normalUsers: 0,
    totalStock: 0, stockValue: 0,
    avgPrice: 0, maxPrice: 0, minPrice: 0,
}

const summary = ref<Summary>({ ...EMPTY_SUMMARY })
const categoryStats = ref<(CategoryStat & { color: string })[]>([])
const priceRangeStats = ref<(PriceRangeStat & { color: string; height: number })[]>([])
const error = ref('')
const loading = ref(true)

/**
 * 数字格式化：千分位 + 两位小数
 */
function formatNum(n: number): string {
    const num = Number(n)
    if (Number.isNaN(num)) return String(n)
    return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
    error.value = ''
    loading.value = true
    try {
        const res = await get<StatisticsVO>('/statistics')
        if (res.success && res.data) {
            summary.value = { ...EMPTY_SUMMARY, ...res.data.summary }
            categoryStats.value = (res.data.categoryStats || []).map((c) => ({
                ...c,
                color: CATEGORY_BAR_COLOR,
            }))
            const priceList = res.data.priceRangeStats || []
            const maxCount = Math.max(...priceList.map(p => p.count), 1)
            priceRangeStats.value = priceList.map((p, i) => ({
                ...p,
                color: PRICE_COLORS[i % PRICE_COLORS.length],
                height: Math.round((p.count / maxCount) * 100),
            }))
        } else {
            error.value = res.message || '加载统计数据失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    } finally {
        loading.value = false
    }
})

const adminPercent = computed(() => {
    const total = summary.value.totalUsers || 1
    return Math.round((summary.value.adminUsers / total) * 100)
})

const normalPercent = computed(() => {
    const total = summary.value.totalUsers || 1
    return Math.round((summary.value.normalUsers / total) * 100)
})

const donutStyle = computed(() => {
    const total = summary.value.totalProducts || 1
    const activePercent = (summary.value.activeProducts / total) * 100
    return {
        background: `conic-gradient(#52c41a 0% ${activePercent}%, #f5222d ${activePercent}% 100%)`,
    }
})
</script>
