<template>
  <div class="audit-center">
    <a-tabs v-model:activeKey="activeTab">
      <!-- 学生身份认证审核 -->
      <a-tab-pane key="identity" tab="身份认证审核">
        <IdentityAudit />
      </a-tab-pane>

      <!-- 用户账号审核 -->
      <a-tab-pane key="account" tab="账号审核">
        <AccountAudit />
      </a-tab-pane>

      <!-- 体测数据异常审核 -->
      <a-tab-pane key="test-data" tab="体测数据审核">
        <TestDataAudit />
      </a-tab-pane>

      <!-- 审核历史 -->
      <a-tab-pane key="history" tab="审核历史">
        <AuditHistory />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import IdentityAudit from './components/IdentityAudit.vue';
import AccountAudit from './components/AccountAudit.vue';
import TestDataAudit from './components/TestDataAudit.vue';
import AuditHistory from './components/AuditHistory.vue';

const activeTab = ref('identity');
</script>

<style scoped>
.audit-center {
  padding: 16px;
}
</style>
