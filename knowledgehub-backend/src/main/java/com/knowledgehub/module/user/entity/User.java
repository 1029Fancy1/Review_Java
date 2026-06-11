package com.knowledgehub.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * 学习要点（Day 2）：
 * 1. @TableName：指定表名，支持蛇形/驼峰自动映射
 * 2. @TableId(type = IdType.AUTO)：数据库自增主键
 * 3. @TableLogic：逻辑删除，MyBatis-Plus 自动在 WHERE 后追加 deleted=0
 * 4. @TableField(fill = ...)：自动填充（可选，配合 MetaObjectHandler）
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    /** 状态：1=正常 0=禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    private Integer deleted;
}
