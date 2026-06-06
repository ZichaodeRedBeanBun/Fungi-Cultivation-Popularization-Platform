/**
 * 通用分页结果VO（前端版，匹配后端PageResult）
 */
export interface PageResult<T> {
  items: T[];                              // 列表数据
  total?: number;                          // 总条目数
  pageSize?: number;                       // 每页显示条目个数
  currentPage?: number;                    // 当前页数
}

export interface ChatResponse {
  reply: string
  mushroomName?: string
  isPoison?: number
  confidence?: number
  toxicity?: string
}

/**
 * 科普号搜索查询DTO
 */
export interface PopSciAuthorSearchDTO {
  pageNum: number;                 // 当前页码（必填）
  pageSize: number;                // 每页条数（必填）
  username?: string;               // 科普号名称（模糊查询，可选）
  userId?: number;                 // 科普号ID（模糊查询，可选）
}

/**
 * 科普号ID+名称VO
 */
export interface PopSciAuthorNameVO {
  userId: number;                  // 科普号ID
  authorName: string;              // 科普号名称
  avatar: string;                  // 头像URL
}
/**
 * 品类项VO（匹配后端CategoryItemVO）
 */
export interface CategoryItemVO {
  id: number;                 // 品类ID
  categoryName: string;       // 品类名称
  categoryType: string;       // 品类类型
  unit: string;               // 单位（如：吨、万袋、元/公斤）
}

/**
 * 全国趋势VO（匹配后端NationalTrendVO）
 */
export interface NationalTrendVO {
  categoryName: string;       // 品类名称
  unit: string;               // 单位
  years: number[];            // 年份列表（如：[2018,2019,2020]）
  values: number[];           // 对应年份的数值（BigDecimal前端转为number）
}

/**
 * 省份对比VO（匹配后端ProvinceCompareVO）
 */
export interface ProvinceCompareVO {
  categoryName: string;       // 品类名称
  unit: string;               // 单位
  year: number;               // 年份
  provinces: string[];        // 省份列表（如：["山东","河南","江苏"]）
  values: number[];           // 对应省份的数值（BigDecimal前端转为number）
}
/**
 * 科普号
 */
/**
 * 科普号分页查询DTO（对应后端 PopSciAuthorDTO）
 */
export interface PopSciAuthorDTO {
  /** 分页参数-页码（默认1） */
  pageNum?: number;
  /** 分页参数-页大小（默认10） */
  pageSize?: number;
  /** 科普号名称（模糊查询） */
  authorName?: string;
  /** 科普号类型：2=待认证，3=已认证 */
  userType?: number;
  /** 认证号码 */
  certificateNum?: string;
}

/**
 * 用户VO（含科普号专属字段，对应后端 UserVO）
 */
export interface UserVO {
  /** 用户id */
  userId: number;
  /** 用户名 */
  username: string;
  /** 用户邮箱 */
  email?: string;
  /** 用户身份：1=普通用户，2=待认证科普号，3=已认证科普号 */
  userType?: number;
  /** 用户头像 */
  userAvatar?: string;
  /** 用户简介 */
  introduction?: string;

  // 科普号专属字段（普通用户为null/undefined）
  /** 认证号码 */
  certificateNum?: string;
  /** 所属单位 */
  affiliateUnit?: string;
  /** 认证申请时间 */
  certApplyTime?: string;
  /** 审核时间 */
  certAuditTime?: string;
}
/**
 * 科普号VO（列表展示专用）
 */
export interface PopSciAuthorVO {
  /** 科普号ID */
  userId: number;
  /** 科普号名称 */
  username: string;
  /** 头像URL */
  avatar: string;
  /** 科普号认证号码 */
  certificateNum?: string;
  /** 科普号类型：2=待认证，3=已认证 */
  userType: number;
  /** 所属单位 */
  affiliateUnit?: string;
  /** 账号状态：1=正常，2=封禁 */
  userStatus: number;
  /** 创建时间 */
  createTime: string;
}
/**
 * 科普内容收藏类型枚举（匹配后端FavoriteTypeEnum）
 */
