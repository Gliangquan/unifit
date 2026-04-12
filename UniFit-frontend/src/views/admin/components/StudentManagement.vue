<template>
  <div class="student-management">
    <!-- 搜索和筛选 -->
    <a-card style="margin-bottom: 16px;">
      <a-space style="width: 100%; margin-bottom: 12px;">
        <a-input 
          v-model:value="query.userName" 
          placeholder="学生姓名" 
          style="width: 180px" 
        />
        <a-input 
          v-model:value="query.userPhone" 
          placeholder="手机号" 
          style="width: 180px" 
        />
        <a-select 
          v-model:value="query.userRole" 
          placeholder="角色" 
          style="width: 140px" 
          allow-clear
        >
          <a-select-option value="student">学生</a-select-option>
        </a-select>
        <a-select 
          v-model:value="query.status" 
          placeholder="状态" 
          style="width: 140px" 
          allow-clear
        >
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">禁用</a-select-option>
        </a-select>
        <a-button type="primary" @click="load">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>
    </a-card>

    <!-- 用户列表 -->
    <a-card title="学生列表">
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
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'userRole'">
            {{ getRoleLabel(record.userRole) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="showDetail(record)">查看</a-button>
              <a-button size="small" @click="showEdit(record)">编辑</a-button>
              <a-button 
                size="small" 
                @click="toggleStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-popconfirm 
                title="确定删除该学生吗？" 
                ok-text="确定" 
                cancel-text="取消" 
                @confirm="deleteStudent(record)"
              >
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 查看详情对话框 -->
    <a-modal 
      v-model:open="detailVisible" 
      title="学生详情" 
      :footer="null"
      width="600px"
    >
      <a-descriptions :column="1" bordered v-if="currentUser">
        <a-descriptions-item label="ID">{{ currentUser.id }}</a-descriptions-item>
        <a-descriptions-item label="账号">{{ currentUser.userAccount }}</a-descriptions-item>
        <a-descriptions-item label="姓名">{{ currentUser.userName }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentUser.userPhone }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ currentUser.userEmail }}</a-descriptions-item>
        <a-descriptions-item label="角色">{{ getRoleLabel(currentUser.userRole) }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentUser.status === 1 ? 'green' : 'red'">
            {{ currentUser.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑对话框 -->
    <a-modal 
      v-model:open="editVisible" 
      title="编辑学生信息" 
      @ok="submitEdit" 
      :confirm-loading="editLoading"
      width="600px"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="姓名" required>
          <a-input 
            v-model:value="editForm.userName" 
            placeholder="请输入姓名" 
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input 
            v-model:value="editForm.userPhone" 
            placeholder="请输入手机号" 
          />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input 
            v-model:value="editForm.userEmail" 
            placeholder="请输入邮箱" 
            type="email"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { listUsers, getUserDetail, updateUser, deleteUser as deleteUserApi } from '../../../api';

const loading = ref(false);
const editLoading = ref(false);
const rows = ref([]);
const query = reactive({ 
  current: 1, 
  pageSize: 10, 
  userName: '', 
  userPhone: '', 
  userRole: 'student',
  status: undefined
});
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const detailVisible = ref(false);
const editVisible = ref(false);
const currentUser = ref(null);
const editForm = reactive({ 
  userName: '', 
  userPhone: '', 
  userEmail: ''
});

const roleMap = {
  student: '学生',
  admin: '管理员',
  ban: '禁用'
};

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 140 },
  { title: '姓名', dataIndex: 'userName', key: 'userName', width: 120 },
  { title: '手机号', dataIndex: 'userPhone', key: 'userPhone', width: 140 },
  { title: '邮箱', dataIndex: 'userEmail', key: 'userEmail', width: 160 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 280 },
];

const getRoleLabel = (role) => {
  return roleMap[role] || role;
};

const load = async () => {
  loading.value = true;
  try {
    const res = await listUsers(query);
    const page = res.data;
    rows.value = page.records || [];
    pagination.total = page.total || 0;
    pagination.current = page.current || query.current;
    pagination.pageSize = page.size || query.pageSize;
  } catch (error) {
    message.error('加载学生列表失败');
  } finally {
    loading.value = false;
  }
};

const onPageChange = (p) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  load();
};

const resetQuery = () => {
  query.userName = '';
  query.userPhone = '';
  query.userRole = 'student';
  query.status = undefined;
  query.current = 1;
  load();
};

const toggleStatus = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1;
    await updateUser(record.id, undefined, undefined, undefined);
    message.success('状态更新成功');
    await load();
  } catch (error) {
    message.error('状态更新失败');
  }
};

const showDetail = async (record) => {
  try {
    const res = await getUserDetail(record.id);
    currentUser.value = res.data;
    detailVisible.value = true;
  } catch (error) {
    message.error('获取学生详情失败');
  }
};

const showEdit = async (record) => {
  try {
    const res = await getUserDetail(record.id);
    const user = res.data;
    editForm.userName = user.userName;
    editForm.userPhone = user.userPhone;
    editForm.userEmail = user.userEmail;
    currentUser.value = user;
    editVisible.value = true;
  } catch (error) {
    message.error('获取学生详情失败');
  }
};

const submitEdit = async () => {
  if (!editForm.userName) {
    message.error('姓名不能为空');
    return;
  }

  editLoading.value = true;
  try {
    await updateUser(currentUser.value.id, editForm.userName, editForm.userPhone);
    message.success('学生信息更新成功');
    editVisible.value = false;
    await load();
  } catch (error) {
    message.error('更新失败');
  } finally {
    editLoading.value = false;
  }
};

const deleteStudent = async (record) => {
  try {
    await deleteUserApi(record.id);
    message.success('学生删除成功');
    await load();
  } catch (error) {
    message.error('删除失败');
  }
};

onMounted(load);
</script>

<style scoped>
.student-management {
  width: 100%;
}
</style>
