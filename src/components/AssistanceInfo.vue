<!-- 助力信息展示对话框 -->
<template>
    <el-dialog v-model="dialogVisible" title="助力详情" width="500px">
        <div v-if="assistance" class="assistance-detail">
            <div class="detail-item">
                <span class="label">助力描述：</span>
                <span class="value">{{ assistance.description || '暂无描述' }}</span>
            </div>

            <div v-if="assistance.image_url && assistance.image_url.length" class="media-container">
                <div class="images">
                    <el-image v-for="(url, index) in assistance.image_url" :key="index" :src="url"
                        :preview-src-list="assistance.image_url" fit="cover" class="media-item" />
                </div>
            </div>

            <div v-if="assistance.video_urls && assistance.video_urls.length" class="media-container">
                <div class="videos">
                    <video v-for="(url, index) in assistance.video_urls" :key="index" :src="url" controls
                        class="media-item" />
                </div>
            </div>
        </div>

        <template #footer>
            <div v-if="assistance?.status === 0" class="dialog-footer">
                <el-button type="success" @click="handleAccept">
                    <el-icon>
                        <Check />
                    </el-icon>接受
                </el-button>
                <el-button type="danger" @click="handleReject">
                    <el-icon>
                        <Close />
                    </el-icon>拒绝
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import axios from 'axios'
import { getToken } from '@/utils/auth'

// 接收数据
const props = defineProps({
    assistance: Object,
    dialogVisible: Boolean
})

const emits = defineEmits(['update:dialogVisible'])

const dialogVisible = computed({
    get: () => props.dialogVisible,
    set: (value) => emits('update:dialogVisible', value)
})

const assistance = computed(() => props.assistance)

const handleAccept = () => {
    ElMessageBox.confirm('确定接受该助力吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const response = await axios.post(
                "http://10.29.39.146:8088/api/assistance/update",
                {
                    token: getToken(),
                    assistance_id: props.assistance.assistance_id,
                    action: 1
                }
            );
            if (response.data.status === "success") {
                ElMessage.success('已接受')
                console.log(response.data);
                dialogVisible.value = false;
                emits("success")
            } else {
                ElMessage.error(response.data.message);
                console.error(response.data);
            }
        } catch (error) {
            ElMessage.error("接受失败，请稍后再试");
            console.error(error);
        }
    })
        .catch(() => {
            ElMessage.info('已取消')
        })
}

const handleReject = () => {
    ElMessageBox.confirm('确定拒绝该助力吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const response = await axios.post(
                "http://10.29.39.146:8088/api/assistance/update",
                {
                    token: getToken(),
                    assistance_id: props.assistance.assistance_id,
                    action: 2
                }
            );
            if (response.data.status === "success") {
                ElMessage.success('已拒绝')
                console.log(response.data);
                dialogVisible.value = false;
                emits("success")
            } else {
                ElMessage.error(response.data.message);
                console.error(response.data);
            }
        } catch (error) {
            ElMessage.error("拒绝失败，请稍后再试");
            console.error(error);
        }
    })
        .catch(() => {
            ElMessage.info('已取消')
        })
}
</script>

<style lang="scss" scoped>
.assistance-detail {
    padding: 20px;
    font-size: 16px;

    .detail-item {
        margin-bottom: 20px;
        line-height: 1.5; // 增加行高
    }

    .label {
        font-weight: bold;
        margin-right: 10px;
        color: #606266;
    }

    .media-container {
        margin: 20px 0;

        .images,
        .videos {
            display: flex;
            justify-content: center;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        .media-item {
            width: 300px;
            height: 225px;
            object-fit: cover;
            border-radius: 4px;
        }
    }
}
</style>