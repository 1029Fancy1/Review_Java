package com.knowledgehub.interceptor;

import com.knowledgehub.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器
 *
 * 学习要点（Day 6 重点理解）：
 * 1. HandlerInterceptor 三个回调：
 *    - preHandle()：Controller 方法执行前调用，返回 false 则阻断请求
 *    - postHandle()：Controller 方法执行后、视图渲染前调用
 *    - afterCompletion()：整个请求结束后调用（无论是否异常），适合资源清理
 * 2. 执行顺序：
 *    请求 → Filter → Interceptor.preHandle() → Controller → Interceptor.postHandle()
 *    → 视图渲染 → Interceptor.afterCompletion() → Filter → 响应
 * 3. ThreadLocal 的完整生命周期：
 *    preHandle() → setUserId() → Controller 使用 getUserId() → afterCompletion() → remove()
 *
 * - Interceptor vs Filter？
 *   → Filter 是 Servlet 规范，Interceptor 是 Spring 的
 *   → Filter 在 DispatcherServlet 之前，Interceptor 在之后
 *   → Filter 能拦截所有请求，Interceptor 只能拦截到 Spring Controller
 * - 为什么在 afterCompletion 中 remove？
 *   → 保证即使 Controller 抛异常，ThreadLocal 也会被清理
 *
 * Day 6：当前版本为测试框架，暂时不做真实鉴权（Day 9 接入 Redis token 校验）
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            UserContext.setUserId(Long.valueOf(userIdHeader));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        UserContext.remove();
    }
}
