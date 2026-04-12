<template>
  <div class="audit-history">
    <a-card title="审核历史">
      <!-- 筛选 -->
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="query.auditType" 
          placeholder="审核类型" 
          style="width: 140px"
          allow-clear
        >
          <a-select-option value="identity">身份认证</a-select-option>
          <a-select-option value="account">账号审核</a-select-option>
          <a-select-option value="test-data">体测数据</a-select-option>
        </a-select>
        <a-select 
          v-model:value="query.result" 
          placeholder="审核结果" 
          style="width: 140px"
          allow-clear
        >
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已拒绝</a-select-option>
        </a-select>
        <a-range-picker 
          v-model:value="query.dateRange" 
          style="width: 240px"
          format="YYYY-MM-DD"
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
          <template v-if="column.key === 'auditType'">
            {{ getAuditTypeLabel(record.auditType) }}
          </template>
          <template v-else-if="column.key === 'result'">
            <a-tag :color="record.result === 'approved' ? 'green' : 'red'">
              {{ record.result === 'approved' ? '已通过' : '已拒绝' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import dayjs from 'dayjs';

const loading = ref(false);
const rows = ref([]);

const query = reactive({
  current: 1,
  pageSize: 10,
  auditType: '',
  result: '',
  dateRange: [dayjs().subtract(7, 'days'), dayjs()],
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns = [
  { title: '审核ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '审核类型', key: 'auditType', width: 120 },
  { title: '对象', dataIndex: 'targetName', key: 'targetName', width: 120 },
  { title: '审核人', dataIndex: 'auditorName', key: 'auditorName', width: 120 },
  { title: '审核结果', key: 'result', width: 100 },
  { title: '审核时间', dataIndex: 'auditTime', key: 'auditTime', width: 160 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
];

const getAuditTypeLabel = (type) => {
  const map = {
    identity: '身份认证',
    account: '账号审核',
    'test-data': '体测数据',
  };
  return map[type] || type;
};

const load = async () => {
  loading.value = true;
  // 模拟数据
  setTimeout(() => {
    rows.value = [
      {
        id: 1,
        auditType: 'identity',
        targetName: '张三',
        auditorName: '管理员',
        result: 'approved',
        auditTime: '2024-02-27 10:30',
        remark: '学号和姓名验证通过',
      },
      {
        id: 2,
        auditType: 'account',
        targetName: '王老师',
        auditorName: '管理员',
        result: 'approved',
        auditTime: '2024-02-27 09:15',
        remark: '账号信息完整',
      },
      {
        id: 3,
        auditType: 'test-data',
        targetName: '李四',
        auditorName: '管理员',
        result: 'rejected',
        auditTime: '2024-02-26 14:45',
        remark: '数据异常，需要重新提交',
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

onMounted(load);
</script>

<style scoped>
.audit-history {
  width: 100%;
}
</style>
