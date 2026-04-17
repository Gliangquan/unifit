<template>
  <div class="class-comparison-analysis">
    <a-card title="班级对比分析">
      <a-space style="margin-bottom: 16px;">
        <a-select v-model:value="selectedClasses" placeholder="选择班级" style="width: 320px" mode="multiple" @change="drawChart">
          <a-select-option v-for="item in classOptions" :key="item.classId" :value="item.classId">{{ item.className }}</a-select-option>
        </a-select>
        <a-select v-model:value="comparisonMetric" placeholder="对比指标" style="width: 200px" @change="drawChart">
          <a-select-option value="checkinRate">打卡率</a-select-option>
          <a-select-option value="avgScore">平均成绩</a-select-option>
          <a-select-option value="avgDuration">平均时长</a-select-option>
          <a-select-option value="excellentRate">优秀率</a-select-option>
        </a-select>
      </a-space>

      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :xs="24" :lg="12">
          <a-card title="班级对比" :loading="chartLoading">
            <div id="classComparisonChart" style="height: 400px;"></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="班级排行" :loading="tableLoading">
            <a-table :columns="rankColumns" :data-source="classRanking" :pagination="false" size="small" row-key="classId">
              <template #bodyCell="{ column, index }">
                <template v-if="column.key === 'rank'">
                  <a-tag :color="getRankColor(index)">{{ index + 1 }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="详细对比数据" :loading="tableLoading">
        <a-table :columns="detailColumns" :data-source="detailData" :pagination="false" size="small" row-key="classId" />
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { getClassComparisonData } from '../../../api';

const chartLoading = ref(false);
const tableLoading = ref(false);
const comparisonMetric = ref('checkinRate');
const selectedClasses = ref([]);
const detailData = ref([]);
let chartInstance = null;
const resizeHandlers = [];

const rankColumns = [
  { title: '排名', key: 'rank', width: 80 },
  { title: '班级', dataIndex: 'className', key: 'className', width: 150 },
  { title: '数值', dataIndex: 'metricValue', key: 'metricValue', width: 120 },
];

const detailColumns = [
  { title: '班级', dataIndex: 'className', key: 'className', width: 150 },
  { title: '人数', dataIndex: 'studentCount', key: 'studentCount', width: 100 },
  { title: '打卡率', dataIndex: 'checkinRate', key: 'checkinRate', width: 100 },
  { title: '平均成绩', dataIndex: 'avgScore', key: 'avgScore', width: 100 },
  { title: '平均时长', dataIndex: 'avgDuration', key: 'avgDuration', width: 100 },
  { title: '优秀率', dataIndex: 'excellentRate', key: 'excellentRate', width: 100 },
];

const classOptions = computed(() => detailData.value);
const visibleRows = computed(() => {
  if (!selectedClasses.value.length) return detailData.value;
  return detailData.value.filter((item) => selectedClasses.value.includes(item.classId));
});
const classRanking = computed(() => {
  return [...visibleRows.value]
    .map((item) => ({ ...item, metricValue: item[comparisonMetric.value] }))
    .sort((a, b) => numericValue(b[comparisonMetric.value]) - numericValue(a[comparisonMetric.value]));
});

const getRankColor = (index) => {
  if (index === 0) return 'gold';
  if (index === 1) return 'silver';
  if (index === 2) return '#cd7f32';
  return 'default';
};

const numericValue = (value) => Number(String(value || 0).replace('%', '')) || 0;

const bindResize = (chart) => {
  const handler = () => chart?.resize();
  window.addEventListener('resize', handler);
  resizeHandlers.push(handler);
};

const drawChart = async () => {
  await nextTick();
  const dom = document.getElementById('classComparisonChart');
  if (!dom) return;
  chartInstance?.dispose();
  chartInstance = echarts.init(dom);
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: visibleRows.value.map((item) => item.className) },
    yAxis: { type: 'value' },
    series: [{ data: visibleRows.value.map((item) => numericValue(item[comparisonMetric.value])), type: 'bar', itemStyle: { color: '#1890ff' } }],
  });
  bindResize(chartInstance);
};

const load = async () => {
  chartLoading.value = true;
  tableLoading.value = true;
  try {
    const res = await getClassComparisonData();
    detailData.value = res?.data || [];
    if (!selectedClasses.value.length) {
      selectedClasses.value = detailData.value.slice(0, 3).map((item) => item.classId);
    }
    await drawChart();
  } finally {
    chartLoading.value = false;
    tableLoading.value = false;
  }
};

onMounted(load);
onBeforeUnmount(() => {
  resizeHandlers.forEach((handler) => window.removeEventListener('resize', handler));
  chartInstance?.dispose();
});
</script>

<style scoped>
.class-comparison-analysis {
  width: 100%;
}
</style>
