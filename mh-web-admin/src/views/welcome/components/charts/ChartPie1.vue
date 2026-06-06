<script setup lang="ts">
import { ref, computed, nextTick, watch } from "vue";
import { useDark, useECharts } from "@pureadmin/utils";

const props = defineProps({
  chartData: {
    type: Array as PropType<Array<Record<string, any>>>,
    default: () => []
  },
  // 新增：自定义饼图名称
  seriesName: {
    type: String,
    default: "各菌种关联科普内容数"
  }
});

const { isDark } = useDark();

const theme = computed(() => (isDark.value ? "dark" : "light"));
const textColor = computed(() => (isDark.value ? "#e5e7eb" : "#606266"));

// 定义更美观的配色方案（移除紫色系）
const colorPalette = [
  '#5470C6', '#91CC75', '#FAC858', '#EE6666', 
  '#73C0DE', '#3BA272', '#FC8452', '#FF9F7F', 
  '#00CCFF', '#FFD700', '#8A8A8A', '#FF7F50'
];

const chartRef = ref();
const { setOptions } = useECharts(chartRef, {
  theme
});

watch(
  () => props.chartData,
  async newVal => {
    await nextTick();
    setOptions({
      color: colorPalette,
      tooltip: {
        trigger: "item",
        formatter: "{b}: {c} 条 ({d}%)",
        backgroundColor: isDark.value ? 'rgba(30, 30, 30, 0.8)' : 'rgba(255, 255, 255, 0.9)',
        borderColor: isDark.value ? '#444' : '#eee',
        textStyle: {
          color: isDark.value ? '#e5e7eb' : '#333',
          fontSize: 12
        },
        extraCssText: 'border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);'
      },
      legend: {
        top: "5%",
        left: "center",
        textStyle: {
          color: textColor.value,
          fontSize: "0.875rem"
        },
        // 分页显示图例（菌种过多时）
        type: "scroll",
        pageIconColor: textColor.value,
        pageTextStyle: { color: textColor.value }
      },
      series: [
        {
          name: props.seriesName,
          type: "pie",
          radius: ["40%", "70%"], // 调整内半径使图表更饱满
          avoidLabelOverlap: false,
          padAngle: 2,
          itemStyle: {
            borderRadius: 8,
            borderColor: isDark.value ? "#1f2937" : "#ffffff",
            borderWidth: 1,
            // 添加阴影效果
            shadowBlur: 20,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: {
            show: true,
            position: "outside",
            formatter: '{b}\n{d}%',
            color: textColor.value,
            fontSize: 12,
            fontWeight: 'normal',
            lineHeight: 16
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: "bold",
              color: isDark.value ? '#fff' : '#333'
            },
            // 添加扇形高亮效果
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 10,
            smooth: true,
            lineStyle: {
              width: 1,
              color: isDark.value ? 'rgba(229, 231, 235, 0.7)' : 'rgba(96, 98, 102, 0.7)'
            }
          },
          data: newVal
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
  <!-- 调整高度适配页面布局 -->
  <div ref="chartRef" style="width: 100%; height: 587px" />
</template>