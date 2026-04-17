<template>
  <div class="identity-audit">
    <a-card title="学生身份认证审核">
      <a-space style="margin-bottom: 16px;">
        <a-select v-model:value="query.status" placeholder="认证状态" style="width: 140px" allow-clear>
          <a-select-option value="pending">待审核</a-select-option>
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
        </a-select>
        <a-input v-model:value="query.userName" placeholder="学生姓名" style="width: 180px" />
        <a-button type="primary" @click="load">查询</a-button>
      </a-space>

      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 16px"
        message="正确流程：管理员先在班级管理中创建班级，学生提交认证时选择班级，审核通过后系统自动加入对应班级。"
      />

      <a-table
        :columns="columns"
        :data-source="filteredRows"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.verificationStatus)">{{ getStatusLabel(record.verificationStatus) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="record.verificationStatus === 'pending'">
              <a-button size="small" type="primary" @click="approve(record)">通过</a-button>
              <a-button size="small" danger @click="showRejectModal(record)">拒绝</a-button>
            </a-space>
            <span v-else>{{ getStatusLabel(record.verificationStatus) }}</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="rejectVisible" title="拒绝认证" @ok="submitReject" :confirm-loading="rejectLoading">
      <a-form :model="rejectForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="拒绝原因" required>
          <a-textarea v-model:value="rejectForm.reason" placeholder="请输入拒绝原因" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { auditStudent, listPendingStudents } from '../../../api';

const loading = ref(false);
const rejectLoading = ref(false);
const rows = ref([]);
const rejectVisible = ref(false);
const currentRecord = ref(null);

const query = reactive({
  status: 'pending',
  userName: '',
});

const rejectForm = reactive({
  reason: '',
});

const columns = [
  { title: '学生ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '学生姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '学号', dataIndex: 'studentId', key: 'studentId', width: 140 },
  { title: '申请班级', dataIndex: 'className', key: 'className', width: 180 },
  { title: '认证状态', key: 'status', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 180 },
];

const filteredRows = computed(() => {
  return rows.value.filter((item) => {
    if (query.status && item.verificationStatus !== query.status) return false;
    const keyword = query.userName.trim();
    if (keyword && !(item.realName || '').includes(keyword)) return false;
    return true;
  });
});

const getStatusLabel = (status) => ({ pending: '待审核', approved: '已通过', rejected: '已拒绝' }[status] || status);
const statusColor = (status) => ({ pending: 'orange', approved: 'green', rejected: 'red' }[status] || 'default');

const load = async () => {
  loading.value = true;
  try {
    const res = await listPendingStudents();
    rows.value = res?.data || [];
  } catch (error) {
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const approve = async (record) => {
  await auditStudent({
    userId: record.userId,
    verificationStatus: 'approved',
    rejectReason: '',
  });
  message.success(`已通过 ${record.realName} 的身份认证，并自动加入班级`);
  await load();
};

const showRejectModal = (record) => {
  currentRecord.value = record;
  rejectForm.reason = '';
  rejectVisible.value = true;
};

const submitReject = async () => {
  if (!rejectForm.reason.trim()) {
    message.error('拒绝原因不能为空');
    return;
  }
  rejectLoading.value = true;
  try {
    await auditStudent({
      userId: currentRecord.value.userId,
      verificationStatus: 'rejected',
      rejectReason: rejectForm.reason.trim(),
    });
    message.success(`已拒绝 ${currentRecord.value.realName} 的身份认证`);
    rejectVisible.value = false;
    await load();
  } finally {
    rejectLoading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.identity-audit {
  width: 100%;
}
</style>
