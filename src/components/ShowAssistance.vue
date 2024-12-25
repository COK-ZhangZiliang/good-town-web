<!-- 展示所有助力信息 -->
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
        <MultipageComp :data="assistanceData" :type="props.type" @refresh="getAllAssistance" />
    </div>
</template>

<script setup>
import axios from 'axios'
import MultipageComp from '@/components/MultipageComp.vue'
import { ref, onMounted, defineProps } from 'vue'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const assistanceData = ref([])
const loading = ref(true)
const searchQuery = ref('')

const props = defineProps({
    type: {
        type: String,
        default: 'myAssistance'
    }
})

// 获取所有助力信息
const getAllAssistance = async () => {
    if (props.type === 'myAssistance') {
        try {
            loading.value = true

            const response = await axios.get('http://10.29.39.146:8088/api/assistance/my', { headers: { token: getToken() } })
            if (response.data.status === 'success') {
                console.log(response.data)
                assistanceData.value = response.data.data
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
    else if (props.type === 'allAssistance') {
        try {
            loading.value = true

            const response = await axios.get('http://10.29.39.146:8088/api/assistance/all', { headers: { token: getToken() } })
            if (response.data.status === 'success') {
                console.log(response.data)
                assistanceData.value = response.data.data
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
    getAllAssistance()
})

// 处理搜索
const handleSearch = async () => {
    if (props.type === 'myAssistance') {
        try {
            const response = await axios.post('http://10.29.39.146:8088/api/assistance/search', {
                token: getToken(),
                keyword: searchQuery.value
            })
            if (response.data.status === 'success') {
                console.log(response.data)
                assistanceData.value = response.data.data
            }
            else {
                ElMessage.error(response.data.message)
            }
        } catch (error) {
            console.error(error)
        }
    }
    else if (props.type === 'allAssistance') try {
        const response = await axios.post('http://10.29.39.146:8088/api/assistance/search/all', {
            keyword: searchQuery.value
        }, {
            headers: { token: getToken() }
        })
        if (response.data.status === 'success') {
            console.log(response.data)
            assistanceData.value = response.data.data
        }
        else {
            ElMessage.error(response.data.message)
        }
    } catch (error) {
        console.error(error)
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