<template>
  <div class="data-analysis">
    <a-tabs v-model:activeKey="activeTab" destroyInactiveTabPane>
      <!-- 体测数据分析 -->
      <a-tab-pane key="test-data" tab="体测数据分析">
        <TestDataAnalysis />
      </a-tab-pane>

      <!-- 训练计划执行分析 -->
      <a-tab-pane key="plan-execution" tab="计划执行分析">
        <PlanExecutionAnalysis />
      </a-tab-pane>

      <!-- 打卡数据分析 -->
      <a-tab-pane key="checkin" tab="打卡数据分析">
        <CheckinAnalysis />
      </a-tab-pane>

      <!-- 班级对比分析 -->
      <a-tab-pane key="class-comparison" tab="班级对比分析">
        <ClassComparisonAnalysis />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import TestDataAnalysis from './components/TestDataAnalysis.vue';
import PlanExecutionAnalysis from './components/PlanExecutionAnalysis.vue';
import CheckinAnalysis from './components/CheckinAnalysis.vue';
import ClassComparisonAnalysis from './components/ClassComparisonAnalysis.vue';

const activeTab = ref('test-data');
</script>

<style scoped>
.data-analysis {
  padding: 16px;
}
</style>
