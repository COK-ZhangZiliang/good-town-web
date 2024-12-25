<template>
    <el-loading v-if="loading" />
    <template v-else>
        <div class="hot-list">
            <h3 class="hot-title">
                <el-icon>
                    <Histogram />
                </el-icon>
                热度榜
            </h3>
            <ul class="hot-items">
                <li v-for="(item, index) in hotList" :key="index" class="hot-item">
                    <span class="rank" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
                    <span class="title">{{ item.title }}</span>
                    <span class="hot-value">{{ item.hot }}</span>
                </li>
            </ul>
        </div>
    </template>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'
import { Histogram } from '@element-plus/icons-vue'

const publicityData = ref([])
const loading = ref(true)
const hotList = ref([])

// 获取所有宣传信息
const getPopularity = async () => {
    try {
        loading.value = true

        const response = await axios.get('http://10.29.39.146:8088/api/publicity/all')
        if (response.data.status === 'success') {
            console.log(response.data)
            publicityData.value = response.data.data
        }
        else {
            console.error(response.data.message)
        }
    } catch (error) {
        console.error(error)
    }
}

onMounted(async () => {
    try {
        await getPopularity()
        const content = [...publicityData.value].sort((a, b) =>
            (b.assistance_requests?.length || 0) - (a.assistance_requests?.length || 0)
        ).slice(0, 10) // 只显示前10名
        hotList.value = content.map(item => ({
            title: item.title,
            hot: item.assistance_requests?.length || 0
        }))
        console.log(hotList.value)
        loading.value = false
    } catch (error) {
        console.error(error)
        loading.value = false
    }
})
</script>

<style lang="scss">
$max-width: 1200px;
$primary-color: #409EFF;
$border-color: #e6e6e6;
$text-primary: #303133;
$text-secondary: #606266;
$text-light: #909399;
$hot-color: #ff6b6b;

@mixin flex-center {
  display: flex;
  align-items: center;
}

.hot-title {
    @include flex-center;
    gap: 8px;
    margin-bottom: 16px;
    color: $text-primary;
}

.hot-items {
    list-style: none;
    padding: 0;
}

.hot-item {
    @include flex-center;
    padding: 12px 0;
    border-bottom: 1px solid #eee;

    .rank {
        width: 24px;
        height: 24px;
        @include flex-center;
        justify-content: center;
        margin-right: 12px;
        border-radius: 4px;
        background-color: #f0f0f0;
        color: $text-light;

        &.top-three {
            background-color: $primary-color;
            color: white;
        }
    }

    .title {
        flex: 1;
        margin-right: 12px;
        color: $text-primary;
    }

    .hot-value {
        color: $hot-color;
        font-size: 14px;
    }
}
</style>