<template>
    <div class="toolbar">
        <div class="search-box">
            <slot name="prepend"></slot>
            <input :value="modelValue" type="text" :placeholder="placeholder"
                @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
                @keyup.enter="$emit('search')" />
            <button @click="$emit('search')">搜索</button>
            <slot name="append"></slot>
        </div>
        <div class="toolbar-actions" v-if="$slots.actions">
            <slot name="actions"></slot>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    modelValue: string
    placeholder?: string
}>()

defineEmits<{
    (e: 'update:modelValue', value: string): void
    (e: 'search'): void
}>()
</script>
