package cn.edu.seig.MhWeb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import org.springframework.util.AntPathMatcher;
/**
 * 角色权限管理器
 * 编写RolePathPermissionsConfig配置类的实现类
 */
@Component
@Slf4j
public class RolePermissionManager {

    private final RolePathPermissionsConfig rolePathPermissionsConfig;

    @Autowired
    public RolePermissionManager(RolePathPermissionsConfig rolePathPermissionsConfig) {
        this.rolePathPermissionsConfig = rolePathPermissionsConfig;
    }
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean hasPermission(String role, String requestURI) {
        if (role == null || role.isEmpty() || requestURI == null || requestURI.isEmpty()) {
            log.warn("角色或请求路径为空，角色：{}，请求路径：{}", role, requestURI);
            return false;
        }
        Map<String, List<String>> permissions = rolePathPermissionsConfig.getPermissions();
        List<String> allowedPaths = permissions.get(role);
        if (allowedPaths != null) {
            for (String path : allowedPaths) {
                boolean match = pathMatcher.match(path, requestURI);
                log.info("角色{}，请求路径{}，匹配权限路径{}：{}", role, requestURI, path, match);
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }
}

