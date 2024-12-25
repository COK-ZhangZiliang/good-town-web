<!-- 发布宣传对话框 -->
<template>
    <el-dialog v-model="dialogVisible" :title="!!props.editData ? '修改宣传信息' : '发布宣传信息'" width="600px"
        :close-on-click-modal="false" custom-class="promotion-dialog">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="promotion-form">
            <el-form-item label="省份" prop="province">
                <el-select v-model="formData.province" @change="handleProvinceChange" :disabled="!!props.editData">
                    <el-option v-for="item in provinces" :key="item" :label="item" :value="item" />
                </el-select>
            </el-form-item>

            <el-form-item label="城市" prop="city">
                <el-select v-model="formData.city" :disabled="!!props.editData">
                    <el-option v-for="item in cities" :key="item" :label="item" :value="item" />
                </el-select>
            </el-form-item>

            <el-form-item label="乡镇名" prop="town">
                <el-input v-model="formData.town" placeholder="请输入乡镇名称" :disabled="!!props.editData" />
            </el-form-item>

            <el-form-item label="宣传类型" prop="type">
                <el-select v-model="formData.type" :disabled="!!props.editData">
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
                <el-button @click="cancelSubmit">取 消</el-button>
                <el-button type="primary" @click="handleSubmit">发 布</el-button>
            </div>
        </template>
    </el-dialog>

    <el-dialog v-model="previewVisible">
        <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
</template>

<script setup>
import { ref, reactive, defineProps, defineEmits, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import axios from 'axios'
import pcData from '@/assets/pc.json'
import { uploadFiles } from '@/utils/upload'

const formRef = ref(null)

const dialogImageUrl = ref('')
const previewVisible = ref(false)

// 接收变量
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    editData: {
        type: Object,
        required: false
    }
})

const emits = defineEmits(['update:visible'], ['success'])

const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => {
        emits('update:visible', value)
    }
})

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

// 处理提交
const handleSubmit = async () => {
    if (!formRef.value) return
    const token = getToken()

    try {
        await formRef.value.validate();
    } catch (error) {
        return
    }

    try {
        const imageUrls = await uploadFiles(formData.images, token)
        const videoUrls = await uploadFiles(formData.videos, token)

        if (props.editData) {
            try {
                const response = await axios.put(`http://10.29.39.146:8088/api/publicity/update/${props.editData.publicity_id}`, {
                    title: formData.title,
                    description: formData.description,
                    image_url: imageUrls,
                    video_url: videoUrls
                },
                    {
                        headers: {
                            token: token
                        }
                    }
                )
                if (response.data.status === 'success') {
                    ElMessage.success('修改成功')
                    console.log(response.data)
                    dialogVisible.value = false
                    emits("success")
                }
                else {
                    ElMessage.error(response.data.message)
                    console.error(response.data)
                }
            } catch (error) {
                ElMessage.error('修改失败，请稍后再试')
                console.error(error)
            }
        }
        else {
            try {
                console.log(imageUrls)
                const response = await axios.post('http://10.29.39.146:8088/api/publicity/create', {
                    token: token,
                    province: formData.province,
                    city: formData.city,
                    townName: formData.town,
                    type: formData.type,
                    title: formData.title,
                    description: formData.description,
                    image_url: imageUrls,
                    video_url: videoUrls
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

// 取消提交
const cancelSubmit = () => {
    dialogVisible.value = false
}

// 检测上传图片大小
const beforeImageUpload = (file) => {
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
        ElMessage.error('上传图片大小不能超过 5MB!')
    }
}

// 检测上传视频大小
const beforeVideoUpload = (file) => {
    const isLt100M = file.size / 1024 / 1024 < 100
    if (!isLt100M) {
        ElMessage.error('上传视频大小不能超过 100MB!')
    }
}

// 图片预览
const handleImagePreview = (uploadFile) => {
    dialogImageUrl.value = uploadFile.url
    previewVisible.value = true
}

onMounted(() => {
    if (props.editData) {
        formData.province = props.editData.province
        formData.city = props.editData.city
        formData.town = props.editData.town_name
        formData.type = props.editData.type
        formData.title = props.editData.title
        formData.description = props.editData.description
        for (const url of props.editData.image_url) {
            formData.images.push({
                name: '',
                url: url
            })
        }
        for (const url of props.editData.video_url) {
            formData.videos.push({
                name: '',
                url: url
            })
        }
    }
})
</script>

<style lang="scss" scoped>
$input-width: 400px;

.el-form {
    padding: 20px;

    .el-select {
        width: $input-width;
    }

    .el-input {
        width: $input-width;
    }

    :deep(.el-textarea__inner) {
        width: $input-width;
    }
}
</style>