export enum FavoriteTypeEnum {
  ARTICLE = 'ARTICLE', // 图文
  VIDEO = 'VIDEO'      // 视频
}

/**
 * 收藏类型ID映射（数据库数字值 ↔ 枚举）
 */
export const FavoriteTypeIdMap = {
  0: FavoriteTypeEnum.ARTICLE,
  1: FavoriteTypeEnum.VIDEO
};

/**
 * 收藏类型中文描述映射
 */
export const FavoriteTypeDescMap = {
  [FavoriteTypeEnum.ARTICLE]: '图文',
  [FavoriteTypeEnum.VIDEO]: '视频'
};
/**
 * 新增科普内容评论DTO（前端版，匹配后端CommentPopSciDTO）
 */
export interface CommentPopSciDTO {
  popsciId: number;                // 科普内容ID（必填）
  content: string;                 // 评论内容（必填）
}

/**
 * 用户收藏科普内容查询DTO（前端版，匹配后端PopSciFavoriteDTO）
 */
export interface PopSciFavoriteDTO {
  pageNum: number;                 // 当前页码（必填）
  pageSize: number;                // 每页条数（必填）
  favoriteType?: number;           // 收藏类型（0=图文，1=视频，可选）
  mushroomId?: number;             // 菌种ID（可选）
}
// ====== 扩展接口：解决 TypeScript 报错（无需修改全局 interface.ts）======
export interface PopSciContentVOWithExtra extends PopSciContentVO {
  isCollection?: boolean;   // 当前用户是否收藏
  collectCount?: number; // 收藏总数
  reviewCount?: number;     // 评论总数（用于显示）
  mushroomId?: number;      // 菌种ID（收藏时需要）
}
/**
 * 科普内容评论VO（前端版，匹配后端CommentPopSciVO）
 */
export interface CommentPopSciVO {
  commentId: number;               // 评论ID
  userId: number;
  contentAuthorId:number;
  username: string;                // 用户名
  userAvatar: string;              // 用户头像
  content: string;                 // 评论内容
  createTime: string;              // 评论时间（格式：yyyy-MM-dd）
  likeCount: number;               // 点赞数量
}
/**
 * 种植计划状态枚举（匹配后端PlantPlanStatusEnum）
 */
export enum PlantPlanStatusEnum {
  EXECUTING = 'EXECUTING', // 执行中
  COMPLETED = 'COMPLETED', // 已完成
  PAUSED = 'PAUSED',       // 暂停
  INVALID = 'INVALID'      // 作废
}

/**
 * 种植计划状态ID映射（用于数据库数字值与枚举的转换）
 */
export const PlantPlanStatusIdMap = {
  1: PlantPlanStatusEnum.EXECUTING,
  2: PlantPlanStatusEnum.COMPLETED,
  3: PlantPlanStatusEnum.PAUSED,
  4: PlantPlanStatusEnum.INVALID
};

/**
 * 种植计划状态中文描述映射
 */
export const PlantPlanStatusDescMap = {
  [PlantPlanStatusEnum.EXECUTING]: '执行中',
  [PlantPlanStatusEnum.COMPLETED]: '已完成',
  [PlantPlanStatusEnum.PAUSED]: '暂停',
  [PlantPlanStatusEnum.INVALID]: '作废'
};

/**
 * 种植生长状态枚举（匹配后端PlantGrowthStatusEnum）
 */
export enum PlantGrowthStatusEnum {
  EXCELLENT = 'EXCELLENT', // 优
  GOOD = 'GOOD',           // 良
  POOR = 'POOR'            // 差
}

/**
 * 种植生长状态ID映射（用于数据库数字值与枚举的转换）
 */
export const PlantGrowthStatusIdMap = {
  1: PlantGrowthStatusEnum.EXCELLENT,
  2: PlantGrowthStatusEnum.GOOD,
  3: PlantGrowthStatusEnum.POOR
};

/**
 * 种植生长状态中文描述映射
 */
