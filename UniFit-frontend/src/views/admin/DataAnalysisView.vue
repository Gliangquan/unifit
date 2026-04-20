<template>
  <div class="data-analysis">
    <a-tabs v-model:activeKey="activeTab" :animated="false">
      <a-tab-pane key="test-data" tab="体测数据分析">
        <TestDataAnalysis v-if="activeTab === 'test-data'" :key="'test-data'" />
      </a-tab-pane>

      <a-tab-pane key="plan-execution" tab="计划执行分析">
        <PlanExecutionAnalysis v-if="activeTab === 'plan-execution'" :key="'plan-execution'" />
      </a-tab-pane>

      <a-tab-pane key="checkin" tab="打卡数据分析">
        <CheckinAnalysis v-if="activeTab === 'checkin'" :key="'checkin'" />
      </a-tab-pane>

      <a-tab-pane key="class-comparison" tab="班级对比分析">
        <ClassComparisonAnalysis v-if="activeTab === 'class-comparison'" :key="'class-comparison'" />
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
