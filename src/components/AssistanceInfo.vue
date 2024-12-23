<!-- 助力信息展示对话框 -->
<template>
    <el-dialog v-model="dialogVisible" title="助力详情" width="50%">
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
    </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue'

// 接收数据
const props = defineProps({
    assistance: Object,
    dialogVisible: Boolean
})

const emits = defineEmits(['update:dialogVisible'])

const dialogVisible = computed ({
    get: () => props.dialogVisible,
    set: (value) => emits('update:dialogVisible', value)
})

const assistance = computed(() => props.assistance)
</script>

<style lang="scss" scoped>
.assistance-detail {
    padding: 20px;

    .detail-item {
        margin-bottom: 15px;
    }

    .label {
        font-weight: bold;
        margin-right: 10px;
        color: #606266;
    }

    .media-container {
        margin-top: 15px;

        .images,
        .videos {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 10px;
            margin-top: 10px;
        }

        .media-item {
            width: 100%;
            height: 150px;
            object-fit: cover;
            border-radius: 4px;
        }
    }
}
</style>