export const PlantGrowthStatusDescMap = {
  [PlantGrowthStatusEnum.EXCELLENT]: '优',
  [PlantGrowthStatusEnum.GOOD]: '良',
  [PlantGrowthStatusEnum.POOR]: '差'
};

/**
 * 种植节点状态枚举（匹配后端节点状态定义）
 */
export enum PlantNodeStatusEnum {
  UNFINISHED = 1,   // 未完成
  FINISHED = 2,     // 已完成
  OVERDUE = 3       // 逾期未完成
}

/**
 * 种植节点类型枚举（匹配后端节点类型定义）
 */
export enum PlantNodeTypeEnum {
  MATERIAL_PREP = '备料',    // 备料
  STERILIZATION = '灭菌',    // 灭菌
  INOCULATION = '接种',      // 接种
  MYCELIUM_CULTURE = '发菌', // 发菌
  FRUITING = '出菇',         // 出菇
  HARVEST = '采收'           // 采收
}

/**
 * 新增种植成果DTO（前端版，匹配后端PlantResultAddDTO）
 */
export interface PlantResultAddDTO {
  planId: number;                          // 关联种植计划ID（必填）
  harvestTime: string;                     // 采收时间（必填，格式：yyyy-MM-dd HH:mm:ss）
  actualYield: number;                     // 实际产量（kg，必填）
  yieldGrade?: string;                     // 产量品质（特级/一级/普通，可选）
  actualIncome?: number;                   // 实际收益（元，可选）
  lossReason?: string;                     // 减产/损失原因（可选）
  summary?: string;                        // 计划总结（可选）
}
/**
 * 科普号认证申请DTO
 */
export interface PopsciApplyDTO {
  /**
   * 科普号认证号码（必填）
   */
  certificateNum: string;
  /**
   * 科普号所属单位（必填）
   */
  affiliateUnit: string;
  /**
   * 科普号简介（可选）
   */
  introduction?: string;
}
/**
 * 新增种植数据DTO（前端版，匹配后端PlantDataAddDTO）
 */
export interface PlantDataAddDTO {
  planId: number;                          // 关联种植计划ID（必填）
  recordTime?: string;                     // 数据记录时间（默认当前时间，可选，格式：yyyy-MM-dd HH:mm:ss）
  envTemp?: number;                        // 环境温度（℃，可选）
  envHumidity?: number;                    // 环境湿度（%，可选）
  co2Concentration?: number;               // CO₂浓度（‰，可选）
  farmingOper?: string;                    // 农事操作（如浇水5L/㎡；灭菌2小时，可选）
  growthStatus?: PlantGrowthStatusEnum;    // 生长状态：1=优 2=良 3=差（可选）
  diseaseStatus?: string;                  // 病虫害情况（可选）
  inputCost?: number;                      // 当日投入成本（元，可选）
}

/**
 * 更新种植节点DTO（前端版，匹配后端PlantNodeUpdateDTO）
 */
export interface PlantNodeUpdateDTO {
  nodeId: number;                          // 节点主键ID（必填）
  nodeType?: PlantNodeTypeEnum;            // 节点类型（备料/灭菌/接种/发菌/出菇/采收，可选）
  nodeName?: string;                       // 节点名称（可选）
  planTime?: string;                       // 节点预计执行时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  remindTime?: string;                     // 节点提醒时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  actualTime?: string;                     // 节点实际完成时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  status?: PlantNodeStatusEnum;            // 节点状态：1=未完成 2=已完成 3=逾期未完成（可选）
  remark?: string;                         // 节点备注（可选）
}

/**
 * 新增种植节点DTO（前端版，匹配后端PlantNodeAddDTO）
 */
export interface PlantNodeAddDTO {
  planId: number;                          // 关联种植计划ID（必填）
  nodeType: PlantNodeTypeEnum;             // 节点类型（备料/灭菌/接种/发菌/出菇/采收，必填）
  nodeName: string;                        // 节点名称（如香菇接种后7天翻堆，必填）
  planTime: string;                        // 节点预计执行时间（必填，格式：yyyy-MM-dd HH:mm:ss）
  remindTime?: string;                     // 节点提醒时间（默认提前1天，可选，格式：yyyy-MM-dd HH:mm:ss）
  remark?: string;                         // 节点备注（可选）
}

