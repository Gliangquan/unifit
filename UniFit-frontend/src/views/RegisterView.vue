<template>
  <div class="auth-page">
    <div class="orb orb-left"></div>
    <div class="orb orb-right"></div>
    <a-card class="auth-card" :bordered="false">
      <div class="form-head">
        <h2>注册账号</h2>
        <a-button type="link" @click="goLogin">返回登录</a-button>
      </div>
      <a-form layout="vertical" :model="form" @finish="onSubmit">
        <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="form.userAccount" placeholder="请输入账号" size="large" allow-clear />
        </a-form-item>
        <a-form-item label="姓名" name="userName" :rules="[{ required: true, message: '请输入姓名' }]">
          <a-input v-model:value="form.userName" placeholder="请输入姓名" size="large" allow-clear />
        </a-form-item>
        <a-form-item label="手机号" name="userPhone">
          <a-input v-model:value="form.userPhone" placeholder="请输入手机号（可选）" size="large" allow-clear />
        </a-form-item>
        <a-form-item label="密码" name="userPassword" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.userPassword" placeholder="请输入密码" size="large" />
        </a-form-item>
        <a-form-item label="确认密码" name="checkPassword" :rules="[{ required: true, message: '请再次输入密码' }]">
          <a-input-password v-model:value="form.checkPassword" placeholder="请再次输入密码" size="large" />
        </a-form-item>
        <a-button type="primary" html-type="submit" block size="large" :loading="loading">注册</a-button>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { userRegister } from '../api';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  userAccount: '',
  userName: '',
  userPhone: '',
  userPassword: '',
  checkPassword: '',
});

const goLogin = () => {
  router.push('/login');
};

const onSubmit = async () => {
  if (form.userPassword !== form.checkPassword) {
    message.error('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  try {
    await userRegister({
      userAccount: form.userAccount.trim(),
      userName: form.userName.trim(),
      userPhone: form.userPhone.trim() || undefined,
      userPassword: form.userPassword,
      checkPassword: form.checkPassword,
    });
    message.success('注册成功，请登录');
    router.replace({
      path: '/login',
      query: { account: form.userAccount.trim() },
    });
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
:global(body) {
  margin: 0;
}

.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(140deg, #eef7ff 0%, #f8f5ec 48%, #eef6f1 100%);
  overflow: hidden;
}

.orb {
  position: absolute;
  width: 40vw;
  height: 40vw;
  border-radius: 50%;
  filter: blur(44px);
}

.orb-left {
  top: -14vw;
  left: -10vw;
  background: rgba(16, 185, 129, 0.18);
}

.orb-right {
  right: -10vw;
  bottom: -14vw;
  background: rgba(14, 165, 233, 0.16);
}

.auth-card {
  position: relative;
  z-index: 2;
  width: min(460px, 100%);
  border-radius: 16px;
  box-shadow: 0 18px 50px rgba(22, 28, 45, 0.14);
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

h2 {
  margin: 0;
  color: #18293f;
}
</style>
