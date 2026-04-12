<template>
  <a-card title="学生认证审核">
    <a-table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="audit(record, 'approved')">通过</a-button>
            <a-button danger size="small" @click="audit(record, 'rejected')">拒绝</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import { auditStudent, listPendingStudents } from '../../api';

const loading = ref(false);
const rows = ref([]);
const columns = [
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '学号', dataIndex: 'studentId', key: 'studentId', width: 180 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 140 },
  { title: '状态', dataIndex: 'verificationStatus', key: 'verificationStatus', width: 140 },
  { title: '操作', key: 'action' },
];

const load = async () => {
  loading.value = true;
  try {
    const res = await listPendingStudents();
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

const audit = async (record, status) => {
  await auditStudent({
    userId: record.userId,
    verificationStatus: status,
    rejectReason: status === 'rejected' ? '信息不完整，请补充后重新提交' : '',
  });
  message.success(status === 'approved' ? '已通过' : '已拒绝');
  await load();
};

onMounted(load);
</script>
