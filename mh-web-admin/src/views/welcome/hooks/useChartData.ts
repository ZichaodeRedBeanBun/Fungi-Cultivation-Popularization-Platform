import { getAllPopSciContentsCount, getAllUsersCount, getAllMushroomsCount, getAllDiseasePestsCount } from "@/api/data";
import { getMushroomList } from "@/api/system";
import { onMounted, ref } from "vue";
import { message } from "@/utils/message";

// 定义颜色数组（供饼图使用）
export const chartColorList = [
  "#faf6ff", // 极浅紫（基底，适配主题）
  "#dbc0e3", // 淡蓝紫（紫+浅蓝，协调不突兀）
  "#f5edff", // 超浅紫
  "#f0e8ff", // 柔粉紫（低饱和，无艳粉感）
  "#eee2ff", // 浅堇紫
  "#e5d4ff", // 淡堇紫
  "#d9bfff", // 柔紫堇
  "#c8a6ff", // 浅紫罗兰
  "#b78bff", // 柔和紫罗兰
  "#a56eff"  // 中度紫（最深仅到这里，保留区分度）
];

// 用户身份类型常量（对齐数据库：1=普通用户，2=待认证科普号，3=已认证科普号）
const USER_TYPE = {
  NORMAL: 1,        // 普通用户
  PENDING_AUTH: 2,  // 待认证科普号
  AUTHED: 3         // 已认证科普号
};

// 科普内容类型
const CONTENT_TYPE = {
  ARTICLE: "article", // 图文
  VIDEO: "video"      // 视频
};

