package com.knowledgehub.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回体
 *
 * 前端接收格式：
 * {
 *   "code": 200,
 *   "message": "ok",
 *   "data": {
 *     "total": 100,
 *     "page": 1,
 *     "size": 10,
 *     "list": [ ... ]
 *   }
 * }
 *
 * 使用方式：
 * PageResult<UserVO> page = PageResult.of(mybatisPlusPage.getTotal(), dto.getPage(), dto.getSize(), voList);
 * return Result.ok(page);
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer page;

    /** 每页大小 */
    private Integer size;

    /** 数据列表 */
    private List<T> list;

    public static <T> PageResult<T> of(Long total, Integer page, Integer size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }
}
