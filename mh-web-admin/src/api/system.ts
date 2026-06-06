import { http } from "@/utils/http";
import { getToken, formatToken } from "@/utils/auth"; // 新增导入 formatToken
export type Result = {
  code: number;
  message: string;
  data?: Array<any> | number | string | object;
};

export type ResultTable = {
  code: number;
  message: string;
  data?: {
    /** 列表数据 */
    items: Array<any>;
    /** 总条目数 */
    total?: number;
    /** 每页显示条目个数 */
    pageSize?: number;
    /** 当前页数 */
    currentPage?: number;
  };
};

/** 用户管理-获取用户列表 */
export const getUserList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/user/getAllUsers", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    }, // 确保是 JSON
    data // 直接传 JSON 对象
  });
};

/** 用户管理-新增用户 */
export const addUser = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/user/addUser", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 用户管理-编辑用户 */
export const updateUser = (data: object) => {
  const userData = getToken();
  return http.request<Result>("put", "/admin/user/updateUser", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 用户管理-更新用户状态 */
export const updateUserStatus = (id: number, status: number) => {
  const userData = getToken();
  return http.request<Result>(
    "patch",
    `/admin/user/updateUserStatus/${id}/${status}`,
    {
      headers: {
        Authorization: userData.accessToken
      }
    }
  );
};

/** 用户管理-删除用户 */
export const deleteUser = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/user/deleteUser/${id}`, {
    headers: { Authorization: userData.accessToken }
  });
};

/** 用户管理-批量删除用户 */
export const deleteUsers = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/user/deleteUsers`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data: ids
  });
};

/** 科普号管理-获取科普号列表（分页+条件查询） */
export const getPopSciAuthorList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/popSciAuthor/getAllPopSciAuthors", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 科普号管理-获取所有科普号ID和名称 */
export const getPopSciAuthorNames = (userId: number, authorName: string) => {
  const userData = getToken();
  return http.request<Result>("get","/admin/popSciAuthor/getAllPopSciAuthorNames",
    {
      headers: {
        Authorization: formatToken(userData.accessToken)
      }
    }
  );
};

/** 科普号管理-获取随机科普号（数量10） */
export const getRandomPopSciAuthors = () => {
  const userData = getToken();
  return http.request<Result>(
    "get",
    "/admin/popSciAuthor/getRandomPopSciAuthors",
    {
      headers: {
        Authorization: formatToken(userData.accessToken || "")
      }
    }
  );
};

/** 科普号管理-获取科普号详情 */
export const getPopSciAuthorDetail = (userId: number) => {
  const userData = getToken();
  return http.request<Result>(
    "get",
    `/admin/popSciAuthor/getPopSciAuthorDetail/${userId}`,
    {
      headers: {
        Authorization: formatToken(userData.accessToken || "")
      }
    }
  );
};

/** 科普号管理-新增科普号 */
export const addPopSciAuthor = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/popSciAuthor/addPopSciAuthor", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 科普号管理-更新科普号信息 */
export const updatePopSciAuthor = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/popSciAuthor/updatePopSciAuthor", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 科普号管理-更新科普号头像（对齐菌种封面更新格式） */
export const updatePopSciAuthorAvatar = (userId: number, data: object) => {
  const userData = getToken();
  return http.request<Result>("patch", `/admin/popSciAuthor/updatePopSciAuthorAvatar/${userId}`, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: userData.accessToken // 对齐菌种接口：直接使用accessToken（无formatToken）
    },
    data, // 传入FormData对象（包含avatar文件）
    responseType: "json" // 确保响应类型为json，和菌种接口一致
  });
};
/** 科普号管理-删除单个科普号 */
export const deletePopSciAuthor = (userId: number) => {
  const userData = getToken();
  const params = new URLSearchParams();
  params.append("userId", userId.toString());
  return http.request<Result>("post", "/admin/popSciAuthor/deletePopSciAuthor", {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      Authorization: formatToken(userData.accessToken || "")
    },
    data: params
  });
};

/** 科普号管理-批量删除科普号 */
export const deletePopSciAuthors = (userIds: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/popSciAuthor/deletePopSciAuthors", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data: userIds
  });
};

