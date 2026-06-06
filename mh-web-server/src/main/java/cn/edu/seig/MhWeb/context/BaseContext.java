package cn.edu.seig.MhWeb.context;

import java.util.Map;

// 确保 BaseContext 是全局唯一的 ThreadLocal 工具类
public class BaseContext {
    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> CLAIMS_THREAD_LOCAL = new ThreadLocal<>();

    // 设置用户ID
    public static void setCurrentId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    // 获取用户ID
    public static Long getCurrentId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    // 设置完整Claims（可选）
    public static void setClaims(Map<String, Object> claims) {
        CLAIMS_THREAD_LOCAL.set(claims);
    }

    // 清空ThreadLocal（关键：防止内存泄漏）
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
        CLAIMS_THREAD_LOCAL.remove();
    }
}