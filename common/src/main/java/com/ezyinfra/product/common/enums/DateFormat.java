package com.ezyinfra.product.common.enums;

import lombok.Getter;

@Getter
public enum DateFormat {

    DMY("ddMMyyyy"),
    DMY_DASH("dd-MM-yyyy"),
    DMY_SLASH("dd/MM/yyyy"),
    MDY_DASH("MM-dd-yyyy"),
    MDY_SLASH("MM/dd/yyyy"),
    YMD_DASH("yyyy-MM-dd"),
    YMDHM_DASH("yyyy-MM-dd HH:mm"),
    YMDHMS_DASH("yyyy-MM-dd HH:mm.SSS"),
    YMD_SLASH("yyyy/MM/dd");

    private final String format;

    DateFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return format;
    }
}
