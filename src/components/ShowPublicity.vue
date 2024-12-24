<!-- 展示所有宣传信息 -->
<template>
    <div>
        <el-loading v-if="loading" />
        <MultipageComp :publicity-data="publicityData" :username="props.username" :type="props.type"/>
    </div>
</template>

<script setup>
import axios from 'axios'
import MultipageComp from '@/components/MultipageComp.vue'
import { ref, onMounted, defineProps } from 'vue'
import { getToken } from '@/utils/auth'

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

// 获取所有宣传信息
const getAllPublicity = async () => {
    console.log(props.type)
    if (props.type === "allPromotions") {
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
            console.log(1)
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
</script>