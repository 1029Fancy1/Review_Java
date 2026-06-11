package com.knowledgehub.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledgehub.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * 学习要点（Day 2）：
 * 1. 继承 BaseMapper<User> 后自动拥有 CRUD 方法：
 *    - insert(user)
 *    - deleteById(id)
 *    - updateById(user)
 *    - selectById(id)
 *    - selectList(wrapper)
 *    - selectPage(page, wrapper)
 * 2. 复杂查询可以用 XML 或 @Select 注解自定义
 * 3. @Mapper 注解也可以在启动类用 @MapperScan 统一扫描
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已提供基础 CRUD，复杂查询在此扩展
}
