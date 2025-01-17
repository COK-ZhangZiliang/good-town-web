<!-- 宣传卡片 -->
<template>
  <div class="publicity-item">
    <!-- 头部：用户头像、用户名、更新时间 -->
    <div class="header">
      <template v-if="user.avatarUrl">
        <el-avatar :size="40" class="avatar" :class="{ 'clickable': props.type === 'adminQuery' }"
          @click="handleAvatarClick">
          <img :src="user.avatarUrl" />
        </el-avatar>
      </template>
      <template v-else>
        <el-avatar :size="40" class="avatar" :class="{ 'clickable': props.type === 'adminQuery' }"
          @click="handleAvatarClick">
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

    <!-- 图片、视频展示 -->
    <div class="media-container">
      <el-image v-for="(url, index) in imageUrls" :key="index" :src="url" :preview-src-list="imageUrls" fit="cover"
        class="media-item" />
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
        <el-button v-if="props.type === 'allPromotions'" type="primary" @click="handleSupport">
          <el-icon>
            <Star />
          </el-icon>
          助力
        </el-button>

        <template v-if="props.type === 'myPromotions'">
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
    </div>
    <div class="support-users" v-if="acceptedAssist.length">
      <span class="support-label">已接受助力:</span>
      <span v-for="(ass, index) in acceptedAssist" :key="index" class="support-name" @click="showAssistDetail(ass)">
        {{ ass.user_name }}{{ index < acceptedAssist.length - 1 ? "," : "" }} </span>
    </div>

    <div class="support-users pending" v-if="pendingAssist.length">
      <span class="support-label">待接受助力:</span>
      <span v-for="(ass, index) in pendingAssist" :key="index" class="support-name pending"
        @click="showAssistDetail(ass)">
        {{ ass.user_name }}{{ index < pendingAssist.length - 1 ? "," : "" }} </span>
    </div>
  </div>

  <!-- 用户信息对话框 -->
  <el-dialog v-model="userInfoVisible" title="用户信息" width="400px">
    <div class="user-detail">
      <div class="user-avatar">
        <el-avatar :size="80" :src="user.avatarUrl">
          <el-icon v-if="!user.avatarUrl">
            <UserFilled />
          </el-icon>
        </el-avatar>
      </div>

      <div class="user-info">
        <div class="info-item">
          <div class="label">用户名</div>
          <div class="value">{{ user.username }}</div>
        </div>

        <div class="info-item">
          <div class="label">手机号</div>
          <div class="value">{{ user.phone }}</div>
        </div>

        <div class="info-item">
          <div class="label">个人简介</div>
          <div class="value bio">{{ user.bio || '暂无简介' }}</div>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 助力信息详情对话框 -->
  <AssistanceInfo v-model:dialog-visible="assistanceInfoVisible" :assistance="selectedAssistance"
    @success="handleAssistSuccess" />

  <!-- 创建助力 -->
  <CreateAssistance v-if="supportVisible" v-model:visible="supportVisible" :publicity_id="content.publicity_id"
    @success="handleAssistSuccess" />

  <!-- 修改宣传信息 -->
  <CreatePublicity v-if="editVisible" v-model:visible="editVisible" :editData="content" @success="handleEditSuccess" />
</template>

<script setup>
import { computed, defineProps, ref, defineEmits } from "vue";
import { Star, Edit, Delete, UserFilled } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AssistanceInfo from "@/components/AssistanceInfo.vue";
import { getToken } from "@/utils/auth";
import axios from "axios";
import CreatePublicity from "@/components/CreatePublicity.vue";
import CreateAssistance from "@/components/CreateAssistance.vue";

// 接收数据
const props = defineProps({
  content: {
    type: Object,
    required: true,
    default: () => ({}),
  },
  type: {
    type: String,
    required: true,
  },
  username: {
    type: String,
    required: true,
  },
});

const emits = defineEmits(['refresh']);

const content = computed(() => {
  return props.content;
});
const imageUrls = computed(() => content.value.image_url || []);
const videoUrls = computed(() => content.value.video_url || []);
const user = computed(() => content.value.user || {});
const assist = computed(() => content.value.assistance_requests || {});

const acceptedAssist = computed(() => assist.value.filter(item => item.status === 1))
const pendingAssist = computed(() => assist.value.filter(item => item.status === 0))

const assistanceInfoVisible = ref(false);
const selectedAssistance = ref(null);

const editVisible = ref(false);

const userInfoVisible = ref(false);

// 展示详细助力信息
const showAssistDetail = (ass) => {
  assistanceInfoVisible.value = true;
  selectedAssistance.value = ass;
};

// 类型映射
const typeMap = {
  farmhouse: "农家院",
  nature: "自然风光",
  ancient: "古建筑",
  specialty: "土特产",
  food: "特色小吃",
  folk: "民俗活动",
};

const convertedType = computed(() => {
  return typeMap[props.content.type] || props.content.type;
});

const supportVisible = ref(false);

const handleSupport = () => {
  if (user.value.username === props.username) {
    ElMessage.error("无法对自己的宣传助力");
    return;
  }
  supportVisible.value = true;
};

const handleEdit = () => {
  editVisible.value = true;
};

const handleEditSuccess = () => {
  editVisible.value = false
  emits('refresh')
}

const handleDelete = async () => {
  ElMessageBox.confirm(
    '确定要删除这条宣传信息吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await axios.delete(`http://10.29.39.146:8088/api/publicity/delete/${content.value.publicity_id}`, { headers: { token: getToken() } })
        if (response.data.status === 'success') {
          ElMessage.success('删除成功')
          emits('refresh')
        }
        else {
          console.log(response.data)
          ElMessage.error(response.data.message)
        }
      }
      catch (error) {
        ElMessage.error('删除失败，请稍后再试')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
};

const handleAssistSuccess = () => {
  emits('refresh');
};

const handleAvatarClick = () => {
  if (props.type === 'adminQuery') {
    userInfoVisible.value = true;
  }
};
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

    .clickable {
      cursor: pointer;
      transition: opacity 0.3s;

      &:hover {
        opacity: 0.8;
      }
    }
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
    width: 250px;
    height: 190px;
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
  margin-top: 5px;
  padding: 5px;
  border-radius: 6px;
  text-align: left;
  
  .support-label {
    font-weight: bold;
    color: #606266;
    margin-right: 8px;
    font-size: 14px;
  }

  .support-name {
    color: #409EFF;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    
    &:hover {
      color: #66b1ff;
      text-decoration: underline;
    }
  }

  &.pending {
    background-color: #fdf6ec;
    border: 1px solid #faecd8;
    
    .support-label {
      color: #e6a23c;
    }
    
    .support-name {
      color: #e6a23c;
      
      &:hover {
        color: #ebb563;
      }
    }
  }
}

.user-detail {
  padding: 20px;

  .user-avatar {
    text-align: center;
    margin-bottom: 20px;
  }

  .user-info {
    .info-item {
      margin-bottom: 15px;

      .label {
        color: #909399;
        font-size: 14px;
        margin-bottom: 5px;
      }

      .value {
        color: #303133;
        font-size: 14px;

        &.bio {
          white-space: pre-wrap;
          line-height: 1.5;
        }
      }
    }
  }
}
</style>
