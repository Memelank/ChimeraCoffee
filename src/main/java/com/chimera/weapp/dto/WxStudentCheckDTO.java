package com.chimera.weapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxStudentCheckDTO {
    private int errcode;
    private String errmsg;
    private int bind_status;
    private boolean is_student;
}