/** 科普号管理-审核科普号认证申请 */
export const auditPopSciAuthor = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/popSciAuthor/audit", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 菌种管理-获取菌种列表 */
export const getMushroomList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/mushroom/getAllMushrooms", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};
/** 科普内容管理-获取科普内容列表 */
export const getPopSciContentList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/popScicont/getAllPopSciContents", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};
/** 科普内容管理-审核科普内容*/
export const auditPopSciContent = (data: object) => {
  const userData = getToken();
  return http.request<Result>("put", "/admin/popScicont/auditPopSciContent", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};
/** 科普内容管理-新增科普内容 */
export const addPopSciContent = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/user/popScicon/addPopSciContent", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 科普内容管理-编辑科普内容 */
export const updatePopSciContent = (data: object) => {
  const userData = getToken();
  return http.request<Result>("put", "/user/popScicon/updatePopSciContent", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 科普内容管理-删除单个科普内容 */
export const deletePopSciContent = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/popScicont/deletePopSciContent/${id}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || "")
    }
  });
};

/** 科普内容管理-批量删除科普内容 */
export const deletePopSciContents = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/popScicont/deletePopSciContents`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data: ids
  });
};

/** 科普内容管理-上传图文详情图（单张） */
export const uploadArticleDetailPic = (contentId: number, formData: FormData) => {
  const userData = getToken();
  return http.request<Result>("post", `/user/popScicon/uploadArticleDetailPic/${contentId}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || ""),
      "Content-Type": "multipart/form-data"
    },
    data: formData
  });
};

/** 科普内容管理-上传视频封面（单张） */
export const uploadVideoCover = (contentId: number, formData: FormData) => {
  const userData = getToken();
  return http.request<Result>("post", `/user/popScicon/uploadVideoCover/${contentId}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || ""),
      "Content-Type": "multipart/form-data"
    },
    data: formData
  });
};

/** 科普内容管理-上传视频文件 */
export const uploadVideoFile = (contentId: number, formData: FormData) => {
  const userData = getToken();
  return http.request<Result>("post", `/user/popScicon/uploadVideoFile/${contentId}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || ""),
      "Content-Type": "multipart/form-data"
    },
    data: formData
  });
};

/** 科普内容管理-删除单张图文详情图 */
export const deleteArticleDetailPic = (picId: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/user/popScicon/deleteArticleDetailPic/${picId}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || "")
    }
  });
};

/** 科普内容管理-删除视频封面 */
export const deleteVideoCover = (picId: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/user/popScicon/deleteVideoCover/${picId}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || "")
    }
  });
};
/** 菌种管理-新增菌种 */
export const addMushroom = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/mushroom/addMushroom", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 菌种管理-编辑菌种 */
export const updateMushroom = (data: object) => {
  const userData = getToken();
  return http.request<Result>("put", "/admin/mushroom/updateMushroom", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 菌种管理-更新菌种封面 */
export const updateMushroomCover = (id: number, data: object) => {
  const userData = getToken();
  return http.request<Result>("patch", `/admin/mushroom/updateMushroomCover/${id}`, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: userData.accessToken
    },
    data,
    responseType: "json" // 确保使用正确的响应类型（可以使用 'json' 或 'blob'）
  });
};

/** 菌种管理-删除菌种 */
export const deleteMushroom = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/mushroom/deleteMushroom/${id}`, {
    headers: { Authorization: userData.accessToken }
  });
};

/** 菌种管理-批量删除菌种 */
export const deleteMushrooms = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/mushroom/deleteMushrooms`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data: ids
  });
};
/**
 * 根据url和菌种ID获取picID
 * @param mushroomId 
 * @param picUrl 
 * @returns 
 */
/** 菌种管理-根据URL查询图片ID */
export const getPicIdByUrl = (mushroomId: number, picUrl: string) => {
  const userData = getToken();
  return http.request<Result>("get", `/admin/mushroom/getPicIdByUrl`, {
    headers: {
      Authorization: userData.accessToken
    },
    params: { mushroomId, picUrl }
  });
};

