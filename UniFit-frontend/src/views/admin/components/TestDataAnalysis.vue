<template>
  <div class="test-data-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <!-- 统计卡片 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="总体测人数" 
            :value="stats.totalStudents"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="平均成绩" 
            :value="stats.avgScore"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="优秀率" 
            :value="stats.excellentRate"
            :value-style="{ color: '#faad14' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="及格率" 
            :value="stats.passRate"
            :value-style="{ color: '#f5222d' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 体测项目分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="体测项目成绩分布" :loading="chartLoading">
          <div id="testItemChart" style="height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 成绩等级分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="成绩等级分布" :loading="chartLoading">
          <div id="gradeChart" style="height: 400px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表 -->
    <a-card title="体测项目详情" style="margin-top: 16px;" :loading="tableLoading">
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
  totalStudents: 156,
  avgScore: 78.5,
  excellentRate: '32.5%',
  passRate: '94.2%',
});

const columns = [
  { title: '体测项目', dataIndex: 'itemName', key: 'itemName', width: 120 },
  { title: '参与人数', dataIndex: 'participantCount', key: 'participantCount', width: 100 },
  { title: '平均成绩', dataIndex: 'avgScore', key: 'avgScore', width: 100 },
  { title: '最高成绩', dataIndex: 'maxScore', key: 'maxScore', width: 100 },
  { title: '最低成绩', dataIndex: 'minScore', key: 'minScore', width: 100 },
  { title: '优秀率', dataIndex: 'excellentRate', key: 'excellentRate', width: 100 },
  { title: '及格率', dataIndex: 'passRate', key: 'passRate', width: 100 },
];

const tableData = [
  {
    itemName: '引体向上',
    participantCount: 156,
    avgScore: 8.2,
    maxScore: 20,
    minScore: 0,
    excellentRate: '35.2%',
    passRate: '92.3%',
  },
  {
    itemName: '1000米跑',
    participantCount: 156,
    avgScore: 4.15,
    maxScore: 5.0,
    minScore: 3.2,
    excellentRate: '28.8%',
    passRate: '96.2%',
  },
  {
    itemName: '800米跑',
    participantCount: 156,
    avgScore: 3.45,
    maxScore: 4.2,
    minScore: 2.8,
    excellentRate: '31.4%',
    passRate: '95.5%',
  },
  {
    itemName: '立定跳远',
    participantCount: 156,
    avgScore: 2.35,
    maxScore: 2.8,
    minScore: 1.5,
    excellentRate: '32.1%',
    passRate: '93.6%',
  },
  {
    itemName: '仰卧起坐',
    participantCount: 156,
    avgScore: 42.3,
    maxScore: 60,
    minScore: 20,
    excellentRate: '33.3%',
    passRate: '94.9%',
  },
];

const drawTestItemChart = () => {
  chartLoading.value = true;
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('testItemChart'));
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['引体向上', '1000米跑', '800米跑', '立定跳远', '仰卧起坐'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: [8.2, 4.15, 3.45, 2.35, 42.3],
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

const drawGradeChart = () => {
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('gradeChart'));
    const option = {
      tooltip: { trigger: 'item' },
      series: [
        {
          data: [
            { value: 50, name: '优秀' },
            { value: 100, name: '良好' },
            { value: 4, name: '及格' },
            { value: 2, name: '不及格' },
          ],
          type: 'pie',
          radius: '50%',
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
  }, 300);
};

onMounted(() => {
  drawTestItemChart();
  drawGradeChart();
});
</script>

<style scoped>
.test-data-analysis {
  width: 100%;
}
</style>
