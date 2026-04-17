<template>
  <div class="checkin-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="总打卡数" :value="stats.totalCheckins" :value-style="{ color: '#1890ff' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="活跃用户" :value="stats.activeUsers" :value-style="{ color: '#52c41a' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="平均时长" :value="stats.avgDuration" :value-style="{ color: '#faad14' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="打卡率" :value="stats.checkinRate" :value-style="{ color: '#f5222d' }" /></a-card></a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="12"><a-card title="打卡趋势（最近30天）" :loading="chartLoading"><div id="checkinTrendChartAnalysis" style="height: 400px;"></div></a-card></a-col>
      <a-col :xs="24" :lg="12"><a-card title="打卡时段分布" :loading="chartLoading"><div id="timeDistributionChartAnalysis" style="height: 400px;"></div></a-card></a-col>
    </a-row>

    <a-card title="打卡统计详情" style="margin-top: 16px;" :loading="tableLoading">
      <a-table :columns="columns" :data-source="tableData" :pagination="false" size="small" row-key="date" />
    </a-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import * as echarts from 'echarts';
import { getAnalysisOverview } from '../../../api';

const chartLoading = ref(false);
const tableLoading = ref(false);
const tableData = ref([]);
const trendData = ref([]);
const timeDistribution = ref([]);
let trendChart = null;
let timeChart = null;
const resizeHandlers = [];

const stats = reactive({
  totalCheckins: 0,
  activeUsers: 0,
  avgDuration: 0,
  checkinRate: '0.0%',
});

const columns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '打卡数', dataIndex: 'checkinCount', key: 'checkinCount', width: 100 },
  { title: '活跃用户', dataIndex: 'activeUserCount', key: 'activeUserCount', width: 100 },
  { title: '平均时长', dataIndex: 'avgDuration', key: 'avgDuration', width: 100 },
  { title: '最长时长', dataIndex: 'maxDuration', key: 'maxDuration', width: 100 },
  { title: '最短时长', dataIndex: 'minDuration', key: 'minDuration', width: 100 },
];

const bindResize = (chart) => {
  const handler = () => chart?.resize();
  window.addEventListener('resize', handler);
  resizeHandlers.push(handler);
};

const drawCharts = async () => {
  await nextTick();
  const trendDom = document.getElementById('checkinTrendChartAnalysis');
  const timeDom = document.getElementById('timeDistributionChartAnalysis');
  if (!trendDom || !timeDom) return;

  trendChart?.dispose();
  timeChart?.dispose();

  trendChart = echarts.init(trendDom);
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendData.value.map((item) => item.date) },
    yAxis: { type: 'value' },
    series: [{ data: trendData.value.map((item) => item.checkinCount || 0), type: 'line', smooth: true, itemStyle: { color: '#1890ff' }, areaStyle: { color: 'rgba(24, 144, 255, 0.2)' } }],
  });
  bindResize(trendChart);

  timeChart = echarts.init(timeDom);
  timeChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: timeDistribution.value.map((item) => item.label) },
    yAxis: { type: 'value' },
    series: [{ data: timeDistribution.value.map((item) => item.count || 0), type: 'bar', itemStyle: { color: '#faad14' } }],
  });
  bindResize(timeChart);
};

const load = async () => {
  chartLoading.value = true;
  tableLoading.value = true;
  try {
    const res = await getAnalysisOverview();
    const data = res?.data?.checkinStats || {};
    stats.totalCheckins = data.totalCheckins || 0;
    stats.activeUsers = data.activeUsers || 0;
    stats.avgDuration = data.avgDuration || 0;
    stats.checkinRate = data.checkinRate || '0.0%';
    tableData.value = data.details || [];
    trendData.value = [...tableData.value].reverse();
    timeDistribution.value = data.timeDistribution || [];
    await drawCharts();
  } finally {
    chartLoading.value = false;
    tableLoading.value = false;
  }
};

onMounted(load);
onBeforeUnmount(() => {
  resizeHandlers.forEach((handler) => window.removeEventListener('resize', handler));
  trendChart?.dispose();
  timeChart?.dispose();
});
</script>

<style scoped>
.checkin-analysis {
  width: 100%;
}
</style>
