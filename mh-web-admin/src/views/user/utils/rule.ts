import { reactive } from "vue";
import type { FormRules } from "element-plus";
import { isPhone, isEmail } from "@pureadmin/utils";
import type { FormItemRule } from "element-plus"; // 先导入类型
/** 自定义表单规则校验 */
/** 用户名正则（用户名格式应为4-16位字母、数字、下划线、连字符的任意组合） */
export const REGEXP_USERNAME = /^[a-zA-Z0-9_-]{4,16}$/;
// 仅保留无动态依赖的基础规则
export const baseRules: Record<string, FormItemRule[]> = {
  username: [
    { required: true, message: "用户名不能为空", trigger: "blur" }
  ],
  password: [
    { required: true, message: "密码不能为空", trigger: "blur" }
  ],
  userType: [
    { required: true, message: "请选择用户身份", trigger: "change" }
  ],
  email: [
    { required: true, message: "邮箱不能为空", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
  ],
  userId: [
    { required: true, message: "用户编号不能为空", trigger: "blur" }
  ]
};
/** 密码正则（密码格式应为8-18位数字、字母、符号的任意两种组合） */
export const REGEXP_PWD =
  /^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)]|[()])+$)(?!^.*[\u4E00-\u9FA5].*$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){8,18}$/;

// export const formRules = reactive(<FormRules>{
//   username: [
//     {
//       validator: (rule, value, callback) => {
//         if (value === "") {
//           callback(new Error("用户名为必填项"));
//         } else if (!REGEXP_USERNAME.test(value)) {
//           callback(new Error("4-16位字母、数字、下划线、连字符的任意组合"));
//         } else {
//           callback();
//         }
//       },
//       trigger: "blur"
//     }
//   ],
//   password: [
//     {
//       validator: (rule, value, callback) => {
//         if (value === "") {
//           callback(new Error("用户密码为必填项"));
//         } else if (!REGEXP_PWD.test(value)) {
//           callback(new Error("8-18位数字、字母、符号的任意两种组合"));
//         } else {
//           callback();
//         }
//       },
//       trigger: "blur"
//     }
//   ],
//   // phone: [
//   //   {
//   //     validator: (rule, value, callback) => {
//   //       if (value === "") {
//   //         callback();
//   //       } else if (!isPhone(value)) {
//   //         callback(new Error("请输入正确的手机号码格式"));
//   //       } else {
//   //         callback();
//   //       }
//   //     },
//   //     trigger: "blur"
//   //     // trigger: "click" // 如果想在点击确定按钮时触发这个校验，trigger 设置成 click 即可
//   //   }
//   // ],
//   userType: [
//     { required: true, message: "请选择用户身份", trigger: "change" },
//     { type: "number", min: 1, max: 3, message: "用户身份值错误（仅支持1/2/3）", trigger: "change" }
//   ],
//   email: [
//     {
//       validator: (rule, value, callback) => {
//         if (value === "") {
//           callback(new Error("邮箱为必填项"));
//         } else if (!isEmail(value)) {
//           callback(new Error("请输入正确的邮箱格式"));
//         } else {
//           callback();
//         }
//       },
//       trigger: "blur"
//     }
//   ],
//   certificateNum: [
//     {
//       validator: (rule: FormItemRule, value: string, callback: Function) => {
//         // ✅ 直接取组件内的表单数据（无类型报错）
//         const userType = newFormInline.value.userType;
//         // 仅当用户身份是2/3时，科普认证号码必填
//         if ((userType === 2 || userType === 3) && !value) {
//           callback(new Error("科普认证号码不能为空"));
//         } else {
//           callback(); // 验证通过，必须调用callback
//         }
//       },
//       trigger: "blur" // 失去焦点时触发验证
//     }
//   ],
//   // 科普单位验证规则
//   affiliateUnit: [
//     {
//       validator: (rule: FormItemRule, value: string, callback: Function) => {
//         const userType = newFormInline.value.userType;
//         if ((userType === 2 || userType === 3) && !value) {
//           callback(new Error("科普单位不能为空"));
//         } else {
//           callback();
//         }
//       },
//       trigger: "blur"
//     }
//   ]
// });
