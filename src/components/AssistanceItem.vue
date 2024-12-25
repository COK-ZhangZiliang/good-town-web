<!-- 助力卡片 -->
<template>
    <div class="assistance-item">
        <!-- 头部： 用户头像、用户名、更新时间 -->
        <div class="header">
            <template v-if="user.avatarUrl">
                <el-avatar :size="40" class="avatar" :class="{ 'clickable': props.type === 'adminQuery' }"
                    @click="handleAvatarClick">
                    <img :src="user.avatarUrl" />
                </el-avatar>
            </template>
            <template v-else>
                <el-avatar :size="40" class="avatar" :class="{ 'clickable': props.type === 'adminQuery' }"
                    @click="handleAvatarClick">
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
            <p class="description">{{ content.description }}</p>
        </div>

        <!-- 图片、视频展示 -->
        <div class="media-container">
            <el-image v-for="(url, index) in imageUrls" :key="index" :src="url" :preview-src-list="imageUrls"
                fit="cover" class="media-item" />
            <video v-for="(url, index) in videoUrls" :key="index" :src="url" controls class="media-item" />
        </div>

        <div class="footer">
            <!-- 省市标签，乡镇标签 -->
            <div class="tags">
                <el-tag type="success" size="large" effect="dark">{{ content.province }}</el-tag>
                <el-tag type="warning" size="large" effect="dark">{{ content.city }}</el-tag>
                <el-tag type="primary" size="large" effect="dark">{{ content.town_name }}</el-tag>
                <div class="status-tag" :class="statusClass">
                    {{ getStatusText(content.status) }}
                </div>
            </div>
            <!-- 修改、删除按钮 -->
            <template v-if="props.type === 'myAssistance'">
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
            </template>
        </div>
    </div>

    <!-- 用户信息对话框 -->
    <el-dialog v-model="userInfoVisible" title="用户信息" width="400px">
        <div class="user-detail">
            <div class="user-avatar">
                <el-avatar :size="80" :src="user.avatarUrl">
                    <el-icon v-if="!user.avatarUrl">
                        <UserFilled />
                    </el-icon>
                </el-avatar>
            </div>

            <div class="user-info">
                <div class="info-item">
                    <div class="label">用户名</div>
                    <div class="value">{{ user.username }}</div>
                </div>

                <div class="info-item">
                    <div class="label">手机号</div>
                    <div class="value">{{ user.phone }}</div>
                </div>

                <div class="info-item">
                    <div class="label">个人简介</div>
                    <div class="value bio">{{ user.bio || '暂无简介' }}</div>
                </div>
            </div>
        </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <CreateAssistance v-if="editVisible" v-model:visible="editVisible" :editData="content"
        @success="handleEditSuccess" />
</template>

<script setup>
import { computed, defineProps, ref, defineEmits } from "vue";
import { Edit, Delete, UserFilled } from "@element-plus/icons-vue";
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
    },
    type: {
        type: String,
        required: true
    }
});

const emits = defineEmits(['refresh']);

const content = computed(() => {
    return props.content;
});
const imageUrls = computed(() => content.value.image_url || []);
const videoUrls = computed(() => content.value.video_url || []);
const user = computed(() => content.value.user || {});

const editVisible = ref(false);
const userInfoVisible = ref(false);

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

// 类型转换
const STATUS_MAP = {
    0: { text: '待接受', type: 'warning' },
    1: { text: '已接受', type: 'success' },
    2: { text: '已拒绝', type: 'danger' }
}
const getStatusText = (status) => {
    return STATUS_MAP[status]?.text || '未知状态'
}

const statusClass = computed(() => {
    return `status-${STATUS_MAP[content.value.status]?.type || 'info'}`
})

const handleAvatarClick = () => {
    if (props.type === 'allAssistance') {
        userInfoVisible.value = true;
    }
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

    .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        margin-right: 8px;

        .clickable {
            cursor: pointer;
            transition: opacity 0.3s;

            &:hover {
                opacity: 0.8;
            }
        }
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

    .tags {
        display: flex;
        gap: 8px;

        .status-tag {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;

            &.status-warning {
                background-color: #fdf6ec;
                color: #e6a23c;
                border: 1px solid #faecd8;
            }

            &.status-success {
                background-color: #f0f9eb;
                color: #67c23a;
                border: 1px solid #e1f3d8;
            }

            &.status-danger {
                background-color: #fef0f0;
                color: #f56c6c;
                border: 1px solid #fde2e2;
            }

            &.status-info {
                background-color: #f4f4f5;
                color: #909399;
                border: 1px solid #e9e9eb;
            }
        }
    }

    .button-container {
        display: flex;
        gap: 8px;
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

.user-detail {
    padding: 20px;

    .user-avatar {
        text-align: center;
        margin-bottom: 20px;
    }

    .user-info {
        .info-item {
            margin-bottom: 15px;

            .label {
                color: #909399;
                font-size: 14px;
                margin-bottom: 5px;
            }

            .value {
                color: #303133;
                font-size: 14px;

                &.bio {
                    white-space: pre-wrap;
                    line-height: 1.5;
                }
            }
        }
    }
}
</style>