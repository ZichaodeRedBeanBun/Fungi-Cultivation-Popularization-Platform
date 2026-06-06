// src/store/modules/epTheme.ts
import { defineStore } from "pinia";
import { ref } from "vue";

// 定义主题相关的 Pinia Store
const useEpThemeStore = defineStore(
  "epTheme", // Store 唯一标识
  () => {
    // 示例：主题模式（亮色/暗色）
    const themeMode = ref<string>("light");
    // 示例：主题颜色
    const primaryColor = ref<string>("#409eff");

    // 修改主题模式
    const setThemeMode = (mode: string) => {
      themeMode.value = mode;
      // 可选：同步到本地存储，刷新不丢失
      localStorage.setItem("epThemeMode", mode);
    };

    // 修改主题色
    const setPrimaryColor = (color: string) => {
      primaryColor.value = color;
      localStorage.setItem("epPrimaryColor", color);
    };

    // 初始化主题（从本地存储读取）
    const initTheme = () => {
      const mode = localStorage.getItem("epThemeMode");
      const color = localStorage.getItem("epPrimaryColor");
      if (mode) themeMode.value = mode;
      if (color) primaryColor.value = color;
    };

    return {
      themeMode,
      primaryColor,
      setThemeMode,
      setPrimaryColor,
      initTheme
    };
  }
);

// 封装 Hook（和你导入的 useEpThemeStoreHook 命名一致）
export function useEpThemeStoreHook() {
  return useEpThemeStore();
}