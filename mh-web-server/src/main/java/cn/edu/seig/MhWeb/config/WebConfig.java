package cn.edu.seig.MhWeb.config;

import cn.edu.seig.MhWeb.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 *
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 注册自定义拦截器
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口和注册接口不拦截
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList"
                        ,"/user/search/**");
    }
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.clear(); // 清空默认异常解析器
    }
}
