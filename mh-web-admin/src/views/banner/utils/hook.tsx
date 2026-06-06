import "./reset.css";
import { message } from "@/utils/message";
import { usePublicHooks } from "../hooks";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import { getKeyList, deviceDetection, formatBytes } from "@pureadmin/utils";
import { ElMessageBox, ElSwitch, ElImage, ElInput, ElFormItem, ElForm, ElButton } from "element-plus";
import { h, ref, computed, reactive, onMounted, type Ref } from "vue";
// 替换为科普轮播图专用接口
import {
  getPopSciBannerList as getBannerList,
  addPopSciBanner as addBanner,
  updatePopSciBanner as updateBanner,
  updatePopSciBannerStatus as updateBannerStatus,
  deletePopSciBanner as deleteBanner,
  deletePopSciBanners as deleteBanners,
  getPopSciContentList
} from "@/api/system";

export function useBanner(formData: any, tableRef: Ref) {
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const selectionList = ref([]);
  const searchContentForm = reactive({
    contentId: null as number | null,
    theme: null as string | null,
    contentStatus: 2 as number | null
  });
  const matchedContents = ref<any[]>([]); // 修改为数组
  const { switchStyle } = usePublicHooks();
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 5,
    currentPage: 1,
    background: true
  });
  // 添加一个标志位，用于标识是否是初始化加载
  const isInitialLoad = ref(true);

  const columns: TableColumnList = [
    {
      label: "勾选列",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "轮播图编号",
      prop: "bannerId",
      minWidth: 100
    },
    {
      label: "轮播图",
      prop: "bannerUrl",
      minWidth: 400,
      cellRenderer: ({ row }) => (
        <ElImage
          fit="cover"
          preview-teleported={true}
          src={row.bannerUrl}
          preview-src-list={[row.bannerUrl]}
          class="w-[500px] h-[195px] rounded-lg align-middle"
        />
      )
    },
    {
      label: "状态",
      prop: "bannerStatus",
      minWidth: 200,
      cellRenderer: scope => (
        <ElSwitch
          v-model={scope.row.bannerStatus}
          activeValue={1}
          inactiveValue={0}
          style={switchStyle.value}
          onChange={val => {
            // 如果是初始化加载阶段，不触发onChange事件
            if (isInitialLoad.value) return;
            onChange(scope.row, scope.$index, val);
          }}
        />
      )
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];

  function onChange(row, index, newValue) {
    ElMessageBox.confirm(
      `确认要<strong>${newValue === 1 ? "启用" : "禁用"
      }</strong><strong style='color:var(--el-color-primary)'> 编号:${row.bannerId
      } </strong>的轮播图吗?`,
      "系统提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(() => {
        // 设置 loading 状态
        switchLoadMap.value[index] = {
          ...switchLoadMap.value[index],
          loading: true
        };

        // 调用接口更新状态
        updateBannerStatus(row.bannerId, newValue)
          .then(res => {
            if (res.code === 0) {
              row.bannerStatus = newValue; // 更新前端状态
              message("已成功修改轮播图状态", { type: "success" });
              onSearch(); // 刷新表格数据
            } else {
              message("修改轮播图状态失败，" + res.message, { type: "error" });
              row.bannerStatus = newValue === 1 ? 0 : 1; // 还原状态：如果newValue是1(启用)，则还原为0(禁用)，反之亦然
            }
          })
          .catch(err => {
            console.error("修改轮播图状态失败", err);
            message("修改轮播图状态失败", { type: "error" });
            row.bannerStatus = newValue === 1 ? 0 : 1; // 还原状态
          })
          .finally(() => {
            // 关闭 loading 状态
            switchLoadMap.value[index] = {
              ...switchLoadMap.value[index],
              loading: false
            };
          });
      })
      .catch(() => {
        row.bannerStatus = newValue === 1 ? 0 : 1; // 还原状态
      });
  }

  function handleDelete(row) {
    deleteBanner(row.bannerId)
      .then(res => {
        if (res.code === 0) {
          message(`删除了 编号 为 ${row.bannerId} 的轮播图`, {
            type: "success"
          });
          onSearch();
        } else {
          message(`删除失败: ${res.message}`, { type: "error" });
        }
      })
      .catch(err => {
        message(`删除失败: ${err}`, { type: "error" });
      });
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    selectionList.value = val;
  }

  function onSelectionCancel() {
    selectionList.value = [];
    tableRef.value?.getTableRef()?.clearSelection();
  }

  function onBatchDelete() {
    if (selectionList.value.length === 0) {
      message("请先选择需要删除的数据", { type: "warning" });
      return;
    }

    ElMessageBox.confirm(
      `确认要删除选中的 <strong>${selectionList.value.length}</strong> 条轮播图吗?`,
      "系统提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(() => {
        const ids = getKeyList(selectionList.value, "bannerId");
        loading.value = true;
        deleteBanners(ids)
          .then(res => {
            if (res.code === 0) {
              message(`成功删除了 ${ids.length} 条数据`, { type: "success" });
              onSearch();
              onSelectionCancel();
            } else {
              message(`批量删除失败: ${res.message}`, { type: "error" });
            }
          })
          .catch(err => {
            message(`批量删除失败: ${err}`, { type: "error" });
          })
          .finally(() => {
            loading.value = false;
          });
      })
      .catch(() => {
        // 用户取消操作
      });
  }

  async function onSearch() {
    loading.value = true;
    const params = { 
      ...formData, 
      ...pagination,
      // Handle bannerStatus properly - only include if not null
      ...(formData.bannerStatus !== null && { bannerStatus: formData.bannerStatus })
    };
    params.pageNum = params.currentPage;
    delete params.currentPage;
    delete params.total;

    try {
      const result = await getBannerList(params);

      if (result.code === 0 && result.data && result.data.items) {
        // If return status code is 0 (success), and data and items exist
        pagination.total = result.data.total; // Set total number of entries
        // Popular science banner status values are directly 0/1, no conversion needed
        // Fix field mapping: map backend id to frontend bannerId, status to bannerStatus
        dataList.value = result.data.items.map(item => {
          // Create a new object containing original data
          const mappedItem = {
            ...item,
            // Map fields
            bannerId: item.id,  // Map id to bannerId
            bannerStatus: item.status  // Map status to bannerStatus
          };

          // Keep original fields (optional)
          // delete mappedItem.id;
          // delete mappedItem.status;

          return mappedItem;

        });
      } else {
        // If return status code is not 0 or data.items doesn't exist
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的轮播图信息", { type: "warning" });
      }
    } catch (error) {
      console.error("请求失败：", error);
      message("会话过期，请重新登录", { type: "error" });
    }

    setTimeout(() => {
      loading.value = false;
      // After data loading is complete, set initialization flag to false
      isInitialLoad.value = false;
    }, 500);
  }

  async function searchContent() {
    if (!searchContentForm.contentId && !searchContentForm.theme) {
      message("请至少输入内容ID或标题", { type: "warning" });
      return;
    }

    try {
      const params = {
        pageNum: 1,
        pageSize: 250,
        id: searchContentForm.contentId || undefined,
        theme: searchContentForm.theme || undefined,
        contentStuatus: searchContentForm.contentStatus || undefined
      };

      const res = await getPopSciContentList(params);
      if (res.code === 0 && res.data && res.data.items && res.data.items.length > 0) {
        // 保存所有匹配项
        matchedContents.value = res.data.items;
        message(`找到 ${res.data.items.length} 个匹配的科普内容`, { type: "success" });
      } else {
        matchedContents.value = [];
        message("未找到匹配的科普内容", { type: "warning" });
      }
    } catch (error) {
      console.error("搜索科普内容失败", error);
      message("搜索失败，请重试", { type: "error" });
    }
  }

  function handleUpload() {
    const title = "新增轮播图"; // 删除:isEdit ? "编辑轮播图" : "新增轮播图";

    // 重置搜索表单和匹配结果
    searchContentForm.contentId = null;
    searchContentForm.theme = null;
    matchedContents.value = []; // 重置为数组

    addDialog({
      title: title,
      width: "60%",
      draggable: true,
      fullscreen: deviceDetection(),
      closeOnClickModal: false,
      contentRenderer: () =>
        h(
          "div",
          {
            class: "flex flex-col"
          },
          [
            // 搜索区域
            h(
              ElForm,
              {
                model: searchContentForm,
                labelWidth: "80px",
                class: "mb-4"
              },
              [
                h(
                  ElFormItem,
                  {
                    label: "内容ID",
                    prop: "contentId"
                  },
                  h(ElInput, {
                    modelValue: searchContentForm.contentId,
                    "onUpdate:modelValue": (val: string) => {
                      searchContentForm.contentId = val === '' ? null : Number(val);
                    },
                    placeholder: "请输入科普内容ID",
                    type: "number",
                    clearable: true
                  })
                ),
                h(
                  ElFormItem,
                  {
                    label: "标题",
                    prop: "theme"
                  },
                  h(ElInput, {
                    modelValue: searchContentForm.theme,
                    "onUpdate:modelValue": (val: string | null) => {
                      searchContentForm.theme = val;
                    },
                    placeholder: "请输入科普内容标题",
                    clearable: true
                  })
                ),
                h(
                  "div",
                  {
                    class: "flex justify-end"
                  },
                  h(
                    ElButton,
                    {
                      type: "primary",
                      onClick: searchContent
                    },
                    "搜索科普内容"
                  )
                )
              ]
            ),
            // 匹配结果显示区域
            matchedContents.value.length > 0
              ? h(
                "div",
                {
                  class: "border p-4 rounded-lg bg-gray-50 max-h-[400px] overflow-y-auto"
                },
                [
                  h("h3", { class: "text-lg font-bold mb-2" }, `找到 ${matchedContents.value.length} 个匹配项`),
                  ...matchedContents.value.map((item, index) =>
                    h(
                      "div",
                      {
                        key: item.id,
                        class: "border-b py-2 last:border-0 cursor-pointer hover:bg-gray-100",
                        onClick: () => {
                          // 选择该项作为轮播图内容
                          matchedContents.value = [item]; // 只保留选中的项
                        }
                      },
                      [
                        h("p", {}, `ID: ${item.id}`),
                        h("p", {}, `标题: ${item.theme}`),
                        h("p", {}, `关联菌种: ${item.mushroomName || '无'}`),
                        h("p", {}, `内容类型: ${item.contentType === 'article' ? '图文' : '视频'}`),
                        (item.contentType === 'article' ? item.articleCoverUrl : item.videoCoverUrl)
                          ? h("p", { class: "text-green-500 mt-2" }, "该内容有封面图")
                          : h("p", { class: "text-red-500 mt-2" }, "该内容没有封面图")
                      ]
                    )
                  )
                ]
              )
              : h(
                "div",
                {
                  class: "text-center py-8 text-gray-500"
                },
                "请先搜索科普内容"
              )
          ]
        ),
      beforeSure: async done => {
        if (matchedContents.value.length === 0) {
          message("请先搜索并选择一个科普内容", { type: "warning" });
          return;
        }

        const selectedContent = matchedContents.value[0];
        // 修改: 正确检查 articleCoverUrl 或 videoCoverUrl
        const coverUrl = selectedContent.contentType === 'article'
          ? selectedContent.articleCoverUrl
          : selectedContent.videoCoverUrl;

        if (!coverUrl) {
          message("该科普内容没有封面图，无法作为轮播图", { type: "warning" });
          return;
        }

        const formData = new FormData();
        formData.append("contentId", selectedContent.id.toString());
        formData.append("coverUrl", coverUrl);

        loading.value = true;

        try {
            const res = await addBanner(formData);

          if (res.code === 0) {
            message("新增成功", { type: "success" });
            onSearch();
            done();
          } else {
            message(`新增失败: ${res.message}`, { type: "error" });
          }
        } catch (error) {
          message(`新增失败: ${error}`, { type: "error" });
        } finally {
          loading.value = false;
        }
      }
    });
  }

  onMounted(() => {
    onSearch();
  });

  return {
    loading,
    columns,
    dataList,
    pagination,
    selectionList,
    selectedNum: computed(() => selectionList.value.length),
    onSearch,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    handleDelete,
    onBatchDelete,
    handleUpload
  };
}





