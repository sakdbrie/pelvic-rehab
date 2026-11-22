<template>
  <!-- 原型图：06 报表总览页面（网页后台）——展示所有报表卡片，点击进入对应报表 -->
  <div class="page-main">
    <div class="grid">
      <div v-for="r in reports" :key="r.path" class="prototype-card report-card" @click="router.push(r.path)">
        <el-icon :size="26" color="#1E9E8F"><component :is="r.icon" /></el-icon>
        <div class="name">{{ r.name }}</div>
        <div class="tip">{{ r.desc }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { User, UserFilled, ArrowDown, ToiletPaper, Document, VideoPlay, DataLine } from '@element-plus/icons-vue'

// 原型图：06 报表总览页面（网页后台）——7 张报表卡片（对应 07-1~07-7）
const router = useRouter()

const reports = [
  { path: '/reports/female', name: '女性基础信息表', desc: '基本信息、月经史、产科史、分娩方式、孕期情况等', icon: User },
  { path: '/reports/male', name: '男性基础信息表', desc: '基本信息、肛肠史、男性性功能障碍等', icon: UserFilled },
  { path: '/reports/urination', name: '排尿日记明细表', desc: '饮水量、排尿量、漏尿量、导尿量、当日排尿总次数', icon: ArrowDown },
  { path: '/reports/defecation', name: '排便日记明细表', desc: '大便类型、排便速度、排便量、其他', icon: ToiletPaper },
  { path: '/reports/assessment', name: '盆底自评表数据表', desc: '量表名称、填写时间、总得分、各维度得分、评估结果', icon: Document },
  { path: '/reports/training', name: '康复训练打卡数据表', desc: '训练项目名称、训练类型、打卡日期、训练时长', icon: VideoPlay },
  { path: '/reports/summary', name: '患者数据汇总表', desc: '自评表得分、排尿/排便日记、训练打卡、异常预警次数', icon: DataLine },
]
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.report-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.report-card:hover {
  border-color: #1e9e8f;
}
.name {
  font-weight: 600;
  font-size: 14px;
}
.tip {
  font-size: 11px;
  color: #9ca3af;
  line-height: 1.6;
}
</style>
