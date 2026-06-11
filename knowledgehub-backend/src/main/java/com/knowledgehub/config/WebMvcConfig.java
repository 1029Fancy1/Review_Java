package com.knowledgehub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * 学习要点：
 * 1. WebMvcConfigurer 是 Spring Boot 提供的 MVC 配置接口
 * 2. addCorsMappings：配置跨域
 * 3. addInterceptors：注册拦截器（后续 Day 6 添加 LoginInterceptor）
 * 4. addResourceHandlers：配置静态资源映射
 *
 * TODO: Day 9 时在此类中注册 LoginInterceptor
 *
 * 参考实现：
 * @Autowired
 * private LoginInterceptor loginInterceptor;
 *
 * @Override
 * public void addInterceptors(InterceptorRegistry registry) {
 *     registry.addInterceptor(loginInterceptor)
 *             .addPathPatterns("/api/**")
 *             .excludePathPatterns(
 *                 "/api/user/login",
 *                 "/api/user/register",
 *                 "/api/user/captcha",
 *                 "/doc.html",
 *                 "/v3/api-docs/**",
 *                 "/swagger-resources/**"
 *             );
 * }
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     *
     * 为什么需要跨域？
     * 前端（localhost:5173）和后端（localhost:8080）不同端口，
     * 浏览器同源策略会阻止跨域请求。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射
     * 让 Knife4j 的静态资源可以被访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
