<template>
  <div class="identity-audit">
    <a-card title="学生身份认证审核">
      <!-- 筛选 -->
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="query.status" 
          placeholder="认证状态" 
          style="width: 140px"
          allow-clear
        >
          <a-select-option value="pending">待审核</a-select-option>
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
        </a-select>
        <a-input 
          v-model:value="query.userName" 
          placeholder="学生姓名" 
          style="width: 180px"
        />
        <a-button type="primary" @click="load">查询</a-button>
      </a-space>

      <!-- 列表 -->
      <a-table 
        :columns="columns" 
        :data-source="rows" 
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="onPageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag 
              :color="
                record.verificationStatus === 'pending' ? 'orange' :
                record.verificationStatus === 'approved' ? 'green' : 'red'
              "
            >
              {{ getStatusLabel(record.verificationStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="record.verificationStatus === 'pending'">
              <a-button size="small" type="primary" @click="approve(record)">
                通过
              </a-button>
              <a-button size="small" danger @click="showRejectModal(record)">
                拒绝
              </a-button>
            </a-space>
            <span v-else>{{ getStatusLabel(record.verificationStatus) }}</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 拒绝原因对话框 -->
    <a-modal 
      v-model:open="rejectVisible" 
      title="拒绝认证" 
      @ok="submitReject"
      :confirm-loading="rejectLoading"
    >
      <a-form :model="rejectForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="拒绝原因" required>
          <a-textarea 
            v-model:value="rejectForm.reason" 
            placeholder="请输入拒绝原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

const loading = ref(false);
const rejectLoading = ref(false);
const rows = ref([]);
const rejectVisible = ref(false);
const currentRecord = ref(null);

const query = reactive({
  current: 1,
  pageSize: 10,
  status: 'pending',
  userName: '',
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const rejectForm = reactive({
  reason: '',
});

const columns = [
  { title: '学生ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '学生姓名', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '学号', dataIndex: 'studentId', key: 'studentId', width: 120 },
  { title: '班级', dataIndex: 'className', key: 'className', width: 120 },
  { title: '认证状态', key: 'status', width: 100 },
  { title: '提交时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 200 },
];

const getStatusLabel = (status) => {
  const map = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
  };
  return map[status] || status;
};

const load = async () => {
  loading.value = true;
  // 模拟数据
  setTimeout(() => {
    rows.value = [
      {
        id: 1,
        userId: 101,
        userName: '张三',
        studentId: '2021001',
        className: '计算机1班',
        verificationStatus: 'pending',
        createdAt: '2024-02-27 10:30',
      },
      {
        id: 2,
        userId: 102,
        userName: '李四',
        studentId: '2021002',
        className: '计算机1班',
        verificationStatus: 'pending',
        createdAt: '2024-02-27 11:15',
      },
      {
        id: 3,
        userId: 103,
        userName: '王五',
        studentId: '2021003',
        className: '计算机2班',
        verificationStatus: 'approved',
        createdAt: '2024-02-26 09:00',
      },
    ];
    pagination.total = 3;
    loading.value = false;
  }, 300);
};

const onPageChange = (p) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  load();
};

const approve = async (record) => {
  try {
    // TODO: 调用审核通过的 API
    message.success(`已通过 ${record.userName} 的身份认证`);
    await load();
  } catch (error) {
    message.error('操作失败');
  }
};

const showRejectModal = (record) => {
  currentRecord.value = record;
  rejectForm.reason = '';
  rejectVisible.value = true;
};

const submitReject = async () => {
  if (!rejectForm.reason) {
    message.error('拒绝原因不能为空');
    return;
  }

  rejectLoading.value = true;
  try {
    // TODO: 调用拒绝认证的 API
    message.success(`已拒绝 ${currentRecord.value.userName} 的身份认证`);
    rejectVisible.value = false;
    await load();
  } catch (error) {
    message.error('操作失败');
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
