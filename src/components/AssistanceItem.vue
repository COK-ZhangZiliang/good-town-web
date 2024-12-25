<!-- 助力卡片 -->
<template>
    <div class="assistance-item">
        <!-- 头部： 助力乡镇信息、更新时间 -->
        <div class="header">
            <!-- 省市标签，乡镇标签 -->
            <div class="tags">
                <el-tag type="success" size="large" effect="dark">{{ content.province }}</el-tag>
                <el-tag type="warning" size="large" effect="dark">{{ content.city }}</el-tag>
                <el-tag type="primary" size="large" effect="dark">{{ content.town_name }}</el-tag>
            </div>
            <span class="update-time">{{ content.updated_at }}</span>
        </div>

        <!-- 主要内容 -->
        <div class="content">
            <p class="description">{{ content.description }}</p>
        </div>

        <!-- 图片、视频展示 -->
        <div class="media-container">
            <el-image v-for="(url, index) in imageUrls" :key="index" :src="url" :preview-src-list="imageUrls"
                fit="cover" class="media-item" />
            <video v-for="(url, index) in videoUrls" :key="index" :src="url" controls class="media-item" />
        </div>

        <div class="footer">
            <!-- 修改、删除按钮 -->
            <div class="button-container">
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
            </div>
        </div>
    </div>

    <!-- 修改对话框 -->
    <CreateAssistance v-if="editVisible" v-model:visible="editVisible" :editData="content"
        @success="handleEditSuccess" />
</template>

<script setup>
import { computed, defineProps, ref, defineEmits } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getToken } from "@/utils/auth";
import axios from "axios";
import CreateAssistance from "./CreateAssistance.vue";

// 接收数据
const props = defineProps({
    content: {
        type: Object,
        required: true,
        default: () => ({}),
    }
});

const emits = defineEmits(['refresh']);

const content = computed(() => {
    return props.content;
});
const imageUrls = computed(() => content.value.image_url || []);
const videoUrls = computed(() => content.value.video_url || []);

const editVisible = ref(false);

const handleEdit = () => {
    editVisible.value = true;
};

const handleEditSuccess = () => {
    editVisible.value = false
    emits('refresh')
}

const handleDelete = async () => {
    ElMessageBox.confirm(
        '确定要删除这条助力信息吗？',
        '警告',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            try {
                const response = await axios.delete(`http://10.29.39.146:8088/api/assistance/delete/${content.value.assistance_id}`, { headers: { token: getToken() } })
                if (response.data.status === 'success') {
                    ElMessage.success('删除成功')
                    emits('refresh')
                }
                else {
                    console.log(response.data)
                    ElMessage.error(response.data.message)
                }
            }
            catch (error) {
                ElMessage.error('删除失败，请稍后再试')
            }
        })
        .catch(() => {
            ElMessage.info('已取消删除')
        })
};
</script>

<style lang="scss" scoped>
.assistance-item {
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

    .update-time {
        position: absolute;
        right: 0;
        color: #999;
        font-size: 14px;
    }

    .tags {
        display: flex;
        gap: 8px;
    }
}

.content {
    margin-bottom: 12px;

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
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 30px;

    .button-container {
        display: flex;
        gap: 8px;
        margin-left: auto;
    }
}

$input-width: 400px;

.el-form {
    padding: 20px;

    :deep(.el-textarea__inner) {
        padding: 20px;
        width: $input-width;
    }
}
</style>