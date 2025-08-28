package com.zmbdp.chat.service.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author 稚名不带撇
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    MESSAGE_TEXT(0, "文本"),

    MESSAGE_PIC(1, "图片"),

    MESSAGE_MP3(2, "音频"),

    MESSAGE_MP4(3, "视频"),

    MESSAGE_CARD(4, "卡片")

    ;

    private final Integer code;

    private final String info;

    public static MessageTypeEnum getByCode(int code) {
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if (messageTypeEnum.getCode() == code) {
                return messageTypeEnum;
            }
        }
        return null;
    }
}
