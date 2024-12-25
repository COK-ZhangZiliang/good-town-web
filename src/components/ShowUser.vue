<template>
  <div class="user-list">
    <el-table :data="currentPageData" border style="width: 100%" v-loading="loading" :header-cell-style="{
      background: '#f5f7fa',
      color: '#606266',
      textAlign: 'center',
      fontWeight: 'bold'
    }" :cell-style="{ textAlign: 'center' }" highlight-current-row>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="idType" label="证件类型" width="120">
        <template #default="{ row }">
          {{ getIdTypeText(row.idType) }}
        </template>
      </el-table-column>
      <el-table-column prop="idNumber" label="证件号码" width="180" />
      <el-table-column prop="bio" label="个人简介" />
      <el-table-column prop="userType" label="是否管理员" width="100">
        <template #default="{ row }">
          {{ row.userType === 1 ? '是' : '否' }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5, 10, 20]"
        :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { getToken } from '../utils/auth'

const loading = ref(false)
const userInfo = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 获取所有用户信息
const getUserInfo = async () => {
  loading.value = true
  try {
    const response = await axios.get('http://10.29.39.146:8088/api/users/all', {
      headers: { token: getToken() }
    })
    if (response.data.status === 'success') {
      userInfo.value = response.data.data
      total.value = response.data.data.length
      console.log(response.data)
    } else {
      ElMessage.error(response.data.message)
      console.log(response.data)
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 获取当前页面数据
const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return (userInfo.value || []).slice(start, end)
})

// 类型转换
const ID_TYPE_MAP = {
  'ID_CARD': '身份证',
  'PASSPORT': '护照',
  'MILITARY_ID': '军官证'
}
const getIdTypeText = (type) => {
  return ID_TYPE_MAP[type] || '其他'
}

onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.user-list {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  :deep(.el-table) {
    margin-bottom: 20px;

    .el-table__row {
      transition: all 0.3s;

      &:hover {
        background-color: #f0f9ff !important;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>