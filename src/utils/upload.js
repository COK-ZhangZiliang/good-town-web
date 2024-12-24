import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 上传文件
 * @param {Array} files - 文件数组
 * @param {string} token - 认证token
 * @returns {Promise<Array>} 上传成功的文件URL数组
 */
export async function uploadFiles(files, token) {
  const uploadUrls = []

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    const data = new FormData()
    data.append('file', file.raw)
    data.append('token', token)

    try {
      const response = await axios.post('http://10.29.39.146:8088/api/files/upload', data)
      if (response.data.status === 'success') {
        uploadUrls.push(response.data.file_url)
        console.log(response.data)
      } else {
        ElMessage.error(response.data.message)
        console.error(response.data)
      }
    } catch (error) {
      ElMessage.error('文件上传失败，请稍后再试')
      console.error(error)
    }
  }

  return uploadUrls
}