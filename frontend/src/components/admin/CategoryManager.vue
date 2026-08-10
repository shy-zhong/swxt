<template>
    <div class="edit-panel">
        <div v-if="error" class="error">{{ error }}</div>

        <h3>分类管理</h3>

        <div class="category-tree">
            <CategoryTreeNode v-for="node in tree" :key="node.category.id" :node="node" :depth="0"
                @add-child="openSubCategoryCreate" @edit="openCategoryEdit" @delete="deleteCategory" />
            <div v-if="tree.length === 0" class="empty-row">暂无分类，点击下方按钮新增</div>
        </div>

        <ModalPanel :visible="categoryEditing" :title="categoryForm.id ? '编辑分类' : '新增分类'"
            @close="categoryEditing = false">
            <div class="form-row">
                <label>分类名称</label>
                <input v-model="categoryForm.name" type="text" />
            </div>
            <div class="form-row">
                <label>父分类</label>
                <select v-model.number="categoryForm.parentId">
                    <option :value="0">无（作为顶级分类）</option>
                    <option v-for="opt in parentOptions" :key="opt.id" :value="opt.id">{{ opt.path }}</option>
                </select>
            </div>
            <div class="form-row">
                <label>排序号</label>
                <input v-model.number="categoryForm.sortOrder" type="number" />
            </div>
            <template #actions>
                <button @click="saveCategory">确认</button>
                <button class="cancel-btn" @click="categoryEditing = false">取消</button>
            </template>
        </ModalPanel>

        <div class="form-actions panel-actions">
            <button @click="openCategoryCreate">新增顶级分类</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { get, post, put, del } from '../../utils/request'
import ModalPanel from '../common/ModalPanel.vue'
import CategoryTreeNode from './CategoryTreeNode.vue'
import { buildTree, buildPathMap, collectDescendantIds } from '../../utils/category'
import type { Category } from '../../utils/category'

const categories = ref<Category[]>([])
const error = ref('')

const categoryEditing = ref(false)
const categoryForm = ref<{ id?: number; name: string; parentId: number; sortOrder: number }>({
    name: '',
    parentId: 0,
    sortOrder: 0,
})

const tree = computed(() => buildTree(categories.value))
const pathMap = computed(() => buildPathMap(categories.value))

/**
 * 父分类下拉选项：按路径排序；
 */
const parentOptions = computed(() => {
    const list = categories.value
        .map(c => ({ id: c.id, path: pathMap.value.get(c.id) || c.name }))
    if (!categoryForm.value.id) return list
    const excluded = collectDescendantIds(categories.value, categoryForm.value.id)
    return list.filter(o => !excluded.has(o.id))
})

async function loadCategories() {
    error.value = ''
    try {
        const res = await get<Category[]>('/categories')
        if (res.success) {
            categories.value = res.data || []
        } else {
            error.value = res.message || '加载分类失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

function openCategoryCreate() {
    categoryForm.value = { name: '', parentId: 0, sortOrder: 0 }
    categoryEditing.value = true
}

function openSubCategoryCreate(parent: Category) {
    categoryForm.value = { name: '', parentId: parent.id, sortOrder: 0 }
    categoryEditing.value = true
}

function openCategoryEdit(category: Category) {
    categoryForm.value = {...category}
    categoryEditing.value = true
}

async function saveCategory() {
    error.value = ''
    try {
        if (categoryForm.value.id) {
            const res = await put<{ code: number; message: string }>('/categories', {...categoryForm.value})
            if (!res.success) {
                error.value = res.message || '保存分类失败'
                return
            }
        } else {
            const res = await post<{ code: number; message: string }>('/categories',{...categoryForm.value})
            if (!res.success) {
                error.value = res.message || '保存分类失败'
                return
            }
        }
        categoryEditing.value = false
        await loadCategories()
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

async function deleteCategory(category: Category) {
    const descendantCount = collectDescendantIds(categories.value, category.id).size
    const hint = descendantCount > 0
        ? `将级联删除其下 ${descendantCount} 个子分类`
        : '该分类无子分类'
    if (!confirm(`确定删除分类 ${category.name}吗?\n${hint}。`)) return
    error.value = ''
    try {
        const res = await del<{ code: number; message: string }>(`/categories/${category.id}`)
        if (res.success) {
            await loadCategories()
        } else {
            error.value = res.message || '删除分类失败'
        }
    } catch (e) {
        error.value = e instanceof Error ? e.message : '网络错误'
    }
}

onMounted(() => {
    loadCategories()
})
</script>
