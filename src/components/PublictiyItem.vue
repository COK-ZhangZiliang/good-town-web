<!-- 宣传卡片 -->
<template>
    <div class="publicity-item">
        <div class="header">
            <img :src="userAvatar" alt="用户头像" class="avatar" />
            <span class="username">{{ username }}</span>
        </div>
        <div class="content">
            <h3 class="title">{{ title }}</h3>
            <p class="description">{{ description }}</p>
            <div class="media" v-if="media.length">
                <img v-for="(item, index) in media" :key="index" :src="item" alt="媒体文件" class="media-item" />
            </div>
        </div>
        <div class="actions">
            <button @click="like">点赞</button>
            <button @click="support">助力</button>
            <button @click="comment">评论</button>
        </div>
    </div>
</template>

<script setup>

const like = () => {
    // 点赞逻辑
}

const support = () => {
    // 助力逻辑
}

const comment = () => {
    // 评论逻辑
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

.actions {
    display: flex;
    gap: 8px;

    button {
        padding: 8px 12px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        background-color: #007bff;
        color: white;

        &:hover {
            background-color: #0056b3;
        }
    }
}
</style>
