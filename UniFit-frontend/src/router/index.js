import { createRouter, createWebHistory } from 'vue-router';
import { getLoginUser } from '../api';

import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import LayoutView from '../views/LayoutView.vue';
import DashboardView from '../views/admin/DashboardView.vue';
import StudentsAuditView from '../views/admin/StudentsAuditView.vue';
import ExercisesView from '../views/admin/ExercisesView.vue';
import TemplatesView from '../views/admin/TemplatesView.vue';
import StandardsView from '../views/admin/StandardsView.vue';
import UsersView from '../views/admin/UsersView.vue';
import UserManagementView from '../views/admin/UserManagementView.vue';
import ClassManagementView from '../views/admin/ClassManagementView.vue';
import AuditCenterView from '../views/admin/AuditCenterView.vue';
import DataAnalysisView from '../views/admin/DataAnalysisView.vue';
import RankingView from '../views/admin/RankingView.vue';

const routes = [
  { path: '/login', component: LoginView },
  { path: '/register', component: RegisterView },
  {
    path: '/',
    redirect: '/admin/dashboard',
  },
  {
    path: '/admin',
    component: LayoutView,
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', component: DashboardView, meta: { requiresAuth: true } },
      { path: 'user-management', component: UserManagementView, meta: { requiresAuth: true } },
      { path: 'class-management', component: ClassManagementView, meta: { requiresAuth: true } },
      { path: 'audit-center', component: AuditCenterView, meta: { requiresAuth: true } },
      { path: 'data-analysis', component: DataAnalysisView, meta: { requiresAuth: true } },
      { path: 'students', component: StudentsAuditView, meta: { requiresAuth: true } },
      { path: 'exercises', component: ExercisesView, meta: { requiresAuth: true } },
      { path: 'templates', component: TemplatesView, meta: { requiresAuth: true } },
      { path: 'standards', component: StandardsView, meta: { requiresAuth: true } },
      { path: 'users', component: UsersView, meta: { requiresAuth: true } },
      { path: 'ranking', component: RankingView, meta: { requiresAuth: true } },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const publicPaths = new Set(['/login', '/register']);
  if (publicPaths.has(to.path)) {
    next();
    return;
  }

  try {
    const res = await getLoginUser();
    const user = res.data;
    localStorage.setItem('user', JSON.stringify(user));
    if (user.userRole !== 'admin' && user.userRole !== 'teacher') {
      next('/login');
      return;
    }
    next();
  } catch (e) {
    localStorage.removeItem('user');
    next('/login');
  }
});

export default router;
