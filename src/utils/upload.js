import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 上传单个文件
 * @param {File} file - 文件对象
 * @param {string} token - 认证token
 * @returns {Promise<string>} 文件URL
 */
async function uploadSingleFile(file, token) {
  if (file.url.startsWith('http://localhost:8088/') || file.url.startsWith('http://10.29.39.146:8088')) {
    return file.url // 文件已经上传过
  }

  const data = new FormData()
  data.append('file', file.raw)
  data.append('token', token)

  const response = await axios.post('http://10.29.39.146:8088/api/files/upload', data)
  if (response.data.status === 'success') {
    console.log(response.data)
    return response.data.file_url
  } else {
    ElMessage.error(response.data.message)
    console.error(response.data)
    throw new Error(response.data.message)
  }
}

/**
 * 批量上传文件
 * @param {Array} files - 文件数组
 * @param {string} token - 认证token
 * @returns {Promise<Array>} 上传成功的文件URL数组
 */
export async function uploadFiles(files, token) {
  try {
    const promises = files.map(file => uploadSingleFile(file, token))
    const uploadUrls = await Promise.all(promises)
    return uploadUrls
  } catch (error) {
    ElMessage.error('文件上传失败，请稍后再试')
    console.error(error)
    return []
  }
}