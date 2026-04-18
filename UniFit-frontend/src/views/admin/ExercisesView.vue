<template>
  <a-card title="动作库管理（内容发布）">
    <a-space style="margin-bottom: 12px;">
      <a-input v-model:value="query.keyword" placeholder="搜索标题/简介" style="width: 260px" @pressEnter="load" />
      <a-select
        v-model:value="query.category"
        placeholder="类型"
        style="width: 160px"
        allow-clear
        :options="typeOptions"
      />
      <a-select
        v-model:value="query.difficulty"
        placeholder="分类等级"
        style="width: 140px"
        allow-clear
        :options="levelOptions"
      />
      <a-button @click="load">查询</a-button>
      <a-button type="primary" @click="openModal()">新增内容</a-button>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      @change="onPageChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'cover'">
          <img v-if="record.coverImageUrl" :src="record.coverImageUrl" class="cover-thumb" alt="cover" />
          <span v-else>--</span>
        </template>
        <template v-else-if="column.key === 'publishTime'">
          {{ formatTime(record.publishTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openModal(record)">编辑</a-button>
            <a-button type="link" @click="openInteraction(record)">互动</a-button>
            <a-popconfirm title="确认删除该内容?" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal
    v-model:open="modalOpen"
    :title="form.id ? '编辑动作内容' : '新增动作内容'"
    wrap-class-name="exercise-fullscreen-modal"
    width="100vw"
    :style="{ top: '0' }"
    :body-style="{ height: 'calc(100vh - 142px)', overflow: 'auto' }"
    @ok="save"
  >
    <a-row :gutter="16">
      <a-col :span="14">
        <a-form layout="vertical">
          <a-form-item label="标题">
            <a-input v-model:value="form.name" placeholder="请输入标题" />
          </a-form-item>
          <a-form-item label="封面图">
            <a-space direction="vertical" style="width: 100%">
              <a-input v-model:value="form.coverImageUrl" placeholder="封面图 URL（可手填）" />
              <a-upload :show-upload-list="false" :custom-request="uploadCover">
                <a-button :loading="coverUploading">上传封面图</a-button>
              </a-upload>
              <img v-if="form.coverImageUrl" :src="form.coverImageUrl" class="cover-preview" alt="cover-preview" />
            </a-space>
          </a-form-item>
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="类型">
                <a-select
                  v-model:value="form.category"
                  :options="typeOptions"
                  show-search
                  option-filter-prop="label"
                  placeholder="请选择类型"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="分类等级">
                <a-select
                  v-model:value="form.difficulty"
                  :options="levelOptions"
                  placeholder="请选择等级"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="器械">
                <a-select
                  v-model:value="equipmentValues"
                  mode="tags"
                  :options="equipmentOptions"
                  placeholder="可多选，也可自定义输入"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item label="简介">
            <a-textarea v-model:value="form.description" :rows="2" placeholder="简要描述" />
          </a-form-item>
          <a-form-item label="示范视频">
            <a-space direction="vertical" style="width: 100%">
              <a-input v-model:value="form.demoVideoUrl" placeholder="示范视频 URL（可手填）" />
              <a-upload :show-upload-list="false" :custom-request="uploadDemoVideo">
                <a-button :loading="demoVideoUploading">上传示范视频</a-button>
              </a-upload>
              <video v-if="form.demoVideoUrl" :src="form.demoVideoUrl" class="demo-video-preview" controls />
            </a-space>
          </a-form-item>
          <a-form-item label="Markdown 内容">
            <a-space style="margin-bottom: 8px">
              <a-upload :show-upload-list="false" :custom-request="uploadContentImage">
                <a-button :loading="contentUploadingImage">插入图片</a-button>
              </a-upload>
              <a-upload :show-upload-list="false" :custom-request="uploadContentVideo">
                <a-button :loading="contentUploadingVideo">插入视频</a-button>
              </a-upload>
            </a-space>
            <a-textarea v-model:value="form.contentMd" :rows="16" placeholder="支持 Markdown 文本、图片、视频标签" />
          </a-form-item>
        </a-form>
      </a-col>
      <a-col :span="10">
        <div class="preview-wrap">
          <div class="preview-title">预览</div>
          <div class="preview-content markdown-content" v-html="previewHtml"></div>
        </div>
      </a-col>
    </a-row>
  </a-modal>

  <a-drawer v-model:open="interactionOpen" title="评论与点赞" width="520">
    <template v-if="currentExercise">
      <div class="interaction-header">
        <div><strong>{{ currentExercise.name }}</strong></div>
        <div class="meta-line">
          发布者：{{ currentExercise.publishUserName || '--' }} | 发布时间：{{ formatTime(currentExercise.publishTime) }}
        </div>
      </div>
      <a-space style="margin-bottom: 12px">
        <a-button type="primary" @click="toggleLikeCurrent">
          {{ likeState.liked ? '取消点赞' : '点赞' }}（{{ likeState.likeCount }}）
        </a-button>
        <span>评论数：{{ currentExercise.commentCount || 0 }}</span>
      </a-space>

      <a-input-group compact style="display: flex; margin-bottom: 14px">
        <a-input v-model:value="commentInput" placeholder="输入评论内容" style="flex: 1" />
        <a-button type="primary" :loading="commentSubmitting" @click="submitComment">发布评论</a-button>
      </a-input-group>

      <a-list :data-source="comments" :locale="{ emptyText: '暂无评论' }" item-layout="vertical">
        <template #renderItem="{ item }">
          <a-list-item>
            <template #actions>
              <span>{{ formatTime(item.createTime) }}</span>
              <a-popconfirm title="删除该评论？" @confirm="removeComment(item)">
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </template>
            <a-list-item-meta :title="item.userName || '用户'" />
            <div>{{ item.content }}</div>
          </a-list-item>
        </template>
      </a-list>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  addExerciseComment,
  deleteExercise,
  deleteExerciseComment,
  getExerciseLikeStatus,
  listExerciseComments,
  listExercises,
  toggleExerciseLike,
  upsertExercise,
  uploadExerciseMedia,
} from '../../api';
import type { Exercise, ExerciseComment } from '../../api';

const loading = ref(false);
const modalOpen = ref(false);
const rows = ref<Exercise[]>([]);
const query = reactive({ current: 1, pageSize: 10, keyword: '', category: undefined, difficulty: undefined });
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const categoryLabelMap: Record<string, string> = {
  cardio: '有氧',
  aerobic: '有氧',
  strength: '力量',
  core: '核心',
  upper: '上肢',
  upper_body: '上肢',
  lower: '下肢',
  lower_body: '下肢',
  recovery: '恢复',
};

const difficultyLabelMap: Record<string, string> = {
  newbie: '零基础',
  beginner: '初级',
  basic: '初级',
  intermediate: '进阶',
  advanced: '强化',
  '零基础': '零基础',
  '初级': '初级',
  '进阶': '进阶',
  '强化': '强化',
};

const equipmentLabelMap: Record<string, string> = {
  bodyweight: '无器械',
  dorm: '宿舍器械',
  dorm_equipment: '宿舍器械',
  gym: '健身房',
  track: '跑道',
  band: '弹力带',
  dumbbell: '哑铃',
  kettlebell: '壶铃',
  barbell: '杠铃',
  mat: '瑜伽垫',
  rope: '跳绳',
};

const typeOptions = [
  { label: '有氧操', value: '有氧操' },
  { label: '跳绳', value: '跳绳' },
  { label: '八段锦', value: '八段锦' },
  { label: 'HIIT', value: 'HIIT' },
  { label: '舞蹈燃脂', value: '舞蹈燃脂' },
  { label: '腰腹减脂塑形', value: '腰腹减脂塑形' },
  { label: '瑜伽', value: '瑜伽' },
  { label: '跑步', value: '跑步' },
  { label: '增肌', value: '增肌' },
  { label: '冥想', value: '冥想' },
  { label: '瘦腿', value: '瘦腿' },
  { label: '体态矫正', value: '体态矫正' },
  { label: '有氧', value: 'cardio' },
  { label: '力量', value: 'strength' },
  { label: '核心', value: 'core' },
  { label: '上肢', value: 'upper' },
  { label: '下肢', value: 'lower' },
  { label: '恢复', value: 'recovery' },
];

const levelOptions = [
  { label: '零基础', value: 'newbie' },
  { label: '初级', value: 'beginner' },
  { label: '进阶', value: 'intermediate' },
  { label: '强化', value: 'advanced' },
  { label: '零基础', value: '零基础' },
  { label: '初级', value: '初级' },
  { label: '进阶', value: '进阶' },
  { label: '强化', value: '强化' },
];

const equipmentOptions = [
  '无器械',
  '瑜伽垫',
  '跳绳',
  '弹力带',
  '哑铃',
  '壶铃',
  '杠铃',
  '泡沫轴',
  '健身球',
  '拉力器',
  '跑步机',
  '动感单车',
  '划船机',
].map(item => ({ label: item, value: item }));

const form = reactive<any>({
  id: undefined,
  name: '',
  category: '',
  difficulty: '',
  equipmentRequired: '',
  description: '',
  coverImageUrl: '',
  contentMd: '',
  demoVideoUrl: '',
  status: 1,
});
const equipmentValues = ref<string[]>([]);

const interactionOpen = ref(false);
const currentExercise = ref<Exercise | null>(null);
const comments = ref<ExerciseComment[]>([]);
const commentInput = ref('');
const commentSubmitting = ref(false);
const likeState = reactive({ liked: false, likeCount: 0 });

const coverUploading = ref(false);
const demoVideoUploading = ref(false);
const contentUploadingImage = ref(false);
const contentUploadingVideo = ref(false);

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '封面', key: 'cover', width: 90 },
  { title: '标题', dataIndex: 'name', key: 'name', width: 200 },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: 100,
    customRender: ({ record }: any) => categoryLabelMap[record.category] || record.category || '--',
  },
  {
    title: '难度',
    dataIndex: 'difficulty',
    key: 'difficulty',
    width: 100,
    customRender: ({ record }: any) => difficultyLabelMap[record.difficulty] || record.difficulty || '--',
  },
  { title: '发布者', dataIndex: 'publishUserName', key: 'publishUserName', width: 120 },
  { title: '发布时间', key: 'publishTime', width: 160 },
  { title: '点赞', dataIndex: 'likeCount', key: 'likeCount', width: 80 },
  { title: '评论', dataIndex: 'commentCount', key: 'commentCount', width: 80 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
];

