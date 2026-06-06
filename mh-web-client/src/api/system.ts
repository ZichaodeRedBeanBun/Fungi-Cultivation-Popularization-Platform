import { http } from '@/utils/http'

export type Result<T = Array<any> | number | string | object> = {
  code: number
  message: string
  data?: T // 泛型T，默认是你原来的宽泛类型，特定场景可传PlantPlanVO
}

export type ResultTable = {
  code: number
  message: string
  data?: {
    /** 列表数据 */
    items: Array<any>
    /** 总条目数 */
    total?: number
    /** 每页显示条目个数 */
    pageSize?: number
    /** 当前页数 */
    currentPage?: number
  }
}

/** 用户登录 */
export const login = (data: object) => {
  return http<Result>('post', '/user/login', { data })
}

/** 用户登出 */
export const logout = () => {
  return http<Result>('post', '/user/logout')
}

/** 发送邮箱验证码 */
export const sendEmailCode = (email: string) => {
  return http<Result>('get', '/user/sendVerificationCode', {
    params: { email },
  })
}

/** 用户注册 */
export const register = (data: object) => {
  return http<Result>('post', '/user/register', { data })
}

/** 重置密码 */
export const resetPassword = (data: object) => {
  return http<Result>('patch', '/user/resetUserPassword', { data })
}

/** 获取用户信息 */
export const getUserInfo = () => {
  return http<Result>('get', '/user/getUserInfo')
}

/** 更新用户信息 */
export const updateUserInfo = (data: object) => {
  return http<Result>('put', '/user/updateUserInfo', { data })
}
/**
 * 根据用户ID查询用户信息
 * @param userId 用户ID
 */
export const getUserById = (userId: number | string) => {
  return http<Result>('get', '/user/getUserById', {
    params: { userId } // GET请求查询参数（对应后端@RequestParam）
  });
};
/** 更新用户头像 */
export const updateUserAvatar = (formData: FormData) => {
  return http<Result>('patch', '/user/updateUserAvatar', {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    data: formData,
    transformRequest: [(data) => data], // 防止 axios 处理 FormData
  })
}
/** 注销账号 */
export const deleteUser = () => {
  return http<Result>('delete', '/user/deleteAccount')
}
/**
 * 获取菌种分页列表
 */
export const getAllMushrooms = (data: object)=> {
  return http<ResultTable>('post', '/user/search/getAllMushrooms', { data })
}
/**
 * 获取菌种概要VO分页列表（列表展示专用，少字段、高性能）
 */
export const getMushroomSummaryPage = (data: object) => {
  return http<ResultTable>('post', '/user/search/getMushroomSummaryPage', { data })
}

/**
 * 获取病虫害概要VO分页列表（列表展示专用，带封面图、少字段）
 */
export const getDiseasePestSummaryPage = (data: object) => {
  return http<ResultTable>('post', '/user/search/getDiseasePestSummaryPage', { data })
}
//==========================菌种数据=============================/**
 /* 获取所有品类列表（菌菇数据分类）
 */
export const getCategories = () => {
  return http<Result>('get', '/user/data/categories');
};

/**
 * 获取全国趋势数据
 * @param categoryId 品类ID（路径参数）
 */
export const getNationalTrend = (categoryId: number | string) => {
  return http<Result>('get', `/user/data/national-trend/${categoryId}`);
};

/**
 * 获取省份对比数据
 * @param categoryId 品类ID（路径参数）
 * @param year 年份（路径参数）
 */
export const getProvinceCompare = (categoryId: number | string, year: number) => {
  return http<Result>('get', `/user/data/province-compare/${categoryId}/${year}`);
};
// ====================== 病虫害详情API ======================
/**
 * 根据病虫害ID查询单条病虫害详细信息
 * @param diseasePestId 病虫害ID
 */
export const getDiseasePestById = (diseasePestId: number | string) => {
  return http<Result>('get', '/user/search/getDiseasePestById', {
    params: { diseasePestId } // GET查询参数
  });
};

// ====================== 菌种详情API ======================
/**
 * 根据菌种ID查询单条菌种详细信息
 * @param mushroomId 菌种ID
 */
export const getMushroomById = (mushroomId: number | string) => {
  return http<Result>('get', '/user/search/getMushroomById', {
    params: { mushroomId } // GET查询参数
  });
};

// ====================== 科普内容详情API ======================
/**
 * 根据ID查询科普内容详情（返回完整VO，含图片/作者/菌种等关联信息）
 * @param contentId 科普内容主键ID
 */
