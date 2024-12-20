<template>
  <div class="home-container">
    <!-- 顶部菜单栏 -->
    <header class="header">
      <div class="left">
        <!-- 未登录显示登录注册链接，已登录显示用户头像 -->
        <div v-if="!isLoggedIn" class="auth-links">
          <router-link to="/login" class="login-link">
            <el-avatar :size="40" src="src/assets/default-avatar.png">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span>登录/注册</span>
          </router-link>
        </div>
        <div v-else class="user-info">
          <el-avatar :size="40" :src="userAvatar" @click="handleAvatarClick" />
          <span class="username">{{ username }}</span>
        </div>
      </div>
      <div class="logo">
        <img src="../assets/logo.png" alt="Logo" />
      </div>
    </header>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧导航栏 -->
      <div class="left-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="help">
            <el-icon><Star /></el-icon>
            <span>我助力</span>
          </el-menu-item>
          <el-menu-item index="promote">
            <el-icon><Promotion /></el-icon>
            <span>我宣传</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 中间内容区 -->
      <div class="content">
        <div class="search">
          <el-input
            v-model="searchQuery"
            placeholder="搜索..."
            prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <router-view></router-view>
      </div>

      <!-- 右侧热度榜 -->
      <div class="right-sidebar">
        <div class="hot-list">
          <h3 class="hot-title">
            <el-icon><Histogram /></el-icon>
            热度榜
          </h3>
          <ul class="hot-items">
            <li v-for="(item, index) in hotList" :key="index" class="hot-item">
              <span class="rank" :class="{'top-three': index < 3}">{{ index + 1 }}</span>
              <span class="title">{{ item.title }}</span>
              <span class="hot-value">{{ item.hot }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Star, Promotion, Histogram } from '@element-plus/icons-vue'

// 状态管理
const isLoggedIn = ref(false)
const username = ref('')
const userAvatar = ref('')
const searchQuery = ref('')
const activeMenu = ref('help')
const router = useRouter()

// 模拟热度榜数据
const hotList = ref([
  { title: '热门话题1', hot: '999+' },
  { title: '热门话题2', hot: '888+' },
  { title: '热门话题3', hot: '777+' },
  { title: '热门话题4', hot: '666+' },
  { title: '热门话题5', hot: '555+' },
])

// 处理搜索
const handleSearch = () => {
  console.log('搜索:', searchQuery.value)
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

// 处理头像点击
const handleAvatarClick = () => {
  if (!isLoggedIn.value) {
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
// 变量定义
$max-width: 1200px;
$primary-color: #409EFF;
$border-color: #e6e6e6;
$text-primary: #303133;
$text-secondary: #606266;
$text-light: #909399;
$hot-color: #ff6b6b;

// 混合器
@mixin flex-center {
  display: flex;
  align-items: center;
}

@mixin container-width {
  width: 100%;
  max-width: $max-width;
  margin: 0 auto;
}

.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: relative;
  display: flex;
  width: 100%;
  height: 100px; // 固定顶栏高度
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

  .left {
    position: absolute;
    left: 20px;
    height: 100%;
    display: flex;
    align-items: center;
    z-index: 1;
  }

  .logo {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    
    img {
      height: 70px; // 图片高度稍小于顶栏
      width: auto;
      object-fit: contain;
    }
  }
}

.auth-links {
  @include flex-center;

  .login-link {
    @include flex-center;
    text-decoration: none;
    color: $text-secondary;
    gap: 8px;
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 60px);
}

.left-sidebar {
  width: 200px;
  background-color: white;
  border-right: 1px solid $border-color;

  .nav-menu {
    border-right: none;
  }
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;

  .search {
    flex: 1;
    max-width: 500px;
    margin: 20px auto;
  }
}

.right-sidebar {
  width: 300px;
  background-color: white;
  border-left: 1px solid $border-color;
  padding: 20px;

  .hot-title {
    @include flex-center;
    gap: 8px;
    margin-bottom: 16px;
    color: $text-primary;
  }

  .hot-items {
    list-style: none;
    padding: 0;
  }

  .hot-item {
    @include flex-center;
    padding: 12px 0;
    border-bottom: 1px solid #eee;

    .rank {
      width: 24px;
      height: 24px;
      @include flex-center;
      justify-content: center;
      margin-right: 12px;
      border-radius: 4px;
      background-color: #f0f0f0;
      color: $text-light;

      &.top-three {
        background-color: $primary-color;
        color: white;
      }
    }

    .title {
      flex: 1;
      margin-right: 12px;
      color: $text-primary;
    }

    .hot-value {
      color: $hot-color;
      font-size: 14px;
    }
  }
}
</style>