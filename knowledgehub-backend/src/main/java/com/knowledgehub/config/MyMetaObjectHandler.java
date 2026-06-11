package com.knowledgehub.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 学习要点：
 * 1. 配合 Entity 中的 @TableField(fill = ...) 使用
 * 2. insertFill：执行 insert 时自动填充
 * 3. updateFill：执行 update 时自动填充
 * 4. 不需要每次在 Service 里手动 setCreateTime/setUpdateTime
 *
 * 面试复盘：
 * - MetaObjectHandler 的原理？
 *   → MyBatis-Plus 在执行 SQL 前拦截，根据注解配置自动调用 fill 方法
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // INSERT 时自动填充 createTime 和 updateTime
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // UPDATE 时自动填充 updateTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