export const getPopSciContentById = (contentId: number | string) => {
  return http<Result>('get', '/user/search/getByContentId', {
    params: { contentId } // GET查询参数
  });
};
/**
 * 获取病虫害分页列表
 */
export const getAllDiseasePests = (data: object) => {
  return http<ResultTable>('post', '/user/search/getAllDiseasePests', { data })
}
// 获取科普内容列表（分页）
export const getAllPopSciContents = (data: object)=> {
  return http<ResultTable>('post', '/user/search/getAllPopSciContents', { data })
}
// ====================== 科普号列表API ======================
/**
 * 提交科普号认证申请
 * @param popsciApplyDTO 科普号认证申请信息
 */
export const applyPopsciAuth = (data: object) => {
  return http<Result>('post', '/user/applyPopsciAuth', { data })
}

/**
 * 分页搜索科普号列表
 * @param data 查询条件
 */
export const searchPopSciAuthors = (data: object) => {
  return http<ResultTable>('post', '/user/search/getPopSciAuthors', { data });
};
/**
 * 获取随机科普号（数量10）
 * 对应后端 getRandomPopSciAuthors 接口
 * @returns 随机科普号VO列表（固定10条）
 */
export const getRandomPopSciAuthors = () => {
  return http<Result>('get', '/user/search/getRandomPopSciAuthors');
};

// ====================== 科普号详情API ======================
/**
 * 根据科普号ID查询单条科普号详细信息
 * @param userId 科普号ID（用户ID）
 */
export const getPopSciAuthorDetail = (userId: number | string) => {
  return http<Result>('get', `/user/search/getPopSciAuthorDetail/${userId}`);
};

/**
 * 获取推荐图文列表（返回12条）
 * 未登录：返回图文类型随机12条
 * 已登录：根据用户收藏的菌种偏好推荐，不足则用随机内容填充
 */
export const getRecommendedArticles = () => {
  return http<Result>('get', '/user/popsci/recommended/articles')
}

/**
 * 获取推荐视频列表（返回6条）
 * 未登录：返回视频类型随机6条
 * 已登录：根据用户收藏的菌种偏好推荐，不足则用随机内容填充
 */
export const getRecommendedVideos = () => {
  return http<Result>('get', '/user/popsci/recommended/videos')
}
// 新增科普内容
export const addPopSciContent = (data: FormData): Promise<Result> => {
  return httpUpload<Result>('/user/popsci/addPopSciContent', data)
}

// 编辑科普内容
export const updatePopSciContent = (data: FormData): Promise<Result> => {
  return httpUpload<Result>('/user/popsci/updatePopSciContent', data)
}
// 删除单张图文详情图片
export const deleteArticleDetailPic = (picId: number) => {
  return http<Result>('delete', `/user/popsci/deleteArticleDetailPic/${picId}`)
}

// 删除视频封面
export const deleteVideoCover = (picId: number) => {
  return http<Result>('delete', `/user/popsci/deleteVideoCover/${picId}`)
}

/**
 * 通用删除图片（支持多种删除条件）
 */
export const deletePicture = (data:object) => {
  // ✅ 关键：把数据放到 config 的 data 属性里
  return http<Result>('post', `/user/popsci/deletePicture`, { data })
}
// 删除单个科普内容
export const deletePopSciContent = (id: number): Promise<Result> => {
  return http<Result>('delete', `/user/popsci/deletePopSciContent/${id}`)
}

// 批量删除科普内容
export const deletePopSciContents = (ids: number[]): Promise<Result> => {
  return http<Result>('delete', '/user/popsci/deletePopSciContents', { data: ids })
}

// ===================== 种植计划相关API =====================
/**
 * 获取种植计划分页列表（支持筛选执行中/所有计划）
 * @param data 分页查询参数（PlantPlanPageQueryDTO）
 */
export const getPlantPlanPage = (data: object) => {
  return http<ResultTable>('post', '/user/plantPlan/getPlantPlanPage', { data })
}

/**
 * 根据ID查询种植计划详情
 * @param planId 种植计划ID
 */
export const getPlantPlanById = (planId: number | string) => {
  return http<Result>('get', `/user/plantPlan/getPlantPlanById/${planId}`)
}

/**
 * 新增种植计划
 * @param data 种植计划新增参数（PlantPlanAddDTO）
 */
export const addPlantPlan = (data: object) => {
  return http<Result>('post', '/user/plantPlan/addPlantPlan', { data })
}

/**
 * 更新种植计划
 * @param data 种植计划更新参数（PlantPlanUpdateDTO）
 */
export const updatePlantPlan = (data: object) => {
  return http<Result>('put', '/user/plantPlan/updatePlantPlan', { data })
}

