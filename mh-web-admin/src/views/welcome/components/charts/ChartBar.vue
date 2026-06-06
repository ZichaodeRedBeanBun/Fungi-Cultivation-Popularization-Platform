<script setup lang="ts">
import { useDark, useECharts } from "@pureadmin/utils";
import { type PropType, ref, computed, watch, nextTick } from "vue";

const props = defineProps({
  barChartData: {
    type: Array as PropType<Array<number>>,
    default: () => []
  },
  // 新增：自定义X轴标签（适配用户身份分布）
  xAxisLabels: {
    type: Array as PropType<Array<string>>,
    default: () => ["普通用户", "待认证科普号", "已认证科普号"]
  },
  // 新增：自定义系列名称
  seriesName: {
    type: String,
    default: "用户数量"
  }
});

const { isDark } = useDark();

const theme = computed(() => (isDark.value ? "dark" : "light"));
// 适配深色模式的文字颜色
const textColor = computed(() => (isDark.value ? "#e5e7eb" : "#606266"));

const chartRef = ref();
const { setOptions } = useECharts(chartRef, {
  theme
});

watch(
  () => props,
  async () => {
    await nextTick(); // 确保DOM更新完成后再执行
    setOptions({
      container: ".bar-card",
      color: ["#41b6ff"],
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "shadow" // 鼠标悬浮显示阴影，更直观
        }
      },
      grid: {
        top: "20px",
        left: "30px",
        right: "20px",
        bottom: "30px",
        containLabel: true
      },
      legend: {
        data: [props.seriesName],
        textStyle: {
          color: textColor.value,
          fontSize: "0.875rem"
        },
        bottom: 0
      },
      xAxis: [
        {
          type: "category",
          data: props.xAxisLabels,
          axisLabel: {
            fontSize: "0.875rem",
            color: textColor.value
          },
          axisLine: {
            lineStyle: {
              color: textColor.value // X轴线颜色适配深色模式
            }
          },
          axisPointer: {
            type: "shadow"
          }
        }
      ],
      yAxis: [
        {
          type: "value",
          axisLabel: {
            fontSize: "0.875rem",
            color: textColor.value
          },
          axisLine: {
            lineStyle: {
              color: textColor.value // Y轴线颜色适配深色模式
            }
          },
          splitLine: {
            show: false // 去网格线
          }
        }
      ],
      series: [
        {
          name: props.seriesName,
          type: "bar",
          barWidth: 39,
          itemStyle: {
            color: "#41b6ff",
            borderRadius: [10, 10, 0, 0]
          },
          data: props.barChartData
        }
      ]
    });
  },
  {
    deep: true,
    immediate: true
  }
);
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 253px" />
</template>