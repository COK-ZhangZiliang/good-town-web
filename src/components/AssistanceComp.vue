<!-- 我助力组件，展示所有宣传信息 -->
<template>
    <div>
        <el-loading v-if="loading" />
        <MultipageComp :publicity-data="publicityData" />
    </div>
</template>

<script setup>
import axios from 'axios'
import MultipageComp from '@/components/MultipageComp.vue'
import { ref, onMounted } from 'vue';

const publicityData = ref([])
const loading = ref(true)

// 获取所有宣传信息
const getAllPublicity = async () => {
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

onMounted(() => {
    getAllPublicity()
})
</script>