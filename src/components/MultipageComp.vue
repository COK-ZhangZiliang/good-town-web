<!-- 分页展示容器 -->
<template>
    <div class="multipage-container">
        <div class="items-container">
            <template v-if="props.type === 'myPromotions' || props.type === 'allPromotions' || props.type === 'adminQuery'">
                <PublicityItem v-for="item in currentPageItems"
                    :key="`publicity-${item.id}`" :content="item" :username="props.username" :type="props.type"
                    @refresh="fetchData" />
            </template>
            <template v-else>
                <AssistanceItem v-for="item in currentPageItems" :key="`assistance-${item.id}`" :content="item"
                    @refresh="fetchData" :type="props.type" />
            </template>
        </div>
        <div class="pagination-wrapper">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="itemsPerPage" :background="background"
                layout="prev, pager, next, jumper" :total="items.length" @current-change="handleCurrentChange" />
        </div>
    </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'
import PublicityItem from '@/components/PublicityItem.vue'
import AssistanceItem from './AssistanceItem.vue'

// 接收数据
const props = defineProps(
    {
        data: {
            type: Array,
            required: true
        },
        username: {
            type: String,
            required: true
        },
        type: {
            type: String,
            required: true
        }
    }
)

const emits = defineEmits(['refresh'])

const items = computed(() => props.data)

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

const fetchData = () => {
    emits('refresh')
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