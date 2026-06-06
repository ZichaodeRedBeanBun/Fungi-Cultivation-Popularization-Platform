import { reactive } from "vue";
import type { FormRules } from "element-plus";

export const formRules = reactive<FormRules>({
  diseaseName: [
    { required: true, message: "请输入病虫害名称", trigger: "blur" }
  ],
  itemType: [
    { required: true, message: "请选择类型", trigger: "change" }
  ]
  // 其他字段可根据需要添加规则
});
