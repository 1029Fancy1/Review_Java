package com.knowledgehub.module.document.enums;

import lombok.Getter;

/**
 * 文档解析状态枚举
 *
 * 状态流转：
 * 待解析(0) → 解析中(1) → 解析成功(2)
 *                       → 解析失败(3)
 */
@Getter
public enum ParseStatusEnum {

    PENDING(0, "待解析"),
    PARSING(1, "解析中"),
    SUCCESS(2, "解析成功"),
    FAILED(3, "解析失败");

    private final Integer code;
    private final String desc;

    ParseStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举（可选工具方法）
     */
    public static ParseStatusEnum of(Integer code) {
        for (ParseStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
