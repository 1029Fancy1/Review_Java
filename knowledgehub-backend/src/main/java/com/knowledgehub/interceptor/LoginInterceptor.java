package com.knowledgehub.interceptor;

import com.knowledgehub.context.UserContext;
import com.knowledgehub.redis.TokenService;
import com.knowledgehub.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器 — Day 9 升级版
 *
 * 对比 Day 6：
 * - 不再从 X-User-Id 读明文（谁都能伪造）
 * - 改为从 Authorization Header 取 JWT → 解析 → 查 Redis 验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从 Header 取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录");
            return false;
        }
        String token = authHeader.substring(7);  // 去掉 "Bearer " 前缀

        // 2. JWT 解析出 userId
        Long userId;
        try {
            userId = jwtUtils.parseUserId(token);
        } catch (Exception e) {
            sendUnauthorized(response, "token 无效或已过期");
            return false;
        }

        // 3. 查 Redis 确认 token 有效（支持主动踢下线）
        Long cachedUserId = tokenService.getUserId(token);
        if (cachedUserId == null || !cachedUserId.equals(userId)) {
            sendUnauthorized(response, "token 已失效");
            return false;
        }

        // 4. 写入 ThreadLocal
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        UserContext.remove();
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
