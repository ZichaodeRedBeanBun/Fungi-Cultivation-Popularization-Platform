interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  formTitle: string; // 弹窗标题，如“新增”或“编辑”
  id: number;
  authorName: string;
  mushroomName: string;
  contentType: "ARTICLE" | "VIDEO";
  theme: string; // 标题字段，和后端VO的title对应
  publishDate: string; // 建议用string，和接口返回保持一致
  contentStatus: string; // 1: 待审核, 2: 审核通过, 3: 审核驳回
  description?: string;
  coverUrl?: string;
  /** 图文正文（可选，图文内容时使用） */
  content?: string;
  /** 视频播放链接（可选，视频内容时使用） */
  videoUrl?: string;
  /** 审核时间（可选） */
  auditTime?: string;
  /** 驳回原因（可选） */
  rejectReason?: string;
  // 你可以根据需要补充其他字段
}

interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
