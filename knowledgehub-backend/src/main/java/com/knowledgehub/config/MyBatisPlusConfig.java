package com.knowledgehub.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * 学习要点（Day 2 重点理解）：
 * 1. MyBatis-Plus 的分页插件本质是一个 MyBatis 拦截器
 * 2. PaginationInnerInterceptor 在 SQL 执行前拦截，自动追加 LIMIT 语句
 * 3. 支持的数据库类型：MYSQL、POSTGRE_SQL、ORACLE 等
 *
 * 面试复盘：
 * - MyBatis-Plus 分页插件原理？
 *   → 拦截 Executor.query()，解析原始 SQL，根据数据库方言拼接分页 SQL
 * - 为什么需要指定 DbType？
 *   → 不同数据库分页语法不同（MySQL: LIMIT，PostgreSQL: LIMIT/OFFSET）
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器
     **/
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // TODO: 手敲实现分页插件注册
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        paginationInnerInterceptor.setMaxLimit(50L);
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}
