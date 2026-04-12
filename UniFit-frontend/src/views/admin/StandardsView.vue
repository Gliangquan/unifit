<template>
  <a-card title="体测标准管理（国标）">
    <a-space style="margin-bottom: 12px;">
      <a-select v-model:value="query.stage" style="width: 120px" allow-clear placeholder="学段">
        <a-select-option value="college">大学</a-select-option>
      </a-select>
      <a-select v-model:value="query.gender" style="width: 120px" allow-clear placeholder="性别">
        <a-select-option value="male">男</a-select-option>
        <a-select-option value="female">女</a-select-option>
      </a-select>
      <a-select v-model:value="query.itemCode" style="width: 180px" allow-clear placeholder="项目">
        <a-select-option v-for="item in testItems" :key="item.itemCode" :value="item.itemCode">{{ item.itemName }}</a-select-option>
      </a-select>
      <a-button type="primary" @click="load">查询</a-button>
      <a-button type="dashed" @click="openModal()">新增标准</a-button>
      <a-button type="primary" danger @click="doImportFull">一键导入完整版国标</a-button>
    </a-space>

    <a-table :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" row-key="id" @change="onPageChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openModal(record)">编辑</a-button>
            <a-popconfirm title="确认删除该标准？" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal v-model:open="modalOpen" :title="form.id ? '编辑标准' : '新增标准'" @ok="save">
    <a-form layout="vertical">
      <a-form-item label="学段">
        <a-select v-model:value="form.stage">
          <a-select-option value="college">大学</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="年级范围">
        <a-select v-model:value="form.gradeRange">
          <a-select-option value="all">全部年级</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="性别">
        <a-select v-model:value="form.gender">
          <a-select-option value="male">男</a-select-option>
          <a-select-option value="female">女</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="项目">
        <a-select v-model:value="form.itemCode">
          <a-select-option v-for="item in testItems" :key="item.itemCode" :value="item.itemCode">{{ item.itemName }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="最小值"><a-input-number v-model:value="form.minScore" :precision="2" style="width:100%" /></a-form-item>
      <a-form-item label="最大值"><a-input-number v-model:value="form.maxScore" :precision="2" style="width:100%" /></a-form-item>
      <a-form-item label="等级">
        <a-select v-model:value="form.level">
          <a-select-option value="fail">不及格</a-select-option>
          <a-select-option value="pass">及格</a-select-option>
          <a-select-option value="good">良好</a-select-option>
          <a-select-option value="excellent">优秀</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="分值"><a-input-number v-model:value="form.standardPoint" :min="0" :max="100" style="width:100%" /></a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  listStandards,
  upsertStandard,
  deleteStandard,
  importCollegeFullStandard,
  listStandardTestItems,
  type TestStandard,
  type TestItem,
} from '../../api';

const loading = ref(false);
const rows = ref<TestStandard[]>([]);
const testItems = ref<TestItem[]>([]);
const query = reactive({ current: 1, pageSize: 20, stage: 'college', gender: undefined as string | undefined, itemCode: undefined as string | undefined });
const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

const modalOpen = ref(false);
const form = reactive<any>({
  id: undefined,
  stage: 'college',
  gradeRange: 'all',
  gender: 'male',
  itemCode: '',
  minScore: 0,
  maxScore: 0,
  level: 'pass',
  standardPoint: 60,
});

const stageLabelMap: Record<string, string> = {
  college: '大学',
};

const genderLabelMap: Record<string, string> = {
  male: '男',
  female: '女',
};

const levelLabelMap: Record<string, string> = {
  fail: '不及格',
  pass: '及格',
  good: '良好',
  excellent: '优秀',
};

const getItemName = (itemCode?: string) => {
  if (!itemCode) return '-';
  const row = testItems.value.find(item => item.itemCode === itemCode);
  return row?.itemName || itemCode;
};

const columns = [
  {
    title: '学段',
    dataIndex: 'stage',
    key: 'stage',
    width: 90,
    customRender: ({ record }: any) => stageLabelMap[record.stage] || record.stage || '-',
  },
  {
    title: '性别',
    dataIndex: 'gender',
    key: 'gender',
    width: 90,
    customRender: ({ record }: any) => genderLabelMap[record.gender] || record.gender || '-',
  },
  {
    title: '项目',
    dataIndex: 'itemCode',
    key: 'itemCode',
    width: 130,
    customRender: ({ record }: any) => getItemName(record.itemCode),
  },
  { title: '区间', key: 'range', width: 180, customRender: ({ record }: any) => `${record.minScore} ~ ${record.maxScore}` },
  {
    title: '等级',
    dataIndex: 'level',
    key: 'level',
    width: 100,
    customRender: ({ record }: any) => levelLabelMap[record.level] || record.level || '-',
  },
  { title: '分值', dataIndex: 'standardPoint', key: 'standardPoint', width: 90 },
  { title: '操作', key: 'action', width: 140 },
];

const load = async () => {
  loading.value = true;
  try {
    const res = await listStandards(query);
    const page = res.data;
    rows.value = page.records || [];
    pagination.current = page.current || query.current;
    pagination.pageSize = page.size || query.pageSize;
    pagination.total = page.total || 0;
  } finally {
    loading.value = false;
  }
};

const loadTestItems = async () => {
  const res = await listStandardTestItems();
  testItems.value = res.data || [];
  if (!form.itemCode && testItems.value.length) {
    form.itemCode = testItems.value[0].itemCode;
  }
};

const onPageChange = (p: any) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  load();
};

const openModal = (row?: TestStandard) => {
  if (row) {
    Object.assign(form, row);
  } else {
    Object.assign(form, {
      id: undefined,
      stage: 'college',
      gradeRange: 'all',
      gender: 'male',
      itemCode: testItems.value[0]?.itemCode || '',
      minScore: 0,
      maxScore: 0,
      level: 'pass',
      standardPoint: 60,
    });
  }
  modalOpen.value = true;
};

const save = async () => {
  await upsertStandard(form);
  message.success('标准保存成功');
  modalOpen.value = false;
  await load();
};

const remove = async (row: TestStandard) => {
  await deleteStandard(row.id);
  message.success('标准已删除');
  await load();
};

const doImportFull = () => {
  Modal.confirm({
    title: '确认导入完整版国标？',
    content: '会覆盖当前 college 学段的标准数据。',
    onOk: async () => {
      const res = await importCollegeFullStandard();
      message.success(`导入完成，共 ${res.data.inserted} 条`);
      await loadTestItems();
      await load();
    },
  });
};

onMounted(async () => {
  await loadTestItems();
  await load();
});
</script>
