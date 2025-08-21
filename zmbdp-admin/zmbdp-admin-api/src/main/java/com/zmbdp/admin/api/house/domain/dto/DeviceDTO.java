package com.zmbdp.admin.api.house.domain.dto;

import lombok.Data;

/**
 * 设备 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class DeviceDTO {
    /**
     * 设备码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;
}