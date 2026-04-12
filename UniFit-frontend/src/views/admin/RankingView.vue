<template>
  <a-card title="打卡排行榜">
    <a-space style="margin-bottom: 12px;">
      <a-select v-model:value="days" style="width: 180px" @change="load">
        <a-select-option :value="7">最近7天</a-select-option>
        <a-select-option :value="30">最近30天</a-select-option>
      </a-select>
    </a-space>
    <a-table :columns="columns" :data-source="rows" :loading="loading" row-key="userId" :pagination="false">
      <template #bodyCell="{ column, index }">
        <template v-if="column.key === 'rank'">
          <a-tag :color="index < 3 ? 'gold' : 'blue'">{{ index + 1 }}</a-tag>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getCheckinRanking } from '../../api';

const days = ref(7);
const loading = ref(false);
const rows = ref([]);

const columns = [
  { title: '排名', key: 'rank', width: 90 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 120 },
  { title: '用户名', dataIndex: 'userName', key: 'userName', width: 180 },
  { title: '打卡次数', dataIndex: 'checkinCount', key: 'checkinCount', width: 120 },
  { title: '总时长(分钟)', dataIndex: 'totalDuration', key: 'totalDuration', width: 160 },
];

const load = async () => {
  loading.value = true;
  try {
    const res = await getCheckinRanking(days.value, 50);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>
