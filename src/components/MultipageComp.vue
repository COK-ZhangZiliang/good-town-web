<!-- 分页展示容器 -->
<template>
    <div class="multipage-container">
            <div class="items-container">
                <PublictiyItem v-for="item in currentPageItems" :key="item.id" :title="item.title"
                    :description="item.description" />
            </div>
            <div class="pagination-wrapper">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="itemsPerPage"
                    :background="background" layout="prev, pager, next, jumper" :total="items.length"
                    @current-change="handleCurrentChange" />
            </div>
    </div>
</template>

<script setup>
import { ref, computed, defineProps } from 'vue'
import PublictiyItem from '@/components/PublictiyItem.vue'

// 接收数据
const props = defineProps(
    {
        publicityData: {
            type: Array,
            required: true
        }
    }
)
const items = computed(() => props.publicityData)

const currentPage = ref(1)
const itemsPerPage = 5

// 获取当前页面的items
const currentPageItems = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    const end = start + itemsPerPage
    return (items.value || []).slice(start, end)
})

// 跳转页面
const handleCurrentChange = (val) => {
    currentPage.value = val
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