import { reactive } from "vue";
import type { FormRules } from "element-plus";

/** 自定义表单规则校验 */
export const formRules = reactive(<FormRules>{
  rejectReason: [
    {
      validator: (rule, value, callback, source, options) => {
        // source 是整个表单数据
        if (source.contentStatus === "REJECTED" && (!value || !value.trim())) {
          callback(new Error("请填写驳回原因"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]

});