const formatTime = (v?: string) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '--');

const renderMarkdown = (markdown: string) => {
  if (!markdown) return '<p style="color:#94a3b8;">暂无内容</p>';
  return markdown
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img alt="$1" src="$2" />')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/\n/g, '<br/>');
};

const previewHtml = computed(() => renderMarkdown(form.contentMd || ''));

const load = async () => {
  loading.value = true;
  try {
    const res = await listExercises(query);
    const page = res.data;
    rows.value = page.records || [];
    pagination.total = page.total || 0;
    pagination.current = page.current || query.current;
    pagination.pageSize = page.size || query.pageSize;
  } finally {
    loading.value = false;
  }
};

const onPageChange = (p: any) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  load();
};

const openModal = (row?: Exercise) => {
  if (row) {
    Object.assign(form, row);
    equipmentValues.value = row.equipmentRequired
      ? row.equipmentRequired
        .split(/[、,，\s]+/)
        .filter(Boolean)
        .map(item => equipmentLabelMap[item] || item)
      : [];
  } else {
    Object.assign(form, {
      id: undefined,
      name: '',
      category: '',
      difficulty: '',
      equipmentRequired: '',
      description: '',
      coverImageUrl: '',
      contentMd: '',
      demoVideoUrl: '',
      status: 1,
    });
    equipmentValues.value = [];
  }
  modalOpen.value = true;
};

