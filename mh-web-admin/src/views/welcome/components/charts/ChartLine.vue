<script setup lang="ts">
import { type PropType, ref, computed, watch, nextTick } from "vue";
import { useDark, useECharts } from "@pureadmin/utils";

const props = defineProps({
  data: {
    type: Array as PropType<Array<number>>,
    default: () => []
  },
  color: {
    type: String,
    default: "#41b6ff"
  }
});

const { isDark } = useDark();

const theme = computed(() => (isDark.value ? "dark" : "light"));

const chartRef = ref();
const { setOptions } = useECharts(chartRef, {
  theme,
  renderer: "svg"
});

// 改用watch监听数据变化，确保动态更新
watch(
  () => props.data,
  async () => {
    await nextTick();
    setOptions({
      container: ".line-card",
      xAxis: {
        type: "category",
        show: false,
        data: props.data.map((_, index) => index) // 适配任意长度数据
      },
      grid: {
        top: "15px",
        bottom: 0,
        left: 0,
        right: 0
      },
      yAxis: {
        show: false,
        type: "value"
      },
      series: [
        {
          data: props.data,
          type: "line",
          symbol: "none",
          smooth: true,
          color: props.color,
          lineStyle: {
            width: 2, // 加粗线条，更清晰
            shadowOffsetY: 3,
            shadowBlur: 7,
            shadowColor: props.color
          },
          areaStyle: { // 新增渐变填充，提升视觉效果
            color: {
              type: "linear",
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: `${props.color}33` // 半透明起始色
              }, {
                offset: 1, color: `${props.color}00` // 透明结束色
              }]
            }
          }
        }
      ]
    });
  },
  { deep: true, immediate: true }
);
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 60px" />
</template>