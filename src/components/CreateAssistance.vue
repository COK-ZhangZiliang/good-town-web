<!-- 发布助力对话框 -->
<template>
    <el-dialog v-model="dialogVisible" title="添加助力信息" width="600px" :close-on-click-modal="false">
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
import { ref, reactive, defineProps, computed, defineEmits } from 'vue'
import { uploadFiles } from "@/utils/upload";
import { Plus } from "@element-plus/icons-vue";
import { getToken } from '@/utils/auth';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 接收变量
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    editData: {
        type: Object,
        required: false
    },
    publicity_id: {
        type: Number,
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

const supportFormRef = ref();
const supportFormData = reactive({
    description: "",
    images: [],
    videos: [],
});

const dialogImageUrl = ref("");
const previewVisible = ref(false);

const rules = {
    description: [
        { required: true, message: "请输入助力描述", trigger: "blur" },
        { min: 1, max: 500, message: "长度在 1 到 500 个字符", trigger: "blur" },
    ],
};

// 处理助力提交
const submitSupport = async () => {
    if (!supportFormRef.value) return;
    const token = getToken();

    try {
        await supportFormRef.value.validate();

        const imageUrls = await uploadFiles(supportFormData.images, token)
        const videoUrls = await uploadFiles(supportFormData.videos, token)

        try {
            const response = await axios.post(
                "http://10.29.39.146:8088/api/assistance/create",
                {
                    token: token,
                    publicity_id: props.publicity_id,
                    description: supportFormData.description,
                    image_url: imageUrls,
                    video_url: videoUrls,
                }
            );
            if (response.data.status === "success") {
                ElMessage.success("发布成功");
                console.log(response.data);
                dialogVisible.value = false;
            } else {
                ElMessage.error(response.data.message);
                console.error(response.data);
            }
        } catch (error) {
            ElMessage.error("发布失败，请稍后再试");
            console.error(error);
        }
    } catch (error) {
        ElMessage.error("请求失败，请稍后再试");
        console.error(error);
    }
};

// 取消提交
const cancelSupport = () => {
    dialogVisible.value = false;
};

// 检测上传图片大小
const beforeImageUpload = (file) => {
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
        ElMessage.error("上传图片大小不能超过 5MB!");
    }
};

// 检测上传视频大小
const beforeVideoUpload = (file) => {
    const isLt100M = file.size / 1024 / 1024 < 100;
    if (!isLt100M) {
        ElMessage.error("上传视频大小不能超过 100MB!");
    }
};

// 图片预览
const handleImagePreview = (uploadFile) => {
    dialogImageUrl.value = uploadFile.url;
    previewVisible.value = true;
};
</script>

<style lang="scss" scoped>
$input-width: 400px;

.el-form {
    padding: 20px;

    :deep(.el-textarea__inner) {
        width: $input-width;
    }
}
</style>