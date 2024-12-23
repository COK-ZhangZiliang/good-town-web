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
                <el-button v-if="props.showType === 'showAll'" type="primary" @click="handleSupport">
                    <el-icon>
                        <Star />
                    </el-icon>
                    助力
                </el-button>

                <template v-if="props.type === 'showMy'">
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
</template>

<script setup>
import { computed, defineProps, ref } from 'vue'
import { Star, Edit, Delete, UserFilled } from '@element-plus/icons-vue'
import AssistanceInfo from '@/components/AssistanceInfo.vue';

const props = defineProps({
    content: {
        type: Object,
        required: true,
        default: () => ({}),
    },
    showType: {
        type: String,
        required: true,
        default: 'showAll',
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

const handleSupport = () => {
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
</style>