<template>
  <div class="statistics">
    <el-card>
      <div class="filter-panel">
        <el-form :inline="true" @submit.prevent="fetchStatistics">
          <el-form-item label="起始年月">
            <el-date-picker v-model="filters.startMonth" type="month" placeholder="选择起始年月" format="YYYY-MM"
              value-format="YYYY-MM" style="width: 150px;"></el-date-picker>
          </el-form-item>
          <el-form-item label="结束年月">
            <el-date-picker v-model="filters.endMonth" type="month" placeholder="选择结束年月" format="YYYY-MM"
              value-format="YYYY-MM" style="width: 150px;"></el-date-picker>
          </el-form-item>
          
          <br>
          <el-form-item label="省份" prop="province">
            <el-select v-model="filters.province" @change="handleProvinceChange" placeholder="选择省份" style="width: 100px;">
              <el-option v-for="item in provinces" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市" prop="city" >
            <el-select v-model="filters.city" placeholder="选择城市" style="width: 100px;">
              <el-option v-for="item in cities" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchStatistics">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="chart-panel">
        <div id="town-chart" style="width: 100%; height: 400px;"></div>
      </div>

      <el-table :data="statisticsData" style="width: 100%; margin-top: 20px">
        <el-table-column prop="townName" label="乡镇名称" width="150"></el-table-column>
        <el-table-column prop="city" label="城市" width="150"></el-table-column>
        <el-table-column prop="province" label="省份" width="150"></el-table-column>
        <el-table-column prop="month" label="月份" width="150"></el-table-column>
        <el-table-column prop="totalPublicityUsers" label="宣传用户总数" width="150"></el-table-column>
        <el-table-column prop="totalAssistanceUsers" label="帮扶用户总数" width="150"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import pcData from '@/assets/pc.json'

const filters = ref({
  startMonth: '', // 格式为 "yyyy-MM"
  endMonth: '',
  province: '',
  city: ''
})

const statisticsData = ref([])
var chartInstance

const fetchStatistics = async () => {
  try {
    // 检查并格式化过滤器
    const { startMonth, endMonth, province, city } = filters.value

    if (!startMonth || !endMonth) {
      ElMessage.warning('请选择起始年月和结束年月')
      return
    }
    if (dayjs(endMonth).isBefore(dayjs(startMonth))) {
      this.$message.warning("结束年月不能早于起始年月");
      return;
    }

    const formattedFilters = {
      startMonth,
      endMonth,
      province: province.trim() || null,
      city: city.trim() || null,
    };

    const response = await axios.get("http://10.29.39.146:8088/api/statistics/towns", {
      params: formattedFilters,
    });

    // 检查数据是否为数组
    if (!Array.isArray(response.data)) {
      console.error("后端返回的数据格式不正确：", response.data);
      ElMessage.error("统计数据格式错误");
      return;
    }

    statisticsData.value = response.data;
    updateChart();
  }
  catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}


const updateChart = () => {
  const chartElement = document.getElementById("town-chart");
  if (!chartElement) {
    console.error("无法找到图表的 DOM 元素");
    return;
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartElement);
  }

  // 按月份汇总数据
  const groupedData = {};

  statisticsData.value.forEach((item) => {
    const month = item.month;
    if (!groupedData[month]) {
      groupedData[month] = {
        totalPublicityUsers: 0,
        totalAssistanceUsers: 0,
      };
    }
    groupedData[month].totalPublicityUsers += item.totalPublicityUsers || 0;
    groupedData[month].totalAssistanceUsers += item.totalAssistanceUsers || 0;
  });

  // 按月份排序
  const sortedMonths = Object.keys(groupedData).sort();

  const publicityData = sortedMonths.map((month) => groupedData[month].totalPublicityUsers);
  const assistanceData = sortedMonths.map((month) => groupedData[month].totalAssistanceUsers);

  const option = {
    title: {
      text: "月累计宣传与帮扶用户数趋势",
      left: "center",
    },
    tooltip: {
      trigger: "axis",
    },
    legend: {
      data: ["宣传用户总数", "帮扶用户总数"],
      top: "bottom",
    },
    xAxis: {
      type: "category",
      data: sortedMonths,
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: "宣传用户总数",
        type: "line",
        data: publicityData,
        smooth: true,
        color: "#5470C6",
      },
      {
        name: "帮扶用户总数",
        type: "line",
        data: assistanceData,
        smooth: true,
        color: "#91CC75",
      },
    ],
  };

  if (publicityData.length === 0 && assistanceData.length === 0) {
    console.warn("没有可用的数据来渲染图表");
    return;
  }

  chartInstance.setOption(option);
}

const setDefaultDates = () => {
  const end = dayjs();
  const start = end.subtract(5, "month");
  filters.value.endMonth = end.format("YYYY-MM");
  filters.value.startMonth = start.format("YYYY-MM");
}

onMounted(() => {
  chartInstance = echarts.init(document.getElementById("town-chart"));
  window.addEventListener("resize", () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  });
  setDefaultDates();
  fetchStatistics();
});

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose();
  }
  window.removeEventListener("resize", () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  });
});

const provinces = Object.keys(pcData)
const cities = ref([])

const handleProvinceChange = (value) => {
  cities.value = pcData[value] || []
}
</script>

<style scoped>
.statistics {
  margin-top: 20px;
  padding: 10px;
}

.filter-panel {
  margin-bottom: 20px;
}

.chart-panel {
  margin-top: 20px;
}
</style>
