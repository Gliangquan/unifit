<template>
  <div class="audit-history">
    <a-card title="审核历史">
      <a-space style="margin-bottom: 16px;">
        <a-select v-model:value="resultFilter" placeholder="审核结果" style="width: 140px" allow-clear>
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
        </a-select>
        <a-input v-model:value="keyword" placeholder="搜索姓名 / 学号 / 班级" style="width: 240px" />
        <a-button type="primary" @click="load">刷新</a-button>
      </a-space>

      <a-table :columns="columns" :data-source="filteredRows" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'result'">
            <a-tag :color="record.verificationStatus === 'approved' ? 'green' : 'red'">
              {{ record.verificationStatus === 'approved' ? '已通过' : '已拒绝' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'auditTime'">
            {{ record.auditTime || record.updateTime || '--' }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { listStudentAuditHistory } from '../../../api';

const loading = ref(false);
const rows = ref([]);
const resultFilter = ref();
const keyword = ref('');

const columns = [
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 90 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '学号', dataIndex: 'studentId', key: 'studentId', width: 140 },
  { title: '班级', dataIndex: 'className', key: 'className', width: 180 },
  { title: '审核结果', key: 'result', width: 100 },
  { title: '审核时间', key: 'auditTime', width: 180 },
  { title: '拒绝原因', dataIndex: 'rejectReason', key: 'rejectReason' },
];

const filteredRows = computed(() => {
  const kw = keyword.value.trim();
  return rows.value.filter((item) => {
    if (resultFilter.value && item.verificationStatus !== resultFilter.value) {
      return false;
    }
    if (!kw) return true;
    return [item.realName, item.studentId, item.className].some((value) => String(value || '').includes(kw));
  });
});

const load = async () => {
  loading.value = true;
  try {
    const res = await listStudentAuditHistory();
    rows.value = res?.data || [];
  } catch (error) {
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.audit-history {
  width: 100%;
}
</style>
