interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  formTitle: string;
  mushroomId?: number;
  mushroomName: string;
  intro: string; // 基本信息（替换简介）
  morphologicalCharacteristics: string; // 形态特征
  environmentalRequirements: string; // 环境要求
  cultivationTechnology: string; // 栽培技术
  // detailPicUrls?: string[]; // 详情图URL列表（回显用）
  otherDetail: string; // 其它信息（替换简介）
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
