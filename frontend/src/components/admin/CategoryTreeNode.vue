<template>
    <div class="tree-node">
        <div class="tree-row" :style="{ paddingLeft: (depth ?? 0) * 20 + 'px' }">
            <span v-if="node.children.length > 0" class="tree-arrow" :class="{ expanded: expanded }"
                @click="expanded = !expanded">▶</span>
            <span v-else class="tree-arrow tree-arrow-empty">▶</span>
            <span class="tree-name">{{ node.category.name }}</span>
            <span class="tree-count">{{ node.children.length }} 个子分类</span>
            <div class="tree-actions">
                <button class="action-btn add-sub" @click="$emit('add-child', node.category)">增加子分类</button>
                <button class="action-btn" @click="$emit('edit', node.category)">编辑</button>
                <button class="action-btn delete" @click="$emit('delete', node.category)">删除</button>
            </div>
        </div>
        <template v-if="expanded && node.children.length > 0">
            <CategoryTreeNode v-for="child in node.children" :key="child.category.id" :node="child" :depth="depth! + 1"
                @add-child="$emit('add-child', $event)"
                @edit="$emit('edit', $event)"
                @delete="$emit('delete', $event)" />
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TreeNode } from '../../utils/category'

defineProps<{
    node: TreeNode
    depth?: number
}>()

defineEmits<{
    (e: 'add-child', category: TreeNode['category']): void
    (e: 'edit', category: TreeNode['category']): void
    (e: 'delete', category: TreeNode['category']): void
}>()

/** 本节点是否展开（每个节点独立管理，默认收起） */
const expanded = ref(false)
</script>
