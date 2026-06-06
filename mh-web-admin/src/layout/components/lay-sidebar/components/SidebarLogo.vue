<script setup lang="ts">
import { computed } from "vue";
import { getTopMenu } from "@/router/utils";
import { useNav } from "@/layout/hooks/useNav";

const props = defineProps({
  collapse: Boolean,
  customTitle: String
});

const { title, getLogo } = useNav();

// 优先使用自定义 title，否则使用全局配置的 title
const displayTitle = computed(() => {
  return props.customTitle ?? title.value;
});
</script>

<template>
  <div class="sidebar-logo-container" :class="{ collapses: collapse }">
    <transition name="sidebarLogoFade">
      <router-link
        v-if="collapse"
        key="collapse"
        :title="displayTitle"
        class="sidebar-logo-link"
        :to="getTopMenu()?.path ?? '/'"
      >
        <img :src="getLogo()" alt="logo" />
        <span class="sidebar-title">{{ displayTitle }}</span>
      </router-link>
      <router-link
        v-else
        key="expand"
        :title="displayTitle"
        class="sidebar-logo-link"
        :to="getTopMenu()?.path ?? '/'"
      >
        <img :src="getLogo()" alt="logo" />
        <span class="sidebar-title">{{ displayTitle }}</span>
      </router-link>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 48px;
  overflow: hidden;

  .sidebar-logo-link {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    height: 100%;
    padding-left: 10px;

    img {
      display: inline-block;
      height: 32px;
    }

    .sidebar-title {
      display: -webkit-box;
      height: auto;
      max-height: 40px;
      margin: 2px 0 0 12px;
      overflow: hidden;
      font-size: 18px;
      font-weight: 600;
      line-height: 20px;
      color: var(--pure-theme-sub-menu-active-text);
      text-overflow: ellipsis;
      white-space: normal;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}
</style>
