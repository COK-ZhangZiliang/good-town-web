<template>
  <div class="login-container">
    <!-- 
      表单组件：使用 element-plus 的 el-form
      ref: 用于表单验证
      :model: 表单数据绑定
      :rules: 表单验证规则
     -->
    <el-form ref="formRef" :model="formData" :rules="rules" label-position="left" label-width="80px" class="login-form">
      <!-- 动态标题：根据 isLogin 切换显示 -->
      <h2 class="title">{{ isLogin ? '用户登录' : '用户注册' }}</h2>

      <!-- 登录表单部分 -->
      <template v-if="isLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password
            :show-password-on-click="true" />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-wrapper">
            <el-input v-model="formData.captcha" placeholder="请输入验证码" class="captcha-input" />
            <div class="captcha-code" @click="refreshCaptcha" v-html="captchaHtml"></div>
          </div>
        </el-form-item>

        <div class="remember-forgot">
          <el-checkbox v-model="formData.remember">记住我</el-checkbox>
          <el-button type="text" @click="handleForgotPassword">忘记密码？</el-button>
        </div>
      </template>

      <!-- 注册表单部分 -->
      <template v-else>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="用户姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入用户姓名" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="证件类型" prop="idType">
          <el-select v-model="formData.idType" placeholder="请选择证件类型">
            <el-option label="身份证" value="ID_CARD" />
            <el-option label="护照" value="PASSPORT" />
            <el-option label="军官证" value="MILITARY_ID" />
          </el-select>
        </el-form-item>

        <el-form-item label="证件号码" prop="idNumber">
          <el-input v-model="formData.idNumber" :placeholder="getIdNumberPlaceholder" prefix-icon="Document" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号码" prefix-icon="Phone" />
        </el-form-item>

        <el-form-item label="密码" prop="registerPassword">
          <el-input v-model="formData.registerPassword" type="password" placeholder="请输入密码" prefix-icon="Lock"
            show-password :show-password-on-click="true" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="formData.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="Lock"
            show-password :show-password-on-click="true" />
        </el-form-item>

        <el-form-item label="个人简介" prop="introduction">
          <el-input v-model="formData.introduction" type="textarea" :rows="3" placeholder="请输入个人简介" />
        </el-form-item>
      </template>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" class="submit-btn" @click="handleSubmit">
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
      </el-form-item>

      <!-- 切换登录/注册的链接 -->
      <div class="switch-type">
        <span>{{ isLogin ? '没有账号？' : '已有账号？' }}</span>
        <el-link type="primary" @click="switchLoginType">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </el-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted} from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 控制当前是登录还是注册状态
const isLogin = ref(true)
// 表单引用，用于表单验证
const formRef = ref()
// 添加验证码相关数据
const captchaHtml = ref('')
const captchaText = ref('')

// 生成验证码
const generateCaptcha = () => {
  const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
  captchaText.value = Array.from({ length: 4 }, () =>
    chars[Math.floor(Math.random() * chars.length)]
  ).join('')

  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
  captchaHtml.value = captchaText.value.split('').map(char => {
    const color = colors[Math.floor(Math.random() * colors.length)]
    const rotate = Math.random() * 30 - 15
    return `<span style="color:${color};transform:rotate(${rotate}deg)">${char}</span>`
  }).join('')
}

// 刷新验证码
const refreshCaptcha = () => {
  generateCaptcha()
  formData.captcha = ''
}

// 在组件挂载时生成验证码
onMounted(() => {
  generateCaptcha()
})

// 表单数据对象：包含所有表单字段
const formData = reactive({
  username: '',        // 用户名
  name: '',            // 用户姓名
  idType: '',          // 证件类型
  idNumber: '',        // 证件号码
  phone: '',          // 手机号
  password: '',       // 密码
  registerPassword: '', //注册密码
  confirmPassword: '', // 确认密码
  introduction: '',   // 个人简介
  remember: false,     // 记住密码选项
  captcha: ''          // 验证码
})

// 获取证件号码输入提示
const getIdNumberPlaceholder = computed(() => {
  switch (formData.idType) {
    case 'ID_CARD':
      return '请输入18位身份证号码'
    case 'PASSPORT':
      return '请输入护照号码'
    case 'MILITARY_ID':
      return '请输入军官证号码'
    default:
      return '请输入证件号码'
  }
})

// 证件号码验证规则
const validateIdNumber = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入证件号码'))
  }

  switch (formData.idType) {
    case 'ID_CARD':
      if (!/^\d{17}[\dXx]$/.test(value)) {
        callback(new Error('请输入正确的身份证号码'))
      }
      break
    case 'PASSPORT':
      if (!/^[A-Za-z0-9]{8,}$/.test(value)) {
        callback(new Error('请输入正确的护照号码'))
      }
      break
    case 'MILITARY_ID':
      if (!/^[A-Za-z0-9]{7,}$/.test(value)) {
        callback(new Error('请输入正确的军官证号码'))
      }
      break
  }
  callback()
}