/**
 * 新增种植计划DTO（前端版，匹配后端PlantPlanAddDTO）
 */
export interface PlantPlanAddDTO {
  planName: string;                        // 计划名称（必填）
  fungusType: string;                      // 种植菌型（必填）
  plantArea?: number;                      // 种植面积（亩/㎡，可选）
  startTime?: string;                      // 计划开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  endTime?: string;                        // 计划结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  expectYield?: number;                    // 预期产量（kg，可选）
  expectIncome?: number;                   // 预期收益（元，可选）
  status?: PlantPlanStatusEnum;            // 计划状态：1=执行中 2=已完成 3=暂停 4=作废（默认1）
  remark?: string;                         // 计划备注（可选）
}

/**
 * 种植数据分页查询DTO（前端版，匹配后端PlantDataPageQueryDTO）
 */
export interface PlantDataPageQueryDTO {
  pageNum: number;                         // 当前页码（默认1）
  pageSize: number;                        // 每页条数（默认10）
  planId: number;                          // 关联种植计划ID（必填）
  recordTime?: string;                     // 记录时间开始范围（如2025-01-01 00:00:00）
  growthStatus?: PlantGrowthStatusEnum;    // 生长状态：1=优 2=良 3=差（为空则查所有）
  // recorderId 由后端获取当前登录用户，前端无需传递
}

/**
 * 更新种植数据DTO（前端版，匹配后端PlantDataUpdateDTO）
 */
export interface PlantDataUpdateDTO {
  dataId: number;                          // 数据记录主键ID（必填）
  recordTime?: string;                     // 数据记录时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  envTemp?: number;                        // 环境温度（℃，可选）
  envHumidity?: number;                    // 环境湿度（%，可选）
  co2Concentration?: number;               // CO₂浓度（‰，可选）
  farmingOper?: string;                    // 农事操作（可选）
  growthStatus?: PlantGrowthStatusEnum;    // 生长状态：1=优 2=良 3=差（可选）
  diseaseStatus?: string;                  // 病虫害情况（可选）
  inputCost?: number;                      // 当日投入成本（元，可选）
}

/**
 * 更新种植成果DTO（前端版，匹配后端PlantResultUpdateDTO）
 */
export interface PlantResultUpdateDTO {
  resultId: number;                        // 成果记录主键ID（必填）
  harvestTime?: string;                    // 采收时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  actualYield?: number;                    // 实际产量（kg，可选）
  yieldGrade?: string;                     // 产量品质（特级/一级/普通，可选）
  actualIncome?: number;                   // 实际收益（元，可选）
  lossReason?: string;                     // 减产/损失原因（可选）
  summary?: string;                        // 计划总结（可选）
}

/**
 * 种植计划分页查询DTO（前端版，匹配后端PlantPlanPageQueryDTO）
 */
export interface PlantPlanPageQueryDTO {
  pageNum: number;                         // 当前页码（默认1）
  pageSize: number;                        // 每页条数（默认10）
  planName?: string;                       // 计划名称（模糊查询）
  status?: PlantPlanStatusEnum |"";            // 计划状态：1=执行中 2=已完成 3=暂停 4=作废（为空则查所有）
  // userId 由后端获取当前登录用户，前端无需传递
}

/**
 * 种植节点展示VO（前端版，匹配后端PlantNodeVO）
 */
export interface PlantNodeVO {
  nodeId: number;                          // 节点主键ID
  planId: number;                          // 关联种植计划ID
  nodeType: PlantNodeTypeEnum |"";             // 节点类型（备料/灭菌/接种/发菌/出菇/采收）
  nodeName: string;                        // 节点名称
  planTime: string;                        // 节点预计执行时间（格式：yyyy-MM-dd HH:mm:ss）
  remindTime: string;                      // 节点提醒时间（格式：yyyy-MM-dd HH:mm:ss）
  actualTime?: string;                     // 节点实际完成时间（格式：yyyy-MM-dd HH:mm:ss）
  status: PlantNodeStatusEnum |"";             // 节点状态：1=未完成 2=已完成 3=逾期未完成
  statusName: string;                      // 节点状态名称（前端展示用）
  remark?: string;                         // 节点备注
}
/**
 * 种植计划展示VO（前端版，匹配后端PlantPlanVO）
 */
