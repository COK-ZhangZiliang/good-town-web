<!-- 主页面 -->
<template>
  <div class="home-container">
    <!-- 顶部菜单栏 -->
    <header class="header">
      <div class="left">
        <!-- 未登录显示登录注册链接，已登录显示用户头像 -->
        <div v-if="!isLoggedIn" class="auth-links">
          <router-link to="/login" class="login-link">
            <el-avatar :size="50">
              <el-icon>
                <UserFilled />
              </el-icon>
            </el-avatar>
            <span>登录/注册</span>
          </router-link>
        </div>
        <div v-else class="user-info">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <template v-if="formData.userAvatar">
                <el-avatar :size="50" class="avatar">
                  <img :src="formData.userAvatar" />
                </el-avatar>
              </template>
              <template v-else>
                <el-avatar :size="50" class="avatar">
                  <el-icon>
                    <UserFilled />
                  </el-icon>
                </el-avatar>
              </template>
              <span class="username">{{ formData.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">修改信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
        <el-menu :default-active="activeMenu" class="nav-menu" :default-openeds="['assist', 'promote', 'admin']"
          @select="handleMenuSelect">
          <el-sub-menu index="assist">
            <template #title>
              <el-icon>
                <Star />
              </el-icon>
              <span>我助力</span>
            </template>
            <el-menu-item index="myAssistance">
              <el-icon>
                <Document />
              </el-icon>
              <span>我的助力</span>
            </el-menu-item>
            <el-menu-item index="createAssistance">
              <el-icon>
                <Plus />
              </el-icon>
              <span>发布助力</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="promote">
            <template #title>
              <el-icon>
                <Promotion />
              </el-icon>
              <span>我宣传</span>
            </template>
            <el-menu-item index="myPromotions">
              <el-icon>
                <Document />
              </el-icon>
              <span>我的宣传</span>
            </el-menu-item>
            <el-menu-item index="createPromotion">
              <el-icon>
                <Plus />
              </el-icon>
              <span>发布宣传</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="admin" v-if="isAdmin">
            <template #title>
              <el-icon>
                <Setting />
              </el-icon>
              <span>管理员</span>
            </template>
            <el-menu-item index="queryUsers">
              <el-icon>
                <User />
              </el-icon>
              <span>查询用户</span>
            </el-menu-item>
            <el-menu-item index="queryPublicity">
              <el-icon>
                <Document />
              </el-icon>
              <span>查询宣传</span>
            </el-menu-item>
            <el-menu-item index="queryAssistance">
              <el-icon>
                <Star />
              </el-icon>
              <span>查询助力</span>
            </el-menu-item>
            <el-menu-item index="statistics">
              <el-icon>
                <Histogram />
              </el-icon>
              <span>查询统计</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- 中间内容区 -->
      <div class="content">
        <ShowPublicity v-if="activeMenu === 'createAssistance'" :username="formData.username" :type="'allPromotions'" />
        <ShowPublicity v-if="activeMenu === 'myPromotions'" :type="'myPromotions'" />
        <ShowAssistance v-if="activeMenu === 'myAssistance'" />
        <showUser v-if="activeMenu === 'queryUsers'" :type="'myAssistance'" />
        <ShowPublicity v-if="activeMenu === 'queryPublicity'" :type="'adminQuery'" />
        <ShowAssistance v-if="activeMenu === 'queryAssistance'" :type="'allAssistance'" />
        <ShowStatistics v-if="activeMenu === 'statistics'" />
      </div>

      <!-- 右侧热度榜 -->
      <div class="right-sidebar">
        <PopularityBoard />
      </div>
    </div>
  </div>

  <!--修改个人信息-->
  <el-dialog v-model="updateVisible" title="修改个人信息" width="480px" :close-on-click-modal="false"
    custom-class="profile-dialog">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px" class="profile-form">
      <div class="avatar-container">
        <el-upload class="avatar-uploader" v-model:file-list="formData.avatarFile" list-type="picture-card"
          :before-upload="beforeAvatarUpload" :auto-upload="false" single>
          <el-avatar v-if="formData.userAvatar" :size="50" :src="formData.userAvatar" class="avatar" />
          <el-avatar v-else :size="50" class="avatar">
            <el-icon>
              <UserFilled />
            </el-icon>
          </el-avatar>
          <div class="avatar-hover-text">点击更换头像</div>
        </el-upload>
      </div>
      <el-form-item label="用户名">
        <el-input v-model="formData.username" disabled class="disabled-input" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="formData.name" disabled class="disabled-input" />
      </el-form-item>
      <el-form-item label="证件类型">
        <el-input v-model="formData.idType" disabled class="disabled-input" />
      </el-form-item>
      <el-form-item label="证件号码">
        <el-input v-model="formData.idNumber" disabled class="disabled-input" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="formData.phone" prefix-icon="Phone" placeholder="请输入新的手机号" />
      </el-form-item>
      <el-form-item label="个人简介">
        <el-input v-model="formData.bio" type="textarea" rows="4" resize="none" placeholder="介绍一下你自己吧..." />
      </el-form-item>
      <el-form-item label="修改密码" prop="password">
        <el-input v-model="formData.password" type="password" show-password prefix-icon="Lock" placeholder="请输入新的密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="updateVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdateProfile">确 认</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 发布宣传 -->
  <CreatePublicity v-if="promoteVisible" v-model:visible="promoteVisible" />
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Star, Promotion, Plus, Document, Setting, User, Histogram } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { removeToken } from '@/utils/auth'
import axios from 'axios'
import CreatePublicity from '@/components/CreatePublicity.vue'
import ShowPublicity from '@/components/ShowPublicity.vue'
import ShowAssistance from '@/components/ShowAssistance.vue'
import ShowUser from '@/components/ShowUser.vue'
import PopularityBoard from '@/components/PopularityBoard.vue'
import ShowStatistics from '@/components/ShowStatistics.vue'
import { uploadFiles } from '@/utils/upload'

// 状态管理
const isLoggedIn = ref(false)
const activeMenu = ref('createAssistance')
const router = useRouter()
const updateVisible = ref(false)
var token = ''
const promoteVisible = ref(false)

const formData = reactive({
  userAvatar: '',       // 用户头像url
  username: '',        // 用户名
  name: '',            // 用户姓名
  idType: '',          // 证件类型
  idNumber: '',        // 证件号码
  phone: '',          // 手机号
  bio: '',            // 个人简介
  password: '',        // 密码
  avatarFile: [],      // 头像文件
})

const formRef = ref(null)

const isAdmin = ref(false)

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback()
  }

  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
    return
  }

  const hasNumber = /\d.*\d/.test(value)

  if (!hasNumber) {
    callback(new Error('密码必须包含至少两个数字'))
    return
  }

  if (value === value.toLowerCase() || value === value.toUpperCase()) {
    callback(new Error('密码不能全为大写或小写字母'))
    return
  }

  callback()
}

