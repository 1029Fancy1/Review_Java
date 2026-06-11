package com.knowledgehub.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户返回 VO
 *
 * 学习要点（Day 3）：
 * 1. VO (View Object) — 返回给前端的对象，和 Entity 隔离
 * 2. 为什么不用 Entity 直接返回？
 *    —— Entity 的 password 字段绝不能返回给前端（安全）
 *    —— VO 可以只返回前端需要的字段（id、username、email）
 * 3. DTO 进、VO 出：前端 → DTO → Service → Entity → DB → VO → 前端
 */
@Data
@Builder
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态：1=正常 0=禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