export interface PlantPlanVO {
  planId: number;                          // 种植计划主键ID
  userId: number;                          // 所属用户ID
  planName: string;                        // 计划名称
  fungusType: string;                      // 种植菌型（香菇/金针菇/羊肚菌等）
  plantArea?: number;                      // 种植面积（亩/㎡，BigDecimal转number）
  startTime?: string;                      // 计划开始时间（格式：yyyy-MM-dd HH:mm:ss）
  endTime?: string;                        // 计划结束时间（格式：yyyy-MM-dd HH:mm:ss）
  expectYield?: number;                    // 预期产量（kg，BigDecimal转number）
  expectIncome?: number;                   // 预期收益（元，BigDecimal转number）
  status: PlantPlanStatusEnum |"";             // 计划状态：1=执行中 2=已完成 3=暂停 4=作废
  remark?: string;                         // 计划备注
  createTime: string;                      // 计划创建时间（格式：yyyy-MM-dd HH:mm:ss）
  updateTime: string;                      // 计划更新时间（格式：yyyy-MM-dd HH:mm:ss）
}
/**
 * 种植成果展示VO（前端版，匹配后端PlantResultVO）
 */
export interface PlantResultVO {
  resultId: number;                        // 成果记录主键ID
  planId: number;                          // 关联种植计划ID
  harvestTime?: string;                    // 采收时间（格式：yyyy-MM-dd HH:mm:ss）
  actualYield?: number;                    // 实际产量（kg，BigDecimal转number）
  yieldGrade?: string;                     // 产量品质（特级/一级/普通）
  actualIncome?: number;                   // 实际收益（元，BigDecimal转number）
  lossReason?: string;                     // 减产/损失原因
  summary?: string;                        // 计划总结
  createTime: string;                      // 成果记录创建时间（格式：yyyy-MM-dd HH:mm:ss）
  updateTime: string;                      // 成果记录更新时间（格式：yyyy-MM-dd HH:mm:ss）
  // 扩展字段（从计划主表获取，用于前端对比展示）
  expectYield?: number;                    // 预期产量（kg，BigDecimal转number）
  expectIncome?: number;                   // 预期收益（元，BigDecimal转number）
  yieldCompare?: string;                   // 产量对比（如“超出5kg”/“不足3kg”）
  incomeCompare?: string;                  // 收益对比（如“超出200元”/“不足500元”）
}
/**
 * 种植数据展示VO（前端版，匹配后端PlantDataVO）
 */
export interface PlantDataVO {
  dataId: number;                          // 数据记录主键ID（后端Long → 前端number）
  planId: number;                          // 关联种植计划ID（后端Long → 前端number）
  recordTime: string;                      // 数据记录时间（格式：yyyy-MM-dd HH:mm:ss，后端LocalDateTime转字符串）
  envTemp?: number;                        // 环境温度（℃，后端BigDecimal → 前端number，可选）
  envHumidity?: number;                    // 环境湿度（%，后端BigDecimal → 前端number，可选）
  co2Concentration?: number;               // CO₂浓度（‰，后端BigDecimal → 前端number，可选）
  farmingOper?: string;                    // 农事操作（如浇水5L/㎡；灭菌2小时，可选）
  growthStatus?: number;                   // 生长状态：1=优 2=良 3=差（后端Integer → 前端number，可选）
  growthStatusName?: string;               // 生长状态名称（前端展示用，如“优”/“良”/“差”，可选）
  diseaseStatus?: string;                  // 病虫害情况（可选）
  inputCost?: number;                      // 当日投入成本（元，后端BigDecimal → 前端number，可选）
  recorderId: number;                      // 记录人ID（后端Long → 前端number）
  createTime: string;                      // 记录创建时间（格式：yyyy-MM-dd HH:mm:ss，后端LocalDateTime转字符串）
}
/**
 * 种植计划完整详情VO（前端版，包含节点/数据/成果，匹配后端PlantPlanDetailVO）
 */
