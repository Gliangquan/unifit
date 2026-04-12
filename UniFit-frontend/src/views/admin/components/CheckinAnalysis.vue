<template>
  <div class="checkin-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <!-- 统计卡片 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="总打卡数" 
            :value="stats.totalCheckins"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="活跃用户" 
            :value="stats.activeUsers"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="平均时长" 
            :value="stats.avgDuration"
            :value-style="{ color: '#faad14' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="打卡率" 
            :value="stats.checkinRate"
            :value-style="{ color: '#f5222d' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 打卡趋势 -->
      <a-col :xs="24" :lg="12">
        <a-card title="打卡趋势（最近30天）" :loading="chartLoading">
          <div id="checkinTrendChart" style="height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 打卡时段分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="打卡时段分布" :loading="chartLoading">
          <div id="timeDistributionChart" style="height: 400px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表 -->
    <a-card title="打卡统计详情" style="margin-top: 16px;" :loading="tableLoading">
      <a-table 
        :columns="columns" 
        :data-source="tableData"
        :pagination="false"
        size="small"
      />
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import * as echarts from 'echarts';

const chartLoading = ref(false);
const tableLoading = ref(false);

const stats = reactive({
  totalCheckins: 3245,
  activeUsers: 156,
  avgDuration: '42分钟',
  checkinRate: '87.2%',
});

const columns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '打卡数', dataIndex: 'checkinCount', key: 'checkinCount', width: 100 },
  { title: '活跃用户', dataIndex: 'activeUserCount', key: 'activeUserCount', width: 100 },
  { title: '平均时长', dataIndex: 'avgDuration', key: 'avgDuration', width: 100 },
  { title: '最长时长', dataIndex: 'maxDuration', key: 'maxDuration', width: 100 },
  { title: '最短时长', dataIndex: 'minDuration', key: 'minDuration', width: 100 },
];

const tableData = [
  {
    date: '2024-02-27',
    checkinCount: 142,
    activeUserCount: 142,
    avgDuration: '45分钟',
    maxDuration: '120分钟',
    minDuration: '15分钟',
  },
  {
    date: '2024-02-26',
    checkinCount: 138,
    activeUserCount: 138,
    avgDuration: '42分钟',
    maxDuration: '110分钟',
    minDuration: '12分钟',
  },
  {
    date: '2024-02-25',
    checkinCount: 145,
    activeUserCount: 145,
    avgDuration: '44分钟',
    maxDuration: '115分钟',
    minDuration: '18分钟',
  },
  {
    date: '2024-02-24',
    checkinCount: 135,
    activeUserCount: 135,
    avgDuration: '40分钟',
    maxDuration: '105分钟',
    minDuration: '10分钟',
  },
];

const drawCheckinTrendChart = () => {
  chartLoading.value = true;
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('checkinTrendChart'));
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['2/1', '2/5', '2/10', '2/15', '2/20', '2/25', '2/27'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: [120, 132, 101, 134, 90, 130, 142],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#1890ff' },
          areaStyle: { color: 'rgba(24, 144, 255, 0.2)' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
    chartLoading.value = false;
  }, 300);
};

const drawTimeDistributionChart = () => {
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('timeDistributionChart'));
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['6-8点', '8-10点', '10-12点', '14-16点', '16-18点', '18-20点', '20-22点'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: [45, 120, 95, 110, 130, 140, 85],
          type: 'bar',
          itemStyle: { color: '#faad14' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
  }, 300);
};

onMounted(() => {
  drawCheckinTrendChart();
  drawTimeDistributionChart();
});
</script>

<style scoped>
.checkin-analysis {
  width: 100%;
}
</style>
