import { ref, h,reactive, computed, toRaw, onMounted, type Ref } from "vue";
import { message } from "@/utils/message";
import editForm from "../form/index.vue";
import { addDialog } from "@/components/ReDialog";
import {
  getPopSciContentList,
  deletePopSciContent,
  deletePopSciContents,
  getMushroomList,
  auditPopSciContent
} from "@/api/system";
import {
  ElMessage,
  ElMessageBox
} from "element-plus";
import type { PaginationProps } from "@pureadmin/table";
import type { FormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";

export function usePopSciContent(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    authorName: null as string | null,
    contentType: null as "article" | "video" | null,
    theme: null as string | null,
    mushroomName: null as string | null
  });
  // --- 审核弹窗相关 ---
  const formRef = ref();
  const auditDialogVisible = ref(false);
  const strainOptions = ref<{ value: string; label: string }[]>([]);
  const allMushroomList = ref<{ value: string; label: string }[]>([]);
  const loading = ref(true);
  const dataList = ref<FormItemProps[]>([]);
  const selectedNum = ref(0);
  const auditFormRef = ref(null);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const columns: TableColumnList = [
    { label: "选择", type: "selection", fixed: "left", reserveSelection: true },
    { label: "ID", prop: "id", width: 90 },
    { label: "关联菌种", prop: "mushroomName", minWidth: 150 },
    { label: "标题", prop: "theme", minWidth: 250 },
    { label: "作者", prop: "authorName", width: 150 },
    {
      label: "内容类型",
      prop: "contentType",
      width: 100,
      formatter: ({ contentType }) => {
        if (!contentType) return "";
        if (typeof contentType === "string") {
          const map = {
            article: "图文",
            video: "视频",
            ARTICLE: "图文",
            VIDEO: "视频"
          };
          return map[contentType] || contentType;
        }
        return contentType.name || "";
      }
    },
    { label: "发布日期", prop: "publishDate", width: 120 },
    {
      label: "内容状态",
      prop: "contentStatus",
      width: 120,
      formatter: ({ contentStatus }) => {
        if (!contentStatus) return "";
        if (typeof contentStatus === "string") {
          const map = {
            PENDING: "待审核",
            PASSED: "审核通过",
            REJECTED: "审核驳回"
          };
          return map[contentStatus] || contentStatus;
        }
        return contentStatus.name || "";
      }
    },
    {
      label: "描述",
      prop: "description",
      minWidth: 300,
      formatter: ({ description }) => {
        if (!description) return "";
        if (typeof description !== "string") return "无效数据";
        return description.length > 60 ? description.slice(0, 60) + "..." : description;
      }
    },
    {
      label: "审核时间",
      prop: "auditTime",
      width: 160,
      formatter: ({ auditTime }) =>
        auditTime ? auditTime.slice(0, 19).replace("T", " ") : ""
    },
    {
      label: "驳回原因",
      prop: "rejectReason",
      minWidth: 200,
      formatter: ({ rejectReason }) => rejectReason || ""
    },
    {
      label: "操作",
      fixed: "right",
      width: 120,
      slot: "operation"
    }
  ];

  const buttonClass = computed(() => [
    "!h-[20px]",
    "reset-margin",
    "!text-gray-500",
    "dark:!text-white",
    "dark:hover:!text-primary"
  ]);
  function handleUpdate(row) {
    console.log(row);
  }
  function openAuditDialog(formTitle = "新增", row?: FormItemProps) {
    addDialog({
      title: `科普内容--${formTitle}`,
      props: {
        formInline: {
          id: row.id,
          theme: row.theme,
          content: row.content,
          videoUrl:row.videoUrl,
          contentType: row.contentType, 
          description: row.description,
          authorName: row.authorName,
          publishDate: row.publishDate,
          contentStatus: row.contentStatus || "PENDING",
          mushroomName:row.mushroomName,
          rejectReason: row.rejectReason || ""
        }
      },
      width: "600px",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: ({ options }) => h(editForm, {
          ref: formRef,
          formInline: options.props.formInline // 现在能正确拿到弹窗的props.formInline
        }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = formRef.value.getFormData();
        function chores() {
          message(`您${formTitle}了标题为 ${curData.theme} 的这条科普内容`, {
            type: "success"
          });
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        }
        FormRef.validate(valid => {
          if (valid) {
            // 表单规则校验通过
            if (formTitle === "审核") {
              auditPopSciContent(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("修改审核状态失败，" + res.message, { type: "error" });
                }
              });
            }
          }
        });
      }
    });
  }
  async function onSearch() {
    loading.value = true;
    try {
      const params = {
        pageNum: form.pageNum,
        pageSize: form.pageSize,
        authorName: form.authorName || undefined,
        contentType: form.contentType || undefined,
        theme: form.theme || undefined,
        mushroomName: form.mushroomName || undefined
      };
      const res = await getPopSciContentList(toRaw(params));
      if (res.code === 0 && res.data && res.data.items) {
        pagination.total = res.data.total || 0;
        dataList.value = res.data.items;
        console.log(dataList.value);
      } else {
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的科普内容", { type: "warning" });
      }
    } catch (error) {
      console.error("请求科普内容列表失败", error);
      message("请求失败，请重试", { type: "error" });
    } finally {
      loading.value = false;
    }
  }

  function resetForm(formEl: any) {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  }

  function handleDelete(row: FormItemProps) {
    deletePopSciContent(row.id).then(res => {
      if (res.code === 0) {
        message(`成功删除ID为 ${row.id} 的科普内容`, { type: "success" });
        onSearch();
      } else {
        message("删除失败：" + res.message, { type: "error" });
      }
    });
  }

  function handleStrainClear() {
    strainOptions.value = [...allMushroomList.value];
  }

  function onbatchDel() {
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "id");
    if (ids.length === 0) {
      message("请选择要删除的科普内容", { type: "warning" });
      return;
    }
    deletePopSciContents(ids)
      .then(res => {
        if (res.code === 0) {
          message(`成功删除ID为 ${ids.join(", ")} 的科普内容`, { type: "success" });
          tableRef.value.getTableRef().clearSelection();
          onSearch();
        } else {
          message("批量删除失败：" + res.message, { type: "error" });
        }
      })
      .catch(() => {
        message("批量删除失败", { type: "error" });
      });
  }

  async function fetchAllMushrooms() {
    try {
      const params = {
        pageNum: 1,
        pageSize: 1000,
        mushroomName: null
      };
      const res = await getMushroomList(params);
      if (res.code === 0 && res.data && res.data.items) {
        allMushroomList.value = res.data.items.map((item: any) => ({
          value: item.mushroomName,
          label: item.mushroomName
        }));
        strainOptions.value = [...allMushroomList.value];
      } else {
        allMushroomList.value = [];
        strainOptions.value = [];
      }
    } catch (error) {
      console.error("获取菌种列表失败", error);
      allMushroomList.value = [];
      strainOptions.value = [];
    }
  }

  function filterStrain(val: string) {
    if (!val) {
      strainOptions.value = [...allMushroomList.value];
      return;
    }
    const lowerVal = val.toLowerCase();
    strainOptions.value = allMushroomList.value.filter(item =>
      item.label.toLowerCase().includes(lowerVal)
    );
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    form.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    form.pageNum = val;
    onSearch();
  }

  function handleSelectionChange(val: any[]) {
    selectedNum.value = val.length;
    tableRef.value.setAdaptive?.();
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  onMounted(() => {
    fetchAllMushrooms();
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    buttonClass,
    strainOptions,
    openAuditDialog,
    filterStrain,
    handleStrainClear,
    deviceDetection,
    onSearch,
    resetForm,
    onbatchDel,
    handleDelete,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange
  };
}
