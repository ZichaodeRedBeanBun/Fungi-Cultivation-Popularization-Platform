export interface DiseasePestFormItemProps {
  formTitle: string; // 弹窗标题，如“新增”、“编辑”、“查看”
  id: number;
  diseaseName: string;       // 病虫害名称
  mushroomList: MushroomInfo[];
  mushroomIds: number[];
  brief: string;             // 简介
  symptom: string;           // 为害症状
  pathogen: string;          // 病原
  infection: string;         // 侵染
  occurrenceRule: string;    // 发生规律
  prevention: string;        // 防治
  itemType: string;          // 类型ID（1=病害，2=虫害）
  createTime?: string;       // 创建时间，字符串格式
  updateTime?: string;       // 更新时间，字符串格式
  morphology: string;        // 形态
  habits: string;            // 习性
}
export interface MushroomInfo {
  id: number;
  mushroomName: string;
  intro?: string;
  otherDetail?: string;
  contentMorphology?: string;
  contentEnvironment?: string;
  contentCultivation?: string;
  createTime?: string; // ISO时间字符串
  updateTime?: string;
}
export interface DiseasePestFormProps {
  formInline: DiseasePestFormItemProps;
}
