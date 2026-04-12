<template>
  <div class="account-audit">
    <a-card title="用户账号审核">
      <!-- 筛选 -->
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="query.status" 
          placeholder="审核状态" 
          style="width: 140px"
          allow-clear
        >
          <a-select-option value="pending">待审核</a-select-option>
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
        </a-select>
        <a-input 
          v-model:value="query.userName" 
          placeholder="用户名" 
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
                record.auditStatus === 'pending' ? 'orange' :
                record.auditStatus === 'approved' ? 'green' : 'red'
              "
            >
              {{ getStatusLabel(record.auditStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="record.auditStatus === 'pending'">
              <a-button size="small" type="primary" @click="approve(record)">
                通过
              </a-button>
              <a-button size="small" danger @click="reject(record)">
                拒绝
              </a-button>
            </a-space>
            <span v-else>{{ getStatusLabel(record.auditStatus) }}</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

const loading = ref(false);
const rows = ref([]);

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

const columns = [
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 120 },
  { title: '姓名', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 100 },
  { title: '审核状态', key: 'status', width: 100 },
  { title: '申请时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
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
        userId: 201,
        userAccount: 'teacher001',
        userName: '王老师',
        userRole: '教师',
        auditStatus: 'pending',
        createdAt: '2024-02-27 14:30',
      },
      {
        id: 2,
        userId: 202,
        userAccount: 'teacher002',
        userName: '李老师',
        userRole: '教师',
        auditStatus: 'approved',
        createdAt: '2024-02-26 10:00',
      },
    ];
    pagination.total = 2;
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
    message.success(`已通过 ${record.userName} 的账号审核`);
    await load();
  } catch (error) {
    message.error('操作失败');
  }
};

const reject = async (record) => {
  try {
    message.success(`已拒绝 ${record.userName} 的账号审核`);
    await load();
  } catch (error) {
    message.error('操作失败');
  }
};

onMounted(load);
</script>

<style scoped>
.account-audit {
  width: 100%;
}
</style>