const save = async () => {
  if (!form.name?.trim()) {
    message.error('请输入标题');
    return;
  }
  await upsertExercise({
    ...form,
    name: form.name.trim(),
    description: form.description?.trim(),
    equipmentRequired: equipmentValues.value.length ? equipmentValues.value.join('、') : undefined,
    contentMd: form.contentMd || '',
    demoVideoUrl: form.demoVideoUrl?.trim() || undefined,
    coverImageUrl: form.coverImageUrl?.trim() || undefined,
    status: 1,
  });
  message.success('保存成功');
  modalOpen.value = false;
  await load();
};

const remove = async (row: Exercise) => {
  await deleteExercise(row.id);
  message.success('删除成功');
  await load();
};

const uploadByType = async (file: File, bizType: string) => {
  const res = await uploadExerciseMedia(file, bizType);
  return res.data;
};

const uploadCover = async (options: any) => {
  coverUploading.value = true;
  try {
    const url = await uploadByType(options.file as File, 'exercise-cover');
    form.coverImageUrl = url;
    message.success('封面上传成功');
    options.onSuccess?.(url);
  } catch (e) {
    options.onError?.(e);
  } finally {
    coverUploading.value = false;
  }
};

const uploadDemoVideo = async (options: any) => {
  demoVideoUploading.value = true;
  try {
    const url = await uploadByType(options.file as File, 'exercise-demo-video');
    form.demoVideoUrl = url;
    message.success('示范视频上传成功');
    options.onSuccess?.(url);
  } catch (e) {
    options.onError?.(e);
  } finally {
    demoVideoUploading.value = false;
  }
};

