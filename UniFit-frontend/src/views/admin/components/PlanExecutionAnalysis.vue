<template>
  <div class="plan-execution-analysis">
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <!-- 统计卡片 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="总计划数" 
            :value="stats.totalPlans"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="完成率" 
            :value="stats.completionRate"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="平均进度" 
            :value="stats.avgProgress"
            :value-style="{ color: '#faad14' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic 
            title="未完成" 
            :value="stats.incompletePlans"
            :value-style="{ color: '#f5222d' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 计划完成趋势 -->
      <a-col :xs="24" :lg="12">
        <a-card title="计划完成趋势（最近30天）" :loading="chartLoading">
          <div id="planTrendChart" style="height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 计划难度分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="计划难度分布" :loading="chartLoading">
          <div id="difficultyChart" style="height: 400px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表 -->
    <a-card title="计划执行详情" style="margin-top: 16px;" :loading="tableLoading">
      <a-table 
        :columns="columns" 
        :data-source="tableData"
        :pagination="false"
        size="small"
      >
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
import { onMounted, reactive, ref } from 'vue';
import * as echarts from 'echarts';

const chartLoading = ref(false);
const tableLoading = ref(false);

const stats = reactive({
  totalPlans: 245,
  completionRate: '78.4%',
  avgProgress: '72.3%',
  incompletePlans: 53,
});

const columns = [
  { title: '计划ID', dataIndex: 'planId', key: 'planId', width: 100 },
  { title: '学生', dataIndex: 'studentName', key: 'studentName', width: 120 },
  { title: '目标项目', dataIndex: 'targetItem', key: 'targetItem', width: 120 },
  { title: '难度', dataIndex: 'difficulty', key: 'difficulty', width: 80 },
  { title: '进度', key: 'progress', width: 150 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '预计完成', dataIndex: 'endDate', key: 'endDate', width: 120 },
];

const tableData = [
  {
    planId: 'P001',
    studentName: '张三',
    targetItem: '引体向上',
    difficulty: '中等',
    progress: 85,
    startDate: '2024-02-01',
    endDate: '2024-03-01',
  },
  {
    planId: 'P002',
    studentName: '李四',
    targetItem: '1000米跑',
    difficulty: '高',
    progress: 60,
    startDate: '2024-02-05',
    endDate: '2024-03-05',
  },
  {
    planId: 'P003',
    studentName: '王五',
    targetItem: '立定跳远',
    difficulty: '低',
    progress: 100,
    startDate: '2024-01-15',
    endDate: '2024-02-15',
  },
  {
    planId: 'P004',
    studentName: '赵六',
    targetItem: '仰卧起坐',
    difficulty: '中等',
    progress: 45,
    startDate: '2024-02-10',
    endDate: '2024-03-10',
  },
];

const drawPlanTrendChart = () => {
  chartLoading.value = true;
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('planTrendChart'));
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['2/1', '2/5', '2/10', '2/15', '2/20', '2/25', '3/1'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: [50, 65, 72, 78, 80, 82, 85],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#52c41a' },
          areaStyle: { color: 'rgba(82, 196, 26, 0.2)' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
    chartLoading.value = false;
  }, 300);
};

const drawDifficultyChart = () => {
  setTimeout(() => {
    const chart = echarts.init(document.getElementById('difficultyChart'));
    const option = {
      tooltip: { trigger: 'item' },
      series: [
        {
          data: [
            { value: 80, name: '低' },
            { value: 120, name: '中等' },
            { value: 45, name: '高' },
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
  drawPlanTrendChart();
  drawDifficultyChart();
});
</script>

<style scoped>
.plan-execution-analysis {
  width: 100%;
}
</style>
