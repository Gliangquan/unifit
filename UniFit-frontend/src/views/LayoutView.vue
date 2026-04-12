<template>
  <a-layout class="layout-root">
    <a-layout-sider width="240" class="sider">
      <div class="brand">
        <div class="brand-dot"></div>
        <div>
          <div class="brand-title">UniFit</div>
          <div class="brand-sub">Admin Console</div>
        </div>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline" @click="onMenuClick">
        <a-menu-item key="/admin/dashboard">总览看板</a-menu-item>
        
        <!-- Admin only -->
        <template v-if="isAdmin">
          <a-menu-item key="/admin/user-management">用户管理</a-menu-item>
          <a-menu-item key="/admin/audit-center">审核中心</a-menu-item>
          <a-menu-item key="/admin/data-analysis">数据分析</a-menu-item>
          <a-menu-item key="/admin/students">学生审核</a-menu-item>
          <a-menu-item key="/admin/standards">体测标准</a-menu-item>
          <a-menu-item key="/admin/ranking">排行榜</a-menu-item>
        </template>
        
        <!-- Admin and Teacher -->
        <a-menu-item key="/admin/class-management">班级管理</a-menu-item>
        <a-menu-item key="/admin/exercises">动作库管理</a-menu-item>
        <a-menu-item key="/admin/templates">计划模板</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <span>{{ userName }}</span>
        <a-button danger type="link" @click="logout">退出</a-button>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { userLogout } from '../api';

const route = useRoute();
const router = useRouter();
const selectedKeys = ref([route.path]);

watch(() => route.path, (p) => {
  selectedKeys.value = [p];
});

const userName = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.userName || '管理员';
});

const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.userRole === 'admin';
});

const onMenuClick = ({ key }) => {
  router.push(key);
};

const logout = async () => {
  try {
    await userLogout();
  } catch (e) {
    // ignore
  }
  localStorage.removeItem('user');
  router.push('/login');
};
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
}
.sider {
  background: linear-gradient(180deg, #1b2a41, #142033);
}
.brand {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  gap: 12px;
}
.brand-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1ba499, #55d2a2);
  box-shadow: 0 0 0 6px rgba(85, 210, 162, 0.18);
}
.brand-title {
  color: #fff;
  font-weight: 700;
}
.brand-sub {
  color: #93a5be;
  font-size: 12px;
}
.header {
  background: #ffffff;
  border-bottom: 1px solid #edf1f6;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}
.content {
  padding: 20px;
  background: #f5f8fc;
}
</style>
