<template>
  <div class="user-management">
    <!-- 标签页 -->
    <a-tabs v-model:activeKey="activeTab">
      <!-- 学生管理 -->
      <a-tab-pane key="students" tab="学生管理">
        <StudentManagement />
      </a-tab-pane>

      <!-- 教师管理 -->
      <a-tab-pane key="teachers" tab="教师管理">
        <TeacherManagement />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import StudentManagement from './components/StudentManagement.vue';
import TeacherManagement from './components/TeacherManagement.vue';

const activeTab = ref('students');
</script>

<style scoped>
.user-management {
  padding: 16px;
}
</style>
