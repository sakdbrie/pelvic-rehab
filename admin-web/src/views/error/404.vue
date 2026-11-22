<template>
  <div class="notfound">
    <div class="code">404</div>
    <div class="msg">页面不存在</div>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.notfound {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
}
.code {
  font-size: 48px;
  font-weight: 700;
  color: #1e9e8f;
}
.msg {
  color: #6b7280;
}
</style>
