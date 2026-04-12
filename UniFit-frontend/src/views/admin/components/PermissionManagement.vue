<template>
  <div class="permission-management">
    <a-card title="权限管理">
      <a-alert 
        message="权限管理说明" 
        description="在此配置不同角色的功能权限。系统预设了三个角色：学生、教师、管理员。"
        type="info" 
        show-icon
        style="margin-bottom: 16px;"
      />

      <!-- 角色选择 -->
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="6">
          <a-card title="角色列表" :bordered="false">
            <a-list :data-source="roles" :split="false">
              <template #renderItem="{ item }">
                <a-list-item 
                  :class="{ 'role-item-active': selectedRole === item.value }"
                  class="role-item"
                  @click="selectedRole = item.value"
                >
                  {{ item.label }}
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>

        <!-- 权限配置 -->
        <a-col :span="18">
          <a-card title="权限配置" :loading="permLoading">
            <a-tree
              v-model:checkedKeys="checkedPermissions"
              :tree-data="permissionTree"
              checkable
              :default-expand-all="true"
              @check="onPermissionChange"
            />
            <a-space style="margin-top: 16px;">
              <a-button type="primary" @click="savePermissions" :loading="savingPermissions">
                保存权限
              </a-button>
              <a-button @click="resetPermissions">重置</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

const selectedRole = ref('student');
const permLoading = ref(false);
const savingPermissions = ref(false);
const checkedPermissions = ref([]);

const roles = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
  { label: '管理员', value: 'admin' },
];

// 权限树结构
const permissionTree = [
  {
    title: '用户管理',
    key: 'user_manage',
    children: [
      { title: '查看用户', key: 'user_view' },
      { title: '编辑用户', key: 'user_edit' },
      { title: '删除用户', key: 'user_delete' },
      { title: '禁用用户', key: 'user_disable' },
    ],
  },
  {
    title: '体测管理',
    key: 'test_manage',
    children: [
      { title: '查看体测数据', key: 'test_view' },
      { title: '编辑体测数据', key: 'test_edit' },
      { title: '导出体测数据', key: 'test_export' },
    ],
  },
  {
    title: '训练计划',
    key: 'plan_manage',
    children: [
      { title: '查看计划', key: 'plan_view' },
      { title: '创建计划', key: 'plan_create' },
      { title: '编辑计划', key: 'plan_edit' },
      { title: '删除计划', key: 'plan_delete' },
    ],
  },
  {
    title: '数据分析',
    key: 'analytics',
    children: [
      { title: '查看仪表板', key: 'dashboard_view' },
      { title: '查看报表', key: 'report_view' },
      { title: '导出报表', key: 'report_export' },
    ],
  },
  {
    title: '系统管理',
    key: 'system_manage',
    children: [
      { title: '查看日志', key: 'log_view' },
      { title: '系统配置', key: 'system_config' },
      { title: '权限管理', key: 'permission_manage' },
    ],
  },
];

// 预设权限配置
const defaultPermissions = {
  student: ['user_view', 'test_view', 'plan_view', 'dashboard_view'],
  teacher: ['user_view', 'test_view', 'test_edit', 'plan_view', 'plan_create', 'plan_edit', 'dashboard_view', 'report_view'],
  admin: ['user_manage', 'user_view', 'user_edit', 'user_delete', 'user_disable', 'test_manage', 'test_view', 'test_edit', 'test_export', 'plan_manage', 'plan_view', 'plan_create', 'plan_edit', 'plan_delete', 'analytics', 'dashboard_view', 'report_view', 'report_export', 'system_manage', 'log_view', 'system_config', 'permission_manage'],
};

const loadPermissions = () => {
  permLoading.value = true;
  setTimeout(() => {
    checkedPermissions.value = defaultPermissions[selectedRole.value] || [];
    permLoading.value = false;
  }, 300);
};

const onPermissionChange = () => {
  // 权限变化时的处理
};

const savePermissions = () => {
  savingPermissions.value = true;
  setTimeout(() => {
    message.success(`${roles.find(r => r.value === selectedRole.value)?.label}权限已保存`);
    savingPermissions.value = false;
  }, 500);
};

const resetPermissions = () => {
  checkedPermissions.value = defaultPermissions[selectedRole.value] || [];
};

// 初始化
loadPermissions();

// 监听角色变化
watch(() => selectedRole.value, () => {
  loadPermissions();
});
</script>

<script>
import { watch } from 'vue';
</script>

<style scoped>
.permission-management {
  width: 100%;
}

.role-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.role-item:hover {
  background-color: #f5f5f5;
}

.role-item-active {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
  padding-left: 9px;
}
</style>