export interface PlantPlanDetailVO {
  planInfo: PlantPlanVO;                   // 计划基础信息（主表数据）
  nodeList: PlantNodeVO[];                 // 该计划的所有节点列表（复用已定义的PlantNodeVO）
  dataPage: PageResult<PlantDataVO>;       // 该计划的日常种植数据（分页结果）
  resultInfo?: PlantResultVO | null;       // 该计划的采收成果（无则为null）
}
/**
 * 更新种植计划DTO（前端版，匹配后端PlantPlanUpdateDTO）
 */
export interface PlantPlanUpdateDTO {
  planId: number;                          // 种植计划主键ID（必填）
  planName?: string;                       // 计划名称（可选）
  fungusType?: string;                     // 种植菌型（可选）
  plantArea?: number;                      // 种植面积（亩/㎡，可选）
  startTime?: string;                      // 计划开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  endTime?: string;                        // 计划结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
  expectYield?: number;                    // 预期产量（kg，可选）
  expectIncome?: number;                   // 预期收益（元，可选）
  status?: PlantPlanStatusEnum |"";            // 计划状态：1=执行中 2=已完成 3=暂停 4=作废（可选）
  remark?: string;                         // 计划备注（可选）
}

/**
 * 种植节点分页查询DTO（前端版，匹配后端PlantNodePageQueryDTO）
 */
export interface PlantNodePageQueryDTO {
  pageNum: number;                         // 当前页码（默认1）
  pageSize: number;                        // 每页条数（默认10）
  planId?: number;                         // 关联种植计划ID
  nodeType?: PlantNodeTypeEnum |"";            // 节点类型（备料/灭菌/接种/发菌/出菇/采收）
  status?: PlantNodeStatusEnum |"";            // 节点状态：1=未完成 2=已完成 3=逾期未完成
}
/**
 * 科普内容类型枚举（匹配后端PopSciContentTypeEnum）
 */
export enum PopSciContentTypeEnum {
  ARTICLE = 'article', // 图文
  VIDEO = 'video'      // 视频
}

/**
 * 科普内容状态枚举（匹配后端PopSciContentStatusEnum）
 */
export enum PopSciContentStatusEnum {
  PENDING = 1,    // 待审核
  APPROVED = 2,   // 审核通过（可展示）
  REJECTED = 3    // 审核驳回
}

/**
 * 科普内容分页查询DTO（前端版，匹配后端PopSciContentPageQueryDTO）
 */
export interface PopSciContentPageQueryDTO {
  pageNum: number;
  pageSize: number;
  mushroomName?: string;
  title?: string;
  contentType?: PopSciContentTypeEnum;
  authorName?: string;
  contentStatus?: PopSciContentStatusEnum;
}
/**
 * 科普内容精简VO（推荐图文/视频列表展示专用，匹配后端PopSciContentSummaryVO）
 */
export interface PopSciContentSummaryVO {
  id: number; // 科普内容主键ID（用于跳转详情/传参）
  contentType: PopSciContentTypeEnum; // 内容类型：图文/视频
  theme: string; // 标题（后端title序列化后的值，核心）
  authorName: string; // 作者名字
  contentStatus: PopSciContentStatusEnum;
  publishDate: string; // 发布日期（后端格式化：yyyy-MM-dd）
  description?: string; // 内容描述（可选，可能为空）
  mushroomName: string; // 菌种名称（非数据库字段，补充展示）
  articleCoverUrl?: string; // 图文封面URL（仅图文类型有值）
  videoCoverUrl?: string; // 视频封面URL（仅视频类型有值）
}
/**
 * 科普内容VO（前端展示版，匹配后端PopSciContentVO）
 */
