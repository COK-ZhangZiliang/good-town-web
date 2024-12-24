<!-- 宣传卡片 -->
<template>
    <div class="publicity-item">
        <!-- 头部：用户头像、用户名 -->
        <div class="header">
            <template v-if="user.avatarUrl">
                <el-avatar :size="40" class="avatar">
                    <img :src="user.avatarUrl" />
                </el-avatar>
            </template>
            <template v-else>
                <el-avatar :size="40" class="avatar">
                    <el-icon>
                        <UserFilled />
                    </el-icon>
                </el-avatar>
            </template>
            <span class="username">{{ user.username }}</span>
            <span class="update-time">{{ content.updated_at }}</span>
        </div>

        <!-- 主要内容 -->
        <div class="content">
            <h3 class="title">{{ content.title }}</h3>
            <p class="description">{{ content.description }}</p>
        </div>

        <!-- 图片展示 -->
        <div class="media-container">
            <el-image v-for="(url, index) in imageUrls" :key="index" :src="url" :preview-src-list="imageUrls"
                fit="cover" class="media-item" />
            <video v-for="(url, index) in videoUrls" :key="index" :src="url" controls class="media-item" />
        </div>

        <div class="footer">
            <!-- 省市标签，乡镇标签，类型标签 -->
            <div class="tags">
                <el-tag type="success" size="large" effect="dark">{{ content.province }}</el-tag>
                <el-tag type="warning" size="large" effect="dark">{{ content.city }}</el-tag>
                <el-tag type="primary" size="large" effect="dark">{{ content.town_name }}</el-tag>
                <el-tag type="info" size="large" effect="dark">{{ convertedType }}</el-tag>
            </div>

            <!-- 助力、修改、删除按钮 -->
            <div class="button-container">
                <el-button v-if="props.type === 'allPromotions'" type="primary" @click="handleSupport">
                    <el-icon>
                        <Star />
                    </el-icon>
                    助力
                </el-button>

                <template v-if="props.type === 'myPromotions'">
                    <el-button type="warning" @click="handleEdit">
                        <el-icon>
                            <Edit />
                        </el-icon>
                        修改
                    </el-button>
                    <el-button type="danger" @click="handleDelete">
                        <el-icon>
                            <Delete />
                        </el-icon>
                        删除
                    </el-button>
                </template>
            </div>

            <div class="support-users" v-if="assist.length">
                <span class="support-label">已接受助力:</span>
                <span v-for="(ass, index) in assist" :key="index" class="support-name" @click="showAssistDetail(ass)">
                    {{ ass.user_name }}{{ index < assist.length - 1 ? ',' : '' }} </span>
            </div>
        </div>
    </div>

    <!-- 助力信息详情对话框 -->
    <AssistanceInfo v-model:dialog-visible="AssistanceInfoVisible" :assistance="selectedAssistance" />

    <!-- 填写助力信息对话框 -->
    <el-dialog v-model="supportVisible" title="添加助力信息" width="600px" :close-on-click-modal="false">
        <el-form :model="supportFormData" :rules="rules" ref="supportFormRef">
            <el-form-item label="助力描述" prop="description">
                <el-input v-model="supportFormData.description" type="textarea" :rows="4" placeholder="请输入助力描述" />
            </el-form-item>

            <el-form-item label="上传图片">
                <el-upload v-model:file-list="supportFormData.images" list-type="picture-card"
                    :on-preview="handleImagePreview" :before-upload="beforeImageUpload" accept="image/*"
                    :auto-upload="false" multiple>
                    <el-icon>
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item label="上传视频">
                <el-upload v-model:file-list="supportFormData.videos" :before-upload="beforeVideoUpload"
                    accept="video/*" :auto-upload="false" multiple>
                    <el-button type="primary">选择视频</el-button>
                </el-upload>
            </el-form-item>
        </el-form>
        
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="cancelSupport">取消</el-button>
                <el-button type="primary" @click="submitSupport">确定</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-model="previewVisible">
        <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
</template>

