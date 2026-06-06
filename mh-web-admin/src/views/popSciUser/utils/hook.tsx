import "./reset.css";
import dayjs from "dayjs";
import editForm from "../form/index.vue";
import { message } from "@/utils/message";
import userAvatar from "@/assets/user.jpg";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps } from "./types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import {
  getPopSciAuthorList,
  addPopSciAuthor,
  updatePopSciAuthor,
  updatePopSciAuthorAvatar,
  deletePopSciAuthor,
  deletePopSciAuthors,
  auditPopSciAuthor
} from "@/api/system";
import { type Ref, h, ref, toRaw, computed, reactive, onMounted } from "vue";

export function usePopSciAuthor(tableRef: Ref) {
  // 查询表单，分页及筛选条件
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    username: "", // 科普号名称模糊查询
    userType: null, // 科普号类型，2=待审核，3=审核通过
    certificateNum:null
  });

  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  const selectedNum = ref(0);

  // 分页配置
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  // 表格列定义
  const columns: TableColumnList = [
    {
      label: "选择",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "科普号ID",
      prop: "userId",
      width: 100
    },
    {
      label: "头像",
      prop: "avatar",
      width: 90,
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.avatar || userAvatar}
          preview-src-list={Array.of(row.avatar || userAvatar)}
          class="w-[64px] h-[64px] rounded-full align-middle"
        />
      )
    },
    {
      label: "科普号名称",
      prop: "username",
      minWidth: 180
    },
    {
      label: "科普号类型",
      prop: "userType",
      width: 120,
      formatter: ({ userType }) => {
        switch (userType) {
          case "PENDING_POPSCI":
            return "待审核";
          case "AUTHED_POPSCI":
            return "审核通过";
          default:
            return "未知";
        }
      }
    },
    {
      label: "科普认证号码",
      prop: "certificateNum",
      minWidth: 180
    },
    {
      label: "所属单位",
      prop: "affiliateUnit",
      minWidth: 180
    },
    {
      label: "创建时间",
      prop: "createTime",
      width: 180,
      formatter: ({ createTime }) => (createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "")
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
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

  // 查询数据
  async function onSearch() {
    loading.value = true;
    try {
      const params = {
        pageNum: form.pageNum,
        pageSize: form.pageSize,
        username: form.username || undefined,
        userType: form.userType || undefined,
        certificateNum: form.certificateNum || undefined
      };

      const result = await getPopSciAuthorList(toRaw(params));

      if (result.code === 0 && result.data && result.data.items) {
        pagination.total = result.data.total;
        dataList.value = result.data.items.map(item => ({
          userId: item.userId,
          username: item.username,
          avatar: item.avatar,
          userType: item.userType,
          certificateNum: item.certificateNum,
          affiliateUnit: item.affiliateUnit,
          createTime: item.createTime
        }));
      } else {
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的科普号信息", { type: "warning" });
      }
    } catch (error) {
      console.error("请求失败：", error);
      message("请求失败，请重试", { type: "error" });
    } finally {
      loading.value = false;
    }
  }

  // 重置表单
  function resetForm(formEl) {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  }

  // 分页大小改变
  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    form.pageSize = val;
    onSearch();
  }

  // 当前页改变
  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    form.pageNum = val;
    onSearch();
  }

  // 选择项变化
  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    tableRef.value.setAdaptive();
  }

  // 取消选择
  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  // 批量删除
  async function onBatchDelete() {
    const selectedRows = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(selectedRows, "userId");
    try {
      const res = await deletePopSciAuthors(ids);
      if (res.code === 0) {
        message(`已删除科普号ID为 ${ids} 的数据`, { type: "success" });
        tableRef.value.getTableRef().clearSelection();
        onSearch();
      } else {
        message("删除失败：" + res.message, { type: "error" });
      }
    } catch (error) {
      console.error("删除失败：", error);
      message("删除失败，请重试", { type: "error" });
    }
  }

  // 打开新增/编辑弹窗
  function openDialog(title = "新增", row?: FormItemProps) {
    addDialog({
      title: `${title}科普号`,
      props: {
        formInline: {
          title,
          userId: row?.userId ?? "",
          username: row?.username ?? "",
          userType: row?.userType ?? null,
          certificateNum: row?.certificateNum ??null,
          affiliateUnit: row?.affiliateUnit ?? ""
        }
      },
      width: "46%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: ({ options }) => h(editForm, {
        ref: formRef,
        formInline: options.props.formInline
      }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline;
        FormRef.validate(async valid => {
          if (valid) {
            try {
              let res;
              if (title === "新增") {
                res = await addPopSciAuthor(curData);
              } else {
                res = await updatePopSciAuthor(curData);
              }
              if (res.code === 0) {
                message(`您${title}了科普号 ${curData.username} 的数据`, { type: "success" });
                done();
                onSearch();
              } else {
                message(`${title}失败：${res.message}`, { type: "error" });
              }
            } catch (error) {
              message(`${title}失败，请重试`, { type: "error" });
            }
          }
        });
      }
    });
  }

  // 上传头像
  const cropRef = ref();
  const avatarInfo = ref();

  async function handleUpload(row) {
    addDialog({
      title: "裁剪、上传科普号头像",
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.avatar || userAvatar,
          onCropper: info => (avatarInfo.value = info)
        }),
      beforeSure: async done => {
        if (!avatarInfo.value?.blob) {
          message("请先裁剪图片或图片无效", { type: "warning" });
          return;
        }
        const formData = new FormData();
        formData.append("avatar", avatarInfo.value.blob);
        formData.append("userId", row.userId);
        try {
          const res = await updatePopSciAuthorAvatar(row.userId, formData);
          if (res.code === 0) {
            message("上传头像成功", { type: "success" });
            done();
            onSearch();
          } else {
            message("上传头像失败：" + res.message, { type: "error" });
          }
        } catch (error) {
          message("上传头像失败，请重试", { type: "error" });
        }
      },
      closeCallBack: () => {
        avatarInfo.value = null;
        cropRef.value?.hidePopover?.();
      }
    });
  }

  onMounted(() => {
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
    deviceDetection,
    onSearch,
    resetForm,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    onSelectionCancel,
    onBatchDelete,
    openDialog,
    handleUpdate: (row) => console.log(row),
    handleDelete: (row) => {
      deletePopSciAuthor(row.userId).then(res => {
        if (res.code === 0) {
          message(`删除了科普号ID为 ${row.userId} 的数据`, { type: "success" });
          onSearch();
        } else {
          message("删除失败：" + res.message, { type: "error" });
        }
      });
    },
    handleUpload
  };
}