export interface PopSciContentVO {
  id: number;
  contentType: PopSciContentTypeEnum;
  theme: string; // 对应后端theme
  authorName: string;
  publishDate: string; // 后端LocalDate，前端转为字符串yyyy-MM-dd
  content?: string; // 图文正文（仅article类型有）
  videoUrl?: string; // 视频播放链接（仅video类型有）
  description?: string; // 内容描述
  contentStatus: PopSciContentStatusEnum;
  mushroomName?: string;
  auditTime?: string; // 审核时间 yyyy-MM-dd HH:mm:ss
  rejectReason?: string; // 驳回原因
  // ========== 新增图片相关字段（匹配后端返回） ==========
  pictureList: Picture[];               // 该科普内容关联的所有图片列表
  articleCoverUrl?: string;             // 图文封面图URL（快捷字段）
  videoCoverUrl?: string;               // 视频封面图URL（快捷字段）
  reviewCount?:number;
  praiseCount?:number;
  collectCount?:number;
}
export interface Picture {
  id: number
  picUrl: string           // 对应后端的 picUrl
  mushroomId?: number   // 关联的菌种ID
  contentId?: number    // 关联科普内容表主键ID，null表示菌种基础详情图
  type?: string         // 图片类型，后端是枚举，前端用string或定义枚举类型
  createTime?: string   // 格式化后的时间字符串，如 "yyyy-MM-dd HH:mm:ss"
  updateTime?: string   // 格式化后的时间字符串
}
// src/api/interface.ts 或新建 src/enums/picture.ts
/**
 * 删除图片DTO（和后端 PictureDTO 完全对应）
 */
export interface PictureDTO {
  /**
   * 图片ID（优先使用）
   */
  picId?: number;

  /**
   * 关联科普内容ID（删除该内容下的所有图片）
   */
  contentId?: number;

  /**
   * 关联菌种ID（删除该菌种下的所有图片）
   */
  mushroomId?: number;

  /**
   * 关联病虫害ID（删除该病虫害下的所有图片）
   */
  diseaseId?: number;

  picUrl?: string;
}
/**
 * 图片类型枚举（完美匹配后端 PictureTypeEnum）
 */
export enum PictureTypeEnum {
  /** 菌种基础详情图（无科普内容关联） */
  DETAIL = '详情图',
  
  /** 科普图文内容关联图 */
  ARTICLE_DETAIL = '图文详情图',
  
  /** 菌种封面图（核心展示图） */
  COVER = '菌种封面图',
  
  /** 科普视频封面图 */
  VIDEO_COVER = '视频封面',
  
  /** 栽培技术示意图 */
  CULTIVATION_SCHEMATIC = '栽培示意图',
  
  /** 病虫害封面 */
  DISEASE_PEST_COVER = '病虫害封面'
}

export interface MushroomVO {
  id: number
  mushroomName: string
  intro: string
  contentMorphology: string
  contentEnvironment: string
  contentCultivation: string
  otherDetail: string
  createTime: string // 格式化后的时间字符串，如 "yyyy-MM-dd HH:mm:ss"
  updateTime: string // 格式化后的时间字符串
  coverUrl: string
  detailPicUrls: Picture[]
}
/**
 * 菌种概要VO（后端MushroomSummaryVO，列表展示专用）
 */
export interface MushroomSummaryVO {
  /** 主键ID（跳转详情用，后端Long） */
  id: number;
  /** 食用菌种名称 */
  mushroomName: string;
  /** 菌种基本信息 */
  intro: string;
  /** 封面图（单张，列表核心展示） */
  coverUrl: string;
  /** 插入时间（后端@JsonFormat序列化后：yyyy-MM-dd HH:mm:ss） */
  createTime: string;
  /** 修改时间（后端@JsonFormat序列化后：yyyy-MM-dd HH:mm:ss） */
  updateTime: string;
}
// ========== 新增：类型谓词函数（核心，解决属性访问报错） ==========
/**
 * 判断是否为合法的病虫害详情VO
 * 类型谓词：告诉TS，若返回true，val就是DiseasePestVO类型
 */
