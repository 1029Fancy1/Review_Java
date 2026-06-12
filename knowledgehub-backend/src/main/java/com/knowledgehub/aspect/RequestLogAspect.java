package com.knowledgehub.aspect;

import com.knowledgehub.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 请求日志切面 —— 使用 AOP 环绕通知记录每个接口的入参、耗时、用户信息
 *
 * 学习要点（Day 6 重点理解）：
 * 1. AOP 原理：Spring 在运行时动态生成代理对象，在方法调用前后插入切面逻辑
 * 2. @Around vs @Before/@After：环绕通知同时控制方法执行前后，还能决定是否执行原方法
 * 3. @Pointcut 定义切入点，execution 表达式指定拦截哪些方法
 * 4. ProceedingJoinPoint.proceed() 调用原方法，不调它原方法就不会执行
 *
 * 面试复盘：
 * - JDK 动态代理 vs CGLIB？
 *   → JDK：基于接口，要求目标类实现接口
 *   → CGLIB：基于继承，生成目标类的子类
 *   → Spring Boot 默认 CGLIB（因为 Controller 一般没实现接口）
 * - @Around 和 @Before/@After 的区别？
 *   → @Around 可以修改入参、修改返回值、决定是否执行原方法
 *   → @Before/@After 只能做额外操作，不能控制原方法
 */
@Slf4j
@Aspect
@Component
public class RequestLogAspect {

    /**
     * 切入点：拦截 module 包下所有 Controller 的 public 方法
     *
     * 表达式含义：
     * execution(* com.knowledgehub.module.*.controller.*.*(..))
     *          ↑                 ↑        ↑       ↑ ↑  ↑
     *          |                 |        |       | |  └─ 任意参数
     *          |                 |        |       | └──── 任意方法名
     *          |                 |        |       └────── controller 包
     *          |                 |        └────────────── 任意子模块（user,kb,document...）
     *          |                 └─────────────────────── module 包
     *          └───────────────────────────────────────── 任意返回值类型
     */
    @Pointcut("execution(* com.knowledgehub.module.*.controller.*.*(..))")
    public void controllerPointcut() {
    }

    
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //环绕通知
        // 1. 获取当前请求信息
        ServletRequestAttributes attributes = 
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 2. 获取请求参数
        String uri = request.getRequestURI();
        String method = request.getMethod();
        Long userId = UserContext.getUserId();
        String args = Arrays.toString(joinPoint.getArgs());
        log.info("[请求开始] {} {} | userId = {}｜args = {}", method, uri, userId, args);
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;
        log.info("[请求结束] {} {} | userId = {}| 耗时={}ms", method, uri, userId, cost);
        return result;

    }
}
