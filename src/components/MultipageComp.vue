<!-- 分页展示容器 -->
<template>
    <div class="multipage-container">
        <div class="items-container">
            <PublictiyItem v-for="item in currentPageItems" :key="item.id" 
                :userAvatar="item.userAvatar" 
                :username="item.username" 
                :title="item.title" 
                :description="item.description" 
                :media="item.media" />
        </div>
        <div class="pagination">
            <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
                上一页
            </button>
            <span>{{ currentPage }} / {{ totalPages }}</span>
            <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
                下一页
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import PublictiyItem from '@/components/PublictiyItem.vue'

const items = ref([])
const currentPage = ref(1)
const itemsPerPage = 5

const totalPages = computed(() => {
    return Math.ceil(items.value.length / itemsPerPage)
})

const currentPageItems = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    const end = start + itemsPerPage
    return items.value.slice(start, end)
})

function changePage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
    }
}
</script>

<style lang="scss" scoped>
.multipage-container {
    padding: 20px;

    .items-container {
        margin-bottom: 20px;

        .item {
            padding: 10px;
            border-bottom: 1px solid #eee;
        }
    }

    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;

        button {
            padding: 5px 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: pointer;

            &:disabled {
                cursor: not-allowed;
                opacity: 0.6;
            }
        }
    }
}
</style>