/** 菌种管理-上传菌种详情图（修正：入参为FormData，解决类型错误） */
export const uploadMushroomDetailPics = (mushroomId: number, formData: FormData) => {
  const userData = getToken();
  return http.request<Result>("post", `/admin/mushroom/uploadMushroomDetailPics/${mushroomId}`, {
    headers: {
      Authorization: userData.accessToken,
      "Content-Type": "multipart/form-data" // 显式指定文件上传类型
    },
    data: formData
  });
};

/** 菌种管理-删除单张详情图 */
export const deleteMushroomDetailPic = (picId: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/mushroom/deleteMushroomDetailPic/${picId}`, {
    headers: {
      Authorization: userData.accessToken
    }
  });
};
/** 病虫害管理-获取病虫害分页列表 */
export const getAllDiseasePests = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/diseasePest/getAllDiseasePests", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};
/** 病虫害管理-更新病虫害封面（单张） */
export const updateDiseasePestCover = (id: number, formData: FormData) => {
  const userData = getToken();
  return http.request<Result>("patch", `/admin/diseasePest/updateDiseasePestCover/${id}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || ""),
      "Content-Type": "multipart/form-data"
    },
    data: formData
  });
};
/** 病虫害管理-新增病虫害 */
export const addDiseasePest = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/diseasePest/addDiseasePest", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 病虫害管理-编辑病虫害 */
export const updateDiseasePest = (data: object) => {
  const userData = getToken();
  return http.request<Result>("put", "/admin/diseasePest/updateDiseasePest", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data
  });
};

/** 病虫害管理-删除单个病虫害 */
export const deleteDiseasePest = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/diseasePest/deleteDiseasePest/${id}`, {
    headers: {
      Authorization: formatToken(userData.accessToken || "")
    }
  });
};

/** 病虫害管理-批量删除病虫害 */
export const deleteDiseasePests = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", "/admin/diseasePest/deleteDiseasePests", {
    headers: {
      "Content-Type": "application/json",
      Authorization: formatToken(userData.accessToken || "")
    },
    data: ids
  });
};
/** 反馈管理-获取反馈列表 */
export const getFeedbackList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/getAllFeedbacks", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 反馈管理-删除反馈 */
export const deleteFeedback = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/deleteFeedback/${id}`, {
    headers: { Authorization: userData.accessToken }
  });
};

/** 反馈管理-批量删除反馈 */
export const deleteFeedbacks = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/deleteFeedbacks`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data: ids
  });
};

/** 科普轮播图管理-获取轮播图列表 */
export const getPopSciBannerList = (data: object) => {
  const userData = getToken();
  return http.request<ResultTable>("post", "/admin/popSciBanner/getAllBanners", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 科普轮播图管理-新增轮播图 */
export const addPopSciBanner = (data: object) => {
  const userData = getToken();
  return http.request<Result>("post", "/admin/popSciBanner/addBanner", {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 科普轮播图管理-编辑轮播图（排序/状态） */
export const updatePopSciBanner = (id: number, data: object) => {
  const userData = getToken();
  return http.request<Result>("patch", `/admin/popSciBanner/updateBanner/${id}`, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: userData.accessToken
    },
    data
  });
};

/** 科普轮播图管理-单独编辑轮播图状态（适配前端拆分的状态编辑接口） */
export const updatePopSciBannerStatus = (id: number, status: number) => {
  const userData = getToken();
  return http.request<Result>("patch", `/admin/popSciBanner/updateBanner/${id}`, {
    headers: {
      Authorization: userData.accessToken
    },
    params: { status }
  });
};

/** 科普轮播图管理-删除单张轮播图 */
export const deletePopSciBanner = (id: number) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/popSciBanner/deleteBanner/${id}`, {
    headers: { Authorization: userData.accessToken }
  });
};

/** 科普轮播图管理-批量删除轮播图 */
export const deletePopSciBanners = (ids: Array<number>) => {
  const userData = getToken();
  return http.request<Result>("delete", `/admin/popSciBanner/deleteBanners`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    data: ids
  });
};


