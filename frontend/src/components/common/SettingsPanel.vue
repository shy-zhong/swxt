<template>
    <div class="settings-page">
        <div class="page-header">
            <h2>{{ title }}</h2>
            <button class="back-btn" @click="router.push(backPath)">返回</button>
        </div>

        <div v-if="error" class="error">{{ error }}</div>
        <div v-if="successMsg" class="success-tip">{{ successMsg }}</div>

        <div class="group-tabs">
            <button v-for="group in groups" :key="group.title" class="group-tab"
                :class="{ active: activeGroup === group.title }" @click="activeGroup = group.title">
                <span class="group-icon">{{ group.icon }}</span>
                {{ group.title }}
            </button>
        </div>

        <div v-for="group in groups" :key="group.title" v-show="activeGroup === group.title"
            class="chart-card settings-group">
            <div class="settings-list">
                <div v-for="item in group.items" :key="item.id" class="settings-row">
                    <div class="settings-label">
                        <div class="settings-key">{{ item.configKey }}</div>
                        <div class="settings-desc">{{ item.description }}</div>
                    </div>
                    <div class="settings-control">
                        <input v-if="item.type === 'text' || item.type === 'number'" :type="item.type"
                            v-model="item.configValue" class="settings-input" :disabled="readonly" />
                        <label v-else-if="item.type === 'switch'" class="switch">
                            <input type="checkbox" v-model="item.configValue" true-value="true" false-value="false"
                                :disabled="readonly" />
                            <span class="switch-slider"></span>
                            <span class="switch-text">{{ item.configValue === 'true' ? '已启用' : '已禁用' }}</span>
                        </label>
                    </div>
                </div>
                <div v-if="group.items.length === 0" class="settings-row">
                    <div class="settings-label">
                        <div class="settings-key">暂无配置项</div>
                    </div>
                </div>
            </div>
            <div v-if="!readonly" class="group-actions">
                <button @click="saveGroup(group)">保存本组设置</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { get, put } from '../../utils/request'
import router from '../../route/Router'
import { loadConfig } from '../../utils/configStore'

interface SystemConfig {
    id: number
    configKey: string
    configValue: string
    description: string
    type: 'text' | 'number' | 'switch'
}

interface SettingsGroup {
    title: string
    icon: string
    items: SystemConfig[]
}

const props = withDefaults(defineProps<{
    title: string
    backPath: string
    readonly?: boolean
}>(), {
    readonly: false
})

const CATEGORY_MAP: Record<string, { title: string; type: 'text' | 'number' | 'switch' }> = {
    site_name: { title: '站点信息', type: 'text' },
    site_description: { title: '站点信息', type: 'text' },
    contact_phone: { title: '站点信息', type: 'text' },
    contact_email: { title: '站点信息', type: 'text' },
    currency_symbol: { title: '站点信息', type: 'text' },
    homepage_background: { title: '站点信息', type: 'text' },
    page_size: { title: '站点信息', type: 'number' },
    allow_register: { title: '安全设置', type: 'switch' },
    default_user_role: { title: '安全设置', type: 'text' },
    password_min_length: { title: '安全设置', type: 'number' },
    enable_log: { title: '安全设置', type: 'switch' },
    low_stock_threshold: { title: '库存设置', type: 'number' },
    enable_stock_warning: { title: '库存设置', type: 'switch' },
    default_product_status: { title: '商品设置', type: 'text' },
}

const groups = ref<SettingsGroup[]>([
    { title: '站点信息', icon: '🌐', items: [] },
    { title: '安全设置', icon: '🔒', items: [] },
    { title: '库存设置', icon: '📦', items: [] },
    { title: '商品设置', icon: '🛍️', items: [] }
])

const activeGroup = ref<string>(groups.value[0].title)
const error = ref('')
const successMsg = ref('')

onMounted(async () => {
    error.value = ''
    try {
        const res = await get<SystemConfig[]>('/system-config')
        if (res.success) {
            res.data.forEach(item => {
                if (CATEGORY_MAP[item.configKey]) {
                    const targetGroup = groups.value.find(g => g.title === CATEGORY_MAP[item.configKey].title)
                    if (targetGroup) {
                        item.type = CATEGORY_MAP[item.configKey].type
                        targetGroup.items.push(item)
                    }
                }
            })
        } else {
            error.value = res.message || '加载系统设置失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
})

/**
 * 保存某一组的全部配置项到后端（仅可编辑模式可用）
 */
async function saveGroup(group: SettingsGroup) {
    error.value = ''
    successMsg.value = ''
    try {
        const res = await put<string[]>('/system-config', group.items)
        if (res.success) {
            successMsg.value = `${group.title} 保存成功`
            loadConfig()
        } else {
            const failedKeys = res.data && res.data.length > 0 ? `（失败项：${res.data.join(', ')}）` : ''
            error.value = `${res.message || '保存失败'}${failedKeys}`
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}
</script>
