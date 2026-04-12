<template>
  <div class="dashboard-container">
    <!-- Admin Dashboard -->
    <template v-if="isAdmin">
      <!-- 关键指标卡片 -->
      <a-row :gutter="16" class="metrics-row">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="学生总数" 
              :value="stats.studentCount"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="打卡率" 
              :value="stats.checkinRate"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="认证率" 
              :value="stats.verificationRate"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="待审核" 
              :value="stats.pendingStudentAudit"
              :value-style="{ color: '#f5222d' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 详细统计 -->
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

      <!-- 图表区域 -->
      <a-row :gutter="16" style="margin-top: 16px;">
        <!-- 打卡趋势 -->
        <a-col :xs="24" :lg="12">
          <a-card title="打卡趋势（最近7天）" :loading="chartLoading">
            <div id="checkinTrendChart" style="height: 300px;"></div>
          </a-card>
        </a-col>

        <!-- 体测项目分布 -->
        <a-col :xs="24" :lg="12">
          <a-card title="体测项目分布" :loading="chartLoading">
            <div id="testDistributionChart" style="height: 300px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 班级排行榜 -->
      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24">
          <a-card title="班级排行榜（最近7天）" :loading="tableLoading">
            <a-table 
              :columns="classColumns"
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

      <!-- 数据导出 -->
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

    <!-- Teacher Dashboard -->
    <template v-else>
      <!-- 教师统计卡片 -->
      <a-row :gutter="16" class="metrics-row">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="我的班级数" 
              :value="teacherStats.classCount"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="班级学生数" 
              :value="teacherStats.studentCount"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="班级打卡率" 
              :value="teacherStats.checkinRate"
              :value-style="{ color: '#faad14' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="metric-card">
            <a-statistic 
              title="班级认证率" 
              :value="teacherStats.verificationRate"
              :value-style="{ color: '#f5222d' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 教师班级列表 -->
      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24">
          <a-card title="我的班级" :loading="tableLoading">
            <a-table 
              :columns="teacherClassColumns"
              :data-source="teacherClasses"
              :pagination="false"
              size="small"
            />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import { 
  getDashboardDetail, 
  getCheckinTrend, 
  getClassRanking, 
  getTestDistribution,
  getActivityStats,
  exportUserScoresCsv, 
  exportClassChallengeCsv
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

const getRankColor = (index) => {
  if (index === 0) return 'gold';
  if (index === 1) return 'silver';
  if (index === 2) return '#cd7f32';
  return 'default';
};

const loadDashboard = async () => {
  try {
    const res = await getDashboardDetail();
    if (res) {
      Object.assign(stats, res);
    }
  } catch (error) {
    console.error('Failed to load dashboard detail:', error);
  }
};

const loadActivityStats = async () => {
  try {
    const res = await getActivityStats();
    if (res) {
      Object.assign(activityStats, res);
    }
  } catch (error) {
    console.error('Failed to load activity stats:', error);
  }
};

const loadClassRanking = async () => {
  tableLoading.value = true;
  try {
    const res = await getClassRanking();
    classRanking.value = res || [];
  } catch (error) {
    console.error('Failed to load class ranking:', error);
  } finally {
    tableLoading.value = false;
  }
};

const drawCheckinTrendChart = async () => {
  try {
    const res = await getCheckinTrend();
    const data = res || [];
    
    await nextTick();
    const chartDom = document.getElementById('checkinTrendChart');
    if (!chartDom) {
      console.error('Chart DOM not found');
      return;
    }

    const chart = echarts.init(chartDom);
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.map(item => item.date),
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: data.map(item => item.count),
          type: 'line',
          smooth: true,
          itemStyle: { color: '#1890ff' },
          areaStyle: { color: 'rgba(24, 144, 255, 0.2)' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
  } catch (error) {
    console.error('Failed to draw checkin trend chart:', error);
  }
};

const drawTestDistributionChart = async () => {
  try {
    const res = await getTestDistribution();
    const data = res || [];
    
    await nextTick();
    const chartDom = document.getElementById('testDistributionChart');
    if (!chartDom) {
      console.error('Chart DOM not found');
      return;
    }

    const chart = echarts.init(chartDom);
    const option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.map(item => item.itemCode),
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: data.map(item => item.count),
          type: 'bar',
          itemStyle: { color: '#52c41a' },
        },
      ],
    };
    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
  } catch (error) {
    console.error('Failed to draw test distribution chart:', error);
  }
};

const exportCsv = () => {
  window.open(exportUserScoresCsv(), '_blank');
};

const loadTeacherClasses = async () => {
  try {
    const res = await getClassList();
    if (res) {
      teacherClasses.value = res;
      teacherStats.classCount = res.length;
      teacherStats.studentCount = res.reduce((sum, cls) => sum + (cls.studentCount || 0), 0);
    }
  } catch (error) {
    console.error('Failed to load teacher classes:', error);
  }
};

const exportClassCsv = () => {
  window.open(exportClassChallengeCsv(30), '_blank');
};

onMounted(async () => {
  if (isAdmin.value) {
    // Admin dashboard
    chartLoading.value = true;
    try {
      await Promise.all([
        loadDashboard(),
        loadActivityStats(),
        loadClassRanking(),
      ]);
      
      await nextTick();
      
      await Promise.all([
        drawCheckinTrendChart(),
        drawTestDistributionChart(),
      ]);
    } finally {
      chartLoading.value = false;
    }
  } else {
    // Teacher dashboard
    tableLoading.value = true;
    try {
      await loadTeacherClasses();
    } finally {
      tableLoading.value = false;
    }
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}

.metrics-row {
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

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
