<!-- 发布宣传对话框 -->
<template>
    <el-dialog v-model="dialogVisible" title="发布宣传信息" width="600px" :close-on-click-modal="false"
        custom-class="promotion-dialog">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="promotion-form">
            <el-form-item label="省份" prop="province">
                <el-select v-model="formData.province" @change="handleProvinceChange">
                    <el-option v-for="item in provinces" :key="item" :label="item" :value="item" />
                </el-select>
            </el-form-item>

            <el-form-item label="城市" prop="city">
                <el-select v-model="formData.city">
                    <el-option v-for="item in cities" :key="item" :label="item" :value="item" />
                </el-select>
            </el-form-item>

            <el-form-item label="乡镇名" prop="town">
                <el-input v-model="formData.town" placeholder="请输入乡镇名称" />
            </el-form-item>

            <el-form-item label="宣传类型" prop="type">
                <el-select v-model="formData.type">
                    <el-option label="农家院" value="farmhouse" />
                    <el-option label="自然风光" value="nature" />
                    <el-option label="古建筑" value="ancient" />
                    <el-option label="土特产" value="specialty" />
                    <el-option label="特色小吃" value="food" />
                    <el-option label="民俗活动" value="folk" />
                </el-select>
            </el-form-item>

            <el-form-item label="宣传主题" prop="title">
                <el-input v-model="formData.title" placeholder="请输入宣传主题" />
            </el-form-item>

            <el-form-item label="宣传描述" prop="description">
                <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入宣传描述" />
            </el-form-item>

            <el-form-item label="上传图片">
                <el-upload v-model:file-list="formData.images" list-type="picture-card" :on-preview="handleImagePreview"
                    :before-upload="beforeImageUpload" accept="image/*" :auto-upload="false" multiple>
                    <el-icon>
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item label="上传视频">
                <el-upload v-model:file-list="formData.videos" :before-upload="beforeVideoUpload" accept="video/*"
                    :auto-upload="false" multiple>
                    <el-button type="primary">选择视频</el-button>
                </el-upload>
            </el-form-item>
        </el-form>

        <template #footer>
            <div class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="handleSubmit">发 布</el-button>
            </div>
        </template>
    </el-dialog>

    <el-dialog v-model="previewVisible">
        <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
</template>

<script setup>
import { ref, reactive, defineProps, defineEmits, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import axios from 'axios'
import pcData from '@/assets/pc.json'

const formRef = ref(null)

const dialogImageUrl = ref('')
const previewVisible = ref(false)

const formData = reactive({
    province: '',
    city: '',
    town: '',
    type: '',
    title: '',
    description: '',
    images: [],
    videos: []
})

const rules = {
    province: [{ required: true, message: '请选择省份', trigger: 'change' }],
    city: [{ required: true, message: '请选择城市', trigger: 'change' }],
    town: [{ required: true, message: '请输入乡镇名', trigger: 'blur' }],
    type: [{ required: true, message: '请选择宣传类型', trigger: 'change' }],
    title: [{ required: true, message: '请输入宣传主题', trigger: 'blur' }],
    description: [{ required: true, message: '请输入宣传描述', trigger: 'blur' }]
}

const provinces = Object.keys(pcData)
const cities = ref([])

const handleProvinceChange = (value) => {
    cities.value = pcData[value] || []
}

const handleSubmit = async () => {
    if (!formRef.value) return
    const token = getToken()
    const imageUrls = []
    const videoUrls = []

    try {
        await formRef.value.validate()

        for (let i = 0; i < formData.images.length; i++) {
            const file = formData.images[i]
            const data = new FormData()
            data.append('file', file.raw)
            data.append('token', token)
            try {
                const response = await axios.post('http://10.29.39.146:8088/api/files/upload', data)
                if (response.data.status === 'success') {
                    imageUrls.push(response.data.file_url)
                    console.log(response.data)
                }
                else {
                    ElMessage.error(response.data.message)
                    console.error(response.data)
                    return
                }
            } catch (error) {
                ElMessage.error('图片上传失败，请稍后再试')
                console.error(error)
                return
            }
        }

        for (let i = 0; i < formData.videos.length; i++) {
            const file = formData.videos[i]
            const data = new FormData()
            data.append('file', file.raw)
            data.append('token', token)
            try {
                const response = await axios.post('http://10.29.39.146:8088/api/files/upload', data)
                if (response.data.status === 'success') {
                    videoUrls.push(response.data.file_url)
                    console.log(response.data)
                }
                else {
                    ElMessage.error(response.data.message)
                    console.error(response.data)
                    return
                }
            } catch (error) {
                ElMessage.error('视频上传失败，请稍后再试')
                console.error(error)
                return
            }

            try {
                const response = await axios.post('http://10.29.39.146:8088/api/publicity/create', {
                    token: token,
                    province: formData.province,
                    city: formData.city,
                    townName: formData.town,
                    type: formData.type,
                    title: formData.title,
                    description: formData.description,
                    image_url: imageUrls.join(','),
                    video_url: videoUrls.join(',')
                })
                if (response.data.status === 'success') {
                    ElMessage.success('发布成功')
                    console.log(response.data)
                    dialogVisible.value = false
                }
                else {
                    ElMessage.error(response.data.message)
                    console.error(response.data)
                }
            } catch (error) {
                ElMessage.error('发布失败，请稍后再试')
                console.error(error)
            }
        }

    } catch (error) {
        ElMessage.error('请求失败，请稍后再试')
        console.error(error)
    }
}

const beforeImageUpload = (file) => {
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
        ElMessage.error('上传图片大小不能超过 5MB!')
    }
}

const beforeVideoUpload = (file) => {
    const isLt100M = file.size / 1024 / 1024 < 100
    if (!isLt100M) {
        ElMessage.error('上传视频大小不能超过 100MB!')
    }
}

const handleImagePreview = (uploadFile) => {
    dialogImageUrl.value = uploadFile.url
    previewVisible.value = true
}

const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => {
        emit('update:visible', value)
    }
})
</script>