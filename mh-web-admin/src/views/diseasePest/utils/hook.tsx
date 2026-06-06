import { ref, h, reactive, computed, toRaw, onMounted, type Ref } from "vue";
import { message } from "@/utils/message";
import editForm from "../form/index.vue";
import { addDialog } from "@/components/ReDialog";
import {
  getAllDiseasePests,
  addDiseasePest,
  updateDiseasePest,
  deleteDiseasePest,
  deleteDiseasePests,
  getMushroomList,
  updateDiseasePestCover
} from "@/api/system";
import {
  ElMessage,
  ElMessageBox
} from "element-plus";
import type { PaginationProps } from "@pureadmin/table";
import type { DiseasePestFormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
// 新增：导入封面相关资源
import ReCropperPreview from "@/components/ReCropperPreview";
import mushroomCover from "@/assets/mushroom.png";

export function useDiseasePest(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    diseaseName: null as string | null,
    itemType: null as number | null
  });
  const typeNameMap: Record<string, string> = {
    disease: "病害",
    pest: "虫害"
  };
  const formRef = ref();
  const loading = ref(true);
  const dataList = ref<DiseasePestFormItemProps[]>([]);
  const selectedNum = ref(0);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  // 新增：封面相关状态
  const coverInfo = ref();
  const cropRef = ref();
  const coverBlob = ref<Blob | null>(null);
  const coverBase64 = ref<string | null>(null);

  const columns: TableColumnList = [
    { label: "选择", type: "selection", fixed: "left", reserveSelection: true },
    { label: "ID", prop: "id", width: 90 },
    {
      label: "图片",
      prop: "cover",
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.cover || mushroomCover}
          preview-src-list={Array.of(row.cover || mushroomCover)}
          class="w-[72px] h-[72px] rounded-lg align-middle"
        />
      ),
      width: 90
    },
    { label: "病虫害名称", prop: "diseaseName", minWidth: 150 },
    {
      label: "类型",
      prop: "itemType",
      width: 100,
      formatter: ({ itemType }) => {
        return typeNameMap[itemType] || itemType || "";
      }
    },
    {
      label: "简介",
      prop: "brief",
      minWidth: 250,
      formatter: ({ brief }) => {
        if (!brief) return "";
        if (typeof brief !== "string") return "无效数据";
        return brief.length > 60 ? brief.slice(0, 60) + "..." : brief;
      }
    },
    {
      label: "为害症状",
      prop: "symptom",
      minWidth: 250,
      formatter: ({ symptom }) => {
        if (!symptom) return "";
        if (typeof symptom !== "string") return "无效数据";
        return symptom.length > 60 ? symptom.slice(0, 60) + "..." : symptom;
      }
    },
    {
      label: "病原",
      prop: "pathogen",
      minWidth: 200,
      formatter: ({ pathogen }) => pathogen || ""
    },
    {
      label: "侵染",
      prop: "infection",
      minWidth: 200,
      formatter: ({ infection }) => infection || ""
    },
    {
      label: "发生规律",
      prop: "occurrenceRule",
      minWidth: 250,
      formatter: ({ occurrenceRule }) => {
        if (!occurrenceRule) return "";
        if (typeof occurrenceRule !== "string") return "无效数据";
        return occurrenceRule.length > 60 ? occurrenceRule.slice(0, 60) + "..." : occurrenceRule;
      }
    },
    {
      label: "防治",
      prop: "prevention",
      minWidth: 250,
      formatter: ({ prevention }) => {
        if (!prevention) return "";
        if (typeof prevention !== "string") return "无效数据";
        return prevention.length > 60 ? prevention.slice(0, 60) + "..." : prevention;
      }
    },
    {
      label: "形态",
      prop: "morphology",
      minWidth: 250,
      formatter: ({ morphology }) => {
        if (!morphology) return "";
        if (typeof morphology !== "string") return "无效数据";
        return morphology.length > 60 ? morphology.slice(0, 60) + "..." : morphology;
      }
    },
    {
      label: "习性",
      prop: "habits",
      minWidth: 250,
      formatter: ({ habits }) => {
        if (!habits) return "";
        if (typeof habits !== "string") return "无效数据";
        return habits.length > 60 ? habits.slice(0, 60) + "..." : habits;
      }
    },
    {
      label: "关联菌种",
      prop: "mushroomList",
      minWidth: 200,
      formatter: ({ mushroomList }) => {
        if (!mushroomList || mushroomList.length === 0) return "";
        return mushroomList.map((m: any) => m.mushroomName).join(", ");
      }
    },
    {
      label: "创建时间",
      prop: "createTime",
      width: 160,
      formatter: ({ createTime }) => {
        return createTime ? createTime.slice(0, 19).replace("T", " ") : "";
      }
    },
    {
      label: "操作",
      fixed: "right",
      width: 120,
      slot: "operation"
    }
  ];
  const strainOptions = ref<{ value: string; label: string }[]>([]);
  const allMushroomList = ref<{ value: string; label: string }[]>([]);
  function handleStrainClear() {
    strainOptions.value = [...allMushroomList.value];
  }
  const buttonClass = computed(() => [
    "!h-[20px]",
    "reset-margin",
    "!text-gray-500",
    "dark:!text-white",
    "dark:hover:!text-primary"
  ]);

  function openEditDialog(formTitle = "新增", row?: DiseasePestFormItemProps) {
    const mushroomIds = row?.mushroomList?.map(m => m.id) || [];
    addDialog({
      title: `病虫害管理--${formTitle}`,
      props: {
        formInline: {
          ...row,
          formTitle,
          mushroomIds
        }
      },
      width: "800px",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: ({ options }) => h(editForm, {
        ref: formRef,
        formInline: options.props.formInline,
        strainOptions: strainOptions.value // **传入菌种选项**
      }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = formRef.value.getFormData();
        const submitData = {
          ...curData,
          mushroomIds: curData.mushroomIds, // 传id数组
        };
        console.log(submitData);
        function chores() {
          message(`您${formTitle}了病虫害【${curData.diseaseName}】`, { type: "success" });
          done();
          onSearch();
        }
        FormRef.validate(valid => {
          if (valid) {
            if (formTitle === "新增") {
              // 调用新增接口
              // 这里假设你有 addDiseasePest 接口
              addDiseasePest(submitData).then(res => {
                if (res.code === 0) chores();
                else message("新增失败：" + res.message, { type: "error" });
              });
              chores(); // 先占位
            } else {
              // 调用编辑接口
              updateDiseasePest(submitData).then(res => {
                if (res.code === 0) chores();
                else message("编辑失败：" + res.message, { type: "error" });
              });
              chores(); // 先占位
            }
          }
        });
      }
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
          value: item.id,
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
  function handleDelete(row: DiseasePestFormItemProps) {
    ElMessageBox.confirm(
      `确认删除病虫害【${row.diseaseName}】吗？`,
      "删除确认",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
        draggable: true
      }
    )
      .then(() => {
        deleteDiseasePest(row.id)
          .then(res => {
            if (res.code === 0) {
              message(`删除成功：${row.diseaseName}`, { type: "success" });
              onSearch();
            } else {
              message("删除失败：" + res.message, { type: "error" });
            }
          })
          .catch(() => {
            message("删除失败", { type: "error" });
          });
      })
      .catch(() => { });
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

  function handleSelectionChange(val: DiseasePestFormItemProps[]) {
    selectedNum.value = val.length;
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  function onbatchDel() {
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "id");
    if (ids.length === 0) {
      message("请选择要删除的病虫害", { type: "warning" });
      return;
    }
    ElMessageBox.confirm(
      `确认删除选中的 <strong style="color:var(--el-color-primary)">${ids.length}</strong> 条病虫害吗？`,
      "批量删除确认",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(() => {
        deleteDiseasePests(ids)
          .then(res => {
            if (res.code === 0) {
              message(`成功删除 ${ids.length} 条病虫害`, { type: "success" });
              onSearch();
              onSelectionCancel();
            } else {
              message("批量删除失败：" + res.message, { type: "error" });
            }
          })
          .catch(() => {
            message("批量删除失败", { type: "error" });
          });
      })
      .catch(() => { });
  }

  async function onSearch() {
    loading.value = true;
    try {
      const params = toRaw(form);
      if (params.diseaseName === "" || params.diseaseName == null) {
        params.diseaseName = undefined;
      }
      if (params.itemType === null) {
        params.itemType = undefined;
      }
      const res = await getAllDiseasePests(params);
      if (res.code === 0 && res.data && res.data.items) {
        // 赋值itemTypeName，并映射coverUrl到cover字段
        dataList.value = res.data.items.map(item => ({
          ...item,
          itemTypeName: typeNameMap[item.itemType] || item.itemType || "",
          cover: item.coverUrl || "" // 确保将coverUrl映射到cover字段用于表格显示
        }));
        pagination.total = res.data.total || 0;
      } else {
        dataList.value = [];
        pagination.total = 0;
        message("获取病虫害列表失败：" + res.message, { type: "warning" });
      }
    } catch (e) {
      console.error("获取病虫害列表异常:", e);
      message("获取病虫害列表异常", { type: "error" });
      dataList.value = [];
      pagination.total = 0;
    } finally {
      loading.value = false;
    }
  }

  function resetForm(formEl: any) {
    if (!formEl) return;
    formEl.resetFields();
    form.diseaseName = null;
    form.itemType = null;
    onSearch();
  }
  // 新增：下拉框模糊搜索逻辑（基于全量列表）
  const filterStrain = (val: string) => {
    if (!val) {
      strainOptions.value = [...allMushroomList.value];
      return;
    }
    // 按菌种名称模糊匹配（不区分大小写）
    strainOptions.value = allMushroomList.value.filter(item =>
      item.label.toLowerCase().includes(val.toLowerCase())
    );
  };

  // 新增：处理裁剪器返回的数据
  function handleCropperData({ blob, base64, info }: { blob: Blob; base64: string; info: any }) {
    console.log("裁剪数据", blob, base64, info);
    coverBlob.value = blob;
    coverBase64.value = base64;
    coverInfo.value = info;
  }

  // 新增：上传病虫害封面
  async function handleUpload(row: any) {
    addDialog({
      title: "裁剪、上传病虫害封面",
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.cover || mushroomCover,
          onCropper: handleCropperData
        }),
      beforeSure: async done => {
        if (!coverBlob.value) {
          message("请先裁剪图片或图片无效", { type: "warning" });
          return;
        }

        const formData = new FormData();
        formData.append("file", coverBlob.value, `disease_pest_${row.id}_${Date.now()}.png`);

        loading.value = true;
        try {
          // 注意：这里需要调用病虫害的封面更新接口，假设为 updateDiseasePestCover
          // 由于原代码中没有提供该API，此处保留逻辑框架，实际使用时需替换为真实API
          const res = await updateDiseasePestCover(row.id, formData);
          if (res.code === 0) {
            message("上传病虫害封面成功", { type: "success" });
            done();
            onSearch();
          } else {
            message("上传病虫害封面失败：" + res.message, { type: "error" });
          }
        } catch (error) {
          console.error("上传失败:", error);
          message("上传失败，请重试", { type: "error" });
        } finally {
          loading.value = false;
        }
      },
      closeCallBack: () => {
        coverBlob.value = null;
        coverBase64.value = null;
        coverInfo.value = null;
        cropRef.value?.hidePopover?.();
      }
    });
  }

  onMounted(() => {
    onSearch();
    fetchAllMushrooms(); // 加载菌种列表
  });

  return {
    form,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    buttonClass,
    filterStrain,
    strainOptions,
    openEditDialog,
    handleStrainClear,
    onSearch,
    resetForm,
    onbatchDel,
    handleDelete,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange,
    deviceDetection,
    // 新增：导出封面相关方法
    handleUpload,
    handleCropperData
  };
}
