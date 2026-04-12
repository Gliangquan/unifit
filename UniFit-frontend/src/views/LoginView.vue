<template>
  <div class="auth-page">
    <div class="orb orb-left"></div>
    <div class="orb orb-right"></div>
    <div class="auth-shell">
      <section class="hero-panel">
        <p class="badge">UniFit Admin Portal</p>
        <h1>体测与训练数据统一管理</h1>
        <p class="intro">支持学生审核、测试标准配置、训练动作库和排行榜管理。</p>
      </section>
      <a-card class="auth-card" :bordered="false">
        <div class="form-head">
          <h2>账号登录</h2>
          <a-button type="link" @click="goRegister">立即注册</a-button>
        </div>
        <a-form layout="vertical" :model="form" @finish="onSubmit">
          <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="form.userAccount" placeholder="请输入账号" size="large" allow-clear />
          </a-form-item>
          <a-form-item label="密码" name="userPassword" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password v-model:value="form.userPassword" placeholder="请输入密码" size="large" />
          </a-form-item>
          <a-button type="primary" html-type="submit" block size="large" :loading="loading">登录</a-button>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { useRoute, useRouter } from 'vue-router';
import { userLoginByAccount } from '../api';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const form = reactive({
  userAccount: '',
  userPassword: '',
});

onMounted(() => {
  if (typeof route.query.account === 'string') {
    form.userAccount = route.query.account;
  }
});

const goRegister = () => {
  router.push('/register');
};

const onSubmit = async () => {
  loading.value = true;
  try {
    const res = await userLoginByAccount(form.userAccount.trim(), form.userPassword);
    localStorage.setItem('user', JSON.stringify(res.data));
    message.success('登录成功');
    
    // 根据用户角色跳转
    const userRole = res.data?.userRole;
    if (userRole === 'admin' || userRole === 'teacher') {
      router.replace('/admin/dashboard');
    } else {
      // 其他角色跳转到首页或其他页面
      router.replace('/');
    }
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
  background: linear-gradient(140deg, #f6f0e6 0%, #f0f7ff 48%, #edf7ef 100%);
  overflow: hidden;
}

.orb {
  position: absolute;
  width: 44vw;
  height: 44vw;
  border-radius: 50%;
  filter: blur(44px);
}

.orb-left {
  top: -16vw;
  left: -10vw;
  background: rgba(34, 197, 94, 0.18);
}

.orb-right {
  right: -8vw;
  bottom: -16vw;
  background: rgba(59, 130, 246, 0.2);
}

.auth-shell {
  position: relative;
  z-index: 2;
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 28px;
}

.hero-panel {
  padding: 48px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.1);
}

.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e7f6ec;
  color: #125f37;
  font-size: 12px;
  letter-spacing: 0.04em;
}

h1 {
  margin: 14px 0 12px;
  color: #10223f;
  font-size: 34px;
  line-height: 1.25;
  font-family: 'Avenir Next', 'PingFang SC', sans-serif;
}

.intro {
  margin: 0;
  color: #4b5f78;
  font-size: 15px;
}

.auth-card {
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

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    padding: 28px;
  }

  .auth-card {
    width: 100%;
  }
}
</style>
