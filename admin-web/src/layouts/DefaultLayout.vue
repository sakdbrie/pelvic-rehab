<template>
  <!-- 布局：原型各页共用左侧导航（.side）+ 主内容区（.main/topbar） -->
  <el-container class="layout">
    <el-aside width="200px" class="side">
      <div class="logo">康复管理后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        class="side-menu"
        background-color="#111827"
        text-color="#D1D5DB"
        active-text-color="#1E9E8F"
      >
        <el-menu-item index="/user">用户管理</el-menu-item>
        <el-menu-item index="/dashboard">数据总览</el-menu-item>
        <el-menu-item index="/reports">报表总览</el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar" height="48px">
        <span>{{ route.meta.title }}</span>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

/** 侧边栏高亮：按路由前缀归一到四个一级菜单 */
const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/user')) return '/user'
  if (p.startsWith('/dashboard')) return '/dashboard'
  if (p.startsWith('/reports')) return '/reports'
  if (p.startsWith('/profile')) return '/profile'
  return p
})
</script>

<style scoped>
.layout {
  height: 100%;
}
.side {
  background: #111827;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.side-menu {
  border-right: none;
  flex: 1;
}
.side-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
}
.side-menu .el-menu-item.is-active {
  background: rgba(30, 158, 143, 0.12);
}
.topbar {
  background: #fff;
  border-bottom: 1px solid #edeff2;
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}
.main {
  background: #e9ebee;
  padding: 12px;
}
</style>
