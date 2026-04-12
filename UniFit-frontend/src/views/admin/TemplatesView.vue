<template>
  <a-card title="计划模板管理">
    <a-space style="margin-bottom: 12px;">
      <a-input v-model:value="query.keyword" placeholder="模板编码/名称" style="width: 220px" @pressEnter="loadTemplates" />
      <a-button type="primary" @click="openTemplateModal()">新增模板</a-button>
    </a-space>

    <a-table :columns="templateColumns" :data-source="templates" :loading="loading" :pagination="pagination" row-key="id" @change="onPageChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openTemplateModal(record)">编辑</a-button>
            <a-button type="link" @click="openItemsDrawer(record)">动作明细</a-button>
            <a-popconfirm title="确认删除该模板？" @confirm="removeTemplate(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal v-model:open="templateModalOpen" :title="templateForm.id ? '编辑模板' : '新增模板'" @ok="saveTemplate">
    <a-form layout="vertical">
      <a-form-item label="模板编码"><a-input v-model:value="templateForm.templateCode" /></a-form-item>
      <a-form-item label="模板名称"><a-input v-model:value="templateForm.templateName" /></a-form-item>
      <a-form-item label="体测项目">
        <a-select v-model:value="templateForm.testItemCode">
          <a-select-option v-for="item in testItems" :key="item.itemCode" :value="item.itemCode">{{ item.itemName }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="成绩等级">
        <a-select v-model:value="templateForm.scoreLevel">
          <a-select-option value="beginner">初级</a-select-option>
          <a-select-option value="intermediate">中级</a-select-option>
          <a-select-option value="advanced">高级</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="训练基础">
        <a-select v-model:value="templateForm.fitnessLevel">
          <a-select-option value="newbie">新手</a-select-option>
          <a-select-option value="basic">有基础</a-select-option>
          <a-select-option value="advanced">经常训练</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="器械类型">
        <a-select v-model:value="templateForm.equipmentType" :options="equipmentTypeOptions" />
      </a-form-item>
      <a-form-item label="BMI 范围">
        <a-select v-model:value="templateForm.bmiRange" :options="bmiRangeOptions" />
      </a-form-item>
      <a-form-item label="每周训练天数"><a-input-number v-model:value="templateForm.daysPerWeek" :min="1" :max="7" style="width:100%" /></a-form-item>
      <a-form-item label="描述"><a-textarea v-model:value="templateForm.description" :rows="3" /></a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="templateForm.status">
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">禁用</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-drawer v-model:open="drawerOpen" title="模板动作明细" width="720">
    <div v-if="currentTemplate">
      <div style="margin-bottom: 12px;color:#64748b;">模板：{{ currentTemplate.templateName }}（{{ currentTemplate.templateCode }}）</div>
      <a-space style="margin-bottom: 12px;">
        <a-button type="primary" @click="openItemModal()">新增动作项</a-button>
      </a-space>
      <a-table :columns="itemColumns" :data-source="templateItems" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'exerciseId'">
            {{ exerciseLabel(record.exerciseId) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openItemModal(record)">编辑</a-button>
              <a-popconfirm title="确认删除该动作项？" @confirm="removeItem(record)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </a-drawer>

  <a-modal v-model:open="itemModalOpen" :title="itemForm.id ? '编辑动作项' : '新增动作项'" @ok="saveItem">
    <a-form layout="vertical">
      <a-form-item label="周次"><a-input-number v-model:value="itemForm.weekNo" :min="1" :max="4" style="width:100%" /></a-form-item>
      <a-form-item label="日次"><a-input-number v-model:value="itemForm.dayNo" :min="1" :max="7" style="width:100%" /></a-form-item>
      <a-form-item label="动作">
        <a-select v-model:value="itemForm.exerciseId" show-search>
          <a-select-option v-for="exercise in exercises" :key="exercise.id" :value="exercise.id">
            {{ exercise.name }}（#{{ exercise.id }}）
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="组数"><a-input-number v-model:value="itemForm.setsCount" :min="1" style="width:100%" /></a-form-item>
      <a-form-item label="次数"><a-input-number v-model:value="itemForm.repsCount" :min="1" style="width:100%" /></a-form-item>
      <a-form-item label="时长(分钟)"><a-input-number v-model:value="itemForm.durationMinutes" :min="1" style="width:100%" /></a-form-item>
      <a-form-item label="强度说明"><a-input v-model:value="itemForm.intensityNote" /></a-form-item>
      <a-form-item label="排序"><a-input-number v-model:value="itemForm.sortNo" :min="1" style="width:100%" /></a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  listTemplates,
  upsertTemplate,
  deleteTemplate,
  listTemplateItems,
  upsertTemplateItem,
  deleteTemplateItem,
  listTemplateTestItems,
  listTemplateExercises,
  type PlanTemplate,
  type PlanTemplateItem,
  type TestItem,
  type ExerciseLite,
} from '../../api';

const loading = ref(false);
const templates = ref<PlanTemplate[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const query = reactive({ current: 1, pageSize: 10, keyword: '' });

const templateModalOpen = ref(false);
const templateForm = reactive<any>({
  id: undefined,
  templateCode: '',
  templateName: '',
  testItemCode: '',
  scoreLevel: 'beginner',
  fitnessLevel: 'newbie',
  equipmentType: 'bodyweight',
  bmiRange: 'all',
  daysPerWeek: 3,
  description: '',
  status: 1,
});

const drawerOpen = ref(false);
const currentTemplate = ref<PlanTemplate | null>(null);
const templateItems = ref<PlanTemplateItem[]>([]);

const itemModalOpen = ref(false);
const itemForm = reactive<any>({
  id: undefined,
  weekNo: 1,
  dayNo: 1,
  exerciseId: 1,
  setsCount: 3,
  repsCount: 10,
  durationMinutes: 15,
  intensityNote: '',
  sortNo: 1,
});

const testItems = ref<TestItem[]>([]);
const exercises = ref<ExerciseLite[]>([]);

const scoreLevelLabelMap: Record<string, string> = {
  beginner: '初级',
  intermediate: '中级',
  advanced: '高级',
};

const fitnessLevelLabelMap: Record<string, string> = {
  newbie: '新手',
  basic: '有基础',
  advanced: '经常训练',
};

const equipmentTypeLabelMap: Record<string, string> = {
  bodyweight: '徒手',
  dorm: '宿舍器械',
  dorm_equipment: '宿舍器械',
  gym: '健身房',
  track: '跑道',
  mixed: '综合',
};

const bmiRangeLabelMap: Record<string, string> = {
  all: '全部',
  underweight: '偏瘦',
  normal: '正常',
  overweight: '超重',
  obese: '肥胖',
};

const equipmentTypeOptions = [
  { label: '徒手', value: 'bodyweight' },
  { label: '宿舍器械', value: 'dorm' },
  { label: '健身房', value: 'gym' },
  { label: '跑道', value: 'track' },
  { label: '综合', value: 'mixed' },
];

const bmiRangeOptions = [
  { label: '全部', value: 'all' },
  { label: '偏瘦', value: 'underweight' },
  { label: '正常', value: 'normal' },
  { label: '超重', value: 'overweight' },
  { label: '肥胖', value: 'obese' },
];

const getTestItemName = (itemCode?: string) => {
  if (!itemCode) return '--';
  const item = testItems.value.find(row => row.itemCode === itemCode);
  return item?.itemName || itemCode;
};

const templateColumns = [
  { title: '编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '名称', dataIndex: 'templateName', key: 'templateName', width: 220 },
  {
    title: '项目',
    dataIndex: 'testItemCode',
    key: 'testItemCode',
    width: 120,
    customRender: ({ record }: any) => getTestItemName(record.testItemCode),
  },
  {
    title: '等级',
    dataIndex: 'scoreLevel',
    key: 'scoreLevel',
    width: 120,
    customRender: ({ record }: any) => scoreLevelLabelMap[record.scoreLevel] || record.scoreLevel || '--',
  },
  {
    title: '训练基础',
    dataIndex: 'fitnessLevel',
    key: 'fitnessLevel',
    width: 120,
    customRender: ({ record }: any) => fitnessLevelLabelMap[record.fitnessLevel] || record.fitnessLevel || '--',
  },
  {
    title: '器械类型',
    dataIndex: 'equipmentType',
    key: 'equipmentType',
    width: 120,
    customRender: ({ record }: any) => equipmentTypeLabelMap[record.equipmentType] || record.equipmentType || '--',
  },
  {
    title: 'BMI范围',
    dataIndex: 'bmiRange',
    key: 'bmiRange',
    width: 120,
    customRender: ({ record }: any) => bmiRangeLabelMap[record.bmiRange] || record.bmiRange || '--',
  },
  { title: '每周天数', dataIndex: 'daysPerWeek', key: 'daysPerWeek', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' },
];

const itemColumns = [
  { title: '周次', dataIndex: 'weekNo', key: 'weekNo', width: 70 },
  { title: '日次', dataIndex: 'dayNo', key: 'dayNo', width: 70 },
  { title: '动作', dataIndex: 'exerciseId', key: 'exerciseId', width: 180 },
  { title: '组', dataIndex: 'setsCount', key: 'setsCount', width: 70 },
  { title: '次', dataIndex: 'repsCount', key: 'repsCount', width: 70 },
  { title: '时长', dataIndex: 'durationMinutes', key: 'durationMinutes', width: 90 },
  { title: '说明', dataIndex: 'intensityNote', key: 'intensityNote' },
  { title: '操作', key: 'action', width: 140 },
];

const loadTemplates = async () => {
  loading.value = true;
  try {
    const res = await listTemplates(query);
    const page = res.data;
    templates.value = page.records || [];
    pagination.current = page.current || query.current;
    pagination.pageSize = page.size || query.pageSize;
    pagination.total = page.total || 0;
  } finally {
    loading.value = false;
  }
};

const loadTestItems = async () => {
  const res = await listTemplateTestItems();
  testItems.value = res.data || [];
  if (!templateForm.testItemCode && testItems.value.length) {
    templateForm.testItemCode = testItems.value[0].itemCode;
  }
};

const loadExercises = async () => {
  const res = await listTemplateExercises();
  exercises.value = res.data || [];
  if (!itemForm.exerciseId && exercises.value.length) {
    itemForm.exerciseId = exercises.value[0].id;
  }
};

const exerciseLabel = (exerciseId?: number) => {
  if (!exerciseId) return '--';
  const exercise = exercises.value.find(item => item.id === exerciseId);
  return exercise ? `${exercise.name} (#${exercise.id})` : `#${exerciseId}`;
};

const onPageChange = (p: any) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  loadTemplates();
};

const openTemplateModal = (record?: PlanTemplate) => {
  if (record) {
    Object.assign(templateForm, record);
  } else {
    Object.assign(templateForm, {
      id: undefined,
      templateCode: '',
      templateName: '',
      testItemCode: testItems.value[0]?.itemCode || '',
      scoreLevel: 'beginner',
      fitnessLevel: 'newbie',
      equipmentType: 'bodyweight',
      bmiRange: 'all',
      daysPerWeek: 3,
      description: '',
      status: 1,
    });
  }
  templateModalOpen.value = true;
};

const saveTemplate = async () => {
  await upsertTemplate(templateForm);
  message.success('模板保存成功');
  templateModalOpen.value = false;
  loadTemplates();
};

const removeTemplate = async (record: PlanTemplate) => {
  await deleteTemplate(record.id);
  message.success('模板删除成功');
  loadTemplates();
};

const openItemsDrawer = async (record: PlanTemplate) => {
  currentTemplate.value = record;
  drawerOpen.value = true;
  const res = await listTemplateItems(record.id);
  templateItems.value = res.data || [];
};

const openItemModal = (record?: PlanTemplateItem) => {
  if (record) {
    Object.assign(itemForm, record);
  } else {
    Object.assign(itemForm, {
      id: undefined,
      weekNo: 1,
      dayNo: 1,
      exerciseId: exercises.value[0]?.id || 1,
      setsCount: 3,
      repsCount: 10,
      durationMinutes: 15,
      intensityNote: '',
      sortNo: 1,
    });
  }
  itemModalOpen.value = true;
};

const saveItem = async () => {
  if (!currentTemplate.value) return;
  await upsertTemplateItem({ ...itemForm, templateId: currentTemplate.value.id });
  message.success('模板动作保存成功');
  itemModalOpen.value = false;
  const res = await listTemplateItems(currentTemplate.value.id);
  templateItems.value = res.data || [];
};

const removeItem = async (record: PlanTemplateItem) => {
  await deleteTemplateItem(record.id);
  message.success('动作项删除成功');
  if (!currentTemplate.value) return;
  const res = await listTemplateItems(currentTemplate.value.id);
  templateItems.value = res.data || [];
};

onMounted(async () => {
  await loadTestItems();
  await loadExercises();
  await loadTemplates();
});
</script>