const appendContent = (text: string) => {
  form.contentMd = `${form.contentMd || ''}\n${text}\n`;
};

const uploadContentImage = async (options: any) => {
  contentUploadingImage.value = true;
  try {
    const url = await uploadByType(options.file as File, 'exercise-image');
    appendContent(`![图片描述](${url})`);
    message.success('图片上传成功，已插入正文');
    options.onSuccess?.(url);
  } catch (e) {
    options.onError?.(e);
  } finally {
    contentUploadingImage.value = false;
  }
};

const uploadContentVideo = async (options: any) => {
  contentUploadingVideo.value = true;
  try {
    const url = await uploadByType(options.file as File, 'exercise-video');
    appendContent(`<video controls src="${url}" style="max-width:100%;"></video>`);
    message.success('视频上传成功，已插入正文');
    options.onSuccess?.(url);
  } catch (e) {
    options.onError?.(e);
  } finally {
    contentUploadingVideo.value = false;
  }
};

const loadComments = async () => {
  if (!currentExercise.value?.id) return;
  const res = await listExerciseComments(currentExercise.value.id);
  comments.value = res.data || [];
};

const loadLikeStatus = async () => {
  if (!currentExercise.value?.id) return;
  const res = await getExerciseLikeStatus(currentExercise.value.id);
  likeState.liked = !!res.data?.liked;
  likeState.likeCount = res.data?.likeCount || 0;
};

const openInteraction = async (row: Exercise) => {
  currentExercise.value = row;
  interactionOpen.value = true;
  commentInput.value = '';
  await Promise.all([loadComments(), loadLikeStatus()]);
};

const submitComment = async () => {
  if (!currentExercise.value?.id) return;
  if (!commentInput.value.trim()) {
    message.error('请输入评论内容');
    return;
  }
  commentSubmitting.value = true;
  try {
    await addExerciseComment(currentExercise.value.id, commentInput.value.trim());
    commentInput.value = '';
    await loadComments();
    await load();
  } finally {
    commentSubmitting.value = false;
  }
};

const removeComment = async (item: ExerciseComment) => {
  await deleteExerciseComment(item.id);
  message.success('评论已删除');
  await loadComments();
  await load();
};

const toggleLikeCurrent = async () => {
  if (!currentExercise.value?.id) return;
  const res = await toggleExerciseLike(currentExercise.value.id);
  likeState.liked = res.data?.liked || false;
  likeState.likeCount = res.data?.likeCount || 0;
  await load();
};

onMounted(load);
</script>

<style scoped>
.cover-thumb {
  width: 56px;
  height: 36px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-preview {
  width: 180px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
}

.demo-video-preview {
  width: 260px;
  max-width: 100%;
  border-radius: 6px;
  background: #000;
}

.preview-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  height: 100%;
  min-height: 620px;
  overflow: hidden;
}

.preview-title {
  border-bottom: 1px solid #e5e7eb;
  padding: 10px 12px;
  font-weight: 600;
  color: #334155;
}

.preview-content {
  padding: 12px;
  height: calc(100% - 42px);
  overflow: auto;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin: 10px 0;
  line-height: 1.4;
}

.markdown-content :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}

.markdown-content :deep(video) {
  max-width: 100%;
  border-radius: 6px;
}

.interaction-header {
  margin-bottom: 10px;
}

.meta-line {
  color: #64748b;
  margin-top: 6px;
}

:deep(.exercise-fullscreen-modal .ant-modal) {
  max-width: 100vw;
  margin: 0;
  padding-bottom: 0;
}

:deep(.exercise-fullscreen-modal .ant-modal-content) {
  min-height: 100vh;
  border-radius: 0;
}

:deep(.exercise-fullscreen-modal .ant-modal-body) {
  padding-top: 12px;
}
</style>
