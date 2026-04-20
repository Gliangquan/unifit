<template>
  <div class="dashboard-container">
    <template v-if="isAdmin">
      <a-row :gutter="16" class="metrics-row">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="学生总数" :value="stats.studentCount" :value-style="{ color: '#1890ff' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="打卡率" :value="stats.checkinRate" :value-style="{ color: '#52c41a' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="认证率" :value="stats.verificationRate" :value-style="{ color: '#faad14' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="待审核" :value="stats.pendingStudentAudit" :value-style="{ color: '#f5222d' }" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">今日打卡</div>
              <div class="stat-value">{{ stats.todayCheckin }}</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">活跃用户</div>
              <div class="stat-value">{{ activityStats.activeUsers }}</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">平均时长</div>
              <div class="stat-value">{{ activityStats.avgDuration }}分</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">活跃班级</div>
              <div class="stat-value">{{ stats.activeClasses }}</div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24" :lg="12">
          <a-card title="打卡趋势（最近7天）" :loading="chartLoading">
            <div id="checkinTrendChart" style="height: 300px;"></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="体测项目分布" :loading="chartLoading">
            <div id="testDistributionChart" style="height: 300px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24">
          <a-card title="班级排行榜（最近7天）" :loading="tableLoading">
            <a-table :columns="classColumns" :data-source="classRanking" :pagination="false" size="small">
              <template #bodyCell="{ column, index }">
                <template v-if="column.key === 'rank'">
                  <a-tag :color="getRankColor(index)">{{ index + 1 }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24">
          <a-card title="数据导出">
            <a-space>
              <a-button type="primary" @click="exportCsv">导出用户与成绩</a-button>
              <a-button @click="exportClassCsv">导出班级挑战数据</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </template>

    <template v-else>
      <a-row :gutter="16" class="metrics-row">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="我的班级数" :value="teacherStats.classCount" :value-style="{ color: '#1890ff' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="班级学生数" :value="teacherStats.studentCount" :value-style="{ color: '#52c41a' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="班级打卡率" :value="teacherStats.checkinRate" :value-style="{ color: '#faad14' }" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic title="班级认证率" :value="teacherStats.verificationRate" :value-style="{ color: '#f5222d' }" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24">
          <a-card title="我的班级" :loading="tableLoading">
            <a-table :columns="teacherClassColumns" :data-source="teacherClasses" :pagination="false" size="small" />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import * as echarts from 'echarts';
import {
  exportClassChallengeCsv,
  exportUserScoresCsv,
  getActivityStats,
  getCheckinTrend,
  getClassRanking,
  getDashboardDetail,
  getTestDistribution,
} from '../../api';
import { getClassList } from '../../api/modules/class';

const stats = reactive({
  studentCount: 0,
  checkinRate: '0%',
  verificationRate: '0%',
  pendingStudentAudit: 0,
  todayCheckin: 0,
  activeClasses: 0,
});

const activityStats = reactive({
  activeUsers: 0,
  avgDuration: 0,
});

const teacherStats = reactive({
  classCount: 0,
  studentCount: 0,
  checkinRate: '0%',
  verificationRate: '0%',
});

const teacherClasses = ref([]);
const classRanking = ref([]);
const chartLoading = ref(false);
const tableLoading = ref(false);

const classColumns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', width: 80 },
  { title: '班级', dataIndex: 'className', key: 'className' },
  { title: '打卡数', dataIndex: 'checkinCount', key: 'checkinCount', width: 100 },
  { title: '参与人数', dataIndex: 'participantCount', key: 'participantCount', width: 100 },
];

const teacherClassColumns = [
  { title: '班级名称', dataIndex: 'className', key: 'className' },
  { title: '班级代码', dataIndex: 'classCode', key: 'classCode', width: 120 },
  { title: '学生数', dataIndex: 'studentCount', key: 'studentCount', width: 80 },
  { title: '年级', dataIndex: 'grade', key: 'grade', width: 80 },
  { title: '专业', dataIndex: 'major', key: 'major', width: 120 },
];

const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.userRole === 'admin';
});

let checkinTrendChart = null;
let testDistributionChart = null;
const resizeHandlers = [];

const getRankColor = (index) => {
  if (index === 0) return 'gold';
  if (index === 1) return 'silver';
  if (index === 2) return '#cd7f32';
  return 'default';
};

const toPayload = (res) => {
  if (res && typeof res === 'object' && 'data' in res) {
    return res.data;
  }
  return res;
};

const loadDashboard = async () => {
  const res = await getDashboardDetail();
  Object.assign(stats, toPayload(res) || {});
};

const loadActivity = async () => {
  const res = await getActivityStats();
  Object.assign(activityStats, toPayload(res) || {});
};

const loadClassRanking = async () => {
  tableLoading.value = true;
  try {
    const res = await getClassRanking();
    classRanking.value = toPayload(res) || [];
  } finally {
    tableLoading.value = false;
  }
};

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

const drawCheckinTrendChart = async () => {
  const res = await getCheckinTrend();
  const data = toPayload(res) || [];
  const dom = await waitForVisible('checkinTrendChart');
  if (!dom || !dom.clientWidth) return;
  checkinTrendChart?.dispose();
  checkinTrendChart = initChart(dom);
  if (!checkinTrendChart) return;
  checkinTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: data.map((item) => item.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: data.map((item) => item.count || 0), type: 'line', smooth: true, itemStyle: { color: '#1890ff' }, areaStyle: { color: 'rgba(24, 144, 255, 0.2)' } }],
  });
  bindResize(checkinTrendChart);
};

const drawTestDistributionChart = async () => {
  const res = await getTestDistribution();
  const data = toPayload(res) || [];
  const dom = await waitForVisible('testDistributionChart');
  if (!dom || !dom.clientWidth) return;
  testDistributionChart?.dispose();
  testDistributionChart = initChart(dom);
  if (!testDistributionChart) return;
  testDistributionChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: data.map((item) => item.itemCode), axisLabel: { interval: 0, rotate: data.length > 4 ? 20 : 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: data.map((item) => item.count || 0), type: 'bar', itemStyle: { color: '#52c41a' } }],
  });
  bindResize(testDistributionChart);
};

const loadTeacherClasses = async () => {
  const res = await getClassList();
  const rows = toPayload(res) || [];
  teacherClasses.value = rows;
  teacherStats.classCount = rows.length;
  teacherStats.studentCount = rows.reduce((sum, item) => sum + (item.studentCount || 0), 0);
};

const exportCsv = () => {
  window.open(exportUserScoresCsv(), '_blank');
};

const exportClassCsv = () => {
  window.open(exportClassChallengeCsv(30), '_blank');
};

onMounted(async () => {
  if (isAdmin.value) {
    chartLoading.value = true;
    try {
      await Promise.all([loadDashboard(), loadActivity(), loadClassRanking()]);
    } finally {
      chartLoading.value = false;
    }
    await nextTick();
    await Promise.all([drawCheckinTrendChart(), drawTestDistributionChart()]);
    return;
  }

  tableLoading.value = true;
  try {
    await loadTeacherClasses();
  } finally {
    tableLoading.value = false;
  }
});

onBeforeUnmount(() => {
  resizeHandlers.forEach((handler) => window.removeEventListener('resize', handler));
  checkinTrendChart?.dispose();
  testDistributionChart?.dispose();
});
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}

.metrics-row {
  margin-bottom: 16px;
}

.metric-card,
.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-item {
  text-align: center;
  padding: 16px 0;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1890ff;
}
</style>
