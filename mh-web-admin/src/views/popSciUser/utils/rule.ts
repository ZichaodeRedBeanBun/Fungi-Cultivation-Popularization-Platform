// import { reactive } from "vue";
// import type { FormRules } from "element-plus";

// /** 自定义表单规则校验 */
// export const formRules = reactive(<FormRules>{
//   artistName: [
//     {
//       validator: (rule, value, callback) => {
//         if (value === "") {
//           callback(new Error("科普号名称为必填项"));
//         } else {
//           callback();
//         }
//       },
//       trigger: "blur"
//     }
//   ]
// });
import { reactive } from "vue";
import type { FormRules } from "element-plus";

/** 自定义表单规则校验 */
export const formRules = reactive(<FormRules>{
  username: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.trim() === "") {
          callback(new Error("科普号名称为必填项"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  userType: [
    {
      required: true,
      message: "请选择科普号类型",
      trigger: "change"
    }
  ]
});
