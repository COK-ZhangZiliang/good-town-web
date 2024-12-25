<!-- 展示所有宣传信息 -->
<template>
    <div>
        <div class="search">
            <el-input v-model="searchQuery" placeholder="搜索..." prefix-icon="Search" clearable
                @keyup.enter="handleSearch">
                <template #append>
                    <el-button @click="handleSearch">搜索</el-button>
                </template>
            </el-input>
        </div>
        <el-loading v-if="loading" />
        <MultipageComp :data="publicityData" :username="props.username" :type="props.type" @refresh="getAllPublicity" />
    </div>
</template>

<script setup>
import axios from 'axios'
import MultipageComp from '@/components/MultipageComp.vue'
import { ref, onMounted, defineProps } from 'vue'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

// 接收数据
const props = defineProps({
    username: {
        type: String,
        required: false
    },
    type: {
        type: String,
        required: true
    }
})

const publicityData = ref([])
const loading = ref(true)
const searchQuery = ref('')

// 获取所有宣传信息
const getAllPublicity = async () => {
    if (props.type === "allPromotions" || props.type === "adminQuery") {
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
        } finally {
            loading.value = false
        }
    }

    else if (props.type === "myPromotions") {
        try {
            loading.value = true
            const token = getToken()
            const response = await axios.get('http://10.29.39.146:8088/api/publicity/my', { headers: { token: token } })
            if (response.data.status === 'success') {
                console.log(response.data)
                publicityData.value = response.data.data
            }
            else {
                console.error(response.data.message)
            }
        } catch (error) {
            console.error(error)
        } finally {
            loading.value = false
        }
    }
}

onMounted(() => {
    getAllPublicity()
})

// 处理搜索
const handleSearch = async () => {
    if (props.type === "allPromotions" || props.type === "adminQuery") {
        try {
            const response = await axios.post('http://10.29.39.146:8088/api/publicity/search', { keyword: searchQuery.value })
            if (response.data.status === 'success') {
                console.log(response.data)
                publicityData.value = response.data.data
            }
            else {
                ElMessage.error(response.data.message)
            }
        } catch (error) {
            console.error(error)
        }
    }
    else if (props.type === "myPromotions") {
        try {
            const response = await axios.post('http://10.29.39.146:8088/api/publicity/search/my', {
                token: getToken(),
                keyword: searchQuery.value
            })
            if (response.data.status === 'success') {
                console.log(response.data)
                publicityData.value = response.data.data
            }
            else {
                ElMessage.error(response.data.message)
            }
        } catch (error) {
            console.error(error)
        }
    }

}
</script>

<style scoped>
.search {
    flex: 1;
    max-width: 500px;
    margin: 20px auto;
}
</style>