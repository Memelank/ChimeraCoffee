package com.chimera.weapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxSendSubMsgResDTO {
    private int errcode;
    private String errmsg;
}
