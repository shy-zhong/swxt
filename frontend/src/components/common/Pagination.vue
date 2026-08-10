<template>
    <div class="pagination">
        <button class="page-btn" :disabled="page <= 1" @click="$emit('change', page - 1)">上一页</button>
        <template v-for="(item, index) in pageItems" :key="index">
            <button v-if="item !== '...'" class="page-btn" :class="{ active: item === page }" @click="$emit('change', Number(item))">{{ item }}</button>
            <span v-else class="page-ellipsis">…</span>
        </template>
        <button class="page-btn" :disabled="page >= totalPages" @click="$emit('change', page + 1)">下一页</button>
        <span class="page-info">
            第 {{ totalPages > 0 ? page : 0 }} / {{ totalPages }} 页，共 {{ total }} 条
        </span>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/** 分页参数 */
const props = defineProps<{
    page: number
    total: number
    totalPages: number
}>()

/** 翻页事件 */
defineEmits<{
    (e: 'change', target: number): void
}>()

const pageItems = computed<(number | string)[]>(() => {
    const pages = Array.from(new Set([1, props.page - 2 , props.page -1, props.page, props.page + 1, props.page + 2 , props.totalPages]))
        .filter(p => p >= 1 && p <= props.totalPages)
        .sort((a, b) => a - b)
    const items: (number | string)[] = []
    let prev = 0
    for (const p of pages) {
        if (p - prev > 1) items.push('...')
        items.push(p)
        prev = p
    }
    return items
})
</script>
