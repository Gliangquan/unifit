<template>
  <div class="test-data-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="总体测人数" :value="stats.totalStudents" :value-style="{ color: '#1890ff' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="平均成绩" :value="stats.avgScore" :value-style="{ color: '#52c41a' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="优秀率" :value="stats.excellentRate" :value-style="{ color: '#faad14' }" /></a-card></a-col>
      <a-col :xs="24" :sm="12" :lg="6"><a-card><a-statistic title="及格率" :value="stats.passRate" :value-style="{ color: '#f5222d' }" /></a-card></a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <a-card title="体测项目成绩分布" :loading="chartLoading">
          <div id="testItemChart" style="height: 400px;"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="成绩等级分布" :loading="chartLoading">
          <div id="gradeChart" style="height: 400px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="体测项目详情" style="margin-top: 16px;" :loading="tableLoading">
      <a-table :columns="columns" :data-source="tableData" :pagination="false" size="small" row-key="itemCode" />
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
const gradeData = ref([]);
let testItemChart = null;
let gradeChart = null;
const resizeHandlers = [];

const stats = reactive({
  totalStudents: 0,
  avgScore: 0,
  excellentRate: '0.0%',
  passRate: '0.0%',
});

const columns = [
  { title: '体测项目', dataIndex: 'itemName', key: 'itemName', width: 140 },
  { title: '参与人数', dataIndex: 'participantCount', key: 'participantCount', width: 100 },
  { title: '平均成绩', dataIndex: 'avgScore', key: 'avgScore', width: 100 },
  { title: '最高成绩', dataIndex: 'maxScore', key: 'maxScore', width: 100 },
  { title: '最低成绩', dataIndex: 'minScore', key: 'minScore', width: 100 },
  { title: '优秀率', dataIndex: 'excellentRate', key: 'excellentRate', width: 100 },
  { title: '及格率', dataIndex: 'passRate', key: 'passRate', width: 100 },
];

const bindResize = (chart) => {
  const handler = () => chart?.resize();
  window.addEventListener('resize', handler);
  resizeHandlers.push(handler);
};

const waitForVisible = async (id, retry = 12) => {
  for (let i = 0; i < retry; i += 1) {
    await nextTick();
    const dom = document.getElementById(id);
    if (dom && dom.clientWidth > 0 && dom.clientHeight > 0) {
      return dom;
    }
    await new Promise((resolve) => setTimeout(resolve, 80));
  }
  return document.getElementById(id);
};

const initChart = (dom) => {
  if (!dom) return null;
  const instance = echarts.getInstanceByDom(dom);
  return instance || echarts.init(dom);
};

const drawCharts = async () => {
  const itemDom = await waitForVisible('testItemChart');
  const gradeDom = await waitForVisible('gradeChart');
  if (!itemDom || !gradeDom || !itemDom.clientWidth || !gradeDom.clientWidth) return;

  testItemChart?.dispose();
  gradeChart?.dispose();

  testItemChart = initChart(itemDom);
  testItemChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: tableData.value.map((item) => item.itemName) },
    yAxis: { type: 'value' },
    series: [{ data: tableData.value.map((item) => Number(item.avgScore || 0)), type: 'bar', itemStyle: { color: '#1890ff' } }],
  });
  bindResize(testItemChart);

  gradeChart = initChart(gradeDom);
  gradeChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '55%', data: gradeData.value.map((item) => ({ value: item.value, name: item.name })) }],
  });
  bindResize(gradeChart);
};

const load = async () => {
  chartLoading.value = true;
  tableLoading.value = true;
  try {
    const res = await getAnalysisOverview();
    const data = res?.data || {};
    stats.totalStudents = data.totalStudents || 0;
    stats.avgScore = data.avgScore || 0;
    stats.excellentRate = data.excellentRate || '0.0%';
    stats.passRate = data.passRate || '0.0%';
    tableData.value = data.testItemStats || [];
    gradeData.value = data.gradeDistribution || [];
  } finally {
    chartLoading.value = false;
    tableLoading.value = false;
  }
  await nextTick();
  await drawCharts();
};

onMounted(async () => {
  await nextTick();
  await load();
});


onBeforeUnmount(() => {
  resizeHandlers.forEach((handler) => window.removeEventListener('resize', handler));
  testItemChart?.dispose();
  gradeChart?.dispose();
});
</script>

<style scoped>
.test-data-analysis {
  width: 100%;
}
</style>
