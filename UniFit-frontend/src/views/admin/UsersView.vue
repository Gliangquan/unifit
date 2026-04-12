<template>
  <a-card title="用户管理">
    <a-space style="margin-bottom: 12px;">
      <a-input v-model:value="query.userName" placeholder="用户名" style="width: 180px" />
      <a-input v-model:value="query.userPhone" placeholder="手机号" style="width: 180px" />
      <a-select v-model:value="query.userRole" placeholder="角色" style="width: 140px" allow-clear>
        <a-select-option value="student">学生</a-select-option>
        <a-select-option value="admin">管理员</a-select-option>
        <a-select-option value="ban">禁用</a-select-option>
      </a-select>
      <a-button type="primary" @click="load">查询</a-button>
    </a-space>

    <a-table :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" row-key="id"
      @change="onPageChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'userRole'">
          {{ getRoleLabel(record.userRole) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="showDetail(record)">查看</a-button>
            <a-button size="small" @click="showEdit(record)">编辑</a-button>
            <a-button size="small" @click="toggle(record)">{{ record.status === 1 ? '禁用' : '启用' }}</a-button>
            <a-popconfirm title="确定删除该用户吗？" ok-text="确定" cancel-text="取消" @confirm="deleteUser(record)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 查看详情对话框 -->
    <a-modal v-model:open="detailVisible" title="用户详情" :footer="null">
      <a-descriptions :column="1" bordered v-if="currentUser">
        <a-descriptions-item label="ID">{{ currentUser.id }}</a-descriptions-item>
        <a-descriptions-item label="账号">{{ currentUser.userAccount }}</a-descriptions-item>
        <a-descriptions-item label="姓名">{{ currentUser.userName }}</a-descriptions-item>
        <a-descriptions-item label="角色">{{ getRoleLabel(currentUser.userRole) }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentUser.userPhone }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ currentUser.userEmail }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentUser.status === 1 ? 'green' : 'red'">{{ currentUser.status === 1 ? '启用' : '禁用' }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑对话框 -->
    <a-modal v-model:open="editVisible" title="编辑用户" @ok="submitEdit" :confirm-loading="editLoading">
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="姓名">
          <a-input v-model:value="editForm.userName" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.userPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="editForm.userRole">
            <a-select-option value="student">学生</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="ban">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { listUsers, updateUserStatus, getUserDetail, updateUser, deleteUser as deleteUserApi } from '../../api';

const loading = ref(false);
const editLoading = ref(false);
const rows = ref([]);
const query = reactive({ current: 1, pageSize: 10, userName: '', userPhone: '', userRole: undefined });
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const detailVisible = ref(false);
const editVisible = ref(false);
const currentUser = ref(null);
const editForm = reactive({ userName: '', userPhone: '', userRole: '' });

const roleMap = {
  student: '学生',
  admin: '管理员',
  ban: '禁用'
};

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 160 },
  { title: '姓名', dataIndex: 'userName', key: 'userName', width: 140 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 120 },
  { title: '手机号', dataIndex: 'userPhone', key: 'userPhone', width: 140 },
  { title: '状态', key: 'status', width: 120 },
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
  } finally {
    loading.value = false;
  }
};

const onPageChange = (p) => {
  query.current = p.current;
  query.pageSize = p.pageSize;
  load();
};

const toggle = async (record) => {
  await updateUserStatus(record.id, record.status === 1 ? 0 : 1);
  message.success('状态更新成功');
  await load();
};

const showDetail = async (record) => {
  const res = await getUserDetail(record.id);
  currentUser.value = res.data;
  detailVisible.value = true;
};

const showEdit = async (record) => {
  const res = await getUserDetail(record.id);
  const user = res.data;
  editForm.userName = user.userName;
  editForm.userPhone = user.userPhone;
  editForm.userRole = user.userRole;
  currentUser.value = user;
  editVisible.value = true;
};

const submitEdit = async () => {
  editLoading.value = true;
  try {
    await updateUser(currentUser.value.id, editForm.userName, editForm.userPhone, editForm.userRole);
    message.success('用户信息更新成功');
    editVisible.value = false;
    await load();
  } finally {
    editLoading.value = false;
  }
};

const deleteUser = async (record) => {
  try {
    await deleteUserApi(record.id);
    message.success('用户删除成功');
    await load();
  } catch (error) {
    message.error('删除失败');
  }
};

onMounted(load);
</script>
