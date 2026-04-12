<template>
  <div class="role-configuration">
    <a-card title="角色配置">
      <a-alert 
        message="角色配置说明" 
        description="在此配置系统中的角色及其基本属性。系统预设了三个角色，可以根据需要进行修改。"
        type="info" 
        show-icon
        style="margin-bottom: 16px;"
      />

      <!-- 角色列表 -->
      <a-table 
        :columns="columns" 
        :data-source="roles" 
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="showEdit(record)">编辑</a-button>
              <a-button 
                size="small" 
                @click="toggleStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 添加角色按钮 -->
      <a-button type="primary" style="margin-top: 16px;" @click="showAddRole">
        添加角色
      </a-button>
    </a-card>

    <!-- 编辑对话框 -->
    <a-modal 
      v-model:open="editVisible" 
      :title="isAddMode ? '添加角色' : '编辑角色'" 
      @ok="submitEdit" 
      :confirm-loading="editLoading"
      width="600px"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="角色名称" required>
          <a-input 
            v-model:value="editForm.roleName" 
            placeholder="请输入角色名称"
            :disabled="!isAddMode"
          />
        </a-form-item>
        <a-form-item label="角色描述">
          <a-textarea 
            v-model:value="editForm.description" 
            placeholder="请输入角色描述"
            :rows="4"
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

const loading = ref(false);
const editLoading = ref(false);
const isAddMode = ref(false);
const editVisible = ref(false);

const roles = ref([
  {
    id: 1,
    roleName: '学生',
    description: '普通学生用户，可以查看个人信息、打卡、查看训练计划等',
    status: 1,
  },
  {
    id: 2,
    roleName: '教师',
    description: '教师用户，可以管理班级、查看学生数据、创建训练计划等',
    status: 1,
  },
  {
    id: 3,
    roleName: '管理员',
    description: '系统管理员，拥有所有权限',
    status: 1,
  },
]);

const editForm = reactive({
  roleName: '',
  description: '',
  status: 1,
});

const columns = [
  { title: '角色ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 120 },
  { title: '角色描述', dataIndex: 'description', key: 'description' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 200 },
];

const showAddRole = () => {
  isAddMode.value = true;
  editForm.roleName = '';
  editForm.description = '';
  editForm.status = 1;
  editVisible.value = true;
};

const showEdit = (record) => {
  isAddMode.value = false;
  editForm.roleName = record.roleName;
  editForm.description = record.description;
  editForm.status = record.status;
  editVisible.value = true;
};

const submitEdit = () => {
  if (!editForm.roleName) {
    message.error('角色名称不能为空');
    return;
  }

  editLoading.value = true;
  setTimeout(() => {
    if (isAddMode.value) {
      roles.value.push({
        id: Math.max(...roles.value.map(r => r.id)) + 1,
        roleName: editForm.roleName,
        description: editForm.description,
        status: editForm.status,
      });
      message.success('角色添加成功');
    } else {
      message.success('角色更新成功');
    }
    editVisible.value = false;
    editLoading.value = false;
  }, 500);
};

const toggleStatus = (record) => {
  record.status = record.status === 1 ? 0 : 1;
  message.success('角色状态已更新');
};

onMounted(() => {
  // 初始化
});
</script>

<style scoped>
.role-configuration {
  width: 100%;
}
</style>
