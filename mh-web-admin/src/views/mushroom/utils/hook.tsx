import "./reset.css";
import editForm from "../form/index.vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { message } from "@/utils/message";
import mushroomCover from "@/assets/mushroom.png";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import 'element-plus/dist/index.css';
import {
  getMushroomList,
  addMushroom,
  updateMushroom,
  updateMushroomCover,
  uploadMushroomDetailPics,
  deleteMushroomDetailPic,
  deleteMushroom,
  deleteMushrooms,
  getPicIdByUrl // 新增：导入根据URL查ID的API
} from "@/api/system";
import { type Ref, h, ref, toRaw, computed, reactive, onMounted } from "vue";

export function useMushroom(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    mushroomName: null
  });
  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  // 上传菌种封面信息
  const coverInfo = ref();
  // 新增：定义全量菌种列表（用于下拉框）
  const allMushroomList = ref([]);
  // 菌种下拉选项（核心：动态更新）
  const strainOptions = ref([]);
  const selectedNum = ref(0);
  const detailPicDialogVisible = ref(false); // 详情图上传弹窗显隐
  const detailFileList = ref([]); // 详情图上传文件列表
  const detailUploadLoading = ref(false); // 详情图上传加载状态
  const currentMushroomRow = ref<any>(null); // 当前操作的菌种行数据
  const detailUploadRef = ref(); // 新增：上传组件Ref
  const uploadedPics = ref<{ id?: number; url: string }[]>([]); // 已上传详情图
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  interface UploadResponseData {
    uploadedUrls: string[] | string; // 可能是数组也可能是单字符串
  }

  interface ApiResponse<T = any> {
    code: number;
    message?: string;
    data?: T;
  }
  const columns: TableColumnList = [
    {
      label: "勾选列", // 如果需要表格多选，此处label必须设置
      type: "selection",
      fixed: "left",
      reserveSelection: true // 数据刷新后保留选项
    },
    {
      label: "菌种编号",
      prop: "mushroomId",
      width: 90
    },
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
    {
      label: "菌种",
      prop: "mushroomName",
      minWidth: 300
    },
    {
      label: "基本信息",
      prop: "intro",
      minWidth: 300,
      width: 420,
      formatter: ({ intro }) => {
        if (!intro) {
          return "";
        }
        if (typeof intro !== "string") {
          return "无效数据";
        }
        const maxChars = 60; // 两行最多60个字符
        if (intro.length > maxChars) {
          return intro.slice(0, maxChars) + "...";
        }
        return intro;
      }
    },
    {
      label: "形态特征", // 新增列
      prop: "morphologicalCharacteristics",
      minWidth: 300,
      width: 420,
      formatter: ({ morphologicalCharacteristics }) => {
        if (!morphologicalCharacteristics) return "";
        if (typeof morphologicalCharacteristics !== "string") return "无效数据";
        const maxChars = 60;
        return morphologicalCharacteristics.length > maxChars ? `${morphologicalCharacteristics.slice(0, maxChars)}...` : morphologicalCharacteristics;
      }
    },
    {
      label: "环境要求", // 新增列
      prop: "environmentalRequirements",
      minWidth: 300,
      width: 420,
      formatter: ({ environmentalRequirements }) => {
        if (!environmentalRequirements) return "";
        if (typeof environmentalRequirements !== "string") return "无效数据";
        const maxChars = 60;
        return environmentalRequirements.length > maxChars ? `${environmentalRequirements.slice(0, maxChars)}...` : environmentalRequirements;
      }
    },
    {
      label: "栽培技术", // 新增列
      prop: "cultivationTechnology",
      minWidth: 300,
      width: 420,
      formatter: ({ cultivationTechnology }) => {
        if (!cultivationTechnology) return "";
        if (typeof cultivationTechnology !== "string") return "无效数据";
        const maxChars = 60;
        return cultivationTechnology.length > maxChars ? `${cultivationTechnology.slice(0, maxChars)}...` : cultivationTechnology;
      }
    },
    {
      label: "其它信息", // 新增列
      prop: "otherDetail",
      minWidth: 300,
      width: 420,
      formatter: ({ otherDetail }) => {
        if (!otherDetail) return "";
        if (typeof otherDetail !== "string") return "无效数据";
        const maxChars = 60;
        return otherDetail.length > maxChars ? `${otherDetail.slice(0, maxChars)}...` : otherDetail;
      }
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];
  const buttonClass = computed(() => {
    return [
      "!h-[20px]",
      "reset-margin",
      "!text-gray-500",
      "dark:!text-white",
      "dark:hover:!text-primary"
    ];
  });

  function handleUpdate(row) {
    console.log(row);
  }

  function handleDelete(row) {
    deleteMushroom(row.mushroomId).then(res => {
      if (res.code === 0) {
        message(`您删除了菌种编号为 ${row.mushroomId} 的这条数据`, {
          type: "success"
        });
        onSearch();
      } else {
        message("删除菌种失败", { type: "error" });
      }
    });
  }

  function handleSizeChange(val: number) {
    // console.log(`${val} items per page`);
    pagination.pageSize = val; // 更新每页条目数
    form.pageSize = val; // 更新页码参数
    onSearch(); // 重新获取数据
  }

  function handleCurrentChange(val: number) {
    // console.log(`current page: ${val}`);
    pagination.currentPage = val; // 更新当前页码
    form.pageNum = val; // 更新页码参数
    onSearch(); // 重新获取数据
  }

  /** 当CheckBox选择项发生变化时会触发该事件 */
  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    // 重置表格高度
    tableRef.value.setAdaptive();
  }

  /** 取消选择 */
  function onSelectionCancel() {
    selectedNum.value = 0;
    // 用于多选表格，清空菌种的选择
    tableRef.value.getTableRef().clearSelection();
  }

  /** 批量删除 */
  function onbatchDel() {
    // 返回当前选中的行
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "mushroomId");

    deleteMushrooms(ids)
      .then(res => {
        if (res.code === 0) {
          message(`已删除菌种编号为 ${ids} 的数据`, {
            type: "success"
          });
          tableRef.value.getTableRef().clearSelection();
          onSearch();
        } else {
          message("删除菌种失败", { type: "error" });
        }
      })
      .catch(err => {
        console.error("删除菌种失败", err);
        message("删除菌种失败", { type: "error" });
      });
  }
  // 新增：获取全量菌种列表（不分页）
  async function getAllMushroomList() {
    try {
      // 后端支持全量查询则直接调用，不支持则传超大pageSize（如9999）
      const result = await getMushroomList({
        pageNum: 1,
        pageSize: 9999, // 覆盖所有数据
        mushroomName: null // 不筛选，查全量
      });
      if (result.code === 0 && result.data && result.data.items) {
        allMushroomList.value = result.data.items.map(item => ({
          value: item.mushroomName,
          label: item.mushroomName || '未命名菌种'
        }));
        // 初始化下拉选项为全量列表
        strainOptions.value = [...allMushroomList.value];
      }
    } catch (error) {
      console.error("获取全量菌种列表失败：", error);
    }
  }

  // 新增：清空选择器后的逻辑（重置搜索+刷新表格）
  function handleStrainClear() {
    form.mushroomName = null; // 重置选择器绑定值
    filterStrain("");   // 重置下拉框为全量列表
    onSearch();         // 刷新表格（显示全部数据）
  }
  async function onSearch() {
    try {
      // 构建请求参数：重点传递 mushroomName（选中的菌种名称）
      const requestParams = {
        pageNum: form.pageNum,
        pageSize: form.pageSize,
        mushroomName: form.mushroomName || undefined, // 关键：按名称筛选，参数名和后端保持一致
      };
      console.log("按名称筛选参数：", requestParams); // 调试用，确认参数是菌种名称

      const result = await getMushroomList(toRaw(requestParams));

      if (result.code === 0 && result.data && result.data.items) {
        pagination.total = result.data.total;
        // 映射接口字段（和接口响应完全匹配）
        dataList.value = result.data.items.map(item => ({
          mushroomId: item.id,
          mushroomName: item.mushroomName || '未命名菌种',
          cover: item.coverUrl || '',
          intro: item.intro || '',
          morphologicalCharacteristics: item.contentMorphology || '',
          environmentalRequirements: item.contentEnvironment || '',
          cultivationTechnology: item.contentCultivation || '',
          otherDetail: item.otherDetail || '',
          detailPicUrls: item.detailPicUrls || []
        }));
      } else {
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的菌种信息", { type: "warning" });
      }
    } catch (error) {
      console.error("请求失败：", error);
      message("会话过期，请重新登录", { type: "error" });
    }
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };
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

  function openDialog(formTitle = "新增", row?: FormItemProps) {
    addDialog({
      title: `${formTitle}菌种`,
      props: {
        formInline: {
          formTitle,
          mushroomId: row?.mushroomId ?? "",
          mushroomName: row?.mushroomName ?? "",
          intro: row?.intro ?? "",
          morphologicalCharacteristics: row?.morphologicalCharacteristics ?? "",
          environmentalRequirements: row?.environmentalRequirements ?? "",
          cultivationTechnology: row?.cultivationTechnology ?? "",
          otherDetail: row?.otherDetail ?? "" // 补充otherDetail默认值，匹配FormItemProps
        }
      },
      width: "46%",
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
        const curData = formRef.value.getFormData(); // 需在 editForm 中暴露获取表单数据的方法
        function chores() {
          message(`您${formTitle}了菌种为 ${curData.mushroomName} 的这条数据`, {
            type: "success"
          });
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        }
        FormRef.validate(valid => {
          if (valid) {
            // 表单规则校验通过
            if (formTitle === "新增") {
              addMushroom(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("新增菌种失败，" + res.message, { type: "error" });
                }
              });
            } else {
              updateMushroom(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("修改菌种失败，" + res.message, { type: "error" });
                }
              });
            }
          }
        });
      }
    });
  }
  const cropRef = ref();
  const coverBlob = ref<Blob | null>(null);
  const coverBase64 = ref<string | null>(null);
  /** 处理裁剪器返回的数据 */
  function handleCropperData({ blob, base64, info }: { blob: Blob; base64: string; info: any }) {
    console.log("裁剪数据", blob, base64, info);
    coverBlob.value = blob;
    coverBase64.value = base64;
    coverInfo.value = info;
  }

  /** 上传菌种封面 */
  async function handleUpload(row: any) {
    addDialog({
      title: "裁剪、上传菌种封面",
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
        formData.append("file", coverBlob.value, `mushroom_${row.mushroomId}_${Date.now()}.png`);
        // formData.append("cover", coverBlob.value, `cover_${Date.now()}.png`);
        // formData.append("mushroomId", row.mushroomId);

        loading.value = true;
        try {
          const res = await updateMushroomCover(row.mushroomId, formData);
          if (res.code === 0) {
            message("上传菌种封面成功", { type: "success" });
            done();
            onSearch();
          } else {
            message("上传菌种封面失败：" + res.message, { type: "error" });
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
  // 打开弹窗时初始化
  // 打开详情图上传弹窗，初始化文件列表和已上传图片列表
  function openDetailPicUploadDialog(row: any) {
    currentMushroomRow.value = row;
    detailFileList.value = [];
    uploadedPics.value = [...(row.detailPicUrls || []).map(pic => ({
      id: pic.id,          // 提取图片ID（后端返回的Picture实体id）
      url: pic.picUrl      // 提取图片URL（后端返回的Picture实体picUrl）
    }))];
    detailPicDialogVisible.value = true;
  }

  // 上传文件列表变化时同步
  function handleDetailUploadChange(file: any, fileList: any[]) {
    detailFileList.value = fileList;
  }

  // 上传前校验
  function handleBeforeDetailUpload(file: File) {
    const isImage = file.type.startsWith("image/");
    if (!isImage) {
      message("仅支持上传图片格式文件！", { type: "error" });
      return false;
    }
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      message("单张图片大小不能超过5MB！", { type: "error" });
      return false;
    }
    return true;
  }
  // 删除单张详情图
  async function submitDetailPicUpload() {
    if (detailFileList.value.length === 0) {
      message("请选择要上传的详情图！", { type: "warning" });
      return;
    }
    if (!currentMushroomRow.value?.mushroomId) {
      message("未选择目标菌种！", { type: "error" });
      return;
    }

    detailUploadLoading.value = true;
    try {
      const formData = new FormData();
      detailFileList.value.forEach(file => {
        if (file.raw) formData.append("files", file.raw);
      });

      const res = (await uploadMushroomDetailPics(
        currentMushroomRow.value.mushroomId,
        formData
      )) as ApiResponse<UploadResponseData>;

      if (res.code === 0) {
        message("详情图上传成功！", { type: "success" });
        detailFileList.value = [];

        // 兼容 uploadedUrls 是字符串或字符串数组
        const urls = res.data?.uploadedUrls;
        if (typeof urls === "string") {
          uploadedPics.value.push({ url: urls });
        } else if (Array.isArray(urls)) {
          uploadedPics.value.push(...urls.map(url => ({ url })));
        }

        // 上传成功后刷新菌种列表，确保数据最新
        await onSearch();

        // 更新当前菌种的详情图列表，防止数据不同步
        const updatedRow = dataList.value.find(item => item.mushroomId === currentMushroomRow.value.mushroomId);
        if (updatedRow) {
          uploadedPics.value = (updatedRow.detailPicUrls || []).map((pic: any) => ({
            id: pic.id,    // 刷新后获取图片ID
            url: pic.picUrl// 刷新后获取图片URL
          }));
        }
      } else {
        message(`详情图上传失败：${res.message || "系统异常"}`, { type: "error" });
      }
    } catch (error) {
      console.error("详情图上传失败：", error);
      message("详情图上传失败，请重试", { type: "error" });
    } finally {
      detailUploadLoading.value = false;
    }
  }

  async function handleDeleteDetailPic(pic: { id?: number; url: string }) {
    if (!currentMushroomRow.value?.mushroomId) {
      message("未选择目标菌种！", { type: "error" });
      return;
    }
    try {
      if (!pic.id) {
        message("图片ID缺失，无法删除", { type: "error" });
        return;
      }
      const res = await deleteMushroomDetailPic(pic.id);
      if (res.code === 0) {
        // 从列表中移除删除的图片
        uploadedPics.value = uploadedPics.value.filter(p => p.id !== pic.id);
        // 刷新列表确保数据最新
        await onSearch();
      } else {
        message(`删除失败：${res.message}`, { type: "error" });
      }
    } catch (err) {
      // 取消删除不提示错误
    }
  }
  // 点击图片时弹出删除确认框
  const showDeleteConfirm = async (pic: { url: string }) => {
    try {
      // 弹出确认框
      await ElMessageBox.confirm(
        '此操作将删除该详情图，是否继续？',
        '提示',
        { type: 'warning' }
      );
      // 确认后执行删除逻辑
      handleDeleteDetailPic(pic);
      ElMessage.success('详情图删除成功！');
    } catch (error) {
      // 取消删除
      ElMessage.info('已取消删除');
    }
  };
  // 关闭弹窗时清理状态
  function handleDialogClose(done: () => void) {
    detailFileList.value = [];
    uploadedPics.value = [];
    currentMushroomRow.value = null;
    done();
  }

  async function confirmDeleteDetailPic(pic: { id?: number; url: string }) {
    if (!pic.id) {
      message("图片ID缺失，无法删除", { type: "error" });
      return;
    }
    try {
      await ElMessageBox.confirm("确认删除该详情图？", "提示", {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning"
      });
      const res = await deleteMushroomDetailPic(pic.id);
      if (res.code === 0) {
        message("删除成功", { type: "success" });
        uploadedPics.value = uploadedPics.value.filter(p => p.id !== pic.id);
        // 上传成功后刷新菌种列表和详情图
        await onSearch();
      } else {
        message(`删除失败：${res.message}`, { type: "error" });
      }
    } catch {
      // 取消删除不提示错误
    }
  }

  onMounted(async () => {
    await getAllMushroomList(); // 先加载全量下拉选项
    onSearch();// 再加载表格分页数据
  });

  return {
    form,
    loading,
    columns,
    dataList,
    strainOptions,
    uploadedPics, // 新增导出
    selectedNum,
    pagination,
    buttonClass,
    detailPicDialogVisible, // 导出弹窗显隐
    detailFileList, // 导出文件列表
    detailUploadLoading, // 导出上传加载状态
    detailUploadRef, // 导出上传组件Ref
    filterStrain, // 新增：导出搜索方法
    handleStrainClear,
    deviceDetection,
    submitDetailPicUpload,
    deleteDetailPic: handleDeleteDetailPic, // 绑定为 deleteDetailPic
    onSearch,
    resetForm,
    onbatchDel,
    openDetailPicUploadDialog,
    handleBeforeDetailUpload,
    openDialog,
    handleUpdate,
    handleDelete,
    handleUpload,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange,
    handleDetailUploadChange,
    handleDialogClose,
    handleDeleteDetailPic,
    confirmDeleteDetailPic,
    showDeleteConfirm
  };
  
}
