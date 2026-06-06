interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  title: string;
  userId: number;
  username: string;
  password: string;
  // 2. 新增用户身份字段
  userType: number;
  email: string;
  certificateNum: string; // 新增：科普认证号码
  affiliateUnit: string;  // 新增：科普单位
  userStatus: number;
  introduction: string;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