export function isDiseasePestVO(val: unknown): val is DiseasePestVO {
  return (
    typeof val === 'object' && // 是对象
    val !== null && // 不是null（typeof null === 'object'，坑点）
    'id' in val && typeof (val as DiseasePestVO).id === 'number' && // 有数字类型的id
    'diseaseName' in val && typeof (val as DiseasePestVO).diseaseName === 'string' // 有字符串类型的diseaseName
  )
}

/**
 * 判断是否为合法的菌种详情VO
 * 类型谓词：告诉TS，若返回true，val就是MushroomVO类型
 */
export function isMushroomVO(val: unknown): val is MushroomVO {
  return (
    typeof val === 'object' &&
    val !== null &&
    'id' in val && typeof (val as MushroomVO).id === 'number' &&
    'mushroomName' in val && typeof (val as MushroomVO).mushroomName === 'string'
  )
}
/**
 * 病虫害概要VO（后端DiseasePestSummaryVO，列表展示专用，新增封面图）
 */
export interface DiseasePestSummaryVO {
  /** 病虫害主键ID（后端Long） */
  id: number;
  /** 病虫害名称 */
  diseaseName: string;
  /** 关联菌种列表 */
  mushroomList: MushroomVO[];
  /** 病虫害简介 */
  brief: string;
  /** 类型（1=病害，2=虫害，对应枚举DiseasePestTypeEnum） */
  itemType: DiseasePestTypeEnum;
  /** 封面图（新增：列表展示用） */
  coverUrl: string;
  /** 创建时间（后端@JsonFormat序列化后：yyyy-MM-dd HH:mm:ss） */
  createTime: string;
  /** 更新时间（后端@JsonFormat序列化后：yyyy-MM-dd HH:mm:ss） */
  updateTime: string;
}
export interface MushroomAiResult {
  mushroomName?: string;
  isPoison?: number;        // 0=无毒,1=有毒
  confidence?: number;      // 0~1
  toxicity?: string;        // "poisonous", "edible", "conditionally_edible"
}
export interface MushroomPageQueryDTO {
  pageNum: number
  pageSize: number
  mushroomName?: string
}
/**
 * 病虫害
 */
export enum DiseasePestTypeEnum {
  DISEASE = 'disease', // 病害
  PEST = 'pest',     // 虫害
}
export interface DiseasePestVO {
  id: number
  diseaseName: string
  brief: string
  symptom: string
  pathogen: string
  infection: string
  occurrenceRule: string
  prevention: string
  itemType: string
  createTime: string
  updateTime: string
  morphology: string
  habits: string
  coverUrl: string
  mushroomList: any[] // 你可以根据需要定义 MushroomInfo 类型
}

export interface DiseasePestPageQueryDTO {
  pageNum: number
  pageSize: number
  diseaseName?: string
  itemType?: number
}

export interface Comment {
    commentId: number
    username: string
    userAvatar: string | null
    content: string
    createTime: string
    likeCount: number
}

export interface SongDetail {
    songId: number
    songName: string
    artistName: string
    album: string
    lyric: string | null
    duration: string
    coverUrl: string
    audioUrl: string
    releaseTime: string
    likeStatus: boolean | null
    comments: Comment[]
}

/**
 * 科普内容发布/编辑DTO（前端版，匹配后端PopSciContentPublishDTO）
 */
export interface PopSciContentPublishDTO {
  id?: number;                          // 科普内容主键ID（修改时必填，新增时为空）
  mushroomId: number;                   // 关联菌种ID（必填）
  contentType: 'article' | 'video';     // 内容类型（article-图文/ video-视频，必填）
  title: string;                        // 标题（必填）
  authorId: number;                     // 关联科普号ID（必填，当前登录科普号的ID）
  publishDate?: string;                 // 发布日期（选填，默认填充当前日期）
  content?: string;                     // 图文正文（图文类型必填）
  videoUrl?: string;                    // 视频播放链接（视频类型必填）
  articleDetailPicUrl?: string[];         // 图文详情图URL（图文类型选填，编辑时替换用）
  videoCoverUrl?: string;               // 视频封面URL（视频类型选填，编辑时替换用）
  description?: string;                 // 内容描述（选填）
}