const rules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { message: '请输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await axios.get('http://10.29.39.146:8088/api/users/info', { headers: { token: token } })
    if (response.data.status === 'success') {
      formData.userAvatar = response.data.data.avatarUrl
      formData.username = response.data.data.username
      formData.name = response.data.data.name
      isAdmin.value = response.data.data.userType === 1
      switch (response.data.data.idType) {
        case "ID_CARD":
          formData.idType = '身份证'
          break
        case "PASSPORT":
          formData.idType = '护照'
          break
        case "MILITARY_ID":
          formData.idType = '军官证'
          break
      }
      formData.idNumber = response.data.data.idNumber
      formData.phone = response.data.data.phone
      formData.bio = response.data.data.bio
    } else {
      ElMessage.error(response.data.message)
      console.error(response.data)
      isLoggedIn.value = false
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败，请稍后再试')
    token = ''
    isLoggedIn.value = false
    console.error(error)
  }
}

// 更新用户信息方法
const updateInfo = async () => {
  try {
    console.log(1)
    const avatarUrl = await uploadFiles(formData.avatarFile, token)
    const updateData = {
      token: token,
      phone: formData.phone,
      bio: formData.bio,
    }
    if (formData.password) {
      updateData.password = formData.password
    }
    if (avatarUrl.length > 0) {
      updateData.avatarUrl = avatarUrl[0]
    }

    const response = await axios.put('http://10.29.39.146:8088/api/users/update', {
      ...updateData
    })
    if (response.data.status === 'success') {
      ElMessage.success(response.data.message)
      console.log(response.data)
    }
    else {
      ElMessage.error(response.data.message)
      console.error(response.data)
    }
  } catch (error) {
    ElMessage.error('更新用户信息失败，请稍后再试')
    console.error(error)
  }
}

// 检查用户是否已登录
onMounted(() => {
  token = getToken()
  if (token) {
    isLoggedIn.value = true
    getUserInfo()
  }
})

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'createPromotion') {
    promoteVisible.value = true
  }
}

// 处理点击头像后的下拉菜单选项
const handleCommand = (command) => {
  if (command === 'logout') {
    removeToken()
    localStorage.removeItem('username')
    router.push('/login')
    ElMessage.success('退出登录成功')
  }
  else if (command === 'profile') {
    updateVisible.value = true
  }
}

const handleUpdateProfile = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    await updateInfo()
    updateVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('请上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
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

  .user-info {
    .username {
      font-weight: bold;
      margin-left: 8px;
      font-size: large;
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
  width: 225px;
  background-color: white;
  border-right: 1px solid $border-color;
  padding: 10px;

  .nav-menu {

    .el-menu-item,
    .el-sub-menu__title {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.content {
  flex: 1;
  padding: 40px;
  padding-top: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.right-sidebar {
  width: 250px;
  background-color: white;
  border-left: 1px solid $border-color;
  padding: 20px;
}

.profile-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    margin: 0;
    border-bottom: 1px solid #eee;

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  :deep(.el-dialog__body) {
    padding: 30px 24px;
  }

  .profile-form {

    .el-input,
    .el-textarea {
      width: 100%;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-1px);
      }
    }

    .disabled-input {
      :deep(.el-input__wrapper) {
        background-color: #f5f7fa;
        cursor: not-allowed;
      }
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
    }
  }

  .dialog-footer {
    padding: 10px 0;
    text-align: right;

    .el-button {
      padding: 12px 24px;
      font-weight: 500;

      &--primary {
        background: linear-gradient(45deg, #409eff, #5caaff);
        border: none;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
        }
      }
    }
  }
}

.avatar-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  position: relative;

  .avatar-uploader {
    cursor: pointer;
    position: relative;
    text-align: center;

    .avatar {
      display: block;
      width: 80px;
      height: 80px;
      margin: 0 auto;
      transition: all 0.3s;

      :deep(.el-icon) {
        font-size: 60px;
        line-height: 80px;
      }
    }

    &:hover {
      .avatar {
        opacity: 0.8;
      }

      .avatar-hover-text {
        opacity: 1;
      }
    }

    .avatar-hover-text {
      position: absolute;
      bottom: -20px;
      left: 50%;
      transform: translateX(-50%);
      width: 100%;
      font-size: 12px;
      color: #909399;
      opacity: 0;
      transition: all 0.3s;
      text-align: center;
      white-space: nowrap;
    }
  }
}
</style>