<script setup>
import { computed, defineProps, ref, reactive } from 'vue'
import { Star, Edit, Delete, UserFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AssistanceInfo from '@/components/AssistanceInfo.vue'
import { getToken } from '@/utils/auth'
import { uploadFiles } from '@/utils/upload'
import axios from 'axios'

// 接收数据
const props = defineProps({
    content: {
        type: Object,
        required: true,
        default: () => ({}),
    },
    type: {
        type: String,
        required: true,
    },
    username: {
        type: String,
        required: true
    }
})

const content = computed(() => {
    return props.content
})
const imageUrls = computed(() => content.value.image_url || [])
const videoUrls = computed(() => content.value.video_url || [])
const user = computed(() => content.value.user || {})
const assist = computed(() => content.value.assistance_requests || {})

const AssistanceInfoVisible = ref(false)
const selectedAssistance = ref(null)

const dialogImageUrl = ref('')
const previewVisible = ref(false)

// 展示详细助力信息
const showAssistDetail = (ass) => {
    AssistanceInfoVisible.value = true
    selectedAssistance.value = ass
}

// 类型映射
const typeMap = {
    'farmhouse': '农家院',
    'nature': '自然风光',
    'ancient': '古建筑',
    'specialty': '土特产',
    'food': '特色小吃',
    'folk': '民俗活动'
}

const convertedType = computed(() => {
    return typeMap[props.content.type] || props.content.type
})

const supportVisible = ref(false)
const supportFormRef = ref()
const supportFormData = reactive({
    description: '',
    images: [],
    videos: []
})

const rules = {
    description: [
        { required: true, message: '请输入助力描述', trigger: 'blur' },
        { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
    ]
}

const handleSupport = () => {
    if (user.value.username === props.username) {
        ElMessage.error('无法对自己的宣传助力')
        return
    }
    supportVisible.value = true
}

// 处理助力提交
const submitSupport = async () => {
    if (!supportFormRef.value) return
    const token = getToken()
    const imageUrls = []
    const videoUrls = []

    try {
        await supportFormRef.value.validate()

        imageUrls.value = uploadFiles(supportFormData.images, token).value
        videoUrls.value = uploadFiles(supportFormData.videos, token).value

        try {
            const response = await axios.post('http://10.29.39.146:8088/api/assistance/create', {
                token: token,
                publicity_id: content.value.publicity_id,
                description: supportFormData.description,
                image_url: imageUrls,
                video_url: videoUrls
            })
            if (response.data.status === 'success') {
                supportFormData.description = ''
                supportFormData.images = []
                supportFormData.videos = []
                ElMessage.success('发布成功')
                console.log(response.data)
                supportVisible.value = false
            }
            else {
                ElMessage.error(response.data.message)
                console.error(response.data)
            }
        } catch (error) {
            ElMessage.error('发布失败，请稍后再试')
            console.error(error)
        }

    } catch (error) {
        ElMessage.error('请求失败，请稍后再试')
        console.error(error)
    }
}

// 取消提交
const cancelSupport = () => {
    supportVisible.value = false
    supportFormData.description = ''
    supportFormData.images = []
    supportFormData.videos = []
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

const handleEdit = () => {
}

const handleDelete = () => {
}

</script>

<style lang="scss" scoped>
.publicity-item {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
}

.header {
    position: relative;
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        margin-right: 8px;
    }

    .username {
        font-weight: bold;
    }

    .update-time {
        position: absolute;
        right: 0;
        color: #999;
        font-size: 14px;
    }
}

.content {
    margin-bottom: 12px;

    .title {
        font-size: 18px;
        margin-bottom: 8px;
    }

    .description {
        margin-bottom: 8px;
    }

    .media {
        display: flex;
        gap: 8px;

        .media-item {
            width: 100px;
            height: 100px;
            object-fit: cover;
            border-radius: 4px;
        }
    }
}

.media-container {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 12px;
    margin: 10px 0;
    flex-wrap: wrap;

    .media-item {
        width: 300px;
        height: 225px;
        object-fit: cover;
        border-radius: 4px;
    }
}

.footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 30px;

    .tags {
        display: flex;
        gap: 8px;
    }

    .button-container {
        display: flex;
        gap: 8px;
    }
}

.support-users {
    margin-top: 8px;

    .support-name {
        color: #409EFF;
        cursor: pointer;
        margin: 0 4px;

        &:hover {
            text-decoration: underline;
        }
    }
}

$input-width: 400px;

.el-form {
    padding: 20px;

    :deep(.el-textarea__inner) {
        padding: 20px;
        width: $input-width ;
    }
}
</style>