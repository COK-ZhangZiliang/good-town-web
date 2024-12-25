<template>
    <div class="user-list">
      <div class="table-header">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
  
      <el-table :data="userList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="id_type" label="证件类型" width="120">
          <template #default="{ row }">
            {{ row.id_type === 1 ? '身份证' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column prop="id_number" label="证件号码" width="180" />
        <el-table-column prop="bio" label="个人简介" />
        <el-table-column prop="is_admin" label="是否管理员" width="100">
          <template #default="{ row }">
            {{ row.is_admin === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
      </el-table>
  
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Refresh } from '@element-plus/icons-vue'
  import axios from 'axios'
  import { getToken } from '../utils/auth'
  
  const loading = ref(false)
  const userList = ref([])
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  const loadData = async () => {
    loading.value = true
    try {
      const response = await axios.get('http://10.29.39.146:8088/api/users', {
        params: {
          token: getToken(),
          page: currentPage.value,
          page_size: pageSize.value
        }
      })
      if (response.data.status === 'success') {
        userList.value = response.data.data.users
        total.value = response.data.data.total
      } else {
        ElMessage.error(response.data.message)
      }
    } catch (error) {
      ElMessage.error('获取用户列表失败')
    } finally {
      loading.value = false
    }
  }
  
  const refreshData = () => {
    loadData()
  }
  
  const handleSizeChange = (val) => {
    pageSize.value = val
    loadData()
  }
  
  const handleCurrentChange = (val) => {
    currentPage.value = val
    loadData()
  }
  
  onMounted(() => {
    loadData()
  })
  </script>
  
  <style lang="scss" scoped>
  .user-list {
    padding: 20px;
  
    .table-header {
      margin-bottom: 20px;
    }
  
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  </style>