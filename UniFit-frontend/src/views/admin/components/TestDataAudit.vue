<template>
  <div class="test-data-audit">
    <a-card title="体测数据异常审核">
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
        <a-select 
          v-model:value="query.anomalyType" 
          placeholder="异常类型" 
          style="width: 140px"
          allow-clear
        >
          <a-select-option value="outlier">离群值</a-select-option>
          <a-select-option value="duplicate">重复数据</a-select-option>
          <a-select-option value="invalid">无效数据</a-select-option>
        </a-select>
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
          <template v-if="column.key === 'anomalyType'">
            <a-tag :color="getAnomalyColor(record.anomalyType)">
              {{ getAnomalyLabel(record.anomalyType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
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
  anomalyType: '',
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns = [
  { title: '学生ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '学生姓名', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '体测项目', dataIndex: 'testItem', key: 'testItem', width: 120 },
  { title: '异常类型', key: 'anomalyType', width: 100 },
  { title: '数据值', dataIndex: 'dataValue', key: 'dataValue', width: 100 },
  { title: '审核状态', key: 'status', width: 100 },
  { title: '发现时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 200 },
];

const getAnomalyLabel = (type) => {
  const map = {
    outlier: '离群值',
    duplicate: '重复数据',
    invalid: '无效数据',
  };
  return map[type] || type;
};

const getAnomalyColor = (type) => {
  const map = {
    outlier: 'orange',
    duplicate: 'blue',
    invalid: 'red',
  };
  return map[type] || 'default';
};

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
        testItem: '引体向上',
        anomalyType: 'outlier',
        dataValue: '45次',
        auditStatus: 'pending',
        createdAt: '2024-02-27 15:30',
      },
      {
        id: 2,
        userId: 102,
        userName: '李四',
        testItem: '1000米跑',
        anomalyType: 'invalid',
        dataValue: '9999秒',
        auditStatus: 'pending',
        createdAt: '2024-02-27 14:15',
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
    message.success(`已通过 ${record.userName} 的数据审核`);
    await load();
  } catch (error) {
    message.error('操作失败');
  }
};

const reject = async (record) => {
  try {
    message.success(`已拒绝 ${record.userName} 的数据审核`);
    await load();
  } catch (error) {
    message.error('操作失败');
  }
};

onMounted(load);
</script>

<style scoped>
.test-data-audit {
  width: 100%;
}
</style>