export default () => {
  /** 原有兼容变量（保留模板适配） */
  const userCount = ref<number>(0);
  const artistCount = ref<number>(0);
  const songCount = ref<number>(0);
  const playlistCount = ref<number>(0);
  const westernPopCount = ref<number>(0);
  const chinesePopCount = ref<number>(0);
  const cantonesePopCount = ref<number>(0);
  const koreanPopCount = ref<number>(0);
  const classicCount = ref<number>(0);
  const hiphopCount = ref<number>(0);
  const rockCount = ref<number>(0);
  const electronicCount = ref<number>(0);
  const jazzCount = ref<number>(0);
  const lightCount = ref<number>(0);
  const countAmerica = ref<number>(0);
  const countChina = ref<number>(0);
  const countKorea = ref<number>(0);
  const countJapan = ref<number>(0);
  const countGermany = ref<number>(0);
  const countBritain = ref<number>(0);
  const maleCount = ref<number>(0);
  const femaleCount = ref<number>(0);

  /** 核心图表数据 */
  const pieChartData1 = ref<any[]>([]);         // 各菌种科普内容数（饼图）
  const userIdentityBarData = ref<number[]>([0, 0, 0]); // 用户身份分布：[普通用户, 待认证, 已认证]
  const contentTypePieData = ref<any[]>([       // 科普内容类型比例
    { value: 0, name: "图文", itemStyle: { color: "#c379e8" } },
    { value: 0, name: "视频", itemStyle: { color: "#f4e773" } }
  ]);

  /** 重置所有数据（异常兜底） */
  const resetCounts = () => {
    // 重置原有兼容变量
    userCount.value = 0;
    artistCount.value = 0;
    songCount.value = 0;
    playlistCount.value = 0;
    westernPopCount.value = 0;
    chinesePopCount.value = 0;
    cantonesePopCount.value = 0;
    koreanPopCount.value = 0;
    classicCount.value = 0;
    hiphopCount.value = 0;
    rockCount.value = 0;
    electronicCount.value = 0;
    jazzCount.value = 0;
    lightCount.value = 0;
    countAmerica.value = 0;
    countChina.value = 0;
    countKorea.value = 0;
    countJapan.value = 0;
    countGermany.value = 0;
    countBritain.value = 0;
    maleCount.value = 0;
    femaleCount.value = 0;

    // 重置核心图表数据
    // 修正：直接重置外层响应式变量，而非重新声明
    pieChartData1.value = [];
    userIdentityBarData.value = [0, 0, 0];
    contentTypePieData.value = [
      { value: 0, name: "图文", itemStyle: { color: "#c379e8" } },
      { value: 0, name: "视频", itemStyle: { color: "#f4e773" } }
    ];
  };

  const fetchData = async () => {
    try {
      // ========== 第一步：并行请求基础数据（用户总数 + 用户身份分布 + 科普内容类型） ==========
      // 1. 构建基础请求Promise数组（参考示例的map + Promise.all风格）
      const basePromises = [
        getAllUsersCount(),                          // 总用户数
        getAllUsersCount(USER_TYPE.NORMAL),          // 普通用户数
        getAllUsersCount(USER_TYPE.PENDING_AUTH),    // 待认证科普号数
        getAllUsersCount(USER_TYPE.AUTHED),          // 已认证科普号数
        getAllPopSciContentsCount(),                 // 科普内容总数（新增：不传参数查总数）
        getAllMushroomsCount(),                      // 菌种总数（新增）
        getAllDiseasePestsCount(),                   // 病虫害总数（新增）
        getAllPopSciContentsCount(undefined, CONTENT_TYPE.ARTICLE), // 图文数量（null→undefined）
        getAllPopSciContentsCount(undefined, CONTENT_TYPE.VIDEO)     // 视频数量（null→undefined）
      ];

      // 2. 并行执行基础请求（参考示例的Promise.all）
      const baseResponses = await Promise.all(basePromises);

      // 3. 解析基础响应数据
      const [
        userTotalRes,        // 总用户数响应
        normalUserRes,       // 普通用户响应
        pendingAuthRes,      // 待认证科普号响应
        authedRes,           // 已认证科普号响应
        contentTotalRes,     // 科普内容总数响应
        mushroomTotalRes,    // 菌种总数响应
        diseaseTotalRes,     // 病虫害总数响应
        articleCountRes,     // 图文数量响应
        videoCountRes        // 视频数量响应
      ] = baseResponses;

      // ========== 第二步：处理基础数据 ==========
      let allSuccess = true;

      // 检查总用户数
      if (userTotalRes.code === 0 && !isNaN(Number(userTotalRes.data))) {
        userCount.value = Number(userTotalRes.data);
      } else {
        allSuccess = false;
        console.error("获取总用户数失败:", userTotalRes);
      }
      // 2. 菌种卡片总数
      if (mushroomTotalRes.code === 0 && !isNaN(Number(mushroomTotalRes.data))) {
        artistCount.value = Number(mushroomTotalRes.data);
      } else {
        allSuccess = false;
        console.error("获取菌种总数失败:", mushroomTotalRes);
      }

      // 3. 科普内容总数
      if (contentTotalRes.code === 0 && !isNaN(Number(contentTotalRes.data))) {
        songCount.value = Number(contentTotalRes.data);
      } else {
        allSuccess = false;
        console.error("获取科普内容总数失败:", contentTotalRes);
      }

      // 4. 病虫害总数
      if (diseaseTotalRes.code === 0 && !isNaN(Number(diseaseTotalRes.data))) {
        playlistCount.value = Number(diseaseTotalRes.data);
      } else {
        allSuccess = false;
        console.error("获取病虫害总数失败:", diseaseTotalRes);
      }

      // 处理用户身份分布数据
      const normalCount = normalUserRes.code === 0 ? Number(normalUserRes.data) || 0 : 0;
      const pendingCount = pendingAuthRes.code === 0 ? Number(pendingAuthRes.data) || 0 : 0;
      const authedCount = authedRes.code === 0 ? Number(authedRes.data) || 0 : 0;
      userIdentityBarData.value = [normalCount, pendingCount, authedCount];

      // 处理科普内容类型数据
      const articleCount = articleCountRes.code === 0 ? Number(articleCountRes.data) || 0 : 0;
      const videoCount = videoCountRes.code === 0 ? Number(videoCountRes.data) || 0 : 0;
      contentTypePieData.value = [
        { value: articleCount, name: "图文", itemStyle: { color: "#41b6ff" } },
        { value: videoCount, name: "视频", itemStyle: { color: "#e85f33" } }
      ];

      // ========== 第三步：获取菌种列表 + 并行请求各菌种科普数 ==========
      // 1. 请求菌种列表
      const mushroomParams = { pageNum: 1, pageSize: 1000, mushroomName: null };
      const mushroomRes = await getMushroomList(mushroomParams);

      if (mushroomRes.code === 0 && mushroomRes.data?.items) {
        const mushroomList = mushroomRes.data.items;

        if (mushroomList.length > 0) {
          // 2. 构建菌种科普数请求Promise数组（参考示例的map风格）
          const mushroomPromises = mushroomList.map(mushroom =>
            getAllPopSciContentsCount(mushroom.id)
          );

          // 3. 并行执行菌种科普数请求（参考示例的Promise.all）
          const mushroomResponses = await Promise.all(mushroomPromises);

          // 4. 解析菌种科普数数据
          const mushroomContentList = [];
          mushroomResponses.forEach((res, index) => {
            const mushroom = mushroomList[index];
            const count = res?.code === 0 ? Number(res.data) || 0 : 0;
            mushroomContentList.push({
              value: count,
              name: mushroom.mushroomName || `未知菌种${index + 1}`, // 补充：防止菌种名为空
              itemStyle: { color: chartColorList[index % chartColorList.length] }
            });
          });
          pieChartData1.value = mushroomContentList;
        } else {
          pieChartData1.value = [{ value: 0, name: "暂无菌种数据" }];
        }
      } else {
        allSuccess = false;
        console.error("获取菌种列表失败:", mushroomRes);
        pieChartData1.value = [{ value: 0, name: "暂无菌种数据" }];
      }

      // ========== 第四步：校验所有请求是否成功 ==========
      if (!allSuccess) {
        resetCounts();
        console.warn("部分数据获取失败，已重置为默认值");
      }

    } catch (error) {
      console.error("获取统计数据失败:", error);
      message("数据加载失败，请稍后重试", { type: "error" });
      resetCounts();
    }
  };

  // 页面挂载时加载数据
  onMounted(fetchData);

  // 返回所有变量（兼容原有模板 + 新增图表数据）
  return {
    // 原有兼容变量
    userCount,
    artistCount,
    songCount,
    playlistCount,
    westernPopCount,
    chinesePopCount,
    cantonesePopCount,
    koreanPopCount,
    classicCount,
    hiphopCount,
    rockCount,
    electronicCount,
    jazzCount,
    lightCount,
    countAmerica,
    countChina,
    countKorea,
    countJapan,
    countGermany,
    countBritain,
    maleCount,
    femaleCount,
    // 核心图表数据
    pieChartData1,
    userIdentityBarData,
    contentTypePieData
  };
};