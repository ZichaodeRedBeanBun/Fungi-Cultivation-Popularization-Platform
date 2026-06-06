interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  title: string;

  /** 科普号ID */
  userId?: number;

  /** 科普号名称 */
  username: string;



  /** 科普号类型：2=待认证，3=已认证 */
  userType: number | null;

  certificateNum: string;


  /** 所属单位 */
  affiliateUnit: string;
}

interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };

