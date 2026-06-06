package cn.edu.seig.MhWeb.util;

public class TypeConversionUtil {

    /**
     * 将 Object 转换为 Long，支持 Long 和 Integer 类型的转换。
     *
     * @param obj 要转换的对象
     * @return 转换后的 Long 值
     * @throws IllegalArgumentException 如果对象类型不支持转换
     */
    public static Long toLong(Object obj) {
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else {
            throw new IllegalArgumentException("转换失败，不支持类型：" + obj.getClass());
        }
    }
    /**
     * 将 Object 转换为 Integer，支持 Integer 和 Long 类型的转换（Long需在Integer取值范围内）。
     *
     * @param obj 要转换的对象（不可为null）
     * @return 转换后的 Integer 值
     * @throws IllegalArgumentException 如果对象类型不支持转换，或Long值超出Integer范围
     * @throws NullPointerException     如果对象为null
     */
    public static Integer toInteger(Object obj) {
        if (obj == null) {
            throw new NullPointerException("转换对象不能为空");
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Long) {
            Long longValue = (Long) obj;
            // 校验Long值是否在Integer范围内，避免溢出
            if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("Long值 " + longValue + " 超出Integer取值范围");
            }
            return longValue.intValue();
        } else {
            throw new IllegalArgumentException("转换失败，不支持类型：" + obj.getClass().getName());
        }
    }

}