/**
 * 删除单个种植计划
 * @param planId 种植计划ID
 */
export const deletePlantPlan = (planId: number | string) => {
  return http<Result>('delete', `/user/plantPlan/deletePlantPlan/${planId}`)
}

/**
 * 批量删除种植计划
 * @param data 种植计划ID列表
 */
export const deletePlantPlans = (data: Array<number | string>) => {
  return http<Result>('delete', '/user/plantPlan/deletePlantPlans', { data })
}

// ===================== 种植节点相关API =====================
/**
 * 根据计划ID查询种植节点列表
 * @param planId 种植计划ID
 */
export const getPlantNodeListByPlanId = (planId: number | string) => {
  return http<Result>('get', `/user/plantNode/getPlantNodeListByPlanId/${planId}`)
}

/**
 * 新增种植节点
 * @param data 种植节点新增参数（PlantNodeAddDTO）
 */
export const addPlantNode = (data: object) => {
  return http<Result>('post', '/user/plantNode/addPlantNode', { data })
}

/**
 * 更新种植节点
 * @param data 种植节点更新参数（PlantNodeUpdateDTO）
 */
export const updatePlantNode = (data: object) => {
  return http<Result>('put', '/user/plantNode/updatePlantNode', { data })
}

/**
 * 标记节点为已完成
 * @param nodeId 种植节点ID
 */
export const markNodeCompleted = (nodeId: number | string) => {
  return http<Result>('put', `/user/plantNode/markNodeCompleted/${nodeId}`)
}

/**
 * 删除单个种植节点
 * @param nodeId 种植节点ID
 */
export const deletePlantNode = (nodeId: number | string) => {
  return http<Result>('delete', `/user/plantNode/deletePlantNode/${nodeId}`)
}

/**
 * 批量删除种植节点
 * @param data 种植节点ID列表
 */
export const deletePlantNodes = (data: Array<number | string>) => {
  return http<Result>('delete', '/user/plantNode/deletePlantNodes', { data })
}

// ===================== 种植数据相关API =====================
/**
 * 获取种植数据分页列表（支持按计划ID/时间/生长状态筛选）
 * @param data 分页查询参数（PlantDataPageQueryDTO）
 */
export const getPlantDataPage = (data: object) => {
  return http<ResultTable>('post', '/user/plantData/getPlantDataPage', { data })
}

/**
 * 根据ID查询种植数据详情
 * @param dataId 种植数据ID
 */
export const getPlantDataById = (dataId: number | string) => {
  return http<Result>('get', `/user/plantData/getPlantDataById/${dataId}`)
}

/**
 * 新增种植数据记录
 * @param data 种植数据新增参数（PlantDataAddDTO）
 */
export const addPlantData = (data: object) => {
  return http<Result>('post', '/user/plantData/addPlantData', { data })
}

/**
 * 更新种植数据记录
 * @param data 种植数据更新参数（PlantDataUpdateDTO）
 */
export const updatePlantData = (data: object) => {
  return http<Result>('put', '/user/plantData/updatePlantData', { data })
}

/**
 * 删除单个种植数据记录
 * @param dataId 种植数据ID
 */
export const deletePlantData = (dataId: number | string) => {
  return http<Result>('delete', `/user/plantData/deletePlantData/${dataId}`)
}

/**
 * 批量删除种植数据记录
 * @param data 种植数据ID列表
 */
export const deletePlantDatas = (data: Array<number | string>) => {
  return http<Result>('delete', '/user/plantData/deletePlantDatas', { data })
}

// ===================== 种植成果相关API =====================
/**
 * 根据计划ID查询种植成果（一对一）
 * @param planId 种植计划ID
 */
export const getPlantResultByPlanId = (planId: number | string) => {
  return http<Result>('get', `/user/plantResult/getPlantResultByPlanId/${planId}`)
}

/**
 * 新增种植成果
 * @param data 种植成果新增参数（PlantResultAddDTO）
 */
export const addPlantResult = (data: object) => {
  return http<Result>('post', '/user/plantResult/addPlantResult', { data })
}

/**
 * 更新种植成果
 * @param data 种植成果更新参数（PlantResultUpdateDTO）
 */
export const updatePlantResult = (data: object) => {
  return http<Result>('put', '/user/plantResult/updatePlantResult', { data })
}

/**
 * 删除单个种植成果
 * @param resultId 种植成果ID
 */
