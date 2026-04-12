<template>
  <div class="class-comparison-analysis">
    <a-card title="班级对比分析">
      <!-- 筛选 -->
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="selectedClasses" 
          placeholder="选择班级" 
          style="width: 300px"
          mode="multiple"
          @change="loadComparison"
        >
          <a-select-option value="class1">计算机1班</a-select-option>
          <a-select-option value="class2">计算机2班</a-select-option>
          <a-select-option value="class3">计算机3班</a-select-option>
          <a-select-option value="class4">计算机4班</a-select-option>
        </a-select>
        <a-select 
          v-model:value="comparisonMetric" 
          placeholder="对比指标" 
          style="width: 200px"
          @change="loadComparison"
        >
          <a-select-option value="checkin">打卡率</a-select-option>
          <a-select-option value="avgScore">平均成绩</a-select-option>
          <a-select-option value="avgDuration">平均时长</a-select-option>
        </a-select>
      </a-space>

      <!-- 对比图表 -->
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :xs="24" :lg="12">
          <a-card title="班级对比" :loading="chartLoading">
            <div id="classComparisonChart" style="height: 400px;"></div>
          </a-card>
        </a-col>

        <!-- 班级排行 -->
        <a-col :xs="24" :lg="12">
          <a-card title="班级排行" :loading="tableLoading">
            <a-table 
              :columns="rankColumns" 
              :data-source="classRanking"
              :pagination="false"
              size="small"
            >
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'rank'">
                  <a-tag :color="getRankColor(index)">{{ index + 1 }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <!-- 详细对比表 -->
      <a-card title="详细对比数据" :loading="tableLoading">
        <a-table 
          :columns="detailColumns" 
          :data-source="detailData"
          :pagination="false"
          size="small"
        />
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import * as echarts from 'echarts';

const chartLoading = ref(false);
const tableLoading = ref(false);
const selectedClasses = ref(['class1', 'class2', 'class3']);
const comparisonMetric = ref('checkin');

const rankColumns = [
  { title: '排名', key: 'rank', width: 80 },
  { title: '班级', dataIndex: 'className', key: 'className', width: 120 },
  { title: '数值', dataIndex: 'value', key: 'value', width: 100 },
];

const detailColumns = [
  { title: '班级', dataIndex: 'className', key: 'className', width: 120 },
  { title: '人数', dataIndex: 'studentCount', key: 'studentCount', width: 100 },
  { title: '打卡率', dataIndex: 'checkinRate', key: 'checkinRate', width: 100 },
  { title: '平均成绩', dataIndex: 'avgScore', key: 'avgScore', width: 100 },
  { title: '平均时长', dataIndex: 'avgDuration', key: 'avgDuration', width: 100 },
  { title: '优秀率', dataIndex: 'excellentRate', key: 'excellentRate', width: 100 },
];

const classRanking = ref([
  { className: '计算机1班', value: '92.5%' },
  { className: '计算机2班', value: '88.3%' },
  { className: '计算机3班', value: '85.6%' },
]);

const detailData = ref([
  {
    className: '计算机1班',
    studentCount: 45,
    checkinRate: '92.5%',
    avgScore: 78.5,
    avgDuration: '45分钟',
    excellentRate: '35.6%',
  },
  {
    className: '计算机2班',
    studentCount: 42,
    checkinRate: '88.3%',
    avgScore: 76.2,
    avgDuration: '42分钟',
    excellentRate: '31.0%',
  },
  {
    className: '计算机3班',
    studentCount: 48,
    checkinRate: '85.6%',
    avgScore: 74.8,
    avgDuration: '40分钟',
    excellentRate: '28.5%',
  },
  {
    className: '计算机4班',
    studentCount: 41,
    checkinRate: '82.1%',
    avgScore: 72.3,
    avgDuration: '38分钟',
    excellentRate: '24.4%',
  },
]);

const getRankColor = (index) => {
  if (index === 0) return 'gold';
  if (index === 1) return 'silver';
  if (index === 2) return '#cd7f32';
  return 'default';
};

const loadComparison = () => {
  drawComparisonChart();
};

const drawComparisonChart = () => {
  chartLoading.value = true;
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('classComparisonChart'));
    
    let data = [];
    let yAxisName = '';
    
    if (comparisonMetric.value === 'checkin') {
      data = [92.5, 88.3, 85.6];
      yAxisName = '打卡率(%)';
    } else if (comparisonMetric.value === 'avgScore') {
      data = [78.5, 76.2, 74.8];
      yAxisName = '平均成绩';
    } else {
      data = [45, 42, 40];
      yAxisName = '平均时长(分钟)';
    }

    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['计算机1班', '计算机2班', '计算机3班'],
      },
      yAxis: { 
        type: 'value',
        name: yAxisName,
      },
      series: [
        {
          data: data,
          type: 'bar',
          itemStyle: { color: '#1890ff' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
    chartLoading.value = false;
  }, 300);
};

onMounted(() => {
  drawComparisonChart();
});
</script>

<style scoped>
.class-comparison-analysis {
  width: 100%;
}
</style>
