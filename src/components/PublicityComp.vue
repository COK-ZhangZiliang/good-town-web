<template>
    <el-dialog v-model="dialogVisible" title="发布宣传信息" width="600px" :close-on-click-modal="false"
        custom-class="promotion-dialog">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="promotion-form">
            <el-form-item label="省份" prop="province">
                <el-select v-model="formData.province" @change="handleProvinceChange">
                    <el-option v-for="item in provinces" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>

            <el-form-item label="城市" prop="city">
                <el-select v-model="formData.city" @change="handleCityChange">
                    <el-option v-for="item in cities" :key="item.value" :label="item.label" :value="item.value" />
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
                <el-upload action="/api/upload" list-type="picture-card" :on-preview="handlePictureCardPreview"
                    :on-remove="handleRemove" :before-upload="beforeUpload" multiple>
                    <el-icon>
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item label="上传视频">
                <el-upload action="/api/upload" :on-success="handleVideoSuccess" :before-upload="beforeVideoUpload"
                    accept="video/*">
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
</template>

<script setup>
import { ref, reactive, defineProps, defineEmits, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const formRef = ref(null)

const formData = reactive({
    province: '',
    city: '',
    town: '',
    type: '',
    title: '',
    description: '',
    images: [],
    video: ''
})

const rules = {
    province: [{ required: true, message: '请选择省份', trigger: 'change' }],
    city: [{ required: true, message: '请选择城市', trigger: 'change' }],
    town: [{ required: true, message: '请输入乡镇名', trigger: 'blur' }],
    type: [{ required: true, message: '请选择宣传类型', trigger: 'change' }],
    title: [{ required: true, message: '请输入宣传主题', trigger: 'blur' }],
    description: [{ required: true, message: '请输入宣传描述', trigger: 'blur' }]
}

const handleSubmit = async () => {
    if (!formRef.value) return

    try {
        await formRef.value.validate()
        // TODO: 调用API提交数据
        ElMessage.success('发布成功')
        dialogVisible.value = false
    } catch (error) {
        console.error('表单验证失败:', error)
    }
}

const beforeUpload = (file) => {
    const isImage = file.type.startsWith('image/')
    const isLt2M = file.size / 1024 / 1024 < 2

    if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
    }
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
    }
    return true
}

const beforeVideoUpload = (file) => {
    const isLt50M = file.size / 1024 / 1024 < 50

    if (!isLt50M) {
        ElMessage.error('视频大小不能超过 50MB!')
        return false
    }
    return true
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