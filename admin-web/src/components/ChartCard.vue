<template>
  <!-- ECharts 统一封装（架构文档约定：init/resize/dispose 生命周期统一处理） -->
  <div class="chart-card">
    <div class="chart-head">
      <div>
        <div class="chart-title">{{ title }}</div>
        <div class="chart-sub">{{ sub }}</div>
      </div>
      <slot name="extra" />
    </div>
    <div ref="chartEl" class="chart-box" />
    <div class="chart-tip">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  title: string
  sub?: string
  tip?: string
  option: echarts.EChartsOption
}>()

const chartEl = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function render() {
  if (!chartEl.value) return
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption(props.option)
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  render()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})

watch(() => props.option, render, { deep: true })
</script>

<style scoped>
.chart-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}
.chart-sub {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}
.chart-box {
  flex: 1;
  min-height: 300px;
}
.chart-tip {
  font-size: 12px;
  color: #9ca3af;
}
</style>
