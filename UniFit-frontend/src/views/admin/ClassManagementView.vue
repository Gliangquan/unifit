<template>
  <div class="class-management">
    <a-card title="班级管理">
      <!-- 操作按钮 -->
      <a-space style="margin-bottom: 16px;">
        <a-button type="primary" @click="showAddModal">添加班级</a-button>
        <a-input-search 
          v-model:value="searchText" 
          placeholder="搜索班级名称或代码" 
          style="width: 250px"
          @search="load"
        />
      </a-space>

      <!-- 班级列表 -->
      <a-table 
        :columns="columns" 
        :data-source="classList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="onPageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="showEditModal(record)">编辑</a-button>
              <a-button size="small" @click="showMembersModal(record)">成员</a-button>
              <a-button size="small" @click="showTeachersModal(record)">教师</a-button>
              <a-popconfirm 
                title="确定删除此班级吗？" 
                @confirm="deleteClass(record.id)"
              >
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑班级对话框 -->
    <a-modal 
      v-model:open="editVisible" 
      :title="isAddMode ? '添加班级' : '编辑班级'" 
      @ok="submitEdit" 
      :confirm-loading="editLoading"
      width="600px"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="班级名称" required>
          <a-input 
            v-model:value="editForm.className" 
            placeholder="请输入班级名称"
          />
        </a-form-item>
        <a-form-item label="班级代码" required>
          <a-input 
            v-model:value="editForm.classCode" 
            placeholder="请输入班级代码"
          />
        </a-form-item>
        <a-form-item label="年级">
          <a-input 
            v-model:value="editForm.grade" 
            placeholder="请输入年级"
          />
        </a-form-item>
        <a-form-item label="专业">
          <a-input 
            v-model:value="editForm.major" 
            placeholder="请输入专业"
          />
        </a-form-item>
        <a-form-item label="班主任">
          <a-select 
            v-model:value="editForm.teacherId" 
            placeholder="选择班主任"
            allow-clear
          >
            <a-select-option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
              {{ teacher.userName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="班级描述">
          <a-textarea 
            v-model:value="editForm.description" 
            placeholder="请输入班级描述"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="editForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 班级成员对话框 -->
    <a-modal 
      v-model:open="membersVisible" 
      title="班级成员管理" 
      width="900px"
      :footer="null"
    >
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="selectedStudentId" 
          placeholder="选择学生添加" 
          style="width: 250px"
          allow-clear
        >
          <a-select-option v-for="student in availableStudents" :key="student.id" :value="student.id">
            {{ student.userName }} ({{ student.userAccount }})
          </a-select-option>
        </a-select>
        <a-button type="primary" @click="addMember" :loading="addingMember">添加</a-button>
      </a-space>

      <a-table 
        :columns="memberColumns" 
        :data-source="classMembers"
        :pagination="false"
        size="small"
        :loading="loadingMembers"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userId'">
            {{ record.teacherId }}
          </template>
          <template v-else-if="column.key === 'userName'">
            {{ record.teacherName }}
          </template>
          <template v-else-if="column.key === 'joinTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm 
              title="确定移除此成员吗？" 
              @confirm="removeMember(record.teacherId)"
            >
              <a-button size="small" danger>移除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 班级教师对话框 -->
    <a-modal 
      v-model:open="teachersVisible" 
      title="班级教师管理" 
      width="900px"
      :footer="null"
    >
      <a-space style="margin-bottom: 16px;">
        <a-select 
          v-model:value="selectedTeacherId" 
          placeholder="选择教师添加" 
          style="width: 250px"
          allow-clear
        >
          <a-select-option v-for="teacher in availableTeachers" :key="teacher.id" :value="teacher.id">
            {{ teacher.userName }} ({{ teacher.userAccount }})
          </a-select-option>
        </a-select>
        <a-select 
          v-model:value="selectedTeacherRole" 
          style="width: 150px"
        >
          <a-select-option value="teacher">教师</a-select-option>
          <a-select-option value="head_teacher">班主任</a-select-option>
        </a-select>
        <a-button type="primary" @click="addTeacher" :loading="addingTeacher">添加</a-button>
      </a-space>

      <a-table 
        :columns="teacherColumns" 
        :data-source="classTeachers"
        :pagination="false"
        size="small"
        :loading="loadingTeachers"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'teacherId'">
            {{ record.teacherId }}
          </template>
          <template v-else-if="column.key === 'teacherName'">
            {{ record.teacherName }}
          </template>
          <template v-else-if="column.key === 'userPhone'">
            {{ record.userPhone }}
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.description === 'head_teacher' ? 'blue' : 'green'">
              {{ record.description === 'head_teacher' ? '班主任' : '教师' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm 
              title="确定移除此教师吗？" 
              @confirm="removeTeacher(record.teacherId)"
            >
              <a-button size="small" danger>移除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { 
  getClassList, 
  createClass, 
  updateClass, 
  deleteClass as deleteClassApi,
  addClassMember,
  removeClassMember,
  addClassTeacher,
  removeClassTeacher,
  getClassMembers,
  getClassTeachers,
} from '../../api';
import { getUserList } from '../../api';

const loading = ref(false);
const editLoading = ref(false);
const loadingMembers = ref(false);
const addingMember = ref(false);
const loadingTeachers = ref(false);
const addingTeacher = ref(false);
const classList = ref([]);
const classMembers = ref([]);
const classTeachers = ref([]);
const teachers = ref([]);
const students = ref([]);
const availableStudents = ref([]);
const availableTeachers = ref([]);

const searchText = ref('');
const editVisible = ref(false);
const membersVisible = ref(false);
const teachersVisible = ref(false);
const isAddMode = ref(false);
const currentClassId = ref(null);
const selectedStudentId = ref(null);
const selectedTeacherId = ref(null);
const selectedTeacherRole = ref('teacher');

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const editForm = reactive({
  className: '',
  classCode: '',
  grade: '',
  major: '',
  teacherId: null,
  description: '',
  status: 1,
});

const columns = [
  { title: '班级名称', dataIndex: 'className', key: 'className', width: 120 },
  { title: '班级代码', dataIndex: 'classCode', key: 'classCode', width: 120 },
  { title: '年级', dataIndex: 'grade', key: 'grade', width: 80 },
  { title: '专业', dataIndex: 'major', key: 'major', width: 120 },
  { title: '班主任', dataIndex: 'teacherName', key: 'teacherName', width: 100 },
  { title: '学生数', dataIndex: 'studentCount', key: 'studentCount', width: 80 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 280 },
];

const memberColumns = [
  { title: '学生ID', dataIndex: 'userId', key: 'userId', width: 80 },
  { title: '学生名称', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 120 },
  { title: '加入时间', dataIndex: 'joinTime', key: 'joinTime', width: 160 },
  { title: '操作', key: 'action', width: 100 },
];

const teacherColumns = [
  { title: '教师ID', dataIndex: 'teacherId', key: 'teacherId', width: 80 },
  { title: '教师名称', dataIndex: 'teacherName', key: 'teacherName', width: 120 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 120 },
  { title: '手机号', dataIndex: 'userPhone', key: 'userPhone', width: 120 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

const load = async () => {
  loading.value = true;
  try {
    const res = await getClassList();
    classList.value = res.data || [];
    pagination.total = classList.value.length;
  } catch (error) {
    message.error('加载班级列表失败');
  } finally {
    loading.value = false;
  }
};

const loadTeachers = async () => {
  try {
    const res = await getUserList();
    teachers.value = res.data?.filter(u => u.userRole === 'teacher') || [];
  } catch (error) {
    console.error('加载教师列表失败');
  }
};

const loadStudents = async () => {
  try {
    const res = await getUserList();
    students.value = res.data?.filter(u => u.userRole === 'student') || [];
  } catch (error) {
    console.error('加载学生列表失败');
  }
};

const showAddModal = () => {
  isAddMode.value = true;
  editForm.className = '';
  editForm.classCode = '';
  editForm.grade = '';
  editForm.major = '';
  editForm.teacherId = null;
  editForm.description = '';
  editForm.status = 1;
  editVisible.value = true;
};

const showEditModal = (record) => {
  isAddMode.value = false;
  Object.assign(editForm, record);
  editVisible.value = true;
};

const submitEdit = async () => {
  if (!editForm.className || !editForm.classCode) {
    message.error('班级名称和代码不能为空');
    return;
  }

  editLoading.value = true;
  try {
    if (isAddMode.value) {
      await createClass(editForm);
      message.success('班级添加成功');
    } else {
      await updateClass(editForm);
      message.success('班级更新成功');
    }
    editVisible.value = false;
    await load();
  } catch (error) {
    message.error('操作失败');
  } finally {
    editLoading.value = false;
  }
};

const deleteClass = async (classId) => {
  try {
    await deleteClassApi(classId);
    message.success('班级删除成功');
    await load();
  } catch (error) {
    message.error('删除失败');
  }
};

const showMembersModal = async (record) => {
  currentClassId.value = record.id;
  selectedStudentId.value = null;
  membersVisible.value = true;
  loadingMembers.value = true;
  
  try {
    const res = await getClassMembers(record.id);
    classMembers.value = res.data || [];
    
    const memberIds = classMembers.value.map(m => m.teacherId);
    availableStudents.value = students.value.filter(s => !memberIds.includes(s.id));
  } catch (error) {
    message.error('加载班级成员失败');
  } finally {
    loadingMembers.value = false;
  }
};

const addMember = async () => {
  if (!selectedStudentId.value) {
    message.error('请选择学生');
    return;
  }

  addingMember.value = true;
  try {
    await addClassMember(currentClassId.value, selectedStudentId.value);
    message.success('成员添加成功');
    selectedStudentId.value = null;
    
    const res = await getClassMembers(currentClassId.value);
    classMembers.value = res.data || [];
    
    const memberIds = classMembers.value.map(m => m.teacherId);
    availableStudents.value = students.value.filter(s => !memberIds.includes(s.id));
    
    await load();
  } catch (error) {
    message.error('添加失败');
  } finally {
    addingMember.value = false;
  }
};

const removeMember = async (userId) => {
  try {
    await removeClassMember(currentClassId.value, userId);
    message.success('成员移除成功');
    
    const res = await getClassMembers(currentClassId.value);
    classMembers.value = res.data || [];
    
    const memberIds = classMembers.value.map(m => m.teacherId);
    availableStudents.value = students.value.filter(s => !memberIds.includes(s.id));
    
    await load();
  } catch (error) {
    message.error('移除失败');
  }
};

const showTeachersModal = async (record) => {
  currentClassId.value = record.id;
  selectedTeacherId.value = null;
  selectedTeacherRole.value = 'teacher';
  teachersVisible.value = true;
  loadingTeachers.value = true;
  
  try {
    const res = await getClassTeachers(record.id);
    classTeachers.value = res.data || [];
    
    const teacherIds = classTeachers.value.map(t => t.teacherId);
    availableTeachers.value = teachers.value.filter(t => !teacherIds.includes(t.id));
  } catch (error) {
    message.error('加载班级教师失败');
  } finally {
    loadingTeachers.value = false;
  }
};

const addTeacher = async () => {
  if (!selectedTeacherId.value) {
    message.error('请选择教师');
    return;
  }

  addingTeacher.value = true;
  try {
    await addClassTeacher(currentClassId.value, selectedTeacherId.value, selectedTeacherRole.value);
    message.success('教师添加成功');
    selectedTeacherId.value = null;
    
    const res = await getClassTeachers(currentClassId.value);
    classTeachers.value = res.data || [];
    
    const teacherIds = classTeachers.value.map(t => t.teacherId);
    availableTeachers.value = teachers.value.filter(t => !teacherIds.includes(t.id));
    
    await load();
  } catch (error) {
    message.error('添加失败');
  } finally {
    addingTeacher.value = false;
  }
};

const removeTeacher = async (teacherId) => {
  try {
    await removeClassTeacher(currentClassId.value, teacherId);
    message.success('教师移除成功');
    
    const res = await getClassTeachers(currentClassId.value);
    classTeachers.value = res.data || [];
    
    const teacherIds = classTeachers.value.map(t => t.teacherId);
    availableTeachers.value = teachers.value.filter(t => !teacherIds.includes(t.id));
    
    await load();
  } catch (error) {
    message.error('移除失败');
  }
};

const onPageChange = (p) => {
  pagination.current = p.current;
  pagination.pageSize = p.pageSize;
};

const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

onMounted(async () => {
  await Promise.all([
    load(),
    loadTeachers(),
    loadStudents(),
  ]);
});
</script>

<style scoped>
.class-management {
  width: 100%;
}
</style>
