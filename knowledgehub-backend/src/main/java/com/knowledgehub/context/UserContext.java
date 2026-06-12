package com.knowledgehub.context;

/**
 * 用户上下文 —— 基于 ThreadLocal 实现线程隔离
 *
 * 学习要点（Day 6 重点理解）：
 * 1. 为什么用 ThreadLocal？
 *    —— 每个 HTTP 请求在独立的线程中处理，ThreadLocal 保证不同请求的 userId 互不干扰
 * 2. 为什么要 remove()？
 *    —— Tomcat 使用线程池，线程会复用！上一个请求的 userId 残留到下一个请求 → 数据串了
 *    所以在请求结束后必须调用 remove() 清理
 * 3. 使用流程：
 *    LoginInterceptor.preHandle() → UserContext.setUserId(userId)
 *    Controller/Service 中 → UserContext.getUserId()
 *    afterCompletion() → UserContext.remove()
 *
 * 面试复盘：
 * - ThreadLocal 原理？ → 每个 Thread 内部维护一个 ThreadLocalMap，key 是 ThreadLocal 弱引用
 * - ThreadLocal 内存泄漏？ → key 是弱引用会被 GC，但 value 强引用还在 → 不 remove() 会泄漏
 *
 * 使用示例：
 * UserContext.setUserId(1L);
 * Long userId = UserContext.getUserId();  // 1
 * UserContext.remove();  // 清理
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void remove() {
        USER_ID.remove();
    }
}
