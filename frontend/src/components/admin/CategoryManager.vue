<template>
    <div class="edit-panel">
        <div v-if="error" class="error">{{ error }}</div>

        <h3>分类管理</h3>
        <div class="accordion">
            <div v-for="parent in parentCategories" :key="parent.id" class="accordion-item">
                <div class="accordion-header" @click="toggleParent(parent.id)">
                    <span class="accordion-arrow" :class="{ expanded: expandedParent === parent.id }">▶</span>
                    <span class="accordion-title">{{ parent.name }}</span>
                    <span class="accordion-count">{{ getChildCategories(parent.id).length }} 个子分类</span>
                    <div class="accordion-header-actions">
                        <button class="action-btn" @click.stop="openCategoryEdit(parent)">编辑</button>
                        <button class="action-btn delete" @click.stop="deleteCategory(parent)">删除</button>
                        <button class="action-btn add-sub" @click.stop="openSubCategoryCreate(parent.id)">+ 子分类</button>
                    </div>
                </div>
                <div v-if="expandedParent === parent.id" class="accordion-body">
                    <table class="product-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>子分类名称</th>
                                <th>排序号</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="child in getChildCategories(parent.id)" :key="child.id">
                                <td>{{ child.id }}</td>
                                <td>{{ child.name }}</td>
                                <td>{{ child.sortOrder }}</td>
                                <td>
                                    <button class="action-btn" @click="openCategoryEdit(child)">编辑</button>
                                    <button class="action-btn delete" @click="deleteCategory(child)">删除</button>
                                </td>
                            </tr>
                            <tr v-if="getChildCategories(parent.id).length === 0">
                                <td colspan="4" class="empty-row">暂无子分类</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <ModalPanel :visible="categoryEditing" :title="categoryForm.id ? '编辑分类' : '新增分类'" @close="categoryEditing = false">
            <div class="form-row">
                <label>分类名称</label>
                <input v-model="categoryForm.name" type="text" />
            </div>
            <div class="form-row">
                <label>父分类</label>
                <select v-model.number="categoryForm.parentId">
                    <option :value="0">无（作为父分类）</option>
                    <option v-for="parent in parentCategories" :key="parent.id" :value="parent.id">{{ parent.name }}
                    </option>
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
            <button @click="openCategoryCreate">新增父分类</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { get, post, put, del } from '../../utils/request'
import ModalPanel from '../common/ModalPanel.vue'

interface Category {
    id: number
    name: string
    parentId: number
    sortOrder: number
}

const emit = defineEmits<{
    (e: 'close'): void
}>()

/** 列表数据 */
const categories = ref<Category[]>([])
const error = ref('')

/** 表单状态 */
const categoryEditing = ref(false)
const expandedParent = ref<number | null>(null)
const categoryForm = ref<{ id?: number; name: string; parentId: number; sortOrder: number }>({
    name: '',
    parentId: 0,
    sortOrder: 0,
})

/** 父分类列表（按排序号升序） */
const parentCategories = computed(() =>
    categories.value.filter(c => c.parentId === 0).sort((a, b) => a.sortOrder - b.sortOrder)
)

function getChildCategories(parentId: number): Category[] {
    return categories.value.filter(c => c.parentId === parentId).sort((a, b) => a.sortOrder - b.sortOrder)
}

function toggleParent(id: number) {
    expandedParent.value = expandedParent.value === id ? null : id
}

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

function openSubCategoryCreate(parentId: number) {
    categoryEditing.value = true
    categoryForm.value = { name: '', parentId, sortOrder: 0 }
}

function openCategoryEdit(category: Category) {
    categoryForm.value = { id: category.id, name: category.name, parentId: category.parentId, sortOrder: category.sortOrder }
    categoryEditing.value = true
}

async function saveCategory() {
    error.value = ''
    try {
        if (categoryForm.value.id) {
            const res = await put<{ code: number; message: string }>('/categories', {
                id: categoryForm.value.id,
                name: categoryForm.value.name,
                parentId: categoryForm.value.parentId,
                sortOrder: categoryForm.value.sortOrder,
            })
            if (!res.success) {
                error.value = res.message || '保存分类失败'
                return
            }
        } else {
            const res = await post<{ code: number; message: string }>('/categories', {
                name: categoryForm.value.name,
                parentId: categoryForm.value.parentId,
                sortOrder: categoryForm.value.sortOrder,
            })
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
    if (!confirm(`确定删除分类 ${category.name} 吗？`)) return
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