export const deletePlantResult = (resultId: number | string) => {
  return http<Result>('delete', `/user/plantResult/deletePlantResult/${resultId}`)
}
// ====================== 收藏相关API ======================
/**
 * 收藏科普内容
 * @param popsciId 科普内容ID
 * @param favoriteType 收藏类型（ARTICLE/VIDEO）
 * @param mushroomName 菌种名字
 */
export const collectPopSci = (params: { 
  popsciId: number | string; 
  favoriteType: string; // 核心修改：从number改为string
  mushroomName: string 
}) => {
  return http<Result>('post', '/user/favorite/collect', {
    params: params // 直接透传，前端传ARTICLE/VIDEO，后端自动解析
  });
};

/**
 * 取消收藏科普内容
 * @param popsciId 科普内容ID
 */
export const cancelCollectPopSci = (popsciId: number | string) => {
  return http<Result>('delete', '/user/favorite/cancelCollect', {
    params: { popsciId } // DELETE请求查询参数
  });
};
/**
 * 批量取消收藏科普内容
 * @param popsciIds 科普内容ID数组
 */
export const batchCancelCollectPopSci = (popsciIds: number[]) => {
  return http<Result>('delete', '/user/favorite/batchCancelCollect', {
    data: popsciIds // DELETE请求请求体参数（对应后端@RequestBody）
  });
};
/**
 * 获取用户收藏的科普内容列表（分页）
 * @param dto 查询条件
 */
export const getUserFavoritePopSciList = (data: object) => {
  return http<ResultTable>('post', '/user/favorite/getFavoriteList', {
    data// POST请求体参数
  });
};
/**
 * 科普内容点赞
 * @param popsciId 科普内容ID
 */
export const praisePopSci = (popsciId: number | string) => {
  return http<Result>('post', '/user/popsci/praise', {
    params: { popsciId } // POST请求查询参数（对应后端@RequestParam）
  });
};

/**
 * 取消科普内容点赞
 * @param popsciId 科普内容ID
 */
export const cancelPraisePopSci = (popsciId: number | string) => {
  return http<Result>('delete', '/user/popsci/cancelPraise', {
    params: { popsciId } // DELETE请求查询参数（对应后端@RequestParam）
  });
};
// ====================== 评论相关API ======================
/**
 * 新增科普内容评论
 * @param dto 评论入参
 */
export const addPopSciComment = (data: object) => {
  return http<Result>('post', '/user/comment/addPopSciComment', {
    data // POST请求体参数
  });
};

/**
 * 点赞评论
 * @param commentId 评论ID
 */
export const likeComment = (commentId: number | string) => {
  return http<Result>('patch', `/user/comment/likeComment/${commentId}`); // 路径参数
};

/**
 * 取消点赞评论
 * @param commentId 评论ID
 */
export const cancelLikeComment = (commentId: number | string) => {
  return http<Result>('patch', `/user/comment/cancelLikeComment/${commentId}`); // 路径参数
};
/**
 * 用户个人主页 - 查询科普内容精简列表（分页）
 * @param dto 查询条件（userId/mediaType/mushroomId/pageNum/pageSize）
 */
export const getUserContentList = (data) => {
  return http<ResultTable>('post', '/user/popsci/userContentList', {
    data // POST请求体参数（对应后端@RequestBody）
  });
};

/**
 * 根据科普内容ID查询评论列表（按时间倒序）
 * @param popsciId 科普内容主键ID
 */
export const listPopSciComment = (popsciId: number | string) => {
  return http<Result>('get', `/user/comment/listPopSciComment/${popsciId}`)
}

/**
 * 删除单条科普评论
 * @param commentId 评论主键ID（适配你后端deleteComment/{id}接口）
 */
export const deletePopSciComment = (commentId: number) => {
  return http<Result>('delete', `/user/comment/deleteComment/${commentId}`)
}
/** 科普轮播图-用户端获取展示列表 */
export const getUserPopSciBannerList = () => {
  return http<Result>('get', '/user/search/getBannerList')
}
/** 新增反馈 */
export const addFeedback = (data: { content: string }) => {
  return http<Result>('post', '/user/addFeedback', { params: data })
}

/**
 * 智能聊天主入口（支持文字+图片同时上传）
 * @param data FormData对象，包含可选的 message（文本）和 image（图片）
 */
export const chatWithMushroomAI = (data: FormData): Promise<Result> => {
  return httpUpload<Result>('/user/AIChat/chat', data)
}

/**
 * 蘑菇图片单独识别（仅上传图片，返回识别结果）
 * @param data FormData对象，包含必须的 file（蘑菇图片）
 */
export const recognizeMushroomImage = (data: FormData): Promise<Result> => {
  return httpUpload<Result>('/user/mushroomAI/recognize', data)
}
