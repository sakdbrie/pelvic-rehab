import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

/**
 * 路由表：静态路由（架构文档：登录、404 静态；权限路由后续接 sys_permission 动态下发）
 * meta.proto = 原型图页面编号（与 ui/prototype/web-interactive-prototype/index.html 对应）
 */
export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '后台登录页', proto: '00' },
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/user',
    children: [
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserListView.vue'),
        meta: { title: '用户管理', proto: '01-1 / 01-2' },
      },
      {
        path: 'user/patient/:id',
        name: 'PatientDetail',
        component: () => import('@/views/user/PatientDetailView.vue'),
        meta: { title: '用户信息详情（患者）', proto: '02' },
      },
      {
        path: 'user/doctor/:id',
        name: 'DoctorDetail',
        component: () => import('@/views/user/DoctorDetailView.vue'),
        meta: { title: '用户信息详情（医生端）', proto: '03' },
      },
      {
        path: 'dashboard',
        name: 'DataOverview',
        component: () => import('@/views/dashboard/DataOverviewView.vue'),
        meta: { title: '数据总览', proto: '04' },
      },
      {
        path: 'dashboard/patient/:id',
        name: 'PatientDataDetail',
        component: () => import('@/views/dashboard/PatientDataDetailView.vue'),
        meta: { title: '患者数据详情', proto: '05' },
      },
      {
        path: 'reports',
        name: 'ReportCenter',
        component: () => import('@/views/report/ReportCenterView.vue'),
        meta: { title: '报表总览', proto: '06' },
      },
      {
        path: 'reports/female',
        name: 'FemaleBaseInfo',
        component: () => import('@/views/report/FemaleBaseInfoView.vue'),
        meta: { title: '女性基础信息表', proto: '07-1' },
      },
      {
        path: 'reports/male',
        name: 'MaleBaseInfo',
        component: () => import('@/views/report/MaleBaseInfoView.vue'),
        meta: { title: '男性基础信息表', proto: '07-2' },
      },
      {
        path: 'reports/urination',
        name: 'UrinationDetail',
        component: () => import('@/views/report/UrinationDetailView.vue'),
        meta: { title: '排尿日记明细表', proto: '07-3' },
      },
      {
        path: 'reports/defecation',
        name: 'DefecationDetail',
        component: () => import('@/views/report/DefecationDetailView.vue'),
        meta: { title: '排便日记明细表', proto: '07-4' },
      },
      {
        path: 'reports/assessment',
        name: 'SelfAssessment',
        component: () => import('@/views/report/SelfAssessmentView.vue'),
        meta: { title: '盆底自评数据表', proto: '07-5' },
      },
      {
        path: 'reports/training',
        name: 'TrainingCheckin',
        component: () => import('@/views/report/TrainingCheckinView.vue'),
        meta: { title: '康复训练打卡数据表', proto: '07-6' },
      },
      {
        path: 'reports/summary',
        name: 'PatientSummary',
        component: () => import('@/views/report/PatientSummaryView.vue'),
        meta: { title: '患者数据汇总表', proto: '07-7' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人主页', proto: '08' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  // TODO: 联调后端时接入登录态校验（token 缺失且非登录页 → 跳 /login）
  document.title = `${String(to.meta.title || '')} - 盆底康复管理 · 网页后台`
  return true
})

export default router
