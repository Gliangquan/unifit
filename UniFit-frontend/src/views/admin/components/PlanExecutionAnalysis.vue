<template>
  <div class="plan-execution-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="总计划数" :value="stats.totalPlans" :value-style="{ color: '#1890ff' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="完成率" :value="stats.completionRate" :value-style="{ color: '#52c41a' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="平均进度" :value="stats.avgProgress" :value-style="{ color: '#faad14' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="未完成" :value="stats.incompletePlans" :value-style="{ color: '#f5222d' }" /></a-card></a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="12"><a-card title="计划完成分布" :loading="chartLoading"><div id="planTrendChartAnalysis" style="height: 400px;"></div></a-card></a-col>
      <a-col :xs="24" :lg="12"><a-card title="计划难度分布" :loading="chartLoading"><div id="difficultyChartAnalysis" style="height: 400px;"></div></a-card></a-col>
    </a-row>

    <a-card title="计划执行详情" style="margin-top: 16px;" :loading="tableLoading">
      <a-table :columns="columns" :data-source="tableData" :pagination="false" size="small" row-key="planId">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'progress'">
            <a-progress :percent="record.progress" :status="record.progress === 100 ? 'success' : 'active'" />
          </template>
        </template>
      </a-table>
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
const difficultyDistribution = ref([]);
let progressChart = null;
let difficultyChart = null;
const resizeHandlers = [];

const stats = reactive({
  totalPlans: 0,
  completionRate: '0.0%',
  avgProgress: '0.0%',
  incompletePlans: 0,
});

const columns = [
  { title: '计划ID', dataIndex: 'planId', key: 'planId', width: 100 },
  { title: '学生', dataIndex: 'studentName', key: 'studentName', width: 120 },
  { title: '目标项目', dataIndex: 'targetItem', key: 'targetItem', width: 120 },
  { title: '难度', dataIndex: 'difficulty', key: 'difficulty', width: 80 },
  { title: '进度', key: 'progress', width: 180 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 160 },
  { title: '预计完成', dataIndex: 'endDate', key: 'endDate', width: 160 },
];

const bindResize = (chart) => {
  const handler = () => chart?.resize();
  window.addEventListener('resize', handler);
  resizeHandlers.push(handler);
};

const initChart = (dom) => {
  if (!dom) return null;
  const instance = echarts.getInstanceByDom(dom);
  return instance || echarts.init(dom);
};

const drawCharts = async () => {
  await nextTick();
  const progressDom = document.getElementById('planTrendChartAnalysis');
  const difficultyDom = document.getElementById('difficultyChartAnalysis');
  if (!progressDom || !difficultyDom) return;

  progressChart?.dispose();
  difficultyChart?.dispose();

  progressChart = initChart(progressDom);
  progressChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: tableData.value.map((item) => `#${item.planId}`) },
    yAxis: { type: 'value', max: 100 },
    series: [{ data: tableData.value.map((item) => item.progress || 0), type: 'bar', itemStyle: { color: '#52c41a' } }],
  });
  bindResize(progressChart);

  difficultyChart = initChart(difficultyDom);
  difficultyChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '55%', data: difficultyDistribution.value.map((item) => ({ name: item.name, value: item.value })) }],
  });
  bindResize(difficultyChart);
};

const load = async () => {
  chartLoading.value = true;
  tableLoading.value = true;
  try {
    const res = await getAnalysisOverview();
    const data = res?.data?.planStats || {};
    stats.totalPlans = data.totalPlans || 0;
    stats.completionRate = data.completionRate || '0.0%';
    stats.avgProgress = data.avgProgress || '0.0%';
    stats.incompletePlans = data.incompletePlans || 0;
    tableData.value = data.details || [];
    difficultyDistribution.value = data.difficultyDistribution || [];
    await drawCharts();
  } finally {
    chartLoading.value = false;
    tableLoading.value = false;
  }
};

onMounted(load);
onBeforeUnmount(() => {
  resizeHandlers.forEach((handler) => window.removeEventListener('resize', handler));
  progressChart?.dispose();
  difficultyChart?.dispose();
});
</script>

<style scoped>
.plan-execution-analysis {
  width: 100%;
}
</style>
