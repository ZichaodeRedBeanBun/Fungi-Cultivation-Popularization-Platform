package cn.edu.seig.MhWeb.interceptor;


import cn.edu.seig.MhWeb.config.RolePermissionManager;
import cn.edu.seig.MhWeb.constant.JwtClaimsConstant;
import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.constant.PathConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.enumeration.RoleEnum;
import cn.edu.seig.MhWeb.util.JwtUtil;
import cn.edu.seig.MhWeb.util.ThreadLocalUtil;
import cn.edu.seig.MhWeb.util.TypeConversionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.edu.seig.MhWeb.constant.JwtClaimsConstant.ADMIN_TOKEN;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RolePermissionManager rolePermissionManager;

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 CORS 预检请求（OPTIONS 方法）直接通过
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }
        // **新增：打印实际收到的 Content-Type**
        String contentType = request.getContentType();
        log.info("实际收到的 Content-Type: {}", contentType);
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }
        // 2. Token为空，直接放行（未登录场景，由业务层处理）
        if (token == null || token.isEmpty()) {
            return true;
        }
        // 3. 解析Token并提取用户ID（仅USER角色）
        try {
            Map<String, Object> tokenMap = JwtUtil.parseToken(token);
            String role = (String) tokenMap.get(JwtClaimsConstant.ROLE);
            if (role != null && role.equals(RoleEnum.USER.getRole())) {
                Object userIdObj = tokenMap.get(JwtClaimsConstant.USER_ID);
                Long userId = TypeConversionUtil.toLong(userIdObj);
                if (userId != null) {
                    // 将用户ID存入ThreadLocal（BaseContext）
                    BaseContext.setCurrentId(userId);
                    log.debug("JWT拦截器：用户ID {} 已存入BaseContext", userId);
                }
            }
        } catch (Exception e) {
            log.error("JWT拦截器解析Token失败：{}", e.getMessage(), e);
            // 解析失败不拦截请求，仅不存入用户ID，由业务层处理未登录逻辑
        }
        String path = request.getRequestURI();
        log.info("前端传递的URI：{}", path);

        // 获取 Spring 的 PathMatcher 实例
        PathMatcher pathMatcher = new AntPathMatcher();

        // 定义允许访问的路径
        List<String> allowedPaths = Arrays.asList(
                PathConstant.PLAYLIST_DETAIL_PATH,
                PathConstant.ARTIST_DETAIL_PATH,
                PathConstant.SONG_LIST_PATH,
                PathConstant.SONG_DETAIL_PATH,
                "/user/search/**"
        );

        // 检查路径是否匹配
        boolean isAllowedPath = allowedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (token == null || token.isEmpty()) {
            if (isAllowedPath) {
                return true; // 允许未登录用户访问这些路径
            }

            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN); // 缺少令牌
            return false;
        }

        try {
            // Redis验证（判断令牌是否已登出/失效）
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            // 1. 如果是管理员接口（/admin/**），用固定Key查Redis（满足你的诉求）
            String redisToken;
            if (path.startsWith("/admin/")) {
                redisToken = operations.get(ADMIN_TOKEN);
                log.info("管理员接口，Redis固定Key({})的值：{}", ADMIN_TOKEN, redisToken);

                // 管理员场景：对比Redis固定Key的值 和 前端纯Token
                if (redisToken == null || !redisToken.equals(token)) {
                    log.error("管理员Token不匹配！Redis存的：{}，前端传的：{}", redisToken, token);
                    throw new RuntimeException("管理员Token失效");
                }
            }
            // 2. 普通用户接口，用Token本身做Key查Redis（兼容多用户）
            else {
                redisToken = operations.get(token);
                log.info("普通用户接口，Redis Token({})的值：{}", token, redisToken);
                if (redisToken == null) throw new RuntimeException("普通用户Token失效");
            }
//            String redisToken = operations.get(token);
//            if (redisToken == null) {
//                // token失效
//                throw new RuntimeException();
//            }
//            JWT解析 验证令牌签名 提取用户数据
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String role = (String) claims.get(JwtClaimsConstant.ROLE);
            String requestURI = request.getRequestURI();// 当前请求的接口路径
            log.info("解析Token得到角色：{}，请求接口路径：{}", role, requestURI);
//            角色权限验证
            if (rolePermissionManager.hasPermission(role, requestURI)) {
                // 统一设置BaseContext
                Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
                if (userIdObj != null) {
                    Long userId = TypeConversionUtil.toLong(userIdObj);
                    BaseContext.setCurrentId(userId);  // 设置用户ID到BaseContext
                }
                ThreadLocalUtil.set(claims);
                return true;
            } else {
                sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION); // 无权限访问
                return false;
            }
        } catch (Exception e) {
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED); // 令牌无效
            return false;
        }
    }
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