/**
 * 密码验证规则
 * 1. 长度至少6位
 * 2. 必须包含至少两个数字
 * 3. 不能全为大写或小写字母
 */
const validatePassword = (rule, value, callback) => {
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

/**
 * 确认密码验证
 * 验证两次输入的密码是否一致
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== formData.registerPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 定义表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名长度至少为2位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入用户姓名', trigger: 'blur' },
    { min: 2, message: '用户姓名长度至少为2位', trigger: 'blur' }
  ],
  idType: [
    { required: true, message: '请选择证件类型', trigger: 'change' }
  ],
  idNumber: [
    { required: true, message: '请输入证件号码', trigger: 'blur' },
    { validator: validateIdNumber, trigger: 'blur' }
  ],
  phone: [
    { required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
  registerPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  introduction: [
    { required: false, message: '请输入个人简介', trigger: 'blur' },
    { max: 200, message: '简介长度不能超过200字', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

/**
 * 切换登录/注册状态
 * 切换时重置表单数据
 */
const switchLoginType = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

/**
 * 处理忘记密码功能
 * 当前仅显示提示信息
 */
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

// 登录方法
const login = async () => {
  if (formData.captcha.toLowerCase() !== captchaText.value.toLowerCase()) {
    ElMessage.error('验证码错误，请重新输入')
    refreshCaptcha()
    return
  }
  refreshCaptcha()
  try {
    const response = await axios.post('http://localhost:8088/api/users/login', {
      username: formData.username,
      password: formData.password
    })
    if (response.data.status === 'success') {
      ElMessage.success(response.data.message)
      console.log(response.data)
    } else {
      ElMessage.error(response.data.message)
      console.error(response.data)
    }
  } catch (error) {
    ElMessage.error('登录失败，请稍后再试')
    console.error(error)
  }
}

// 注册方法
const register = async () => {
  try {
    const response = await axios.post('http://localhost:8088/api/users/register', {
      username: formData.username,
      name: formData.name,
      password: formData.registerPassword,
      idType: formData.idType,
      idNumber: formData.idNumber,
      phone: formData.phone,
      bio: formData.introduction
    })
    if (response.data.status === 'success') {
      ElMessage.success('注册成功！')
      console.log(response.data)
      isLogin.value = true
    } else {
      ElMessage.error(response.data.message)
      console.error(response.data)
    }
  } catch (error) {
    ElMessage.error('注册失败，请稍后再试')
    console.error(error)
  }
}

// 表单提交处理
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (isLogin.value) {
      await login()
    } else {
      await register()
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
/* 登录容器样式：全屏显示，背景图设置 */
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('../assets/background.png');
  background-size: cover;
  background-attachment: fixed;
}

/* 登录表单卡片样式：磨砂玻璃效果 */
.login-form {
  width: 100%;
  max-width: 350px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(3px);
  transition: transform 0.3s ease;
}

/* 表单悬停动画效果 */
.login-form:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

/* 标题样式 */
.title {
  text-align: center;
  margin-bottom: 35px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 1px;
}

:deep(.el-input) {
  width: 80%;
}

:deep(.el-select) {
  width: 80%;
}

:deep(.el-select__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
  padding: 0 10px;
}

:deep(.el-select__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 输入框容器样式 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.el-input__wrapper.is-error) {
  box-shadow: 0 0 0 1px #f56c6c;
}

/* 提交按钮样式：渐变背景 */
.submit-btn {
  width: 60%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(45deg, #409eff, #5caaff);
  border: none;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-radio-group) {
  width: 90%;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.el-radio__label) {
  color: #2c3e50;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #409eff;
  border-color: #409eff;
}

.remember-forgot {
  display: flex;
  justify-content: center;
  /* 改为居中对齐 */
  align-items: center;
  margin-bottom: 20px;
  gap: 50px;
  /* 添加间距 */
}

:deep(.el-checkbox__label) {
  color: #606266;
}

.switch-type {
  text-align: center;
  margin-top: 25px;
  color: #606266;
}

.switch-type .el-link {
  margin-left: 8px;
  font-weight: 500;
}

:deep(.el-textarea .el-textarea__inner) {
  width: 80%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
  padding: 5px 10px;
  /* 减小文本域内边距 */
}

:deep(.el-textarea .el-textarea__inner:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-textarea .el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.el-input__password-icon) {
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  transition: color 0.3s;
}

:deep(.el-input__password-icon:hover) {
  color: #409EFF;
}

/* 响应式布局适配 */
@media screen and (max-width: 480px) {
  .login-form {
    margin: 15px;
    padding: 25px;
    border-radius: 12px;
  }

  .title {
    font-size: 24px;
    margin-bottom: 25px;
  }

  :deep(.el-form-item) {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-code {
  width: 100px;
  height: 40px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  letter-spacing: 4px;
  cursor: pointer;
  user-select: none;
  border-radius: 4px;
}
</style>