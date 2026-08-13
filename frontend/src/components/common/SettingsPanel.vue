<template>
    <div class="settings-page">
        <div class="page-header">
            <h2>{{ title }}</h2>
            <button class="back-btn" @click="router.back">返回</button>
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
                        <template v-if="item.type === 'text' || item.type === 'number'">
                            <input :type="item.type" v-model="item.configValue" class="settings-input"
                                :disabled="readonly" />
                        </template>
                        <template v-else-if="item.type === 'switch'">
                            <label class="switch">
                                <input type="checkbox" v-model="item.configValue" true-value="true"
                                    false-value="false" :disabled="readonly" />
                                <span class="switch-slider"></span>
                                <span class="switch-text">{{ item.configValue === 'true' ? '已启用' : '已禁用' }}</span>
                            </label>
                        </template>
                        <template v-else-if="item.type === 'image'">
                            <div class="settings-image-upload">
                                <input type="file" accept="image/png,image/jpeg,image/gif,image/webp"
                                    @change="(e) => handleImageUpload(e, item)" :disabled="readonly" />
                                <div v-if="item.configValue" class="settings-image-preview">
                                    <img :src="normalizeImage(item.configValue)" alt="预览" />
                                </div>
                                <div v-else class="settings-image-placeholder">暂无图片</div>
                            </div>
                        </template>
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
import { get, put, upload } from '../../utils/request'
import router from '../../route/Router'
import { loadConfig } from '../../utils/configStore'

interface SystemConfig {
    id: number
    configKey: string
    configValue: string
    description: string
    type: 'text' | 'number' | 'switch' | 'image'
}

interface SettingsGroup {
    title: string
    icon: string
    items: SystemConfig[]
}

const props = withDefaults(defineProps<{
    title: string
    readonly?: boolean
}>(), {
    readonly: false
})

/** 配置项分组映射 */
const CATEGORY_MAP: Record<string, { title: string; type: 'text' | 'number' | 'switch' | 'image' }> = {
    site_name: { title: '站点信息', type: 'text' },
    site_description: { title: '站点信息', type: 'text' },
    contact_phone: { title: '站点信息', type: 'text' },
    contact_email: { title: '站点信息', type: 'text' },
    currency_symbol: { title: '站点信息', type: 'text' },
    homepage_background: { title: '站点信息', type: 'image' },
    page_size: { title: '站点信息', type: 'number' },
    allow_register: { title: '安全设置', type: 'switch' },
    default_user_role: { title: '安全设置', type: 'text' },
    password_min_length: { title: '安全设置', type: 'number' },
    enable_log: { title: '安全设置', type: 'switch' },
    low_stock_threshold: { title: '库存设置', type: 'number' },
    enable_stock_warning: { title: '库存设置', type: 'switch' },
    default_product_status: { title: '商品设置', type: 'text' },
    show_disabled_products: { title: '商品设置', type: 'switch' },
}

/** 分组与状态 */
const groups = ref<SettingsGroup[]>([
    { title: '站点信息', icon: '🌐', items: [] },
    { title: '安全设置', icon: '🔒', items: [] },
    { title: '库存设置', icon: '📦', items: [] },
    { title: '商品设置', icon: '🛍️', items: [] }
])

const activeGroup = ref<string>(groups.value[0].title)
const error = ref('')
const successMsg = ref('')

/** 待上传的图片文件：key 为配置项 id，保存时才上传，避免放弃修改产生孤儿文件 */
const pendingFiles: Record<number, File> = {}

function normalizeImage(src: string): string {
    if (!src) return ''
    if (/^https?:/i.test(src) || /^blob:/i.test(src)) return src
    if (/^\//.test(src)) return src
    return '/' + src
}

/**
 * 图片选择处理
 */
function handleImageUpload(e: Event, item: SystemConfig) {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return

    error.value = ''
    successMsg.value = ''
    // 释放上一张未保存的本地预览
    if (pendingFiles[item.id] && /^blob:/i.test(item.configValue)) {
        URL.revokeObjectURL(item.configValue)
    }
    pendingFiles[item.id] = file
    item.configValue = URL.createObjectURL(file)
    successMsg.value = `${item.configKey} 已选择图片，请点击「保存本组设置」上传`
    input.value = ''
}

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
 * 保存某一组配置：先上传本组待传图片，再提交配置到后端
 */
async function saveGroup(group: SettingsGroup) {
    error.value = ''
    successMsg.value = ''
    // 第一步：上传本组中已选择但未上传的图片，拿到真实 URL 回填
    for (const item of group.items) {
        if (!pendingFiles[item.id]) continue
        try {
            const formData = new FormData()
            formData.append('file', pendingFiles[item.id])
            formData.append('type', 'config')
            const res = await upload<string>('/upload', formData)
            if (!res.success || !res.data) {
                error.value = `${item.configKey} 图片上传失败：${res.message || '未知错误'}`
                return
            }
            URL.revokeObjectURL(item.configValue)
            item.configValue = res.data
            delete pendingFiles[item.id]
        } catch (err) {
            error.value = err instanceof Error ? err.message : '网络错误'
            return
        }
    }
    // 第二步：提交本组配置
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
