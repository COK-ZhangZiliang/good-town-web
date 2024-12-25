<!-- 展示所有助力信息 -->
<template>
    <div>
        <el-loading v-if="loading" />
        <MultipageComp :data="assistanceData" :type="'myAssistance'" @refresh="getAllAssistance" />
    </div>
</template>

<script setup>
import axios from 'axios'
import MultipageComp from '@/components/MultipageComp.vue'
import { ref, onMounted } from 'vue'
import { getToken } from '@/utils/auth'

const assistanceData = ref([])
const loading = ref(true)

// 获取所有助力信息
const getAllAssistance = async () => {
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

onMounted(() => {
    getAllAssistance()
})
</script>